/*
 * $Log$
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
public interface CommBroadcaster {
  void addCommListener   (CommListener l);
  void removeCommListener(CommListener event);
}



