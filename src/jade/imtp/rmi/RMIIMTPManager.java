/****************************************************************
 * JADE - Java Agent DEvelopment Framework is a framework to develop
 * multi-agent systems in compliance with the FIPA specifications.
 * Copyright (C) 2000 CSELT S.p.A.
 * GNU Lesser General Public License
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation,
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 ******************************************************************/

package jade.imtp.rmi;

import java.net.InetAddress;
import java.net.UnknownHostException;

import java.io.IOException;

import java.rmi.*;
import java.rmi.registry.*;
import java.rmi.server.*;

import jade.util.leap.Iterator;
import jade.util.leap.List;
import jade.util.leap.LinkedList;
import jade.util.leap.Map;
import jade.util.leap.HashMap;

import jade.security.AuthException;

import jade.core.*;
import jade.lang.acl.ACLMessage;
import jade.mtp.TransportAddress;


/**
 * @author Giovanni Caire - Telecom Italia Lab
 * @author Giovanni Rimassa - FRAMeTech s.r.l.
 */
public class RMIIMTPManager implements IMTPManager {

    /**
       This constant is the name of the property whose value contains
       the name of the host where a local Service Manager is to be
       exported (when using JADE fault-tolerant deployment).
    */
    private static final String LOCAL_SERVICE_MANAGER_HOST = "smhost";

    /**
       This constant is the name of the property whose value contains
       the TCP port where a local Service Manager is to be exported
       (when using JADE fault-tolerant deployment).
    */
    public static final String LOCAL_SERVICE_MANAGER_PORT = "smport";

    /**
       This constant is the name of the property whose value contains
       the name of the host where the local RMI Node is to be
       exported.
    */
    private static final String LOCAL_NODE_HOST = "nodehost";

    /**
       This constant is the name of the property whose value contains
       the TCP port where the local RMI Node is to be exported.
    */
    public static final String LOCAL_NODE_PORT = "nodeport";


    private static final int DEFAULT_RMI_PORT = 1099;


    private Profile myProfile;

    // Host and port where the original RMI Registry is listening
    private String mainHost;
    private int mainPort;

    // Host and port where the local RMI Registry (if any) is listening
    private String localHost;
    private int localPort;

    // Host and port where the local Service Manager (if any) is listening
    private String localSvcMgrHost;
    private int localSvcMgrPort;

    // Host and port where the local RMI Node is listening
    private String localNodeHost;
    private int localNodePort;

    // The RMI URL where the Service Manager is to be. If there is a
    // locally installed one, this string points to it, otherwise it 
    // points to the Service Manager of the original one.
    private String baseRMI;


    // The RMI URL where the original (i.e. the first replica) Service
    // Manager is to be found.
    private String originalRMI;

    private NodeAdapter localNode;
    private BaseServiceManagerProxy myServiceManagerProxy;
    private ServiceManagerRMIImpl myRMIServiceManager;
    private Map remoteServiceManagers;

  public RMIIMTPManager() {
      remoteServiceManagers = new HashMap();
  }

  /**
   */
  public void initialize(Profile p, CommandProcessor cp) throws IMTPException {
      myProfile = p;
      mainHost = myProfile.getParameter(Profile.MAIN_HOST, null);
      
      mainPort = DEFAULT_RMI_PORT;
      String mainPortStr = myProfile.getParameter(Profile.MAIN_PORT, null);
      try {
	  mainPort = Integer.parseInt(mainPortStr);
      }
      catch (NumberFormatException nfe) {
	  // Do nothing. The DEFAULT_RMI_PORT is used 
      }

      localHost = myProfile.getParameter(Profile.LOCAL_HOST, null);

      localPort = DEFAULT_RMI_PORT;
      String localPortStr = myProfile.getParameter(Profile.LOCAL_PORT, null);
      try {
	  localPort = Integer.parseInt(localPortStr);
      }
      catch (NumberFormatException nfe) {
	  // Do nothing. The DEFAULT_RMI_PORT is used
      }

      // For a sole Main Container, local host and port overwrite the
      // main host and port
      if(myProfile.getParameter(Profile.MAIN, true) && !myProfile.getParameter(Profile.LOCAL_SERVICE_MANAGER, false)) {
	  originalRMI = "rmi://" + localHost + ":" + localPort + "/";
      }
      else {
	  originalRMI = "rmi://" + mainHost + ":" + mainPort + "/";
      }

      localSvcMgrHost = myProfile.getParameter(LOCAL_SERVICE_MANAGER_HOST, mainHost);

      localSvcMgrPort = 0;
      String localSvcMgrPortStr = myProfile.getParameter(LOCAL_SERVICE_MANAGER_PORT, null);
      if(localSvcMgrPortStr != null) {
	  try {
	      localSvcMgrPort = Integer.parseInt(localSvcMgrPortStr);
	  }
	  catch(NumberFormatException nfe) {
	      // Do nothing. An OS-chosen port is used
	  }
      }

      // Read the host and port for the local RMI Node from the profile.
      localNodeHost = myProfile.getParameter(LOCAL_NODE_HOST, mainHost);
      localNodePort = 0;
      String localNodePortStr = myProfile.getParameter(LOCAL_NODE_PORT, null);
      if(localNodePortStr != null) {
	  try {
	      localNodePort = Integer.parseInt(localNodePortStr);
	  }
	  catch(NumberFormatException nfe) {
	      // Do nothing. An OS-chosen port is used
	  }
      }

      try {
	  // Create the local node and mark it as hosting a local Service Manager
	  localNode = new NodeAdapter(AgentManager.UNNAMED_CONTAINER_NAME, myProfile.getParameter(Profile.MAIN, true), localNodePort);
      }
      catch(RemoteException re) {
	  throw new IMTPException("An RMI error occurred", re);
      }

      // If the LOCAL_SERVICE_MANAGER_HOST property is set, then we have to use it to set the
      // RMI address where the Service Manager is going to be exported.
      if(myProfile.getParameter(Profile.LOCAL_SERVICE_MANAGER, false)) {

	  baseRMI = "rmi://" + localHost + ":" + localPort + "/";

	  // Attach to the pre-existing ServiceManager...
	  addServiceManagerAddress(originalRMI);
      }
      else {
	  baseRMI = originalRMI;
      }

      localNode.setCommandProcessor(cp);

  }


    /**
     * Get the RMIRegistry. If a registry is already active on this host
     * and the given portNumber, then that registry is returned, 
     * otherwise a new registry is created and returned.
     * @param portNumber is the port on which the registry accepts requests
     * @param host host for the remote registry, if null the local host is used
     * @author David Bell (HP Palo Alto)
     **/
    private Registry getRmiRegistry(String host, int portNumber) throws RemoteException {
	Registry rmiRegistry = null;
	// See if a registry already exists and
	// make sure we can really talk to it.
	try {
	    rmiRegistry = LocateRegistry.getRegistry(host, portNumber);
	    rmiRegistry.list();
	} catch (Exception exc) {
	    rmiRegistry = null;
	}

	// If rmiRegistry is null, then we failed to find an already running
	// instance of the registry, so let's create one.
	if (rmiRegistry == null) {
	    //if (isLocalHost(host))
		rmiRegistry = LocateRegistry.createRegistry(portNumber);
		//else
		//throw new java.net.UnknownHostException("cannot create RMI registry on a remote host");
	}

	return rmiRegistry;
	
    } // END getRmiRegistry()


  public void exportServiceManager(ServiceManager sm) throws IMTPException {
    try {

      ServiceManagerImpl smImpl = (ServiceManagerImpl)sm;
      String svcMgrName = baseRMI + SERVICE_MANAGER_NAME;
      smImpl.setLocalAddress(baseRMI);
      myRMIServiceManager = new ServiceManagerRMIImpl(smImpl, this, localSvcMgrPort);

      int registryPort = localPort;
      if(registryPort == 0) {
	  registryPort = mainPort;
      }

      Registry theRegistry = getRmiRegistry(null, registryPort);
      Naming.bind(svcMgrName, myRMIServiceManager);

      // Attach to the original Service manager, if any
      ServiceManagerRMI originalSM = getRemoteServiceManager(originalRMI);

      if(originalSM != null) {
	  String[] smAddresses = originalSM.addReplica(baseRMI);

	  // Copy the addresses for all the other replicas
	  for(int i = 0; i < smAddresses.length; i++) {
	      addServiceManagerAddress(smAddresses[i]);
	  }
      }
    }
    catch(ConnectException ce) {
      // This one is thrown when trying to bind in an RMIRegistry that
      // is not on the current host
      System.out.println("ERROR: trying to bind to a remote RMI registry.");
      System.out.println("If you want to start a JADE main container:");
      System.out.println("  Make sure the specified host name or IP address belongs to the local machine.");
      System.out.println("  Please use '-host' and/or '-port' options to setup JADE host and port.");
      System.out.println("If you want to start a JADE non-main container: ");
      System.out.println("  Use the '-container' option, then use '-host' and '-port' to specify the ");
      System.out.println("  location of the main container you want to connect to.");
      throw new IMTPException("RMI Binding error", ce);
    }
    catch(RemoteException re) {
      throw new IMTPException("Communication failure while starting JADE Runtime System. Check if the RMIRegistry CLASSPATH includes the RMI Stub classes of JADE.", re);
    }
    catch(AlreadyBoundException abe) {
	throw new IMTPException("The Service Manager was already bound in the RMI Registry", abe);
    }
    catch(Exception e) {
      throw new IMTPException("Problem starting JADE Runtime System.", e);
    }
  }


  public void unexportServiceManager(ServiceManager sm) throws IMTPException {
      if(sm instanceof ServiceManagerImpl) { // Local implementation
	  try {

	      // Remove the RMI remote object from the RMI registry
	      // and disconnect it from the network
	      String svcMgrName = baseRMI + SERVICE_MANAGER_NAME;
	      Naming.unbind(svcMgrName);
	      myRMIServiceManager.unexportObject(myRMIServiceManager, true);
	  }
	  catch(Exception e) {
	      throw new IMTPException("Error in unexporting the RMI Service Manager", e);
	  }
      }
      else { // Remote Proxy
	  // Do Nothing...
      }
  }

  public void addServiceManagerAddress(String addr) throws IMTPException {

      try {
	  String smName = addr + SERVICE_MANAGER_NAME;
	  ServiceManagerRMI sm = (ServiceManagerRMI)Naming.lookup(smName);
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

  public void nodeAdded(NodeDescriptor desc, String[] svcNames, Class[] svcInterfaces, int nodeCnt, int mainCnt) throws IMTPException {

      synchronized(remoteServiceManagers) {
	  Iterator it = remoteServiceManagers.values().iterator();
	  while(it.hasNext()) {
	      try {
		  ServiceManagerRMI sm = (ServiceManagerRMI)it.next();
		  sm.updateCounters(nodeCnt, mainCnt);
		  sm.addNode(desc, svcNames, svcInterfaces, false);
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
		  ServiceManagerRMI sm = (ServiceManagerRMI)remoteServiceManagers.get(smAddr);
		  sm.removeNode(desc, false);
	      }
	      catch(RemoteException re) {
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
		  ServiceManagerRMI sm = (ServiceManagerRMI)it.next();
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
		  ServiceManagerRMI sm = (ServiceManagerRMI)it.next();
		  sm.deactivateService(svcName, new NodeDescriptor(where.getName(), where), false);
	      }
	      catch(Exception e) {
		  e.printStackTrace();
	      }
	  }
      }
  }

  public ServiceManager createServiceManagerProxy(CommandProcessor proc) throws IMTPException {
      try {

	  // FIXME: Must get the whole list from the profile...
	  final String svcMgrName = baseRMI + SERVICE_MANAGER_NAME;

	  myServiceManagerProxy = new BaseServiceManagerProxy(this, proc) {

	      // Look up the actual remote object in the RMI Registry
	      private ServiceManagerRMI remoteSvcMgr = (ServiceManagerRMI)Naming.lookup(svcMgrName);

	      public String getPlatformName() throws IMTPException {
		  try {
		      return remoteSvcMgr.getPlatformName();
		  }
		  catch(RemoteException re) {

		      while(reconnect()) {
			  try {
			      return remoteSvcMgr.getPlatformName();
			  }
			  catch(RemoteException re2) {
			      // Ignore it and just try reconnecting again...
			  }
		      }

		      throw new IMTPException("An RMI exception occurred", re);
		  }
	      }

	      public String getLocalAddress() throws IMTPException {
		  return baseRMI;
	      }

	      protected String addRemoteNode(NodeDescriptor desc, ServiceDescriptor[] services) throws IMTPException, ServiceException, AuthException {

		  String[] svcNames = new String[services.length];
		  Class[] svcInterfaces = new Class[services.length];

		  // Fill the parameter arrays
		  for(int i = 0; i < services.length; i++) {
		      svcNames[i] = services[i].getName();
		      svcInterfaces[i] = services[i].getService().getHorizontalInterface();
		  }

		  try {
		      // Now register this node and all its services with the Service Manager
		      return remoteSvcMgr.addNode(desc, svcNames, svcInterfaces, true);
		  }
		  catch(RemoteException re) {

		      while(reconnect()) {
			  try {
			      return remoteSvcMgr.addNode(desc, svcNames, svcInterfaces, true);
			  }
			  catch(RemoteException re2) {
			      // Ignore it and just try reconnecting again...
			  }
		      }

		      throw new IMTPException("An RMI exception occurred", re);
		  }
	      }

	      protected void removeRemoteNode(NodeDescriptor desc) throws IMTPException, ServiceException {
		  try {
		      // First, deregister this node with the service manager
		      remoteSvcMgr.removeNode(desc, true);
		  }
		  catch(RemoteException re) {

		      while(reconnect()) {
			  try {
			      remoteSvcMgr.removeNode(desc, true);
			      return;
			  }
			  catch(RemoteException re2) {
			      // Ignore it and just try reconnecting again...
			  }
		      }

		      throw new IMTPException("RMI exception", re);
		  }
	      }

	      protected void addRemoteSlice(String svcName, Class itf, NodeDescriptor where) throws IMTPException, ServiceException {
		  try {
		      remoteSvcMgr.activateService(svcName, itf, where, true);
		  }
		  catch(RemoteException re) {

		      while(reconnect()) {
			  try {
			      remoteSvcMgr.activateService(svcName, itf, where, true);
			      return;
			  }
			  catch(RemoteException re2) {
			      // Ignore it and just try reconnecting again...
			  }
		      }

		      throw new IMTPException("An RMI exception was thrown", re);
		  }
	      }

	      protected void removeRemoteSlice(String svcName, NodeDescriptor where) throws IMTPException, ServiceException {
		  try {
		      remoteSvcMgr.deactivateService(svcName, where, true);
		  }
		  catch(RemoteException re) {

		      while(reconnect()) {
			  try {
			      remoteSvcMgr.deactivateService(svcName, where, true);
			      return;
			  }
			  catch(RemoteException re2) {
			      // Ignore it and just try reconnecting again...
			  }
		      }

		      throw new IMTPException("An RMI exception was thrown", re);
		  }
	      }

	      protected Node findSliceNode(String serviceKey, String sliceKey) throws IMTPException, ServiceException {
		  try {
			  return remoteSvcMgr.findSliceNode(serviceKey, sliceKey);
		  }
		  catch(RemoteException re) {

		      while(reconnect()) {
			  try {
			      return remoteSvcMgr.findSliceNode(serviceKey, sliceKey);
			  }
			  catch(RemoteException re2) {
			      // Ignore it and just try reconnecting again...
			  }
		      }

		      throw new IMTPException("An RMI exception was thrown", re);
		  }
	      }

	      protected Node[] findAllNodes(String serviceKey) throws IMTPException, ServiceException {
		  try {
		      Node[] result = remoteSvcMgr.findAllNodes(serviceKey);

		      return result;
		  }
		  catch(RemoteException re) {

		      while(reconnect()) {
			  try {
			      return remoteSvcMgr.findAllNodes(serviceKey);
			  }
			  catch(RemoteException re2) {
			      // Ignore it and just try reconnecting again...
			  }
		      }

		      throw new IMTPException("An RMI exception was thrown", re);
		  }
	      }

	      // Scan the address list and try to connect once again
	      private boolean reconnect() throws IMTPException {
		  String[] addrs = getAddresses();
		  for(int i = 0; i < addrs.length; i++) {
		      try {
			  remoteSvcMgr = (ServiceManagerRMI)Naming.lookup(addrs[i] + SERVICE_MANAGER_NAME);
			  remoteSvcMgr.adopt(localNode);
			  baseRMI = addrs[i];
			  return true;
		      }
		      catch(Exception e) {
			  // Ignore it and try the next address...
		      }
		  }

		  return false;
	      }


	  };

	  // Add the initial addresses to the proxy
	  myServiceManagerProxy.addAddress(baseRMI);

	  // Add all the additional addresses to the Service Manager Proxy...
	  List smAddrs = myProfile.getSpecifiers(Profile.REMOTE_SERVICE_MANAGER_ADDRESSES);
	  Iterator smIt = smAddrs.iterator();
	  while(smIt.hasNext()) {
	      Specifier spec = (Specifier)smIt.next();
	      String smAddr = "rmi://" + spec.toString() + "/";
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

  public void connect(ContainerID id) throws IMTPException {
      // Create and export the RMI endpoint for this container
      String containerName = id.getName();
      localNode.setName(containerName);
  }

  public void disconnect(ContainerID id) throws IMTPException {
      // Simply exit the local node...
      localNode.exit();
  }

  public Node getLocalNode() throws IMTPException {
      return localNode;
  }

  /**
   */
  public void shutDown() {
  }

  /**
   */
  public List getLocalAddresses() throws IMTPException {
    try {
      List l = new LinkedList();
      // The port is meaningful only on the Main container
      TransportAddress addr = new RMIAddress(InetAddress.getLocalHost().getHostName(), String.valueOf(localPort), null, null);
      l.add(addr);
      return l;
    }
    catch (Exception e) {
      throw new IMTPException("Exception in reading local addresses", e);
    }
  }

    public ServiceManagerRMI getRemoteServiceManager(String addr) {
	synchronized(remoteServiceManagers) {
	    return (ServiceManagerRMI)remoteServiceManagers.get(addr);
	}
    }



    /**
       Creates the client socket factory, which will be used
       to instantiate a <code>UnicastRemoteObject</code>.
       @return The client socket factory.
    */
    public RMIClientSocketFactory getClientSocketFactory() {
 	return null;
    }
 
    /**
       Creates the server socket factory, which will be used
       to instantiate a <code>UnicastRemoteObject</code>.
       @return The server socket factory.
    */
    public RMIServerSocketFactory getServerSocketFactory() { 
 	return null;
    }


}
