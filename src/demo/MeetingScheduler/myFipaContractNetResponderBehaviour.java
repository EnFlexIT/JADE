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
import java.util.Date;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.proto.FipaContractNetResponderBehaviour;
import jade.domain.FIPAException;


import demo.MeetingScheduler.Ontology.Appointment;

/**
Javadoc documentation for the file
@author Fabio Bellifemine - CSELT S.p.A
@version $Date$ $Revision$
*/
public class myFipaContractNetResponderBehaviour extends FipaContractNetResponderBehaviour {

MeetingSchedulerAgent myAgent;

public myFipaContractNetResponderBehaviour(MeetingSchedulerAgent a) {
super(a);
myAgent = a;
}

public void handleOtherMessages(ACLMessage msg) {
 System.err.println(myAgent.getLocalName()+":myFipaContractNetResponderBehaviour. handleOtherMessages:"+msg.toString());
}

 public ACLMessage handleAcceptProposalMessage(ACLMessage msg) {
   ACLMessage reply = msg.createReply();
   try {
     Appointment app = myAgent.extractAppointment(msg); 
     if (myAgent.isFree(app.getFixedDate())) {
       myAgent.addMyAppointment(app);
       reply.setPerformative(ACLMessage.INFORM);
     } else 
       reply.setPerformative(ACLMessage.FAILURE);
   } catch (FIPAException e) {
     e.printStackTrace();
     reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
     reply.setContent(e.getMessage());
   }
   return reply;
 }



 public void handleRejectProposalMessage(ACLMessage msg) {
    System.err.println(myAgent.getLocalName()+":FipaContractNetResponder: the proposal has been rejected with this message"+msg.toString());
 }


 public ACLMessage handleCfpMessage(ACLMessage cfp) {
   ACLMessage propose = cfp.createReply();

   try {
     Appointment app = myAgent.extractAppointment(cfp); 
     Appointment proposal = (Appointment)app.clone();
     proposal.clearAllPossibleDates();
     for (Iterator i=app.getAllPossibleDates(); i.hasNext(); ) {
       Date d = (Date)i.next();
       if (myAgent.isFree(d))
	 proposal.addPossibleDates(d);
     }

     if (proposal.getAllPossibleDates().hasNext()) {
       // there is at least one possible date that is ok for me
       propose.setPerformative(ACLMessage.PROPOSE);
       myAgent.fillAppointment(propose,proposal);
     } else {
       propose.setPerformative(ACLMessage.REFUSE);
       propose.setContent("( noavailabledate)");
     }
   } catch (FIPAException e) {
     e.printStackTrace();
     propose.setPerformative(ACLMessage.NOT_UNDERSTOOD);
     propose.setContent(e.getMessage());
   }
   return propose;
 }

}




