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
import java.util.Enumeration;
import java.util.Vector;

// Import required Jade classes
import jade.domain.AgentManagementOntology;
import jade.domain.FIPAException;

/**
@author Tiziana Trucco - CSELT S.p.A.
@version $Date$ $Revision$
*/

class DFGUISearchWithConstraintAction extends AbstractAction
{
	private DFGUI gui;

	public DFGUISearchWithConstraintAction(DFGUI gui)
	{
		super ("Search With Constraints");
		this.gui = gui;
	}
	
	public void actionPerformed(ActionEvent e) 
	{
		AgentManagementOntology.DFAgentDescriptor dfd = new AgentManagementOntology.DFAgentDescriptor ();
		
		String df = gui.myAgent.getLocalName();
		int kind = gui.kindOfOperation(); // to discriminate between the view
	

		if(kind == DFGUI.PARENT_VIEW) // search on a parent
		{
			String name = gui.getSelectedAgentInTable();
			if(name != null)
				df = name;
		}
		else
		if(kind == DFGUI.CHILDREN_VIEW) // search on a child
		{
			String name = gui.getSelectedAgentInTable();
			if(name != null)
				df = name;
		}
		
		ConstraintDlg constraint = new ConstraintDlg((Frame)gui);
		
	  Vector out = constraint.viewConstraint(gui.myAgent.getConstraints());
	  
	 
	  if ( out == null)
	  	// cancel button pressed 
	  	return;
	  else
	  { // ok button pressed
	  	DFAgentDscDlg dlg = new DFAgentDscDlg((Frame) gui);
	  	AgentManagementOntology.DFAgentDescriptor editedDfd = dlg.editDFD(null);
	  	
	  	if (editedDfd != null)
	  		gui.myAgent.postSearchWithConstraintEvent((Object) gui,df, editedDfd,out);
	  	
	    gui.setTab("Search");
	
	  }
	}
}
	