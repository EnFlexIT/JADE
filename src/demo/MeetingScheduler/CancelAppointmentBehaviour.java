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
import jade.core.behaviours.CyclicBehaviour;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import demo.MeetingScheduler.CLP.*;
import jade.domain.FIPAAgentManagement.AID;

import java.io.*;
import java.util.Date;

/**
Javadoc documentation for the file
@author Fabio Bellifemine - CSELT S.p.A
@version $Date$ $Revision$
*/
public class CancelAppointmentBehaviour extends CyclicBehaviour {

  MessageTemplate mt=MessageTemplate.MatchPerformative(ACLMessage.CANCEL);
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
    System.err.println("CancelAppointmentBehaviour: received "+cancel.toString());
    try {
      parser = SL0Parser.create();
      a = (Action)parser.parse(new StringReader(cancel.getContent()), ACLMessage.getPerformative(cancel.getPerformative()));
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


