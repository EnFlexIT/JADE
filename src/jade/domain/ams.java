/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop 
multi-agent systems in compliance with the FIPA specifications.
Copyright (C) 2000 CSELT S.p.A. 

The updating of this file to JADE 2.0 has been partially supported by the IST-1999-10211 LEAP Project

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

package jade.domain;

import java.io.StringReader;
import java.io.StringWriter;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.FileWriter;
import java.net.InetAddress;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;

import jade.core.*;
import jade.core.behaviours.*;

import jade.core.event.PlatformEvent;
import jade.core.event.MTPEvent;

import jade.domain.FIPAAgentManagement.*;
import jade.domain.JADEAgentManagement.*;
import jade.domain.introspection.*;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import jade.lang.Codec;
import jade.lang.sl.SL0Codec;

import jade.onto.Ontology;
import jade.onto.OntologyException;
import jade.onto.Frame;

import jade.onto.basic.Action;
import jade.onto.basic.BasicOntology;
import jade.onto.basic.ResultPredicate;
import jade.onto.basic.DonePredicate;
import jade.onto.basic.TrueProposition;

import jade.mtp.MTPException;

import jade.proto.FipaRequestResponderBehaviour;

/**
  Standard <em>Agent Management System</em> agent. This class
  implements <em><b>FIPA</b></em> <em>AMS</em> agent. <b>JADE</b>
  applications cannot use this class directly, but interact with it
  through <em>ACL</em> message passing.

  
  @author Giovanni Rimassa - Universita` di Parma
  @version $Date$ $Revision$


*/
public class ams extends Agent implements AgentManager.Listener {

  private abstract class AMSBehaviour
      extends FipaRequestResponderBehaviour.ActionHandler
      implements FipaRequestResponderBehaviour.Factory {

	
    protected AMSBehaviour(ACLMessage req) {
      super(ams.this,req);
    }


    /**
     * Create the content for the AGREE message
     * @param a is the action that has been agreed to perform
     * @return a String with the content ready to be set into the message
     **/
    protected String createAgreeContent(Action a) {
	ACLMessage temp = new ACLMessage(ACLMessage.AGREE); 
	temp.setLanguage(getRequest().getLanguage());
	temp.setOntology(getRequest().getOntology());
	List l = new ArrayList(2);
	if (a == null) {
	    a = new Action();
	    a.set_0(getAID());
	    a.set_1("UnknownAction");
	}
	l.add(a);
	l.add(new TrueProposition());
	try {
	    fillContent(temp,l);
	} catch (Exception ee) { // in any case try to return some good content
	    return "( true )";
	} 
	return temp.getContent();
    }

    /**
     * Create the content for a so-called "exceptional" message, i.e.
     * one of NOT_UNDERSTOOD, FAILURE, REFUSE message
     * @param a is the Action that generated the exception
     * @param e is the generated Exception
     * @return a String containing the content to be sent back in the reply
     * message; in case an exception is thrown somewhere, the method
     * try to return anyway a valid content with a best-effort strategy
     **/
    protected String createExceptionalMsgContent(Action a, String ontoName, FIPAException e) {
	ACLMessage temp = new ACLMessage(ACLMessage.NOT_UNDERSTOOD); 
	temp.setLanguage(SL0Codec.NAME);
	temp.setOntology(ontoName);
	List l = new ArrayList(2);
	if (a == null) {
	    a = new Action();
	    a.set_0(getAID());
	    a.set_1("UnknownAction");
	}
	l.add(a);
	l.add(e);
	try {
	    fillContent(temp,l);
	} catch (Exception ee) { // in any case try to return some good content
	    return e.getMessage();
	} 
	return temp.getContent();
    }


    // Each concrete subclass will implement this deferred method to
    // do action-specific work
    protected abstract void processAction(Action a) throws FIPAException;

    public void action() {
	Action a = null;
	try {
	ACLMessage msg = getRequest();
	List l = myAgent.extractContent(msg);
	a = (Action)l.get(0);
	// Do real action, deferred to subclasses
	processAction(a);
	} catch(FIPAException fe) {
	    String ontoName = getRequest().getOntology();
	    sendReply(ACLMessage.REFUSE,createExceptionalMsgContent(a, ontoName, fe));
	}
    }

    /**
       Writes the <code>Done</code> predicate for the specific action
       into the result <code>String</code> object, encoded in SL0.
     */
    protected String doneAction(Action a) throws FIPAException {
      try {
	Ontology o = lookupOntology(getRequest().getOntology());
	DonePredicate dp = new DonePredicate();
	dp.set_0(a);
	Frame f = o.createFrame(dp, BasicOntology.DONE);
	List l = new ArrayList(1);
	l.add(f);
	Codec c = lookupLanguage(SL0Codec.NAME);
	String result = c.encode(l, o);
	return result;
      }
      catch(OntologyException oe) {
	oe.printStackTrace();
	throw new FIPAException("Internal error in building Done predicate.");
      }

    }


    public boolean done() {
      return true;
    }

    public void reset() {
    }

  } // End of AMSBehaviour class


  // These four concrete classes serve both as a Factory and as an
  // Action: when seen as Factory they can spawn a new
  // Behaviour to process a given request, and when seen as
  // Action they process their request and terminate.

  private class RegBehaviour extends AMSBehaviour {

    public RegBehaviour(ACLMessage msg) {
      super(msg);
    }
    public FipaRequestResponderBehaviour.ActionHandler create(ACLMessage msg) {
      return new RegBehaviour(msg);
    }

    protected void processAction(Action a) throws FIPAException {
      Register r = (Register)a.getAction();
      AMSAgentDescription amsd = (AMSAgentDescription)r.get_0();

      // This agent was created by some other, which is still
      // waiting for an 'inform' message. Recover the buffered
      // message from the Map and send it back.
      ACLMessage informCreator = (ACLMessage)pendingInforms.remove(amsd.getName());

      //The message in pendingInforms can be registered with only the localName 
      //without the platformID
      if(informCreator == null) {
	String name = amsd.getName().getName();
	int atPos = name.lastIndexOf('@');
	if(atPos > 0) {
	  name = name.substring(0, atPos);
	  informCreator = (ACLMessage)pendingInforms.remove(name);
	}
      }

      try {
	// Write new agent data in AMS Agent Table
	AMSRegister(amsd);
	sendReply(ACLMessage.AGREE,createAgreeContent(a));
	sendReply(ACLMessage.INFORM, doneAction(a));

	// Inform agent creator that registration was successful.
	if(informCreator !=  null) {
	  send(informCreator);
	}
      }
      catch(AlreadyRegistered are) {
	sendReply(ACLMessage.AGREE, createAgreeContent(a));
	String ontoName = getRequest().getOntology();
	sendReply(ACLMessage.FAILURE,createExceptionalMsgContent(a, ontoName, are));
	// Inform agent creator that registration failed.
	if(informCreator != null) {
	  informCreator.setPerformative(ACLMessage.FAILURE);
	  informCreator.setContent(createExceptionalMsgContent(a, ontoName, are));
	  send(informCreator);
	}
      }
    }

  } // End of RegBehaviour class

  private class DeregBehaviour extends AMSBehaviour {
    public DeregBehaviour(ACLMessage msg) {
      super(msg);
    }
    public FipaRequestResponderBehaviour.ActionHandler create(ACLMessage msg) {
      return new DeregBehaviour(msg);
    }

    protected void processAction(Action a) throws FIPAException {
      Deregister d = (Deregister)a.getAction();
      AMSAgentDescription amsd = (AMSAgentDescription)d.get_0();
      AMSDeregister(amsd);
      sendReply(ACLMessage.AGREE, createAgreeContent(a));
      sendReply(ACLMessage.INFORM,doneAction(a));
    }

  } // End of DeregBehaviour class

  private class ModBehaviour extends AMSBehaviour {
    public ModBehaviour(ACLMessage msg) {
      super(msg);
    }
    public FipaRequestResponderBehaviour.ActionHandler create(ACLMessage msg) {
      return new ModBehaviour(msg);
    }

    protected void processAction(Action a) throws FIPAException {
      Modify m = (Modify)a.getAction();
      AMSAgentDescription amsd = (AMSAgentDescription)m.get_0();
      AMSModify(amsd);
      sendReply(ACLMessage.AGREE, createAgreeContent(a));
      sendReply(ACLMessage.INFORM,doneAction(a));
    }

  } // End of ModBehaviour class

  private class SrchBehaviour extends AMSBehaviour {
    public SrchBehaviour(ACLMessage msg) {
      super(msg);
    }
    public FipaRequestResponderBehaviour.ActionHandler create(ACLMessage msg) {
      return new SrchBehaviour(msg);
    }

    protected void processAction(Action a) throws FIPAException {
      Search s = (Search)a.getAction();
      AMSAgentDescription amsd = (AMSAgentDescription)s.get_0();
      SearchConstraints constraints = s.get_1();
      List l = AMSSearch(amsd, constraints, getReply());
      sendReply(ACLMessage.AGREE,createAgreeContent(a));
      ACLMessage msg = getRequest().createReply();
      msg.setPerformative(ACLMessage.INFORM);
      ResultPredicate r = new ResultPredicate();
      r.set_0(a);
      for (int i=0; i<l.size(); i++)
      	r.add_1(l.get(i));
      l.clear();
      l.add(r);
      fillContent(msg,l); 
      send(msg);
    }

  } // End of SrchBehaviour class

  private class GetDescriptionBehaviour extends AMSBehaviour {
    public GetDescriptionBehaviour(ACLMessage msg) {
      super(msg);
    }
    public FipaRequestResponderBehaviour.ActionHandler create(ACLMessage msg) {
      return new GetDescriptionBehaviour(msg);
    }

    protected void processAction(Action a) throws FIPAException {

      sendReply(ACLMessage.AGREE, createAgreeContent(a));
      ACLMessage reply = getReply();
      reply.setPerformative(ACLMessage.INFORM);
      List l = new ArrayList(1);
      ResultPredicate rp = new ResultPredicate();
      rp.set_0(a);
      rp.add_1(theProfile);
      ArrayList list = new ArrayList(1);
      list.add(rp);
      fillContent(reply,list);
      send(reply);
    }

  } // End of GetDescriptionBehaviour class


  // These Behaviours handle interactions with platform tools.

  private class RegisterToolBehaviour extends CyclicBehaviour {

    private MessageTemplate subscriptionTemplate;

    RegisterToolBehaviour() {

      MessageTemplate mt1 = MessageTemplate.MatchLanguage(SL0Codec.NAME);
      MessageTemplate mt2 = MessageTemplate.MatchOntology(JADEIntrospectionOntology.NAME);
      MessageTemplate mt12 = MessageTemplate.and(mt1, mt2);

      mt1 = MessageTemplate.MatchReplyWith("tool-subscription");
      mt2 = MessageTemplate.MatchPerformative(ACLMessage.SUBSCRIBE);
      subscriptionTemplate = MessageTemplate.and(mt1, mt2);
      subscriptionTemplate = MessageTemplate.and(subscriptionTemplate, mt12);

    }

    public void action() {

      // Receive 'subscribe' ACL messages.
      ACLMessage current = receive(subscriptionTemplate);
      if(current != null) {
	// FIXME: Should parse 'iota ?x ...'

	// Get new tool name from subscription message
	AID newTool = current.getSender();

	try {

	  // Send back the whole container list.
	  ContainerID[] ids = myPlatform.containerIDs();
	  for(int i = 0; i < ids.length; i++) {

	    ContainerID cid = ids[i];

	    AddedContainer ac = new AddedContainer();
	    ac.setContainer(cid);

	    EventRecord er = new EventRecord(ac, here());
	    Occurred o = new Occurred();
	    o.set_0(er);

	    List l = new ArrayList(1);
	    l.add(o);

	    toolNotification.clearAllReceiver();
	    toolNotification.addReceiver(newTool);
	    fillContent(toolNotification, l);

	    send(toolNotification);

	  }

	  // Send all agent names, along with their container name.
	  AID[] agents = myPlatform.agentNames();
	  for(int i = 0; i < agents.length; i++) {

	    AID agentName = agents[i];
	    ContainerID cid = myPlatform.getContainerID(agentName);

	    BornAgent ba = new BornAgent();
	    ba.setAgent(agentName);
	    ba.setWhere(cid);

	    EventRecord er = new EventRecord(ba, here());
	    Occurred o = new Occurred();
	    o.set_0(er);

	    List l = new ArrayList(1);
	    l.add(o);

	    toolNotification.clearAllReceiver();
	    toolNotification.addReceiver(newTool);
	    fillContent(toolNotification, l);

	    send(toolNotification);
	  }

	  // Send the list of the installed MTPs
	  String[] addresses = myPlatform.platformAddresses();
	  for(int i = 0; i < addresses.length; i++) {
	    AddedMTP amtp = new AddedMTP();
	    amtp.setAddress(addresses[i]);
	    amtp.setWhere(new ContainerID(AgentManager.MAIN_CONTAINER_NAME, null)); // FIXME: should use AgentManager to know the container

	    EventRecord er = new EventRecord(amtp, here());
	    Occurred o = new Occurred();
	    o.set_0(er);

	    List l = new ArrayList(1);
	    l.add(o);

	    toolNotification.clearAllReceiver();
	    toolNotification.addReceiver(newTool);
	    fillContent(toolNotification, l);

	    send(toolNotification);
	  }
	  /*
	  //Notification to the RMA of the APDescription
	   PlatformDescription ap = new PlatformDescription();
	   ap.setPlatform(theProfile);
	   EventOccurred eo = new EventOccurred();
	   eo.setEvent(ap);
	   List l = new ArrayList(1);
	   l.add(eo);
	   toolNotification.clearAllReceiver();
	   toolNotification.addReceiver(newTool);
	   fillContent(toolNotification, l);
	   send(toolNotification);
	  */
	  // Add the new tool to tools list.
	  tools.add(newTool);

	}
	catch(NotFoundException nfe) {
	  nfe.printStackTrace();
	}
	catch(FIPAException fe) {
	  fe.printStackTrace();
	}
      }
      else
	block();

    }

  } // End of RegisterToolBehaviour class


  private class DeregisterToolBehaviour extends CyclicBehaviour {

    private MessageTemplate cancellationTemplate;

    DeregisterToolBehaviour() {

      MessageTemplate mt1 = MessageTemplate.MatchLanguage(SL0Codec.NAME);
      MessageTemplate mt2 = MessageTemplate.MatchOntology(JADEIntrospectionOntology.NAME);
      MessageTemplate mt12 = MessageTemplate.and(mt1, mt2);

      mt1 = MessageTemplate.MatchReplyWith("tool-cancellation");
      mt2 = MessageTemplate.MatchPerformative(ACLMessage.CANCEL);
      cancellationTemplate = MessageTemplate.and(mt1, mt2);
      cancellationTemplate = MessageTemplate.and(cancellationTemplate, mt12);

    }

    public void action() {

      // Receive 'cancel' ACL messages.
      ACLMessage current = receive(cancellationTemplate);
      if(current != null) {
	// FIXME: Should parse the content

	// Remove this tool to tools agent group.
	tools.remove(current.getSender());
        
      }
      else
	block();

    }

  } // End of DeregisterToolBehaviour class


  private class NotifyToolsBehaviour extends CyclicBehaviour {

    public void action() {

      synchronized(ams.this) { // Mutual exclusion with handleXXX() methods

	// Look into the event buffer
	Iterator it = eventQueue.iterator();
	Occurred o = new Occurred();
	while(it.hasNext()) {

	  // Write the event into the notification message
	  EventRecord er = (EventRecord)it.next();
	  o.set_0(er);

	  List l = new ArrayList(1);
	  l.add(o);
	  try {
	    fillContent(toolNotification, l);
	  }
	  catch(FIPAException fe) {
	    fe.printStackTrace();
	  }

	  // Put all tools in the receiver list
	  toolNotification.clearAllReceiver();
	  Iterator toolIt = tools.iterator();
          
	  while(toolIt.hasNext()) {
	    AID tool = (AID)toolIt.next();
	    toolNotification.addReceiver(tool);
	  }


	  send(toolNotification);
	  it.remove();
	}
      }

      block();
    }

  } // End of NotifyToolsBehaviour class

  private class KillContainerBehaviour extends AMSBehaviour {
    public KillContainerBehaviour(ACLMessage msg) {
      super(msg);
    }
    public FipaRequestResponderBehaviour.ActionHandler create(ACLMessage msg) {
      return new KillContainerBehaviour(msg);
    }

    protected void processAction(Action a) throws FIPAException {

      KillContainer kc = (KillContainer)a.get_1();
      ContainerID cid = kc.getContainer();
      myPlatform.killContainer(cid);
      sendReply(ACLMessage.AGREE, createAgreeContent(a));
      sendReply(ACLMessage.INFORM,doneAction(a));
    }

  } // End of KillContainerBehaviour class

  private class CreateBehaviour extends AMSBehaviour {
    public CreateBehaviour(ACLMessage msg) {
      super(msg);
    }
    public FipaRequestResponderBehaviour.ActionHandler create(ACLMessage msg) {
      return new CreateBehaviour(msg);
    }

    protected void processAction(Action a) throws FIPAException {
      CreateAgent ca = (CreateAgent)a.get_1();

      String agentName = ca.getAgentName();
      String className = ca.getClassName();
      ContainerID container = ca.getContainer();
      Iterator arg = ca.getAllArguments(); //return an iterator of all arguments
      //create the array of string
      ArrayList listArg = new ArrayList();
      while(arg.hasNext())
       	listArg.add(arg.next().toString());
      String[] arguments = new String[listArg.size()];
      for(int n = 0; n< listArg.size(); n++)
       	arguments[n] = (String)listArg.get(n);

      sendReply(ACLMessage.AGREE, createAgreeContent(a));

      try {
	myPlatform.create(agentName, className, arguments, container);
	// An 'inform Done' message will be sent to the requester only
	// when the newly created agent will register itself with the
	// AMS. The new agent's name will be used as the key in the map.
	ACLMessage reply = getReply();
	reply = (ACLMessage)reply.clone();
	reply.setPerformative(ACLMessage.INFORM);
	reply.setContent(doneAction(a));

	pendingInforms.put(agentName, reply);
      }
      catch(UnreachableException ue) {
	throw new jade.domain.FIPAAgentManagement.InternalError("The container is not reachable");
      }

    }

  } // End of CreateBehaviour class

  private class KillBehaviour extends AMSBehaviour {
    public KillBehaviour(ACLMessage msg) {
      super(msg);
    }
    public FipaRequestResponderBehaviour.ActionHandler create(ACLMessage msg) {
      return new KillBehaviour(msg);
    }

    protected void processAction(Action a) throws FIPAException {
      // Kill an agent
      KillAgent ka = (KillAgent)a.get_1();
      AID agentID = ka.getAgent();
      String password = ka.getPassword();

      try {
	myPlatform.kill(agentID, password);
	sendReply(ACLMessage.AGREE, createAgreeContent(a));
	sendReply(ACLMessage.INFORM, doneAction(a));
      }
      catch(UnreachableException ue) {
	throw new jade.domain.FIPAAgentManagement.InternalError("The container is not reachable");
      }
      catch(NotFoundException nfe) {
	throw new NotRegistered();
      }

    }

  } // End of KillBehaviour class


  private class SniffAgentOnBehaviour extends AMSBehaviour { 
    public SniffAgentOnBehaviour(ACLMessage msg) {
      super(msg);
    }
    public FipaRequestResponderBehaviour.ActionHandler create(ACLMessage msg) {
      return new SniffAgentOnBehaviour(msg);
    }

    protected void processAction(Action a) throws FIPAException {
      SniffOn so = (SniffOn)a.get_1();
      try {
	myPlatform.sniffOn(so.getSniffer(), so.getCloneOfSniffedAgents());
	sendReply(ACLMessage.AGREE, createAgreeContent(a));
	sendReply(ACLMessage.INFORM, doneAction(a));
      }
      catch(UnreachableException ue) {
	throw new jade.domain.FIPAAgentManagement.InternalError("The container is not reachable");
      }
      catch(NotFoundException nfe) {
	throw new NotRegistered();
      }
    }

  } // End of SniffAgentOnBehaviour class

  private class SniffAgentOffBehaviour extends AMSBehaviour {
    public SniffAgentOffBehaviour(ACLMessage msg) {
      super(msg);
    }
    public FipaRequestResponderBehaviour.ActionHandler create(ACLMessage msg) {
      return new SniffAgentOffBehaviour(msg);
    }

    protected void processAction(Action a) throws FIPAException {
      SniffOff so = (SniffOff)a.get_1();
      try {
	myPlatform.sniffOff(so.getSniffer(), so.getCloneOfSniffedAgents());
	sendReply(ACLMessage.AGREE, createAgreeContent(a));
	sendReply(ACLMessage.INFORM,doneAction(a));
      }
      catch(UnreachableException ue) {
	throw new jade.domain.FIPAAgentManagement.InternalError("The container is not reachable");
      }
      catch(NotFoundException nfe) {
	throw new NotRegistered();
      }
    }

  } // End of SniffAgentOffBehaviour class


  private class DebugAgentOnBehaviour extends AMSBehaviour { 
    public DebugAgentOnBehaviour(ACLMessage msg) {
      super(msg);
    }

    public FipaRequestResponderBehaviour.ActionHandler create(ACLMessage msg) {
      return new DebugAgentOnBehaviour(msg);
    }

    protected void processAction(Action a) throws FIPAException {
      DebugOn dbgOn = (DebugOn)a.get_1();
      try {
	myPlatform.debugOn(dbgOn.getDebugger(), dbgOn.getCloneOfDebuggedAgents());
	sendReply(ACLMessage.AGREE, createAgreeContent(a));
	sendReply(ACLMessage.INFORM, doneAction(a));
      }
      catch(UnreachableException ue) {
	throw new jade.domain.FIPAAgentManagement.InternalError("The container is not reachable");
      }
      catch(NotFoundException nfe) {
	throw new NotRegistered();
      }
    }

  } // End of DebugAgentOnBehaviour class

  private class DebugAgentOffBehaviour extends AMSBehaviour {
    public DebugAgentOffBehaviour(ACLMessage msg) {
      super(msg);
    }

    public FipaRequestResponderBehaviour.ActionHandler create(ACLMessage msg) {
      return new DebugAgentOffBehaviour(msg);
    }

    protected void processAction(Action a) throws FIPAException {
      DebugOff dbgOff = (DebugOff)a.get_1();
      try {
	myPlatform.debugOff(dbgOff.getDebugger(), dbgOff.getCloneOfDebuggedAgents());
	sendReply(ACLMessage.AGREE, createAgreeContent(a));
	sendReply(ACLMessage.INFORM, doneAction(a));
      }
      catch(UnreachableException ue) {
	throw new jade.domain.FIPAAgentManagement.InternalError("The container is not reachable");
      }
      catch(NotFoundException nfe) {
	throw new NotRegistered();
      }
    }

  } // End of DebugAgentOffBehaviour class

  private class InstallMTPBehaviour extends AMSBehaviour {
    public InstallMTPBehaviour(ACLMessage msg) {
      super(msg);
    }
    public FipaRequestResponderBehaviour.ActionHandler create(ACLMessage msg) {
      return new InstallMTPBehaviour(msg);
    }

    protected void processAction(Action a) throws FIPAException {
      InstallMTP imtp = (InstallMTP)a.get_1();
      try {
	myPlatform.installMTP(imtp.getAddress(), imtp.getContainer(), imtp.getClassName());
	sendReply(ACLMessage.AGREE, createAgreeContent(a));
	sendReply(ACLMessage.INFORM, doneAction(a));
      }
      catch(NotFoundException nfe) {
	throw new jade.domain.FIPAAgentManagement.UnrecognisedParameterValue("MTP", nfe.getMessage());
      }
      catch(UnreachableException ue) {
	throw new jade.domain.FIPAAgentManagement.InternalError(ue.getMessage());
      }
      catch(MTPException mtpe) {
	throw new jade.domain.FIPAAgentManagement.UnrecognisedParameterValue("MTP", mtpe.getMessage());
      }
    }

  } // End of InstallMTPBehaviour class

  private class UninstallMTPBehaviour extends AMSBehaviour {
    public UninstallMTPBehaviour(ACLMessage msg) {
      super(msg);
    }
    public FipaRequestResponderBehaviour.ActionHandler create(ACLMessage msg) {
      return new UninstallMTPBehaviour(msg);
    }

    protected void processAction(Action a) throws FIPAException {
      UninstallMTP umtp = (UninstallMTP)a.get_1();
      try {
	myPlatform.uninstallMTP(umtp.getAddress(), umtp.getContainer());
	sendReply(ACLMessage.AGREE, createAgreeContent(a));
	sendReply(ACLMessage.INFORM, doneAction(a));
      }
      catch(NotFoundException nfe) {
	throw new jade.domain.FIPAAgentManagement.UnrecognisedParameterValue("MTP", nfe.getMessage());
      }
      catch(UnreachableException ue) {
	throw new jade.domain.FIPAAgentManagement.InternalError(ue.getMessage());
      }
      catch(MTPException mtpe) {
	throw new jade.domain.FIPAAgentManagement.UnrecognisedParameterValue("MTP", mtpe.getMessage());
      }
    }

  } // End of UninstallMTPBehaviour class



  // The AgentPlatform where information about agents is stored 
  /**
  @serial
  */
  private AgentManager myPlatform;

  // Maintains an association between action names and behaviours to
  // handle 'fipa-agent-management' actions
  /**
  @serial
  */
  private FipaRequestResponderBehaviour dispatcher;

  // Maintains an association between action names and behaviours to
  // handle 'jade-agent-management' actions
  /**
  @serial
  */
  private FipaRequestResponderBehaviour extensionsDispatcher;

  // Contains a main Behaviour and some utilities to handle JADE mobility
  /**
  @serial
  */
  private MobilityManager mobilityMgr;

  // Behaviour to listen to incoming 'subscribe' messages from tools.
  /**
  @serial
  */
  private RegisterToolBehaviour registerTool;

  // Behaviour to broadcats AgentPlatform notifications to each
  // registered tool.
  /**
  @serial
  */
  private NotifyToolsBehaviour notifyTools;

  // Behaviour to listen to incoming 'cancel' messages from tools.
  /**
  @serial
  */
  private DeregisterToolBehaviour deregisterTool;

  // Group of tools registered with this AMS
  /**
  @serial
  */
  private List tools;

  // ACL Message to use for tool notification
  /**
  @serial
  */
  private ACLMessage toolNotification = new ACLMessage(ACLMessage.INFORM);

  // Buffer for AgentPlatform notifications
  /**
  @serial
  */
  private List eventQueue = new ArrayList(10);

  /**
  @serial
  */
  private Map pendingInforms = new HashMap();

  /**
  @serial
  */
  private APDescription theProfile = new APDescription();

  /**
     This constructor creates a new <em>AMS</em> agent. Since a direct
     reference to an Agent Platform implementation must be passed to
     it, this constructor cannot be called from application
     code. Therefore, no other <em>AMS</em> agent can be created
     beyond the default one.
  */
  public ams(AgentManager ap) {

    // Fill Agent Platform Profile with data.
    theProfile.setDynamic(new Boolean(false));
    theProfile.setMobility(new Boolean(false));
    APTransportDescription mtps = new APTransportDescription();
    theProfile.setTransportProfile(mtps);

    myPlatform = ap;
    myPlatform.addListener(this);

    MessageTemplate mtFIPA = 
      MessageTemplate.and(MessageTemplate.MatchLanguage(SL0Codec.NAME),
			  MessageTemplate.MatchOntology(FIPAAgentManagementOntology.NAME));
    dispatcher = new FipaRequestResponderBehaviour(this, mtFIPA);

    MessageTemplate mtJADE = 
      MessageTemplate.and(MessageTemplate.MatchLanguage(SL0Codec.NAME),
			  MessageTemplate.MatchOntology(JADEAgentManagementOntology.NAME));
    extensionsDispatcher = new FipaRequestResponderBehaviour(this, mtJADE);

    mobilityMgr = new MobilityManager(this);
    registerTool = new RegisterToolBehaviour();
    deregisterTool = new DeregisterToolBehaviour();
    notifyTools = new NotifyToolsBehaviour();

    tools = new ArrayList();

    toolNotification.setSender(new AID());
    toolNotification.setLanguage(SL0Codec.NAME);
    toolNotification.setOntology(JADEIntrospectionOntology.NAME);
    toolNotification.setInReplyTo("tool-subscription");

    // Associate each AMS action name with the behaviour to execute
    // when the action is requested in a 'request' ACL message

    dispatcher.registerFactory(FIPAAgentManagementOntology.REGISTER, new RegBehaviour(null));
    dispatcher.registerFactory(FIPAAgentManagementOntology.DEREGISTER, new DeregBehaviour(null));
    dispatcher.registerFactory(FIPAAgentManagementOntology.MODIFY, new ModBehaviour(null));
    dispatcher.registerFactory(FIPAAgentManagementOntology.SEARCH, new SrchBehaviour(null));

    dispatcher.registerFactory(FIPAAgentManagementOntology.GETDESCRIPTION, new GetDescriptionBehaviour(null));

    extensionsDispatcher.registerFactory(JADEAgentManagementOntology.CREATEAGENT, new CreateBehaviour(null));
    extensionsDispatcher.registerFactory(JADEAgentManagementOntology.KILLAGENT, new KillBehaviour(null));
    extensionsDispatcher.registerFactory(JADEAgentManagementOntology.KILLCONTAINER, new KillContainerBehaviour(null));
    extensionsDispatcher.registerFactory(JADEAgentManagementOntology.SNIFFON, new SniffAgentOnBehaviour(null));
    extensionsDispatcher.registerFactory(JADEAgentManagementOntology.DEBUGOFF, new DebugAgentOffBehaviour(null));
    extensionsDispatcher.registerFactory(JADEAgentManagementOntology.DEBUGON, new DebugAgentOnBehaviour(null));
    extensionsDispatcher.registerFactory(JADEAgentManagementOntology.SNIFFOFF, new SniffAgentOffBehaviour(null));
    extensionsDispatcher.registerFactory(JADEAgentManagementOntology.INSTALLMTP, new InstallMTPBehaviour(null));
    extensionsDispatcher.registerFactory(JADEAgentManagementOntology.UNINSTALLMTP, new UninstallMTPBehaviour(null));

  }

  /**
   This method starts the <em>AMS</em> behaviours to allow the agent
   to carry on its duties within <em><b>JADE</b></em> agent platform.
  */
  protected void setup() {

    // Fill the ':name' slot of the Agent Platform Profile with the Platform ID.
    theProfile.setName("\"" + getHap() + "\"");
    writeAPDescription();

    // Register the supported ontologies 
    registerOntology(FIPAAgentManagementOntology.NAME, FIPAAgentManagementOntology.instance());
    registerOntology(JADEAgentManagementOntology.NAME, JADEAgentManagementOntology.instance());
    registerOntology(JADEIntrospectionOntology.NAME, JADEIntrospectionOntology.instance());
    registerOntology(MobilityOntology.NAME, MobilityOntology.instance());

    // register the supported languages
    registerLanguage(SL0Codec.NAME, new SL0Codec());	

    // Add a dispatcher Behaviour for all ams actions following from a
    // 'fipa-request' interaction with 'fipa-agent-management' ontology.
    addBehaviour(dispatcher);

    // Add a dispatcher Behaviour for all ams actions following from a
    // 'fipa-request' interaction with 'jade-agent-management' ontology.
    addBehaviour(extensionsDispatcher);

    // Add a main behaviour to manage mobility related messages
    addBehaviour(mobilityMgr.getMain());

    // Add a Behaviour to accept incoming tool registrations and a
    // Behaviour to broadcast events to registered tools.
    addBehaviour(registerTool);
    addBehaviour(deregisterTool);
    addBehaviour(notifyTools);

  }

  
  /**
  * checks that all the mandatory slots for a register/modify/deregister action
  * are present.
  * @param actionName is the name of the action (one of 
  * <code>FIPAAgentManagementOntology.REGISTER</code>,
  * <code>FIPAAgentManagementOntology.MODIFY</code>,
  * <code>FIPAAgentManagementOntology.DEREGISTER</code>)
  * @param amsd is the AMSAgentDescription to be checked for
  * @throws MissingParameter if one of the mandatory slots is missing
  **/
  private void checkMandatorySlots(String actionName, AMSAgentDescription amsd) throws MissingParameter {
    try {
      AID name = amsd.getName();
      if ((name == null)||(name.getName().length() == 0))
	throw new MissingParameter(FIPAAgentManagementOntology.AMSAGENTDESCRIPTION, "name");
    } catch (Exception e) {
      e.printStackTrace();
      throw new MissingParameter(FIPAAgentManagementOntology.AMSAGENTDESCRIPTION, "name");
    }
    if (!actionName.equalsIgnoreCase(FIPAAgentManagementOntology.DEREGISTER))
      try {
	String state = amsd.getState();
	if((state == null)||(state.length() == 0))
	  throw new MissingParameter(FIPAAgentManagementOntology.AMSAGENTDESCRIPTION, "state");
      } catch (Exception e) {
	e.printStackTrace();
	throw new MissingParameter(FIPAAgentManagementOntology.AMSAGENTDESCRIPTION, "state");
      }
  }
  
  /**
     @serial
   */
  private KB agentDescriptions = new KBAbstractImpl() {
      protected boolean match(Object template, Object fact) {
	try {
	  AMSAgentDescription templateDesc = (AMSAgentDescription)template;
	  AMSAgentDescription factDesc = (AMSAgentDescription)fact;

	  String o1 = templateDesc.getOwnership();
	  if(o1 != null) {
	    String o2 = factDesc.getOwnership();
	    if((o2 == null) || (!o1.equalsIgnoreCase(o2)))
	      return false;
	  }

	  String s1 = templateDesc.getState();
	  if(s1 != null) {
	    String s2 = factDesc.getState();
	    if((s2 == null) || (!s1.equalsIgnoreCase(s2)))
	      return false;
	  }

	  AID id1 = templateDesc.getName();
	  if(id1 != null) {
	    AID id2 = factDesc.getName();
	    if((id2 == null) || (!matchAID(id1, id2)))
	      return false;
	  }

	  return true;
	}
	catch(ClassCastException cce) {
	  return false;
	}
      }
    };

  /** it is called also by Agent.java **/
  public void AMSRegister(AMSAgentDescription amsd) throws FIPAException {
    checkMandatorySlots(FIPAAgentManagementOntology.REGISTER, amsd);
    String[] addresses = myPlatform.platformAddresses();
    AID id = amsd.getName();
    for(int i = 0; i < addresses.length; i++)
      id.addAddresses(addresses[i]);
    Object old = agentDescriptions.register(amsd.getName(), amsd);
    if(old != null)
      throw new AlreadyRegistered();
  }

  /** it is called also by Agent.java **/
  public void AMSDeregister(AMSAgentDescription amsd) throws FIPAException {
    checkMandatorySlots(FIPAAgentManagementOntology.DEREGISTER, amsd);
    Object old = agentDescriptions.deregister(amsd.getName());
    if(old == null)
      throw new NotRegistered();
  }

  private void AMSModify(AMSAgentDescription amsd) throws FIPAException {
    checkMandatorySlots(FIPAAgentManagementOntology.MODIFY, amsd);
    Object old = agentDescriptions.deregister(amsd.getName());
    if(old == null)
      throw new NotRegistered();
    agentDescriptions.register(amsd.getName(), amsd);
  }

  private List AMSSearch(AMSAgentDescription amsd, SearchConstraints constraints, ACLMessage reply) throws FIPAException {
    // Search has no mandatory slots
    return agentDescriptions.search(amsd);
  }

  // This one is called in response to a 'move-agent' action
  void AMSMoveAgent(AID agentID, Location where) throws FIPAException {
    try {
      myPlatform.move(agentID, where, "");
    }
    catch(UnreachableException ue) {
      throw new jade.domain.FIPAAgentManagement.InternalError("The container is not reachable");
    }
    catch(NotFoundException nfe) {
      throw new NotRegistered();
    }
  }

  // This one is called in response to a 'clone-agent' action
  void AMSCloneAgent(AID agentID, Location where, String newName) throws FIPAException {
    try {
      myPlatform.copy(agentID, where, newName, "");
    }
    catch(UnreachableException ue) {
      throw new jade.domain.FIPAAgentManagement.InternalError("The container is not reachable");
    }
    catch(NotFoundException nfe) {
      throw new NotRegistered();
    }
  }


  // This one is called in response to a 'where-is-agent' action
  Location AMSWhereIsAgent(AID agentID) throws FIPAException {
    try {
      ContainerID cid = myPlatform.getContainerID(agentID);
      String containerName = cid.getName();
      return mobilityMgr.getLocation(containerName);
    }
    catch(NotFoundException nfe) {
      nfe.printStackTrace();
      throw new NotRegistered();
    }
  }

  // This one is called in response to a 'query-platform-locations' action
  Iterator AMSGetPlatformLocations() {
    return mobilityMgr.getLocations();
  }


  // Methods to be called from AgentPlatform to notify AMS of special events

  /**
    Post an event to the AMS agent. This method must not be used by
    application agents.
  */
  public synchronized void addedContainer(PlatformEvent ev) {

    ContainerID cid = ev.getContainer();
    String name = cid.getName();

    // Add a new location to the locations list
    mobilityMgr.addLocation(name, cid);

    // Fire an 'added container' event
    AddedContainer ac = new AddedContainer();
    ac.setContainer(cid);

    EventRecord er = new EventRecord(ac, here());
    er.setWhen(ev.getTime());
    eventQueue.add(er);
    doWake();
  }

  /**
    Post an event to the AMS agent. This method must not be used by
    application agents.
  */
  public synchronized void removedContainer(PlatformEvent ev) {
    ContainerID cid = ev.getContainer();
    String name = cid.getName();

    // Remove the location from the location list
    mobilityMgr.removeLocation(name);

    // Fire a 'container is dead' event
    RemovedContainer rc = new RemovedContainer();
    rc.setContainer(cid);

    EventRecord er = new EventRecord(rc, here());
    er.setWhen(ev.getTime());
    eventQueue.add(er);
    doWake();
  }

  /**
    Post an event to the AMS agent. This method must not be used by
    application agents.
  */
  public synchronized void bornAgent(PlatformEvent ev) {
    ContainerID cid = ev.getContainer();
    AID agentID = ev.getAgent();

    BornAgent ba = new BornAgent();
    ba.setAgent(agentID);
    ba.setWhere(cid);

    EventRecord er = new EventRecord(ba, here());
    er.setWhen(ev.getTime());
    eventQueue.add(er);
    doWake();
  }

  /**
    Post an event to the AMS agent. This method must not be used by
    application agents.
  */
  public synchronized void deadAgent(PlatformEvent ev) {
    ContainerID cid = ev.getContainer();
    AID agentID = ev.getAgent();

    // Deregister the agent, if it's still there.
    try {
      AMSAgentDescription amsd = new AMSAgentDescription();
      amsd.setName(agentID);
      List l = AMSSearch(amsd, null, null);
      if(!l.isEmpty())
	AMSDeregister(amsd);
    }
    catch(FIPAException fe) {
      fe.printStackTrace();
    }

    DeadAgent da = new DeadAgent();
    da.setAgent(agentID);
    da.setWhere(cid);

    EventRecord er = new EventRecord(da, here());
    er.setWhen(ev.getTime());
    eventQueue.add(er);
    doWake();
  }

  /**
    Post an event to the AMS agent. This method must not be used by
    application agents.
  */
  public synchronized void movedAgent(PlatformEvent ev) {
    ContainerID from = ev.getContainer();
    ContainerID to = ev.getNewContainer();
    AID agentID = ev.getAgent();

    MovedAgent ma = new MovedAgent();
    ma.setAgent(agentID);
    ma.setFrom(from);
    ma.setTo(to);

    EventRecord er = new EventRecord(ma, here());
    er.setWhen(ev.getTime());
    eventQueue.add(er);
    doWake();
  }

  /**
    Post an event to the AMS agent. This method must not be used by
    application agents.
  */
  public synchronized void addedMTP(MTPEvent ev) {

    Channel ch = ev.getChannel();
    ContainerID cid = ev.getPlace();
    String address = ch.getAddress();

    // Add the new address to the platform profile
    APTransportDescription mtps = theProfile.getTransportProfile();
    MTPDescription desc = new MTPDescription();
    int colonPos = address.indexOf(':');
    if(colonPos != -1)
      desc.setMtpName(address.substring(0, colonPos));
    desc.addAddresses(address);
    mtps.addAvailableMtps(desc);
 
    //Update the APDescription file.
    if(getState() != AP_INITIATED)
    	writeAPDescription();

    // Retrieve all agent descriptors
    AMSAgentDescription amsd = new AMSAgentDescription();
    List l = agentDescriptions.search(amsd);

    // Add the new address to all the agent descriptors
    Iterator it = l.iterator();
    while(it.hasNext()) {
      AMSAgentDescription ad = (AMSAgentDescription)it.next();
      AID name = ad.getName();
      name.addAddresses(address);
    }

    /*
    //Notify the update of the APDescription...
    PlatformDescription ap = new PlatformDescription();
    ap.setPlatform(theProfile);
    eventQueue.add(ap);
    */

    // Generate a suitable AMS event
    AddedMTP amtp = new AddedMTP();
    amtp.setAddress(address);
    amtp.setWhere(cid);

    EventRecord er = new EventRecord(amtp, here());
    er.setWhen(ev.getTime());
    eventQueue.add(er);
    doWake();

  }

  /**
    Post an event to the AMS agent. This method must not be used by
    application agents.
  */
  public synchronized void removedMTP(MTPEvent ev) {

    Channel ch = ev.getChannel();
    ContainerID cid = ev.getPlace();
    String address = ch.getAddress();

    // Remove the dead address from the platform profile
    APTransportDescription mtps = theProfile.getTransportProfile();
    Iterator it = mtps.getAllAvailableMtps();
    while(it.hasNext()) {
      MTPDescription desc = (MTPDescription)it.next();
      Iterator addresses = desc.getAllAddresses();
      while(addresses.hasNext()) {
	// Remove all MTPs that have the 'address' String in their
	// address list.
	String nextAddr = (String)addresses.next();
	if(nextAddr.equalsIgnoreCase(address))
	  it.remove();
      }
    }
    
    //update the APDescription file
    writeAPDescription();

    // Remove the dead address from all the registered agents
    AID[] agents = myPlatform.agentNames();
    AMSAgentDescription amsd = new AMSAgentDescription();
    for(int i = 0; i < agents.length; i++) {
      amsd.setName(agents[i]);
      List l = agentDescriptions.search(amsd);
      AMSAgentDescription desc = (AMSAgentDescription)l.get(0);
      AID name = desc.getName();
      name.removeAddresses(address);
    }

    // Generate a suitable AMS event
    RemovedMTP rmtp = new RemovedMTP();
    rmtp.setAddress(address);
    rmtp.setWhere(cid);

    EventRecord er = new EventRecord(rmtp, here());
    er.setWhen(ev.getTime());
    eventQueue.add(er);
    doWake();

  }

  public void messageIn(MTPEvent ev) { System.out.println("Message In."); }
  public void messageOut(MTPEvent ev) { System.out.println("Message Out."); }



 private void writeAPDescription()
  {
  	 //Write the APDescription file.
    try{
    	FileWriter f = new FileWriter("APDescription.txt");
    
    	theProfile.toText(f);
	  	//f.write(s, 0, s.length());
	  	f.write('\n');
    	f.close();
    }catch(java.io.IOException ioe){ioe.printStackTrace();}
    

  }


} // End of class ams
