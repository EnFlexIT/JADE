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

