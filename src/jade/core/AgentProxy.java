/*
  $Log$
  Revision 1.2  1999/08/27 15:45:48  rimassa
  Added support for TransientException in order to retry message
  dispatch when the receiver agent has moved.

  Revision 1.1  1999/03/17 13:08:25  rimassa
  An interface to represent agent addresses cached within a container, through
  which an ACL message can be dispatched.

*/

package jade.core;

import jade.lang.acl.ACLMessage;

interface AgentProxy {
  void dispatch(ACLMessage msg) throws NotFoundException, TransientException;
}
