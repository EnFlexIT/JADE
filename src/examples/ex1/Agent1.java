/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop multi-agent systems in compliance with the FIPA specifications.
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

package examples.ex1;

import jade.core.*;
import jade.core.behaviours.*;

/**
Javadoc documentation for the file
@author Giovanni Rimassa - Università di Parma
@version  $Date$ $Revision$
*/

// Simple example of an agent.
public class Agent1 extends Agent {


  class Behaviour1 extends CyclicBehaviour {

    private int counter;
    private String myID;

    public Behaviour1(String ID) {
      counter = 1;
      myID = ID;
    }

    public void action() {

      System.out.println("I'm " + myID + " :");
      System.out.println("Running " + counter + " times. ");
      ++counter;
      try {
	Thread.sleep(1000);
      }
      catch(InterruptedException ie) {
	// Do nothing ...
      }
    }


  }


  protected void setup() {

    addBehaviour(new Behaviour1("First"));
    addBehaviour(new Behaviour1("Second"));
    addBehaviour(new Behaviour1("Third"));

  }

}
