/*
  $Log$
  Revision 1.4  1999/11/04 09:57:52  rimassaJade
  Removed TransientException related code.

  Revision 1.3  1999/11/03 07:50:28  rimassaJade
  Changed an older, check-and-wait code to adhere to new try-and-see
  approach.

  Revision 1.2  1999/08/27 15:46:32  rimassa
  Added support for TransientException in order to retry message
  dispatch when the receiver agent has moved.

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

    Agent receiver = (Agent)ref.get();
    // If the agent has been collected, throw an exception
    if(receiver == null)
      throw new NotFoundException("Stale local proxy");
    receiver.postMessage(msg);

  }

}
