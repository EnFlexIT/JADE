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
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.io.IOException;
import java.net.*;
import java.util.Hashtable;

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

    private LEAPProperties props = new LEAPProperties();

    public void putProperty(String key, String value) {
	props.put(key,value);
    }
    
    public void putSpecifierList(String key, List value) {
	props.put(key,value);
    }


    private MainContainer myMain = null;
    private IMTPManager myIMTPManager = null;
    private acc myACC = null;



    
    
    /**
     */
    protected MainContainer getMain() throws ProfileException { 
    	if (myMain == null) {
    		createMain();
    	}
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
    protected acc getAcc() throws ProfileException {
    	if (myACC == null) {
    		createACC();
    	}
    	return myACC;
    }
    
    private void createMain() throws ProfileException {
			// Be sure that the IMTPManager is not null
    	getIMTPManager();   
    	
    	try {
        	String isMain = props.getProperty(MAIN);
        	if(isMain == null || isMain.equalsIgnoreCase("true")) {
        		// The real Main
        		myMain = new MainContainerImpl(this);
        		myIMTPManager.remotize(myMain);
        	}
        	else {
        		// A proxy to the main
        		myMain = new MainContainerProxy(this, myIMTPManager.getMain());
        	}
    	}
    	catch (IMTPException imtpe) {
    		throw new ProfileException(imtpe.getMessage());
    	}
    }
    
    private void createIMTPManager() throws ProfileException {
      	// Use the RMI IMTP by default
	String className = new String("jade.imtp.rmi.RMIIMTPManager");

      try {
        myIMTPManager = (IMTPManager) Class.forName(className).newInstance();
        myIMTPManager.initialize(this);
      } 
      catch (Exception e) {
      	e.printStackTrace();
	  		throw new ProfileException("Error loading IMTPManager class " + className);
      }
    }
    
    private void createACC() throws ProfileException {
	// Use the Full ACC by default
	String className = new String("jade.core.FullAcc");
        try {
            myACC = (acc) Class.forName(className).newInstance();
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
        return (List)props.get(key);
    } 

}

