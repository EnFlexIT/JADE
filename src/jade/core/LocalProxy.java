/*
  $Log$
  Revision 1.1  1999/03/17 13:09:54  rimassa
  A class representing a cached local agent address. Weak references are used
  to allow garbage collection of dead agents even in the presence of cached
  aliases.

*/

package jade.core;

import java.lang.ref.WeakReference;

import jade.lang.acl.ACLMessage;

class LocalProxy implements AgentProxy {

  // A weak reference to the local agent.
  private WeakReference ref;

  public LocalProxy(Agent a) {
    ref = new WeakReference(a);
  }

  public void dispatch(ACLMessage msg) throws NotFoundException {

    Agent a = (Agent)ref.get();
    // If the agent has been collected, throw an exception
    if(a == null)
      throw new NotFoundException("Stale local proxy");
    a.postMessage(msg);
  }

}
