package jade.gui;

import com.sun.java.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.lang.*;

/**
 * SuspendAction suspends selected agents
 * @see jade.gui.AMSAbstractAction
 */
public class SuspendAction extends AMSAbstractAction
{
	public SuspendAction()
	{
		super ("SuspendActionIcon","Suspend Selected Agents");
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

