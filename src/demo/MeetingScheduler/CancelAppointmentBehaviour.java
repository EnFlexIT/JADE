package demo.MeetingScheduler;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import demo.MeetingScheduler.CLP.*;

import java.io.*;
import java.util.Date;

public class CancelAppointmentBehaviour extends CyclicBehaviour {

  MessageTemplate mt=MessageTemplate.MatchType("cancel");
  ACLMessage cancel; 
  SL0Parser parser;
  MultiValue mv;
  Action a;
  Proposition app;

  CancelAppointmentBehaviour(Agent a) {
    super(a);
  }

  public void action(){
    cancel = myAgent.receive(mt);
    if (cancel == null) {
      block();
      return;
    }
    System.err.println("CancelAppointmentBehaviour: received");
    cancel.dump();
    try {
      parser = SL0Parser.create();
      a = (Action)parser.parse(new StringReader(cancel.getContent()), cancel.getType());
      // a = (Action)mv.getValue(0);
      app = (Proposition)a.getActionParameter("list");
      for (int i=0; i<app.getNumberOfTerms(); i++) {
	int dateNumber = Integer.parseInt(app.getTerm(i).toString());
	Date d = new Date();
	if ((dateNumber > 0) && (dateNumber < 32)) { // if is a valid day
	  d.setDate(dateNumber);
	  Appointment appointment = ((MeetingSchedulerAgent)myAgent).getAppointment(d);
	  if (appointment != null) {
	    if (appointment.getInvitingAgent().equalsIgnoreCase(myAgent.getName())) 
	      // ho chiamato io l'appuntamento ed ora lo devo cancellare io
	      ((MeetingSchedulerAgent)myAgent).cancelAppointment(d);
	    ((MeetingSchedulerAgent)myAgent).removeAppointment(d);
	  }
	}
      }  // end of for
    }catch (demo.MeetingScheduler.CLP.ParseException e) {
      e.printStackTrace();
    }
  }
}


