/*
  $Log$
  Revision 1.4  1998/11/03 00:40:56  rimassa
  Changed a method name from resetCursor() to reset().

  Revision 1.3  1998/10/04 18:00:59  rimassa
  Added a 'Log:' field to every source file.

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
    (ACLMessage,CommEvent)

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

  public void reset() {
    iterator = memberNames.elements();
  }

  public boolean hasMoreMembers() {
    return iterator.hasMoreElements();
  }

  public String getNextMember() {
    if(hasMoreMembers())
      return (String)iterator.nextElement();
    else
      return null;
  }

}

