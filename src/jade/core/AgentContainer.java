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

import java.util.List;

import jade.lang.acl.ACLMessage;

import jade.mtp.MTPException;

/**
  @author Giovanni Rimassa - Universita` di Parma
  @version $Date$ $Revision$
*/

interface AgentContainer {

  static final boolean NOSTART = false;
  static final boolean START = true;

  static final boolean TRANSFER_ABORT = false;
  static final boolean TRANSFER_COMMIT = true;

  static final int ADD_RT = 1;
  static final int DEL_RT = 2;

  void createAgent(AID agentID, String className,String arguments[], boolean startIt) throws InvocationException;
  void createAgent(AID  agentID, byte[] serializedInstance, AgentContainer classSite, boolean startIt) throws InvocationException;
  byte[] fetchClassFile(String name) throws InvocationException, ClassNotFoundException;

  void suspendAgent(AID agentID) throws InvocationException, NotFoundException;
  void resumeAgent(AID agentID) throws InvocationException, NotFoundException;

  void waitAgent(AID agentID) throws InvocationException, NotFoundException;
  void wakeAgent(AID agentID) throws InvocationException, NotFoundException;

  void moveAgent(AID agentID, Location where) throws InvocationException, NotFoundException;
  void copyAgent(AID agentID, Location where, String newName) throws InvocationException, NotFoundException;

  void killAgent(AID agentID) throws InvocationException, NotFoundException;
  void exit() throws InvocationException;

  void postTransferResult(AID agentID, boolean result, List messages) throws InvocationException, NotFoundException;
  void dispatch(ACLMessage msg, AID receiverID) throws InvocationException, NotFoundException;
  void ping(boolean hang) throws InvocationException;

  String installMTP(String address, String className) throws InvocationException, MTPException;
  void uninstallMTP(String address) throws InvocationException, NotFoundException, MTPException;

  void updateRoutingTable(int op, String address, AgentContainer ac) throws InvocationException;
  void routeOut(ACLMessage msg, AID receiver, String address) throws InvocationException, MTPException;

  void enableSniffer(AID snifferName , AID toBeSniffed) throws InvocationException;
  void disableSniffer(AID snifferName, AID notToBeSniffed) throws InvocationException;

  void enableDebugger(AID debuggerName , AID toBeDebugged) throws InvocationException;
  void disableDebugger(AID debuggerName, AID notToBeDebugged) throws InvocationException;

}
