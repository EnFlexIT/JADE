package fipa.core;

import java.util.Hashtable;
import java.util.Vector;

import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;

/************************************************************************

  Name: AgentPlatform

  Responsibilities and Collaborations:

  + Provides a global access point for the agent platform as a whole.


  + Maintains a list of the Object Reference for every agent container.
    (AgentContainer)

  + Holds a complete table of agent descriptors, knowing the container
    which every agent is in.
    (AgentContainer, AgentDescriptor)

************************************************************************/
interface AgentPlatform extends Remote {
  public void addContainer(AgentContainer ac) throws RemoteException;
  public void removeContainer(AgentContainer ac) throws RemoteException;

  public void bornAgent(AgentDescriptor desc) throws RemoteException;
  public void deadAgent(String name) throws RemoteException;

  public AgentDescriptor lookup(String agentName) throws RemoteException, NotFoundException;

}


class AgentPlatformImpl extends UnicastRemoteObject implements AgentPlatform {

  // Initial size of agent hash table
  private static final MAP_SIZE = 100;

  // Load factor of agent hash table
  private static final MAP_LOAD_FACTOR = 0.25f;

  private Vector containers = new Vector();
  private Hashtable agentMap = new Hashtable(MAP_SIZE, MAP_LOAD_FACTOR);


  public synchronized void addContainer(AgentContainer ac) throws RemoteException {
    containers.addElement(ac);
  }

  public synchronized void removeContainer(AgentContainer ac) throws RemoteException {
    containers.removeElement(ac);
  }

  public void bornAgent(AgentDescriptor desc) throws RemoteException {
    agentMap.put(desc.getName(), desc.getContainer());
  }

  public void deadAgent(String name) throws RemoteException {
    agentMap.remove(name);
  }

  public AgentDescriptor lookup(String agentName) throws RemoteException, NotFoundException {
    Object o = agentMap.get(agentName);
    if(o == null) throw new NotFoundException("Failed to find " + agentName);
    else return (AgentDescriptor)o;
  }

}

