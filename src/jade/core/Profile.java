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
  public static final String MAIN_PROTO = "proto";

  /**
     This constant is the name of the property whose value is the name
     (or the IP address) of the network host where the JADE Main
     Container is running.
   */
  public static final String MAIN_HOST = "host";

  /**
     This constant is the name of the property whose value contains an
     integer representing the port number where the Main Container is
     listening for container registrations.
   */
  public static final String MAIN_PORT = "port";

  /**
     This constant is the name of the property whise value contains
     the host name the container must bind on. The host name must
     refer to the local machine, and is generally needed only when
     multiple network interfaces are present or a non-default name is
     desired.
  */
  public static final String LOCAL_HOST = "local-host";

  /**
     This constant is the name of the TCP port the container node must
     listen to for incoming IMTP messages.
  */
  public static final String LOCAL_PORT = "local-port";

  /**
     This constant is the name of the property whose Boolean value
     tells whether a local Service Manager is exported by this
     container (only when using JADE support for fault-tolerant
     platform configurations).
  */
  public static final String LOCAL_SERVICE_MANAGER = "backupmain";

  /**
     This constant is the name of the property whose value contains
     the name of the application-specific class that will be installed
     on the local container as a filter for undelivered ACL
     messages(only when using JADE Persistent-Delivery kernel
     service).
  */
  public static final String PERSISTENT_DELIVERY_FILTER = "persistent-delivery-filter";

  /**
     This constant is the name of the property whose value contains an
     integer representing how often (in milliseconds) JADE will try to
     send again previously undelivered ACL messages which have been
     buffered (only when using JADE Persistent-Delivery kernel
     service).
  */
  public static final String PERSISTENT_DELIVERY_SENDFAILUREPERIOD = "persistent-delivery-sendfailureperiod";

  /**
     This constant is the name of the property whose value contains
     the storage method used to persist undelivered ACL messages (only
     when using JADE Persistent-Delivery kernel service).
     The supported values for this profile parameter are:
     <ul>
     <li><b>file</b> - A directory tree on the local filesystem is used.</li>
     </ul>
     If this property is not specified when starting the
     Persistent-Delivery kernel service, undelivered ACL messages ae
     kept in memory and not persisted at all.
  */
  public static final String PERSISTENT_DELIVERY_STORAGEMETHOD = "persistent-delivery-storagemethod";

  /**
     This constant is the name of the property whose value contains
     the root of the directory tree that is used to persist
     undelivered ACL messages (only when using JADE
     Persistent-Delivery kernel service with the <i>file</i> storage
     method).
  */
  public static final String PERSISTENT_DELIVERY_BASEDIR = "persistent-delivery-basedir";

  /**
   * This constant is the name of the property which, when it is set,
   * tells the system to use UDP based failure monitoring 
   * instead of the default monitoring.
   */
  public static final String UDP_MONITORING = "udpmonitoring";
  
  /**
   * This constant is the name of the property whose value contains an
   * integer representing the time interval (in milliseconds) in which a simple
   * container sends UDP ping messages to the Main Container 
   * (This property is only used in combination with
   * <code>UDP_MONITORING</code> for a simple container)
   */
  public static final String UDP_MONITORING_PING_DELAY = "udpmonitoring_ping_delay";
 
  /**
   * This constant is the name of the property whose value contains an
   * integer representing the port number where the Main Container is
   * listening for UDP pings. (This property is only used in combination with
   * <code>UDP_MONITORING</code> for a main container)
   */
  public static final String UDP_MONITORING_PORT = "udpmonitoring_port"; 
  
  /**
   * This constant is the name of the property whose value contains an
   * integer representing the maximum time (in milliseconds) the main container 
   * waits for a ping message.
   * (This property is only used in combination with
   * <code>UDP_MONITORING</code> for a main container)
   */
  public static final String UDP_MONITORING_PING_DELAY_LIMIT = "udpmonitoring_ping_delay_limit";

  /**
   * This constant is the name of the property whose value contains an
   * integer representing the maximum time a node can stay unreachable after it gets removed
   * from the platform.
   * (This property is only used in combination with
   * <code>UDP_MONITORING</code> for a main container)
   */
  public static final String UDP_MONITORING_UNREACHABLE_LIMIT = "udpmonitoring_unreachable_limit";
  
  //#APIDOC_EXCLUDE_BEGIN
  public static final String MASTER_NODE_NAME = "master-node-name";
  public static final String BE_BASE_NAME = "be-base-name";
  public static final String BE_REPLICA_ZERO_ADDRESS = "be-replica-zero-address";
  public static final String BE_REPLICA_INDEX = "be-replica-index";
  public static final String BE_MEDIATOR_ID = "be-mediator-id";

  //public static final String MAINAUTH_CLASS = "main-auth";
  //public static final String AUTHORITY_CLASS = "authority";
  //public static final String POLICY_FILE = "java.security.policy";
  //public static final String PASSWD_FILE = "jade.security.passwd";
  public static final String OWNER = "owner";

  // On J2SE and pJava, install mobility and notification services by default
  //#J2ME_EXCLUDE_BEGIN
  public static final String DEFAULT_SERVICES = "jade.core.mobility.AgentMobilityService;jade.core.event.NotificationService";
  public static final String DEFAULT_SERVICES_NOMOBILITY = "jade.core.event.NotificationService";
  //#J2ME_EXCLUDE_END

  // On PJAVA the Notification service is not supported
  /*#PJAVA_INCLUDE_BEGIN
  public static final String DEFAULT_SERVICES = "jade.core.mobility.AgentMobilityService";
  public static final String DEFAULT_SERVICES_NOMOBILITY = "";
  #PJAVA_INCLUDE_END*/

  // On MIDP, no additional services are installed by default
  /*#MIDP_INCLUDE_BEGIN
  public static final String DEFAULT_SERVICES = "";
  #MIDP_INCLUDE_END*/

  //#APIDOC_EXCLUDE_END


  /**
     This constant is the name of the property whose value contains
     the unique platform ID of a JADE platform. Agent GUIDs in JADE
     are made by a platform-unique nickname, the '@' character and the
     platform ID.
   */
  public static final String PLATFORM_ID = "platform-id";

  /**
     This constant is the name of the property whose value contains
     the user authentication type to be used to login to the JADE platform.
   */
  public static final String USERAUTH_KEY = "userauth-key";

  /**
     This constant is the name of the property whose value contains the
     list of agents that have to be launched at bootstrap time
   */
  public static final String AGENTS = "agents";

  /**
     This constants is the name of the property whose value contains
     the list of kernel-level services that have to be launched at
     bootstrap time
  */
  public static final String SERVICES = "services";

  /**
     This constant is the name of the property whose value contains the
     list of addresses through which the platform <i>Service
     Manager</i> can be reached.
  */
  public static final String REMOTE_SERVICE_MANAGER_ADDRESSES = "smaddrs";

  /**
   * This constant is the key of the property whose value contains the
   * list of MTPs that have to be launched at bootstrap time.
   * This list must be retrieved via the <code>getSpecifiers(MTPS)<code>
   * method.
   */
  public static final String MTPS = "mtps";
    /**
     * This constant is the key of the property whose value
     * identifies the IMTP Manager to be created by ProfileImpl
     **/
    public static final String IMTP = "imtp";

    /**
     * This constant is the key of the property whose value contains
     * the desired name of the container. If this container name exists
     * already, then a default name is assigned by the platform.
     * The name of the main-container is always assigned by the platform
     * and cannot be changed.
     **/
    public static final String CONTAINER_NAME = "container-name";

  /**
   * This constant is the key of the property whose value contains the
   * list of ACLCODECSs that have to be launched at bootstrap time.
   * This list must be retrieved via the <code>getSpecifiers(ACLCODECS)<code>
   * method.
   */
  public static final String ACLCODECS = "aclcodecs";

  /**
     This constant is the key of the property whose value (true or false)
     indicates whether or not this platform accepts foreign agents i.e.
     agents whose names are not of the form <local-name>@<platform-name>.
   */
	public static final String ACCEPT_FOREIGN_AGENTS = "accept-foreign-agents";
	
  //#APIDOC_EXCLUDE_BEGIN

  /**
   * This constant is the key of the property whose value contains the
   * indication about the type of JVM. 
   */
  public static final String JVM = "jvm";
  public static final String J2SE = "j2se";
  public static final String PJAVA = "pjava";
  public static final String MIDP = "midp";

  //#APIDOC_EXCLUDE_END

    /**
     * This constant is the key of the property whose value contains
     * the name of the directory where all the files generated by JADE
     * should be put. The defaul value is the current directory.
     **/
    public static final String FILE_DIR = "file-dir";


    //#APIDOC_EXCLUDE_BEGIN

    /**
       Obtain a reference to the platform <i>Service Manager</i>, with
       which kernel-level services can be added and removed.
       @return A <code>ServiceManager</code> object, representing the
       platform service manager.
     */
    protected abstract ServiceManager getServiceManager() throws ProfileException;

    /**
       Obtain a reference to the platform <i>Service Finder</i>, with
       which kernel-level services can be looked up.
       @return A <code>ServiceFinder</code> object, representing the
       platform service manager.
    */
    protected abstract ServiceFinder getServiceFinder() throws ProfileException;

    /**
       Obtain a reference to the container <i>Command Processor</i>,
       which manages kernel-level commands dispatching them to the
       proper platform services.
       @return A <code>ServiceManager</code> object, representing the
       platform service manager.
     */
    protected abstract CommandProcessor getCommandProcessor() throws ProfileException;

    //#MIDP_EXCLUDE_BEGIN
    protected abstract MainContainerImpl getMain() throws ProfileException;
    //#MIDP_EXCLUDE_END
    
    /**
     */
    protected abstract IMTPManager getIMTPManager() throws ProfileException;
    
    /**
     */
    public abstract ResourceManager getResourceManager() throws ProfileException;

    
    //#APIDOC_EXCLUDE_END


    /**
     * Retrieve a String value from the configuration properties.
     * If no parameter corresponding to the specified key is found,
     * return the provided default.
     * @param key The key identifying the parameter to be retrieved
     * among the configuration properties.
     * @param aDefault The value to return when there is no property
     * set for the given key.
     */
    public abstract String getParameter(String key, String aDefault);

    /**
     * Retrieve a boolean value for a configuration property.  If no
     * corresponding property is found or if its string value cannot
     * be converted to a boolean one, a default value is returned.
     * @param key The key identifying the parameter to be retrieved
     * among the configuration properties.
     * @param aDefault The value to return when there is no property
     * set for the given key, or its value cannot be converted to a
     * boolean value.
     */
    public abstract boolean getBooleanProperty(String key, boolean aDefault);

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


    public static String getDefaultNetworkName() {
		  String host = "localhost";
		  //#MIDP_EXCLUDE_BEGIN
			try {
		  	host = java.net.InetAddress.getLocalHost().getHostAddress(); 
		
		    if ("127.0.0.1".equals(host)) {
		      // Try with the name
		      host = java.net.InetAddress.getLocalHost().getHostName();
		    }
			}
			catch(Exception e) {
			}
		  //#MIDP_EXCLUDE_END
			return host;
    }

}

