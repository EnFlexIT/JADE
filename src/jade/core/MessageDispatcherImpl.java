/*
  $Id$
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
