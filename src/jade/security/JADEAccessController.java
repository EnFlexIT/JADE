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

import jade.core.AID;
import jade.core.ContainerID;
import jade.core.MainContainer;
import jade.core.Profile;
import jade.security.CertificateFolder;
import java.security.Permission;




/**
*	The <code>JADEAccessController</code> interface represents the
*    way of building a Policy Enforcement Point (PEP) for a component 
*    (i.e. a container or an agent) that provides a service.
*
*    JADEAccessController has methods for checking for the authorization for performing
*    an action according to a given policy.
*
*
*    @author Giosue Vitaglione - Telecom Italia LAB
*    @version $Date$ $Revision$
*/
public interface JADEAccessController {

 /**
  *   Check whether the <code>requester</code> can perform the <code>action</code> 
  *   on the given <code>target</code>.
  *   The requestor's <code>credentials</code> can be provided (or 'null' can be passed).
  *
  *
  * @param requester
  * @param action
  * @param target
  * @param credentials
  * @throws AuthException
  */
  public void checkAction( JADEPrincipal requester, 
                           Permission p, 
                           JADEPrincipal target, 
                           Credentials credentials) 
         throws AuthException;

  /**
   *
   * @param action
   * @return
   * @throws java.lang.Exception
   */
  public Object doAsPrivileged(PrivilegedExceptionAction action, Credentials certs) throws Exception;




}
