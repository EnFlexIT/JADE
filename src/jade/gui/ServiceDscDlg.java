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

// Import required JADE classes
import jade.domain.FIPAAgentManagement.ServiceDescription;

/**
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

	ServiceDescription viewSD(ServiceDescription dsc, boolean editable)
	{
		final IntRetValue  ret = new IntRetValue();
		ret.setValue(0);
		
		setTitle("Service");
		
		JPanel main = new JPanel();
		main.setLayout(new BoxLayout(main,BoxLayout.Y_AXIS));
		
		JPanel p = new JPanel();
		JLabel l;
		
		//Name	
		p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
		l = new JLabel("Name");
		l.setPreferredSize(new Dimension(130,20));
		p.add(l);
		p.add(Box.createHorizontalGlue());
		JTextField txtName = new JTextField();
	
		txtName.setPreferredSize(new Dimension(200,20));
		p.add(txtName);
		main.add(p);
		main.add(Box.createRigidArea(new Dimension (0,3)));
		
    //Type
		p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
		l = new JLabel("Type");
		l.setPreferredSize(new Dimension(130,20));
    p.add(l);
		p.add(Box.createHorizontalGlue());
		JTextField txtType = new JTextField();
		
		txtType.setPreferredSize(new Dimension (200,20));
		p.add(txtType);
		main.add(p);
		main.add(Box.createRigidArea(new Dimension (0,3)));

		//Ontology
		p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
 		l = new JLabel("Ontology");
 		l.setPreferredSize(new Dimension(130,20));
 		p.add(l);
		p.add(Box.createHorizontalGlue());
		JTextField txtOntology = new JTextField();
	
	  txtOntology.setPreferredSize(new Dimension (200,20));
	  p.add(txtOntology);
		main.add(p);
		main.add(Box.createRigidArea(new Dimension (0,3)));

    //Fixed Props
		p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
		l = new JLabel("Fixed Props.");	
		l.setPreferredSize(new Dimension(130,20));
		p.add(l);
		JTextField txtFixedProps = new JTextField();
	
		txtFixedProps.setPreferredSize(new Dimension (200,20));
	  p.add(txtFixedProps);
		main.add(p);
		main.add(Box.createRigidArea(new Dimension (0,3)));

		//Negotiable Props.
		p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
		l = new JLabel("Negotiable Props.");	
		l.setPreferredSize(new Dimension(130,20));
		p.add(l);
		JTextField txtNegProps = new JTextField();
	
	  txtNegProps.setPreferredSize(new Dimension (200,20));
	  p.add(txtNegProps);
		main.add(p);
		main.add(Box.createRigidArea(new Dimension (0,3)));


	  //Communication Props.
		p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
		l = new JLabel("Communication Props.");	
		l.setPreferredSize(new Dimension(130,20));
	  p.add(l);
		JTextField txtCommProps = new JTextField();
	
		txtCommProps.setPreferredSize(new Dimension (200,20));
		p.add(txtCommProps);
		main.add(p);
		main.add(Box.createRigidArea(new Dimension (0,3)));


		if (dsc != null) 
		{
			txtName.setText(dsc.getName());
			/*
			txtType.setText(dsc.getType());
			txtOntology.setText(dsc.getOntology());
			txtFixedProps.setText(dsc.getFixedProps());
			txtNegProps.setText(dsc.getNegotiableProps());
			txtCommProps.setText(dsc.getCommunicationProps());
			*/
		}
		
		//getContentPane().add(p, BorderLayout.CENTER);
		getContentPane().add(main, BorderLayout.NORTH);

		p = new JPanel();
		
		if (editable == false) //not editable
		{	
			txtName.setEditable(false);
      txtType.setEditable(false);
      txtOntology.setEditable(false);
      txtFixedProps.setEditable(false);
      txtNegProps.setEditable(false);
      txtCommProps.setEditable(false);
      p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
      JButton bOK = new JButton("OK");
      p.add(Box.createRigidArea(new Dimension(0,25)));
      bOK.setAlignmentX(Component.CENTER_ALIGNMENT);
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
		
		}
		else // editable
	  {
	  	p.setLayout(new FlowLayout(FlowLayout.CENTER));
	  	JButton bOK = new JButton("OK");
	  	JButton bCancel = new JButton("Cancel");
	  	bOK.setPreferredSize(bCancel.getPreferredSize());
	  	p.add(bOK);
	  	p.add(bCancel);
	  	p.add(Box.createRigidArea(new Dimension(0,60)));
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
	  }
		setModal(true);
		setResizable(false);
		setLocation(100, 100);
		pack();
		show();
	 
		if (ret.getValue() == 1)
		{	
			ServiceDescription editedDsc = new ServiceDescription(); 	  
		 			
	  	editedDsc.setName(getSaveText(txtName));
		/*
			editedDsc.setType(getSaveText(txtType));
			editedDsc.setOntology(getSaveText(txtOntology));
			editedDsc.setFixedProps(getSaveText(txtFixedProps));
			editedDsc.setNegotiableProps(getSaveText(txtNegProps));
			editedDsc.setCommunicationProps(getSaveText(txtCommProps)); 
		*/
			return(editedDsc);
 		}
		return(null);		
			
	}

	
	
	/*
	Return the string relative to service description fields if not empty, null otherwise
	*/
	private String getSaveText(JTextField field){
	try{
		String out = field.getText();
		return (out.length() == 0 ? null : out);
	}catch( Exception e){
	return null;
	}
	
	}

}
