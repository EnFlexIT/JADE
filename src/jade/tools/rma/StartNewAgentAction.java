/*
  $Log$
  Revision 1.2  1999/06/06 21:56:06  rimassa
  Removed old, commented out code.

  Revision 1.1  1999/05/20 15:42:12  rimassa
  Moved RMA agent from jade.domain package to jade.tools.rma package.

  Revision 1.7  1999/05/19 18:31:32  rimassa
  Changed various classes to remove static references to RMA agent from GUI
  components and actions.

  Revision 1.6  1999/02/04 14:47:31  rimassa
  Changed package specification for Swing: now it's 'javax.swing' and no more
  'com.sun.swing'.

  Revision 1.5  1998/11/09 00:36:42  rimassa
  Changed callback logic to allow different ways to start new agents:
   - When no node is selected, one can write the container name within
     start dialog window.
   - When a container node is selected the agent is started on that
     agent container.
   - When more container nodes are selected an agent is started on each
     one of them.

  Revision 1.4  1998/11/05 23:44:59  rimassa
  Changed code indentation style.

  Revision 1.3  1998/10/10 19:37:26  rimassa
  Imported a newer version of JADE GUI from Fabio.

  Revision 1.2  1998/10/04 18:01:41  rimassa
  Added a 'Log:' field to every source file.
*/

package jade.tools.rma;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.event.*;
import java.awt.*;
import java.lang.*;

/**
 * StartNewAgentAction starts a new agent in the selected
 * container or in agent platform.
 * @see jade.gui.AMSAbstractAction
 */

public class StartNewAgentAction extends AMSAbstractAction {

  private rma myRMA;

  public StartNewAgentAction(rma anRMA) {
    super ("StartNewAgentActionIcon","Start New Agent");
    myRMA = anRMA;
  }

  private int doStartNewAgent(String containerName) {
    int result = StartDialog.showStartNewDialog(containerName);
    if (result == StartDialog.OK_BUTTON) {

      String agentName = StartDialog.getAgentName();
      String className = StartDialog.getClassName();
      String container = StartDialog.getContainer();

      myRMA.newAgent(agentName, className, container);
    }
    return result;
  }

  public void actionPerformed(ActionEvent e) {
    if (listeners.size() >= 1) { // Some tree node is selected
      for (int i=0;i<listeners.size();i++) {
	try {

	  TreeData parent = (TreeData) listeners.elementAt(i);
	  if (parent.getLevel() == TreeData.AGENT || parent.getLevel() == TreeData.SUPER_NODE) {
	    throw new StartException();
	  }
	  else {
	    String containerName = parent.getName();

	    int result = doStartNewAgent(containerName);
	    if (result == StartDialog.OK_BUTTON) {
	      ((TreeData)listeners.elementAt(i)).setState(TreeData.RUNNING);
 
	    }
	  }
	}
	catch (StartException ex) {
	  StartException.handle();	
	}
      }
    }
    else
      doStartNewAgent(null);
  }
}


/** 
 * This class is useful to handle user input error
 */
class StartException extends Exception
{
  public static final String ErrorMessage = "You must select an agent-platform or a agent-container in the Tree";
  public static final String ErrorPaneTitle = "Start Procedure Error";
  
  public StartException()
    {}

  public static final void handle ()
    {
      JOptionPane.showMessageDialog(new JFrame(),ErrorMessage,ErrorPaneTitle,JOptionPane.ERROR_MESSAGE);
    }
}







