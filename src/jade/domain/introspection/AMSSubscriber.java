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

package jade.domain.introspection;

import java.util.Map;
import java.util.TreeMap;

import jade.core.Agent;
import jade.core.behaviours.*;

import jade.domain.FIPAException;
import jade.domain.FIPANames;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import jade.content.lang.sl.SLCodec;
import jade.util.leap.Serializable;

/**
   This behaviour subscribes to the AMS to receive notifications
   about platform-wide events. The <code>installHandlers</code> 
   method must be redefined to define the handlers for events the
   agent executing this behaviour is interested in.
   @author Giovanni Caire - TILAB
   @author Giovanni Rimassa - Universita' di Parma
 */
public abstract class AMSSubscriber extends CyclicBehaviour {
	// FIXME: Change the values of these constants
	public static final String AMS_SUBSCRIPTION = "tool-subscription";
	public static final String AMS_CANCELLATION = "tool-cancellation";
	public static final String PLATFORM_EVENTS = "platform-events";
	
  private ACLMessage AMSSubscription = new ACLMessage(ACLMessage.SUBSCRIBE);
  private ACLMessage AMSCancellation = new ACLMessage(ACLMessage.CANCEL);

  private MessageTemplate listenTemplate;

  // Ignore case for event names
  private Map handlers = new TreeMap(String.CASE_INSENSITIVE_ORDER);

  /**
     This interface must be implemented by concrete event handlers
     installed by this AMSSubscriber.
   */
  public static interface EventHandler extends Serializable {
    void handle(Event ev);
  }


  /**
	   Default constructor. 
	 */
  public AMSSubscriber() {
   	super();
   	
		// Prepare the template to receive AMS notification
    MessageTemplate mt1 = MessageTemplate.MatchLanguage(FIPANames.ContentLanguage.FIPA_SL0);
    MessageTemplate mt2 = MessageTemplate.MatchOntology(IntrospectionOntology.NAME);
    MessageTemplate mt12 = MessageTemplate.and(mt1, mt2);

    mt1 = MessageTemplate.MatchInReplyTo(AMS_SUBSCRIPTION);
    mt2 = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
    listenTemplate = MessageTemplate.and(mt1, mt2);
    listenTemplate = MessageTemplate.and(listenTemplate, mt12);

    // Fill the event handler table, using a deferred operation.
    installHandlers(handlers);
  }
   
  public void onStart() {
    // Register the Introspection ontology 
    myAgent.getContentManager().registerOntology(IntrospectionOntology.getInstance());

    // Register the SL0 language
    myAgent.getContentManager().registerLanguage(new SLCodec(), FIPANames.ContentLanguage.FIPA_SL0);

    // Fill the subscription message
    AMSSubscription.addReceiver(myAgent.getAMS());
    AMSSubscription.setLanguage(FIPANames.ContentLanguage.FIPA_SL0);
    AMSSubscription.setOntology(IntrospectionOntology.NAME);
    AMSSubscription.setReplyWith(AMS_SUBSCRIPTION);
    AMSSubscription.setConversationId(myAgent.getLocalName());
    AMSSubscription.setContent(PLATFORM_EVENTS);

    // Fill the cancellation message
    AMSCancellation.addReceiver(myAgent.getAMS());
    AMSCancellation.setLanguage(FIPANames.ContentLanguage.FIPA_SL0);
    AMSCancellation.setOntology(IntrospectionOntology.NAME);
    AMSCancellation.setReplyWith(AMS_CANCELLATION);
    AMSCancellation.setConversationId(myAgent.getLocalName());
    // No content is needed (cfr. FIPA 97 Part 2 page 26)
    
    // Subscribe to the AMS
    myAgent.send(AMSSubscription);
  }

  /**
     This method has to be implemented by concrete subclasses,
     filling the <code>Map</code> passed as parameter with
     implementations of the <code>EventHandler</code> interface,
     using the name of the event as key (see the <code>Event</code>
     interface.
     @param handlersTable The table that associates each event name
     with a proper handler.
   */
  protected abstract void installHandlers(Map handlersTable);

  public final void action() {
    ACLMessage current = myAgent.receive(listenTemplate);
    if(current != null) {
			// Handle 'inform' messages from the AMS
		  try {
			  Occurred o = (Occurred) myAgent.getContentManager().extractContent(current);
			  EventRecord er = (EventRecord)o.getWhat();
			  Event ev = er.getWhat();
			  String eventName = ev.getName();
			  EventHandler h = (EventHandler)handlers.get(eventName);
			  if(h != null) {
			    h.handle(ev);
			  }
			}
      catch(ClassCastException cce) {
        cce.printStackTrace();
      }
			catch(Exception fe) {
			  fe.printStackTrace();
			}
    }
    else {
			block();
    }
  }
  
  /**
     Retrieve the <code>subscribe</code> ACL message used to subscribe 
     to the AMS.
     This message is automatically sent when this behaviour is added to an
     Agent.
     @return The subscription ACL message.
   */
  public final ACLMessage getSubscribe() {
    return AMSSubscription;
  }

  /**
     Retrieve the <code>cancel</code> ACL message 
     used to cancel the subscription to the AMS.
     Since this behaviour is cyclic (never ends) it is the responsibility
     of the agent executing this behaviour to send the <code>cancel</code>
     message to the AMS when notifications are no longer required.
     @return The cancellation ACL message.
   */
  public final ACLMessage getCancel() {
    return AMSCancellation;
  }
}

