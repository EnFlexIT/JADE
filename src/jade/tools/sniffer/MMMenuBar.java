/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop 
multi-agent systems in compliance with the FIPA specifications.
Copyright (C) 2000 CSELT S.p.A. 

GNU Lesser General Public License

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation, 
version 2.1 of the License. 

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the
Free Software Foundation, Inc., 59 Temple Place - Suite 330,
Boston, MA  02111-1307, USA.
*****************************************************************/



package jade.tools.sniffer;

import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JFrame;

import jade.gui.AboutJadeAction;

/**
 * Sets up the menu bar and the relative menus
 * @author Gianluca Tanca
 * @version $Date$ $Revision$
 */
public class MMMenuBar extends JMenuBar{
	
	/**
	@serial
	*/
	private SnifferGUI myGui; 
	
	public MMMenuBar(SnifferGUI snifferGui){
		
		super();

		myGui = snifferGui; 

		MMAbstractAction act;
		JMenu jmenu = new JMenu("Actions");
	  JMenuItem tmp;

		act = new AddRemoveAgentAction();
		tmp = jmenu.add(act);

		jmenu.addSeparator(); 

		act = new ClearCanvasAction();
		tmp = jmenu.add(act);
		
		jmenu.addSeparator(); 
		
		act = new DisplayLogFileAction();
		tmp = jmenu.add(act);

		act = new WriteLogFileAction();
		tmp = jmenu.add(act);

		act = new WriteMessageListAction();
		tmp = jmenu.add(act);

		jmenu.addSeparator(); 
		
		act = new ExitAction(myGui);
		tmp = jmenu.add(act);

		add(jmenu);
		
		jmenu = new JMenu("Help"); 
		act = new AboutBoxAction(myGui);
		tmp = jmenu.add(act);
		tmp = jmenu.add(new AboutJadeAction((JFrame)myGui));
		
		add(jmenu);
	}

}

        