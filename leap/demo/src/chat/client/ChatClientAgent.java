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

package chat.client;

import jade.content.ContentManager;
import jade.content.abs.AbsAggregate;
import jade.content.abs.AbsConcept;
import jade.content.abs.AbsPredicate;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.BasicOntology;
import jade.content.onto.Ontology;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.util.Logger;
import jade.util.leap.Iterator;
import jade.util.leap.Set;
import jade.util.leap.SortedSetImpl;
import chat.ontology.ChatOntology;
//#ANDROID_INCLUDE_BEGIN
import android.content.Context;
//#ANDROID_INCLUDE_END

/**
 * This agent implements the logic of the chat client running on the user
 * terminal. User interactions are handled by the ChatGui in a
 * terminal-dependent way. The ChatClientAgent performs 3 types of behaviours: -
 * ParticipantsManager. A CyclicBehaviour that keeps the list of participants up
 * to date on the basis of the information received from the ChatManagerAgent.
 * This behaviour is also in charge of subscribing as a participant to the
 * ChatManagerAgent. - ChatListener. A CyclicBehaviour that handles messages
 * from other chat participants. - ChatSpeaker. A OneShotBehaviour that sends a
 * message conveying a sentence written by the user to other chat participants.
 * 
 * @author Giovanni Caire - TILAB
 */
public class ChatClientAgent extends Agent {
	private Logger logger = Logger.getMyLogger(this.getClass().getName());

	private static final String CHAT_ID = "__chat__";
	private static final String CHAT_MANAGER_NAME = "manager";

	private ChatGui myGui;
	private Set participants = new SortedSetImpl();
	private Codec codec = new SLCodec();
	private Ontology onto = ChatOntology.getInstance();
	private ACLMessage spokenMsg;

	// #ANDROID_INCLUDE_BEGIN
	private Context context;
	// #ANDROID_INCLUDE_END

	protected void setup() {
		// #ANDROID_INCLUDE_BEGIN
		Object[] args = getArguments();
		if (args != null && args.length > 0) {
			if (args[0] instanceof Context) {
				context = (Context) args[0];
			}
		}
		// #ANDROID_INCLUDE_END
		
		// Register language and ontology
		ContentManager cm = getContentManager();
		cm.registerLanguage(codec);
		cm.registerOntology(onto);
		cm.setValidationMode(false);

		// Add initial behaviours
		addBehaviour(new ParticipantsManager(this));
		addBehaviour(new ChatListener(this));

		// Initialize the message used to convey spoken sentences
		spokenMsg = new ACLMessage(ACLMessage.INFORM);
		spokenMsg.setConversationId(CHAT_ID);

		// Activate the GUI
		// #MIDP_EXCLUDE_BEGIN
		// myGui = new AWTChatGui(this);
		// #MIDP_EXCLUDE_END
		/*
		 * #MIDP_INCLUDE_BEGIN myGui = new MIDPChatGui(this); #MIDP_INCLUDE_END
		 */
		// #ANDROID_INCLUDE_BEGIN
		myGui = new AndroidChatGui(context, this);
    	// #ANDROID_INCLUDE_END
	}

	protected void takeDown() {
		if (myGui != null) {
			myGui.dispose();
		}
	}

	// ///////////////////////////////////////
	// Methods called by the GUI
	// ///////////////////////////////////////
	void handleSpoken(String s) {
		// Add a ChatSpeaker behaviour that INFORMs all participants about
		// the spoken sentence
		addBehaviour(new ChatSpeaker(this, s));
	}

	/**
	 * Inner class ParticipantsManager. This behaviour registers as a chat
	 * participant and keeps the list of participants up to date by managing the
	 * information received from the ChatManager agent.
	 */
	class ParticipantsManager extends CyclicBehaviour {
		private MessageTemplate template;

		ParticipantsManager(Agent a) {
			super(a);
		}

		public void onStart() {
			// Subscribe as a chat participant to the ChatManager agent
			ACLMessage subscription = new ACLMessage(ACLMessage.SUBSCRIBE);
			subscription.setLanguage(codec.getName());
			subscription.setOntology(onto.getName());
			String convId = "C-" + myAgent.getLocalName();
			subscription.setConversationId(convId);
			subscription
					.addReceiver(new AID(CHAT_MANAGER_NAME, AID.ISLOCALNAME));
			myAgent.send(subscription);
			// Initialize the template used to receive notifications
			// from the ChatManagerAgent
			template = MessageTemplate.MatchConversationId(convId);
		}

		public void action() {
			// Receives information about people joining and leaving
			// the chat from the ChatManager agent
			ACLMessage msg = myAgent.receive(template);
			if (msg != null) {
				if (msg.getPerformative() == ACLMessage.INFORM) {
					try {
						AbsPredicate p = (AbsPredicate) myAgent
								.getContentManager().extractAbsContent(msg);
						if (p.getTypeName().equals(ChatOntology.JOINED)) {
							// Get new participants, add them to the list of
							// participants
							// and notify the gui
							AbsAggregate agg = (AbsAggregate) p
									.getAbsTerm(ChatOntology.JOINED_WHO);
							if (agg != null) {
								Iterator it = agg.iterator();
								while (it.hasNext()) {
									AbsConcept c = (AbsConcept) it.next();
									participants.add(BasicOntology
											.getInstance().toObject(c));
								}
							}
							myGui.notifyParticipantsChanged(getParticipantNames());
						}
						if (p.getTypeName().equals(ChatOntology.LEFT)) {
							// Get old participants, remove them from the list
							// of participants
							// and notify the gui
							AbsAggregate agg = (AbsAggregate) p
									.getAbsTerm(ChatOntology.JOINED_WHO);
							if (agg != null) {
								Iterator it = agg.iterator();
								while (it.hasNext()) {
									AbsConcept c = (AbsConcept) it.next();
									participants.remove(BasicOntology
											.getInstance().toObject(c));
								}
							}
							myGui.notifyParticipantsChanged(getParticipantNames());
						}
					} catch (Exception e) {
						Logger.println(e.toString());
						e.printStackTrace();
					}
				} else {
					handleUnexpected(msg);
				}
			} else {
				block();
			}
		}
	} // END of inner class ParticipantsManager

	/**
	 * Inner class ChatListener. This behaviour registers as a chat participant
	 * and keeps the list of participants up to date by managing the information
	 * received from the ChatManager agent.
	 */
	class ChatListener extends CyclicBehaviour {
		private MessageTemplate template = MessageTemplate
				.MatchConversationId(CHAT_ID);

		ChatListener(Agent a) {
			super(a);
		}

		public void action() {
			ACLMessage msg = myAgent.receive(template);
			if (msg != null) {
				if (msg.getPerformative() == ACLMessage.INFORM) {
					myGui.notifySpoken(msg.getSender().getLocalName(),
							msg.getContent());
				} else {
					handleUnexpected(msg);
				}
			} else {
				block();
			}
		}
	} // END of inner class ChatListener

	/**
	 * Inner class ChatSpeaker. INFORMs other participants about a spoken
	 * sentence
	 */
	private class ChatSpeaker extends OneShotBehaviour {
		private String sentence;

		private ChatSpeaker(Agent a, String s) {
			super(a);
			sentence = s;
		}

		public void action() {
			spokenMsg.clearAllReceiver();
			Iterator it = participants.iterator();
			while (it.hasNext()) {
				spokenMsg.addReceiver((AID) it.next());
			}
			spokenMsg.setContent(sentence);
			myGui.notifySpoken(myAgent.getLocalName(), sentence);
			send(spokenMsg);
		}
	} // END of inner class ChatSpeaker

	// ////////////////////////
	// Private utility methods
	// ////////////////////////
	private String[] getParticipantNames() {
		String[] pp = new String[participants.size()];
		Iterator it = participants.iterator();
		int i = 0;
		while (it.hasNext()) {
			AID id = (AID) it.next();
			pp[i++] = id.getLocalName();
		}
		return pp;
	}

	private void handleUnexpected(ACLMessage msg) {
		if (logger.isLoggable(Logger.WARNING)) {
			logger.log(Logger.WARNING, "Unexpected message received from "
					+ msg.getSender().getName());
			logger.log(Logger.WARNING, "Content is: " + msg.getContent());
		}
	}

}
