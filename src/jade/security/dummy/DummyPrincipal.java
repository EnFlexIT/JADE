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


public class DummyPrincipal implements AgentPrincipal, UserPrincipal, ContainerPrincipal, jade.util.leap.Serializable {
	
	protected String name1 = null;
	protected String name2 = null;
	protected static final char sep = '/';
	
	public DummyPrincipal() {
		name1 = NONE;
	}
	
	public DummyPrincipal(String name) {
		init(name);
	}
	
	public void init(String name) {
		int pos = name.indexOf(sep);
		if (pos != -1) {
			this.name1 = name.substring(0, pos);
			this.name2 = name.substring(pos + 1, name.length());
		}
		else {
			this.name1 = name;
			this.name2 = null;
		}
	}
	
	public void init(AID agentID, UserPrincipal user) {
		if (agentID == null)
			this.name2 = null;
		else
			this.name2 = agentID.getName();
		if (user == null)
			this.name1 = null;
		else
			this.name1 = user.getName();
	}
	
	public void init(ContainerID containerID, UserPrincipal user) {
		if (containerID == null)
			this.name2 = null;
		else
			this.name2 = containerID.getName();
		if (user == null)
			this.name1 = null;
		else
			this.name1 = user.getName();
	}
	
	public String getName() {
		if (name2 != null)
			return name1 + sep + name2;
		else
			return name1;
	}
	
	public AID getAgentID() {
		if (name2 == null)
			return null;
		else
			return new AID(name2, AID.ISGUID);
	}
	
	public ContainerID getContainerID() {
		if (name2 == null)
			return null;
		else
			return new ContainerID(name2, null);
	}
	
	public UserPrincipal getUser() {
		if (name1 == null)
			return null;
		else
			return new DummyPrincipal(name1);
	}
	
	public String toString() {
		return getName();
	}

}
