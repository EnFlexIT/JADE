/*
  $Log$
  Revision 1.1  1999/05/20 15:42:08  rimassa
  Moved RMA agent from jade.domain package to jade.tools.rma package.

  Revision 1.4  1999/02/04 14:47:25  rimassa
  Changed package specification for Swing: now it's 'javax.swing' and no more
  'com.sun.swing'.

  Revision 1.3  1998/10/10 19:37:08  rimassa
  Imported a newer version of JADE GUI from Fabio.

  Revision 1.2  1998/10/04 18:01:41  rimassa
  Added a 'Log:' field to every source file.
*/

package jade.tools.rma;

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;
import java.io.*;
import java.util.*;

/**
 * The model of the table
 */
public class AMSTableModel extends AbstractTableModel
{
	protected Vector agents;
	protected static int ColNumber = 3;

	public AMSTableModel ()
	{	
	}
	
	public AMSTableModel(Vector agents)
	{
	    this.agents = agents;
	}
	
	public int getRowCount()
	{
	    return agents.size();
	}
	
	public int getColumnCount()
	{
	    return ColNumber;
	}
	
	public String getColumnName(int columnIndex)
	{
	    String retVal = "";
	    
	    switch(columnIndex)
	    {
	        case 0:
	            retVal = "agent-name";
	            break;
	        case 1:
	            retVal = "agent-addresses";
	            break;
	        case 2:
	            retVal = "agent-type";
	            break;
	        default:
	            retVal = "default";
	            break;
	    }
	    
	    return retVal;
	}
	
	public Class getColumnClass(int columnIndex)
	{
	    return String.class;
	}
	
	public boolean isCellEditable(int rowIndex,
                                       int columnIndex)
    {
        boolean retVal = false;
        
        if(columnIndex == 0) retVal = true;
        
        return retVal;
    }
    
	public Object getValueAt(int rowIndex,
                                   int columnIndex)
    {
        Object retVal=null;
		TreeData current = null;
        
        if(rowIndex < agents.size())
        {
            current = (TreeData)agents.elementAt(rowIndex);
        }
        
        if(current == null) return "";
        
        if(columnIndex == 0)
        {
            retVal = current.getName();
        }
        else if(columnIndex == 1)
        {
            retVal = current.getAddressesAsString();
        }
        else if(columnIndex == 2)
        {
            retVal = current.getType();
        }
        else
        {
            retVal = current;
        }
        
        return retVal;
    }

	public void setAgents(Vector agents)
	{
		this.agents = agents;
	}
	
	public void setValueAt(Object aValue,
                                 int rowIndex,
                                 int columnIndex)
    {
		fireTableCellUpdated(rowIndex,columnIndex);
    }
}
