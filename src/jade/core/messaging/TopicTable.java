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

package jade.core.messaging;

//#MIDP_EXCLUDE_FILE
//#APIDOC_EXCLUDE_FILE

import jade.core.AID;
import jade.lang.acl.ACLMessage;

import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

/**
 * @author Giovanni Caire - TILAB
 */
class TopicTable {
	// Maps a given topic with a TopicInfo object embedding the relevant information for that topic 
	private Map allRegistrations = new HashMap();
	
	final synchronized void register(AID aid, AID topic) {
		TopicInfo info = (TopicInfo) allRegistrations.get(topic);
		if (info == null) {
			info = new TopicInfo(topic);
			allRegistrations.put(topic, info);
		}
		info.addAgent(aid);
	}
	
	final synchronized void deregister(AID aid, AID topic) {
		TopicInfo info = (TopicInfo) allRegistrations.get(topic);
		if (info != null) {
			info.removeAgent(aid);
			if (info.isEmpty()) {
				// NO Agent is interested in this topic anymore --> remove it
				allRegistrations.remove(topic);
			}
		}
	}
	
	final synchronized List getAllRegistrations() {
		List l = new ArrayList();
		Iterator it = allRegistrations.values().iterator();
		while (it.hasNext()) {
			TopicInfo info = (TopicInfo) it.next();
			AID[] agents = info.getAgents();
			for (int i = 0; i < agents.length; ++i) {
				l.add(new TopicRegistration(agents[i], info.getTopic()));
			}
		}
		return l;
	}
	
	/**
	 * Retrieve all agents that are interested in receiving a given message directed to a given topic
	 * NOTE that only a section of this method is synchronized to speed up performances 
	 */
	final synchronized AID[] getInterestedAgents(AID topic, ACLMessage msg) {
		AID[] interestedAgents = null;
		TopicInfo info = (TopicInfo) allRegistrations.get(topic);
		if (info != null) {
			interestedAgents = info.getAgents();
		}
		return interestedAgents;
	}
	
	/**
	 * Retrieve the list of topics a given agent is interested in
	 */
	final synchronized List getRelevantTopics(AID aid) {
		List l = new ArrayList();
		Iterator it = allRegistrations.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
			TopicInfo info = (TopicInfo) entry.getValue();
			if (info.containsAgent(aid)) {
				l.add(entry.getKey());
			}
		}
		return l;
	}
	
	
	/**
	 * Inner class TopicInfo
	 * This class embeds all information related to a given topic i.e. all registrations to that topic
	 */
	private class TopicInfo {
		private AID topic;
		// This is used to keep agents interested in this topic
		private Set agents = new HashSet();
		// This is used to deliver messages to agents interested in this topic without any synchronization requirement with respect to insertions and deletions  
		private volatile AID[] agentsArray = new AID[0];
		
		private TopicInfo(AID topic) {
			this.topic = topic;
		}
		
		public final AID getTopic() {
			return topic;			
		}
		
		public final void addAgent(AID aid) {
			agents.add(aid);
			agentsArray = (AID[]) agents.toArray(new AID[0]);
		}
		
		public final void removeAgent(AID aid) {
			agents.remove(aid);
			agentsArray = (AID[]) agents.toArray(new AID[0]);
		}
				
		public final AID[] getAgents() {
			return agentsArray;
		}
		
		public final boolean isEmpty() {
			return agents.isEmpty();
		}
		
		public final boolean containsAgent(AID aid) {
			return agents.contains(aid);
		}
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer("REGISTRATIONS:\n");
		Iterator it = allRegistrations.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
			TopicInfo info = (TopicInfo) entry.getValue();
			AID topic = (AID) entry.getKey();
			sb.append("- Topic "+topic.getLocalName()+"\n");
			AID[] agents = info.getAgents();
			for (int i = 0; i < agents.length; ++i) {
				sb.append("  - "+agents[i].getLocalName()+"\n");
			}
		}
		return sb.toString();
	}
}
