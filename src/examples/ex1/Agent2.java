/*
  $Id$
*/

package examples.ex1;

import jade.core.*;
import jade.lang.acl.*;


// A test on Message Templates ...
public class Agent2 extends Agent {

  class Behaviour2 extends OneShotBehaviour {

    public Behaviour2(Agent a) {
      super(a);
    }

    protected void action() {
      System.out.println(getName() + ": executing...");
      MessageTemplate mt1 = MessageTemplate.MatchProtocol("fipa-request");
      MessageTemplate mt2 = MessageTemplate.MatchOntology("fipa-agent-management");
      MessageTemplate mtAnd = MessageTemplate.and(mt1, mt2);

      System.out.println("Message Template 1:");
      mt1.dump();

      System.out.println("Message Template 2:");
      mt2.dump();

      System.out.println("Message Template 1 AND Message Template 2:");
      mtAnd.dump();

      ACLMessage msg = new ACLMessage();
      msg.setType("inform");
      msg.setProtocol("fipa-request");
      msg.setOntology("pifa-chicken-management");

      System.out.println("Message :");
      msg.dump();
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

    }

  }


  protected void setup() {
    addBehaviour(new Behaviour2(this));
  }

}
