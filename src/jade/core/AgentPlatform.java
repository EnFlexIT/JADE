/*
  $Log$
  Revision 1.5  1998/10/04 18:01:00  rimassa
  Added a 'Log:' field to every source file.

*/

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
public interface AgentPlatform extends AgentContainer {
  public void addContainer(AgentContainer ac) throws RemoteException;
  public void removeContainer(AgentContainer ac) throws RemoteException;

  public void bornAgent(String name, AgentDescriptor desc) throws RemoteException;
  public void deadAgent(String name) throws RemoteException;

  public AgentDescriptor lookup(String agentName) throws RemoteException, NotFoundException;

}
