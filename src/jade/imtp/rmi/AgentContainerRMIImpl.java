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

import java.rmi.*;
import java.rmi.server.*;

import jade.util.leap.List;

import jade.core.AgentContainer;
import jade.core.AID;
import jade.core.Location;
import jade.core.NotFoundException;
import jade.core.IMTPException;

import jade.lang.acl.ACLMessage;

import jade.mtp.MTPException;
import jade.mtp.MTPDescriptor;

import jade.security.AgentPrincipal;

/**
   @author Giovanni Rimassa - Universita` di Parma
   @version $Date$ $Revision$
 */
public class AgentContainerRMIImpl extends UnicastRemoteObject implements AgentContainerRMI {

    private AgentContainer impl;
    private RMIIMTPManager manager;

    /** Creates new AgentContainerRMIImpl */
    public AgentContainerRMIImpl(AgentContainer ac, RMIIMTPManager mgr) throws RemoteException {
      impl = ac;
      manager = mgr;
    }

    public byte[] fetchClassFile(String name) throws RemoteException, ClassNotFoundException, IMTPException {
      return impl.fetchClassFile(name);
    }
    
    public void disableDebugger(AID debuggerName, AID notToBeDebugged) throws RemoteException, IMTPException {
      impl.disableDebugger(debuggerName, notToBeDebugged);
    }
    
    public void copyAgent(AID agentID, Location where, String newName) throws RemoteException, NotFoundException, IMTPException {
      impl.copyAgent(agentID, where, newName);
    }
    
    public void exit() throws RemoteException, IMTPException {
      impl.exit();
    }
    
    public void uninstallMTP(String address) throws RemoteException, NotFoundException, MTPException, IMTPException {
      impl.uninstallMTP(address);
    }
    
    public void changeAgentPrincipal(AID agentID, AgentPrincipal newPrincipal) throws RemoteException, NotFoundException, IMTPException {
      impl.changeAgentPrincipal(agentID, newPrincipal);
    }
    
    public void suspendAgent(AID agentID) throws RemoteException, NotFoundException, IMTPException {
      impl.suspendAgent(agentID);
    }
    
    public MTPDescriptor installMTP(String address, String className) throws RemoteException, MTPException, IMTPException {
      return impl.installMTP(address, className);
    }
    
    public void wakeAgent(AID agentID) throws RemoteException, NotFoundException, IMTPException {
      impl.wakeAgent(agentID);
    }
    
    public void dispatch(ACLMessage msg, AID receiverID) throws RemoteException, NotFoundException, IMTPException {
      impl.dispatch(msg, receiverID);
    }
    
    public void disableSniffer(AID snifferName, AID notToBeSniffed) throws RemoteException, IMTPException {
      impl.disableSniffer(snifferName, notToBeSniffed);
    }
    
    public void createAgent(AID agentID, String className, Object[] arguments, boolean startIt) throws RemoteException, IMTPException {
      impl.createAgent(agentID, className, arguments, startIt);
    }
    
    public void createAgent(AID agentID, byte[] serializedInstance, AgentContainerRMI classSite, boolean startIt) throws RemoteException, IMTPException {
      AgentContainer cont = manager.getAdapter(classSite);
      impl.createAgent(agentID, serializedInstance, cont, startIt);
      //impl.createAgent(agentID, serializedInstance, new AgentContainerAdapter(classSite, manager), startIt);
    }

    public void resumeAgent(AID agentID) throws RemoteException, NotFoundException, IMTPException {
      impl.resumeAgent(agentID);
    }

    public void moveAgent(AID agentID, Location where) throws RemoteException, NotFoundException, IMTPException {
      impl.moveAgent(agentID, where);
    }

    public void updateRoutingTable(int op, MTPDescriptor mtp, AgentContainerRMI ac) throws RemoteException, IMTPException {
      AgentContainer cont = manager.getAdapter(ac);
      impl.updateRoutingTable(op, mtp, cont);
    }

    public void postTransferResult(AID agentID, boolean result, List messages) throws RemoteException, NotFoundException, IMTPException {
      impl.postTransferResult(agentID, result, messages);
    }
    
    public void routeOut(ACLMessage msg, AID receiver, String address) throws RemoteException, MTPException, IMTPException {
      impl.routeOut(msg, receiver, address);
    }
    
    public void enableSniffer(AID snifferName, AID toBeSniffed) throws RemoteException, IMTPException {
      impl.enableSniffer(snifferName, toBeSniffed);
    }
    
    public void enableDebugger(AID debuggerName, AID toBeDebugged) throws RemoteException, IMTPException {
      impl.enableDebugger(debuggerName, toBeDebugged);
    }
    
    public void killAgent(AID agentID) throws RemoteException, NotFoundException, IMTPException {
      impl.killAgent(agentID);
    }
    
    public void waitAgent(AID agentID) throws RemoteException, NotFoundException, IMTPException {
      impl.waitAgent(agentID);
    }
    
    public void ping(boolean hang) throws RemoteException, IMTPException {
      impl.ping(hang);
    }

}
