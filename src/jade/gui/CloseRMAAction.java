/*
  $Log$
  Revision 1.2  1999/02/04 14:47:26  rimassa
  Changed package specification for Swing: now it's 'javax.swing' and no more
  'com.sun.swing'.

  Revision 1.1  1998/11/09 22:21:35  Giovanni
  An action to close just the Remote Management Agent (and its GUI).

  */

package jade.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

import jade.domain.rma;

/** 
 * Close RMA Action
 * @see jade.gui.AMSAbstractAction
 */
public class CloseRMAAction extends AMSAbstractAction {

  public CloseRMAAction () {
    super ("SuspendActionIcon","Close RMA Agent");
  }

  public void actionPerformed(ActionEvent evt) {
    rma myRMA = AMSMainFrame.getRMA();
    myRMA.doDelete();
  }
}

