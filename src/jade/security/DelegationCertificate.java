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

package jade.security;

import jade.util.leap.ArrayList;
import jade.util.leap.Iterator;

public class DelegationCertificate extends JADECertificate implements java.io.Serializable {
  ArrayList permissions = new ArrayList();
  
  public DelegationCertificate() {
  }
  
  public DelegationCertificate(DelegationCertificate delegation) {
    super(delegation);
    for (int i = 0; i < delegation.permissions.size(); i++)
      permissions.add(delegation.permissions.get(i));
  }

  public void addPermissionHolder(PermissionHolder p) {
		permissions.add(p);
  }

	public Iterator getAllPermissionHolders() {
		return permissions.iterator();
	}

	public String toString() {
		StringBuffer str = new StringBuffer(super.toString());
		for (int i = 0; i < permissions.size(); i++)
		  str.append(permissions.get(i).toString()).append("\n");
		return str.toString();
	}
}
