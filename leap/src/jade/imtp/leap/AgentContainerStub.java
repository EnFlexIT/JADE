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
 * Copyright (C) 2001 Motorola.
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
//import jade.security.IdentityCertificate;
//import jade.security.DelegationCertificate;

/**
 * @author Nicolas Lhuillier - Motorola
 */
class AgentContainerStub extends Stub implements AgentContainer {

  private static final String NOT_FOUND_EXCEPTION = "jade.core.NotFoundException";
  private static final String CLASS_NOT_FOUND_EXCEPTION = "java.lang.ClassNotFoundException";

  /**
   * Constructor declaration
   * 
   */
  protected AgentContainerStub() {
    super();
  }

  /**
   * Constructor declaration
   * 
   * @param ac
   * 
   */
  protected AgentContainerStub(int id) {
    super(id);
  }

  // /////////////////////////////////////////
  // AgentContainer INTERFACE
  // /////////////////////////////////////////

  /**
   * Calls the <code>createAgent</code> method of the remote container
   */
  public void createAgent(AID agentID, String className, Object arguments[], String ownership, CertificateFolder certs, boolean startIt) throws IMTPException {
    Command send = new Command(Command.CREATE_AGENT_FROM_NAME, remoteID);

    send.addParam(agentID);
    send.addParam(className);
    send.addParam(arguments);
    send.addParam(ownership);
    send.addParam(certs);
    send.addParam(new Boolean(startIt));

    try {
      Command result = theDispatcher.dispatchCommand(remoteTAs, send);

      // Check whether an exception occurred in the remote container
      checkResult(result, new String[]{
      });
    } 
    catch (DispatcherException de) {
      throw new IMTPException(DISP_ERROR_MSG, de);
    } 
    catch (UnreachableException ue) {
      throw new IMTPException(UNRCH_ERROR_MSG, ue);
    } 
  } 

  /**
   * Calls the associated <code>createAgent<\code> method of the remote container
   */
  public void createAgent(AID agentID, byte[] serializedInstance, AgentContainer classSite, boolean startIt) throws IMTPException {
    Command send = new Command(Command.CREATE_AGENT_FROM_DATA, remoteID);

    send.addParam(agentID);
    send.addParam(serializedInstance);
    send.addParam(classSite);
    send.addParam(new Boolean(startIt));

    try {
      Command result = theDispatcher.dispatchCommand(remoteTAs, send);

      // Check whether an exception occurred in the remote container
      checkResult(result, new String[]{
      });
    } 
    catch (DispatcherException de) {
      throw new IMTPException(DISP_ERROR_MSG, de);
    } 
    catch (UnreachableException ue) {
      throw new IMTPException(UNRCH_ERROR_MSG, ue);
    } 
  } 

  /**
   * Calls the associated <code>fetchClassFile<\code> method of the remote container
   */
  public byte[] fetchClassFile(String name) throws IMTPException, ClassNotFoundException {
    Command send = new Command(Command.FETCH_CLASS_FILE, remoteID);

    send.addParam(name);

    try {
      Command result = theDispatcher.dispatchCommand(remoteTAs, send);

      // Check whether an exception occurred in the remote container
      if (checkResult(result, new String[] {
        CLASS_NOT_FOUND_EXCEPTION
      }) > 0) {
        throw new ClassNotFoundException((String) result.getParamAt(1));
      } 

      return (byte[]) result.getParamAt(0);
    } 
    catch (DispatcherException de) {
      throw new IMTPException(DISP_ERROR_MSG, de);
    } 
    catch (UnreachableException ue) {
      throw new IMTPException(UNRCH_ERROR_MSG, ue);
    } 
  } 

  /**
   * Calls the associated <code>suspendAgent<\code> method of the remote container
   */
  public void suspendAgent(AID agentID) throws IMTPException, NotFoundException {
    Command send = new Command(Command.SUSPEND_AGENT, remoteID);

    send.addParam(agentID);

    try {
      Command result = theDispatcher.dispatchCommand(remoteTAs, send);

      // Check whether an exception occurred in the remote container
      if (checkResult(result, new String[] {
        NOT_FOUND_EXCEPTION
      }) > 0) {
        throw new NotFoundException((String) result.getParamAt(1));
      } 
    } 
    catch (DispatcherException de) {
      throw new IMTPException(DISP_ERROR_MSG, de);
    } 
    catch (UnreachableException ue) {
      throw new IMTPException(UNRCH_ERROR_MSG, ue);
    } 
  } 

  /**
   * Calls the associated <code>resumeAgent<\code> method of the remote container
   */
  public void resumeAgent(AID agentID) throws IMTPException, NotFoundException {
    Command send = new Command(Command.RESUME_AGENT, remoteID);

    send.addParam(agentID);

    try {
      Command result = theDispatcher.dispatchCommand(remoteTAs, send);

      // Check whether an exception occurred in the remote container
      if (checkResult(result, new String[] {
        NOT_FOUND_EXCEPTION
      }) > 0) {
        throw new NotFoundException((String) result.getParamAt(1));
      } 
    } 
    catch (DispatcherException de) {
      throw new IMTPException(DISP_ERROR_MSG, de);
    } 
    catch (UnreachableException ue) {
      throw new IMTPException(UNRCH_ERROR_MSG, ue);
    } 
  } 

  /**
   * Calls the associated <code>waitAgent<\code> method of the remote container
   */
  public void waitAgent(AID agentID) throws IMTPException, NotFoundException {
    Command send = new Command(Command.WAIT_AGENT, remoteID);

    send.addParam(agentID);

    try {
      Command result = theDispatcher.dispatchCommand(remoteTAs, send);

      // Check whether an exception occurred in the remote container
      if (checkResult(result, new String[] {
        NOT_FOUND_EXCEPTION
      }) > 0) {
        throw new NotFoundException((String) result.getParamAt(1));
      } 
    } 
    catch (DispatcherException de) {
      throw new IMTPException(DISP_ERROR_MSG, de);
    } 
    catch (UnreachableException ue) {
      throw new IMTPException(UNRCH_ERROR_MSG, ue);
    } 
  } 

  /**
   * Calls the associated <code>wakeAgent<\code> method of the remote container
   */
  public void wakeAgent(AID agentID) throws IMTPException, NotFoundException {
    Command send = new Command(Command.WAKE_AGENT, remoteID);

    send.addParam(agentID);

    try {
      Command result = theDispatcher.dispatchCommand(remoteTAs, send);

      // Check whether an exception occurred in the remote container
      if (checkResult(result, new String[] {
        NOT_FOUND_EXCEPTION
      }) > 0) {
        throw new NotFoundException((String) result.getParamAt(1));
      } 
    } 
    catch (DispatcherException de) {
      throw new IMTPException(DISP_ERROR_MSG, de);
    } 
    catch (UnreachableException ue) {
      throw new IMTPException(UNRCH_ERROR_MSG, ue);
    } 
  } 

  /**
   * Calls the associated <code>moveAgent<\code> method of the remote container
   */
  public void moveAgent(AID agentID, Location where) throws IMTPException, NotFoundException {
    Command send = new Command(Command.MOVE_AGENT, remoteID);

    send.addParam(agentID);
    send.addParam(where);

    try {
      Command result = theDispatcher.dispatchCommand(remoteTAs, send);

      // Check whether an exception occurred in the remote container
      if (checkResult(result, new String[] {
        NOT_FOUND_EXCEPTION
      }) > 0) {
        throw new NotFoundException((String) result.getParamAt(1));
      } 
    } 
    catch (DispatcherException de) {
      throw new IMTPException(DISP_ERROR_MSG, de);
    } 
    catch (UnreachableException ue) {
      throw new IMTPException(UNRCH_ERROR_MSG, ue);
    } 
  } 

  /**
   * Calls the associated <code>copyAgent<\code> method of the remote container
   */
  public void copyAgent(AID agentID, Location where, String newName) throws IMTPException, NotFoundException {
    Command send = new Command(Command.COPY_AGENT, remoteID);

    send.addParam(agentID);
    send.addParam(where);
    send.addParam(newName);

    try {
      Command result = theDispatcher.dispatchCommand(remoteTAs, send);

      // Check whether an exception occurred in the remote container
      if (checkResult(result, new String[] {
        NOT_FOUND_EXCEPTION
      }) > 0) {
        throw new NotFoundException((String) result.getParamAt(1));
      } 
    } 
    catch (DispatcherException de) {
      throw new IMTPException(DISP_ERROR_MSG, de);
    } 
    catch (UnreachableException ue) {
      throw new IMTPException(UNRCH_ERROR_MSG, ue);
    } 
  } 

  /**
   * Calls the associated <code>copyAgent<\code> method of the remote container
   */
  public void killAgent(AID agentID) throws IMTPException, NotFoundException {
    Command send = new Command(Command.KILL_AGENT, remoteID);

    send.addParam(agentID);

    try {
      Command result = theDispatcher.dispatchCommand(remoteTAs, send);

      // Check whether an exception occurred in the remote container
      if (checkResult(result, new String[] {
        NOT_FOUND_EXCEPTION
      }) > 0) {
        throw new NotFoundException((String) result.getParamAt(1));
      } 
    } 
    catch (DispatcherException de) {
      throw new IMTPException(DISP_ERROR_MSG, de);
    } 
    catch (UnreachableException ue) {
      throw new IMTPException(UNRCH_ERROR_MSG, ue);
    } 
  } 

  /**
   * Calls the associated <code>changeAgentPrincipal<\code> method of
   * the remote container
   */
  public void changeAgentPrincipal(AID agentID, CertificateFolder certs) throws IMTPException, NotFoundException {
    Command send = new Command(Command.CHANGE_AGENT_PRINCIPAL, remoteID);

    send.addParam(agentID);
    send.addParam(certs);

    try {
      Command result = theDispatcher.dispatchCommand(remoteTAs, send);

      // Check whether an exception occurred in the remote container
      if (checkResult(result, new String[] {
        NOT_FOUND_EXCEPTION
      }) > 0) {
        throw new NotFoundException((String) result.getParamAt(1));
      } 
    } 
    catch (DispatcherException de) {
      throw new IMTPException(DISP_ERROR_MSG, de);
    } 
    catch (UnreachableException ue) {
      throw new IMTPException(UNRCH_ERROR_MSG, ue);
    } 
  } 

  /**
   * Calls the associated <code>changedAgentPrincipal<\code> method
   * of the remote container
   */
  public void changedAgentPrincipal(AID agentID, AgentPrincipal principal) throws IMTPException {
    Command send = new Command(Command.CHANGED_AGENT_PRINCIPAL, remoteID);

    send.addParam(agentID);
    send.addParam(principal);

    try {
      Command result = theDispatcher.dispatchCommand(remoteTAs, send);

      // Check whether an exception occurred in the remote container
      checkResult(result, new String[]{
      });
    } 
    catch (DispatcherException de) {
      throw new IMTPException(DISP_ERROR_MSG, de);
    } 
    catch (UnreachableException ue) {
      throw new IMTPException(UNRCH_ERROR_MSG, ue);
    } 
  } 
  	
  /**
   * Calls the associated <code>changeContainerPrincipal<\code> method
   * of the remote container
   */
  public void changeContainerPrincipal(CertificateFolder certs) throws IMTPException {
    Command send = new Command(Command.CHANGE_CONTAINER_PRINCIPAL, remoteID);

    send.addParam(certs);

    try {
      Command result = theDispatcher.dispatchCommand(remoteTAs, send);

      // Check whether an exception occurred in the remote container
      checkResult(result, new String[]{
      });
    } 
    catch (DispatcherException de) {
      throw new IMTPException(DISP_ERROR_MSG, de);
    } 
    catch (UnreachableException ue) {
      throw new IMTPException(UNRCH_ERROR_MSG, ue);
    } 
  } 

  /**
   * Calls the associated <code>exit<\code> method of the remote container
   */
  public void exit() throws IMTPException {
    Command send = new Command(Command.EXIT, remoteID);

    try {
      Command result = theDispatcher.dispatchCommand(remoteTAs, send);

      // Check whether an exception occurred in the remote container
      checkResult(result, new String[]{
      });
    } 
    catch (DispatcherException de) {
      throw new IMTPException(DISP_ERROR_MSG, de);
    } 
    catch (UnreachableException ue) {
      throw new IMTPException(UNRCH_ERROR_MSG, ue);
    } 
  } 

  /**
   * Calls the associated <code>exit<\code> method of the remote container
   */
  public void postTransferResult(AID agentID, boolean result, List messages) throws IMTPException, NotFoundException {
    Command send = new Command(Command.POST_TRANSFER_RESULT, remoteID);

    send.addParam(agentID);
    send.addParam(new Boolean(result));
    send.addParam(messages);

    try {
      Command res = theDispatcher.dispatchCommand(remoteTAs, send);

      // Check whether an exception occurred in the remote container
      if (checkResult(res, new String[] {
        NOT_FOUND_EXCEPTION
      }) > 0) {
        throw new NotFoundException((String) res.getParamAt(1));
      } 
    } 
    catch (DispatcherException de) {
      throw new IMTPException(DISP_ERROR_MSG, de);
    } 
    catch (UnreachableException ue) {
      throw new IMTPException(UNRCH_ERROR_MSG, ue);
    } 
  } 

  /**
   * Calls the associated <code>dispatch<\code> method of the remote container
   */
  public void dispatch(ACLMessage msg, AID receiverID) throws IMTPException, NotFoundException {
    Command send = new Command(Command.DISPATCH, remoteID);

    send.addParam(msg);
    send.addParam(receiverID);

    try {
      Command result = theDispatcher.dispatchCommand(remoteTAs, send);

      // Check whether an exception occurred in the remote container
      if (checkResult(result, new String[] {
        NOT_FOUND_EXCEPTION
      }) > 0) {
        throw new NotFoundException((String) result.getParamAt(1));
      } 
    } 
    catch (DispatcherException de) {
      throw new IMTPException(DISP_ERROR_MSG, de);
    } 
    catch (UnreachableException ue) {
      throw new IMTPException(UNRCH_ERROR_MSG, ue);
    } 
  } 

  /**
   * Calls the associated <code>ping<\code> method of the remote container
   */
  public void ping(boolean hang) throws IMTPException {

    // a hack by Steffen Rusitschka. In the future, remove the boolean
    // parameter and implement different ping methods in the
    // AgentContainer interface.
    Command send = new Command(hang ? Command.BLOCKING_PING : Command.PING, remoteID);

    try {
      Command result = theDispatcher.dispatchCommand(remoteTAs, send);

      // Check whether an exception occurred in the remote container
      checkResult(result, new String[]{
      });
    } 
    catch (DispatcherException de) {
      throw new IMTPException(DISP_ERROR_MSG, de);
    } 
    catch (UnreachableException ue) {
      throw new IMTPException(UNRCH_ERROR_MSG, ue);
    } 
  } 

  /**
   * Calls the associated <code>installMTP<\code> method of the remote container
   */
  public MTPDescriptor installMTP(String address, String className) throws IMTPException, MTPException {
    Command send = new Command(Command.INSTALL_MTP, remoteID);

    send.addParam(address);
    send.addParam(className);

    try {
      Command result = theDispatcher.dispatchCommand(remoteTAs, send);

      // Check whether an exception occurred in the remote container
      if (checkResult(result, new String[] {
        "jade.mtp.MTP.MTPException"
      }) > 0) {
        throw new MTPException((String) result.getParamAt(1));
      } 

      return (MTPDescriptor) result.getParamAt(0);
    } 
    catch (DispatcherException de) {
      throw new IMTPException(DISP_ERROR_MSG, de);
    } 
    catch (UnreachableException ue) {
      throw new IMTPException(UNRCH_ERROR_MSG, ue);
    } 
  } 

  /**
   * Calls the associated <code>uninstallMTP<\code> method of the remote container
   */
  public void uninstallMTP(String address) throws IMTPException, NotFoundException, MTPException {
    Command send = new Command(Command.UNINSTALL_MTP, remoteID);

    send.addParam(address);

    try {
      Command result = theDispatcher.dispatchCommand(remoteTAs, send);

      // Check whether an exception occurred in the remote container
      switch (checkResult(result, new String[] {
        NOT_FOUND_EXCEPTION, "jade.mtp.MTP.MTPException"
      })) {

      case 1:
        throw new NotFoundException((String) result.getParamAt(1));

      case 2:
        throw new MTPException((String) result.getParamAt(1));
      }
    } 
    catch (DispatcherException de) {
      throw new IMTPException(DISP_ERROR_MSG, de);
    } 
    catch (UnreachableException ue) {
      throw new IMTPException(UNRCH_ERROR_MSG, ue);
    } 
  } 

  /**
   * Calls the associated <code>updateRoutingTable<\code> method of the remote container
   */
  public void updateRoutingTable(int op, MTPDescriptor mtp, AgentContainer ac) throws IMTPException {
    Command send = new Command(Command.UPDATE_ROUTING_TABLE, remoteID);

    send.addParam(new Integer(op));
    send.addParam(mtp);
    send.addParam(ac);

    try {
      Command result = theDispatcher.dispatchCommand(remoteTAs, send);

      // Check whether an exception occurred in the remote container
      checkResult(result, new String[]{
      });
    } 
    catch (DispatcherException de) {
      throw new IMTPException(DISP_ERROR_MSG, de);
    } 
    catch (UnreachableException ue) {
      throw new IMTPException(UNRCH_ERROR_MSG, ue);
    } 
  } 

  /**
   * Calls the associated <code>route<\code> method of the remote container
   */
  public void routeOut(ACLMessage msg, AID receiver, String address) throws IMTPException {
    Command send = new Command(Command.ROUTE_OUT, remoteID);

    send.addParam(msg);
    send.addParam(receiver);
    send.addParam(address);

    try {
      Command result = theDispatcher.dispatchCommand(remoteTAs, send);

      // Check whether an exception occurred in the remote container
      checkResult(result, new String[]{
      });
    } 
    catch (DispatcherException de) {
      throw new IMTPException(DISP_ERROR_MSG, de);
    } 
    catch (UnreachableException ue) {
      throw new IMTPException(UNRCH_ERROR_MSG, ue);
    } 
  } 

  /**
   * Calls the associated <code>enableSniffer<\code> method of the remote container
   */
  public void enableSniffer(AID snifferName, AID toBeSniffed) throws IMTPException {
    Command send = new Command(Command.ENABLE_SNIFFER, remoteID);

    send.addParam(snifferName);
    send.addParam(toBeSniffed);

    try {
      Command result = theDispatcher.dispatchCommand(remoteTAs, send);

      // Check whether an exception occurred in the remote container
      checkResult(result, new String[]{
      });
    } 
    catch (DispatcherException de) {
      throw new IMTPException(DISP_ERROR_MSG, de);
    } 
    catch (UnreachableException ue) {
      throw new IMTPException(UNRCH_ERROR_MSG, ue);
    } 
  } 

  /**
   * Calls the associated <code>enableSniffer<\code> method of the remote container
   */
  public void disableSniffer(AID snifferName, AID notToBeSniffed) throws IMTPException {
    Command send = new Command(Command.DISABLE_SNIFFER, remoteID);

    send.addParam(snifferName);
    send.addParam(notToBeSniffed);

    try {
      Command result = theDispatcher.dispatchCommand(remoteTAs, send);

      // Check whether an exception occurred in the remote container
      checkResult(result, new String[]{
      });
    } 
    catch (DispatcherException de) {
      throw new IMTPException(DISP_ERROR_MSG, de);
    } 
    catch (UnreachableException ue) {
      throw new IMTPException(UNRCH_ERROR_MSG, ue);
    } 
  } 

  /**
   * Method declaration
   * 
   * @param debuggerName
   * @param toBeDebugged
   * 
   * @throws IMTPException
   * 
   * @see
   */
  public void enableDebugger(AID debuggerName, AID toBeDebugged) throws IMTPException {
    Command send = new Command(Command.ENABLE_DEBUGGER, remoteID);

    send.addParam(debuggerName);
    send.addParam(toBeDebugged);

    try {
      Command result = theDispatcher.dispatchCommand(remoteTAs, send);

      // Check whether an exception occurred in the remote container
      checkResult(result, new String[]{
      });
    } 
    catch (DispatcherException de) {
      throw new IMTPException(DISP_ERROR_MSG, de);
    } 
    catch (UnreachableException ue) {
      throw new IMTPException(UNRCH_ERROR_MSG, ue);
    } 
  } 

  /**
   * Method declaration
   * 
   * @param debuggerName
   * @param notToBeDebugged
   * 
   * @throws IMTPException
   * 
   * @see
   */
  public void disableDebugger(AID debuggerName, AID notToBeDebugged) throws IMTPException {
    Command send = new Command(Command.DISABLE_DEBUGGER, remoteID);

    send.addParam(debuggerName);
    send.addParam(notToBeDebugged);

    try {
      Command result = theDispatcher.dispatchCommand(remoteTAs, send);

      // Check whether an exception occurred in the remote container
      checkResult(result, new String[]{
      });
    } 
    catch (DispatcherException de) {
      throw new IMTPException(DISP_ERROR_MSG, de);
    } 
    catch (UnreachableException ue) {
      throw new IMTPException(UNRCH_ERROR_MSG, ue);
    } 
  } 

}

