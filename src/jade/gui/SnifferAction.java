/*
  $Log$
  Revision 1.4  1999/02/04 14:47:30  rimassa
  Changed package specification for Swing: now it's 'javax.swing' and no more
  'com.sun.swing'.

  Revision 1.3  1998/10/10 19:37:23  rimassa
  Imported a newer version of JADE GUI from Fabio.

  Revision 1.2  1998/10/04 18:01:41  rimassa
  Added a 'Log:' field to every source file.
*/

package jade.gui;

import javax.swing.*;
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
	
