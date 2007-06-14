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

package test.topic;

import jade.core.*;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.messaging.TopicManagementHelper;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;


/**
   @author Giovanni Caire - TILAB
 */
public class TopicMessageCollectorAgent extends Agent {
	public static final String COLLECTOR_CONV_ID = "collector-cid";
	
	protected void setup() {
		Object[] args = getArguments();
		if (args != null && args.length == 2) {
			// The topic to register to
			String topicName = (String) args[0];
			// The agent to forward topic-message to
			final AID target = (AID) args[1];
			
			try {
				TopicManagementHelper topicHelper = (TopicManagementHelper) getHelper(TopicManagementHelper.SERVICE_NAME);
				final AID topic = topicHelper.createTopic(topicName);
				topicHelper.register(topic);
				
				addBehaviour(new CyclicBehaviour(this) {
					public void action() {
						ACLMessage msg = myAgent.receive(MessageTemplate.MatchReceiver(new AID[]{topic}));
						if (msg != null) {
							// Forward the received message to the target agent
							msg.setSender(myAgent.getAID());
							msg.clearAllReceiver();
							msg.addReceiver(target);
							msg.setConversationId(COLLECTOR_CONV_ID);
							myAgent.send(msg);
						}
						else {
							block();
						}
					}
				} );
			}
			catch (Exception e) {
				System.err.println("########### CollectorAgent "+getLocalName()+": ERROR registering to topic "+topicName+" ############");
				e.printStackTrace();
			}
		}
		else {
			System.err.println("########### CollectorAgent "+getLocalName()+": WRONG arguments. Aborting... ############");
			doDelete();
		}
	}
}