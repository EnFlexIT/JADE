/*
  $Id$
*/

package examples.ex1;

import jade.core.*;

// More examples on Complex Behaviours, featuring NonDeterministic Behaviours.
public class Agent4 extends Agent {

  class Behaviour4Step extends SimpleBehaviour {

    private String myCode;
    private int executionTimes;
    private boolean finished = false;

    public Behaviour4Step(Agent a, String code, int i) {
      super(a);
      myCode = code;
      executionTimes = i;
    }

    public void action() {
      System.out.println("Agent " + getName() + ": Step " + myCode);
      --executionTimes;
      if(executionTimes<=0)
	finished = true;
    } 

    public boolean done() {
      return finished;
    }

}


  protected void setup() {

    ComplexBehaviour myBehaviour1 = new SequentialBehaviour(this);
    ComplexBehaviour myBehaviour2 = NonDeterministicBehaviour.createWhenAll(this);

    ComplexBehaviour myBehaviour2_1 = NonDeterministicBehaviour.createWhenAll(this);
    ComplexBehaviour myBehaviour2_2 = new SequentialBehaviour(this);

    myBehaviour2_1.addBehaviour(new Behaviour4Step(this,"2.1a",2));
    myBehaviour2_1.addBehaviour(new Behaviour4Step(this,"2.1b",2));
    myBehaviour2_1.addBehaviour(new Behaviour4Step(this,"2.1c",2));

    myBehaviour2_2.addBehaviour(new Behaviour4Step(this,"2.2.1",2));
    myBehaviour2_2.addBehaviour(new Behaviour4Step(this,"2.2.2",2));
    myBehaviour2_2.addBehaviour(new Behaviour4Step(this,"2.2.3",2));

    myBehaviour1.addBehaviour(new Behaviour4Step(this,"1.1",1));
    myBehaviour1.addBehaviour(new Behaviour4Step(this,"1.2",1));
    myBehaviour1.addBehaviour(new Behaviour4Step(this,"1.3",1));

    myBehaviour2.addBehaviour(myBehaviour2_1);
    myBehaviour2.addBehaviour(myBehaviour2_2);
    myBehaviour2.addBehaviour(new Behaviour4Step(this,"2.3",2));
    myBehaviour2.addBehaviour(new Behaviour4Step(this,"2.4",2));
    myBehaviour2.addBehaviour(new Behaviour4Step(this,"2.5",2));

    addBehaviour(myBehaviour1);
    addBehaviour(myBehaviour2);

    System.out.println("Blocking ...");
    myBehaviour2_2.block();
    System.out.println("Restarting ...");
    myBehaviour2_2.restart();

  }


}
