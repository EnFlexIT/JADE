/*
  $Log$
  Revision 1.5  1998/11/15 23:17:48  rimassa
  Some changes to the constructor and actionPerformed() method to
  distinguish between Agent and AgentContainers.

  Revision 1.4  1998/11/05 23:41:41  rimassa
  Implemented KillAction, using RMA agent to send a 'kill-agent' request
  message to the AMS which will do the real work.
  Changed some indentation style.

  Revision 1.3  1998/10/10 19:37:16  rimassa
  Imported a newer version of JADE GUI from Fabio.

  Revision 1.2  1998/10/04 18:01:41  rimassa
  Added a 'Log:' field to every source file.
*/

package jade.gui;

import com.sun.java.swing.*;
import java.awt.event.*;
import java.awt.*;

import jade.domain.rma;


/** 
 * Kill Action
 * @see jade.gui.AMSAbstractAction
 */
public class KillAction extends AMSAbstractAction {

  public KillAction(String label) {
    super ("KillActionIcon", label);
  }

  public void actionPerformed(ActionEvent evt) {
    for (int i=0;i<listeners.size();i++) {
      TreeData current = (TreeData)listeners.elementAt(i);
      String toKill = current.getName();
      rma myRMA = AMSMainFrame.getRMA();

      int level = current.getLevel();

      switch(level) {
      case TreeData.AGENT:
	myRMA.killAgent(toKill);
	break;
      case TreeData.CONTAINER:
	myRMA.killContainer(toKill);
	break;
      }
    }
    listeners.removeAllElements();
  }

}


