/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop
multi-agent systems in compliance with the FIPA specifications.
Copyright (C) 2002 TILAB S.p.A. 

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


public class DummyPrincipal implements JADEPrincipal, jade.util.leap.Serializable {
	
	protected String name = null;
	
	public DummyPrincipal() {
	}
	
	public DummyPrincipal(String name) {
		this.name = name;
	}
	
	public DummyPrincipal(AID agentID, String ownership) {
		this.name = ownership;
	}
	
	public DummyPrincipal(ContainerID containerID, String ownership) {
		this.name = ownership;
	}
	
	public String getOwnership() {
		return name;
	}
	
	public String getName() {
		return (name != null) ? name : "";
	}
	
	public boolean implies(JADEPrincipal p) {
		return true;
	}
	
	public String toString() {
		return getName();
	}

        public jade.security.SDSIName getSDSIName() {
         
               return new jade.security.SDSIName (){
                  public String getAlgorithm() { return " "; }
                  public byte[] getEncoded()  { return new byte[] {}; }
                  public String getFormat() { return " "; }
                  public String[] getLocalNames() { return new String[] {"."}; }
                  public String getLastLocalName() { return ""; }
    };
        }

  public byte[] getEncoded() {
    return toString().getBytes();
  }

  public boolean equals(Object object) {
    return false;
  }

  public int hashCode() {
    return 0;
  }
}
