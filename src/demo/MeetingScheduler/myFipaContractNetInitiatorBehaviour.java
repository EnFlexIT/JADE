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

import java.util.*;

import jade.lang.acl.ACLMessage;

import jade.core.*;

import jade.proto.FipaContractNetInitiatorBehaviour;


/**
Javadoc documentation for the file
@author Fabio Bellifemine - CSELT S.p.A
@version $Date$ $Revision$
*/

public class myFipaContractNetInitiatorBehaviour extends FipaContractNetInitiatorBehaviour {

    public myFipaContractNetInitiatorBehaviour(Agent a, ACLMessage msg, List group) {
      super(a,msg,group);
      //System.err.println("myFipaContractNetInitiatorBehaviour with these agents: " + group.toString());
    }

  // FIXME This method is called to handle message different from proposal
  public void handleOtherMessages(ACLMessage msg) {
    System.err.println("!!! FipaContractNetInitiator handleOtherMessages: "+msg.toString());
  }
  

public String createCfpContent(String basicContent, AID receiver) {
  int p = basicContent.indexOf('*');
  if (p != -1) 
    //replace with the actual actor name that is 1 actor for each message.
    return basicContent.substring(0,p-1) + receiver.getName() + basicContent.substring(p+1,basicContent.length());
  else
    return basicContent;
}  
  
  Appointment pendingAppointment;

public Vector handleProposeMessages(Vector proposals) {
  System.err.println(myAgent.getLocalName()+": FipacontractNetInitiator is evaluating the proposals");
  Vector retMsgs = new Vector(proposals.size());
  ACLMessage msg;
  Vector acceptableDates = new Vector();
  Vector acceptedDates;
  SL0Parser parser;
  MultiValue mv;
  Action a;
  Proposition  possApp;
  
  if (proposals.size()==0)
    return null;
  
  try {
    parser = SL0Parser.create();
    mv = (MultiValue)parser.parse(new StringReader(cfpMsg.getContent()), ACLMessage.getPerformative(cfpMsg.getPerformative()));
    a = (Action)mv.getValue(0);
    possApp = (Proposition)a.getActionParameter("list");
    if ( (a==null) || (possApp == null) ) {
      //fixme send not understood
    } else {
      for (int i=0; i<possApp.getNumberOfTerms(); i++) {
	int dateNumber = Integer.parseInt(possApp.getTerm(i).toString());
	Date d = new Date();
	if ((dateNumber > 0) && (dateNumber < 32)) { // if is a valid day
	  d.setDate(dateNumber);
	  if ((Appointment)((MeetingSchedulerAgent)myAgent).getAppointment(d) == null) // is free
	    acceptableDates.addElement(new Integer(dateNumber)); 
	}
      }
    }
  } catch (demo.MeetingScheduler.CLP.ParseException e) {
    e.printStackTrace();
    finished = true;
    return null;
  }
  // now acceptableDates contains all the dates when I have no appointment and were in the cfp
    
  for (int i=0; i<proposals.size(); i++) {
    //System.err.println("EvaluateProposals, start round "+i+" acceptableDates = "+acceptableDates.toString());
    msg = (ACLMessage)proposals.elementAt(i);
    if (msg.getPerformative() == ACLMessage.PROPOSE) {
      try {
	acceptedDates = new Vector();
	parser = SL0Parser.create();
	mv = (MultiValue)parser.parse(new StringReader(msg.getContent()), ACLMessage.getPerformative(msg.getPerformative()));
	a = (Action)mv.getValue(0);
	possApp = (Proposition)a.getActionParameter("list");
	for (int ii=0; ii<possApp.getNumberOfTerms(); ii++) {
	  int dateNumber = Integer.parseInt(possApp.getTerm(ii).toString());
	  if (acceptableDates.contains(new Integer(dateNumber)))
	    acceptedDates.addElement(new Integer(dateNumber));     
	}
	acceptableDates = acceptedDates;
	if (msg.getReplyWith() != null)
	  msg.setInReplyTo(msg.getReplyWith());
	msg.clearAllReceiver();
	msg.addReceiver(msg.getSender());
	retMsgs.addElement(msg);
      } catch (ParseException e) {
	e.printStackTrace();
      }
    } // end if "propose"
  } // end of for proposals.size()   
  //System.err.println("EvaluateProposals, end rounds acceptableDates = "+acceptableDates.toString());
  
  String content = "( (action "+ " * " +" (possible-appointments (list ";
  int msgType;
  if (acceptableDates.size() > 0) {
    pendingAppointment = new Appointment(myAgent.getName());
    Date d = new Date();
    int dateNumber = ((Integer)acceptableDates.elementAt(0)).intValue();
    d.setDate(dateNumber);
    pendingAppointment.setFixedDate(d);
    //FIXME I should here also add the invited Persons otherwise the cancel
    // does not work.
    content = content + acceptableDates.elementAt(0) + "))) true )";
    msgType = ACLMessage.ACCEPT_PROPOSAL;
  } else {
    content = content + "))) false )";
    msgType = ACLMessage.REJECT_PROPOSAL;
  }
  
  int p = content.indexOf('*'); // the actor name '*' must be replaced with 
  // the actual actor name (1 actor for each message)
  for (int i=0; i<retMsgs.size(); i++) {
    ((ACLMessage)retMsgs.elementAt(i)).setPerformative(msgType);
    ((ACLMessage)retMsgs.elementAt(i)).setContent(content.substring(0,p-1) + ((ACLMessage)retMsgs.elementAt(i)).getSender().getName() + content.substring(p+1,content.length())); // for each message replace '*' with the right actor name
  }
  
  return retMsgs;
}
  
public Vector handleFinalMessages(Vector messages) {
  // I here receive failure or inform-done
  ACLMessage msg;
  boolean accepted=false;
  Person p;
  for (int i=0; i<messages.size(); i++) {    
    msg = (ACLMessage)messages.elementAt(i);
    if (msg.getPerformative() == ACLMessage.INFORM) {
      accepted = true;
      p = ((MeetingSchedulerAgent)myAgent).getPersonbyAgentName(msg.getSender());
      if (p == null) 
	p = new Person(msg.getSender().getName());
      pendingAppointment.addInvitedPerson(p);
    }
  }
  if (accepted)
    ((MeetingSchedulerAgent)myAgent).addAppointment(pendingAppointment);            
  return new Vector();
  }
} 
