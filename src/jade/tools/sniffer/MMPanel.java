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



package jade.tools.sniffer;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Color;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
Javadoc documentation for the file
@author Gianluca Tanca
@version $Date$ $Revision$
*/
/**
 * Encapsulates both the Agent Canavs and the Message Canvas in one JPanel
 * 
 * @see javax.swing.JPanel
 */

public class MMPanel extends JPanel{
	
		private SnifferGUI myGui; 
	
    public MMPanel(SnifferGUI snifferGui){ 
		myGui = snifferGui;
		  
		MMCanvas canvAgent,canvMess;
		JScrollPane scrollAgent,scrollMess;
		
		GridBagConstraints gbc;
		setLayout(new GridBagLayout());
   		
		gbc = new GridBagConstraints();
    gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.gridheight = 1;
		//gbc.anchor = GridBagConstraints.CENTER;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.weightx = 0.5;
		gbc.weighty = 0; // c'era 0
		gbc.fill = GridBagConstraints.BOTH;

      
	  //E' il canvas per gli agenti
		canvAgent = new MMCanvas(true,myGui,this);
    MMAbstractAction.setMMCanvasAgent(canvAgent);
		
		add(canvAgent,gbc);
		
		gbc = new GridBagConstraints();
    gbc.gridx = 0;
		gbc.gridy = 1; // c'era 1
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.gridheight = 100;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		// gbc.fill = GridBagConstraints.VERTICAL;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 0.5;
		gbc.weighty = 1;
   	  
	  //E' il canvas per i messaggi
		canvMess = new MMCanvas(false,myGui,this);
		MMAbstractAction.setMMCanvasMess(canvMess);
		add(canvMess,gbc);
		
	}

}