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
import jade.mtp.TransportAddress;
import jade.mtp.MTPDescriptor;
import jade.core.Runtime;

import jade.security.AgentPrincipal;
import jade.security.AuthException;
import jade.security.JADECertificate;
import jade.security.CertificateFolder;
//import jade.security.IdentityCertificate;
//import jade.security.DelegationCertificate;

import java.util.Vector;

/**
 * A stub linking to the main container. This container is created locally
 * in the agent containers, and is used for all interactions with the main
 * container. Therefore, it does not implement the <code>Deliverable</code>
 * interface.
 * 
 * @author Ronnie Taib - Motorola
 */
class MainContainerStub extends Stub implements MainContainer {
  private static final String NOT_FOUND_EXCEPTION = "jade.core.NotFoundException";
  private static final String NAME_CLASH_EXCEPTION = "jade.core.NameClashException";
  private static final String AUTH_EXCEPTION = "jade.security.AuthException";

  /**
   * Constructor declaration
   */
  MainContainerStub() {
    super();

    remoteID = 0;
  }

  /**
   * Constructor declaration
   * 
   * @param mc
   * 
   */
  MainContainerStub(int id) {
    super(id);
  }

  // /////////////////////////////////////////
  // MainContainer INTERFACE
  // /////////////////////////////////////////

  /**
   */
  public String getPlatformName() throws IMTPException {
    Command send = new Command(Command.GET_PLATFORM_NAME, remoteID);

    try {
      Command result = theDispatcher.dispatchCommand(remoteTAs, send);

      // Check whether an exception occurred in the remote container
      checkResult(result, new String[]{
      });

      return (String) result.getParamAt(0);
    } 
    catch (DispatcherException de) {
      throw new IMTPException(DISP_ERROR_MSG, de);
    } 
    catch (UnreachableException ue) {
      throw new IMTPException(UNRCH_ERROR_MSG, ue);
    } 
  } 

  /**
   * Calls the <code>addContainer</code> method of the remote main container
   */
  public String addContainer(AgentContainer ac, ContainerID cid, String username, byte[] passwd) throws IMTPException, AuthException {
    Command send = new Command(Command.ADD_CONTAINER, remoteID);

    send.addParam(ac);
    send.addParam(cid);
    send.addParam(username);
    send.addParam(passwd);

    try {
      // Runtime.instance().gc(37);

      Command result = theDispatcher.dispatchCommand(remoteTAs, send);

      // Check whether an exception occurred in the remote container
      if (checkResult(result, new String[] {
        AUTH_EXCEPTION
      }) > 0) {
        throw new AuthException((String) result.getParamAt(1));
      } 

      return (String) result.getParamAt(0);
    } 
    catch (DispatcherException de) {
      throw new IMTPException(DISP_ERROR_MSG, de);
    } 
    catch (UnreachableException ue) {
      throw new IMTPException(UNRCH_ERROR_MSG, ue);
    } 
  } 

  /**
   * Calls the <code>removeContainer</code> method of the remote main container
   */
  public void removeContainer(ContainerID cid) throws IMTPException {
    Command send = new Command(Command.REMOVE_CONTAINER, remoteID);

    send.addParam(cid);

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
   * Calls the <code>lookup</code> method of the remote main container
   */
  public AgentContainer lookup(ContainerID cid) throws IMTPException, NotFoundException {
    Command send = new Command(Command.LOOKUP, remoteID);

    send.addParam(cid);

    try {
      Command result = theDispatcher.dispatchCommand(remoteTAs, send);

      // Check whether an exception occurred in the remote container
      if (checkResult(result, new String[] {
        NOT_FOUND_EXCEPTION
      }) > 0) {
        throw new NotFoundException((String) result.getParamAt(1));
      } 

      return (AgentContainer) result.getParamAt(0);
    } 
    catch (DispatcherException de) {
      throw new IMTPException(DISP_ERROR_MSG, de);
    } 
    catch (UnreachableException ue) {
      throw new IMTPException(UNRCH_ERROR_MSG, ue);
    } 
  } 

  /**
   * Calls the <code>bornAgent</code> method of the remote main container
   */
  public void bornAgent(AID name, ContainerID cid, CertificateFolder certs) throws IMTPException, NameClashException, NotFoundException, AuthException {
    Command send = new Command(Command.BORN_AGENT, remoteID);

    send.addParam(name);
    send.addParam(cid);
    send.addParam(certs);

    try {
      Command result = theDispatcher.dispatchCommand(remoteTAs, send);

      // Check whether an exception occurred in the remote container
      switch (checkResult(result, new String[] {
        NAME_CLASH_EXCEPTION,
        NOT_FOUND_EXCEPTION,
        AUTH_EXCEPTION
      }) ) {
      case 1:
        throw new NameClashException((String) result.getParamAt(1));
      case 2:
        throw new NotFoundException((String) result.getParamAt(1));
      case 3:
        throw new AuthException((String) result.getParamAt(1));
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
   * Calls the <code>deadAgent</code> method of the remote main container
   */
  public void deadAgent(AID name) throws IMTPException, NotFoundException {
    Command send = new Command(Command.DEAD_AGENT, remoteID);

    send.addParam(name);

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
   * Calls the <code>suspendedAgent</code> method of the remote main container
   */
  public void suspendedAgent(AID name) throws IMTPException, NotFoundException {
    Command send = new Command(Command.SUSPENDED_AGENT, remoteID);

    send.addParam(name);

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
   * Calls the <code>resumedAgent</code> method of the remote main container
   */
  public void resumedAgent(AID name) throws IMTPException, NotFoundException {
    Command send = new Command(Command.RESUMED_AGENT, remoteID);

    send.addParam(name);

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
   * Calls the <code>changedAgentPrincipal</code> method of the
   * remote main container
   */
  public void changedAgentPrincipal(AID name, CertificateFolder certs) throws IMTPException, NotFoundException {
    Command send = new Command(Command.CHANGED_AGENT_PRINCIPAL, remoteID);

    send.addParam(name);
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
   * Calls the <code>getAgentPrincipal</code> method of the
   * remote main container
   */
  public AgentPrincipal getAgentPrincipal(AID name) throws IMTPException, NotFoundException {
    Command send = new Command(Command.GET_AGENT_PRINCIPAL, remoteID);

    send.addParam(name);
    try {
      Command result = theDispatcher.dispatchCommand(remoteTAs, send);

      // Check whether an exception occurred in the remote container
      if (checkResult(result, new String[] {
        NOT_FOUND_EXCEPTION
      }) > 0) {
        throw new NotFoundException((String) result.getParamAt(1));
      } 
      
      return (AgentPrincipal) result.getParamAt(0);
    } 
    catch (DispatcherException de) {
      throw new IMTPException(DISP_ERROR_MSG, de);
    } 
    catch (UnreachableException ue) {
      throw new IMTPException(UNRCH_ERROR_MSG, ue);
    } 
  } 

  /**
   * Calls the <code>sign</code> method of the remote main container
   */
  public JADECertificate sign(JADECertificate certificate, CertificateFolder certs) throws IMTPException, AuthException {
    Command send = new Command(Command.SIGN, remoteID);

    send.addParam(certificate);
    send.addParam(certs);

    try {
      Command result = theDispatcher.dispatchCommand(remoteTAs, send);

      // Check whether an exception occurred in the remote container
      if (checkResult(result, new String[] {
        AUTH_EXCEPTION
      }) > 0) {
        throw new AuthException((String) result.getParamAt(1));
      } 

      return (JADECertificate) result.getParamAt(0);
    } 
    catch (DispatcherException de) {
      throw new IMTPException(DISP_ERROR_MSG, de);
    } 
    catch (UnreachableException ue) {
      throw new IMTPException(UNRCH_ERROR_MSG, ue);
    } 
  } 

  /**
   * Calls the <code>getPublicKey()</code> method of the remote main container
   */
  public byte[] getPublicKey() throws IMTPException {
    Command send = new Command(Command.GET_PUBLIC_KEY, remoteID);

    try {
      Command result = theDispatcher.dispatchCommand(remoteTAs, send);

      // Check whether an exception occurred in the remote container
      checkResult(result, new String[]{
      });

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
   * Calls the <code>newMTP</code> method of the remote main container
   */
  public void newMTP(MTPDescriptor mtp, ContainerID cid) throws IMTPException {
    Command send = new Command(Command.NEW_MTP, remoteID);

    send.addParam(mtp);
    send.addParam(cid);

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
   * Calls the <code>deadMTP</code> method of the remote main container
   */
  public void deadMTP(MTPDescriptor mtp, ContainerID cid) throws IMTPException {
    Command send = new Command(Command.DEAD_MTP, remoteID);

    send.addParam(mtp);
    send.addParam(cid);

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
   * Calls the <code>transferIdentity</code> method of the remote main container
   */
  public boolean transferIdentity(AID agentID, ContainerID src, ContainerID dest) throws IMTPException, NotFoundException {
    Command send = new Command(Command.TRANSFER_IDENTITY, remoteID);

    send.addParam(agentID);
    send.addParam(src);
    send.addParam(dest);

    try {
      Command result = theDispatcher.dispatchCommand(remoteTAs, send);

      // Check whether an exception occurred in the remote container
      if (checkResult(result, new String[] {
        NOT_FOUND_EXCEPTION
      }) > 0) {
        throw new NotFoundException((String) result.getParamAt(1));
      } 

      return ((Boolean) result.getParamAt(0)).booleanValue();
    } 
    catch (DispatcherException de) {
      throw new IMTPException(DISP_ERROR_MSG, de);
    } 
    catch (UnreachableException ue) {
      throw new IMTPException(UNRCH_ERROR_MSG, ue);
    } 
  } 

  /**
   * Calls the <code>getProxy</code> method of the remote main container
   */
  public AgentProxy getProxy(AID id) throws IMTPException, NotFoundException {
    Command send = new Command(Command.GET_PROXY, remoteID);

    send.addParam(id);

    try {
      Command result = theDispatcher.dispatchCommand(remoteTAs, send);

      // Check whether an exception occurred in the remote container
      if (checkResult(result, new String[] {
        NOT_FOUND_EXCEPTION
      }) > 0) {
        throw new NotFoundException((String) result.getParamAt(1));
      } 

      return (AgentProxy) result.getParamAt(0);
    } 
    catch (DispatcherException de) {
      throw new IMTPException(DISP_ERROR_MSG, de);
    } 
    catch (UnreachableException ue) {
      throw new IMTPException(UNRCH_ERROR_MSG, ue);
    } 
  } 

}

