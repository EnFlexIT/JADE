/*
  $Log$
  Revision 1.2  1999/08/27 15:47:19  rimassa
  Added support for TransientException in order to retry message
  dispatch when the receiver agent has moved.

  Revision 1.1  1999/03/17 13:13:14  rimassa
  A remote proxy for an agent that can be reached by RMI (ordinary JADE agents).

*/

package jade.core;

import java.rmi.RemoteException;

import jade.lang.acl.ACLMessage;

class RemoteProxyRMI extends RemoteProxy {

  private AgentContainer ref;

  public RemoteProxyRMI(AgentContainer ac) {
    ref = ac;
  }

  public void dispatch(ACLMessage msg) throws NotFoundException, TransientException {
    try {
	ref.dispatch(msg); // RMI call
    }
    catch(RemoteException re) {
      throw new NotFoundException("RMI communication failure: ["+ re.getMessage() + "]");
    }

  }

  public void ping() throws UnreachableException {
    try {
      ref.ping();
    }
    catch(RemoteException re) {
      throw new UnreachableException("Unreachable RMI object");
    }
  }

}
