package jade.gui;

import com.sun.java.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.lang.*;
import java.util.*;

/**
 * StartAction starts all the selected agents in the tree
 * @see jade.gui.AMSAbstractAction
 */
public class StartAction extends AMSAbstractAction
{
	public StartAction()
	{
		super ("StartActionIcon","Start Selected Agents");
	}

	public void actionPerformed(ActionEvent evt)
    {
        for (int i=0;(i<listeners.size() && listeners.elementAt(i) instanceof TreeData);i++)
		try
		{
			TreeData current = (TreeData)listeners.elementAt(i);
			StartDialog.setHost(current.getAddressesAsString());
			StringTokenizer  st  =  new  StringTokenizer(current.getAddressesAsString(),":");
			
			StartDialog.setHost(st.nextToken());
			StartDialog.setPort(st.nextToken());

			int result = StartDialog.showStartDialog(listeners.elementAt(i).toString());
			
			if (result == StartDialog.OK_BUTTON)
			{
					current.setState(TreeData.RUNNING);
					String [] s = new String[1];
					s[0] = StartDialog.getHost()+" : "+StartDialog.getPort();
					current.setAddresses(s);
					tree.repaint();
			}			
		}
		catch (Exception e){}
	}
}