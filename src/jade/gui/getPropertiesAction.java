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

