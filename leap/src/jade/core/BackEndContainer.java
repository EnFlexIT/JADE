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

//#MIDP_EXCLUDE_FILE

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

/**
@author Giovanni Caire - TILAB
*/

public class BackEndContainer extends AgentContainerImpl implements BackEnd {

    public static final String BE_REPLICAS_SIZE = "be-replicas-size";

	private static final String OUTGOING_NAME = "out";
        private static final String ADDR_LIST_DELIMITERS = ", \n\t\r";

	private long outCnt = 0;
	
	// The FrontEnd this BackEndContainer is connected to
	private FrontEnd myFrontEnd;

        private Profile myProfile;

	// The manager of the connection with the FrontEnd
	private BEConnectionManager myConnectionManager;

        private CommandProcessor myCommandProcessor;

        private Map agentImages = new HashMap();
	private boolean refreshPlatformInfo = true;

        private String[] replicasAddresses;
	
	public BackEndContainer(Profile p, BEConnectionManager cm) {
	    super(p);
	    myProfile = p;
	    myConnectionManager = cm;

	    try {

		myCommandProcessor = myProfile.getCommandProcessor();

		String beAddrs = p.getParameter(FrontEnd.REMOTE_BACK_END_ADDRESSES, null);
		if(beAddrs != null) {
		    replicasAddresses = parseAddressList(beAddrs);
		    p.setParameter(BE_REPLICAS_SIZE, Integer.toString(replicasAddresses.length));
		}

		myFrontEnd = cm.getFrontEnd(this, null);
		Runtime.instance().beginContainer();
		joinPlatform();
	    }
	    catch (IMTPException imtpe) {
		// Should never happen
		imtpe.printStackTrace();
	    }
	    catch(ProfileException pe) {
		// Should never happen
		pe.printStackTrace();
	    }
	}


      protected void startServices() throws IMTPException, ProfileException, ServiceException, AuthException, NotFoundException {
	  // Create the agent management service
	  jade.core.management.BEAgentManagementService agentManagement = new jade.core.management.BEAgentManagementService();
	  agentManagement.init(this, myProfile);

	  // Create the messaging service
	  jade.core.messaging.MessagingService messaging = new jade.core.messaging.MessagingService();

	  messaging.init(this, myProfile);

	  // Create the back-end replication service
	  jade.core.replication.BEReplicationService beReplication = new jade.core.replication.BEReplicationService();
	  beReplication.init(this, myProfile);

	  ServiceDescriptor[] baseServices = new ServiceDescriptor[] {
	      new ServiceDescriptor(agentManagement.getName(), agentManagement),
	      new ServiceDescriptor(messaging.getName(), messaging)
	  };

	  // Register with the platform and activate all the container fundamental services
	  // This call can modify the name of this container
	  getServiceManager().addNode(getNodeDescriptor(), baseServices);

	  // Install all ACL Codecs and MTPs specified in the Profile
	  messaging.boot(myProfile);

	  // Start the Back-End replication service
	  startService("jade.core.replication.BEReplicationService");

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
  	
	// Mark this thread as an outgoing message dispatcher. This is 
	// necessary, in the case some of the receivers lives in the FrontEnd,
	// to avoid sending him back the message (see dispatch()).
	Thread.currentThread().setName(OUTGOING_NAME+outCnt++);

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
	myFrontEnd.createAgent(name, className, args);
    }

    public void killAgentOnFE(String name) throws IMTPException, NotFoundException {
	myFrontEnd.killAgent(name);
	deadAgent(name);
    }

    public void suspendAgentOnFE(String name) throws IMTPException, NotFoundException {
	myFrontEnd.suspendAgent(name);
    }

    public void resumeAgentOnFE(String name) throws IMTPException, NotFoundException {
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

	      if (Thread.currentThread().getName().startsWith(OUTGOING_NAME)) {
		  // The message was sent by an agent living in the FrontEnd. The
		  // receiverID (living in the FrontEnd too) has already received
		  // the message.
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
		  myFrontEnd.messageIn(msg, receiverID.getLocalName());
		  return true;
	      }
	      catch(NotFoundException nfe) {
		  return false;
	      }
	      catch(IMTPException imtpe) {
		  return false;
	      }	      

	  }
	  else {
	      // Agent not found
	      return false;
	  }
      }
  }

  public void activateReplicas(Properties props) {
      Properties newProps = (Properties)props.clone();
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

    /*public void exit() {
	GenericCommand cmd = new GenericCommand(jade.core.management.AgentManagementSlice.KILL_CONTAINER, jade.core.management.AgentManagementSlice.NAME, null);

	myCommandProcessor.processOutgoing(cmd);
    }*/

  /**
   */
  public void shutDown() {

      agentImages.clear();

      // Forward the exit command to the FrontEnd
      try {
	  myFrontEnd.exit(false);
      }
      catch (IMTPException imtpe) {
	  // The FrontEnd is disconnected. Force the shutdown of the connection
	  myConnectionManager.shutdown();
      }

      super.shutDown();
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
	return (AgentImage)agentImages.remove(id);
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

}

