/*
  $Log$
  Revision 1.9  1998/10/31 12:58:56  rimassa
  Made 'protected' the former 'private' inner class BehaviourList.

  Revision 1.8  1998/10/30 18:22:36  rimassa
  Added an implementation of 'reset()' method: a ComplexBehaviour can
  reset itself by changing some state variables, by moving at the
  beginning the cursor of its children list and by recursively calling
  reset() for every child.

  Revision 1.7  1998/10/04 18:01:06  rimassa
  Added a 'Log:' field to every source file.

*/

/***************************************************************

  Name: ComplexBehaviour

  Responsibilities and Collaborations:

  + Allows agent programmers to structure agent behaviours in
    recursive aggregations.

  + Provides natural computation units for complex behaviours,
    inserting suitable breakpoints automatically.

****************************************************************/

package jade.core;

import java.util.Vector;
import java.util.Stack;


public abstract class ComplexBehaviour extends Behaviour {

  // Inner class to implement a singly linked list of behaviours
  protected class BehaviourList {

    private class Node {
      public Behaviour item;
      public Node next;
    }

    Node first = null;
    Node last = null;
    Node current = null;

    Stack nodeStack = new Stack();

    // Node counter
    int length = 0;

    public BehaviourList() {
    }

    public boolean isEmpty() {
      return first == null;
    }

    // Add a new Node to the end of the list, with b in it.
    public void addElement(Behaviour b) {
      Node n = new Node();
      n.item = b;
      n.next = null;
      if(last != null)
	last.next = n;
      last = n;
      if(first == null)
	first = n;
      ++length;
    }

    public boolean removeElement(Behaviour b) {
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
	--length;
	return true;
      }
    }

    public void pushCurrent() {
      nodeStack.push(current);
    }

    public void popCurrent() {
      current = (Node)nodeStack.pop();
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

    public int size() {
      return length;
    }

  }

  protected BehaviourList subBehaviours = new BehaviourList();

  // This variables mark the states when no sub-behaviour has been run
  // yet and when all sub-behaviours have been run.
  private boolean starting = true;
  private boolean finished = false;

  public ComplexBehaviour() {
    super();
  }

  public ComplexBehaviour(Agent a) {
    super(a);
  } 

  protected void preAction() {
  }

  // Subclasses implementation must return true when done
  protected abstract boolean bodyAction();

  protected void postAction() {
  }

  public final void action() {
    if(starting) {
      preAction();
      subBehaviours.begin();
      starting = false;
    }

    finished = bodyAction();

    if(finished) {
      postAction();
    }
  }

  public boolean done() {
    return finished;
  }

  public void reset() {

    subBehaviours.begin();

    Behaviour b = subBehaviours.getCurrent();
    while(b != null) {
      b.reset();
      subBehaviours.next();
      b = subBehaviours.getCurrent();
    }

    subBehaviours.begin();
    starting = true;
    finished = false;

  }

  public void addBehaviour(Behaviour b) {
    subBehaviours.addElement(b);
    b.setParent(this);
    //    b.setAgent(myAgent); // FIXME: Forcing the same agent in all behaviours tree ?
  }

  public void removeBehaviour(Behaviour b) {
    boolean rc = subBehaviours.removeElement(b);
    if(rc) {
      b.setParent(null);
    }
    else {
      // The specified behaviour was not found
    }
  }

  // This method handles notification by simply forwarding it
  // according to its original direction
  protected void handle(RunnableChangedEvent rce) {

    // Pass downwards events to children
    if(!rce.isUpwards()) {
      subBehaviours.pushCurrent(); // Save cursor

      // Iterate over the entire list and call handle() for each
      // sub-behaviour
      subBehaviours.begin();
      Behaviour b = subBehaviours.getCurrent();
      while(b != null) {
	b.handle(rce);
	subBehaviours.next();
	b = subBehaviours.getCurrent();
      }

      subBehaviours.popCurrent(); // Restore cursor
    }
    // Copy runnable state and pass it to parent, if the event is
    // going upwards and a parent is present
    super.handle(rce);

  }


  // block()/restart() are the public interface to Behaviour
  // scheduling subsystem. A ComplexBehaviour blocks just like its
  // Behaviour superclass, but when restart() is called sub-behaviours
  // are notified, too.

  public void restart() {
    // Notify upwards
    super.restart();

    // Then notify downwards
    myEvent.init(true, NOTIFY_DOWN);
    handle(myEvent);
  }

}

