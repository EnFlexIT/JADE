package jade.tools.sniffer;

import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

/**
 * Sets up the menu bar and the relative menus
 */
public class MMMenuBar extends JMenuBar{
	
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
				
		add(jmenu);
	}

}

        