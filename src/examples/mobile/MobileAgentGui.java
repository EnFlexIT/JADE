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



package examples.mobile;


// Import required Java classes 
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

import java.util.*;
import java.io.*;

import jade.core.*;
import jade.domain.MobilityOntology;
import jade.gui.LocationTableModel;

public class MobileAgentGui extends JFrame implements ActionListener
{
	public MobileAgent          myAgent;
	public LocationTableModel visitedSiteListModel;
	public JTable            visitedSiteList;
	public LocationTableModel availableSiteListModel;
	public JTable            availableSiteList;
	//public JButton          exitB;
	//public JButton          moveB;
	public JTextField       nextDstContainerTxt;
	
	// Constructor
	MobileAgentGui(MobileAgent a)
	{
		super();
		myAgent = a;
		setTitle("GUI of "+a.getLocalName());
		setSize(505,405);

		////////////////////////////////
		// Set GUI window layout manager
	
		JPanel main = new JPanel();
		main.setLayout(new BoxLayout(main,BoxLayout.Y_AXIS));

		JPanel counterPanel = new JPanel();
		counterPanel.setLayout(new BoxLayout(counterPanel, BoxLayout.X_AXIS));
		
		JButton pauseButton = new JButton("STOP COUNTER");
		JButton continueButton = new JButton("CONTINUE COUNTER");
		JLabel counterLabel = new JLabel("Counter value: ");
		JTextField counterText = new JTextField();
		counterPanel.add(pauseButton);
		counterPanel.add(continueButton);
		counterPanel.add(counterLabel);
		counterPanel.add(counterText);
		
		main.add(counterPanel);
		
	   ///////////////////////////////////////////////////
		// Add the list of available sites to the NORTH part 
		availableSiteListModel = new LocationTableModel();
		availableSiteList = new JTable(availableSiteListModel);
		availableSiteList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		JPanel availablePanel = new JPanel();
		availablePanel.setLayout(new BorderLayout());

		JScrollPane avPane = new JScrollPane();
		avPane.getViewport().setView(availableSiteList);
		availablePanel.add(avPane, BorderLayout.CENTER);
		availablePanel.setBorder(BorderFactory.createTitledBorder("Available Locations"));
	  availableSiteList.setRowHeight(20);

		main.add(availablePanel);
		
		TableColumn c;
		c = availableSiteList.getColumn((Object) availableSiteList.getColumnName(0));
		c.setHeaderValue((Object) (new String("ID")));
		c = availableSiteList.getColumn((Object) availableSiteList.getColumnName(1));
		c.setHeaderValue((Object) (new String("Name")));
		c = availableSiteList.getColumn((Object) availableSiteList.getColumnName(2));
		c.setHeaderValue((Object) (new String("Protocol")));
		c = availableSiteList.getColumn((Object) availableSiteList.getColumnName(3));
		c.setHeaderValue((Object) (new String("Address")));

		///////////////////////////////////////////////////
		// Add the list of visited sites to the CENTER part 
		JPanel visitedPanel = new JPanel();
		visitedPanel.setLayout(new BorderLayout());
		visitedSiteListModel = new LocationTableModel();
		visitedSiteList = new JTable(visitedSiteListModel);
		JScrollPane pane = new JScrollPane();
		pane.getViewport().setView(visitedSiteList);
	  visitedPanel.add(pane,BorderLayout.CENTER);
		visitedPanel.setBorder(BorderFactory.createTitledBorder("Visited Locations"));
	  visitedSiteList.setRowHeight(20);

		main.add(visitedPanel);

			// Column names
	
		c = visitedSiteList.getColumn((Object) visitedSiteList.getColumnName(0));
		c.setHeaderValue((Object) (new String("ID")));
		c = visitedSiteList.getColumn((Object) visitedSiteList.getColumnName(1));
		c.setHeaderValue((Object) (new String("Name")));
		c = visitedSiteList.getColumn((Object) visitedSiteList.getColumnName(2));
		c.setHeaderValue((Object) (new String("Protocol")));
		c = visitedSiteList.getColumn((Object) visitedSiteList.getColumnName(3));
		c.setHeaderValue((Object) (new String("Address")));

	
		/////////////////////////////////////////////////////////////////////
		// Add the control buttons to the SOUTH part 
		// Move button
		JPanel p = new JPanel();
		JButton b = new JButton("Move");
		b.addActionListener(this);
		p.add(b);
		// Exit button
		b = new JButton("Exit");
		b.addActionListener(this);
		p.add(b);
		main.add(p);
		
		getContentPane().add(main, BorderLayout.CENTER);
	}

  public void updateLocations(MobilityOntology.Location[] list) {
				  availableSiteListModel.clear();

        	for (int i=0; i<list.length; i++)
				  	availableSiteListModel.add(list[i]);
				  	
				  availableSiteListModel.fireTableDataChanged();
	
        }

	public void actionPerformed(ActionEvent e)
	{
		String command = e.getActionCommand();

		// MOVE
		if      (command.equals("Move"))
		{
		        //String dest = nextDstContainerTxt.getText();
			//FIXME null deve essere una buona dest
			Location dest;
			myAgent.postMoveEvent((Object) this, null);
		}
		// EXIT
		else if (command.equals("Exit"))
		{
			myAgent.postExitEvent((Object) this);
		}
	}
	
	void showCorrect()
	{
		///////////////////////////////////////////
		// Arrange and display GUI window correctly
		pack();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int centerX = (int)screenSize.getWidth() / 2;
		int centerY = (int)screenSize.getHeight() / 2;
		setLocation(centerX - getWidth() / 2, centerY - getHeight() / 2);
		show();
	}
	
	public void addVisitedSite(Location site)
	{
		visitedSiteListModel.add(site);
		visitedSiteListModel.fireTableDataChanged();

	}
}
