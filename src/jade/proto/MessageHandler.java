/*
  $Log$
  Revision 1.4  1999/04/06 00:10:23  rimassa
  Documented public classes with Javadoc. Reduced access permissions wherever possible.

  Revision 1.3  1998/10/04 18:02:15  rimassa
  Added a 'Log:' field to every source file.

*/

package jade.proto;

import jade.lang.acl.ACLMessage;

/**************************************************************

  Name: MessageHandler

  Responsibility and Collaborations:

  + Provides an abstract interface for ProtocolDrivenBehaviour to
    invoke agent-specific code in order to perform some action when a
    specific message is received. An Interaction object is passed to
    user code, thus making the current interaction state accessible.
    (ProtocolDrivenBehaviour, ACLMessage, Interaction)


****************************************************************/
interface MessageHandler {

  public void handle(ACLMessage msg, Interaction i);
}
