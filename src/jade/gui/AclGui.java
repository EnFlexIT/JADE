///////////////////////////////////////////////////////////////
//   
//   /     /  ___/  ___/   / /_   _/ 
//  /  /--/___  /  ___/   /_/ / / /   
// /_____/_____/_____/_____/_/_/_/
// 
// -----------------------------------------------------------
// PROJECT:   DUMMY AGENT	
// FILE NAME: AclGui.java	
// CONTENT:   This file includes the definition of the AclGui class
//            that provides 
//            - a JPanel (that can be normally added to a Container) with 
//              the controls required to input/display an ACL message
//            - methods to get and set the ACL message displayed in the panel
//            - static methods to show and edit an ACL Message in a temporary
//              modal JDialog.
// AUTHORS:	  Giovanni Caire	
// RELEASE:	  4.0	
// MODIFIED:  22/04/1999	
// 
//////////////////////////////////////////////////////////////


package jade.gui;

// Import required java classes
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import java.util.*;

// Import required Jade classes
import jade.core.*;
import jade.lang.acl.*;

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
 @see jade.lang.acl.ACLMessage

 */
public class AclGui extends JPanel
{
	// Controls for ACL message parameter editing
	private boolean      guiEnabledFlag;
	private JTextField   sender;
	private boolean      senderEnabledFlag;
	private JTextField   receiver;
	private JComboBox    communicativeAct;
	private JTextArea    content;
	private JTextField   language;
	private JTextField   ontology;
	private JComboBox    protocol;
	private JTextField   conversationId;
	private JTextField   inReplyTo;
	private JTextField   replyWith;
	private JTextField   replyBy;
	private JButton      replyBySet;
	private Date         replyByDate;
	private JTextArea    envelope;

	// Data for panel layout definition
	GridBagLayout lm = new GridBagLayout();
	GridBagConstraints constraint = new GridBagConstraints();
	private int leftBorder, rightBorder, topBorder, bottomBorder;
	private int xSpacing, ySpacing;
	private int gridNCol, gridNRow;
	private int colWidth[];
	private static final int TEXT_SIZE = 30;

	// Data for initialization and handling of FIPA communicative acts 
	// and iteration protocols
	private static int    N_FIPA_ACTS = 18;
	private static String fipaActs[] = {"accept-proposal",
	                                    "agree",
	                                    "cancel",
	                                    "cfp",
	                                    "confirm",
	                                    "disconfirm",
	                                    "failure",
	                                    "inform",
	                                    "not-understood",
	                                    "propose",
	                                    "query-if",
	                                    "query-ref",
	                                    "refuse",
	                                    "reject-proposal",
	                                    "request",
	                                    "request-when",
	                                    "request-whenever",
	                                    "subscribe"};
	private Vector fipaActVector;
	private static int    N_FIPA_PROTOCOLS = 7;
	private static String fipaProtocols[] = {"fipa-auction-english",
	                                         "fipa-auction-dutch",
                                             "fipa-contract-net",
                                             "fipa-iterated-contract-net",
                                             "fipa-query",
	                                         "fipa-request",
                                             "fipa-request-when" };
	private Vector fipaProtocolVector;


	// Data for the editing of user defined iteration protocols
	private int    lastSelectedIndex;
	private String lastSelectedItem;
	private static final String LABEL_TO_ADD_PROT = "ADD USER-DEF PROTOCOL";
 
	// These data are used to correctly handle the resizing of the AclGui panel
	private JPanel       aclPanel;
	private Dimension    minDim;
	private boolean      firstPaintFlag;

	// Other data used
	private static ACLMessage editedMsg;

	
	
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


		// Initialize the Vector of FIPA communicative acts and iteration protocols
		fipaActVector = new Vector();
		fipaProtocolVector = new Vector();
		for (i = 0;i < N_FIPA_ACTS; ++i)
			fipaActVector.add((Object) fipaActs[i]);
		for (i = 0;i < N_FIPA_PROTOCOLS; ++i)
			fipaProtocolVector.add((Object) fipaProtocols[i]);

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
		setGridColumnWidth(0, 120);
		setGridColumnWidth(1, 65);
		setGridColumnWidth(2, 180);

		// Sender  (line # 0)
		l = new JLabel("Sender:");
		put(l, 0, 0, 1, 1, false); 
		senderEnabledFlag = false; // The sender field is disabled by default, but can be enabled with the setSenderEnabled() method.
		sender = new JTextField(); 	
		put(sender, 1, 0, 2, 1, false);	

		// Receiver (line # 1)
		l = new JLabel("Receiver:");
		put(l, 0, 1, 1, 1, false); 
		receiver = new JTextField(); 	
		put(receiver, 1, 1, 2, 1, false);	

		// Communicative act (line # 2)
		l = new JLabel("Communicative act:");
		put(l, 0, 2, 1, 1, false);  
		communicativeAct = new JComboBox();
		for (i = 0;i < fipaActVector.size(); ++i)
			communicativeAct.addItem((String) fipaActVector.get(i));
		communicativeAct.addItem("UNKNOWN");
		communicativeAct.setSelectedIndex(0);
		put(communicativeAct, 1, 2, 2, 1, true);

		// Content (line # 3-7)
		l = new JLabel("Content:");
		put(l, 0, 3, 3, 1, false);  		
		content = new JTextArea(5,TEXT_SIZE);
		JScrollPane contentPane = new JScrollPane();
		contentPane.getViewport().setView(content); 	
		put(contentPane, 0, 4, 3, 4, false);
		contentPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS); 		
		contentPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS); 		

		// Language (line # 8)
		l = new JLabel("Language:");
		put(l, 0, 8, 1, 1, false);  		
		language = new JTextField(); 	
		put(language, 1, 8, 2, 1, false);	

		// Ontology (line # 9)
		l = new JLabel("Ontology:");
		put(l, 0, 9, 1, 1, false);  		
		ontology = new JTextField(); 	
		put(ontology, 1, 9, 2, 1, false);	

		// Protocol (line # 10)
		l = new JLabel("Protocol:");
		put(l, 0, 10, 1, 1, false);  		
		protocol = new JComboBox(); 	
		for (i = 0;i < fipaProtocolVector.size(); ++i)
			protocol.addItem((String) fipaProtocolVector.get(i));
		protocol.addItem(LABEL_TO_ADD_PROT);
		protocol.addItem("Null");
		protocol.setSelectedItem("Null");
		lastSelectedIndex = protocol.getSelectedIndex();
		lastSelectedItem = (String) protocol.getSelectedItem();
		put(protocol, 1, 10, 2, 1, true);
		protocol.addActionListener( new ActionListener()
		                                {
											public void actionPerformed(ActionEvent e)
											{    
												String param = (String) protocol.getSelectedItem();
												
												// BEFORE THE CURRENT SELECTION THE JComboBox WAS NON EDITABLE (a FIPA protocol or null was selected)
												if (!protocol.isEditable()) 
												{
													// If a user defined protocol has just been selected --> set editable to true
													if (fipaProtocolVector.indexOf((Object) param) < 0 && !param.equals("Null"))
													{
														protocol.setEditable(true);
													}
												}
												// BEFORE THE CURRENT SELECTION THE JComboBox WAS EDITABLE (an editable protocol was selected)
												else 
												{
													// The user selected a FIPA protocol or null (he didn't perform any editing operation) 
													if (fipaProtocolVector.indexOf((Object) param) >= 0 || param.equals("Null"))
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
														if (!param.equals("")) // The new protocol is actually added only if it is != ""
														{
															protocol.addItem(param);
															protocol.setSelectedItem(param);
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

		// Conversation-id (line # 11)
		l = new JLabel("Conversation-id:");
		put(l, 0, 11, 1, 1, false);  		
		conversationId = new JTextField(); 	
		put(conversationId, 1, 11, 2, 1, false);	

		// In-reply-to (line # 12)
		l = new JLabel("In-reply-to:");
		put(l, 0, 12, 1, 1, false);  		
		inReplyTo = new JTextField(); 	
		put(inReplyTo, 1, 12, 2, 1, false);	

		// Reply-with (line # 13)
		l = new JLabel("Reply-with:");
		put(l, 0, 13, 1, 1, false);  		
		replyWith = new JTextField(); 	
		put(replyWith, 1, 13, 2, 1, false);	
		
		// Reply-by (line # 14)
		replyByDate = null;
		l = new JLabel("Reply-by:");
		put(l, 0, 14, 1, 1, false);
		replyBySet = new JButton("Set");
		replyBy = new JTextField();
		put(replyBySet, 1, 14, 1, 1, false);
		put(replyBy, 2, 14, 1, 1, false);	
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


		// Envelope (line # 15-19)
		l = new JLabel("Envelope:");
		put(l, 0, 15, 3, 1, false);  		
		envelope = new JTextArea(5, TEXT_SIZE); 
		JScrollPane envelopePane = new JScrollPane();
		envelopePane.getViewport().setView(envelope);	
		put(envelopePane, 0, 16, 3, 4, false);	
		envelopePane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS); 		
		envelopePane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS); 		
	
		
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
		sender.setEditable(guiEnabledFlag && senderEnabledFlag);
		receiver.setEditable(guiEnabledFlag);
		replyWith.setEditable(guiEnabledFlag);
		inReplyTo.setEditable(guiEnabledFlag);
		conversationId.setEditable(guiEnabledFlag);
		replyBy.setEditable(false);
		replyBySet.setEnabled(true);
		replyBySet.setText(guiEnabledFlag ? "Set" : "View"); 
		envelope.setEditable(guiEnabledFlag);
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

		lowerCase = msg.getType().toLowerCase();
		if ((i = fipaActVector.indexOf((Object) lowerCase)) < 0)
			communicativeAct.setSelectedItem("UNKNOWN");
		else
			communicativeAct.setSelectedIndex(i);
		if ((param = msg.getSource()) == null) param = "";
		sender.setText(param);
		// Destination: a group of agents
		param = new String("");
		AgentGroup destAG = msg.getDests();
		if (destAG != null)
		{
			Enumeration destE = destAG.getMembers();
			while (destE.hasMoreElements())
			{
				String dest = (String) destE.nextElement();
				param = param.concat(dest);
				param = param.concat(" ");
			}
		}
		receiver.setText(param);
		if ((param = msg.getReplyWith()) == null) param = "";
		replyWith.setText(param);
		if ((param = msg.getReplyTo()) == null) param = "";
		inReplyTo.setText(param);
		if ((param = msg.getConversationId()) == null) param = "";
		conversationId.setText(param);
		if ((param = msg.getReplyBy()) == null) param = "";
		replyBy.setText(param);
		if ((param = msg.getEnvelope()) == null) param = "";
		envelope.setText(param);
		if ((param = msg.getProtocol()) == null)
			protocol.setSelectedItem("Null");
		else
		{
			lowerCase = param.toLowerCase();
			if ((i = fipaProtocolVector.indexOf((Object) lowerCase)) < 0)
				protocol.setSelectedItem("user-defined");
			else
				protocol.setSelectedIndex(i);
		}
		if ((param = msg.getLanguage()) == null) param = "";
		language.setText(param);
		if ((param = msg.getOntology()) == null) param = "";
		ontology.setText(param);
		if ((param = msg.getContent()) == null) param = "";
		content.setText(param);
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
		ACLMessage msg = new ACLMessage(param);

		if (!(param = sender.getText()).equals(""))
			msg.setSource(param);
		// Destination: a group of agents
		if (!(param = receiver.getText()).equals(""))
		{
			String s1 = param.trim();
			// The list of destinations can be in the form (d1 d2 ...)
			// If this is the case, skip the initial '(' and final ')'
			int start = (s1.charAt(0) == '(' ? 1 : 0);
			int end = (s1.charAt(s1.length() -1) == ')' ? s1.length()-1 : s1.length());
			String s2 = s1.substring(start,end);
			
			char[] separator = new char[3];
			separator[0] = ' ';
			separator[1] = '\t';
			separator[2] = '\n';
			String dest;
			start = 0;
			while ((end = StringParser.firstOccurrence(s2, start, separator, 3)) > 0)
			{
				dest = s2.substring(start, end);
				if (dest.length() > 0)
				{
					msg.addDest(dest);
				}
				start = end + StringParser.skip(s2, end, separator, 3);
			}
			dest = s2.substring(start);
			if (dest.length() > 0)
			{
				msg.addDest(dest);
			}
		} 
		if (!(param = replyWith.getText()).equals(""))
			msg.setReplyWith(param);
		if (!(param = inReplyTo.getText()).equals(""))
			msg.setReplyTo(param);
		if (!(param = conversationId.getText()).equals(""))
			msg.setConversationId(param);
		if (!(param = replyBy.getText()).equals(""))
			msg.setReplyBy(param);
		if (!(param = envelope.getText()).equals(""))
			msg.setEnvelope(param);
		if (!(param = (String) protocol.getSelectedItem()).equals("Null"))
			msg.setProtocol(param);
		if (!(param = language.getText()).equals(""))
			msg.setLanguage(param);
		if (!(param = ontology.getText()).equals(""))
			msg.setOntology(param);
		if (!(param = content.getText()).equals(""))
			msg.setContent(param);
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
		//if (m == null)
		//System.out.println("MERDA");
		return m;
	}

}

