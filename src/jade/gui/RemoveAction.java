/*
  $Log$
  Revision 1.2  1998/10/04 18:01:57  rimassa
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
 * RemoveAction removes the selected node from the tree.  
 * If the root or nothing is selected nothing is removed.
 * @see jade.gui.AMSAbstractAction
 */
public class RemoveAction extends AMSAbstractAction
{

    public RemoveAction ()
	{
		super ("PingActionIcon","Remove a Node from the Tree");
	}
	
	/**
	  * Removes the selected item as long as it isn't root.
	  */
	public void actionPerformed(ActionEvent e) 
	{
	    DefaultMutableTreeNode lastItem = (DefaultMutableTreeNode) tree.getSelectedNode();

		try
		{
			if(lastItem != null && lastItem != (DefaultMutableTreeNode)tree.getModel().getRoot()) 
			{
				tree.getModel().removeNodeFromParent(lastItem);
			}
		}

		// A swing bug (i suppose) makes sometimes an exception occurs 
		// while removing a node from the tree...
		catch (Exception exc)
		{
			System.out.println("An Exception has occurred removing a node");
		}
    } 
}

