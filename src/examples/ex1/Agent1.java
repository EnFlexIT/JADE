/*
  $Log$
  Revision 1.3  1998/10/04 18:00:10  rimassa
  Added a 'Log:' field to every source file.

*/

package examples.ex1;

import jade.core.*;

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
