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

import java.rmi.Remote;
import java.rmi.RemoteException;

import jade.core.AID;
import jade.core.ContainerID;
import jade.core.AgentProxy;
import jade.core.IMTPException;
import jade.core.NotFoundException;
import jade.core.NameClashException;

import jade.mtp.MTPDescriptor;

import jade.security.AuthException;
import jade.security.AgentPrincipal;
import jade.security.UserPrincipal;
import jade.security.IdentityCertificate;
import jade.security.DelegationCertificate;

/**
   @author Giovanni Rimassa - Universita` di Parma
   @version $Date$ $Revision$
 */
public interface MainContainerRMI extends Remote {
    public void newMTP(MTPDescriptor mtp, ContainerID cid) throws RemoteException, IMTPException;
    public AgentProxy getProxy(AID id) throws RemoteException, NotFoundException, IMTPException;
    public void bornAgent(AID name, ContainerID cid) throws RemoteException, NameClashException, NotFoundException, IMTPException;
    public String getPlatformName() throws RemoteException, IMTPException;
    public AgentContainerRMI lookup(ContainerID cid) throws RemoteException, NotFoundException, IMTPException;
    public void deadAgent(AID name) throws RemoteException, NotFoundException, IMTPException;
    public void suspendedAgent(AID name) throws RemoteException, NotFoundException, IMTPException;
    public void resumedAgent(AID name) throws RemoteException, NotFoundException, IMTPException;
    public void changedAgentPrincipal(AID name, AgentPrincipal from, AgentPrincipal to) throws RemoteException, NotFoundException, IMTPException;
    public String addContainer(AgentContainerRMI ac, ContainerID cid, UserPrincipal user, byte[] passwd) throws RemoteException, IMTPException, AuthException;
    public void deadMTP(MTPDescriptor mtp, ContainerID cid) throws RemoteException, IMTPException;
    public boolean transferIdentity(AID agentID, ContainerID src, ContainerID dest) throws RemoteException, NotFoundException, IMTPException;
    public void removeContainer(ContainerID cid) throws RemoteException, IMTPException;
    public DelegationCertificate sign(DelegationCertificate certificate, IdentityCertificate identity, DelegationCertificate[] delegations) throws RemoteException, IMTPException, AuthException;
    public byte[] getPublicKey() throws RemoteException, IMTPException;
}

