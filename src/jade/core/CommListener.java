/*
 * $Log$
 * Revision 1.3  1998/10/04 18:01:05  rimassa
 * Added a 'Log:' field to every source file.
 *
 */

package jade.core;

import java.util.EventListener;

/***************************************************************

  Name: CommListener

  Responsibilities and Collaborations:

  + Exposes a method to handle incoming messages.

******************************************************************/
public interface CommListener extends EventListener {
  void CommHandle( CommEvent event );
}
