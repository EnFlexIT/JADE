
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

import jade.core.Agent;

/**
   Composite behaviour with sequential children scheduling. It is a
   <code>ComplexBehaviour</code> that executes its children behaviours
   in sequential order, and terminates when its last child has ended.
   
   
   @author Giovanni Rimassa - Universita` di Parma
   @version $Date$ $Revision$

*/
public class SequentialBehaviour extends ComplexBehaviour {

  /**
     Default constructor. It does not set the owner agent for this
     behaviour.
  */
  public SequentialBehaviour() {
  }

  /**
     This constructor sets the owner agent for this behaviour.
     @param a The agent this behaviour belongs to.
  */
  public SequentialBehaviour(Agent a) {
    super(a);
  }

  /**
     Sequential policy for children scheduling. This method executes
     children behaviours one at a time, in a FIFO fashion.
     @see jade.core.behaviours.ComplexBehaviour#bodyAction()
  */
  protected boolean bodyAction() {
    boolean result = false;

    if(!subBehaviours.isEmpty()) {

      Behaviour b = subBehaviours.getCurrent();
      b.action();
      if (b.done()) {
	result = subBehaviours.next();
      }

    }
    else {
      result = true;
    }

    return result;

  }


  /**
     Handle block/restart notifications. A
     <code>SequentialBehaviour</code> is blocked <em>only</em> when
     its currently active child is blocked, and becomes ready again
     when its current child is ready. This method takes care of the
     various possibilities.
     @param rce The event to handle.
  */
  protected void handle(RunnableChangedEvent rce) {

    // For upwards notification from the currently executing
    // sub-behaviour, copy the runnable state and create a new event
    if(rce.isUpwards()) {
      if(rce.getSource() == subBehaviours.getCurrent()) {
	myEvent.init(rce.isRunnable(), NOTIFY_UP);
	super.handle(myEvent);
      }
      // Ignore the event and pass it on
      else
	super.handle(rce);
    }
    // For downwards notifications, always copy the state but
    // forward to current sub-behaviour only when runnable == true
    else {
      boolean b = rce.isRunnable();
      if(b == true)
	super.handle(rce);
      else
	setRunnable(b);
    }

  }

}





