/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop multi-agent systems in compliance with the FIPA specifications.
Copyright (C) 2000 CSELT S.p.A. 

GNU Lesser General Public License

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation, 
version 2.1 of the License. 

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the
Free Software Foundation, Inc., 59 Temple Place - Suite 330,
Boston, MA  02111-1307, USA.
*****************************************************************/



package jade.tools.rma;

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;
import java.io.*;
import java.util.*;

/**
Javadoc documentation for the file
@author Giovanni Rimassa - Universita` di Parma
@version $Date$ $Revision$
*/
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
