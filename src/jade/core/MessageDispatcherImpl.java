/*
  $Log$
  Revision 1.5  1998/10/11 19:37:39  rimassa
  Implemented two new methods from MessageDispatcher interface: ping()
  and getContainer(). Changed the constructor to take an AgentContainer
  as a parameter.
  Fixed a missing toLowerCase() call.

  Revision 1.4  1998/10/04 18:01:09  rimassa
  Added a 'Log:' field to every source file.

*/

package jade.core;

import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import java.util.Hashtable;

import jade.lang.acl.*;

class MessageDispatcherImpl extends UnicastRemoteObject implements MessageDispatcher {

  private ACLParser parser = null;
  private AgentContainer owner;
  private Hashtable localAgents;


  public MessageDispatcherImpl(AgentContainer ac, Hashtable h) throws RemoteException {
    owner = ac;
    localAgents = h;
  }
  
  public void dispatch(ACLMessage msg) throws RemoteException, NotFoundException {
    String receiverName = msg.getDest();
    Agent receiver = (Agent)localAgents.get(receiverName.toLowerCase());

    if(receiver == null) 
      throw new NotFoundException("Message Dispatcher failed to find " + receiverName);

    receiver.postMessage(msg);
  }

  public void ping() throws RemoteException {
  }

  public AgentContainer getContainer() throws RemoteException {
    return owner;
  }

}
