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
* This class implements the GUI of the Directory Facilitator.
* The gui shows a tabbed pane with three different views of the functions  
* provided by a Directory Facilitator.
* The three views are: <ul>
* <li><b>Agents registered with the DF</b> shows a table with all the agents 
* registered with the DF.
* <li><b>Last Search Result</b> shows a table with the list of agent descriptions that
* were returned as a result of the last search operation.
* <li><b>DF Federation</b> shows the DF federation. The Parents table shows the list of DF's 
* with which this
* DF is federated, while the Children table shows the list of DF's 
* that are registered with this DF.</ul>
* According to the tab selected, only some actions are allowed:
* <ol><b>Agents registered with the df</b>.
* <ul>
* <li><b>View</b> the description of the selected  agent from the table.
* <li><b>Modify</b> the description of the selected agent.
* <li><b>Register</b> an agent with the DF. The user is then requested to fill in 
* an agent description, notice that  
* some values are mandatory for registration,
* <li><b>Deregister</b> an agent selected in the table.
* <li><b>Search</b> for agent descriptions with this DF. 
* If no value is inserted in the agent description, the search action returns 
* all the active agents currently registered with this DF.
* <li><b>Search with constraints</b> allows to make a search with this DF 
* by adding further constraints, as specified by the FIPA specifications. 
* The constraints inserted are stored to avoid inserting them again for the next operation.
* Two kinds of constraints are permitted <code>df-depth</code>: 
* the depth of propagation of the search operation to the federated DF's,
* and the <code>resp-req</code>: the number of returned agent descriptions. 
* <li><b>Federate</b> allow to federate this DF with another DF. First of all, 
* the user must provide the full name of the DF with 
* which to federate and then the description of this DF that must be registered with the
* specified DF. </ul>
* <b>Last Search Result</b>
* <ul>
* <li><b>View</b> the description of a selected agent on the table of the results.
* <li><b>Search</b> for agent descriptions with this DF. (see above)
* <li><b>Search with Constraints</b> (see above).</ul>
* <b>DF Federation</b>
* <ul>
* <li><b>View</b> the description of an agent selected in one of the two tables.
* If the agent selected is a parent, then the default description 
* of this DF is shown. Otherwise if the selected agent is a child,
* then the description of this child DF is shown.
* <li><b>Deregister</b> If the selected agent is a parent then this DF is 
* deregistered from the selected one, 
* otherwise, if the agent selected is a child, this child is deregistered from this DF.
* <li><b>Search</b> permits to make a search with default constraint with the DF selected in one of the tables. 
* <li><b>Search with constraints</b> permits to make a search with constraints with the DF selected in one of the tables.
* <li><b>Federate</b> allows to federate this DF with the selected one.
*</ol>
* @author Giovanni Caire - Tiziana Trucco - CSELT S.p.A.
* @version $Date$ $Revision$
*/

public class DFGUI extends JFrame
{
	// class variables used to discriminate between the view of the dfgui.
	public static int AGENT_VIEW = 0;
	public static int LASTSEARCH_VIEW = 1;
	public static int PARENT_VIEW = 2;
	public static int CHILDREN_VIEW = 3;
	
	GUI2DFCommunicatorInterface myAgent;
	AgentNameTableModel         registeredModel, foundModel,parentModel,childrenModel;
	JTable                      registeredTable, foundTable,parentTable,childrenTable;
	JSplitPane                  tablePane;
	JTabbedPane                 tabbedPane;
	JButton                     modifyB,deregB,regNewB,fedDFB,viewB,searchB,searchWithB;
	JTextField                  statusField;
	JScrollPane                 textScroll;
	DFGUIModifyAction           dfModifyAction;
	DFGUIViewAction             dfViewAction;
	DFGUISearchAction           dfSearchAction; 
	DFGUIRegisterAction         dfRegAction;
	DFGUIDeregisterAction       dfDeregAction;
	DFGUIFederateAction         dfFedAction;
  DFGUISearchWithConstraintAction   dfSearchConstraintAction; 
  
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
		dfSearchConstraintAction = new DFGUISearchWithConstraintAction(this);
		
		item = catalogueMenu.add(dfViewAction);
		item = catalogueMenu.add(dfModifyAction);
		item = catalogueMenu.add(dfDeregAction);
		item = catalogueMenu.add(dfRegAction);
		item = catalogueMenu.add(dfSearchAction);
		item = catalogueMenu.add(dfSearchConstraintAction);
		
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

		Icon searchWithImg = DFGuiProperties.getIcon("searchwithconstraints");
		searchWithB = bar.add(new DFGUISearchWithConstraintAction(this));
		searchWithB.setText("");
		searchWithB.setIcon(searchWithImg);
		searchWithB.setToolTipText("Search for agent using constraints");
		
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
		
		tabbedPane.addTab("Agents registered with the DF",registerPanel);
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
		
		////////////////////////
		// Status message
		////////////////////////
		JPanel statusPanel = new JPanel();
		statusPanel.setLayout(new BorderLayout());
		statusPanel.setBorder(BorderFactory.createTitledBorder("Status"));
		statusField= new JTextField();
		statusField.setEditable(false);
		statusPanel.add(statusField, BorderLayout.CENTER);
		getContentPane().add(statusPanel, BorderLayout.SOUTH);
		
		
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
  
  //Use this method to show a message on the DF GUI
  public void showStatusMsg(String msg) {
    statusField.setText(msg);
  }

	private void setButton(int tab)
	{
		switch (tab){
		
			case 0: //setSearch(true);
							setDeregister(true);
							setRegister(true);
							setModify(true);
							setDFfed(true);
							break;
							
			case 1: //setSearch(true);
						  setDeregister(false);
						  setRegister(false);
					    setModify(false);
							setDFfed(false);
							break;
		
		  case 2: //setSearch(true);
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
		
	/*private void setSearch(boolean value)
	{
		searchB.setEnabled(value);
		dfSearchAction.setEnabled(value);

	}*/

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
			out = AGENT_VIEW; //operation from descriptor table
			else if(tab == 1)
				out = LASTSEARCH_VIEW; // operation from lastsearch view 
				else if (tab == 2)
				{
					int rowSelected = parentTable.getSelectedRow();
					if (rowSelected != -1)
						out = PARENT_VIEW; //OPERATION  from  parent table
						else
						{
							rowSelected = childrenTable.getSelectedRow();
						  if (rowSelected != -1) 
							  out = CHILDREN_VIEW; //OPERATION from children table
						}
				}
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
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			int centerX = (int)screenSize.getWidth() / 2;
			int centerY = (int)screenSize.getHeight() / 2;
			setLocation(centerX - getWidth() / 2, centerY - getHeight() / 2);
		
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