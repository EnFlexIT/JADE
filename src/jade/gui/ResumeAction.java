/*
  $Log$
  Revision 1.2  1998/10/04 18:01:58  rimassa
  Added a 'Log:' field to every source file.

*/

package jade.gui;

import com.sun.java.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.lang.*;

/**
 * ResumeAction resumes selected nodes
 * @see jade.gui.AMSAbstractAction
 */
public class ResumeAction extends AMSAbstractAction
{

	public ResumeAction()
	{
		super ("ResumeActionIcon","Resume Selected Agents");
	}

	public void actionPerformed(ActionEvent evt)
    {
        for (int i=0;i<listeners.size();i++)
		{
			System.out.print(ActionName);
			System.out.print(listeners.elementAt(i).toString());
			if (listeners.elementAt(i) instanceof TreeData)
				( (TreeData)listeners.elementAt(i)).setState(TreeData.SUSPENDED);
			System.out.println();
		}
		listeners.removeAllElements();
    }


}


