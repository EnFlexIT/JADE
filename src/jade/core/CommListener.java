/*
 * $Id$
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
