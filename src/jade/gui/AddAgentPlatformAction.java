/*
  $Log$
  Revision 1.3  1998/10/10 19:37:12  rimassa
  Imported a newer version of JADE GUI from Fabio.

  Revision 1.2  1998/10/04 18:01:41  rimassa
  Added a 'Log:' field to every source file.
*/

package jade.gui;

import com.sun.java.swing.*;
import com.sun.java.swing.tree.*;
import com.sun.java.swing.event.*;
import java.awt.event.*;
import java.awt.*;
import java.util.*;
 /**
     * An Action to connect the GUI to a AMS
     *
     * @param elem the element to represent
     */
   
public class AddAgentPlatformAction extends AMSAbstractAction
{	
	
	public AddAgentPlatformAction()
	{
		super ("AddAgentPlatformActionIcon","Connect GUI to Agent Platform");
	}

 /**
	 * Asks to the user platform names and tries to connect 
     * after it is connected updates the AMSTree
	 */
   	public void actionPerformed(ActionEvent e) 
	{
		String result = JOptionPane.showInputDialog(new JFrame(),"insert agent-address of AMS , please");
                                     
        if(result == null || result.length() == 0) 	return ;
        else 
		{
			AMSTreeModel	treeModel = (AMSTreeModel)tree.getModel();

		    DefaultMutableTreeNode parent = (DefaultMutableTreeNode)treeModel.getRoot();
			//TreeData.addPlatform(result);
			treeModel.insertNodeInto(new TreeData(result ,TreeData.AGENT_PLATFORM),parent, 0);
		}
	}
     // End of SampleTree.AddAction
}
