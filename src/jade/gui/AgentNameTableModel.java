/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop 
multi-agent systems in compliance with the FIPA specifications.
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

package jade.gui;

// Import required Java classes 
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import java.util.*;

// Import required JADE classes
import jade.domain.*;
import jade.core.AID;

/**
@author Giovanni Caire Adriana Quinto - CSELT S.p.A.
@version $Date$ $Revision$
*/

class AgentNameTableModel extends AbstractTableModel 
{
	Vector names;

	// CONSTRUCTORS
	public AgentNameTableModel() 
	{
		super();
		names = new Vector();
	}

	// ADD
	public void add(AID name)
	{
		names.add(name);
	}
	//REMOVE
	public void remove(AID name)
	{
		names.remove(name);
	}
	
	// GETELEMENTAT
	public AID getElementAt(int index)
	{
		return((AID) names.get(index));
	}

	// CLEAR
	public void clear()
	{
		names.clear();
	}

	// Methods to be implemented to have a concrete class
	public int getRowCount()
	{
		return(names.size());
	}

	public int getColumnCount()
	{
		return(3);
	}

	public Object getValueAt(int row, int column)
	{
		return names.elementAt(row);
		
		/*String value, completeName, localName, address;
		value = new String("");
		completeName = (String) names.get(row);
    		int atPos = completeName.indexOf('@');
    		if(atPos != -1)
		{ 
			localName = completeName.substring(0, atPos);
			address = completeName.substring(atPos + 1);
		}
		else
		{
			localName = completeName;
			address = null;
		}
		switch(column)
		{
		case 0:
			value = new String(localName);
			break;
		case 1:
			if (address != null)
			{
			    value = "STUB";
			}
			break;
		case 2:
			if (address != null)
				value = new String(address);
			break;
		}
		return ((Object) value);*/	
	}
}