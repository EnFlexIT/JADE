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

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Vector;

/**
	The <code>Authority</code> class is an abstract class which represents
	the authorities of the platform. It has methods for signing certificates
	and for verifying their validity.
	
	@author Michele Tomaiuolo - Universita` di Parma
	@version $Date$ $Revision$
*/
public class PlatformAuthority extends Authority {

  class UserEntry {
    String usr;
    String key;
  }
  
  String passwdFile;
  Vector users = null;
  int serial = 1;
  byte[] key = new byte[] { 10, 11, 12, 13, 14, 15, 16, 17 };
  
  public void init (Object[] args) {
    if (args != null && args.length > 0 && args[0] instanceof String) {
      passwdFile = (String)args[0];
      readPasswdFile();
    }
  }
  
  public void readPasswdFile() {
    if (passwdFile == null) return;
    users = new Vector();
    try {
      BufferedReader file = new BufferedReader(new FileReader(passwdFile));
      String line = file.readLine();
      while (line != null) {
        line = line.trim();
        if (line.length() > 0) {
          UserEntry entry = new UserEntry();
          int sep = line.indexOf(":");
          if (sep != -1) {
            entry.usr = line.substring(0, sep);
            entry.key = line.substring(sep + 1, line.length());
          }
          else {
            entry.usr = line;
            entry.key = null;
          }
          System.out.println("usr = " + entry.usr + "; key = " + entry.key + ";");
          users.addElement(entry);
        }
        line = file.readLine();
      }
    }
    catch(Exception e) { e.printStackTrace(); }
  }
  
	/**
		Checks the validity of a given certificate.
		The period of validity is tested, as well as the integrity
		(verified using the carried signature as proof).
		@param cert The certificate to verify.
		@throws AuthenticationException if the certificate is not
			integer or is out of its validity period.
	*/
	public void verify(JADECertificate cert) throws AuthenticationException {
	}
		
	/**
		Signs a new certificate. The certificates presented with the
		<code>subj</code> param are verified and the permissions to
		certify are matched against the possessed ones.
		The period of validity is tested, as well as the integrity
		(verified using the carried signature as proof).
		@param cert The certificate to sign.
		@param subj The subject containing the initial certificates.
		@throws AuthorizationException if the permissions are not owned
			or delegation modes are violated.
		@throws AuthenticationException if the certificates have some
			inconsistence or are out of validity.
	*/
	public void sign(JADECertificate certificate, JADESubject subject) throws JADESecurityException {
	}
	
	public JADESubject authenticateUser(UserPrincipal user, byte[] passwd) throws JADESecurityException {
	  System.out.println(user.getName());
	  System.out.println(new String(passwd));
    IdentityCertificate cert = null;
	  String name = user.getName();
	  if (users == null) {
	    return null;
	  }
	  else for (int i = 0; i < users.size() && cert == null; i++) {
	    UserEntry entry = (UserEntry)users.elementAt(i);
	    if (entry.usr.equals(name)) {
	      if (passwd.length != entry.key.length())
	        throw new AuthenticationException("Wrong password");
	      for (int j = 0; j < entry.key.length(); j++) {
	        if (entry.key.charAt(j) != passwd[j])
  	        throw new AuthenticationException("Wrong password");
	      }
	      // ok: user found + exact password
	      cert = new IdentityCertificate();
	      cert.init(user, 0, 0);
	      cert.issue(new Principal(this.name), key, serial++);
	      byte[] signature = new byte[] { 20, 21, 22, 23, 24, 25, 26, 27 };
	      cert.sign(signature);
	      return new JADESubject(cert, new DelegationCertificate[] {});
	    }
	  }
	  throw new AuthenticationException("Unknown user");
	}
}
