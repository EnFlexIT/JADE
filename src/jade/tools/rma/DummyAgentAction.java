/*
  $Log$
  Revision 1.1  1999/06/04 11:37:25  rimassa
  Action to start a new instance of DummyAgent tool.
 
*/

package jade.tools.rma;

import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.lang.*;

/**
 * DummyAgentAction spawns an external application passing as parameters a 
 * String containing ALL agents selected in the Tree
 * @see jade.gui.AMSAbstractAction
 */
public class DummyAgentAction extends AMSAbstractAction
{
  /**
   * Progressive Number to give always a new name to DummyAgent
   */
  int progressiveNumber;

  private rma myRMA;

	public DummyAgentAction(rma anRMA)
	{
		super ("DummyAgentActionIcon","Start DummyAgent");
		progressiveNumber = 0;
		myRMA = anRMA;
	}
	
	public void actionPerformed(ActionEvent e) 
	{
	  //	System.out.println(ActionName+" for Agents: ");                                     
		//for (int i=0;i<listeners.size();i++)
		//{
		//	System.out.println(listeners.elementAt(i).toString());
		//}
		//listeners.removeAllElements();
		// FIXME. There is no guarantee that a not existing name is
		// given to the agent.
		myRMA.newAgent("da"+progressiveNumber, "jade.tools.DummyAgent.DummyAgent", new String());
		progressiveNumber++;
	}
}
	
