/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop
multi-agent systems in compliance with the FIPA specifications.
Copyright (C) 2000 CSELT S.p.A. 

GNU Lesser General Public License

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation, 
version 2.1 of the License. 

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the
Free Software Foundation, Inc., 59 Temple Place - Suite 330,
Boston, MA  02111-1307, USA.
*****************************************************************/

package jade.core;

import java.io.Serializable;

import java.util.Enumeration;
import java.util.Vector;

import jade.domain.FIPAAgentManagement.AID;

/**
   Represents a group of agent names.
   This class allows to hold a set of agent names together, in order
   to perform message multicasting.
   @see jade.core.Agent#send(ACLMessage msg, AgentGroup g)
	
   
   @author Giovanni Rimassa - Universita` di Parma
   @version $Date$ $Revision$

*/
public class AgentGroup implements Cloneable, Serializable {

  /**
  @serial
  */
	private Vector memberNames = new Vector();

  /**
     Adds an agent to this AgentGroup.
     @param name The agent id to add to this group.
   */
  public void addMember(AID name) {
    memberNames.addElement(name);
  }

  /**
     Removes an agent from this group based on its GUID case-insensitive
     :name attribute 
     @param name The agent id to remove from this group.
   */
  public void removeMember(AID name) {
    if (! memberNames.removeElement(name))  
      // if not found then search on the basis of its GUID :name
      for (int i=0; i<memberNames.size(); i++)
	try {
	  if (((AID)memberNames.get(i)).getName().equalsIgnoreCase(name.getName()))
	    memberNames.removeElementAt(i);
	} catch (ArrayIndexOutOfBoundsException e) {
	  e.printStackTrace(); // should never happen
	} catch (NullPointerException n) {
	  // this exception might be throws if :name or AID is null in one element
	  // of the vector. If it happens, then no problem, just continue. 
	}
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
    String str = "(set ";
    for (int i = 0; i < memberNames.size(); i++)
      str = str + ((AID)memberNames.elementAt(i)).toString() + " ";
    return str + ")";
  }
}
  
}
