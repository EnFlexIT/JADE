/*
  $Log$
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

/**
 * StartNewAgentAction starts a new agent in the selected
 * container or in agent platform.
 * @see jade.gui.AMSAbstractAction
 */

public class StartNewAgentAction extends AMSAbstractAction
{
	public StartNewAgentAction()
	{
		super ("StartNewAgentActionIcon","Start New Agent");
	}

  	public void actionPerformed(ActionEvent e) 
	{
		try	
		{
			if (listeners.size()>=1 && listeners.elementAt(0) instanceof TreeData)
			{
				TreeData parent = (TreeData) listeners.elementAt(0);
				if (parent.getLevel() == TreeData.AGENT || parent.getLevel() == TreeData.SUPER_NODE)
				{	
					throw new StartException();
				}
				else 
				for (int i=0;i<listeners.size();i++)
				{
					int result = StartDialog.showStartNewDialog();
					if (result == StartDialog.OK_BUTTON)
					{
			 			( (TreeData)listeners.elementAt(i)).setState(TreeData.RUNNING);
						System.out.println("STARTING NEW AGENT "+StartDialog.getClassName()+" IN HOST "+
						StartDialog.getHost()+" USING PORT "+StartDialog.getPort());
						System.out.println();
						
						AMSTreeModel	treeModel = (AMSTreeModel)tree.getModel();
						TreeData current = new TreeData(StartDialog.getClassName() ,TreeData.AGENT);
						String [] s = new String[1];
						s[0] = 	StartDialog.getHost()+" : "+StartDialog.getPort();
						current.setAddresses(s);
						treeModel.insertNodeInto(current,parent, 0);
					}
				}
			}
			else throw new StartException();
		}
		catch (StartException ex)
		{
			StartException.handle();	
		}
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
