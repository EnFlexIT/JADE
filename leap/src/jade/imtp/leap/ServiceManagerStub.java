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

import jade.core.*;

import jade.security.AuthException;


/***
import jade.util.leap.List;
import jade.util.leap.ArrayList;
import jade.lang.acl.ACLMessage;
import jade.mtp.TransportAddress;
import jade.mtp.MTPException;
import jade.mtp.MTPDescriptor;
import java.io.IOException;

import jade.security.AgentPrincipal;
import jade.security.ContainerPrincipal;
import jade.security.CertificateFolder;
***/



/**

   The <code>ServiceManagerStub</code> class is the remote
   proxy of JADE platform <i>Service Manager</i> and
   <i>Service Finder</i> components, running over LEAP transport layer.

   @author Giovanni Rimassa - FRAMeTech s.r.l.
*/
class ServiceManagerStub extends Stub {

    private static final String AUTH_EXCEPTION = "jade.security.AuthException";

    /**
     * Constructor declaration
     * 
     */
    protected ServiceManagerStub() {
	super();
    }


    public String getPlatformName() throws IMTPException {

	Command cmd = new Command(Command.GET_PLATFORM_NAME, remoteID);

	try {
	    Command result = theDispatcher.dispatchCommand(remoteTAs, cmd);
	    checkResult(result, new String[] { });

	    return (String)result.getParamAt(0);
	}
	catch (DispatcherException de) {
	    throw new IMTPException(DISP_ERROR_MSG, de);
	}
	catch (UnreachableException ue) {
	    throw new IMTPException(UNRCH_ERROR_MSG, ue);
	}
    }

    public String addNode(NodeDescriptor desc, String[] svcNames, String[] svcInterfaces, boolean propagate) throws IMTPException, ServiceException, AuthException {

	try {

	    // Now register this node and all its services with the Service Manager
	    Command cmd = new Command(Command.ADD_NODE, remoteID);

	    cmd.addParam(desc);
	    cmd.addParam(svcNames);
	    cmd.addParam(svcInterfaces);
	    cmd.addParam(new Boolean(propagate));

	    Command result = theDispatcher.dispatchCommand(remoteTAs, cmd);

	    // Check whether an exception occurred in the remote container
	    if (checkResult(result, new String[] { AUTH_EXCEPTION }) > 0) {
		throw new AuthException((String) result.getParamAt(1));
	    }

	    return (String)result.getParamAt(0);

	}
	catch (DispatcherException de) {
	    throw new IMTPException(DISP_ERROR_MSG, de);
	} 
	catch (UnreachableException ue) {
	    throw new IMTPException(UNRCH_ERROR_MSG, ue);
	}
    }

    public void removeNode(NodeDescriptor desc, boolean propagate) throws IMTPException, ServiceException {
	try {

	    // First, deregister this node with the service manager
	    Command cmd = new Command(Command.REMOVE_NODE, remoteID);
	    cmd.addParam(desc);
	    cmd.addParam(new Boolean(propagate));
	    Command res = theDispatcher.dispatchCommand(remoteTAs, cmd);

	    // Check whether an exception occurred in the remote container
	    checkResult(res, new String[] { });
	}
	catch (DispatcherException de) {
	    throw new IMTPException(DISP_ERROR_MSG, de);
	} 
	catch (UnreachableException ue) {
	    throw new IMTPException(UNRCH_ERROR_MSG, ue);
	}
    }

    public void activateService(String svcName, Class itf, NodeDescriptor where, boolean propagate) throws IMTPException, ServiceException {
	try {

	    // Activate the service with the remote Service Manager
	    Command cmd = new Command(Command.ACTIVATE_SERVICE, remoteID);
	    cmd.addParam(svcName);
	    cmd.addParam(itf.getName());
	    cmd.addParam(where);
	    cmd.addParam(new Boolean(propagate));

	    Command res = theDispatcher.dispatchCommand(remoteTAs, cmd);

	    // Check whether an exception occurred in the remote container
	    checkResult(res, new String[] {  });

	}
	catch (DispatcherException de) {
	    throw new IMTPException(DISP_ERROR_MSG, de);
	}
	catch (UnreachableException ue) {
	    throw new IMTPException(UNRCH_ERROR_MSG, ue);
	}
    }

    public void deactivateService(String name, NodeDescriptor desc, boolean propagate) throws IMTPException, ServiceException {
	// FIXME: To be implemented

	// Remove the slice of the service corresponding to the calling node...

	// If no slices remain, remove also the service...
    }




    public Node findSliceNode(String serviceKey, String sliceKey) throws IMTPException, ServiceException {
	try {

	    // Find the node with the help of the remote Service Manager
	    Command cmd = new Command(Command.FIND_SLICE_NODE, remoteID);
	    cmd.addParam(serviceKey);
	    cmd.addParam(sliceKey);

	    Command result = theDispatcher.dispatchCommand(remoteTAs, cmd);

	    // Check whether an exception occurred in the remote container
	    checkResult(result, new String[] { });

	    return (Node)result.getParamAt(0);
	}
	catch (DispatcherException de) {
	    throw new IMTPException(DISP_ERROR_MSG, de);
	}
	catch (UnreachableException ue) {
	    throw new IMTPException(UNRCH_ERROR_MSG, ue);
	}
    }

    public Node[] findAllNodes(String serviceKey) throws IMTPException, ServiceException {
	try {

	    // Find all the nodes with the help of the remote Service Manager
	    Command cmd = new Command(Command.FIND_ALL_NODES, remoteID);
	    cmd.addParam(serviceKey);

	    Command result = theDispatcher.dispatchCommand(remoteTAs, cmd);

	    // Check whether an exception occurred in the remote container
	    checkResult(result, new String[] { });

	    return (Node[])result.getParamAt(0);
	}
	catch (DispatcherException de) {
	    throw new IMTPException(DISP_ERROR_MSG, de);
	}
	catch (UnreachableException ue) {
	    throw new IMTPException(UNRCH_ERROR_MSG, ue);
	}
    }

    public void ping() throws IMTPException {
	try {

	    Command cmd = new Command(Command.SERVICE_MANAGER_PING, remoteID);

	    Command result = theDispatcher.dispatchCommand(remoteTAs, cmd);

	    // Check whether an exception occurred in the remote container
	    checkResult(result, new String[] { });

	}
	catch (DispatcherException de) {
	    throw new IMTPException(DISP_ERROR_MSG, de);
	}
	catch (UnreachableException ue) {
	    throw new IMTPException(UNRCH_ERROR_MSG, ue);
	}
    }

    public String[] addReplica(String addr) throws IMTPException {
	try {

	    // Find all the nodes with the help of the remote Service Manager
	    Command cmd = new Command(Command.SERVICE_MANAGER_ADD_REPLICA, remoteID);
	    cmd.addParam(addr);

	    Command result = theDispatcher.dispatchCommand(remoteTAs, cmd);

	    // Check whether an exception occurred in the remote container
	    checkResult(result, new String[] { });

	    return (String[])result.getParamAt(0);
	}
	catch (DispatcherException de) {
	    throw new IMTPException(DISP_ERROR_MSG, de);
	}
	catch (UnreachableException ue) {
	    throw new IMTPException(UNRCH_ERROR_MSG, ue);
	}
    }

    public void updateCounters(int nodeCnt, int mainCnt) throws IMTPException {
	try {

	    Command cmd = new Command(Command.SERVICE_MANAGER_UPDATE_COUNTERS, remoteID);
	    cmd.addParam(new Integer(nodeCnt));
	    cmd.addParam(new Integer(mainCnt));

	    Command result = theDispatcher.dispatchCommand(remoteTAs, cmd);

	    // Check whether an exception occurred in the remote container
	    checkResult(result, new String[] { });

	}
	catch (DispatcherException de) {
	    throw new IMTPException(DISP_ERROR_MSG, de);
	}
	catch (UnreachableException ue) {
	    throw new IMTPException(UNRCH_ERROR_MSG, ue);
	}
    }

}
