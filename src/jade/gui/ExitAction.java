/*
  $Log$
  Revision 1.3  1998/10/10 19:37:14  rimassa
  Imported a newer version of JADE GUI from Fabio.

  Revision 1.2  1998/10/04 18:01:41  rimassa
  Added a 'Log:' field to every source file.
*/

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

