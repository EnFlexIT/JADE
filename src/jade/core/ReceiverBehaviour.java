/*
  $Log$
  Revision 1.9  1999/04/06 00:09:43  rimassa
  Documented public classes with Javadoc. Reduced access permissions wherever possible.

  Revision 1.8  1999/03/09 13:02:41  rimassa
  Made a change to correctly receive ACL messages with more than a
  single receiver.

  Revision 1.7  1998/12/01 23:36:50  rimassa
  Fixed a wrong implementation of reset() method.

  Revision 1.6  1998/10/30 18:26:53  rimassa
  Added an implementation of reset() method that simply resets the
  embedded ACL message.

  Revision 1.5  1998/10/10 19:14:34  rimassa
  Fixed some compilation errors. Now the class compiles and runs.

  Revision 1.4  1998/10/05 20:16:04  Giovanni
  Made 'final' ReceiverBehaviour class.

  Revision 1.3  1998/10/04 18:01:13  rimassa
  Added a 'Log:' field to every source file.

*/

package jade.core;

import java.util.Enumeration;

import jade.core.AgentGroup;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

/**
   Behaviour for receiving an ACL message. This class encapsulates a
   <code>receive()</code> as an atomic operation. This behaviour
   terminates when an ACL message is received. If no suitable message
   is present, <code>action()</code> calls <code>block()</code> and
   returns.
   @see jade.core.SenderBehaviour
   @see jade.core.Agent#receive()
   @see jade.lang.acl.ACLMessage

   @author Giovanni Rimassa - Universita` di Parma
   @version $Date$ $Revision$

 */
public final class ReceiverBehaviour extends Behaviour {

  // The agent who wants to receive the ACL message
  private Agent myAgent;

  // This message will contain the result
  private ACLMessage result;

  // The pattern to match incoming messages against
  private MessageTemplate template;

  private boolean finished;

  /**
     Receive a matching ACL message. This constructor creates a
     <code>ReceiverBehaviour</code> object that ends as soon as an ACL
     message matching a given <code>MessageTemplate</code> arrives.
     @param a The agent this behaviour belongs to, and that will
     <code>receive()</code> the message.
     @param msg An ACL message where the received message will be
     copied.
     @param mt A Message template to match incoming messages against.
  */
  public ReceiverBehaviour(Agent a, ACLMessage msg, MessageTemplate mt) {
    myAgent = a;
    result = msg;
    template = mt;
  }

  /**
     Receive any ACL message. This constructor creates a
     <code>ReceiverBehaviour</code> object that ends as soon as any
     ACL message is received.
     @param a The agent this behaviour belongs to, and that will
     <code>receive()</code> the message.
     @param msg An ACL message where the received message will be
     copied.
  */
  public ReceiverBehaviour(Agent a, ACLMessage msg) {
    this(a, msg, null);
  }

  /**
     Actual behaviour implementation. This method receives a suitable
     ACL message and copies it into the message provided by the
     behaviour creator. It blocks the current behaviour if no suitable
     message is available.
  */
  public void action() {
    ACLMessage msg = null;
    if(template == null)
      msg = myAgent.receive();
    else
      msg = myAgent.receive(template);

    if(msg == null) {
      block();
      finished = false;
      return;
    }
    else {
      // Copies msg into result
      result.setType(msg.getType());
      String s = msg.getContent();
      if(s != null)
	result.setContent(s);
      s = msg.getConversationId();
      if(s != null)
	result.setConversationId(s);
      AgentGroup ag = msg.getDests();
      Enumeration e = ag.getMembers();
      while(e.hasMoreElements()) {
	s = (String)e.nextElement();
	result.addDest(s);
      }
      s = msg.getEnvelope();
      if(s != null)
	result.setEnvelope(s);
      s = msg.getLanguage();
      if(s != null)
	result.setLanguage(s);
      s = msg.getOntology();
      if(s != null)
	result.setOntology(s);
      s = msg.getProtocol();
      if(s != null)
	result.setProtocol(s);
      s = msg.getReplyBy();
      if(s != null)
	result.setReplyBy(s);
      s = msg.getReplyTo();
      if(s != null)
	result.setReplyTo(s);
      s = msg.getReplyWith();
      if(s != null)
	result.setReplyWith(s);
      s = msg.getSource();
      if(s != null)
	result.setSource(s);

      finished = true;
    }
  }

  /**
     Checks whether this behaviour ended.
     @return <code>true</code> when an ACL message has been received.
  */
  public boolean done() {
    return finished;
  }

  /**
     Resets this behaviour. This method allows to receive another
     <code>ACLMessage</code> with the same
     <code>ReceiverBehaviour</code> without creating a new object.
  */
  public void reset() {
    finished = false;
    result = null;
  }

} // End of ReceiverBehaviour class

