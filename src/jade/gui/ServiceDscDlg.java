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

// Import required Jade classes
import jade.domain.AgentManagementOntology;

/**
Javadoc documentation for the file
@author Giovanni Caire - Adriana Quinto- CSELT S.p.A.
@version $Date$ $Revision$
*/

class ServiceDscDlg extends JDialog 
{
	JTextField txtString;
	JTextField cpString;
	JTextField fpString;
	JTextField nameString;
	JTextField npString;
	JTextField ontologyString;
	JTextField typeString;
	
	// CONSTRUCTORS
	ServiceDscDlg(Frame parent) 
	{
		super(parent);
	}

	ServiceDscDlg(Dialog parent) 
	{
		super(parent);
	}

	void viewSD(AgentManagementOntology.ServiceDescriptor dsc)
	{
		setTitle("Service");
		
		JPanel p = new JPanel();
		LayoutFacilitator lf = new LayoutFacilitator(p);
		lf.formatGrid(6,  // 6 rows
		              2,  // 2 columns
		              5,  // 5 pixels as left, right, top and bottom border
		              5,
		              5,
		              5,
		              2,  // 2 pixels betweens rows and columns 
		              2); 
		lf.setGridColumnWidth(0, 150);
		lf.setGridColumnWidth(1, 200);

		JLabel l;

		l = new JLabel("Name");
		lf.put(l, 0, 0, 1, 1, false);
		JTextField txtName = new JTextField();
		txtName.setEditable(false);
		lf.put(txtName, 1, 0, 1, 1, false);

		l = new JLabel("Type");
		lf.put(l, 0, 1, 1, 1, false);
		JTextField txtType = new JTextField();
		txtType.setEditable(false);
		lf.put(txtType, 1, 1, 1, 1, false);

		l = new JLabel("Ontology");
		lf.put(l, 0, 2, 1, 1, false);
		JTextField txtOntology = new JTextField();
		txtOntology.setEditable(false);
		lf.put(txtOntology, 1, 2, 1, 1, false);

		l = new JLabel("Fixed Props.");
		lf.put(l, 0, 3, 1, 1, false);
		JTextField txtFixedProps = new JTextField();
		txtFixedProps.setEditable(false);
		lf.put(txtFixedProps, 1, 3, 1, 1, false);

		l = new JLabel("Negotiable Props.");
		lf.put(l, 0, 4, 1, 1, false);
		JTextField txtNegProps = new JTextField();
		txtNegProps.setEditable(false);
		lf.put(txtNegProps, 1, 4, 1, 1, false);

		l = new JLabel("Communication Props.");
		lf.put(l, 0, 5, 1, 1, false);
		JTextField txtCommProps = new JTextField();
		txtCommProps.setEditable(false);
		lf.put(txtCommProps, 1, 5, 1, 1, false);

		if (dsc != null) 
		{
			txtName.setText(dsc.getName());
			txtType.setText(dsc.getType());
			txtOntology.setText(dsc.getOntology());
			txtFixedProps.setText(dsc.getFixedProps());
			txtNegProps.setText(dsc.getNegotiableProps());
			txtCommProps.setText(dsc.getCommunicationProps());
		}
		
		getContentPane().add(p, BorderLayout.CENTER);

		p = new JPanel();
		JButton bOK = new JButton("OK");
		p.add(bOK);
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
		getContentPane().add(p, BorderLayout.SOUTH);

		setModal(true);
		setResizable(false);
		setLocation(100, 100);
		pack();
		show();
	}

	AgentManagementOntology.ServiceDescriptor  editSD(AgentManagementOntology.ServiceDescriptor dsc)
	{
		final IntRetValue ret = new IntRetValue();
		ret.setValue(0);

		setTitle("Service");
		
		JPanel p = new JPanel();
		LayoutFacilitator lf = new LayoutFacilitator(p);
		lf.formatGrid(6,  // 6 rows
		              2,  // 2 columns
		              5,  // 5 pixels as left, right, top and bottom border
		              5,
		              5,
		              5,
		              2,  // 2 pixels betweens rows and columns 
		              2); 
		lf.setGridColumnWidth(0, 150);
		lf.setGridColumnWidth(1, 200);

		JLabel l;

		l = new JLabel("Name");
		lf.put(l, 0, 0, 1, 1, false);
		JTextField txtName = new JTextField();
		lf.put(txtName, 1, 0, 1, 1, false);

		l = new JLabel("Type");
		lf.put(l, 0, 1, 1, 1, false);
		JTextField txtType = new JTextField();
		lf.put(txtType, 1, 1, 1, 1, false);

		l = new JLabel("Ontology");
		lf.put(l, 0, 2, 1, 1, false);
		JTextField txtOntology = new JTextField();
		lf.put(txtOntology, 1, 2, 1, 1, false);

		l = new JLabel("Fixed Props.");
		lf.put(l, 0, 3, 1, 1, false);
		JTextField txtFixedProps = new JTextField();
		lf.put(txtFixedProps, 1, 3, 1, 1, false);

		l = new JLabel("Negotiable Props.");
		lf.put(l, 0, 4, 1, 1, false);
		JTextField txtNegProps = new JTextField();
		lf.put(txtNegProps, 1, 4, 1, 1, false);

		l = new JLabel("Communication Props.");
		lf.put(l, 0, 5, 1, 1, false);
		JTextField txtCommProps = new JTextField();
		lf.put(txtCommProps, 1, 5, 1, 1, false);

		if (dsc != null) 
		{
			txtName.setText(dsc.getName());
			txtType.setText(dsc.getType());
			txtOntology.setText(dsc.getOntology());
			txtFixedProps.setText(dsc.getFixedProps());
			txtNegProps.setText(dsc.getNegotiableProps());
			txtCommProps.setText(dsc.getCommunicationProps());
		}
		
		getContentPane().add(p, BorderLayout.CENTER);

		p = new JPanel();
		JButton bOK = new JButton("OK");
		JButton bCancel = new JButton("Cancel");
		bOK.setPreferredSize(bCancel.getPreferredSize());
		p.add(bOK);
		p.add(bCancel);
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
		getContentPane().add(p, BorderLayout.SOUTH);

		setModal(true);
		setResizable(false);
		setLocation(100, 100);
		pack();
		show();

		if (ret.getValue() == 1)
		{	
			AgentManagementOntology.ServiceDescriptor editedDsc = new AgentManagementOntology.ServiceDescriptor(); 
			editedDsc.setName(txtName.getText());
			editedDsc.setType(txtType.getText());
			editedDsc.setOntology(txtOntology.getText());
			editedDsc.setFixedProps(txtFixedProps.getText());
			editedDsc.setNegotiableProps(txtNegProps.getText());
			editedDsc.setCommunicationProps(txtCommProps.getText());
			return(editedDsc);
 		}
		return(null);		
	}

}
