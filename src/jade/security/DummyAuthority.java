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

import jade.security.leap.PrivilegedAction;

import jade.core.Profile;


public class DummyAuthority implements Authority {

	int serial = 1;
	String name = null;
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public void init(Profile profile) {
	}
		
	public void verify(IdentityCertificate cert) throws AuthException {
	}
		
	public void verify(DelegationCertificate cert) throws AuthException {
	}
		
	public void sign(IdentityCertificate certificate, IdentityCertificate identity, DelegationCertificate[] delegations) throws AuthException {
	}
	
	public void sign(DelegationCertificate certificate, IdentityCertificate identity, DelegationCertificate[] delegations) throws AuthException {
	}
	
	public void authenticateUser(IdentityCertificate identity, DelegationCertificate delegation, byte[] passwd) throws AuthException {
	}

	public Object doAs(PrivilegedAction action, IdentityCertificate identity, DelegationCertificate[] delegations) throws AuthException {
		return action.run();
	}
	
	public void checkPermission(String type, String name, String actions) throws AuthException {
	}
	
	public AgentPrincipal createAgentPrincipal(){
		return new DummyPrincipal();
	}

	public ContainerPrincipal createContainerPrincipal() {
		return new DummyPrincipal();
	}
	
	public UserPrincipal createUserPrincipal() {
		return new DummyPrincipal();
	}
	
	public IdentityCertificate createIdentityCertificate() {
		return null;
	}

	public DelegationCertificate createDelegationCertificate() {
		return null;
	}
	
}
