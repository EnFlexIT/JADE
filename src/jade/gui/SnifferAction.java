/*
  $Log$
  Revision 1.2  1998/10/04 18:01:59  rimassa
  Added a 'Log:' field to every source file.

*/

package jade.gui;

import com.sun.java.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.lang.*;

/**
 * SnifferAction spawns an external application passing as parameters a 
 * String containing ALL agents selected in the Tree
 * @see jade.gui.AMSAbstractAction
 */
public class SnifferAction extends AMSAbstractAction
{
	public SnifferAction()
	{
		super ("SnifferActionIcon","Start Sniffer");
	}
	
	public void actionPerformed(ActionEvent e) 
	{
		System.out.println(ActionName+" for Agents: ");                                     
		for (int i=0;i<listeners.size();i++)
		{
			System.out.println(listeners.elementAt(i).toString());
		}
		listeners.removeAllElements();
	}
}
	
