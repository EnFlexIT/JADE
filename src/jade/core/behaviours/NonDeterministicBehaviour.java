/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop multi-agent
systems in compliance with the FIPA specifications.
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

import java.util.Hashtable;
import java.util.Iterator;

import jade.core.Agent;

/**
   @deprecated Use <code>ParallelBehaviour</code> instead.
   Composite behaviour with non deterministic children scheduling.
   It is a <code>ComplexBehaviour</code> that executes its children
   behaviours non deterministically, and it terminates when a
   particular condition on its sub-behaviours is met. Static
   <em><b>Factory Methods</b></em> are provided to get a
   <code>NonDeterministicBehaviour</code> that ends when all its
   sub-behaviours are done, when any sub-behaviour terminates or when
   <em>N</em> sub-behaviours have finished.

   
   @author Giovanni Rimassa - Universita` di Parma
   @version $Date$ $Revision$

*/
public class NonDeterministicBehaviour extends ParallelBehaviour {
  /**
   * Constructor
   */
  protected NonDeterministicBehaviour(int endCondition) {
  	super(endCondition);
  }

  /**
   * Constructor
   */
  public NonDeterministicBehaviour(Agent a, int endCondition) {
    super(a, endCondition);
  }
  
  // static Factory Methods to create NonDeterministicBehaviours with
  // various kinds of termination condition.

  
  /**
     Static <em>Factory Method</em>. This method creates a new
     <code>NonDeterministicBehaviour</code> that terminates when
     <b><em>all</em></b> its children end. It does not set the owner
     agent for this behaviour.
     @return A new <code>NonDeterministicBehaviour</code>.
  */
  public static NonDeterministicBehaviour createWhenAll() {
    return new NonDeterministicBehaviour(WHEN_ALL);
  }

  /**
     Static <em>Factory Method</em>. This method creates a new
     <code>NonDeterministicBehaviour</code> that terminates when
     <b><em>all</em></b> its children end. It sets the owner agent for
     this behaviour.
     @param a The agent this behaviour belongs to.
     @return A new <code>NonDeterministicBehaviour</code>.
  */
  public static NonDeterministicBehaviour createWhenAll(Agent a) {
    return new NonDeterministicBehaviour(a, WHEN_ALL);
  }

  /**
     Static <em>Factory Method</em>. This method creates a new
     <code>NonDeterministicBehaviour</code> that terminates when
     <b><em>any</em></b> among its children ends. It does not set the
     owner agent for this behaviour.
     @return A new <code>NonDeterministicBehaviour</code>.
  */
  public static NonDeterministicBehaviour createWhenAny() {
    return new NonDeterministicBehaviour(WHEN_ANY);
  }

  /**
     Static <em>Factory Method</em>. This method creates a new
     <code>NonDeterministicBehaviour</code> that terminates when
     <b><em>any</em></b> among its children ends. It sets the owner
     agent for this behaviour.
     @param a The agent this behaviour belongs to.
     @return A new <code>NonDeterministicBehaviour</code>.
  */
  public static NonDeterministicBehaviour createWhenAny(Agent a) {
    return new NonDeterministicBehaviour(a, WHEN_ANY);
  }

  /**
     Static <em>Factory Method</em>. This method creates a new
     <code>NonDeterministicBehaviour</code> that terminates when
     <b><em>at least N</em></b> of its children end. It does not set
     the owner agent for this behaviour.
     @param howMany The number of children behaviour that must
     terminate to make this <code>NonDeterministicBehaviour</code>
     finish.
     @return A new <code>NonDeterministicBehaviour</code>.
  */
  public static NonDeterministicBehaviour createWhenN(int howMany) {
    return new NonDeterministicBehaviour(howMany);
  }

  /**
     Static <em>Factory Method</em>. This method creates a new
     <code>NonDeterministicBehaviour</code> that terminates when
     <b><em>at least N</em></b> of its children end. It sets the owner
     agent for this behaviour.
     @param a The agent this behaviour belongs to.
     @param howMany The number of children behaviour that must
     terminate to make this <code>NonDeterministicBehaviour</code>
     finish.
     @return A new <code>NonDeterministicBehaviour</code>.
  */
  public static NonDeterministicBehaviour createWhenN(Agent a, int howMany) {
    return new NonDeterministicBehaviour(a, howMany);
  }


}