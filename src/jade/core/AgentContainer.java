/*
 * $Id$
 */

package fipa.core;

import java.rmi.Remote;
import java.rmi.RemoteException;

/* Tag interface. Presently it's useless, but in the future a callback
   interface between agent platform and agent container could become important.

   FIXME: To support GUI direct intervention, some methods such as
          startAgent() will be needed. Making them remote will help
          with distributed platform administration.
*/
interface AgentContainer extends Remote {
}

