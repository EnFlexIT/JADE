/*
 * $Log$
 * Revision 1.6  1999/08/10 15:34:41  rimassa
 * Added copySource() and moveSource() methods.
 *
 * Revision 1.5  1999/04/06 00:09:37  rimassa
 * Documented public classes with Javadoc. Reduced access permissions wherever possible.
 *
 * Revision 1.4  1998/10/31 16:36:13  rimassa
 * Added a new method endSource() to notify a CommListeners when its
 * CommBroadcaster dies.
 *
 * Revision 1.3  1998/10/04 18:01:05  rimassa
 * Added a 'Log:' field to every source file.
 *
 */

package jade.core;

import java.util.EventListener;

/***************************************************************

  Name: CommListener

  Responsibilities and Collaborations:

  + Exposes a method to handle outgoing messages.
  + Offers a restricted interface to agent life cycle.

******************************************************************/
interface CommListener extends EventListener {
  void CommHandle(CommEvent event);
  void endSource(String name);
  void moveSource(String name, String where);
  void copySource(String name, String where, String newName);

}
