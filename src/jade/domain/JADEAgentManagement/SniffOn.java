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


package jade.domain.JADEAgentManagement;

import jade.util.leap.List;
import jade.util.leap.ArrayList;
import jade.util.leap.Iterator;

import jade.core.AID;

/**
   
   @author Giovanni Rimassa -  Universita` di Parma
   @version $Date$ $Revision$
*/

public class SniffOn {

  private AID sniffer;
  private List sniffedAgents = new ArrayList();
  private String password;

  public void setSniffer(AID id) {
    sniffer = id;
  }

  public AID getSniffer() {
    return sniffer;
  }

  public void clearAllSniffedAgents() {
    sniffedAgents.clear();
  }

  public void addSniffedAgents(AID id) {
    sniffedAgents.add(id);
  }

  public boolean removeSniffedAgents(AID id) {
    return sniffedAgents.remove(id);
  }

  public Iterator getAllSniffedAgents() {
    return sniffedAgents.iterator();
  }
  
  /**
     This method is called by the AMS in order to prepare an RMI call.
     The <code>getAllSniffedAgents()</code> cannot be used as it returns
     an <code>Iterator</code> that is not serializable.
  */
  public ArrayList getCloneOfSniffedAgents(){
    return (ArrayList) ((ArrayList) sniffedAgents).clone();
  }

  public void setPassword(String p) {
    password = p;
  }

  public String getPassword() {
    return password;
  }

}
