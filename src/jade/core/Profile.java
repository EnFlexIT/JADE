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

package jade.core;

import jade.util.leap.List;
import jade.util.leap.ArrayList;
import jade.util.leap.Iterator;

/**
 * This class allows retrieving configuration-dependent classes.
 * 
 * @author  Federico Bergenti
 * @author  Giovanni Caire - TILAB
 * @version 1.0, 22/11/00
 */
public abstract class Profile {

  /**
     This constant is the name of the property whose value contains a
     boolean indicating if this is the Main Container or a peripheral
     container.
   */
  public static final String MAIN = "main";
  
  /**
     This constant is the name of the property whose value is a String
     indicating the protocol to use to connect to the Main Container.
   */
  public static final String MAIN_PROTO = "main-proto";

  /**
     This constant is the name of the property whose value is the name
     (or the IP address) of the network host where the JADE Main
     Container is running.
   */
  public static final String MAIN_HOST = "main-host";

  /**
     This constant is the name of the property whose value contains an
     integer representing the port number where the Main Container is
     listening for container registrations.
   */
  public static final String MAIN_PORT = "main-port";

  public static final String MAINAUTH_CLASS = "main-auth";
  public static final String AUTHORITY_CLASS = "authority";
  public static final String POLICY_FILE = "policy";
  public static final String PASSWD_FILE = "passwd";
  public static final String OWNERSHIP = "ownership";

  /**
     This constant is the name of the property whose value contains
     the unique platform ID of a JADE platform. Agent GUIDs in JADE
     are made by a platform-unique nickname, the '@' character and the
     platform ID.
   */
  public static final String PLATFORM_ID = "platform-id";
  
  /**
     This constant is the name of the property whose value contains the
     list of agents that have to be launched at bootstrap time
   */
  public static final String AGENTS = "agents";

  /**
   * This constant is the key of the property whose value contains the
   * list of MTPs that have to be launched at bootstrap time.
   * This list must be retrieved via the <code>getSpecifiers(MTPS)<code>
   * method.
   */
  public static final String MTPS = "mtps";

  /**
   * This constant is the key of the property whose value contains the
   * list of ACLCODECSs that have to be launched at bootstrap time.
   * This list must be retrieved via the <code>getSpecifiers(ACLCODECS)<code>
   * method.
   */
  public static final String ACLCODECS = "aclcodecs";
  
  /**
   * This constant is the key of the property whose value contains the
   * indication about the type of JVM. 
   */
  public static final String JVM = "jvm";
  public static final String J2SE = "j2se";
  public static final String PJAVA = "pjava";
  public static final String MIDP = "midp";
  
    /**
     */
    protected abstract Platform getPlatform() throws ProfileException;
    
    /**
     */
    protected abstract IMTPManager getIMTPManager() throws ProfileException;
    
    /**
     */
    protected abstract acc getAcc() throws ProfileException;

    /**
     */
    protected abstract MobilityManager getMobilityManager() throws ProfileException;

    /**
     */
    protected abstract ResourceManager getResourceManager() throws ProfileException;

    /**
     */
    protected abstract NotificationManager getNotificationManager() throws ProfileException;

    /**
     * Retrieve a String value from the configuration properties.
     * If no parameter corresponding to the specified key is found,
     * null is returned.
     * @param key The key identifying the parameter to be retrieved
     * among the configuration properties.
     */
    public abstract String getParameter(String key) throws ProfileException;

    /**
     * Retrieve a list of Specifiers from the configuration properties.
     * Agents, MTPs and other items are specified among the configuration
     * properties in this way.
     * If no list of Specifiers corresponding to the specified key is found,
     * an empty list is returned.
     * @param key The key identifying the list of Specifires to be retrieved
     * among the configuration properties.
     */
    public abstract List getSpecifiers(String key) throws ProfileException;

    /**
     * Assign the given value to the given property name.
     *
     * @param key is the property name
     * @param value is the property value
     *
     */
    public abstract void setParameter(String key, String value);

    /**
     * Assign the given value to the given property name.
     *
     * @param key is the property name
     * @param value is the property value
     *
     */
    public abstract void setSpecifiers(String key, List value);


}

