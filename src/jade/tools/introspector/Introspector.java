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

package jade.tools.introspector;

import java.net.InetAddress;
import java.net.UnknownHostException;

import java.util.Collections;
import jade.util.leap.List;
import jade.util.leap.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;

import jade.core.*;
import jade.core.behaviours.*;

import jade.util.Sensor;
import jade.util.SensorManager;

import jade.tools.introspector.gui.IntrospectorGUI;

import jade.domain.FIPAException;
import jade.domain.FIPAServiceCommunicator;
import jade.domain.introspection.*;

// FIXME: These three imports will not be needed anymore, when
// suitable actions will be put into the 'jade-introspection'
// ontology.
import jade.domain.JADEAgentManagement.JADEAgentManagementOntology;
import jade.domain.JADEAgentManagement.DebugOn;
import jade.domain.JADEAgentManagement.DebugOff;

import jade.gui.AgentTreeModel;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.sl.SL0Codec;

import jade.onto.basic.*;

import jade.proto.SimpleAchieveREInitiator;
import jade.proto.AchieveREInitiator;

import jade.tools.ToolAgent;
import jade.tools.introspector.gui.IntrospectorGUI;
import jade.tools.introspector.gui.MainWindow;



/*
  This class represents the Introspector Agent. This agent registers
  with the AMS as a tool, to manage an AgentTree component, then
  activates its GUI. The agent listens for ACL messages containing
  introspection events and updates the display through the
  IntrospectorGUI class.

  @author Andrea Squeri, -  Universita` di Parma
  @author Giovanni Caire, -  TILAB
*/
public class Introspector extends ToolAgent {

    private class AMSRequester extends SimpleAchieveREInitiator {

    private String actionName;

      public AMSRequester(String an, ACLMessage request) {
          super(Introspector.this, request);
	  			actionName = an;
      }
	
    protected void handleNotUnderstood(ACLMessage reply) {
      myGUI.showError("NOT-UNDERSTOOD received during " + actionName);
    }

    protected void handleRefuse(ACLMessage reply) {
      myGUI.showError("REFUSE received during " + actionName);
    }

    protected void handleAgree(ACLMessage reply) {
      // System.out.println("AGREE received");
    }

    protected void handleFailure(ACLMessage reply) {
      myGUI.showError("FAILURE received during " + actionName);
    }

    protected void handleInform(ACLMessage reply) {
      // System.out.println("INFORM received");
    }

  } // End of AMSRequester class

  // GUI events
  public static final int STEP_EVENT = 1;
  public static final int BREAK_EVENT = 2;
  public static final int SLOW_EVENT = 3;
  public static final int GO_EVENT = 4;
	public static final int KILL_EVENT = 5;
	public static final int SUSPEND_EVENT = 6;

  private IntrospectorGUI myGUI;
  private Sensor guiSensor = new Sensor();
  private String myContainerName;
  private Map windowMap = Collections.synchronizedMap(new TreeMap());
  
  // The set of agents that are observed in step-by-step mode
  private Set stepByStepAgents = new HashSet();
  // The set of agents that are observed in slow mode
  private Set slowAgents = new HashSet();
  // Maps an observed agent with the String used as reply-with in the 
  // message that notified about an event that had to be observed synchronously
  private Map pendingReplies = new HashMap();
  // Maps an observed agent with the ToolNotifier that notifies events 
  // about that agent to this Introspector
  private Map notifiers = new HashMap();

  private SequentialBehaviour AMSSubscribe = new SequentialBehaviour();

  class IntrospectorAMSListenerBehaviour extends AMSListenerBehaviour {
  	
      protected void installHandlers(Map handlersTable) {

	handlersTable.put(JADEIntrospectionOntology.ADDEDCONTAINER, new EventHandler() {
	  public void handle(Event ev) {
	    AddedContainer ac = (AddedContainer)ev;
	    ContainerID cid = ac.getContainer();
	    String name = cid.getName();
	    String address = cid.getAddress();
	    try {
	      InetAddress addr = InetAddress.getByName(address);
	      myGUI.addContainer(name, addr);
	    }
	    catch(UnknownHostException uhe) {
	      myGUI.addContainer(name, null);
	    }
	  }
	});

	handlersTable.put(JADEIntrospectionOntology.REMOVEDCONTAINER, new EventHandler() {
	  public void handle(Event ev) {
	    RemovedContainer rc = (RemovedContainer)ev;
	    ContainerID cid = rc.getContainer();
	    String name = cid.getName();
	    myGUI.removeContainer(name);
	  }
        });

	handlersTable.put(JADEIntrospectionOntology.BORNAGENT, new EventHandler() {
          public void handle(Event ev) {
	    BornAgent ba = (BornAgent)ev;
	    ContainerID cid = ba.getWhere();
	    String container = cid.getName();
	    AID agent = ba.getAgent();
	    myGUI.addAgent(container, agent);
	    if(agent.equals(getAID()))
	      myContainerName = container;
	  }
        });

	handlersTable.put(JADEIntrospectionOntology.DEADAGENT, new EventHandler() {
          public void handle(Event ev) {
	    DeadAgent da = (DeadAgent)ev;
	    ContainerID cid = da.getWhere();
	    String container = cid.getName();
	    AID agent = da.getAgent();
	    MainWindow m = (MainWindow)windowMap.get(agent);
	    if(m != null) {
	      myGUI.closeInternal(m);
	      windowMap.remove(agent);
	    }
	    myGUI.removeAgent(container, agent);
	  }
        });

	handlersTable.put(JADEIntrospectionOntology.MOVEDAGENT, new EventHandler() {
	  public void handle(Event ev) {
	    MovedAgent ma = (MovedAgent)ev;
	    AID agent = ma.getAgent();
	    ContainerID from = ma.getFrom();
	    myGUI.removeAgent(from.getName(), agent);
	    ContainerID to = ma.getTo();
	    myGUI.addAgent(to.getName(), agent);
	    
	    if (windowMap.containsKey(agent)) {
	 			MainWindow m = (MainWindow)windowMap.get(agent);
	    	// FIXME: We should clean behaviours and pending messages here 
	    	requestDebugOn(agent);
	    }
	  }
        });

      } // End of installHandlers() method
  }

  public void toolSetup() {

    ACLMessage msg = getRequest();
    msg.setOntology(JADEAgentManagementOntology.NAME);

    // Send 'subscribe' message to the AMS
    AMSSubscribe.addSubBehaviour(new SenderBehaviour(this, getSubscribe()));

    // Handle incoming 'inform' messages about Platform events from the AMS
    AMSSubscribe.addSubBehaviour(new IntrospectorAMSListenerBehaviour());
    
    addBehaviour(AMSSubscribe);
    
    // Handle incoming INFORM messages about Agent and Message events from the 
    // ToolNotifiers
    addBehaviour(new IntrospectionListenerBehaviour());

    // Handle incoming INFORM messages about observation start/stop from
    // ToolNotifiers
    addBehaviour(new ControlListenerBehaviour(this));

    // Manages GUI events
    addBehaviour(new SensorManager(this, guiSensor) {
    	public void onEvent(jade.util.Event ev) {
  			AID id = ((MainWindow) ev.getSource()).getDebugged();
  			switch (ev.getType()) {
  			case STEP_EVENT: 
  				proceed(id);
  				break;
  			case BREAK_EVENT:
  				stepByStepAgents.add(id);
  				slowAgents.remove(id);
  				break;
  			case SLOW_EVENT:
  				stepByStepAgents.remove(id);
  				slowAgents.add(id);
  				proceed(id);
  				break;
  			case GO_EVENT:
 					stepByStepAgents.remove(id);
  				slowAgents.remove(id);
 					proceed(id);
  			}
    	}
    }	);
      
    // Show Graphical User Interface
    myGUI = new IntrospectorGUI(this);
    myGUI.setVisible(true);

  }

  /*
    Adds an agent to the debugged agents map, and asks the AMS to
    start debugging mode on that agent.
  */
  public void addAgent(AID name) {
    if(!windowMap.containsKey(name)) {
			MainWindow m = new MainWindow(guiSensor, name);
			myGUI.addWindow(m);
			windowMap.put(name, m);
		
			stepByStepAgents.add(name);
			
			requestDebugOn(name);
    }
/*      try {

	ACLMessage msg = getRequest();
	DebugOn dbgOn = new DebugOn();
	dbgOn.setDebugger(getAID());
	dbgOn.addDebuggedAgents(name);
	Action a = new Action();
	a.set_0(getAMS());
	a.set_1(dbgOn);
	List l = new ArrayList(1);
	l.add(a);
	fillMsgContent(msg, l);

	addBehaviour(new AMSRequester("DebugOn", msg));
      }
      catch(FIPAException fe) {
	fe.printStackTrace();
      }
    }*/
  }

  private void requestDebugOn(AID name) {
  	try {
			ACLMessage msg = getRequest();
			DebugOn dbgOn = new DebugOn();
			dbgOn.setDebugger(getAID());
			dbgOn.addDebuggedAgents(name);
			Action a = new Action();
			a.set_0(getAMS());
			a.set_1(dbgOn);
			List l = new ArrayList(1);
			l.add(a);
			fillMsgContent(msg, l);
			
			addBehaviour(new AMSRequester("DebugOn", msg));
		}
		catch(FIPAException fe) {
			fe.printStackTrace();
		}
  }
    
  /*
    Removes an agent from the debugged agents map, and closes its
    window. Moreover,it and asks the AMS to stop debugging mode on
    that agent.
  */
  public void removeAgent(final AID name) {
    if(windowMap.containsKey(name)) {
      try {
	final MainWindow m = (MainWindow)windowMap.get(name);
	myGUI.closeInternal(m);
	windowMap.remove(name);

	stepByStepAgents.remove(name);
	slowAgents.remove(name);
	proceed(name);

	ACLMessage msg = getRequest();
	DebugOff dbgOff = new DebugOff();
	dbgOff.setDebugger(getAID());
	dbgOff.addDebuggedAgents(name);
	Action a = new Action();
	a.set_0(getAMS());
	a.set_1(dbgOff);
	List l = new ArrayList(1);
	l.add(a);
	fillMsgContent(msg, l);

	addBehaviour(new AMSRequester("DebugOff", msg));
      }
      catch(FIPAException fe) {
	fe.printStackTrace();
      }
    }
  }


  /**
   Cleanup during agent shutdown. This method cleans things up when
   <em>RMA</em> agent is destroyed, disconnecting from <em>AMS</em>
   agent and closing down the platform administration <em>GUI</em>.
  */
  public void toolTakeDown() {
    // Stop debugging all the agents
    if(!windowMap.isEmpty()) {
      ACLMessage msg = getRequest();
      DebugOff dbgOff = new DebugOff();
      dbgOff.setDebugger(getAID());
      Iterator it = windowMap.keySet().iterator();
      while(it.hasNext()) {
	AID id = (AID)it.next();
	dbgOff.addDebuggedAgents(id);
      }

      Action a = new Action();
      a.set_0(getAMS());
      a.set_1(dbgOff);
      List l = new ArrayList(1);
      l.add(a);
      try {
	fillMsgContent(msg, l);
	FIPAServiceCommunicator.doFipaRequestClient(this, msg);
      }
      catch(FIPAException fe) {
    	// When the AMS replies the tool notifier is no longer registered.
    	// But we don't care as we are exiting
      //System.out.println(e.getMessage());
      }
    }

    send(getCancel());
    // myGUI.setVisible(false);  Not needed and can cause thread deadlock on join.
    myGUI.disposeAsync();
  }


  /**
   Callback method for platform management <em>GUI</em>.
   */
  public AgentTreeModel getModel() {
    return myGUI.getModel();
  }

  /*
    Listens to introspective messages and dispatches them.
  */
  private class IntrospectionListenerBehaviour extends CyclicBehaviour {

    private MessageTemplate template;
    private Map handlers = new TreeMap(String.CASE_INSENSITIVE_ORDER);

    IntrospectionListenerBehaviour() {
      template = MessageTemplate.and(MessageTemplate.MatchOntology(JADEIntrospectionOntology.NAME),
				     MessageTemplate.MatchConversationId(getName() + "-event"));

      // Fill handlers table ...
      handlers.put(JADEIntrospectionOntology.CHANGEDAGENTSTATE, new EventHandler() {
	public void handle(Event ev) {

	}

      });

      handlers.put(JADEIntrospectionOntology.ADDEDBEHAVIOUR, new EventHandler() {
	public void handle(Event ev) {
                AddedBehaviour ab = (AddedBehaviour)ev;
                AID agent = ab.getAgent();
                MainWindow wnd = (MainWindow)windowMap.get(agent);
                if(wnd != null)
                    myGUI.behaviourAdded(wnd, ab);
	}

      });

      handlers.put(JADEIntrospectionOntology.REMOVEDBEHAVIOUR, new EventHandler() {
	public void handle(Event ev) {
                RemovedBehaviour rb = (RemovedBehaviour)ev;
                AID agent = rb.getAgent();
                MainWindow wnd = (MainWindow)windowMap.get(agent);
                if(wnd != null)
                    myGUI.behaviourRemoved(wnd, rb);
	}

      });

      handlers.put(JADEIntrospectionOntology.CHANGEDBEHAVIOURSTATE, new EventHandler() {
	public void handle(Event ev) {
                ChangedBehaviourState cs = (ChangedBehaviourState)ev;
                AID agent = cs.getAgent();
                MainWindow wnd = (MainWindow)windowMap.get(agent);
                if(wnd != null) {
                    myGUI.behaviourChangeState(wnd, cs);
                }
                if (stepByStepAgents.contains(agent)) {
                	return;
                }
                if (slowAgents.contains(agent)) {
                	try {
                		Thread.sleep(500);
                	}
                	catch (InterruptedException ie) {
                		// The introspector is probably being killed
                	}
                }
                proceed(agent);
	}

      });

      handlers.put(JADEIntrospectionOntology.SENTMESSAGE, new EventHandler() {
	public void handle(Event ev) {
	  SentMessage sm = (SentMessage)ev;
	  AID sender = sm.getSender();

	  MainWindow wnd = (MainWindow)windowMap.get(sender);
	  if(wnd != null)
	    myGUI.messageSent(wnd, sm);
	}

      });

      handlers.put(JADEIntrospectionOntology.RECEIVEDMESSAGE, new EventHandler() {
	public void handle(Event ev) {
	  ReceivedMessage rm = (ReceivedMessage)ev;
	  AID receiver = rm.getReceiver();

	  MainWindow wnd = (MainWindow)windowMap.get(receiver);
	  if(wnd != null)
	    myGUI.messageReceived(wnd, rm);
	}

      });

      handlers.put(JADEIntrospectionOntology.POSTEDMESSAGE, new EventHandler() {
	public void handle(Event ev) {
	  PostedMessage pm = (PostedMessage)ev;
	  AID receiver = pm.getReceiver();

	  MainWindow wnd = (MainWindow)windowMap.get(receiver);
	  if(wnd != null)
	    myGUI.messagePosted(wnd, pm);
	}

      });

      handlers.put(JADEIntrospectionOntology.CHANGEDAGENTSTATE, new EventHandler() {
	public void handle(Event ev) {
	  ChangedAgentState cas = (ChangedAgentState)ev;
	  AID agent = cas.getAgent();

	  MainWindow wnd = (MainWindow)windowMap.get(agent);
	  if(wnd != null)
	    myGUI.changedAgentState(wnd, cas);
	}

      });

    }

    public void action() {

      ACLMessage message = receive(template);
      if(message != null) {
        AID name = message.getSender();
	try{
          List l = extractMsgContent(message);
	  Occurred o = (Occurred)l.get(0);
	  EventRecord er = o.get_0();
	  Event ev = er.getWhat();
	  // DEBUG
	  //System.out.println("Received event "+ev);
    if (message.getReplyWith() != null) {
    	// A reply is expected --> put relevant information into the
    	// pendingReplies Map
      ChangedBehaviourState cs = (ChangedBehaviourState)ev;
    	pendingReplies.put(cs.getAgent(), message.getReplyWith());
    }
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

  } // End of inner class IntrospectionListenerBehaviour


  /**
     Inner class ControlListenerBehaviour.
     This is a behaviour that listen for messages from ToolNotifiers
     informing that they have started notifying events about a given 
     agent. These information are used to keep the map between observed
     agents and ToolNotifiers up to date.
   */
  private class ControlListenerBehaviour extends CyclicBehaviour {
    private MessageTemplate template;

    ControlListenerBehaviour(Agent a) {
    	super(a);
      template = MessageTemplate.and(
     		MessageTemplate.MatchOntology(JADEIntrospectionOntology.NAME),
				MessageTemplate.MatchConversationId(getName() + "-control"));
    }
    
  	public void action() {
      ACLMessage message = receive(template);
      if(message != null) {
				try{
          List l = extractMsgContent(message);
          DonePredicate d = (DonePredicate) l.get(0);
          Action a = d.get_0();
          AID tn = a.getActor();
          StartNotify sn = (StartNotify) a.getAction();
          AID observed = sn.getObserved();
          System.out.println("Map "+observed+" to "+tn);
          notifiers.put(observed, tn);
				}
				catch(FIPAException fe) {
	  			fe.printStackTrace();
				}
				catch(ClassCastException cce) {
	  			cce.printStackTrace();
				}
      }
      else {
				block();
      }
    }

  } // End of inner class ControlListenerBehaviour

  private void proceed(AID id) {
		String pendingReplyWith = (String) pendingReplies.remove(id);
		AID tn = (AID) notifiers.get(id);
		if (pendingReplyWith != null && tn != null) {
			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
			msg.addReceiver(tn);
			msg.setInReplyTo(pendingReplyWith);
			send(msg);
		}
  }
  
}
