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

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.JTree;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.KeyEvent;
import java.awt.Window;
import java.awt.EventQueue;
import java.util.Vector;

/**
 * When the user clicks on the Add/Remove Agent icon on the toolbar or selects the  Add/Remove
 * Agent item on the menu a new instance of this class is created and 
 * Created the Add/Remove Agent windows with tree and menu component. Also registers action
 * listeners and, in future, popup menu.
 *
 * 
 * @author <a href="mailto:alessandro.beneventi@re.nettuno.it"> Alessandro Beneventi </a>
 * @version $Date$ $Revision$
 */
public class AgentFrame extends JFrame implements ActionListener {

  /**
  @serial
  */
	private String newline = "\n";
	
	/**
	@serial
	*/
	private MMTree theMMTree = MMAbstractAction.selTree;
	
	/**
	@serial
	*/
	private JPopupMenu popup;
	
	/**
	@serial
	*/
	private MouseListener popupListener;


	/**
	 * The constructor for the <em>Selection Agents</em> window.
	 */
	public AgentFrame(String type){
		super(type);
		
		theMMTree = MMAbstractAction.selTree;		
		
    JMenuBar menuBar;
    JMenu menu;
    JMenuItem menuItem;

    menuBar = new JMenuBar();
    setJMenuBar(menuBar);

    //Build the first menu.
    menu = new JMenu("Actions");
    menu.setMnemonic(KeyEvent.VK_A);
    menu.getAccessibleContext().setAccessibleDescription(
	    "Sniffing Actions");
    menuBar.add(menu);

    menuItem = new JMenuItem("Do sniff this agent(s)",
                                 KeyEvent.VK_S);
    menuItem.setAccelerator(KeyStroke.getKeyStroke(
	    KeyEvent.VK_S, ActionEvent.ALT_MASK));
    menuItem.getAccessibleContext().setAccessibleDescription(
  	  "on");
    menuItem.addActionListener(this);
    menu.add(menuItem);

    menuItem = new JMenuItem("Do not sniff this agent(s)",
                                 KeyEvent.VK_N);
    menuItem.setAccelerator(KeyStroke.getKeyStroke(
	    KeyEvent.VK_N, ActionEvent.ALT_MASK));
    menuItem.getAccessibleContext().setAccessibleDescription(
  	  "off");
    menuItem.addActionListener(this);
    menu.add(menuItem);

		menu.addSeparator();    

    menuItem = new JMenuItem("Close Window",
                                 KeyEvent.VK_C);
    menuItem.setAccelerator(KeyStroke.getKeyStroke(
	    KeyEvent.VK_Q, ActionEvent.ALT_MASK));
    menuItem.getAccessibleContext().setAccessibleDescription(
  	  "close");
    menuItem.addActionListener(this);
    menu.add(menuItem);

    
    /* PopUpMenu code starts here */

        popup = new JPopupMenu();
        menuItem = new JMenuItem("Do sniff this agent(s)");
        menuItem.addActionListener(this);
        popup.add(menuItem);
        menuItem = new JMenuItem("Do not sniff this agent(s)");
        menuItem.addActionListener(this);
        popup.add(menuItem);

        //Add listener to components that can bring up popup menus.
        MouseListener popupListener = new PopupListener();
        // MMAbstractAction.selTree.tree.addMouseListener(popupListener);
        // scrollPane.addMouseListener(popupListener);
        // menuBar.addMouseListener(popupListener);

		/* PopUpMenu ends here */

	}

  /*
   * This method is invoked everytime the user wants to put an agent on the Agent
   * Canvas. At first, it checks if the agent is already on canvas: if not so, it
   * removes its address and put it on canvas
   */
	private void putAgentOnCanvas(TreeData treeAgent){
		
			Agent agent;

				if(treeAgent.isLeaf()==true) 
				{   
					int atPos = treeAgent.getName().indexOf("@"); 
					if ((atPos == -1)&&(MMAbstractAction.canvasAgent.isPresent(treeAgent.getName())==false)
							||
						(atPos != -1)&&(MMAbstractAction.canvasAgent.isPresent(treeAgent.getName().substring(0,atPos))==false))
					{ 
						
							if ( atPos == -1 )
								agent = new Agent(treeAgent.name);
							else
								agent = new Agent(treeAgent.name.substring(0,atPos));							
							MMAbstractAction.canvasAgent.addAgent(agent);
					}
				}
			//tree.scrollPathToVisible(path);
	
	}


  /*
   * This method is invoked everytime the user wants to remove an agent from the Agent
   * Canvas. At first, it checks if the agent is already on canvas: if not so, it
   * removes its address and put it on canvas
   */
	private void removeAgentFromCanvas(TreeData treeAgent){
		
	  Agent agent;
	  
	  if(treeAgent.isLeaf()==true) 
	    {   
	      int atPos = treeAgent.getName().indexOf("@"); 
	      if ((atPos == -1)&&(MMAbstractAction.canvasAgent.isPresent(treeAgent.getName())==false)
		  ||
		  (atPos != -1)&&(MMAbstractAction.canvasAgent.isPresent(treeAgent.getName().substring(0,atPos))==false))
		{ 
		} else {
		  if ( atPos == -1 )
		    MMAbstractAction.canvasAgent.removeAgent(treeAgent.getName());
		  else
		    MMAbstractAction.canvasAgent.removeAgent(treeAgent.getName().substring(0,atPos));
		}
	    }
	  //tree.scrollPathToVisible(path);
	
	}


  public void actionPerformed(ActionEvent e) {
        JMenuItem source = (JMenuItem)(e.getSource());
        String act = source.getAccessibleContext().getAccessibleDescription();
        Vector listeners = MMAbstractAction.getAllListeners();
        
        if (act.equals("on")) {
        	/* We have been told to put some agents on canvas: let's do it */
        	for (int i = 0; i < listeners.size(); i++){
						TreeData cur = (TreeData)listeners.elementAt(i);
						putAgentOnCanvas(cur);
					}
					/* and sniff those agents */
					SnifferGUI.sniffHandler.sniffMsg(listeners,Sniffer.SNIFF_ON);        	
        }
        if (act.equals("off")) {
        	/* We have been told to disable sniffer for some agents */
        	for (int i = 0; i < listeners.size(); i++){
		  TreeData cur = (TreeData)listeners.elementAt(i);
		  removeAgentFromCanvas(cur);
		}
		SnifferGUI.sniffHandler.sniffMsg(listeners,Sniffer.SNIFF_OFF);       	        	
        }
        if (act.equals("close")) {
     			/* this is to close the Selection Agents window */
        	class disposeIt implements Runnable {
      			private Window toDispose;

      			public disposeIt(Window w) {
							toDispose = w;
      			}

      			public void run() {
							toDispose.dispose();
      			}
    			}
    			EventQueue.invokeLater(new disposeIt(this));
  
        }      
  }

	public void registerPopUp(JTree treeDef) {
		if (treeDef != null)
			treeDef.addMouseListener(popupListener);
		else
			System.out.println("Puntatore Nullo");		

	}

  protected String getClassName(Object o) {
      String classString = o.getClass().getName();
      int dotIndex = classString.lastIndexOf(".");
      return classString.substring(dotIndex+1);
  }
	
	
	    class PopupListener extends MouseAdapter {
        public void mousePressed(MouseEvent e) {
            maybeShowPopup(e);
        }

        public void mouseReleased(MouseEvent e) {
            maybeShowPopup(e);
        }

        private void maybeShowPopup(MouseEvent e) {
            if (e.isPopupTrigger()) {
                popup.show(e.getComponent(),
                           e.getX(), e.getY());
            }
        }

	    }	
}
