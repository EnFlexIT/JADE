/*
  $Log$
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

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public final class ReceiverBehaviour extends Behaviour {

  // The agent who wants to receive the ACL message
  Agent myAgent;

  // This message will contain the result
  private ACLMessage result;

  // The pattern to match incoming messages against
  private MessageTemplate template;

  private boolean finished;

  public ReceiverBehaviour(Agent a, ACLMessage msg, MessageTemplate mt) {
    myAgent = a;
    result = msg;
    template = mt;
  }

  public ReceiverBehaviour(Agent a, ACLMessage msg) {
    this(a, msg, null);
  }

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
      s = msg.getDest();
      if(s != null)
	result.setDest(s);
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

  public boolean done() {
    return finished;
  }

  public void reset() {
    if(result != null)
      result.reset();
  }

} // End of ReceiverBehaviour class

