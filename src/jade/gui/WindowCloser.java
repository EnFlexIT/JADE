/*
  $Log$
  Revision 1.2  1998/10/04 18:02:07  rimassa
  Added a 'Log:' field to every source file.

*/

package jade.gui;

import java.awt.*;
import java.awt.event.*;
import com.sun.java.swing.*;
import com.sun.java.swing.border.*;

/**
 * This class is useful to make MainFrame handle
 * the WindowCLose event.
 */
public class WindowCloser extends WindowAdapter
{
    public void windowClosing(WindowEvent e)
    {
		Window win = e.getWindow();
		win.setVisible(false);
		win.dispose();
		System.exit(0);
    }
}
