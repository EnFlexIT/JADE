/*
  $Log$
  Revision 1.1  1998/10/26 00:12:30  rimassa
  New domain agent to perform platform administration: this agent has a GUI to
  manage the Agent Platform and special access rights to the AMS.

*/


package jade.domain;

import jade.core.*;
import jade.lang.acl.ACLMessage;
import jade.gui.*;

/**************************************************************

  Name: rma

  Responsibility and Collaborations:

  + Serves as Remote Management Agent for the Agent Platform,
    according to our proposal to FIPA 97 specification.

  + Relies on the AMS to perform Agent Management actions, talking
    with it through simple ACL mesages.
    (ams)

****************************************************************/
public class rma extends Agent {

  private AMSMainFrame myGUI = new AMSMainFrame();

  public void setup() {
    myGUI.ShowCorrect();
    // Notice: no behaviours for now
  }

}
