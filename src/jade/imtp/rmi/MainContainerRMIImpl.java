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

import jade.core.AID;
import jade.core.ContainerID;
//import jade.core.MainContainerImpl;
import jade.core.MainContainer;
import jade.core.IMTPException;
import jade.core.NotFoundException;
import jade.core.NameClashException;
import jade.core.RemoteProxy;

/**
   @author Giovanni Rimassa - Universita` di Parma
   @version $Date$ $Revision$
 */
public class MainContainerRMIImpl extends UnicastRemoteObject implements MainContainerRMI {

    //private MainContainerImpl impl;
    private MainContainer impl;

    /** Creates new MainContainerRMIImpl */
    //public MainContainerRMIImpl(MainContainerImpl mc) throws RemoteException {
    public MainContainerRMIImpl(MainContainer mc) throws RemoteException {
      impl = mc;
    }

    public AgentContainerRMI lookup(ContainerID cid) throws RemoteException, NotFoundException, IMTPException {
      return impl.lookup(cid).getAdapter().adaptee;
    }
    
    public void deadMTP(String mtpAddress, ContainerID cid) throws RemoteException, IMTPException {
      impl.deadMTP(mtpAddress, cid);
    }
    
    public RemoteProxy getProxy(AID id) throws RemoteException, NotFoundException, IMTPException {
      return impl.getProxy(id);
    }
    
    public boolean transferIdentity(AID agentID, ContainerID src, ContainerID dest) throws RemoteException, NotFoundException, IMTPException {
      return impl.transferIdentity(agentID, src, dest);
    }
    
    public String getPlatformName() throws RemoteException, IMTPException {
      return impl.getPlatformName();
    }
    
    public void bornAgent(AID name, RemoteProxy rp, ContainerID cid) throws RemoteException, NameClashException, IMTPException {
      impl.bornAgent(name, rp, cid);
    }
    
    public void removeContainer(ContainerID cid) throws RemoteException, IMTPException {
      impl.removeContainer(cid);
    }
    
    public String addContainer(AgentContainerRMI ac, ContainerID cid) throws RemoteException, IMTPException {
      return impl.addContainer(new AgentContainerAdapter(ac), cid);
    }
    
    public void deadAgent(AID name) throws RemoteException, NotFoundException, IMTPException {
      impl.deadAgent(name);
    }
    
    public void newMTP(String mtpAddress, ContainerID cid) throws RemoteException, IMTPException {
      impl.deadMTP(mtpAddress, cid);
    }
    
}
