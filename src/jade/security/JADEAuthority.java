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

package jade.security;

import jade.core.Agent;
import jade.core.Profile;
import jade.core.MainContainer;
import jade.security.SDSIName;

/**
	The <code>Authority</code> interface represents an authority.
        It has methods for signing certificates and for
	verifying their validity.

        @author Giosue Vitaglione - Telecom Italia LAB
	@version $Date$ $Revision$
*/
public interface JADEAuthority {



    /**
     *  Initialize authority
     *  Reads configuration parameters from the Profile
     *  (the ref to myAgent it is used only to get the Profile)
     */
    public void init(String authorityName, Agent myAgent);
    public void init(String authorityName, Agent myAgent, Credentials cred);
    public void init(String authorityName, Profile myProfile);
    public void init(String authorityName, Profile myProfile, Credentials cred);


    /**
     *  @return the name of this Authority as known to the platform
     */
    public String getName();

    /**
     *  get the SDSI name identifying this Authority
     */
	public SDSIName getSDSIName();

	/**
		Checks the validity of a given certificate.
		The period of validity is tested, as well as the integrity
		(verified using the carried signature as proof).

		No verification is performed on the content (e.g. names, permission, delegations, etc.)

		@param cert The certificate to verify.
		@throws AuthenticationException if the certificate is not
			integer or is out of its validity period.
	*/
	public void verify(JADECertificate certificate) throws AuthException;

	/**
		Sign the given certificate.

		@param cert The certificate to sign.
		@throws AuthException
	*/
	public void sign(JADECertificate certificate) throws AuthException;

}