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

import jade.util.leap.Iterator;
import jade.util.leap.List;
import jade.util.leap.ArrayList;
import jade.util.leap.LinkedList;
import jade.util.leap.Map;
import jade.util.leap.Iterator;
import jade.util.leap.HashMap;
import jade.util.leap.Set;

import jade.lang.acl.ACLMessage;
import jade.core.behaviours.Behaviour;

import jade.domain.FIPAAgentManagement.InternalError;
import jade.domain.FIPAAgentManagement.Envelope;

import jade.mtp.MTPException;
import jade.mtp.MTPDescriptor;
import jade.mtp.TransportAddress;

//__SECURITY__BEGIN
import jade.security.Authority;
import jade.security.AuthException;
import jade.security.AgentPrincipal;
import jade.security.ContainerPrincipal;
import jade.security.IdentityCertificate;
import jade.security.DelegationCertificate;
import jade.security.CertificateFolder;
import jade.security.PrivilegedExceptionAction;
//__SECURITY__END


/**
   This class is a concrete implementation of the JADE agent
   container, providing runtime support to JADE agents.

   This class cannot be instantiated from applications. Instead, the
   <code>Runtime.createAgentContainer(Profile p)</code> method must be called.

   @see Runtime#createAgentContainer(Profile)

   @author Giovanni Rimassa - Universita` di Parma
   @version $Date$ $Revision$

*/
public class AgentContainerImpl implements AgentContainer, AgentToolkit {

  // Local agents, indexed by agent name
  private LADT localAgents = new LADT();

  // The Profile defining the configuration of this Container
  private Profile myProfile;
  
  // The agent platform this container belongs to
  private Platform myPlatform;

  // The IMTP manager, used to access IMTP-dependent functionalities
  private IMTPManager myIMTPManager;
  
  // The Agent Communication Channel, managing the external MTPs.
  private acc myACC;

  // The Object managing all operations related to agent mobility
  // in this container
  private MobilityManager myMobilityManager;
  
  // The Object managing Thread resources in this container
  private ResourceManager myResourceManager;
  
  // The Object managing messages that cannot reach the destination because
  // of disconnection problems in this container
  private MessageManager myMessageManager;
  
  // The Object managing all operations related to event notification
  // in this container
  private NotificationManager myNotificationManager;
  
  // Unique ID of the platform, used to build the GUID of resident
  // agents.
  private static String platformID;
  private ContainerID myID;

  private String username = null;
  private byte[] password = null;
  private CertificateFolder certs;
  private Authority authority;
  private Map principals = new HashMap();
  private Map containerPrincipals = new HashMap();

  private AID theAMS;
  private AID theDefaultDF;

  // This monitor is used to hang a remote ping() call from the front
  // end, in order to detect container failures.
  private Object pingLock = new Object();

  // Package scoped constructor, so that only the Runtime  
  // class can actually create a new Agent Container.
  AgentContainerImpl(Profile p) {
    myProfile = p;
  }

  //__CLDC_UNSUPPORTED__BEGIN
  /**
   * Get the agentcontroller for a local agent given its AID.
   * @param agentID The agentID of the desired agent.
   * @see jade.wrapper.PlatformController#getAgent(String)
   * @since JADE2.6
   */
  public jade.wrapper.AgentController getAgent(AID agentID) {
      // This method is called by jade.wrapper.AgentContainer
      // FIXME. To check for security permissions
      Agent agent = localAgents.acquire(agentID);
      localAgents.release(agentID);
      if (agent != null)
	  return new jade.wrapper.Agent(agentID, agent); 
      else
	  return null;
  }
  //__CLDC_UNSUPPORTED__END

  // /////////////////////////////////////////
  // AgentContainer INTERFACE
  // /////////////////////////////////////////
  public void createAgent(AID agentID, String className, Object[] args, String ownership, CertificateFolder certs, boolean startIt) throws IMTPException {
      Agent agent = null;
      try {
          agent = (Agent)Class.forName(new String(className)).newInstance();
          agent.setArguments(args);
          // Set agent principal and certificates
          if(certs != null) {
              agent.setPrincipal(certs);
          }
          // Set agent ownership
          if(ownership != null)
              agent.setOwnership(ownership);
          else if(certs.getIdentityCertificate() != null)
              agent.setOwnership(((AgentPrincipal)certs.getIdentityCertificate().getSubject()).getOwnership());

          initAgent(agentID, agent, startIt);
      }
      catch(ClassNotFoundException cnfe) {
          System.err.println("Class " + className + " for agent " + agentID + " was not found.");
          throw new IMTPException("Exception in createAgent",cnfe);
      }
      catch(Exception e ) {
          // FIXME: Log the Exception
          throw new IMTPException("Exception in createAgent",e);
      }
  }

  public void createAgent(AID agentID, byte[] serializedInstance, AgentContainer classSite, boolean startIt) throws IMTPException, AuthException {
  	// Delegate the operation to the MobilityManager
  	try {
	  	myMobilityManager.createAgent(agentID, serializedInstance, classSite, startIt);
  	}
  	catch (AuthException e) {
  		throw e;
  	}
  	catch (Exception e) {
  		e.printStackTrace();
  		//throw new IMTPException("Exception in createAgent",e); 
  		
  	}
  }

  // Accepts the fully qualified class name as parameter and searches
  // the class file in the classpath
  public byte[] fetchClassFile(String name) throws IMTPException, ClassNotFoundException {
  	// Delegate the operation to the MobilityManager
  	return myMobilityManager.fetchClassFile(name);
  }

  public void initAgent(AID agentID, Agent instance, boolean startIt) throws NameClashException, IMTPException, NotFoundException, AuthException {
      
      // Subscribe as a listener for the new agent
      instance.setToolkit(this);
      
      // put the agent in the local table and get the previous one, if any
      Agent previous = localAgents.put(agentID, instance);
      if(startIt) {
          try {
              CertificateFolder agentCerts = instance.getCertificateFolder();
              if(agentCerts.getIdentityCertificate() == null) {
                  AgentPrincipal principal = authority.createAgentPrincipal(agentID, AgentPrincipal.NONE);
                  IdentityCertificate identity = authority.createIdentityCertificate();
                  identity.setSubject(principal);
                  authority.sign(identity, certs);
                  agentCerts.setIdentityCertificate(identity);
              }
              myPlatform.bornAgent(agentID, myID, agentCerts);
              instance.powerUp(agentID, myResourceManager);
          }
          catch(NameClashException nce) {
              // System.out.println("Agentname already in use:"+nce.getMessage());
              localAgents.remove(agentID);
              if(previous != null) {
                  localAgents.put(agentID, previous);
              }
              throw nce;
          }
          catch (IMTPException imtpe) {
              localAgents.remove(agentID);
              throw imtpe;
          }
          catch (NotFoundException nfe) {
              localAgents.remove(agentID);
              throw nfe;
          }
          catch (AuthException ae) {
              localAgents.remove(agentID);
              throw ae;
          }
      }
  }

  public void suspendAgent(AID agentID) throws IMTPException, NotFoundException {
    Agent agent = localAgents.acquire(agentID);
    if(agent == null)
      throw new NotFoundException("SuspendAgent failed to find " + agentID);
    agent.doSuspend();
    localAgents.release(agentID);
  }

  public void resumeAgent(AID agentID) throws IMTPException, NotFoundException {
    Agent agent = localAgents.acquire(agentID);
    if(agent == null)
      throw new NotFoundException("ResumeAgent failed to find " + agentID);
    agent.doActivate();
    localAgents.release(agentID);
  }

//__SECURITY__BEGIN
  
	public void changeAgentPrincipal(AID agentID, CertificateFolder certs) throws IMTPException, NotFoundException {
		Agent agent = localAgents.acquire(agentID);
		if (agent != null)
			agent.setPrincipal(certs);
		localAgents.release(agentID);
	}

	public void changedAgentPrincipal(AID agentID, AgentPrincipal principal) throws IMTPException {
		principals.put(agentID, principal);
	}

	public void changeContainerPrincipal(CertificateFolder certs) throws IMTPException {
		this.certs = certs;
	}

	public ContainerPrincipal getContainerPrincipal() {
		ContainerPrincipal cp = null;
		cp = (ContainerPrincipal) certs.getIdentityCertificate().getSubject();
		return cp;
	}


	public ContainerPrincipal getContainerPrincipal(ContainerID cid) throws IMTPException, NotFoundException {
		// FIXME: manage the HashMap 'containerPrincipals' as done for 'principals'
		// see getAgentPrincipal()
		return myPlatform.getContainerPrincipal(cid);
	}

	public ContainerPrincipal getContainerPrincipal(Location loc) throws IMTPException, NotFoundException {
		return myPlatform.getContainerPrincipal((ContainerID)loc);
	}

	public AgentPrincipal getAgentPrincipal(final AID agentID) {
		AgentPrincipal principal = (AgentPrincipal)principals.get(agentID);
		if (principal == null) {
			try {
				principal = (AgentPrincipal)authority.doPrivileged(new jade.security.PrivilegedExceptionAction() {
					public Object run() throws IMTPException, NotFoundException {
						return myPlatform.getAgentPrincipal(agentID);
					}
				});
				principals.put(agentID, principal);
			}
			catch (IMTPException ie) {
				//!!! Manage ie
				ie.printStackTrace();
			}
			catch (NotFoundException ne) {
				//!!! Manage ne
				ne.printStackTrace();
			}
			catch (Exception e) {
				// e should never be thrown
				e.printStackTrace();
			}
		}
		return principal;
	}
//__SECURITY__END

  public void waitAgent(AID agentID) throws IMTPException, NotFoundException {
    Agent agent = localAgents.acquire(agentID);
    if(agent==null)
      throw new NotFoundException("WaitAgent failed to find " + agentID);
    agent.doWait();
    localAgents.release(agentID);
  }

  public void wakeAgent(AID agentID) throws IMTPException, NotFoundException {
    Agent agent = localAgents.acquire(agentID);
    if(agent==null)
      throw new NotFoundException("WakeAgent failed to find " + agentID);
    agent.doWake();
    localAgents.release(agentID);
  }

  public void moveAgent(AID agentID, Location where) throws IMTPException, NotFoundException {
  	// Delegate the operation to the MobilityManager
  	myMobilityManager.moveAgent(agentID, where);
  }

  public void copyAgent(AID agentID, Location where, String newName) throws IMTPException, NotFoundException {
  	// Delegate the operation to the MobilityManager
  	myMobilityManager.copyAgent(agentID, where, newName);
  }

  public void killAgent(AID agentID) throws IMTPException, NotFoundException {
    Agent agent = localAgents.acquire(agentID);
    if(agent == null)
      throw new NotFoundException("KillAgent failed to find " + agentID);
    agent.doDelete();
    localAgents.release(agentID);
  }

  public void exit() throws IMTPException {
    shutDown();
	}

  public void postTransferResult(AID agentID, boolean result, List messages) throws IMTPException, NotFoundException {
  	// Delegate the operation to the MobilityManager
		myMobilityManager.handleTransferResult(agentID, result, messages);
  }

  /**
    @param snifferName The Agent ID of the sniffer to send messages to.
    @param toBeSniffed The <code>AID</code> of the agent to be sniffed
  **/
  public void enableSniffer(AID snifferName, AID toBeSniffed) throws IMTPException {
  	// Delegate the operation to the NotificationManager
  	myNotificationManager.enableSniffer(snifferName, toBeSniffed);
  }


  /**
    @param snifferName The Agent ID of the sniffer to send messages to.
    @param notToBeSniffed The <code>AID</code> of the agent to stop sniffing
  **/
  public void disableSniffer(AID snifferName, AID notToBeSniffed) throws IMTPException {
  	// Delegate the operation to the NotificationManager
  	myNotificationManager.disableSniffer(snifferName, notToBeSniffed);
  }


  /**
    @param debuggerName The Agent ID of the debugger to send messages to.
    @param toBeDebugged The <code>AID</code> of the agent to start debugging.
  **/
  public void enableDebugger(AID debuggerName, AID toBeDebugged) throws IMTPException {
  	// Delegate the operation to the NotificationManager
  	myNotificationManager.enableDebugger(debuggerName, toBeDebugged);
  }

  /**
    @param debuggerName The Agent ID of the debugger to send messages to.
    @param notToBeDebugged The <code>AID</code> of the agent to stop debugging.
  **/
  public void disableDebugger(AID debuggerName, AID notToBeDebugged) throws IMTPException {
  	// Delegate the operation to the NotificationManager
  	myNotificationManager.disableDebugger(debuggerName, notToBeDebugged); 
  }

  public void dispatch(ACLMessage msg, AID receiverID) throws IMTPException, NotFoundException {
		// DEBUG
		//Runtime.instance().debug(receiverID.getLocalName(), "incoming message received by container: "+Runtime.instance().stringify(msg));

    Agent receiver = localAgents.acquire(receiverID);
    if(receiver == null) {
			//Runtime.instance().debug(receiverID.getLocalName(), "Not found on this container");
			throw new NotFoundException("DispatchMessage failed to find " + receiverID);
    }
    receiver.postMessage(msg);

    localAgents.release(receiverID);

  }

  public void ping(boolean hang) throws IMTPException {
    if(hang) {
      synchronized(pingLock) {
	try {
	  pingLock.wait();
	}
	catch(InterruptedException ie) {
		System.out.println("PING wait interrupted");
	  // Do nothing
	}
      }
    }
  }

  public void installACLCodec(String className) throws jade.lang.acl.ACLCodec.CodecException {
  	myACC.addACLCodec(className);
  }

  public MTPDescriptor installMTP(String address, String className) throws IMTPException, MTPException {
      MTPDescriptor result = myACC.addMTP(className, address);

    // Add the address of the new MTP to the AIDs of all local agents
    Agent[] allLocalAgents = localAgents.values();
  	for(int i = 0; i < allLocalAgents.length; i++) {
	  String[] addrs = result.getAddresses();
          for(int j = 0; j < addrs.length; j++) {
            allLocalAgents[i].addPlatformAddress(addrs[j]);
          }
  	}

    // Add the new addresses to the AMS and Default DF AIDs
    String[] addresses = result.getAddresses();
    for(int i = 0; i < addresses.length; i++) {
        theAMS.addAddresses(addresses[i]);
        theDefaultDF.addAddresses(addresses[i]);
    }

    myPlatform.newMTP(result, myID);
    return result;
  }

  public void uninstallMTP(String address) throws IMTPException, NotFoundException, MTPException {
    MTPDescriptor mtp = myACC.removeMTP(address);

    // Remove the address of the old MTP from the AIDs of all local agents
    Agent[] allLocalAgents = localAgents.values();
    for(int i = 0; i < allLocalAgents.length; i++) {
      allLocalAgents[i].removePlatformAddress(address);
    }

    // Remove the address of the old MTP from the AIDs of the AMS and the Default DF

    theAMS.removeAddresses(address);
    theDefaultDF.removeAddresses(address);

    myPlatform.deadMTP(mtp, myID);
  }

  public void updateRoutingTable(int op, MTPDescriptor mtp, AgentContainer ac) throws IMTPException {
    Agent[] allLocalAgents = localAgents.values();
    String[] addresses = mtp.getAddresses();
    switch(op) {
    case ADD_RT:
      myACC.addRoute(mtp, ac);
      for(int i = 0; i < addresses.length; i++) {

        // Add the address of the new MTP to the AIDs of all local agents
        for(int j = 0; j < allLocalAgents.length; j++) {
            allLocalAgents[j].addPlatformAddress(addresses[i]);
        }

        // Add the new addresses to the AMS and Default DF AIDs
        theAMS.addAddresses(addresses[i]);
        theDefaultDF.addAddresses(addresses[i]);
      }
      break;
    case DEL_RT:
      myACC.removeRoute(mtp, ac);
      for(int i = 0; i < addresses.length; i++) {

        // Remove the address of the old MTP to the AIDs of all local agents
        for(int j = 0; j < allLocalAgents.length; j++) {
            allLocalAgents[j].removePlatformAddress(addresses[i]);
        }

        // Remove the addresses of the old MTP from the AIDs of the AMS and the Default DF
        theAMS.removeAddresses(addresses[i]);
        theDefaultDF.removeAddresses(addresses[i]);
      }
      break;
    }

  }

  public void routeOut(ACLMessage msg, AID receiver, String address) throws IMTPException, MTPException {
    myACC.forwardMessage(msg, receiver, address);
  }

  void routeIn(ACLMessage msg, AID receiver) {
   	unicastPostMessage(msg, receiver);
  }
  
  public Authority getAuthority() {
    return authority;
  }

  void joinPlatform() {
      try {
          // Create and initialize the IMTPManager
          myIMTPManager = myProfile.getIMTPManager();
          myIMTPManager.initialize(myProfile);
          
          // Make itself accessible from remote JVMs
          myIMTPManager.remotize(this);
          
          // Get the Main
          myPlatform = myProfile.getPlatform();
          
          // Create and init container-authority
          try {
	      if (myProfile.getParameter(Profile.OWNER, null) != null) {
		  // if there is an owner for this container
		  // then try to use the full implementation of security
		  myProfile.setParameter(Profile.MAINAUTH_CLASS,"jade.security.impl.PlatformAuthority");
		  myProfile.setParameter(Profile.AUTHORITY_CLASS,"jade.security.impl.ContainerAuthority");
	      }
              String type = myProfile.getParameter(Profile.AUTHORITY_CLASS, null);
              if (type != null) {
                  authority = (Authority)Class.forName(type).newInstance();
                  authority.setName("container-authority");
                  authority.init(myProfile, myPlatform);
              }
          }
          catch (Exception e1) {
	      System.out.println("Some problems occured during the initialization of the security. JADE will continue execution by using dummy security.");
	      authority = null;
              //e1.printStackTrace();
          }
          
          try {
              if (authority == null) {
                  authority = new jade.security.dummy.DummyAuthority();
                  authority.setName("container-authority");
                  authority.init(myProfile, myProfile.getPlatform());
              }
          }
          catch (Exception e2) {
              e2.printStackTrace();
          }
          
          // This string will be used to build the GUID for every agent on
          // this platform.
          platformID = myPlatform.getPlatformName();
          
          // Build the Agent IDs for the AMS and for the Default DF.
          theAMS = new AID("ams", AID.ISLOCALNAME);
          theDefaultDF = new AID("df", AID.ISLOCALNAME);

          // Create the ResourceManager
          myResourceManager = myProfile.getResourceManager();

          // Create the MessageManager
          myMessageManager = new MessageManager();
          myMessageManager.initialize(myProfile, this);

          // Create and initialize the NotificationManager
          myNotificationManager = myProfile.getNotificationManager();
          myNotificationManager.initialize(this, localAgents);

          // Create and initialize the MobilityManager.
          myMobilityManager = myProfile.getMobilityManager();
          myMobilityManager.initialize(myProfile, this, localAgents);

          // Create the ACC.
          myACC = myProfile.getAcc();

          // Initialize the Container ID
          TransportAddress addr = (TransportAddress) myIMTPManager.getLocalAddresses().get(0);
	  // the name for this container is got from the Profile, if exists
	  // "No-name" is needed because the NAME is mandatory in the Ontology
          myID = new ContainerID(myProfile.getParameter(Profile.CONTAINER_NAME,
							"No-Name"), addr);

          // Acquire username and password
          String ownership = myProfile.getParameter(Profile.OWNER, ContainerPrincipal.NONE);
          password = Agent.extractPassword(ownership);
          username = Agent.extractUsername(ownership);
          myProfile.setParameter(Profile.OWNER, username);

          // Register to the platform. If myPlatform is the real MainContainerImpl
          // this call also starts the AMS and DF
          myPlatform.register(this, myID, username, password);

          // Install MTPs and ACLCodecs. Must be done after registering with the Main
          myACC.initialize(this, myProfile);
      }
      catch (IMTPException imtpe) {
          System.err.println("Communication failure while contacting agent platform: " + imtpe.getMessage());
          imtpe.printStackTrace();
          endContainer();
          return;
      }
      catch (AuthException ae) {
          System.err.println("Authentication or authorization failure while contacting agent platform.");
          ae.printStackTrace();
          endContainer();
          return;
      }
      catch (Exception e) {
          System.err.println("Some problem occurred while contacting agent platform.");
          e.printStackTrace();
          endContainer();
          return;
      }
      
      // Create and activate agents that must be launched at bootstrap
      try {
          List l = myProfile.getSpecifiers(Profile.AGENTS);
          Iterator agentSpecifiers = l.iterator();
          while(agentSpecifiers.hasNext()) {
              Specifier s = (Specifier) agentSpecifiers.next();
              
              AID agentID = new AID(s.getName(), AID.ISLOCALNAME);
              
              try {
                  String agentOwnership = username;
                  AgentPrincipal agentPrincipal = authority.createAgentPrincipal(agentID, username);
                  
                  IdentityCertificate agentIdentity = authority.createIdentityCertificate();
                  agentIdentity.setSubject(agentPrincipal);
                  authority.sign(agentIdentity, certs);
                  
                  DelegationCertificate agentDelegation = authority.createDelegationCertificate();
                  agentDelegation.setSubject(agentPrincipal);
                  for (int c = 0; c < certs.getDelegationCertificates().size(); c++)
                      agentDelegation.addPermissions(((DelegationCertificate)certs.getDelegationCertificates().get(c)).getPermissions());
                  authority.sign(agentDelegation, certs);
                  
                  CertificateFolder agentCerts = new CertificateFolder();
                  agentCerts.setIdentityCertificate(agentIdentity);
                  agentCerts.addDelegationCertificate(agentDelegation);
                  
                  try {
                      createAgent(agentID, s.getClassName(), s.getArgs(), agentOwnership, agentCerts, NOSTART);
                  }
                  catch (IMTPException imtpe) {
                      // The call to createAgent() in this case is local --> no need to
                      // print the exception again. Just skip this agent
                      continue;
                  }
                  myPlatform.bornAgent(agentID, myID, agentCerts);
              }
              catch (IMTPException imtpe1) {
                  imtpe1.printStackTrace();
                  localAgents.remove(agentID);
              }
              catch (NameClashException nce) {
                  System.out.println("Agent name already in use: " + nce.getMessage());
                  // FIXME: If we have two agents with the same name among the initial
                  // agents, the second one replaces the first one, but then a
                  // NameClashException is thrown --> both agents are removed even if
                  // the platform "believes" that the first on is alive.
                  localAgents.remove(agentID);
              }
              catch (NotFoundException nfe) {
                  System.out.println("This container does not appear to be registered with the main container.");
                  localAgents.remove(agentID);
              }
              catch (AuthException ae) {
                  System.out.println("Authorization or authentication error while adding a new agent to the platform.");
                  localAgents.remove(agentID);
              }
          }
          
          // Now activate all agents (this call starts their embedded threads)
          AID[] allLocalNames = localAgents.keys();
          for (int i = 0; i < allLocalNames.length; i++) {
              AID id = allLocalNames[i];
              Agent agent = localAgents.acquire(id);
              agent.powerUp(id, myResourceManager);
              localAgents.release(id);
          }
      }
      catch (ProfileException pe) {
          System.out.println("Warning: error reading initial agents");
      }
      
      System.out.println("Agent container " + myID + " is ready.");
  }


  public void shutDown() {
    // Close down the ACC
    myACC.shutdown();

    // Remove all non-system agents 
    Agent[] allLocalAgents = localAgents.values();

    for(int i = 0; i < allLocalAgents.length; i++) {
      // Kill agent and wait for its termination
      Agent a = allLocalAgents[i];

      // Skip the Default DF and the AMS
      AID id = a.getAID();
      if(id.equals(getAMS()) || id.equals(getDefaultDF()))
        continue;

      a.doDelete();
      //System.out.println("Killed agent "+a.getLocalName()+". Waiting for its termination...");
      //System.out.flush();
      a.join();
      //System.out.println("Agent "+a.getLocalName()+" terminated");
      //System.out.flush();
      a.resetToolkit();
    }

    try {
      // Deregister this container from the platform.
    	// If this is the Main Container this call also stop the AMS and DF
      myPlatform.deregister(this);

      // Unblock threads hung in ping() method (this will deregister the container)
      synchronized(pingLock) {
				pingLock.notifyAll();
      }

  		// Make itself no longer accessible from remote JVMs
      myIMTPManager.unremotize(this); 
    }
    catch(IMTPException imtpe) {
      imtpe.printStackTrace();
    }

    // Releases Thread resources
    myResourceManager.releaseResources();
    
    // Notify the JADE Runtime that the container has terminated execution
    endContainer();
  }

  // calls Runtime.instance().endContainer()
  // with the security priviledges of AgentContainerImpl 
  // no matter priviledges of who originaltely triggered this action 
  private void endContainer() {
	try {
	authority.doPrivileged(new PrivilegedExceptionAction() {
	        public Object run() {
					Runtime.instance().endContainer();
	            return null; // nothing to return
	        }
	});
	} catch(Exception e) {
		e.printStackTrace();
	}
  }



  // Implementation of AgentToolkit interface

  public Location here() {
    return myID;
  }

	public void handleSend(ACLMessage msg) throws AuthException {

		AgentPrincipal target1 = getAgentPrincipal(msg.getSender());
		//System.out.println("AgContImpl:  target1="+target1);
		//System.out.println("AgContImpl:  msg="+msg);
		authority.checkAction(Authority.AGENT_SEND_AS, target1, null);
		

		AuthException lastException = null;

		// 26-Mar-2001. The receivers set into the Envelope of the message, 
		// if present, must have precedence over those set into the ACLMessage.
		// If no :intended-receiver parameter is present in the Envelope, 
		// then the :to parameter
		// is used to generate :intended-receiver field. 
		//
		// create an Iterator with all the receivers to which the message must be 
		// delivered
		Iterator it = null;
		Envelope env = msg.getEnvelope();
		if (env != null) {
			it = env.getAllIntendedReceiver();
			if ((it != null) && (it.hasNext())) {
				//System.out.println("WARNING: Envelope.intendedReceiver taking precedence over ACLMessage.to");
				// ok. use the intendedreceiver
			}
			else {
				it = env.getAllTo();
				if ((it != null) && (it.hasNext())) {
					//System.out.println("WARNING: Envelope.to taking precedence over ACLMessage.to");
					// ok. use the :to
					// FIXME. Should I copy all the :to values in the :IntendedReceiver?
				}
				else {
					it = msg.getAllReceiver();
					// ok. use the receivers set in the ACLMessage
				}
			}
		}
		else
			it = msg.getAllReceiver(); //use the receivers set in the ACLMessage
		if (it == null)
			return; // No Message is sent in this case because no receiver was found
		
		// Now it contains the Iterator with all the receivers of this message
		// Iterator it = msg.getAllReceiver();
		while (it.hasNext()) {
			try {
				AID dest = (AID)it.next();
				AgentPrincipal target2 = getAgentPrincipal(dest);
				authority.checkAction(Authority.AGENT_SEND_TO, target2, null);
				ACLMessage copy = (ACLMessage)msg.clone();
				unicastPostMessage(copy, dest);
			}
			catch (AuthException ae) {
				lastException = ae;
				//ae.printStackTrace();
				notifyFailureToSender(msg, new InternalError(ae.getMessage()));
			}
		}

		// Notify message listeners
		//fireSentMessage(msg, msg.getSender());
		myNotificationManager.fireEvent(NotificationManager.SENT_MESSAGE,
			new Object[] {msg, msg.getSender()});

		if (lastException != null)
			throw lastException;
	}

  public void handlePosted(AID agentID, ACLMessage msg) throws AuthException {
    AgentPrincipal target = getAgentPrincipal(msg.getSender());
    authority.checkAction(Authority.AGENT_RECEIVE_FROM, target, null);
    //firePostedMessage(msg, agentID);
    myNotificationManager.fireEvent(NotificationManager.POSTED_MESSAGE,
    	new Object[]{msg, agentID});
  }

  public void handleReceived(AID agentID, ACLMessage msg) throws AuthException {
    //!!!AgentPrincipal target = getAgentPrincipal(msg.getSender());
    //!!!authority.checkAction(Authority.AGENT_RECEIVE_FROM, target, null);
    //fireReceivedMessage(msg, agentID);
    myNotificationManager.fireEvent(NotificationManager.RECEIVED_MESSAGE,
      new Object[]{msg, agentID});
  }

  public void handleBehaviourAdded(AID agentID, Behaviour b) {
      myNotificationManager.fireEvent(NotificationManager.ADDED_BEHAVIOUR,
        new Object[]{agentID, b});
  }
  
  public void handleBehaviourRemoved(AID agentID, Behaviour b) {
      myNotificationManager.fireEvent(NotificationManager.REMOVED_BEHAVIOUR,
        new Object[]{agentID, b});
  }
  
  public void handleChangeBehaviourState(AID agentID, Behaviour b,
                                         String from, String to) {
      myNotificationManager.fireEvent(NotificationManager.CHANGED_BEHAVIOUR_STATE,
        new Object[]{agentID, b, from, to});
  }

//__SECURITY__BEGIN
  public void handleChangedAgentPrincipal(AID agentID, AgentPrincipal oldPrincipal, CertificateFolder certs) {
    myNotificationManager.fireEvent(NotificationManager.CHANGED_AGENT_PRINCIPAL,
      new Object[]{agentID, oldPrincipal, (AgentPrincipal)certs.getIdentityCertificate().getSubject()});
    try {
      myPlatform.changedAgentPrincipal(agentID, certs);
    }
    catch (IMTPException re) {
      re.printStackTrace();
    }
    catch (NotFoundException nfe) {
      nfe.printStackTrace();
    }
  }
//__SECURITY__END

  public void handleChangedAgentState(AID agentID, AgentState from, AgentState to) {
    //fireChangedAgentState(agentID, from, to);
    myNotificationManager.fireEvent(NotificationManager.CHANGED_AGENT_STATE,
    	new Object[]{agentID, from, to});
    if (to.equals(jade.domain.FIPAAgentManagement.AMSAgentDescription.SUSPENDED)) {
      try {
        myPlatform.suspendedAgent(agentID);
      }
      catch(IMTPException re) {
        re.printStackTrace();
      }
      catch(NotFoundException nfe) {
        nfe.printStackTrace();
      }
    }
    else if (from.equals(jade.domain.FIPAAgentManagement.AMSAgentDescription.SUSPENDED)) {
      try {
        myPlatform.resumedAgent(agentID);
      }
      catch(IMTPException re) {
        re.printStackTrace();
      }
      catch(NotFoundException nfe) {
        nfe.printStackTrace();
      }
    }
  }

  public void handleStart(String localName, Agent instance) {
    AID agentID = new AID(localName, AID.ISLOCALNAME);
    try {
        initAgent(agentID, instance, START);
    }
    catch(NameClashException ne) {
        // Do nothing, since this agent hassn't started yet
    }
    catch(IMTPException imtpe) {
        // Do nothing, since this agent hassn't started yet
    }
    catch(NotFoundException nfe) {
        // Do nothing, since this agent hassn't started yet
    }
    catch(AuthException ae) {
        // Do nothing, since this agent hassn't started yet
    }

  }

  public void handleEnd(AID agentID) {
    try {
      localAgents.remove(agentID);
      myPlatform.deadAgent(agentID);
    }
    catch(IMTPException re) {
      re.printStackTrace();
    }
    catch(NotFoundException nfe) {
      nfe.printStackTrace();
    }
  }

  public void handleMove(AID agentID, Location where) throws AuthException, NotFoundException, IMTPException {
  	// Delegate the operation to the MobilityManager
    myMobilityManager.handleMove(agentID, where);
  }

  public void handleClone(AID agentID, Location where, String newName) throws AuthException {
  	// Delegate the operation to the MobilityManager
	myMobilityManager.handleClone(agentID, where, newName);
  }

  public void setPlatformAddresses(AID id) {
    myACC.setPlatformAddresses(id);
  }
	
  public AID getAMS() {
    return (AID)theAMS.clone();
  }

  public AID getDefaultDF() {
    return (AID)theDefaultDF.clone();
  }



  // Private and package scoped methods

  /**
   * This method is used by the class AID in order to get the HAP.
   */
  static String getPlatformID()
  {
  	return platformID;
  }

  private void unicastPostMessage(ACLMessage msg, AID receiverID) {
    try {
			// If the receiver is local, post the message directly in its message queue	  
			dispatch(msg, receiverID);
    }
    catch(NotFoundException nfe) {
    	// Otherwise let the MessageManager deliver it asynchronously
    	myMessageManager.deliver(msg, receiverID);
    }
    catch(IMTPException imtpe) {
			// This should never happen as this is a local call
			imtpe.printStackTrace();
    }
  }

  /**
   * Package scoped as it is called by the MessageManager
   */
  void deliverNow(ACLMessage msg, AID receiverID) throws UnreachableException {
    try {
    	if(livesHere(receiverID)) {
				// Dispatch it through the MainContainerProxy
				myPlatform.dispatch(msg, receiverID);
    	}
    	else {
				// Dispatch it through the ACC
				myACC.dispatch(msg, receiverID);
    	}
    }
    catch(NotFoundException nfe) {
    	// The receiver does not exist --> Send a FAILURE message
      notifyFailureToSender(msg, new InternalError("\"Agent not found: " + nfe.getMessage()+"\""));
    }
  }
  
  /**
   * This method is used internally by the platform in order
   * to notify the sender of a message that a failure was reported by
   * the Message Transport Service.
   * Package scoped as it can be called by the MessageManager
   */
  void notifyFailureToSender(ACLMessage msg, InternalError ie) {
		//if (the sender is not the AMS and the performative is not FAILURE)
		if ( (msg.getSender()==null) || ((msg.getSender().equals(getAMS())) && (msg.getPerformative()==ACLMessage.FAILURE))) // sanity check to avoid infinte loops
	    return;
		// else send back a failure message
		ACLMessage failure = msg.createReply();
		failure.setPerformative(ACLMessage.FAILURE);
		//System.err.println(failure.toString());
		failure.setSender(getAMS());
		// FIXME the content is not completely correct, but that should
		// also avoid creating wrong content
		String content = "( (action " + msg.getSender().toString();
		content = content + " ACLMessage ) "+ie.getMessage()+")" ;
		failure.setContent(content);
		try {
			handleSend(failure);
		}
		catch (AuthException ae) {
		}
  }


 	// Tells whether the given AID refers to an agent of this platform
  // or not.
  private boolean livesHere(AID id) {
    String hap = id.getHap();
    return CaseInsensitiveString.equalsIgnoreCase(hap, platformID);
  }
  
  LADT getLocalAgents() {
  	return localAgents;
  }
  
//__JADE_ONLY__BEGIN  
  //FIXME: These methods have been added to support 
  // PlatformListener registration from the In-process-interface
  // with minimum effort. They will possibly be removed in a 
  // future (more general) implementation
  public void addPlatformListener(AgentManager.Listener l) throws ClassCastException {
  	AgentManager m = (AgentManager) myPlatform;
  	m.addListener(l);
  }
  
  public void removePlatformListener(AgentManager.Listener l) throws ClassCastException {
  	AgentManager m = (AgentManager) myPlatform;
  	m.removeListener(l);
  }
//__JADE_ONLY__END  
}


