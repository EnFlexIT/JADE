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
import javax.swing.border.*;

import java.awt.event.*;
import java.awt.*;
import java.util.List;

import jade.domain.FIPAAgentManagement.SearchConstraints;

/*
@author Tiziana Trucco - CSELT S.p.A.
@version $Date$ $Revision$
*/

/**
* This class implements the gui to insert the constraints for the search operation.
*/
class ConstraintDlg extends JDialog
{
	//CONSTRUCTORS
	ConstraintDlg(Frame parent)
	{
		super(parent);
	}
	
	ConstraintDlg(Dialog parent)
	{
		super(parent);
	}
	
	class SingleConstraint extends JPanel
	{
	  SingleConstraint()
		{
			super();
		
		}
		
		JPanel newSingleConstraint(SearchConstraints c)
		{
			Border etched = BorderFactory.createEtchedBorder();
			JPanel tempPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			/*
			String[] sel = {"df-depth","resp-req"};
			JComboBox tempCombo = new JComboBox(sel);
			tempCombo.setBorder(etched);
			tempPanel.add(tempCombo);
			String[] sel2 ={"MIN", "MAX", "EXACTLY"}; 
			JComboBox tempCombo2 = new JComboBox(sel2);
			tempCombo2.setBorder(etched);
			tempPanel.add(tempCombo2);
			JTextField val = new JTextField("1");
			val.setBorder(etched);
			val.setPreferredSize(new Dimension(40,26));
			val.setMinimumSize(new Dimension(40,26));
			val.setMaximumSize(new Dimension(40,26));
			
			if (c != null)
			{
				//Initialization of elements
				if (c.getName() == AgentManagementOntology.Constraint.DFDEPTH)
					tempCombo.setSelectedIndex(0);
				else
					tempCombo.setSelectedIndex(1);
				if(c.getFn() == AgentManagementOntology.Constraint.MIN)
					tempCombo2.setSelectedIndex(0);
				else 
				if(c.getFn() == AgentManagementOntology.Constraint.MAX)
				  tempCombo2.setSelectedIndex(1);
				else
				  tempCombo2.setSelectedIndex(2);
				  
				val.setText(Integer.toString(c.getArg()));
				
			}
			
			tempPanel.add(val);
			*/
			return tempPanel;
		}
	}
	
	List viewConstraint(List initialValue)
	{
		final IntRetValue ret = new IntRetValue();
		int counter = 0;
		
		List constraint = null;
		/*
		Border bevelBorder = BorderFactory.createRaisedBevelBorder();
		
		ret.setValue(0);
		
		setTitle("Search Constraints");
		
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		
		JLabel text = new JLabel("Search with the following constraints: ");
		text.setAlignmentX(Component.CENTER_ALIGNMENT);
		mainPanel.add(text);
		mainPanel.add(Box.createRigidArea(new Dimension(10,10)));
		
		 
		final JPanel constraintPanel = new JPanel();
		constraintPanel.setLayout(new BoxLayout(constraintPanel, BoxLayout.Y_AXIS));
    
		if(initialValue != null)
		{
			//add new constraintpanel according to the constraint already inserted 
			Enumeration e = initialValue.elements();
			while(e.hasMoreElements())
			{
				SearchConstraints c = (SearchConstraints) e.nextElement();
				SingleConstraint single = new SingleConstraint();
				JPanel singleConstraintPanel = single.newSingleConstraint(c);
				constraintPanel.add(singleConstraintPanel);
				counter++;
		}
		}
		else
		constraintPanel.add(new SingleConstraint().newSingleConstraint(null));
		
		JPanel buttonConstraintPanel = new JPanel();
		buttonConstraintPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		
		JButton moreButton = new JButton("More");
		moreButton.setBorder(bevelBorder);
		
		final JButton resetButton = new JButton("Reset");
		resetButton.setBorder(bevelBorder);
		
		moreButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				String param = (String) e.getActionCommand();
				if (param.equals("More"))
				{
						SingleConstraint panel = new SingleConstraint();
						JPanel tempPanel = panel.newSingleConstraint(null);
						constraintPanel.add(tempPanel);
						resetButton.setEnabled(true);
						pack();
						show();
						
				}
			}
		}	);
		
		resetButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				String param = (String)e.getActionCommand();
				if (param.equals("Reset"))
				{
					int component = constraintPanel.getComponentCount();
				  if (component != 1)
						constraintPanel.remove(--component);
				
					if (component == 1)
						resetButton.setEnabled(false);
					pack();
					show();
						
				}
			}
		}
			);
		
		if(counter >1)
			resetButton.setEnabled(true);
		else
		  resetButton.setEnabled(false); 
		
		buttonConstraintPanel.add(moreButton);
		buttonConstraintPanel.add(resetButton);
		
	
		mainPanel.add(constraintPanel);
		mainPanel.add(buttonConstraintPanel);
		
		JPanel dialogButtonPanel = new JPanel(new FlowLayout());
		JButton okButton = new JButton("OK");
		
		okButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				String param = (String)e.getActionCommand();
				if (param.equals("OK"))
				{
					ret.setValue(1);
					dispose();
				}
			}	
		});
		
		dialogButtonPanel.add(okButton);
		
		JButton cancelButton = new JButton("Cancel");
		
		cancelButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				String param = (String)e.getActionCommand();
				if (param.equals("Cancel"))
				{
					ret.setValue(0);
					dispose();
				}
			}
		});
		
		dialogButtonPanel.add(cancelButton);
		
		mainPanel.add(dialogButtonPanel);
		
		getContentPane().add(mainPanel, BorderLayout.SOUTH);
		setModal(true);
		setResizable(false);
		setLocation(100,100);
		
		pack();
		show();
	
		if (ret.getValue() == 1)
			{
				String st = null;
				Integer value;
				constraint = new Vector();
			  int size = constraintPanel.getComponentCount();
				for(int i = 0; i<size; i++)
				{
					JPanel p = (JPanel)constraintPanel.getComponent(i);
					SearchConstraints c = new SearchConstraints();
					JComboBox temp = (JComboBox)p.getComponent(0);
					JComboBox minMax = (JComboBox)p.getComponent(1);
					JTextField tx = (JTextField)p.getComponent(2);
					int comboSel = temp.getSelectedIndex();
					if (comboSel == 0)
						c.setName(AgentManagementOntology.Constraint.DFDEPTH);
				  else
						c.setName(AgentManagementOntology.Constraint.RESPREQ);
					
					comboSel = minMax.getSelectedIndex();
					if (comboSel == 0)
						c.setFn(AgentManagementOntology.Constraint.MIN);
					else
					if(comboSel == 1)
						c.setFn(AgentManagementOntology.Constraint.MAX);
					else
					c.setFn(AgentManagementOntology.Constraint.EXACTLY);
					
					try{
						st = tx.getText();
						value = Integer.valueOf(st);
						
						//Not possible search with value < 1.
						//FIXME:give a warning to the user.
						if (value.intValue() < 1)
							value = new Integer(1);
					}catch(Exception ex)
						{
						value = new Integer(1);
						}
					c.setArg(value.intValue());
					constraint.add(c);
				}
				
			}
			else 
			constraint = null;
		*/			
		return constraint;
	}

}
