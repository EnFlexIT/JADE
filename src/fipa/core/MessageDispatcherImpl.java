package fipa.core;

import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import java.util.Hashtable;

import fipa.lang.acl.*;

class MessageDispatcherImpl extends UnicastRemoteObject implements MessageDispatcher {

  private    ACLParser    parser = null;
  private    Hashtable    localAgents;


  public MessageDispatcherImpl(Hashtable h) throws RemoteException {
    localAgents = h;
  }
  
  public void dispatch(ACLMessage msg) throws RemoteException, NotFoundException {
    System.out.println(" Dispatching ...");
    String receiverName = msg.getDest(); // FIXME: Will be 'msg.getValue(":dest");'
    Agent receiver = localAgents.get(receiverName);

    if(receiver == null) 
      throw new NotFoundException("Message Dispatcher failed to find " + receiverName);

    receiver.postMessage(msg);
  }

  public void dispatch(String msg) throws RemoteException, NotFoundException {
    // FIXME: To be implemented
    // Parse the string into an ACL message, then invoke dispatch with it
  }

}
