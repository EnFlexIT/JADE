/*
  $Log$
  Revision 1.1  1999/05/20 13:43:16  rimassa
  Moved all behaviour classes in their own subpackage.

  Revision 1.12  1999/04/06 00:09:38  rimassa
  Documented public classes with Javadoc. Reduced access permissions wherever possible.

  Revision 1.11  1999/02/15 11:44:19  rimassa
  Changed removeBehaviour() method name to removeSubBehaviour().

  Revision 1.10  1999/02/14 23:15:06  rimassa
  Changed addBehaviour() method name to addSubBehaviour() to avoid
  confusion with Agent.addBehaviour().

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

package jade.core.behaviours;

import java.util.Vector;
import java.util.Stack;

import jade.core.Agent;

/**
   An abstract superclass for behaviours composed by many parts. This
   class holds inside a list of <b><em>children behaviours</em></b>,
   to which elements can be aded or emoved dynamically.
   When a <code>ComplexBehaviour</code> receives it execution quantum
   from the agent scheduler, it executes one of its children according
   to some policy. This class must be extended to provide the actual
   scheduling policy to apply when running children behaviours.
   @see jade.core.behaviours.SequentialBehaviour
   @see jade.core.behaviours.NonDeterministicBehaviour

   @author Giovanni Rimassa - Universita` di Parma
   @version $Date$ $Revision$

 */
public abstract class ComplexBehaviour extends Behaviour {

  /**
     Inner class to implement a list of behaviours.
  */
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

    /**
       Default constructor
    */
    public BehaviourList() {
    }

    /**
       Tells whether this list is empty.
    */
    public boolean isEmpty() {
      return first == null;
    }

    /**
       Add a new <code>Behaviour</code> to the end of the list.
    */
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

    /**
       Remove a <code>Behaviour</code> from the list.
       @return <code>true</code> if the element was present in the
       list, <code>false</code> otherwise.
    */
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

    /**
       Save the list cursor on an internal stack.
     */
    public void pushCurrent() {
      nodeStack.push(current);
    }

    /**
       Retrieves a previously saved list cursor.
    */
    public void popCurrent() {
      current = (Node)nodeStack.pop();
    }

    /**
       Reads the <code>Behaviour</code> pointed to by the list cursor.
    */
    public Behaviour getCurrent() {
      if(current != null)
	return current.item;
      else
	return null;
    }

    /**
       Moves the list cursor to the beginning.
    */
    public void begin() {
      current = first;
    }

    /**
       Moves the list cursor to the end.
    */
    public void end() {
      current = last;
    }

    /**
       Advances the list cursor by one.
       @return <code>true</code> if the end of the list has been
       reached.
    */
    public boolean next() {
      if(current != null) {
	current = current.next;
      }

      return current == null;
    }

    /**
       Reads the current size of the list.
    */
    public int size() {
      return length;
    }

  }

  /**
     The children list for this behaviour. It can be accessed by
     subclasses to implement their scheduling policies.
  */
  protected BehaviourList subBehaviours = new BehaviourList();

  // This variables mark the states when no sub-behaviour has been run
  // yet and when all sub-behaviours have been run.
  private boolean starting = true;
  private boolean finished = false;

  /**
     Default constructor, does not set the owner agent.
  */
  public ComplexBehaviour() {
    super();
  }

  /**
     This constructor sets the owner agent.
     @param a The agent this behaviour belongs to.
  */
  public ComplexBehaviour(Agent a) {
    super(a);
  } 

  /**
     This method is just an empty placeholders for subclasses. It is
     executed just once before starting children
     scheduling. Therefore, it acts as a prolog to the composite
     action represented by this <code>ComplexBehaviour</code>.
  */
  protected void preAction() {
  }

  /**
     Abstract policy method for children execution. Different
     subclasses will implement this method to run children according
     to some policy (sequentially, round robin, priority based, ...).
     @return <code>true</code> when done, <code>false</code> when
     children behaviours still need to be run.
     @see jade.core.behaviours.SequentialBehaviour
     @see jade.core.behaviours.NonDeterministicBehaviour
  */
  protected abstract boolean bodyAction();

  /**
     This method is just an empty placeholder for subclasses. It is
     invoked just once after children scheduling has ended. Therefore,
     it acts as an epilog for the composite task represented by this
     <code>ComplexBehaviour</code>. Overriding this method,
     application programmers can build <em>fork()/join()</em>
     execution structures.
     An useful idiom can be used to implement composite cyclic
     behaviours (e.g. a behaviour that continuously follows a specific
     interaction protocol): puttng a <code>reset()</code> call into
     <code>postAction()</code> method makes a complex behaviour
     restart as soon as it terminates, thereby turning it into a
     cyclic composite behaviour.
   */
  protected void postAction() {
  }

  /**
     Executes this <code>ComplexBehaviour</code>. This method starts
     by executing <code>preAction()</code>; then
     <code>bodyAction()</code> is called once per scheduling turn
     until it returns <code>true</code>. Eventually,
     <code>postAction()</code> is called.
   */
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

  /**
     Checks whether this behaviour has terminated.
     @return <code>true</code> if this <code>ComplexBehaviour</code>
     has finished executing, <code>false</code>otherwise.
  */
  public boolean done() {
    return finished;
  }

  /**
     Puts a <code>ComplexBehaviour</code> back in initial state. The
     internal state is cleaned up and <code>reset()</code> is
     recursively called for each child behaviour. 
  */
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

  /**
     Adds a behaviour to the children list. This method can be called
     whenever deemed useful, so that dynamic activation of behaviours
     is fully supported.
     @param b The behaviour to add.
  */
  public void addSubBehaviour(Behaviour b) {
    subBehaviours.addElement(b);
    b.setParent(this);
  }

  /**
     Removes a behaviour from the children list. This method can be
     called whenever deemed useful, so that dynamic removal of
     behaviour is fully supported.
     @param b The behaviour to remove. If it's not present in the
     list, nothing happens.
  */
  public void removeSubBehaviour(Behaviour b) {
    boolean rc = subBehaviours.removeElement(b);
    if(rc) {
      b.setParent(null);
    }
    else {
      // The specified behaviour was not found
    }
  }

  /**
     Handle block/restart notifications. This method handles
     notifications by simply forwarding them according to their
     original direction.
     @param rce The event to handle
  */
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


  /**
     Restarts this behaviour. A <code>ComplexBehaviour</code> blocks
     just like its <code>Behaviour</code> superclass, but when
     <code>restart()</code> is called all its children behaviours are
     notified, too.
  */
  public void restart() {
    // Notify upwards
    super.restart();

    // Then notify downwards
    myEvent.init(true, NOTIFY_DOWN);
    handle(myEvent);
  }

}

