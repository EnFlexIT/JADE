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

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import jade.core.*;
import jade.core.behaviours.*;

import jade.domain.MobilityOntology;
import jade.lang.Codec;
import jade.lang.sl.SL0Codec;

import jade.gui.GuiAgent;
import jade.gui.GuiEvent;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;




/**
This is an example of mobile agent. 
This class contains the two resources used by the agent behaviours: the counter and the 
flag cntEnabled. At the setup it adds two behaviours to serve the incoming messages and
to increment the counter. 
In particular, notice the usage of the two methods <code>beforeMove()</code> and
<code>afterMove()</code> to execute some application-specific tasks just before and just after
the agent migration takes effect.
@author Giovanni Caire - CSELT S.p.A
@version $Date$ $Revision$
*/
public class MobileAgent extends GuiAgent {
  int     cnt;   // this is the counter
  boolean cntEnabled;  // this flag indicates if counting is enabled
  protected MobileAgentGui gui;  // this is the gui
  Location nextSite;  // this variable holds the destination site

	public void setup() {
	  // register the SL0 content language
	  registerLanguage(SL0Codec.NAME, new SL0Codec());
	  // register the mobility ontology
	  registerOntology(MobilityOntology.NAME, MobilityOntology.instance());

	  // creates and shows the GUI
	  gui = new MobileAgentGui(this);
	  gui.showCorrect();

	  // get the list of available locations and show it in the GUI
	  addBehaviour(new GetAvailableLocationsBehaviour(this));

	  cnt = 0;
	  cntEnabled = true;

	  ///////////////////////
	  // Add agent behaviours
	  Behaviour b1 = new CounterBehaviour(this);
	  addBehaviour(b1);	
	  Behaviour b2 = new ExecutorBehaviour(this);
	  addBehaviour(b2);	
	}


	protected void beforeMove() 
	{
		gui.dispose();
		System.out.println(getLocalName()+" is now moving elsewhere.");
	}

	protected void afterMove() 
	{
		System.out.println(getLocalName()+" is just arrived to this location.");
		gui.addVisitedSite(nextSite);
		gui.showCorrect();

		// Register again SL0 content language and JADE mobility ontology,
		// since they don't migrate.
		registerLanguage(SL0Codec.NAME, new SL0Codec());
		registerOntology(MobilityOntology.NAME, MobilityOntology.instance());		
	}
	


	/////////////////////////////////
	// GUI HANDLING

	// MOBILE GUI EVENT 
	private class MobGuiEvent extends GuiEvent
	{
		public static final int MOVE = 1001;
		public Location destination;

		public MobGuiEvent(Object source, int type, Location dest)
		{
			super(source, type);
			this.destination = dest;
		}
	}
		
	// METHODS PROVIDED TO THE GUI TO POST EVENTS REQUIRING AGENT OPERATIONS 	
	public void postMoveEvent(Object source, Location dest)
	{
		MobGuiEvent ev = new MobGuiEvent(source, MobGuiEvent.MOVE, dest);
		postGuiEvent(ev);
	}
	
	// AGENT OPERATIONS FOLLOWING GUI EVENTS
	protected void onGuiEvent(GuiEvent ev)
	{
		switch(ev.getType()) 
		{
		case MobGuiEvent.EXIT:
			gui.dispose();
			gui = null;
			doDelete();
			break;
		case MobGuiEvent.MOVE:
			nextSite = (Location)(((MobGuiEvent)ev).destination);
			doMove(nextSite);
			break;
		}
	}

}


