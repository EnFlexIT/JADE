package jade.gui;

import java.applet.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import com.sun.java.swing.*;
import com.sun.java.swing.event.*;
 
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

