/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

/**
 * ***************************************************************
 * JADE - Java Agent DEvelopment Framework is a framework to develop
 * multi-agent systems in compliance with the FIPA specifications.
 * Copyright (C) 2000 CSELT S.p.A.
 * 
 * GNU Lesser General Public License
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation,
 * version 2.1 of the License.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 * **************************************************************
 */

package jade.core;

import jade.lang.acl.ACLMessage;
import jade.util.leap.List;
import jade.mtp.MTPDescriptor;
import jade.domain.FIPANames;

// __SECURITY__BEGIN
import jade.security.AgentPrincipal;
import jade.security.AuthException;
import jade.security.JADECertificate;
import jade.security.CertificateFolder;
// __SECURITY__END

/**
 * 
 * This class acts as a Smart Proxy for the Main Container, transparently
 * handling caching and reconnection
 * 
 * @author Giovanni Rimassa - Universita` di Parma
 * @author Giovanni Caire - TILAB
 * @version $Date$ $Revision$
 */
class MainContainerProxy implements Platform {

  private Profile            myProfile;
  private MainContainer      adaptee;

  private AgentContainerImpl localContainer;

  /**
   */
  public MainContainerProxy(Profile p) throws ProfileException, IMTPException {
    myProfile = p;
    // Use the IMTPManager to get a fresh stub of the real Main container
    adaptee = myProfile.getIMTPManager().getMain(true);
  }

  /**
   */
  public void register(AgentContainerImpl ac, ContainerID cid, String username, byte[] passwd) throws IMTPException, AuthException {
    localContainer = ac;

    // The Main Container initialization of a peripheral container is just adding it to the platform.
    String name = adaptee.addContainer(ac, cid, username, passwd);
    cid.setName(name);
    Runtime.instance().gc("Register");
  } 

  /**
   */
  public void deregister(AgentContainer ac) throws IMTPException {
    // This call does nothing, since the container deregistration is triggered by the FailureMonitor thread.
  } 

  /**
   */
  public void dispatch(ACLMessage msg, AID receiverID) throws NotFoundException, UnreachableException {
    dispatchUntilOK(msg, receiverID);
  } 

  /**
   */
  public void newMTP(MTPDescriptor mtp, ContainerID cid) throws IMTPException {
    adaptee.newMTP(mtp, cid);
  } 

  /**
   */
  public AgentProxy getProxy(AID id) throws IMTPException, NotFoundException {
    return adaptee.getProxy(id);
  } 

  /**
   */
  public void bornAgent(AID name, ContainerID cid, CertificateFolder certs) throws IMTPException, NameClashException, NotFoundException, AuthException {
    adaptee.bornAgent(name, cid, certs);
    Runtime.instance().gc("Born agent");
  } 

  /**
   */
  public String getPlatformName() throws IMTPException {
    String name = adaptee.getPlatformName();
    Runtime.instance().gc("Get platform name");
    return name;
  } 

  /**
   */
  public AgentContainer lookup(ContainerID cid) throws IMTPException, NotFoundException {
    return adaptee.lookup(cid);
  } 

  /**
   */
  public void deadAgent(AID name) throws IMTPException, NotFoundException {
    adaptee.deadAgent(name);
    Runtime.instance().gc("Dead agent");
  } 

  /**
   */
  public void suspendedAgent(AID name) throws IMTPException, NotFoundException {
    adaptee.suspendedAgent(name);
    Runtime.instance().gc("Suspend agent");
  } 

  /**
   */
  public void resumedAgent(AID name) throws IMTPException, NotFoundException {
    adaptee.resumedAgent(name);
    Runtime.instance().gc("Resume agent");
  } 

  // __SECURITY__BEGIN

  /**
   */
  public void changedAgentPrincipal(AID name, CertificateFolder certs) throws IMTPException, NotFoundException {
    adaptee.changedAgentPrincipal(name, certs);
  } 

  /**
   */
  public AgentPrincipal getAgentPrincipal(AID name) throws IMTPException, NotFoundException {
  	return adaptee.getAgentPrincipal(name);
  }
  
  /**
   */
  public JADECertificate sign(JADECertificate certificate, CertificateFolder certs) throws IMTPException, AuthException {
    return adaptee.sign(certificate, certs);
  } 

  /**
   */
  public byte[] getPublicKey() throws IMTPException {
    return adaptee.getPublicKey();
  } 
  // __SECURITY__END

  /**
   */
  public String addContainer(AgentContainer ac, ContainerID cid, String username, byte[] passwd) throws IMTPException, AuthException {
    return adaptee.addContainer(ac, cid, username, passwd);
  } 
			
  /**
   */
  public void deadMTP(MTPDescriptor mtp, ContainerID cid) throws IMTPException {
    adaptee.deadMTP(mtp, cid);
  } 

  /**
   */
  public boolean transferIdentity(AID agentID, ContainerID src, ContainerID dest) throws IMTPException, NotFoundException {
    return adaptee.transferIdentity(agentID, src, dest);
  } 

  /**
   */
  public void removeContainer(ContainerID cid) throws IMTPException {
    adaptee.removeContainer(cid);
    Runtime.instance().gc("Remove container");
  } 


  /**
   */
  private void dispatchUntilOK(ACLMessage msg, AID receiverID) throws NotFoundException, UnreachableException {
    boolean ok = false;
    int     i = 0;
    do {

      AgentProxy proxy = null;
      try {
        // Try first with the local container
        localContainer.dispatch(msg, receiverID);

        /*
         * proxy = new LocalProxy(localContainer, receiverID);
         * cachedProxies.put(receiverID, proxy);
         */
        ok = true;
      } 
      catch (IMTPException imtpe) {
        // It should never happen as this is a local call
        imtpe.printStackTrace();
      } 
      catch (NotFoundException nfe) {
        // The destination agent is not in the local container.
        // Try with the Main Container: if this call raises a
        // NotFoundException, the agent is not found in the whole
        // GADT, so the exception breaks out of the loop and ends the
        // dispatch attempts.
        try {
          proxy = adaptee.getProxy(receiverID);    // Remote call
    			Runtime.instance().gc("Get proxy");
        } 
        catch (IMTPException imtpe) {
          /*
           * System.out.println("Communication error while contacting the Main container");
           * System.out.print("Trying to reconnect... ");
           * try {
           * restoreMainContainer();
           * System.out.println("OK.");
           * proxy = adaptee.getProxy(receiverID); // Remote call
           * }
           * catch(IMTPException imtpe2) {
           * System.out.println("Cannot reconnect: "+imtpe2.getMessage());
           * throw new UnreachableException("Main container unreachable: "+imtpe2.getMessage());
           * }
           */
          throw new UnreachableException("Main container unreachable: "+imtpe.getMessage());
        } 

        try {
          proxy.dispatch(msg);
    			Runtime.instance().gc("Dispatch");

          // cachedProxies.put(receiverID, proxy);
          ok = true;
        } 
        catch (NotFoundException nfe2) {
          // Stale proxy (the agent can have moved in the meanwhile).
          // Need to check again.
          ok = false;
        } 
      } 

      /*
       * i++;
       * if(i > 100) { // Watchdog counter...
       * System.out.println("===================================================================");
       * System.out.println(" Possible livelock in message dispatching:");
       * System.out.println(" Receiver is:");
       * receiverID.toText(new java.io.OutputStreamWriter(System.out));
       * System.out.println();
       * System.out.println();
       * System.out.println(" Message is:");
       * msg.toText(new java.io.OutputStreamWriter(System.out));
       * System.out.println();
       * System.out.println();
       * System.out.println("===================================================================");
       * try {
       * Thread.sleep(3000);
       * }
       * catch(InterruptedException ie) {
       * System.out.println("Interrupted !!!");
       * }
       * return;
       * }
       */
    } 
    while (!ok);
  } 


  /*
   * private void restoreMainContainer() throws IMTPException, NotFoundException {
   * try {
   * // Use the IMTPManager to get a fresh stub of the real Main container
   * adaptee = myProfile.getIMTPManager().getMain(true);
   * 
   * // Register again with the Main Container.
   * ContainerID myID = (ContainerID) localContainer.here();
   * String name = adaptee.addContainer(localContainer, myID); // Remote call
   * myID.setName(name);
   * 
   * // Restore registration of local agents
   * ACLMessage regMsg = new ACLMessage(ACLMessage.REQUEST);
   * regMsg.setSender(Agent.getAMS());
   * regMsg.addReceiver(Agent.getAMS());
   * regMsg.setLanguage(jade.lang.sl.SL0Codec.NAME);
   * regMsg.setOntology(jade.domain.FIPAAgentManagement.FIPAAgentManagementOntology.NAME);
   * regMsg.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
   * 
   * AID[] agentIDs = localContainer.getLocalAgents().keys();
   * for(int i = 0; i < agentIDs.length; i++) {
   * AID agentID = agentIDs[i];
   * 
   * // Register again the agent with the Main Container.
   * try {
   * adaptee.bornAgent(agentID, myID); // Remote call
   * }
   * catch(NameClashException nce) {
   * throw new NotFoundException("Agent name already in use: "+ nce.getMessage());
   * }
   * 
   * String content = "((action (agent-identifier :name " + Agent.getAMS().getName() + " ) (register (ams-agent-description :name (agent-identifier :name " + agentID.getName() + " ) :ownership JADE :state active ) ) ))";
   * // Register again the agent with the AMS
   * regMsg.setContent(content);
   * localContainer.routeIn(regMsg, Agent.getAMS());
   * 
   * }
   * 
   * // Restore the registration of local MTPs
   * List localAddresses = myProfile.getAcc().getLocalAddresses();
   * for(int i = 0; i < localAddresses.size(); i++) {
   * adaptee.newMTP((String)localAddresses.get(i), myID); // Remote call
   * }
   * 
   * }
   * catch(ProfileException pe) {
   * throw new NotFoundException("Profile error trying to reconnect to the Main container: "+pe.getMessage());
   * }
   * }
   */

}

