/*
  $Log$
  Revision 1.7  1998/11/01 14:54:21  rimassa
  Added a 'doDelete()' call to make the Agent terminate at the end of
  its Behaviour.

  Revision 1.6  1998/10/18 16:10:16  rimassa
  Some code changes to avoid deprecated APIs.

   - Agent.parse() is now deprecated. Use ACLMessage.fromText(Reader r) instead.
   - ACLMessage() constructor is now deprecated. Use ACLMessage(String type)
     instead.
   - ACLMessage.dump() is now deprecated. Use ACLMessage.toText(Writer w)
     instead.

  Revision 1.5  1998/10/04 18:00:11  rimassa
  Added a 'Log:' field to every source file.

*/

package examples.ex1;

import java.io.*;

import jade.core.*;
import jade.lang.acl.*;


// A test on Message Templates ...
public class Agent2 extends Agent {

  class Behaviour2 extends OneShotBehaviour {

    public Behaviour2(Agent a) {
      super(a);
    }

    public void action() {
      System.out.println(getName() + ": executing...");
      MessageTemplate mt1 = MessageTemplate.MatchProtocol("fipa-request");
      MessageTemplate mt2 = MessageTemplate.MatchOntology("fipa-agent-management");
      MessageTemplate mtAnd = MessageTemplate.and(mt1, mt2);

      System.out.println("Message Template 1:");
      mt1.toText(new BufferedWriter(new OutputStreamWriter(System.out)));

      System.out.println("Message Template 2:");
      mt1.toText(new BufferedWriter(new OutputStreamWriter(System.out)));

      System.out.println("Message Template 1 AND Message Template 2:");
      mt1.toText(new BufferedWriter(new OutputStreamWriter(System.out)));

      ACLMessage msg = new ACLMessage("inform");

      msg.setProtocol("fipa-request");
      msg.setOntology("pifa-chicken-management");

      System.out.println("Message :");
      msg.toText(new BufferedWriter(new OutputStreamWriter(System.out)));
      if(mt1.match(msg))
	System.out.println("Matches 'mt1' template.");
      else
	System.out.println("Doesn't match 'mt1' template.");
      if(mt2.match(msg))
	System.out.println("Matches 'mt2' template.");
      else
	System.out.println("Doesn't match 'mt2' template.");
      if(mtAnd.match(msg))
	System.out.println("Matches 'mt1 AND mt2' template.");
      else
	System.out.println("Doesn't match 'mt1 AND mt2' template.");

      doDelete();
    }

  }


  protected void setup() {
    addBehaviour(new Behaviour2(this));
  }

}
