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

package jade.security;

import jade.util.leap.List;
import java.util.Date;


/**
	The <code>DelegationCeritificate</code> interface is meant
	to carry a list of permissions granted to certain principals.
	It can be obtained after authenticating before an authority
	or by an another (already authenticated) principal which owns
	a list of permissions and wants to delegate them.
   
	@author Michele Tomaiuolo - Universita` di Parma
	@version $Date$ $Revision$
*/
public interface DelegationCertificate extends JADECertificate {
	
	/**
		Adds a permission to this delegation certificate.
		@param permission The permission to add.
	*/
	public void addPermission(Object permission);

	/**
		Adds a list of permissions to this delegation certificate.
		@param permissions The list of permissions to add.
	*/
	public void addPermissions(List permissions);

	/**
		Returns all permissions carried by this certificate.
		@return The list of permissions.
	*/
	public List getPermissions();
	
}
