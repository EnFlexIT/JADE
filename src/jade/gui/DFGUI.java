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

package jade.gui;

// Import required Java classes 
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.table.*;
import java.util.*;

// Import required Jade classes
import jade.domain.*;

/**
@author Giovanni Caire - CSELT S.p.A.
@version $Date$ $Revision$
*/

public class DFGUI extends JFrame
{
	GUI2DFCommunicatorInterface myAgent;
	AgentNameTableModel         registeredModel, foundModel;
	JTable                      registeredTable, foundTable;
	JSplitPane                  tablePane;

	// CONSTRUCTORS
	public DFGUI(GUI2DFCommunicatorInterface a) 
	{
		//////////////////////////
		// Initialization
		super();
    setSize(505,405);
		setTitle("DF: " + a.getLocalName());
		myAgent = a;

		/////////////////////////////////////
		// Add main menu to the GUI window
		JMenuBar jmb = new JMenuBar();
		JMenuItem item;

		JMenu generalMenu = new JMenu ("General");
		item = generalMenu.add(new DFGUIExitDFAction(this));
		item = generalMenu.add(new DFGUICloseGuiAction(this));
		jmb.add (generalMenu);

		JMenu catalogueMenu = new JMenu ("Catalogue");
		item = catalogueMenu.add(new DFGUIViewAction(this));
		item = catalogueMenu.add(new DFGUIModifyAction(this));
		item = catalogueMenu.add(new DFGUIDeregisterAction(this));
		item = catalogueMenu.add(new DFGUIRegisterAction(this));
		item = catalogueMenu.add(new DFGUISearchAction(this));
		jmb.add (catalogueMenu);
		
		JMenu superDFMenu = new JMenu ("Super DF");
		item = superDFMenu.add(new DFGUIFederateAction(this));
		jmb.add (superDFMenu);

		JMenu helpMenu = new JMenu ("Help");
		item = helpMenu.add(new DFGUIAboutAction(this));
		item = helpMenu.add(new AboutJadeAction(this));
		jmb.add (helpMenu);

		setJMenuBar(jmb);

		/////////////////////////////////////////////////////
		// Add Toolbar to the NORTH part of the border layout 
		JToolBar bar = new JToolBar();

		// GENERAL
		Icon exitImg = DFGuiProperties.getIcon("exitdf");
		JButton exitB  = bar.add(new DFGUIExitDFAction(this));
		exitB.setText("");
		exitB.setIcon(exitImg);
		exitB.setToolTipText("Exit and kill the DF agent");

		Icon closeImg = DFGuiProperties.getIcon("closegui");
		JButton closeB  = bar.add(new DFGUICloseGuiAction(this));
		closeB.setText("");
		closeB.setIcon(closeImg);
		closeB.setToolTipText("Close the DF GUI");

		bar.addSeparator();

		// CATALOGUE
		Icon viewImg = DFGuiProperties.getIcon("view");
		JButton viewB  = bar.add(new DFGUIViewAction(this));
		viewB.setText("");
		viewB.setIcon(viewImg);
		viewB.setToolTipText("View the services provided by the selected agent");
											
		Icon modifyImg = DFGuiProperties.getIcon("modify");
		JButton modifyB = bar.add(new DFGUIModifyAction(this));
		modifyB.setText("");
		modifyB.setIcon(modifyImg);
		modifyB.setToolTipText("Modify the services provided by the selected agent");

		Icon deregImg = DFGuiProperties.getIcon("deregister");
		JButton deregB  = bar.add(new DFGUIDeregisterAction(this));
		deregB.setText("");
		deregB.setIcon(deregImg);
		deregB.setToolTipText("Deregister the selected agent");

		Icon regNewImg = DFGuiProperties.getIcon("registeragent");
		JButton regNewB  = bar.add(new DFGUIRegisterAction(this));
		regNewB.setText("");
		regNewB.setIcon(regNewImg);
		regNewB.setToolTipText("Register a new agent with this DF");

		Icon searchImg = DFGuiProperties.getIcon("search");
		JButton searchB  = bar.add(new DFGUISearchAction(this));
		searchB.setText("");
		searchB.setIcon(searchImg);
		searchB.setToolTipText("Search for agents matching a given description");

		bar.addSeparator();

		// SUPER DF
		Icon fedDFImg = DFGuiProperties.getIcon("federatedf");
		JButton fedDFB  = bar.add(new DFGUIFederateAction(this));
		fedDFB.setText("");
		fedDFB.setIcon(fedDFImg);
		fedDFB.setToolTipText("Federate this DF with another DF");

		bar.addSeparator();

		// HELP
		Icon aboutImg = DFGuiProperties.getIcon("about");
		JButton aboutB  = bar.add(new DFGUIAboutAction(this));
		aboutB.setText("");
		aboutB.setIcon(aboutImg);
		aboutB.setToolTipText("About DF");

		getContentPane().add(bar, BorderLayout.NORTH);

		////////////////////////////////////////////////////
		// Table Pane to the Center part
		tablePane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		tablePane.setContinuousLayout(true);

		//////////////////////////////
		// Registered agents table
		JPanel p = new JPanel();
		p.setLayout(new BorderLayout());
		registeredModel = new AgentNameTableModel();
		registeredTable = new JTable(registeredModel); 
		registeredTable.setRowHeight(20);
		registeredTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		// Column names
		TableColumn c;
		c = registeredTable.getColumn((Object) registeredTable.getColumnName(0));
		c.setHeaderValue((Object) (new String("Agent name")));
		c = registeredTable.getColumn((Object) registeredTable.getColumnName(1));
		c.setHeaderValue((Object) (new String("Host:port")));
		c = registeredTable.getColumn((Object) registeredTable.getColumnName(2));
		c.setHeaderValue((Object) (new String("Agent address")));

		// Doubleclick = view
		MouseListener mouseListener = new MouseAdapter() 
		{
     			public void mouseClicked(MouseEvent e)
			{
				if (e.getClickCount() == 2)
				{
					DFGUIViewAction ac = new DFGUIViewAction(DFGUI.this);
					ac.actionPerformed(new ActionEvent((Object) this, 0, "View"));
				}  
			} 
 		};
 		registeredTable.addMouseListener(mouseListener); 
		// Press Del = Deregister
		KeyListener keyListener = new KeyAdapter()
		{
			public void keyPressed(KeyEvent e)
			{
				int code = e.getKeyCode();
				if (code == KeyEvent.VK_CANCEL || code == KeyEvent.VK_DELETE)
				{
					DFGUIDeregisterAction ac = new DFGUIDeregisterAction(DFGUI.this);
					ac.actionPerformed(new ActionEvent((Object) this, 0, "Deregister"));
				}

			}
		}; 
 		registeredTable.addKeyListener(keyListener);

		p.setLayout(new BorderLayout());
		JScrollPane pane = new JScrollPane();
		pane.getViewport().setView(registeredTable); 
		p.add(pane, BorderLayout.CENTER);
		p.setBorder(BorderFactory.createTitledBorder("Agents registered with this DF"));
		
		tablePane.setTopComponent(p);
		
		/////////////////////////
		// Search result table
		p = new JPanel();
		p.setLayout(new BorderLayout());
		foundModel = new AgentNameTableModel();
		foundTable = new JTable(foundModel); 
		foundTable.setRowHeight(20);
		foundTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		foundTable.setToolTipText("Double click on agent name to view the services provided by the selected agent");
		// Column names
		c = foundTable.getColumn((Object) foundTable.getColumnName(0));
		c.setHeaderValue((Object) (new String("Agent name")));
		c = foundTable.getColumn((Object) foundTable.getColumnName(1));
		c.setHeaderValue((Object) (new String("Host:port")));
		c = foundTable.getColumn((Object) foundTable.getColumnName(2));
		c.setHeaderValue((Object) (new String("Agent address")));
	
			// Doubleclick = view
		MouseListener mouseListener2 = new MouseAdapter() 
		{
     			public void mouseClicked(MouseEvent e)
			{
				if (e.getClickCount() == 2)
				{
					DFGUISearchViewAction ac = new DFGUISearchViewAction(DFGUI.this);
					ac.actionPerformed(new ActionEvent((Object) this, 0, "SearchView"));
				}  
			} 
 		};
 		foundTable.addMouseListener(mouseListener2); 


		p.setLayout(new BorderLayout());
		pane = new JScrollPane();
		pane.getViewport().setView(foundTable); 
		p.add(pane, BorderLayout.CENTER);
		p.setBorder(BorderFactory.createTitledBorder("Last search result"));
		
		tablePane.setBottomComponent(p);
		tablePane.setDividerLocation(150);
		getContentPane().add(tablePane, BorderLayout.CENTER); 
		
		////////////////////////////////////////////////////////////////
		// Execute the Close GUI action when the user attempts to close 
		// the DF GUI window using the button on the upper right corner
    		addWindowListener(new	WindowAdapter()
		                      	{
  							public void windowClosing(WindowEvent e) 
							{
								DFGUICloseGuiAction ac = new DFGUICloseGuiAction(DFGUI.this);
								ac.actionPerformed(new ActionEvent((Object) this, 0, "Close GUI"));
							}
						} );
	}

  //FIXME. Dummy method. We should add a textfield to display error messages
  public void showErrorMsg(String msg) {
    System.err.println(msg);
  }

	////////////////////////////////////
	// Refresh the DF GUI 
	public void refresh() 
	{
		registeredModel.clear();
		Enumeration registered = myAgent.getAllDFAgentDsc();
		while (registered.hasMoreElements())
		{
			AgentManagementOntology.DFAgentDescriptor dfd = (AgentManagementOntology.DFAgentDescriptor) registered.nextElement();
			registeredModel.add(dfd.getName());
		}
		registeredModel.fireTableDataChanged();		
	}

	public void refreshLastSearch(Enumeration e){
		foundModel.clear();
		while(e.hasMoreElements()){
		AgentManagementOntology.DFAgentDescriptor dfd = (AgentManagementOntology.DFAgentDescriptor) e.nextElement();
		foundModel.add(dfd.getName());
		}
		foundModel.fireTableDataChanged();
	}
	////////////////////////////////////
	// Show DF GUI properly
	public void setVisible(boolean b) 
	{
		if(b) 
		{
			setLocation(50, 50);
		}
		super.setVisible(b);
	}

	/////////////////////////////////////////////////////////////////
	// Perform asynchronous disposal to avoid nasty InterruptedException
	// printout.
	public void disposeAsync() 
	{

		class disposeIt implements Runnable 
		{
			private Window toDispose;

			public disposeIt(Window w) 
			{
				toDispose = w;
			}

			public void run() 
			{
				toDispose.dispose();
			}

		}

		// Make AWT Event Dispatcher thread dispose DF window for us.
		EventQueue.invokeLater(new disposeIt(this));
	}

}

