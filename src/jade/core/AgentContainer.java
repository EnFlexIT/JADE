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

import java.rmi.Remote;
import java.rmi.RemoteException;

import java.util.List;
import java.util.Iterator;

import jade.lang.acl.ACLMessage;

/**
@author Giovanni Rimassa - Universita` di Parma
@version $Date$ $Revision$
*/

interface AgentContainer extends Remote {

  static final boolean NOSTART = false;
  static final boolean START = true;

  static final boolean TRANSFER_ABORT = false;
  static final boolean TRANSFER_COMMIT = true;

  void createAgent(AID agentID, String className, boolean startIt) throws RemoteException;
  void createAgent(AID  agentID, byte[] serializedInstance, AgentContainer classSite, boolean startIt) throws RemoteException;
  byte[] fetchClassFile(String name) throws RemoteException, ClassNotFoundException;

  void suspendAgent(AID agentID) throws RemoteException, NotFoundException;
  void resumeAgent(AID agentID) throws RemoteException, NotFoundException;

  void waitAgent(AID agentID) throws RemoteException, NotFoundException;
  void wakeAgent(AID agentID) throws RemoteException, NotFoundException;

  void moveAgent(AID agentID, Location where) throws RemoteException, NotFoundException;
  void copyAgent(AID agentID, Location where, String newName) throws RemoteException, NotFoundException;

  void killAgent(AID agentID) throws RemoteException, NotFoundException;
  void exit() throws RemoteException;

  void postTransferResult(AID agentID, boolean result, List messages) throws RemoteException, NotFoundException;
  void dispatch(ACLMessage msg, AID receiverID) throws RemoteException, NotFoundException;
  void ping(boolean hang) throws RemoteException;

  void enableSniffer(AID snifferName, Iterator toBeSniffed) throws RemoteException;
  void disableSniffer(AID snifferName, Iterator notToBeSniffed) throws RemoteException;

}
