/*
  $Id$
*/

package jade.core;

import java.util.Enumeration;
import java.util.Vector;

/**************************************************************

  Name: AgentGroup

  Responsibility and Collaborations:

  + Holds a set of agent names, to see them as a single group.

  + Provides means for iterating along the names, easing message
    multicast.
    (ACLMessage)

****************************************************************/
public class AgentGroup {

  private Vector memberNames = new Vector();
  private Enumeration iterator = memberNames.elements();

  public void addMember(String name) {
    memberNames.addElement(name);
  }

  public void removeMember(String name) {
    memberNames.removeElement(name);
  }

  public void resetCursor() {
    iterator = memberNames.elements();
  }

  public boolean hasMoreMembers() {
    return iterator.hasMoreElements();
  }

  public String getNextMember() {
    if(hasMoreMembers())
      return iterator.nextElement();
    else
      return null;
  }

}

