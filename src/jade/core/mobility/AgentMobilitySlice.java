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

package jade.core.mobility;

import jade.core.Service;
import jade.core.Filter;
import jade.core.AID;
import jade.core.ContainerID;
import jade.core.Location;
import jade.core.IMTPException;
import jade.core.ServiceException;
import jade.core.NotFoundException;
import jade.core.NameClashException;

import jade.security.CertificateFolder;
import jade.security.AuthException;

import jade.util.leap.List;

/**

   The horizontal interface for the JADE kernel-level service managing
   the mobility-related agent life cycle: migration and clonation.

   @author Giovanni Rimassa - FRAMeTech s.r.l.
*/
public interface AgentMobilitySlice extends Service.Slice {

    void createAgent(AID agentID, byte[] serializedInstance, String classSiteName, boolean isCloned, boolean startIt) throws IMTPException, ServiceException, NotFoundException, AuthException;
    byte[] fetchClassFile(String name) throws IMTPException, ClassNotFoundException;

    void moveAgent(AID agentID, Location where) throws IMTPException, NotFoundException;
    void copyAgent(AID agentID, Location where, String newName) throws IMTPException, NotFoundException;

    boolean prepare();

    boolean transferIdentity(AID agentID, Location src, Location dest) throws IMTPException, NotFoundException;
    void handleTransferResult(AID agentID, boolean result, List messages) throws IMTPException, NotFoundException;
    void clonedAgent(AID agentID, ContainerID cid, CertificateFolder certs) throws IMTPException, AuthException, NotFoundException, NameClashException;

}
