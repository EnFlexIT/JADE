/*
  $Log$
  Revision 1.6  1999/02/25 08:42:30  rimassa
  Delegated shutdown to RMA agent insted of relying on finalizers and
  calling System.exit().

  Revision 1.5  1999/02/04 14:47:27  rimassa
  Changed package specification for Swing: now it's 'javax.swing' and no more
  'com.sun.swing'.

  Revision 1.4  1998/11/09 22:18:16  Giovanni
  Changed code indentation to comply with JADE coding style.

  Revision 1.3  1998/10/10 19:37:14  rimassa
  Imported a newer version of JADE GUI from Fabio.

  Revision 1.2  1998/10/04 18:01:41  rimassa
  Added a 'Log:' field to every source file.
*/

package jade.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

import jade.domain.rma;

/** 
 * Exit Action
 * @see jade.gui.AMSAbstractAction
 */
public class ExitAction extends AMSAbstractAction {

  public ExitAction () {
    super ("ExitActionIcon","Exit this Container");
  }

  public void actionPerformed(ActionEvent evt) {
    rma myRMA = AMSMainFrame.getRMA();
    myRMA.exit();
  }
}

