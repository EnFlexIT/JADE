/*
  $Log$
  Revision 1.1  1999/05/20 15:42:09  rimassa
  Moved RMA agent from jade.domain package to jade.tools.rma package.

  Revision 1.7  1999/05/19 18:31:29  rimassa
  Changed various classes to remove static references to RMA agent from GUI
  components and actions.

  Revision 1.6  1999/02/04 14:47:28  rimassa
  Changed package specification for Swing: now it's 'javax.swing' and no more
  'com.sun.swing'.

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

package jade.tools.rma;

import javax.swing.*;
import java.awt.event.*;
import java.awt.*;


/** 
 * Kill Action
 * @see jade.gui.AMSAbstractAction
 */
public class KillAction extends AMSAbstractAction {

  private rma myRMA;

  public KillAction(String label, rma anRMA) {
    super ("KillActionIcon", label);
    myRMA = anRMA;
  }

  public void actionPerformed(ActionEvent evt) {
    for (int i=0;i<listeners.size();i++) {
      TreeData current = (TreeData)listeners.elementAt(i);
      String toKill = current.getName();

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


