package jade.gui;

import com.sun.java.swing.*;
import com.sun.java.swing.tree.*;
import com.sun.java.swing.event.*;
import java.awt.event.*;
import java.awt.*;
import java.util.*;

/**
 * The Context Menu of the GUI
 */

public class AMSPopupMenu extends JPopupMenu
{
	PopupMenuListener parent;
	protected AMSAbstractAction act;
	protected PingAction ping = new PingAction();
	protected StartAction start = new StartAction();
	protected KillAction kill = new KillAction();
	protected ResumeAction resume = new ResumeAction();	
	protected CustomAction custom = new CustomAction();
	protected getPropertiesAction properties = new getPropertiesAction();
	protected SnifferAction sniffer = new SnifferAction();
	protected StartNewAgentAction NewAgent = new StartNewAgentAction();
	protected SuspendAction suspend = new SuspendAction ();
	protected AddAgentPlatformAction NewAgentPlatform = new AddAgentPlatformAction();
	private JMenu ActionsMenu;

	public AMSPopupMenu (PopupMenuListener parent)
	{
		super();
		this.parent = parent;

		JMenu ActionsMenu = new JMenu ("Actions");
		JMenuItem tmp;

		tmp = add(NewAgentPlatform);
		tmp.setIcon(null);

		tmp = add(properties);			
		tmp.setIcon(null);

		tmp = ActionsMenu.add(NewAgent);
		tmp.setIcon(null);
		
		tmp = ActionsMenu.add(start);
		tmp.setIcon(null);
		
		tmp = ActionsMenu.add(kill);
		tmp.setIcon(null);
		
		tmp = ActionsMenu.add(suspend);
		tmp.setIcon(null);
		
		tmp = ActionsMenu.add(resume);
		tmp.setIcon(null);
		
		tmp = ActionsMenu.add(ping);
		tmp.setIcon(null);
		
		tmp = ActionsMenu.add(custom);
		tmp.setIcon(null);
		
		tmp = ActionsMenu.add(sniffer);
		tmp.setIcon(null);
				
		addPopupMenuListener(parent);
		add(ActionsMenu);
		
	}

	/**
	 * This method is called to enable and disable some actions 
	 * depending from the item selected in the AMSTree
	 @param Level is the level of the Node selected
	 */ 

	public boolean setMenu(int Level)
	{		
		boolean flag = true;

		switch(Level)
		{
			case TreeData.AGENT:
				NewAgentPlatform.setEnabled(false);				
				NewAgent.setEnabled(false);
				custom.setEnabled(true);
				resume.setEnabled(true);
				kill.setEnabled(true);
				ping.setEnabled(true);
				properties.setEnabled(true);
				sniffer.setEnabled(true);
				start.setEnabled(true);
				suspend.setEnabled(true);
			break;
			case TreeData.CONTAINER:
				NewAgentPlatform.setEnabled(false);
				NewAgent.setEnabled(true);
				custom.setEnabled(false);
				resume.setEnabled(false);
				kill.setEnabled(false);
				ping.setEnabled(false);
				properties.setEnabled(false);
				sniffer.setEnabled(false);
				suspend.setEnabled(false);
				start.setEnabled(false);
			break;
			case TreeData.AGENT_PLATFORM:
				NewAgentPlatform.setEnabled(false);
				NewAgent.setEnabled(true);
				start.setEnabled(false);
				resume.setEnabled(true);
				kill.setEnabled(true);
				custom.setEnabled(true);
				ping.setEnabled(true);
				properties.setEnabled(true);
				sniffer.setEnabled(true);
				suspend.setEnabled(true);
				NewAgentPlatform.setEnabled(false);
			break;
			default:
				NewAgentPlatform.setEnabled(true);
				properties.setEnabled(false);
				NewAgent.setEnabled(false);
				custom.setEnabled(false);
				resume.setEnabled(false);
				kill.setEnabled(false);
				ping.setEnabled(false);
				properties.setEnabled(false);
				sniffer.setEnabled(false);
				suspend.setEnabled(false);
				start.setEnabled(false);
			break;
		}
		return flag;
	}


}

