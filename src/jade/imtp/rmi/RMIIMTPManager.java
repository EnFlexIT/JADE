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

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;

import java.net.InetAddress;
import java.net.UnknownHostException;

import java.io.IOException;
import java.io.Serializable;

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

  // A smart proxy class for the real Service Manager and Service Finder implementation
  private class ServiceManagerProxy implements ServiceManager, ServiceFinder {

      public ServiceManagerProxy(ServiceManagerRMI sm, CommandProcessor proc) {
	  myRemoteImpl = sm;
	  myCommandProcessor = proc;
	  services = new HashMap();
      }


      public String getPlatformName() throws IMTPException {
	  try {
	      return myRemoteImpl.getPlatformName();
	  }
	  catch(RemoteException re) {
	      throw new IMTPException("An RMI exception occurred", re);
	  }
      }

      public void addNode(NodeDescriptor desc, ServiceDescriptor[] services) throws IMTPException, ServiceException, AuthException {

	  try {
	      String name = desc.getName();
	      NodeAdapter localNode = (NodeAdapter)desc.getNode();
	      ContainerID cid = desc.getContainer();
	      String[] svcNames = new String[services.length];
	      Class[] svcInterfaces = new Class[services.length];

	      // Fill the parameter arrays
	      for(int i = 0; i < services.length; i++) {
		  svcNames[i] = services[i].getName();
		  svcInterfaces[i] = services[i].getService().getHorizontalInterface();
	      }

	      // Now register this node and all its services with the Service Manager
	      String containerName = myRemoteImpl.addNode(desc, svcNames, svcInterfaces);

	      if(cid != null) {
		  cid.setName(containerName);
		  connect(cid);
	      }

	      List failedServices = new LinkedList();
	      for(int i = 0; i < services.length; i++) {
		  // Install the local component of the new service
		  ServiceDescriptor svcDesc = services[i];
		  String svcName = svcDesc.getName();
		  Class svcInterface = svcDesc.getService().getHorizontalInterface();
		  try {
		      installServiceLocally(svcName, svcDesc.getService());
		  }
		  catch(IMTPException imtpe) {
		      // Undo the local service installation
		      failedServices.add(desc.getName());
		      uninstallServiceLocally(desc.getName());
		  }
	      }

	      // Throw a failure exception, if needed
	      if(!failedServices.isEmpty()) {

		  // All service activations failed: throw a single exception 
		  if(failedServices.size() == services.length) {
		      throw new ServiceException("Total failure in locally installing the services");
		  }
		  else {

		      // Only some service activations failed: throw a single exception with the list of the failed services
		      Iterator it = failedServices.iterator();
		      String names = "[ ";
		      while(it.hasNext()) {
			  names = names.concat((String)it.next() + " ");		
		      }
		      names = names.concat("]");
		      throw new ServiceException("Partial failure in locally installing the services " + names);
		  }
	      }

	  }
	  catch(RemoteException re) {
	      throw new IMTPException("An RMI exception was thrown", re);
	  }
      }

      public void removeNode(NodeDescriptor desc) throws IMTPException, ServiceException {
	  String name = desc.getName();
	  NodeAdapter localNode = (NodeAdapter)desc.getNode();

	  try {
	      // First, deregister this node with the service manager
	      myRemoteImpl.removeNode(desc);
	  }
	  catch(RemoteException re) {
	      throw new IMTPException("RMI exception", re);
	  }

	  // Then, locally deactivate all the services
	  Object[] names = services.keySet().toArray();
	      for(int i = 0; i < names.length; i++) {
		  try {
		      String svcName = (String)names[i];
		      uninstallServiceLocally(svcName);
		  }
		  catch(IMTPException imtpe) {
		      // This should never happen, because it's a local call...
		      imtpe.printStackTrace();
		  }
	      }


      }

      public void activateService(ServiceDescriptor desc) throws IMTPException, ServiceException {
	  try {

	      String name = desc.getName();
	      Service svc = desc.getService();

	      // Install the local component of the new service
	      try {
		  installServiceLocally(name, svc);
	      }
	      catch(IMTPException imtpe) {
		  // Undo the local service installation
		  uninstallServiceLocally(name);
	      }

	      myRemoteImpl.activateService(name, svc.getHorizontalInterface(), localNode.getName(), localNode.getRMIStub());
	  }
	  catch(RemoteException re) {
	      throw new IMTPException("An RMI exception was thrown", re);
	  }
      }

      public void deactivateService(ServiceDescriptor desc) throws IMTPException, ServiceException {
	  try {

	      String name = desc.getName();

	      myRemoteImpl.deactivateService(name, localNode.getRMIStub());

	      // Uninstall the local component of the service
	      uninstallServiceLocally(name);

	  }
	  catch(RemoteException re) {
	      throw new IMTPException("An RMI exception was thrown", re);
	  }
      }

      public Service findService(String key) throws IMTPException, ServiceException {
	  return (Service)services.get(key);
      }

      public Service.Slice findSlice(String serviceKey, String sliceKey) throws IMTPException, ServiceException {
	  try {

	      // FIXME: It should not be needed, and the horizontal interface should be returned from the remote end
	      Service localService = findService(serviceKey);
	      if(localService == null) {
		  // FIXME: Should install a DummyService...
		  throw new ServiceException("The service <" + serviceKey + "> is not available on node <" + localNode.getName() + ">");
	      }

	      NodeAdapter node = myRemoteImpl.findSliceNode(serviceKey, sliceKey);
	      return createSliceProxy(serviceKey, localService.getHorizontalInterface(), node);
	  }
	  catch(RemoteException re) {
	      throw new IMTPException("An RMI exception was thrown", re);
	  }
      }

      public Service.Slice[] findAllSlices(String serviceKey) throws IMTPException, ServiceException {
	  try {

	      // FIXME: It should not be needed, and the horizontal interface should be returned from the remote end
	      Service localService = findService(serviceKey);
	      if(localService == null) {
		  // FIXME: Should install a DummyService...
		  throw new ServiceException("The service <" + serviceKey + "> is not available on node <" + localNode.getName() + ">");
	      }

	      NodeAdapter[] nodes = myRemoteImpl.findAllNodes(serviceKey);
	      Class itf = localService.getHorizontalInterface();
	      Service.Slice[] result = new Service.Slice[nodes.length];
	      for(int i = 0; i < nodes.length; i++) {
		  result[i] = createSliceProxy(serviceKey, itf, nodes[i]);
	      }

	      return result;
	  }
	  catch(RemoteException re) {
	      throw new IMTPException("An RMI exception was thrown", re);
	  }
      }


      // Private helper method, common to one-shot and batch service activation
      private void installServiceLocally(String name, Service svc) throws IMTPException {
	  // Install the service filter
	  Filter f = svc.getCommandFilter();
	  myCommandProcessor.addFilter(f);

	  // Export the local slice so that it can be reached through the network
	  Service.Slice localSlice = svc.getLocalSlice();
	  if(localSlice != null) {
	      exportSlice(name, localSlice);
	  }

	  // Add the service to the local service finder so that it can be found
	  services.put(svc.getName(), svc);
      }

      // Private helper method, common to one-shot and batch service deactivation
      private void uninstallServiceLocally(String name) throws IMTPException {
	  // FIXME: It should remove the service only if there are no more active slices in the whole platform.
	  Service svc = (Service)services.get(name); // Find the local copy of the service
	  services.remove(name);
	  unexportSlice(name, svc.getLocalSlice());

	  // Uninstall the service filter
	  Filter f = svc.getCommandFilter();
	  myCommandProcessor.removeFilter(f);
      }


      private Map services;
      private ServiceManagerRMI myRemoteImpl;
      private CommandProcessor myCommandProcessor;

  } // End of ServiceManagerProxy class




  private static final int DEFAULT_RMI_PORT = 1099;


  private Profile myProfile;
  private String mainHost;
  private int mainPort;
  private String baseRMI;
  private String platformRMI;
  private NodeAdapter localNode;
  private ServiceManagerProxy myServiceManagerProxy;

  // Maps agent containers into their stubs
  private Map stubs;

  public RMIIMTPManager() {
      try {
	  localNode = new NodeAdapter("No-Name");
	  stubs = new HashMap();
      }
      catch(RemoteException re) {
	  re.printStackTrace();
      }
  }

  /**
   */
  public void initialize(Profile p) throws IMTPException {
      myProfile = p;
      mainHost = myProfile.getParameter(Profile.MAIN_HOST, null);
      if (mainHost == null) {
      	// Use the local host by default
      	try {
	  			mainHost= InetAddress.getLocalHost().getHostName();      
      	} 
      	catch(UnknownHostException uhe) {
      		throw new IMTPException("Unknown main host");
      	}
      }
      
      mainPort = DEFAULT_RMI_PORT;
      String mainPortStr = myProfile.getParameter(Profile.MAIN_PORT, null);
      if (mainPortStr != null) {
      	try {
      		mainPort = Integer.parseInt(mainPortStr);
      	}
      	catch (NumberFormatException nfe) {
      		// Do nothing. The DEFAULT_RMI_PORT is used 
      	}
      }

      baseRMI = "rmi://" + mainHost + ":" + mainPort + "/";
      platformRMI = baseRMI + "JADE";

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

      String svcMgrName = baseRMI + SERVICE_MANAGER_NAME;
      ServiceManagerRMI smRMI = new ServiceManagerRMIImpl((ServiceManagerImpl)sm, this);
      Registry theRegistry = getRmiRegistry(null, mainPort);
      Naming.bind(svcMgrName, smRMI);
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
    catch(Exception e) {
      throw new IMTPException("Problem starting JADE Runtime System.", e);
    }
  }


  public ServiceManager createServiceManagerProxy(CommandProcessor proc) throws IMTPException {
      try {
	  String svcMgrName = baseRMI + SERVICE_MANAGER_NAME;

	  // Look up the actual remote object in the RMI Registry
	  final ServiceManagerRMI remoteSvcMgr = (ServiceManagerRMI)Naming.lookup(svcMgrName);

	  final CommandProcessor myCommandProcessor = proc;

	  myServiceManagerProxy = new ServiceManagerProxy(remoteSvcMgr, proc);
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

	  ClassLoader myCL = getClass().getClassLoader();

	  if(itf == null) {
	      throw new IMTPException("No proxy interface specified");
	  }

	  // Make sure that the first element of the array is a sub-interface of Service.Slice
	  if(!itf.isInterface() || !Service.Slice.class.isAssignableFrom(itf)) {
	      throw new IMTPException("The first proxy interface must extend Service.Slice [" + itf.getName() + "]");
	  }

	  // Recover the RMI stub from the node where the slice represented by this proxy resides.
	  NodeAdapter adapter = (NodeAdapter)where;
	  final NodeRMI target = adapter.getRMIStub();
	  final String svcName = serviceName;
	  final Class intface = itf;

	  // Build and return a Dynamic Proxy for the remote Slice
	  return (Service.Slice)Proxy.newProxyInstance(myCL, new Class[] {itf}, new InvocationHandler() {

	      // Reflective invocation handler, creating a
	      // GenericCommand object from the method name and
	      // parameters, and then dispatching it to the remote
	      // slice over the NodeRMI remote interface, wrapping any
	      // thrown RemoteException into an IMTPException.
	      //
	      // Notice: For this proxy to work, every parameter must be Serializable.
	      //
	      public Object invoke(Object proxy, Method meth, Object[] args) throws Throwable {

		  try {
		      GenericCommand cmd = new GenericCommand(meth.getName(), svcName, "");
		      if(args != null) {
			  for(int i = 0; i < args.length; i++) {
			      cmd.addParam(args[i]);
			  }
		      }

		      Class[] classes = meth.getParameterTypes();
		      Class retType = meth.getReturnType();
		      Object result = target.accept(cmd, intface, classes);

		      if(result == null) {
			  return result;
		      }

		      // Check for:
		      // a. A legitimate result, instance of the declared return type
		      // b. A thrown exception of a declared type
		      // c. A thrown exception of an unknown type
		      // The 'isPrimitive()' clause is needed to cope with wrapper types (e.g. boolean vs. java.lang.Boolean)
		      if(retType.isInstance(result) || (retType.isPrimitive() && !(result instanceof Throwable))) {
			  return result;
		      }
		      else if(result instanceof Throwable) {
			  throw (Throwable)result;
		      }
		      else {
			  throw new IMTPException("Incorrect type returned from a remote call: " + result.getClass().getName() + " [expected " + retType.getName() + " ]");
		      }

		  }
		  catch(RemoteException re) {
		      // Convert a RemoteException into an IMTPException, and let all other exceptions through
		      throw new IMTPException("An RMI Exception was thrown", re);
		  }
	      }
	  });

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
      TransportAddress addr = new RMIAddress(InetAddress.getLocalHost().getHostName(), String.valueOf(mainPort), null, null);
      l.add(addr);
      return l;
    }
    catch (Exception e) {
      throw new IMTPException("Exception in reading local addresses", e);
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
