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

//#MIDP_EXCLUDE_FILE


import jade.security.AuthException;

import jade.util.leap.Map;
import jade.util.leap.HashMap;
import jade.util.leap.List;
import jade.util.leap.LinkedList;
import jade.util.leap.Iterator;


/**

   The <code>ServiceManagerImpl</code> class is the actual
   implementation of JADE platform <i>Service Manager</i> and
   <i>Service Finder</i> components. It holds a set of services and
   manages them.

   @author Giovanni Rimassa - FRAMeTech s.r.l.
*/
public class ServiceManagerImpl implements ServiceManager, ServiceFinder {

    private class ServiceEntry {

	public ServiceEntry(Service s) {
	    myService = s;
	    slices = new HashMap();
	}

	public void addSlice(String name, Service.Slice s, Node n) {
	    SliceEntry e = new SliceEntry(s, n);
	    slices.put(name, e);
	}

	public void removeSlice(String name) {
	    slices.remove(name);
	}

	public Service.Slice[] getSlices() {
	    Object[] sliceEntries = slices.values().toArray();
	    Service.Slice[] result = new Service.Slice[sliceEntries.length];

	    for(int i = 0; i < result.length; i++) {
		SliceEntry e = (SliceEntry)sliceEntries[i];
		result[i] = e.getSlice();
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

	public Service getService() {
	    return myService;
	}


	private Service myService;
	private Map slices;

    } // End of ServiceEntry class


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
    ServiceManagerImpl(Profile p) throws ProfileException, IMTPException {
	myCommandProcessor = p.getCommandProcessor();
	myIMTPManager = p.getIMTPManager();
	services = new HashMap();
    }


    // FIXME: The association between MainContainer and ServiceManagerImpl need be clarified...
    private MainContainerImpl myMain;
    void setMain(MainContainerImpl impl) {
	myMain = impl;
    }

    // Implementation of the ServiceManager interface

    public String getPlatformName() throws IMTPException {
	return myMain.getPlatformName();
    }

    public void addNode(NodeDescriptor desc, ServiceDescriptor[] services) throws IMTPException, ServiceException, AuthException {

	// Add the node as a local agent container and activate the new container with the IMTP manager
	myMain.addLocalContainer(desc);
	myIMTPManager.connect(desc.getContainer());

	// Activate all the node services
	List failedServices = new LinkedList();
	for(int i = 0; i < services.length; i++) {
	    try {
		activateService(services[i]);
	    }
	    catch(IMTPException imtpe) {
		// This should never happen, because it's a local call...
		imtpe.printStackTrace();
	    }
	    catch(ServiceException se) {
		failedServices.add(services[i].getName());
	    }
	}

	// Throw a failure exception, if needed
	if(!failedServices.isEmpty()) {

	    // All service activations failed: throw a single exception 
	    if(failedServices.size() == services.length) {
		throw new ServiceException("Total failure in installing the services for local node");
	    }
	    else {

		// Only some service activations failed: throw a single exception with the list of the failed services
		Iterator it = failedServices.iterator();
		String names = "[ ";
		while(it.hasNext()) {
		    names = names.concat((String)it.next() + " ");		
		}
		names = names.concat("]");
		throw new ServiceException("Partial failure in installing the services " + names);
	    }
	}

    }

    public void removeNode(NodeDescriptor desc) throws IMTPException, ServiceException {
	// Retrieve the node information
	Node localNode = desc.getNode();
	if(!localNode.equals(myIMTPManager.getLocalNode())) {
	    throw new ServiceException("A remote node cannot be added with this method call");
	}

	// Deactivate all the node services
	Object[] names = services.keySet().toArray();
	for(int i = 0; i < names.length; i++) {
	    try {
		String svcName = (String)names[i];
		Service svc = (Service)services.get(svcName);
		ServiceDescriptor svcDesc = new ServiceDescriptor(svcName, svc);
		deactivateService(svcDesc);
	    }
	    catch(IMTPException imtpe) {
		// This should never happen, because it's a local call...
		imtpe.printStackTrace();
	    }
	    catch(ServiceException se) {
		se.printStackTrace();
	    }
	}

	// Remove the node as a local agent container
	myMain.removeLocalContainer();

    }

    public void activateService(ServiceDescriptor desc) throws IMTPException, ServiceException {

	String name = desc.getName();
	Service svc = desc.getService();
	ServiceEntry old = (ServiceEntry)services.get(name);
	if(old != null) {
	    throw new ServiceException("A service named <" + name + "> is already active.");
	}
	else {
	    // Install the new service
	    ServiceEntry e = new ServiceEntry(svc);
	    services.put(name, e);

	    // Export the local slice so that it can be reached through the network
	    Service.Slice localSlice = svc.getLocalSlice();
	    if(localSlice != null) {
		myIMTPManager.exportSlice(name, localSlice);
	    }

	    // Put the local slice into the Service Finder so that it can be found later
	    Node here = myIMTPManager.getLocalNode();
	    e.addSlice(here.getName(), svc.getLocalSlice(), here);

	    System.out.println("Added a local slice <" + name + ";" + here.getName() + ">");

	    // Install the service filter
	    Filter f = svc.getCommandFilter(Filter.OUTGOING);
	    myCommandProcessor.addFilter(f, Filter.OUTGOING);

	    // FIXME: Should also add incoming filters and sink
	}
    }

    public void deactivateService(ServiceDescriptor desc) throws IMTPException, ServiceException {

	String name = desc.getName();
	ServiceEntry e = (ServiceEntry)services.get(name);

	if(e != null) {

	    Service svc = e.getService();

	    // Uninstall the service filter
	    Filter f = svc.getCommandFilter(Filter.OUTGOING);
	    myCommandProcessor.removeFilter(f, Filter.OUTGOING);

	    // FIXME: Should also remove incoming filters and sink

	    // Uninstall the service
	    services.remove(name);
	}

    }



    // Implementation of the ServiceFinder interface


    public Service findService(String key) throws IMTPException, ServiceException {
	ServiceEntry e = (ServiceEntry)services.get(key);
	return e.getService();
    }

    public Service.Slice findSlice(String serviceKey, String sliceKey) throws IMTPException, ServiceException {

	ServiceEntry e = (ServiceEntry)services.get(serviceKey);

	if(e == null) {
	    return null;
	}
	else {
	    return e.getSlice(sliceKey);
	}
    }

    public Service.Slice[] findAllSlices(String serviceKey) throws IMTPException, ServiceException {
	ServiceEntry e = (ServiceEntry)services.get(serviceKey);
	if(e == null) {
	    return null;
	}
	else {
	    return e.getSlices();
	}
    }


    public Node[] findAllNodes(String serviceKey) throws IMTPException, ServiceException {
	ServiceEntry e = (ServiceEntry)services.get(serviceKey);
	if(e == null) {
	    return null;
	}
	else {
	    return e.getNodes();
	}
    }

    public Node findSliceNode(String serviceKey, String nodeKey) throws IMTPException, ServiceException {

	ServiceEntry e = (ServiceEntry)services.get(serviceKey);
	if(e == null) {
	    return null;
	}
	else {
	    return e.getNode(nodeKey);
	}
    }



    // Slice management methods

    public String addRemoteNode(NodeDescriptor desc) throws AuthException {


	ContainerID cid = desc.getContainer();
	String username = desc.getPrincipalName();
	byte[] password = desc.getPrincipalPwd();

	// Add the node as a local agent container
	myMain.addRemoteContainer(desc);
	desc.setName(cid.getName());

	String name = desc.getName();
	Node node = desc.getNode();
	node.setName(name);

	// Set up a failure monitor using the blocking ping...
	NodeFailureMonitor monitor = new NodeFailureMonitor(node, new NodeEventListener() {

		// FIXME: Should notify all the interested service slices...

		public void nodeAdded(Node n) {
		    System.out.println("--- Node <" + n.getName() + "> ADDED ---");
		}

		public void nodeRemoved(Node n) {
		    System.out.println("--- Node <" + n.getName() + "> REMOVED ---");
		    removeRemoteNode(new NodeDescriptor(n.getName(), n));
		}

		public void nodeUnreachable(Node n) {
		    System.out.println("--- Node <" + n.getName() + "> UNREACHABLE ---");
		}

		public void nodeReachable(Node n) {
		    System.out.println("--- Node <" + n.getName() + "> REACHABLE ---");
		}

	});

	// Start a new node failure monitor
	Thread t = new Thread(monitor);
	t.start();

	// Return the name given to the new container
	return cid.getName();

    }

    public void removeRemoteNode(NodeDescriptor desc) {

	// Remove all the slices corresponding to the removed node
	Object[] allServices = services.values().toArray();
	for(int i = 0; i < allServices.length; i++) {
	    ServiceEntry e = (ServiceEntry)allServices[i];
	    System.out.println("Removing slice for node <" + desc.getName() + "> from service " + e.getService().getName());
	    e.removeSlice(desc.getName());
	}

	// Remove the node as a remote container
	myMain.removeRemoteContainer(desc);
    }

    public void addRemoteSlice(String serviceKey, String sliceKey, Service.Slice slice, Node remoteNode) {
	ServiceEntry e = (ServiceEntry)services.get(serviceKey);

	if(e != null) {
	    e.addSlice(sliceKey, slice, remoteNode);
	}
	else {
	    // FIXME: Should install a DummyService...
	    System.out.println("Not implemented: installing a service only on remote containers.");
	}

    }

    public void removeRemoteSlice(String serviceKey, String sliceKey) {
	// FIXME: To be implemented...
    }

    private IMTPManager myIMTPManager;
    private CommandProcessor myCommandProcessor;
    private Map services;

}
