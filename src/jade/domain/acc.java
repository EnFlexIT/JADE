/*
  $Id$
*/

package jade.domain;

import jade.core.*;

public class acc extends Agent {

  private class ACCBehaviour extends SimpleBehaviour {

    public void action() {
    }

    public boolean done() {
      return false;
    }

  } // End of ACCBehaviour class

  protected void setup() {

    addBehaviour(new ACCBehaviour());
  }

}
