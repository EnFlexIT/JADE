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

package jade.core;

//#J2ME_EXCLUDE_FILE
import jade.util.SynchList;
import jade.util.leap.Iterator;
import jade.util.leap.List;
import jade.util.leap.LinkedList;

import jade.core.behaviours.Behaviour;

import jade.core.event.MessageEvent;
import jade.core.event.MessageListener;
import jade.core.event.AgentEvent;
import jade.core.event.AgentListener;
import jade.core.event.PlatformEvent;
import jade.core.event.PlatformListener;
import jade.core.event.MTPEvent;
import jade.core.event.MTPListener;

import jade.lang.acl.ACLMessage;

import jade.tools.ToolNotifier; // FIXME: This should not be imported

//__SECURITY__BEGIN
import jade.security.AgentPrincipal;
//__SECURITY__END

import java.util.Hashtable;

/** 
   Full implementation of the <code>NotificationManager</code> 
   interface
   @see NotificationManager;
   @author Giovanni Caire - TILAB
 */
class RealNotificationManager implements NotificationManager {

	private final static String AMS_DEBUG_HELPER = "AMS-debug-helper";
	
  private AgentContainerImpl myContainer;
  private LADT localAgents;
  
  private SynchList messageListeners = new SynchList();
  private SynchList agentListeners = new SynchList();

  // This maps a debugged agent into the list of debuggers that are 
  // currently debugging it. It is used to know when an agent is no longer
  // debugged by any debugger.
  private Hashtable debuggers = new Hashtable();
  
  /** 
     Constructor
   */
  public RealNotificationManager() {
  }
  
  public void initialize(AgentContainerImpl ac, LADT ladt) {
  	myContainer = ac;
  	localAgents = ladt;
  }
  
  // ACTIVATION/DEACTIVATION METHODS
  public void enableSniffer(AID snifferName, AID toBeSniffed) {

    ToolNotifier tn = findNotifier(snifferName);
    if(tn == null) { // Need a new notifier 
      tn = new ToolNotifier(snifferName);
      AID id = new AID(snifferName.getLocalName() + "-on-" + myID().getName(), AID.ISLOCALNAME);
      try {
	      myContainer.initAgent(id, tn, AgentContainerImpl.CREATE_AND_START);
  	    addMessageListener(tn);
      }
      catch (Exception e) {
      	e.printStackTrace();
      }
    }
    tn.addObservedAgent(toBeSniffed);
  }
  
  public void disableSniffer(AID snifferName, AID notToBeSniffed) {
    ToolNotifier tn = findNotifier(snifferName);
    if(tn != null) { 
      tn.removeObservedAgent(notToBeSniffed);
      if(tn.isEmpty()) {
				removeMessageListener(tn);
				tn.doDelete();
      }
    }
  }

  public void enableDebugger(AID debuggerName, AID toBeDebugged) {
  	// AMS debug enabling must be done by a separated Thread to avoid
  	// deadlock with ToolNotifier startup
  	if (toBeDebugged.equals(myContainer.getAMS()) && !(Thread.currentThread().getName().equals(AMS_DEBUG_HELPER))) {
			final AID dn = debuggerName;
			final AID tbd = toBeDebugged;
  		Thread helper = new Thread(new Runnable() {
  			public void run() {
  				enableDebugger(dn, tbd);
  			}
  		} );
  		helper.setName(AMS_DEBUG_HELPER);
  		helper.start();
  		return;
  	}
  	
  	// Get the ToolNotifier for the indicated debugger (or create a new one
  	// if not yet there)
    ToolNotifier tn = findNotifier(debuggerName);
    if(tn == null) { // Need a new notifier
    	tn = new ToolNotifier(debuggerName);
      AID id = new AID(debuggerName.getLocalName() + "-on-" + myID().getName(), AID.ISLOCALNAME);
      try {
	      myContainer.initAgent(id, tn, AgentContainerImpl.CREATE_AND_START);
	      if (toBeDebugged.equals(myContainer.getAMS())) {
	      	// If we are debugging the AMS, let's wait for the ToolNotifier 
	      	// be ready to avoid deadlock problems. Note also that in 
	      	// this case this code is executed by the helper thread and not
	      	// by the AMS thread
	      	tn.waitUntilStarted();
	      }
  	    addMessageListener(tn);
    	  addAgentListener(tn);
      }
      catch (Exception e) {
      	e.printStackTrace();
      }
    }
    tn.addObservedAgent(toBeDebugged);
    
    // Update the map of debuggers currently debugging the toBeDebugged agent
    synchronized (debuggers) {
	    List l = (List) debuggers.get(toBeDebugged);
  	  if (l == null) {
    		l = new LinkedList();
    		debuggers.put(toBeDebugged, l);
    	}
    	if (!l.contains(debuggerName)) {
    		l.add(debuggerName);
    	}
    }

    // Activate behaviour-related events generation on the toBeDebugged agent
    Agent a = localAgents.acquire(toBeDebugged);
    AgentState as = a.getAgentState();
    Scheduler s = a.getScheduler();
    MessageQueue mq = a.getMessageQueue();
    a.setGenerateBehaviourEvents(true);
    localAgents.release(toBeDebugged);
    
    // Notify current agent state
    fireChangedAgentState(toBeDebugged, as, as);
    
    // Notify currently loaded behaviour 
    // (Mutual exclusion with Scheduler.add(), remove()...)
    synchronized (s) {
    	Iterator it = s.readyBehaviours.iterator();
    	while (it.hasNext()) {
    		Behaviour b = (Behaviour) it.next();
    		// We can't just call fireAddedBehaviour() as we must only notify the 
    		// ToolNotifier associated with debuggerName (NOT all AgentListeners)
				AgentEvent ev = new AgentEvent(AgentEvent.ADDED_BEHAVIOUR, toBeDebugged, new BehaviourID(b), myID());
    		tn.addedBehaviour(ev);
    	}
    	
    	it = s.blockedBehaviours.iterator();
    	while (it.hasNext()) {
    		Behaviour b = (Behaviour) it.next();
    		BehaviourID bid = new BehaviourID(b);
				AgentEvent ev = new AgentEvent(AgentEvent.ADDED_BEHAVIOUR, toBeDebugged, bid, myID());
    		tn.addedBehaviour(ev);
				ev = new AgentEvent(AgentEvent.CHANGED_BEHAVIOUR_STATE, toBeDebugged, bid, Behaviour.STATE_READY, Behaviour.STATE_BLOCKED, myID());
    		tn.changedBehaviourState(ev);
    	}
    }
    
    // Notify messages currently pending in the message queue
    // (Mutual exclusion with Agent.receive(), blockingReceive(), postMessage()...)
    synchronized (mq) {
    	Iterator it = mq.iterator();
    	while (it.hasNext()) {
    		ACLMessage msg = (ACLMessage) it.next();
    		// We can't just call firePostedMessage() as we must only notify the 
    		// ToolNotifier associated with debuggerName (NOT all AgentListeners)
				MessageEvent ev = new MessageEvent(MessageEvent.POSTED_MESSAGE, msg, toBeDebugged, myID());
    		tn.postedMessage(ev);
    	}
    }
  }

  public void disableDebugger(AID debuggerName, AID notToBeDebugged) {
  	ToolNotifier tn = findNotifier(debuggerName);
    if(tn != null) { 
      tn.removeObservedAgent(notToBeDebugged);
      if(tn.isEmpty()) {
				removeMessageListener(tn);
				removeAgentListener(tn);
				tn.doDelete();
      }
    }
    
    boolean resetGenerateBehaviourEvents = true;
    synchronized (debuggers) {
	    List l = (List) debuggers.get(notToBeDebugged);
  	  if (l != null) {
    		l.remove(debuggerName);
    		if (l.size() > 0) {
    			// There is still at least 1 debugger debugging the agent 
    			// Do not stop generation of behaviour events
    			resetGenerateBehaviourEvents = false;
    		}
    		else {
    			debuggers.remove(notToBeDebugged);
    		}
    	}
    }
    
    if (resetGenerateBehaviourEvents) {
    	Agent a = localAgents.acquire(notToBeDebugged);
    	if (a != null) {
    		a.setGenerateBehaviourEvents(false);
    	}
    	localAgents.release(notToBeDebugged);
    }
  }

  
  // NOTIFICATION METHODS
  public void fireEvent(int eventType, Object[] param) {
  	try {
  		switch (eventType) {
  		case SENT_MESSAGE:
  			fireSentMessage((ACLMessage) param[0], (AID) param[1]);
  			break;
	  	case POSTED_MESSAGE:
  			firePostedMessage((ACLMessage) param[0], (AID) param[1]);
  			break;
	  	case RECEIVED_MESSAGE:
  			fireReceivedMessage((ACLMessage) param[0], (AID) param[1]);
  			break;
  		case ROUTED_MESSAGE:
  			fireRoutedMessage((ACLMessage) param[0], (Channel) param[1], (Channel) param[2]);
  			break;
  		case CHANGED_AGENT_STATE:
  			fireChangedAgentState((AID) param[0], (AgentState) param[1], (AgentState) param[2]);
  			break;
      case ADDED_BEHAVIOUR:
        fireAddedBehaviour((AID) param[0], (Behaviour) param[1]);
        break;
     	case REMOVED_BEHAVIOUR:
        fireRemovedBehaviour((AID) param[0], (Behaviour) param[1]);
        break;
      case CHANGED_BEHAVIOUR_STATE:
        fireChangedBehaviourState((AID)param[0], (Behaviour)param[1], (String)param[2], (String)param[3]);
        break;
//__SECURITY__BEGIN
  		case CHANGED_AGENT_PRINCIPAL:
  			fireChangedAgentPrincipal((AID) param[0], (AgentPrincipal) param[1], (AgentPrincipal) param[2]);
  			break;
//__SECURITY__END
  		}
  	}
  	catch (ClassCastException cce) {
  		cce.printStackTrace();
  	}
  	catch (IndexOutOfBoundsException ioobe) {
  		ioobe.printStackTrace();
  	}
  }
  		
  // PRIVATE MANAGEMENT METHODS
  private void fireSentMessage(ACLMessage msg, AID sender) {
  	// NOTE: A normal synchronized block could create deadlock problems
  	// as it prevents concurrent scannings of the listeners list.
    List l = messageListeners.startScanning();
    if (l != null) {
			MessageEvent ev = new MessageEvent(MessageEvent.SENT_MESSAGE, msg, sender, myID());
    	Iterator it = l.iterator();
    	while (it.hasNext()) {
	  		MessageListener ml = (MessageListener) it.next();
	  		ml.sentMessage(ev);
    	}
    	messageListeners.stopScanning();
    }	
  }

  private void firePostedMessage(ACLMessage msg, AID receiver) {
  	// NOTE: A normal synchronized block could create deadlock problems
  	// as it prevents concurrent scannings of the listeners list.
    List l = messageListeners.startScanning();
    if (l != null) {
			MessageEvent ev = new MessageEvent(MessageEvent.POSTED_MESSAGE, msg, receiver, myID());
    	Iterator it = l.iterator();
    	while (it.hasNext()) {
	  		MessageListener ml = (MessageListener) it.next();
	  		ml.postedMessage(ev);
    	}
    	messageListeners.stopScanning();
    }	
  }

  private void fireReceivedMessage(ACLMessage msg, AID receiver) {
  	// NOTE: A normal synchronized block could create deadlock problems
  	// as it prevents concurrent scannings of the listeners list.
    List l = messageListeners.startScanning();
    if (l != null) {
			MessageEvent ev = new MessageEvent(MessageEvent.RECEIVED_MESSAGE, msg, receiver, myID());
    	Iterator it = l.iterator();
    	while (it.hasNext()) {
	  		MessageListener ml = (MessageListener) it.next();
	  		ml.receivedMessage(ev);
    	}
    	messageListeners.stopScanning();
    }	
  }

  private void fireRoutedMessage(ACLMessage msg, Channel from, Channel to) {
  	// NOTE: A normal synchronized block could create deadlock problems
  	// as it prevents concurrent scannings of the listeners list.
    List l = messageListeners.startScanning();
    if (l != null) {
			MessageEvent ev = new MessageEvent(MessageEvent.ROUTED_MESSAGE, msg, from, to, myID());
    	Iterator it = l.iterator();
    	while (it.hasNext()) {
	  		MessageListener ml = (MessageListener) it.next();
	  		ml.routedMessage(ev);
    	}
    	messageListeners.stopScanning();
    }	
  }

  private void fireAddedBehaviour(AID agentID, Behaviour b) {
  	// NOTE: A normal synchronized block could create deadlock problems
  	// as it prevents concurrent scannings of the listeners list.
    List l = agentListeners.startScanning();
    if (l != null) {
    	AgentEvent ev = null;
    	if (b == b.root()) {
    		// The behaviour has been added to the Agent
				ev = new AgentEvent(AgentEvent.ADDED_BEHAVIOUR, agentID, new BehaviourID(b), myID());
    	}
    	else {
    		// The behaviour is actually a new child that has been added to a CompositeBehaviour
				//FIXME: TO be done
    		//ev = new AgentEvent(AgentEvent.ADDED_SUB_BEHAVIOUR, agentID, new BehaviourID(b.getParent()), new BehaviourID(b), myID());
    	}
    	
    	Iterator it = l.iterator();
    	while (it.hasNext()) {
	  		AgentListener al = (AgentListener) it.next();
	  		al.addedBehaviour(ev);
    	}
    	agentListeners.stopScanning();
    }	
  }

private void fireRemovedBehaviour(AID agentID, Behaviour b) {
  	// NOTE: A normal synchronized block could create deadlock problems
  	// as it prevents concurrent scannings of the listeners list.
    List l = agentListeners.startScanning();
    if (l != null) {
			AgentEvent ev = new AgentEvent(AgentEvent.REMOVED_BEHAVIOUR, agentID, new BehaviourID(b), myID());
    	Iterator it = l.iterator();
    	while (it.hasNext()) {
	  		AgentListener al = (AgentListener) it.next();
	  		al.removedBehaviour(ev);
    	}
    	agentListeners.stopScanning();
    }	
  }

  private void fireChangedBehaviourState(AID agentID, Behaviour b, String from, String to) {
  	// NOTE: A normal synchronized block could create deadlock problems
  	// as it prevents concurrent scannings of the listeners list.
    List l = agentListeners.startScanning();
    if (l != null) {
			AgentEvent ev = new AgentEvent(AgentEvent.CHANGED_BEHAVIOUR_STATE, agentID, new BehaviourID(b), from, to, myID());
    	Iterator it = l.iterator();
    	while (it.hasNext()) {
	  		AgentListener al = (AgentListener) it.next();
	  		al.changedBehaviourState(ev);
    	}
    	agentListeners.stopScanning();
    }	
  }
  



//__SECURITY__BEGIN
  private void fireChangedAgentPrincipal(AID agentID, AgentPrincipal from, AgentPrincipal to) {
  	// NOTE: A normal synchronized block could create deadlock problems
  	// as it prevents concurrent scannings of the listeners list.
    List l = agentListeners.startScanning();
    if (l != null) {
      AgentEvent ev = new AgentEvent(AgentEvent.CHANGED_AGENT_PRINCIPAL, agentID, from, to, myID());
    	Iterator it = l.iterator();
    	while (it.hasNext()) {
	  		AgentListener al = (AgentListener) it.next();
	  		al.changedAgentPrincipal(ev);
    	}
    	agentListeners.stopScanning();
    }	
  }
//__SECURITY__END

  private void fireChangedAgentState(AID agentID, AgentState from, AgentState to) {
  	// NOTE: A normal synchronized block could create deadlock problems
  	// as it prevents concurrent scannings of the listeners list.
    List l = agentListeners.startScanning();
    if (l != null) {
			AgentEvent ev = new AgentEvent(AgentEvent.CHANGED_AGENT_STATE, agentID, from, to, myID());
    	Iterator it = l.iterator();
    	while (it.hasNext()) {
	  		AgentListener al = (AgentListener) it.next();
	  		al.changedAgentState(ev);
    	}
    	agentListeners.stopScanning();
    }	
  }
  
  private ToolNotifier findNotifier(AID observerName) {
    ToolNotifier tn = null;
    // Note that if a ToolNotifier exists it must be among the messageListeners
    // --> There is no need to search it also among the agentListeners.
		List l = messageListeners.startScanning();
		if (l != null) {
	  	Iterator it = l.iterator();
	  	while(it.hasNext()) {
	    	Object obj = it.next();
	    	if(obj instanceof ToolNotifier) {
	    		ToolNotifier tni = (ToolNotifier) obj;
					AID id = tni.getObserver();
					if(id.equals(observerName)) {
	  				tn = tni;
	  				break;
	    		}
	  		}
	  	}
			messageListeners.stopScanning();
		}
  	
    if(tn != null && tn.getState() == Agent.AP_DELETED) { // A formerly dead notifier
      removeMessageListener(tn);
      removeAgentListener(tn);
      tn = null;
    }
		return tn;
  }

  private void addMessageListener(MessageListener ml) {
    List l = messageListeners.startModifying();
    l.add(ml);
    messageListeners.stopModifying();
  }

  private void removeMessageListener(MessageListener ml) {
    List l = messageListeners.startModifying();
    l.remove(ml);
    messageListeners.stopModifying();    
  }

  private void addAgentListener(AgentListener al) {
    List l = agentListeners.startModifying();
    l.add(al);
    agentListeners.stopModifying();    
  }

  private void removeAgentListener(AgentListener al) {
    List l = agentListeners.startModifying();
    l.remove(al);
    agentListeners.stopModifying();    
  }

  private ContainerID myID() {
  	return (ContainerID) myContainer.here();
  }

}
