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

package examples.behaviours;

import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;

/**
This an examples of an agent whose behaviours built as finite state
machine.
Note that the first behaviour never ends since the method done returns 
the boolean false;
@author Tiziana Trucco - CSELT S.p.A.
@version  $Date$ $Revision$  
*/

// An example of agent behaviours like  state machines.
public class FSMAgent extends Agent {

  class my3StepBehaviour extends SimpleBehaviour {

  	final int FIRST = 1;
  	final int SECOND = 2;
  	final int THIRD = 3;

  	private int state = FIRST;
    private boolean finished = false;
    
    public my3StepBehaviour(Agent a) {
      super(a);
    }

    public void action() {
      switch (state){
      	case FIRST: {op1(); state = SECOND; break;}
      	case SECOND:{op2(); state = THIRD;  break;}
      	case THIRD: {op3(); state = FIRST;finished = false; break;}
      }
    }

    
    public boolean done(){
    	return finished;
    }
    
    private void op1(){
    	System.out.println("\nAgent: "+ myAgent.getLocalName() + " is executing Step 1.1");
    }
    
    private void op2(){
    	System.out.println("\nAgent: "+ myAgent.getLocalName() + " is executing Step 1.2");
    }
    
    private void op3(){
    	System.out.println("\nAgent: "+ myAgent.getLocalName() + " is executing Step 1.3" );
    }     
	}
	
	class my4StepBehaviour extends SimpleBehaviour {

    final int FIRST  = 1;
    final int SECOND = 2;
    final int THIRD  = 3;
    final int FOURTH = 4;
    
		private int state = FIRST;
    private boolean finished = false;
    
    public my4StepBehaviour(Agent a) {
      super(a);
    }

   	public void action() {
      switch (state){
      	case FIRST:{op1();  state = SECOND; break;}
      	case SECOND:{op2(); state = THIRD;  break;}
      	case THIRD:{op3();  state = FOURTH; break;}
      	case FOURTH:{op4(); state = FIRST; finished = true; break;}
      }
      
    }

    public boolean done(){
    	return finished;
    }
    

		private void op1(){
    	System.out.println("\n\tAgent: "+ myAgent.getLocalName() + " is executing Step 2.1" );
    }
    
    private void op2(){
    	System.out.println("\n\tAgent: "+ myAgent.getLocalName() + " is executing Step 2.2");
    }
    
    private void op3(){
    	System.out.println("\n\tAgent: "+ myAgent.getLocalName() + " is executing Step 2.3" );
    }
    private void op4(){
    	System.out.println("\n\tAgent: "+ myAgent.getLocalName() + " is executing Step 2.4");
    }
	}
  
	protected void setup() {

    my3StepBehaviour mybehaviour = new my3StepBehaviour(this);
    addBehaviour(mybehaviour);
    
    // This is another way to add a new behaviour. 
    // The only difference being the programming style. 
		addBehaviour(new my4StepBehaviour(this));
  }


}