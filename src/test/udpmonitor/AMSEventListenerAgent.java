package test.udpmonitor;

import java.util.Map;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.introspection.AMSSubscriber;
import jade.domain.introspection.Event;
import jade.domain.introspection.IntrospectionOntology;
import jade.lang.acl.ACLMessage;

/**
 * This agent subscribes at the AMS for container events and
 * forwards fired events to an <code>EventReceiverAgent</code>
 * 
 * Expected arguments:
 * 0 ... GUID to create an AID (exammple: agent@platform)
 * 1 ... address where the agent is reachable
 * 
 * @see test.udpmonitor.EventReceiverAgent
 * @author Roland Mungenast - Profactor
 */
public class AMSEventListenerAgent extends Agent {

  private Object lock = new Object();
  private String newEvent;
  private AID receiverAID;
  
  protected void setup() {
    
    // get AID for the receiver agent
    Object[] args = getArguments();
    receiverAID = new AID((String)args[0], AID.ISGUID);
    receiverAID.addAddresses((String)args[1]);
    
    addBehaviour(new AMSSubscriber() {

      class Handler implements EventHandler {

        public void handle(Event ev) {
          // save type of fired event and initiate its forwarding
          newEvent = ev.getName();
          synchronized (lock) {
            lock.notify();
          }
        }
      }
      
      protected void installHandlers(Map handlersTable) {
        // register handler for container eventss
        Handler h = new Handler();
        handlersTable.put(IntrospectionOntology.ADDEDCONTAINER, h);
        handlersTable.put(IntrospectionOntology.REMOVEDCONTAINER, h);        
      }
      
    });
    
    addBehaviour(new CyclicBehaviour() {

      public void action() {
        
        // forward new event
        if (newEvent != null) {
          ACLMessage msgOut = new ACLMessage(ACLMessage.INFORM);
          msgOut.addReceiver(receiverAID);
          msgOut.setContent(newEvent);
          send(msgOut);
          newEvent = null;
          }
        }
    });
  }
}

