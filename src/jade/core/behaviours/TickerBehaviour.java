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

package jade.core.behaviours;

import jade.core.behaviours.*;
import jade.lang.acl.*;
import jade.core.*;

/**
 * This abstract class implements a <code>Behaviour</code> that 
 * periodically executes a user-defined piece of code.
 * The user is expected to extend this class re-defining the method
 * <code>onTick()</code> and including the piece of code that 
 * must be periodically executed into it.
 * 
 * @author Giovanni Caire - TILAB
 */
public abstract class TickerBehaviour extends SimpleBehaviour {
	private long wakeupTime, period;
	private int state;
	private boolean finished;

  /**
   * Construct a <code>TickerBehaviour</code> that call its 
   * <code>onTick()</code> method every <code>period</code> ms.
   * @param a is the pointer to the agent
   * @param period the tick period in ms
   */
	public TickerBehaviour(Agent a, long period) {
  	super(a);
  	if (period <= 0) {
  		throw new IllegalArgumentException("Period must be greater than 0");
  	}
  	this.period = period;
  	state = 0;
	}

	public void action() {
  	switch (state) {
  	case 0:
    	// In this state the behaviour blocks itself
  		wakeupTime = System.currentTimeMillis() + period;
      block(period);
    	state = 1;
    	break;
  	case 1:
    	// In this state the behaviour has been restarted. This can be due to
    	// 1. Timeout expiration --> call onTick() and go back to state 0
    	// 2. A message has arrived --> block again and remains in state 1
    	long blockTime = wakeupTime - System.currentTimeMillis();
    	if (blockTime <= 0) {
      	// Timeout is expired
    		onTick();
    		state = 0;
    	} 
    	else 
      	block(blockTime);
    	break;
  	default :
  		// Should never happen
  		throw new RuntimeException("TickerBehaviour unknown state "+state);
  	} // END of switch
	} 

	public boolean done() {
  	return finished;
	}
	
  /**
   * Subclasses are expected to define this method specifying the action
   * that must be performed at every tick
   * @return when this method returns <code>true</code> the 
   * <code>TickerBehaviour</code> terminates
   */
	protected abstract void onTick();
	
  /**
   * This method must be called to reset the behaviour and starts again
   * @param period the new tick time
   */
	public void reset(long period) {
		this.reset();
  	if (period <= 0) {
  		throw new IllegalArgumentException("Period must be greater than 0");
  	}
  	this.period = period;
	}

  /**
   * This method must be called to reset the behaviour and starts again
   * @param timeout indicates in how many milliseconds from now the behaviour
   * must be waken up again. 
   */
	public void reset() {
  	super.reset();
  	state = 0;
  	finished = false;
	}

	/**
	 * This method makes this <code>TickerBehaviour</code> terminate
	 */
	public void stop() {
		finished = true;
	}
}
