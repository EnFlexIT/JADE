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

/**
 * The AclGui class is an extension of Swing JPanel customized with the 
 * controls necessary to edit/display an ACL message.<p>
 * <p>
 * There are basically two ways of using the AclGui class.
 * <ul>
 * <li> As it derives from JComponent, an AclGui object can be 
 * directly added to a GUI where a certain area should be permanently
 * dedicated to the editing/display of ACL messages 
 * <li> Moreover it also provides static methods to edit/display an 
 * ACL message in a temporary dialog window.  
 * </ul>
 */

package jade.tools.DummyAgent;

// Import AWT and Swing classes to create the frame and to handle user interactions
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

// Import the java.util.Vector class
import java.util.*;

// Import useful Jade classes
import jade.core.*;
import jade.lang.acl.*;

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
	private static String fipaProtocols[] = {"fipa-request",
                                             "fipa-query",
                                             "fipa-request-when",
                                             "fipa-contract-net",
                                             "fipa-iterated-contract-net",
                                             "fipa-auction-english",
                                             "fipa-auction-dutch"};
	private Vector fipaProtocolVector;


	// Data for the editing of user defined iteration protocols
	private int    lastSelectedIndex;
	private String lastSelectedItem;
	private static final String LABEL_TO_ADD_PROT = "USER DEFINED";
 
	// These data are used to correctly handle the resizing of the AclGui panel
	private JPanel       aclPanel;
	private Dimension    minDim;
	private boolean      firstPaintFlag;

	// Other data used
	private static ACLMessage editedMsg;

	
	
	/////////////////
	// CONSTRUCTOR
	/////////////////
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
												if (!protocol.isEditable()) // The previously selected protocol was non editable
												{
													if (fipaProtocolVector.indexOf((Object) param) < 0 && param != "Null")
													{
														protocol.setEditable(true);
													}
												}
												else // The user has just edited a protocol
												{
													// The user simply selected a FIPA protocol or null
													if (fipaProtocolVector.indexOf((Object) param) >= 0 || param == "Null")
													{
														protocol.setEditable(false);
														protocol.setSelectedItem(param);
													}
													// The user simply selected the label to add a new protocol
													else if (param == LABEL_TO_ADD_PROT)
													{
														protocol.setSelectedItem(param);
													}	
													// The user actually added a new protocol (unless the "new protocol" is null)
													else if (lastSelectedItem == LABEL_TO_ADD_PROT)
													{
														if (param.length() != 0)
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
														protocol.removeItemAt(lastSelectedIndex);
														if (param.length() == 0)
														{
															protocol.setEditable(false);
															protocol.setSelectedItem("Null");
														}
														else 
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
													TimeChooser t = new TimeChooser(replyByDate);
													try
													{
														t.setDate(ISO8601.toDate(replyBy.getText()));
				 									}
													catch (Exception ee)
													{
														//replyByDate = null;
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
													else if (command == "View")
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
		if ((param = msg.getDest()) == null) param = "";
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
				protocol.setSelectedItem("USER DEFINED");
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
	
	public ACLMessage getMsg()
	{
	String param;

		param = (String) communicativeAct.getSelectedItem();
		ACLMessage msg = new ACLMessage(param);

		if ((param = sender.getText()).length() != 0)
			msg.setSource(param);
		if ((param = receiver.getText()).length() != 0)
			msg.setDest(param);
		if ((param = replyWith.getText()).length() != 0)
			msg.setReplyWith(param);
		if ((param = inReplyTo.getText()).length() != 0)
			msg.setReplyTo(param);
		if ((param = conversationId.getText()).length() != 0)
			msg.setConversationId(param);
		if ((param = replyBy.getText()).length() != 0)
			msg.setReplyBy(param);
		if ((param = envelope.getText()).length() != 0)
			msg.setEnvelope(param);
		if ((param = (String) protocol.getSelectedItem()) != "Null")
			msg.setProtocol(param);
		if ((param = language.getText()).length() != 0)
			msg.setLanguage(param);
		if ((param = ontology.getText()).length() != 0)
			msg.setOntology(param);
		if ((param = content.getText()).length() != 0)
			msg.setContent(param);
		return msg;
	}
	
	
	/////////////////////////
	// UTILITY PUBLIC METHODS
	/////////////////////////
	public void setEnabled(boolean enabledFlag)
	{
		guiEnabledFlag = enabledFlag;
		updateEnabled();
	}

	public void setSenderEnabled(boolean enabledFlag)
	{
		senderEnabledFlag = enabledFlag;
		updateEnabled();
	}

	public void setBorder(Border b)
	{
		if (aclPanel != null)
			aclPanel.setBorder(b);
	}

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
	public static void showMsgInDialog(ACLMessage m, Frame parent)
	{
		AclGui aclPanel = new AclGui();
		aclPanel.setBorder(new BevelBorder(BevelBorder.RAISED));
		aclPanel.setEnabled(false);
		aclPanel.setMsg(m);

		JButton okButton = new JButton("OK");
		JPanel p = new JPanel();
		p.setLayout(new GridLayout(1,1));
		p.add(okButton);

		final JDialog tempAclDlg = new JDialog(parent, "ACL Message", true);
		tempAclDlg.getContentPane().setLayout(new BorderLayout());
		tempAclDlg.getContentPane().add("Center", aclPanel);
		tempAclDlg.getContentPane().add("South", p);

		okButton.addActionListener(new ActionListener()
									   {
											public void actionPerformed(ActionEvent e)
											{
												tempAclDlg.dispose();
											}
									   } );

		tempAclDlg.pack();
		tempAclDlg.setResizable(false);
		tempAclDlg.setLocation(parent.getX() + (parent.getWidth() - tempAclDlg.getWidth()) / 2, parent.getY() + (parent.getHeight() - tempAclDlg.getHeight() / 2));
		tempAclDlg.show();
	}

	public static ACLMessage editMsgInDialog(ACLMessage m, Frame parent)
	{
		final JDialog tempAclDlg = new JDialog(parent, "ACL Message", true);
		final AclGui  aclPanel = new AclGui();
		aclPanel.setBorder(new BevelBorder(BevelBorder.RAISED));
		aclPanel.setSenderEnabled(true);
		aclPanel.setMsg(m);

		JButton okButton = new JButton("OK");
		JButton cancelButton = new JButton("Cancel");
		JPanel p = new JPanel();
		p.setLayout(new GridLayout(1,2));
		p.add(okButton);
		p.add(cancelButton);  

		tempAclDlg.getContentPane().setLayout(new BorderLayout());
		tempAclDlg.getContentPane().add("Center", aclPanel);
		tempAclDlg.getContentPane().add("South", p);
		
		okButton.addActionListener(new ActionListener()
									   {
											public void actionPerformed(ActionEvent e)
											{
												tempAclDlg.dispose();
												editedMsg = aclPanel.getMsg();
											}
									   } );
		cancelButton.addActionListener(new ActionListener()
										   {
												public void actionPerformed(ActionEvent e)
												{
													tempAclDlg.dispose();
													editedMsg = null;
												}
										   } );
		
		tempAclDlg.pack();
		tempAclDlg.setResizable(false);
		tempAclDlg.setLocation(parent.getX() + (parent.getWidth() - tempAclDlg.getWidth()) / 2, parent.getY() + (parent.getHeight() - tempAclDlg.getHeight() / 2));
		tempAclDlg.show();

		return editedMsg;
	}

}

