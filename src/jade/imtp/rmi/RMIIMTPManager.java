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

import jade.util.leap.List;
import jade.util.leap.LinkedList;

import jade.security.AuthException;

import jade.core.*;
import jade.lang.acl.ACLMessage;
import jade.mtp.TransportAddress;


/**
 * @author Giovanni Caire - Telecom Italia Lab
 * @author Giovanni Rimassa - FRAMeTech s.r.l.
 */
public class RMIIMTPManager implements IMTPManager {


  private static final int DEFAULT_RMI_PORT = 1099;


  private Profile myProfile;
  private String mainHost;
  private int mainPort;
  private String baseRMI;
  private String platformRMI;
  private NodeAdapter localNode;
  private BaseServiceManagerProxy myServiceManagerProxy;
  private ServiceManagerRMIImpl myRMIServiceManager;

  public RMIIMTPManager() {
      try {
	  localNode = new NodeAdapter("No-Name");
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
      myRMIServiceManager = new ServiceManagerRMIImpl((ServiceManagerImpl)sm, this);
      Registry theRegistry = getRmiRegistry(null, mainPort);
      Naming.bind(svcMgrName, myRMIServiceManager);
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

  public ServiceManager createServiceManagerProxy(CommandProcessor proc) throws IMTPException {
      try {
	  String svcMgrName = baseRMI + SERVICE_MANAGER_NAME;

	  // Look up the actual remote object in the RMI Registry
	  final ServiceManagerRMI remoteSvcMgr = (ServiceManagerRMI)Naming.lookup(svcMgrName);

	  myServiceManagerProxy = new BaseServiceManagerProxy(this, proc) {

	      public String getPlatformName() throws IMTPException {
		  try {
		      return remoteSvcMgr.getPlatformName();
		  }
		  catch(RemoteException re) {
		      throw new IMTPException("An RMI exception occurred", re);
		  }
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
		      return remoteSvcMgr.addNode(desc, svcNames, svcInterfaces);
		  }
		  catch(RemoteException re) {
		      throw new IMTPException("An RMI exception occurred", re);
		  }
	      }

	      protected void removeRemoteNode(NodeDescriptor desc) throws IMTPException, ServiceException {
		  try {
		      // First, deregister this node with the service manager
		      remoteSvcMgr.removeNode(desc);
		  }
		  catch(RemoteException re) {
		      throw new IMTPException("RMI exception", re);
		  }
	      }

	      protected void addRemoteSlice(String svcName, Class itf, NodeDescriptor where) throws IMTPException, ServiceException {
		  try {
		      remoteSvcMgr.activateService(svcName, itf, where);
		  }
		  catch(RemoteException re) {
		      throw new IMTPException("An RMI exception was thrown", re);
		  }
	      }

	      protected void removeRemoteSlice(String svcName, NodeDescriptor where) throws IMTPException, ServiceException {
		  try {
		      remoteSvcMgr.deactivateService(svcName, where);
		  }
		  catch(RemoteException re) {
		      throw new IMTPException("An RMI exception was thrown", re);
		  }
	      }

	      protected Node findSliceNode(String serviceKey, String sliceKey) throws IMTPException, ServiceException {
		  try {
		      return remoteSvcMgr.findSliceNode(serviceKey, sliceKey);
		  }
		  catch(RemoteException re) {
		      throw new IMTPException("An RMI exception was thrown", re);
		  }
	      }

	      protected Node[] findAllNodes(String serviceKey) throws IMTPException, ServiceException {
		  try {
		      return remoteSvcMgr.findAllNodes(serviceKey);
		  }
		  catch(RemoteException re) {
		      throw new IMTPException("An RMI exception was thrown", re);
		  }
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
