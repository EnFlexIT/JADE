/*
  $Log$
  Revision 1.4  1999/02/04 14:47:29  rimassa
  Changed package specification for Swing: now it's 'javax.swing' and no more
  'com.sun.swing'.

  Revision 1.3  1998/10/10 19:37:19  rimassa
  Imported a newer version of JADE GUI from Fabio.

  Revision 1.2  1998/10/04 18:01:41  rimassa
  Added a 'Log:' field to every source file.
*/

package jade.gui;

import java.applet.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
 
public class PopupMouser extends MouseAdapter
{
    JPopupMenu popup;
    
    public PopupMouser(JPopupMenu p)
    {
        popup = p;
    }

    public void mouseReleased(MouseEvent e)
    {
        if (e.isPopupTrigger())
        { 
            popup.show(e.getComponent(), e.getX(), e.getY());
        }
    }

    public void mousePressed(MouseEvent e)
    {
        if (e.isPopupTrigger())
        { 
            popup.show(e.getComponent(), e.getX(), e.getY());
        }
    }
}

