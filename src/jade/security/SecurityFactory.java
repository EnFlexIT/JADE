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

import jade.core.Profile;
import jade.util.Logger;


/**
    This is a factory class for security-related objects.
 
    @author Giosue Vitaglione - Telecom Italia LAB
	@version $Date$ $Revision$
*/
abstract public class SecurityFactory {

// NOTE: There can be more Profile (one for each container) into the same JVM
// instead, there is one unique SecurityFactory into the whole JVM
// therefore, more containrs into the same JVM must have the same security settings


  // this is the name of the parameter into the configuration file
  public static final String SECURITY_FACTORY_CLASS_KEY = "jade.security.SecurityFactory";
  public static final String SECURITY_FACTORY_CLASS_DEFAULT = "jade.security.dummy.DummySecurityFactory";

  public static final String PWD_HASH_ALGORITHM_KEY = "jade_security_impl_pwdHashAlgorithm";
  public static final String PWD_HASH_ALGORITHM_DEFAULT =  "DES";
  //public static final String PWD_DIALOG_CLASS_KEY = "jade.security.PwdDialog";
  //public static final String PWD_DIALOG_CLASS_DEFAULT = "jade.core.security.PwdDialogSwingImpl";

  //public static final String MAINAUTH_CLASS = "main-auth";
  //public static final String AUTHORITY_CLASS = "authority";
  public static final String POLICY_FILE_KEY = "java.security.policy";
  public static final String POLICY_FILE_DEFAULT = "policy";
  public static final String PASSWD_FILE_KEY = "jade.security.passwd";
  public static final String PASSWD_FILE_DEFAULT = "passwd";



  // the singleton instance of this object
  private static SecurityFactory singleton = null;

  // the first profile registered with the SecurityFactory
  private static Profile profile = null; // see NOTE above


  /*  Returns the SecurityFactory according the Profile p, 
   *  The first time this is called, a single instance is created as 
   *  a singleton. 
   *  @seealso#getSecurityFactory()
   */
  static public SecurityFactory getSecurityFactory(Profile p) {
    String className = p.getParameter(SECURITY_FACTORY_CLASS_KEY, 
                                      SECURITY_FACTORY_CLASS_DEFAULT);
    
    if (singleton != null) {

      try {
        singleton = (jade.security.SecurityFactory) Class.forName(className).
            newInstance();
      }
      catch (Exception e) {
        e.printStackTrace();
        Logger.println("\nError loading jade.security SecurityFactory:" +
                       className);
        Logger.println("Continuing with default: " +
                       SECURITY_FACTORY_CLASS_DEFAULT);
      }

      if (singleton == null) {
        try {
          singleton = (jade.security.SecurityFactory) Class.forName(
              SECURITY_FACTORY_CLASS_DEFAULT).
              newInstance();
        }
        catch (Exception e) {
          e.printStackTrace();
          Logger.println("\nError loading SecurityFactory:" +
                         SECURITY_FACTORY_CLASS_DEFAULT);
          Logger.println(" Exiting... ");
          System.exit( -1);
        }
      }
      profile = p;
    }

    return singleton;
  } // end getSecurityFactory


  /* Returns the SecurityFavtory if ever created.
   * If it has never created, returns 'null'.
   */
  static public SecurityFactory getSecurityFactory() {
    return singleton;
  } // end getSecurityFactory






// methods to get configurations parameters value

public static String getParameter(String key, String defaultVal) {
    String val = null;
    if (profile!=null) {
      val = profile.getParameter( key, defaultVal);
    }
    return val;
} // end getProperty()




/**
// password dialog (interface to enter user credentials) 
  public PwdDialog getPwdDialog() {

      //default is GUI swing password dialog
      String className = getParameter(PWD_DIALOG_CLASS_KEY, PWD_DIALOG_CLASS_DEFAULT);

      jade.security.PwdDialog dialog=null;
      try {
          dialog = (jade.security.PwdDialog) Class.forName(className).newInstance();
      }
      catch (Exception e) {
          //throw new ProfileException("Error loading jade.security password dialog:"+className);
          //e.printStackTrace();
          Logger.println("\nError: Could not load jade.security password dialog class: '"+className+"' ");
          Logger.println("\n Check parameter: '"+ PWD_DIALOG_CLASS_KEY +"' into your JADE config file." );
          Logger.println("\n Its default value is: "+PWD_DIALOG_CLASS_DEFAULT );
          System.exit(-1);
      }
      return dialog;
  }
*/



// abstract methods of the factory, follows: 

abstract public JADEAuthority newJADEAuthority();



} // end SecurityFactory