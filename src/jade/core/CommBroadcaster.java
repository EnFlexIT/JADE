/*
 * $Log$
 * Revision 1.4  1999/03/03 16:04:46  rimassa
 * Made CommBroadcaster interface package scoped instead of publicly
 * available.
 *
 * Revision 1.3  1998/10/04 18:01:03  rimassa
 * Added a 'Log:' field to every source file.
 *
 */

package jade.core;


/***************************************************************

  Name: CommBroadcaster

  Responsibilities and Collaborations:

  + Abstracts the notion of a CommEvent broadcaster.

******************************************************************/
interface CommBroadcaster {
  void addCommListener   (CommListener l);
  void removeCommListener(CommListener event);
}



