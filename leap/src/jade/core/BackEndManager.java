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

//#J2ME_EXCLUDE_FILE
//#APIDOC_EXCLUDE_FILE

import jade.util.leap.Map;
import jade.util.leap.HashMap;
import jade.util.leap.Properties;
import jade.security.*;

import jade.util.Logger;

/**
   This class is an auxiliary JADE Node that act as parent node
   for all back-ends in the local JVM 
   @author Giovanni Caire - TILAB
 */

class BackEndManager {
	public static final String PERMANENT = "jade_core_BackEndManager_permanent";
	
  // The Profile defining the configuration of this Container
  protected Profile myProfile;

  // The IMTP manager, used to access IMTP-dependent functionalities
  protected IMTPManager myIMTPManager;

  // The platform Service Manager
  private ServiceManager myServiceManager;

  // The descriptor of the local node
  private NodeDescriptor myNodeDescriptor;
  
  private Map children = new HashMap();
  private boolean permanent;
  
  private Logger myLogger = Logger.getMyLogger(getClass().getName());

  private static BackEndManager theInstance;
  
  public static BackEndManager getInstance(Profile p) throws IMTPException {
  	if (theInstance == null) {
  		theInstance = new BackEndManager(p);
		  Runtime.instance().beginContainer();
  		theInstance.joinPlatform();
  	}
  	return theInstance;
  }
  
  private BackEndManager(Profile p) {
  	myProfile = p;
  }
	
  private void joinPlatform() throws IMTPException {
  	try {
  		permanent = myProfile.getBooleanProperty(PERMANENT, false);
  		
      myIMTPManager = myProfile.getIMTPManager();
      myIMTPManager.initialize(myProfile);

      // Get the Service Manager and the Service Finder
      myServiceManager = myProfile.getServiceManager();

      // Attach CommandProcessor and ServiceManager to the local node
			BaseNode localNode = (BaseNode) myIMTPManager.getLocalNode();
			localNode.setServiceManager(myServiceManager);

      // Initialize the NodeDescriptor
    	myNodeDescriptor = new NodeDescriptor(myIMTPManager.getLocalNode());
	    
    	// Actually join the platform (this call can modify the name of this node)
		  myServiceManager.addNode(myNodeDescriptor, null);
  	}
  	catch (IMTPException imtpe) {
  		// Let it through
			Runtime.instance().endContainer();
  		throw imtpe;
  	}
  	catch (Throwable t) {
			Runtime.instance().endContainer();
  		throw new IMTPException("Unexpected error in BackEndManager.joinPlatform(). ", t);
  	}
  }
  
  public void shutDown() {
  	theInstance = null;
  	
    try {
    	// Deregister services locally
			myServiceManager.removeNode(myNodeDescriptor);
    }
    catch(Exception e) {
      e.printStackTrace();
    }

		// Make the local node terminate (this releases threads blocked in ping)
		myIMTPManager.shutDown();
		
    // Notify the JADE Runtime that the node has terminated execution
    Runtime.instance().endContainer();
  }
  
  public Node getNode() {
  	return myNodeDescriptor.getNode();
  }
  
  public synchronized void register(NodeDescriptor child) {
  	children.put(child.getName(), child);
  	if (myLogger.isLoggable(Logger.CONFIG)) {
  		myLogger.log(Logger.CONFIG, "Child node "+child.getName()+" registered.");
  	}
  }
  
  public synchronized void deregister(NodeDescriptor child) {
  	try {
	  	NodeDescriptor dsc = (NodeDescriptor) children.remove(child.getName());
	  	// Notify the PlatformManager
	  	PlatformManager pm = myIMTPManager.getPlatformManagerProxy();
	  	pm.removeNode(dsc, false);
	  	if (myLogger.isLoggable(Logger.CONFIG)) {
	  		myLogger.log(Logger.CONFIG, "Child node "+child.getName()+" deregistered.");
	  	}
	  	if (children.isEmpty() && !permanent) {
	  		shutDown();
	  	}
  	}
  	catch (Exception e) {
  		myLogger.log(Logger.WARNING, "Error deregistering child node "+child.getName()+". "+e);
  		e.printStackTrace();
  	}
  }
}  
  