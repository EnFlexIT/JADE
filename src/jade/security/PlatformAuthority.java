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

import jade.core.Profile;

import java.io.BufferedReader;
import java.io.FileReader;

import java.util.Enumeration;
import java.util.Vector;

import java.security.*;


/**
	The <code>Authority</code> class is an abstract class which represents
	the authorities of the platform. It has methods for signing certificates
	and for verifying their validity.
	b 
	@author Michele Tomaiuolo - Universita` di Parma
	@version $Date$ $Revision$
*/
public class PlatformAuthority extends ContainerAuthority {

  class UserEntry {
    String usr;
    String key;
  }
  
  String passwdFile;
  Vector users = null;
	
  public void init(Profile profile) {
  	if (profile != null) {
  		try {
      	passwdFile = ((jade.BootProfileImpl)profile).getArgProperties().getProperty("jade.security.passwd");
	      parsePasswdFile();
      }
      catch (Exception e) { e.printStackTrace(); }
    }
    
    System.out.println("" + getPermissions(new UserPrincipal("all.users.tomamic")));
  }
  
  public void parsePasswdFile() {
  	System.out.println("parsing passwd file " + passwdFile);
    if (passwdFile == null) return;
    users = new Vector();
    try {
      BufferedReader file = new BufferedReader(new FileReader(passwdFile));
      String line = file.readLine();
      while (line != null) {
        line = line.trim();
        if (line.length() > 0) {
          UserEntry entry = new UserEntry();
          int sep = line.indexOf(':');
          if (sep != -1) {
            entry.usr = line.substring(0, sep);
            entry.key = line.substring(sep + 1, line.length());
          }
          else {
            entry.usr = line;
            entry.key = null;
          }
          System.out.println("username=" + entry.usr + "; password=" + entry.key + ";");
          users.addElement(entry);
        }
        line = file.readLine();
      }
    }
    catch(Exception e) { e.printStackTrace(); }
  }
  
	public void authenticateUser(IdentityCertificate identity, DelegationCertificate delegation, byte[] passwd) throws AuthException {
	  BasicPrincipal principal = identity.getSubject();
	  if (principal instanceof AgentPrincipal)
	    principal = ((AgentPrincipal)principal).getUser();

	  UserPrincipal user = null;
	  if (principal instanceof UserPrincipal)
	  	user = (UserPrincipal)principal;
	  else
	  	return;

	  System.out.println("authenticating user");
	  System.out.println("username=" + user.getName() + ";");
	  System.out.println("password=" + new String(passwd) + ";");
	  String name = user.getName();
	  if (users == null)
	      return;
	  else for (int i = 0; i < users.size(); i++) {
	    UserEntry entry = (UserEntry)users.elementAt(i);
	    if (entry.usr.equals(name)) {
	      if (passwd.length != entry.key.length())
	        throw new AuthenticationException("Wrong password");
	      for (int j = 0; j < entry.key.length(); j++) {
	        if (entry.key.charAt(j) != passwd[j])
  	        throw new AuthenticationException("Wrong password");
	      }

	      // ok: user found + exact password
	      // add permissions here
				PermissionCollection perms = getPermissions(user);
				for (Enumeration e = perms.elements(); e.hasMoreElements();) {
					Permission p = (Permission)e.nextElement();
					delegation.addPermission(p);
				}

	      return;
	    }
	  }
	  throw new AuthenticationException("Unknown user");
	}
	
	private PermissionCollection getPermissions(UserPrincipal user) {
		Policy policy = Policy.getPolicy();
		CodeSource source = new CodeSource(null, null);
		
		ProtectionDomain nullDomain = new ProtectionDomain(
				source, null, null, null);
		PermissionCollection nullPerms = policy.getPermissions(nullDomain);
		
		Permissions perms = new Permissions();

		while (user != null) {
			ProtectionDomain userDomain = new ProtectionDomain(
					source, null, getClass().getClassLoader(), new java.security.Principal[] {user});
			PermissionCollection userPerms = policy.getPermissions(userDomain);

			for (Enumeration e = userPerms.elements(); e.hasMoreElements();) {
				Permission p = (Permission)e.nextElement();
				if (p instanceof UnresolvedPermission) {
					p = resolve((UnresolvedPermission)p);
				}
				if (!nullPerms.implies(p))
					perms.add(p);
			}
			user = user.getParentUser();
		}

		return perms;
	}
	
	private Permission resolve(UnresolvedPermission up) {
		String str = up.toString();
		int blank0 = str.indexOf(' ');
		int blank1 = str.indexOf(' ', blank0 + 1);
		int blank2 = str.indexOf(' ', blank1 + 1);
		int blank3 = str.indexOf(' ', blank2 + 1);
		if (blank3 < 0) blank3 = str.length() - 1;
		String type = str.substring(blank0 + 1, blank1);
		String name = str.substring(blank1 + 1, blank2);
		String actions = str.substring(blank2 + 1, blank3);
		
		System.out.println("resolving " + up);
		System.out.println("type=" + type + ";");
		System.out.println("name=" + name + ";");
		System.out.println("actions=" + actions + ";");
		
		Permission resolved = null;
		try {
			resolved = (Permission)Class.forName(type).getConstructor(new Class[] {String.class, String.class}).newInstance(new Object[] {name, actions});
		}
		catch (Exception e) { e.printStackTrace(); }
		return resolved;
	}
}
