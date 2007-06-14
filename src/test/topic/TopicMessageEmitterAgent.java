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

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.messaging.TopicManagementHelper;
import jade.lang.acl.ACLMessage;


/**
   @author Giovanni Caire - TILAB
 */
public class TopicMessageEmitterAgent extends Agent {
	public static final char SEPARATOR = '#';
	
	protected void setup() {
		try {
			final TopicManagementHelper topicHelper = (TopicManagementHelper) getHelper(TopicManagementHelper.SERVICE_NAME);
			addBehaviour(new CyclicBehaviour(this) {
				public void action() {
					ACLMessage msg = myAgent.receive();
					if (msg != null) {
						String tmp = msg.getContent();
						int index = tmp.indexOf(SEPARATOR);
						if (index > 0) {
							String topic = tmp.substring(0, index);
							String content = tmp.substring(index+1);
							ACLMessage topicMsg = new ACLMessage(ACLMessage.INFORM);
							topicMsg.addReceiver(topicHelper.createTopic(topic));
							topicMsg.setContent(content);
							myAgent.send(topicMsg);
						}
						else {
							System.err.println("########### ERROR: Unexpected message received from agent "+msg.getSender().getName()+" ############");
							System.err.println(msg);
						}
					}
					else {
						block();
					}
				}
			} );
		}
		catch (Exception e) {
			System.err.println("########### ERROR retrieving TopicManagementHelper ############");
			e.printStackTrace();
		}
	}
}