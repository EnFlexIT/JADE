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
import jade.util.leap.List;
import jade.util.leap.ArrayList;
import jade.util.leap.Map;
import jade.util.leap.HashMap;
import jade.util.leap.Iterator;
import jade.util.leap.Properties;
import jade.security.*;

import jade.core.messaging.*;

import java.util.StringTokenizer;
import java.util.Enumeration;

/**
   @author Giovanni Caire - TILAB
   @author Jerome Picault - Motorola Labs
*/

public class BackEndContainer extends AgentContainerImpl implements BackEnd {
	
    public static final String RESYNCH = "resynch";
    public static final String BE_REPLICAS_SIZE = "be-replicas-size";
    public static final Long REPLICA_CHECK_DELAY = new Long(5000); // new Long(5*60*1000); // 5 Minutes


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

    private Map principals = new HashMap();

    // The original properties passed to this container when it was created
    private Properties creationProperties;

    private static Properties adjustProperties(Properties pp) {
			// A BackEndContainer is never a Main
    	pp.setProperty(Profile.MAIN, "false");
    	
			// Set default additional services if not already set
			if (pp.getProperty(Profile.SERVICES) == null) {
				pp.setProperty(Profile.SERVICES, "jade.core.event.NotificationService");
			}
			return pp;
    }
    	
    public BackEndContainer(Properties props, BEConnectionManager cm) throws ProfileException {
			super(new ProfileImpl(adjustProperties(props)));
			creationProperties = props;
			myConnectionManager = cm;
    }
	
    public boolean connect() {
			try {
		    myCommandProcessor = myProfile.getCommandProcessor();
	
		    String beAddrs = myProfile.getParameter(FrontEnd.REMOTE_BACK_END_ADDRESSES, null);
		    if(beAddrs != null) {
					replicasAddresses = parseAddressList(beAddrs);
					myProfile.setParameter(BE_REPLICAS_SIZE, Integer.toString(replicasAddresses.length));
		    }
	
		    myFrontEnd = myConnectionManager.getFrontEnd(this, null);
		    Runtime.instance().beginContainer();
		    boolean connected = joinPlatform();
		    if (connected) {
		    	if ("true".equals(myProfile.getParameter(RESYNCH, "false"))) {
		    		resynch();
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
	  // Start all the container fundamental services (without activating them)
  	List basicServices = new ArrayList();
	  ServiceDescriptor dsc = startService("jade.core.management.BEAgentManagementService", false);
	  basicServices.add(dsc);
	  dsc = startService("jade.core.messaging.MessagingService", false);
	  basicServices.add(dsc);
    List l = myProfile.getSpecifiers(Profile.SERVICES);
    myProfile.setSpecifiers(Profile.SERVICES, l); // Avoid parsing services twice
    Iterator serviceSpecifiers = l.iterator();
    while(serviceSpecifiers.hasNext()) {
		  Specifier s = (Specifier) serviceSpecifiers.next();
		  String serviceClass = s.getClassName();
		  if (serviceClass.equals("jade.core.security.SecurityService")) {
		  	l.remove(s);
		  	dsc = startService("jade.core.security.SecurityService", false);
			  basicServices.add(dsc);
			  break;
		  }
    }
	
    // Register with the platform 
    ServiceDescriptor[] descriptors = new ServiceDescriptor[basicServices.size()];
    for (int i = 0; i < descriptors.length; ++i) {
    	descriptors[i] = (ServiceDescriptor) basicServices.get(i);
    }
	  // This call can modify the name of this container
	  getServiceManager().addNode(getNodeDescriptor(), descriptors);

	  // Boot all basic services
    for (int i = 0; i < descriptors.length; ++i) {
    	descriptors[i].getService().boot(myProfile);
    }
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
  public String[] bornAgent(String name) throws JADESecurityException, IMTPException {
      AID id = new AID(name, AID.ISLOCALNAME);
      GenericCommand cmd = new GenericCommand(jade.core.management.AgentManagementSlice.INFORM_CREATED, jade.core.management.AgentManagementSlice.NAME, null);
      cmd.addParam(id);

      Object ret = myCommandProcessor.processOutgoing(cmd);
      if (ret instanceof NameClashException) {
      	throw new JADESecurityException("Name already in use");
      }
      else if (ret instanceof JADESecurityException) {
      	throw (JADESecurityException) ret;
      }
      else if (ret instanceof Exception) {
      	throw new IMTPException(null, (Exception) ret);
      }

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

  	AgentImage image = null;
  	synchronized (frontEndSynchLock) {
	  	image = (AgentImage) agentImages.get(id);
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
  	
		handleSend(msg, id);
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

  // This flag is used only to prevent two successive shut-down processes. 
  private boolean terminating = false;
  /**
   */
  public void shutDown() {
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
      AID[] ids = getAgentImages();
      for (int i = 0; i < ids.length; ++i) {
	  handleEnd(ids[i]);
      }

      if (agentImages.size() > 0) {
      	System.out.println("WARNING: Zombie agent images found");
      }
      agentImages.clear();
		
      super.shutDown();
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
			ServiceFinder myFinder = getServiceFinder();
			MessagingService msgSvc = (MessagingService) myFinder.findService(MessagingSlice.NAME);
			msgSvc.notifyFailureToSender(new GenericMessage(msg), id, new InternalError("Agent dead"));  
    }
    catch (Exception e) {
    	System.out.println("Cannot send AMS FAILURE. "+e);
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
  // Flag indicating that the input-connection is ready
  private boolean inputConnectionReady = false;
  
  private Object inputConnectionLock = new Object();
  private Object frontEndSynchLock = new Object();
  
  private List fronEndSynchBuffer = new ArrayList();
  
  /**
     Start the front-end synchronization process.
   */
  private void resynch() {
  	synchronizing = true;
		inputConnectionReady = false;
  	Thread synchronizer = new Thread() {
  		public void run() {
  			while (true) {
	  			try {
	  				// Wait for the input connection to be established.
		  			waitUntilInputConnectionReady();
		  			myFrontEnd.synch();
		  			notifySynchronized();
		  			break;
	  			}
	  			catch (IMTPException imtpe) {
	  				// The input connection is down again. Go back waiting
	  			}
  			}
  		}
  	};
  	synchronizer.start();
  }

  private void waitUntilInputConnectionReady() {
  	synchronized (inputConnectionLock) {
  		while (!inputConnectionReady) {
  			try {
  				inputConnectionLock.wait();
  			}
  			catch (Exception e) {}
  		}
  	}
  }
  
  public void notifyInputConnectionReady() {
  	synchronized (inputConnectionLock) {
  		inputConnectionReady = true;
  		inputConnectionLock.notifyAll();
  	}
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

