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

package demo.MeetingScheduler;
import java.util.Enumeration;
import java.util.Vector;
import java.util.Date;
import java.lang.Exception;

/**
Javadoc documentation for the file
@author Fabio Bellifemine - CSELT S.p.A
@version $Date$ $Revision$
*/

public class Appointment {

    private String description = new String();
    private Date startingOn;
    private Date endingWith;
    private Vector invited = new Vector(); // Vector of Persons
    private Date fixedDate;
    private String invitingAgent;
    
    /**
    * constructor
    * @param agentName is the name of the Agent who called the appointment
    */
    Appointment(String agentName) {
        invitingAgent = agentName;
        startingOn = new Date();
        endingWith = new Date();
        description = "Appointment called by "+invitingAgent;
    }

    /* 
    Appointment(String agentName, String fromText) {
        this(agentName);
        int i,j;
        long d;
        System.err.println("originale= "+fromText);
        String str = fromText;  // non posso farlo per la data
        i=str.indexOf(":description");
        j=str.indexOf(")",i);
        if (i>0)
            setDescription(str.substring(i+14,j-1)); //14 to skip :description "
                                                     // -1 to skip "
                                                     System.err.println(str);
        i=str.indexOf(":starting-on");
        j=str.indexOf(")",i);
        if (i>0) {
            d = Long.parseLong(str.substring(i+14,j-1));
            // System.err.println(startingOn.toString());
            startingOn = new Date(d);
            System.err.println("starting-on: "+startingOn.toString());
        }
            // setStartingOn(str.substring(i+14,j-1));
        i=str.indexOf(":ending-with");
        j=str.indexOf(")",i);
        if (i>0) { 
            d = Long.parseLong(str.substring(i+14,j-1));
            endingWith = new Date(d);
            System.err.println("ending-with: "+endingWith.toString());
            //setEndingWith(str.substring(i+14,j-1));
        }
        //FIXME prendere anche il campo invited ed il campo called-by ed il campo fixed
        //i=str.indexOf(":called-by");
        //j=str.indexOf(")",i);
        //setEndingWith(str.substring(i,j));

    }
    */
    

  String getInvitingAgent(){
    return invitingAgent;
  }

    void setDescription (String descr) {
        description = descr;
    }
    String getDescription() {
        return description;
    }

    void setStartingOn( Date date) {
        startingOn = date;
    }

    Date getStartingOn() {
        return startingOn;
    }

    void setEndingWith (Date date) {
        endingWith = date;
    }
    Date getEndingWith () {
        return endingWith;
    }

    void addInvitedPerson (Person p) { 
       invited.addElement(p);
    }

  Enumeration getInvitedPersons() {
    return invited.elements();
  }


  
    Date getDate() {
        if (fixedDate == null)
        return startingOn;
        else return fixedDate;
    }

public void setFixedDate(Date date) {
    fixedDate = date;
}

    boolean isValid() throws Exception {
     if (startingOn.after(endingWith))
        throw new Exception("Ending date must be before Starting Date");
     if (description.length() <= 0)
        throw new Exception("The Appointment must contain a description");
     return true;
    }
    
        
    
    public String toString() {
        String str = "(appointment ";
        if (description.length() > 0)
            str = str + "(:description \"" + description + "\") ";
        if (fixedDate == null) {
            str = str + "(:starting-on \"" + startingOn.toString() + "\") (:ending-with \"" + endingWith.toString() + "\") ";
        } else str = str + "(:fixed-on \"" + fixedDate.toString() + "\") ";
        str = str + "(:invited (list ";
        for (int i=0; i<invited.size(); i++)
                str = str + ((Person)invited.elementAt(i)).getName() + " ";
        str = str + "))";
        str = str + "(:called-by " +invitingAgent + ")";
        return str + ")";        
    }
    
}
