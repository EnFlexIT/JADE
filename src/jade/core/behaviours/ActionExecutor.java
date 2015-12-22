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

import jade.content.AgentAction;
import jade.content.ContentElement;
import jade.content.ContentException;
import jade.content.onto.Ontology;
import jade.content.onto.basic.Action;
import jade.content.onto.basic.Result;
import jade.core.AID;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;

import java.util.Date;

/**
 * Base class for behaviours intended to request the execution of an action to a given actor and
 * get back the result (if any)
 *
 * @param <ActionT> The class of the action to be executed
 * @param <ResultT> The class of the result or Void if the action is not expected to return any result
 */
public class ActionExecutor<ActionT extends AgentAction, ResultT> extends BaseInitiator {
	private static final long serialVersionUID = 54354676089783L;
	
	protected ActionT action;
	protected ResultT result;
	
	protected AID actor;
	protected String language = FIPANames.ContentLanguage.FIPA_SL;
	protected Ontology ontology;
	protected long timeout = 30000; // Default 30 sec
	protected String conversationId = null;
		
	public ActionExecutor(ActionT action, Ontology ontology, AID actor) {
		this.action = action;
		this.ontology = ontology;
		this.actor = actor;
	}
	
	public void setAction(ActionT action) {
		this.action = action;
	}
	public void setActor(AID actor) {
		this.actor = actor;
	}
	public void setOntology(Ontology ontology) {
		this.ontology = ontology;
	}
	public void setLanguage(String language) {
		this.language = language;
	}
	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}
	public void setConversationId(String conversationId) {
		this.conversationId = conversationId;
	}
	
	public ResultT getResult() {
		return result;
	}
	public AID getActualActor() {
		AID actualActor = null;
		ACLMessage reply = (ACLMessage) getDataStore().get(REPLY_K);
		if (reply != null) {
			if (reply.getPerformative() != ACLMessage.FAILURE && !reply.getSender().getLocalName().equalsIgnoreCase("AMS")) {
				actualActor = reply.getSender();
			}
		}
		return actualActor;
	}
	
	protected AID retrieveActor() throws FIPAException {
		throw new FIPAException("Cannot retrieve actor for action "+action);
	}
	
	@Override
	protected ACLMessage createInitiation() {		
		checkLanguage(language);
		checkOntology(ontology);
		
		try {
			if (actor == null) {
				actor = retrieveActor();
				if (actor == null) {
					outcome.error("Actor for action "+action.getClass().getSimpleName()+" not found", null);
					return null;
				}
			}
			Action actExpr = new Action(actor, action);
			ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
			request.addReceiver(actor);
			request.setLanguage(language);
			request.setOntology(ontology.getName());
			request.setConversationId(conversationId);
			if (timeout > 0) {
				request.setReplyByDate(new Date(System.currentTimeMillis() + timeout));
			}
			myAgent.getContentManager().fillContent(request, actExpr);
			return request;
		}
		catch (ContentException ce) {
			outcome.error("Error encoding "+action.getClass().getSimpleName()+" request", ce);
		}
		catch (FIPAException fe) {
			outcome.error("Error searching for actor for action "+action.getClass().getSimpleName(), fe);
		}
		catch (Exception e) {
			outcome.error("Unexpected error", e);
		}
		return null;
	}

	@Override
	public void handleInform(ACLMessage inform) {
		try {
			ContentElement el = myAgent.getContentManager().extractContent(inform);
			if (el instanceof Result) {
				Result r = (Result) el;
				result = extractResult(r);
			}
		}
		catch (Exception e) {
			outcome.error("Error decoding response", e);
		}
	}
	
	protected ResultT extractResult(Result r) {
		return (ResultT) r.getValue();
	}
}
