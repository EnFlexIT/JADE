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

//#APIDOC_EXCLUDE_FILE

import jade.lang.acl.ACLMessage;

import jade.util.leap.List;

import jade.security.JADESecurityException;
import jade.security.JADEPrincipal;
import jade.security.Credentials;


/**
  @author Giovanni Rimassa - Universita' di Parma
  @version $Date$ $Revision$
*/

public interface AgentContainer {
  static final String MAIN_CONTAINER_NAME = "Main-Container";
  static final String AUX_CONTAINER_NAME = "Container";



    ContainerID getID();
    String getPlatformID();
    NodeDescriptor getNodeDescriptor();
    MainContainer getMain();
    ServiceManager getServiceManager();
    ServiceFinder getServiceFinder();

    AID getAMS();
    AID getDefaultDF();

    void initAgent(AID agentID, Agent instance,  
                   JADEPrincipal ownerPrincipal, Credentials initialCredentials                  
                   ) throws NameClashException, IMTPException, NotFoundException, JADESecurityException;
    void powerUpLocalAgent(AID agentID) throws NotFoundException;
    Agent addLocalAgent(AID id, Agent a) throws JADESecurityException;
    void removeLocalAgent(AID id);
    Agent acquireLocalAgent(AID id);
    void releaseLocalAgent(AID id);

    //#MIDP_EXCLUDE_BEGIN
    void fillListFromMessageQueue(List messages, Agent a);
    void fillListFromReadyBehaviours(List behaviours, Agent a);
    void fillListFromBlockedBehaviours(List behaviours, Agent a);

    //void commitMigration(Agent instance);
    //void abortMigration(Agent instance);
    //#MIDP_EXCLUDE_END

    void addAddressToLocalAgents(String address);
    void removeAddressFromLocalAgents(String address);
    boolean postMessageToLocalAgent(ACLMessage msg, AID receiverID);
    boolean livesHere(AID id);
    Location here();

    void shutDown();

    void becomeLeader();
}
