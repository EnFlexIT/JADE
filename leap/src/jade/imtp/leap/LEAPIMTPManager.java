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

  private String originalSMAddr;
  private String localSMAddr;
  private BaseServiceManagerProxy myServiceManagerProxy;
  private Map remoteServiceManagers;
  private NodeAdapter localNode;

  /**
   * Default constructor used to dynamically instantiate a new
   * LEAPIMTPManager object
   */
  public LEAPIMTPManager() {
      remoteServiceManagers = new HashMap();
  }

  // ////////////////////////////////
  // IMTPManager Interface
  // ////////////////////////////////

  /**
   * Initialize the support for intra-platform communication
   */
  public void initialize(Profile p, CommandProcessor cp) throws IMTPException {
    theProfile = p;

    if (!CommandDispatcher.create(theProfile)) {
      throw new IMTPException("wrong type of command dispatcher!");
    } 

    // Get the singleton CommandDispatcher
    theDispatcher = CommandDispatcher.getDispatcher();

    String localSvcMgrHost = theProfile.getParameter(Profile.LOCAL_SERVICE_MANAGER_HOST, null);
    String localSvcMgrPort = theProfile.getParameter(Profile.LOCAL_SERVICE_MANAGER_PORT, Integer.toString(JICPProtocol.DEFAULT_PORT));
    if(localSvcMgrHost != null) {
	try {
	    Integer.parseInt(localSvcMgrPort);
	}
	catch(NumberFormatException nfe) {
	    // The DEFAULT_PORT is used
	    localSvcMgrPort = Integer.toString(JICPProtocol.DEFAULT_PORT);
	}

	// Copy the Service Manager host and port to the LOCAL_HOST and LOCAL_PORT profile parameters
	theProfile.setParameter(JICPProtocol.LOCAL_HOST_KEY, localSvcMgrHost);
	theProfile.setParameter(JICPProtocol.LOCAL_PORT_KEY, localSvcMgrPort);

    }


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
      String mainProp = theProfile.getParameter(Profile.MAIN, null);
      boolean hasServiceManager = (mainProp == null || CaseInsensitiveString.equalsIgnoreCase(mainProp, "true"));

      localNode = new NodeAdapter("No-Name", theDispatcher, hasServiceManager);
      localNode.setCommandProcessor(cp);
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

    String mainURL = theProfile.getParameter(MAIN_URL, null);
    if(localSvcMgrHost != null) {

	// Attach to the pre-existing ServiceManager...
	addServiceManagerAddress(mainURL);
	originalSMAddr = mainURL;
	localSMAddr = theProfile.getParameter(Profile.MAIN_PROTO, "jicp") + "://" + localSvcMgrHost + ":" + localSvcMgrPort;
    }
    else {
	localSMAddr = mainURL;
    }

  }

  public void addServiceManagerAddress(String addr) throws IMTPException {

      try {
	  ServiceManagerStub sm = theDispatcher.getServiceManagerStub(addr);
	  synchronized(remoteServiceManagers) {
	      remoteServiceManagers.put(addr, sm);
	  }
      }
      catch(Exception e) {
	  throw new IMTPException("Error in contacting newly added Service Manager address", e);
      }

  }

  public void removeServiceManagerAddress(String addr) throws IMTPException {
      synchronized(remoteServiceManagers) {
	  remoteServiceManagers.remove(addr);
      }
  }

  public String[] getServiceManagerAddresses() throws IMTPException {
      synchronized(remoteServiceManagers) {

	  Object[] objs = remoteServiceManagers.keySet().toArray();
	  String[] result = new String[objs.length];

	  for(int i = 0; i < result.length; i++) {
	      result[i] = (String)objs[i];
	  }

	  return result;
      }
  }

  public ServiceManagerStub getRemoteServiceManager(String addr) {
      synchronized(remoteServiceManagers) {
	  return (ServiceManagerStub)remoteServiceManagers.get(addr);
      }
  }

  public ServiceManagerStub lookupRemoteServiceManager(String addr) throws IMTPException {
      ServiceManagerStub result = getRemoteServiceManager(addr);
      if(result == null) {
	  result = theDispatcher.getServiceManagerStub(addr);	  
      }

      return result;
  }

  public void nodeAdded(NodeDescriptor desc, String[] svcNames, Class[] svcInterfaces, int nodeCnt, int mainCnt) throws IMTPException {

      // Fill a String array with the class names
      String[] svcInterfacesNames = new String[svcInterfaces.length];
      for(int i = 0; i < svcInterfaces.length; i++) {
	  svcInterfacesNames[i] = svcInterfaces[i].getName();
      }

      synchronized(remoteServiceManagers) {
	  Iterator it = remoteServiceManagers.values().iterator();
	  while(it.hasNext()) {
	      try {
		  ServiceManagerStub sm = (ServiceManagerStub)it.next();
		  sm.updateCounters(nodeCnt, mainCnt);
		  sm.addNode(desc, svcNames, svcInterfacesNames, false);
	      }
	      catch(Exception e) {
		  e.printStackTrace();
	      }
	  }
      }

  }

  public void nodeRemoved(NodeDescriptor desc) throws IMTPException {
      synchronized(remoteServiceManagers) {
	  Iterator it = remoteServiceManagers.keySet().iterator();
	  Object[] keys = remoteServiceManagers.keySet().toArray();
	  for(int i = 0; i < keys.length; i++) {
	      String smAddr = (String)keys[i];
	      try {
		  ServiceManagerStub sm = (ServiceManagerStub)remoteServiceManagers.get(smAddr);
		  sm.removeNode(desc, false);
	      }
	      catch(IMTPException imtpe) {
		  // The removed node is the one hosting this Service Manager replica...
		  removeServiceManagerAddress(smAddr);
	      }
	      catch(Exception e) {
		  e.printStackTrace();
	      }
	  }
      }
  }

  public void serviceActivated(String svcName, Class svcItf, Node where) throws IMTPException {

      synchronized(remoteServiceManagers) {
	  Iterator it = remoteServiceManagers.values().iterator();
	  while(it.hasNext()) {
	      try {
		  ServiceManagerStub sm = (ServiceManagerStub)it.next();
		  sm.activateService(svcName, svcItf, new NodeDescriptor(where.getName(), where), false);
	      }
	      catch(Exception e) {
		  e.printStackTrace();
	      }
	  }
      }
  }

  public void serviceDeactivated(String svcName, Node where) throws IMTPException {
      synchronized(remoteServiceManagers) {
	  Iterator it = remoteServiceManagers.values().iterator();
	  while(it.hasNext()) {
	      try {
		  ServiceManagerStub sm = (ServiceManagerStub)it.next();
		  sm.deactivateService(svcName, new NodeDescriptor(where.getName(), where), false);
	      }
	      catch(Exception e) {
		  e.printStackTrace();
	      }
	  }
      }
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
      //#MIDP_EXCLUDE_BEGIN
      ServiceManagerImpl smImpl = (ServiceManagerImpl)mgr;
      Skeleton skel = new ServiceManagerSkel(smImpl, this);
      smImpl.setLocalAddress(localSMAddr);
      theDispatcher.registerSkeleton(skel, mgr);

      // Attach to the original Service manager, if any
      if(originalSMAddr != null) {
	  ServiceManagerStub originalSM = getRemoteServiceManager(originalSMAddr);

	  if(originalSM != null) {
	      String[] smAddresses = originalSM.addReplica(localSMAddr);

	      // Copy the addresses for all the other replicas
	      for(int i = 0; i < smAddresses.length; i++) {
		  addServiceManagerAddress(smAddresses[i]);
	      }
	  }
      }

      //#MIDP_EXCLUDE_END
  }

  public void unexportServiceManager(ServiceManager sm) throws IMTPException {
      //#MIDP_EXCLUDE_BEGIN
      if(sm instanceof ServiceManagerImpl) {
	  theDispatcher.deregisterSkeleton(sm);
      }
      //#MIDP_EXCLUDE_END
  }

  public ServiceManager createServiceManagerProxy(CommandProcessor proc) throws IMTPException {
      try {

	  // Look up the actual remote object in the LEAP profile
	  final ServiceManagerStub remoteSvcMgr = theDispatcher.getServiceManagerStub(theProfile);

	  myServiceManagerProxy = new BaseServiceManagerProxy(this, proc) {

	      public String getLocalAddress() throws IMTPException {
		  List addrs = theDispatcher.getLocalURLs();
		  if((addrs == null) || (addrs.size() == 0)) {
		      return null;
		  }
		  else {
		      return (String)addrs.get(0);
		  }
	      }

	      public String getPlatformName() throws IMTPException {
		  try {
		      return remoteSvcMgr.getPlatformName();
		  }
		  catch(IMTPException imtpe) {
		      while(reconnect()) {
			  try {
			      return remoteSvcMgr.getPlatformName();
			  }
			  catch(IMTPException imtpe2) {
			      // Store it and just try reconnecting again...
			      imtpe = imtpe2;
			  }
		      }

		      throw imtpe;
		  }
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
		  try {
		      return remoteSvcMgr.addNode(desc, svcNames, svcInterfaces, true);
		  }
		  catch(IMTPException imtpe) {
		      while(reconnect()) {
			  try {
			      return remoteSvcMgr.addNode(desc, svcNames, svcInterfaces, true);
			  }
			  catch(IMTPException imtpe2) {
			      // Store it and just try reconnecting again...
			      imtpe = imtpe2;
			  }
		      }

		      throw imtpe;
		  }
	      }

	      protected void removeRemoteNode(NodeDescriptor desc) throws IMTPException, ServiceException {
		  // First, deregister this node with the service manager
		  try {
		      remoteSvcMgr.removeNode(desc, true);
		  }
		  catch(IMTPException imtpe) {
		      while(reconnect()) {
			  try {
			      remoteSvcMgr.removeNode(desc, true);
			  }
			  catch(IMTPException imtpe2) {
			      // Store it and just try reconnecting again...
			      imtpe = imtpe2;
			  }
		      }

		      throw imtpe;
		  }
	      }

	      protected void addRemoteSlice(String svcName, Class itf, NodeDescriptor where) throws IMTPException, ServiceException {
		  try {
		      remoteSvcMgr.activateService(svcName, itf, where, true);
		  }
		  catch(IMTPException imtpe) {
		      while(reconnect()) {
			  try {
			      remoteSvcMgr.activateService(svcName, itf, where, true);
			  }
			  catch(IMTPException imtpe2) {
			      // Store it and just try reconnecting again...
			      imtpe = imtpe2;
			  }
		      }

		      throw imtpe;
		  }
	      }

	      protected void removeRemoteSlice(String svcName, NodeDescriptor where) throws IMTPException, ServiceException {
		  try {
		      remoteSvcMgr.deactivateService(svcName, where, true);
		  }
		  catch(IMTPException imtpe) {
		      while(reconnect()) {
			  try {
			      remoteSvcMgr.deactivateService(svcName, where, true);
			  }
			  catch(IMTPException imtpe2) {
			      // Store it and just try reconnecting again...
			      imtpe = imtpe2;
			  }
		      }

		      throw imtpe;
		  }
	      }

	      protected Node findSliceNode(String serviceKey, String sliceKey) throws IMTPException, ServiceException {
		  try {
		      return remoteSvcMgr.findSliceNode(serviceKey, sliceKey);
		  }
		  catch(IMTPException imtpe) {
		      while(reconnect()) {
			  try {
			      return remoteSvcMgr.findSliceNode(serviceKey, sliceKey);
			  }
			  catch(IMTPException imtpe2) {
			      // Store it and just try reconnecting again...
			      imtpe = imtpe2;
			  }
		      }

		      throw imtpe;
		  }
	      }

	      protected Node[] findAllNodes(String serviceKey) throws IMTPException, ServiceException {
		  try {
		      return remoteSvcMgr.findAllNodes(serviceKey);
		  }
		  catch(IMTPException imtpe) {
		      while(reconnect()) {
			  try {
			      return remoteSvcMgr.findAllNodes(serviceKey);
			  }
			  catch(IMTPException imtpe2) {
			      // Store it and just try reconnecting again...
			      imtpe = imtpe2;
			  }
		      }

		      throw imtpe;
		  }
	      }

	      // Try to connect once again
	      private boolean reconnect() throws IMTPException {

		  String[] addrs = getAddresses();
		  for(int i = 0; i < addrs.length; i++) {
		      try {
			  theDispatcher.clearStubAddresses(remoteSvcMgr);
			  theDispatcher.addAddressToStub(remoteSvcMgr, addrs[i]);

			  remoteSvcMgr.adopt(localNode);
			  return true;
		      }
		      catch(Exception e) {
			  // Ignore it and try the next address...
		      }
		  }

		  return false;
	      }

	  };

	  // Add all the additional addresses to the Service Manager Proxy...
	  List smAddrs = theProfile.getSpecifiers(Profile.REMOTE_SERVICE_MANAGER_ADDRESSES);
	  Iterator smIt = smAddrs.iterator();
	  while(smIt.hasNext()) {
	      Specifier spec = (Specifier)smIt.next();
	      String smAddr = "jicp://" + spec.toString();
	      myServiceManagerProxy.addAddress(smAddr);
	  }

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
          mainPort = (mainPort != null) ? (":" + mainPort) : (":" + JICPProtocol.DEFAULT_PORT);
          mainURL = new String(mainProto + "://" + mainHost + mainPort);
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
      // This is a Main Container
      String mainURL = theProfile.getParameter(MAIN_URL, null);
      if (mainURL == null) {
        String mainHost = getMainHost();
        if (mainHost != null) {
	    String mainProto = theProfile.getParameter(Profile.MAIN_PROTO, "jicp");
	    String mainPort = theProfile.getParameter(Profile.MAIN_PORT, null);
	    mainPort = (mainPort != null) ? (":" + mainPort) : (":" + JICPProtocol.DEFAULT_PORT);
	    mainURL = new String(mainProto + "://" + mainHost + mainPort);
	    theProfile.setParameter(MAIN_URL, mainURL);
        }
      }

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

