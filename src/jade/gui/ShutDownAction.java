/*
  $Log$
  Revision 1.1  1998/11/09 22:22:14  Giovanni
  An action to shut down the whole Agent Platform.

  */

package jade.gui;

import java.awt.*;
import java.awt.event.*;
import com.sun.java.swing.*;
import com.sun.java.swing.border.*;

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

    // Ask the AMS to do it, then the AgentPlatform itself
    // uses its shutDown() method.

  }
}

