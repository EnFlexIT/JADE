/*
  $Log$
  Revision 1.13  1999/10/08 08:26:34  rimassa
  Added an explanatory comments for a behaviour change (case
  sensitiveness).

  Revision 1.12  1999/10/06 14:49:03  rimassa
  Removed a couple of 'toLowerCase()' calls, to preserve case in agent
  names when inserted and extracted into and from an AgentGroup.

  Revision 1.11  1999/04/08 12:00:51  rimassa
  Changed clone() method to correctly implement a deep copy.

  Revision 1.10  1999/04/06 00:09:34  rimassa
  Documented public classes with Javadoc. Reduced access permissions wherever possible.

  Revision 1.9  1999/03/09 12:57:44  rimassa
  Added 'implements Serializable' clause to AgentGroup class, since now
  it an AgentGroup is part of ACLMessage objects.

  Revision 1.8  1999/03/07 22:50:52  rimassa
  Added a reset() method to remove all group members.

  Revision 1.7  1999/02/25 08:27:26  rimassa
  Made AgentGroup case-insensitive with respect to agent names.

  Revision 1.6  1999/02/14 23:12:09  rimassa
  Removed reset(), hasMoreMembers() and nextMember() methods. Added
  getMembers() and size() methods.

  Revision 1.5  1999/02/04 11:46:06  rimassa
  Added getMembers() method. Added clone() and toString() methods to
  better support fipa-contract-net protocol.

  Revision 1.4  1998/11/03 00:40:56  rimassa
  Changed a method name from resetCursor() to reset().

  Revision 1.3  1998/10/04 18:00:59  rimassa
  Added a 'Log:' field to every source file.

*/

package jade.core;

import java.io.Serializable;

import java.util.Enumeration;
import java.util.Vector;

/**
   Represents a group of agent names.
   This class allows to hold a set of agent names together, in order
   to perform message multicasting.
   @see jade.core.Agent#send(ACLMessage msg, AgentGroup g)

   @author Giovanni Rimassa - Universita` di Parma
   @version $Date$ $Revision$

*/
public class AgentGroup implements Cloneable, Serializable {

  private Vector memberNames = new Vector();

  /**
     Adds an agent to this AgentGroup.
     @param name The agent name to add to this group.
   */
  public void addMember(String name) {
    memberNames.addElement(name);
  }

  /**
     Removes an agent from this group. 
     @param name The agent name (case-sensitive) to remove from this group.
   */
  public void removeMember(String name) {
     memberNames.removeElement(name);
  }

  /**
     Empties this agent group.
     Calling this method removes all member agents from this group,
     permitting to reuse this object for other tasks.
  */
  public void reset() {
    memberNames.removeAllElements();
  }

  /**
     Provide access to group members as <code>Enumeration</code>.
     @return An <code>Enumeration</code> allowing sequential iteration
     across all member names.    
     @see java.util.Enumeration
  */
  public Enumeration getMembers(){
    return memberNames.elements();
  }

  /**
     Reads the size of this group.
     @return A non negative <code>int</code> value, representing the
     number of agent members within this agent group.
  */
  public int size() {
    return memberNames.size();
  }

  /**
     Method to clone this object. 
     @return an istance of <code>AgentGroup</code> that is a
     field-by-field copy of this object.
     This instance must then be cast to (AgentGroup) type.
  */
  public synchronized Object clone() {
    AgentGroup result;
    try {
      result = (AgentGroup)super.clone();
      result.memberNames = (Vector)memberNames.clone(); // Deep copy
    }
    catch(CloneNotSupportedException cnse) {
      throw new InternalError(); // This should never happen
    }
    return result;
  }

  /**
     String conversion for this class.
     @return A String representing this AgentGroup.
   */
public String toString() {
  if(memberNames.size() == 1)
    return (String)memberNames.elementAt(0);
  else {
    String str = "(";
    for (int i = 0; i < memberNames.size(); i++)
      str = str + (String)memberNames.elementAt(i) + " ";
    return str + ")";
  }
}
  
}

