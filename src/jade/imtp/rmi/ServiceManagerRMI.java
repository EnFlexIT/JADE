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
import jade.core.NodeDescriptor;
import jade.core.ServiceException;

import jade.security.AuthException;



/**
   @author Giovanni Rimassa - FRAMeTech s.r.l

 */
interface ServiceManagerRMI extends Remote {

    // Proper ServiceManager-like methods
    String getPlatformName() throws RemoteException;
    void activateService(String name, Class itf, String sliceName, NodeRMI node) throws ServiceException, RemoteException;
    void deactivateService(String name, NodeRMI node) throws ServiceException, RemoteException;
    String addNode(NodeDescriptor desc, String[] svcNames, Class[] svcInterfaces) throws ServiceException, AuthException, RemoteException;
    void removeNode(NodeDescriptor desc) throws ServiceException, RemoteException;

    // Added ServiceFinder-like method
    NodeAdapter[] findAllNodes(String serviceKey) throws ServiceException, RemoteException;
    NodeAdapter findSliceNode(String serviceKey, String sliceKey) throws ServiceException, RemoteException;


}
