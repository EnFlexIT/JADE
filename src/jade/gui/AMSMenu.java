package jade.gui;

import java.awt.*;
import java.awt.event.*;
import com.sun.java.swing.*;
import com.sun.java.swing.border.*;

/**
 * The Menu of the GUI
 */
public class AMSMenu extends JMenuBar
{
	public AMSMenu ()
	{
		super();
		
		AMSAbstractAction act;

		JMenu menu = new JMenu ("File");
		JMenuItem tmp;
		
		act = new AddAgentPlatformAction();
		tmp = menu.add(act);
		tmp.setIcon((Icon)null);
		
		act = new OpenScriptFileAction();
		tmp = menu.add(act);
		tmp.setIcon((Icon)null);
		add(menu);

		tmp = new JMenuItem ("Execute Current Script");
		menu.add(tmp);

		act = new ExitAction();
		tmp = menu.add(act);
		tmp.setIcon((Icon)null);
		
		add(menu);


		menu = new JMenu ("Actions");
		
		act = new StartNewAgentAction();
		tmp = menu.add(act);
		tmp.setIcon((Icon)null);

		act = new StartAction();
		tmp = menu.add(act);
		tmp.setIcon((Icon)null);

		act = new KillAction();
		tmp = menu.add(act);
		tmp.setIcon((Icon)null);
		
		act = new SuspendAction();
		tmp = menu.add(act);
		tmp.setIcon((Icon)null);
		
		act = new ResumeAction();
		tmp = menu.add(act);
		tmp.setIcon((Icon)null);
		
		act = new PingAction();
		tmp = menu.add(act);
		tmp.setIcon((Icon)null);
		
		act = new SnifferAction();
		tmp = menu.add(act);
		tmp.setIcon((Icon)null);
			
		act = new CustomAction();
		tmp = menu.add(act);
		tmp.setIcon((Icon)null);
			
		add(menu);

	}
}
