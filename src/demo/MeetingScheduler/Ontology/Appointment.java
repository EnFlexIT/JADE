/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop 
multi-agent systems in compliance with the FIPA specifications.
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

package demo.MeetingScheduler.Ontology;
import java.util.*;
import jade.core.AID;

/**
Javadoc documentation for the file
@author Fabio Bellifemine - CSELT S.p.A
@version $Date$ $Revision$
*/

public class Appointment {

    private String description = new String();
    private Date startingOn;
    private Date endingWith;
    private List invited = new ArrayList(); // Vector of Persons
    private Date fixedDate;
    private AID invitingAgent;
    
    /**
    * constructor
    * @param agentName is the name of the Agent who called the appointment
    */
    public Appointment() {
        startingOn = new Date();
        endingWith = new Date();
        description = "Appointment called by "+invitingAgent.toString();
    }

  public void setInviter(AID name){
    invitingAgent=name;
  }


  public AID getInviter(){
    return invitingAgent;
  }

   public void setDescription (String descr) {
        description = descr;
    }

   public String getDescription() {
        return description;
    }

   public void setStartingOn( Date date) {
        startingOn = date;
    }

   public Date getStartingOn() {
        return startingOn;
    }

   public void setEndingWith (Date date) {
        endingWith = date;
    }
   
  public Date getEndingWith () {
        return endingWith;
    }

   public  void addInvitedPersons (Person p) { 
       invited.add(p);
    }

  public Iterator getAllInvitedPersons() {
    return invited.iterator();
  }


  
    public Date getFixedDate() {
        if (fixedDate == null)
        return startingOn;
        else return fixedDate;
    }

public void setFixedDate(Date date) {
    fixedDate = date;
}


    public boolean isValid() throws Exception {
     if (startingOn.after(endingWith))
        throw new Exception("Ending date must be before Starting Date");
     if (description.length() <= 0)
        throw new Exception("The Appointment must contain a description");
     return true;
    }
    
        
    
    public String toString() {
        String str = "(Appointment ";
        if (description.length() > 0)
            str = str + ":description \"" + description + "\" ";
        if (fixedDate == null) {
            str = str + ":starting-on \"" + startingOn.toString() + "\" :ending-with \"" + endingWith.toString() + "\" ";
        } else str = str + ":fixed-on \"" + fixedDate.toString() + "\" ";
        str = str + ":invited (set ";
        for (int i=0; i<invited.size(); i++)
                str = str + ((Person)invited.get(i)).getName() + " ";
        str = str + ") ";
        str = str + ":called-by " +invitingAgent;
        return str + ")";        
    }
    
}
