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
