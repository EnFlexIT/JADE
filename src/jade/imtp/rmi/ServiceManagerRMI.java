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


import java.rmi.Remote;
import java.rmi.RemoteException;

import jade.core.Service;
import jade.core.Node;
import jade.core.NodeDescriptor;
import jade.core.ServiceException;

import jade.security.AuthException;



/**
   @author Giovanni Rimassa - FRAMeTech s.r.l

 */
interface ServiceManagerRMI extends Remote {

    // Proper ServiceManager-like methods
    String getPlatformName() throws RemoteException;
    void activateService(String name, Class itf, NodeDescriptor desc, boolean propagate) throws ServiceException, RemoteException;
    void deactivateService(String name, NodeDescriptor desc, boolean propagate) throws ServiceException, RemoteException;
    String addNode(NodeDescriptor desc, String[] svcNames, Class[] svcInterfaces, boolean propagate) throws ServiceException, AuthException, RemoteException;
    void removeNode(NodeDescriptor desc, boolean propagate) throws ServiceException, RemoteException;

    // Added ServiceFinder-like method
    Node[] findAllNodes(String serviceKey) throws ServiceException, RemoteException;
    Node findSliceNode(String serviceKey, String sliceKey) throws ServiceException, RemoteException;

    // Service methods
    void adopt(Node n) throws RemoteException;
    String[] addReplica(String addr) throws RemoteException;
    void updateCounters(int nodeCnt, int mainCnt) throws RemoteException;

}
