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

/**
@author Giovanni Caire - CSELT S.p.A.
@version $Date$ $Revision$
*/

class StringDlg extends JDialog 
{
	String     hint;
	JTextField txtString;

	// CONSTRUCTORS
	StringDlg(Frame parent, String hint) 
	{
		super(parent);
		this.hint = new String(hint);
	}

	StringDlg(Dialog parent, String hint) 
	{
		super(parent);
		this.hint = new String(hint);
	}

	String editString(String value)
	{
		final IntRetValue ret = new IntRetValue();
		ret.setValue(0);

		setTitle("Edit");

		JPanel p = new JPanel();
		//p.setLayout(new GridLayout(2, 1));
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		JLabel l = new JLabel(hint);
		p.add(Box.createRigidArea(new Dimension(0,6)));
		p.add(l);
		txtString = new JTextField();
		txtString.setText(value);
		txtString.setPreferredSize(new Dimension(300, txtString.getPreferredSize().height));
		p.add(txtString);
		p.add(Box.createRigidArea(new Dimension(0,15)));
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
			String out = txtString.getText();
			if (out.length() == 0)
				out = null;
			return(out);
 		}
		return(null);		
	}

}
