/*
  $Log$
  Revision 1.3  1998/10/10 19:37:20  rimassa
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
 * Reload Action
 * This action forces the AMSTree to reload ALL its nodes
 * @see jade.gui.AMSAbstractAction
 */
public class ReloadAction extends AMSAbstractAction
{

	public ReloadAction()
	{
		super ("PingActionIcon","Reload Tree from selected Node");
	}
	
	/**
	  * Determines the selection from the Tree and asks the treemodel
	  * to reload itself from that node.
	  */
	public void actionPerformed(ActionEvent e) 
	{
	    DefaultMutableTreeNode lastItem = tree.getSelectedNode();

	    if(lastItem != null)
		tree.getModel().reload(lastItem);
	}


}

