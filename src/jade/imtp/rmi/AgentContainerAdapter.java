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

import java.io.Serializable;

import java.rmi.RemoteException;

import java.util.List;

import jade.core.AID;
import jade.core.AgentContainer;
import jade.core.Location;
import jade.core.IMTPException;
import jade.core.NotFoundException;

import jade.lang.acl.ACLMessage;

import jade.mtp.MTPException;

/**
   @author Giovanni Rimassa - Universita` di Parma
   @version $Date$ $Revision$
 */
public class AgentContainerAdapter implements AgentContainer, Serializable {

  private AgentContainerRMI adaptee;
  private RMIIMTPManager manager;

  /** Creates new AgentContainerAdapter */
  public AgentContainerAdapter(AgentContainerRMI ac, RMIIMTPManager mgr) {
    adaptee = ac;
    manager = mgr;
  }

  public void moveAgent(AID agentID, Location where) throws IMTPException, NotFoundException {
    try {
      adaptee.moveAgent(agentID, where);
    }
    catch(RemoteException re) {
      throw new IMTPException("Communication Failure", re);
    }
  }
  
  public void waitAgent(AID agentID) throws IMTPException, NotFoundException {
    try {
      adaptee.waitAgent(agentID);
    }
    catch(RemoteException re) {
      throw new IMTPException("Communication Failure", re);
    }
  }
  
  public byte[] fetchClassFile(String name) throws IMTPException, ClassNotFoundException {
    try {
      return adaptee.fetchClassFile(name);
    }
    catch(RemoteException re) {
      throw new IMTPException("Communication Failure", re);
    }
  }
  
  public void disableDebugger(AID debuggerName, AID notToBeDebugged) throws IMTPException {
    try {
      adaptee.disableDebugger(debuggerName, notToBeDebugged);
    }
    catch(RemoteException re) {
      throw new IMTPException("Communication Failure", re);
    }
  }
  
  public void exit() throws IMTPException {
    try {
      adaptee.exit();
    }
    catch(RemoteException re) {
      throw new IMTPException("Communication Failure", re);
    }
  }
  
  public void uninstallMTP(String address) throws IMTPException, NotFoundException, MTPException {
    try {
      adaptee.uninstallMTP(address);
    }
    catch(RemoteException re) {
      throw new IMTPException("Communication Failure", re);
    }
  }
  
  public void wakeAgent(AID agentID) throws IMTPException, NotFoundException {
    try {
      adaptee.wakeAgent(agentID);
    }
    catch(RemoteException re) {
      throw new IMTPException("Communication Failure", re);
    }
  }
  
  public void createAgent(AID agentID, byte[] serializedInstance, AgentContainer classSite, boolean startIt) throws IMTPException {
    try {
      adaptee.createAgent(agentID, serializedInstance, manager.getRMIStub(classSite), startIt);
    }
    catch(RemoteException re) {
      throw new IMTPException("Communication Failure", re);
    }
  }

  public void createAgent(AID agentID, String className, Object[] arguments, boolean startIt) throws IMTPException {
    try {
      adaptee.createAgent(agentID, className, arguments, startIt);
    }
    catch(RemoteException re) {
      throw new IMTPException("Communication Failure", re);
    }
  }
  
  public void killAgent(AID agentID) throws IMTPException, NotFoundException {
    try {
      adaptee.killAgent(agentID);
    }
    catch(RemoteException re) {
      throw new IMTPException("Communication Failure", re);
    }
  }
  
  public String installMTP(String address, String className) throws IMTPException, MTPException {
    try {
      return adaptee.installMTP(address, className);
    }
    catch(RemoteException re) {
      throw new IMTPException("Communication Failure", re);
    }
  }
  
  public void disableSniffer(AID snifferName, AID notToBeSniffed) throws IMTPException {
    try {
      adaptee.disableSniffer(snifferName, notToBeSniffed);
    }
    catch(RemoteException re) {
      throw new IMTPException("Communication Failure", re);
    }
  }
  
  public void resumeAgent(AID agentID) throws IMTPException, NotFoundException {
    try {
      adaptee.resumeAgent(agentID);
    }
    catch(RemoteException re) {
      throw new IMTPException("Communication Failure", re);
    }
  }
  
  public void suspendAgent(AID agentID) throws IMTPException, NotFoundException {
    try {
      adaptee.suspendAgent(agentID);
    }
    catch(RemoteException re) {
      throw new IMTPException("Communication Failure", re);
    }
  }
  
  public void copyAgent(AID agentID, Location where, String newName) throws IMTPException, NotFoundException {
    try {
      adaptee.copyAgent(agentID, where, newName);
    }
    catch(RemoteException re) {
      throw new IMTPException("Communication Failure", re);
    }
  }
  
  public void enableSniffer(AID snifferName, AID toBeSniffed) throws IMTPException {
    try {
      adaptee.enableSniffer(snifferName, toBeSniffed);
    }
    catch(RemoteException re) {
      throw new IMTPException("Communication Failure", re);
    }
  }
  
  public void enableDebugger(AID debuggerName, AID toBeDebugged) throws IMTPException {
    try {
      adaptee.enableDebugger(debuggerName, toBeDebugged);
    }
    catch(RemoteException re) {
      throw new IMTPException("Communication Failure", re);
    }
  }
  
  public void routeOut(ACLMessage msg, AID receiver, String address) throws IMTPException, MTPException {
    try {
      adaptee.routeOut(msg, receiver, address);
    }
    catch(RemoteException re) {
      throw new IMTPException("Communication Failure", re);
    }
  }
  
  public void postTransferResult(AID agentID, boolean result, List messages) throws IMTPException, NotFoundException {
    try {
      adaptee.postTransferResult(agentID, result, messages);
    }
    catch(RemoteException re) {
      throw new IMTPException("Communication Failure", re);
    }
  }
  
  public void dispatch(ACLMessage msg, AID receiverID) throws IMTPException, NotFoundException {
    try {
      adaptee.dispatch(msg, receiverID);
    }
    catch(RemoteException re) {
      throw new IMTPException("Communication Failure", re);
    }
  }

  public void updateRoutingTable(int op, String address, AgentContainer ac) throws IMTPException {
    try {
      adaptee.updateRoutingTable(op, address, manager.getRMIStub(ac));
    }
    catch(RemoteException re) {
      throw new IMTPException("Communication Failure", re);
    }
  }
  
  public void ping(boolean hang) throws IMTPException {
    try {
      adaptee.ping(hang);
    }
    catch(RemoteException re) {
      throw new IMTPException("Communication Failure", re);
    }
  }

  
}
