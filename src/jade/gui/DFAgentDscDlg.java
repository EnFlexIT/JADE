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
import java.util.*;

// Import required Jade classes
import jade.domain.AgentManagementOntology;
import jade.domain.AgentManagementOntology.DFAgentDescriptor;

/**
@author Tiziana Trucco - CSELT S.p.A
@version $Date$ $Revision$
*/
class DFAgentDscDlg extends JDialog 
{
	JTextField agentName;
	JComboBox  agentAddresses;
	JTextField agentType;
	JTextField dfState;
	JTextField ownership;
	JTextField ontology;
	JTextField language;
	JComboBox  protocols;
	Dialog     dlgParent;

	// CONSTRUCTORS
	DFAgentDscDlg(Frame parent) 
	{
		super(parent);
		dlgParent = (Dialog) this;
	}

	DFAgentDscDlg(Dialog parent) 
	{
		super(parent);
		dlgParent = (Dialog) this;
	}

	void viewDFD(AgentManagementOntology.DFAgentDescriptor dfd)
	{
		setTitle("DF description");

		JPanel p = new JPanel();
		JPanel main = new JPanel();
		JPanel main1 = new JPanel();
		
		JLabel l;
		JPanel bPane;
	  JScrollPane lPane;
		JButton bAdd, bModify, bRemove;
		
		main1.setLayout(new BoxLayout(main1, BoxLayout.X_AXIS));
		main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
		
		p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
		
		// Agent name 
		l = new JLabel("Agent-name:");
		l.setPreferredSize(new Dimension(80,20));
		p.add(l);
		p.add(Box.createHorizontalGlue());
		agentName = new JTextField();
		agentName.setEditable(false);
		agentName.setPreferredSize(new Dimension (250, 20));
		p.add(agentName);
		main.add(p);
		main.add(Box.createRigidArea(new Dimension (0,3)));
		
		// Agent addresses 
		JPanel pAddress = new JPanel();
		pAddress .setLayout(new BorderLayout());
		DefaultListModel addressesListModel = new DefaultListModel();
		JList addressesList = new JList(addressesListModel);
		lPane = new JScrollPane();
		lPane.getViewport().setView(addressesList);
		lPane.setPreferredSize( new Dimension ( 250, 100));
		pAddress .add(lPane, BorderLayout.CENTER);
	  pAddress .setBorder(BorderFactory.createTitledBorder("Agent-addresses"));
		main.add(pAddress);
		main.add(Box.createRigidArea(new Dimension (0,3)));

		// Agent type 
		p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
		l = new JLabel("Agent-type:");
		l.setPreferredSize(new Dimension(80,20));
		p.add(l);
		p.add(Box.createHorizontalGlue());
		agentType= new JTextField();
		agentType.setEditable(false);
    p.add(agentType);
    main.add(p);
	  main.add(Box.createRigidArea(new Dimension (0,3)));
	  
    // Ownership 	
    p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
    l = new JLabel("Ownership:");
    l.setPreferredSize(new Dimension(80,20));
		p.add(l);	
		p.add(Box.createHorizontalGlue());
		ownership = new JTextField();
		ownership.setEditable(false);
		p.add(ownership);
		main.add(p);
		main.add(Box.createRigidArea(new Dimension (0,3)));

		// DF state 
		p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
    l = new JLabel("DF-state:");
    l.setPreferredSize(new Dimension(80,20));
    p.add(l);
	  p.add(Box.createHorizontalGlue());
		dfState = new JTextField();
		dfState.setEditable(false);
		p.add(dfState);
		main.add(p);
	  main.add(Box.createRigidArea(new Dimension (0,3)));

	  // Ontology 
	  p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
    l = new JLabel("Ontology:");
    l.setPreferredSize(new Dimension(80,20));
	  p.add(l);	
	  p.add(Box.createHorizontalGlue());
		ontology = new JTextField();
		ontology.setEditable(false);
		p.add(ontology);
		main.add(p);
		main.add(Box.createRigidArea(new Dimension (0,3)));
		
		// Language
		p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
		l = new JLabel("Language:");
		l.setPreferredSize(new Dimension(80,20));
    p.add(l);
    p.add(Box.createHorizontalGlue());
		language = new JTextField();
		language.setEditable(false);
	  p.add(language);
    main.add(p);
		main.add(Box.createRigidArea(new Dimension (0,3)));
		
		// Interaction protocols 
		JPanel pProtocols = new JPanel();
		pProtocols .setLayout(new BorderLayout());
		DefaultListModel protocolsListModel = new DefaultListModel();
		JList protocolsList = new JList(protocolsListModel);
		lPane = new JScrollPane();
		lPane.getViewport().setView(protocolsList);
		pProtocols .add(lPane, BorderLayout.CENTER);
		pProtocols .setBorder(BorderFactory.createTitledBorder("Interaction-protocols"));
		main.add(pProtocols);
		main.add(Box.createRigidArea(new Dimension (0,3)));

    main1.add(main);
    
    
    // Services list
		JPanel p1 = new JPanel();
		p1.setLayout(new BorderLayout());
		final DefaultListModel servicesListModel = new DefaultListModel();
		final JList servicesList = new JList(servicesListModel);
		servicesList.setCellRenderer(new ServiceDscCellRenderer());
		lPane = new JScrollPane();
		lPane.getViewport().setView(servicesList);
		p1.setPreferredSize(new Dimension (300,400));
		p1.add(lPane, BorderLayout.CENTER);
    
		// View Button
		bPane = new JPanel();
		JButton bView = new JButton("View");
		bPane.add(bView);

		MouseListener mouseListenerService = new MouseAdapter()
		{
     			public void mouseClicked(MouseEvent e)
			{
				if (e.getClickCount() == 2)
				{
					int i = servicesList.getSelectedIndex();
					if (i != -1)
					{
						ServiceDscDlg dlg = new ServiceDscDlg(dlgParent);
						dlg.viewSD((AgentManagementOntology.ServiceDescriptor) servicesListModel.getElementAt(i));
					} 
				}  
			} 
 		};
 		servicesList.addMouseListener(mouseListenerService); 

            bView.addActionListener( new ActionListener()
		                           {
						   	public void actionPerformed(ActionEvent e)
							{    
								String param = (String) e.getActionCommand();
								if (param.equals("View"))
								{
									int i = servicesList.getSelectedIndex();
									if (i != -1)
									{
										ServiceDscDlg dlg = new ServiceDscDlg(dlgParent);
										dlg.viewSD((AgentManagementOntology.ServiceDescriptor) servicesListModel.getElementAt(i));
									} 
								}
							} 
		                           } );
		p1.add(bPane, BorderLayout.SOUTH);
		p1.setBorder(BorderFactory.createTitledBorder("Agent services"));
		main1.add(p1);

    
    getContentPane().add(main1,BorderLayout.NORTH);
			
	
		// INITIALIZATION
		if (dfd != null)
		{
			agentName.setText(dfd.getName());
		  agentType.setText(dfd.getType());
			ownership.setText(dfd.getOwnership());
			dfState.setText(dfd.getDFState());
			ontology.setText(dfd.getOntology());
			//language.setText(dfd.getLanguage());
			
			Enumeration temp;
			temp = dfd.getAddresses();
			while (temp.hasMoreElements())
				addressesListModel.add(0, temp.nextElement());
			temp = dfd.getInteractionProtocols();
			while (temp.hasMoreElements())
				protocolsListModel.add(0, temp.nextElement());
			temp = dfd.getAgentServices();
			while (temp.hasMoreElements())
				servicesListModel.add(0, temp.nextElement());
		}

		// OK BUTTON
		bPane = new JPanel();
		bPane.setLayout(new BoxLayout(bPane, BoxLayout.Y_AXIS));
		JButton bOK = new JButton("OK");
		bOK.addActionListener( new ActionListener()
		                           {
						   	public void actionPerformed(ActionEvent e)
							{    
								String param = (String) e.getActionCommand();
								if (param.equals("OK"))
								{
									dispose();
								}
							} 
		                           } );
		
	  bPane.add(Box.createRigidArea(new Dimension(0,20)));
    bOK.setAlignmentX(Component.CENTER_ALIGNMENT);
		bPane.add(bOK);
		bPane.add(Box.createRigidArea(new Dimension(0,10)));

		getContentPane().add(bPane, BorderLayout.SOUTH);

		setModal(true);
		setResizable(false);
		setLocation(50, 50);
		pack();
		show();

	}

	AgentManagementOntology.DFAgentDescriptor editDFD(AgentManagementOntology.DFAgentDescriptor dfd)
	{
		final IntRetValue ret = new IntRetValue();
		ret.setValue(0);
		
		setTitle("DF description");

		JPanel p = new JPanel();
		JPanel main = new JPanel();
		JPanel main1 = new JPanel();
	  JLabel l;
		JPanel bPane;
	  JScrollPane lPane;
		JButton bAdd, bModify, bRemove;

		main1.setLayout(new BoxLayout(main1, BoxLayout.X_AXIS));
		main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
		
		p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
		
	  // Agent name 
		l = new JLabel("Agent-name:");
		l.setPreferredSize(new Dimension(80,20));
		p.add(l);
		p.add(Box.createHorizontalGlue());
		agentName = new JTextField();
		agentName.setPreferredSize(new Dimension (250, 20));
	  if (dfd !=null)
	  	if (!dfd.getName().equals(""))
	  		agentName.setEditable(true);
		p.add(agentName);
		main.add(p);
		main.add(Box.createRigidArea(new Dimension (0,3)));
	
	  // Agent addresses 
		JPanel pAddress = new JPanel();
		pAddress .setLayout(new BorderLayout());
		final DefaultListModel addressesListModel = new DefaultListModel();
		final JList addressesList = new JList(addressesListModel);
		lPane = new JScrollPane();
		lPane.getViewport().setView(addressesList);
		lPane.setPreferredSize( new Dimension ( 250, 100));
		pAddress .add(lPane, BorderLayout.CENTER);
		// Buttons
		bPane = new JPanel();
		bAdd = new JButton("Add");
		bModify = new JButton("Modify");
		bRemove = new JButton("Remove");
		bAdd.setPreferredSize(bRemove.getPreferredSize());
		bModify.setPreferredSize(bRemove.getPreferredSize());
		bPane.add(bAdd);
		bPane.add(bModify);
		bPane.add(bRemove);

		MouseListener mouseListenerAddresses = new MouseAdapter()
		{
     			public void mouseClicked(MouseEvent e)
			{
				if (e.getClickCount() == 2)
				{
					StringDlg dlg = new StringDlg(dlgParent, "Address:");
					int i = addressesList.getSelectedIndex();
					if (i != -1)
					{
						String editedString = dlg.editString((String) addressesListModel.getElementAt(i));
						if (editedString != null && !editedString.equals(""))
						addressesListModel.setElementAt((Object) editedString, i);
					}
				}  
			} 
 		};
 		addressesList.addMouseListener(mouseListenerAddresses); 

		KeyListener keyListenerAddresses = new KeyAdapter()
		{
			public void keyPressed(KeyEvent e)
			{
				int code = e.getKeyCode();
				if (code == KeyEvent.VK_CANCEL || code == KeyEvent.VK_DELETE)
				{
					int i = addressesList.getSelectedIndex();
					if (i != -1)
					{
						addressesListModel.removeElementAt(i);
					}
				}

			}
		}; 
 		addressesList.addKeyListener(keyListenerAddresses); 

		bAdd.addActionListener( new ActionListener()
		                           {
						   	public void actionPerformed(ActionEvent e)
							{    
								String param = (String) e.getActionCommand();
								if (param.equals("Add"))
								{
									StringDlg dlg = new StringDlg(dlgParent, "Address:");
									String editedString = dlg.editString(null);
									if (editedString != null && !editedString.equals(""))
										addressesListModel.add(0, (Object) editedString);
								}
							} 
		                           } );            
            bModify.addActionListener( new ActionListener()
		                           {
						   	public void actionPerformed(ActionEvent e)
							{    
								String param = (String) e.getActionCommand();
								if (param.equals("Modify"))
								{
									StringDlg dlg = new StringDlg(dlgParent, "Address:");
									int i = addressesList.getSelectedIndex();
									if (i != -1)
									{
										String editedString = dlg.editString((String) addressesListModel.getElementAt(i));
										if (editedString != null && !editedString.equals(""))
											addressesListModel.setElementAt((Object) editedString, i);
									}
								}
							} 
		                           } );            
            bRemove.addActionListener( new ActionListener()
		                           {
						   	public void actionPerformed(ActionEvent e)
							{    
								String param = (String) e.getActionCommand();
								if (param.equals("Remove"))
								{
									int i = addressesList.getSelectedIndex();
									if (i != -1)
									{
										addressesListModel.removeElementAt(i);
									}
								}
							} 
		                           });

		
	  pAddress.add(bPane, BorderLayout.SOUTH);
		pAddress .setBorder(BorderFactory.createTitledBorder("Agent-addresses"));
		main.add(pAddress);
		main.add(Box.createRigidArea(new Dimension (0,3)));
		
		// Agent type 
		p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
		l = new JLabel("Agent-type:");
		l.setPreferredSize(new Dimension(80,20));
		p.add(l);
		p.add(Box.createHorizontalGlue());
		agentType= new JTextField();
    p.add(agentType);
    main.add(p);
	  main.add(Box.createRigidArea(new Dimension (0,3)));

    // Ownership 	
    p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
    l = new JLabel("Ownership:");
    l.setPreferredSize(new Dimension(80,20));
		p.add(l);	
		p.add(Box.createHorizontalGlue());
		ownership = new JTextField();
		p.add(ownership);
		main.add(p);
		main.add(Box.createRigidArea(new Dimension (0,3)));

		// DF state 
		p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
    l = new JLabel("DF-state:");
    l.setPreferredSize(new Dimension(80,20));
    p.add(l);
	  p.add(Box.createHorizontalGlue());
		dfState = new JTextField();
		p.add(dfState);
		main.add(p);
	  main.add(Box.createRigidArea(new Dimension (0,3)));

	  // Ontology 
	  p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
    l = new JLabel("Ontology:");
    l.setPreferredSize(new Dimension(80,20));
	  p.add(l);	
	  p.add(Box.createHorizontalGlue());
		ontology = new JTextField();
		p.add(ontology);
		main.add(p);
		main.add(Box.createRigidArea(new Dimension (0,3)));
		
		// Language
		p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
		l = new JLabel("Language:");
		l.setPreferredSize(new Dimension(80,20));
    p.add(l);
    p.add(Box.createHorizontalGlue());
		language = new JTextField();
	  p.add(language);
    main.add(p);
		main.add(Box.createRigidArea(new Dimension (0,3)));
		
		// Interaction protocols 
		JPanel pProtocols = new JPanel();
		pProtocols .setLayout(new BorderLayout());
		final DefaultListModel protocolsListModel = new DefaultListModel();
		final JList protocolsList = new JList(protocolsListModel);
		lPane = new JScrollPane();
		lPane.getViewport().setView(protocolsList);
		pProtocols .add(lPane, BorderLayout.CENTER);
		//Buttons
		bPane = new JPanel();
		bAdd = new JButton("Add");
		bModify = new JButton("Modify");
		bRemove = new JButton("Remove");
		bAdd.setPreferredSize(bRemove.getPreferredSize());
		bModify.setPreferredSize(bRemove.getPreferredSize());
		bPane.add(bAdd);
		bPane.add(bModify);
		bPane.add(bRemove);

		MouseListener mouseListenerProtocols = new MouseAdapter()
		{
     			public void mouseClicked(MouseEvent e)
			{
				if (e.getClickCount() == 2)
				{
					StringDlg dlg = new StringDlg(dlgParent, "Interaction protocol:");
					int i = protocolsList.getSelectedIndex();
					if (i != -1)
					{
						String editedString = dlg.editString((String) protocolsListModel.getElementAt(i));
						if (editedString != null && !editedString.equals(""))
						protocolsListModel.setElementAt((Object) editedString, i);
					}
				}  
			} 
 		};
 		protocolsList.addMouseListener(mouseListenerProtocols); 

		KeyListener keyListenerProtocols = new KeyAdapter()
		{
			public void keyPressed(KeyEvent e)
			{
				int code = e.getKeyCode();
				if (code == KeyEvent.VK_CANCEL || code == KeyEvent.VK_DELETE)
				{
					int i = protocolsList.getSelectedIndex();
					if (i != -1)
					{
						protocolsListModel.removeElementAt(i);
					}
				}

			}
		}; 
 		protocolsList.addKeyListener(keyListenerProtocols); 

            bAdd.addActionListener( new ActionListener()
		                           {
						   	public void actionPerformed(ActionEvent e)
							{    
								String param = (String) e.getActionCommand();
								if (param.equals("Add"))
								{
									StringDlg dlg = new StringDlg(dlgParent, "Interaction protocol:");
									String editedString = dlg.editString(null);
									if (editedString != null && !editedString.equals(""))
										protocolsListModel.add(0, (Object) editedString);
								}
							} 
		                           } );            
            bModify.addActionListener( new ActionListener()
		                           {
						   	public void actionPerformed(ActionEvent e)
							{    
								String param = (String) e.getActionCommand();
								if (param.equals("Modify"))
								{
									StringDlg dlg = new StringDlg(dlgParent, "Interaction protocol:");
									int i = protocolsList.getSelectedIndex();
									if (i != -1)
									{
										String editedString = dlg.editString((String) protocolsListModel.getElementAt(i));
										if (editedString != null && !editedString.equals(""))
											protocolsListModel.setElementAt((Object) editedString, i);
									}
								}
							} 
		                           } );            
            bRemove.addActionListener( new ActionListener()
		                           {
						   	public void actionPerformed(ActionEvent e)
							{    
								String param = (String) e.getActionCommand();
								if (param.equals("Remove"))
								{
									int i = protocolsList.getSelectedIndex();
									if (i != -1)
									{
										protocolsListModel.removeElementAt(i);
									}
								}
							} 
		                           } );            
		pProtocols .add(bPane, BorderLayout.SOUTH);
		pProtocols .setBorder(BorderFactory.createTitledBorder("Interaction-protocols"));
		main.add(pProtocols);
		main.add(Box.createRigidArea(new Dimension (0,3)));

    main1.add(main);

    // Services list
		JPanel p1 = new JPanel();
		p1.setLayout(new BorderLayout());
		final DefaultListModel servicesListModel = new DefaultListModel();
		final JList servicesList = new JList(servicesListModel);
		servicesList.setCellRenderer(new ServiceDscCellRenderer());
		lPane = new JScrollPane();
		lPane.getViewport().setView(servicesList);
		p1.setPreferredSize(new Dimension (300,400));
		p1.add(lPane, BorderLayout.CENTER);
		//Buttons
		bPane = new JPanel();
		bAdd = new JButton("Add");
		bModify = new JButton("Modify");
		bRemove = new JButton("Remove");
		bAdd.setPreferredSize(bRemove.getPreferredSize());
		bModify.setPreferredSize(bRemove.getPreferredSize());
		bPane.add(bAdd);
            bPane.add(bModify);
		bPane.add(bRemove);

		MouseListener mouseListenerService = new MouseAdapter()
		{
     			public void mouseClicked(MouseEvent e)
			{
				if (e.getClickCount() == 2)
				{
					int i = servicesList.getSelectedIndex();
					if (i != -1)
					{
						ServiceDscDlg dlg = new ServiceDscDlg(dlgParent);
						AgentManagementOntology.ServiceDescriptor dsc = dlg.editSD((AgentManagementOntology.ServiceDescriptor) servicesListModel.getElementAt(i));
						if (dsc != null)
						{	
							servicesListModel.setElementAt((Object) dsc, i);
						}
					} 
				}  
			} 
 		};
 		servicesList.addMouseListener(mouseListenerService); 

		KeyListener keyListenerService = new KeyAdapter()
		{
			public void keyPressed(KeyEvent e)
			{
				int code = e.getKeyCode();
				if (code == KeyEvent.VK_CANCEL || code == KeyEvent.VK_DELETE)
				{
					int i = servicesList.getSelectedIndex();
					if (i != -1)
					{
						servicesListModel.removeElementAt(i);
					}
				}

			}
		}; 
 		servicesList.addKeyListener(keyListenerService); 

            bAdd.addActionListener( new ActionListener()
		                           {
						   	public void actionPerformed(ActionEvent e)
							{    
								String param = (String) e.getActionCommand();
								if (param.equals("Add"))
								{
									ServiceDscDlg dlg = new ServiceDscDlg(dlgParent);
									AgentManagementOntology.ServiceDescriptor dsc = dlg.editSD(null);
									if (dsc != null)
										servicesListModel.add(0, (Object) dsc);
								}
							} 
		                           } );            
            bModify.addActionListener( new ActionListener()
		                           {
						   	public void actionPerformed(ActionEvent e)
							{    
								String param = (String) e.getActionCommand();
								if (param.equals("Modify"))
								{
									int i = servicesList.getSelectedIndex();
									if (i != -1)
									{
										ServiceDscDlg dlg = new ServiceDscDlg(dlgParent);
										AgentManagementOntology.ServiceDescriptor dsc = dlg.editSD((AgentManagementOntology.ServiceDescriptor) servicesListModel.getElementAt(i));
										if (dsc != null)
											servicesListModel.setElementAt((Object) dsc, i);
									} 
								}
							} 
		                           } );
            bRemove.addActionListener( new ActionListener()
		                           {
						   	public void actionPerformed(ActionEvent e)
							{    
								String param = (String) e.getActionCommand();
								if (param.equals("Remove"))
								{

									int i = servicesList.getSelectedIndex();
									if (i != -1)
									{
										servicesListModel.removeElementAt(i);
									}
								}
							} 
		                           } );
		p1.add(bPane, BorderLayout.SOUTH);
		p1.setBorder(BorderFactory.createTitledBorder("Agent services"));
		main1.add(p1);

		getContentPane().add(main1, BorderLayout.NORTH);
	  
		// INITIALIZATION
		if (dfd != null)
		{
			agentName.setText(dfd.getName());
			agentType.setText(dfd.getType());
			ownership.setText(dfd.getOwnership());
			dfState.setText(dfd.getDFState());
			ontology.setText(dfd.getOntology());
			//language.setText(dfd.getLanguage());
			
			Enumeration temp;
			temp = dfd.getAddresses();
			while (temp.hasMoreElements())
				addressesListModel.add(0, temp.nextElement());
			temp = dfd.getInteractionProtocols();
			while (temp.hasMoreElements())
				protocolsListModel.add(0, temp.nextElement());
			temp = dfd.getAgentServices();
			while (temp.hasMoreElements())
				servicesListModel.add(0, temp.nextElement());
		}

		// OK AND CANCEL BUTTONS
		bPane = new JPanel();
		bPane.setLayout(new FlowLayout(FlowLayout.CENTER));
	//	bPane.add(Box.createRigidArea(new Dimension(0,30)));
		JButton bOK = new JButton("OK");
	
		JButton bCancel = new JButton("Cancel");
	
		bOK.setPreferredSize(bCancel.getPreferredSize());
		bPane.add(bOK);
		bOK.addActionListener( new ActionListener()
		                           {
						   	public void actionPerformed(ActionEvent e)
							{    
								String param = (String) e.getActionCommand();
								if (param.equals("OK"))
								{
									ret.setValue(1);
									dispose();
								}
							} 
		                           } );
		bPane.add(bCancel);
		bCancel.addActionListener( new ActionListener()
		                           {
						   	public void actionPerformed(ActionEvent e)
							{    
								String param = (String) e.getActionCommand();
								if (param.equals("Cancel"))
								{
									ret.setValue(0);
									dispose();
								}
							} 
		                           } );
		                           
		bPane.add(Box.createRigidArea(new Dimension(0,70)));
	                           
		getContentPane().add(bPane, BorderLayout.SOUTH);
				
		setModal(true);
		setResizable(false);
		setLocation(50, 50);
		pack();
		show();
		if (ret.getValue() == 1)
		{	
			AgentManagementOntology.DFAgentDescriptor editedDfd = new AgentManagementOntology.DFAgentDescriptor();
		
			editedDfd.setName(getSaveText(agentName));
			editedDfd.setDFState(getSaveText(dfState));
			editedDfd.setOwnership(getSaveText(ownership));
      editedDfd.setType(getSaveText(agentType));
      editedDfd.setOntology(getSaveText(ontology));
      // editedDfd.setLanguage(language.getText());
		
			Enumeration temp;
			editedDfd.removeAddresses();
			temp = addressesListModel.elements();
			while (temp.hasMoreElements())
				editedDfd.addAddress((String) temp.nextElement());
			editedDfd.removeInteractionProtocols();
			temp = protocolsListModel.elements();
			while (temp.hasMoreElements())
				editedDfd.addInteractionProtocol((String) temp.nextElement());
			editedDfd.removeAgentServices();
			temp = servicesListModel.elements();
			while (temp.hasMoreElements())
				editedDfd.addAgentService((AgentManagementOntology.ServiceDescriptor) temp.nextElement()); 

			return(editedDfd);
 		}
		return(null);		
	}

	//Returns a String corresponding to the textfield if not empty, null otherwise
	private String getSaveText(JTextField field){
	
		try {
			String out = field.getText();
			return (out.length() == 0 ? null : out); 
		}catch(Exception e){
		return null;
		}
	
	}
}