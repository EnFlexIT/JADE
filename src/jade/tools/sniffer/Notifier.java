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

package jade.tools.sniffer;

import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashSet;

import jade.core.AID;

import jade.core.Agent;

import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SenderBehaviour;
import jade.core.behaviours.SequentialBehaviour;

import jade.core.event.MessageEvent;
import jade.core.event.MessageListener;

import jade.domain.FIPAException;
import jade.domain.introspection.JADEIntrospectionOntology;
import jade.domain.introspection.Event;
import jade.domain.introspection.DeadAgent;
import jade.domain.introspection.Occurred;
import jade.domain.introspection.EventRecord;
import jade.domain.introspection.SentMessage;
import jade.domain.introspection.PostedMessage;


import jade.lang.acl.ACLMessage;
import jade.lang.acl.StringACLCodec;
import jade.lang.sl.SL0Codec;

import jade.tools.ToolAgent;


public class Notifier extends ToolAgent implements MessageListener {

  private AID snifferAgent;
  private Set sniffedAgents = new HashSet();
  private SequentialBehaviour AMSSubscribe = new SequentialBehaviour();

  public Notifier(AID id) {
    snifferAgent = id;
  }


  protected void toolSetup() {

    // Send 'subscribe' message to the AMS
    AMSSubscribe.addSubBehaviour(new SenderBehaviour(this, getSubscribe()));

    // Handle incoming 'inform' messages
    AMSSubscribe.addSubBehaviour(new AMSListenerBehaviour() {

      protected void installHandlers(Map handlersTable) {

        // Fill the event handler table.
        handlersTable.put(JADEIntrospectionOntology.DEADAGENT, new EventHandler() {
          public void handle(Event ev) {
	    DeadAgent da = (DeadAgent)ev;
	    AID dead = da.getAgent();
	    removeSniffedAgent(dead);
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

    // Schedule Behaviours for execution
    addBehaviour(AMSSubscribe);

  }


  protected void toolTakeDown() {
    send(getCancel());
  }

  public void addSniffedAgent(AID id) {
    sniffedAgents.add(id);
  }

  public void removeSniffedAgent(AID id) {
    sniffedAgents.remove(id);
  }

  public AID getSniffer() {
    return snifferAgent;
  }

  public boolean isEmpty() {
    return sniffedAgents.isEmpty();
  }

  public void sentMessage(MessageEvent ev) {
    AID id = ev.getAgent();
    if(sniffedAgents.contains(id)) {
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

      informSniffer(l);

    }
  }

  public void postedMessage(MessageEvent ev) {
    AID id = ev.getAgent();
    if(sniffedAgents.contains(id)) {
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

      informSniffer(l);

    }
  }

  public void receivedMessage(MessageEvent ev) {
    // Do nothing
  }

  public void routedMessage(MessageEvent ev) {
    // Do nothing
  }

  /*
   * Creates the message to be sent to the sniffer. The ontology must be set to 
   * "sniffed-message" otherwise the sniffer doesn't recognize it. The sniffed 
   * message is put in the content field of this message.
   *
   * @param theMsg handler of the sniffed message
   * @param theDests list of the destination (sniffers)
   */
  private void informSniffer(List content) {

    final ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
    msg.clearAllReceiver();
    msg.addReceiver(snifferAgent);
    msg.setConversationId(snifferAgent.getName() + "-event");
    msg.setOntology(JADEIntrospectionOntology.NAME);
    msg.setLanguage(SL0Codec.NAME);
    final List l = content;
    addBehaviour(new OneShotBehaviour(this) {
      public void action() {
	try {
	  myAgent.fillContent(msg, l);
	  send(msg);
	}
	catch(FIPAException fe) {
	  fe.printStackTrace();
	}

      }

    });

  }


}
