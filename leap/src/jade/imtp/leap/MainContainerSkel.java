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
import jade.mtp.TransportAddress;

import jade.security.UserPrincipal;
import jade.security.AgentPrincipal;
import jade.security.AuthException;
import jade.security.JADECertificate;
import jade.security.CertificateFolder;
//import jade.security.IdentityCertificate;
//import jade.security.DelegationCertificate;

import java.util.Vector;
import java.util.Enumeration;

/**
 * @author Giovanni Caire - Telecom Italia LAB
 */
class MainContainerSkel extends Skeleton {
  private MainContainer mc;

  /**
   */
  public MainContainerSkel(MainContainer mc) {
    this.mc = mc;
  }

  /**
   */
  public Command executeCommand(Command command) throws Throwable {
    Command resp = null;

    switch (command.getCode()) {

    case Command.GET_PROXY: {
      AID        id = (AID) command.getParamAt(0);
      AgentProxy arg = mc.getProxy(id);

      resp = new Command(Command.OK);

      resp.addParam(arg);

      break;
    } 

    case Command.GET_PLATFORM_NAME: {
      String name = mc.getPlatformName();

      resp = new Command(Command.OK);

      resp.addParam(name);

      break;
    } 

    case Command.ADD_CONTAINER: {
      AgentContainer ac = (AgentContainer) command.getParamAt(0);
      ContainerID    cid = (ContainerID) command.getParamAt(1);
      String         user = (String) command.getParamAt(2);
      byte[]         passwd = (byte[]) command.getParamAt(3);
      String         arg = mc.addContainer(ac, cid, user, passwd);

      resp = new Command(Command.OK);

      resp.addParam(arg);

      break;
    } 

    case Command.REMOVE_CONTAINER: {
      ContainerID cid = (ContainerID) command.getParamAt(0);

      mc.removeContainer(cid);

      resp = new Command(Command.OK);

      break;
    } 

    case Command.LOOKUP: {
      ContainerID    cid = (ContainerID) command.getParamAt(0);
      AgentContainer arg = mc.lookup(cid);

      resp = new Command(Command.OK);

      resp.addParam(arg);

      break;
    } 

    case Command.BORN_AGENT: {
      AID         name = (AID) command.getParamAt(0);
      ContainerID cid = (ContainerID) command.getParamAt(1);
      CertificateFolder certs = (CertificateFolder) command.getParamAt(2);

      mc.bornAgent(name, cid, certs);

      resp = new Command(Command.OK);

      break;
    } 

    case Command.DEAD_AGENT: {
      AID name = (AID) command.getParamAt(0);

      mc.deadAgent(name);

      resp = new Command(Command.OK);

      break;
    } 

    case Command.SUSPENDED_AGENT: {
      AID name = (AID) command.getParamAt(0);

      mc.suspendedAgent(name);

      resp = new Command(Command.OK);

      break;
    } 

    case Command.RESUMED_AGENT: {
      AID name = (AID) command.getParamAt(0);

      mc.resumedAgent(name);

      resp = new Command(Command.OK);

      break;
    } 

    case Command.CHANGED_AGENT_PRINCIPAL: {
      AID                 name = (AID) command.getParamAt(0);
      CertificateFolder certs = (CertificateFolder) command.getParamAt(1);

      mc.changedAgentPrincipal(name, certs);

      resp = new Command(Command.OK);

      break;
    } 

    case Command.GET_AGENT_PRINCIPAL: {
      AID  name = (AID) command.getParamAt(0);

      AgentPrincipal arg = mc.getAgentPrincipal(name);

      resp = new Command(Command.OK);

      resp.addParam(arg);

      break;
    } 

    case Command.SIGN: {
      JADECertificate   certificate = (JADECertificate) command.getParamAt(0);
      CertificateFolder certs = (CertificateFolder) command.getParamAt(1);
      
      /*Vector                  v = (Vector) command.getParamAt(2);
      DelegationCertificate[] delegations = new DelegationCertificate[v.size()];
      Enumeration             e = v.elements();
      int                     i = 0;
      while (e.hasMoreElements()) {
        delegations[i++] = (DelegationCertificate) e.nextElement();
      } */

      JADECertificate arg = mc.sign(certificate, certs);

      resp = new Command(Command.OK);

      resp.addParam(arg);

      break;
    } 

    case Command.GET_PUBLIC_KEY: {
      byte[] arg = mc.getPublicKey();

      resp = new Command(Command.OK);

      resp.addParam(arg);

      break;
    } 

    case Command.NEW_MTP: {
      MTPDescriptor mtp = (MTPDescriptor) command.getParamAt(0);
      ContainerID   cid = (ContainerID) command.getParamAt(1);

      mc.newMTP(mtp, cid);

      resp = new Command(Command.OK);

      break;
    } 

    case Command.DEAD_MTP: {
      MTPDescriptor mtp = (MTPDescriptor) command.getParamAt(0);
      ContainerID   cid = (ContainerID) command.getParamAt(1);

      mc.deadMTP(mtp, cid);

      resp = new Command(Command.OK);

      break;
    } 

    case Command.TRANSFER_IDENTITY: {
      AID         name = (AID) command.getParamAt(0);
      ContainerID src = (ContainerID) command.getParamAt(1);
      ContainerID dest = (ContainerID) command.getParamAt(2);
      boolean     arg = mc.transferIdentity(name, src, dest);

      resp = new Command(Command.OK);

      resp.addParam(new Boolean(arg));

      break;
    } 

    default:
      throw new DispatcherException("Unknown command code");
    }

    return resp;
  } 

}

