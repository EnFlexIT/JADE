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
import java.util.List;
import java.util.Iterator;
import java.util.Enumeration;

// Import required JADE classes
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.core.AID;

/**
@author Tiziana Trucco - CSELT S.p.A
@version $Date$ $Revision$
*/
public class DFAgentDscDlg extends JDialog 
{
	
	/**
  @serial
  */
  Dialog     dlgParent;
  /**
  @serial
  */
  DFAgentDescription dfdAgent;
	/**
  @serial
  */
  DFAgentDescription out;
	/**
  @serial
  */
  AID newAID = null;
	/**
  @serial
  */
  boolean editable;
	/**
  @serial
  */
  boolean checkSlots;
	
	/**
  @serial
  */
  private VisualStringList ontologiesListPanel;
	/**
  @serial
  */
  private VisualStringList languagesListPanel;
	/**
  @serial
  */
  private VisualStringList protocolsListPanel;
	/**
  @serial
  */
  private VisualServicesList servicesListPanel;
	/**
  @serial
  */
  private JTextField agentName;
	
	// CONSTRUCTORS
	public DFAgentDscDlg(Frame parent) 
	{
		super(parent);
		dlgParent = (Dialog) this;
	}

	public DFAgentDscDlg(Dialog parent) 
	{
		super(parent);
		dlgParent = (Dialog) this;
	}

	/**
	* This method show a a giu for a DFAgentDescription.
	* @param dfd the DFAgentDescrption to show
	* @param ed true if the fields of the gui must be editable, false otherwise.
	* @param checkMandatorySlots true to verify that a value is specified for all the mandatory fields, false otherwise.  
	* @return a DFAgentDescription if the OK button is pressed, false if the Cancel button is pressed.
	*/
	
	public DFAgentDescription ShowDFDGui(DFAgentDescription dfd, boolean ed , boolean checkMandatorySlots)
	{
		setTitle("DF description");

		this.out = null;
		this.editable = ed;
		this.checkSlots = checkMandatorySlots;
	
		if(dfd != null)
		 {
		 	dfdAgent = dfd;
		 	newAID = dfd.getName();
		 }
		else
		 dfdAgent = new DFAgentDescription();
		 
		JPanel p = new JPanel();
		JPanel main = new JPanel();
	
		JLabel l;
		JPanel bPane;
		JButton AIDButton;
		
		main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
		
		p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
		Border raised = BorderFactory.createRaisedBevelBorder();
		main.setBorder(raised);
		
		// Agent name
		main.add(Box.createRigidArea(new Dimension(300,5)));

		l = new JLabel("Agent-name:");
		l.setPreferredSize(new Dimension(80,26));
		p.add(l);
		p.add(Box.createHorizontalGlue());
		agentName = new JTextField();
		agentName.setEditable(false);
		agentName.setPreferredSize(new Dimension (250, 26));
		agentName.setMinimumSize(new Dimension(250,26));
		agentName.setMaximumSize(new Dimension(250,26));
		agentName.setBackground(Color.white);
		AID aidtemp = dfdAgent.getName();
    if (aidtemp == null)
    	agentName.setText("");
    else
      agentName.setText(aidtemp.getName());
    
		AIDButton = new JButton(editable ? "Set":"View");
		
		AIDButton.addActionListener(new ActionListener(){
    	public void actionPerformed(ActionEvent e)
    	{
    		String command = e.getActionCommand();
    		AIDGui guiSender = new AIDGui(dlgParent);
    	
    		if(command.equals("View"))
    			guiSender.ShowAIDGui(dfdAgent.getName(), false,false);
    		else
    		  if(command.equals("Set"))
    		  	{
    		  		
    		  		newAID = guiSender.ShowAIDGui(dfdAgent.getName(),true,checkSlots);
    		  		
    		  		if (newAID != null)
    		  		{	
    		  			agentName.setText(newAID.getName());
    		  			dfdAgent.setName(newAID);
    		  		}
    		  	}
    			
    	}
    });
   
	  p.add(AIDButton); 
		p.add(agentName);
		main.add(p);
		main.add(Box.createRigidArea(new Dimension (0,3)));
		
	  // Ontologies 
	  JPanel pOntologies = new JPanel();
	  pOntologies.setLayout(new BorderLayout());
	  pOntologies .setBorder(BorderFactory.createTitledBorder("Ontologies"));
		ontologiesListPanel = new VisualStringList(dfdAgent.getAllOntologies(),dlgParent);
		ontologiesListPanel.setDimension(new Dimension(400,45));
		ontologiesListPanel.setEnabled(editable);
		pOntologies.add(ontologiesListPanel);
   	main.add(pOntologies);
		main.add(Box.createRigidArea(new Dimension (0,3)));
		
		// Languages
		JPanel pLanguages = new JPanel();
		pLanguages.setLayout(new BorderLayout());
		pLanguages .setBorder(BorderFactory.createTitledBorder("Languages"));
    languagesListPanel = new VisualStringList(dfdAgent.getAllLanguages(),dlgParent);
		languagesListPanel.setDimension(new Dimension(400,45));
		languagesListPanel.setEnabled(editable);
		pLanguages.add(languagesListPanel);
    main.add(pLanguages);
		main.add(Box.createRigidArea(new Dimension (0,3)));
		
		// Interaction protocols 
		JPanel pProtocols = new JPanel();
		pProtocols .setLayout(new BorderLayout());	
		pProtocols .setBorder(BorderFactory.createTitledBorder("Interaction-protocols"));
	  protocolsListPanel = new VisualStringList(dfdAgent.getAllProtocols(),dlgParent);
		protocolsListPanel.setDimension(new Dimension(400,45));
		protocolsListPanel.setEnabled(editable);
		pProtocols.add(protocolsListPanel);
		main.add(pProtocols);
		main.add(Box.createRigidArea(new Dimension (0,3)));
   
    // Services list
		JPanel pServices = new JPanel();
		pServices.setBorder(BorderFactory.createTitledBorder("Agent services"));
	  servicesListPanel = new VisualServicesList(dfdAgent.getAllServices(),dlgParent);
	  servicesListPanel.setDimension(new Dimension(400,45));
	  servicesListPanel.setEnabled(editable);
	  servicesListPanel.setCheckMandatorySlots(checkMandatorySlots);
	  pServices.add(servicesListPanel);
	  main.add(pServices);
	      
    getContentPane().add(main,BorderLayout.NORTH);
			
		// OK BUTTON
		bPane = new JPanel();
		bPane.setLayout(new BoxLayout(bPane, BoxLayout.X_AXIS));
		JButton bOK = new JButton("OK");
		bOK.addActionListener( new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{    
				String param = (String) e.getActionCommand();
				if (param.equals("OK"))
					{ // the user pressed the OK button
            if(editable)
            { // if it is editable then I have to collect all data in the GUI and create a DFAgentDescription to return to the caller
            	out = new DFAgentDescription();
            	
            	if(checkSlots)
            		//AID
            	  if (newAID == null) //newAID was set when the "Set" button was pressed
            		{
            			JOptionPane.showMessageDialog(dlgParent,"AID must have a non-empty name.","Error Message",JOptionPane.ERROR_MESSAGE); 
 						      return;
            	  }
								// There is no need to check the slots of ServiceDescription because it is
            	  // done already by ServiceDscDlg
            	  	
              out.setName(newAID);
            	            		
            	//Ontologies
            	Enumeration onto = ontologiesListPanel.getContent();
            	while(onto.hasMoreElements())
            		out.addOntologies((String)onto.nextElement());
            	
            	//Protocols
            	Enumeration proto = protocolsListPanel.getContent();
            	while(proto.hasMoreElements())
            		out.addProtocols((String)proto.nextElement());
            		
            	//Languages
            	Enumeration lang = languagesListPanel.getContent();
            	while(lang.hasMoreElements())
            		out.addLanguages((String)lang.nextElement());
            	
            	//Services
            	Enumeration serv = servicesListPanel.getContent();
            	while(serv.hasMoreElements())
            	  out.addServices((ServiceDescription)serv.nextElement());
            	  	
            }
            else
              out = dfdAgent; // if not editable returns the old dfd
            
						dispose();
					}
			} 
		} );
		
		bPane.add(bOK);
		
		if(editable)
		{
			JButton cancelButton = new JButton("Cancel");
			cancelButton.addActionListener(new ActionListener()
			{
			
				public void actionPerformed(ActionEvent e)
				{
					String param = e.getActionCommand();
					if(param.equals("Cancel"))
					{
						out = null;
						dispose();
					}
				}
			});
			
			bPane.add(cancelButton);
		}
		main.add(Box.createRigidArea(new Dimension(300,20)));
		main.add(bPane);
		main.add(Box.createRigidArea(new Dimension(300,20)));
		getContentPane().add(main, BorderLayout.CENTER);

		setModal(true);
		setResizable(false);
	
		ShowCorrect();
		return out;
    
	}
	
	private void ShowCorrect() 
 	 {
    pack();
   
    try{
    	int x = getOwner().getX() + (getOwner().getWidth() - getWidth()) / 2;
    	int y = getOwner().getY() + (getOwner().getHeight() - getHeight()) / 2; 
    	setLocation(x>0 ? x:0,y>0 ? y:0);
    }catch(Exception e){}

    setVisible(true);
    toFront();
 	 }


	
}