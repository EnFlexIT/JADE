/**
 * ***************************************************************
 * JADE - Java Agent DEvelopment Framework is a framework to develop
 * multi-agent systems in compliance with the FIPA specifications.
 * Copyright (C) 2000 CSELT S.p.A.
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
import jade.util.BasicProperties;
import java.io.IOException;
import java.net.*;
import java.util.Hashtable;

/**
 * This class allows the JADE core to retrieve configuration-dependent classes
 * and boot parameters.
 * <p>
 * Take care of using different instances of this class when launching
 * different containers/main-containers on the same JVM otherwise
 * they would conflict!
 * 
 * @author  Federico Bergenti
 * @author  Giovanni Caire - TILAB
 * @author  Giovanni Rimassa - Universita' di Parma
 * @version 1.0, 22/11/00
 * 
 */
public class ProfileImpl extends Profile {

  // HP Patch begin ----------------------------------------------------------------------------------
  private BasicProperties  props = null;
  // private Properties props = null;

  /**
   * Default communication port number.
   */
  public static final int DEFAULT_PORT = 1099;
  // HP Patch end ------------------------------------------------------------------------------------

    /**
       This constant is the key of the property whose value is the class name of
       the mobility manager.
     **/
  public static final String MOBILITYMGRCLASSNAME = "mobility";


  private ServiceManager myServiceManager = null;
  private ServiceFinder myServiceFinder = null;
  private CommandProcessor myCommandProcessor = null;
  private MainContainerImpl myMain = null;
  private IMTPManager     myIMTPManager = null;
  private ResourceManager myResourceManager = null;

  public ProfileImpl(BasicProperties aProp) {
    props = aProp;
    try {
      // Set default values
      String host = InetAddress.getLocalHost().getHostName();
      props.setPropertyIfNot(MAIN, "true");
      props.setPropertyIfNot(MAIN_PROTO, "rmi");
      props.setPropertyIfNot(MAIN_HOST, host);
      props.setPropertyIfNot(MAIN_PORT, Integer.toString(DEFAULT_PORT));
      updatePlatformID();
      if (!props.getBooleanProperty("nomtp", false)) {
        Specifier s = new Specifier();
        s.setClassName("jade.mtp.iiop.MessageTransportProtocol"); 
        List l = new ArrayList(1);
        l.add(s);
        props.put(MTPS, l);
      }
    } 
    catch (UnknownHostException uhe) {
      uhe.printStackTrace();
    } 
    catch (IOException ioe) {
      ioe.printStackTrace();
    } 
  }

  /**
   * Creates a Profile implementation with the default configuration
   * for launching a main-container on the localhost, 
   * RMI internal Message Transport Protocol, port number 1099,
   * iiop MTP.
   */
  public ProfileImpl() {
    this(new BasicProperties());
  }

    /**
     * This constructor creates a default Profile for launching a platform.
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
     public ProfileImpl(String host, int port, String platformID) {
     	this(); // Call default constructor
     	if(host != null)
       		props.setProperty(MAIN_HOST, host);
     	if(port > 0)
       		props.setIntProperty(MAIN_PORT, port);
     	if(platformID != null)
       		props.setProperty(PLATFORM_ID, platformID);
     	else 
	    updatePlatformID();
 	}

    public void updatePlatformID() {
	String h = props.getProperty(MAIN_HOST);
	String p = props.getProperty(MAIN_PORT);
	props.setProperty(PLATFORM_ID, h + ":" + p + "/JADE");
    }

    /**
     * Copy a collection of properties into this profile.
     * @param source The collection to be copied.
     */
    void copyProperties(BasicProperties source) {
        props.copyProperties(source);
    }
  
    /**
     * Return the underlying properties collection.
     * @return BasicProperties The properties collection.
     */
    public BasicProperties getProperties() {
        return props;
    }      

    /** HP.
    private MainContainerImpl theMainContainer = null;

    public void addPlatformListener(AgentManager.Listener aListener) throws NotFoundException {
        if (theMainContainer == null) {
            throw new NotFoundException("Unable to add listener, main container not set");
        }
        theMainContainer.addListener(aListener);
    }

    public void removePlatformListener(AgentManager.Listener aListener) throws NotFoundException {
        if (theMainContainer == null) {
            throw new NotFoundException("Unable to remove listener, main container not set");
        }
        theMainContainer.removeListener(aListener);
    }
    **/


  /**
   * Assign the given value to the given property name.
   *
   * @param key is the property name
   * @param value is the property value
   */
    public void setParameter(String key, String value) {
	props.put(key, value);
    }

  /**
   * Assign the given property value to the given property name
   *
   * @param key is the property name
   * @param value is the property value
   */
  public void setSpecifiers(String key, List value) {
    props.put(key, value);
  } 


  /**
     Access the platform service manager.
     @return The platform service manager, either the real
     implementation or a remote proxy object.
     @throws ProfileException If some needed information is wrong or
     missing from the profile.
  */
  protected ServiceManager getServiceManager() throws ProfileException {
      if(myServiceManager == null) {
	  createServiceManager();
      }

      return myServiceManager;
  }

  /**
     Access the platform service finder.
     @return The platform service finder, either the real
     implementation or a remote proxy object.
     @throws ProfileException If some needed information is wrong or
     missing from the profile.
  */
  protected ServiceFinder getServiceFinder() throws ProfileException {
      if(myServiceFinder == null) {
	  createServiceFinder();
      }

      return myServiceFinder;
  }

  protected CommandProcessor getCommandProcessor() throws ProfileException {
      if(myCommandProcessor == null) {
	  createCommandProcessor();
      }

      return myCommandProcessor;
  }


  /**
   */
  protected MainContainerImpl getMain() throws ProfileException {
      return myMain;
  }

  /**
   */
  protected IMTPManager getIMTPManager() throws ProfileException {
    if (myIMTPManager == null) {
      createIMTPManager();
    } 

    return myIMTPManager;
  }

  /**
   */
  public ResourceManager getResourceManager() throws ProfileException {
    if (myResourceManager == null) {
      createResourceManager();
    } 

    return myResourceManager;
  }



  /**
   */
  public jade.security.PwdDialog getPwdDialog() throws ProfileException {
          
    //default is GUI swing password dialog
    String className = getParameter(PWD_DIALOG_CLASS, "jade.security.impl.PwdDialogSwingImpl");

	jade.security.PwdDialog dialog=null;
    try {
      dialog = (jade.security.PwdDialog) Class.forName(className).newInstance();
    } 
    catch (Exception e) {
      //throw new ProfileException("Error loading jade.security password dialog:"+className);
      //e.printStackTrace();
      System.out.println("\nError: Could not load jade.security password dialog class: '"+PWD_DIALOG_CLASS+"' ");
      System.out.println("\n Check parameter: '"+Profile.PWD_DIALOG_CLASS+"' in your JADE config file." );
      System.out.println("\n Its default value is: jade.security.impl.PwdDialogSwingImpl" );
      System.exit(-1);
    } 
    return dialog;
  }



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
	    pe.initCause(imtpe);
	    throw pe;
	}
    }

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
	    pe.initCause(imtpe);
	    throw pe;
	}
    }


    private void createCommandProcessor() throws ProfileException {
	try {
	    myCommandProcessor = new CommandProcessor();
	}
	catch(Exception e) {
	    ProfileException pe = new ProfileException("Exception creating the Command Processor");
	    pe.initCause(e);
	    throw pe;
	}
    }


  /**
   * Method declaration
   *
   * @throws ProfileException
   *
   * @see
   */
  private void createIMTPManager() throws ProfileException {
    // Get the parameter from the profile, use the RMI IMTP by default
    String className = getParameter(IMTP, "jade.imtp.rmi.RMIIMTPManager");

    try {
      myIMTPManager = (IMTPManager) Class.forName(className).newInstance();
    }
    catch (Exception e) {
      e.printStackTrace();

      throw new ProfileException("Error loading IMTPManager class "+className);
    } 
  } 

  private void createResourceManager() throws ProfileException {
  	myResourceManager = new FullResourceManager();
  } 


  /**
   * Retrieve a String value from the configuration properties.
   * If no parameter corresponding to the specified key is found,
   * return the provided default.
   * @param key The key identifying the parameter to be retrieved
   * among the configuration properties.
   */
  public String getParameter(String key, String aDefault) {
    return props.getProperty(key, aDefault);
  } 

  /**
   * Retrieve a list of Specifiers from the configuration properties.
   * Agents, MTPs and other items are specified among the configuration
   * properties in this way.
   * If no list of Specifiers corresponding to the specified key is found,
   * an empty list is returned.
   * @param key The key identifying the list of Specifiers to be retrieved
   * among the configuration properties.
   */
  public List getSpecifiers(String key) throws ProfileException {
    // Check if the list of specs is already in the properties as a list
    List l = null;

    try {
      l = (List) props.get(key);
      if (l == null) {
      	l = new ArrayList(0);
      }
      return l;
    }
    catch (ClassCastException cce) {
    }

    // Otherwise the list should be present as a string --> parse it
    String    specsLine = getParameter(key, null);

    try {
    	return Specifier.parseSpecifierList(specsLine);
    }
    catch (Exception e) {
    	throw new ProfileException("Error parsing specifier list "+specsLine+". "+e.getMessage());
    }
  } 

    public String toString() {
	StringBuffer str = new StringBuffer("(Profile");
	String[] properties = props.toStringArray();
	if (properties != null)
	    for (int i=0; i<properties.length; i++)
		str.append(" "+properties[i]);
	str.append(")");
	return str.toString();
    }
}

