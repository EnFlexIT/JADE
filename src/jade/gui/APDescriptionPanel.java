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

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.BoxLayout;
import javax.swing.BorderFactory;
import java.awt.Dimension;
import java.util.Iterator;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import jade.domain.FIPAAgentManagement.APDescription;
import jade.domain.FIPAAgentManagement.APTransportDescription;
import jade.domain.FIPAAgentManagement.MTPDescription;
import javax.swing.JCheckBox;
import java.awt.Frame;
import javax.swing.JDialog;
import javax.swing.JButton;
import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Component;

/** This class permits to show the APDescription of a platform. 
@author Tiziana Trucco - CSELT S.p.A
@version $Date$ $Revision$
**/

public class APDescriptionPanel extends JPanel
{
	private JTextField platformName_Field;
	private VisualAPTransportProfileList MTPs_List;
  private JCheckBox dynamic;
	private JCheckBox mobility;

	
	/** 
	creates a panel ho show an APDescription.
	All the fields are not editable.
	*/
	public APDescriptionPanel(Component owner){
		
		super();
	
		GridBagLayout gridBag = new GridBagLayout();
		GridBagConstraints  c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.SOUTH;
		setLayout(gridBag);
		
		JLabel label = new JLabel("AgentPlatform Description");
		
		c.weightx = 0.5;
	  c.ipadx = 15;
	  c.ipady = 20;
    c.gridx = 0;
		c.gridy = 0;
    c.gridwidth = 2;
	  gridBag.setConstraints(label,c);
		add(label);
		
	  label = new JLabel("Platform Name: ");
	  c.ipady = 0;
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 1;
		gridBag.setConstraints(label,c);
		add(label);
		
		label = new JLabel("Dynamic: ");
    c.gridx = 0;
		c.gridy = 2;
    gridBag.setConstraints(label,c);
    add(label);

    label = new JLabel("Mobility: ");
		c.gridx = 0;
		c.gridy = 3;
		gridBag.setConstraints(label,c);
		add(label);

		platformName_Field = new JTextField();
		platformName_Field.setEditable(false);
		platformName_Field.setBackground(java.awt.Color.white);
    c.ipadx = 30;
		c.gridx = 1;
		c.gridy = 1;
    gridBag.setConstraints(platformName_Field,c);
		add(platformName_Field);
		
   
		dynamic = new JCheckBox();
		dynamic.setEnabled(false);
		c.gridx = 1;
		c.gridy = 2;
    gridBag.setConstraints(dynamic,c);
    add(dynamic);
  
		mobility = new JCheckBox();
		mobility.setEnabled(false);
    c.gridx = 1;
		c.gridy = 3;
    gridBag.setConstraints(mobility,c);
    add(mobility);
   
    JPanel profilePanel = new JPanel();
    profilePanel.setLayout(new BoxLayout(profilePanel,BoxLayout.Y_AXIS));
    profilePanel.setBorder(BorderFactory.createTitledBorder("AP Transport Profile"));

    MTPs_List = new VisualAPTransportProfileList((new java.util.ArrayList()).iterator(), owner);
		c.gridx = 0;
		c.gridy = 4;
		c.gridwidth = 2;
		
    gridBag.setConstraints(profilePanel,c);
    
    MTPs_List.setEnabled(false);
    MTPs_List.setDimension(new Dimension(250,50));   
    profilePanel.add(MTPs_List);
  
    add(profilePanel);
	}
 
	/**
	To set the field with the valued of a given APDescription.
	*/
	public void setAPDescription(APDescription desc){
		
		try{
			platformName_Field.setText(desc.getName());
			dynamic.setSelected(desc.getDynamic().booleanValue());
		  mobility.setSelected(desc.getMobility().booleanValue());
		  
		  MTPs_List.resetContent(desc.getTransportProfile().getAllAvailableMtps());
		  
		}catch(Exception e){e.printStackTrace();}
	   
	}
	
	/**
	To show an APDescription in a JDialog
	*/
	
	public static void showAPDescriptionInDialog(APDescription desc, Frame parent,String title)
	{
		final JDialog tempDlg = new JDialog(parent, title, true);
  
		APDescriptionPanel AP_Panel = new APDescriptionPanel(tempDlg);
		AP_Panel.setAPDescription(desc);

		JButton okButton = new JButton("OK");
		JPanel buttonPanel = new JPanel();
		// Use default (FlowLayout) layout manager to dispose the OK button
		buttonPanel.add(okButton);

		tempDlg.getContentPane().setLayout(new BorderLayout());
		tempDlg.getContentPane().add("Center", AP_Panel);
		tempDlg.getContentPane().add("South", buttonPanel);

		okButton.addActionListener(new ActionListener()
									   {
											public void actionPerformed(ActionEvent e)
											{
												tempDlg.dispose();
											}
									   } );

		tempDlg.pack();
		tempDlg.setResizable(false);
		if (parent != null) {
		  int locx = parent.getX() + (parent.getWidth() - tempDlg.getWidth()) / 2;
		  if (locx < 0)
		    locx = 0;
		  int locy = parent.getY() + (parent.getHeight() - tempDlg.getHeight()) / 2;
		  if (locy < 0)
		    locy = 0;
		  tempDlg.setLocation(locx,locy);
		}
		tempDlg.show();
	}

}