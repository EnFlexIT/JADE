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
import jade.domain.FIPAAgentManagement.Envelope;
import jade.domain.FIPAAgentManagement.InternalError;
import jade.util.Logger;
import jade.util.leap.List;
import jade.util.leap.ArrayList;
import jade.util.leap.Map;
import jade.util.leap.HashMap;
import jade.util.leap.Iterator;
import jade.util.leap.Properties;
import jade.security.*;

import java.util.StringTokenizer;
import java.util.Enumeration;

/**
@author Giovanni Caire - TILAB
*/

public class BackEndContainer extends AgentContainerImpl implements BackEnd {
	private static final String BE_PROPERTIES_FILE = "backends.properties";
	
    public static final String BE_REPLICAS_SIZE = "be-replicas-size";
    public static final Long REPLICA_CHECK_DELAY = new Long(5000); // new Long(5*60*1000); // 5 Minutes


    private static final String OUTGOING_NAME = "out";
    private static final String ADDR_LIST_DELIMITERS = ", \n\t\r";

    private long outCnt = 0;

    // The FrontEnd this BackEndContainer is connected to
    private FrontEnd myFrontEnd;

    // The manager of the connection with the FrontEnd
    private BEConnectionManager myConnectionManager;

    private CommandProcessor myCommandProcessor;

    private Map agentImages = new HashMap();
    private boolean refreshPlatformInfo = true;

    private String[] replicasAddresses;

    // The original properties passed to this container when it was created
    private Properties creationProperties;

    public BackEndContainer(Properties props, BEConnectionManager cm) throws ProfileException {
	super(new ProfileImpl(props));
	creationProperties = props;
	
	// Set default additional services
	props.setProperty(Profile.SERVICES, "jade.core.event.NotificationService");
	
	// Read the BackEnd configuration properties
	Properties beProps = new Properties();
	try {
		beProps.load(BE_PROPERTIES_FILE);
		Enumeration e = beProps.propertyNames();
		while (e.hasMoreElements()) {
			String key = (String) e.nextElement();
			props.setProperty(key, beProps.getProperty(key));
		}
	}
	catch (Exception e) {
		// Ignore and keep defaults
		e.printStackTrace();
	}
		
	myConnectionManager = cm;

	try {

	    myCommandProcessor = myProfile.getCommandProcessor();

	    String beAddrs = myProfile.getParameter(FrontEnd.REMOTE_BACK_END_ADDRESSES, null);
	    if(beAddrs != null) {
		replicasAddresses = parseAddressList(beAddrs);
		myProfile.setParameter(BE_REPLICAS_SIZE, Integer.toString(replicasAddresses.length));
	    }

	    myFrontEnd = cm.getFrontEnd(this, null);
	    Runtime.instance().beginContainer();
	    joinPlatform();
	}
	catch (IMTPException imtpe) {
	    // Should never happen
	    imtpe.printStackTrace();
	}
    }


      protected void startBasicServices() throws IMTPException, ProfileException, ServiceException, AuthException, NotFoundException {
	  // Create the agent management service
	  jade.core.management.BEAgentManagementService agentManagement = new jade.core.management.BEAgentManagementService();
	  agentManagement.init(this, myProfile);

	  // Create the messaging service
	  jade.core.messaging.MessagingService messaging = new jade.core.messaging.MessagingService();

	  messaging.init(this, myProfile);

	  ServiceDescriptor[] baseServices = new ServiceDescriptor[] {
	      new ServiceDescriptor(agentManagement.getName(), agentManagement),
	      new ServiceDescriptor(messaging.getName(), messaging)
	  };

	  // Register with the platform and activate all the container fundamental services
	  // This call can modify the name of this container
	  getServiceManager().addNode(getNodeDescriptor(), baseServices);

	  // Install all ACL Codecs and MTPs specified in the Profile
	  messaging.boot(myProfile);

	  ((BaseService)agentManagement).setCommandProcessor(myCommandProcessor);
	  ((BaseService)messaging).setCommandProcessor(myCommandProcessor);

      }


    /////////////////////////////////////
    // BackEnd interface implementation
    /////////////////////////////////////


  /**
     A new agent has just started on the FrontEnd.
     - Create an image for the new agent and set its CertificateFolder
     unless there is already a pending image (see createAgent()).
     - Notify the Main
     - Return the platform info to the FrontEnd if required
  */
  public String[] bornAgent(String name) throws IMTPException {
      AID id = new AID(name, AID.ISLOCALNAME);
      GenericCommand cmd = new GenericCommand(jade.core.management.AgentManagementSlice.INFORM_CREATED, jade.core.management.AgentManagementSlice.NAME, null);
      cmd.addParam(id);

      myCommandProcessor.processOutgoing(cmd);

      // Prepare platform info to return if necessary
      String[] info = null;
      if (refreshPlatformInfo) {
	  AID ams = getAMS();
	  String[] addresses = ams.getAddressesArray();
	  info = new String[2+addresses.length];
	  info[0] = getID().getName();
	  info[1] = ams.getHap();
	  for (int i = 0; i < addresses.length; ++i) {
	      info[i+2] = addresses[i];
	  }
	  refreshPlatformInfo = false;
      }

      return info;
  }

  /**
     An agent has just died on the FrontEnd.
     Remove its image and notify the Main
  */
  public void deadAgent(String name) throws IMTPException {
      AID id = new AID(name, AID.ISLOCALNAME);
      GenericCommand cmd = new GenericCommand(jade.core.management.AgentManagementSlice.INFORM_KILLED, jade.core.management.AgentManagementSlice.NAME, null);
      cmd.addParam(id);
      myCommandProcessor.processOutgoing(cmd);
  }

  /**
	 */
  public void suspendedAgent(String name) throws NotFoundException, IMTPException {
  	System.out.println("BackEndContainer.suspendedAgent() not yet implemented");
  	// FIXME: to be implemented
  }
  
  /**
	 */
  public void resumedAgent(String name) throws NotFoundException, IMTPException {
  	System.out.println("BackEndContainer.resumedAgent() not yet implemented");
  	// FIXME: to be implemented
  }
  
  /**
     An agent on the FrontEnd has sent a message.
     Note that the NotFoundException here is referred to the sender and
     indicates an inconsistency between the FrontEnd and the BackEnd
	 */
    public void messageOut(final ACLMessage msg, String sender) throws NotFoundException, IMTPException {

	// Check whether the sender exists
  	final AID id = new AID(sender, AID.ISLOCALNAME);

  	AgentImage image = (AgentImage) agentImages.get(id);
  	if (image == null) {
	    throw new NotFoundException("No image for agent "+sender+" on the BackEndContainer");
  	}
  	
	try {

	    // An AuthException will be thrown if the sender does not have
	    // - the permission to send a message on behalf of msg.getSender()
	    // - the permission to send a message to one of the receivers
	    getAuthority().doAsPrivileged(new PrivilegedExceptionAction() {
		    public Object run() throws AuthException {
			handleSend(msg, id);
			return null;
		    }
		}, image.getCertificateFolder());
	}
	catch (AuthException e) {
	    // FIXME: This will probably disappear as all the AuthExecptions
	    // should be handled within the "unicastPostMessage loop" inside
	    // handleSend()
	    System.out.println("AuthException: "+e.getMessage() );
	} 
	catch (Exception e) {
	    // Should never happen
	    e.printStackTrace();
	}
    }

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

	myFrontEnd.killAgent(name);
	deadAgent(name);
    }

    public void suspendAgentOnFE(String name) throws IMTPException, NotFoundException {
	if(!isMaster()) {
	    throw new IMTPException("This is not the active back-end replica.");
	}

	myFrontEnd.suspendAgent(name);
    }

    public void resumeAgentOnFE(String name) throws IMTPException, NotFoundException {
	if(!isMaster()) {
	    throw new IMTPException("This is not the active back-end replica.");
	}

	myFrontEnd.resumeAgent(name);
    }

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

	      // FIXME: The right way to do things should be i) check permission
	      // ii) call messageIn() iii) notify listeners. On the other hand 
	      // handlePosted() currently does i) and iii). 
	      try {
				  final ACLMessage msgFinal = msg;
				  final AID receiverIDFinal = receiverID;
		
				  // An AuthException will be thrown if the receiver does not have
				  // the permission to receive messages from the sender of this message
				  getAuthority().doAsPrivileged(new PrivilegedExceptionAction() {
					  public Object run() throws AuthException {
					      handlePosted(receiverIDFinal, msgFinal);
					      return null;
					  }
		      }, image.getCertificateFolder());
	      }
	      catch (AuthException ae) {
				  String errorMsg = new String("\"Agent "+receiverID.getName()+" not authorized to receive messages from agent "+msg.getSender().getName());
				  System.out.println(errorMsg+". "+ae.getMessage());
				  notifyFailureToSender(msg, receiverID, new InternalError(errorMsg));
	      }
	      catch (Exception e) {
				  // Should never happen
				  e.printStackTrace();
	      }
	      try {
				  // Forward the message to the FrontEnd
		
				  if(isMaster()) {
			      myFrontEnd.messageIn(msg, receiverID.getLocalName());
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

  /**
   */
  public void changeAgentPrincipal(AID agentID, CertificateFolder certs) throws IMTPException, NotFoundException {
      throw new IMTPException("Unsupported operation");
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

  public CertificateFolder createCertificateFolder(AID agentID) throws AuthException {
      return super.createCertificateFolder(agentID);
  }

  /**
   */
  public void shutDown() {
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
      AID[] ids = getAgentImages();
      for (int i = 0; i < ids.length; ++i) {
	  handleEnd(ids[i]);
      }

      if (agentImages.size() > 0) {
      	System.out.println("AAAAAAAAAAAAA");
      }
      //agentImages.clear();
		
      super.shutDown();
  }

    private void stopReplicaMonitor() {
	GenericCommand cmd = new GenericCommand(jade.core.replication.BEReplicationSlice.STOP_MONITOR, jade.core.replication.BEReplicationSlice.NAME, null);
	myCommandProcessor.processOutgoing(cmd);
    }

    /**
     */
    private void notifyFailureToSender(ACLMessage msg, AID receiver, InternalError ie) {

	// If the message was sent by an agent living on the FrontEnd, the
	// FAILURE has to be notified only if the receiver does not live
	// on the FrontEnd too. In this case in fact the message has
	// been delivered even if we have an exception. 
	if (Thread.currentThread().getName().startsWith(OUTGOING_NAME)) {
	    if (agentImages.get(receiver) == null) {
		Thread.currentThread().setName("dummy");
	    }
	}

	//if (the sender is not the AMS and the performative is not FAILURE)
	if ( (msg.getSender()==null) || ((msg.getSender().equals(getAMS())) && (msg.getPerformative()==ACLMessage.FAILURE))) // sanity check to avoid infinte loops
	    return;
	// else send back a failure message
	final ACLMessage failure = msg.createReply();
	failure.setPerformative(ACLMessage.FAILURE);
	//System.err.println(failure.toString());
	final AID theAMS = getAMS();
	failure.setSender(theAMS);

	// FIXME: the content is not completely correct, but that should
	// also avoid creating wrong content
	// FIXME: the content should include the indication about the 
	// receiver to wich dispatching failed.
	String content = "( (action " + msg.getSender().toString();
	content = content + " ACLMessage ) " + ie.getMessage() + ")";
	failure.setContent(content);

	try {
	    Authority authority = getAuthority();
	    authority.doPrivileged(new PrivilegedExceptionAction() {
		    public Object run() {
			try {
			    handleSend(failure, theAMS);
			} catch (AuthException ae) {
			    // it does not have permission to notify the failure 
			    // it never happens if the policy file gives 
			    // enough permission to the jade.jar 
			    System.out.println( ae.getMessage() );
			}
			return null; // nothing to return
		    }
		});
	} catch(Exception e) {
	    // should be never thrown
	    e.printStackTrace();
	}
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
	List pendingMsg = ((jade.imtp.leap.FrontEndStub) myFrontEnd).getPendingMessages(id);
	Iterator it = pendingMsg.iterator();
	while (it.hasNext()) {
		try {
			ACLMessage msg = (ACLMessage) it.next();
			handleSend(msg, msg.getSender());
    }
    catch (Exception e) {
    	// This should never happen
    	e.printStackTrace();
    }
	}
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

    public void becomeMaster() {

	// Do nothing if already a master back-end container
	if(isMaster()) {
	    return;
	}

	GenericCommand cmd1 = new GenericCommand(jade.core.replication.BEReplicationSlice.BECOME_MASTER, jade.core.replication.BEReplicationSlice.NAME, null);
	myCommandProcessor.processOutgoing(cmd1);

	GenericCommand cmd2 = new GenericCommand(jade.core.replication.BEReplicationSlice.START_MONITOR, jade.core.replication.BEReplicationSlice.NAME, null);
	cmd2.addParam(REPLICA_CHECK_DELAY);
	myCommandProcessor.processOutgoing(cmd2);


	// Make all agent images known to the rest of the platform
	AID[] imgs = getAgentImages();
	for(int i = 0; i < imgs.length; i++) {
	    String name = imgs[i].getLocalName();
	    try {
		bornAgent(name);
	    }
	    catch(IMTPException imtpe) {
		// Ignore it and try the next agent...
		imtpe.printStackTrace();
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

}

