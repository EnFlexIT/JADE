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

/**
	The <code>Authority</code> class is an abstract class which represents
	the authorities of the platform. It has methods for signing certificates
	and for verifying their validity.
	
	@author Michele Tomaiuolo - Universita` di Parma
	@version $Date$ $Revision$
*/
public class DummyAuthority extends Authority {

  int serial = 1;
  byte[] key = null;
  
  public DummyAuthority() {
    super();
  }
  
  public DummyAuthority(String name) {
    super(name);
  }
  
  public void init(Object[] args) {
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
	  IdentityCertificate identity = new IdentityCertificate();
	  identity.init(user, 0, 0);
	  identity.issue(new Principal(name), key, serial++);
	  identity.sign(null);
	  
	  DelegationCertificate delegation = new DelegationCertificate();
	  delegation.init(user, 0, 0);
	  delegation.issue(new Principal(name), key, serial++);
	  //delegation.addPermission(...);
	  delegation.sign(null);

	  return new JADESubject(identity, new DelegationCertificate[] {delegation});
	}

	public Object doAs(JADESubject subject, PrivilegedAction action) throws JADESecurityException {
	  return action.run();
	}
	
	public void checkPermission(PermissionHolder permission) throws JADESecurityException {
	}
	
}
