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

import java.util.Vector;
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
  public boolean cntEnabled;  // this flag indicates if counting is enabled
  transient protected MobileAgentGui gui;  // this is the gui
  Location nextSite;  // this variable holds the destination site

  // These constants are used by the Gui to post Events to the Agent
  public static final int MOVE_EVENT = 1001;
  public static final int STOP_EVENT = 1002;
  public static final int CONTINUE_EVENT = 1003;
  public static final int REFRESH_EVENT = 1004;

  Vector visitedLocations = new Vector();;


	public void setup() {
	  // register the SL0 content language
	  registerLanguage(SL0Codec.NAME, new SL0Codec());
	  // register the mobility ontology
	  registerOntology(MobilityOntology.NAME, MobilityOntology.instance());

	  // creates and shows the GUI
	  gui = new MobileAgentGui(this);
	  gui.setVisible(true); 

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

   void stopCounter(){
    cntEnabled = false;
   }
   void continueCounter(){
     cntEnabled = true;
   }
   void displayCounter(){
     gui.displayCounter(cnt);
   }
  
   
	protected void beforeMove() 
	{
		gui.dispose();
		gui.setVisible(false);
		System.out.println(getLocalName()+" is now moving elsewhere.");
	}

	protected void afterMove() 
	{
		System.out.println(getLocalName()+" is just arrived to this location.");
		// creates and shows the GUI
		gui = new MobileAgentGui(this);

		visitedLocations.addElement(nextSite);
		for (int i=0; i<visitedLocations.size(); i++)
			gui.addVisitedSite((Location)visitedLocations.elementAt(i));
		gui.setVisible(true); 	
			
		// Register again SL0 content language and JADE mobility ontology,
		// since they don't migrate.
		registerLanguage(SL0Codec.NAME, new SL0Codec());
		registerOntology(MobilityOntology.NAME, MobilityOntology.instance());		

		addBehaviour(new GetAvailableLocationsBehaviour(this));
	}
	


	/////////////////////////////////
	// GUI HANDLING

	// MOBILE GUI EVENT 
	private class MobGuiEvent extends GuiEvent
	{
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
		MobGuiEvent ev = new MobGuiEvent(source, MOVE_EVENT, dest);
		postGuiEvent(ev);
	}

        public void postSimpleEvent(int eventType) {
	  MobGuiEvent ev = new MobGuiEvent(null, eventType, null);
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
		case MOVE_EVENT:
			nextSite = (Location)(((MobGuiEvent)ev).destination);
			doMove(nextSite);
			break;
   	        case STOP_EVENT:
		  stopCounter();
		  break;
		case CONTINUE_EVENT:
		  continueCounter();
		  break;
		case REFRESH_EVENT:
		  addBehaviour(new GetAvailableLocationsBehaviour(this));
		  break;
		}

	}

}


