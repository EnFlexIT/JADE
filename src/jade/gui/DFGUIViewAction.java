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
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAException;

/**
@author Giovanni Caire - CSELT S.p.A.
@version $Date$ $Revision$
*/

class DFGUIViewAction extends AbstractAction
{
	private DFGUI gui;
	

	public DFGUIViewAction(DFGUI gui)
	{
		super ("View");
		this.gui = gui;

	}
	
	public void actionPerformed(ActionEvent e) 
	{
		//System.out.println("VIEW");
	    DFAgentDescription dfd = new DFAgentDescription();
	    int kind = gui.kindOfOperation();
		
		if ( kind == DFGUI.AGENT_VIEW || kind == DFGUI.CHILDREN_VIEW || kind == DFGUI.LASTSEARCH_VIEW)
	  {
	  	String name = gui.getSelectedAgentInTable();
	  	if (name != null)
	      try{
	      	if(kind == DFGUI.LASTSEARCH_VIEW)
	      	  dfd = gui.myAgent.getDFAgentSearchDsc(name);
	        else
	  			  dfd = gui.myAgent.getDFAgentDsc(name);
	  		}catch (FIPAException fe){
	  			//System.out.println("WARNING! No agent called " + name + " is currently registered with this DF");
	  			gui.showStatusMsg("WARNING! No description for agent called " + name + " is found");
	  			return;}
	  	else dfd = null;
	  	
	  	}
	  	else
	  	if (kind == DFGUI.PARENT_VIEW)
	  	{
	  	  // In this case the description that will be shown will be the standard description used to federate the df 
	  		dfd = gui.myAgent.getDescriptionOfThisDF();
	  	
	  	}		
	  	
	    if(dfd != null && kind != -1)
	    {
	    	DFAgentDscDlg dlg = new DFAgentDscDlg((Frame) gui);
	    	dlg.viewDFD(dfd);
	    }
	    
		
		
	}
}
	
