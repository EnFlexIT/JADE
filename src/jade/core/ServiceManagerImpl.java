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

import jade.security.JADESecurityException;

import jade.util.leap.Map;
import jade.util.leap.HashMap;
import jade.util.leap.Iterator;
import jade.util.Logger;
import java.util.Vector;

/**

 The <code>ServiceManagerImpl</code> class is the actual
 implementation of JADE platform <i>Service Manager</i> and
 <i>Service Finder</i> components. It holds a set of services and
 manages them.

 @author Giovanni Rimassa - FRAMeTech s.r.l.
 @author Giovanni Caire - TILAB
 */
public class ServiceManagerImpl implements ServiceManager, ServiceFinder {
	private IMTPManager myIMTPManager;
	private CommandProcessor myCommandProcessor;
	private PlatformManager myPlatformManager;
	private boolean invalidPlatformManager;
	private String platformName;
	private Node localNode;
	private NodeDescriptor localNodeDescriptor;
	private Map localServices;
	private Map backupManagers;

	private jade.util.Logger myLogger;

	/**
	 Constructs a new Service Manager implementation complying with
	 a given JADE profile. This constructor is package-scoped, so
	 that only the JADE kernel is allowed to create a new Service
	 Manager implementation.

	 @param p The platform profile describing how the JADE platform
	 is to be configured.
	 */
	ServiceManagerImpl(Profile p, PlatformManager pm) throws ProfileException {
		myCommandProcessor = p.getCommandProcessor();
		myIMTPManager = p.getIMTPManager();
		myPlatformManager = pm;
		invalidPlatformManager = false;
		localServices = new HashMap(5);
		backupManagers = new HashMap(1);

		myLogger = Logger.getMyLogger(getClass().getName());
	}

	// Implementation of the ServiceManager interface

	public String getPlatformName() throws IMTPException {
		if (platformName == null) {
			try {
				platformName = myPlatformManager.getPlatformName();
			} catch (IMTPException imtpe) {
				if (reconnect()) {
					platformName = myPlatformManager.getPlatformName();
				} else {
					throw imtpe;
				}
			}
		}
		return platformName;
	}

	public synchronized void addAddress(String addr) throws IMTPException {
		if (myLogger.isLoggable(Logger.FINE)) {
			myLogger.log(Logger.FINE, "Adding PlatformManager address " + addr);
		}

		if (invalidPlatformManager || !addr.equals(myPlatformManager.getLocalAddress())) {
			backupManagers.put(addr, myIMTPManager.getPlatformManagerProxy(addr));
			if (invalidPlatformManager) {
				reconnect();
			}
		}
	}

	public synchronized void removeAddress(String addr) throws IMTPException {
		if (myLogger.isLoggable(Logger.FINE)) {
			myLogger.log(Logger.FINE, "Removing PlatformManager address " + addr);
		}

		backupManagers.remove(addr);
		if (addr.equals(myPlatformManager.getLocalAddress())) {
			reconnect();
		}
	}

	public String getLocalAddress() throws IMTPException {
		return myPlatformManager.getLocalAddress();
	}

	public void addNode(NodeDescriptor desc, ServiceDescriptor[] services) throws IMTPException, ServiceException, JADESecurityException {
		localNodeDescriptor = desc;
		localNode = desc.getNode();
		try {
			// Install all services locally
			Vector ss = new Vector(services != null ? services.length : 0);
			if (services != null) {
				for (int i = 0; i < services.length; ++i) {
					try {
						installServiceLocally(services[i]);
						ss.addElement(services[i]);
					}
					catch (Exception e) {
						if (services[i].isMandatory()) {
							throw e;
						}
						else {
					  		myLogger.log(Logger.WARNING,"Exception installing service " + services[i].getService() + ". " + e);
					  		e.printStackTrace();
						}
					}
				}
			}

			// Notify the platform manager. Get back a valid name and assign
			// it to both the node, the node descriptor and the container (if any)
			String name = null;
			try {
				name = myPlatformManager.addNode(desc, ss, false);
			} catch (IMTPException imtpe) {
				if (reconnect()) {
					name = myPlatformManager.addNode(desc, ss, false);
				} else {
					throw imtpe;
				}
			}
			adjustName(name);
		} catch (IMTPException imtpe2) {
			throw imtpe2;
		} catch (ServiceException se) {
			throw se;
		} catch (JADESecurityException ae) {
			throw ae;
		} catch (Throwable t) {
			throw new ServiceException("Unexpected error activating node", t);
		}
	}

	public void removeNode(NodeDescriptor desc) throws IMTPException, ServiceException {
		// Do not notify the platform manager. The node termination will cause the deregistration...

		// Uninstall all services locally
		Object[] names = localServices.keySet().toArray();
		for (int i = 0; i < names.length; i++) {
			try {
				String svcName = (String) names[i];
				uninstallServiceLocally(svcName);
			} catch (IMTPException imtpe) {
				// This should never happen, because it's a local call...
				imtpe.printStackTrace();
			}
		}

		//#MIDP_EXCLUDE_BEGIN
		// If this node was exporting a PlatformManager, unexport it
		if (desc.getNode().hasPlatformManager()) {
			myIMTPManager.unexportPlatformManager(myPlatformManager);
		}
		//#MIDP_EXCLUDE_END
	}

	public void activateService(ServiceDescriptor desc) throws IMTPException, ServiceException {
		try {
			// Install the service locally
			installServiceLocally(desc);
			// Notify the platform manager (add a slice for this service on this node)
			try {
				myPlatformManager.addSlice(desc, localNodeDescriptor, false);
			} catch (IMTPException imtpe) {
				if (reconnect()) {
					myPlatformManager.addSlice(desc, localNodeDescriptor, false);
				} else {
					throw imtpe;
				}
			}
		} catch (IMTPException imtpe2) {
			// Undo the local service installation
			uninstallServiceLocally(desc.getName());
			// Rethrow the exception
			throw imtpe2;
		}
	}

	public void deactivateService(String name) throws IMTPException, ServiceException {
		ServiceDescriptor desc = (ServiceDescriptor) localServices.get(name);
		if (desc != null) {
			// Notify the platform manager (remove the slice for this service on this node)
			try {
				myPlatformManager.removeSlice(name, localNode.getName(), false);
			} 
			catch (IMTPException imtpe) {
				if (reconnect()) {
					myPlatformManager.removeSlice(name, localNode.getName(), false);
				} 
				else {
					throw imtpe;
				}
			}
	
			// Uninstall the service locally
			uninstallServiceLocally(name);
		}
	}

	/////////////////////////////////////////////////
	// ServiceFinder interface
	/////////////////////////////////////////////////

	public Service findService(String key) throws IMTPException, ServiceException {
		Service svc = null;
		ServiceDescriptor svcDsc = (ServiceDescriptor) localServices.get(key);
		if (svcDsc != null) {
			svc = svcDsc.getService();
		}
		return svc;
	}

	public Service.Slice findSlice(String serviceKey, String sliceKey) throws IMTPException, ServiceException {
		Service.Slice slice = null;
		try {
			slice = myPlatformManager.findSlice(serviceKey, sliceKey);
		} catch (IMTPException imtpe) {
			if (reconnect()) {
				slice = myPlatformManager.findSlice(serviceKey, sliceKey);
			} else {
				throw imtpe;
			}
		}
		return checkLocal(slice);
	}

	public Service.Slice[] findAllSlices(String serviceKey) throws IMTPException, ServiceException {
		Vector v = null;
		try {
			v = myPlatformManager.findAllSlices(serviceKey);
		} catch (IMTPException imtpe) {
			if (reconnect()) {
				v = myPlatformManager.findAllSlices(serviceKey);
			} else {
				throw imtpe;
			}
		}
		if (v == null) {
			return null;
		} else {
			Service.Slice[] ss = new Service.Slice[v.size()];
			for (int i = 0; i < ss.length; ++i) {
				ss[i] = checkLocal((Service.Slice) v.elementAt(i));
			}
			return ss;
		}
	}

	/////////////////////////////////////////////////
	// Private methods
	/////////////////////////////////////////////////

	private void installServiceLocally(ServiceDescriptor svcDsc) throws IMTPException, ServiceException {
		Service svc = svcDsc.getService();

		// Install the service filters
		Filter fOut = svc.getCommandFilter(Filter.OUTGOING);
		if (fOut != null) {
			fOut.setServiceName(svc.getName());
			myCommandProcessor.addFilter(fOut, Filter.OUTGOING);
		}
		Filter fIn = svc.getCommandFilter(Filter.INCOMING);
		if (fIn != null) {
			if (fIn == fOut) {
				// NOTE that fOut is certainly != null
				myCommandProcessor.removeFilter(fOut, Filter.OUTGOING);
				throw new ServiceException("The same filter object cannot be used as both incoming and outgoing filter.");
			}
			fIn.setServiceName(svc.getName());
			myCommandProcessor.addFilter(fIn, Filter.INCOMING);
		}

		// Install the service sinks
		Sink sSrc = svc.getCommandSink(Sink.COMMAND_SOURCE);
		if (sSrc != null) {
			myCommandProcessor.registerSink(sSrc, Sink.COMMAND_SOURCE, svc.getName());
		}
		Sink sTgt = svc.getCommandSink(Sink.COMMAND_TARGET);
		if (sTgt != null) {
			myCommandProcessor.registerSink(sTgt, Sink.COMMAND_TARGET, svc.getName());
		}

		// Export the local slice so that it can be reached through the network
		Service.Slice localSlice = svc.getLocalSlice();
		if (localSlice != null) {
			localNode.exportSlice(svc.getName(), localSlice);
		}

		// Add the service to the local service finder so that it can be found
		localServices.put(svc.getName(), svcDsc);

		// If this service extends BaseService, attach it to the Command Processor
		if (svc instanceof BaseService) {
			BaseService bs = (BaseService) svc;
			bs.setCommandProcessor(myCommandProcessor);
		}		
	}

	private void uninstallServiceLocally(String name) throws IMTPException, ServiceException {
		ServiceDescriptor svcDsc = (ServiceDescriptor) localServices.get(name);
		if (svcDsc != null) {
			Service svc = svcDsc.getService();

			// Stop the service
			svc.shutdown();

			// Uninstall the service filters 
			Filter fOut = svc.getCommandFilter(Filter.OUTGOING);
			if (fOut != null) {
				myCommandProcessor.removeFilter(fOut, Filter.OUTGOING);
			}
			Filter fIn = svc.getCommandFilter(Filter.INCOMING);
			if (fIn != null) {
				myCommandProcessor.removeFilter(fIn, Filter.INCOMING);
			}

			// Uninistall the service sinks
			Sink sSrc = svc.getCommandSink(Sink.COMMAND_SOURCE);
			if (sSrc != null) {
				myCommandProcessor.deregisterSink(Sink.COMMAND_SOURCE, svc.getName());
			}
			Sink sTgt = svc.getCommandSink(Sink.COMMAND_TARGET);
			if (sTgt != null) {
				myCommandProcessor.deregisterSink(Sink.COMMAND_TARGET, svc.getName());
			}
		}

		// Unexport the service slice
		localNode.unexportSlice(name);

		// Remove the service
		localServices.remove(name);
	}

	private void invalidatePlatformManager() {
		if (!invalidPlatformManager) {
			invalidPlatformManager = true;
			// Issue a DEAD_PLATFORM_MANAGER incoming vertical command
			GenericCommand gCmd = new GenericCommand(Service.DEAD_PLATFORM_MANAGER, null, null);
			gCmd.addParam(myPlatformManager.getLocalAddress());
			Object result = myCommandProcessor.processIncoming(gCmd);
			if (result instanceof Throwable) {
				myLogger.log(Logger.WARNING, "Unexpected error processing DEAD_PLATFORM_MANAGER command.");
				((Throwable) result).printStackTrace();
			}
		}
	}
	
	// This is package scoped since it is called by BaseNode.platformManagerDead()
	synchronized void reattach(String pmAddr) {
		// We reattach to the recovered PM either if it is our PM or if our
		// PM is invalid (a previous reattach/reconnect attempt failed).
		// Otherwise we just do nothing
		if (invalidPlatformManager || pmAddr.equals(myPlatformManager.getLocalAddress())) {
			invalidatePlatformManager();
			try {
				myPlatformManager = myIMTPManager.getPlatformManagerProxy(pmAddr);
				String name = myPlatformManager.addNode(localNodeDescriptor, getLocalServices(), false);
				if (!name.equals(localNodeDescriptor.getName())) {
					myLogger.log(Logger.WARNING, "Container name changed re-attaching to PlatformManager: new name = " + name);
				}
				adjustName(name);

				handlePMRefreshed(pmAddr);

				// Issue a REATTACHED incoming V-Command
				System.out.println("Issuing REATTACHED Incoming command");
				GenericCommand gCmd = new GenericCommand(Service.REATTACHED, null, null);
				Object result = myCommandProcessor.processIncoming(gCmd);
				if (result instanceof Throwable) {
					myLogger.log(Logger.SEVERE, "Unexpected error processing REATTACHED command.");
					((Throwable) result).printStackTrace();
				}

				myLogger.log(Logger.INFO, "Re-attached to PlatformManager at " + pmAddr);
			} catch (Exception e) {
				myLogger.log(Logger.SEVERE, "Cannot re-attach to PlatformManager at " + pmAddr + ". " + e);
				e.printStackTrace();
			}
		}
	}

	private synchronized boolean reconnect() {
		// Check if the current PlatformManager is actually down (another thread
		// may have reconnected in the meanwhile)
		try {
			myPlatformManager.ping();
			return true;
		} 
		catch (IMTPException imtpe) {
			// The current PlatformManager is actually down --> try to reconnect
			invalidatePlatformManager();
			
			Iterator it = backupManagers.keySet().iterator();
			while (it.hasNext()) {
				String addr = (String) it.next();
				try {
					myPlatformManager = (PlatformManager) backupManagers.get(addr);
					myLogger.log(Logger.INFO, "Reconnecting to PlatformManager at address " + myPlatformManager.getLocalAddress());

					myPlatformManager.adopt(localNode, null);
					handlePMRefreshed(addr);

					myLogger.log(Logger.INFO, "Reconnection OK");
					return true;
				} 
				catch (Exception e) {
					myLogger.log(Logger.WARNING, "Reconnection failed");
					// Ignore it and try the next address...
				}
			}
			return false;
		}
	}

	private void handlePMRefreshed(String pmAddr) {
		// Clear any cached slice of the Main container
		Object[] services = localServices.values().toArray();
		for (int i = 0; i < services.length; ++i) {
			ServiceDescriptor svcDsc = (ServiceDescriptor) services[i];
			Service svc = svcDsc.getService();
			if (svc instanceof BaseService) {
				((BaseService) svc).clearCachedSlice(MAIN_SLICE);
			}
		}
		myIMTPManager.reconnected(myPlatformManager);
		backupManagers.remove(pmAddr);
		invalidPlatformManager = false;
	}

	private Vector getLocalServices() {
		Object[] services = localServices.values().toArray();
		Vector ss = new Vector(services.length);
		for (int i = 0; i < services.length; ++i) {
			ss.addElement(services[i]);
		}
		return ss;
	}

	private void adjustName(String name) {
		localNodeDescriptor.setName(name);
		localNode.setName(name);
		ContainerID cid = localNodeDescriptor.getContainer();
		if (cid != null) {
			cid.setName(name);
		}
	}

	private Service.Slice checkLocal(Service.Slice slice) throws ServiceException {
		if (slice != null) {
			// If the slice is for the local node be sure it includes the real local
			// node and not a proxy
			Node n = slice.getNode();
			if (n.getName().equals(localNode.getName()) && !n.equals(localNode)) {
				((SliceProxy) slice).setNode(localNode);
			}
		}
		return slice;
	}
}
