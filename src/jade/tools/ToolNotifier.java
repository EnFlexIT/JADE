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
import java.util.Set;
import java.util.Map;
import jade.util.leap.ArrayList;
import java.util.HashSet;

import jade.core.AID;
import jade.core.Agent;
import jade.core.AgentState;

import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SenderBehaviour;
import jade.core.behaviours.SequentialBehaviour;

import jade.core.event.MessageEvent;
import jade.core.event.MessageListener;
import jade.core.event.AgentEvent;
import jade.core.event.AgentListener;

import jade.domain.FIPAException;
import jade.domain.introspection.JADEIntrospectionOntology;
import jade.domain.introspection.Event;
import jade.domain.introspection.DeadAgent;
import jade.domain.introspection.Occurred;
import jade.domain.introspection.EventRecord;
import jade.domain.introspection.SentMessage;
import jade.domain.introspection.PostedMessage;
import jade.domain.introspection.ReceivedMessage;
import jade.domain.introspection.ChangedAgentState;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.StringACLCodec;
import jade.lang.sl.SL0Codec;

import jade.tools.ToolAgent;

public class ToolNotifier extends ToolAgent implements MessageListener, AgentListener {

  private AID observerAgent;
  private Set observedAgents = new HashSet();
  private SequentialBehaviour AMSSubscribe = new SequentialBehaviour();

  // flag used to stop handling events during shutdown
  private boolean closingDown = false;  


  /**
     Inner class NotifierAMSListenerBehaviour
   */
  class NotifierAMSListenerBehaviour extends AMSListenerBehaviour {
  	protected void installHandlers(Map handlersTable) {
    	// Fill the event handler table.
      handlersTable.put(JADEIntrospectionOntology.DEADAGENT, new EventHandler() {
      	public void handle(Event ev) {
	    		DeadAgent da = (DeadAgent)ev;
	    		AID dead = da.getAgent();
	    		removeObservedAgent(dead);
	    		if(isEmpty()) {
	      		// FIXME: should do 'removeMessageListener(this);', but has no container objref for this...
	      		doDelete();
	    		}
	  		}
      });

      handlersTable.put(JADEIntrospectionOntology.MOVEDAGENT, new EventHandler() {
      	public void handle(Event ev) {
	    		// FIXME: Should follow the mobile agent
	  		}
      });

    } // END of installHandlers() method
  	
  } // END of inner class NotifierAMSListenerBehaviour
  
  
  
  public ToolNotifier(AID id) {
    observerAgent = id;
  }


  protected void toolSetup() {

      // Send 'subscribe' message to the AMS
      AMSSubscribe.addSubBehaviour(new SenderBehaviour(this, getSubscribe()));

      // Handle incoming 'inform' messages
      AMSSubscribe.addSubBehaviour(new NotifierAMSListenerBehaviour());
      /*AMSSubscribe.addSubBehaviour(new AMSListenerBehaviour() {

      protected void installHandlers(Map handlersTable) {

        // Fill the event handler table.
        handlersTable.put(JADEIntrospectionOntology.DEADAGENT, new EventHandler() {
          public void handle(Event ev) {
	    DeadAgent da = (DeadAgent)ev;
	    AID dead = da.getAgent();
	    removeObservedAgent(dead);
	    if(isEmpty()) {
	      // FIXME: should do 'removeMessageListener(this);', but has no container objref for this...
	      doDelete();
	    }
	  }
        });

        handlersTable.put(JADEIntrospectionOntology.MOVEDAGENT, new EventHandler() {
          public void handle(Event ev) {
	    // FIXME: Should follow the mobile agent
	  }
        });

      } // End of installHandlers() method

    });
    */

      // Schedule Behaviours for execution
      addBehaviour(AMSSubscribe);

  }


  protected void toolTakeDown() {
    closingDown = true;
    send(getCancel());
  }

  public void addObservedAgent(AID id) {
    observedAgents.add(id);
  }

  public void removeObservedAgent(AID id) {
    observedAgents.remove(id);
  }

  public AID getObserver() {
    return observerAgent;
  }

  public boolean isEmpty() {
    return observedAgents.isEmpty();
  }

  public void sentMessage(MessageEvent ev) {
    if(closingDown)
      return;

    AID id = ev.getAgent();
    if(observedAgents.contains(id)) {
      ACLMessage msg = ev.getMessage();

      jade.domain.introspection.ACLMessage m = new jade.domain.introspection.ACLMessage();
      m.setEnvelope(msg.getEnvelope());
      m.setAclRepresentation(StringACLCodec.NAME);
      m.setPayload(msg.toString());

      SentMessage sm = new SentMessage();
      sm.setSender(id);
      sm.setMessage(m);

      EventRecord er = new EventRecord(sm, here());
      Occurred o = new Occurred();
      o.set_0(er);

      List l = new ArrayList(1);
      l.add(o);

      informObserver(l);

    }
  }

  public void postedMessage(MessageEvent ev) {
    if(closingDown)
      return;  	
    AID id = ev.getAgent();
    if(observedAgents.contains(id)) {
      ACLMessage msg = ev.getMessage();

      jade.domain.introspection.ACLMessage m = new jade.domain.introspection.ACLMessage();
      Object env = msg.getEnvelope();
      if(env != null) {
	m.setEnvelope(msg.getEnvelope());
	m.setAclRepresentation(StringACLCodec.NAME);
      }

      m.setPayload(msg.toString());

      PostedMessage pm = new PostedMessage();
      pm.setReceiver(id);
      pm.setMessage(m);

      EventRecord er = new EventRecord(pm, here());
      Occurred o = new Occurred();
      o.set_0(er);

      List l = new ArrayList(1);
      l.add(o);

      informObserver(l);

    }

  }

  public void receivedMessage(MessageEvent ev) {
    if(closingDown)
      return;  	
    AID id = ev.getAgent();
    if(observedAgents.contains(id)) {
      ACLMessage msg = ev.getMessage();

      jade.domain.introspection.ACLMessage m = new jade.domain.introspection.ACLMessage();
      Object env = msg.getEnvelope();
      if(env != null) {
	m.setEnvelope(msg.getEnvelope());
	m.setAclRepresentation(StringACLCodec.NAME);
      }

      m.setPayload(msg.toString());

      ReceivedMessage rm = new ReceivedMessage();
      rm.setReceiver(id);
      rm.setMessage(m);

      EventRecord er = new EventRecord(rm, here());
      Occurred o = new Occurred();
      o.set_0(er);

      List l = new ArrayList(1);
      l.add(o);

      informObserver(l);

    }
  }

  public void routedMessage(MessageEvent ev) {
    // Do nothing
    if(closingDown)
      return;
  }

  public void changedAgentState(AgentEvent ev) {
    if(closingDown)
      return;

    AID id = ev.getAgent();
    if(observedAgents.contains(id)) {

      AgentState from = ev.getFrom();
      AgentState to = ev.getTo();

      ChangedAgentState cas = new ChangedAgentState();
      cas.setAgent(id);
      cas.setFrom(from);
      cas.setTo(to);

      EventRecord er = new EventRecord(cas, here());
      Occurred o = new Occurred();
      o.set_0(er);

      List l = new ArrayList(1);
      l.add(o);

      informObserver(l);

    }
  }

  public void changedAgentPrincipal(AgentEvent ev) {
    // Do nothing
    if (closingDown)
      return;
  }

  public void addedBehaviour(AgentEvent ev) {
    // Do nothing
    if(closingDown)
      return;
  }

  public void removedBehaviour(AgentEvent ev) {
    // Do nothing
    if(closingDown)
      return;
  }

  public void changedBehaviourState(AgentEvent ev) {
    // Do nothing
    if(closingDown)
      return;
  }

  /*
   * Creates the message to be sent to the observer. The observed
   * message is put in the content field of this message.
   *
   * @param theMsg handler of the observed message
   * @param theDests list of the destination (observers)
   */
  private void informObserver(List content) {

    final ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
    msg.clearAllReceiver();
    msg.addReceiver(observerAgent);
    msg.setConversationId(observerAgent.getName() + "-event");
    msg.setOntology(JADEIntrospectionOntology.NAME);
    msg.setLanguage(SL0Codec.NAME);
    final List l = content;
    addBehaviour(new OneShotBehaviour(this) {
      public void action() {
	try {
	  myAgent.fillMsgContent(msg, l);
	  send(msg);
	}
	catch(FIPAException fe) {
	  fe.printStackTrace();
	}

      }

    });

  }


}
