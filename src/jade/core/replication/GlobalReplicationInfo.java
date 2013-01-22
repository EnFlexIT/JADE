package jade.core.replication;

import jade.core.AID;
import jade.util.Logger;
import jade.util.leap.RoundList;

import java.io.Serializable;

class GlobalReplicationInfo implements Serializable {
	private AID virtualAid;
	private AID masterAid;
	private int replicationMode;
	private RoundList allReplicas = new RoundList();
	
	private Logger myLogger = Logger.getJADELogger(getClass().getName());

	GlobalReplicationInfo(AID virtual, AID master, int replicationMode) {
		this.virtualAid = virtual;
		this.masterAid = master;
		this.replicationMode = replicationMode;
		allReplicas.add(master);
	}

	AID getVirtual() {
		return virtualAid;
	}
	
	AID getMaster() {
		return masterAid;
	}
	
	int getReplicationMode() {
		return replicationMode;
	}
	
	synchronized void addReplica(AID replicaAid) {
		if (!allReplicas.contains(replicaAid)) {
			myLogger.log(Logger.INFO, "Adding replica "+replicaAid.getLocalName()+" to global replication information of virtual agent "+virtualAid.getLocalName());
			allReplicas.add(replicaAid);
		}
	}

	synchronized void removeReplica(AID replicaAid) {
		if (allReplicas.remove(replicaAid)) {
			myLogger.log(Logger.INFO, "Removing replica "+replicaAid.getLocalName()+" from global replication information of virtual agent "+virtualAid.getLocalName());
		}
	}

	synchronized AID getReplica() {
		if (replicationMode == AgentReplicationHelper.HOT_REPLICATION) {
			// HOT replication: select a replica round-robin
			return (AID) allReplicas.get();
		}
		else {
			// COLD replication: always select the master replica 
			return masterAid;
		}
	}

	synchronized AID[] getAllReplicas() {
		Object[] oo = allReplicas.toArray();
		AID[] aids = new AID[oo.length];
		for (int i = 0; i < oo.length; ++i) {
			aids[i] = (AID) oo[i];
		}
		return aids;
	}
	
	/**
	 * Remove the current master replica
	 * Select a new one and return it or null if no replica is available
	 */
	synchronized AID masterReplicaDead() {
		removeReplica(masterAid);
		if (allReplicas.size() == 0) {
			masterAid = null;
		}
		else {
			masterAid = (AID) allReplicas.get();
			myLogger.log(Logger.INFO, "New master replica "+masterAid.getLocalName()+" selected for virtual agent "+virtualAid.getLocalName());
		}
		return masterAid;
	}
	
	/**
	 * Remove the current master replica
	 * Set the new one
	 */
	synchronized void masterReplicaChanged(AID newMasterAid) {
		removeReplica(masterAid);
		masterAid = newMasterAid; 
		myLogger.log(Logger.INFO, "New master replica "+masterAid.getLocalName()+" set for virtual agent "+virtualAid.getLocalName());
	}
}
