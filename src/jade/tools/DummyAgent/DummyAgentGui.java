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



package jade.tools.DummyAgent;

// Import required Java classes 
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

import java.util.*;
import java.io.*;

// Import required Jade classes
import jade.core.*;
import jade.lang.acl.*;
import jade.gui.*;

/**
@author Giovanni Caire - CSELT S.p.A
@version $Date$ $Revision$
*/
class DummyAgentGui extends JFrame implements ActionListener
{
	DummyAgent        myAgent;
	AID               agentName;
	AclGui            currentMsgGui;
	DefaultListModel  queuedMsgListModel;
	JList             queuedMsgList;
	File              currentDir;

	// Constructor
	DummyAgentGui(DummyAgent a)
	{
		//////////////////////////
		// Call JFrame constructor
		super();

		//////////////////////////////////////////////////////////
		// Store pointer to the Dummy agent controlled by this GUI
		myAgent = a;

		/////////////////////////////////////////////////////////////////////
		// Get agent name and initialize the saving/opening directory to null 
		agentName = myAgent.getAID();
		currentDir = null;

		////////////////////////////////////////////////////////////////
		// Prepare for killing the agent when the agent window is closed
    		addWindowListener(new	WindowAdapter()
		                      	{
							// This is executed when the user attempts to close the DummyAgent 
							// GUI window using the button on the upper right corner
  							public void windowClosing(WindowEvent e) 
							{
								myAgent.doDelete();
							}
						} );

		//////////////////////////
		// Set title in GUI window
		try{
			setTitle(agentName.getName() + ":DummyAgent");
		}catch(Exception e){setTitle("DummyAgent");}
		
		////////////////////////////////
		// Set GUI window layout manager
		getContentPane().setLayout(new BorderLayout());

		//////////////////////////////////////////////////////////////////////////////////////
		// Add the queued message scroll pane to the CENTER part of the border layout manager
		queuedMsgListModel = new DefaultListModel();
		queuedMsgList = new JList(queuedMsgListModel);
		queuedMsgList.setCellRenderer(new ToFromCellRenderer());
		JScrollPane pane = new JScrollPane();
		pane.getViewport().setView(queuedMsgList);
		getContentPane().add("Center", pane);

		///////////////////////////////////////////////////////////////////////////////////////////////////
		// Add the current message editing fields (an AclGui) to the WEST part of the border layout manager
		currentMsgGui = new AclGui();
		currentMsgGui.setBorder(new TitledBorder("Current message"));
		ACLMessage msg = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
		
		msg.setSender(agentName);
		
		currentMsgGui.setMsg(msg);
		getContentPane().add("West", currentMsgGui);

		/////////////////////////////////////
		// Add main menu to the GUI window
		JMenuBar jmb = new JMenuBar();
		JMenuItem item;

		JMenu generalMenu = new JMenu ("General");
		generalMenu.add (item = new JMenuItem ("Exit"));
		item.addActionListener (this);
		jmb.add (generalMenu);


		JMenu currentMsgMenu = new JMenu ("Current message");
		currentMsgMenu.add (item = new JMenuItem ("Reset"));
		item.addActionListener (this);
		currentMsgMenu.add (item = new JMenuItem ("Send"));
		item.addActionListener (this);
		currentMsgMenu.add (item = new JMenuItem ("Open"));
		item.addActionListener (this);
		currentMsgMenu.add (item = new JMenuItem ("Save"));
		item.addActionListener (this);
		currentMsgMenu.addSeparator();
		jmb.add (currentMsgMenu);

		JMenu queuedMsgMenu = new JMenu ("Queued message");
		queuedMsgMenu.add (item = new JMenuItem ("Open queue"));
		item.addActionListener (this);
		queuedMsgMenu.add (item = new JMenuItem ("Save queue"));
		item.addActionListener (this);
		queuedMsgMenu.add (item = new JMenuItem ("Set as current"));
		item.addActionListener (this);
		queuedMsgMenu.add (item = new JMenuItem ("Reply"));
		item.addActionListener (this);
		queuedMsgMenu.add (item = new JMenuItem ("View"));
		item.addActionListener (this);
		queuedMsgMenu.add (item = new JMenuItem ("Delete"));
		item.addActionListener (this);
		jmb.add (queuedMsgMenu);

		setJMenuBar(jmb);

		/////////////////////////////////////////////////////
		// Add Toolbar to the NORTH part of the border layout 
		JToolBar bar = new JToolBar();

		Icon resetImg = GuiProperties.getIcon("reset");
		JButton resetB = new JButton();
		resetB.setText("Reset");
		resetB.setIcon(resetImg);
		resetB.setToolTipText("New the current ACL message");
		resetB.addActionListener(this);
		bar.add(resetB);
											
		Icon sendImg = GuiProperties.getIcon("send");
		JButton sendB = new JButton();
		sendB.setText("Send");
		sendB.setIcon(sendImg);
		sendB.setToolTipText("Send the current ACL message");
		sendB.addActionListener(this);
		bar.add(sendB);

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

		bar.addSeparator();

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

		Icon setImg = GuiProperties.getIcon("set");
		JButton setB = new JButton();
		setB.setText("Set as current");
		setB.setIcon(setImg);
		setB.setToolTipText("Set the selected ACL message to be the current message");
		setB.addActionListener(this);
		bar.add(setB);

		Icon replyImg = GuiProperties.getIcon("reply");
		JButton replyB = new JButton();
		replyB.setText("Reply");
		replyB.setIcon(replyImg);
		replyB.setToolTipText("Prepare a message to reply to the selected message");
		replyB.addActionListener(this);
		bar.add(replyB);

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

		getContentPane().add("North", bar);
	}

	void showCorrect()
	{
		///////////////////////////////////////////
		// Arrange and display GUI window correctly
		pack();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int centerX = (int)screenSize.getWidth() / 2;
		int centerY = (int)screenSize.getHeight() / 2;
		setLocation(centerX - getWidth() / 2, centerY - getHeight() / 2);
		show();
	}

	///////////////////////////////////////////
	// Handlers for menu and toolbar commands
	public void actionPerformed(ActionEvent e)
	{
		String command = e.getActionCommand();

		// RESET
		if      (command.equals("Reset"))
		{
			ACLMessage m = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
			m.setSender(agentName);
			currentMsgGui.setMsg(m);
		}
		// SEND
		else if (command.equals("Send"))
		{
			ACLMessage m = currentMsgGui.getMsg();
			queuedMsgListModel.add(0, (Object) new MsgIndication(m, MsgIndication.OUTGOING, new Date()));
		  StringACLCodec codec = new StringACLCodec();
		  try {
		      codec.decode(codec.encode(m));
		      myAgent.send(m);
		  } catch (ACLCodec.CodecException ce) {	
		  	  System.out.println("Wrong ACL Message");
			  // ce.printStackTrace();
		      JOptionPane.showMessageDialog(null,"Wrong ACL Message: "+"\n"+ ce.getMessage(),"Error Message",JOptionPane.ERROR_MESSAGE);
						  }

			
		}
		// OPEN
		else if (command.equals("Open"))
		{
			JFileChooser chooser = new JFileChooser(); 
			if (currentDir != null)
				chooser.setCurrentDirectory(currentDir); 
			int returnVal = chooser.showOpenDialog(null); 
			if(returnVal == JFileChooser.APPROVE_OPTION)
			{
				currentDir = chooser.getCurrentDirectory();
				String fileName = chooser.getSelectedFile().getAbsolutePath();

				try
				{
					StringACLCodec codec = new StringACLCodec(new FileReader(fileName),null);
					currentMsgGui.setMsg(codec.decode());
				}
				catch(FileNotFoundException e1) {
						JOptionPane.showMessageDialog(null,"File not found: "+ fileName + e1.getMessage(),"Error Message",JOptionPane.ERROR_MESSAGE);
					System.out.println("File Not Found: " + fileName); }
				catch (ACLCodec.CodecException e2) {
					System.out.println("Wrong ACL Message in file: " +fileName);
					// e2.printStackTrace(); 
					JOptionPane.showMessageDialog(null,"Wrong ACL Message in file: "+ fileName +"\n"+ e2.getMessage(),"Error Message",JOptionPane.ERROR_MESSAGE);
				
			}
			} 
		}
		// SAVE
		else if (command.equals("Save"))
		{
			JFileChooser chooser = new JFileChooser();
			if (currentDir != null)
				chooser.setCurrentDirectory(currentDir); 
			int returnVal = chooser.showSaveDialog(null); 
			if(returnVal == JFileChooser.APPROVE_OPTION)
			{
				currentDir = chooser.getCurrentDirectory();
				String fileName = chooser.getSelectedFile().getAbsolutePath();

				try
				{
					StringACLCodec codec = new StringACLCodec(null,new FileWriter(fileName));
					ACLMessage ACLmsg = currentMsgGui.getMsg();
					codec.write(ACLmsg); 
				}
				catch(FileNotFoundException e3) { System.out.println("Can't open file: " + fileName); }
				catch(IOException e4) { System.out.println("IO Exception"); }
			} 
		}
		// VIEW
		else if (command.equals("View"))
		{
			int i = queuedMsgList.getSelectedIndex();
			if (i != -1)
			{
				MsgIndication mi = (MsgIndication) queuedMsgListModel.getElementAt(i);
				ACLMessage m = mi.getMessage();
				AclGui.showMsgInDialog(m, this);
			}
		}
		// DELETE
		else if (command.equals("Delete"))
		{
			int i = queuedMsgList.getSelectedIndex();
			if (i != -1)
			{
				queuedMsgListModel.removeElementAt(i);
			}
		}
		// SET AS CURRENT
		else if (command.equals("Set as current"))
		{
			int i = queuedMsgList.getSelectedIndex();
			if (i != -1)
			{
				MsgIndication mi = (MsgIndication) queuedMsgListModel.getElementAt(i);
				ACLMessage m = mi.getMessage();
				currentMsgGui.setMsg(m);
			}
		}
		// REPLY
		else if (command.equals("Reply"))
		{
			int i = queuedMsgList.getSelectedIndex();
			if (i != -1)
			{
				MsgIndication mi = (MsgIndication) queuedMsgListModel.getElementAt(i);
				ACLMessage m = mi.getMessage();
				ACLMessage reply = m.createReply();
				reply.setSender(myAgent.getAID());
				//currentMsgGui.setMsg(m.createReply());
				currentMsgGui.setMsg(reply);
			}
		}
		// OPEN QUEUE
		else if (command.equals("Open queue"))
		{
			JFileChooser chooser = new JFileChooser(); 
			if (currentDir != null)
				chooser.setCurrentDirectory(currentDir); 
			int returnVal = chooser.showOpenDialog(null); 
			if(returnVal == JFileChooser.APPROVE_OPTION)
			{
				// Flush current queue
				for (int i = 0;i < queuedMsgListModel.getSize(); ++i)
				{
					queuedMsgListModel.removeElementAt(i);
				}

				currentDir = chooser.getCurrentDirectory();
				String fileName = chooser.getSelectedFile().getAbsolutePath();

				try
				{
					BufferedReader inp = new BufferedReader(new FileReader(fileName));
					// Read the number of messages in the queue
					int n = -1;
					try
					{
						Integer nn = new Integer(inp.readLine());
						n = nn.intValue();
					}
					catch(IOException ioEx) { System.out.println("IO Exception reading the number of messages in the queue"); }
					
					// Read the messages and insert them in the queue
					MsgIndication mi; 
					for (int i = 0;i < n; ++i)
					{
						mi = MsgIndication.fromText(inp);
						queuedMsgListModel.add(i, (Object) mi);
					}
				}
				catch(FileNotFoundException e5) { System.out.println("Can't open file: " + fileName); }
				catch(IOException e6) { System.out.println("IO Exception"); }
			} 
		}
		// SAVE QUEUE
		else if (command.equals("Save queue"))
		{
			JFileChooser chooser = new JFileChooser(); 
			if (currentDir != null)
				chooser.setCurrentDirectory(currentDir); 
			int returnVal = chooser.showSaveDialog(null); 
			if(returnVal == JFileChooser.APPROVE_OPTION)
			{
				currentDir = chooser.getCurrentDirectory();
				String fileName = chooser.getSelectedFile().getAbsolutePath();

				try
				{
					BufferedWriter out = new BufferedWriter(new FileWriter(fileName));
					// Write the number of messages in the queue
					try
					{
						out.write(String.valueOf(queuedMsgListModel.getSize()));
						out.newLine();
					}
					catch(IOException ioEx) { System.out.println("IO Exception writing the number of messages in the queue"); }

					// Write the messages
					MsgIndication mi;
					for (int i = 0;i < queuedMsgListModel.getSize(); ++i)
					{
						mi = (MsgIndication) queuedMsgListModel.get(i);
						mi.toText(out);
					}
				}
				catch(FileNotFoundException e5) { System.out.println("Can't open file: " + fileName); }
				catch(IOException e6) { System.out.println("IO Exception"); }
			} 
		}
		// EXIT
		else if (command.equals("Exit"))
		{
			myAgent.doDelete();
		}
	}

}
