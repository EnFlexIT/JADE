package jade.gui;

import java.awt.*;
import java.awt.event.*;
import com.sun.java.swing.*;
import com.sun.java.swing.border.*;

/** 
 * Exit Action
 * @see jade.gui.AMSAbstractAction
 */
public class ExitAction extends AMSAbstractAction
{
	
	public ExitAction ()
	{
		super ("ExitActionIcon","Exit");
	}

	public void actionPerformed(ActionEvent evt)
    {
        System.exit(0);
    }
}

