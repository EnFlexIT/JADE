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
	AgentNameTableModel         registeredModel, foundModel,parentModel,childrenModel;
	JTable                      registeredTable, foundTable,parentTable,childrenTable;
	JSplitPane                  tablePane;
	JTabbedPane                 tabbedPane;
	JButton                     modifyB,deregB,regNewB,fedDFB,viewB,searchB;
	DFGUIModifyAction           dfModifyAction;
	DFGUIViewAction             dfViewAction;
	DFGUISearchAction           dfSearchAction; 
	DFGUIRegisterAction         dfRegAction;
	DFGUIDeregisterAction       dfDeregAction;
	DFGUIFederateAction         dfFedAction;

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
		dfModifyAction = new DFGUIModifyAction(this);
		dfViewAction =  new DFGUIViewAction(this);
		dfDeregAction = new DFGUIDeregisterAction(this);
		dfRegAction = new DFGUIRegisterAction(this);
		dfSearchAction = new DFGUISearchAction(this); 
		item = catalogueMenu.add(dfViewAction);
		item = catalogueMenu.add(dfModifyAction);
		item = catalogueMenu.add(dfDeregAction);
		item = catalogueMenu.add(dfRegAction);
		item = catalogueMenu.add(dfSearchAction);
		jmb.add (catalogueMenu);
		
		JMenu superDFMenu = new JMenu ("Super DF");
		dfFedAction = new DFGUIFederateAction(this);
		item = superDFMenu.add(dfFedAction);
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
		viewB  = bar.add(new DFGUIViewAction(this));
		viewB.setText("");
		viewB.setIcon(viewImg);
		viewB.setToolTipText("View the services provided by the selected agent");
											
		Icon modifyImg = DFGuiProperties.getIcon("modify");
		modifyB = bar.add(new DFGUIModifyAction(this));
		modifyB.setText("");
		modifyB.setIcon(modifyImg);
		modifyB.setToolTipText("Modify the services provided by the selected agent");

		Icon deregImg = DFGuiProperties.getIcon("deregister");
		deregB  = bar.add(new DFGUIDeregisterAction(this));
		deregB.setText("");
		deregB.setIcon(deregImg);
		deregB.setToolTipText("Deregister the selected agent");

		Icon regNewImg = DFGuiProperties.getIcon("registeragent");
		regNewB  = bar.add(new DFGUIRegisterAction(this));
		regNewB.setText("");
		regNewB.setIcon(regNewImg);
		regNewB.setToolTipText("Register a new agent with this DF");

		Icon searchImg = DFGuiProperties.getIcon("search");
		searchB  = bar.add(new DFGUISearchAction(this));
		searchB.setText("");
		searchB.setIcon(searchImg);
		searchB.setToolTipText("Search for agents matching a given description");

		bar.addSeparator();

		// SUPER DF
		Icon fedDFImg = DFGuiProperties.getIcon("federatedf");
		fedDFB  = bar.add(new DFGUIFederateAction(this));
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
		// tablePane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		// tablePane.setContinuousLayout(true);

		////////////////////////////////////////////////////
		// JTabbedPane
		////////////////////////////////////////////////////
		tabbedPane = new JTabbedPane();
		
		//////////////////////////////
		// Registered agents table
		JPanel registerPanel = new JPanel();
		registerPanel.setLayout(new BorderLayout());
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

		registerPanel.setLayout(new BorderLayout());
		JScrollPane pane = new JScrollPane();
		pane.getViewport().setView(registeredTable); 
		registerPanel.add(pane, BorderLayout.CENTER);
		registerPanel.setBorder(BorderFactory.createEtchedBorder());
		
		tabbedPane.addTab("Agent registered with the DF",registerPanel);
		tabbedPane.setSelectedIndex(0);
		
		/////////////////////////
		// Search result table
		JPanel lastSearchPanel = new JPanel();
		lastSearchPanel.setLayout(new BorderLayout());
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
					DFGUIViewAction ac = new DFGUIViewAction(DFGUI.this);
					ac.actionPerformed(new ActionEvent((Object) this, 0, "View"));
				}  
			} 
 		};
 		foundTable.addMouseListener(mouseListener2); 


		lastSearchPanel.setLayout(new BorderLayout());
		pane = new JScrollPane();
		pane.getViewport().setView(foundTable); 
		lastSearchPanel.add(pane, BorderLayout.CENTER);
		lastSearchPanel.setBorder(BorderFactory.createEtchedBorder());
			
		tabbedPane.addTab("Last Search Result",lastSearchPanel);	
		
		JSplitPane tablePane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		tablePane.setContinuousLayout(true);
		
		//////////////////////////////
		// Parent agents table
		JPanel parentPanel = new JPanel();
		parentPanel.setLayout(new BorderLayout());
		parentModel = new AgentNameTableModel();
		parentTable = new JTable(parentModel); 
		parentTable.setRowHeight(20);
		parentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		// Column names
	
		c = parentTable.getColumn((Object) parentTable.getColumnName(0));
		c.setHeaderValue((Object) (new String("Agent name")));
		c = parentTable.getColumn((Object) parentTable.getColumnName(1));
		c.setHeaderValue((Object) (new String("Host:port")));
		c = parentTable.getColumn((Object) parentTable.getColumnName(2));
		c.setHeaderValue((Object) (new String("Agent address")));

		MouseListener mouseListenerParent = new MouseAdapter()
		{
			public void mouseClicked(MouseEvent e)
			{
				if (e.getClickCount() >= 1)
					childrenTable.clearSelection();
				if (e.getClickCount() == 2)
				{
					DFGUIViewAction ac = new DFGUIViewAction(DFGUI.this);
					ac.actionPerformed(new ActionEvent((Object)this, 0, "View"));
				
				}
			}
		
		};
		
		parentTable.addMouseListener(mouseListenerParent);
		
		parentPanel.setLayout(new BorderLayout());
		JScrollPane pane1 = new JScrollPane();
		pane1.getViewport().setView(parentTable);
		parentPanel.add(pane1,BorderLayout.CENTER);
		parentPanel.setBorder(BorderFactory.createTitledBorder("Parents"));
		
		tablePane.setTopComponent(parentPanel);
		
		JPanel childrenPanel = new JPanel();
		childrenPanel.setLayout(new BorderLayout());
		childrenModel = new AgentNameTableModel();
		childrenTable = new JTable(childrenModel); 
		childrenTable.setRowHeight(20);
		childrenTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
	  // Column names
		c = childrenTable.getColumn((Object) childrenTable.getColumnName(0));
		c.setHeaderValue((Object) (new String("Agent name")));
		c = childrenTable.getColumn((Object) childrenTable.getColumnName(1));
		c.setHeaderValue((Object) (new String("Host:port")));
		c = childrenTable.getColumn((Object) childrenTable.getColumnName(2));
		c.setHeaderValue((Object) (new String("Agent address")));
	
		MouseListener mouseListenerChildren = new MouseAdapter()
		{
			public void mouseClicked(MouseEvent e)
			{
				if (e.getClickCount() >= 1)
					parentTable.clearSelection();
				if (e.getClickCount() ==2)
					{
						DFGUIViewAction ac = new DFGUIViewAction(DFGUI.this);
					  ac.actionPerformed(new ActionEvent((Object)this,0, "View"));
					}
			}
		
		};
		
		childrenTable.addMouseListener(mouseListenerChildren);

    pane1 = new JScrollPane();
    pane1.getViewport().setView(childrenTable);
    childrenPanel.add(pane1,BorderLayout.CENTER);
    childrenPanel.setBorder(BorderFactory.createTitledBorder("Children"));
    
    tablePane.setBottomComponent(childrenPanel);
    tablePane.setDividerLocation(150);
    
    
		tabbedPane.addTab("DF Federation",tablePane);
    tabbedPane.addChangeListener(new tabListener());
	
		getContentPane().add(tabbedPane, BorderLayout.CENTER);
		
		
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
	
  class tabListener implements ChangeListener
  {
  	public void stateChanged(ChangeEvent event)
  	{
  		Object object = event.getSource();
  		if (object == tabbedPane)
  			tabStateChanged(event);
  	
  	}
  	
  	public void tabStateChanged(ChangeEvent event)
  	{
  		int index = tabbedPane.getSelectedIndex();
  		setButton(index);
	  	
  	}
  
  }
  
  //FIXME. Dummy method. We should add a textfield to display error messages
  public void showErrorMsg(String msg) {
    System.err.println(msg);
  }

	private void setButton(int tab)
	{
		switch (tab){
		
			case 0: setSearch(true);
							setDeregister(true);
							setRegister(true);
							setModify(true);
							setDFfed(true);
							break;
							
			case 1: setSearch(true);
						  setDeregister(false);
						  setRegister(false);
					    setModify(false);
							setDFfed(false);
							break;
		
		  case 2: setSearch(true);
							setDeregister(true);
							setRegister(false);
							setModify(false);
              setDFfed(true);
							break;		
		}
	}
	
	private void setRegister(boolean value)
	{
	  regNewB.setEnabled(value);
		dfRegAction.setEnabled(value);

	}
	
	
	private void setModify(boolean value)
	{
		modifyB.setEnabled(value);
	  dfModifyAction.setEnabled(value);

	}
	
	private void setDeregister(boolean value)
	{	
		deregB.setEnabled(value);
	  dfDeregAction.setEnabled(value);

	}
		
	private void setSearch(boolean value)
	{
		searchB.setEnabled(value);
		dfSearchAction.setEnabled(value);

	}

	private void setDFfed(boolean value)
	{
	  fedDFB.setEnabled(value);
	  dfFedAction.setEnabled(value);
	}
	
	public void setTab (String tab) 
	{
		if (tab.equalsIgnoreCase("Search"))
			tabbedPane.setSelectedIndex(1);
			else
			if (tab.equalsIgnoreCase("Federate"))
				tabbedPane.setSelectedIndex(2);
				else
				 tabbedPane.setSelectedIndex(0);
			
	}
	
	public String getSelectedAgentInTable()
	{
		String out = null;
		int tab = tabbedPane.getSelectedIndex();
		int row;
		if (tab == 0)
		{
			row = registeredTable.getSelectedRow();
			if ( row != -1)
				out = registeredModel.getElementAt(row);
				else out = null;
		}
		else
		if ( tab == 1)
		{
			row = foundTable.getSelectedRow();
			if (row != -1)
				out = foundModel.getElementAt(row);
				else
				out = null;
		}
		else 
		if (tab == 2)
		{
		   row = parentTable.getSelectedRow();
		   if (row != -1)
		   	out = parentModel.getElementAt(row);
		   	else
		   	{
		   		row = childrenTable.getSelectedRow();
		   	  if (row != -1)
		   	  	out = childrenModel.getElementAt(row);
		   	  	else out = null;
		   	}
		   		
		}
	
		return out;
	}
	
	public int kindOfOperation()
	{
	
		int out = -1;
		int tab = tabbedPane.getSelectedIndex();

		if (tab == 0)
			out = 0; //deregister an agent from descriptor table
			else if(tab == 1)
				out = 1; // deregister from lastsearch view (NOW NOT Possible)
				else if (tab == 2)
				{
					int rowSelected = parentTable.getSelectedRow();
					if (rowSelected != -1)
						out = 2; //deregister the df from a parent
						else
						{
							rowSelected = childrenTable.getSelectedRow();
						  if (rowSelected != -1) 
							  out = 3; //deregister a children
						}
				}
		System.out.println("out: "+ out);		
		return out;
						
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
		refreshFederation();
	}

	public void refreshLastSearch(Enumeration e){
		foundModel.clear();
		while(e.hasMoreElements()){
		AgentManagementOntology.DFAgentDescriptor dfd = (AgentManagementOntology.DFAgentDescriptor) e.nextElement();
		foundModel.add(dfd.getName());
		}
		foundModel.fireTableDataChanged();
	}
	
	public void refreshFederation()
	{
		parentModel.clear();
		Enumeration parent = myAgent.getParents();
		while (parent.hasMoreElements())
		{
			parentModel.add((String)parent.nextElement());
		}
		parentModel.fireTableDataChanged();
		
		childrenModel.clear();
		Enumeration children  = myAgent.getChildren();
		while(children.hasMoreElements())
		{
			childrenModel.add((String)children.nextElement());
		
		}
		childrenModel.fireTableDataChanged();
		
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
