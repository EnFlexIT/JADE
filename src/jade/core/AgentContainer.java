/*
 * $Log$
 * Revision 1.6  1998/10/25 23:55:19  rimassa
 * Added some function for remote agent management.
 *
 * Revision 1.5  1998/10/11 19:12:26  rimassa
 * Added a method to invalidate an entry of remote agents cache.
 *
 * Revision 1.4  1998/10/04 18:00:56  rimassa
 * Added a 'Log:' field to every source file.
 *
 */

package jade.core;

import java.rmi.Remote;
import java.rmi.RemoteException;


public interface AgentContainer extends Remote {

  static final boolean NOSTART = false;
  static final boolean START = true;

  void createAgent(String agentName, String className, boolean startIt) throws RemoteException;
  void createAgent(String agentName, Agent instance, boolean startIt) throws RemoteException;

  void killAgent(String agentName) throws RemoteException, NotFoundException;


  void invalidateCacheEntry(String key) throws RemoteException;

}

