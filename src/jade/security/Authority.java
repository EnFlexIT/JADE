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

import jade.core.MainContainer;
import jade.core.Profile;


/**
	The <code>Authority</code> interface represents the authorities
	of the platform. It has methods for signing certificates and for
	verifying their validity.
	
	@author Michele Tomaiuolo - Universita` di Parma
	@version $Date$ $Revision$
*/
public interface Authority {
	
	public final String AMS_REGISTER        = "ams-register";
	public final String AMS_DEREGISTER      = "ams-deregister";
	public final String AMS_MODIFY          = "ams-modify";
	
	public final String AGENT_CREATE        = "agent-create";
	public final String AGENT_KILL          = "agent-kill";
	public final String AGENT_SUSPEND       = "agent-suspend";
	public final String AGENT_RESUME        = "agent-resume";
	public final String AGENT_TAKE          = "agent-take";
	public final String AGENT_SEND_TO       = "agent-send-to";
	public final String AGENT_SEND_AS       = "agent-send-as";
	public final String AGENT_RECEIVE_FROM  = "agent-receive-from";
	public final String AGENT_MOVE          = "agent-move";
	public final String AGENT_COPY          = "agent-copy";
	
	public final String CONTAINER_CREATE    = "container-create";
	public final String CONTAINER_KILL      = "container-kill";
	public final String CONTAINER_CREATE_IN = "container-create-in";
	public final String CONTAINER_KILL_IN   = "container-kill-in";
	public final String CONTAINER_MOVE_FROM = "container-move-from";
	public final String CONTAINER_MOVE_TO   = "container-move-to";
	public final String CONTAINER_COPY_FROM = "container-copy-from";
	public final String CONTAINER_COPY_TO   = "container-copy-to";

	public final String PLATFORM_CREATE     = "platform-create";
	public final String PLATFORM_KILL       = "platform-kill";

	public final String AUTHORITY_SIGN_IC   = "authority-sign-ic";
	public final String AUTHORITY_SIGN_DC   = "authority-sign-dc";

	public void init(Profile profile, MainContainer platform) throws AuthException;
	
	/**
		Sets the name of the authority.
		@param name The name of the authority.
	*/
	public void setName(String name) throws AuthException;
	
	/**
		Returns the name of the authority.
		@return the name of the authority.
	*/
	public String getName();
	
	public byte[] getPublicKey();
	
	/**
		Checks the validity of a given certificate.
		The period of validity is tested, as well as the integrity
		(verified using the carried signature as proof).
		@param cert The certificate to verify.
		@throws AuthenticationException if the certificate is not
			integer or is out of its validity period.
	*/
	public void verify(JADECertificate cert) throws AuthException;
	
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
	public void sign(JADECertificate certificate, IdentityCertificate identity, DelegationCertificate[] delegations) throws AuthException;

	public void authenticate(IdentityCertificate identity, DelegationCertificate delegation, byte[] password) throws AuthException;
	
	public Object doAs(PrivilegedExceptionAction action, IdentityCertificate identity, DelegationCertificate[] delegations) throws Exception;
	
	public void checkAction(String action, JADEPrincipal target, IdentityCertificate identity, DelegationCertificate[] delegations) throws AuthException;
	
	public AgentPrincipal createAgentPrincipal();

	public ContainerPrincipal createContainerPrincipal();
	
	public UserPrincipal createUserPrincipal();
	
	public IdentityCertificate createIdentityCertificate();

	public DelegationCertificate createDelegationCertificate();
	
}
