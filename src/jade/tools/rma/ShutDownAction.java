/*
  $Log$
  Revision 1.1  1999/05/20 15:42:11  rimassa
  Moved RMA agent from jade.domain package to jade.tools.rma package.

  Revision 1.4  1999/05/19 18:31:31  rimassa
  Changed various classes to remove static references to RMA agent from GUI
  components and actions.

  Revision 1.3  1999/02/04 14:47:30  rimassa
  Changed package specification for Swing: now it's 'javax.swing' and no more
  'com.sun.swing'.

  Revision 1.2  1998/11/15 23:19:04  rimassa
  Filled actionPerformed() method to obtain a reference to the RMA agent
  and calling its shutDownPlatform() method.

  Revision 1.1  1998/11/09 22:22:14  Giovanni
  An action to shut down the whole Agent Platform.

  */

package jade.tools.rma;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

/** 
 * Close RMA Action
 * @see jade.gui.AMSAbstractAction
 */
public class ShutDownAction extends AMSAbstractAction {

  private rma myRMA;

  public ShutDownAction (rma anRMA) {
    super ("SuspendActionIcon","Shut down Agent Platform");
    myRMA = anRMA;
  }

  public void actionPerformed(ActionEvent evt) {
    // Shut down the whole Agent Platform

    // Ask the RMA to do it, then the AgentPlatform itself
    // uses its shutDown() method.
    myRMA.shutDownPlatform();

  }
}

