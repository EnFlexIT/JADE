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

// Import required java classes
import java.util.Vector;

// Import required Jade classes
import jade.core.*;
import jade.core.behaviours.*;

/**
@author Giovanni Caire - CSELT S.p.A.
@version $Date$ $Revision$
*/

public class GuiAgent extends Agent
{
	private Vector guiEventQueue;
	private transient Object guiEventQueueLock;

	////////////////////////
	// GUI HANDLER BEHAVIOUR
	private class GuiHandlerBehaviour extends SimpleBehaviour
	{
		protected GuiHandlerBehaviour()
		{
			super(GuiAgent.this);
		}

		public void action()
		{
			if (!guiEventQueue.isEmpty())
			{
				GuiEvent ev = null;  				
				synchronized(guiEventQueueLock)
				{
					try
					{
						ev  = (GuiEvent) guiEventQueue.remove(0);
					}
					catch (ArrayIndexOutOfBoundsException ex)
					{
						ex.printStackTrace(); // Should never happen
					}
				}			
				onGuiEvent(ev);
			}
			else
				block();
		}

		public boolean done()
		{
			return(false);
		}
	}

	//////////////
	// CONSTRUCTOR
	public GuiAgent()
	{
		super();
		guiEventQueue = new Vector();
		guiEventQueueLock = new Object();

		// Add the GUI handler behaviour
		Behaviour b = new GuiHandlerBehaviour();
		addBehaviour(b);
	}

	///////////////////////////////////////////////////////////////
	// PROTECTED METHODS TO POST AND GET A GUI EVENT FROM THE QUEUE
	protected void postGuiEvent(GuiEvent e)
	{
		synchronized(guiEventQueueLock)
		{
			guiEventQueue.add( (Object) e );
			doWake();
		}
	}

	/////////////////////////////////////////////////////////////////////////
	// METHODS TO POST PREDEFINED EXIT AND CLOSEGUI EVENTS IN GUI EVENT QUEUE
	public void postExitEvent(Object g)
	{
		GuiEvent e = new GuiEvent(g, GuiEvent.EXIT);
		postGuiEvent(e);
	}

	public void postCloseGuiEvent(Object g)
	{
		GuiEvent e = new GuiEvent(g, GuiEvent.CLOSEGUI);
		postGuiEvent(e);
	}

	///////////////////////////////
	// METHOD TO HANDLE GUI EVENTS
	protected void onGuiEvent(GuiEvent ev)
	{
	}		
}
