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

import jade.util.leap.List;

import jade.lang.acl.ACLMessage;

import jade.mtp.MTPDescriptor;
import jade.mtp.MTPException;

//__JADE_ONLY__BEGIN
import jade.security.AgentPrincipal;
import jade.security.DelegationCertificate;
import jade.security.IdentityCertificate;
//__JADE_ONLY__END

/**
  @author Giovanni Rimassa - Universita` di Parma
  @version $Date$ $Revision$
*/

public interface AgentContainer {

  static final boolean NOSTART = false;
  static final boolean START = true;

  static final boolean TRANSFER_ABORT = false;
  static final boolean TRANSFER_COMMIT = true;

  static final int ADD_RT = 1;
  static final int DEL_RT = 2;

  void createAgent(AID agentID, String className, Object arguments[], boolean startIt) throws IMTPException;
  void createAgent(AID  agentID, byte[] serializedInstance, AgentContainer classSite, boolean startIt) throws IMTPException;
  byte[] fetchClassFile(String name) throws IMTPException, ClassNotFoundException;

  void suspendAgent(AID agentID) throws IMTPException, NotFoundException;
  void resumeAgent(AID agentID) throws IMTPException, NotFoundException;

  void waitAgent(AID agentID) throws IMTPException, NotFoundException;
  void wakeAgent(AID agentID) throws IMTPException, NotFoundException;

//__JADE_ONLY__BEGIN
  void changeAgentPrincipal(AID agentID, IdentityCertificate identity, DelegationCertificate delegation) throws IMTPException, NotFoundException;
//__JADE_ONLY__END

  void moveAgent(AID agentID, Location where) throws IMTPException, NotFoundException;
  void copyAgent(AID agentID, Location where, String newName) throws IMTPException, NotFoundException;

  void killAgent(AID agentID) throws IMTPException, NotFoundException;
  void exit() throws IMTPException;

  void postTransferResult(AID agentID, boolean result, List messages) throws IMTPException, NotFoundException;
  void dispatch(ACLMessage msg, AID receiverID) throws IMTPException, NotFoundException;
  void ping(boolean hang) throws IMTPException;

  MTPDescriptor installMTP(String address, String className) throws IMTPException, MTPException;
  void uninstallMTP(String address) throws IMTPException, NotFoundException, MTPException;

  void updateRoutingTable(int op, MTPDescriptor mtp, AgentContainer ac) throws IMTPException;
  void routeOut(ACLMessage msg, AID receiver, String address) throws IMTPException, MTPException;

  void enableSniffer(AID snifferName , AID toBeSniffed) throws IMTPException;
  void disableSniffer(AID snifferName, AID notToBeSniffed) throws IMTPException;

  void enableDebugger(AID debuggerName , AID toBeDebugged) throws IMTPException;
  void disableDebugger(AID debuggerName, AID notToBeDebugged) throws IMTPException;

}
