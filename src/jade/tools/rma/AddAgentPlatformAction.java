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
import javax.swing.event.*;
import java.awt.event.*;
import java.awt.*;
import java.util.*;

/**
Javadoc documentation for the file
@author Giovanni Rimassa - Universita` di Parma
@version $Date$ $Revision$
*/
/**
 * An Action to connect the GUI to a AMS
 *
 * @param elem the element to represent
 */
   
public class AddAgentPlatformAction extends AMSAbstractAction {	
	
  public AddAgentPlatformAction() {
    super ("AddAgentPlatformActionIcon","Connect GUI to Agent Platform");
  }

  /**
   * Asks to the user platform names and tries to connect 
   * after it is connected updates the AMSTree
   */
  public void actionPerformed(ActionEvent e)  {
    String result = JOptionPane.showInputDialog(new JFrame(),"insert agent-address of AMS , please");
                                     
    if(result == null || result.length() == 0)
      return ;
    else {
      AMSTreeModel treeModel = (AMSTreeModel)tree.getModel();

      DefaultMutableTreeNode parent = (DefaultMutableTreeNode)treeModel.getRoot();
      //TreeData.addPlatform(result);
      treeModel.insertNodeInto(new TreeData(result ,TreeData.AGENT_PLATFORM),parent, 0);
    }
  }
  // End of SampleTree.AddAction
}
