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

import jade.core.Node;
import jade.core.Service;
import jade.core.SliceProxy;
import jade.core.GenericCommand;
import jade.core.AID;
import jade.core.ContainerID;
import jade.core.Location;
import jade.core.IMTPException;
import jade.core.ServiceException;
import jade.core.NotFoundException;
import jade.core.NameClashException;

import jade.mtp.MTPDescriptor;

/**
   The remote proxy for the JADE kernel-level service managing
   the back-end container replication subsystem installed in the platform.

   @author Giovanni Rimassa - FRAMeTech s.r.l.
*/
public class BEReplicationProxy extends SliceProxy implements BEReplicationSlice {

    public void acceptReplica(String sliceName, String replicaIndex) throws IMTPException {
	try {
	    GenericCommand cmd = new GenericCommand(H_ACCEPTREPLICA, NAME, null);
	    cmd.addParam(sliceName);
	    cmd.addParam(replicaIndex);

	    Node n = getNode();
	    Object result = n.accept(cmd);
	    if((result != null) && (result instanceof Throwable)) {
		if(result instanceof IMTPException) {
		    throw (IMTPException)result;
		}
		else {
		    throw new IMTPException("An undeclared exception was thrown", (Throwable)result);
		}
	    }

	}
	catch(ServiceException se) {
	    throw new IMTPException("Unable to access remote node", se);
	}
    }

    public void setMaster(String name) throws IMTPException {
	try {
	    GenericCommand cmd = new GenericCommand(H_SETMASTER, NAME, null);
	    cmd.addParam(name);

	    Node n = getNode();
	    Object result = n.accept(cmd);
	    if((result != null) && (result instanceof Throwable)) {
		if(result instanceof IMTPException) {
		    throw (IMTPException)result;
		}
		else {
		    throw new IMTPException("An undeclared exception was thrown", (Throwable)result);
		}
	    }
	}
	catch(ServiceException se) {
	    throw new IMTPException("Unable to access remote node", se);
	}
    }

    public void setReplicas(String[] replicas, boolean[] status) throws IMTPException {
	try {
	    GenericCommand cmd = new GenericCommand(H_SETREPLICAS, NAME, null);
	    cmd.addParam(replicas);
	    cmd.addParam(status);

	    Node n = getNode();
	    Object result = n.accept(cmd);
	    if((result != null) && (result instanceof Throwable)) {
		if(result instanceof IMTPException) {
		    throw (IMTPException)result;
		}
		else {
		    throw new IMTPException("An undeclared exception was thrown", (Throwable)result);
		}
	    }

	}
	catch(ServiceException se) {
	    throw new IMTPException("Unable to access remote node", se);
	}
    }

    public void replicaUp(int index) throws IMTPException {
	try {
	    GenericCommand cmd = new GenericCommand(H_REPLICAUP, NAME, null);
	    cmd.addParam(new Integer(index));

	    Node n = getNode();
	    Object result = n.accept(cmd);
	    if((result != null) && (result instanceof Throwable)) {
		if(result instanceof IMTPException) {
		    throw (IMTPException)result;
		}
		else {
		    throw new IMTPException("An undeclared exception was thrown", (Throwable)result);
		}
	    }
	}
	catch(ServiceException se) {
	    throw new IMTPException("Unable to access remote node", se);
	}
    }

    public void replicaDown(int index) throws IMTPException { 
	try {
	    GenericCommand cmd = new GenericCommand(H_REPLICADOWN, NAME, null);
	    cmd.addParam(new Integer(index));

	    Node n = getNode();
	    Object result = n.accept(cmd);
	    if((result != null) && (result instanceof Throwable)) {
		if(result instanceof IMTPException) {
		    throw (IMTPException)result;
		}
		else {
		    throw new IMTPException("An undeclared exception was thrown", (Throwable)result);
		}
	    }
	}
	catch(ServiceException se) {
	    throw new IMTPException("Unable to access remote node", se);
	}
    }

    public void exitReplica() throws IMTPException {
	try {
	    GenericCommand cmd = new GenericCommand(H_EXITREPLICA, NAME, null);

	    Node n = getNode();
	    Object result = n.accept(cmd);
	    if((result != null) && (result instanceof Throwable)) {
		if(result instanceof IMTPException) {
		    throw (IMTPException)result;
		}
		else {
		    throw new IMTPException("An undeclared exception was thrown", (Throwable)result);
		}
	    }
	}
	catch(ServiceException se) {
	    throw new IMTPException("Unable to access remote node", se);
	}
    }

    public void bornAgent(AID name) throws IMTPException {
	try {
	    GenericCommand cmd = new GenericCommand(H_BORNAGENT, NAME, null);
	    cmd.addParam(name);

	    Node n = getNode();
	    Object result = n.accept(cmd);
	    if((result != null) && (result instanceof Throwable)) {
		if(result instanceof IMTPException) {
		    throw (IMTPException)result;
		}
		else {
		    throw new IMTPException("An undeclared exception was thrown", (Throwable)result);
		}
	    }
	}
	catch(ServiceException se) {
	    throw new IMTPException("Unable to access remote node", se);
	}
    }

    public void deadAgent(AID name) throws IMTPException {
	try {
	    GenericCommand cmd = new GenericCommand(H_DEADAGENT, NAME, null);
	    cmd.addParam(name);

	    Node n = getNode();
	    Object result = n.accept(cmd);
	    if((result != null) && (result instanceof Throwable)) {
		if(result instanceof IMTPException) {
		    throw (IMTPException)result;
		}
		else {
		    throw new IMTPException("An undeclared exception was thrown", (Throwable)result);
		}
	    }
	}
	catch(ServiceException se) {
	    throw new IMTPException("Unable to access remote node", se);
	}
    }

}
