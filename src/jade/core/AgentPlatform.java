/*
  $Log$
  Revision 1.7  1998/11/03 00:28:56  rimassa
  Added an exeption specification to deadAgent() method.

  Revision 1.6  1998/10/11 19:21:32  rimassa
  Added the new name clash exception to bornAgent() throws clause.

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

  public void bornAgent(String name, AgentDescriptor desc) throws RemoteException, NameClashException;
  public void deadAgent(String name) throws RemoteException, NotFoundException;

  public AgentDescriptor lookup(String agentName) throws RemoteException, NotFoundException;

}
