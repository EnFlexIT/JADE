package demo.MeetingScheduler;
import java.util.Date;
import java.io.*;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.proto.FipaContractNetResponderBehaviour;
import demo.MeetingScheduler.CLP.*;

public class myFipaContractNetResponderBehaviour extends FipaContractNetResponderBehaviour {

public myFipaContractNetResponderBehaviour(Agent a) {
super(a);
}

public void handleOtherMessages(ACLMessage msg) {
 System.err.println(myAgent.getLocalName()+":myFipaContractNetResponderBehaviour. handleOtherMessages:");
 msg.dump();
}

 public ACLMessage handleAcceptProposalMessage(ACLMessage msg) {
    Person p;
    ACLMessage inform = new ACLMessage("inform");
    try {
        SL0Parser parser = SL0Parser.create();
        MultiValue mv = (MultiValue)parser.parse(new StringReader(msg.getContent()), msg.getType());
        Action a = (Action)mv.getValue(0);
        Proposition possApp = (Proposition)a.getActionParameter("list");
        
        int dateNumber = Integer.parseInt(possApp.getTerm(0).toString());
        Date d = new Date();
        if ((dateNumber > 0) && (dateNumber < 32)) { // if is a valid day
                d.setDate(dateNumber);
                if ((Appointment)((MeetingSchedulerAgent)myAgent).getAppointment(d) == null) {
                    // is free
                    Appointment app = new Appointment(msg.getSource());
                    app.setFixedDate(d);
		    p = ((MeetingSchedulerAgent)myAgent).getPersonbyAgentName(msg.getSource());
		    if (p == null) p = new Person(msg.getSource());
		    app.addInvitedPerson(p);
                    ((MeetingSchedulerAgent)myAgent).addAppointment(app);
                    inform.setContent("(done (action " +myAgent.getName() + 
                        " (possible-appointments (list " + dateNumber + "))))");
                } else {
                    inform.setType("failure");
                    inform.setContent("(action " + myAgent.getName() + 
                        " (possible-appointments (list " + dateNumber + "))) (busy-date))");
                }
        } else {
            inform.setType("failure");
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
    System.err.println(myAgent.getLocalName()+":FipaContractNetResponder: the proposal has been rejected with this message");
    msg.dump();
 }


 public ACLMessage handleCfpMessage(ACLMessage cfp) {
   boolean isacceptable = false;
   ACLMessage propose = new ACLMessage("propose");
   propose.setDest(cfp.getSource());
   try {
     SL0Parser parser = SL0Parser.create();
     MultiValue mv = (MultiValue)parser.parse(new StringReader(cfp.getContent()), cfp.getType());
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
       propose.setType("refuse");
       propose.setContent(proposeContent + "))) noavailabledate)");
     }
     return propose;
    }
    catch (demo.MeetingScheduler.CLP.ParseException pe) {
        pe.printStackTrace();
        propose.setType("not-understood");
        propose.setContent("(parser error)");
        myAgent.send(propose);
        return null;
      }
}

}




