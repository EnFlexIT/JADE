/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop multi-agent systems in compliance with the FIPA specifications.
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

package demo.MeetingScheduler;

import jade.util.leap.*;

import java.util.List;
import java.util.Vector;
import java.util.Date;
import jade.lang.acl.ACLMessage;
import jade.core.*;
import jade.proto.FipaContractNetInitiatorBehaviour;
import jade.domain.FIPAException;
import jade.lang.sl.SL0Codec;

import demo.MeetingScheduler.Ontology.*;

/**
Javadoc documentation for the file
@author Fabio Bellifemine - CSELT S.p.A
@version $Date$ $Revision$
*/

public class myFipaContractNetInitiatorBehaviour extends FipaContractNetInitiatorBehaviour {

  private ACLMessage cfpMsg = new ACLMessage(ACLMessage.CFP);
  private final static long TIMEOUT = 60000; // 1 minute
  private Appointment pendingApp;
  private MeetingSchedulerAgent myAgent;

    public myFipaContractNetInitiatorBehaviour(MeetingSchedulerAgent a, Appointment app, List group) {
      super(a,new ACLMessage(ACLMessage.CFP),group);
      myAgent = a;
      // fill the fields of the cfp message
      cfpMsg.setLanguage(SL0Codec.NAME);
      cfpMsg.setOntology(MSOntology.NAME);
      cfpMsg.setReplyByDate(new Date(System.currentTimeMillis()+TIMEOUT));

      try {// fill the content
	myAgent.fillAppointment(cfpMsg,app);
      } catch (FIPAException e) {
	e.printStackTrace();
	myAgent.doDelete();
      }
      pendingApp = (Appointment)app.clone();
      reset(cfpMsg,group); // updates the message to be sent
      //System.err.println("myFipaContractNetInitiatorBehaviour with these agents: " + group.toString());
    }

  // This method is called to handle message different from proposal
  public void handleOtherMessages(ACLMessage msg) {
    System.err.println("!!! FipaContractNetInitiator handleOtherMessages: "+msg.toString());
  }
  


public Vector handleProposeMessages(Vector proposals) {
  System.err.println(myAgent.getLocalName()+": FipacontractNetInitiator is evaluating the proposals");
  Vector retMsgs = new Vector(proposals.size());
  ACLMessage msg;
  ArrayList acceptableDates = new ArrayList();
  ArrayList acceptedDates = new ArrayList();
  
  if (proposals.size()==0)
    return null;

  for (Iterator i=pendingApp.getAllPossibleDates(); i.hasNext(); )
    acceptableDates.add(new Integer(((Date)i.next()).getDate()));

  for (int i=0; i<proposals.size(); i++) {
    //System.err.println("EvaluateProposals, start round "+i+" acceptableDates = "+acceptableDates.toString());
    msg = (ACLMessage)proposals.elementAt(i);
    if (msg.getPerformative() == ACLMessage.PROPOSE) {
      acceptedDates = new ArrayList();
      
      try {
	Appointment a = myAgent.extractAppointment(msg);
	for (Iterator ii=a.getAllPossibleDates(); ii.hasNext(); ) {
	  Integer day = new Integer(((Date)ii.next()).getDate());
	  if (acceptableDates.contains(day))
	    acceptedDates.add(day);
	}
	acceptableDates = (ArrayList)acceptedDates.clone();
	if (msg.getReplyWith() != null)
	  msg.setInReplyTo(msg.getReplyWith());
	msg.clearAllReceiver();
	msg.addReceiver(msg.getSender());
	msg.setSender(myAgent.getAID());
	retMsgs.addElement(msg);
      } catch (FIPAException e) {
	e.printStackTrace();
      }
    } // end if "propose"
  } // end of for proposals.size()   
  //System.err.println("EvaluateProposals, end rounds acceptableDates = "+acceptableDates.toString());

  ACLMessage replyMsg = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
  replyMsg.setLanguage(SL0Codec.NAME);
  replyMsg.setOntology(MSOntology.NAME);
  if (acceptableDates.size() > 0) {
    Date d = new Date();
    int dateNumber = ((Integer)acceptableDates.get(0)).intValue();
    d.setDate(dateNumber);
    pendingApp.setFixedDate(d);
    try {
      myAgent.fillAppointment(replyMsg,pendingApp);
    } catch (FIPAException e) {
      e.printStackTrace();
      myAgent.doDelete();
    }
  } else 
    replyMsg.setPerformative(ACLMessage.REJECT_PROPOSAL);
  
  for (int i=0; i<retMsgs.size(); i++) {
    ((ACLMessage)retMsgs.elementAt(i)).setPerformative(replyMsg.getPerformative());
    ((ACLMessage)retMsgs.elementAt(i)).setContent(replyMsg.getContent()); 
  }
  
  return retMsgs;
}
  
public Vector handleFinalMessages(Vector messages) {
  // I here receive failure or inform-done
  ACLMessage msg;
  boolean accepted=false;
  Person p;
  pendingApp.clearAllInvitedPersons();
  for (int i=0; i<messages.size(); i++) {    
    msg = (ACLMessage)messages.elementAt(i);
    if (msg.getPerformative() == ACLMessage.INFORM) {
      accepted = true;
      p = myAgent.getPersonbyAgentName(msg.getSender());
      if (p == null) 
	p = new Person(msg.getSender().getName(),null,null);
      pendingApp.addInvitedPersons(p);
    }
  }
  if (accepted)
    myAgent.addMyAppointment(pendingApp);            
  return new Vector();
  }
} 
