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

import jade.core.AgentContainer;
import jade.core.MainContainer;
import jade.core.AgentProxy;
import jade.core.AID;
import jade.core.ContainerID;
import jade.core.NotFoundException;
import jade.core.NameClashException;
import jade.core.IMTPException;

import jade.mtp.MTPException;
import jade.mtp.MTPDescriptor;

import jade.security.AuthException;
import jade.security.AgentPrincipal;
import jade.security.ContainerPrincipal;
import jade.security.JADECertificate;
import jade.security.IdentityCertificate;
import jade.security.DelegationCertificate;
import jade.security.CertificateFolder;

/**
   @author Giovanni Rimassa - Universita` di Parma
   @version $Date$ $Revision$
 */
public class MainContainerAdapter implements MainContainer, Serializable {

  private MainContainerRMI adaptee;
  private RMIIMTPManager manager;

  /** Creates new MainContainerAdapter */
  public MainContainerAdapter(MainContainerRMI mc, RMIIMTPManager mgr) {
    adaptee = mc;
    manager = mgr;
  }

  /*public void register(AgentContainerImpl ac, ContainerID cid) throws IMTPException {
    throw new IMTPException("This method cannot be called on an adapter.");
  }  
	*/
  public void newMTP(MTPDescriptor mtp, ContainerID cid) throws IMTPException {
    try {
      adaptee.newMTP(mtp, cid);
    }
    catch (RemoteException re) {
      throw new IMTPException("Communication Failure", re);
    }
  }

  public AgentProxy getProxy(AID id) throws IMTPException, NotFoundException {
    try {
      AgentProxy ap = adaptee.getProxy(id);
      manager.adopt(ap); // This needs to be restored by hand, since it must not be serialized...
      return ap;
    }
    catch (RemoteException re) {
      throw new IMTPException("Communication Failure", re);
    }
  }

  public void bornAgent(AID name, ContainerID cid, CertificateFolder certs) throws IMTPException, NameClashException, NotFoundException, AuthException {
    try {
      adaptee.bornAgent(name, cid, certs);
    }
    catch (RemoteException re) {
      throw new IMTPException("Communication Failure", re);
    }
  }

  public String getPlatformName() throws IMTPException {
    try {
      return adaptee.getPlatformName();
    }
    catch (RemoteException re) {
      throw new IMTPException("Communication Failure", re);
    }
  }

  public AgentContainer lookup(ContainerID cid) throws IMTPException, NotFoundException {
    try {
      return new AgentContainerAdapter(adaptee.lookup(cid), manager);
    }
    catch (RemoteException re) {
      throw new IMTPException("Communication Failure", re);
    }
  }

  public void deadAgent(AID name) throws IMTPException, NotFoundException {
    try {
      adaptee.deadAgent(name);
    }
    catch (RemoteException re) {
      throw new IMTPException("Communication Failure", re);
    }
  }

  public void suspendedAgent(AID name) throws IMTPException, NotFoundException {
    try {
      adaptee.suspendedAgent(name);
    }
    catch (RemoteException re) {
      throw new IMTPException("Communication Failure", re);
    }
  }

  public void resumedAgent(AID name) throws IMTPException, NotFoundException {
    try {
      adaptee.resumedAgent(name);
    }
    catch (RemoteException re) {
      throw new IMTPException("Communication Failure", re);
    }
  }

  public void changedAgentPrincipal(AID name, CertificateFolder certs) throws NotFoundException, IMTPException {
    try {
      adaptee.changedAgentPrincipal(name, certs);
    }
    catch (RemoteException re) {
      throw new IMTPException("Communication Failure", re);
    }
  }

  public AgentPrincipal getAgentPrincipal(AID name) throws IMTPException, NotFoundException {
    try {
      return adaptee.getAgentPrincipal(name);
    }
    catch (RemoteException re) {
      throw new IMTPException("Communication Failure", re);
    }
  }

  public ContainerPrincipal getContainerPrincipal(ContainerID cid) throws IMTPException, NotFoundException {
    try {
      return adaptee.getContainerPrincipal(cid);
    }
    catch (RemoteException re) {
      throw new IMTPException("Communication Failure", re);
    }
  }

  public String addContainer(AgentContainer ac, ContainerID cid, String username, byte[] password) throws IMTPException, AuthException {
    try {
      return adaptee.addContainer(manager.getRMIStub(ac), cid, username, password);
    }
    catch (RemoteException re) {
      throw new IMTPException("Communication Failure", re);
    }
  }

  /*public void deregister(AgentContainer ac) throws IMTPException {
      throw new IMTPException("This method cannot be called on an adapter.");
  }

  public void dispatch(jade.lang.acl.ACLMessage msg, AID receiverID) throws NotFoundException {
      throw new NotFoundException("This method cannot be called on an adapter.");
  }
	*/

  public void deadMTP(MTPDescriptor mtp, ContainerID cid) throws IMTPException {
    try {
      adaptee.deadMTP(mtp, cid);
    }
    catch (RemoteException re) {
      throw new IMTPException("Communication Failure", re);
    }
  }

  public boolean transferIdentity(AID agentID, ContainerID src, ContainerID dest) throws IMTPException, NotFoundException {
    try {
      return adaptee.transferIdentity(agentID, src, dest);
    }
    catch (RemoteException re) {
      throw new IMTPException("Communication Failure", re);
    }
  }

  public void removeContainer(ContainerID cid) throws IMTPException {
    try {
      adaptee.removeContainer(cid);
    }
    catch (RemoteException re) {
      throw new IMTPException("Communication Failure", re);
    }
  }

  // This method serializes the adaptee instead of the adapter. Since the adaptee
  // is really an RMI remote object, the stub is really sent over the network. This
  // technique is used to support bynary remote methods, i.e. remote methods that have
  // a remote object as a parameter. The RMI server object will get the stub and build
  // a new adapter around it for the implementation to use.
  private Object writeReplace() {
    return adaptee;
  }

  public JADECertificate sign(JADECertificate certificate, CertificateFolder certs) throws IMTPException, AuthException {
    try {
      return adaptee.sign(certificate, certs);
    }
    catch (RemoteException re) {
      throw new IMTPException("Communication Failure", re);
    }
  }

  public byte[] getPublicKey() throws IMTPException {
    try {
      return adaptee.getPublicKey();
    }
    catch (RemoteException re) {
      throw new IMTPException("Communication Failure", re);
    }
  }

}
