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

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;

import jade.core.Agent;
import jade.core.AgentGroup;
import jade.lang.acl.ACLMessage;

import jade.domain.FIPAAgentManagement.*;

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
    final static long REPLYBY = 59000; // 59 seconds
    
protected void setup() {
  (new PasswordDialog(this, "Enter your name and password")).setVisible(true);
}

  void startTasks(){
    addBehaviour(new myFipaContractNetResponderBehaviour(this));
    addBehaviour(new CancelAppointmentBehaviour(this));
  }


protected void searchPerson(AID dfname, String personName) {
  ServiceDescription sd = new ServiceDescription();
  sd.setType("personal-agent");
    Property p = new Property();
    p.setName("represents");
    p.setValue(personName);
    sd.addProperties(p);
  DFAgentDescription dfd = new DFAgentDescription();
  dfd.addOntologies("pa-ontology");
  //dfd.addAgentService(sd);
  //dfd.setDFState("active");
  //dfd.setOwnership(user);
  try {
    //dfd.toText(new BufferedWriter(new OutputStreamWriter(System.out)));
    DFSearchResult result;
    SearchConstraints c = new SearchConstraints();
    c.setMaxDepth(new Long(3));
    result = searchDF(dfname,dfd,c);
    //System.err.println("\nSearch DF results:");
    //result.toText(new BufferedWriter(new OutputStreamWriter(System.out)));
    
    // add values to knownPersons
    Enumeration e = result.elements();
    while (e.hasMoreElements()) {
      dfd = (DFAgentDescription)e.nextElement();
      try {
	Person prs = new Person(((ServiceDescription)dfd.getAllServices().next()).getOwnership(),dfd,dfname); 
	addKnownPerson(prs);
      } catch (Exception exc) { } 
    }
  } catch (FIPAException fe) {
    fe.printStackTrace();
    mf.showErrorMessage(fe.getMessage());
  }
}



  void DFregistration(AID dfAID) {
    ServiceDescription sd = new ServiceDescription();
    sd.setName("AppointmentScheduling");
    sd.setType("personal-agent");
    sd.addOntologies("pa-ontology");
    Property p = new Property();
    p.setName("represents");
    p.setValue(user);
    sd.addProperties(p);
    sd.setOwnership(user);
    DFAgentDescription dfd = new DFAgentDescription();
    dfd.setName(getAID());
    dfd.addServices(sd);
    dfd.addOntologies("pa-ontology");
    dfd.addProtocols("fipa-request fipa-Contract-Net");

    try {
      //dfd.toText(new BufferedWriter(new OutputStreamWriter(System.out)));
      registerWithDF(dfAID,dfd);
      knownDF.addElement(dfAID);
      Person prs = new Person(user,dfd,dfAID); 
      addKnownPerson(prs);
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

protected Person getPersonbyAgentName(AID agentname) {
  Enumeration e = knownPersons.elements();
  Person p;
  while (e.hasMoreElements()) {
    p = (Person)e.nextElement();
    if (p.getAgentName().equals(agentname)) 
      return p;
  }
  return null;
}

protected void fixAppointment(Appointment a) {
  System.err.println("fix Appointment" + a.toString());
    
    ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
    //FIXME only for Seoul 
    // cfp.setContent(a.toString());
    //cfp.setSource(getName());
    cfp.setReplyByDate(new Date(System.currentTimeMillis()+REPLYBY));
    List ag = new ArrayList();
    Enumeration e = a.getInvitedPersons();
    int numberOfInvited = 0;
    AID name;
    String listOfNames = "";
    while (e.hasMoreElements()) {
      numberOfInvited++;
      name = ((Person)e.nextElement()).getAgentName();
      ag.add(name);
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
    AID name;
    ACLMessage cancel = new ACLMessage(ACLMessage.CANCEL);
    //cancel.setSource(getName());
    while (e.hasMoreElements()) {
      name = ((Person)e.nextElement()).getAgentName();
      cancel.addReceiver(name);
      //FIXME only for Seoul 
      // cfp.setContent(a.toString());
      cancel.setContent("(action " + name + " (possible-appointments (list " + (a.getDate()).getDate() + ")))");   
      System.out.println(cancel.toString());
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
    DFregistration(DEFAULT_DF);  // register with the default DF
    mf.setVisible(true);
    startTasks();
}

protected String getUser() {
    return user;
}



} // end Agent.java

