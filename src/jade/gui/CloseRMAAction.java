/*
  $Log$
  Revision 1.1  1998/11/09 22:21:35  Giovanni
  An action to close just the Remote Management Agent (and its GUI).

  */

package jade.gui;

import java.awt.*;
import java.awt.event.*;
import com.sun.java.swing.*;
import com.sun.java.swing.border.*;

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

