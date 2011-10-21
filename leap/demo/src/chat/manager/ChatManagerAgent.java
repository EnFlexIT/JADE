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

package chat.manager;

//#J2ME_EXCLUDE_FILE
//#MIDP_EXCLUDE_FILE
//#PJAVA_EXCLUDE_FILE
//#ANDROID_EXCLUDE_FILE

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import jade.content.ContentManager;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.content.onto.BasicOntology;
import jade.content.abs.*;

import jade.proto.SubscriptionResponder;
import jade.proto.SubscriptionResponder.SubscriptionManager;
import jade.proto.SubscriptionResponder.Subscription;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.FailureException;

import jade.domain.introspection.IntrospectionOntology;
import jade.domain.introspection.Event;
import jade.domain.introspection.DeadAgent;
import jade.domain.introspection.MovedAgent;
import jade.domain.introspection.AMSSubscriber;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

import chat.ontology.*;

/**
   This agent maintains knowledge of agents currently attending the 
   chat and inform them when someone joins/leaves the chat.
   @author Giovanni Caire - TILAB
 */
public class ChatManagerAgent extends Agent implements SubscriptionManager {
	private Map participants = new HashMap();
	private Codec codec = new SLCodec();
	private Ontology onto = ChatOntology.getInstance();
	private AMSSubscriber myAMSSubscriber;
	
  protected void setup() {
  	// Prepare to accept subscriptions from chat participants
		getContentManager().registerLanguage(codec);
		getContentManager().registerOntology(onto);
		
	  MessageTemplate sTemplate = MessageTemplate.and(
	  	MessageTemplate.MatchPerformative(ACLMessage.SUBSCRIBE),
	  	MessageTemplate.and(
	  		MessageTemplate.MatchLanguage(codec.getName()),
	  		MessageTemplate.MatchOntology(onto.getName()) ) );
	  addBehaviour(new SubscriptionResponder(this, sTemplate, this));

	  // Register to the AMS to detect when chat participants suddenly die
	  myAMSSubscriber = new AMSSubscriber() {
	  	protected void installHandlers(Map handlersTable) {
	    	// Fill the event handler table. We are only interested in the
	  		// DEADAGENT event
	      handlersTable.put(IntrospectionOntology.DEADAGENT, new EventHandler() {
	      	public void handle(Event ev) {
		    		DeadAgent da = (DeadAgent)ev;
		    		AID id = da.getAgent();
	    			// If the agent was attending the chat --> notify all
		    		// other participants that it has just left.
		    		if (participants.containsKey(id)) {
		    			try {
			    			deregister((Subscription) participants.get(id));
		    			}
		    			catch (Exception e) {
		    				//Should never happen
		    				e.printStackTrace();
		    			}
		    		}
	      	}
	      });
	    }
	  };
	  addBehaviour(myAMSSubscriber);
  }

  protected void takeDown() {
  	// Unsubscribe from the AMS
    send(myAMSSubscriber.getCancel());
    //FIXME: should inform current participants if any
  }
  
  ///////////////////////////////////////////////
  // SubscriptionManager interface implementation
  ///////////////////////////////////////////////
	public boolean register(Subscription s) throws RefuseException, NotUnderstoodException { 
		try {
			AID newId = s.getMessage().getSender();
  		// Notify the new participant about the others (if any) and VV
  		if (!participants.isEmpty()) {
  			// The message for the new participant
	  		ACLMessage notif1 = s.getMessage().createReply();
	  		notif1.setPerformative(ACLMessage.INFORM);
	  		
	  		// The message for the old participants
		  	ACLMessage notif2 = (ACLMessage) notif1.clone();
		  	notif2.clearAllReceiver();
		  	AbsPredicate p = new AbsPredicate(ChatOntology.JOINED);
		  	AbsAggregate agg = new AbsAggregate(BasicOntology.SEQUENCE);
		  	agg.add((AbsTerm) BasicOntology.getInstance().fromObject(newId));
		  	p.set(ChatOntology.JOINED_WHO, agg);
		  	getContentManager().fillContent(notif2, p);
		  	agg.clear();
		  	
	  		Iterator it = participants.keySet().iterator();
	  		while (it.hasNext()) {
	  			AID oldId = (AID) it.next();
	  			agg.add((AbsTerm) BasicOntology.getInstance().fromObject(oldId));
	  			
	  			Subscription oldS = (Subscription) participants.get(oldId);
	  			oldS.notify(notif2);
	  		}
	  		
		  	getContentManager().fillContent(notif1, p);
	  		s.notify(notif1);
  		}
  		// Add the new subscription
  		participants.put(newId, s);
  		return false;
	  }
	  catch (Exception e) {
	  	e.printStackTrace();
	  	throw new RefuseException("Subscription error");
	  }		
	}
	
	public boolean deregister(Subscription s) throws FailureException {
		AID oldId = s.getMessage().getSender();
		// Remove the subscription
		if (participants.remove(oldId) != null) {
			// Notify other participants if any
			if (!participants.isEmpty()) {
				try {
		  		ACLMessage notif = s.getMessage().createReply();
		  		notif.setPerformative(ACLMessage.INFORM);
			  	notif.clearAllReceiver();
			  	AbsPredicate p = new AbsPredicate(ChatOntology.LEFT);
			  	AbsAggregate agg = new AbsAggregate(BasicOntology.SEQUENCE);
			  	agg.add((AbsTerm) BasicOntology.getInstance().fromObject(oldId));
			  	p.set(ChatOntology.LEFT_WHO, agg);
			  	getContentManager().fillContent(notif, p);
		
			  	Iterator it = participants.values().iterator();
		  		while (it.hasNext()) {
		  			Subscription s1 = (Subscription) it.next();
		  			s1.notify(notif);
		  		}
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return false;
	}
}
