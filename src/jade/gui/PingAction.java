/*
  $Log$
  Revision 1.3  1998/10/10 19:37:18  rimassa
  Imported a newer version of JADE GUI from Fabio.

  Revision 1.2  1998/10/04 18:01:41  rimassa
  Added a 'Log:' field to every source file.
*/

package jade.gui;

import com.sun.java.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.lang.*;


/** 
 * Ping Action. This Action pings ALL the agent loaded in 
 * the vector of listeners (AMSAbstractAction.listeners)
 * @see jade.gui.AMSAbstractAction
 */
public class PingAction extends AMSAbstractAction
{
	public PingAction()
	{
		super ("PingActionIcon","Ping Selected Agents");
	}
	
	public void actionPerformed(ActionEvent e) 
	{
		System.out.println(ActionName);                                     
	}

}
