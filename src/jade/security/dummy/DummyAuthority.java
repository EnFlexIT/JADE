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

package jade.security.dummy;

import jade.security.*;

import jade.core.AID;
import jade.core.ContainerID;
import jade.core.MainContainer;
import jade.core.Profile;
import jade.security.JADECertificate;


public class DummyAuthority implements Authority {
	
	String name = null;
	
	public void init(Profile profile, MainContainer platform) {
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public byte[] getPublicKey() {
		return null;
	}
	
	public void verify(JADECertificate cert) throws AuthException {
	}
	
	public void sign(JADECertificate certificate, Credentials creds) throws AuthException {
	}
/*	
	public CertificateFolder authenticate(JADEPrincipal principal, byte[] password) throws AuthException {
		IdentityCertificate identity = createIdentityCertificate();
		identity.setSubject(principal);
		CertificateFolder certs = new CertificateFolder();
		certs.setIdentityCertificate(identity);
		return certs;
	}
*/	
	public Object doPrivileged(jade.security.PrivilegedExceptionAction action) throws Exception {
		return action.run();
	}

	public Object doAsPrivileged(jade.security.PrivilegedExceptionAction action, Credentials creds) throws Exception {
		return action.run();
	}
	
	public void checkAction(String action, JADEPrincipal target, Credentials creds) throws AuthException {
	}
	
	public JADEPrincipal createAgentPrincipal(AID aid, String ownership){
		return new DummyPrincipal(aid, ownership);
	}
	
	public JADEPrincipal createContainerPrincipal(ContainerID cid, String ownership) {
		return new DummyPrincipal(cid, ownership);
	}
	

	public DelegationCertificate createDelegationCertificate() {
		return new DummyCertificate();
	}

	public DelegationCertificate createDelegationCertificate(String encoded) throws CertificateException {
		return new DummyCertificate(encoded);
	}
	
	public DelegationCertificate createDelegationCertificate(byte[] encoded) throws CertificateException {
		return new DummyCertificate(encoded);
	}
	
	
}
