package jade.core;

import java.util.Hashtable;
import java.util.Vector;

import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;

public class AgentPlatformImpl extends AgentContainerImpl implements AgentPlatform {

  // Initial size of agent hash table
  private static final int GLOBALMAP_SIZE = 100;

  // Load factor of agent hash table
  private static final float GLOBALMAP_LOAD_FACTOR = 0.25f;

  private Vector containers = new Vector();
  private Hashtable platformAgents = new Hashtable(GLOBALMAP_SIZE, GLOBALMAP_LOAD_FACTOR);

  public AgentPlatformImpl() throws RemoteException {
  }

  public void addContainer(AgentContainer ac) throws RemoteException {
    System.out.println("Adding container...");
    containers.addElement(ac);
  }

  public void removeContainer(AgentContainer ac) throws RemoteException {
    System.out.println("Removing container...");
    containers.removeElement(ac);
  }

  public void bornAgent(AgentDescriptor desc) throws RemoteException {
    System.out.println("Born agent " + desc.getName());
    platformAgents.put(desc.getName(), desc);
  }

  public void deadAgent(String name) throws RemoteException {
    System.out.println("Dead agent " + name);
    platformAgents.remove(name);
    // FIXME: Must update all container caches
  }

  public AgentDescriptor lookup(String agentName) throws RemoteException, NotFoundException {
    System.out.println("Looking up " + agentName + " in agents table...");
    Object o = platformAgents.get(agentName);
    if(o == null)
      throw new NotFoundException("Failed to find " + agentName);
    else
      return (AgentDescriptor)o;
  }

}

