/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

/*
 * ***************************************************************
 * The LEAP libraries, when combined with certain JADE platform components,
 * provide a run-time environment for enabling FIPA agents to execute on
 * lightweight devices running Java. LEAP and JADE teams have jointly
 * designed the API for ease of integration and hence to take advantage
 * of these dual developments and extensions so that users only see
 * one development platform and a
 * single homogeneous set of APIs. Enabling deployment to a wide range of
 * devices whilst still having access to the full development
 * environment and functionalities that JADE provides.
 * Copyright (C) 2001 Telecom Italia LAB S.p.A.
 * 
 * GNU Lesser General Public License
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation,
 * version 2.1 of the License.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 * **************************************************************
 */

package jade.imtp.leap;

import jade.core.*;
import jade.lang.acl.*;
import jade.util.leap.*;
import jade.mtp.MTPDescriptor;

import jade.security.AgentPrincipal;
import jade.security.ContainerPrincipal;
import jade.security.CertificateFolder;
//import jade.security.IdentityCertificate;
//import jade.security.DelegationCertificate;

/**
 * @author Giovanni Caire - Telecom Italia LAB
 */
class AgentContainerSkel extends Skeleton {
  private AgentContainer ac;

  /**
   * Constructor declaration
   * 
   * @param ac
   * 
   */
  public AgentContainerSkel(AgentContainer ac) {
    this.ac = ac;
  }

  /**
   */
  public Command executeCommand(Command command) throws Throwable {
    Command resp = null;

    switch (command.getCode()) {

    case Command.DISPATCH: {
      ACLMessage msg = (ACLMessage) command.getParamAt(0);
      AID        receiverID = (AID) command.getParamAt(1);

      ac.dispatch(msg, receiverID);
      
      resp = new Command(Command.OK);

      break;
    } 

    // a hack by Steffen Rusitschka. In the future, remove the boolean
    // parameter and implement different ping methods in the
    // AgentContainer interface.
    case Command.BLOCKING_PING:
      ac.ping(true);

      resp = new Command(Command.OK);

      break;

    case Command.PING:
      ac.ping(false);

      resp = new Command(Command.OK);

      break;


    case Command.CREATE_AGENT_FROM_NAME: {
      AID                   agentID = (AID) command.getParamAt(0);
      String                className = (String) command.getParamAt(1);
      Object                arguments[] = (Object[]) command.getParamAt(2);
      String                ownership = (String) command.getParamAt(3);
      CertificateFolder     certs = (CertificateFolder) command.getParamAt(4);
      Boolean               startIt = (Boolean) command.getParamAt(5);

      ac.createAgent(agentID, className, arguments, ownership, certs, startIt.booleanValue());

      resp = new Command(Command.OK);

      break;
    } 

    case Command.CREATE_AGENT_FROM_DATA: {
      AID            agentID = (AID) command.getParamAt(0);
      byte[]         serializedInstance = (byte[]) command.getParamAt(1);
      AgentContainer classSite = (AgentContainer) command.getParamAt(2);
      Boolean        startIt = (Boolean) command.getParamAt(3);

      ac.createAgent(agentID, serializedInstance, classSite, startIt.booleanValue());

      resp = new Command(Command.OK);

      break;
    } 

    case Command.FETCH_CLASS_FILE: {
      String name = (String) command.getParamAt(0);
      byte[] arg = ac.fetchClassFile(name);

      resp = new Command(Command.OK);

      resp.addParam(arg);

      break;
    } 

    case Command.SUSPEND_AGENT: {
      AID agentID = (AID) command.getParamAt(0);

      ac.suspendAgent(agentID);

      resp = new Command(Command.OK);

      break;
    } 

    case Command.RESUME_AGENT: {
      AID agentID = (AID) command.getParamAt(0);

      ac.resumeAgent(agentID);

      resp = new Command(Command.OK);

      break;
    } 

    case Command.WAIT_AGENT: {
      AID agentID = (AID) command.getParamAt(0);

      ac.waitAgent(agentID);

      resp = new Command(Command.OK);

      break;
    } 

    case Command.WAKE_AGENT: {
      AID agentID = (AID) command.getParamAt(0);

      ac.wakeAgent(agentID);

      resp = new Command(Command.OK);

      break;
    } 

    case Command.MOVE_AGENT: {
      AID      agentID = (AID) command.getParamAt(0);
      Location where = (Location) command.getParamAt(1);

      ac.moveAgent(agentID, where);

      resp = new Command(Command.OK);

      break;
    } 

    case Command.COPY_AGENT: {
      AID      agentID = (AID) command.getParamAt(0);
      Location where = (Location) command.getParamAt(1);
      String   newName = (String) command.getParamAt(2);

      ac.copyAgent(agentID, where, newName);

      resp = new Command(Command.OK);

      break;
    } 

    case Command.KILL_AGENT: {
      AID agentID = (AID) command.getParamAt(0);

      ac.killAgent(agentID);

      resp = new Command(Command.OK);

      break;
    } 

    case Command.CHANGE_AGENT_PRINCIPAL: {
      AID                   agentID = (AID) command.getParamAt(0);
      CertificateFolder     certs = (CertificateFolder) command.getParamAt(1);

      ac.changeAgentPrincipal(agentID, certs);

      resp = new Command(Command.OK);

      break;
    } 

    case Command.CHANGED_AGENT_PRINCIPAL: {
      AID            agentID = (AID) command.getParamAt(0);
      AgentPrincipal principal = (AgentPrincipal) command.getParamAt(1);

      ac.changedAgentPrincipal(agentID, principal);

      resp = new Command(Command.OK);

      break;
    } 

    case Command.CHANGE_CONTAINER_PRINCIPAL: {
      CertificateFolder     certs = (CertificateFolder) command.getParamAt(0);

      ac.changeContainerPrincipal(certs);

      resp = new Command(Command.OK);

      break;
    } 

    case Command.EXIT: {
    	// We must be sure that the thread that serves the EXIT command
    	// (i.e. this thread) completes before the local JVM is closed
    	jade.core.Runtime.instance().invokeOnTermination(new Joiner(Thread.currentThread()));
      ac.exit();

      resp = new Command(Command.OK);

      break;
    } 

    case Command.POST_TRANSFER_RESULT: {
      AID     agentID = (AID) command.getParamAt(0);
      Boolean result = (Boolean) command.getParamAt(1);
      List    messages = (List) command.getParamAt(2);

      ac.postTransferResult(agentID, result.booleanValue(), messages);

      resp = new Command(Command.OK);

      break;
    } 

    case Command.INSTALL_MTP: {
      String        address = (String) command.getParamAt(0);
      String        className = (String) command.getParamAt(1);
      MTPDescriptor arg = ac.installMTP(address, className);

      resp = new Command(Command.OK);

      resp.addParam(arg);

      break;
    } 

    case Command.UNINSTALL_MTP: {
      String address = (String) command.getParamAt(0);

      ac.uninstallMTP(address);

      resp = new Command(Command.OK);

      break;
    } 

    case Command.UPDATE_ROUTING_TABLE: {
      Integer        op = (Integer) command.getParamAt(0);
      MTPDescriptor  mtp = (MTPDescriptor) command.getParamAt(1);
      AgentContainer container = (AgentContainer) command.getParamAt(2);

      ac.updateRoutingTable(op.intValue(), mtp, container);

      resp = new Command(Command.OK);

      break;
    } 

    case Command.ROUTE_OUT: {
      ACLMessage msg = (ACLMessage) command.getParamAt(0);
      AID        receiver = (AID) command.getParamAt(1);
      String     address = (String) command.getParamAt(2);

      ac.routeOut(msg, receiver, address);

      resp = new Command(Command.OK);

      break;
    } 

    case Command.ENABLE_SNIFFER: {
      AID snifferName = (AID) command.getParamAt(0);
      AID toBeSniffed = (AID) command.getParamAt(1);

      ac.enableSniffer(snifferName, toBeSniffed);

      resp = new Command(Command.OK);

      break;
    } 

    case Command.DISABLE_SNIFFER: {
      AID snifferName = (AID) command.getParamAt(0);
      AID notToBeSniffed = (AID) command.getParamAt(1);

      ac.disableSniffer(snifferName, notToBeSniffed);

      resp = new Command(Command.OK);

      break;
    } 

    case Command.ENABLE_DEBUGGER: {
      AID debuggerName = (AID) command.getParamAt(0);
      AID toBeDebugged = (AID) command.getParamAt(1);

      ac.enableDebugger(debuggerName, toBeDebugged);

      resp = new Command(Command.OK);

      break;
    } 

    case Command.DISABLE_DEBUGGER: {
      AID debuggerName = (AID) command.getParamAt(0);
      AID notToBeDebugged = (AID) command.getParamAt(1);

      ac.disableDebugger(debuggerName, notToBeDebugged);

      resp = new Command(Command.OK);

      break;
    } 

    default:
      throw new DispatcherException("Unknown command code");
    }

    return resp;
  } 

  /**
   * Inner class Joiner
   * This is just a Runnable that waits for the termination of
   * given thread. It is used to ensure that the thread serving
   * an EXIT command completes before the local JVM is killed.
   */
  class Joiner implements Runnable {
    private Thread threadToJoin;

    Joiner(Thread t) {
      threadToJoin = t;
    }

    public void run() {
      try {
      	// DEBUG
        //System.out.println(Thread.currentThread()+": Wait for "+threadToJoin+" to complete...");
        threadToJoin.join();
        // DEBUG
        //System.out.println(Thread.currentThread()+": "+threadToJoin+" completed");
      } 
      catch (InterruptedException ie) {
        ie.printStackTrace();
      } 
    } 

  }    // END of inner class Joiner
  
}

