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

package jade.tools;

import jade.util.leap.List;
import java.util.Map;
import java.util.TreeMap;

import jade.core.Agent;
import jade.core.behaviours.*;

import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.FIPAAgentManagementOntology;
import jade.domain.JADEAgentManagement.JADEAgentManagementOntology;
import jade.domain.introspection.JADEIntrospectionOntology;
import jade.domain.introspection.Event;
import jade.domain.introspection.EventRecord;
import jade.domain.introspection.Occurred;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.sl.SL0Codec;

import jade.proto.FipaRequestInitiatorBehaviour;


/**

  This abstract class is the common ancestor of all JADE tools (RMA,
  Sniffer, Introspector, etc.). It provides suitable behaviours to
  interact with the AMS, registering for interesting events and
  requesting actions when needed.

  @author Giovanni Rimassa - Universita` di Parma
  @version $Date$ $Revision$

 */
public abstract class ToolAgent extends Agent {

  private ACLMessage AMSSubscription = new ACLMessage(ACLMessage.SUBSCRIBE);
  private ACLMessage AMSCancellation = new ACLMessage(ACLMessage.CANCEL);
  private ACLMessage AMSRequest = new ACLMessage(ACLMessage.REQUEST);

  private SequentialBehaviour AMSSubscribe = new SequentialBehaviour();


  // Used by AMSListenerBehaviour
  // FIXME. This interface should have been declared protected. However JDK1.2.2 complains and
  // requires it to be declared public.
  public static interface EventHandler {
    void handle(Event ev);
  }


  // Receives notifications by AMS
  protected abstract class AMSListenerBehaviour extends CyclicBehaviour {

    private MessageTemplate listenTemplate;

    // Ignore case for event names
    private Map handlers = new TreeMap(String.CASE_INSENSITIVE_ORDER);

    protected AMSListenerBehaviour() {

      MessageTemplate mt1 = MessageTemplate.MatchLanguage(SL0Codec.NAME);
      MessageTemplate mt2 = MessageTemplate.MatchOntology(JADEIntrospectionOntology.NAME);
      MessageTemplate mt12 = MessageTemplate.and(mt1, mt2);

      mt1 = MessageTemplate.MatchInReplyTo("tool-subscription");
      mt2 = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
      listenTemplate = MessageTemplate.and(mt1, mt2);
      listenTemplate = MessageTemplate.and(listenTemplate, mt12);

      // Fill the event handler table, using a deferred operation.
      installHandlers(handlers);

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

    public void action() {
      ACLMessage current = receive(listenTemplate);
      if(current != null) {
	// Handle 'inform' messages from the AMS
  try {
	  List l = extractContent(current);
	  Occurred o = (Occurred)l.get(0);
	  EventRecord er = o.get_0();
	  Event ev = er.getWhat();
	  String eventName = ev.getName();
	  EventHandler h = (EventHandler)handlers.get(eventName);
	  if(h != null)
	    h.handle(ev);
	}
	catch(FIPAException fe) {
	  fe.printStackTrace();
	}
	catch(ClassCastException cce) {
	  cce.printStackTrace();
	}
      }
      else
	block();
    }

  } // End of AMSListenerBehaviour class


  /**
     This method is invoked just after the generic agent
     setup. Subclasses must use this method the same way ordinary
     agents use their <code>setup()</code> method.
   */
  protected void toolSetup() {

  }

  /**
     This method is invoked just before the generic agent
     takedown. Subclasses must use this method the same way ordinary
     agents use their <code>takeDown()</code> method.
   */
  protected void toolTakeDown() {

  }


  protected ACLMessage getSubscribe() {
    return AMSSubscription;
  }

  protected ACLMessage getCancel() {
    return AMSCancellation;
  }

  protected ACLMessage getRequest() {
    return AMSRequest;
  }

  public final void setup() {

    // Register the supported ontologies 
    registerOntology(FIPAAgentManagementOntology.NAME, FIPAAgentManagementOntology.instance());
    registerOntology(JADEAgentManagementOntology.NAME, JADEAgentManagementOntology.instance());
    registerOntology(JADEIntrospectionOntology.NAME, JADEIntrospectionOntology.instance());

    // register the supported languages
    registerLanguage(SL0Codec.NAME, new SL0Codec());

    // Fill ACL messages fields

    AMSSubscription.setSender(getAID());
    AMSSubscription.clearAllReceiver();
    AMSSubscription.addReceiver(getAMS());
    AMSSubscription.setLanguage(SL0Codec.NAME);
    AMSSubscription.setOntology(JADEIntrospectionOntology.NAME);
    AMSSubscription.setReplyWith("tool-subscription");
    AMSSubscription.setConversationId(getLocalName());

    String content = "platform-events";
    AMSSubscription.setContent(content);

    AMSCancellation.setSender(getAID());
    AMSCancellation.clearAllReceiver();
    AMSCancellation.addReceiver(getAMS());
    AMSCancellation.setLanguage(SL0Codec.NAME);
    AMSCancellation.setOntology(JADEIntrospectionOntology.NAME);
    AMSCancellation.setReplyWith("tool-cancellation");
    AMSCancellation.setConversationId(getLocalName());
    // No content is needed (cfr. FIPA 97 Part 2 page 26)

    AMSRequest.setSender(getAID());
    AMSRequest.clearAllReceiver();
    AMSRequest.addReceiver(getAMS());
    AMSRequest.setProtocol("fipa-request");
    AMSRequest.setLanguage(SL0Codec.NAME);

    // Call tool-specific setup
    toolSetup();

  }

  protected final void takeDown() {
    // Call tool-specific takedown
    toolTakeDown();
  }

}
