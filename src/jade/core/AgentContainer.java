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

import jade.security.Authority;
import jade.security.AgentPrincipal;
import jade.security.ContainerPrincipal;
import jade.security.AuthException;

import jade.util.leap.List;


/**
  @author Giovanni Rimassa - Universita' di Parma
  @version $Date$ $Revision$
*/

public interface AgentContainer {

    static final boolean CREATE_AND_START = true;
    static final boolean CREATE_ONLY = false;


    ContainerID getID();
    String getPlatformID();
    MainContainer getMain();
    ServiceManager getServiceManager();
    ServiceFinder getServiceFinder();

    AID getAMS();
    AID getDefaultDF();

    void initAgent(AID agentID, Agent instance, boolean startIt) throws NameClashException, IMTPException, NotFoundException, AuthException;
    Agent addLocalAgent(AID id, Agent a) throws AuthException;
    void powerUpLocalAgent(AID agentID, Agent instance);
    void removeLocalAgent(AID id);
    Agent acquireLocalAgent(AID id);
    void releaseLocalAgent(AID id);

    //#MIDP_EXCLUDE_BEGIN
    void fillListFromMessageQueue(List messages, Agent a);
    void fillListFromReadyBehaviours(List behaviours, Agent a);
    void fillListFromBlockedBehaviours(List behaviours, Agent a);

    void commitMigration(Agent instance);
    void abortMigration(Agent instance);
    //#MIDP_EXCLUDE_END

    void addAddressToLocalAgents(String address);
    void removeAddressFromLocalAgents(String address);
    boolean postMessageToLocalAgent(ACLMessage msg, AID receiverID);
    boolean livesHere(AID id);
    Location here();

    Authority getAuthority();
    AgentPrincipal getAgentPrincipal(final AID agentID);
    ContainerPrincipal getContainerPrincipal();

    void shutDown();

    void becomeLeader();
}
