/*
  $Log$
  Revision 1.2  1999/11/08 15:29:00  rimassaJade
  Made static the variable "progressiveNumber" to avoid name clashes
  between the menubar and the toolbar.

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
  private  static int progressiveNumber = 0;

  private rma myRMA;

    public DummyAgentAction(rma anRMA) {
      super ("DummyAgentActionIcon","Start DummyAgent");
      progressiveNumber = 0;
      myRMA = anRMA;
    }
	
    public void actionPerformed(ActionEvent e) 
    {
      myRMA.newAgent("da"+progressiveNumber, "jade.tools.DummyAgent.DummyAgent", new String());
      progressiveNumber++;
    }
}
