/*
  $Log$
  Revision 1.3  1999/04/06 00:10:24  rimassa
  Documented public classes with Javadoc. Reduced access permissions wherever possible.

  Revision 1.2  1998/10/04 18:02:16  rimassa
  Added a 'Log:' field to every source file.

*/

package jade.proto;

/**************************************************************

  Name: MessageSelector

  Responsibility and Collaborations:

  + Provides an abstract interface for ProtocolDrivenBehaviour to
    invoke agent-specific code in order to select a response among the
    ones allowed by the protcol. A MessageGroup contains the allowed
    ACL messages, and has an internal Iterator that must be set to
    point to the chosen message. Besides, an Interaction object is
    passed to user code to give access to current interaction state.
    (ProtocolDrivenBehaviour)


****************************************************************/
interface MessageSelector {

  public void select(MessageGroup answers, Interaction i);

}
