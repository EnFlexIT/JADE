/*
 * $Id$
 */

package fipa.core;

import java.awt.AWTEvent;
import fipa.lang.acl.*;

/***************************************************************

  Name: CommEvent

  Responsibilities and Collaborations:

  + Objectifies the reception event, embedding the received ACL
    message.
    (ACLMessage)

******************************************************************/
public class CommEvent extends AWTEvent {

  private String      command;
  private ACLMessage  message;

  // FIXME: Check with Paolo whether this is still needed
  public CommEvent(CommBroadcaster source, String command) {
    super(source, -1);
    if(command != null) this.command = new String(command);
    else this.command = null;
  }

  public CommEvent(CommBroadcaster source, ACLMessage message) {
    super(source, -1);
    this.message = message;
  }

  public String getCommand() {
    return command;
  }

  public ACLMessage getMessage() {
    return message;
  }

}
