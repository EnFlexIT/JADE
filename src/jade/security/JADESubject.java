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

import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.ProtectionDomain;

/**

  ...

  @author Michele Tomaiuolo - Universita` di Parma
  @version $Date$ $Revision$
*/
public class JADESubject implements java.security.Principal, java.io.Serializable {
  
  IdentityCertificate identity;
  DelegationCertificate[] delegations;

  public JADESubject() {
  }

  public JADESubject(IdentityCertificate identity, DelegationCertificate[] delegations) {
    this.identity = identity;
    this.delegations = delegations;
  }

  public void setIdentity(IdentityCertificate identity) { 
    this.identity = identity;
  }

  public void setDelegations(DelegationCertificate[] delegations) { 
    this.delegations = delegations;
  }
  
  public IdentityCertificate getIdentity() {
    return identity;
  }

  public DelegationCertificate[] getDelegations() {
    return delegations;
  }

  public String getName() {
    return (identity != null) ? identity.getSubject().getName() : null;
  }

  public boolean equals(Object o) {
    try {
      JADESubject s = (JADESubject)o;
      if (identity != null && !identity.equals(s.identity)) return false;
      if (identity == null && s.identity != null) return false;
      if (delegations != null) {
        if (s.delegations == null || s.delegations.length != delegations.length) return false;
        for (int i = 0; i < delegations.length; i++)
          if (!delegations[i].equals(s.delegations)) return false;
      }
      else {
        if (s.delegations != null) return false;
      }
      return true;
    }
    catch(ClassCastException cce) {
      return false;
    }
  }

	/**
		Performs a privileged action with the permissions owned by this subject.
		@param action The action to perform.
		@throws AuthorizationException if the action requires not owned permissions.
	*/
	public void doPrivileged(PrivilegedAction action) throws AuthorizationException {
	  /*DomainCombiner combiner = new jade.security.SubjectDomainCombiner(this);
		AccessControlContext acc = new AccessControlContext(AccessController.getContext(), combiner);
  	try {
	  	AccessController.doPrivileged(action, acc);
	  }
	  catch (java.security.AccessControlException e) {
	  	throw new AuthorizationException(e.getMessage());
  	}*/
	}

}
