/*
  $Log$
  Revision 1.2  1999/06/16 00:20:40  rimassa
  Added a comprehensive support for timeouts on message reception.

  Revision 1.1  1999/05/20 13:43:17  rimassa
  Moved all behaviour classes in their own subpackage.

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

package jade.core.behaviours;

import java.util.Enumeration;

import jade.core.Agent;
import jade.core.AgentGroup;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

/**
   Behaviour for receiving an ACL message. This class encapsulates a
   <code>receive()</code> as an atomic operation. This behaviour
   terminates when an ACL message is received. If no suitable message
   is present, <code>action()</code> calls <code>block()</code> and
   returns.
   @see jade.core.behaviours.SenderBehaviour
   @see jade.core.Agent#receive()
   @see jade.lang.acl.ACLMessage

   @author Giovanni Rimassa - Universita` di Parma
   @version $Date$ $Revision$

 */
public final class ReceiverBehaviour extends Behaviour {

  /**
   Exception class for timeouts. This exception is thrown when trying
   to obtain an <code>ACLMessage</code> object from an
   <code>Handle</code>, but no message was received within a specified
   timeout.
   @see jade.core.behaviours.ReceiverBehaviour.Handle#getMessage()
  */
  public static class TimedOut extends Exception {
    TimedOut() {
      super("No message was received before time limit.");
    }
  }

  /**
   Exception class for timeouts. This exception is thrown when trying
   to obtain an <code>ACLMessage</code> from an <code>Handle</code>
   and no message was received so far, but the time limit is not yet
   reached.
   @see jade.core.behaviours.ReceiverBehaviour.Handle#getMessage()
  */
  public static class NotYetReady extends Exception {
    NotYetReady() {
      super("Requested message is not ready yet.");
    }
  }

  /**
   An interface representing ACL messages due to arrive within a time
   limit. This interface is used to create a
   <code>ReceiverBehaviour</code> object to receive an ACL message
   within a user specified time limit. When the user tries to read the
   message represented by the handle, either gets it or gets an
   exception.
   @see jade.core.behaviours.ReceiverBehaviour#newHandle()
   @see jade.core.behaviours.ReceiverBehaviour#ReceiverBehaviour(Agent
   a, ReceiverBehaviour.Handle h, long millis)
   */
  public static interface Handle {

    /**
     Tries to retrieve the <code>ACLMessage</code> object represented
     by this handle.
     @return The ACL message, received by the associated
     <code>ReceiverBehaviour</code>, if any.
     @exception TimedOut If the associated
     <code>ReceiverBehaviour</code> did not receive a suitable ACL
     message within the time limit.
     @exception NotYetReady If the associated
     <code>ReceiverBehaviour</code> is still waiting for a suitable
     ACL message to arrive.
     @see jade.core.behaviours.ReceiverBehaviour#ReceiverBehaviour(Agent
     a, ReceiverBehaviour.Handle h, long millis)
    */
    ACLMessage getMessage() throws TimedOut, NotYetReady;

  }

  private static class MessageFuture implements Handle {

    private static final int OK = 0;
    private static final int NOT_YET = 1;
    private static final int TIMED_OUT = 2;

    private int state = NOT_YET;
    private ACLMessage message;

    public void reset() {
      message = null;
      state = NOT_YET;
    }

    public void setMessage(ACLMessage msg) {
      message = msg;
      if(message != null)
	state = OK;
      else
	state = TIMED_OUT;
    }

    public ACLMessage getMessage() throws TimedOut, NotYetReady {
      switch(state) {
      case NOT_YET:
	throw new NotYetReady();
      case TIMED_OUT:
	throw new TimedOut();
      default:
	return message;
      }
    }
  }

  /**
   Factory method for message handles. This method returns a new
   <code>Handle</code> object, which can be used to retrieve an ACL
   message out of a <code>ReceiverBehaviour</code> object.
   @return A new <code>Handle</code> object.
   @see jade.core.behaviours.ReceiverBehaviour.Handle
  */
  public static Handle newHandle() {
    return new MessageFuture();
  }

  // This message will contain the result
  private ACLMessage result;

  // The pattern to match incoming messages against
  private MessageTemplate template;

  // A future for the ACL message, used when a timeout was specified
  private MessageFuture future;

  // A time out value, when present
  private long timeOut;

  // A running counter for calling block(millis) until 'timeOut' milliseconds pass.
  private long timeToWait;

  // Timestamp holder, used when calling block(millis) many times.
  private long blockingTime = 0;

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
    super(a);
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
     Receive any ACL message, waiting at most <code>millis</code>
     milliseconds.
     When calling this constructor, a suitable <code>Handle</code>
     must be created and passed to it. When this behaviour ends, some
     other behaviour will try to get the ACL message out of the
     handle, and an exception will be thrown in case of a time out.
     The following example code explains this:

     <code><pre>
       // ReceiverBehaviour creation, e.g. in agent setup() method
       h = ReceiverBehaviour.newHandle(); // h is an agent instance variable
       addBehaviour(new ReceiverBehaviour(this, h, 10000); // Wait 10 seconds

       ...

       // Some other behaviour, later, tries to read the ACL message
       // in its action() method
       try {
         ACLMessage msg = h.getMessage();
	 // OK. Message received within timeout.
       }
       catch(ReceiverBehaviour.TimedOut rbte) {
         // Receive timed out
       }
       catch(ReceiverBehaviour.NotYetReady rbnyr) {
         // Message not yet ready, but timeout still active
       }
     </pre></code>
     @param a The agent this behaviour belongs to.
     @param h An <em>Handle</em> representing the message to receive.
     @param millis The maximum amount of time to wait for the message,
     in milliseconds.
     @see jade.core.behaviours.ReceiverBehaviour.Handle
     @see jade.core.behaviours.ReceiverBehaviour#newHandle()
   */
  public ReceiverBehaviour(Agent a, Handle h, long millis) {
    this(a, h, millis, null);
  }

  public ReceiverBehaviour(Agent a, Handle h, long millis, MessageTemplate mt) {
    super(a);
    future = (MessageFuture)h;
    timeOut = millis;
    timeToWait = timeOut;
    result = new ACLMessage("not-understood");
    template = mt;
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
      if(future == null) {
	block();
	finished = false;
	return;
      }
      else {
	long elapsedTime = 0;
	if(blockingTime != 0)
	  elapsedTime = System.currentTimeMillis() - blockingTime;
	else
	  elapsedTime = 0;
	timeToWait -= elapsedTime;
	if(timeToWait > 0) {
	  blockingTime  = System.currentTimeMillis();
	  // System.out.println("Waiting for " + timeToWait + " ms.");
	  block(timeToWait);
	  return;
	}
	else {
	  future.setMessage(msg);
	  finished = true;
	}
      }
    }
    else {
      copyInResult(msg);
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
    if(future != null) {
      future.reset();
      result = new ACLMessage("not-understood");
    }
    timeToWait = timeOut;
    blockingTime = 0;
  }

  private void copyInResult(ACLMessage msg) {
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

    if(future != null)
      future.setMessage(result);

    finished = true;
  }

} // End of ReceiverBehaviour class

