/*
  $Log$
  Revision 1.4  1999/02/04 14:47:25  rimassa
  Changed package specification for Swing: now it's 'javax.swing' and no more
  'com.sun.swing'.

  Revision 1.3  1998/10/10 19:37:06  rimassa
  Imported a newer version of JADE GUI from Fabio.

  Revision 1.2  1998/10/04 18:01:41  rimassa
  Added a 'Log:' field to every source file.
*/

package jade.gui;

import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.io.*;
import java.util.*;

/**
 * The Table with three cols on the left
 */
public class AMSTable extends JPanel
{
	private AMSTableModel model;
    public AMSTable() 
    {
        JTable table;
  
   		Font f;
     
        f = new Font("SanSerif",Font.PLAIN,24);
        setFont(f);
        setLayout(new BorderLayout());
        
        model = new AMSTableModel(AMSAbstractAction.getAllListeners());
        
        table = new JTable();
        table.setModel(model);
        table.createDefaultColumnsFromModel();
        
        add(JTable.createScrollPaneForTable(table),"Center");
    }

	public AMSTableModel getModel()
	{
		return model;
	}

    public Dimension getPreferredSize()
    {
        return new Dimension(300, 300);
    }   
}
