/*
  $Log$
  Revision 1.2  1998/10/04 18:01:49  rimassa
  Added a 'Log:' field to every source file.

*/

package jade.gui;

import com.sun.java.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.lang.*;

/** 
 * Send Custom message Action
 * @see jade.gui.AMSAbstractAction
 */
public class CustomAction extends AMSAbstractAction
{
	public CustomAction()
	{
		super ("CustomActionIcon","Send Custom Message to Selected Agents");
	}

	public void actionPerformed(ActionEvent e) 
	{
		System.out.println(ActionName);                                     
	}
}

