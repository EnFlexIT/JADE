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

import java.awt.Frame;
import jade.lang.acl.ACLMessage;
import jade.gui.*;

/**
   Javadoc documentation for the file
   @author Francisco Regi, Andrea Soracchi - Universita` di Parma
   @version $Date$ $Revision$
 */
public class CustomAction extends AgentAction {

 private rma myRMA;
 private Frame mainWnd;

  public CustomAction(rma anRMA, Frame f,ActionProcessor actPro)
    {
	super ("CustomActionIcon","Send Message",actPro);
	myRMA = anRMA;
	mainWnd = f;
    }

  public void doAction(AgentTree.AgentNode nod) {

    AgentTree.Node node=(AgentTree.Node) nod;
    ACLMessage msg2 = new ACLMessage(ACLMessage.NOT_UNDERSTOOD);
    msg2.addDest(node.getName());
    ACLMessage msg = jade.gui.AclGui.editMsgInDialog(msg2, mainWnd);
      if (msg != null)
	  myRMA.send(msg);

  }

} // End CustomAction
