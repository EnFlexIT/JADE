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

// Import required Jade classes
import jade.domain.AgentManagementOntology;
import jade.domain.FIPAException;

/**
Javadoc documentation for the file
@author Giovanni Caire - CSELT S.p.A
@version $Date$ $Revision$
*/

class DFGUIModifyAction extends AbstractAction
{
	private DFGUI gui;

	public DFGUIModifyAction(DFGUI gui)
	{
		super ("Modify");
		this.gui = gui;
	}
	
	public void actionPerformed(ActionEvent e) 
	{
		//System.out.println("MODIFY");
		int i = gui.registeredTable.getSelectedRow();
		if (i != -1)
		{
			AgentManagementOntology.DFAgentDescriptor dfd;
			String name = gui.registeredModel.getElementAt(i);
			try
			{
				dfd = gui.myAgent.getDFAgentDsc(name);
			}
			catch (FIPAException fe)
			{
				System.out.println("WARNING! No agent called " + name + " is currently resistered with this DF");
				return;
			}
			DFAgentDscDlg dlg = new DFAgentDscDlg((Frame) gui);
			AgentManagementOntology.DFAgentDescriptor editedDfd = dlg.editDFD(dfd);
			if (editedDfd != null)
			{
				gui.myAgent.postModifyEvent((Object) gui, gui.myAgent.getName(), editedDfd);
			}
		}
	}
}
	
