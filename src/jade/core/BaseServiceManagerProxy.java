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

import jade.security.AuthException;

import jade.util.leap.List;
import jade.util.leap.LinkedList;
import jade.util.leap.Map;
import jade.util.leap.HashMap;
import jade.util.leap.Iterator;


/**

   The <code>BaseServiceManagerProxy</code> abstract class partially
   implements the <code>ServiceManager</code> and
   <code>ServiceFinder</code> interfaces, providing an IMTP
   independent smart proxy for the remote service manager and finder
   installed in the platform.

   This class manages some operations locally; whenever a remote
   invocation is needed, a suitable abstract method is
   invoked. Concrete subclasses have to implement those methods using
   whatever IMTP they are related to.

   @author Giovanni Rimassa - FRAMeTech s.r.l.

*/
public abstract class BaseServiceManagerProxy implements ServiceManager, ServiceFinder {

    /**
       Accessible constructor for this abstract class. Subclasses have
       to provide the local node and the local Command Processor for
       this class to work properly.

       @param mgr The IMTP manager used by this Service Manager proxy.
       @param proc The command processor used to hold and manage the
       service filters for the various installed services.
    */
    public BaseServiceManagerProxy(IMTPManager mgr, CommandProcessor proc) {
	try {
	    myIMTPManager = mgr;
	    myCommandProcessor = proc;
	    localNode = mgr.getLocalNode();
	    addresses = new LinkedList();
	    services = new HashMap();
	}
	catch(IMTPException imtpe) {
	    // This shouldn't happen because all calls are local
	    imtpe.printStackTrace();
	}
    }

    /**
       Add the given node to an existing remote Service Manager node
       list, and activate slices for the given services.
       This method must be implemented by subclasses in term of their
       chosen IMTP.

       @param desc The information about the node that wants to join the platform.
       @param services An array of descriptors for all the services
       that have to be activated on the new node.
       @return The name assigned to the new node by the platform.

       @throws IMTPException If a network error occurs.
       @throws ServiceException If some service-level operation fails.
       @throws AuthException If the new node is refused to join the
       platform due to some security constraint.
    */
    protected abstract String addRemoteNode(NodeDescriptor desc, ServiceDescriptor[] services) throws IMTPException, ServiceException, AuthException;

    /**
       Remove the given node to an existing remote Service Manager node
       list, and deactivate slices for all the node services.
       This method must be implemented by subclasses in term of their
       chosen IMTP.

       @param desc The information about the node that wants to join the platform.

       @throws IMTPException If a network error occurs.
       @throws ServiceException If some service-level operation fails.
       platform due to some security constraint.
    */
    protected abstract void removeRemoteNode(NodeDescriptor desc) throws IMTPException, ServiceException;


    /**
       Add the given service slice to an existing remote Service
       Manager slice table.
       This method must be implemented by subclasses in term of their
       chosen IMTP.

       @param svcName The name of the service the slice is part of.
       @param itf The <code>Class</code> object corresponding to the
       public slice interface.
       @param where The description of the node where this slice
       actually resides.

       @throws IMTPException If a network error occurs.
       @throws ServiceException If some service-level operation fails.
    */
    protected abstract void addRemoteSlice(String svcName, Class itf, NodeDescriptor where) throws IMTPException, ServiceException;


    /**
       Remove the slice for the given service on the given node from
       the Service Manager slice table.
       This method must be implemented by subclasses in term of their
       chosen IMTP.

       @param svcName The name of the service the slice is part of.
       @param where The description of the node where this slice
       actually resides.

       @throws IMTPException If a network error occurs.
       @throws ServiceException If some service-level operation fails.
    */
    protected abstract void removeRemoteSlice(String svcName, NodeDescriptor where) throws IMTPException, ServiceException;

    /**
       Retrieve the node where the given service slice is deployed.
       This method must be implemented by subclasses in term of their
       chosen IMTP.

       @param serviceKey The name of the requested service.
       @param sliceKey The name of the requested slice (within its service namespace).
       @return The <code>Node</code> object for the node where the
       slice is deployed, or <code>null</code> if no such slice
       exists.

       @throws IMTPException If a network error occurs.
       @throws ServiceException If some service-level operation fails.
    */
    protected abstract Node findSliceNode(String serviceKey, String sliceKey) throws IMTPException, ServiceException;


    /**
       Retrieve all the nodes where the various slices of a given service are deployed.
       This method must be implemented by subclasses in term of their chosen IMTP.

       @param serviceKey The name of the requested service.
       @return An array of <code>Node</code> objects, representing all
       the nodes where the requested service resides.

       @throws IMTPException If a network error occurs.
       @throws ServiceException If some service-level operation fails.
    */
    protected abstract Node[] findAllNodes(String serviceKey) throws IMTPException, ServiceException;

    public void addAddress(String addr) throws IMTPException {
	addresses.add(addr);
    }

    public void removeAddress(String addr) throws IMTPException {
	addresses.remove(addr);
    }

    public String[] getAddresses() throws IMTPException {
	Object[] objs = addresses.toArray();
	String[] addrs = new String[objs.length];

	for(int i = 0; i < addrs.length; i++) {
	    addrs[i] = (String)objs[i];
	}

	return addrs;
    }

    public void addNode(NodeDescriptor desc, ServiceDescriptor[] services) throws IMTPException, ServiceException, AuthException {

	// Register this node and all its services with the Service Manager

	// Remote call, IMTP-dependent
	String containerName = addRemoteNode(desc, services);

	ContainerID cid = desc.getContainer();
	if(cid != null) {
	    cid.setName(containerName);
	    myIMTPManager.connect(cid);
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

    public void removeNode(NodeDescriptor desc) throws IMTPException, ServiceException {
	// First, deregister this node with the service manager

	// Remote call, IMTP-dependent
	removeRemoteNode(desc);

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

	// Add a service slice for this service on this node to the remote Service Manager

	// Remote call, IMTP-dependent
	addRemoteSlice(name, svc.getHorizontalInterface(), new NodeDescriptor(localNode.getName(), localNode));

    }

    public void deactivateService(ServiceDescriptor desc) throws IMTPException, ServiceException {

	// Remove the service slice for this service on this node from the remote Service Manager

	// Remote call, IMTP-dependent
	String name = desc.getName();
	removeRemoteSlice(name, new NodeDescriptor(localNode.getName(), localNode));

	// Uninstall the local component of the service
	uninstallServiceLocally(name);
    }

    public Service findService(String key) throws IMTPException, ServiceException {
	return (Service)services.get(key);
    }

    public Service.Slice findSlice(String serviceKey, String sliceKey) throws IMTPException, ServiceException {

	// FIXME: It should not be needed, and the horizontal interface should be returned from the remote end
	Service localService = findService(serviceKey);
	if(localService == null) {
	    // FIXME: Should install a DummyService...
	    throw new ServiceException("The service <" + serviceKey + "> is not available on node <" + localNode.getName() + ">");
	}

	// Find the node for this slice with the help of the remote Service Manager
  Node node = null;
	if(sliceKey.equals(localNode.getName())) {
		node = localNode;
	}
	else {
		// Remote call, IMTP-dependent
		node = findSliceNode(serviceKey, sliceKey);
	}
	return myIMTPManager.createSliceProxy(serviceKey, localService.getHorizontalInterface(), node);
    }

    public Service.Slice[] findAllSlices(String serviceKey) throws IMTPException, ServiceException {

	// FIXME: It should not be needed, and the horizontal interface should be returned from the remote end
	Service localService = findService(serviceKey);
	if(localService == null) {
	    // FIXME: Should install a DummyService...
	    throw new ServiceException("The service <" + serviceKey + "> is not available on node <" + localNode.getName() + ">");
	}

	// Find all nodes for this service with the help of the remote Service Manager

	// Remote call, IMTP-dependent
	Node[] nodes = findAllNodes(serviceKey);
  // Replace the stub for the local node (if present) with the real thing
  for(int i = 0; i < nodes.length; i++) {
		String nodeName = nodes[i].getName();
		if(nodeName.equals(localNode.getName())) {
	    nodes[i] = localNode;
		}
  }

	Class itf = localService.getHorizontalInterface();
	Service.Slice[] result = new Service.Slice[nodes.length];
	for(int i = 0; i < nodes.length; i++) {
	    result[i] = myIMTPManager.createSliceProxy(serviceKey, itf, nodes[i]);
	}

	return result;
    }

    /**
       Allows subclasses to access the command processor.

       @return The <code>CommandProcessor</code> instance used by this
       Service Manager proxy.
    */
    protected CommandProcessor getCommandProcessor() {
	return myCommandProcessor;
    }

    // Private helper method, common to one-shot and batch service activation
    private void installServiceLocally(String name, Service svc) throws IMTPException, ServiceException {

	// Install the service filters
	Filter fOut = svc.getCommandFilter(Filter.OUTGOING);
	if(fOut != null) {
	    myCommandProcessor.addFilter(fOut, Filter.OUTGOING);
	}
	Filter fIn = svc.getCommandFilter(Filter.INCOMING);
	if(fIn != null) {
	    myCommandProcessor.addFilter(fIn, Filter.INCOMING);
	}

	// Install the service sinks
	String[] commandNames = svc.getOwnedCommands();
	Sink sSrc = svc.getCommandSink(Sink.COMMAND_SOURCE);
	if(sSrc != null) {
	    myCommandProcessor.registerSink(sSrc, Sink.COMMAND_SOURCE, commandNames);
	}
	Sink sTgt = svc.getCommandSink(Sink.COMMAND_TARGET);
	if(sTgt != null) {
	    myCommandProcessor.registerSink(sTgt, Sink.COMMAND_TARGET, commandNames);
	}

	// Export the local slice so that it can be reached through the network
	Service.Slice localSlice = svc.getLocalSlice();
	if(localSlice != null) {
	    myIMTPManager.exportSlice(name, localSlice);
	}

	// Add the service to the local service finder so that it can be found
	services.put(svc.getName(), svc);
    }

    // Private helper method, common to one-shot and batch service deactivation
    private void uninstallServiceLocally(String name) throws IMTPException, ServiceException {

	// FIXME: It should remove the service only if there are no more active slices in the whole platform.
	Service svc = (Service)services.get(name); // Find the local copy of the service
	services.remove(name);
	myIMTPManager.unexportSlice(name, svc.getLocalSlice());

	// Uninstall the service filters
	Filter fOut = svc.getCommandFilter(Filter.OUTGOING);
	if(fOut != null) {
	    myCommandProcessor.removeFilter(fOut, Filter.OUTGOING);
	}
	Filter fIn = svc.getCommandFilter(Filter.INCOMING);
	if(fIn != null) {
	    myCommandProcessor.removeFilter(fIn, Filter.INCOMING);
	}

	// Uninistall the service sinks
	String[] commandNames = svc.getOwnedCommands();
	Sink sSrc = svc.getCommandSink(Sink.COMMAND_SOURCE);
	if(sSrc != null) {
	    myCommandProcessor.deregisterSink(Sink.COMMAND_SOURCE, commandNames);
	}
	Sink sTgt = svc.getCommandSink(Sink.COMMAND_TARGET);
	if(sTgt != null) {
	    myCommandProcessor.deregisterSink(Sink.COMMAND_TARGET, commandNames);
	}

    }


    private List addresses;
    private Map services;
    private Node localNode;
    private IMTPManager myIMTPManager;
    private CommandProcessor myCommandProcessor;

}

