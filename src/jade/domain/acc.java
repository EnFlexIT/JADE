/*
  $Id$
*/

package jade.domain;

public class acc extends Agent {

  private class ACCBehaviour implements Behaviour {

    public void execute() {
    }

    public boolean done() {
      return false;
    }

  } // End of ACCBehaviour class

  protected void setup() {

    addBehaviour(new ACCBehaviour());
  }

}
