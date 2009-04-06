/*****************************************************************
 JADE - Java Agent DEvelopment Framework is a framework to develop 
 multi-agent systems in compliance with the FIPA specifications.
 Copyright (C) 2000 CSELT S.p.A. 
 
 GNU Lesser General Public License
 
 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation, 
 version 2.1 of the License. 
 
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.
 
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the
 Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 Boston, MA  02111-1307, USA.
 *****************************************************************/

package jade.core;

//#J2ME_EXCLUDE_FILE
//#APIDOC_EXCLUDE_FILE

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.FIPAAgentManagement.Envelope;
import jade.domain.FIPAAgentManagement.InternalError;
import jade.domain.JADEAgentManagement.JADEManagementOntology;
import jade.util.leap.List;
import jade.util.leap.ArrayList;
import jade.util.leap.Map;
import jade.util.leap.HashMap;
import jade.util.leap.Iterator;
import jade.util.leap.Properties;
import jade.security.*;

import jade.core.messaging.*;

import jade.util.Logger;

import java.lang.reflect.Method;
import java.util.StringTokenizer;
import java.util.Enumeration;
import java.util.Vector;

/**
 @author Giovanni Caire - TILAB
 @author Jerome Picault - Motorola Labs
 */

public class BackEndContainer extends AgentContainerImpl implements BackEnd {
	
	public static final String USE_BACKEND_MANAGER = "jade_core_BackEndContainer_usebemanager";
	public static final String RESYNCH = "jade_core_BackEndContainer_resynch";
	public static final String BE_REPLICAS_SIZE = "be-replicas-size";
	public static final Long REPLICA_CHECK_DELAY = new Long(5000); // new Long(5*60*1000); // 5 Minutes
	
	
	private static final String ADDR_LIST_DELIMITERS = ", \n\t\r";
	
	// This flag is used to prevent two parallel shut-down processes and
	// also to be sure that threads possibly started by this BEContainer
	// do not survive after the BEContainer shutdown. 
	private boolean terminating = false;
	
	// The FrontEnd this BackEndContainer is connected to
	private FrontEnd myFrontEnd;
	
	// The manager of the connection with the FrontEnd
	private BEConnectionManager myConnectionManager;
	
	private BackEndManager theBEManager;
	
	private Map agentImages = new HashMap(1);
	
	private Map serviceBECodecs = null; // Lazy initialization
	
	private String[] replicasAddresses;
	
	private Map principals = new HashMap(1);
	
	// The original properties passed to this container when it was created
	private Properties creationProperties;
	
	private Logger myLogger = Logger.getMyLogger(getClass().getName());
	
	private static Properties adjustProperties(Properties pp) {
		// A BackEndContainer is never a Main
		pp.setProperty(Profile.MAIN, "false");
		
		// Set default additional services if not already set
		if (pp.getProperty(Profile.SERVICES) == null) {
			pp.setProperty(Profile.SERVICES, "jade.core.event.NotificationService");
		}
		
		// Merge back-end and front-end services
		String feServices = pp.getProperty(MicroRuntime.BE_REQUIRED_SERVICES_KEY);
		if (feServices != null) {
			Vector ss = Specifier.parseList(pp.getProperty(Profile.SERVICES), ';');
			Vector fess = Specifier.parseList(feServices, ';');
			for (int i = 0; i < fess.size(); ++i) {
				String s = (String) fess.get(i);
				if (!ss.contains(s)) {
					ss.add(s);
				}
			}
			pp.setProperty(Profile.SERVICES, Specifier.encodeList(ss, Specifier.SPECIFIER_SEPARATOR));
		}
		return pp;
	}
	
	public BackEndContainer(Properties props, BEConnectionManager cm) throws ProfileException {
		// Do not call the parent constructor to avoid creating a big LADT
		myProfile = new ProfileImpl(adjustProperties(props));
		localAgents = new LADT(1);
		
		creationProperties = props;
		myConnectionManager = cm;
	}
	
	
	public boolean connect() {
		try {
			// Initialize the BackEndManager if required
			if (myProfile.getBooleanProperty(USE_BACKEND_MANAGER, false)) {
				theBEManager = initBEManager();
			}
			
			String beAddrs = myProfile.getParameter(FrontEnd.REMOTE_BACK_END_ADDRESSES, null);
			if(beAddrs != null) {
				replicasAddresses = parseAddressList(beAddrs);
				myProfile.setParameter(BE_REPLICAS_SIZE, Integer.toString(replicasAddresses.length));
			}
			
			Vector agentSpecs = Specifier.parseSpecifierList(myProfile.getParameter(Profile.AGENTS, null));
			myProfile.setParameter(Profile.AGENTS, null);
			
			myFrontEnd = myConnectionManager.getFrontEnd(this, null);
			myLogger.log(Logger.FINE, "BackEnd container "+myProfile.getParameter(Profile.CONTAINER_NAME, null)+" joining the platform ...");
			Runtime.instance().beginContainer();
			boolean connected = joinPlatform();
			if (connected) {
				myLogger.log(Logger.FINE, "Join platform OK");
				AID amsAID  = getAMS();
				myProfile.setParameter(Profile.PLATFORM_ID, amsAID.getHap());
				String[] addresses = amsAID.getAddressesArray();
				if(addresses != null){
					StringBuffer sb = new StringBuffer();
					for(int i = 0; i <addresses.length; i++){
						sb.append(addresses[i]);
						if(i<addresses.length-1){
							sb.append(';');
						}
					}
					myProfile.setParameter(MicroRuntime.PLATFORM_ADDRESSES_KEY, sb.toString());
				}
				if ("true".equals(myProfile.getParameter(RESYNCH, "false"))) {
					myLogger.log(Logger.INFO, "BackEnd container "+ myProfile.getParameter(Profile.CONTAINER_NAME, null)+" re-synching ...");
					resynch();
				}
				else {
					//Notify the main container about bootstrap agents on the FE.
					for(int i = 0; i< agentSpecs.size(); i++){
						Specifier sp = (Specifier)agentSpecs.elementAt(i);
						try{
							String name = bornAgent(sp.getName());
							sp.setClassName(name);
							sp.setArgs(null);
						}catch(Exception e){
							myLogger.log(Logger.SEVERE,"Error creating agent "  + sp.getName() + ". " + e);
							sp.setClassName(e.getClass().getName());
							sp.setArgs(new Object[]{e.getMessage()});
						}
					}
					myProfile.setParameter(Profile.AGENTS, Specifier.encodeSpecifierList(agentSpecs));
				}
			}
			return connected;
		}
		catch (Exception e) {
			// Should never happen 
			e.printStackTrace();
			return false;
		}
	}
	
	
	protected void startNode() throws IMTPException, ProfileException, ServiceException, JADESecurityException, NotFoundException {
		// Initialize all services (without activating them)
		List services = new ArrayList();
		ServiceDescriptor dsc = startService("jade.core.management.BEAgentManagementService", false);
		dsc.setMandatory(true);
		services.add(dsc);
		dsc = startService("jade.core.messaging.MessagingService", false);
		dsc.setMandatory(true);
		services.add(dsc);
		
		List l = myProfile.getSpecifiers(Profile.SERVICES);
		myProfile.setSpecifiers(Profile.SERVICES, l); // Avoid parsing services twice
		Iterator serviceSpecifiers = l.iterator();
		while(serviceSpecifiers.hasNext()) {
			Specifier s = (Specifier) serviceSpecifiers.next();
			String serviceClass = s.getClassName();
			boolean isMandatory = false;
			if ( s.getArgs() != null ) {
				isMandatory = CaseInsensitiveString.equalsIgnoreCase( (String) s.getArgs()[0], "true" );
			}
			try {
				dsc = startService(serviceClass, false);
				dsc.setMandatory(isMandatory);
				services.add(dsc);
			} 
			catch (ServiceException se) {
				if (isMandatory) {
					throw se;
				}
				else {
					myLogger.log(Logger.WARNING,"Exception initializing service " + serviceClass + " : " + se.toString());
					se.printStackTrace();
				}
			}
		}
		
		// Register with the platform
		ServiceDescriptor[] descriptors = new ServiceDescriptor[services.size()];
		for (int i = 0; i < descriptors.length; ++i) {
			descriptors[i] = (ServiceDescriptor) services.get(i);
		}
		// Actually join the platform (this call can modify the name of this container)
		if (theBEManager != null) {
			myNodeDescriptor.setParentNode(theBEManager.getNode());
		}
		getServiceManager().addNode(myNodeDescriptor, descriptors);
		if (theBEManager != null) {
			theBEManager.register(myNodeDescriptor);
		}
		
		// Boot all services
		for (int i = 0; i < descriptors.length; ++i) {
			
			ServiceDescriptor currentServDesc = descriptors[i];
			try {
				currentServDesc.getService().boot(myProfile);
			} 
			catch(Throwable t) {
				if ( currentServDesc.isMandatory() ) {
					throw new ServiceException("An error occurred during service booting", t);
				}
				else {
					myLogger.log(Logger.WARNING,"Exception booting service " + currentServDesc.getName() + " : " + t.toString());
					t.printStackTrace();
				}
			}
		}
	}
	
	
	/////////////////////////////////////
	// BackEnd interface implementation
	/////////////////////////////////////
	
	/**
	 A new agent has just started on the FrontEnd.
	 Adjust the agent name taking into account wild-cards.
	 Issue an INFORM_CREATED vertical command.
	 @return the actual name of the agent
	 */
	public String bornAgent(String name) throws JADESecurityException, IMTPException {
		name = JADEManagementOntology.adjustAgentName(name, new String[]{getID().getName()});
		AID id = new AID(name, AID.ISLOCALNAME);
		GenericCommand cmd = new GenericCommand(jade.core.management.AgentManagementSlice.INFORM_CREATED, jade.core.management.AgentManagementSlice.NAME, null);
		cmd.addParam(id);
		
		Object ret = myCommandProcessor.processOutgoing(cmd);
		if (ret instanceof NameClashException) {
			throw new JADESecurityException(((NameClashException) ret).getMessage());
		}
		else if (ret instanceof JADESecurityException) {
			throw (JADESecurityException) ret;
		}
		else if (ret instanceof IMTPException) {
			throw (IMTPException) ret;
		}
		else if (ret instanceof Throwable) {
			throw new IMTPException(null, (Exception) ret);
		}
		return name;
	}
	
	/**
	 An agent has just died on the FrontEnd.
	 Remove its image and notify the Main
	 */
	public void deadAgent(String name) throws IMTPException {
		AID id = new AID(name, AID.ISLOCALNAME);
		handleEnd(id);
	}
	
	/**
	 */
	public void suspendedAgent(String name) throws NotFoundException, IMTPException {
		AID id = new AID(name, AID.ISLOCALNAME);
		handleChangedAgentState(id, Agent.AP_ACTIVE, Agent.AP_SUSPENDED);
	}
	
	/**
	 */
	public void resumedAgent(String name) throws NotFoundException, IMTPException {
		AID id = new AID(name, AID.ISLOCALNAME);
		handleChangedAgentState(id, Agent.AP_SUSPENDED, Agent.AP_ACTIVE);
	}
	
	/**
	 An agent on the FrontEnd has sent a message.
	 Note that the NotFoundException here is referred to the sender and
	 indicates an inconsistency between the FrontEnd and the BackEnd
	 */
	public void messageOut(ACLMessage msg, String sender) throws NotFoundException, IMTPException {
		// Check whether the sender exists
		AID id = new AID(sender, AID.ISLOCALNAME);
		
		synchronized (frontEndSynchLock) {
			AgentImage image = getAgentImage(id);
			if (image == null) {
				if (synchronizing) {
					// The image is not yet there since the front-end is synchronizing.
					// Buffer the message. It will be delivered as soon as the 
					// FrontEnd synchronization process completes
					postponeAfterFrontEndSynch(msg, sender);
					return;
				}
				else {
					throw new NotFoundException("No image for agent "+sender+" on the BackEndContainer");
				}
			}
		}
		
		handleSend(msg, id, false);
	}
	
	public Object serviceInvokation(String actor, String serviceName, String methodName, Object[] methodParams) throws NotFoundException, ServiceException, IMTPException {
		AID id = new AID(actor, AID.ISLOCALNAME);
		AgentImage image = getAgentImage(id);
		ServiceHelper helper = image.getHelper(serviceName);
		if (helper == null) {
			throw new ServiceException("Service "+serviceName+"does not have a Service-helper");
		}
		BECodec codec = getBECodec(serviceName);
		Object[] decodedParams = codec.decodeParams(methodName, methodParams);
		try {
			Method m = null;
			Method[] mm = helper.getClass().getMethods();
			for (int i = 0; i < mm.length; ++i) {
				if (mm[i].getName().equals(methodName)) {
					if (isCompatible(mm[i], decodedParams)) {
						m = mm[i];
						break;
					}
				}
			}
			if (m != null) {
				Object result = m.invoke(helper, decodedParams);
				return codec.encodeResult(methodName, result);
			}
			else {
				throw new ServiceException("No valid "+methodName+" method found in service helper");
			}
		}
		catch (ServiceException se) {
			throw se;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException("Unexpected error, ", e);
		}
	}
	
	private boolean isCompatible(Method method, Object[] decodedParams) {
		// FIXME: To be implemented
		return true;
	}

	private BECodec getBECodec(String serviceName) {
		if (serviceBECodecs == null) {
			serviceBECodecs = new HashMap(2);
		}
		BECodec codec = (BECodec) serviceBECodecs.get(serviceName);
		if (codec == null) {
			try {
				codec = (BECodec) Class.forName(serviceName+"BECodec").newInstance();	
			}
			catch (Exception e) {
				// The service does not have a BECodec --> Use a dummy one
				codec = new BECodec() {
					public Object[] decodeParams(String methodName, Object[] methodParams) {
						return methodParams;
					}
					public Object encodeResult(String methodName, Object result) {
						return result;
					}
					
				};
			}
			serviceBECodecs.put(serviceName, codec);
		}
		return codec;
	}

	///////////////////////////////////////////////
	// Methods called by the BEManagementService
	///////////////////////////////////////////////
	public void createAgentOnFE(String name, String className, String[] args) throws IMTPException {
		if(!isMaster()) {
			throw new IMTPException("This is not the active back-end replica.");
		}
		
		myFrontEnd.createAgent(name, className, args);
	}
	
	public void killAgentOnFE(String name) throws IMTPException, NotFoundException {
		if(!isMaster()) {
			throw new IMTPException("This is not the active back-end replica.");
		}
		
		try {
			myFrontEnd.killAgent(name);
		}
		catch (PostponedException pe) {
			// Hide the delivery delay to the rest of the platform
			deadAgent(name);
		}
	}
	
	public void suspendAgentOnFE(String name) throws IMTPException, NotFoundException {
		if(!isMaster()) {
			throw new IMTPException("This is not the active back-end replica.");
		}
		
		try {
			myFrontEnd.suspendAgent(name);
		}
		catch (PostponedException pe) {
			// Hide the delivery delay to the rest of the platform
			suspendedAgent(name);
		}
	}
	
	public void resumeAgentOnFE(String name) throws IMTPException, NotFoundException {
		if(!isMaster()) {
			throw new IMTPException("This is not the active back-end replica.");
		}
		
		try {
			myFrontEnd.resumeAgent(name);
		}
		catch (PostponedException pe) {
			// Hide the delivery delay to the rest of the platform
			resumedAgent(name);
		}
	}
	
	
	/////////////////////////////////////////////////////
	// Redefined methods of the AgentContainer interface
	/////////////////////////////////////////////////////
	/**
	 Dispatch a message to an agent in the FrontEnd.
	 If this method is called by a thread that is serving a message 
	 sent by an agent in the FrontEnd too, nothing is done as the
	 dispatch has already taken place in the FrontEnd (see messageOut()).
	 */
	public boolean postMessageToLocalAgent(ACLMessage msg, AID receiverID) {
		
		// Try first in the LADT
		boolean found = super.postMessageToLocalAgent(msg, receiverID);
		if(found) {
			return found;
		}
		else {
			// The receiver must be in the FrontEnd
			AgentImage image = (AgentImage) agentImages.get(receiverID);
			if(image != null) {
				if (agentImages.containsKey(msg.getSender())) {
					// The message was sent by an agent living in the FrontEnd. The
					// receiverID (living in the FrontEnd too) has already received
					// the message.
					// FIXME: This does not take into account that an agent not living 
					// in the FrontEnd may send a message on behalf of an agent living 
					// in the FrontEnd. 
					return true;
				}
				
				try {
					// Forward the message to the FrontEnd
					if(isMaster()) {
						myFrontEnd.messageIn(msg, receiverID.getLocalName());
						handlePosted(receiverID, msg);
						return true;
					}
					else {
						System.out.println("WARNING: Trying to deliver a message through a replica");
						return false;
					}
				}
				catch(NotFoundException nfe) {
					System.out.println("WARNING: Missing agent in FrontEnd");
					return false;
				}
				catch(IMTPException imtpe) {
					System.out.println("WARNING: Can't deliver message to FrontEnd");
					return false;
				}	      
			}
			else {
				// Agent not found
				System.out.println("WARNING: Agent "+receiverID+" not found on BackEnd container");
				return false;
			}
		}
	}
	
	public Agent acquireLocalAgent(AID id) {
		Agent ag = super.acquireLocalAgent(id);
		if (ag == null) {
			// The agent is not in the LADT. Likely it is in the FE --> Acquire its image
			// FIXME: The image is not "acquired". Likely we have to use a LADT 
			// also for agent images
			ag = (Agent) agentImages.get(id);
		}
		return ag;
	}
	
	public void releaseLocalAgent(AID id) {
		// If the aquired agent was an image there is nothing to release (see FIXME above)
		super.releaseLocalAgent(id);
	}
	
	public AID[] agentNames() {
		AID[] realAgents = super.agentNames();
		AID[] images = getAgentImages();
		AID[] all = new AID[realAgents.length + images.length];
		for (int i = 0; i < realAgents.length; ++i) {
			all[i] = realAgents[i];
		}
		for (int i = 0; i < images.length; ++i) {
			all[i + realAgents.length] = images[i];
		}
		return all;
	}
	
	/**
	 This method is re-defined to avoid NullPointerException. In fact
	 a search in the LADT would be done for the agent to be debugged, but
	 the LADT is obviously empty.
	 */
	public void enableDebugger(AID debuggerName, AID toBeDebugged) throws IMTPException {
		throw new IMTPException("Unsupported operation");
	}
	
	/**
	 This method is re-defined to avoid NullPointerException. In fact
	 a search in the LADT would be done for the agent to be debugged, but
	 the LADT is obviously empty.
	 */
	public void disableDebugger(AID debuggerName, AID notToBeDebugged) throws IMTPException {
		throw new IMTPException("Unsupported operation");
	}
	
	/**
	 */
	public void shutDown() {
		// Avoid two parallel shut-downs
		synchronized(this) {
			if (terminating) {
				return;
			}
			else {
				terminating = true;
			}
		}
		
		// Stop monitoring replicas, if active
		stopReplicaMonitor();
		
		// Forward the exit command to the FrontEnd only if this is the master replica
		try {
			if(isMaster()) {
				myFrontEnd.exit(false);
			}
		}
		catch (IMTPException imtpe) {
			// The FrontEnd is disconnected. Force the shutdown of the connection
			myConnectionManager.shutdown();
		}
		
		// "Kill" all agent images
		killAgentImages();
		
		if (theBEManager != null) {
			theBEManager.deregister(myNodeDescriptor);
		}
		super.shutDown();
	}
	
	private void killAgentImages() {
		AID[] ids = getAgentImages();
		for (int i = 0; i < ids.length; ++i) {
			handleEnd(ids[i]);
		}
		
		if (agentImages.size() > 0) {
			myLogger.log(Logger.WARNING, "# "+agentImages.size()+" zombie agent images found.");
			agentImages.clear();
		}
	}
	
	
	//////////////////////////////////////////////////////////
	// Methods related to the back-end replication mechanism
	//////////////////////////////////////////////////////////
	public void activateReplicas() {
		creationProperties.setProperty(Profile.BE_BASE_NAME, getID().getName());
		Properties newProps = (Properties)creationProperties.clone();
		newProps.setProperty(Profile.MASTER_NODE_NAME, getID().getName());
		if(replicasAddresses != null) {
			for(int i = 0; i < replicasAddresses.length; i++) {
				try {
					newProps.setProperty(Profile.CONTAINER_NAME, getID().getName() + "-Replica-" + (i + 1));
					newProps.setProperty(Profile.BE_REPLICA_INDEX, Integer.toString(i + 1));
					myConnectionManager.activateReplica(replicasAddresses[i], newProps);
				}
				catch(IMTPException imtpe) {
					System.out.println("--- Replica activation failed [" + replicasAddresses[i] + "] ---");
				}
			}
		}
	}
	
	public void restartReplica(int index) throws IMTPException {
		Properties newProps = (Properties)creationProperties.clone();
		
		String baseName = creationProperties.getProperty(Profile.BE_BASE_NAME);
		String masterNodeName = getMasterName();
		if(masterNodeName == null) {
			masterNodeName = getID().getName();
		}
		
		// Set the master node property anyway
		newProps.setProperty(Profile.MASTER_NODE_NAME, masterNodeName);
		
		if(index == 0) {
			// Original master replica, at array index zero
			String replicaZeroAddr = creationProperties.getProperty(Profile.BE_REPLICA_ZERO_ADDRESS);
			newProps.setProperty(Profile.CONTAINER_NAME, baseName);
			newProps.setProperty(Profile.BE_REPLICA_INDEX, "0");
			
			myConnectionManager.activateReplica(replicaZeroAddr, newProps);
		}
		else {
			// One of the other replicas
			newProps.setProperty(Profile.CONTAINER_NAME, baseName + "-Replica-" + index);
			newProps.setProperty(Profile.BE_REPLICA_INDEX, Integer.toString(index));
			
			myConnectionManager.activateReplica(replicasAddresses[index - 1], newProps);
		}
	}
	
	public void becomeMaster() {
		
		// Do nothing if already a master back-end container
		if(isMaster()) {
			return;
		}
		
		GenericCommand cmd1 = new GenericCommand(jade.core.replication.BEReplicationSlice.BECOME_MASTER, jade.core.replication.BEReplicationSlice.NAME, null);
		myCommandProcessor.processOutgoing(cmd1);
		
		// Make all agent images known to the rest of the platform
		AID[] imgs = getAgentImages();
		for(int i = 0; i < imgs.length; i++) {
			String name = imgs[i].getLocalName();
			try {
				bornAgent(name);
			}
			catch(Exception e) {
				// Ignore it and try the next agent...
				e.printStackTrace();
			}
		}
		
	}
	
	public boolean isMaster() {
		GenericCommand cmd = new GenericCommand(jade.core.replication.BEReplicationSlice.IS_MASTER, jade.core.replication.BEReplicationSlice.NAME, null);
		myCommandProcessor.processOutgoing(cmd);
		Object result = cmd.getReturnValue();
		if (result instanceof Boolean) {
			return ((Boolean)result).booleanValue();
		}
		else if (result == null) { 
			// The replication service is not installed --> behave as if it were a master
			return true;
		}
		else {
			// Some exception was thrown
			return false;
		}
	}
	
	public String getMasterName() {
		GenericCommand cmd = new GenericCommand(jade.core.replication.BEReplicationSlice.GET_MASTER_NAME, jade.core.replication.BEReplicationSlice.NAME, null);
		myCommandProcessor.processOutgoing(cmd);
		Object result = cmd.getReturnValue();
		if(result instanceof String) {
			return (String)result;
		}
		else {
			return null;
		}
	}
	
	private void stopReplicaMonitor() {
		GenericCommand cmd = new GenericCommand(jade.core.replication.BEReplicationSlice.STOP_MONITOR, jade.core.replication.BEReplicationSlice.NAME, null);
		myCommandProcessor.processOutgoing(cmd);
	}
	
	
	private String[] parseAddressList(String toParse) {
		
		StringTokenizer lexer = new StringTokenizer(toParse, ADDR_LIST_DELIMITERS);
		List addresses = new ArrayList();
		while(lexer.hasMoreTokens()) {
			String tok = lexer.nextToken();
			addresses.add(tok);
		}
		
		Object[] objs = addresses.toArray();
		String[] result = new String[objs.length];
		for(int i = 0; i < result.length; i++) {
			result[i] = (String)objs[i];
		}
		
		return result;
		
	}
	
	private BackEndManager initBEManager() {
		try {
			return BackEndManager.getInstance(null);
		}
		catch (Exception e) {
			myLogger.log(Logger.WARNING, "Cannot retrieve BackEndManager. "+e);
			e.printStackTrace();
		}
		return null;
	}
	
	
	/**
	 Inner class AgentImage
	 */
	public class AgentImage extends Agent {
		private AgentImage(AID id) {
			super(id);
			setToolkit(BackEndContainer.this);
		}
	}
	
	// Factory method for the inner class
	public AgentImage createAgentImage(AID id) {
		return new AgentImage(id);
	}
	
	public AgentImage addAgentImage(AID id, AgentImage img) {
		return (AgentImage)agentImages.put(id, img);
	}
	
	public AgentImage removeAgentImage(AID id) {
		AgentImage img = (AgentImage)agentImages.remove(id);
		// If there are messages that were waiting to be delivered to the 
		// real agent on the FrontEnd, notify failure to sender
		removePendingMessages(MessageTemplate.MatchReceiver(new AID[]{id}), true);
		return img;
	}
	
	public AgentImage getAgentImage(AID id) {
		return (AgentImage)agentImages.get(id);
	}
	
	public AID[] getAgentImages() {
		Object[] objs = agentImages.keySet().toArray();
		AID[] result = new AID[objs.length];
		for(int i = 0; i < result.length; i++) {
			result[i] = (AID)objs[i];
		}
		
		return result;
	}
	
	
	public List removePendingMessages(MessageTemplate template, boolean notifyFailure) {
		List pendingMsg = ((jade.imtp.leap.FrontEndStub) myFrontEnd).removePendingMessages(template);
		if (pendingMsg.size() > 0) {
			myLogger.log(Logger.INFO, "Removed "+pendingMsg.size()+" pending messages from BackEnd queue.");
		}
		if (notifyFailure) {
			Iterator it = pendingMsg.iterator();
			while (it.hasNext()) {
				try {
					Object[] removed = (Object[]) it.next();
					ACLMessage msg = (ACLMessage) removed[0];
					AID receiver = new AID((String) removed[1], AID.ISLOCALNAME);						
					ServiceFinder myFinder = getServiceFinder();
					MessagingService msgSvc = (MessagingService) myFinder.findService(MessagingSlice.NAME);
					msgSvc.notifyFailureToSender(new GenericMessage(msg), receiver, new InternalError("Agent dead"));  
				}
				catch (Exception e) {
					myLogger.log(Logger.WARNING, "Cannot send AMS FAILURE. "+e);
				}
			}
		}
		return pendingMsg;
	}
	
	
	////////////////////////////////////////////////////////////
	// Methods and variables related to the front-end synchronization 
	// mechanism that allows a FrontEnd to re-join the platform after 
	// his BackEnd got lost (e.g. because of a crash of the hosting 
	// container).
	//
	// - The BackEnd waits for the input connection to be ready
	//   and then asks the FrontEnd to synchronize.
	// - In the meanwhile some messages could arrive from the 
	//   FrontEnd and the sender may not have an image in the BackEnd 
	//   yet -->
	// - While synchronizing outgoing messages are bufferd and 
	//   actually sent as soon as the synchronization process completes
	////////////////////////////////////////////////////////////
	
	// Flag indicating that the front-end synchronization process is in place
	private boolean synchronizing = false;  
	private Object frontEndSynchLock = new Object();
	private List fronEndSynchBuffer = new ArrayList();
	
	/**
	 Start the front-end synchronization process.
	 NOTE that when this method is called it may not be possible
	 to send commands to the FE. If a bi-connection dispatcher
	 is used in fact, the INP connection is not yet ready at this time.
	 However we can't just send a SYNCH command and rely on the 
	 store-and-forward mechanism of the FrontEndStub since we need to know 
	 when the SYNCH command is completely served. --> We start a separated
	 Thread that tries sending the SYNCH command until success.
	 */
	private void resynch() {
		synchronizing = true;
		Thread synchronizer = new Thread() {
			public void run() {
				while (!terminating) {
					try {
						myFrontEnd.synch();
						notifySynchronized();
						myLogger.log(Logger.INFO, "Resynch completed");
						break;
					}
					catch (IMTPException imtpe) {
						// Since the synchronization process will be repeated, be
						// sure we start from a clean situation
						//killAgentImages();
						
						// The input connection is down again (or there was an IMTP
						// error resynching). Go back waiting
						if (myLogger.isLoggable(Logger.FINE)) {
							myLogger.log(Logger.FINE, "Can't issue SYNCH command to FE. Wait a bit and retry...");
						}
						try {Thread.sleep(1000);} catch (Exception e) {}
					}
				}
			}
		};
		synchronizer.start();
	}
	
	private void postponeAfterFrontEndSynch(ACLMessage msg, String sender) {
		// No need for synchronization since this is called within a synchronized block
		fronEndSynchBuffer.add(new MessageSenderPair(msg, sender));
	}
	
	private void notifySynchronized() {
		synchronized (frontEndSynchLock) {
			Iterator it = fronEndSynchBuffer.iterator();
			while (it.hasNext()) {
				try {
					MessageSenderPair msp = (MessageSenderPair) it.next();
					messageOut(msp.getMessage(), msp.getSender());
				}
				catch (NotFoundException nfe) {
					// The sender does not exist --> nothing to notify
					nfe.printStackTrace();
				}
				catch (IMTPException imtpe) {
					// Should never happen since this is a local call
					imtpe.printStackTrace();
				}
			}
			fronEndSynchBuffer.clear();
			synchronizing = false;
		}
	}  		
	
	/** 
	 Inner class MessageSenderPair
	 */
	private class MessageSenderPair {
		private ACLMessage msg;
		private String sender;
		
		private MessageSenderPair(ACLMessage msg, String sender) {
			this.msg = msg;
			this.sender = sender;
		}
		
		private ACLMessage getMessage() {
			return msg;
		}
		
		private String getSender() {
			return sender;
		}
	} // END of inner class MessageSenderPair
}

