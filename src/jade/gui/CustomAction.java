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

