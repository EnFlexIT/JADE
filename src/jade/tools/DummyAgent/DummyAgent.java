///////////////////////////////////////////////////////////////
//   
//   /     /  ___/  ___/   / /_   _/ 
//  /  /--/___  /  ___/   /_/ / / /   
// /_____/_____/_____/_____/_/_/_/
// 
// -----------------------------------------------------------
// PROJECT:    DUMMY AGENT	
// FILE NAME:  DummyAgent.java	
// CONTENT:	   This file includes the definition of the DummyAgent class.
// AUTHORS:	   Giovanni Caire	
// RELEASE:	   4.0	
// MODIFIED:   22/04/1999	
// 
//////////////////////////////////////////////////////////////

package jade.tools.DummyAgent;

// Import necessary AWT and Swing classes 
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

// Import the java.util and java.io classes
import java.util.*;
import java.io.*;

// Import useful Jade classes
import jade.core.*;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.*;


public class DummyAgent extends Agent implements ActionListener
{
	String            agentName;
	JFrame            agentWnd;
	AclGui            currentMsgGui;
	DefaultListModel  queuedMsgListModel;
	JList             queuedMsgList;

	// Extends the Agent setup method
	protected void setup()
	{
		//////////////////
		// Get agent name
		agentName = getLocalName();

		///////////////////////
		// Create agent window
		agentWnd = new JFrame();
		agentWnd.setTitle(agentName + ":DummyAgent");
		agentWnd.getContentPane().setLayout(new BorderLayout());

		//////////////////////////////////////////////////////////////////////////////////////
		// Add the queued messages scroll pane to the CENTER part of the border layout manager
		queuedMsgListModel = new DefaultListModel();
		queuedMsgList = new JList(queuedMsgListModel);
        queuedMsgList.setCellRenderer(new ToFromCellRenderer());
		JScrollPane pane = new JScrollPane();
		pane.getViewport().setView(queuedMsgList);
		agentWnd.getContentPane().add("Center", pane);

		/////////////////////////////////////////////////////////////////////////////////
		// Add the current message AclPanel to the WEST part of the border layout manager
		currentMsgGui = new AclGui();
		currentMsgGui.setBorder(new TitledBorder("Current message"));
		ACLMessage msg = new ACLMessage("accept-proposal");
		msg.setSource(agentName);
		currentMsgGui.setMsg(msg);
		//JPanel tmpPanel = new JPanel();
		//tmpPanel.setLayout(new BoxLayout(tmpPanel, BoxLayout.Y_AXIS));
		//tmpPanel.add(currentMsgGui);
		//agentWnd.getContentPane().add("West", tmpPanel);
		agentWnd.getContentPane().add("West", currentMsgGui);

		/////////////////////////////////////
		// Add main menu to the agent window
		JMenuBar jmb = new JMenuBar();
		JMenuItem item;

		JMenu generalMenu = new JMenu ("General");
		generalMenu.add (item = new JMenuItem ("Exit"));
		item.addActionListener (this);
		jmb.add (generalMenu);


		JMenu currentMsgMenu = new JMenu ("Current message");
		currentMsgMenu.add (item = new JMenuItem ("New"));
		item.addActionListener (this);
		currentMsgMenu.add (item = new JMenuItem ("Open"));
		item.addActionListener (this);
		currentMsgMenu.add (item = new JMenuItem ("Save"));
		item.addActionListener (this);
		currentMsgMenu.addSeparator();
		currentMsgMenu.add (item = new JMenuItem ("Send"));
		item.addActionListener (this);
		jmb.add (currentMsgMenu);

		JMenu queuedMsgMenu = new JMenu ("Queued message");
		queuedMsgMenu.add (item = new JMenuItem ("View"));
		item.addActionListener (this);
		queuedMsgMenu.add (item = new JMenuItem ("Delete"));
		item.addActionListener (this);
		queuedMsgMenu.add (item = new JMenuItem ("Set as current"));
		item.addActionListener (this);
		queuedMsgMenu.add (item = new JMenuItem ("Open queue"));
		item.addActionListener (this);
		queuedMsgMenu.add (item = new JMenuItem ("Save queue"));
		item.addActionListener (this);
		jmb.add (queuedMsgMenu);

		agentWnd.setJMenuBar(jmb);

		/////////////////////////////////////////////////////
		// Add Toolbar to the NORTH part of the border layout 
		JToolBar bar = new JToolBar();

		Icon newImg = GuiProperties.getIcon("new");
		JButton newB = new JButton();
		newB.setText("New");
		newB.setIcon(newImg);
		newB.setToolTipText("New the current ACL message");
		newB.addActionListener(this);
		bar.add(newB);
											
		Icon openImg = GuiProperties.getIcon("open");
		JButton openB = new JButton();
		openB.setText("Open");
		openB.setIcon(openImg);
		openB.setToolTipText("Read the current ACL message from file");
		openB.addActionListener(this);
		bar.add(openB);

		Icon saveImg = GuiProperties.getIcon("save");
		JButton saveB = new JButton();
		saveB.setText("Save");
		saveB.setIcon(saveImg);
		saveB.setToolTipText("Save the current ACL message to file");
		saveB.addActionListener(this);
		bar.add(saveB);

		Icon sendImg = GuiProperties.getIcon("send");
		JButton sendB = new JButton();
		sendB.setText("Send");
		sendB.setIcon(sendImg);
		sendB.setToolTipText("Send the current ACL message");
		sendB.addActionListener(this);
		bar.add(sendB);

		bar.addSeparator();

		Icon viewImg = GuiProperties.getIcon("view");
		JButton viewB = new JButton();
		viewB.setText("View");
		viewB.setIcon(viewImg);
		viewB.setToolTipText("View the selected ACL message");
		viewB.addActionListener(this);
		bar.add(viewB);

		Icon deleteImg = GuiProperties.getIcon("delete");
		JButton deleteB = new JButton();
		deleteB.setText("Delete");
		deleteB.setIcon(deleteImg);
		deleteB.setToolTipText("Delete the selected ACL message");
		deleteB.addActionListener(this);
		bar.add(deleteB);

		Icon setImg = GuiProperties.getIcon("set");
		JButton setB = new JButton();
		setB.setText("Set as current");
		setB.setIcon(setImg);
		setB.setToolTipText("Set the selected ACL message to be the current message");
		setB.addActionListener(this);
		bar.add(setB);

		Icon openQImg = GuiProperties.getIcon("openq");
		JButton openQB = new JButton();
		openQB.setText("Open queue");
		openQB.setIcon(openQImg);
		openQB.setToolTipText("Read the queue of sent/received messages from file");
		openQB.addActionListener(this);
		bar.add(openQB);

		Icon saveQImg = GuiProperties.getIcon("saveq");
		JButton saveQB = new JButton();
		saveQB.setText("Save queue");
		saveQB.setIcon(saveQImg);
		saveQB.setToolTipText("Save the queue of sent/received messages to file");
		saveQB.addActionListener(this);
		bar.add(saveQB);

		agentWnd.getContentPane().add("North", bar);

		///////////////////////////////////
		// Arrange and display agent window
		agentWnd.pack();
		agentWnd.show();

		///////////////////////
		// Add agent behaviour
		Behaviour b = new DummyBehaviour(this);
		addBehaviour(b);	
	
	}

	///////////////////////////////////////////
	// Handlers for menu and toolbar commands
	public void actionPerformed(ActionEvent e)
	{
		String command = e.getActionCommand();
		if      (command == "New")
		{
			ACLMessage m = new ACLMessage("accept-proposal");
			m.setSource(agentName);
			currentMsgGui.setMsg(m);
		}
		else if (command == "Open")
		{
			JFileChooser chooser = new JFileChooser(); 
			int returnVal = chooser.showOpenDialog(null); 
			if(returnVal == JFileChooser.APPROVE_OPTION)
			{
				String fileName = chooser.getSelectedFile().getAbsolutePath();

				try
				{
					BufferedReader in = new BufferedReader(new FileReader(fileName));
					ACLParser parser = ACLParser.create();
					ACLMessage ACLmsg = parser.parse(in);
					currentMsgGui.setMsg(ACLmsg);
				}
				catch(FileNotFoundException e1) 
				{
					System.out.println("File Not Found: " + fileName);
				} 
				catch(ParseException e2)
				{
					System.out.println("Parse Exception");
				}
			} 
		}
		else if (command == "Save")
		{
			JFileChooser chooser = new JFileChooser(); 
			int returnVal = chooser.showSaveDialog(null); 
			if(returnVal == JFileChooser.APPROVE_OPTION)
			{
				String fileName = chooser.getSelectedFile().getAbsolutePath();

				try
				{
					BufferedWriter out = new BufferedWriter(new FileWriter(fileName));
					ACLMessage ACLmsg = currentMsgGui.getMsg();
					ACLmsg.toText(out);
				}
				catch(FileNotFoundException e3) 
				{
					System.out.println("Can't open file: " + fileName);
				} 
				catch(IOException e4) 
				{
					System.out.println("IO Exception");
				} 
			} 
		}
		else if (command == "Send")
		{
			ACLMessage m = currentMsgGui.getMsg();
			queuedMsgListModel.add(0, (Object) new MsgIndication(m, MsgIndication.OUTGOING, new Date()));
			try
			{
				Thread.sleep(1000);
			}
			catch(InterruptedException intE)
			{
				System.out.println("Interrupted exception");
			}
			send(m);
		}
		else if (command == "View")
		{
			int i = queuedMsgList.getSelectedIndex();
			if (i != -1)
			{
				MsgIndication mi = (MsgIndication) queuedMsgListModel.getElementAt(i);
				ACLMessage m = mi.getMessage();
				AclGui.showMsgInDialog(m, agentWnd);
				AclGui.editMsgInDialog(m, agentWnd);
			}
		}
		else if (command == "Delete")
		{
			int i = queuedMsgList.getSelectedIndex();
			if (i != -1)
			{
				queuedMsgListModel.removeElementAt(i);
			}
		}
		else if (command == "Set as current")
		{
			int i = queuedMsgList.getSelectedIndex();
			if (i != -1)
			{
				MsgIndication mi = (MsgIndication) queuedMsgListModel.getElementAt(i);
				ACLMessage m = mi.getMessage();
				currentMsgGui.setMsg(m);
			}
		}
		else if (command == "Open queue")
		{
		}
		else if (command == "Save queue")
		{
			JFileChooser chooser = new JFileChooser(); 
			int returnVal = chooser.showSaveDialog(null); 
			if(returnVal == JFileChooser.APPROVE_OPTION)
			{
				String fileName = chooser.getSelectedFile().getAbsolutePath();

				try
				{
					BufferedWriter out = new BufferedWriter(new FileWriter(fileName));
					int n = queuedMsgListModel.getSize();
					int i = 0;
					MsgIndication mi;
					while (i < n)
					{
						mi = (MsgIndication) queuedMsgListModel.get(i);
						mi.toText(out);
						System.out.println(i);
						++i;
					}
				}
				catch(FileNotFoundException e5) 
				{
					System.out.println("Can't open file: " + fileName);
				} 
				catch(IOException e6) 
				{
					System.out.println("IO Exception");
				} 
			} 
		}
		else if (command == "Exit")
		{
			System.out.println(agentName + ": bye!");
			agentWnd.dispose();
			doDelete();
		}
	}

}
