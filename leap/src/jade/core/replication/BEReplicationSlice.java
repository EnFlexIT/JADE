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

//#MIDP_EXCLUDE_FILE

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


    // Constants for the names of horizontal commands associated to methods
    static final String H_ACCEPTREPLICA = "1";
    static final String H_SETREPLICAS = "2";
    static final String H_GETLABEL = "3";
    static final String H_ADDREPLICA = "4";
    static final String H_REMOVEREPLICA = "5";
    static final String H_BORNAGENT = "6";
    static final String H_DEADAGENT = "7";

    void acceptReplica(String sliceName, String replicaIndex) throws IMTPException;
    void setReplicas(String[] replicas) throws IMTPException;

    String getLabel() throws IMTPException;
    void addReplica(String sliceName) throws IMTPException;
    void removeReplica(String sliceName) throws IMTPException;

    void bornAgent(AID name) throws IMTPException;
    void deadAgent(AID name) throws IMTPException;

}
