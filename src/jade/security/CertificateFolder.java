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


public class CertificateFolder {
	
	IdentityCertificate identity = null;
	List delegations = new ArrayList();
	
	public CertificateFolder() {
	}
	
	public CertificateFolder(IdentityCertificate identity) {
		this.identity = identity;
	}
	
	public CertificateFolder(IdentityCertificate identity, DelegationCertificate delegation) {
		this.identity = identity;
		delegations.add(delegation);
	}
	
	public void setIdentityCertificate(IdentityCertificate identity) {
		this.identity = identity;
	}
	
	public IdentityCertificate getIdentityCertificate() {
		return identity;
	}

	public List getDelegationCertificates() {
		return delegations;
	}
	
	public void addDelegationCertificate(DelegationCertificate delegation) {
		delegations.add(delegation);
	}
	
	public void removeDelegationCertificate(DelegationCertificate delegation) {
		delegations.remove(delegation);
	}

	public DelegationCertificate[] getDelegationsAsArray() {
		Object[] objs = delegations.toArray();
		DelegationCertificate[] result = new DelegationCertificate[objs.length];
		System.arraycopy(objs, 0, result, 0, objs.length);
		return result;
	}
}
