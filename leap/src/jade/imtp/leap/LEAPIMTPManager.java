/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

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

package jade.imtp.leap;


import jade.core.*;
import jade.lang.acl.ACLMessage;
import jade.mtp.TransportAddress;
import jade.security.AuthException;
import jade.util.leap.*;
import jade.imtp.leap.JICP.JICPProtocol;
import jade.imtp.leap.JICP.Connection;
import jade.util.Logger;
import java.io.IOException;

/**
 * @author Giovanni Caire - Telecom Italia Lab
 */
public class LEAPIMTPManager implements IMTPManager {

    /**
     * Profile keys for LEAP-IMTP specific parameters
     */
    static final String MAIN_URL = "main-url";

    private static final String LOCAL_PORT = "local-port";
    private static final String ICPS = "icps";
    private static final String ROUTER_URL = "router-url";
	
  /**
   * Local pointer to the singleton command dispatcher in this JVM
   */
  private CommandDispatcher theDispatcher = null;

  /**
   * The Profile holding the configuration for this IMTPManager
   */
  private Profile           theProfile = null;


  private BaseServiceManagerProxy myServiceManagerProxy;
  private NodeAdapter localNode;

  /**
   * Default constructor used to dynamically instantiate a new
   * LEAPIMTPManager object
   */
  public LEAPIMTPManager() {
  }

  // ////////////////////////////////
  // IMTPManager Interface
  // ////////////////////////////////

  /**
   * Initialize the support for intra-platform communication
   */
  public void initialize(Profile p) throws IMTPException {
    theProfile = p;

    if (!CommandDispatcher.create(theProfile)) {
      throw new IMTPException("wrong type of command dispatcher!");
    } 

    // Get the singleton CommandDispatcher
    theDispatcher = CommandDispatcher.getDispatcher();



    // Add to the CommandDispatcher the ICPs specified in the Profile
    try {
    	// Set defaults if not explicitly set. 
    	setDefaults();
    	
      List     l = theProfile.getSpecifiers(ICPS);
      Iterator it = l.iterator();

      while (it.hasNext()) {
        Specifier s = (Specifier) it.next();

        try {
          ICP peer = (ICP) Class.forName(s.getClassName()).newInstance();
          String id = (s.getArgs() != null ? (String) (s.getArgs()[0]) : null);
          theDispatcher.addICP(peer, id, theProfile);
        } 
        catch (Exception e) {
          Logger.println("Error adding ICP. "+e);
        } 
      }

      // Initialize the local node
      localNode = new NodeAdapter("No-Name", theDispatcher);
 
    } 
    catch (ProfileException pe) {
      // Just print a warning
      Logger.println("Profile error. "+pe.getMessage());
    } 

    // Now check that some ICP is active. Note that, as a CommandDispatcher
    // can serve multiple IMTPManagers, some ICPs could be already active.
    List URLs = theDispatcher.getLocalURLs();

    if (URLs.size() == 0) {
      throw new IMTPException("No ICP active");
    } 
    else {
      Iterator it = URLs.iterator();

      Logger.println("Listening for intra-platform commands on address:");

      while (it.hasNext()) {
        Logger.println((String) it.next());
      } 
    } 

    // Finally, if a URL for the default router is specified in the
    // Profile, set it in the CommandDispatcher
    theDispatcher.setRouterAddress(theProfile.getParameter(ROUTER_URL, null));
  } 

  public void connect(ContainerID id) throws IMTPException {
      String containerName = id.getName();
      localNode.setName(containerName);
  }

  public void disconnect(ContainerID id) throws IMTPException {
      // Simply exit the local node...
      localNode.exit();
      theDispatcher.deregisterSkeleton(localNode);
  }

  public Node getLocalNode() throws IMTPException {
      return localNode;
  }

  public void exportServiceManager(ServiceManager mgr) throws IMTPException {
      Skeleton skel = new ServiceManagerSkel((ServiceManagerImpl)mgr, this);
      theDispatcher.registerSkeleton(skel, mgr);
  }

  public void unexportServiceManager(ServiceManager sm) throws IMTPException {
      if(sm instanceof ServiceManagerImpl) {
	  theDispatcher.deregisterSkeleton(sm);
      }
  }

  public ServiceManager createServiceManagerProxy(CommandProcessor proc) throws IMTPException {
      try {

	  // Look up the actual remote object in the LEAP profile
	  final ServiceManagerStub remoteSvcMgr = theDispatcher.getServiceManagerStub(theProfile);

	  myServiceManagerProxy = new BaseServiceManagerProxy(this, proc) {

	      public String getPlatformName() throws IMTPException {
		  return remoteSvcMgr.getPlatformName();
	      }

	      protected String addRemoteNode(NodeDescriptor desc, ServiceDescriptor[] services) throws IMTPException, ServiceException, AuthException {
		  String[] svcNames = new String[services.length];
		  String[] svcInterfaces = new String[services.length];

		  // Fill the parameter arrays
		  for(int i = 0; i < services.length; i++) {
		      svcNames[i] = services[i].getName();
		      svcInterfaces[i] = services[i].getService().getHorizontalInterface().getName();
		  }

		  // Now register this node and all its services with the Service Manager
		  return remoteSvcMgr.addNode(desc, svcNames, svcInterfaces);
	      }

	      protected void removeRemoteNode(NodeDescriptor desc) throws IMTPException, ServiceException {
		  // First, deregister this node with the service manager
		  remoteSvcMgr.removeNode(desc);
	      }

	      protected void addRemoteSlice(String svcName, Class itf, NodeDescriptor where) throws IMTPException, ServiceException {
		  remoteSvcMgr.activateService(svcName, itf, where);
	      }

	      protected void removeRemoteSlice(String svcName, NodeDescriptor where) throws IMTPException, ServiceException {
		  remoteSvcMgr.deactivateService(svcName, where);
	      }

	      protected Node findSliceNode(String serviceKey, String sliceKey) throws IMTPException, ServiceException {
		  return remoteSvcMgr.findSliceNode(serviceKey, sliceKey);
	      }

	      protected Node[] findAllNodes(String serviceKey) throws IMTPException, ServiceException {
		  return remoteSvcMgr.findAllNodes(serviceKey);
	      }

	  };

	  return myServiceManagerProxy;
      }
      catch (Exception e) {
	  throw new IMTPException("Exception while looking up the Service Manager in the RMI Registry", e);
      }
  }

  public ServiceFinder createServiceFinderProxy() throws IMTPException {
      return myServiceManagerProxy;
  }

  public void exportSlice(String serviceName, Service.Slice localSlice) throws IMTPException {
      localNode.exportSlice(serviceName, localSlice);
  }

  public void unexportSlice(String serviceName, Service.Slice localSlice) throws IMTPException {
      localNode.unexportSlice(serviceName);
  }

  public Service.Slice createSliceProxy(String serviceName, Class itf, Node where) throws IMTPException {
      try {
	  Class proxyClass = Class.forName(serviceName + "Proxy");
	  Service.SliceProxy proxy = (Service.SliceProxy)proxyClass.newInstance();
	  proxy.setNode(where);
	  return proxy;
      }
      catch(Exception e) {
	  throw new IMTPException("Error creating a slice proxy", e);
      }
  }

  /**
   * Release all resources of this LEAPIMTPManager
   */
  public void shutDown() {
    if (theDispatcher != null) {
      theDispatcher.shutDown();

      theDispatcher = null;
    } 
  } 

  /**
   */
  public List getLocalAddresses() {
    return theDispatcher.getLocalTAs();
  } 

  // /////////////////////////
  // PRIVATE METHODS
  // /////////////////////////

  /**
     In the Profile ICPS can be specified as follows.
     1) If there is only one ICP -->
     icps = <icp-class-name>
     <param-key> = <param-value>
     .......
     2) In general (typically used when there are 2 or more ICPs)
     icps = <icp1-class-name>(<icp1-id>);<icp2-class-name>(<icp2-id>)...
     <icp1-id>-<param-key> = <param-value>
     .......
     <icp2-id>-<param-key> = <param-value>
     .......
     
     If there is no ICP indication set it as follows. 
     a) Peripheral container / J2SE 
        - There is at least 1 ICP already active --> Nothing
        - There are no ICPs already active --> JICPPeer
     b) Peripheral container / PJAVA or MIDP --> JICPBMPeer
     c) Main container / J2SE or PJAVA --> JICPPeer
   */
  private void setDefaults() throws ProfileException {
    if ("false".equals(theProfile.getParameter(Profile.MAIN, null))) {
      // This is a "Peripheral Container"
      // - MAIN URL
      String mainURL = theProfile.getParameter(MAIN_URL, null);
      if (mainURL == null) {
        String mainHost = getMainHost();
        if (mainHost != null) {
        	String mainProto = theProfile.getParameter(Profile.MAIN_PROTO, "jicp");
          String mainPort = theProfile.getParameter(Profile.MAIN_PORT, null);
          mainPort = (mainPort != null ? new String(":"+mainPort) : "");
          mainURL = new String(mainProto+"://"+mainHost+mainPort);
          theProfile.setParameter(MAIN_URL, mainURL);
        }
        else {
          throw new ProfileException("Main URL not specified");
        }
      }

      // - ICPS
      if (theProfile.getParameter(ICPS, null) == null) {
      	String jvm = theProfile.getParameter(Profile.JVM, null);
      	if (Profile.J2SE.equals(jvm)) {
      		// Set default ICPS for J2SE
    			if (theDispatcher.getLocalURLs().size() == 0) {
        		theProfile.setParameter(ICPS, "jade.imtp.leap.JICP.JICPPeer");
    			}
      	}
      	else {
      		// Set default ICPS for PJAVA and MIDP (same)
      		theProfile.setParameter(ICPS, "jade.imtp.leap.JICP.JICPBMPeer");
      		if (theProfile.getParameter(JICPProtocol.REMOTE_URL_KEY, null) == null) {
	        	theProfile.setParameter(JICPProtocol.REMOTE_URL_KEY, mainURL);
      		}
      	}
      }
    }
    else {
      // This is the Main Container
      if (theProfile.getParameter(ICPS, null) == null) {
        theProfile.setParameter(ICPS, "jade.imtp.leap.JICP.JICPPeer");
      }
    }  
  }
  
  /**
   */
  private String getMainHost() {
  	String host = theProfile.getParameter(Profile.MAIN_HOST, null);
  	if (host == null) {
	    try {
      	host = Connection.getLocalHost();
    	}
    	catch (Exception e) {
    	}
  	}
    return host;
  }
  
}

