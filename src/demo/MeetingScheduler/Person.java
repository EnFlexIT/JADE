package demo.MeetingScheduler;
import java.io.StringWriter;

import java.lang.System;

import jade.domain.AgentManagementOntology;

class Person 
{
    String name;   // name of the person
    String dfName; // name of the DF with which this person is known
    AgentManagementOntology.DFAgentDescriptor dfd; // description registered with the DF
    
    public Person (String username) {
        name=username;
        dfName = "df";  // default DF
        dfd = new AgentManagementOntology.DFAgentDescriptor();
        dfd.setName(name);
		//{{INIT_CONTROLS
		//}}
	}
    
    public Person (String username, AgentManagementOntology.DFAgentDescriptor d, String df) {
        this(username);
        dfd = d;
        dfName = df;
    }
    
    protected String getName() {
        return name;
    }

protected String getAgentName(){
  //  if (dfd.getAddresses().hasMoreElements()) 
  //  return dfd.getName() + "@" + (String)dfd.getAddresses().nextElement();
  //else 
  return dfd.getName();
}
	public String toString() {
      StringWriter text = new StringWriter();
      dfd.toText(text);
      return name + " - " + dfName + " - " + text.toString();
    }
		//{{DECLARE_CONTROLS
		//}}
}
