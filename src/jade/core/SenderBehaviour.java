/*
  $Log$
  Revision 1.3  1998/10/05 20:15:02  Giovanni
  Made 'final' SenderBehaviour class.

  Revision 1.2  1998/10/04 18:01:15  rimassa
  Added a 'Log:' field to every source file.

*/

package jade.core;

public final class SenderBehaviour extends OneShotBehaviour {

  // The agent who is sending the message
  private Agent myAgent;

  // The ACL message to send
  private ACLMessage message;

  // An AgentGroup to perform multicasting
  private AgentGroup receivers;

  public SenderBehaviour(Agent a, ACLMessage msg, AgentGroup ag) {
    myAgent = a;
    message = msg;
    receivers = ag;

    message.setSource(myAgent);
  }

  public SenderBehaviour(Agent a, ACLMessage msg) {
    this(a, msg, null);
  }

  public void action() {
    if(receivers == null)
      myAgent.send(message);
    else
      myAgent.send(message, receivers);
  }

}
