/*
  $Log$
  Revision 1.10  1999/03/17 13:00:59  rimassa
  Some changes were made to the interface, to support new proxy-based
  design.

  Revision 1.9  1999/03/09 12:59:14  rimassa
  Added some String constants to represent container names.
  Added a getAddress() method.

  Revision 1.8  1999/02/03 10:05:44  rimassa
  Changed addContainer() signature: now it returns a String containing
  the IIOP address (in IOR or URL format).

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


public interface AgentPlatform extends AgentContainer {

  public static final String MAIN_CONTAINER_NAME = "Front-End";
  public static final String AUX_CONTAINER_NAME = "Container-";

  public String getAddress() throws RemoteException;

  public String addContainer(AgentContainer ac) throws RemoteException;
  public void removeContainer(String name) throws RemoteException;

  public void bornAgent(String name, RemoteProxy rp, String containerName) throws RemoteException, NameClashException;
  public void deadAgent(String name) throws RemoteException, NotFoundException;

  public RemoteProxy getProxy(String agentName, String agentAddress) throws RemoteException, NotFoundException;

}
