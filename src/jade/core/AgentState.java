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

//#APIDOC_EXCLUDE_FILE

/**

  This class represents the Life-Cycle state of an agent.

  @author Giovanni Rimassa - Universita` di Parma
  @version $Date$ $Revision$
*/
public class AgentState {

  public static AgentState getInstance(int value) {
      for(int i = 0; i < STATES.length; i++) {
	  AgentState as = STATES[i];
	  if(as.getValue() == value) {
	      return as;
	  }
      }

      return null;
  }

  public static AgentState[] getAllInstances() {
      return STATES;
  }

  public AgentState() {
  }

  private AgentState(String n, int v) {
    name = n;
    value = v;
  }

  public void setName(String n) { 
    name = n;
  }

  public String getName() {
    return name;
  }

  public void setValue(int v) {
      value = v;
  }

  public int getValue() {
      return value;
  }

  public boolean equals(Object o) {

    if(o instanceof String) {
      return CaseInsensitiveString.equalsIgnoreCase(name, (String)o);
    }
    try {
      AgentState as = (AgentState)o;
      return CaseInsensitiveString.equalsIgnoreCase(name, as.name);
    }
    catch(ClassCastException cce) {
      return false;
    }

  }

  public String toString() {
      return name;
  }

  public int compareTo(Object o) {
    AgentState as = (AgentState)o;
		return name.toLowerCase().toUpperCase().compareTo(as.name.toLowerCase().toUpperCase());
  }

  public int hashCode() {
    return name.toLowerCase().hashCode();
  }

    private static final AgentState[] STATES = new AgentState[] { 
	new AgentState("Illegal MIN state", Agent.AP_MIN),
	new AgentState("Initiated", Agent.AP_INITIATED),
	new AgentState("Active", Agent.AP_ACTIVE),
	new AgentState("Idle", Agent.AP_IDLE),
	new AgentState("Suspended", Agent.AP_SUSPENDED),
	new AgentState("Waiting", Agent.AP_WAITING),
	new AgentState("Deleted", Agent.AP_DELETED),
	//#MIDP_EXCLUDE_BEGIN
	new AgentState("Transit", Agent.AP_TRANSIT),
	new AgentState("Copy", Agent.AP_COPY),
	new AgentState("Gone", Agent.AP_GONE),
	//#MIDP_EXCLUDE_END
	new AgentState("Illegal MAX state", Agent.AP_MAX)
    };

    // For persistence service
    private Long persistentID;

    // For persistence service
    private Long getPersistentID() {
	return persistentID;
    }

    // For persistence service
    private void setPersistentID(Long l) {
	persistentID = l;
    }

    private String name;
    private int value;



}
