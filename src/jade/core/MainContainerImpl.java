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

import jade.lang.acl.ACLMessage;

import jade.mtp.MTPException;
import jade.mtp.TransportAddress;
import jade.mtp.MTPDescriptor;

//__SECURITY__BEGIN
import jade.security.Authority;
import jade.security.AuthException;
import jade.security.AgentPrincipal;
import jade.security.ContainerPrincipal;
import jade.security.UserPrincipal;
import jade.security.JADECertificate;
import jade.security.IdentityCertificate;
import jade.security.DelegationCertificate;
import jade.security.DelegationCertificate;
import jade.security.IdentityCertificate;
//__SECURITY__END


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


	MainContainerImpl(Profile p) throws ProfileException {
		myIMTPManager = p.getIMTPManager();
		platformID = p.getParameter(Profile.PLATFORM_ID);
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
			String type = p.getParameter(Profile.MAINAUTH_CLASS);
			if (type != null) {
				authority = (Authority)Class.forName(type).newInstance();
				authority.setName("main-authority");
				authority.init(p, this);
			}
		}
		catch (Exception e1) {
			e1.printStackTrace();
		}
		
		try {
			if (authority == null) {
				authority = new jade.security.dummy.DummyAuthority();
				authority.setName("main-authority");
				authority.init(p, this);
			}
		}
		catch (Exception e2) {
			e2.printStackTrace();
		}
		
	}
	
	public Authority getAuthority() {
		return authority;
	}

	public void register(AgentContainerImpl ac, ContainerID cid, UserPrincipal user, byte[] password) throws IMTPException, AuthException {
		// Authenticate user
		ContainerPrincipal principal = getAuthority().createContainerPrincipal();
		principal.init(cid, user);
		IdentityCertificate identity = authority.createIdentityCertificate();
		DelegationCertificate delegation = authority.createDelegationCertificate();
		if (identity != null && delegation != null) {
			identity.setSubject(principal);
			authority.authenticate(identity, delegation, password);
		}
		authority.checkAction(Authority.PLATFORM_CREATE, principal, identity, new DelegationCertificate[] {delegation});
		authority.checkAction(Authority.CONTAINER_CREATE, principal, identity, new DelegationCertificate[] {delegation});

		// Set the container-principal
		ac.changeContainerPrincipal(principal, identity, delegation);

		// Add the calling container as the main container and set its name
		cid.setName(MAIN_CONTAINER_NAME);
		containers.addContainer(cid, ac);
		containersProgNo++;

		String ownership = user.getName() + ":" + new String(password);

		// Start the AMS
		theAMS = new ams(this);
		theAMS.setOwnership(ownership);
		AgentPrincipal amsPrincipal = authority.createAgentPrincipal();
		amsPrincipal.init(Agent.getAMS(), user);
		IdentityCertificate amsIdentity = authority.createIdentityCertificate();
		DelegationCertificate amsDelegation = authority.createDelegationCertificate();
		if (amsIdentity != null && amsDelegation != null) {
			amsIdentity.setSubject(amsPrincipal);
			authority.authenticate(amsIdentity, amsDelegation, password);
			theAMS.setPrincipal(amsPrincipal, amsIdentity, amsDelegation);
		}
		ac.initAgent(Agent.getAMS(), theAMS, AgentContainer.START);
		theAMS.waitUntilStarted();

		// Notify the AMS about the main container existence
		fireAddedContainer(cid);
		fireChangedContainerPrincipal(cid, null, principal);

		// Start the Default DF
		defaultDF = new df();
		defaultDF.setOwnership(ownership);
		AgentPrincipal dfPrincipal = authority.createAgentPrincipal();
		dfPrincipal.init(Agent.getDefaultDF(), user);
		IdentityCertificate dfIdentity = authority.createIdentityCertificate();
		DelegationCertificate dfDelegation = authority.createDelegationCertificate();
		if (dfIdentity != null && dfDelegation != null) {
			dfIdentity.setSubject(dfPrincipal);
			authority.authenticate(dfIdentity, dfDelegation, password);
			defaultDF.setPrincipal(dfPrincipal, dfIdentity, dfDelegation);
		}
		ac.initAgent(Agent.getDefaultDF(), defaultDF, AgentContainer.START);
		defaultDF.waitUntilStarted();
		
		// Make itself accessible from remote JVMs
		myIMTPManager.remotize(this);
	}

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

  public void dispatch(ACLMessage msg, AID receiverID) throws NotFoundException, UnreachableException {
    // Directly use the GADT
    AgentDescriptor ad = platformAgents.get(receiverID);
    if(ad == null) {
      throw new NotFoundException("Agent " + receiverID.getName() + " not found in GADT.");
    }
    ad.lock();
    AgentProxy ap = ad.getProxy();
    ad.unlock();
    ap.dispatch(msg);
  }

  // this variable holds a progressive number just used to name new containers
  private static int containersProgNo = 0;


  AgentContainer getContainerFromAgent(AID agentID) throws NotFoundException {
    AgentDescriptor ad = platformAgents.get(agentID);
    if(ad == null) {
      throw new NotFoundException("Agent " + agentID.getName() + " not found in getContainerFromAgent()");
    }
    ad.lock();
    ContainerID cid = ad.getContainerID();
    AgentContainer ac = containers.getContainer(cid);
    ad.unlock();
    return ac;
  }

  // Inner class to detect agent container failures
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

	    			//containers.removeContainer(targetID.getName());
	    			//fireRemovedContainer(targetID);
						cleanTables(targetID);
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

  private void cleanTables(ContainerID crashedID) {
    // If a container has crashed all its agents
    // appear to be still alive both in the GADT and in the AMS -->
  	// Clean them 
    AID[] allIds = platformAgents.keys();

    for (int i = 0; i < allIds.length; ++i) {
    	AID    id = allIds[i];
      ContainerID cid = platformAgents.get(id).getContainerID();

      if (crashedID.equals(cid)) {
      	// This agent was living in the container that has crashed
        // --> It must be cleaned
        platformAgents.remove(id);
        fireDeadAgent(crashedID, id);
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

  // Private methods to notify platform listeners of a significant event.

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
  		UserPrincipal user = authority.createUserPrincipal();
  		user.init(UserPrincipal.NONE);
  		from = authority.createContainerPrincipal();
  		from.init(cid, user);
  	}
    PlatformEvent ev = new PlatformEvent(cid, from, to);
    for(int i = 0; i < platformListeners.size(); i++) {
      AgentManager.Listener l = (AgentManager.Listener)platformListeners.get(i);
      l.changedContainerPrincipal(ev);
    }
  }

  private void fireBornAgent(ContainerID cid, AID agentID) {
    PlatformEvent ev = new PlatformEvent(PlatformEvent.BORN_AGENT, agentID, cid);

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

//__SECURITY__BEGIN
  private void fireChangedAgentPrincipal(ContainerID cid, AID agentID, AgentPrincipal oldPrincipal, AgentPrincipal newPrincipal) {
  	if (oldPrincipal == null) {
  		UserPrincipal user = authority.createUserPrincipal();
  		user.init(UserPrincipal.NONE);
  		oldPrincipal = authority.createAgentPrincipal();
  		oldPrincipal.init(agentID, user);
  	}
    PlatformEvent ev = new PlatformEvent(agentID, cid, oldPrincipal, newPrincipal);

    for (int i = 0; i < platformListeners.size(); i++) {
      AgentManager.Listener l = (AgentManager.Listener)platformListeners.get(i);
      l.changedAgentPrincipal(ev);
    }
  }
//__SECURITY__END

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

  public String getPlatformName() throws IMTPException {
    return platformID;
  }

	public String addContainer(AgentContainer ac, ContainerID cid, UserPrincipal user, byte[] passwd) throws IMTPException, AuthException {
		// Authenticate user
		ContainerPrincipal principal = authority.createContainerPrincipal();
		principal.init(cid, user);
		IdentityCertificate identity = authority.createIdentityCertificate();
		DelegationCertificate delegation = authority.createDelegationCertificate();
		if (identity != null && delegation != null) {
			identity.setSubject(principal);
			authority.authenticate(identity, delegation, passwd);
		}
		authority.checkAction(Authority.CONTAINER_CREATE, principal, identity, new DelegationCertificate[]{delegation});

		// Set the container-principal
		ac.changeContainerPrincipal(principal, identity, delegation);

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

		// Add the calling container and set its name
		String name = AUX_CONTAINER_NAME + containersProgNo;
		cid.setName(name);
		containers.addContainer(cid, ac);
		containersProgNo++;

		// Spawn a blocking call to the remote container in a separate
		// thread. This is a failure notification technique.
		Thread t = new Thread(new FailureMonitor(ac, cid));
		t.start();

		// Notify listeners
		fireAddedContainer(cid);

		// Return the name given to the new container
		return name;

	}

  public void removeContainer(ContainerID cid) throws IMTPException {
    containers.removeContainer(cid);

    // Notify listeners
    fireRemovedContainer(cid);
  }

  public AgentContainer lookup(ContainerID cid) throws IMTPException, NotFoundException {
    AgentContainer ac = containers.getContainer(cid);
    return ac;
  }

  public void bornAgent(AID name, ContainerID cid) throws IMTPException, NameClashException, NotFoundException  {

    AgentDescriptor desc = new AgentDescriptor();
    AgentContainer ac = containers.getContainer(cid);
    AgentProxy ap = myIMTPManager.createAgentProxy(ac, name);
    desc.setProxy(ap);
    desc.setContainerID(cid);
    AgentDescriptor old = platformAgents.put(name, desc);

    // If there's already an agent with name 'name' throw a name clash
    // exception unless the old agent's container is dead.
    if(old != null) {
      AgentProxy oldProxy = old.getProxy();
      try {
	oldProxy.ping(); // Make sure agent is reachable, then raise a name clash exception
	platformAgents.put(name, old);
	throw new NameClashException("Agent " + name + " already present in the platform ");
      }
      catch(UnreachableException ue) {
	System.out.println("Replacing a dead agent ...");
	fireDeadAgent(old.getContainerID(), name);
      }
    }

    // Notify listeners
    fireBornAgent(cid, name);

  }

  public void deadAgent(AID name) throws IMTPException, NotFoundException {
    AgentDescriptor ad = platformAgents.get(name);
    if(ad == null)
      throw new NotFoundException("DeadAgent failed to find " + name);
    ContainerID cid = ad.getContainerID();
    platformAgents.remove(name);

    // Notify listeners
    fireDeadAgent(cid, name);
  }

  public void suspendedAgent(AID name) throws IMTPException, NotFoundException {
    AgentDescriptor ad = platformAgents.get(name);
    if (ad == null)
      throw new NotFoundException("SuspendedAgent failed to find " + name);
    ContainerID cid = ad.getContainerID();

    // Notify listeners
    fireSuspendedAgent(cid, name);
  }

  public void resumedAgent(AID name) throws IMTPException, NotFoundException {
    AgentDescriptor ad = platformAgents.get(name);
    if(ad == null)
      throw new NotFoundException("ResumedAgent failed to find " + name);
    ContainerID cid = ad.getContainerID();

    // Notify listeners
    fireResumedAgent(cid, name);
  }

//__SECURITY__BEGIN
	public void changedAgentPrincipal(AID name, AgentPrincipal from, AgentPrincipal to, IdentityCertificate identity) throws IMTPException, NotFoundException {
		AgentDescriptor ad = platformAgents.get(name);
		if (ad == null)
			throw new NotFoundException("ChangedAgentPrincipal failed to find " + name);

		try {
			//!!! attenzione se identity == null
			authority.verify(identity);
			if (identity != null && ! identity.getSubject().equals(to))
				throw new AuthException("identity-subject doesn't match new agent principal");

			ContainerID cid = ad.getContainerID();

			// Notify containers
			/* FIXME. This block of code creates a deadlock when launching
			   the party example on a remote container.
			AgentContainer[] allContainers = containers.containers();
			for (int i = 0; i < allContainers.length; i++) {
				AgentContainer ac = allContainers[i];
				// FIXME: If some container is temporarily disconnected it will not be
				// notified. We should investigate the sideeffects
				try {
				    ac.changeAgentPrincipal(name, to, null, null);
				}
				catch (IMTPException imtpe) {
				    imtpe.printStackTrace();
				}
				}

			// Notify listeners
			fireChangedAgentPrincipal(cid, name, from, to); FIXME */
		}
		catch (AuthException ae) {
			ae.printStackTrace();
		}
	}
//__SECURITY__END

  public AgentProxy getProxy(AID agentID) throws IMTPException, NotFoundException {
    AgentProxy ap;
    AgentDescriptor ad = platformAgents.get(agentID);

    if(ad == null)
      throw new NotFoundException("getProxy() failed to find " + agentID.getName());
    else {
      ad.lock();
      ap = ad.getProxy();
      ad.unlock();
      try {
	ap.ping();
      }
      catch(UnreachableException ue) {
	throw new NotFoundException("Container for " + agentID.getName() + " is unreachable");
      }
      return ap;
    }
  }

  public boolean transferIdentity(AID agentID, ContainerID src, ContainerID dest) throws IMTPException, NotFoundException {
    AgentDescriptor ad = platformAgents.get(agentID);
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
    ad.lock();
    ad.setProxy(myIMTPManager.createAgentProxy(destAC, agentID));
    ad.setContainerID(dest);
    fireMovedAgent(src, dest, agentID);
    ad.unlock();
    return true;
  }

  // These methods dispatch agent management operations to
  // appropriate Agent Container through a suitable IMTP.

  public void kill(AID agentID, String password) throws NotFoundException, UnreachableException {
    try {
      AgentContainer ac = getContainerFromAgent(agentID);
      ac.killAgent(agentID);
    }
    catch(IMTPException re) {
      throw new UnreachableException(re.getMessage());
    }
  }

  public void suspend(AID agentID, String password) throws NotFoundException, UnreachableException {
    try {
      AgentContainer ac = getContainerFromAgent(agentID);
      ac.suspendAgent(agentID);
    }
    catch(IMTPException re) {
      throw new UnreachableException(re.getMessage());
    }
  }

  public void activate(AID agentID, String password) throws NotFoundException, UnreachableException {
    try {
      AgentContainer ac = getContainerFromAgent(agentID);
      ac.resumeAgent(agentID);
    }
    catch(IMTPException re) {
      throw new UnreachableException(re.getMessage());
    }
  }

//__SECURITY__BEGIN
	public void changeAgentPrincipal(AID agentID, AgentPrincipal principal, IdentityCertificate identity, DelegationCertificate delegation) throws NotFoundException, UnreachableException, AuthException {
		if (identity != null && delegation != null) {
			try {
	      AgentContainer ac = getContainerFromAgent(agentID);
  	    ac.changeAgentPrincipal(agentID, principal, identity, delegation);
    	}
    	catch (IMTPException re) {
      	throw new UnreachableException(re.getMessage());
    	}
    }
    
		if (delegation != null) {
			AgentPrincipal amsPrincipal = theAMS.getPrincipal();
			DelegationCertificate amsDelegation = authority.createDelegationCertificate();
			amsDelegation.setSubject(amsPrincipal);
			for (Iterator i = delegation.getPermissions().iterator(); i.hasNext(); ) {
				amsDelegation.addPermission(i.next());
			}
			authority.sign(amsDelegation, identity, new DelegationCertificate[] {delegation});
			theAMS.setDelegation(agentID, amsDelegation);
		}		
  }
//__SECURITY__END

  public void wait(AID agentID, String password) throws NotFoundException, UnreachableException {
    try {
      AgentContainer ac = getContainerFromAgent(agentID);
      ac.waitAgent(agentID);
    }
    catch(IMTPException re) {
      throw new UnreachableException(re.getMessage());
    }
  }

  public void wake(AID agentID, String password) throws NotFoundException, UnreachableException {
    try {
      AgentContainer ac = getContainerFromAgent(agentID);
      ac.wakeAgent(agentID);
    }
    catch(IMTPException re) {
      throw new UnreachableException(re.getMessage());
    }
  }

  public void move(AID agentID, Location where, String password) throws NotFoundException, UnreachableException {
    // Retrieve the container for the original agent
    AgentContainer src = getContainerFromAgent(agentID);
    try {
      src.moveAgent(agentID, where);
    }
    catch(IMTPException re) {
      throw new UnreachableException(re.getMessage());
    }
  }

  public void copy(AID agentID, Location where, String newAgentID, String password) throws NotFoundException, UnreachableException {
    // Retrieve the container for the original agent
    AgentContainer src = getContainerFromAgent(agentID);
    try {
      src.copyAgent(agentID, where, newAgentID);
    }
    catch(IMTPException re) {
      throw new UnreachableException(re.getMessage());
    }
  }


  // Methods for Message Transport Protocols management


  public void newMTP(MTPDescriptor mtp, ContainerID cid) throws IMTPException {
    try {
      String containerName = cid.getName();
      String[] mtpAddrs = mtp.getAddresses();
      String mtpAddress = mtpAddrs[0];
      platformAddresses.add(mtpAddress);
      containers.addMTP(cid, mtp);
      AgentContainer target = containers.getContainer(cid);

      // To avoid additions/removals of containers during MTP tables update
      synchronized(containers) {

	// Add the new MTP to the routing tables of all the containers. 
	AgentContainer[] allContainers = containers.containers();
	for(int i = 0; i < allContainers.length; i++) {
	  AgentContainer ac = allContainers[i];
	  // Skip target container
	  if(ac != target)
			// FIXME: If some container is temporarily disconnected it will not be
			// notified. We should investigate the sideeffects
	  	try {
		    ac.updateRoutingTable(AgentContainer.ADD_RT, mtp, target);
	  	}
	  	catch (IMTPException imtpe) {
	  	}
	}

      }

      // Notify listeners (typically the AMS)
      fireAddedMTP(mtp, cid);
    }
    catch(NotFoundException nfe) {
      System.out.println("Error: the container " + cid.getName() + " was not found.");
    }
  }

  public void deadMTP(MTPDescriptor mtp, ContainerID cid) throws IMTPException {
    try {
      String containerName = cid.getName();
      String[] mtpAddrs = mtp.getAddresses();
      String mtpAddress = mtpAddrs[0];
      platformAddresses.remove(mtpAddress);
      containers.removeMTP(cid, mtp);
      AgentContainer target = containers.getContainer(cid);

      // To avoid additions/removals of containers during MTP tables update
      synchronized(containers) {

	// Remove the dead MTP from the routing tables of all the containers. 
	AgentContainer[] allContainers = containers.containers();
	for(int i = 0; i < allContainers.length; i++) {
	  AgentContainer ac = allContainers[i];
	  // Skip target container
	  if(ac != target)
			// FIXME: If some container is temporarily disconnected it will not be
			// notified. We should investigate the sideeffects
	  	try {
		    ac.updateRoutingTable(AgentContainer.DEL_RT, mtp, target);
	  	}
	  	catch (IMTPException imtpe) {
	  	}
	}

      }

      // Notify listeners (typically the AMS)
      fireRemovedMTP(mtp, cid);

    }
    catch(NotFoundException nfe) {
      System.out.println("Error: the container " + cid.getName() + " was not found.");
      nfe.printStackTrace();
    }

  }

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


  // These methods are to be used only by AMS agent.

  public void addListener(AgentManager.Listener l) {
    platformListeners.add(l);
  }

  public void removeListener(AgentManager.Listener l) {
    platformListeners.remove(l);
  }

  // This is used by AMS to obtain the set of all the Agent Containers of the platform.
  public ContainerID[] containerIDs() {
  	return containers.names();
  }

  // This is used by AMS to obtain the list of all the agents of the platform.
  public AID[] agentNames() {
    return platformAgents.keys();
  }

  public String[] platformAddresses() {
    Object[] objs = platformAddresses.toArray();
    String[] result = new String[objs.length];
    System.arraycopy(objs, 0, result, 0, result.length);
    return result;
  }

  // This maps the name of an agent to the ID of the Agent Container the agent lives in.
  public ContainerID getContainerID(AID agentID) throws NotFoundException {
    AgentDescriptor ad = platformAgents.get(agentID);
    if(ad == null)
      throw new NotFoundException("Agent " + agentID.getName() + " not found in getContainerID()");
    ad.lock();
    ContainerID result = ad.getContainerID();
    ad.unlock();
    return result;
  }

  // This is called in response to a 'create-agent' action
  public void create(String agentName, String className, String args[], ContainerID cid, String ownership, IdentityCertificate identity, DelegationCertificate delegation) throws UnreachableException, AuthException {
    try {
      String containerName = cid.getName();
      AgentContainer ac;
      // If no name is given, the agent is started on the MainContainer itself
      if (containerName == null)
        cid.setName(MAIN_CONTAINER_NAME);
      try {
				ac = containers.getContainer(cid);
      }
      catch (NotFoundException nfe) {
        try {
	  			// If a wrong name is given, then again the agent starts on the MainContainer itself
          ac = containers.getContainer(new ContainerID(MAIN_CONTAINER_NAME, null));
        }
        catch (NotFoundException nfe2) {
          throw new UnreachableException(nfe2.getMessage());
        }
      }

      AID id = new AID(agentName, AID.ISLOCALNAME);
      ac.createAgent(id, className, args, ownership, identity, delegation, AgentContainer.START);
    }
    catch(IMTPException re) {
      throw new UnreachableException(re.getMessage());
    }
  }

  public void killContainer(ContainerID cid) {

    // This call spawns a separate thread in order to avoid deadlock.
    try {
      final ContainerID contID = cid;
      final AgentContainer ac = containers.getContainer(cid);
      Thread auxThread = new Thread(new Runnable() {
	 			public void run() {
	   			try {
	     			ac.exit();
	   			}
	   			catch(IMTPException imtp1) {
	     			System.out.println("Container " + contID.getName() + " is unreachable. Ignoring...");
	     			try {
	     				removeContainer(contID);
	     			}
	     			catch (IMTPException imtpe2) {
	     				// Should never happen as this is a local call
	     				imtpe2.printStackTrace();
	     			}
	   			}
	 			}
      });
      auxThread.start();
    }
    catch(NotFoundException nfe) {
      nfe.printStackTrace();
    }
  }


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

  public JADECertificate sign(JADECertificate certificate, IdentityCertificate identity, DelegationCertificate[] delegations) throws IMTPException, AuthException {
    authority.sign(certificate, identity, delegations);
    return certificate;
  }
  
  public byte[] getPublicKey() throws IMTPException {
  	return authority.getPublicKey();
  }
  
}
