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


    public void activateService(String name, Class itf, String sliceName, NodeRMI node) throws ServiceException, RemoteException {
	System.out.println("Activation requested of service <" + name + "> on node <" + sliceName + ">");

	try {
	    // Create a slice proxy for the new node
	    Node remoteNode = new NodeAdapter(sliceName, node);
	    Service.Slice slice = manager.createSliceProxy(name, itf, remoteNode);
	    impl.addRemoteSlice(name, sliceName, slice, remoteNode);
	}
	catch(IMTPException imtpe) {
	    throw new ServiceException("Exception while looking up the local RMI node", imtpe);
	}
    }

    public void deactivateService(String name, NodeRMI node) throws ServiceException, RemoteException {
	// FIXME: To be implemented

	// Remove the slice of the service corresponding to the calling node...

	// If no slices remain, remove also the service...

    }

    public String addNode(NodeDescriptor desc, String[] svcNames, Class[] svcInterfaces) throws ServiceException, AuthException, RemoteException {

	// Add the node to the node table
	String containerName = impl.addRemoteNode(desc);

	String name = desc.getName();
	NodeAdapter remoteNode = (NodeAdapter)desc.getNode();

	System.out.println("Adding node <" + name + "> to the platform.");

	// Activate all the node services
	List failedServices = new LinkedList();
	for(int i = 0; i < svcNames.length; i++) {
	    try {
		activateService(svcNames[i], svcInterfaces[i], name, remoteNode.getRMIStub());
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

	return containerName;
    }

    public void removeNode(NodeDescriptor desc) throws ServiceException, RemoteException {
	// FIXME: To be implemented

	// Remove all the slices corresponding to the calling node...

	// For each service, if no slices remain remove also the service...

    }

    public NodeAdapter[] findAllNodes(String serviceKey) throws ServiceException, RemoteException {
	try {
	  
	    Node[] nodes = impl.findAllNodes(serviceKey);
	    NodeAdapter[] result = new NodeAdapter[nodes.length];
	    for(int i = 0; i < result.length; i++) {
		result[i] = (NodeAdapter)nodes[i];
	    }

	    return result;
	}
	catch(IMTPException imtpe) {
	    throw new ServiceException("IMTP Error during slice list retrieval", imtpe);
	}
    }

    public NodeAdapter findSliceNode(String serviceKey, String sliceKey) throws ServiceException, RemoteException {
	try {
	    NodeAdapter node = (NodeAdapter)impl.findSliceNode(serviceKey, sliceKey);
	    return node;
	}
	catch(IMTPException imtpe) {
	    throw new ServiceException("IMTP Error during slice lookup", imtpe);
	}
    }


    private ServiceManagerImpl impl;
    private RMIIMTPManager manager;


}

