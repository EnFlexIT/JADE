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

import jade.util.Logger;

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

   @author Giovanni Rimassa - Universita' di Parma
   @version $Date$ $Revision$

*/
public class AgentContainerImpl implements AgentContainer, AgentToolkit {

    static final boolean CREATE_AND_START = true;
    static final boolean CREATE_ONLY = false;


  // Local agents, indexed by agent name
  private LADT localAgents = new LADT();

  // The Profile defining the configuration of this Container
  private Profile myProfile;

  // The Command Processor through which all the vertical commands in this container will pass
  CommandProcessor myCommandProcessor;

  // The agent platform this container belongs to
  private Platform myPlatform;

  // The IMTP manager, used to access IMTP-dependent functionalities
  private IMTPManager myIMTPManager;

  // The platform Service Manager
  private ServiceManager myServiceManager;

  // The platform Service Finder
  private ServiceFinder myServiceFinder;

  // The Object managing Thread resources in this container
  private ResourceManager myResourceManager;
  
  // The Object managing all operations related to event notification
  // in this container
  private NotificationManager myNotificationManager;
  
  private ContainerID myID;

  private String username = null;
  private byte[] password = null;
  private CertificateFolder certs;
  private Authority authority;
  private Map principals = new HashMap();
  private Map containerPrincipals = new HashMap();

  private AID theAMS;
  private AID theDefaultDF;

  // Package scoped constructor, so that only the Runtime  
  // class can actually create a new Agent Container.
  AgentContainerImpl(Profile p) {
    try {
	myProfile = p;
	myCommandProcessor = myProfile.getCommandProcessor();
    }
    catch(ProfileException pe) {
	pe.printStackTrace();
    }
    
  }

  //#MIDP_EXCLUDE_BEGIN
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
  //#MIDP_EXCLUDE_END

  private void createAgent(AID agentID, String className, Object[] args, String ownership, CertificateFolder certs, boolean startIt) {
      Agent agent = null;
      try {
          agent = (Agent)Class.forName(new String(className)).newInstance();
          agent.setArguments(args);
          //#MIDP_EXCLUDE_BEGIN
          // Set agent principal and certificates
          if(certs != null) {
              agent.setPrincipal(certs);
          }
          // Set agent ownership
          if(ownership != null)
              agent.setOwnership(ownership);
          else if(certs.getIdentityCertificate() != null)
              agent.setOwnership(((AgentPrincipal)certs.getIdentityCertificate().getSubject()).getOwnership());
          //#MIDP_EXCLUDE_END

          initAgent(agentID, agent, startIt);
      }
      catch(ClassNotFoundException cnfe) {
          System.err.println("Class " + className + " for agent " + agentID + " was not found.");
      }
      catch(Exception e ) {
	  e.printStackTrace();
      }
  }

  public void initAgent(AID agentID, Agent instance, boolean startIt) throws NameClashException, IMTPException, NotFoundException, AuthException {
      GenericCommand cmd = new GenericCommand(jade.core.management.AgentManagementService.INFORM_CREATED, jade.core.management.AgentManagementService.NAME, "");
      cmd.addParam(agentID);
      cmd.addParam(instance);
      cmd.addParam(new Boolean(startIt));
      myCommandProcessor.process(cmd);
  }

	public void changeAgentPrincipal(AID agentID, CertificateFolder certs) throws IMTPException, NotFoundException {
		//#MIDP_EXCLUDE_BEGIN
		Agent agent = localAgents.acquire(agentID);
		if (agent != null)
			agent.setPrincipal(certs);
		localAgents.release(agentID);
		//#MIDP_EXCLUDE_END
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


	/*public ContainerPrincipal getContainerPrincipal(ContainerID cid) throws IMTPException, NotFoundException {
		// FIXME: manage the HashMap 'containerPrincipals' as done for 'principals'
		// see getAgentPrincipal()
		return myPlatform.getContainerPrincipal(cid);
	}

	public ContainerPrincipal getContainerPrincipal(Location loc) throws IMTPException, NotFoundException {
		return myPlatform.getContainerPrincipal((ContainerID)loc);
	}*/

	public AgentPrincipal getAgentPrincipal(final AID agentID) {
		AgentPrincipal principal = (AgentPrincipal)principals.get(agentID);
		if (principal == null) {

		    /***
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
		    ***/

		    // FIXME: Temporary Hack
		    principal = new jade.security.dummy.DummyPrincipal();
		    principals.put(agentID, principal);
		}
		return principal;
	}
//__SECURITY__END

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

  public Authority getAuthority() {
    return authority;
  }

  void joinPlatform() {
      try {
          // Create and initialize the IMTPManager
          myIMTPManager = myProfile.getIMTPManager();
          myIMTPManager.initialize(myProfile);

	  // Get the Service Manager and the Service Finder
	  myServiceManager = myProfile.getServiceManager();
	  myServiceFinder = myProfile.getServiceFinder();

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
          AID.setPlatformID(myServiceManager.getPlatformName());

          // Build the Agent IDs for the AMS and for the Default DF.
          theAMS = new AID("ams", AID.ISLOCALNAME);
          theDefaultDF = new AID("df", AID.ISLOCALNAME);

          // Create the ResourceManager
          myResourceManager = myProfile.getResourceManager();

	  // FIXME: This will be replaced by activateService("Event-Notification")
          // Create and initialize the NotificationManager
          myNotificationManager = myProfile.getNotificationManager();
          myNotificationManager.initialize(this, localAgents);

          // Initialize the Container ID
          TransportAddress addr = (TransportAddress) myIMTPManager.getLocalAddresses().get(0);
	  // the name for this container is got from the Profile, if exists
	  // "No-name" is needed because the NAME is mandatory in the Ontology
          myID = new ContainerID(myProfile.getParameter(Profile.CONTAINER_NAME,
							"No-Name"), addr);

          // Acquire username and password
	  //#MIDP_EXCLUDE_BEGIN
          String ownership = myProfile.getParameter(Profile.OWNER, ContainerPrincipal.NONE);
          password = Agent.extractPassword(ownership);
          username = Agent.extractUsername(ownership);
	  //#MIDP_EXCLUDE_END
	  /*#MIDP_INCLUDE_BEGIN
	    password = new byte[] {};
	    username = ContainerPrincipal.NONE;
	    #MIDP_INCLUDE_END*/
          myProfile.setParameter(Profile.OWNER, username);

	  // FIXME: Temporary Hack --- Start 
	  certs = new CertificateFolder();
	  IdentityCertificate identity = new jade.security.dummy.DummyCertificate();
	  identity.setSubject(new jade.security.dummy.DummyPrincipal(myID, username));
	  certs.setIdentityCertificate(identity);
	  // FIXME: Temporary Hack --- End 

	  // Create the agent management service
	  jade.core.management.AgentManagementService agentManagement = new jade.core.management.AgentManagementService(this, myProfile);

	  // Create the messaging service
	  jade.core.messaging.MessagingService messaging = new jade.core.messaging.MessagingService(this, myProfile);


	  NodeDescriptor localDesc = new NodeDescriptor(myID, myIMTPManager.getLocalNode(), username, password);
	  ServiceDescriptor[] baseServices = new ServiceDescriptor[] {
	      new ServiceDescriptor(agentManagement.getName(), agentManagement),
	      new ServiceDescriptor(messaging.getName(), messaging)
	  };

	  // Register with the platform and activate all the container fundamental services
	  // This call can modify the name of this container
	  myServiceManager.addNode(localDesc, baseServices);

	  // FIXME: It should be already set in the profile as a boolean value
	  boolean disableMobility = myProfile.getParameter("mobility", "").equals("jade.core.DummyMobilityManager");

	  if(!disableMobility) {

	      // Separately create and activate the (optional) mobility service
	      jade.core.mobility.AgentMobilityService agMob = new jade.core.mobility.AgentMobilityService(this, myProfile);
	      myServiceManager.activateService(new ServiceDescriptor(agMob.getName(), agMob));
	  }

          // If myPlatform is the real MainContainerImpl this call starts the AMS and DF, otherwise it does nothing
	  myPlatform.startSystemAgents(this);

	  // Install all ACL Codecs and MTPs specified in the Profile
	  messaging.activateProfile(myProfile);

      }
      catch (IMTPException imtpe) {
          Logger.println("Communication failure while contacting agent platform: " + imtpe.getMessage());
          imtpe.printStackTrace();
          endContainer();
          return;
      }
      catch (AuthException ae) {
          Logger.println("Authentication or authorization failure while contacting agent platform.");
          ae.printStackTrace();
          endContainer();
          return;
      }
      catch (Exception e) {
          Logger.println("Some problem occurred while contacting agent platform.");
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
                  /*AgentPrincipal agentPrincipal = authority.createAgentPrincipal(agentID, username);
                  
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
                  */
                  CertificateFolder agentCerts = createCertificateFolder(agentID);

		  createAgent(agentID, s.getClassName(), s.getArgs(), agentOwnership, agentCerts, CREATE_ONLY);
              }
              catch (AuthException ae) {
                  Logger.println("Authorization or authentication error while adding a new agent to the platform.");
                  localAgents.remove(agentID);
              }
          }
          
          // Now activate all agents (this call starts their embedded threads)
          AID[] allLocalNames = localAgents.keys();
          for (int i = 0; i < allLocalNames.length; i++) {
              AID id = allLocalNames[i];

	      if(!id.equals(theAMS) && !id.equals(theDefaultDF)) { 
		  startAgent(id);
	      }
          }
      }
      catch (ProfileException pe) {
          System.out.println("Warning: error reading initial agents");
      }
      
      System.out.println("Agent container " + myID + " is ready.");
  }

  CertificateFolder createCertificateFolder(AID agentID) throws AuthException {
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
	  
	  return agentCerts;
  }

  public void shutDown() {

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
      // If this is the Main Container this call also stops the AMS and DF
	//      myPlatform.deregister(this);

      myPlatform.removeLocalContainer();

      myIMTPManager.disconnect(myID);

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


	////////////////////////////////////////////
  // AgentToolkit interface implementation
  ////////////////////////////////////////////

  public Location here() {
    return myID;
  }

    public void handleSend(ACLMessage msg, AID sender) throws AuthException {

	GenericCommand cmd = new GenericCommand(jade.core.messaging.MessagingService.SEND_MESSAGE, jade.core.messaging.MessagingService.NAME, "");
	cmd.addParam(msg);
	cmd.addParam(sender);
	Object lastException = myCommandProcessor.process(cmd);

	// Notify message listeners
	myNotificationManager.fireEvent(NotificationManager.SENT_MESSAGE, new Object[] {msg, msg.getSender()});

	if((lastException != null) && (lastException instanceof AuthException)) {
	    throw (AuthException)lastException;
	}

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

  public void handleChangedAgentPrincipal(AID agentID, AgentPrincipal oldPrincipal, CertificateFolder certs) {

      /***
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

      ***/
  }

  public void handleChangedAgentState(AID agentID, AgentState from, AgentState to) {

      // FIXME: This needs to go in the Event Notification Service
      myNotificationManager.fireEvent(NotificationManager.CHANGED_AGENT_STATE,
				      new Object[]{agentID, from, to});

      GenericCommand cmd = new GenericCommand(jade.core.management.AgentManagementService.INFORM_STATE_CHANGED, jade.core.management.AgentManagementService.NAME, "");
      cmd.addParam(agentID);
      cmd.addParam(from);
      cmd.addParam(to);
      myCommandProcessor.process(cmd);

  }

  public void handleStart(String localName, Agent instance) {
      try {
	  AID agentID = new AID(localName, AID.ISLOCALNAME);
	  startAgent(agentID);
      }
      catch(Exception e) {
	  e.printStackTrace();
      }
  }

  public void handleEnd(AID agentID) {
      GenericCommand cmd = new GenericCommand(jade.core.management.AgentManagementService.INFORM_KILLED, jade.core.management.AgentManagementService.NAME, "");
      cmd.addParam(agentID);
      myCommandProcessor.process(cmd);
  }

  public void handleMove(AID agentID, Location where) throws AuthException, NotFoundException, IMTPException {
      GenericCommand cmd = new GenericCommand(jade.core.mobility.AgentMobilityService.INFORM_MOVED, jade.core.mobility.AgentMobilityService.NAME, "");
      cmd.addParam(agentID);
      cmd.addParam(where);
      myCommandProcessor.process(cmd);
  }

  public void handleClone(AID agentID, Location where, String newName) throws AuthException {
      GenericCommand cmd = new GenericCommand(jade.core.mobility.AgentMobilityService.INFORM_CLONED, jade.core.mobility.AgentMobilityService.NAME, "");
      cmd.addParam(agentID);
      cmd.addParam(where);
      cmd.addParam(newName);
      myCommandProcessor.process(cmd);
  }

  public void setPlatformAddresses(AID id) {
      GenericCommand cmd = new GenericCommand(jade.core.messaging.MessagingService.SET_PLATFORM_ADDRESSES, jade.core.messaging.MessagingService.NAME, "");
      cmd.addParam(id);
      myCommandProcessor.process(cmd);
  }
	
  public AID getAMS() {
    return (AID)theAMS.clone();
  }

  public AID getDefaultDF() {
    return (AID)theDefaultDF.clone();
  }

  public String getProperty(String key, String aDefault) {
  	return myProfile.getParameter(key, aDefault);
  }

  // Private and package scoped methods


    private void startAgent(AID agentID) {
	GenericCommand cmd = new GenericCommand(jade.core.management.AgentManagementService.REQUEST_START, jade.core.management.AgentManagementService.NAME, "");
	cmd.addParam(agentID);

	myCommandProcessor.process(cmd);
    }

    LADT getLocalAgents() {
	return localAgents;
    }


    /**
     * This method is used by the class AID in order to get the HAP.
     */
    public String getPlatformID() {
  	return AID.getPlatformID();
    }

    public Agent addLocalAgent(AID id, Agent a) throws AuthException {

	// --- This code could go into a Security Service, intercepting the agent creation...

	//#MIDP_EXCLUDE_BEGIN
	CertificateFolder agentCerts = a.getCertificateFolder();
	//#MIDP_EXCLUDE_END

	/*#MIDP_INCLUDE_BEGIN
	  CertificateFolder agentCerts = new CertificateFolder();
	  #MIDP_INCLUDE_END*/

	if(agentCerts.getIdentityCertificate() == null) {
	    AgentPrincipal principal = authority.createAgentPrincipal(id, AgentPrincipal.NONE);
	    IdentityCertificate identity = authority.createIdentityCertificate();
	    identity.setSubject(principal);
	    authority.sign(identity, certs);
	    agentCerts.setIdentityCertificate(identity);
	}

	// --- End of security code

	a.setToolkit(this);
	return localAgents.put(id, a);
    }

    public void powerUpLocalAgent(AID agentID, Agent instance) {
	Thread t = myResourceManager.getThread(ResourceManager.USER_AGENTS, agentID.getLocalName(), instance);
	instance.powerUp(agentID, t);
    }

    public void removeLocalAgent(AID id) {
	localAgents.remove(id);
    }

    public Agent acquireLocalAgent(AID id) {
	return localAgents.acquire(id);
    }

    public void releaseLocalAgent(AID id) {
	localAgents.release(id);
    }

    public void fillListFromMessageQueue(List messages, Agent a) {
	Iterator i = a.getMessageQueue().iterator();
	while (i.hasNext()) {
	    messages.add(i.next());
	}
    } 

    public void commitMigration(Agent instance) {
	instance.doGone();
	localAgents.remove(instance.getAID());
    }

    public void abortMigration(Agent instance) {
	instance.doExecute();
    }

    public void addAddressToLocalAgents(String address) {
	Agent[] allLocalAgents = localAgents.values();

        // Add the address to the AIDs of all local agents
        for(int j = 0; j < allLocalAgents.length; j++) {
            allLocalAgents[j].addPlatformAddress(address);
        }

        // Add the new addresses to the AMS and Default DF AIDs
        theAMS.addAddresses(address);
        theDefaultDF.addAddresses(address);
    }

    public void removeAddressFromLocalAgents(String address) {
	Agent[] allLocalAgents = localAgents.values();

        // Remove the address from the AIDs of all local agents
        for(int j = 0; j < allLocalAgents.length; j++) {
            allLocalAgents[j].removePlatformAddress(address);
        }

        // Remove the address from the AIDs of the AMS and the Default DF
        theAMS.removeAddresses(address);
        theDefaultDF.removeAddresses(address);
    }

    public boolean postMessageToLocalAgent(ACLMessage msg, AID receiverID) {
	Agent receiver = localAgents.acquire(receiverID);
	if(receiver == null) {
	    return false;
	}
	receiver.postMessage(msg);
	localAgents.release(receiverID);

	return true;
    }

    // Tells whether the given AID refers to an agent of this platform
    // or not.
    public boolean livesHere(AID id) {
	String hap = id.getHap();
	return CaseInsensitiveString.equalsIgnoreCase(hap, AID.getPlatformID());
    }

    public ContainerID getID() {
	return myID;
    }

    public MainContainerImpl getMain() {

	// FIXME: Temporary Hack. The cast shouldn't be necessary, because MainContainerProxy should go away
	if(myPlatform instanceof MainContainerImpl) {
	    return (MainContainerImpl)myPlatform;
	}
	else {
	    return null;
	}

    }


//#ALL_EXCLUDE_BEGIN  
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
//#ALL_EXCLUDE_END  
}
