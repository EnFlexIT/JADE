/*
  $Log$
  Revision 1.5  1999/02/04 11:46:06  rimassa
  Added getMembers() method. Added clone() and toString() methods to
  better support fipa-contract-net protocol.

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
public class AgentGroup implements Cloneable {

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

  // FIXME: seems unnecessary to me ...
  public Enumeration getMembers(){
    return memberNames.elements();
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

  /**
   * public method to clone this object. 
   * @return an istance of this object that is a field-by-field copy.
   * This instance must be then cast to (AgentGroup) type.
   */
  public synchronized Object clone() {
    Object result;
    try {
      result = super.clone();
    }
    catch(CloneNotSupportedException cnse) {
      throw new InternalError(); // This should never happen
    }
    return result;
  }

  /**
   * @return a String representing this AgentGroup
   */
public String toString() {
  if (memberNames.size() == 1)
    return (String)memberNames.elementAt(0);
  else {
    String str="(";
    for (int i=0; i<memberNames.size(); i++)
      str = str + (String)memberNames.elementAt(i)+" ";
    return str+")";
  }
}
  
}

