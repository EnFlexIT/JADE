/*
 * $Log$
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

/* 

   FIXME: To support GUI direct intervention, some methods such as
          startAgent() will be needed. Making them remote will help
          with distributed platform administration.
*/
public interface AgentContainer extends Remote {

  void invalidateCacheEntry(String key) throws RemoteException;

}

