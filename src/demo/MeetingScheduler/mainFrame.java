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


package demo.MeetingScheduler;

import java.awt.*;

import symantec.itools.awt.util.Calendar;

import java.util.*;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.IOException;

import jade.core.Agent;
import jade.core.AID;
import jade.domain.FIPAAgentManagement.*; 
import demo.MeetingScheduler.Ontology.*;

import jade.gui.GuiEvent;

/**
Javadoc documentation for the file
@author Fabio Bellifemine - CSELT S.p.A
@version $Date$ $Revision$
*/
public class mainFrame extends Frame
{



 MeetingSchedulerAgent myAgent;
 int currentAction;   // indicates the action currently being executed
 final static int VIEWKNOWNPERSONS = 0;
 final static int VIEWKNOWNDF = 1;

public mainFrame(MeetingSchedulerAgent a, String title) {
  this(title);
  myAgent = a;
}




public mainFrame() {
  //{{INIT_CONTROLS
  setLayout(null);
  setVisible(false);
  setSize(255,305);
  calendar1 = new symantec.itools.awt.util.Calendar();
  calendar1.setBounds(0,0,250,200);
  calendar1.setFont(new Font("Dialog", Font.BOLD, 10));
  add(calendar1);
  textArea1 = new java.awt.TextArea("",0,0,TextArea.SCROLLBARS_VERTICAL_ONLY);
  textArea1.setEditable(false);
  textArea1.setBounds(0,204,250,70);
  add(textArea1);
  textFieldErrMsg = new java.awt.TextField();
  textFieldErrMsg.setEditable(false);
  textFieldErrMsg.setVisible(false);
  textFieldErrMsg.setBounds(0,276,252,24);
  textFieldErrMsg.setFont(new Font("Dialog", Font.ITALIC, 10));
  textFieldErrMsg.setForeground(new Color(0));
  textFieldErrMsg.setBackground(new Color(16776960));
  add(textFieldErrMsg);
  labelInsertDF = new java.awt.Label("Insert agent address of the DF",Label.CENTER);
  labelInsertDF.setVisible(false);
  labelInsertDF.setBounds(0,0,250,40);
  labelInsertDF.setFont(new Font("Dialog", Font.BOLD, 12));
  add(labelInsertDF);
  textFieldDFaddress = new java.awt.TextField();
  textFieldDFaddress.setVisible(false);
  textFieldDFaddress.setBounds(0,100,250,20);
  add(textFieldDFaddress);
  listNames = new java.awt.List(4);
  listNames.setVisible(false);
  add(listNames);
  listNames.setBounds(0,0,250,200);
  setTitle("A Basic Application");
  
  //{{INIT_MENUS
  mainMenuBar = new java.awt.MenuBar();
  menu1 = new java.awt.Menu("Directory");
  miRegWithDF = new java.awt.MenuItem("Register with a Facilitator");
  menu1.add(miRegWithDF);
  miViewDF = new java.awt.MenuItem("View Known Facilitators");
  menu1.add(miViewDF);
  menuItem3 = new java.awt.MenuItem("View Known Persons");
  menu1.add(menuItem3);
  menuItem4 = new java.awt.MenuItem("Update Known Persons with the Facilitators");
  menu1.add(menuItem4);
  mainMenuBar.add(menu1);
  appMenu = new java.awt.Menu("Appointment");
  menuItem5 = new java.awt.MenuItem("Show");
  appMenu.add(menuItem5);
  menuItem2 = new java.awt.MenuItem("Fix");
  appMenu.add(menuItem2);
  menuItem1 = new java.awt.MenuItem("Cancel");
  appMenu.add(menuItem1);
  mainMenuBar.add(appMenu);
  setMenuBar(mainMenuBar);
		
  //{{REGISTER_LISTENERS
  SymWindow aSymWindow = new SymWindow();
  this.addWindowListener(aSymWindow);
  SymAction lSymAction = new SymAction();
  miViewDF.addActionListener(lSymAction);
  miRegWithDF.addActionListener(lSymAction);
  calendar1.addActionListener(lSymAction);
  menuItem1.addActionListener(lSymAction);
  menuItem2.addActionListener(lSymAction);
  textFieldDFaddress.addActionListener(lSymAction);
  menuItem4.addActionListener(lSymAction);
  menuItem3.addActionListener(lSymAction);
  SymItem lSymItem = new SymItem();
  listNames.addItemListener(lSymItem);
  menuItem5.addActionListener(lSymAction);
  setLocation(50, 50);
}
	
public mainFrame(String title) {
  this();
  setTitle(title);
}
	
	public void addNotify()
	{
		// Record the size of the window prior to calling parents addNotify.
		Dimension d = getSize();
		
		super.addNotify();
	
		if (fComponentsAdjusted)
			return;
	
		// Adjust components according to the insets
		setSize(insets().left + insets().right + d.width, insets().top + insets().bottom + d.height);
		Component components[] = getComponents();
		for (int i = 0; i < components.length; i++)
		{
			Point p = components[i].getLocation();
			p.translate(insets().left, insets().top);
			components[i].setLocation(p);
		}
		fComponentsAdjusted = true;
	}
	
	// Used for addNotify check.
	boolean fComponentsAdjusted = false;

  //{{DECLARE_CONTROLS
  symantec.itools.awt.util.Calendar calendar1;
  java.awt.TextArea textArea1;
  java.awt.TextField textFieldErrMsg;
  java.awt.Label labelInsertDF;
  java.awt.TextField textFieldDFaddress;
  java.awt.List listNames;

  //{{DECLARE_MENUS
  java.awt.MenuBar mainMenuBar;
  java.awt.Menu menu1;
  java.awt.MenuItem miRegWithDF;
  java.awt.MenuItem miViewDF;
  java.awt.MenuItem menuItem3;
  java.awt.MenuItem menuItem4;
  java.awt.Menu appMenu;
  java.awt.MenuItem menuItem5;
  java.awt.MenuItem menuItem2;
  java.awt.MenuItem menuItem1;
	
  class SymWindow extends java.awt.event.WindowAdapter {
public void windowClosing(java.awt.event.WindowEvent event) {
  //clearFrame();
  Object object = event.getSource();
  if (object == mainFrame.this)
    Frame1_WindowClosing(event);
}
  }
	
  void Frame1_WindowClosing(java.awt.event.WindowEvent event)
  {
    setVisible(false);	// hide the Frame
    dispose();			// free the system resources
    myAgent.doDelete();
  }
	
  class SymAction implements java.awt.event.ActionListener
  {
public void actionPerformed(java.awt.event.ActionEvent event)
    {
      //clearFrame();
      Object object = event.getSource();
      if (object == miViewDF)
	miViewDF_Action(event);
      else if (object == miRegWithDF)
	miRegWithDF_Action(event);
      else if (object == calendar1)
	calendar1_Action(event);
      else if (object == menuItem1)
	menuItem1_ActionPerformed(event);
      else if (object == menuItem2)
	menuItem2_ActionPerformed(event);
      else if (object == menuItem4)
	menuItem4_ActionPerformed(event);
      else if (object == textFieldDFaddress)
	textFieldDFaddress_EnterHit(event);
      else if (object == menuItem3)
	menuItem3_ActionPerformed(event);
      else if (object == menuItem5)
	menuItem5_ActionPerformed(event);
    }
  }
	
	
	/** View Known DF */
	void miViewDF_Action(java.awt.event.ActionEvent event)
	{
	  clearFrame();
	  textArea1.setVisible(true);
	  listNames.setVisible(true);
	  listNames.clear();
	  for (Enumeration e = myAgent.getKnownDF(); e.hasMoreElements(); )
            listNames.addItem(((AID)e.nextElement()).getName());
	  currentAction = VIEWKNOWNDF;
	  listNames.select(0);
	  listNames_ItemStateChanged(null); 
	}
	
	void miExit_Action(java.awt.event.ActionEvent event)
	{
	  // Action from Exit Create and show as modal
	  //System.err.println("miExit_ACtion");
	}
	
	void miRegWithDF_Action(java.awt.event.ActionEvent event)
	{ // Register with a DF
	  clearFrame();
	  labelInsertDF.setVisible(true);
	  textFieldDFaddress.setVisible(true);	
	  textFieldDFaddress.requestFocus();
	}

    /** View Known Persons **/
	void menuItem3_ActionPerformed(java.awt.event.ActionEvent event)
	{
	  clearFrame();
	  textArea1.setVisible(true);
	  listNames.setVisible(true);
	  listNames.clear();
	  for (Enumeration e = myAgent.getKnownPersons(); e.hasMoreElements(); ) 
            listNames.addItem(((Person)e.nextElement()).getName());
	  currentAction=VIEWKNOWNPERSONS;
	  listNames.select(0);
	  listNames_ItemStateChanged(null); 
	}

	void calendar1_Action(java.awt.event.ActionEvent event)
	{
	  clearFrame();
	  textArea1.setVisible(true);
	  calendar1.setVisible(true);
	  textArea1.setText("");
	  Appointment a = myAgent.getMyAppointment((new Date(calendar1.getDate())));
	  if (a != null)
            textArea1.setText(a.getDescription());
	}


    /**
      * This method sets to not visible all the components of this frame
      * except the Menu Bar.
      */
    void clearFrame () {
      calendar1.setVisible(false);
      textArea1.setVisible(false);
      textFieldErrMsg.setVisible(false);
      labelInsertDF.setVisible(false);
      textFieldDFaddress.setVisible(false);
      listNames.setVisible(false);
    }
    
	void menuItem1_ActionPerformed(java.awt.event.ActionEvent event)
	{ // Remove an appointment
	  calendar1_Action(null);
	  GuiEvent ev = new GuiEvent(this,myAgent.CANCELAPPOINTMENT);
	  ev.addParameter(new Date(calendar1.getDate()));
	  myAgent.postGuiEvent(ev);
	  calendar1_Action(null);
	}

        /** Fix an appointment */
	void menuItem2_ActionPerformed(java.awt.event.ActionEvent event)
	{
	  calendar1_Action(null);
	  // Create and show the Frame
	  (new FixApp(myAgent,calendar1.getDate())).setVisible(true);
	}

	
	/** This method is called to update the list of known persons */
	void menuItem4_ActionPerformed(java.awt.event.ActionEvent event)
	{
	  AID dfName; 
	  Enumeration e = myAgent.getKnownDF();
	  clearFrame();
	  while (e.hasMoreElements()) {
            dfName=(AID)e.nextElement();
	    GuiEvent ev = new GuiEvent(this,myAgent.SEARCHWITHDF);
	    ev.addParameter(dfName);
	    myAgent.postGuiEvent(ev);	  
	  } 
	}

	
	
	void showErrorMessage(String text) {
	  textFieldErrMsg.setVisible(true);
	  textFieldErrMsg.setText(text);
	  System.err.println(text);
	}

	void textFieldDFaddress_EnterHit(java.awt.event.ActionEvent event)
	{
	  clearFrame();	 
	  GuiEvent ev = new GuiEvent(this,myAgent.REGISTERWITHDF);
	  ev.addParameter(textFieldDFaddress.getText());
	  myAgent.postGuiEvent(ev);	  
	}



	

	

	class SymItem implements java.awt.event.ItemListener
	{
		public void itemStateChanged(java.awt.event.ItemEvent event)
		{
			Object object = event.getSource();
			if (object == listNames)
				listNames_ItemStateChanged(event);
		}
	}

	void listNames_ItemStateChanged(java.awt.event.ItemEvent event)
	{
	  String cur = listNames.getSelectedItem();
	  if (currentAction == VIEWKNOWNPERSONS)
	    textArea1.setText(myAgent.getPerson(cur).toString());
	  else if (currentAction == VIEWKNOWNDF)
	    textArea1.setText(cur); 
	}

	void menuItem5_ActionPerformed(java.awt.event.ActionEvent event)
	{
		calendar1_Action(null);
	}
}

