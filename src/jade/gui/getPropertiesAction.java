/*
  $Log$
  Revision 1.2  1998/10/04 18:02:08  rimassa
  Added a 'Log:' field to every source file.

*/

package jade.gui;

import java.awt.*;
import java.awt.event.*;
import com.sun.java.swing.*;
import com.sun.java.swing.border.*;

/** 
 * Get Node Properties Action
 * @see jade.gui.AMSAbstractAction
 */
public class getPropertiesAction extends AMSAbstractAction
{
	public getPropertiesAction()
	{
		super ("getPropertiesActionIcon","Properties");
	}

   	public void actionPerformed(ActionEvent e) 
	{
		System.out.println(ActionName);                                     
	}
}

