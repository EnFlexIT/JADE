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


import jade.domain.FIPAAgentManagement.DFAgentDescription; 
import jade.core.Agent;
import jade.core.AID;

/**
Javadoc documentation for the file
@author Fabio Bellifemine - CSELT S.p.A
@version $Date$ $Revision$
*/

public class Person 
{
    String name;   // name of the person
    AID dfName; // name of the DF with which this person is known
    DFAgentDescription dfd; // description registered with the DF
    
public Person(){
}

public Person(String userName, DFAgentDescription desc, AID dfName) {
  name=userName;
  dfd = desc;
  this.dfName=dfName;
}

    public String getName() {
        return name;
    }

public void setName(String n) {
  name = n;
}

public void setAgentDescription(DFAgentDescription d) {
  dfd = d;
}

public void setDF(AID df){ dfName = df; }

  /**
   * This method returns the AID of the agent corresponding to this person
   **/
public AID getAID(){
  if (dfd != null)
    return dfd.getName();
  else
    return new AID(name);
}
	
public String toString() {
  return name + " - " + dfName + " - " + dfd.toString();
}

}
