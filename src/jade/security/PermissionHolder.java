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

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.security.Permission;

/**
	A simple class to transmit permissions in ACL messages.
	Methods respect conventions defined for objects corresponding
	to roles of JADE ontologies.
	
	@author Michele Tomaiuolo - Universita` di Parma
	@version $Date$ $Revision$
*/
public class PermissionHolder {

	/**
		The name of the Permission subclass.
	*/
	private String className;
	
	/**
		The name of the Permission.
	*/
	private String name;
	
	/**
		The actions of the Permission.
	*/
	private String actions;
	
	
	/**
		Creates a new empty PermissionHolder.
	*/
	public PermissionHolder() {
	}
	
	/**
		Creates a new PermissionHolder.
		@param p The permission to transmit.
	*/
	public PermissionHolder(Permission p) {
		className = p.getClass().getName();
		if (p.getName() != null)
			name = p.getName();
		if (p.getActions() != null)
			actions = p.getActions();
	}
	
	/**
		Restores the permission encapsulated in this object.
		@return The transmitted permission.
	*/
	public Permission getPermission() {
		Permission perm = null;
		try {
  	  if (actions != null)
	  		perm = (java.security.Permission)(Class.forName(getClassName()).getConstructor(new Class[]{String.class, String.class}).newInstance(new Object[]{name, actions}));
		  else if (name != null)
	  		perm = (java.security.Permission)(Class.forName(getClassName()).getConstructor(new Class[]{String.class}).newInstance(new Object[]{name}));
		  else
	  		perm = (java.security.Permission)(Class.forName(getClassName()).getConstructor(new Class[]{}).newInstance(new Object[]{}));
		}
		catch (Exception cnfe) {
			cnfe.printStackTrace();
		}
		return perm;
	}
	
	/**
		Sets the class name.
		@param cn The class name.
	*/
	public void setClassName(String cn) {
		className = cn;
	}

	/**
		Gets the class name.
		@return The class name.
	*/
	public String getClassName() {
		return className;
	}

	/**
		Sets permission name.
		@param cn The name of the permission.
	*/
	public void setName(String n) {
		name = n;
	}

	/**
		Gets permission name.
		@return The name of the permission.
	*/
	public String getName() {
		return name;
	}

	/**
		Sets permission actions.
		@param cn The actions of the permission.
	*/
	public void setActions(String a) {
		actions = a;
	}

	/**
		Gets permission actions.
		@return The actions of the permission.
	*/
	public String getActions() {
		return actions;
	}

	public String toString() {
		StringBuffer str = new StringBuffer();
		str.append("PermissionHolder: ").append(className);
		if (name != null)
  		str.append(", ").append(name);
		if (actions != null)
  		str.append(", ").append(actions);
  	str.append(";");
 		return str.toString();
  }
	
}
