package jade.gui;

import com.sun.java.swing.*;
import com.sun.java.swing.table.*;
import com.sun.java.swing.event.*;
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