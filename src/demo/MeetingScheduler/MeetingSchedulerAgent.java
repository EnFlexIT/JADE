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

import java.util.Vector;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Date;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;

import jade.core.Agent;
import jade.core.AgentGroup;
import jade.lang.acl.ACLMessage;

import jade.domain.AgentManagementOntology;

/**
Javadoc documentation for the file
@author Fabio Bellifemine - CSELT S.p.A
@version $Date$ $Revision$
*/
public class MeetingSchedulerAgent extends Agent {

    String user; // this is the name of the current user
    Vector knownDF = new Vector(); // list of known DF with which the agent is registered
    mainFrame mf; // pointer to the main frame of the GUI
    Hashtable knownPersons = new Hashtable(10, 0.8f); // list of known persons: (String)name -> Person
    Hashtable appointments = new Hashtable(10, 0.8f); // list of appointments:  (String)date -> Appointment
    final static String REPLYBY = "+00000000T000059000Z"; // 59 seconds
    
protected void setup() {
  (new PasswordDialog(this, "Enter your name and password")).setVisible(true);
  addBehaviour(new myFipaContractNetResponderBehaviour(this));
  addBehaviour(new CancelAppointmentBehaviour(this));
}


protected void searchPerson(String dfname, String personName) {
  AgentManagementOntology.ServiceDescriptor sd = new AgentManagementOntology.ServiceDescriptor();
  sd.setType("personal-agent");
  if (personName != null)
    sd.setFixedProps("(represents " + personName + ")");
  AgentManagementOntology.DFAgentDescriptor dfd = new AgentManagementOntology.DFAgentDescriptor();
  dfd.setOntology("pa-ontology");
  //dfd.addAgentService(sd);
  //dfd.setDFState("active");
  //dfd.setOwnership(user);
  try {
    //dfd.toText(new BufferedWriter(new OutputStreamWriter(System.out)));
    AgentManagementOntology.DFSearchResult result;
    Vector vc = new Vector(1);
    AgentManagementOntology.Constraint c = new AgentManagementOntology.Constraint();
    c.setName(AgentManagementOntology.Constraint.DFDEPTH);
    c.setFn(AgentManagementOntology.Constraint.MAX); // MIN
    c.setArg(3);
    vc.addElement(c);
    result = searchDF(dfname,dfd,vc);
    //System.err.println("\nSearch DF results:");
    //result.toText(new BufferedWriter(new OutputStreamWriter(System.out)));
    
    // add values to knownPersons
    Enumeration e = result.elements();
    while (e.hasMoreElements()) {
      dfd = (AgentManagementOntology.DFAgentDescriptor)e.nextElement();
      Person p = new Person(dfd.getOwnership(),dfd,dfname); 
      addKnownPerson(p);
    }
  } catch (jade.domain.FIPAException fe) {
    fe.printStackTrace();
    mf.showErrorMessage(fe.getMessage());
  }
}

protected void DFregistration(String dfname) {
    AgentManagementOntology.ServiceDescriptor sd = new AgentManagementOntology.ServiceDescriptor();
    sd.setName("AppointmentScheduling");
    sd.setType("personal-agent");
    sd.setOntology("pa-ontology");
    sd.setFixedProps("(represents " + user + ")");
    AgentManagementOntology.DFAgentDescriptor dfd = new AgentManagementOntology.DFAgentDescriptor();
    dfd.setName(getName());
    dfd.addAddress(getAddress());
    dfd.addAgentService(sd);
    dfd.setType("fipa-agent");
    dfd.setOntology("pa-ontology");
    dfd.addInteractionProtocol("fipa-request fipa-Contract-Net");
    dfd.setDFState("active");
    dfd.setOwnership(user);
    try {
      //dfd.toText(new BufferedWriter(new OutputStreamWriter(System.out)));
      registerWithDF(dfname,dfd);
      knownDF.addElement(dfname);
      Person p = new Person(user,dfd,dfname); 
      addKnownPerson(p);
    } catch (jade.domain.FIPAException fe) {
      fe.printStackTrace();
      mf.showErrorMessage(fe.getMessage());
    }
}

/**
* This method returns an Enumeration of String.
* Each String is the name of a DF with which the agent has been registered
*/
protected Enumeration getKnownDF() {
  return knownDF.elements();
}

/** 
* This method returns an Enumeration of objects.
* The object type is Person
*/
protected Enumeration getKnownPersons() {
    return knownPersons.elements();
}

protected void addKnownPerson(Person p) {
    knownPersons.put(p.getName(),p);
    mf.showErrorMessage("Known " + p.getName());
}

protected Person getPerson(String name) {
  return (Person)knownPersons.get(name);
}

protected Person getPersonbyAgentName(String agentname) {
  //System.err.println("getPersonbyAgentName: "+agentname);
  Enumeration e = knownPersons.elements();
  Person p;
  while (e.hasMoreElements()) {
    p = (Person)e.nextElement();
    //System.err.println(p.toString());
    if (p.getAgentName().startsWith(agentname)) 
      return p;
  }
  return null;
}

protected void fixAppointment(Appointment a) {
  System.err.println("fix Appointment" + a.toString());
    
    ACLMessage cfp = new ACLMessage("cfp");
    //FIXME only for Seoul 
    // cfp.setContent(a.toString());
    cfp.setSource(getName());
    cfp.setReplyBy(REPLYBY);
    AgentGroup ag = new AgentGroup();
    Enumeration e = a.getInvitedPersons();
    int numberOfInvited = 0;
    String name;
    String listOfNames = "";
    while (e.hasMoreElements()) {
      numberOfInvited++;
      name = ((Person)e.nextElement()).getAgentName();
      //if (name.endsWith(getAddress())) 
	/* it belongs to my platform. so remove platform name otherwise 
	   fipacontractnetinitiatorbheaviour does not work */
      //ag.addMember(name.substring(0,name.indexOf('@')));
      //else 
      ag.addMember(name);
      listOfNames = listOfNames + name + " ";
    }
    
    if (numberOfInvited == 0) { // ci sono solo io
      // devo solo trovare un giorno libero
      Date goodDate = findADate(a);
      if (goodDate == null) 
	    mf.showErrorMessage("No free date for "+a.toString());
      else {
    	a.setFixedDate(goodDate);
    	addAppointment(a);
      }
    } else {
        if (numberOfInvited > 1) 
            listOfNames = "(" + listOfNames + ")"; // becomes a list
        String possibleAppList = possibleAppointments(a);
	if (possibleAppList != null) {
	  cfp.setContent("( (action " + " * " + " (possible-appointments " + possibleAppList + ")) true )");   
	  addBehaviour(new myFipaContractNetInitiatorBehaviour(this,cfp,ag)); 
	} else mf.showErrorMessage("No free date for "+a.toString());
    }
}


/**
* This function returns a string representing the list of available days for this
* appointment
* @param a the Appointment
* @return a String representing the list of valid dates, e.g. (list 21 22),
* returns null if no date is available
*/
//FIXME it is using FIPA Seoul syntax. May need changes
String possibleAppointments(Appointment a) {
    String str = "(list ";
    boolean nodatepossible = true;
    Date ds = a.getStartingOn(); 
    Date de = a.getEndingWith();
    while (! ds.after(de)) {     
      if (! appointments.containsKey(key(ds))) {
	nodatepossible = false;
        str = str+ds.getDate() + " "; // mette solo il giorno tra 1 e 31
      }
     ds.setTime(ds.getTime()+(24*60*60*1000)); // + 1 day    
    }
    if (nodatepossible) return null;
    else return str + ")";
}

  /**
   * @param a an Appointment
   * @return a good Date for that Appointment
   */
  Date findADate(Appointment a) {
    Date ds = a.getStartingOn(); 
    if (appointments.containsKey(key(ds))) {
     ds.setTime(ds.getTime()+(24*60*60*1000)); // + 1 day    
     Date de = a.getEndingWith();

     while (! ds.after(de)) { 
       if (appointments.containsKey(key(ds))) { 
             ds.setTime(ds.getTime()+(24*60*60*1000)); // + 1 day    
       } else return ds;
     } // end of while
     return null;
    }
    return ds;
    }
  

protected void cancelAppointment(Date date) {
    Appointment a = (Appointment)appointments.get(key(date));
    //System.err.println("Cancel Appointment: " + a.toString());
    Enumeration e = a.getInvitedPersons();
    int numberOfInvited = 0;
    String name;
    ACLMessage cancel = new ACLMessage("cancel");
    cancel.setSource(getName());
    while (e.hasMoreElements()) {
      name = ((Person)e.nextElement()).getAgentName();
      cancel.setDest(name);
      //FIXME only for Seoul 
      // cfp.setContent(a.toString());
      cancel.setContent("(action " + name + " (possible-appointments (list " + (a.getDate()).getDate() + ")))");   
      cancel.dump();
      send(cancel);
    }
    removeAppointment(date);
}


public void removeAppointment(Date date) {
  Appointment a = getAppointment(date);
  if (a == null) {
    mf.showErrorMessage("Someone has requested to cancel an appointment for " + date.toString()+" but there was no appointment actually");
  } else {
    appointments.remove(key(date));
    mf.calendar1_Action(null);
    mf.showErrorMessage("Cancelled Appointment: "+ a.toString());
  }
}

protected void addAppointment(Appointment a) {
  //System.err.println("addAppointment: "+a.toString());
    appointments.put(key(a.getDate()),a);
    mf.calendar1_Action(null);	
    mf.showErrorMessage(a.toString());
}

/**
* This function return the key to be used in the Hashtable
*/
private String key(Date d) {
    return ""+d.getYear()+d.getMonth()+d.getDate();
}
protected Appointment getAppointment(Date date) {
    return (Appointment)appointments.get(key(date));
}

protected void setUser(String username) {
    user = username;
    mf = new mainFrame(this,user + " - Appointment Scheduler" );
    DFregistration("df");  // register with the default DF
    mf.setVisible(true);
}

protected String getUser() {
    return user;
}



} // end Agent.java

