/*
  $Log$
  Revision 1.2  1998/11/15 23:19:04  rimassa
  Filled actionPerformed() method to obtain a reference to the RMA agent
  and calling its shutDownPlatform() method.

  Revision 1.1  1998/11/09 22:22:14  Giovanni
  An action to shut down the whole Agent Platform.

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
public class ShutDownAction extends AMSAbstractAction {

  public ShutDownAction () {
    super ("SuspendActionIcon","Shut down Agent Platform");
  }

  public void actionPerformed(ActionEvent evt) {
    // Shut down the whole Agent Platform

    // Ask the RMA to do it, then the AgentPlatform itself
    // uses its shutDown() method.
    rma myRMA = AMSMainFrame.getRMA();
    myRMA.shutDownPlatform();

  }
}

