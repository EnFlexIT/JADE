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

package jade.security.dummy;

import jade.security.*;

import jade.core.AID;
import jade.core.ContainerID;


public class DummyPrincipal implements AgentPrincipal, UserPrincipal, ContainerPrincipal {
	
	protected String name1 = null;
	protected String name2 = null;
	protected static final char sep = '/';
	
	public DummyPrincipal() {
	}
	
	public DummyPrincipal(String name) {
		init(name);
	}
	
	public void init(String name) {
		if (name.indexOf(sep) == -1)
			name = name + sep;
		int pos = name.indexOf(sep);
		
		name1 = pos > 0 ? name.substring(0, pos) : null;
		name2 = pos < name.length() - 1 ? name.substring(pos + 1, name.length()) : null;
	}
	
	public void init(AID agentID, UserPrincipal user) {
		name1 = user != null ? user.getName() : null;
		name2 = agentID != null ? agentID.getName() : null;
	}
	
	public void init(ContainerID containerID, UserPrincipal user) {
		name1 = user != null ? user.getName() : null;
		name2 = containerID != null ? containerID.getName() : null;
	}
	
	public String getName() {
		return (name1 != null ? name1 : "") + (name2 != null ? sep + name2 : "");
	}
	
	public AID getAgentID() {
		return name2 != null ? new AID(name2, AID.ISLOCALNAME) : null;
	}
	
	public ContainerID getContainerID() {
		return name2 != null ? new ContainerID(name2, null) : null;
	}
	
	public UserPrincipal getUser() {
		return name1 != null ? new DummyPrincipal(name1) : null;
	}
	
	public String toString() {
		return getName();
	}
	
	public boolean equals(Object o) {
		return (o != null) && getName().equals(o.toString());
	}
	
}
