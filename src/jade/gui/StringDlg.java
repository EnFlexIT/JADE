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
  String out;
  Component parentGUI;
	
  // CONSTRUCTORS
	/*StringDlg(Frame parent, String hint) 
	{
		super(parent);
		this.hint = new String(hint);
		this.out = null;
	}

	StringDlg(Dialog parent, String hint) 
	{
		super(parent);
		this.hint = new String(hint);
		this.out = null;
	}*/
	
	StringDlg(Component parent,String hint) 
	{
		super();
		parentGUI = parent;
		this.hint = new String(hint);
		this.out = null;
	}
	
	String editString(String value)
	{
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
									String insertedValue = (txtString.getText()).trim();
                  if(insertedValue.length() == 0)
                  		{
                  			JOptionPane.showMessageDialog(null,"Must have non-empty fields !","Error Message",JOptionPane.ERROR_MESSAGE);
									      return;
                  		}
									else 
									{
										out = insertedValue;
									  dispose();
									}
								}
							} 
		                           } );
		bCancel.addActionListener( new ActionListener()
		                           {
						   	public void actionPerformed(ActionEvent e)
							{    
								String param = (String) e.getActionCommand();
								if (param.equals("Cancel"))
									dispose();
								
							} 
		                           } );
		getContentPane().add(p, BorderLayout.SOUTH);

		setModal(true);
		setResizable(false);
	
		ShowCorrect();

	
		return out;
	}
	
	void viewString(String value)
	{
		
		setTitle("View");

		JPanel p = new JPanel();
		//p.setLayout(new GridLayout(2, 1));
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		JLabel l = new JLabel(hint);
		p.add(Box.createRigidArea(new Dimension(0,6)));
		p.add(l);
		txtString = new JTextField();
		txtString.setEditable(false);
		txtString.setText(value);
		txtString.setPreferredSize(new Dimension(300, txtString.getPreferredSize().height));
		p.add(txtString);
		p.add(Box.createRigidArea(new Dimension(0,15)));
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
	
		ShowCorrect();
	
	}

	private void ShowCorrect() 
 	 {
    pack();
    
    setLocation(parentGUI.getX() + (parentGUI.getWidth() - getWidth()) / 2, parentGUI.getY() + (parentGUI.getHeight() - getHeight()) / 2);
    setVisible(true);
    toFront();
 	 }

}