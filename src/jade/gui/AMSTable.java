/*
  $Log$
  Revision 1.2  1998/10/04 18:01:43  rimassa
  Added a 'Log:' field to every source file.

*/

package jade.gui;

import com.sun.java.swing.*;
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
