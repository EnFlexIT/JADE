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

import jade.lang.acl.ACLMessage;

import jade.mtp.MTPDescriptor;

import jade.util.leap.List;
import jade.util.leap.LinkedList;

//__SECURITY__BEGIN
import jade.security.AgentPrincipal;
import jade.security.AuthException;
import jade.security.JADECertificate;
import jade.security.IdentityCertificate;
import jade.security.DelegationCertificate;
//__SECURITY__END

/**
 
  This class acts as a Smart Proxy for the Main Container, transparently
  handling caching and reconnection
 
  @author Giovanni Rimassa - Universita` di Parma
  @version $Date$ $Revision$
*/
class MainContainerProxy implements Platform {

    private static final int CACHE_SIZE = 10;

    private Profile myProfile;
    private MainContainer adaptee;

    private AgentContainerImpl localContainer;

    // Agents cache, indexed by agent name
    private AgentCache cachedProxies = new AgentCache(CACHE_SIZE);

    // Local MTPs cache
    private List localMTPs = new LinkedList();

    public MainContainerProxy(Profile p) throws ProfileException, IMTPException {
      myProfile = p;
      // Use the IMTPManager to get a fresh stub of the real Main container
      adaptee = myProfile.getIMTPManager().getMain(true);
    }

    public void register(AgentContainerImpl ac, ContainerID cid, String username, byte[] password) throws IMTPException, AuthException {
      localContainer = ac;

      // The Main Container initialization of a peripheral container is just adding it to the platform.
      String name = adaptee.addContainer(ac, cid, username, password);
      cid.setName(name);
    }

    public void deregister(AgentContainer ac) throws IMTPException {
      // This call does nothing, since the container deregistration is triggered by the FailureMonitor thread.
    }


    public void dispatch(ACLMessage msg, AID receiverID) throws NotFoundException, UnreachableException {
	    boolean dispatched = false;
      
  	  // Use the cache.
    	AgentProxy ap = cachedProxies.get(receiverID);
      if(ap != null) { 
      	// Cache hit :-)
				try {
	  			ap.dispatch(msg);
		  		dispatched = true;
				}
				catch(NotFoundException nfe) { // Stale cache entry
	  			cachedProxies.remove(receiverID);
				}
				catch(UnreachableException ue) { // Stale cache entry
	  			cachedProxies.remove(receiverID);
				}
      }
      
      if (!dispatched) {
				dispatchUntilOK(msg, receiverID);
      }
    }

    public void newMTP(MTPDescriptor mtp, ContainerID cid) throws IMTPException {
      localMTPs.add(mtp);
      adaptee.newMTP(mtp, cid);
    }

    public AgentProxy getProxy(AID id) throws IMTPException, NotFoundException {
      return adaptee.getProxy(id);
    }

    public void bornAgent(AID name, ContainerID cid, IdentityCertificate identity, DelegationCertificate delegation) throws IMTPException, NameClashException, NotFoundException, AuthException {
      adaptee.bornAgent(name, cid, identity, delegation);
    }

    public String getPlatformName() throws IMTPException {
      return adaptee.getPlatformName();
    }

    public AgentContainer lookup(ContainerID cid) throws IMTPException, NotFoundException {
      return adaptee.lookup(cid);
    }

    public void deadAgent(AID name) throws IMTPException, NotFoundException {
      cachedProxies.remove(name); // FIXME: It shouldn't be needed
      adaptee.deadAgent(name);
    }

    public void suspendedAgent(AID name) throws IMTPException, NotFoundException {
      adaptee.suspendedAgent(name);
    }

    public void resumedAgent(AID name) throws IMTPException, NotFoundException {
      adaptee.resumedAgent(name);
    }

//__SECURITY__BEGIN
    public void changedAgentPrincipal(AID name, IdentityCertificate identity, DelegationCertificate delegation) throws IMTPException, NotFoundException {
      adaptee.changedAgentPrincipal(name, identity, delegation);
    }
    
    public AgentPrincipal getAgentPrincipal(AID name) throws IMTPException, NotFoundException {
      return adaptee.getAgentPrincipal(name);
    }
//__SECURITY__END

    public String addContainer(AgentContainer ac, ContainerID cid, String username, byte[] password) throws IMTPException, AuthException {
      return adaptee.addContainer(ac, cid, username, password);
    }

    public void deadMTP(MTPDescriptor mtp, ContainerID cid) throws IMTPException {
      localMTPs.remove(mtp);
      adaptee.deadMTP(mtp, cid);
    }

    public boolean transferIdentity(AID agentID, ContainerID src, ContainerID dest) throws IMTPException, NotFoundException {
      return adaptee.transferIdentity(agentID, src, dest);
    }

    public void removeContainer(ContainerID cid) throws IMTPException {
      adaptee.removeContainer(cid);
    }


  private void dispatchUntilOK(ACLMessage msg, AID receiverID) throws NotFoundException, UnreachableException {
    boolean ok;
    int i = 0;
    do {

      AgentProxy proxy = null;
      try {
				// Try first with the local container	  
				localContainer.dispatch(msg, receiverID);
				proxy = new LocalProxy(localContainer, receiverID);
				cachedProxies.put(receiverID, proxy);
				ok = true;
      }
      catch(IMTPException imtpe) {
				// It should never happen as this is a local call
				throw new InternalError("Error: cannot contact the local container.");
      }
      catch(NotFoundException nfe) {
      	// The destination agent is not in the local container.
				// Try with the Main Container: if this call raises a
				// NotFoundException, the agent is not found in the whole
				// GADT, so the exception breaks out of the loop and ends the
				// dispatch attempts.
				try {
	  			proxy = adaptee.getProxy(receiverID); // Remote call
				}
				catch(IMTPException imtpe1) {
	  			//throw new NotFoundException("Communication problem: " + imtpe.getMessage());
	  			System.out.println("Communication error while contacting the Main container");
	  			System.out.print("Trying to reconnect... ");
	  			try {
	    			restoreMainContainer();
	    			System.out.println("OK.");
	    			proxy = adaptee.getProxy(receiverID); // Remote call
	  			}
	  			catch(IMTPException imtpe2) {
	  				System.out.println("Cannot reconnect: "+imtpe2.getMessage());
	    			throw new UnreachableException("Main container unreachable: "+imtpe2.getMessage());
	  			}
				}
				try {
					// This can throw an UnreachableException that is propagated upwards
	  			proxy.dispatch(msg); 
	  			cachedProxies.put(receiverID, proxy);
	  			ok = true;
				}
				catch(NotFoundException nfe2) {
	  			// Stale proxy (the agent can have moved in the meanwhile).
					// Need to check again.
	  			ok = false;
				}
      }

      /*
      i++;
      if(i > 100) { // Watchdog counter...
	System.out.println("===================================================================");
	System.out.println(" Possible livelock in message dispatching:");
	System.out.println(" Receiver is:"+receiverID);
	System.out.println();
	System.out.println();
	System.out.println(" Message is:"+msg);
	System.out.println();
	System.out.println();
	System.out.println("===================================================================");
	try {
	  Thread.sleep(3000);
	}
	catch(InterruptedException ie) {
	  System.out.println("Interrupted !!!");
	}
	return;
      }
      */
    } while(!ok);
  }

  
  private void restoreMainContainer() throws IMTPException, NotFoundException {
    try {
      // Use the IMTPManager to get a fresh stub of the real Main container
      adaptee = myProfile.getIMTPManager().getMain(true);

      // Register again with the Main Container.
      ContainerID myID = (ContainerID) localContainer.here();
      //!!! How to register again?
      String name = adaptee.addContainer(localContainer, myID, null, null); // Remote call
      myID.setName(name);

      // Restore registration of local agents
      ACLMessage regMsg = new ACLMessage(ACLMessage.REQUEST);
      regMsg.setSender(Agent.getAMS());
      regMsg.addReceiver(Agent.getAMS());
      regMsg.setLanguage(jade.lang.sl.SL0Codec.NAME);
      regMsg.setOntology(jade.domain.FIPAAgentManagement.FIPAAgentManagementOntology.NAME);
      regMsg.setProtocol("fipa-request");

      Agent[] agents = localContainer.getLocalAgents().values();
      for (int i = 0; i < agents.length; i++) {
				AID agentID = agents[i].getAID();

				// Register again the agent with the Main Container.
				try {
	  			adaptee.bornAgent(agentID, myID, agents[i].getIdentity(), agents[i].getDelegation()); // Remote call
				}
				catch (NameClashException nce) {
	  			throw new NotFoundException("Agent name already in use: "+ nce.getMessage());
				}

				String content = "((action (agent-identifier :name " + Agent.getAMS().getName() + " ) (register (ams-agent-description :name (agent-identifier :name " + agentID.getName() + " ) :ownership JADE :state active ) ) ))";
				// Register again the agent with the AMS
				regMsg.setContent(content);
				localContainer.routeIn(regMsg, Agent.getAMS());

      }

      // Restore the registration of local MTPs 
      for (int i = 0; i < localMTPs.size(); i++) {
        adaptee.newMTP((MTPDescriptor)localMTPs.get(i), myID); // Remote call
      }

    }
    catch (ProfileException pe) {
      throw new NotFoundException("Profile error trying to reconnect to the Main container: "+pe.getMessage());
    }
    catch (AuthException se) {
      se.printStackTrace();
    }
  }
  
  public JADECertificate sign(JADECertificate certificate, IdentityCertificate identity, DelegationCertificate[] delegations) throws IMTPException, AuthException {
    return adaptee.sign(certificate, identity, delegations);
  }
  
  public byte[] getPublicKey() throws IMTPException {
  	return adaptee.getPublicKey();
  }

}

