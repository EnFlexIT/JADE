/*
 * $Id$
 */

package fipa.core;

import java.awt.AWTEvent;


/***************************************************************

  Name: CommEvent

  Responsibilities and Collaborations:

  + Objectifies the reception event, embedding the received ACL
    message.
    (ACLmessage)

******************************************************************/
public class CommEvent extends AWTEvent {

  private String      command;
  private aclMessage  message;

  public CommEvent(CommBroadcaster source, String command) {
    super(source, -1);
    if(command != null) this.command = new String(command);
    else this.command = null;
  }

  public CommEvent(CommBroadcaster source, aclMessage message) {
    super(source, -1);
    this.message = message;
  }

  public String getCommand() {
    return command;
  }

  public aclMessage getMessage() {
    return message;
  }

}
