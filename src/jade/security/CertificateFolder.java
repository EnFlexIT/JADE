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

import jade.util.leap.List;
import jade.util.leap.ArrayList;


/**
	The <code>CertificateFolder</code> class is basically a list
	of delegation certificates, plus an identity certificate.
   
	@author Michele Tomaiuolo - Universita` di Parma
	@version $Date$ $Revision$
*/
public class CertificateFolder implements jade.util.leap.Serializable {
	
	IdentityCertificate identity = null;
	List delegations = new ArrayList();
	
	/**
		Creates an empty certificate folder.
	*/
	public CertificateFolder() {
	}
	
	/**
		Creates a certificate with a given identity.
		@param identity The identity certificate.
	*/
	public CertificateFolder(IdentityCertificate identity) {
		this.identity = identity;
	}
	
	/**
		Creates a certificate with an identity and a delegation.
		@param identity The identity certificate.
		@param delegation The delegation certificate.
	*/
	public CertificateFolder(IdentityCertificate identity, DelegationCertificate delegation) {
		this.identity = identity;
		delegations.add(delegation);
	}
	
	/**
		Sets the identity certificate.
		@param identity The identity certificate.
	*/
	public void setIdentityCertificate(IdentityCertificate identity) {
		this.identity = identity;
	}
	
	/**
		Returns the identity certificate.
		@return The identity certificate.
	*/
	public IdentityCertificate getIdentityCertificate() {
		return identity;
	}

	/**
		Returns all delegation certificates.
		@return The list of delegation certificates.
	*/
	public List getDelegationCertificates() {
		return delegations;
	}
	
	/**
		Adds a delegation certificate.
		@param delegation The new delegation certificate.
	*/
	public void addDelegationCertificate(DelegationCertificate delegation) {
		delegations.add(delegation);
	}
	
	/**
		Removes a delegation certificate.
		@param delegation The delegation certificate to remove.
	*/
	public void removeDelegationCertificate(DelegationCertificate delegation) {
		delegations.remove(delegation);
	}

	/**
		Returns an arrya containing all delegation certificates.
		@return An array of delegation certificates.
	*/
	public DelegationCertificate[] getDelegationsAsArray() {
		Object[] objs = delegations.toArray();
		DelegationCertificate[] result = new DelegationCertificate[objs.length];
		System.arraycopy(objs, 0, result, 0, objs.length);
		return result;
	}
}
