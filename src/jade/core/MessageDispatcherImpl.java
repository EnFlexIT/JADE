/*
  $Log$
  Revision 1.4  1998/10/04 18:01:09  rimassa
  Added a 'Log:' field to every source file.

*/

package jade.core;

import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import java.util.Hashtable;

import jade.lang.acl.*;

class MessageDispatcherImpl extends UnicastRemoteObject implements MessageDispatcher {

  private    ACLParser    parser = null;
  private    Hashtable    localAgents;


  public MessageDispatcherImpl(Hashtable h) throws RemoteException {
    localAgents = h;
  }
  
  public void dispatch(ACLMessage msg) throws RemoteException, NotFoundException {
    String receiverName = msg.getDest();
    Agent receiver = (Agent)localAgents.get(receiverName);

    if(receiver == null) 
      throw new NotFoundException("Message Dispatcher failed to find " + receiverName);

    receiver.postMessage(msg);
  }

}
