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

import jade.lang.acl.ACLMessage;

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
public class MobileAgent extends Agent 
{
	int     cnt;
	boolean cntEnabled;

	public void setup() 
	{
		System.out.println("Hallo! Now I'm here");
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
		System.out.println("Bye!");
	}

	protected void afterMove() 
	{
		System.out.println("Hallo! Now I'm here");
	}
	
}
