package examples;


import jade.core.*;
import jade.core.behaviours.*;

public class MobileAgent extends Agent {

  private ComplexBehaviour mainBehaviour = new SequentialBehaviour();

  public void setup() {
    mainBehaviour.addSubBehaviour(new OneShotBehaviour(this) {
      public void action() {
	System.out.println("First calculation step...");
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
	doMove("Front-End");
      }
    });

    mainBehaviour.addSubBehaviour(new OneShotBehaviour(this) {
      public void action() {
	System.out.println("Back home");
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
