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

import jade.util.leap.Iterator;
import jade.util.leap.List;
import jade.util.leap.LinkedList;

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

/** 
   Full implementation of the <code>NotificationManager</code> 
   interface
   @see NotificationManager;
   @author Giovanni Caire - TILAB
 */
class RealNotificationManager implements NotificationManager {
	
  private AgentContainerImpl myContainer;
  private LADT localAgents;
  
  private List messageListeners;
  private List agentListeners;
  // This lock is used to synchronize operations on the message
  // listeners list. Using lazy processing (the list is set to null
  // when empty) the space overhead is reduced, even with this lock
  // object (an empty LinkedList holds three null pointers).
  private Object messageListenersLock = new Object();
  
  // This lock is used to synchronize operations on the agent
  // listeners list. Using lazy processing (the list is set to null
  // when empty) the space overhead is reduced, even with this lock
  // object (an empty LinkedList holds three null pointers).
  private Object agentListenersLock = new Object();

  /** 
     Constructor
   */
  public RealNotificationManager() {
  }
  
  public void initialize(AgentContainerImpl ac, LADT ladt) {
  	myContainer = ac;
  	//myID = (ContainerID) myContainer.here();
  	localAgents = ladt;
  }
  
  // ACTIVATION/DEACTIVATION METHODS
  public void enableSniffer(AID snifferName, AID toBeSniffed) {

    ToolNotifier tn = findNotifier(snifferName);
    if(tn != null && tn.getState() == Agent.AP_DELETED) { // A formerly dead notifier
      removeMessageListener(tn);
      tn = null;
    }
    if(tn == null) { // New sniffer
      tn = new ToolNotifier(snifferName);
      AID id = new AID(snifferName.getLocalName() + "-on-" + myID().getName(), AID.ISLOCALNAME);
      myContainer.initAgent(id, tn, AgentContainer.START);
      addMessageListener(tn);
    }
    tn.addObservedAgent(toBeSniffed);

  }
  
  public void disableSniffer(AID snifferName, AID notToBeSniffed) {
    ToolNotifier tn = findNotifier(snifferName);
    if(tn != null) { // The sniffer must be here
      tn.removeObservedAgent(notToBeSniffed);
      if(tn.isEmpty()) {
		removeMessageListener(tn);
		tn.doDelete();
      }
    }

  }

  public void enableDebugger(AID debuggerName, AID toBeDebugged) {
    ToolNotifier tn = findNotifier(debuggerName);
    if(tn != null && tn.getState() == Agent.AP_DELETED) { // A formerly dead notifier
      removeMessageListener(tn);
      // removeAgentListener(tn);
      tn = null;
    }
    if(tn == null) { // New debugger
      tn = new ToolNotifier(debuggerName);
      AID id = new AID(debuggerName.getLocalName() + "-on-" + myID().getName(), AID.ISLOCALNAME);
      myContainer.initAgent(id, tn, AgentContainer.START);
      addMessageListener(tn);
      addAgentListener(tn);
    }
    tn.addObservedAgent(toBeDebugged);

    //  FIXME: Need to send a complete, transactional snapshot of the
    //  agent state.
    Agent a = localAgents.get(toBeDebugged);
    AgentState as = a.getAgentState();
    fireChangedAgentState(toBeDebugged, as, as);

  }

  public void disableDebugger(AID debuggerName, AID notToBeDebugged) {
    ToolNotifier tn = findNotifier(debuggerName);
    if(tn != null) { // The debugger must be here
      tn.removeObservedAgent(notToBeDebugged);
      if(tn.isEmpty()) {
		removeMessageListener(tn);
		removeAgentListener(tn);
		tn.doDelete();
      }
    }
  }

  
  // NOTIFICATION METHODS
  // TO BE DISCUSSED: Would it be possible to have a single notification 
  // method
  // public void fireEvent(int type, Object[] param)
  // and a number of constants SENT_MESSAGE, POSTED_MESSAGE ...
  // On the basis of the type (one of the above constants) the parameters
  // are properly casted.
  
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
    synchronized(messageListenersLock) {
      if(messageListeners != null) {
		MessageEvent ev = new MessageEvent(MessageEvent.SENT_MESSAGE, msg, sender, myID());
		for(int i = 0; i < messageListeners.size(); i++) {
	  		MessageListener l = (MessageListener)messageListeners.get(i);
	  		l.sentMessage(ev);
		}
      }
    }
  }

  private void firePostedMessage(ACLMessage msg, AID receiver) {
    synchronized(messageListenersLock) {
      if(messageListeners != null) {
		MessageEvent ev = new MessageEvent(MessageEvent.POSTED_MESSAGE, msg, receiver, myID());
		for(int i = 0; i < messageListeners.size(); i++) {
	  		MessageListener l = (MessageListener)messageListeners.get(i);
	  		l.postedMessage(ev);
		}
      }
    }
  }

  private void fireReceivedMessage(ACLMessage msg, AID receiver) {
    synchronized(messageListenersLock) {
      if(messageListeners != null) {
		MessageEvent ev = new MessageEvent(MessageEvent.RECEIVED_MESSAGE, msg, receiver, myID());
		for(int i = 0; i < messageListeners.size(); i++) {
	  		MessageListener l = (MessageListener)messageListeners.get(i);
	  		l.receivedMessage(ev);
		}
      }
    }
  }

  private void fireRoutedMessage(ACLMessage msg, Channel from, Channel to) {
    synchronized(messageListenersLock) {
      if(messageListeners != null) {
		MessageEvent ev = new MessageEvent(MessageEvent.ROUTED_MESSAGE, msg, from, to, myID());
		for(int i = 0; i < messageListeners.size(); i++) {
	  		MessageListener l = (MessageListener)messageListeners.get(i);
	  		l.routedMessage(ev);
		}
      }
    }
  }

  private void fireChangedAgentState(AID agentID, AgentState from, AgentState to) {
    synchronized(messageListenersLock) {
      if(agentListeners != null) {
		AgentEvent ev = new AgentEvent(AgentEvent.CHANGED_AGENT_STATE, agentID, from, to, myID());
		for(int i = 0; i < agentListeners.size(); i++) {
	  		AgentListener l = (AgentListener)agentListeners.get(i);
	  		l.changedAgentState(ev);
		}
      }
    }
  }

  
  private ToolNotifier findNotifier(AID observerName) {
    if(messageListeners == null)
      return null;
    Iterator it = messageListeners.iterator();
    while(it.hasNext()) {
      Object obj = it.next();
      if(obj instanceof ToolNotifier) {
	ToolNotifier tn = (ToolNotifier)obj;
	AID id = tn.getObserver();
	if(id.equals(observerName))
	  return tn;
      }
    }
    return null;

  }

  private void addMessageListener(MessageListener l) {
    synchronized(messageListenersLock) {
      if(messageListeners == null)
		messageListeners = new LinkedList();
      messageListeners.add(l);
    }
  }

  private void removeMessageListener(MessageListener l) {
    synchronized(messageListenersLock) {
      if(messageListeners != null) {
		messageListeners.remove(l);
		if(messageListeners.isEmpty())
	  	messageListeners = null;
      }
    }
  }

  private void addAgentListener(AgentListener l) {
    synchronized(messageListenersLock) {
      if(agentListeners == null)
	agentListeners = new LinkedList();
      agentListeners.add(l);
    }
  }

  private void removeAgentListener(AgentListener l) {
    synchronized(messageListenersLock) {
      if(agentListeners != null) {
	agentListeners.remove(l);
	if(agentListeners.isEmpty())
	  agentListeners = null;
      }
    }
  }

  private ContainerID myID() {
  	return (ContainerID) myContainer.here();
  }

}