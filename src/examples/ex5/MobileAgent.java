package examples.ex5;

import jade.core.*;
import jade.core.behaviours.*;

import jade.lang.acl.ACLMessage;

public class MobileAgent extends Agent {

  private ComplexBehaviour mainBehaviour = new SequentialBehaviour();
  private ACLMessage replyMsg = new ACLMessage("inform");

  public void setup() {

    replyMsg.setContent("Hello there, I'm moving");

    mainBehaviour.addSubBehaviour(new OneShotBehaviour(this) {
      public void action() {
	System.out.println("First calculation step...");
	System.out.println("Waiting for a message...");
	ACLMessage msg = blockingReceive();
	System.out.println("OK. Now replying and moving...");
	replyMsg.removeAllDests();
	replyMsg.addDest(msg.getSource());
	send(replyMsg);
	doMove("Container-1");
      }
    });

    mainBehaviour.addSubBehaviour(new OneShotBehaviour(this) {
      public void action() {
	System.out.println("Second calculation step...");
	doMove("Container-2");
      }
    });

    mainBehaviour.addSubBehaviour(new OneShotBehaviour(this) {
      public void action() {
	System.out.println("Third calculation step...");
	System.out.println("Waiting for a message...");
	ACLMessage msg = blockingReceive();
	System.out.println("OK. Now replying and moving...");
	replyMsg.removeAllDests();
	replyMsg.addDest(msg.getSource());
	send(replyMsg);
	doMove("Front-End");
      }
    });

    mainBehaviour.addSubBehaviour(new OneShotBehaviour(this) {
      public void action() {
	System.out.println("Back home");
	System.out.println("Waiting for a message...");
	ACLMessage msg = blockingReceive();
	System.out.println("OK. Now replying and ending...");
	replyMsg.removeAllDests();
	replyMsg.addDest(msg.getSource());
	send(replyMsg);
	doDelete();
      }
    });

    addBehaviour(mainBehaviour);

  }

  protected void beforeMove() {
    System.out.println("beforeMove() called.");
  }

  protected void afterMove() {
    System.out.println("afterMove() called.");
  }

}
