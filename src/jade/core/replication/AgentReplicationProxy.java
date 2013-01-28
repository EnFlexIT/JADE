package jade.core.replication;

//#APIDOC_EXCLUDE_FILE

import jade.core.AID;
import jade.core.ContainerID;
import jade.core.GenericCommand;
import jade.core.IMTPException;
import jade.core.Node;
import jade.core.NotFoundException;
import jade.core.ServiceException;
import jade.core.SliceProxy;

public class AgentReplicationProxy extends SliceProxy implements AgentReplicationSlice {

	public void invokeAgentMethod(AID aid, String methodName, Object[] arguments) throws IMTPException, ServiceException, NotFoundException {
		GenericCommand cmd = new GenericCommand(H_INVOKEAGENTMETHOD, AgentReplicationService.NAME, null);
		cmd.addParam(aid);
		cmd.addParam(methodName);
		cmd.addParam(arguments);
		
		Node n = getNode();
		Object result = n.accept(cmd);
		if((result != null) && (result instanceof Throwable)) {
			if(result instanceof NotFoundException) {
				throw (NotFoundException)result;
			}
			else if(result instanceof ServiceException) {
				throw (ServiceException)result;
			}
			else if(result instanceof IMTPException) {
				throw (IMTPException)result;
			}
			else {
				throw new IMTPException("An undeclared exception was thrown", (Throwable)result);
			}
		}
	}
	
	public ContainerID getAgentLocation(AID aid) throws IMTPException, NotFoundException {
		GenericCommand cmd = new GenericCommand(H_GETAGENTLOCATION, AgentReplicationService.NAME, null);
		cmd.addParam(aid);
		
		try {
			Node n = getNode();
			Object result = n.accept(cmd);
			if ((result != null) && (result instanceof Throwable)) {
				if(result instanceof NotFoundException) {
					throw (NotFoundException)result;
				}
				else if(result instanceof IMTPException) {
					throw (IMTPException)result;
				}
				else {
					throw new IMTPException("An undeclared exception was thrown", (Throwable)result);
				}
			}
			return (ContainerID)result;
		}
		catch (ServiceException se) {
			throw new IMTPException("Error accessing remote node", se);
		}
	}
	
	public void replicaCreationRequested(AID virtualAid, AID replicaAid) throws IMTPException {
		GenericCommand cmd = new GenericCommand(H_REPLICACREATIONREQUESTED, AgentReplicationService.NAME, null);
		cmd.addParam(virtualAid);
		cmd.addParam(replicaAid);
		
		try {
			Node n = getNode();
			Object result = n.accept(cmd);
			if ((result != null) && (result instanceof Throwable)) {
				if(result instanceof IMTPException) {
					throw (IMTPException)result;
				}
				else {
					throw new IMTPException("An undeclared exception was thrown", (Throwable)result);
				}
			}
		}
		catch (ServiceException se) {
			throw new IMTPException("Error accessing remote node", se);
		}
	}

	public void synchReplication(GlobalReplicationInfo info) throws IMTPException {
		GenericCommand cmd = new GenericCommand(H_SYNCHREPLICATION, AgentReplicationService.NAME, null);
		cmd.addParam(info.getVirtual());
		cmd.addParam(info.getMaster());
		cmd.addParam(info.getReplicationMode());
		cmd.addParam(info.getAllReplicas());
		
		try {
			Node n = getNode();
			Object result = n.accept(cmd);
			if ((result != null) && (result instanceof Throwable)) {
				if(result instanceof IMTPException) {
					throw (IMTPException)result;
				}
				else {
					throw new IMTPException("An undeclared exception was thrown", (Throwable)result);
				}
			}
		}
		catch (ServiceException se) {
			throw new IMTPException("Error accessing remote node", se);
		}
	}

	public void notifyBecomeMaster(AID masterAid) throws IMTPException {
		GenericCommand cmd = new GenericCommand(H_NOTIFYBECOMEMASTER, AgentReplicationService.NAME, null);
		cmd.addParam(masterAid);
		
		try {
			Node n = getNode();
			Object result = n.accept(cmd);
			if ((result != null) && (result instanceof Throwable)) {
				if(result instanceof IMTPException) {
					throw (IMTPException)result;
				}
				else {
					throw new IMTPException("An undeclared exception was thrown", (Throwable)result);
				}
			}
		}
		catch (ServiceException se) {
			throw new IMTPException("Error accessing remote node", se);
		}
	}
}
