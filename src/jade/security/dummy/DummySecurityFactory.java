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

import jade.core.Agent;
import jade.core.ServiceHelper;

import jade.core.ContainerID;
import jade.core.MainContainer;
import jade.core.Profile;

import jade.security.AuthException;
import jade.security.JADECertificate;
import jade.security.SDSIName;

import jade.security.Authority;
import jade.security.dummy.DummyAuthority;
import jade.security.SecurityFactory;
import jade.security.JADEAuthority;
import jade.security.JADEPrincipal;
import jade.security.Credentials;
import jade.security.PrivilegedExceptionAction;
//#ALL_EXCLUDE_BEGIN
import java.security.Permission;
import jade.security.JADEAccessController;
//#ALL_EXCLUDE_END

/**

    @author Giosue Vitaglione - Telecom Italia LAB
	@version $Date$ $Revision$
*/
public class DummySecurityFactory extends SecurityFactory {

  public static Authority newAuthority(){
    Authority a = null;
    try {
		a = (Authority) Class.forName("jade.security.dummy.DummyAuthority").newInstance();
    }
    catch(ClassNotFoundException e) { e.printStackTrace(); }
    catch(InstantiationException e) { e.printStackTrace(); }
    catch(IllegalAccessException e) { e.printStackTrace(); }

    return a;
  }

  public JADEAuthority newJADEAuthority() {
    return null; //new DummyJADEAutority();
  }

  public JADEPrincipal newJADEPrincipal(SDSIName sdsiname) {
    return new DummyPrincipal();
  }

  public JADEPrincipal newJADEPrincipal(String name, SDSIName sdsiname1) {
    return new DummyPrincipal(name);
  }


//#ALL_EXCLUDE_BEGIN
  public JADEAccessController newJADEAccessController(
      String name, JADEAuthority authority, String policy) {
    return new JADEAccessController() {
      public void checkAction( JADEPrincipal requester, Permission p,
                               JADEPrincipal target, Credentials credentials) {
      }

      public Object doAsPrivileged( PrivilegedExceptionAction action,
                                    Credentials certs) {
        return null;
      }
    };
  }
//#ALL_EXCLUDE_END


} // end SecurityFactory