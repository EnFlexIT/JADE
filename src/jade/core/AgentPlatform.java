package jade.core;

import java.rmi.Remote;
import java.rmi.RemoteException;

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
