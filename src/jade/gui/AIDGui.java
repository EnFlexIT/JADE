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
 
import javax.swing.*;
import java.awt.event.*;
import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.Color;
import java.util.Enumeration;
import java.util.Properties;

import java.io.StringWriter;

import jade.core.AID;
import jade.domain.FIPAAgentManagement.*;

/**
 This class provide a gui to show an AID of an agent.
 @see jade.core.AID
 @author Tiziana Trucco - CSELT S.p.A.
 @version $Date$ $Revision$
 
*/
 
 public class AIDGui extends JDialog{
 	
 	/**
  @serial
  */
  private boolean editable, checkSlots;
 	/**
  @serial
  */
  private AID agentAID;
 	/**
  @serial
  */
  private JTextField nameText;
 	/**
  @serial
  */
  private VisualStringList addressListPanel;
 	/**
  @serial
  */
  private VisualAIDList resolverListPanel;
 	/**
  @serial
  */
  private VisualPropertiesList	propertiesListPanel;
 	/**
  @serial
  */
  private AID out;
 	
 	public AIDGui()
 	{
   super();	
   setTitle("AID");
 	}
 	
 /**
 This method shows an AID with a GUI.
 @param agentIdentifier is the AID to be shown
 @param editable is true when the AID can be edited
 @param checkMandatorySlots is true when the returned AID must contain all the mandatory slots, as defined by FIPA specs
 @return null if the user pushs the Cancel button, an AID otherwise
 **/
 	public AID ShowAIDGui(AID agentIdentifier, boolean ed, boolean checkMandatorySlots)
 	{
 	  this.out = null;
 		this.editable = ed;
 	  this.checkSlots = checkMandatorySlots;
 	  
 		if(agentIdentifier == null)
 			this.agentAID =  new AID();
 		else
 		  this.agentAID = agentIdentifier;
 		
 		JLabel label;
 
 		JPanel mainPanel = new JPanel();
 		
 		mainPanel.setLayout(new BoxLayout(mainPanel,BoxLayout.Y_AXIS));
 	  
 		//Name
 		JPanel namePanel = new JPanel();
 	  namePanel.setLayout(new BoxLayout(namePanel,BoxLayout.X_AXIS));
 	  label = new JLabel("NAME");
 	  label.setPreferredSize(new Dimension(80,26));
 	  label.setMinimumSize(new Dimension(80,26));
    label.setMaximumSize(new Dimension(80,26));
    namePanel.add(label);
    nameText = new JTextField();
    nameText.setBackground(Color.white);
    nameText.setText(agentAID.getName());
 	  nameText.setEditable(editable);
 		namePanel.add(nameText);
 		
 		mainPanel.add(namePanel);
 		
 		//Addresses
 		JPanel addressesPanel = new JPanel();
 		addressesPanel.setLayout(new BorderLayout());
		addressesPanel.setBorder(BorderFactory.createTitledBorder("Addresses"));
    addressListPanel = new VisualStringList(agentAID.getAllAddresses());
    addressListPanel.setDimension(new Dimension(200,40));
    addressListPanel.setEnabled(editable);
 		addressesPanel.add(addressListPanel);
 		mainPanel.add(addressesPanel);
 		
 		
 		//Resolvers
 		JPanel resolversPanel = new JPanel();
 		resolversPanel.setLayout(new BorderLayout());
 		resolversPanel.setBorder(BorderFactory.createTitledBorder("Resolvers"));
 	  resolverListPanel = new VisualAIDList(agentAID.getAllResolvers());
 	  resolverListPanel.setDimension(new Dimension(200,40));
 	  resolverListPanel.setEnabled(editable);
 	  resolverListPanel.setCheckMandatorySlots(checkMandatorySlots);
 	  resolversPanel.add(resolverListPanel);
 		mainPanel.add(resolversPanel); 
 		
 		
 		//Properties
 		JPanel propertiesPanel = new JPanel();
 	  propertiesPanel.setLayout(new BorderLayout());
 		propertiesPanel.setBorder(BorderFactory.createTitledBorder("Properties"));
 		propertiesListPanel = new VisualPropertiesList(agentAID.getAllUserDefinedSlot());
 		propertiesListPanel.setDimension(new Dimension(200,40));
 		propertiesListPanel.setEnabled(editable);
 		propertiesPanel.add(propertiesListPanel);
 		mainPanel.add(propertiesPanel);

 		//Button Ok-Cancel
 		JPanel buttonPanel = new JPanel();
 		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
 		JButton okButton = new JButton("OK");
 		okButton.addActionListener(new ActionListener()
 			{
 				public void actionPerformed(ActionEvent e)
 				{
 					String param = (String)e.getActionCommand();
 					if(param.equals("OK"))
 					{
 						
 						if(editable)
 						{
 							
 						  String name = (nameText.getText()).trim();
 						  if (checkSlots) 
 						    if (name.length() == 0) {
    		  				JOptionPane.showMessageDialog(null,"AID must have a non-empty name.","Error Message",JOptionPane.ERROR_MESSAGE); 
    		  				return;
    		  			}				  
 						  out = new AID();						  
 						  out.setName(name);
 						  //addresses
 						  Enumeration addresses = addressListPanel.getContent();
 						  
 						  while(addresses.hasMoreElements())
 						  	out.addAddresses((String)addresses.nextElement());
 						  //resolvers
 						  Enumeration resolvers = resolverListPanel.getContent();
 						  while(resolvers.hasMoreElements())
 						  	out.addResolvers((AID)resolvers.nextElement());
 						  //Properties
 						  Properties new_prop = propertiesListPanel.getContentProperties();
 						  Enumeration key_en = new_prop.propertyNames();
 						  while(key_en.hasMoreElements())
 						  {
 						  	String key = (String)key_en.nextElement();
 						  	out.addUserDefinedSlot(key, new_prop.getProperty(key));
 						  }
 						  
 						  
 						}
 						else
 						  out = agentAID;
 						dispose(); 
 					}
 				}
 			});
 			
 		buttonPanel.add(okButton);	
 		
 		if(editable)
 		{
 			JButton cancelButton = new JButton("Cancel");
 			cancelButton.addActionListener(new ActionListener(){
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
 			buttonPanel.add(cancelButton);
 		}
 		
 		mainPanel.add(buttonPanel);
 		
 		getContentPane().add(mainPanel, BorderLayout.CENTER);
 		pack();
 		setResizable(false);
 		setModal(true);
 		//setVisible(true);
 		
 		ShowCorrect();
 		
 		return out;
 	}
 	
 	private void ShowCorrect() 
 	 {
    pack();
    //setSize(300, 300);
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    int centerX = (int)screenSize.getWidth() / 2;
    int centerY = (int)screenSize.getHeight() / 2;
    Dimension sizePanel = getSize();
    int x = (new Double(sizePanel.getWidth())).intValue() / 2;
    int y = (new Double(sizePanel.getHeight())).intValue() / 2;
    setLocation(centerX - x, centerY - y);
    
    setVisible(true);
    toFront();
 	 }

 }