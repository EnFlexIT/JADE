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

import java.security.Permission;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DelegationCertificate extends JADECertificate implements java.io.Serializable {
  List permissions = new ArrayList();
  
  public DelegationCertificate() {
  }
  
  public DelegationCertificate(DelegationCertificate delegation) {
    super(delegation);
    for (int i = 0; i < delegation.permissions.size(); i++)
      permissions.add(delegation.permissions.get(i));
  }

	public void addPermissionHolders(Object o) {
    PermissionHolder perm = (PermissionHolder)o;
		permissions.add(perm.getPermission());
	}
	
	public Iterator getAllPermissionHolders() {
		ArrayList list = new ArrayList();
		for (int i = 0; i < permissions.size(); i++) {
			Permission p = (Permission) permissions.get(i);
			list.add(new PermissionHolder(p));
		}
		return list.iterator();
	}

  public void addPermission(Permission p) {
		permissions.add(p);
  }

	public Iterator getAllPermissions() {
		return permissions.iterator();
	}

	public byte[] getEncoded() {
		StringBuffer str = new StringBuffer(new String(super.getEncoded()));
		for (int i = 0; i < permissions.size(); i++)
		  str.append(permissions.get(i).toString()).append("\n");
		return str.toString().getBytes();
	}
	
	public String toString() {
		StringBuffer str = new StringBuffer(super.toString());
		for (int i = 0; i < permissions.size(); i++)
		  str.append(permissions.get(i).toString()).append("\n");
		return str.toString();
	}
	
}
