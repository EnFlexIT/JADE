/*
  $Id$
*/

/***************************************************************

  Name: ComplexBehaviour

  Responsibilities and Collaborations:

  + Allows agent programmers to structure agent behaviours in
    recursive aggregations.

  + Provides natural computation units for complex behaviours,
    inserting suitable breakpoints automatically.

****************************************************************/

package fipa.core;

import java.util.Vector;


public class ComplexBehaviour implements Behaviour {

  // Inner class to implement a singly linked list of behaviours
  private class BehaviourList {

    private class Node {
      public Behaviour item;
      public Node next;
    }

    Node first = null;
    Node last = null;
    Node current = null;

    public BehaviourList() {
    }

    public boolean isEmpty() {
      return first == null;
    }

    // Add a new Node to the end of the list, with b in it.
    public final synchronized void addElement(Behaviour b) {
      Node n = new Node();
      n.item = b;
      n.next = null;
      if(last != null)
	last.next = n;
      last = n;
      if(first == null)
	first = n;
    }

    public final synchronized boolean removeElement(Behaviour b) {
      // Remove b from the list; if b was in the list, return true
      // otherwise return false
      Node i = first;
      Node old = null;
      while( (i != null)&&(i.item != b) ) {
	old = i;
	i = i.next;
      }
      if(i == null)
	return false;
      else {
	if(i == first) {
	  first = i.next;
	  if(first == null)
	    last = null;
	}
	else { // PRE: i != first <=> PRE: old != null
	  old.next = i.next;
	  if(i == last)
	    last = old;
	}
	return true;
      }
    }

    public Behaviour getCurrent() {
      if(current != null)
	return current.item;
      else
	return null;
    }

    public void begin() {
      current = first;
    }

    public void end() {
      current = last;
    }

    public boolean next() {
      if(current != null) {
	current = current.next;
      }

      return current == null;
    }

  }

  protected Agent myAgent;
  private BehaviourList subBehaviours = new BehaviourList();

  // This variables mark the states when no sub-behaviour has been run
  // yet and when all sub-behaviours have been run.
  private boolean starting = true;
  private boolean finished = false;

  public ComplexBehaviour() {
    myAgent = null;
  }

  public ComplexBehaviour(Agent a) {
    myAgent = a;
  } 

  protected void preAction() {
  }

  protected void postAction() {
  }

  public final void execute() {
    if(starting) {
      preAction();
      subBehaviours.begin();
      starting = false;
    }
    Behaviour b = subBehaviours.getCurrent();
    b.execute();
    if (b.done()) {
      finished = subBehaviours.next();
    }
    if(finished) {
      postAction();
    }
  }

  public boolean done() {
    return finished;
  }

  public void addBehaviour(Behaviour b) {
    subBehaviours.addElement(b);
    //    b.setAgent(myAgent); // FIXME: Forcing the same agent in all behaviours tree ?
  }

  public void removeBehaviour(Behaviour b) {
    boolean rc = subBehaviours.removeElement(b);
    if(!rc) {
      // The specified behaviour was not found
    }
  }

}
