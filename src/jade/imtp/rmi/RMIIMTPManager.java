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

import jade.util.leap.List;
import jade.util.leap.LinkedList;

import jade.core.*;
import jade.security.JADESecurityException;
import jade.mtp.TransportAddress;
import jade.util.Logger;

import java.util.Vector;

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
    //private String baseRMI;
  	
    private String localAddr;


    // The RMI URL where the original (i.e. the first replica) Service
    // Manager is to be found.
    //private String originalRMI;
  	
  	private String originalPMAddr;

    private NodeAdapter localNode;
    private ServiceManagerRMIImpl myRMIServiceManager;
    
    private Logger myLogger = Logger.getMyLogger(getClass().getName());

  public RMIIMTPManager() {
  }

  /**
   */
  public void initialize(Profile p) throws IMTPException {
      myProfile = p;
      
      // 1) Get MAIN host and port (defaults to localhost and 1099)
      mainHost = myProfile.getParameter(Profile.MAIN_HOST, null);
      
      mainPort = DEFAULT_RMI_PORT;
      String mainPortStr = myProfile.getParameter(Profile.MAIN_PORT, null);
      try {
	  mainPort = Integer.parseInt(mainPortStr);
      }
      catch (Exception e) {
	  		// Use default 
      }

      // 2) Get LOCAL host and port (defaults to main-host and main-port if 
      // this is a sole main container, localhost and 1099 otherwise)
      if(myProfile.getBooleanProperty(Profile.MAIN, true) && !myProfile.getBooleanProperty(Profile.LOCAL_SERVICE_MANAGER, false)) {
      	localHost = myProfile.getParameter(Profile.LOCAL_HOST, mainHost);
      	localPort = mainPort;
      }
      else {
      	localHost = myProfile.getParameter(Profile.LOCAL_HOST, null);
      	localPort = DEFAULT_RMI_PORT;
      }
      String localPortStr = myProfile.getParameter(Profile.LOCAL_PORT, null);
      try {
	  localPort = Integer.parseInt(localPortStr);
      }
      catch (Exception e) {
	  		// Use default 
      }

      // 3) Get ServiceManager-LOCAL host and port (defaults to localhost and 0)
      localSvcMgrHost = myProfile.getParameter(LOCAL_SERVICE_MANAGER_HOST, null);

      localSvcMgrPort = 0;
      String localSvcMgrPortStr = myProfile.getParameter(LOCAL_SERVICE_MANAGER_PORT, null);
		  try {
		      localSvcMgrPort = Integer.parseInt(localSvcMgrPortStr);
		  }
		  catch(NumberFormatException nfe) {
	  		// Use default 
		  }

      // 3) Get Node-LOCAL host and port (defaults to local-host and local-port if 
		  // this is a peripheral container, localhost and 0 otherwise)
      if (!myProfile.getBooleanProperty(Profile.MAIN, true)) {
      	localNodeHost = myProfile.getParameter(LOCAL_NODE_HOST, localHost);
      	localNodePort = localPort;
      }
      else {
	      localNodeHost = myProfile.getParameter(LOCAL_NODE_HOST, null);
	      localNodePort = 0;
      }
      String localNodePortStr = myProfile.getParameter(LOCAL_NODE_PORT, null);
		  try {
		      localNodePort = Integer.parseInt(localNodePortStr);
		  }
		  catch(Exception e) {
	  		// Use default 
		  }


		  // Initialize the local and (if necessary) originalPM addresses
  		localAddr = "rmi://" + localHost + ":" + localPort + "/";
      // If this is a backup main, initialize the address of the original PlatformManager 
      if(myProfile.getBooleanProperty(Profile.MAIN, true) && myProfile.getBooleanProperty(Profile.LOCAL_SERVICE_MANAGER, false)) {
			  originalPMAddr = "rmi://" + mainHost + ":" + mainPort + "/";
      }
      
      if (myLogger.isLoggable(Logger.CONFIG)) {
      	myLogger.log(Logger.CONFIG, "IMTP parameters: main-host = "+mainHost+" main-port = "+mainPort+" local-port = "+localPort+" sm-port = "+localSvcMgrPort+" node-port = "+localNodePort);
      }
      
		  // Create the local node and (if needed) mark it as hosting a local Service Manager
      try {
	  localNode = new NodeAdapter(PlatformManager.NO_NAME, myProfile.getBooleanProperty(Profile.MAIN, true), localNodePort, this);
      }
      catch(RemoteException re) {
	  throw new IMTPException("An RMI error occurred", re);
      }
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


  public void exportPlatformManager(PlatformManager mgr) throws IMTPException {
    try {
      String svcMgrName = localAddr + SERVICE_MANAGER_NAME;
      mgr.setLocalAddress(localAddr);
      myRMIServiceManager = new ServiceManagerRMIImpl(mgr, this, localSvcMgrPort);

      Registry theRegistry = getRmiRegistry(null, localPort);
      Naming.bind(svcMgrName, myRMIServiceManager);

      // Attach to the original Platform manager, if any
      if (originalPMAddr != null) {
	      PlatformManager originalPM = getPlatformManagerProxy(originalPMAddr);
		  	try {
		  		((PlatformManagerImpl) mgr).setPlatformName(originalPM.getPlatformName());
			  	mgr.addReplica(originalPMAddr, true); // Do as if it was a propagated info
			    originalPM.addReplica(localAddr, false);
		  	}
		  	catch (ServiceException se) {
		  		throw new IMTPException("Cannot attach to the original PlatformManager.", se);
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


  public void unexportPlatformManager(PlatformManager mgr) throws IMTPException {
  	if (myRMIServiceManager != null && myRMIServiceManager.getPlatformManager().equals(mgr)) {
  		// Unexport the PlatformManager we are currently exporting
		  try {
	      // Remove the RMI remote object from the RMI registry
	      String svcMgrName = localAddr + SERVICE_MANAGER_NAME;
	      Naming.unbind(svcMgrName);
	      // Disconnect it from the network
	      myRMIServiceManager.unexportObject(myRMIServiceManager, true);
		  }
		  catch(Exception e) {
	      throw new IMTPException("Error in unexporting the RMI Service Manager", e);
		  }
    }
    else { 
		  // We are not exporting this PlatformManager --> Do Nothing...
    }
  }


  public PlatformManager getPlatformManagerProxy() throws IMTPException {
  	return getPlatformManagerProxy("rmi://" + mainHost + ":" + mainPort + "/");
  }

  public PlatformManager getPlatformManagerProxy(String addr) throws IMTPException {
    try {
		  String pmName = addr + SERVICE_MANAGER_NAME;
		  ServiceManagerRMI sm = (ServiceManagerRMI)Naming.lookup(pmName);
		  return new PlatformManagerAdapter(sm, addr);
    }
    catch(Exception e) {
		  throw new IMTPException("Can't get a proxy to the PlatformManager at address "+addr, e);
    }
  }

  public void reconnected(PlatformManager pm) {
  	// Just do nothing
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

  public Node getLocalNode() throws IMTPException {
      return localNode;
  }

  /**
   */
  public void shutDown() {
  	try {
      localNode.exit();
  	}
  	catch (IMTPException imtpe) {
  		// Should never happen since this is a local call
  		imtpe.printStackTrace();
  	}
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

    
    
    
  /**
     Inner class PlatformManagerAdapter.
     An adapter, implementing the PlatformManager interface, to 
     a ServiceManagerRMI stub
   */
	private class PlatformManagerAdapter implements PlatformManager {
		private String localAddress;
		private ServiceManagerRMI adaptee;
		
		private PlatformManagerAdapter(ServiceManagerRMI adaptee, String localAddress) {
			this.localAddress = localAddress;
			this.adaptee = adaptee;
		}
		
    public String getPlatformName() throws IMTPException {
		 	try {
			    return adaptee.getPlatformName();
			}
			catch(RemoteException re) {
			    throw new IMTPException("RMI exception", re);
			}
		}
    
	  public String getLocalAddress() {
	  	return localAddress;
	  }
	  	
	  public void setLocalAddress(String addr) {
	  	// Should never be called
	  }
	  
    public String addNode(NodeDescriptor dsc, Vector nodeServices, boolean propagated) throws IMTPException, ServiceException, JADESecurityException {
		 	try {
			    return adaptee.addNode(dsc, nodeServices, propagated);
			}
			catch(RemoteException re) {
			    throw new IMTPException("RMI exception", re);
			}
    }
    
    public void removeNode(NodeDescriptor dsc, boolean propagated) throws IMTPException, ServiceException {
		 	try {
			    adaptee.removeNode(dsc, propagated);
			}
			catch(RemoteException re) {
			    throw new IMTPException("RMI exception", re);
			}
    }
    
    public void addSlice(ServiceDescriptor service, NodeDescriptor dsc, boolean propagated)  throws IMTPException, ServiceException {
		 	try {
			    adaptee.addSlice(service, dsc, propagated);
			}
			catch(RemoteException re) {
			    throw new IMTPException("RMI exception", re);
			}
    }
    
    public void removeSlice(String serviceKey, String sliceKey, boolean propagated)  throws IMTPException, ServiceException {
		 	try {
			    adaptee.removeSlice(serviceKey, sliceKey, propagated);
			}
			catch(RemoteException re) {
			    throw new IMTPException("RMI exception", re);
			}
    }
    
    public void addReplica(String newAddr, boolean propagated)  throws IMTPException, ServiceException {
		 	try {
			    adaptee.addReplica(newAddr, propagated);
			}
			catch(RemoteException re) {
			    throw new IMTPException("RMI exception", re);
			}
    }
    
    public void removeReplica(String address, boolean propagated)  throws IMTPException, ServiceException {
		 	try {
			    adaptee.removeReplica(address, propagated);
			}
			catch(RemoteException re) {
			    throw new IMTPException("RMI exception", re);
			}
    }
    
    public Service.Slice findSlice(String serviceKey, String sliceKey) throws IMTPException, ServiceException {
    	try {
			    return adaptee.findSlice(serviceKey, sliceKey);
			}
			catch(RemoteException re) {
			    throw new IMTPException("RMI exception", re);
			}
    }
    
    public Vector findAllSlices(String serviceKey) throws IMTPException, ServiceException {
		 	try {
			    return adaptee.findAllSlices(serviceKey);
			}
			catch(RemoteException re) {
			    throw new IMTPException("RMI exception", re);
			}
    }
    
    public void adopt(Node n, Node[] children) throws IMTPException {
		 	try {
			    adaptee.adopt(n, children);
			}
			catch(RemoteException re) {
			    throw new IMTPException("RMI exception", re);
			}
    }
    
    public void ping() throws IMTPException {
		 	try {
			    adaptee.ping();
			}
			catch(RemoteException re) {
			    throw new IMTPException("RMI exception", re);
			}
    }
	} // END of inner class PlatformManagerAdapter
}
