/*
  $Log$
  Revision 1.1  1999/06/22 13:20:13  rimassa
  Action to show the GUI of the DF agent.

*/

package jade.tools.rma;

import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.lang.*;
import jade.lang.acl.ACLMessage;

class ShowDFGuiAction extends AMSAbstractAction
{

  private rma myRMA;

	ShowDFGuiAction(rma anRMA)
	{
	  // Note: this class uses the DummyAgentActionIcon just because it 
	  // never displays an icon, but a parameter must anyway be passed.
	  super ("DummyAgentActionIcon","Show the DF GUI");
	  myRMA = anRMA;
	}
	
	public void actionPerformed(ActionEvent e) 
	{
	  ACLMessage msg = new ACLMessage("request");
	  msg.setDest("df");
	  msg.setOntology("jade-extensions");
	  msg.setProtocol("fipa-request");
	  msg.setContent("(action df (SHOWGUI))");
	  myRMA.send(msg);
	}
}
	


