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

package jade.security.dummy;

import jade.security.*;

import jade.core.MainContainer;
import jade.core.Profile;


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
	
	public void sign(JADECertificate certificate, IdentityCertificate identity, DelegationCertificate[] delegations) throws AuthException {
	}
	
	public void authenticate(IdentityCertificate identity, DelegationCertificate delegation, byte[] passwd) throws AuthException {
	}
	
	public Object doAs(PrivilegedExceptionAction action, IdentityCertificate identity, DelegationCertificate[] delegations) throws Exception {
		return action.run();
	}
	
	public void checkAction(String action, JADEPrincipal target, IdentityCertificate identity, DelegationCertificate[] delegations) throws AuthException {
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
		return new DummyCertificate();
	}
	
	public DelegationCertificate createDelegationCertificate() {
		return new DummyCertificate();
	}
	
}
