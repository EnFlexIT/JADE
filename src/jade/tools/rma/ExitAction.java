/*
  $Log$
  Revision 1.1  1999/05/20 15:42:09  rimassa
  Moved RMA agent from jade.domain package to jade.tools.rma package.

  Revision 1.7  1999/05/19 18:31:28  rimassa
  Changed various classes to remove static references to RMA agent from GUI
  components and actions.

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

package jade.tools.rma;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

/** 
 * Exit Action
 * @see jade.gui.AMSAbstractAction
 */
public class ExitAction extends AMSAbstractAction {

  private rma myRMA;

  public ExitAction (rma anRMA) {
    super ("ExitActionIcon","Exit this Container");
    myRMA = anRMA;
  }

  public void actionPerformed(ActionEvent evt) {
    myRMA.exit();
  }
}

