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
import javax.swing.tree.TreeModel;
import java.awt.event.*;
import java.awt.*;
import java.lang.*;

/**
Javadoc documentation for the file
@author Giovanni Rimassa - Universita` di Parma
@version $Date$ $Revision$
*/

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
