/*
 * $Id$
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



