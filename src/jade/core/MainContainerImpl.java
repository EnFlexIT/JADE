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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;

import jade.util.leap.Collection;
import jade.util.leap.HashMap;
import jade.util.leap.Iterator;
import jade.util.leap.List;
import jade.util.leap.ArrayList;
import jade.util.leap.LinkedList;
import jade.util.leap.Map;
import jade.util.leap.Set;

import jade.core.event.PlatformEvent;
import jade.core.event.MTPEvent;

import jade.domain.ams;
import jade.domain.df;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.domain.FIPAAgentManagement.AlreadyRegistered;
import jade.domain.FIPAAgentManagement.NotRegistered;

import jade.lang.acl.ACLMessage;

import jade.mtp.MTPException;
import jade.mtp.TransportAddress;
import jade.mtp.MTPDescriptor;

import jade.security.Authority;
import jade.security.AuthException;
import jade.security.AgentPrincipal;
import jade.security.ContainerPrincipal;
import jade.security.JADECertificate;
import jade.security.IdentityCertificate;
import jade.security.DelegationCertificate;
import jade.security.DelegationCertificate;
import jade.security.IdentityCertificate;
import jade.security.CertificateFolder;
import jade.security.PrivilegedExceptionAction;


/**
   This class is a concrete implementation of the JADE main container,
   providing runtime support to JADE agents, and the special, front
   end container where the AMS and the Default DF can run.
   This class cannot be instantiated from applications. Instead, the
   <code>Runtime.createMainContainer(Profile p)</code> method must be
   called.

   @see Runtime#createMainContainer(Profile p);

   @author Giovanni Rimassa - Universita` di Parma
   @version $Date$ $Revision$

*/
class MainContainerImpl implements Platform, AgentManager {
  
	// this variable holds a progressive number just used to name new containers
  private static int containersProgNo = 0;

  // The two mandatory system agents.
  private ams theAMS;
  private df defaultDF;

  private String platformID;
  private IMTPManager myIMTPManager;

  private List platformListeners = new LinkedList();
  private List platformAddresses = new LinkedList();
  private ContainerTable containers = new ContainerTable();
  private GADT platformAgents = new GADT();
  
  private Authority authority;
  private Profile myProfile;

	MainContainerImpl(Profile p) throws ProfileException {
	    myProfile = p;
		myIMTPManager = p.getIMTPManager();
		platformID = p.getParameter(Profile.PLATFORM_ID, null);
		if (platformID == null || platformID.equals("")) {
			try {
				// Build the PlatformID using the local host and port
				List l = myIMTPManager.getLocalAddresses();
				TransportAddress localAddr = (TransportAddress) l.get(0);
				platformID = localAddr.getHost()+":"+localAddr.getPort()+"/JADE";
			}
			catch (Exception e) {
				throw new ProfileException("Can't set PlatformID");
			}
		}

		try {
		    if (p.getParameter(Profile.OWNER, null) != null) {
			// if there is an owner for this container
			// then try to use the full implementation of security
			p.setParameter(Profile.MAINAUTH_CLASS,"jade.security.impl.PlatformAuthority");
			p.setParameter(Profile.AUTHORITY_CLASS,"jade.security.impl.ContainerAuthority");
		    }
			String type = p.getParameter(Profile.MAINAUTH_CLASS, null);
			if (type != null) {
				authority = (Authority)Class.forName(type).newInstance();
				authority.setName("main-authority");
				authority.init(p, this);
			}
		}
		catch (Exception e1) {
		    System.out.println("Some problems occured during the initialization of the security. JADE will continue execution by using dummy security.");
		    authority = null;
		    //			e1.printStackTrace();
		}
		
		try {
			if (authority == null) {
				authority = new jade.security.dummy.DummyAuthority();
				authority.setName("main-authority");
				authority.init(p, this);
			}
		}
		catch (Exception e2) {
			System.err.println("Could not init dummy platform authority");
			//e2.printStackTrace();
		}
		
	}
	
	////////////////////////////////////////////////////
	// Platform interface implementation
	////////////////////////////////////////////////////
	/**
	   Registers a container to the platform
	 */
	public void register(AgentContainerImpl ac, ContainerID cid, String username, byte[] password) throws IMTPException, AuthException {

		cid.setName(MAIN_CONTAINER_NAME);

		// Authenticate user
		ContainerPrincipal principal = getAuthority().createContainerPrincipal(cid, username);
		CertificateFolder certs = authority.authenticate(principal, password);
		authority.checkAction(Authority.PLATFORM_CREATE, principal, certs);
		authority.checkAction(Authority.CONTAINER_CREATE, principal, certs);

		// Set the container-principal
		ac.changeContainerPrincipal(certs);

		// Add the calling container as the main container
		containers.addContainer(cid, ac, principal);
		containersProgNo++;

		String agentOwnership = username;

		// Start the AMS
		theAMS = new ams(this);
		theAMS.setOwnership(agentOwnership);
		AgentPrincipal amsPrincipal = authority.createAgentPrincipal(ac.getAMS(), username);
		CertificateFolder amsCerts = authority.authenticate(amsPrincipal, password);
		theAMS.setPrincipal(amsCerts);
                try {
                    ac.initAgent(ac.getAMS(), theAMS, AgentContainer.START);
                    theAMS.waitUntilStarted();
                }
                catch(Exception e) {
                    throw new IMTPException("Exception during AMS startup", e);
                }

		// Notify the AMS about the main container existence
		fireAddedContainer(cid);
		fireChangedContainerPrincipal(cid, null, principal);

		// Start the Default DF
		defaultDF = new df();
		defaultDF.setOwnership(agentOwnership);
		AgentPrincipal dfPrincipal = authority.createAgentPrincipal(ac.getDefaultDF(), username);
		CertificateFolder dfCerts = authority.authenticate(dfPrincipal, password);
		defaultDF.setPrincipal(dfCerts);
                try {
                    ac.initAgent(ac.getDefaultDF(), defaultDF, AgentContainer.START);
                    defaultDF.waitUntilStarted();
                }
                catch(Exception e) {
                    throw new IMTPException("Exception during Default DF startup", e);
                }

		// Make itself accessible from remote JVMs
		myIMTPManager.remotize(this);
	}

	/**
	   Deregister a container from the platform
	 */
  public void deregister(AgentContainer ac) throws IMTPException {
    // Deregister yourself as a container
    containers.removeContainer(new ContainerID(MAIN_CONTAINER_NAME, null));

    // Kill every other container
    ContainerID[] allContainers = containers.names();
    for(int i = 0; i < allContainers.length; i++) {
      ContainerID targetID = allContainers[i];
	   	try {
      	AgentContainer target = containers.getContainer(targetID);
	   		// This call indirectly removes target
	     	target.exit(); 
	   	}
	   	catch(IMTPException imtp1) {
	     	System.out.println("Container " + targetID.getName() + " is unreachable. Ignoring...");
	     	try {
	     		removeContainer(targetID);
	     	}
	     	catch (IMTPException imtpe2) {
	     		// Should never happen as this is a local call
	     		imtpe2.printStackTrace();
	     	}
	   	}
	   	catch(NotFoundException nfe) {
	   		// Ignore the exception as we are removing a non-existing container
	     	System.out.println("Container " + targetID.getName() + " deos not exist. Ignoring...");
	   	}
    }

    // Make sure all containers are succesfully removed from the table...
    containers.waitUntilEmpty();

    // Stop the Default DF
    Agent systemAgent = defaultDF;
    systemAgent.doDelete();
    systemAgent.join();
    systemAgent.resetToolkit();

    // Stop the AMS
    systemAgent = theAMS;
    systemAgent.doDelete();
    systemAgent.join();
    systemAgent.resetToolkit();
    removeListener(theAMS);

    // Make itself no longer accessible from remote JVMs
    myIMTPManager.unremotize(this);
  }

  /**
     Dispatch a message to an agent living in the platform
   */
  public void dispatch(ACLMessage msg, AID receiverID) throws NotFoundException, UnreachableException {
  	// Directly use the GADT
  	while (true) {
	    AgentDescriptor ad = platformAgents.acquire(receiverID);
  	  if(ad == null) {
    	  throw new NotFoundException("Agent " + receiverID.getName() + " not found in GADT.");
    	}
    	AgentProxy ap = ad.getProxy();
    	platformAgents.release(receiverID);;
    	try {
		    ap.dispatch(msg);
		    // Dispatch OK --> break out the loop
		    return;
    	}
    	catch (NotFoundException nfe) {
    		// The agent can have been moved to another container just
    		// while we where dispatching the message --> try again
    	}
  	}
  }

	///////////////////////////////////////////////////////
	// MainContainer interface implementation.
  // All these methods can be called from a remote site
  // and can therefore throws IMTPException
	///////////////////////////////////////////////////////
	/**
	   Return the name of the platform
	 */
  public String getPlatformName() throws IMTPException {
    return platformID;
  }
	
  /**
     Add a new container to the platform
   */
	public String addContainer(AgentContainer ac, ContainerID cid, String username, byte[] password) throws IMTPException, AuthException {

		// Set the container name
		if (cid.getName().equals("No-Name")) { // no name => assign a new name
                     String name = AUX_CONTAINER_NAME + containersProgNo;
		     containersProgNo++;
                     cid.setName(name);
		} else { // if this name exists already, assign a new name
		    try {
			containers.getContainer(cid);
			String name = cid.getName() + containersProgNo;
			containersProgNo++;
			cid.setName(name);
		    } catch (NotFoundException e) { 
			// no container with this name exists, ok, go on.
		    }
		}

		// Authenticate user
		ContainerPrincipal principal = authority.createContainerPrincipal(cid, username);
		CertificateFolder certs = authority.authenticate(principal, password);
		authority.checkAction(Authority.CONTAINER_CREATE, principal, certs);
		
		// Set the container-principal
		ac.changeContainerPrincipal(certs);

		// add to the platform's container list
		containers.addContainer(cid, ac, principal);

		// Send all platform addresses to the new container
		ContainerID[] containerNames = containers.names();
		for(int i = 0; i < containerNames.length; i++) {
			ContainerID name = containerNames[i];
			
			try {
				AgentContainer cont = containers.getContainer(name);
					List mtps = containers.getMTPs(name);
					Iterator it = mtps.iterator();
					while(it.hasNext()) {
						MTPDescriptor mtp = (MTPDescriptor)it.next();
						ac.updateRoutingTable(AgentContainer.ADD_RT, mtp, cont);
					}
			}
			catch(NotFoundException nfe) {
				nfe.printStackTrace();
			}
		}

		// Spawn a blocking call to the remote container in a separate
		// thread. This is a failure notification technique.
		Thread t = new Thread(new FailureMonitor(ac, cid));
		t.start();

		// Notify listeners
		fireAddedContainer(cid);

		// Return the name given to the new container
		return cid.getName();
	}

	/**
	   Remove a container from the platform
	 */
  public void removeContainer(ContainerID cid) throws IMTPException {
    containers.removeContainer(cid);

    // Notify listeners
    fireRemovedContainer(cid);
  }

  /**
     Return the AgentContainer corresponding to a given ContainerID
   */
  public AgentContainer lookup(ContainerID cid) throws IMTPException, NotFoundException {
    AgentContainer ac = containers.getContainer(cid);
    return ac;
  }

  /**
     Notify the platform that an agent has just born on a container
   */
  public void bornAgent(AID name, ContainerID cid, CertificateFolder certs) throws IMTPException, NameClashException, NotFoundException, AuthException {
    // verify identity certificate
    authority.verify(certs.getIdentityCertificate());

    AgentDescriptor ad = new AgentDescriptor();
    AgentContainer ac = containers.getContainer(cid);
    AgentProxy ap = myIMTPManager.createAgentProxy(ac, name);
    ad.setProxy(ap);
    ad.setContainerID(cid);
    AgentPrincipal principal = (AgentPrincipal) certs.getIdentityCertificate().getSubject();
    ad.setPrincipal(principal);
    // CertificateFolder to be used by the AMS to perform actions on
    // behalf of (requested  by) the new agent
    ad.setAMSDelegation(prepareAMSDelegation(certs));
    // Registration to the With Pages service
    AMSAgentDescription amsd = new AMSAgentDescription();
    amsd.setName(name);
    amsd.setOwnership(principal.getOwnership());
    amsd.setState(AMSAgentDescription.ACTIVE);
    ad.setDescription(amsd);
    
    AgentDescriptor old = platformAgents.put(name, ad);
    // exception unless the old agent's container is dead.
    if(old != null) {
    	// There's already an agent with name 'name'
      AgentProxy oldProxy = old.getProxy();
      if (oldProxy != null) {
      	// The agent lives in the platform. Make sure it is reachable 
      	// and then restore it and throw an Exception 
      	try {
					oldProxy.ping(); 
					platformAgents.put(name, old);
					throw new NameClashException("Agent " + name + " already present in the platform ");
      	}
      	catch(UnreachableException ue) {
					System.out.println("Replacing a dead agent ...");
					fireDeadAgent(old.getContainerID(), name);
      	}
      }
      else {
      	// The agent lives outside the platform. Can't check whether it is 
      	// reachable. Restore it and throw an Exception
				platformAgents.put(name, old);
				throw new NameClashException("Agent " + name + " already registered to the platform ");
      }
    }

    // Notify listeners
    fireBornAgent(cid, name, principal);
  }
  
  /**
     Notify the platform that an agent has just died
   */
  public void deadAgent(AID name) throws IMTPException, NotFoundException {
    AgentDescriptor ad = platformAgents.acquire(name);
    if(ad == null)
      throw new NotFoundException("DeadAgent failed to find " + name);
    ContainerID cid = ad.getContainerID();
    platformAgents.remove(name);

    // Notify listeners
    fireDeadAgent(cid, name);
  }

  /**
     Notify the platform that an agent has just suspended
   */
  public void suspendedAgent(AID name) throws IMTPException, NotFoundException {
  	AgentDescriptor ad = platformAgents.acquire(name);
    if (ad == null)
      throw new NotFoundException("SuspendedAgent failed to find " + name);
    AMSAgentDescription amsd = ad.getDescription();
    if (amsd != null) {
    	amsd.setState(AMSAgentDescription.SUSPENDED);
    }
    ContainerID cid = ad.getContainerID();
		platformAgents.release(name);
		
    // Notify listeners
    fireSuspendedAgent(cid, name);
  }

  /**
     Notify the platform that an agent has just resumed
   */
  public void resumedAgent(AID name) throws IMTPException, NotFoundException {
    AgentDescriptor ad = platformAgents.acquire(name);
    if(ad == null)
      throw new NotFoundException("ResumedAgent failed to find " + name);
    AMSAgentDescription amsd = ad.getDescription();
    if (amsd != null) {
    	amsd.setState(AMSAgentDescription.ACTIVE);
    }
    ContainerID cid = ad.getContainerID();
		platformAgents.release(name);

    // Notify listeners
    fireResumedAgent(cid, name);
  }

  /**
     Notify the platform that the principal of an agent has changed
   */
	public void changedAgentPrincipal(AID name, CertificateFolder certs) throws IMTPException, NotFoundException {
		// FIXME: Probably we should let the AuthException pass through as in bornAgent()
		try {
			authority.verify(certs.getIdentityCertificate());
			
			AgentDescriptor ad = platformAgents.acquire(name);
			if (ad == null)
				throw new NotFoundException("ChangedAgentPrincipal failed to find " + name);			
			AgentPrincipal from = ad.getPrincipal();
			AgentPrincipal to = (AgentPrincipal)certs.getIdentityCertificate().getSubject();
			ad.setPrincipal(to);
			AMSAgentDescription amsd = ad.getDescription();
			if (amsd != null) {
				amsd.setOwnership(to.getOwnership());
			}
			ad.setAMSDelegation(prepareAMSDelegation(certs));
			ContainerID cid = ad.getContainerID();
			platformAgents.release(name);
			
			// Add the new principal to the principals table of all the containers. 
      // Synchronized to avoid additions/removals of containers during update
      synchronized(containers) {
				AgentContainer[] allContainers = containers.containers();
				for (int i = 0; i < allContainers.length; i++) {
					AgentContainer ac = allContainers[i];
					// FIXME: If some container is temporarily disconnected it will not be
					// notified. We should investigate the side-effects
					try {
						ac.changedAgentPrincipal(name, to);
					}
					catch (IMTPException imtpe) {
						imtpe.printStackTrace();
					}
				}
      }
			
			// Notify listeners
			fireChangedAgentPrincipal(cid, name, from, to);    
		}
		catch (AuthException ae) {
			System.err.println(ae.getMessage());
		}
	}
	
  /**
     Notify the platform that a new MTP has bocome active on a given container
   */
  public void newMTP(MTPDescriptor mtp, ContainerID cid) throws IMTPException {
    try {
      String containerName = cid.getName();
      String[] mtpAddrs = mtp.getAddresses();
      String mtpAddress = mtpAddrs[0];
      platformAddresses.add(mtpAddress);
      containers.addMTP(cid, mtp);
      AgentContainer target = containers.getContainer(cid);

			// Add the new MTP to the routing tables of all the containers. 
      // Synchronized to avoid additions/removals of containers during update
      synchronized(containers) {
				AgentContainer[] allContainers = containers.containers();
				for(int i = 0; i < allContainers.length; i++) {
	  			AgentContainer ac = allContainers[i];
					// FIXME: If some container is temporarily disconnected it will not be
					// notified. We should investigate the side-effects
	  			try {
		    		ac.updateRoutingTable(AgentContainer.ADD_RT, mtp, target);
	  			}
	  			catch (IMTPException imtpe) {
	  				System.out.println("Can't update routing table!");
	  			}
      	}
      }

      // Update the AMS-descriptions of all registered agents living in the platform
	    AID[] allIds = platformAgents.keys();
	    for (int i = 0; i < allIds.length; ++i) {
	    	AgentDescriptor ad = platformAgents.acquire(allIds[i]);
	      AMSAgentDescription dsc = ad.getDescription();	
	      if (dsc != null && ad.getProxy() != null) {
					AID id = dsc.getName();
					id.addAddresses(mtpAddress);
	      } 
	    	platformAgents.release(allIds[i]);
	  	}
      
      // Notify listeners (typically the AMS)
      fireAddedMTP(mtp, cid);
    }
    catch(NotFoundException nfe) {
      System.out.println("Error: the container " + cid.getName() + " was not found.");
    }
  }

  /**
     Notify the platform that an MTP is no longer active on a given container
   */
  public void deadMTP(MTPDescriptor mtp, ContainerID cid) throws IMTPException {
    try {
      String containerName = cid.getName();
      String[] mtpAddrs = mtp.getAddresses();
      String mtpAddress = mtpAddrs[0];
      platformAddresses.remove(mtpAddress);
      containers.removeMTP(cid, mtp);
      AgentContainer target = containers.getContainer(cid);

			// Remove the dead MTP from the routing tables of all the containers. 
      // Synchronized to avoid additions/removals of containers during update
      synchronized(containers) {
				AgentContainer[] allContainers = containers.containers();
				for(int i = 0; i < allContainers.length; i++) {
	  			AgentContainer ac = allContainers[i];
					// FIXME: If some container is temporarily disconnected it will not be
					// notified. We should investigate the sideeffects
	  			try {
		    		ac.updateRoutingTable(AgentContainer.DEL_RT, mtp, target);
	  			}
	  			catch (IMTPException imtpe) {
	  				System.out.println("Warning: Container has just died or disconnected. Can't update routing table!");
	  			}
      	}
      }

      // Update the AMS-descriptions of all agents living in the platform
	    AID[] allIds = platformAgents.keys();
	    for (int i = 0; i < allIds.length; ++i) {
	    	AgentDescriptor ad = platformAgents.acquire(allIds[i]);
	      AMSAgentDescription dsc = ad.getDescription();	
	      if (ad.getProxy() != null) {
					AID id = dsc.getName();
					id.removeAddresses(mtpAddress);
	      } 
	    	platformAgents.release(allIds[i]);
	  	}
      
      // Notify listeners (typically the AMS)
      fireRemovedMTP(mtp, cid);
    }
    catch(NotFoundException nfe) {
      System.out.println("Error: the container " + cid.getName() + " was not found.");
    }
  }

  /**
     Retrieve the proxy to be used to dispatch messages to a given agent
   */
  public AgentProxy getProxy(AID agentID) throws IMTPException, NotFoundException {
    AgentProxy ap;
    AgentDescriptor ad = platformAgents.acquire(agentID);

    if(ad == null) {
      throw new NotFoundException("getProxy() failed to find " + agentID.getName());
    }
    else {
      ap = ad.getProxy();
      platformAgents.release(agentID);
      try {
				ap.ping();
      }
      catch(UnreachableException ue) {
				throw new NotFoundException("Container for " + agentID.getName() + " is unreachable");
      }
      return ap;
    }
  }

  /**
     Transfer the "identity" of a moving agent from a source container
     to a destination container. From this point on messages for that 
     agent will be routed to the destination container.
   */
  public boolean transferIdentity(AID agentID, ContainerID src, ContainerID dest) throws IMTPException, NotFoundException {
    AgentDescriptor ad = platformAgents.acquire(agentID);
    if(ad == null)
      throw new NotFoundException("transferIdentity() unable to find agent " + agentID.getName());
    AgentContainer srcAC = lookup(src);
    AgentContainer destAC = lookup(dest);
    try {
      srcAC.ping(false);
      destAC.ping(false);
    }
    catch(IMTPException re) {
      // Abort transaction
      return false;
    }

    // Commit transaction and notify listeners
    ad.setProxy(myIMTPManager.createAgentProxy(destAC, agentID));
    ad.setContainerID(dest);
    fireMovedAgent(src, dest, agentID);
    platformAgents.release(agentID);
    return true;
  }
  
  /**
     Return the principal of an agent
   */
	public AgentPrincipal getAgentPrincipal(AID agentID) throws IMTPException, NotFoundException {
		return getPrincipal(agentID);
	}

	/**
	   Make the platform authority sign a certificate
	 */
  public JADECertificate sign(JADECertificate certificate, CertificateFolder certs) throws IMTPException, AuthException {
    authority.sign(certificate, certs);
    return certificate;
  }
  
  /**
     Return the public key of the platform
   */
  public byte[] getPublicKey() throws IMTPException {
  	return authority.getPublicKey();
  }

  //////////////////////////////////////////////////////////////////////
  // AgentManager interface implementation.
  // These methods are called by the AMS to execute the actions that can 
  // be requested by agents in the platform.
  //////////////////////////////////////////////////////////////////////
  /**
     Create an agent on a given container
     @see AgentManager#create(String agentName, String className, String arguments[], ContainerID cid, String ownership, CertificateFolder certs) throws UnreachableException, AuthException, NotFoundException
   */
  public void create(final String name, final String className, final String args[], ContainerID cid, final String ownership, final CertificateFolder certs) throws UnreachableException, AuthException, NotFoundException {
  	// Get the container where to create the agent
  	// If it is not specified, assume it is the Main
  	AgentContainer where = null;
  	if (cid == null || cid.getName() == null) {
  		cid = new ContainerID(MAIN_CONTAINER_NAME, null);
  	} 
	where = containers.getContainer(cid);
  		
    // Check permissions
    authority.checkAction(Authority.AGENT_CREATE, (AgentPrincipal)certs.getIdentityCertificate().getSubject(), null);
    authority.checkAction(Authority.CONTAINER_CREATE_IN, getPrincipal(cid), null);

    // Do the action (we need again full permissions to execute a remote call)
    final AgentContainer ac = where;
    try {
	    authority.doPrivileged(new PrivilegedExceptionAction() {
  	  	public Object run() throws IMTPException {
    			ac.createAgent(new AID(name, AID.ISLOCALNAME), className, args, ownership, certs, AgentContainer.START);
    			return null;
    		}
    	} );
    }
    catch (IMTPException re) {
      throw new UnreachableException(re.getMessage());
    }
    catch (Exception e) {
			// Should never happen
    	e.printStackTrace();
    }
  }

  /**
     Kill an agent wherever it is
   */
	public void kill(final AID agentID) throws NotFoundException, UnreachableException, AuthException {
		// Check permissions
		authority.checkAction(Authority.CONTAINER_KILL_IN, getPrincipal(getContainerID(agentID)), null);
		authority.checkAction(Authority.AGENT_KILL, getPrincipal(agentID), null);
		
		// Do the action (we need again full permissions to execute a remote call)
		try {
			authority.doPrivileged(new PrivilegedExceptionAction() {
		  	public Object run() throws IMTPException, NotFoundException, AuthException {
	  			getContainerFromAgent(agentID).killAgent(agentID);
	  			return null;
				}
			});
		}
		catch (IMTPException re) {
			throw new UnreachableException(re.getMessage());
		}
    catch(NotFoundException nfe) {
      // Forward the exception
      throw nfe;
    }
		catch (Exception e) {
    	// Should never happen
			e.printStackTrace();
		}
	}

  /**
     Suspend an agent wherever it is
   */
	public void suspend(final AID agentID) throws NotFoundException, UnreachableException, AuthException {
		// Check permissions
		authority.checkAction(Authority.AGENT_SUSPEND, getPrincipal(agentID), null);
		
		// Do the action (we need again full permissions to execute a remote call)
		try {
			authority.doPrivileged(new PrivilegedExceptionAction() {
		  	public Object run() throws IMTPException, NotFoundException, AuthException {
	  			getContainerFromAgent(agentID).suspendAgent(agentID);
	  			return null;
				}
			});
		}
		catch (IMTPException re) {
			throw new UnreachableException(re.getMessage());
		}
		catch (Exception e) {
			// Should never happen
			e.printStackTrace();
		}
	}

  /**
     Resume an agent wherever it is
   */
	public void activate(final AID agentID) throws NotFoundException, UnreachableException, AuthException {
		// Check permissions
		authority.checkAction(Authority.AGENT_RESUME, getPrincipal(agentID), null);
		
		// Do the action (we need again full permissions to execute a remote call)
		try {
			authority.doPrivileged(new PrivilegedExceptionAction() {
		  	public Object run() throws IMTPException, NotFoundException, AuthException {
	  			getContainerFromAgent(agentID).resumeAgent(agentID);
	  			return null;
				}
			});
		}
		catch (IMTPException re) {
			throw new UnreachableException(re.getMessage());
		}
		catch (Exception e) {
			// Should never happen
			e.printStackTrace();
		}
	}

  /**
     Put an agent in the WAITING state wherever it is
   */
  public void wait(AID agentID, String password) throws NotFoundException, UnreachableException {
    try {
      AgentContainer ac = getContainerFromAgent(agentID);
      ac.waitAgent(agentID);
    }
    catch (IMTPException re) {
      throw new UnreachableException(re.getMessage());
    }
  }

  /**
     Wake-up an agent wherever it is
   */
  public void wake(AID agentID, String password) throws NotFoundException, UnreachableException {
    try {
      AgentContainer ac = getContainerFromAgent(agentID);
      ac.wakeAgent(agentID);
    }
    catch (IMTPException re) {
      throw new UnreachableException(re.getMessage());
    }
  }

  /**
     Move an agent to a given destination
   */
  public void move(final AID agentID, final Location where) throws NotFoundException, UnreachableException, AuthException {
 		ContainerID from = getContainerID(agentID);
 		ContainerID to = (ContainerID)where;
		
 		// Check whether the destination exists
 		containers.getContainer(to);
		
 		// Check permissions
		authority.checkAction(Authority.CONTAINER_MOVE_FROM, getPrincipal(from), null);
		authority.checkAction(Authority.CONTAINER_MOVE_TO, getPrincipal(to), null);
		authority.checkAction(Authority.AGENT_MOVE, getPrincipal(agentID), null);
    
		// Do the action (we need again full permissions to execute a remote call)
		try {
			authority.doPrivileged(new PrivilegedExceptionAction() {
		  	public Object run() throws IMTPException, NotFoundException, AuthException {
	  			getContainerFromAgent(agentID).moveAgent(agentID, where);
	  			return null;
				}
			});
    }
    catch (IMTPException re) {
      throw new UnreachableException(re.getMessage());
    }
    catch (NotFoundException ne) {
			// Forward the exception
    	throw ne;
    }
    catch (AuthException aue) {
    	// Forward the exception
			throw aue;
    }
    catch (Exception e) {
			// Should never happen...
			e.printStackTrace();
    }
  }

  /**
     Clone an agent to a given destination
   */
  public void copy(final AID agentID, final Location where, final String newName) throws NotFoundException, UnreachableException, AuthException {
 		ContainerID from = getContainerID(agentID);
 		ContainerID to = (ContainerID)where;
		
 		// Check whether the destination exists
 		containers.getContainer(to);

 		// Check permissions
 		authority.checkAction(Authority.AGENT_CLONE, getPrincipal(agentID), null);
		authority.checkAction(Authority.CONTAINER_CLONE_FROM, getPrincipal(from), null);
		authority.checkAction(Authority.CONTAINER_CLONE_TO, getPrincipal(to), null);
    
		// Do the action (we need again full permissions to execute a remote call)
		try {
			authority.doPrivileged(new jade.security.PrivilegedExceptionAction() {
		  	public Object run() throws IMTPException, NotFoundException, AuthException {
	  			getContainerFromAgent(agentID).copyAgent(agentID, where, newName);
	  			return null;
				}
			});
    }
    catch (IMTPException re) {
      throw new UnreachableException(re.getMessage());
    }
    catch (NotFoundException ne) {
			throw ne;
    }
    catch (AuthException aue) {
			throw aue;
    }
    catch (Exception e) {
			// Should never happen...
			e.printStackTrace();
    }
  }

  /** 
     Kill a given container
   */
  public void killContainer(final ContainerID cid) throws NotFoundException, AuthException {
  	// Check permissions
    authority.checkAction(Authority.CONTAINER_KILL, getPrincipal(cid), null);
        
    // Do the action in a separate thread to avoid deadlock (we need 
    // again full permissions to start a thread and execute a remote call)
    final AgentContainer ac = containers.getContainer(cid);
    try {
			authority.doPrivileged(new jade.security.PrivilegedExceptionAction() {
			  public Object run() {
			  	Thread auxThread = new Thread() {
			 			public void run() {
			   			try {
			     			 ac.exit();
			   			}
			   			catch (IMTPException imtp1) {
			     			System.out.println("Container " + cid.getName() + " is unreachable. Ignoring...");
			     			handleCrash(cid);
			     			try {
			     				removeContainer(cid);
			     			}
			     			catch (IMTPException imtpe2) {
			     				// Should never happen as this is a local call
			     				imtpe2.printStackTrace();
			     			}
			   			}
			 			}
			    };
			  		
			    auxThread.start();
		  		return null;
				}
			});
    }
    catch (Exception e) {
      // Should never happen
      e.printStackTrace();
    }
  }

  /** 
     Install a new MTP on a given container
   */
  public MTPDescriptor installMTP(String address, ContainerID cid, String className) throws NotFoundException, UnreachableException, MTPException {
    String containerName = cid.getName();
    AgentContainer target = containers.getContainer(cid);
    try {
      return target.installMTP(address, className);
    }
    catch(IMTPException re) {
      throw new UnreachableException("Container " + containerName + " is unreachable.");
    }

  }

  /** 
     Uninstall an MTP on a given container
   */
  public void uninstallMTP(String address, ContainerID cid) throws NotFoundException, UnreachableException, MTPException {
    String containerName = cid.getName();
    AgentContainer target = containers.getContainer(cid);
    try {
      target.uninstallMTP(address);
    }
    catch(IMTPException re) {
      throw new UnreachableException("Container " + containerName + " is unreachable.");
    }
  }

  /**
     Change the principal of an agent
   */
	public void take(final AID agentID, final String username, final byte[] password) throws NotFoundException, UnreachableException, AuthException {
		// Check permissions	
		authority.checkAction(Authority.AGENT_TAKE, getPrincipal(agentID), null);

		// Create the certificate folder for the new principal
		final AgentPrincipal principal = authority.createAgentPrincipal(agentID, username);
		final CertificateFolder certs = authority.authenticate(principal, password);
		
		// Do the action (we need again full permissions to execute a remote call)
		try {	
			authority.doPrivileged(new jade.security.PrivilegedExceptionAction() {
		  	public Object run() throws IMTPException, NotFoundException {
	  			getContainerFromAgent(agentID).changeAgentPrincipal(agentID, certs);
	  			return null;
				}
			});
		}
		catch (IMTPException re) {
			throw new UnreachableException(re.getMessage());
		}
		catch (Exception e) {
			// Should never happen
			e.printStackTrace();
		}
	}

	/**
	   Activate sniffing on a given agent
	 */
  public void sniffOn(AID snifferName, List toBeSniffed) throws NotFoundException, UnreachableException  {
    Iterator it = toBeSniffed.iterator();
    try {
      while(it.hasNext()) {
	AID id = (AID)it.next();
	AgentContainer ac = getContainerFromAgent(id);
	ac.enableSniffer(snifferName, id);
      }
    }
    catch(IMTPException re) {
      throw new UnreachableException(re.getMessage());
    }

  }

	/**
	   Deactivate sniffing on a given agent
	 */
  public void sniffOff(AID snifferName, List notToBeSniffed) throws NotFoundException, UnreachableException {
    Iterator it = notToBeSniffed.iterator();
    try {
      while(it.hasNext()) {
	AID id = (AID)it.next();
	AgentContainer ac = getContainerFromAgent(id);
	ac.disableSniffer(snifferName, id);
      }
    }
    catch(IMTPException re) {
      throw new UnreachableException(re.getMessage());
    }

  }

	/**
	   Activate debugging on a given agent
	 */
  public void debugOn(AID debuggerName, List toBeDebugged) throws NotFoundException, UnreachableException {
    Iterator it = toBeDebugged.iterator();
    try {
      while(it.hasNext()) {
	AID id = (AID)it.next();
	AgentContainer ac = getContainerFromAgent(id);
	ac.enableDebugger(debuggerName, id);
      }
    }
    catch(IMTPException re) {
      throw new UnreachableException(re.getMessage());
    }
  }

	/**
	   Deactivate debugging on a given agent
	 */
  public void debugOff(AID debuggerName, List notToBeDebugged) throws NotFoundException, UnreachableException {
    Iterator it = notToBeDebugged.iterator();
    try {
      while(it.hasNext()) {
	AID id = (AID)it.next();
	AgentContainer ac = getContainerFromAgent(id);
	ac.disableDebugger(debuggerName, id);
      }
    }
    catch(IMTPException re) {
      throw new UnreachableException(re.getMessage());
    }
  }

  /**
     Register an agent to the White Pages service of this platform
   */
  public void amsRegister(AMSAgentDescription dsc) throws AlreadyRegistered, AuthException {
  	// Mandatory slots have already been checked
  	AID agentID = dsc.getName();
  	
		// Check permissions	
		authority.checkAction(Authority.AMS_REGISTER, getPrincipal(agentID), null);

  	AgentDescriptor ad = platformAgents.acquire(agentID);
  	if (ad == null) {
  		System.out.println("No descriptor found for agent "+agentID);
  		// This is a foreign agent registering to this platform
  		ad = new AgentDescriptor();
  		ad.setDescription(dsc);
  		platformAgents.put(agentID, ad);
  	}
  	else {
  		if (ad.getDescription() == null) {
  			System.out.println("Descriptor with null AMSD found for agent "+agentID);
  			// This is an agent living in the platform that had previously deregistered 
  			ad.setDescription(dsc);
  			platformAgents.release(agentID);
  		}
  		else {
  			System.out.println("Descriptor with NON null AMSD found for agent "+agentID);
  			// This agent is already registered --> Exception
  			platformAgents.release(agentID);
	  		throw new AlreadyRegistered();
  		}
  	}
  }
  
  /**
     Deregister an agent from the White Pages service of this platform
   */
  public void amsDeregister(AMSAgentDescription dsc) throws NotRegistered, AuthException {
  	// Mandatory slots have already been checked
  	AID agentID = dsc.getName();
  	
		// Check permissions	
		authority.checkAction(Authority.AMS_DEREGISTER, getPrincipal(agentID), null);
  	
		AgentDescriptor ad = platformAgents.acquire(agentID);
  	if (ad != null) {
  		if (ad.getDescription() != null) {
	  		if (ad.getProxy() != null) {
	  			// This is an agent living in the platform --> just clear its registration
	  			ad.setDescription(null);
	  			platformAgents.release(agentID);
	  		}
	  		else {
	  			// This is a foreign agent --> remove the descriptor completely
	  			platformAgents.remove(agentID);
	  		}
	  		return;
  		}
  	}
  	// This agent was not registered --> Exception
	  throw new NotRegistered();
  }
  
  /**
     Modify the registration of an agent to the White Pages service of 
     this platform.
     If the modification implies a change in the agent state (and the agent
     lives in the platform) --> force that chage 
     If the modification implies a change in the agent ownership (and the agent
     lives in the platform) --> force that chage 
   */
  public void amsModify(AMSAgentDescription dsc) throws NotRegistered, NotFoundException, UnreachableException, AuthException {
  	// Mandatory slots have already been checked
  	AID agentID = dsc.getName();
  	
		// Check permissions	
		authority.checkAction(Authority.AMS_MODIFY, getPrincipal(agentID), null);

  	AgentDescriptor ad = platformAgents.acquire(agentID);
  	if (ad != null) {
  		AMSAgentDescription oldDsc = ad.getDescription();
  		if (oldDsc != null) {
  			ad.setDescription(dsc);
  			String newState = dsc.getState();
  			String newOwnership = dsc.getOwnership();
  			if (newOwnership == null) {
  				newOwnership = oldDsc.getOwnership();
  			}
  			platformAgents.release(agentID);
	  		if (ad.getProxy() != null) {
	  			// This is an agent living in the platform --> if necessary
	  			// force changes in agent state and ownership 
	  			if (AMSAgentDescription.SUSPENDED.equals(newState) && !AMSAgentDescription.SUSPENDED.equals(oldDsc.getState())) {
	  				suspend(agentID);
	  			}
	  			if (AMSAgentDescription.ACTIVE.equals(newState) && !AMSAgentDescription.ACTIVE.equals(oldDsc.getState())) {
	  				activate(agentID);
	  			}
	  			if (newOwnership != null && newOwnership != oldDsc.getOwnership()) {
						byte[] password = Agent.extractPassword(newOwnership);
						String username = Agent.extractUsername(newOwnership);
						take(agentID, username, password);
	  			}
	  		}
	  		return;
  		}
  	}
  	// This agent was not registered --> Exception
	  throw new NotRegistered();
  }
  
  /**
     Searches the White Pages for agents whose description matches a given
     template.
   */
  public List amsSearch(AMSAgentDescription template, long maxResults) {
  	List results = new ArrayList();
  	AID[] ids = platformAgents.keys();
  	for (int i = 0; i < ids.length; ++i) {
  		try {
				AMSAgentDescription amsd = getAMSDescription(ids[i]); 
				if (match(template, amsd)) {
					results.add(amsd);
					if (results.size() >= maxResults) {
						break;
					}
				}
  		}
  		catch (NotFoundException nfe) {
  			// The agent disappeared while we were looping. Ignore it
  		}
  	}
  	return results;
  }
  
  /**
     Return the IDs of all containers in the platform
   */
  public ContainerID[] containerIDs() {
  	return containers.names();
  }

  /**
     Return the IDs of all agents in the platform
   */
  public AID[] agentNames() {
    return platformAgents.keys();
  }

  /**
     Return all MTPs in a given container
   */
  public List containerMTPs(ContainerID cid) throws NotFoundException {
  	return containers.getMTPs(cid);
  }

  /**
     Return all agents living on a container
   */
  public List containerAgents(ContainerID cid) throws NotFoundException {
  	List agents = new ArrayList();
    AID[] allIds = platformAgents.keys();

    for (int i = 0; i < allIds.length; ++i) {
    	AID    id = allIds[i];
    	AgentDescriptor ad = platformAgents.acquire(id);
      ContainerID cid1 = ad.getContainerID();

      if (cid.equals(cid1)) {
				agents.add(id);
      } 
    	platformAgents.release(id);
  	} 
  	return agents;
  }
  
  /**
     Return the ID of the container an agent lives in
   */
  public ContainerID getContainerID(AID agentID) throws NotFoundException {
    AgentDescriptor ad = platformAgents.acquire(agentID);
    if(ad == null)
      throw new NotFoundException("getContainerID() failed to find agent " + agentID.getName());
    ContainerID result = ad.getContainerID();
    platformAgents.release(agentID);
    return result;
  }
  
  /**
     Return the AMS description of an agent
   */
  public AMSAgentDescription getAMSDescription(AID agentID) throws NotFoundException {
    AgentDescriptor ad = platformAgents.acquire(agentID);
    if(ad == null)
      throw new NotFoundException("getAMSDescription() failed to find agent " + agentID.getName());
    AMSAgentDescription amsd = ad.getDescription();
    platformAgents.release(agentID);
    return amsd;
  }
  
  /**
     Return the delegation certificate to be used by the AMS to executed
     actions on behalf of (requested by) a given agent
   */
  public CertificateFolder getAMSDelegation(AID agentID) {
    AgentDescriptor ad = platformAgents.acquire(agentID);
    if (ad == null) {
    	// Create a CertificateFolder with no delegated permissions
    	// FIXME: With should give the permissions of the AgentPrincipal.NONE user
	    try {
	    	AgentPrincipal amsPrincipal = theAMS.getPrincipal();
		    DelegationCertificate amsDelegation = authority.createDelegationCertificate();
		    amsDelegation.setSubject(amsPrincipal);
		    authority.sign(amsDelegation, theAMS.getCertificateFolder());
		    return new CertificateFolder(theAMS.getCertificateFolder().getIdentityCertificate(), amsDelegation);
	    }
	    catch (AuthException ae) {
	    	// Should never happen
	    	ae.printStackTrace();
	    }
    }
    CertificateFolder cf = ad.getAMSDelegation();
    platformAgents.release(agentID);
    return cf;
  }

  /**
     Return the platform main authority
   */
	public Authority getAuthority() {
		return authority;
	}
  
  /**
     Add a listener of platform events
   */
  public void addListener(AgentManager.Listener l) {
    platformListeners.add(l);
  }

  /**
     Remove a listener of platform events
   */
  public void removeListener(AgentManager.Listener l) {
    platformListeners.remove(l);
  }

  ////////////////////////////////////////////////
  // Private utility methods
  ////////////////////////////////////////////////
  private AgentContainer getContainerFromAgent(AID agentID) throws NotFoundException {
    return containers.getContainer(getContainerID(agentID));
  }

  private CertificateFolder prepareAMSDelegation(CertificateFolder certs) throws AuthException {
    AgentPrincipal amsPrincipal = theAMS.getPrincipal();
    DelegationCertificate amsDelegation = authority.createDelegationCertificate();
    amsDelegation.setSubject(amsPrincipal);
    for (int c = 0; c < certs.getDelegationCertificates().size(); c++) {
      amsDelegation.addPermissions(((DelegationCertificate)certs.getDelegationCertificates().get(c)).getPermissions());
    }
    authority.sign(amsDelegation, certs);
    return new CertificateFolder(theAMS.getCertificateFolder().getIdentityCertificate(), amsDelegation);
  }

	public AgentPrincipal getPrincipal(AID agentID) {
		AgentPrincipal ap;
		AgentDescriptor ad = platformAgents.acquire(agentID);

		if (ad == null) {
			return authority.createAgentPrincipal(agentID, AgentPrincipal.NONE);
		}
		else {
			ap = ad.getPrincipal();
			platformAgents.release(agentID);
			return ap;
		}
	}
	
  private ContainerPrincipal getPrincipal(ContainerID cid) {
	 	ContainerPrincipal cp = null;
	 	try {
			cp = containers.getPrincipal(cid);
	 	}
	 	catch (NotFoundException nfe) {
	 	}
	 	if (cp == null) {
	 		cp = authority.createContainerPrincipal(cid, ContainerPrincipal.NONE);
	 	}
	 	return cp;
  }
  
  /**
     Inner class to detect AgentContainer failures
   */
  private class FailureMonitor implements Runnable {

    private AgentContainer target;
    private ContainerID targetID;
    private boolean active = true;

    public FailureMonitor(AgentContainer ac, ContainerID cid) {
      target = ac;
      targetID = cid;
    }

    public void run() {
      while(active) {
				try {
	  			target.ping(true); // Hang on this call
	  			active = false;
	  			System.out.println("PING from container "+targetID.getName()+" returned normally");
				}
				catch(IMTPException imtpe1) { // Connection down
	  			System.out.println("PING from container "+targetID.getName()+" exited with exception");
	  			try {
	    			target.ping(false); // Try a non blocking ping to check
	  			}
	  			catch(IMTPException imtpe2) { // Object down
						handleCrash(targetID);
	    			active = false;
	  			}
				}
				catch(Throwable t) {
	  			t.printStackTrace();
				}
      } // END of while
      
      // If we reach this point the container is no longer active -->
      // remove it
	    try {
	     	removeContainer(targetID);
	    }
	    catch (IMTPException imtpe) {
	    	// Should never happen as this is a local call
	     	imtpe.printStackTrace();
	    }
    } // END of method run()
  
  } // END of inner class FailureMonitor

  private void handleCrash(ContainerID crashedID) {
    // If a container has crashed all its agents
    // appear to be still alive in the GADT --> Clean them 
    AID[] allIds = platformAgents.keys();

    for (int i = 0; i < allIds.length; ++i) {
    	AID    id = allIds[i];
    	AgentDescriptor ad = platformAgents.acquire(id);
      ContainerID cid = ad.getContainerID();

      if (crashedID.equals(cid)) {
      	// This agent was living in the container that has crashed
        // --> It must be cleaned
        platformAgents.remove(id);
        fireDeadAgent(crashedID, id);
    	} 
    	else {
    		platformAgents.release(id);
    	}
  	} 
  	
  	// Also notify listeners and other containers that the MTPs that 
  	// were active on the crashed container are no longer available
  	try {
	  	ContainerID[] names = containers.names();
  		AgentContainer crashed = containers.getContainer(crashedID);
  		List mtps = containers.getMTPs(crashedID);
  		Iterator it = mtps.iterator();
  		while (it.hasNext()) {
  			MTPDescriptor dsc = (MTPDescriptor) it.next();
  			fireRemovedMTP(dsc, crashedID);
  			for (int i = 0; i < names.length; ++i) {
  				if (!crashedID.equals(names[i])) {
  					AgentContainer ac = containers.getContainer(names[i]);
  					ac.updateRoutingTable(AgentContainer.DEL_RT, dsc, crashed);
  				}
  			}
  		}
  	}
  	catch (Exception e) {
  		// Just print a warning
  		System.out.println("Error cleaning MTPs of crashed container");
  	}
  }

  private boolean match(AMSAgentDescription templateDesc, AMSAgentDescription factDesc) {
		try {
		  String o1 = templateDesc.getOwnership();
		  if(o1 != null) {
		    String o2 = factDesc.getOwnership();
		    if((o2 == null) || (!o1.equalsIgnoreCase(o2)))
		      return false;
		  }
	
		  String s1 = templateDesc.getState();
		  if(s1 != null) {
		    String s2 = factDesc.getState();
		    if((s2 == null) || (!s1.equalsIgnoreCase(s2)))
		      return false;
		  }
	
		  AID id1 = templateDesc.getName();
		  if(id1 != null) {
		    AID id2 = factDesc.getName();
		    if((id2 == null) || (!matchAID(id1, id2)))
		      return false;
		  }
	
		  return true;
		}
		catch (ClassCastException cce) {
		  return false;
		}
  }
  
  // Helper method to match two Agent Identifiers
  private final boolean matchAID(AID template, AID fact) {
    // Match the GUID in the ':name' slot
    String templateName = template.getName();
    if(templateName != null) {
      String factName = fact.getName();
      if((factName == null) || (!templateName.equalsIgnoreCase(factName)))
				return false;
    }

    // Match the address sequence. See 'FIPA Agent Management Specification, Sect. 6.4.2.1'
    Iterator itTemplate = template.getAllAddresses();
    Iterator itFact = fact.getAllAddresses();

    // All the elements in the template sequence must appear in the
    // fact sequence, in the same order
    while(itTemplate.hasNext()) {
      String templateAddr = (String)itTemplate.next();

      // Search 'templateAddr' into the remaining part of the fact sequence
      boolean found = false;
      while(!found && itFact.hasNext()) {
				String factAddr = (String)itFact.next();
				found = templateAddr.equalsIgnoreCase(factAddr);
      }
      if(!found) // An element of the template does not appear in the fact sequence
				return false;
    }

    // Match the resolvers sequence. See 'FIPA Agent Management Specification, Sect. 6.4.2.1'
    itTemplate = template.getAllResolvers();
    itFact = fact.getAllResolvers();

    while(itTemplate.hasNext()) {
      AID templateRes = (AID)itTemplate.next();

      // Search 'templateRes' into the remaining part of the fact sequence
      boolean found = false;
      while(!found && itFact.hasNext()) {
				AID factRes = (AID)itFact.next();
				found = matchAID(templateRes, factRes); // Recursive call
      }
      if(!found) // An element of the template does not appear in the fact sequence
				return false;
    }
    
    return true;
  }

  /////////////////////////////////////////////////////////////////
  // Private methods to notify platform listeners of significant 
  // events.
  /////////////////////////////////////////////////////////////////

  private void fireAddedContainer(ContainerID cid) {
    PlatformEvent ev = new PlatformEvent(PlatformEvent.ADDED_CONTAINER, cid);
    for(int i = 0; i < platformListeners.size(); i++) {
      AgentManager.Listener l = (AgentManager.Listener)platformListeners.get(i);
      l.addedContainer(ev);
    }
  }

  private void fireRemovedContainer(ContainerID cid) {
    PlatformEvent ev = new PlatformEvent(PlatformEvent.REMOVED_CONTAINER, cid);

    for(int i = 0; i < platformListeners.size(); i++) {
      AgentManager.Listener l = (AgentManager.Listener)platformListeners.get(i);
      l.removedContainer(ev);
    }
  }

  private void fireChangedContainerPrincipal(ContainerID cid, ContainerPrincipal from, ContainerPrincipal to) {
  	if (from == null) {
  		from = authority.createContainerPrincipal(cid, ContainerPrincipal.NONE);
  	}
    PlatformEvent ev = new PlatformEvent(PlatformEvent.CHANGED_CONTAINER_PRINCIPAL, null, cid, from, to);
    for (int i = 0; i < platformListeners.size(); i++) {
      AgentManager.Listener l = (AgentManager.Listener)platformListeners.get(i);
      l.changedContainerPrincipal(ev);
    }
  }

  private void fireBornAgent(ContainerID cid, AID agentID, AgentPrincipal principal) {
    PlatformEvent ev = new PlatformEvent(PlatformEvent.BORN_AGENT, agentID, cid, null, principal);

    for(int i = 0; i < platformListeners.size(); i++) {
      AgentManager.Listener l = (AgentManager.Listener)platformListeners.get(i);
      l.bornAgent(ev);
    }
  }

  private void fireDeadAgent(ContainerID cid, AID agentID) {
    PlatformEvent ev = new PlatformEvent(PlatformEvent.DEAD_AGENT, agentID, cid);

    for(int i = 0; i < platformListeners.size(); i++) {
      AgentManager.Listener l = (AgentManager.Listener)platformListeners.get(i);
      l.deadAgent(ev);
    }
  }

  private void fireSuspendedAgent(ContainerID cid, AID agentID) {
    PlatformEvent ev = new PlatformEvent(PlatformEvent.SUSPENDED_AGENT, agentID, cid);

    for(int i = 0; i < platformListeners.size(); i++) {
      AgentManager.Listener l = (AgentManager.Listener)platformListeners.get(i);
      l.suspendedAgent(ev);
    }
  }

  private void fireResumedAgent(ContainerID cid, AID agentID) {
    PlatformEvent ev = new PlatformEvent(PlatformEvent.RESUMED_AGENT, agentID, cid);

    for(int i = 0; i < platformListeners.size(); i++) {
      AgentManager.Listener l = (AgentManager.Listener)platformListeners.get(i);
      l.resumedAgent(ev);
    }
  }

	private void fireChangedAgentPrincipal(ContainerID cid, AID agentID, AgentPrincipal from, AgentPrincipal to) {
		if (from == null) {
			from = authority.createAgentPrincipal(agentID, AgentPrincipal.NONE);
		}
		PlatformEvent ev = new PlatformEvent(PlatformEvent.CHANGED_AGENT_PRINCIPAL, agentID, cid, from, to);

		for (int i = 0; i < platformListeners.size(); i++) {
			AgentManager.Listener l = (AgentManager.Listener)platformListeners.get(i);
			l.changedAgentPrincipal(ev);
		}
	}

  private void fireMovedAgent(ContainerID from, ContainerID to, AID agentID) {
  	PlatformEvent ev = new PlatformEvent(agentID, from, to);

    for(int i = 0; i < platformListeners.size(); i++) {
      AgentManager.Listener l = (AgentManager.Listener)platformListeners.get(i);
      l.movedAgent(ev);
    }
  }

  private void fireAddedMTP(MTPDescriptor mtp, ContainerID cid) {
    String name = mtp.getName();
    String[] addrs = mtp.getAddresses();
    Channel ch = new Channel("FIXME: missing channel name", name, addrs[0]);
    MTPEvent ev = new MTPEvent(MTPEvent.ADDED_MTP, cid, ch);
    for(int i = 0; i < platformListeners.size(); i++) {
      AgentManager.Listener l = (AgentManager.Listener)platformListeners.get(i);
      l.addedMTP(ev);
   } 
  }

  private void fireRemovedMTP(MTPDescriptor mtp, ContainerID cid) {
    String name = mtp.getName();
    String[] addrs = mtp.getAddresses();
    Channel ch = new Channel("FIXME: missing channel name", name, addrs[0]);
    MTPEvent ev = new MTPEvent(MTPEvent.REMOVED_MTP, cid, ch);
    for(int i = 0; i < platformListeners.size(); i++) {
      AgentManager.Listener l = (AgentManager.Listener)platformListeners.get(i);
      l.removedMTP(ev);
   }
  }
}
