/*
  $Log$
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

package jade.gui;

import com.sun.java.swing.*;
import com.sun.java.swing.tree.*;
import java.awt.event.*;
import java.awt.*;
import java.lang.*;

import jade.domain.rma;

/**
 * StartNewAgentAction starts a new agent in the selected
 * container or in agent platform.
 * @see jade.gui.AMSAbstractAction
 */

public class StartNewAgentAction extends AMSAbstractAction {
  public StartNewAgentAction() {
    super ("StartNewAgentActionIcon","Start New Agent");
  }

  private int doStartNewAgent(String containerName) {
    int result = StartDialog.showStartNewDialog(containerName);
    if (result == StartDialog.OK_BUTTON) {

      String agentName = StartDialog.getAgentName();
      String className = StartDialog.getClassName();
      String container = StartDialog.getContainer();

      rma myRMA = AMSMainFrame.getRMA();
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
 
	      //	      AMSTreeModel treeModel = (AMSTreeModel)tree.getModel();
	      //	      TreeData current = new TreeData(StartDialog.getAgentName(), TreeData.AGENT);
	      //	      String [] s = new String[1];
	      //	      s[0] =  StartDialog.getHost()+" : "+StartDialog.getPort();
	      //	      current.setAddresses(s);
	      //	      treeModel.insertNodeInto(current,parent, 0);
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
