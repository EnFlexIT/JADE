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

//#APIDOC_EXCLUDE_FILE
//#MIDP_EXCLUDE_FILE


import jade.core.behaviours.Behaviour;

import jade.security.JADESecurityException;
import jade.security.JADEPrincipal;
import jade.security.Credentials;
import jade.mtp.TransportAddress;

import jade.util.leap.Map;
import jade.util.leap.HashMap;
import jade.util.leap.List;
import jade.util.leap.ArrayList;
import jade.util.leap.Iterator;
import jade.util.Logger;

import java.util.Vector;

/**

   The <code>ServiceManagerImpl</code> class is the actual
   implementation of JADE platform <i>Service Manager</i> and
   <i>Service Finder</i> components. It holds a set of services and
   manages them.

   @author Giovanni Caire - TILAB
   @author Giovanni Rimassa - FRAMeTech s.r.l.
*/
public class PlatformManagerImpl implements PlatformManager {
    private IMTPManager myIMTPManager;
    private CommandProcessor myCommandProcessor;

    // FIXME: The association between MainContainer and PlatformManagerImpl need be clarified...
    private MainContainerImpl myMain;

    private Map services;
    private Map replicas;

    private String localAddr;
    private String platformID;

    // These variables hold two progressive numbers just used to name new nodes.
    // By convention, nodes with a local copy of the Service Manager are called
    // Main-Container-<N>, whereas nodes without their own Service Manager are
    // called Container-<M>.
    private int nodeNo = 1;
    private int mainNodeNo = 0;

    //private jade.util.Logger myLogger;
    private Logger myLogger = Logger.getMyLogger(this.getClass().getName());

		/**
		   Private class ServiceEntry.
		 */
    private class ServiceEntry {

	public ServiceEntry(Service s) {
	    myService = s;
	    slices = new HashMap();
	}

	public void addSlice(String name, Service.Slice s, Node n) {
	    SliceEntry e = new SliceEntry(s, n);
	    slices.put(name, e);
	}

	public Service.Slice removeSlice(String name) {
	    SliceEntry e = (SliceEntry)slices.remove(name);
	    if(e == null) {
				return null;
	    }
	    else {
				return e.getSlice();
	    }
	}

	public Vector getSlices() {
	    Iterator sliceEntries = slices.values().iterator();
	    Vector result = new Vector();

	    while (sliceEntries.hasNext()) {
		SliceEntry e = (SliceEntry) sliceEntries.next();
		result.addElement(e.getSlice());
	    }

	    return result;
	}

	public Service.Slice getSlice(String name) {
	    SliceEntry e = (SliceEntry)slices.get(name);
	    if(e == null) {
		return null;
	    }
	    else {
		return e.getSlice();
	    }
	}

	public Node[] getNodes() {
	    Object[] sliceEntries = slices.values().toArray();
	    Node[] result = new Node[sliceEntries.length];

	    for(int i = 0; i < result.length; i++) {
		SliceEntry e = (SliceEntry)sliceEntries[i];
		result[i] = e.getNode();
	    }

	    return result;
	}

	public Node getNode(String name) {
	    SliceEntry e = (SliceEntry)slices.get(name);
	    if(e == null) {
		return null;
	    }
	    else {
		return e.getNode();
	    }
	}

	public void setService(Service svc) {
	    myService = svc;
	}

	public Service getService() {
	    return myService;
	}


	private Service myService;
	private Map slices;

    } // End of ServiceEntry class


    /**
       Inner class SliceEntry
     */
    private class SliceEntry {

	public SliceEntry(Service.Slice s, Node n) {
	    mySlice = s;
	    myNode = n;
	}

	public Service.Slice getSlice() {
	    return mySlice;
	}

	public Node getNode() {
	    return myNode;
	}


	private Service.Slice mySlice;
	private Node myNode;

    } // End of SliceEntry class


    /**
       Constructs a new Service Manager implementation complying with
       a given JADE profile. This constructor is package-scoped, so
       that only the JADE kernel is allowed to create a new Service
       Manager implementation.

       @param p The platform profile describing how the JADE platform
       is to be configured.
    */
    PlatformManagerImpl(Profile p) throws ProfileException {
			myCommandProcessor = p.getCommandProcessor();
			myIMTPManager = p.getIMTPManager();
			myMain = new MainContainerImpl(p, this);
			services = new HashMap();
			replicas = new HashMap();

			// Initialize the logger
			//String verbosityKey = "jade_core_PlatformManager_verbosity";
			//String verbosityFormatKey = "jade_core_PlatformManager_verbosity_format";
			//int verbosity = Integer.parseInt(p.getParameter(verbosityKey, "1"));
			//String defaultFormat = (verbosity > 1 ? "%t [%i] %m" : "[%t] %m");
			//String verbosityFormat = p.getParameter(verbosityFormatKey, defaultFormat);
			//myLogger = new jade.util.Logger("PlatformManager", verbosity, null, verbosityFormat);

			platformID = p.getParameter(Profile.PLATFORM_ID, null);
			if (platformID == null || platformID.equals("")) {
			    try {
				// Build the PlatformID using the local host and port
				List l = myIMTPManager.getLocalAddresses();
				TransportAddress localAddr = (TransportAddress) l.get(0);
				platformID = localAddr.getHost() + ":" + localAddr.getPort() + "/JADE";
			    }
			    catch (Exception e) {
				throw new ProfileException("Can't set PlatformID");
			    }
			}
    }

    MainContainerImpl getMain() {
    	return myMain;
    }

    public void setPlatformName(String name) throws IMTPException {
			platformID = name;
    }

    // Implementation of the PlatformManager interface

    public String getPlatformName() throws IMTPException {
			return platformID;
    }

	  public String getLocalAddress() {
	  	return localAddr;
	  }

	  public void setLocalAddress(String addr) {
			localAddr = addr;
	  }

    /**
       @param dsc The Descriptor of the new Node
       @param services The services currently installed on the new Node
       @param propagated Flag indicating whether the new-node event
       was a propagated event within the replication mechanism
     */
    public String addNode(NodeDescriptor dsc, Vector nodeServices, boolean propagated) throws IMTPException, ServiceException, JADESecurityException {
    	String newName = localAddNode(dsc, nodeServices, propagated);
    	if (!propagated) {
    		broadcastAddNode(dsc, nodeServices);
    	}
    	return newName;
    }

    private String localAddNode(NodeDescriptor dsc, Vector nodeServices, boolean propagated) throws IMTPException, ServiceException, JADESecurityException {
    	Node node = dsc.getNode();

			// Adjust node name
			ContainerID cid = dsc.getContainer();
			adjustContainerName(node, cid);
			node.setName(cid.getName());
			dsc.setName(cid.getName());

			// Issue a NEW_NODE vertical command
			if (!propagated) {
				// In this case we issue the command before adding the node
				// for authorization purposes
				GenericCommand gCmd = new GenericCommand(Service.NEW_NODE, null, null);
				gCmd.addParam(dsc);
				Object result = myCommandProcessor.processIncoming(gCmd);
				if (result instanceof JADESecurityException) {
					throw (JADESecurityException) result;
				}
				else if (result instanceof Throwable) {
                                   //myLogger.log("Unexpected error processing NEW_NODE command. Node is "+dsc.getName(), 0);
                                        if (myLogger.isLoggable(Logger.SEVERE))
					    myLogger.log(Logger.SEVERE,"Unexpected error processing NEW_NODE command. Node is "+dsc.getName());
					((Throwable) result).printStackTrace();
				}
			}

			// Add the new node
			if (isLocalNode(node)) {
				// Add the node as a local agent container and activate the new container with the IMTP manager
				myMain.addLocalContainer(dsc);
			}
			else {
				//myLogger.log("Adding node <" + dsc.getName() + "> to the platform", 1);
                                if (myLogger.isLoggable(Logger.INFO))
                                    myLogger.log(Logger.INFO,"Adding node <" + dsc.getName() + "> to the platform");

				// Add the node as a remote agent container
				myMain.addRemoteContainer(dsc);
				if (!propagated) {
			    monitor(node);
				}
			}

			// Add all service slices
			// Do not broadcast since this information is already conveied when broadcasting the add-node event
			for (int i = 0; i < nodeServices.size(); ++i) {
				ServiceDescriptor service = (ServiceDescriptor) nodeServices.elementAt(i);
				localAddSlice(service, dsc, propagated);
			}

			// Return the name given to the new node
			return node.getName();
    }

    private void broadcastAddNode(NodeDescriptor dsc, Vector nodeServices) throws IMTPException, ServiceException {
    	// Avoid concurrent modification exception
    	Object[] rr = replicas.values().toArray();
    	for (int i = 0; i < rr.length; ++i) {
    		PlatformManager replica = (PlatformManager) rr[i];
    		try {
	    		replica.addNode(dsc, nodeServices, true);
    		}
    		catch (IMTPException imtpe) {
    			// Zombie replica. Just remove it
    			localRemoveReplica(replica.getLocalAddress(), true);
    		}
    		catch (JADESecurityException ae) {
    			// Should never happen since this is a propagated info
    			ae.printStackTrace();
    		}
    	}
    }

    public void removeNode(NodeDescriptor dsc, boolean propagated) throws IMTPException, ServiceException {
    	localRemoveNode(dsc, propagated);
  		// If this is the local node the node termination will cause the deregistration...
    	if (!propagated && !isLocalNode(dsc.getNode())) {
    		broadcastRemoveNode(dsc);
    	}
    }

    private void localRemoveNode(NodeDescriptor dsc, boolean propagated) throws IMTPException, ServiceException {
			Node node = dsc.getNode();

			// Remove all the slices corresponding to the removed node
			// Avoid concurrent modification exception
			Object[] allServiceKeys = services.keySet().toArray();
			for(int i = 0; i < allServiceKeys.length; i++) {
		    String serviceKey = (String) allServiceKeys[i];
		    localRemoveSlice(serviceKey, dsc.getName(), propagated);
			}

			// Remove the node
			if(isLocalNode(node)) {
				// As a local container
				myMain.removeLocalContainer(dsc.getContainer());
			}
			else {
				//myLogger.log("Removing node <" + dsc.getName() + "> from the platform", 1);
                                if (myLogger.isLoggable(Logger.INFO))
                                    myLogger.log(Logger.INFO,"Removing node <" + dsc.getName() + "> from the platform");

				// As a remote container
				myMain.removeRemoteContainer(dsc);
			}

			// Issue a DEAD_NODE vertical command
			if (!propagated) {
				GenericCommand gCmd = new GenericCommand(Service.DEAD_NODE, null, null);
				gCmd.addParam(dsc);
				Object result = myCommandProcessor.processIncoming(gCmd);
				if (result instanceof Throwable) {
					//myLogger.log("Unexpected error processing DEAD_NODE command. Node is "+dsc.getName(), 0);
                                        if (myLogger.isLoggable(Logger.SEVERE))
                                            myLogger.log(Logger.SEVERE,"Unexpected error processing DEAD_NODE command. Node is "+dsc.getName());

					((Throwable) result).printStackTrace();
				}
			}
    }

    private void broadcastRemoveNode(NodeDescriptor dsc) throws IMTPException, ServiceException {
    	// Avoid concurrent modification exception
    	Object[] rr = replicas.values().toArray();
    	for (int i = 0; i < rr.length; ++i) {
    		PlatformManager replica = (PlatformManager) rr[i];
    		try {
	    		replica.removeNode(dsc, true);
    		}
    		catch (IMTPException imtpe) {
    			// Zombie replica. Just remove it
    			localRemoveReplica(replica.getLocalAddress(), true);
    		}
    	}
    }

    public void addSlice(ServiceDescriptor service, NodeDescriptor dsc, boolean propagated)  throws IMTPException, ServiceException {
    	localAddSlice(service, dsc, propagated);
    	if (!propagated) {
    		broadcastAddSlice(service, dsc);
    	}
    }

    private void localAddSlice(ServiceDescriptor serviceDsc, NodeDescriptor dsc, boolean propagated)  throws IMTPException, ServiceException {
    	Service service = serviceDsc.getService();

			String serviceKey = service.getName();
    	ServiceEntry e = (ServiceEntry)services.get(serviceKey);

			if(e == null) {
				//myLogger.log("Adding entry for service <" + serviceKey + ">", 3);
                                if (myLogger.isLoggable(Logger.CONFIG))
                                    myLogger.log(Logger.CONFIG,"Adding entry for service <" + serviceKey + ">");

		    e = new ServiceEntry(service);
		    services.put(serviceKey, e);
			}
			//myLogger.log("Adding slice for service <" + serviceKey+"> on node <"+dsc.getName() + ">", 3);
                        if (myLogger.isLoggable(Logger.CONFIG))
                            myLogger.log(Logger.CONFIG,"Adding slice for service <" + serviceKey+"> on node <"+dsc.getName() + ">");


			Node node = dsc.getNode();
			Service.Slice slice = null;
			if (service.getHorizontalInterface() != null) {
				// Create a real SliceProxy
				slice = myIMTPManager.createSliceProxy(serviceKey, service.getHorizontalInterface(), node);
			}
			else {
				// Create a dummy SliceProxy (it will never be used)
				slice = new Service.SliceProxy(service, node);
			}
				
			String sliceKey = node.getName();
			e.addSlice(sliceKey, slice, node);

			if (isLocalNode(node)) {
				// The service is just started on this main container
				// Register the service-specific behaviour (if any) within the AMS
				Behaviour b = service.getAMSBehaviour();
				if(b != null) {
			    myMain.installAMSBehaviour(b);
				}
			}

			if (!propagated) {
				GenericCommand gCmd = new GenericCommand(Service.NEW_SLICE, serviceKey, null);
				gCmd.addParam(sliceKey);
				Object result = myCommandProcessor.processIncoming(gCmd);
				if (result instanceof Throwable) {
					//myLogger.log("Unexpected error processing NEW_SLICE command. Service is "+serviceKey+" node is "+sliceKey, 0);
                                        if (myLogger.isLoggable(Logger.SEVERE))
                                            myLogger.log(Logger.SEVERE,"Unexpected error processing NEW_SLICE command. Service is "+serviceKey+" node is "+sliceKey);

					((Throwable) result).printStackTrace();
				}
			}
    }

    private void broadcastAddSlice(ServiceDescriptor service, NodeDescriptor dsc) throws IMTPException, ServiceException {
    	// Avoid concurrent modification exception
    	Object[] rr = replicas.values().toArray();
    	for (int i = 0; i < rr.length; ++i) {
    		PlatformManager replica = (PlatformManager) rr[i];
    		try {
	    		replica.addSlice(service, dsc, true);
    		}
    		catch (IMTPException imtpe) {
    			// Zombie replica. Just remove it
    			localRemoveReplica(replica.getLocalAddress(), true);
    		}
    	}
    }

    public void removeSlice(String serviceKey, String sliceKey, boolean propagated)  throws IMTPException, ServiceException {
    	localRemoveSlice(serviceKey, sliceKey, propagated);
    	if (!propagated) {
    		broadcastRemoveSlice(serviceKey, sliceKey);
    	}
    }

    private void localRemoveSlice(String serviceKey, String sliceKey, boolean propagated)  throws IMTPException, ServiceException {
			ServiceEntry e = (ServiceEntry)services.get(serviceKey);

			if(e != null) {
				if (e.removeSlice(sliceKey) != null) {
					//myLogger.log("Removing slice for service <" + serviceKey+"> on node <"+sliceKey + ">", 3);
                                        if (myLogger.isLoggable(Logger.CONFIG))
                                            myLogger.log(Logger.CONFIG,"Removing slice for service <" + serviceKey+"> on node <"+sliceKey + ">");

				}

				try {
					Node node = myMain.getContainerNode(new ContainerID(sliceKey, null));
					if (isLocalNode(node)) {
						// The service slice was removed on this node
				    // Deregister the service-specific behaviour (if any) within the AMS
				    Behaviour b = e.getService().getAMSBehaviour();
				    if(b != null) {
							myMain.uninstallAMSBehaviour(b);
				    }
					}
				}
				catch (NotFoundException nfe) {
					// Just do nothing
				}

				if (!propagated) {
					GenericCommand gCmd = new GenericCommand(Service.DEAD_SLICE, serviceKey, null);
					gCmd.addParam(sliceKey);
					Object result = myCommandProcessor.processIncoming(gCmd);
					if (result instanceof Throwable) {
						//myLogger.log("Unexpected error processing DEAD_SLICE command. Service is "+serviceKey+" node is "+sliceKey, 0);
                                                if (myLogger.isLoggable(Logger.SEVERE))
                                                    myLogger.log(Logger.SEVERE,"Unexpected error processing DEAD_SLICE command. Service is "+serviceKey+" node is "+sliceKey);
						((Throwable) result).printStackTrace();
					}
				}
			}
    }

    private void broadcastRemoveSlice(String serviceKey, String sliceKey) throws IMTPException, ServiceException {
    	// Avoid concurrent modification exception
    	Object[] rr = replicas.values().toArray();
    	for (int i = 0; i < rr.length; ++i) {
    		PlatformManager replica = (PlatformManager) rr[i];
    		replica.removeSlice(serviceKey, sliceKey, true);
    		try {
	    		replica.removeSlice(serviceKey, sliceKey, true);
    		}
    		catch (IMTPException imtpe) {
    			// Zombie replica. Just remove it
    			localRemoveReplica(replica.getLocalAddress(), true);
    		}
    	}
    }

    public void addReplica(String newAddr, boolean propagated)  throws IMTPException, ServiceException {
    	PlatformManager newReplica = myIMTPManager.getPlatformManagerProxy(newAddr);
    	localAddReplica(newReplica, propagated);
    	if (!propagated) {
    		broadcastAddReplica(newAddr);
    	}
    	// Actually add the new replica only after broadcasting
    	replicas.put(newReplica.getLocalAddress(), newReplica);
    }

    private void localAddReplica(PlatformManager newReplica, boolean propagated)  throws IMTPException, ServiceException {
			//myLogger.log("Adding replica <" + newReplica.getLocalAddress() + "> to the platform", 2);
                        if (myLogger.isLoggable(Logger.INFO))
                            myLogger.log(Logger.INFO,"Adding replica <" + newReplica.getLocalAddress() + "> to the platform");


    	if (!propagated) {
		    // Inform the new replica about existing nodes and their installed services...
		    List infos = getAllNodesInfo();

		    Iterator it = infos.iterator();
		    while(it.hasNext()) {
					NodeInfo info = (NodeInfo) it.next();
					try {
						newReplica.addNode(info.getNodeDescriptor(), info.getServices(), true);
					}
					catch (JADESecurityException ae) {
						// Should never happen since this is a propagated info
						ae.printStackTrace();
					}
		    }

		    // Inform the new replica about other replicas
		    // Avoid concurrent modification exception
		    Object[] rr = replicas.values().toArray();
		    for (int i = 0; i < rr.length; ++i) {
		    	PlatformManager replica = (PlatformManager) rr[i];
		    	newReplica.addReplica(replica.getLocalAddress(), true);
		    }

		    // Issue a NEW_REPLICA command
				GenericCommand gCmd = new GenericCommand(Service.NEW_REPLICA, null, null);
				gCmd.addParam(newReplica.getLocalAddress());
				Object result = myCommandProcessor.processIncoming(gCmd);
				if (result instanceof Throwable) {
					//myLogger.log("Unexpected error processing NEW_REPLICA command. Replica address is "+newReplica.getLocalAddress(), 0);
                                        if (myLogger.isLoggable(Logger.SEVERE))
                                            myLogger.log(Logger.SEVERE,"Unexpected error processing NEW_REPLICA command. Replica address is "+newReplica.getLocalAddress());
					((Throwable) result).printStackTrace();
				}
    	}
    }

    private void broadcastAddReplica(String newAddr) throws IMTPException, ServiceException {
    	// Avoid concurrent modification exception
    	Object[] rr = replicas.values().toArray();
    	for (int i = 0; i < rr.length; ++i) {
    		PlatformManager replica = (PlatformManager) rr[i];
    		try {
	    		replica.addReplica(newAddr, true);
    		}
    		catch (IMTPException imtpe) {
    			// Zombie replica. Just remove it
    			localRemoveReplica(replica.getLocalAddress(), true);
    		}
    	}
    }

    public void removeReplica(String address, boolean propagated)  throws IMTPException, ServiceException {
    	localRemoveReplica(address, propagated);
    	if (!propagated) {
    		broadcastRemoveReplica(address);
    	}
    }

    private void localRemoveReplica(String address, boolean propagated)  throws IMTPException, ServiceException {
			//myLogger.log("Removing replica <" + address + "> from the platform", 2);
                        if (myLogger.isLoggable(Logger.INFO))
                            myLogger.log(Logger.INFO,"Removing replica <" + address + "> from the platform");


    	// Remove the old replica
    	replicas.remove(address);

    	if (!propagated) {
    		// Check all non-main nodes and adopt them if they were attached to the
    		// dead PlatformManager
				ContainerID[] allContainers = myMain.containerIDs();
				for(int i = 0; i < allContainers.length; i++) {
			    ContainerID targetID = allContainers[i];
		    	Node n = null;
			    try {
			    	n = myMain.getContainerNode(targetID);
						if(!n.hasPlatformManager()) {
							n.platformManagerDead(address, getLocalAddress());
						}
			    }
			    catch(NotFoundException nfe) {
						// Just ignore it
			    }
			    catch(IMTPException imtpe) {
			    	// The node daid while no one was monitoring it
			    	removeTerminatedNode(n);
			    }
				}

		    // Issue a DEAD_REPLICA command
				GenericCommand gCmd = new GenericCommand(Service.DEAD_REPLICA, null, null);
				gCmd.addParam(address);
				Object result = myCommandProcessor.processIncoming(gCmd);
				if (result instanceof Throwable) {
					//myLogger.log("Unexpected error processing DEAD_REPLICA command. Replica address is "+address, 0);
                                        if (myLogger.isLoggable(Logger.SEVERE))
                                            myLogger.log(Logger.SEVERE,"Unexpected error processing DEAD_REPLICA command. Replica address is "+address);
					((Throwable) result).printStackTrace();
				}
    	}
    }

    private void broadcastRemoveReplica(String address) throws IMTPException, ServiceException {
    	// Avoid concurrent modification exception
    	Object[] rr = replicas.values().toArray();
    	for (int i = 0; i < rr.length; ++i) {
    		PlatformManager replica = (PlatformManager) rr[i];
    		try {
	    		replica.removeReplica(address, true);
    		}
    		catch (IMTPException imtpe) {
    			// Zombie replica. Just remove it
    			localRemoveReplica(replica.getLocalAddress(), true);
    		}
    	}
    }

   	public void adopt(Node n) throws IMTPException {
   		monitor(n);
   	}

   	public void ping() throws IMTPException {
   		// Just do nothing
   	}

    ////////////////////////////////
    // Service finding methods
    ////////////////////////////////
    public Service.Slice findSlice(String serviceKey, String sliceKey) throws IMTPException, ServiceException {
			ServiceEntry e = (ServiceEntry)services.get(serviceKey);

			if(e == null) {
		    return null;
			}
			else {
		    // If the special MAIN_SLICE name is used, return the local slice
		    if(CaseInsensitiveString.equalsIgnoreCase(sliceKey, ServiceFinder.MAIN_SLICE)) {
					sliceKey = myIMTPManager.getLocalNode().getName();
		    }

		    return e.getSlice(sliceKey);
			}
    }

    public Vector findAllSlices(String serviceKey) throws IMTPException, ServiceException {
			ServiceEntry e = (ServiceEntry)services.get(serviceKey);
			if(e == null) {
		    return null;
			}
			else {
		    return e.getSlices();
			}
    }


    //////////////////////////////////
    // Private methods
    //////////////////////////////////
    private boolean isLocalNode(Node n) {
    	try {
	    	return myIMTPManager.getLocalNode().equals(n);
    	}
    	catch (IMTPException imtpe) {
    		// Should never happen
    		imtpe.printStackTrace();
    		return false;
    	}
    }

    private List getAllNodesInfo() {
    	// Get all node descriptors and build the list of NodeInfo
    	ContainerID[] ids = myMain.containerIDs();
    	List infos = new ArrayList(ids.length);
    	for (int i = 0; i < ids.length; ++i) {
    		try {
	    		Node n = myMain.getContainerNode(ids[i]);
			    NodeDescriptor nodeDsc = new NodeDescriptor(ids[i], n);
			    nodeDsc.setOwnerPrincipal(myMain.getPrincipal(ids[i]));
			    nodeDsc.setOwnerCredentials(myMain.getCredentials(ids[i]));
			    infos.add(new NodeInfo(nodeDsc));
    		}
    		catch (NotFoundException nfe) {
    			// Just ignore it
    		}
    	}

    	// Build the map of services for each node
			Map nodeServices = new HashMap();
			// Avoid concurrent modification exception
			Object[] allServices = services.values().toArray();
			for (int j = 0; j < allServices.length; ++j) {
		    ServiceEntry e = (ServiceEntry) allServices[j];
		    Node[] serviceNodes = e.getNodes();

		    for(int i = 0; i < serviceNodes.length; i++) {
					String nodeName = serviceNodes[i].getName();

					Vector v = (Vector) nodeServices.get(nodeName);
					if(v == null) {
			    	v = new Vector();
			    	nodeServices.put(nodeName, v);
					}
					Service svc = e.getService();
					v.addElement(new ServiceDescriptor(svc.getName(), svc));
		    }
			}

			// Now fill the services in the list of NodeInfo
			Iterator it = infos.iterator();
			while (it.hasNext()) {
				NodeInfo ni = (NodeInfo) it.next();
				Vector v = (Vector) nodeServices.get(ni.getNodeDescriptor().getName());
				ni.setServices(v);
			}
			return infos;
    }


    private void adjustContainerName(Node n, ContainerID cid) {

	// Do nothing if a custom name is already supplied
	if((cid != null) && !cid.getName().equals(AgentContainer.UNNAMED_CONTAINER_NAME)) {
	    return;
	}

	if(n.hasPlatformManager()) {

	    // Use the Main-Container-<N> name schema
	    if(mainNodeNo == 0) {
		cid.setName(AgentContainer.MAIN_CONTAINER_NAME);
	    }
	    else {
		cid.setName(AgentContainer.MAIN_CONTAINER_NAME + '-' + mainNodeNo);
	    }
	    try {
		while(true) {
		    // Try until a non-existing name is found...
		    myMain.getContainerNode(cid);
		    mainNodeNo++;
		    cid.setName(AgentContainer.MAIN_CONTAINER_NAME + '-' + mainNodeNo);
		}
	    }
	    catch(NotFoundException nfe) {
		// There is no such named container, so the name is OK.
	    }
	}
	else {

	    // Use the Container-<M> name schema
	    cid.setName(AgentContainer.AUX_CONTAINER_NAME + '-' + nodeNo);
	    try {
		while(true) {
		    // Try until a non-existing name is found...
		    myMain.getContainerNode(cid);
		    nodeNo++;
		    cid.setName(AgentContainer.AUX_CONTAINER_NAME + '-' + nodeNo);
		}
	    }
	    catch(NotFoundException nfe) {
		// There is no such named container, so the name is OK.
	    }
	}
    }


    private void monitor(Node target) {

	// Set up a failure monitor using the blocking ping...
	NodeFailureMonitor failureMonitor = new NodeFailureMonitor(target, new NodeEventListener() {

		// FIXME: Should notify all the interested service slices...

		public void nodeAdded(Node n) {
		    //myLogger.log("--- Node <" + n.getName() + "> ALIVE ---", 1);
                    if (myLogger.isLoggable(Logger.INFO))
                        myLogger.log(Logger.INFO,"--- Node <" + n.getName() + "> ALIVE ---");

		}

		public void nodeRemoved(Node n) {
			removeTerminatedNode(n);
		}

		public void nodeUnreachable(Node n) {
		    //myLogger.log("--- Node <" + n.getName() + "> UNREACHABLE ---", 1);
                    if (myLogger.isLoggable(Logger.WARNING))
                        myLogger.log(Logger.WARNING,"--- Node <" + n.getName() + "> UNREACHABLE ---");

		}

		public void nodeReachable(Node n) {
		    //myLogger.log("--- Node <" + n.getName() + "> REACHABLE ---", 1);
                    if (myLogger.isLoggable(Logger.INFO))
                        myLogger.log(Logger.INFO,"--- Node <" + n.getName() + "> REACHABLE ---");

		}

	    });

	// Start a new node failure monitor
	Thread t = new Thread(failureMonitor);
	t.setName(target.getName()+"-failure-monitor");
	t.start();
    }


    private void removeTerminatedNode(Node n) {
	    //myLogger.log("--- Node <" + n.getName() + "> TERMINATED ---", 1);
            if (myLogger.isLoggable(Logger.INFO))
                myLogger.log(Logger.INFO,"--- Node <" + n.getName() + "> TERMINATED ---");

	    try {
				removeNode(new NodeDescriptor(n), false);
	    }
	    catch(IMTPException imtpe) {
	    	// Should never happen since this is a local call
				imtpe.printStackTrace();
	    }
	    catch(ServiceException se) {
	    	// There is nothing we can do
				se.printStackTrace();
	    }
    }

    /**
       Inner class NodeInfo.
       Embeds the node descriptor and the services currently installed
       on the node
     */
    private class NodeInfo {
			private NodeDescriptor nodeDsc;
			private Vector services;

			private NodeInfo(NodeDescriptor nd) {
				nodeDsc = nd;
			}

			public NodeDescriptor getNodeDescriptor() {
			    return nodeDsc;
			}

			public Vector getServices() {
			    return services;
			}

			public void setServices(Vector ss) {
			    services = ss;
			}
    } // END of inner class NodeInfo

}
