/*
 * $Id$
 */

package fipa.core;

import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;

import java.net.*;
import java.io.*;
import java.util.*;

import Parser.*;

/***********************************************************************

  Name: MessageDispatcher

  Responsibilities and Collaborations:

  + Receives incoming messages from other containers or from the
    platform registry.
    (ACLmessage, AgentPlatform)

  + Maintains a collection of local agents, indexed by agent names.
    (Agent)

  + Builds a suitable ACL message from the received string.
    (ACLmessage, ACLParser)

  + Notifies receiver agent, enqueueing the incoming message.
    (ACLmessage, Agent)

************************************************************************/
interface MessageDispatcher extends Remote {

  public void dispatch(ACLMessage msg) throws RemoteException;

}

class MessageDispatcherImpl extends UnicastRemoteObject implements MessageDispatcher {

  private    ACLParser    parser = null;
  private    Hashtable    localAgents;


  public MessageDispatcher(Hashtable h) throws RemoteException {
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

}
