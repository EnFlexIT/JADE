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

    myBehaviour2_1.addSubBehaviour(new Behaviour4Step(this,"2.1a",2));
    myBehaviour2_1.addSubBehaviour(new Behaviour4Step(this,"2.1b",2));
    myBehaviour2_1.addSubBehaviour(new Behaviour4Step(this,"2.1c",2));

    myBehaviour2_2.addSubBehaviour(new Behaviour4Step(this,"2.2.1",2));
    myBehaviour2_2.addSubBehaviour(new Behaviour4Step(this,"2.2.2",2));
    myBehaviour2_2.addSubBehaviour(new Behaviour4Step(this,"2.2.3",2));

    myBehaviour1.addSubBehaviour(new Behaviour4Step(this,"1.1",1));
    myBehaviour1.addSubBehaviour(new Behaviour4Step(this,"1.2",1));
    myBehaviour1.addSubBehaviour(new Behaviour4Step(this,"1.3",1));

    myBehaviour2.addSubBehaviour(myBehaviour2_1);
    myBehaviour2.addSubBehaviour(myBehaviour2_2);
    myBehaviour2.addSubBehaviour(new Behaviour4Step(this,"2.3",2));
    myBehaviour2.addSubBehaviour(new Behaviour4Step(this,"2.4",2));
    myBehaviour2.addSubBehaviour(new Behaviour4Step(this,"2.5",2));

    addBehaviour(myBehaviour1);
    addBehaviour(myBehaviour2);

    System.out.println("Blocking ...");
    myBehaviour2_2.block();
    System.out.println("Restarting ...");
    myBehaviour2_2.restart();

  }


}
