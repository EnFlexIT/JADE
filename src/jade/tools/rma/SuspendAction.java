/*
  $Log$
  Revision 1.1  1999/05/20 15:42:12  rimassa
  Moved RMA agent from jade.domain package to jade.tools.rma package.

  Revision 1.6  1999/05/19 18:31:33  rimassa
  Changed various classes to remove static references to RMA agent from GUI
  components and actions.

  Revision 1.5  1999/03/03 16:00:03  rimassa
  Implemented action body to really resume suspended agents.

  Revision 1.4  1999/02/04 14:47:31  rimassa
  Changed package specification for Swing: now it's 'javax.swing' and no more
  'com.sun.swing'.

  Revision 1.3  1998/10/10 19:37:27  rimassa
  Imported a newer version of JADE GUI from Fabio.

  Revision 1.2  1998/10/04 18:01:41  rimassa
  Added a 'Log:' field to every source file.
*/

package jade.tools.rma;

import javax.swing.*;
import javax.swing.tree.TreeModel;
import java.awt.event.*;
import java.awt.*;
import java.lang.*;

/**
 * SuspendAction suspends selected agents
 * @see jade.gui.AMSAbstractAction
 */
public class SuspendAction extends AMSAbstractAction {

  private rma myRMA;

  public SuspendAction(rma anRMA) {
    super ("SuspendActionIcon","Suspend Selected Agents");
    myRMA = anRMA;
  }

    public void actionPerformed(ActionEvent evt) {
      for (int i=0;i<listeners.size();i++) {
	TreeData current = (TreeData)listeners.elementAt(i);
	current.setState(TreeData.SUSPENDED);
	String toSuspend = current.getName();

	int level = current.getLevel();

	switch(level) {
	case TreeData.AGENT:
	  myRMA.suspendAgent(toSuspend);
	  break;
	case TreeData.CONTAINER:
	  myRMA.suspendContainer(toSuspend);
	  break;
	}
	AMSTreeModel myModel = myRMA.getModel();
	myModel.nodeChanged(current);
      }
      listeners.removeAllElements();
    }

}

