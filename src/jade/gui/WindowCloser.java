/*
  $Log$
  Revision 1.6  1999/05/19 18:31:34  rimassa
  Changed various classes to remove static references to RMA agent from GUI
  components and actions.

  Revision 1.5  1999/02/04 14:47:33  rimassa
  Changed package specification for Swing: now it's 'javax.swing' and no more
  'com.sun.swing'.

  Revision 1.4  1998/11/09 00:40:47  rimassa
  Modified callback method to terminate only the RMA agent when the GUI
  window is closed and not the whole agent container.

  Revision 1.3  1998/10/10 19:37:31  rimassa
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
 * This class is useful to make MainFrame handle
 * the WindowCLose event.
 */
public class WindowCloser extends WindowAdapter {

  private rma myRMA;

  public WindowCloser(rma anRMA) {
    myRMA = anRMA;
  }

  public void windowClosing(WindowEvent e) {
      myRMA.doDelete();
  }
}
