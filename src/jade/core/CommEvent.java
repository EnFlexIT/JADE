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
