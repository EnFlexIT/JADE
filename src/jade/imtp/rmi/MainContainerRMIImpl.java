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
import jade.core.AgentContainer;
import jade.core.MainContainer;
import jade.core.IMTPException;
import jade.core.NotFoundException;
import jade.core.NameClashException;
import jade.core.AgentProxy;

import jade.mtp.MTPDescriptor;

import jade.security.AuthException;
import jade.security.AgentPrincipal;
import jade.security.JADECertificate;
import jade.security.IdentityCertificate;
import jade.security.DelegationCertificate;
import jade.security.CertificateFolder;

/**
   @author Giovanni Rimassa - Universita` di Parma
   @version $Date$ $Revision$
 */
public class MainContainerRMIImpl extends UnicastRemoteObject implements MainContainerRMI {

  private MainContainer impl;
  private RMIIMTPManager manager;

  /** Creates new MainContainerRMIImpl */
  public MainContainerRMIImpl(MainContainer mc, RMIIMTPManager mgr) throws RemoteException {
    super(0, new RMISSLClientSocketFactory(), new RMISSLServerSocketFactory());
    impl = mc;
    manager = mgr;
  }

  public AgentContainerRMI lookup(ContainerID cid) throws RemoteException, NotFoundException, IMTPException {
    AgentContainer ac = impl.lookup(cid);
    return manager.getRMIStub(ac);
  }

  public void deadMTP(MTPDescriptor mtp, ContainerID cid) throws RemoteException, IMTPException {
    impl.deadMTP(mtp, cid);
  }

  public AgentProxy getProxy(AID id) throws RemoteException, NotFoundException, IMTPException {
    return impl.getProxy(id);
  }

  public boolean transferIdentity(AID agentID, ContainerID src, ContainerID dest) throws RemoteException, NotFoundException, IMTPException {
    return impl.transferIdentity(agentID, src, dest);
  }

  public String getPlatformName() throws RemoteException, IMTPException {
    return impl.getPlatformName();
  }

  public void bornAgent(AID name, ContainerID cid, CertificateFolder certs) throws RemoteException, NameClashException, NotFoundException, IMTPException, AuthException {
    impl.bornAgent(name, cid, certs);
  }
  
  public void removeContainer(ContainerID cid) throws RemoteException, IMTPException {
    impl.removeContainer(cid);
  }
  
  public String addContainer(AgentContainerRMI ac, ContainerID cid, String username, byte[] password) throws RemoteException, IMTPException, AuthException {
    AgentContainer cont = manager.getAdapter(ac);
    return impl.addContainer(cont, cid, username, password);
  }

  public void deadAgent(AID name) throws RemoteException, NotFoundException, IMTPException {
    impl.deadAgent(name);
  }
  
  public void suspendedAgent(AID name) throws RemoteException, NotFoundException, IMTPException {
    impl.suspendedAgent(name);
  }
  
  public void resumedAgent(AID name) throws RemoteException, NotFoundException, IMTPException {
    impl.resumedAgent(name);
  }
  
  public void changedAgentPrincipal(AID name, CertificateFolder certs) throws RemoteException, NotFoundException, IMTPException {
    impl.changedAgentPrincipal(name, certs);
  }
  
  public AgentPrincipal getAgentPrincipal(AID name) throws RemoteException, IMTPException, NotFoundException {
    return impl.getAgentPrincipal(name);
  }

  public void newMTP(MTPDescriptor mtp, ContainerID cid) throws RemoteException, IMTPException {
    impl.newMTP(mtp, cid);
  }
  
  public JADECertificate sign(JADECertificate certificate, CertificateFolder certs) throws RemoteException, IMTPException, AuthException {
    return impl.sign(certificate, certs);
  }
  
  public byte[] getPublicKey() throws RemoteException, IMTPException {
    return impl.getPublicKey();
  }

}
