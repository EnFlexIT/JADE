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


import jade.mtp.MTPDescriptor;

import jade.security.CertificateFolder;
import jade.security.AuthException;

import jade.util.leap.List;

/**
   @author Giovanni Rimassa - Universita' di Parma
   @version $Date$ $Revision$
*/

public interface MainContainer {

    String getPlatformName();

    void bornAgent(AID name, ContainerID cid, CertificateFolder certs, boolean forceReplacement) throws IMTPException, NameClashException, NotFoundException, AuthException;
    void deadAgent(AID name) throws IMTPException, NotFoundException;
    void suspendedAgent(AID name) throws IMTPException, NotFoundException;
    void resumedAgent(AID name) throws IMTPException, NotFoundException;

    void newMTP(MTPDescriptor mtp, ContainerID cid) throws IMTPException;
    void deadMTP(MTPDescriptor mtp, ContainerID cid) throws IMTPException;

    ContainerID[] containerIDs();
    AID[] agentNames();
    List containerMTPs(ContainerID cid) throws NotFoundException;
    List containerAgents(ContainerID cid) throws NotFoundException;
    ContainerID getContainerID(AID agentID) throws NotFoundException;
    Node getContainerNode(ContainerID cid) throws NotFoundException;

    void lockEntryForAgent(AID agentID);
    void updateEntryForAgent(AID agentID, Location srcID, Location destID) throws IMTPException, NotFoundException;
    void unlockEntryForAgent(AID agentID);

}
