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

package jade.imtp.rmi;

import java.rmi.RemoteException;
import java.rmi.Naming;
import java.rmi.server.UnicastRemoteObject;

import jade.core.IMTPException;
import jade.core.Node;
import jade.core.NodeDescriptor;
import jade.core.Service;
import jade.core.ServiceManagerImpl;
import jade.core.ServiceException;

import jade.security.AuthException;

import jade.util.leap.List;
import jade.util.leap.LinkedList;
import jade.util.leap.Iterator;


/**
   @author Giovanni Rimassa - FRAMeTech s. r. l.
 */
public class ServiceManagerRMIImpl extends UnicastRemoteObject implements ServiceManagerRMI {


    /** Creates new ServiceManagerRMIImpl */
    public ServiceManagerRMIImpl(ServiceManagerImpl sm, RMIIMTPManager mgr) throws RemoteException {
	super(0, mgr.getClientSocketFactory(), mgr.getServerSocketFactory());
	impl = sm;
	manager = mgr;
    }


    public String getPlatformName() throws RemoteException {
	try {
	    return impl.getPlatformName();
	}
	catch(IMTPException imtpe) {
	    // It should never happen, since this is a local call
	    throw new RemoteException("IMTPException in local call");
	}
    }

    public void activateService(String name, Class itf, NodeDescriptor desc, boolean propagate) throws ServiceException, RemoteException {

	String sliceName = desc.getName();
	Node remoteNode = desc.getNode();

	//	System.out.println("Activation requested of service <" + name + "> on node <" + sliceName + ">");
	try {
	    // Create a slice proxy for the new node
	    Service.Slice slice = manager.createSliceProxy(name, itf, remoteNode);
	    impl.addRemoteSlice(name, sliceName, slice, remoteNode);

	    if(propagate) {
		manager.serviceActivated(name, itf, remoteNode);
	    }
	}
	catch(IMTPException imtpe) {
	    throw new ServiceException("Exception while looking up the local RMI node", imtpe);
	}
    }

    public void deactivateService(String name, NodeDescriptor desc, boolean propagate) throws ServiceException, RemoteException {
	// FIXME: To be implemented

	// Remove the slice of the service corresponding to the calling node...

	// If no slices remain, remove also the service...

    }

    public String addNode(NodeDescriptor desc, String[] svcNames, Class[] svcInterfaces, boolean propagate) throws ServiceException, AuthException, RemoteException {

	// Add the node to the node table
	String containerName = impl.addRemoteNode(desc, propagate);

	String name = desc.getName();
	System.out.println("Adding node <" + name + "> to the platform.");

	// Activate all the node services
	List failedServices = new LinkedList();
	for(int i = 0; i < svcNames.length; i++) {
	    try {
		activateService(svcNames[i], svcInterfaces[i], desc, propagate);
	    }
	    catch(RemoteException re) {
		// This should never happen, because it's a local call...
		re.printStackTrace();
	    }
	    catch(ServiceException se) {
		failedServices.add(svcNames[i]);
	    }
	}

	// Throw a failure exception, if needed
	if(!failedServices.isEmpty()) {

	    // All service activations failed: throw a single exception 
	    if(failedServices.size() == svcNames.length) {
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

	if(propagate) {
	    try {
		manager.nodeAdded(desc, svcNames, svcInterfaces, impl.getNodeCounter(), impl.getMainNodeCounter());
	    }
	    catch(IMTPException imtpe) {
		throw new ServiceException("IMTP Error during node addition notification", imtpe);
	    }
	}

	return containerName;
    }

    public void removeNode(NodeDescriptor desc, boolean propagate) throws ServiceException, RemoteException {
	try {
	    impl.removeRemoteNode(desc, propagate);
	}
	catch(IMTPException imtpe) {
	    throw new ServiceException("IMTP Error during node removal notification", imtpe);
	}

    }

    public Node[] findAllNodes(String serviceKey) throws ServiceException, RemoteException {
	try {
	    return impl.findAllNodes(serviceKey);
	}
	catch(IMTPException imtpe) {
	    throw new ServiceException("IMTP Error during slice list retrieval", imtpe);
	}
    }

    public Node findSliceNode(String serviceKey, String sliceKey) throws ServiceException, RemoteException {
	try {
	    return (NodeAdapter)impl.findSliceNode(serviceKey, sliceKey);
	}
	catch(IMTPException imtpe) {
	    throw new ServiceException("IMTP Error during slice lookup", imtpe);
	}
    }

    public void adopt(Node n) throws RemoteException {
	impl.monitor(n);
    }

    public String[] addReplica(String addr) throws RemoteException {

	try {
	    // Retrieve the RMI object for the replica...
	    ServiceManagerRMI replica = (ServiceManagerRMI)Naming.lookup(addr + RMIIMTPManager.SERVICE_MANAGER_NAME);

	    // Send all nodes with their installed services...
	    List infos = impl.getAllNodesInfo();

	    Iterator it = infos.iterator();
	    while(it.hasNext()) {
		ServiceManagerImpl.NodeInfo info = (ServiceManagerImpl.NodeInfo)it.next();
		replica.addNode(info.getNodeDescriptor(), info.getServiceNames(), info.getServiceInterfaces(), false);
	    }

	    replica.updateCounters(impl.getNodeCounter(), impl.getMainNodeCounter());
	    return manager.getServiceManagerAddresses();
	}
	catch(Exception e) {
	    e.printStackTrace();
	    return new String[0];
	}
    }

    public void updateCounters(int nodeCnt, int mainCnt) throws RemoteException {
	impl.setNodeCounters(nodeCnt, mainCnt);
    }


    private ServiceManagerImpl impl;
    private RMIIMTPManager manager;


}

