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

//#MIDP_EXCLUDE_FILE


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


    /**
       The name of this service.
    */
    public static final String NAME = "jade.core.mobility.AgentMobility";


    /**
       This command name represents the <code>move-agent</code>
       action.
       This command object represents only the <i>first half</i> of
       the complete agent migration process. Even if this command is
       accepted by the kernel, there is no guarantee that the
       requested mogration will ever happen. Only when the
       <code>InformMoved</code> command is issued can one assume that
       the agent migration has taken place.
    */
    static final String REQUEST_MOVE = "Request-Move";

    /**
       This command name represents the <code>clone-agent</code>
       action.
       This command object represents only the <i>first half</i> of
       the complete agent clonation process. Even if this command is
       accepted by the kernel, there is no guarantee that the
       requested clonation will ever happen. Only when the
       <code>InformCloned</code> command is issued can one assume that
       the agent clonation has taken place.
    */
    static final String REQUEST_CLONE = "Request-Clone";

    /**
       This command is issued by an agent that has just migrated.
       The agent migration can either be an autonomous move of the
       agent or the outcome of a previously issued
       <code>RequestMove</code> command. In the second case, this
       command represents only the <i>second half</i> of the complete
       agent migration process.
    */
    static final String INFORM_MOVED = "Inform-Moved";

    /**
       This command is issued by an agent that has just cloned itself.
       The agent clonation can either be an autonomous move of the
       agent or the outcome of a previously issued
       <code>RequestClone</code> command. In the second case, this
       command represents only the <i>second half</i> of the complete
       agent clonation process.
    */
    static final String INFORM_CLONED = "Inform-Cloned";



    // Constants for the names of horizontal commands associated to methods
    static final String H_CREATEAGENT = "1";
    static final String H_FETCHCLASSFILE = "2";
    static final String H_MOVEAGENT = "3";
    static final String H_COPYAGENT = "4";
    static final String H_PREPARE = "5";
    static final String H_TRANSFERIDENTITY = "6";
    static final String H_HANDLETRANSFERRESULT = "7";
    static final String H_CLONEDAGENT = "8";

    void createAgent(AID agentID, byte[] serializedInstance, String classSiteName, boolean isCloned, boolean startIt) throws IMTPException, ServiceException, NotFoundException, NameClashException, AuthException;
    byte[] fetchClassFile(String name) throws IMTPException, ClassNotFoundException;

    void moveAgent(AID agentID, Location where) throws IMTPException, NotFoundException;
    void copyAgent(AID agentID, Location where, String newName) throws IMTPException, NotFoundException;

    boolean prepare() throws IMTPException;

    boolean transferIdentity(AID agentID, Location src, Location dest) throws IMTPException, NotFoundException;
    void handleTransferResult(AID agentID, boolean result, List messages) throws IMTPException, NotFoundException;
    void clonedAgent(AID agentID, ContainerID cid, CertificateFolder certs) throws IMTPException, AuthException, NotFoundException, NameClashException;

}
