/*
  $Log$
  Revision 1.6  1999/05/19 18:31:30  rimassa
  Changed various classes to remove static references to RMA agent from GUI
  components and actions.

  Revision 1.5  1999/03/03 15:59:22  rimassa
  Implemented the action body to really suspend agents.

  Revision 1.4  1999/02/04 14:47:29  rimassa
  Changed package specification for Swing: now it's 'javax.swing' and no more
  'com.sun.swing'.

  Revision 1.3  1998/10/10 19:37:22  rimassa
  Imported a newer version of JADE GUI from Fabio.

  Revision 1.2  1998/10/04 18:01:41  rimassa
  Added a 'Log:' field to every source file.
*/

package jade.gui;

import javax.swing.*;
import javax.swing.tree.TreeModel;
import java.awt.event.*;
import java.awt.*;
import java.lang.*;

import jade.domain.rma;

/**
 * ResumeAction resumes selected nodes
 * @see jade.gui.AMSAbstractAction
 */
public class ResumeAction extends AMSAbstractAction {

  private rma myRMA;

  public ResumeAction(rma anRMA) {
    super ("ResumeActionIcon","Resume Selected Agents");
    myRMA = anRMA;
  }

  public void actionPerformed(ActionEvent evt) {
    for (int i=0;i<listeners.size();i++) {
      TreeData current = (TreeData)listeners.elementAt(i);
      current.setState(TreeData.RUNNING);
      String toResume = current.getName();

      int level = current.getLevel();

      switch(level) {
      case TreeData.AGENT:
	myRMA.resumeAgent(toResume);
	break;
      case TreeData.CONTAINER:
	myRMA.resumeContainer(toResume);
	break;
      }
      AMSTreeModel myModel = myRMA.getModel();
      myModel.nodeChanged(current);
    }
    listeners.removeAllElements();
  }


}
