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

	public void setService(Service svc) {
	    myService = svc;
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


    private static class DummyService extends BaseService {

	public DummyService(String name, Class itf) {
	    serviceName = name;
	    horizontalInterface = itf;
	}


	public String getName() {
	    return serviceName;
	}

	public Class getHorizontalInterface() {
	    return horizontalInterface;
	}

	public Service.Slice getLocalSlice() {
	    return null;
	}

	public Filter getCommandFilter(boolean direction) {
	    return null;
	}

	public Sink getCommandSink(boolean side) {
	    return null;
	}

	public String[] getOwnedCommands() {
	    return OWNED_COMMANDS;
	}



	private static final String[] OWNED_COMMANDS = new String[0];

	private String serviceName;
	private Class horizontalInterface;


    } // End of DummyService class

    /**
       Constructs a new Service Manager implementation complying with
       a given JADE profile. This constructor is package-scoped, so
       that only the JADE kernel is allowed to create a new Service
       Manager implementation.

       @param p The platform profile describing how the JADE platform
       is to be configured.
    */
    ServiceManagerImpl(Profile p, MainContainerImpl mc) throws ProfileException, IMTPException {
	myCommandProcessor = p.getCommandProcessor();
	myIMTPManager = p.getIMTPManager();
	myMain = mc;
	services = new HashMap();
    }


    // FIXME: The association between MainContainer and ServiceManagerImpl need be clarified...
    private MainContainerImpl myMain;

    // Implementation of the ServiceManager interface

    public String getPlatformName() throws IMTPException {
	return myMain.getPlatformName();
    }

    public void addAddress(String addr) throws IMTPException {
	myIMTPManager.addServiceManagerAddress(addr);
    }

    public void removeAddress(String addr) throws IMTPException {
	myIMTPManager.removeServiceManagerAddress(addr);
    }

    public String[] getAddresses() throws IMTPException {
	return myIMTPManager.getServiceManagerAddresses();
    }

    public String getLocalAddress() throws IMTPException {
	return localAddress;
    }

    public void addNode(NodeDescriptor desc, ServiceDescriptor[] services) throws IMTPException, ServiceException, AuthException {

	Node n = desc.getNode();
	ContainerID cid = desc.getContainer();

	adjustContainerName(n, cid);

	// Add the node as a local agent container and activate the new container with the IMTP manager
	myMain.addLocalContainer(desc);
	myIMTPManager.connect(desc.getContainer());

	// Activate all the node services
	List failedServices = new LinkedList();
	for(int i = 0; i < services.length; i++) {
	    try {
		// No need to notify other Service Manager replicas
		doActivateService(services[i]);
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

	// Tell all the other service managers about the new node
	String[] svcNames = new String[services.length];
	Class[] svcInterfaces = new Class[services.length];

	// Fill the parameter arrays
	for(int i = 0; i < services.length; i++) {
	    svcNames[i] = services[i].getName();
	    svcInterfaces[i] = services[i].getService().getHorizontalInterface();
	}

	myIMTPManager.nodeAdded(desc, svcNames, svcInterfaces, nodeNo, mainNodeNo);

    }

    public void removeNode(NodeDescriptor desc) throws IMTPException, ServiceException {
	// Retrieve the node information
	Node localNode = desc.getNode();
	if(!localNode.equals(myIMTPManager.getLocalNode())) {
	    throw new ServiceException("A remote node cannot be removed with this method call");
	}

	// Deactivate all the node services
	Object[] names = services.keySet().toArray();
	for(int i = 0; i < names.length; i++) {
	    try {
		String svcName = (String)names[i];
		ServiceEntry e = (ServiceEntry)services.get(svcName);
		Service svc = e.getService();

		ServiceDescriptor svcDesc = new ServiceDescriptor(svcName, svc);

		// No need to notify other Service Manager replicas
		doDeactivateService(svcDesc);
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
	myMain.removeLocalContainer(desc.getContainer());

	// Tell all the other service managers about the removed node
	myIMTPManager.nodeRemoved(desc);

    }

    public void activateService(ServiceDescriptor desc) throws IMTPException, ServiceException {
	doActivateService(desc);
	myIMTPManager.serviceActivated(desc.getName(), desc.getService().getHorizontalInterface(), myIMTPManager.getLocalNode());
    }

    public void deactivateService(ServiceDescriptor desc) throws IMTPException, ServiceException {
	doDeactivateService(desc);
	myIMTPManager.serviceDeactivated(desc.getName(), myIMTPManager.getLocalNode());
    }



    // Implementation of the ServiceFinder interface


    public Service findService(String key) throws IMTPException, ServiceException {
	ServiceEntry e = (ServiceEntry)services.get(key);
	if(e == null) {
	    return null;
	}
	else {
	    return e.getService();
	}
    }

    public Service.Slice findSlice(String serviceKey, String sliceKey) throws IMTPException, ServiceException {

	ServiceEntry e = (ServiceEntry)services.get(serviceKey);

	if(e == null) {
	    return null;
	}
	else {

	    // If the special MAIN_SLICE name is used, return the local slice
	    if(CaseInsensitiveString.equalsIgnoreCase(sliceKey, MAIN_SLICE)) {
		sliceKey = myIMTPManager.getLocalNode().getName();
	    }

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

	    // If the special MAIN_SLICE name is used, return the local slice
	    if(CaseInsensitiveString.equalsIgnoreCase(nodeKey, MAIN_SLICE)) {
		nodeKey = myIMTPManager.getLocalNode().getName();
	    }

	    return e.getNode(nodeKey);
	}
    }



    // Slice management methods

    public String addRemoteNode(NodeDescriptor desc, boolean control) throws AuthException {

	Node n = desc.getNode();
	ContainerID cid = desc.getContainer();

	adjustContainerName(n, cid);

	// Add the node as a remote agent container
	myMain.addRemoteContainer(desc);
	desc.setName(cid.getName());

	String name = desc.getName();
	Node node = desc.getNode();
	node.setName(name);

	if(control) {
	    monitor(n);
	}

	// Return the name given to the new container
	return cid.getName();

    }

    public void removeRemoteNode(NodeDescriptor desc, boolean propagate) throws IMTPException {

	System.out.println("Removing node <" + desc.getName() + "> from the platform");

	// Remove the node as a remote container
	myMain.removeRemoteContainer(desc);

	// Remove all the slices corresponding to the removed node
	Object[] allServices = services.values().toArray();
	for(int i = 0; i < allServices.length; i++) {
	    ServiceEntry e = (ServiceEntry)allServices[i];
	    //	    System.out.println("Removing slice for node <" + desc.getName() + "> from service " + e.getService().getName());
	    e.removeSlice(desc.getName());
	}

	if(propagate) {
	    // Tell all the other service managers about the removed node
	    myIMTPManager.nodeRemoved(desc);
	}

    }

    public void addRemoteSlice(String serviceKey, String sliceKey, Service.Slice slice, Node remoteNode) {
	ServiceEntry e = (ServiceEntry)services.get(serviceKey);

	if(e == null) {
		// The service is not installed on this Main container --> Add a Dummy entry
	    Service svc = new DummyService(serviceKey, Service.Slice.class);
	    e = new ServiceEntry(svc);
	    services.put(serviceKey, e);
	}

	e.addSlice(sliceKey, slice, remoteNode);
	
	GenericCommand gCmd = new GenericCommand(Service.NEW_SLICE, serviceKey, null);
	gCmd.addParam(sliceKey);
	myCommandProcessor.processIncoming(gCmd);
    }

    public void removeRemoteSlice(String serviceKey, String sliceKey) {
	// FIXME: To be implemented...
    }

    public void setNodeCounters(int nodeCnt, int mainCnt) {
	nodeNo = nodeCnt;
	mainNodeNo = mainCnt;
    }

    public int getNodeCounter() {
	return nodeNo;
    }

    public int getMainNodeCounter() {
	return mainNodeNo;
    }

    public void setLocalAddress(String addr) {
	localAddress = addr;
    }

    public static class NodeInfo {

	public NodeInfo(Node n, Object[]names, Object[] interfaces) {

	    ContainerID cid = new ContainerID(n.getName(), null);
	    nodeDesc = new NodeDescriptor(cid, n, "", new byte[0]); // FIXME: Temporary Hack

	    svcNames = new String[names.length];
	    for(int i = 0; i < names.length; i++) {
		svcNames[i] = (String)names[i];
	    }

	    svcInterfaces = new Class[interfaces.length];
	    for(int i = 0; i < interfaces.length; i++) {
		svcInterfaces[i] = (Class)interfaces[i];
	    }

	}

	public NodeDescriptor getNodeDescriptor() {
	    return nodeDesc;
	}

	public String[] getServiceNames() {
	    return svcNames;
	}

	public Class[] getServiceInterfaces() {
	    return svcInterfaces;
	}

	public String[] getServiceInterfacesNames() {
	    String[] names = new String[svcInterfaces.length];
	    for(int i = 0; i < names.length; i++) {
		names[i] = svcInterfaces[i].getName();
	    }

	    return names;
	}

	private NodeDescriptor nodeDesc;
	private String[] svcNames;
	private Class[] svcInterfaces;

    }

    public List getAllNodesInfo() {

	// Map: Node name -> List of service names
	Map serviceNames = new HashMap();

	// Map: Node name -> List of horizontal interfaces
	Map serviceInterfaces = new HashMap();

	// Map: Node name -> Node
	Map nodes = new HashMap();

	Iterator it = services.values().iterator();
	while(it.hasNext()) {
	    ServiceEntry e = (ServiceEntry)it.next();
	    Node[] serviceNodes = e.getNodes();

	    for(int i = 0; i < serviceNodes.length; i++) {
		String nodeName = serviceNodes[i].getName();

		List l1 = (List)serviceNames.get(nodeName);
		if(l1 == null) {
		    l1 = new LinkedList();
		    serviceNames.put(nodeName, l1);
		}
		l1.add(e.getService().getName());

		List l2 = (List)serviceInterfaces.get(nodeName);
		if(l2 == null) {
		    l2 = new LinkedList();
		    serviceInterfaces.put(nodeName, l2);
		}
		l2.add(e.getService().getHorizontalInterface());

		Node n = (Node)nodes.get(nodeName);
		if(n == null) {
		    nodes.put(nodeName, serviceNodes[i]);
		}
	    }

	}

	List result = new LinkedList();
	it = nodes.keySet().iterator();
	while(it.hasNext()) {
	    String nodeName = (String)it.next();
	    Node n = (Node)nodes.get(nodeName);

	    Object[] sn = ((List)serviceNames.get(nodeName)).toArray();
	    Object[] si = ((List)serviceInterfaces.get(nodeName)).toArray();

	    result.add(new NodeInfo(n, sn, si));
	}

	return result;

    }

    private IMTPManager myIMTPManager;
    private CommandProcessor myCommandProcessor;

    private String localAddress;
    private List addresses;
    private Map services;

    // These variables hold two progressive numbers just used to name new nodes.
    // By convention, nodes with a local copy of the Service Manager are called
    // Main-Container-<N>, whereas nodes without their own Service Manager are
    // called Container-<M>.
    private int nodeNo = 1;
    private int mainNodeNo = 0;


    private void adjustContainerName(Node n, ContainerID cid) {

	// Do nothing if a custom name is already supplied
	if((cid != null) && !cid.getName().equals(AgentManager.UNNAMED_CONTAINER_NAME)) {
	    return;
	}

	if(n.hasServiceManager()) {

	    // Use the Main-Container-<N> name schema
	    if(mainNodeNo == 0) {
		cid.setName(AgentManager.MAIN_CONTAINER_NAME);
	    }
	    else {
		cid.setName(AgentManager.MAIN_CONTAINER_NAME + '-' + mainNodeNo);
	    }
	    try {
		while(true) {
		    // Try until a non-existing name is found...
		    myMain.getContainerNode(cid);
		    mainNodeNo++;
		    cid.setName(AgentManager.MAIN_CONTAINER_NAME + '-' + mainNodeNo);
		}
	    }
	    catch(NotFoundException nfe) {
		// There is no such named container, so the name is OK.
	    }
	}
	else {

	    // Use the Container-<M> name schema
	    cid.setName(AgentManager.AUX_CONTAINER_NAME + '-' + nodeNo);
	    try {
		while(true) {
		    // Try until a non-existing name is found...
		    myMain.getContainerNode(cid);
		    nodeNo++;
		    cid.setName(AgentManager.AUX_CONTAINER_NAME + '-' + nodeNo);
		}
	    }
	    catch(NotFoundException nfe) {
		// There is no such named container, so the name is OK.
	    }
	}
    }

    private void doActivateService(ServiceDescriptor desc) throws IMTPException, ServiceException {
	String name = desc.getName();
	Service svc = desc.getService();
	ServiceEntry e = (ServiceEntry)services.get(name);
	if(e != null) {
	    Service old = e.getService();
	    if(old instanceof DummyService) {
		// A dummy service: replace it with the real thing, while keeping the slice table
		e.setService(svc);
	    }
	    else {
		// A real service is already installed: abort activation
		throw new ServiceException("A service named <" + name + "> is already active.");
	    }
	}
	else {
	    // Create an entry for the new service
	    e = new ServiceEntry(svc);
	    services.put(name, e);
	}

	// Export the *REAL* local slice so that it can be reached through the network
	Service.Slice localSlice = svc.getLocalSlice();
	if(localSlice != null) {
	    myIMTPManager.exportSlice(name, localSlice);
	}

	// Put the *PROXY* local slice into the Service Finder so that it can be found later
	Node here = myIMTPManager.getLocalNode();
	Service.Slice localProxy = myIMTPManager.createSliceProxy(name, null, here);
	e.addSlice(here.getName(), localProxy, here);

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
	Sink sSrc = svc.getCommandSink(Sink.COMMAND_SOURCE);
	if(sSrc != null) {
	    myCommandProcessor.registerSink(sSrc, Sink.COMMAND_SOURCE, svc.getName());
	}
	Sink sTgt = svc.getCommandSink(Sink.COMMAND_TARGET);
	if(sTgt != null) {
	    myCommandProcessor.registerSink(sTgt, Sink.COMMAND_TARGET, svc.getName());
	}

	// Register the service-specific behaviour (if any) within the AMS
	Behaviour b = svc.getAMSBehaviour();
	if(b != null) {
	    myMain.installAMSBehaviour(b);
	}

    }

    private void doDeactivateService(ServiceDescriptor desc) throws IMTPException, ServiceException {
	String name = desc.getName();
	ServiceEntry e = (ServiceEntry)services.get(name);

	if(e != null) {

	    Service svc = e.getService();

	    // Deregister the service-specific behaviour (if any) within the AMS
	    Behaviour b = svc.getAMSBehaviour();
	    if(b != null) {
		myMain.uninstallAMSBehaviour(b);
	    }

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
	    Sink sSrc = svc.getCommandSink(Sink.COMMAND_SOURCE);
	    if(sSrc != null) {
		myCommandProcessor.deregisterSink(Sink.COMMAND_SOURCE, svc.getName());
	    }
	    Sink sTgt = svc.getCommandSink(Sink.COMMAND_TARGET);
	    if(sTgt != null) {
		myCommandProcessor.deregisterSink(Sink.COMMAND_TARGET, svc.getName());
	    }

	    // Uninstall the service
	    services.remove(name);
	}

    }

    public void monitor(Node target) {

	// Set up a failure monitor using the blocking ping...
	NodeFailureMonitor failureMonitor = new NodeFailureMonitor(target, new NodeEventListener() {

		// FIXME: Should notify all the interested service slices...

		public void nodeAdded(Node n) {
		    System.out.println("--- Node <" + n.getName() + "> ADDED ---");
		}

		public void nodeRemoved(Node n) {
		    System.out.println("--- Node <" + n.getName() + "> TERMINATED ---");
		    try {
			removeRemoteNode(new NodeDescriptor(n.getName(), n), true);
		    }
		    catch(IMTPException imtpe) {
			imtpe.printStackTrace();
		    }
		}

		public void nodeUnreachable(Node n) {
		    System.out.println("--- Node <" + n.getName() + "> UNREACHABLE ---");
		}

		public void nodeReachable(Node n) {
		    System.out.println("--- Node <" + n.getName() + "> REACHABLE ---");
		}

	    });

	// Start a new node failure monitor
	Thread t = new Thread(failureMonitor);
	t.start();
    }

}
