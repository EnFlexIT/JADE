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


package jade.tools.rma;

import jade.lang.acl.ACLMessage;
import jade.domain.JADEAgentManagement.JADEAgentManagementOntology;
import jade.domain.JADEAgentManagement.ShowGui;
import jade.lang.sl.SL0Codec;
import jade.onto.basic.Action;
import jade.domain.FIPAException;
import jade.util.leap.List;
import jade.util.leap.ArrayList;

/**
   
   @author Francisco Regi, Andrea Soracchi - Universita` di Parma
   @version $Date$ $Revision$
 */
class ShowDFGuiAction extends FixedAction
{

  private rma myRMA;
  private ACLMessage msg;
  
  ShowDFGuiAction(rma anRMA,ActionProcessor actPro ) {

     // Note: this class uses the DummyAgentActionIcon just because it
     // never displays an icon, but a parameter must anyway be passed.

     super ("DGGUIActionIcon","Show the DF GUI",actPro);
     myRMA = anRMA;
     msg = new ACLMessage(ACLMessage.REQUEST);
     msg.addReceiver(myRMA.getDefaultDF());
     msg.setOntology(JADEAgentManagementOntology.NAME);
     msg.setLanguage(SL0Codec.NAME);
     msg.setProtocol("fipa-request");
     Action a = new Action();
     a.set_0(myRMA.getDefaultDF());
     a.set_1(new ShowGui());
     List l = new ArrayList();
     l.add(a);
     try {
     	myRMA.fillContent(msg,l);
     } catch (FIPAException e) {
     	e.printStackTrace();
     }
  }

   public void doAction() {
     myRMA.send(msg);
  }

}  // End of ShowDFGuiAction


