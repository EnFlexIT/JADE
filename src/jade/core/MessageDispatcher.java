/*
 * $Id$
 */

package jade.core;

import java.rmi.Remote;
import java.rmi.RemoteException;

import jade.lang.acl.ACLMessage;

/***********************************************************************

  Name: MessageDispatcher

  Responsibilities and Collaborations:

  + Receives incoming messages from other containers or from the
    platform registry.
    (ACLMessage, AgentPlatform)

  + Maintains a collection of local agents, indexed by agent names.
    (Agent)

  + Builds a suitable ACL message from the received string.
    (ACLMessage, ACLParser)

  + Notifies receiver agent, enqueueing the incoming message.
    (ACLMessage, Agent)

************************************************************************/
interface MessageDispatcher extends Remote {

  public void dispatch(ACLMessage msg) throws RemoteException, NotFoundException;

}

