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

// Import required java classes
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import java.util.*;

// Import required JADE classes
import jade.core.*;
import jade.lang.acl.*;
import jade.core.AID;

/**
 * The AclGui class extends the Swing JPanel class by adding all the controls 
 * required to properly edit/show the fields of an an ACL message   
 * compliant to the <b>FIPA 97</b> specs. <p>
 * <p>
 * There are basically two ways of using the AclGui class.
 * <ul>
 * <li> <b>Non Static Mode</b>. As AclGui extends JPanel, an
 * instance of AclGui can be directly added to whatever Container thus providing an easy way 
 * to permanently insert into a GUI a panel for the editing/display of an ACL message.<br>
 * The <em>setMsg()</em> and <em>getMsg()</em> methods can be used to display a given ACL message in the panel 
 * and to retrieve the ACL message currently displayed.<br>
 * The <em>setEnabled()</em> and <em>setSenderEnabled()</em> 
 * methods can be used to enable/disable modifications to all fields in the ACL message
 * and/or the sender field respectively.<br>
 * E.g.<br>
 * This code creates an agent GUI with a panel (in the left part of the GUI) that displays each new 
 * message received by the agent
 * <code>
 * .....<br>
 * AclGui acl;<br>
 * .....<br>
 * JFrame agentGui = new JFrame();<br>
 * agentGui.setLayout(new BorderLayout());<br>
 * acl = new AclGui();<br>
 * acl.setEnabled(false);<br>
 * agentGui.add("West", acl);<br>
 * .....<br>
 * </code>
 * Each time a new message is received (assuming the message has been stored 
 * in the msg variable of type ACLMessage)
 * <code>
 * acl.setMsg(msg);<br>
 * </code>
 * </li> 
 * <li> <b>Static Mode</b>. The AclGui class also provides the <em>editMsgInDialog()</em> and <em>showMsgInDlg()</em>
 * static methods that pop up a temporary dialog window (including an AclGui panel and the proper OK and
 * Cancel buttons) by means of which it is possible to edit and show a given ACL message.<br>
 * E.g.<br>
 * This code creates a button that allows the user to edit an ACL message 
 * by means of a temporary dialog window
 * <code>
 * .....<br>
 * ACLMessage msg;<br>
 * .....<br>
 * JButton b = new JButton("edit");<br>
 * b.addActionListener(new ActionListener()<br>
 * {<br>
 *		public void actionPerformed(ActionEvent e)<br>
 *		{<br>
 *			msg = AclGui.editMsgInDialog(new ACLMessage("", null);<br>
 *		}<br>
 * } );<br>
 * </code>
 * </li>
 * </ul>

 @author Giovanni Caire - CSELT
 @version $Date$ $Revision$
 @see jade.lang.acl.ACLMessage

 */
public class AclGui extends JPanel
{
	// Controls for ACL message parameter editing
	static String ADD_NEW_RECEIVER = "Insert receiver"; 
	
	AID SenderAID = new AID();
	AID newAIDSender = null;

	VisualAIDList receiverListPanel;
	VisualAIDList replyToListPanel;
	VisualPropertiesList propertiesListPanel;
	
	/**
	@serial
	*/
	private boolean      guiEnabledFlag;
	/**
	@serial
	*/
	private JTextField   sender;
	/**
	@serial
	*/
	private boolean      senderEnabledFlag;
	
	/**
	@serial
	*/
	private JComboBox    communicativeAct;
	/**
	@serial
	*/
	private JTextArea    content;
	/**
	@serial
	*/
	private JTextField   language;
	/**
	@serial
	*/
	private JTextField   ontology;
	/**
	@serial
	*/
	private JComboBox    protocol;
	/**
	@serial
	*/
	private JTextField   conversationId;
	/**
	@serial
	*/
	private JTextField   inReplyTo;
	/**
	@serial
	*/
	private JTextField   replyWith;
	/**
	@serial
	*/
	private JTextField   replyBy;
	
	/**
	@serial
	*/
	private JTextField   encoding;
	
	/**
	@serial
	*/
	private JButton      replyBySet;
	/**
	@serial
	*/
	private Date         replyByDate;
	/**
	@serial
	*/
	
	//private JTextArea    envelope;

	// Data for panel layout definition
	/**
	@serial
	*/
	GridBagLayout lm = new GridBagLayout();
	/**
	@serial
	*/
	GridBagConstraints constraint = new GridBagConstraints();
	/**
	@serial
	*/
	private int leftBorder, rightBorder, topBorder, bottomBorder;
	/**
	@serial
	*/
	private int xSpacing, ySpacing;
	/**
	@serial
	*/
	private int gridNCol, gridNRow;
	/**
	@serial
	*/
	private int colWidth[];
	private static final int TEXT_SIZE = 30;

	/**
	@serial
	*/
	private Vector fipaActVector;
	
	private static int    N_FIPA_PROTOCOLS = 7;
	private static String fipaProtocols[] = {"fipa-auction-english",
	                                         "fipa-auction-dutch",
                                             "fipa-contract-net",
                                             "fipa-iterated-contract-net",
                                             "fipa-query",
	                                         "fipa-request",
                                             "fipa-request-when" };
                                             
  /**
  @serial
  */
	private ArrayList fipaProtocolArrayList;


	// Data for the editing of user defined iteration protocols
	/**
	@serial
	*/
	private int    lastSelectedIndex;
	/**
	@serial
	*/
	private String lastSelectedItem;
	private static final String LABEL_TO_ADD_PROT = "ADD USER-DEF PROTOCOL";
 
	// These data are used to correctly handle the resizing of the AclGui panel
	/**
	@serial
	*/
	private JPanel       aclPanel;
	/**
	@serial
	*/
	private Dimension    minDim;
	/**
	@serial
	*/
	private boolean      firstPaintFlag;

	// Other data used
	private static ACLMessage editedMsg;

	
	private JButton senderButton;
	
	/////////////////
	// CONSTRUCTOR
	/////////////////
	/**
		Ordinary <code>AclGui</code> constructor.
		@see jade.lang.acl.ACLMessage#ACLMessage(String type)
	*/
	public AclGui()
	{
	
		JLabel l;
	  int    i;
    
		// Initialize the Vector of interaction protocols
		fipaProtocolArrayList = new ArrayList();
	
		for (i = 0;i < N_FIPA_PROTOCOLS; ++i)
			fipaProtocolArrayList.add((Object) fipaProtocols[i]);

		firstPaintFlag = true;
		guiEnabledFlag = true;
		minDim = new Dimension();
		aclPanel = new JPanel();
		aclPanel.setBackground(Color.lightGray); 
		aclPanel.setLayout(lm);
		
		formatGrid(20,   // N of rows 
		            3,   // N of columns
		            5,   // Right border 
		            5,   // Left border
		            5,   // Top boredr
		            5,   // Bottom border
		            2,   // Space between columns
		            2);  // Space between rows
		setGridColumnWidth(0, 115);
		setGridColumnWidth(1, 40);
		setGridColumnWidth(2, 170);
    
    
		// Sender  (line # 0)
		l = new JLabel("Sender:");
		put(l, 0, 0, 1, 1, false); 
		senderEnabledFlag = false; // The sender field is disabled by default, but can be enabled with the setSenderEnabled() method.
		sender = new JTextField();
		sender.setPreferredSize(new Dimension(80,26));
    sender.setMinimumSize(new Dimension(80,26));
    sender.setMaximumSize(new Dimension(80,26));
    sender.setEditable(false);
    sender.setBackground(Color.white);
		senderButton = new JButton("Set");
		senderButton.setMargin(new Insets(2,3,2,3));
	
		put(senderButton,1,0,1,1,false);
    put(sender, 2, 0, 1, 1, false);	
	
    senderButton.addActionListener(new ActionListener(){
    	public void actionPerformed(ActionEvent e)
    	{
    		String command = e.getActionCommand();
    		AIDGui guiSender = new AIDGui();
    		
    		if(command.equals("Set"))
    		{
    		  newAIDSender = guiSender.ShowAIDGui(SenderAID,true);
    		  //the name can be different
    			if (newAIDSender != null)
    				sender.setText(newAIDSender.getName());
    		}
    		else
    		if(command.equals("View"))
    			guiSender.ShowAIDGui(SenderAID, false);
    		
    	}
    });
    
		// Receiver (line # 1)
    l = new JLabel("Receivers:");
    put(l,0,1,1,1,false);
    receiverListPanel = new VisualAIDList(new ArrayList().iterator());
    receiverListPanel.setDimension(new Dimension(205,37));
 	  put(receiverListPanel,1,1,2,1,false);

		
		//Reply-to (line #2)
		l = new JLabel("Reply-to:");
		put(l, 0, 2, 1, 1,false);
		replyToListPanel = new VisualAIDList(new ArrayList().iterator());
		replyToListPanel.setDimension(new Dimension(205,37));
		put(replyToListPanel,1,2,2,1,false);
			
		// Communicative act (line # 3)
		l = new JLabel("Communicative act:");
		put(l, 0, 3, 1, 1, false);  
		communicativeAct = new JComboBox();
		
		Iterator comm_Act = ACLMessage.getAllPerformatives().iterator();
		while(comm_Act.hasNext())
			communicativeAct.addItem(((String)comm_Act.next()).toLowerCase());
			
		communicativeAct.setSelectedIndex(0);
		put(communicativeAct, 1, 3, 2, 1, true);

		// Content (line # 4-8)
		l = new JLabel("Content:");
		put(l, 0, 4, 3, 1, false);  		
		content = new JTextArea(5,TEXT_SIZE);
		JScrollPane contentPane = new JScrollPane();
		contentPane.getViewport().setView(content); 	
		put(contentPane, 0, 5, 3, 4, false);
		contentPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS); 		
		contentPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS); 		

		// Language (line # 9)
		l = new JLabel("Language:");
		put(l, 0, 9, 1, 1, false);  		
		language = new JTextField();
		language.setBackground(Color.white);
		put(language, 1, 9, 2, 1, false);	
	
		//Encoding (line # 10)
	  l = new JLabel("Encoding:");
		put(l, 0, 10, 1, 1, false);  		
		encoding = new JTextField(); 
		encoding.setBackground(Color.white);
		put(encoding, 1, 10, 2, 1, false);	
		
		// Ontology (line # 11)
		l = new JLabel("Ontology:");
		put(l, 0, 11, 1, 1, false);  		
		ontology = new JTextField();
		ontology.setBackground(Color.white);
		put(ontology, 1, 11, 2, 1, false);	

		// Protocol (line # 12)
		l = new JLabel("Protocol:");
		put(l, 0, 12, 1, 1, false);  		
		protocol = new JComboBox(); 	
		for (i = 0;i < fipaProtocolArrayList.size(); ++i)
			protocol.addItem((String) fipaProtocolArrayList.get(i));
		protocol.addItem(LABEL_TO_ADD_PROT);
		protocol.addItem("Null");
		protocol.setSelectedItem("Null");
		lastSelectedIndex = protocol.getSelectedIndex();
		lastSelectedItem = (String) protocol.getSelectedItem();
		put(protocol, 1, 12, 2, 1, true);
		protocol.addActionListener( new ActionListener()
		                                {
											public void actionPerformed(ActionEvent e)
											{    
												String param = (String) protocol.getSelectedItem();
												
												// BEFORE THE CURRENT SELECTION THE JComboBox WAS NON EDITABLE (a FIPA protocol or null was selected)
												if (!protocol.isEditable()) 
												{
													// If a user defined protocol has just been selected --> set editable to true
													if (fipaProtocolArrayList.indexOf((Object) param) < 0 && !param.equals("Null"))
													{
														protocol.setEditable(true);
													}
												}
												// BEFORE THE CURRENT SELECTION THE JComboBox WAS EDITABLE (an editable protocol was selected)
												else 
												{
													// The user selected a FIPA protocol or null (he didn't perform any editing operation) 
													if (fipaProtocolArrayList.indexOf((Object) param) >= 0 || param.equals("Null"))
													{
														protocol.setEditable(false);
														protocol.setSelectedItem(param);
													}
													// The user selected the label to add a new protocol (he didn't perform any editing operation) 
													else if (param.equals(LABEL_TO_ADD_PROT))
													{
														protocol.setSelectedItem(param);
													}	
													// The user added a new protocol
													else if (lastSelectedItem.equals(LABEL_TO_ADD_PROT))
													{     
														// The new protocol is actually added only if it is != "" and is not already present  
														if (!param.equals("")) 
														{
															protocol.addItem(param);
															int cnt = protocol.getItemCount();
															protocol.setSelectedItem(param);
															int n = protocol.getSelectedIndex();
															if (n != cnt-1)
																protocol.removeItemAt(cnt-1);
														}
														else 
														{
															protocol.setEditable(false);
															protocol.setSelectedItem("Null");
														}
													}
													// The user modified/deleted a previously added user defined protocol
													else if (lastSelectedItem != LABEL_TO_ADD_PROT)
													{
														protocol.removeItemAt(lastSelectedIndex);  // The old protocol is removed
														if (param.equals("")) // Deletion
														{
															protocol.setEditable(false);
															protocol.setSelectedItem("Null");
														}
														else // Modification
														{
															protocol.addItem(param);
															protocol.setSelectedItem(param);
														}
													}
												}

												lastSelectedIndex = protocol.getSelectedIndex();
												lastSelectedItem = (String) protocol.getSelectedItem();
											}
										} );

		// Conversation-id (line # 13)
		l = new JLabel("Conversation-id:");
		put(l, 0, 13, 1, 1, false);  		
		conversationId = new JTextField();
		conversationId.setBackground(Color.white);
		put(conversationId, 1, 13, 2, 1, false);	

		// In-reply-to (line # 14)
		l = new JLabel("In-reply-to:");
		put(l, 0, 14, 1, 1, false);  		
		inReplyTo = new JTextField(); 	
		inReplyTo.setBackground(Color.white);
		put(inReplyTo, 1, 14, 2, 1, false);	

		// Reply-with (line # 15)
		l = new JLabel("Reply-with:");
		put(l, 0, 15, 1, 1, false);  		
		replyWith = new JTextField(); 	
		replyWith.setBackground(Color.white);
		put(replyWith, 1, 15, 2, 1, false);	
		
		// Reply-by (line # 16)
		replyByDate = null;
		l = new JLabel("Reply-by:");
		put(l, 0, 16, 1, 1, false);
		replyBySet = new JButton("Set");
		replyBySet.setMargin(new Insets(2,3,2,3));
		replyBy = new JTextField();
		replyBy.setBackground(Color.white);
		put(replyBySet, 1, 16, 1, 1, false);
		put(replyBy, 2, 16, 1, 1, false);	
		replyBySet.addActionListener(new	ActionListener()
											{
												public void actionPerformed(ActionEvent e)
												{
													String command = e.getActionCommand();
													//TimeChooser t = new TimeChooser(replyByDate);
													TimeChooser t = new TimeChooser();
													String d = replyBy.getText();
													if (!d.equals(""))
													{
														try
														{
															t.setDate(ISO8601.toDate(d));
				 										}
														catch (Exception ee) { System.out.println("Incorrect date format"); }
													}
													if (command.equals("Set"))
													{
														if (t.showEditTimeDlg(null) == TimeChooser.OK)
														{
															replyByDate = t.getDate();
															if (replyByDate == null)
																replyBy.setText("");
															else
																replyBy.setText(ISO8601.toString(replyByDate));
														}
													}
													else if (command.equals("View"))
													{					
														t.showViewTimeDlg(null);
													}
												}
											} );

		
		//Properties (line #17)
		l = new JLabel("User Properties:");
		put(l, 0, 17, 1, 1, false);
	  propertiesListPanel = new VisualPropertiesList(new Properties());
	  propertiesListPanel.setDimension(new Dimension(205,37));
	  put(propertiesListPanel,1,17,2,1,false);

		
		// Envelope (line # 15-19)
		/*l = new JLabel("Envelope:");
		put(l, 0, 15, 3, 1, false);  		
		envelope = new JTextArea(5, TEXT_SIZE); 
		JScrollPane envelopePane = new JScrollPane();
		envelopePane.getViewport().setView(envelope);	
		put(envelopePane, 0, 16, 3, 4, false);	
		envelopePane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS); 		
		envelopePane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);*/ 		

		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		add(aclPanel);

		updateEnabled();
	}


	////////////////////
	// PRIVATE METHODS
	////////////////////
	private void formatGrid(int nr, int nc, int lb, int rb, int tb, int bb, int xs, int ys)
	{
		gridNRow = nr;
		gridNCol = nc;
		colWidth = new int[3];
		//colWidth[0] = 120;
		//colWidth[1] = 63;
		//colWidth[2] = 180;
		leftBorder = lb;
		rightBorder = rb;
		topBorder = tb;
		bottomBorder = bb;
		xSpacing = xs;
		ySpacing = ys;
	}

	private void setGridColumnWidth(int col, int width)
	{
		colWidth[col] = width;
	}

	private void put(JComponent c, int x, int y, int dx, int dy, boolean fill)
	{
	int leftMargin, rightMargin, topMargin, bottomMargin;
	int preferredWidth, preferredHeight;
		
		constraint.gridx = x;
		constraint.gridy = y;
		constraint.gridwidth = dx;
		constraint.gridheight = dy;
		constraint.anchor = GridBagConstraints.WEST;
		if (fill)
			constraint.fill = GridBagConstraints.BOTH;
		else
			constraint.fill = GridBagConstraints.VERTICAL;

		leftMargin =   (x == 0 ? leftBorder : 0);
		rightMargin =  (x+dx == gridNCol ? rightBorder : xSpacing);
		topMargin =    (y == 0 ? topBorder : 0);
		bottomMargin = (y+dy == gridNRow ? bottomBorder : ySpacing);

		int i;
		preferredWidth = 0; 
		for (i = 0; i < dx; ++i)
			preferredWidth += colWidth[x+i] + xSpacing;
		preferredWidth -= xSpacing;
		preferredHeight = c.getPreferredSize().height;
		c.setPreferredSize(new Dimension(preferredWidth, preferredHeight));

		constraint.insets = new Insets(topMargin, leftMargin, bottomMargin, rightMargin);
		lm.setConstraints(c,constraint); 
		aclPanel.add(c);
	}

	private void updateEnabled()
	{
		communicativeAct.setEnabled(guiEnabledFlag);
		//sender.setEditable(guiEnabledFlag && senderEnabledFlag); 
		senderButton.setText(senderEnabledFlag ? "Set" : "View");
	
		receiverListPanel.setEnabled(guiEnabledFlag);
		replyToListPanel.setEnabled(guiEnabledFlag);
		propertiesListPanel.setEnabled(guiEnabledFlag);
		
		replyWith.setEditable(guiEnabledFlag);
		inReplyTo.setEditable(guiEnabledFlag);
		conversationId.setEditable(guiEnabledFlag);
		replyBy.setEditable(false);
		replyBySet.setEnabled(true);
		replyBySet.setText(guiEnabledFlag ? "Set" : "View");
		encoding.setEditable(guiEnabledFlag);
		
	  //envelope.setEditable(guiEnabledFlag);
		protocol.setEnabled(guiEnabledFlag);
		language.setEditable(guiEnabledFlag);
		ontology.setEditable(guiEnabledFlag);
		content.setEditable(guiEnabledFlag);
			
	}


	/////////////////////////////////////////////
	// MESSAGE GETTING and SETTING PUBLIC METHODS
	/////////////////////////////////////////////
	/**
		Displays the specified ACL message into the AclGui panel 
		@param msg The ACL message to be displayed
		@see AclGui#getMsg()
	*/
	public void setMsg(ACLMessage msg)
	{
		int    i;
		String param, lowerCase;
		
    int perf = msg.getPerformative(); 
		lowerCase = (ACLMessage.getPerformative(perf)).toLowerCase();
		
		//No control if the ACLMessage is a well-known one
		//if not present the first of the comboBox is selected
		communicativeAct.setSelectedItem(lowerCase);	
		
		this.SenderAID = msg.getSender();
		
		if ((param = SenderAID.getName()) == null) param = "";
	
		sender.setText(param);
		
	  receiverListPanel.resetContent(msg.getAllReceiver());
		replyToListPanel.resetContent(msg.getAllReplyTo());
		
		Enumeration e = 	msg.getAllUserDefinedParameters().propertyNames();
		ArrayList list = new ArrayList();
		while(e.hasMoreElements())
			list.add(e.nextElement());
		propertiesListPanel.resetContent(list.iterator());
		propertiesListPanel.setContentProperties(msg.getAllUserDefinedParameters());
		
		if ((param = msg.getReplyWith()) == null) param = "";
		replyWith.setText(param);
		if ((param = msg.getInReplyTo()) == null) param = "";
		inReplyTo.setText(param);
		if ((param = msg.getConversationId()) == null) param = "";
		conversationId.setText(param);
		if ((param = msg.getReplyBy()) == null) param = "";
		replyBy.setText(param);
		
		/*Not yet existing
		if ((param = msg.getEnvelope()) == null) param = "";
		envelope.setText(param);*/
		
		if((param = msg.getProtocol()) == null)
			protocol.setSelectedItem("Null");
		else if (param.equals("") || param.equalsIgnoreCase("Null"))
			protocol.setSelectedItem("Null");
		else
		{
			lowerCase = param.toLowerCase();
			if ((i = fipaProtocolArrayList.indexOf((Object) lowerCase)) < 0)
			{
				// This is done to avoid inserting the same user-defined protocol more than once
				protocol.addItem(param);
				int cnt = protocol.getItemCount();
				protocol.setSelectedItem(param);
				int n = protocol.getSelectedIndex();
				if (n != cnt-1)
					protocol.removeItemAt(cnt-1);
			}
			else
				protocol.setSelectedIndex(i);
		}
		if ((param = msg.getLanguage()) == null) param = "";
		language.setText(param);
		if ((param = msg.getOntology()) == null) param = "";
		ontology.setText(param);
		if ((param = msg.getContent()) == null) param = "";
		content.setText(param);
		if((param = msg.getEncoding())== null) param = "";
		encoding.setText(param);
		
		

	}
	
	/**
		Get the ACL message currently displayed by the AclGui panel 
		@return The ACL message currently displayed by the AclGui panel as an ACLMessage object
		@see AclGui#setMsg(ACLMessage msg)
	*/
	public ACLMessage getMsg()
	{
		String param;
		param = (String) communicativeAct.getSelectedItem();
		int perf = ACLMessage.getInteger(param);
		ACLMessage msg = new ACLMessage(perf);
    
		if(newAIDSender != null)
			SenderAID = newAIDSender;
		
		if ( ((param = sender.getText()).trim()).length() > 0 )
			SenderAID.setName(param);
	
		msg.setSender(SenderAID);

		Enumeration rec_Enum = receiverListPanel.getContent();
		while(rec_Enum.hasMoreElements())
			msg.addReceiver((AID)rec_Enum.nextElement());
		
		Enumeration replyTo_Enum = replyToListPanel.getContent();
		while(replyTo_Enum.hasMoreElements())
			msg.addReplyTo((AID)replyTo_Enum.nextElement());
			
		Properties user_Prop = propertiesListPanel.getContentProperties();
		Enumeration keys = user_Prop.propertyNames();
		while(keys.hasMoreElements())
		{
			String k = (String)keys.nextElement();
			msg.addUserDefinedParameter(k,user_Prop.getProperty(k));
		}
		
		if (!(param = replyWith.getText()).equals(""))
			msg.setReplyWith(param);
		if (!(param = inReplyTo.getText()).equals(""))
			msg.setInReplyTo(param);
		if (!(param = conversationId.getText()).equals(""))
			msg.setConversationId(param);
		if (!(param = replyBy.getText()).equals(""))
			msg.setReplyBy(param);
		
	  /* if (!(param = envelope.getText()).equals(""))
			msg.setEnvelope(param);*/
		
			if (!(param = (String) protocol.getSelectedItem()).equals("Null"))
			msg.setProtocol(param);
		if (!(param = language.getText()).equals(""))
			msg.setLanguage(param);
		if (!(param = ontology.getText()).equals(""))
			msg.setOntology(param);
		if (!(param = content.getText()).equals(""))
			msg.setContent(param);
	  
		param = (encoding.getText()).trim();
	  if(param.length() > 0)
	  	msg.setEncoding(param);
			
		return msg;
	}

	
	/////////////////////////
	// UTILITY PUBLIC METHODS
	/////////////////////////
	/** 
		Enables/disables the editability of all the controls in an AclGui panel (default is enabled)
		@param enabledFlag If true enables editability 
		@see AclGui#setSenderEnabled(boolean enabledFlag)
	*/
	public void setEnabled(boolean enabledFlag)
	{
		guiEnabledFlag = enabledFlag;
		updateEnabled();
	}

	/** 
		Enables/disables the editability of the sender field of an AclGui panel (default is enabled)
		@param enabledFlag If true enables editability 
		@see AclGui#setEnabled(boolean enabledFlag)
	*/
	public void setSenderEnabled(boolean enabledFlag)
	{
		senderEnabledFlag = enabledFlag;
		updateEnabled();
	}

	/** 
		Set the specified border to the AclGui panel
		@param b Specifies the type of border
	*/
	public void setBorder(Border b)
	{
		if (aclPanel != null)
			aclPanel.setBorder(b);
	}

	/** 
		Paint the AclGui panel
	*/
	public void paint(Graphics g)
	{
		if (firstPaintFlag)
		{
			firstPaintFlag = false;
			minDim = aclPanel.getSize();
		}
		else
			aclPanel.setMinimumSize(minDim);

		super.paint(g);
	}


	//////////////////
	// STATIC METHODS
	//////////////////
	/**
		Pops up a dialog window including an editing-disabled AclGui panel and displays the specified 
		ACL message in it. 
		@param m The ACL message to be displayed
		@param parent The parent window of the dialog window
		@see AclGui#editMsgInDialog(ACLMessage msg, Frame parent)
	*/
	public static void showMsgInDialog(ACLMessage msg, Frame parent)
	{
		final JDialog tempAclDlg = new JDialog(parent, "ACL Message", true);

		AclGui aclPanel = new AclGui();
		aclPanel.setBorder(new BevelBorder(BevelBorder.RAISED));
		aclPanel.setEnabled(false);
		aclPanel.setMsg(msg);

		JButton okButton = new JButton("OK");
		JPanel buttonPanel = new JPanel();
		// Use default (FlowLayout) layout manager to dispose the OK button
		buttonPanel.add(okButton);

		tempAclDlg.getContentPane().setLayout(new BorderLayout());
		tempAclDlg.getContentPane().add("Center", aclPanel);
		tempAclDlg.getContentPane().add("South", buttonPanel);

		okButton.addActionListener(new ActionListener()
									   {
											public void actionPerformed(ActionEvent e)
											{
												tempAclDlg.dispose();
											}
									   } );

		tempAclDlg.pack();
		tempAclDlg.setResizable(false);
		if (parent != null)
			tempAclDlg.setLocation(parent.getX() + (parent.getWidth() - tempAclDlg.getWidth()) / 2, 
			                       parent.getY() + (parent.getHeight() - tempAclDlg.getHeight()) / 2);
		tempAclDlg.show();
	}

	/**
		Pops up a dialog window including an editing-enabled AclGui panel and displays the specified 
		ACL message in it. The dialog window also includes an OK and a Cancel button to accept or 
		discard the performed editing. 
		@param m The ACL message to be initially displayed
		@param parent The parent window of the dialog window
		@return The ACL message displayed in the dialog window or null depending on whether the user close the window
		by clicking the OK or Cancel button 
		@see AclGui#showMsgInDialog(ACLMessage msg, Frame parent)
	*/
	public static ACLMessage editMsgInDialog(ACLMessage msg, Frame parent)
	{
		final JDialog tempAclDlg = new JDialog(parent, "ACL Message", true);
		final AclGui  aclPanel = new AclGui();
		aclPanel.setBorder(new BevelBorder(BevelBorder.RAISED));
		aclPanel.setSenderEnabled(true);
		aclPanel.setMsg(msg);

		JButton okButton = new JButton("OK");
		JButton cancelButton = new JButton("Cancel");
		okButton.setPreferredSize(cancelButton.getPreferredSize());
		JPanel buttonPanel = new JPanel();
		// Use default (FlowLayout) layout manager to dispose the OK and Cancel buttons
		buttonPanel.add(okButton);
		buttonPanel.add(cancelButton);  

		tempAclDlg.getContentPane().setLayout(new BorderLayout());
		tempAclDlg.getContentPane().add("Center", aclPanel);
		tempAclDlg.getContentPane().add("South", buttonPanel);
		
		okButton.addActionListener(new ActionListener()
									   {
											public void actionPerformed(ActionEvent e)
											{
											  //System.out.println("OK pressed");
												editedMsg = aclPanel.getMsg();
												tempAclDlg.dispose();
											}
									   } );
		cancelButton.addActionListener(new ActionListener()
										   {
												public void actionPerformed(ActionEvent e)
												{
												  //System.out.println("OK pressed");
													editedMsg = null;
													tempAclDlg.dispose();
												}
										   } );
		
		tempAclDlg.pack();
		tempAclDlg.setResizable(false);
		if (parent != null)
			tempAclDlg.setLocation( parent.getX() + (parent.getWidth() - tempAclDlg.getWidth()) / 2,
			                        parent.getY() + (parent.getHeight() - tempAclDlg.getHeight()) / 2);
		tempAclDlg.show();

		ACLMessage m = null;
		if (editedMsg != null)
			m = (ACLMessage) editedMsg.clone();
		
		return m;
	}

	
}
