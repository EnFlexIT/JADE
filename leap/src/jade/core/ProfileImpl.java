/*
 * ***************************************************************
 * The LEAP libraries, when combined with certain JADE platform components,
 * provide a run-time environment for enabling FIPA agents to execute on
 * lightweight devices running Java. LEAP and JADE teams have jointly
 * designed the API for ease of integration and hence to take advantage
 * of these dual developments and extensions so that users only see
 * one development platform and a
 * single homogeneous set of APIs. Enabling deployment to a wide range of
 * devices whilst still having access to the full development
 * environment and functionalities that JADE provides.
 * Copyright (C) 2001 Telecom Italia LAB S.p.A.
 * 
 * GNU Lesser General Public License
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation,
 * version 2.1 of the License.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 * **************************************************************
 */

package jade.core;

import jade.util.leap.Properties;
import jade.util.leap.List;
import jade.util.leap.ArrayList;
import jade.util.leap.Iterator;

import java.util.Vector;
import java.io.IOException;

/**
 * This class allows retrieving configuration-dependent classes.
 *
 * @author  Federico Bergenti
 * @author  Giovanni Caire - TILAB
 * @author  Steffen Rusitschka - Siemens AG, CT IC 6
 * @version 1.0, 22/11/00
 *
 * @see jade.util.leap.Properties
 */
public class ProfileImpl extends Profile {
	//#MIDP_EXCLUDE_BEGIN
  // Keys to retrieve the implementation classes for configurable
  // functionalities among the bootstrap properties.
  private static final String RESOURCE = "resource";
	//#MIDP_EXCLUDE_END
  private static final String IMTP = "imtp";
  private static final String LOCAL_PORT = "local-port";
  private static final int DEFAULT_PORT = 1099;

  private Properties          props = null;
 
  private ServiceManager myServiceManager = null;
  private ServiceFinder myServiceFinder = null;
  private CommandProcessor myCommandProcessor = null; 
  private IMTPManager         myIMTPManager = null;
	//#MIDP_EXCLUDE_BEGIN
  private ResourceManager     myResourceManager = null;
  private MainContainerImpl myMain = null;
	//#MIDP_EXCLUDE_END

  /**
   * Create an empty Profile object
   */
  public ProfileImpl() throws ProfileException {
  	props = new Properties();
  	
  	init();
  }

  /**
   * Create a Profile object initialized with the settings specified
   * in a given property file
   */
  public ProfileImpl(String fileName) throws ProfileException {
  	props = new Properties();
  	if (fileName != null) {
		  try {
  	  	props.load(fileName);
    	}
    	catch (IOException ioe) {
      	throw new ProfileException("Can't load properties: "+ioe.getMessage());
    	}
  	}
  	
  	init();
  }
  
	//#MIDP_EXCLUDE_BEGIN
  /**
   * This constructor creates a default Profile for launching a 
   * platform (main = true).
   * @param host is the name of the host where the main-container should
   * be listen to. A null value means use the default (i.e. localhost)
   * @param port is the port number where the main-container should be
   * listen
   * for other containers. A negative value should be used for using
   * the default port number.
   * @param platformID is the synbolic name of the platform, if
   * different from default. A null value means use the default 
   * (i.e. localhost)
   **/
  public ProfileImpl(String host, int port, String platformID) throws ProfileException {
   	props = new Properties();
   	
   	props.setProperty(MAIN, "true");
   	
   	if(host != null) {
    	props.setProperty(MAIN_HOST, host);
   	}
   	if(port > 0) {
    	props.setProperty(MAIN_PORT, String.valueOf(port));
   	}
   	if(platformID != null) { 
     		props.setProperty(PLATFORM_ID, platformID);
   	}
   	init();
  }

  /**
   * Create a Profile object initialized with
   * the settings specified in the <code>Properties</code> object
   * passed as parameter.
   **/
  public ProfileImpl(Properties props) throws ProfileException {
   	this.props = (props != null ? props : new Properties());
   	
   	init();
  }
  //#MIDP_EXCLUDE_END

  private void init() throws ProfileException {

  	// Set jvm parameter if not set
  	if (props.getProperty(JVM) == null) {
  		//#PJAVA_EXCLUDE_BEGIN
  		props.setProperty(JVM, J2SE);
  		//#PJAVA_EXCLUDE_END
  		/*#PJAVA_INCLUDE_BEGIN
  		props.setProperty(JVM, PJAVA);
  		#PJAVA_INCLUDE_END*/
  		/*#MIDP_INCLUDE_BEGIN
  		props.setProperty(JVM, MIDP);
  		props.setProperty(MAIN, "false");
  		#MIDP_INCLUDE_END*/
  	}

	// Set reasonable defaults for MAIN_HOST, MAIN_PORT and PLATFORM_ID properties

	String h = props.getProperty(MAIN_HOST);
	if(h == null) {
	    try {
		//#MIDP_EXCLUDE_BEGIN
		h = java.net.InetAddress.getLocalHost().getHostName();
		//#MIDP_EXCLUDE_END
		/*#MIDP_INCLUDE_BEGIN
		h = "localhost";
		#MIDP_INCLUDE_END*/
	    }
	    catch(Exception e) {
		throw new ProfileException("Could not retrieve the default local host name");
	    }
	    props.setProperty(MAIN_HOST, h);
	}

	String p = props.getProperty(MAIN_PORT);
	if(p == null) {
	    String localPort = props.getProperty(LOCAL_PORT);
	    if(localPort != null) {
		p = localPort;
	    }
	    else {
		p = Integer.toString(DEFAULT_PORT);
	    }
	    props.setProperty(MAIN_PORT, p);
	}

	String id = props.getProperty(PLATFORM_ID);
	if(id == null) {
	    props.setProperty(PLATFORM_ID, h + ":" + p + "/JADE");
	}

	// Set a default for the service list, if not set
	String services = props.getProperty(SERVICES);
	if(services == null) {
	    props.setProperty(SERVICES, DEFAULT_SERVICES);
	}


  	//#MIDP_EXCLUDE_BEGIN
    // Set agents as a list to handle the "gui" option
    List   l = getSpecifiers(AGENTS);
    String isGui = props.getProperty("gui");

    if (isGui != null && CaseInsensitiveString.equalsIgnoreCase(isGui, "true")) {
      Specifier s = new Specifier();

      s.setName("rma");
      s.setClassName("jade.tools.rma.rma");
      l.add(0, s);
    }

    props.put(AGENTS, l);
    
    //#PJAVA_EXCLUDE_BEGIN
    // Take proper adjustments in case this is the main
    if (!("false".equalsIgnoreCase(props.getProperty(MAIN)))) {
	    // If no MTP is explicitly specified and the nomtp property is not set
	    // --> add the default IIOP MTP
      if ((props.getProperty(MTPS) == null) && (props.getProperty("nomtp") == null)) {
    		props.setProperty(MTPS, "jade.mtp.iiop.MessageTransportProtocol");
      }
    }
    //#PJAVA_EXCLUDE_END
  	//#MIDP_EXCLUDE_END

  }


    protected ServiceManager getServiceManager() throws ProfileException {
      if(myServiceManager == null) {
	  //#MIDP_EXCLUDE_BEGIN
	  createServiceManager();
	  //#MIDP_EXCLUDE_END

	  /*#MIDP_INCLUDE_BEGIN
	  try {
	      myServiceManager = myIMTPManager.createServiceManagerProxy(myCommandProcessor);
	  }
	  catch(IMTPException imtpe) {
	      ProfileException pe = new ProfileException("Can't get a proxy for the platform Service Manager");
	      throw pe;
	  }
	  #MIDP_INCLUDE_END*/
      }

      return myServiceManager;
    }

    protected ServiceFinder getServiceFinder() throws ProfileException {
      if(myServiceFinder == null) {
	  //#MIDP_EXCLUDE_BEGIN
	  createServiceFinder();
	  //#MIDP_EXCLUDE_END

	  /*#MIDP_INCLUDE_BEGIN
	  try {
	      myServiceFinder = myIMTPManager.createServiceFinderProxy();
	  }
	  catch(IMTPException imtpe) {
	      ProfileException pe = new ProfileException("Can't get a proxy for the platform Service Finder");
	      throw pe;
	  }
	  #MIDP_INCLUDE_END*/
      }

      return myServiceFinder;
    }

    protected CommandProcessor getCommandProcessor() throws ProfileException {
      if(myCommandProcessor == null) {
	  createCommandProcessor();
      }

      return myCommandProcessor;
    }


    //#MIDP_EXCLUDE_BEGIN
    protected MainContainerImpl getMain() throws ProfileException {
	return myMain;
    }
    //#MIDP_EXCLUDE_END


  /**
   */
  protected IMTPManager getIMTPManager() throws ProfileException {
    if (myIMTPManager == null) {
  		//#MIDP_EXCLUDE_BEGIN
      createIMTPManager();
  		//#MIDP_EXCLUDE_END
	  	/*#MIDP_INCLUDE_BEGIN
	    String className = getParameter(IMTP, "jade.imtp.leap.LEAPIMTPManager");
	    try {
	      myIMTPManager = (IMTPManager) Class.forName(className).newInstance();
	    } 
	    catch (Exception e) {
	      throw new ProfileException("Error loading IMTPManager class"+className);
	    }
	  	#MIDP_INCLUDE_END*/
    }

    return myIMTPManager;
  }

  /**
   */
  public ResourceManager getResourceManager() throws ProfileException {
  	//#MIDP_EXCLUDE_BEGIN
    if (myResourceManager == null) {
      createResourceManager();
    }

    return myResourceManager;
  	//#MIDP_EXCLUDE_END
  	/*#MIDP_INCLUDE_BEGIN
    return new LightResourceManager();
  	#MIDP_INCLUDE_END*/
  }

    //#MIDP_EXCLUDE_BEGIN
    private void createServiceManager() throws ProfileException {
	try {
	    // Make sure the IMTP manager is initialized
	    myIMTPManager = getIMTPManager();

	    // Make sure the Command Processor is initialized
	    myCommandProcessor = getCommandProcessor();

	    String isMain = props.getProperty(MAIN);
	    if(isMain == null || CaseInsensitiveString.equalsIgnoreCase(isMain, "true")) {
		// This is a main container: create a real Service Manager and export it
		myMain = new MainContainerImpl(this);
		myServiceManager = new ServiceManagerImpl(this, myMain);
		myIMTPManager.exportServiceManager((ServiceManagerImpl)myServiceManager);
	    }
	    else {
		// This is a peripheral container: create a Service Manager Proxy
		myServiceManager = myIMTPManager.createServiceManagerProxy(myCommandProcessor);
	    }
	}
	catch(IMTPException imtpe) {
	    ProfileException pe = new ProfileException("Can't get a proxy for the platform Service Manager");
	    throw pe;
	}
    }
    //#MIDP_EXCLUDE_END

    //#MIDP_EXCLUDE_BEGIN
    private void createServiceFinder() throws ProfileException {
	try {
	    // Make sure the IMTP manager is initialized
	    myIMTPManager = getIMTPManager();

	    String isMain = props.getProperty(MAIN);
	    if(isMain == null || CaseInsensitiveString.equalsIgnoreCase(isMain, "true")) {
		// This is a main container: use the real
		// implementation of the Service Manager as the
		// service finder.
		myServiceFinder = (ServiceFinder)myServiceManager;
	    }
	    else {
		// This is a peripheral container: create a Service Finder Proxy
		myServiceFinder = myIMTPManager.createServiceFinderProxy();
	    }
	}
	catch(IMTPException imtpe) {
	    ProfileException pe = new ProfileException("Can't get a proxy for the platform Service Manager");
	    throw pe;
	}
    }
    //#MIDP_EXCLUDE_END

    private void createCommandProcessor() throws ProfileException {
	try {
	    myCommandProcessor = new CommandProcessor();
	}
	catch(Exception e) {
	    ProfileException pe = new ProfileException("Exception creating the Command Processor");
	    throw pe;
	}
    }


  //#MIDP_EXCLUDE_BEGIN
  private void createIMTPManager() throws ProfileException {
    String className = getParameter(IMTP, "jade.imtp.leap.LEAPIMTPManager");

    try {
      myIMTPManager = (IMTPManager) Class.forName(className).newInstance();
    }
    catch (Exception e) {
      throw new ProfileException("Error loading IMTPManager class"+className);
    }
  }
  //#MIDP_EXCLUDE_END

  //#MIDP_EXCLUDE_BEGIN
  private void createResourceManager() throws ProfileException {
  	//#PJAVA_EXCLUDE_BEGIN
    String className = getParameter(RESOURCE, "jade.core.FullResourceManager");
  	//#PJAVA_EXCLUDE_END
  	/*#PJAVA_INCLUDE_BEGIN
    String className = getParameter(RESOURCE, "jade.core.LightResourceManager");
  	#PJAVA_INCLUDE_END*/

    try {
      myResourceManager = (ResourceManager) Class.forName(className).newInstance();
    }
    catch (Exception e) {
      throw new ProfileException("Error loading ResourceManager class"+className);
    }
  }
  //#MIDP_EXCLUDE_END


  /**
   * Retrieve a String value from the configuration properties.
   * If no parameter corresponding to the specified key is found,
   * <code>aDefault</code> is returned.
   * @param key The key identifying the parameter to be retrieved
   * among the configuration properties.
   * @param aDefault The value that is returned if the specified 
   * key is not found
   */
  public String getParameter(String key, String aDefault) {
    String v = props.getProperty(key);
    return (v != null ? v.trim() : aDefault);
  }

  /**
   * Retrieve a list of Specifiers from the configuration properties.
   * Agents, MTPs and other items are specified among the configuration
   * properties in this way.
   * If no list of Specifiers corresponding to the specified key is found,
   * an empty list is returned.
   * @param key The key identifying the list of Specifires to be retrieved
   * among the configuration properties.
   */
  public List getSpecifiers(String key) throws ProfileException {
    //#MIDP_EXCLUDE_BEGIN
  	// Check if the list of specs is already in the properties as a list
    List l = null;
    try {
      l = (List) props.get(key);
    }
    catch (ClassCastException cce) {
    }
    if (l != null) {
      return l;
    }
    //#MIDP_EXCLUDE_END

    // The list should be present as a string --> parse it
    String    specsLine = getParameter(key, null);
    try {
    	Vector v = Specifier.parseSpecifierList(specsLine);
			// convert the vector into an arraylist (notice that using the vector allows to avoid class loading of ArrayList
			List l1 = new ArrayList(v.size());
			for (int i=0; i<v.size(); i++)
					l1.add(v.elementAt(i));
			return l1;
    }
    catch (Exception e) {
    	throw new ProfileException("Error parsing specifier list "+specsLine+". "+e.getMessage());
    }
  }
  
  /**
   * Assign the given value to the given property name.
   *
   * @param key is the property name
   * @param value is the property value
   *
   */
  public void setParameter(String key, String value) {
    props.setProperty(key, value);
  }

  /**
   * Assign the given value to the given property name.
   *
   * @param key is the property name
   * @param value is the property value
   *
   */
  public void setSpecifiers(String key, List value) {
    //#MIDP_EXCLUDE_BEGIN
    props.put(key, value);
    //#MIDP_EXCLUDE_END
  }

}

