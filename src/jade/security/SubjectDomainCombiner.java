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

import java.security.CodeSource;
import java.security.DomainCombiner;
import java.security.Principal;
import java.security.ProtectionDomain;

/**
	A subclass of <code>java.security.DomainCombiner</code>.
	Used to dinamically assign to code the permissions carried
	by valid certificates owned by a subject.
	
	@author Michele Tomaiuolo - Universita` di Parma
	@version $Date$ $Revision$
*/
public class SubjectDomainCombiner implements DomainCombiner {
	JADESubject subject;
	
	/**
		Creates a new <code>SubjectDomainCombiner</code>.
	*/
	public SubjectDomainCombiner() {
	}
	
	/**
		Creates a new <code>SubjectDomainCombiner</code>.
		@param subj The subject which owns the certificates
			where permissions are stored.
	*/
	public SubjectDomainCombiner(JADESubject subject) {
		this.subject = subject;
	}
	
	/**
		Sets the subject.
		@param subj The subject which owns the certificates
			where permissions are stored.
	*/
	public void setSubject(JADESubject subject) {
		this.subject = subject;
	}
	
	/**
		Assigns to the executing code the permissions contained in valid
		certificates.
		@param currentDomains The domain that existed before calling the
			<code>doPrivileged</code> method.
		@param assignedDomains The domain created after calling the
			<code>doPrivileged</code> method.
		@return A domain which is granted the desired privileges. 
	*/
	public ProtectionDomain[] combine(ProtectionDomain[] currentDomains, ProtectionDomain[] assignedDomains) {
		return new ProtectionDomain[] {
			new ProtectionDomain(new CodeSource(null, null), null, null, new Principal[] { subject } ) };
	}
}
