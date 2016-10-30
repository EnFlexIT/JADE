/*****************************************************************
 WADE - Workflow and Agent Development Environment is a framework to develop 
 multi-agent systems able to execute tasks defined according to the workflow
 metaphor.
 Copyright (C) 2008 Telecom Italia S.p.A. 

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
package jade.core.behaviours;

//#J2ME_EXCLUDE_FILE

import jade.content.lang.leap.LEAPCodec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.core.AID;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.ConversationList;
import jade.proto.AchieveREInitiator;
import jade.util.Logger;
import jade.util.leap.Iterator;

import java.util.Vector;

/**
 * Base class for behaviours implementing a FIPA-request like pattern
 * with a single target agent.
 */
public abstract class BaseInitiator extends AchieveREInitiator {
	private static final long serialVersionUID = -6505544004754497428L;
	
	private AID target;
	private String defaultTargetDescription;
	
	protected OutcomeManager outcome;
	protected ConversationList conversations;
	private String convId;
	private static int conversationCnt = 0;

	protected Logger myLogger = Logger.getJADELogger(getClass().getName());
	
	public BaseInitiator() {
		this(null);
	}
	public BaseInitiator(OutcomeManager om) {
		super(null, null);
		outcome = om != null ? om : new OutcomeManager(this);
	}
	
	public void setActiveConversations(ConversationList conversations) {
		this.conversations = conversations;
	}
	
	protected void checkLanguage(String languageName) {
		if (myAgent.getContentManager().lookupLanguage(languageName) == null) {
			if (languageName.equals(FIPANames.ContentLanguage.FIPA_SL)) {
				myAgent.getContentManager().registerLanguage(new SLCodec(true));
			}
			else if (languageName.equals(LEAPCodec.NAME)) {
				myAgent.getContentManager().registerLanguage(new LEAPCodec());
			}
		}
	}
	
	protected void checkOntology(Ontology onto) {
		if (myAgent.getContentManager().lookupOntology(onto.getName()) == null) {
			myAgent.getContentManager().registerOntology(onto);
		}
	}
	
	public OutcomeManager getOutcome() {
		return outcome;
	}
	
	/**
	 * Shortcut method for getOutcome().getExitCode()
	 */
	public int getExitCode() {
		return outcome.getExitCode();
	}
	
	/**
	 * Shortcut method for getOutcome().getErrorMsg()
	 */
	public String getErrorMsg() {
		return outcome.getErrorMsg();
	}
	
	/**
	 * Concrete subclasses are expected to implement this method to create the initiation message.
	 * @return The initiation message used by this behaviour 
	 */
	protected abstract ACLMessage createInitiation();
	
	/**
	 * Concrete subclasses can redefine this method to provide a human readable description of the 
	 * target agent of this behaviour. Such description will be used in all default error messages. 
	 * @return A human readable description of the target agent of this behaviour
	 */
	protected String getTargetDescription() {
		return defaultTargetDescription;
	}
	
	@Override
	public Vector prepareRequests(ACLMessage msg) {
		Vector v = new Vector(1);
		try {
			ACLMessage initiation = createInitiation();
			if (initiation != null) {
				if (conversations != null) {
					convId = initiation.getConversationId();
					if (convId == null) {
						convId = buildConversationId();
						initiation.setConversationId(convId);
					}
					conversations.registerConversation(convId);
				}
				v.add(initiation);
				Iterator it = initiation.getAllReceiver();
				if (it.hasNext()) {
					target = (AID) it.next();
					defaultTargetDescription = "Agent "+target.getLocalName();
				}
			}
		}
		catch (Exception e) {
			outcome.error("Unexpected error creating initiation message: ", e);
		}
		return v;
	}
	
	@Override
	public void handleRefuse(ACLMessage refuse) {
		// Target agent replied with REFUSE 
		outcome.error("REFUSE response received from "+refuse.getSender().getLocalName()+": "+refuse.getContent(), null);
	}
	
	@Override
	public void handleNotUnderstood(ACLMessage notUnderstood) {
		// Target agent replied with NOT_UNDERSTOOD (Target agent failed decoding initiation message) 
		outcome.error("NOT_UNDERSTOOD response received from "+notUnderstood.getSender().getLocalName()+": "+notUnderstood.getContent(), null);
	}
	
	@Override
	public void handleFailure(ACLMessage failure) {
		if (failure.getSender().equals(myAgent.getAMS()) && !target.equals(myAgent.getAMS())) {
			// Target agent does not exist
			outcome.error(getTargetDescription()+" does not exist or cannot be reached", null);
		}
		else {
			// Target agent replied with FAILURE
			outcome.error("FAILURE response received from "+failure.getSender().getLocalName()+": "+failure.getContent(), null);
		}
	}
	
	@Override
	public void handleAllResultNotifications(Vector notifications) {
		if (notifications.isEmpty()) {
			ACLMessage reply = (ACLMessage) getDataStore().get(REPLY_KEY);
			// Other replies such as REFUSE immediately close the protocol --> We don't get any notification in these cases
			if (reply == null || reply.getPerformative() == ACLMessage.AGREE) {
				handleTimeout();
			}
		}
	}
	
	public void handleTimeout() {
		outcome.error("Timeout expired waiting for response from "+getTargetDescription(), null);
	}
	
	public int onEnd() {
		int ret = super.onEnd();
		if (conversations != null) {
			conversations.deregisterConversation(convId);
		}
		return ret;
	}
	
	private synchronized String buildConversationId() {
		conversationCnt++;
		return myAgent.getLocalName()+"-"+String.valueOf(conversationCnt)+"-"+System.currentTimeMillis();
	}	
}
