/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop multi-agent systems in compliance with the FIPA specifications.
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

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.event.*;
import java.awt.*;
import java.lang.*;

/**
Javadoc documentation for the file
@author Giovanni Rimassa - Universita` di Parma
@version $Date$ $Revision$
*/

/**
 * StartNewAgentAction starts a new agent in the selected
 * container or in agent platform.
 * @see jade.gui.AMSAbstractAction
 */

public class StartNewAgentAction extends AMSAbstractAction {

  private rma myRMA;
  private Frame mainWnd;

  public StartNewAgentAction(rma anRMA, Frame f) {
    super ("StartNewAgentActionIcon","Start New Agent");
    myRMA = anRMA;
    mainWnd = f;
  }

  private int doStartNewAgent(String containerName) {
    int result = StartDialog.showStartNewDialog(containerName, mainWnd);
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







