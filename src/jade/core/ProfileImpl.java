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

import jade.util.leap.LEAPProperties;
import jade.util.leap.List;
import jade.util.leap.ArrayList;
import jade.util.leap.Iterator;
import java.io.IOException;
import java.net.*;

/**
 * This class allows retrieving configuration-dependent classes.
 * 
 * @author  Federico Bergenti
 * @author  Giovanni Caire - TILAB
 * @author  Giovanni Rimassa - Universita` di Parma
 * @version 1.0, 22/11/00
 * 
 * @see jade.util.leap.LEAPProperties
 */
public class ProfileImpl extends Profile {

    /**
     * Key to retrieve the MobilityHandler implementation class among
     * the configuration properties.
     * @see jade.util.leap.LEAPProperties
     */
    public static final String MOBILITY = "mobility";

    /**
     * Key to retrieve the acc implementation class among
     * the configuration properties.
     * @see jade.util.leap.LEAPProperties
     */
    public static final String ACC = "acc";

    /**
     * Key to retrieve the AgentCache implementation class among
     * the configuration properties.
     * @see jade.util.leap.LEAPProperties
     */
    public static final String AGENTCACHE = "cache";

    /**
     * Key to retrieve the IMTPManager implementation class among
     * the configuration properties.
     * @see jade.util.leap.LEAPProperties
     */
    public static final String IMTP = "imtp";
    private LEAPProperties     props = null;

    /**
       Creates a Profile implementation that gets the configuration from 
       a file
     */
    public ProfileImpl(String fileName) {
    	if (fileName != null) {
	        props = new LEAPProperties(fileName);
    	}
    	else {
    		props = new LEAPProperties();
    	}
    	try {
    		// Set default values
      		String host = InetAddress.getLocalHost().getHostName();
      		props.setProperty(MAIN, "true");
      		props.setProperty(MAIN_PROTO, "rmi");
      		props.setProperty(MAIN_HOST, host);
      		props.setProperty(MAIN_PORT, "1099");
      		props.setProperty(PLATFORM_ID, host + ":1099/JADE");
      		
      		// Load the properties from property file
      		if (fileName != null) {
	        	props.load();
      		}
    	}
    	catch(UnknownHostException uhe) {
    		uhe.printStackTrace();
    	}
    	catch(IOException ioe) {
    		ioe.printStackTrace();
    	}

    }

    /**
       Creates a new profile implementation, with default protocol,
       host and port for the Main Container and the default platform ID.
     */
	public ProfileImpl() {
		this(null);
        /*props = new LEAPProperties();
    	try {
      		String host = InetAddress.getLocalHost().getHostName();
      		props.setProperty(MAIN, "true");
      		props.setProperty(MAIN_PROTO, "rmi");
      		props.setProperty(MAIN_HOST, host);
      		props.setProperty(MAIN_PORT, "1099");
      		props.setProperty(PLATFORM_ID, host + ":1099/JADE");
    	}
    	catch(UnknownHostException uhe) {
      		uhe.printStackTrace();
    	}*/
  	}


  	/**
       Creates a Profile implementation, with the given host, port
       and name. To keep the default value for some of them, just pass
       <code>null</code> as the corresponding argument.
       @param host The host name where the Main Container is running.
       @param port The port where the Main Container is listening for
       container registrations.
       @param platformID The unique ID of the platform.
     */
	public ProfileImpl(String host, String port, String platformID) {
    	this(); // Call default constructor
    	props.setProperty(MAIN, "false");
    	if(host != null)
      		props.setProperty(MAIN_HOST, host);
    	if(port != null)
      		props.setProperty(MAIN_PORT, port);
    	if(platformID != null)
      		props.setProperty(PLATFORM_ID, platformID);
    	else {
      		String h = props.getProperty(MAIN_HOST);
      		String p = props.getProperty(MAIN_PORT);
      		props.setProperty(PLATFORM_ID, h + ":" + p + "/JADE");
    	}
	}

    /**
     */
    protected acc getAcc() throws ProfileException {
        String className = props.getProperty(ACC);

        if(className == null) {
          // Use the Full ACC by default
          className = new String("jade.core.FullAcc");
        }

        try {
            return (acc) Class.forName(className).newInstance();
        } 
        catch (Exception e) {
	  throw new ProfileException("Error loading acc class " + className);
        } 
    } 

    /**
     *
    protected AgentCache getAgentCache() throws ProfileException {
        String className = props.getProperty(AGENTCACHE);

	if (className == null) {
			// Use FullAgentCache by default
            className = new String("jade.core.FullAgentCache");
        } 

        try {
            return (AgentCache) Class.forName(className).newInstance();
        } 
        catch (Exception e) {
            throw new ProfileException("Error loading AgentCache class" 
                                       + className);
        } 
    } 
    */

    /**
     *
    protected MobilityHandler getMobilityHandler() throws ProfileException {
        String className = props.getProperty(MOBILITY);

        if (className == null) {
        	// Use RealMobilityHandler by default
            className = new String("jade.core.RealMobilityHandler");
        } 

        try {
            return (MobilityHandler) Class.forName(className).newInstance();
        } 
        catch (Exception e) {
            throw new ProfileException("Error loading MobilityHandler class" 
                                       + className);
        } 
    } 
	*/
	
    /**
     *
    protected IMTPManager getIMTPManager() throws ProfileException {
        String className = props.getProperty(IMTP);

        if (className == null) {
        	// Use RMIIMTPManager by default
            className = new String("jade.imtp.rmi.RMIIMTPManager");
        } 

        try {
            return (IMTPManager) Class.forName(className).newInstance();
        } 
        catch (Exception e) {
            throw new ProfileException("Error loading IMTPManager class" 
                                       + className);
        } 
    } 
	*/
	
    /**
     * Retrieve a String value from the configuration properties.
     * If no parameter corresponding to the specified key is found,
     * null is returned.
     * @param key The key identifying the parameter to be retrieved
     * among the configuration properties.
     */
    public String getParameter(String key) throws ProfileException {
      return props.getProperty(key);
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
        ArrayList specs = new ArrayList();
        String    specsLine = props.getProperty(key);

        if (specsLine != null &&!specsLine.equals("")) {

            // Copy the string with the specifiers into an array of char
            char[] specsChars = new char[specsLine.length()];

            specsLine.getChars(0, specsLine.length(), specsChars, 0);

            // Create the StringBuffer to hold the first specifier
            StringBuffer sbSpecifier = new StringBuffer();
            int          i = 0;

            while (i < specsChars.length) {
                char c = specsChars[i];

                if (c != ';') {
                    sbSpecifier.append(c);
                } 
                else {

                    // The specifier is terminated --> Convert it into a Specifier object
                    String tmp = sbSpecifier.toString().trim();

                    if (tmp.length() > 0) {
                        Specifier s = parseSpecifier(tmp);

                        // Add the Specifier to the list
                        specs.add(s);
                    } 

                    // Create the StringBuffer to hold the next specifier
                    sbSpecifier = new StringBuffer();
                } 

                ++i;
            } 

            // Handle the last specifier
            String tmp = sbSpecifier.toString().trim();

            if (tmp.length() > 0) {
                Specifier s = parseSpecifier(tmp);

                // Add the Specifier to the list
                specs.add(s);
            } 
        } 

        return specs;
    } 

}

