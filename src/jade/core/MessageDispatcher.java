/*
 * $Log$
 * Revision 1.5  1998/10/11 19:35:12  rimassa
 * Added ping() and getContainer() methods to support agent platform
 * error recovery.
 *
 * Revision 1.4  1998/10/04 18:01:08  rimassa
 * Added a 'Log:' field to every source file.
 *
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

  void dispatch(ACLMessage msg) throws RemoteException, NotFoundException;
  void ping() throws RemoteException;
  AgentContainer getContainer() throws RemoteException;

}

