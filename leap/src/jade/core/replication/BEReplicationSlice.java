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

package jade.core.replication;

//#J2ME_EXCLUDE_FILE

import jade.core.Service;
import jade.core.AID;
import jade.core.ContainerID;
import jade.core.Location;
import jade.core.IMTPException;
import jade.core.ServiceException;
import jade.core.NameClashException;
import jade.core.NotFoundException;

import jade.mtp.MTPDescriptor;

import jade.security.CertificateFolder;
import jade.security.AuthException;


/**
   The horizontal interface for the JADE kernel-level service managing
   the back-end container replication subsystem installed in the platform.

   @author Giovanni Rimassa - FRAMeTech s.r.l.
*/
public interface BEReplicationSlice extends Service.Slice {

    // Constants for the names of the service vertical commands

    /**
       The name of this service.
    */
    static final String NAME = "jade.core.replication.BEReplication";

    /**
       This command name represents the <code>become-master</code>
       action. This action requests the local back end to become the
       master replica and to notify all other replicas of this change.
    */
    static final String BECOME_MASTER = "Become-Master";

    /**
       This command name represents the <code>is-master</code>
       query. This query asks whether the local back end is the master
       replica (i.e. the one actually connected to the front end).
    */
    static final String IS_MASTER = "Is-Master";

    /**
       This command name represents the <code>get-master-name</code>
       query. It asks for the name of the replica slice which is
       currently serving as master replica (i.e. the one the Front-End
       container is connected to).
    */
    static final String GET_MASTER_NAME = "Get-Master-Name";

    /**
       This command name represents the <code>start-monitor</code>
       action. This action activates a periodical check that tries to
       restart any dead Back-End containers at their former addresses.
    */
    static final String START_MONITOR = "Start-Monitor";

    /**
       This command name represents the <code>stop-monitor</code>
       action. This action deactivates any periodical check for dead
       Back-End containers that might be active at the time.
    */
    static final String STOP_MONITOR = "Stop-Monitor";


    // Constants for the names of horizontal commands associated to methods
    static final String H_ACCEPTREPLICA = "1";
    static final String H_SETMASTER = "2";
    static final String H_SETREPLICAS = "3";
    static final String H_REPLICAUP = "4";
    static final String H_REPLICADOWN = "5";
    static final String H_EXITREPLICA = "6";
    static final String H_BORNAGENT = "7";
    static final String H_DEADAGENT = "8";

    void acceptReplica(String sliceName, String replicaIndex) throws IMTPException;
    void setMaster(String name) throws IMTPException;
    void setReplicas(String[] replicas, boolean[] status) throws IMTPException;

    void replicaUp(int index) throws IMTPException;
    void replicaDown(int index) throws IMTPException;
    void exitReplica() throws IMTPException;

    void bornAgent(AID name) throws IMTPException;
    void deadAgent(AID name) throws IMTPException;

}
