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
import java.util.Date;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.proto.FipaContractNetResponderBehaviour;

/**
Javadoc documentation for the file
@author Fabio Bellifemine - CSELT S.p.A
@version $Date$ $Revision$
*/
public class myFipaContractNetResponderBehaviour extends FipaContractNetResponderBehaviour {

public myFipaContractNetResponderBehaviour(Agent a) {
super(a);
}

public void handleOtherMessages(ACLMessage msg) {
 System.err.println(myAgent.getLocalName()+":myFipaContractNetResponderBehaviour. handleOtherMessages:"+msg.toString());
}

 public ACLMessage handleAcceptProposalMessage(ACLMessage msg) {
    Person p;
    ACLMessage inform = new ACLMessage(ACLMessage.INFORM);
    try {
        SL0Parser parser = SL0Parser.create();
        MultiValue mv = (MultiValue)parser.parse(new StringReader(msg.getContent()), ACLMessage.getPerformative(msg.getPerformative()));
        Action a = (Action)mv.getValue(0);
        Proposition possApp = (Proposition)a.getActionParameter("list");
        
        int dateNumber = Integer.parseInt(possApp.getTerm(0).toString());
        Date d = new Date();
        if ((dateNumber > 0) && (dateNumber < 32)) { // if is a valid day
                d.setDate(dateNumber);
                if ((Appointment)((MeetingSchedulerAgent)myAgent).getAppointment(d) == null) {
                    // is free
                    Appointment app = new Appointment(msg.getSender().getName());
                    app.setFixedDate(d);
		    p = ((MeetingSchedulerAgent)myAgent).getPersonbyAgentName(msg.getSender());
		    if (p == null) p = new Person(msg.getSender().getName());
		    app.addInvitedPerson(p);
                    ((MeetingSchedulerAgent)myAgent).addAppointment(app);
                    inform.setContent("(done (action " +myAgent.getName() + 
                        " (possible-appointments (list " + dateNumber + "))))");
                } else {
                    inform.setPerformative(ACLMessage.FAILURE);
                    inform.setContent("(action " + myAgent.getName() + 
                        " (possible-appointments (list " + dateNumber + "))) (busy-date))");
                }
        } else {
	  inform.setPerformative(ACLMessage.FAILURE);
	  inform.setContent("(action " + myAgent.getName() + 
                        " (possible-appointments (list" + dateNumber + "))) (invalid-date))");
        }
    } catch (ParseException e) {
        e.printStackTrace();
        finished=true;
    }
    return inform;
 }



 public void handleRejectProposalMessage(ACLMessage msg) {
    System.err.println(myAgent.getLocalName()+":FipaContractNetResponder: the proposal has been rejected with this message"+msg.toString());
 }


 public ACLMessage handleCfpMessage(ACLMessage cfp) {
   boolean isacceptable = false;
   ACLMessage propose = new ACLMessage(ACLMessage.PROPOSE);
   propose.addReceiver(cfp.getSender());
   try {
     SL0Parser parser = SL0Parser.create();
     MultiValue mv = (MultiValue)parser.parse(new StringReader(cfp.getContent()), ACLMessage.getPerformative(cfp.getPerformative()));
     Action a = (Action)mv.getValue(0);
     Proposition possApp = (Proposition)a.getActionParameter("list");
     String proposeContent = "( (action "+myAgent.getName()+" (possible-appointments (list ";
     for (int i=0; i<possApp.getNumberOfTerms(); i++) {
       int dateNumber = Integer.parseInt(possApp.getTerm(i).toString());
       Date d = new Date();
       if ((dateNumber > 0) && (dateNumber < 32)) { // if is a valid day
	 d.setDate(dateNumber);
	 if ((Appointment)((MeetingSchedulerAgent)myAgent).getAppointment(d) == null) { // is free
	   proposeContent = proposeContent + dateNumber + " ";
	   isacceptable = true;
	 }
       }
     }
     if (isacceptable)
       propose.setContent(proposeContent + "))) true)");
     else {
       propose.setPerformative(ACLMessage.REFUSE);
       propose.setContent(proposeContent + "))) noavailabledate)");
     }
     return propose;
    }
    catch (demo.MeetingScheduler.CLP.ParseException pe) {
        pe.printStackTrace();
        propose.setPerformative(ACLMessage.NOT_UNDERSTOOD);
        propose.setContent("(parser error)");
        myAgent.send(propose);
        return null;
      }
}

}




