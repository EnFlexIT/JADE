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

package jade.imtp.leap;


import jade.core.Node;
import jade.core.NodeDescriptor;
import jade.core.Service;
import jade.core.ServiceManagerImpl;
import jade.core.ServiceException;
import jade.core.IMTPException;

import jade.security.AuthException;

import jade.util.leap.List;
import jade.util.leap.LinkedList;
import jade.util.leap.Iterator;


/**

   The <code>ServiceManagerSkel</code> class is the remote
   adapter for JADE platform <i>Service Manager</i> and
   <i>Service Finder</i> components, running over LEAP transport layer.

   @author Giovanni Rimassa - FRAMeTech s.r.l.
*/
class ServiceManagerSkel extends Skeleton {

    private ServiceManagerImpl impl;
    private LEAPIMTPManager manager;

    public ServiceManagerSkel(ServiceManagerImpl sm, LEAPIMTPManager mgr) {
	impl = sm;
	manager = mgr;
    }

    public Command executeCommand(Command command) throws Throwable {
	Command resp = null;

	switch (command.getCode()) {

	case Command.GET_PLATFORM_NAME: {

	    // Execute command...
	    System.out.println("-- GET_PLATFORM_NAME --");
	    String name = impl.getPlatformName();

	    resp = new Command(Command.OK);
	    resp.addParam(name);

	    break;
	} 

	case Command.ADD_NODE: {
	    NodeDescriptor desc = (NodeDescriptor)command.getParamAt(0);
	    String[] svcNames = (String[])command.getParamAt(1);
	    String[] svcInterfaceNames = (String[])command.getParamAt(2);

	    // Fill a Class array from the names array
	    Class[] svcInterfaces = new Class[svcInterfaceNames.length];
	    for(int i = 0; i < svcInterfaceNames.length; i++) {
		svcInterfaces[i] = Class.forName(svcInterfaceNames[i]);
	    }

	    // Execute command...
	    String name = addNode(desc, svcNames, svcInterfaces);

	    resp = new Command(Command.OK);
	    resp.addParam(name);

	    break;
	} 

	case Command.REMOVE_NODE: {
	    NodeDescriptor desc = (NodeDescriptor)command.getParamAt(0);

	    // Execute command...
	    removeNode(desc);

	    resp = new Command(Command.OK);

	    break;
	} 

	case Command.ACTIVATE_SERVICE: {
	    String svcName = (String)command.getParamAt(0);
	    String itfName = (String)command.getParamAt(1);
	    NodeDescriptor where = (NodeDescriptor)command.getParamAt(2);

	    // Execute command...
	    Class itf = Class.forName(itfName);
	    activateService(svcName, itf, where);

	    resp = new Command(Command.OK);

	    break;
	} 

	case Command.FIND_SLICE_NODE: {
	    String serviceKey = (String)command.getParamAt(0);
	    String sliceKey = (String)command.getParamAt(1);

	    // Execute command...
	    Node n = findSliceNode(serviceKey, sliceKey);
      
	    resp = new Command(Command.OK);
	    resp.addParam(n);

	    break;
	} 

	case Command.FIND_ALL_NODES: {
	    String serviceKey = (String)command.getParamAt(0);

	    // Do something...
	    Node[] nodes = findAllNodes(serviceKey);

	    resp = new Command(Command.OK);
	    resp.addParam(nodes);

	    break;
	}

	}

	return resp;
    }

    private void activateService(String name, Class itf, NodeDescriptor desc) throws ServiceException, IMTPException {

	String sliceName = desc.getName();
	Node remoteNode = desc.getNode();

	System.out.println("Activation requested of service <" + name + "> on node <" + sliceName + ">");

	// Create a slice proxy for the new node
	Service.Slice slice = manager.createSliceProxy(name, itf, remoteNode);
	impl.addRemoteSlice(name, sliceName, slice, remoteNode);

    }

    private void deactivateService(String name, NodeDescriptor desc) throws ServiceException, IMTPException {
	// FIXME: To be implemented

	// Remove the slice of the service corresponding to the calling node...

	// If no slices remain, remove also the service...

    }

    private String addNode(NodeDescriptor desc, String[] svcNames, Class[] svcInterfaces) throws ServiceException, AuthException, IMTPException {

	// Add the node to the node table
	String containerName = impl.addRemoteNode(desc);

	String name = desc.getName();
	NodeAdapter remoteNode = (NodeAdapter)desc.getNode();

	System.out.println("Adding node <" + name + "> to the platform.");

	// Activate all the node services
	List failedServices = new LinkedList();
	for(int i = 0; i < svcNames.length; i++) {
	    try {
		activateService(svcNames[i], svcInterfaces[i], desc);
	    }
	    catch(IMTPException imtpe) {
		// This should never happen, because it's a local call...
		imtpe.printStackTrace();
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

    private void removeNode(NodeDescriptor desc) throws ServiceException, IMTPException {
	// FIXME: To be implemented

	// Remove all the slices corresponding to the calling node...

	// For each service, if no slices remain remove also the service...

    }

    private Node[] findAllNodes(String serviceKey) throws ServiceException, IMTPException {
	try {
	    return impl.findAllNodes(serviceKey);
	}
	catch(IMTPException imtpe) {
	    throw new ServiceException("IMTP Error during slice list retrieval", imtpe);
	}
    }

    private Node findSliceNode(String serviceKey, String sliceKey) throws ServiceException, IMTPException {
	try {
	    return (NodeAdapter)impl.findSliceNode(serviceKey, sliceKey);
	}
	catch(IMTPException imtpe) {
	    throw new ServiceException("IMTP Error during slice lookup", imtpe);
	}
    }


}
