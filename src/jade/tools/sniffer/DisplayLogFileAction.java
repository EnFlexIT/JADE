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



package jade.tools.sniffer;

import javax.swing.JFileChooser;
import java.awt.event.ActionEvent;
import java.util.Vector;
import java.io.Serializable;
import java.io.FileInputStream;
import java.io.ObjectInputStream;

/**
@author Gianluca Tanca
@version $Date$ $Revision$
*/
/** 
 * Class for loading a snapshot file in Serialized form.
 * It loads from the stream an AgentList object and a MessageList object.
 *   
 * @see jade.Sniffer.MMAbstractAction
 * @see jade.Sniffer.AgentList
 * @see jade.Sniffer.MessageList
 */
 
public class DisplayLogFileAction extends MMAbstractAction{
	
  public DisplayLogFileAction(){
  	
    super ("DisplayLogFileActionIcon","Open Snapshot File");
	}
    
	public void actionPerformed (ActionEvent evt){
		
		try{
			
			JFileChooser fileDialog = new JFileChooser();
  	 	int returnVal = fileDialog.showOpenDialog(null); 
			
		  if(returnVal == JFileChooser.APPROVE_OPTION){ 
		  
		  	String fileName = fileDialog.getSelectedFile().getAbsolutePath();
			
				FileInputStream istream = new FileInputStream(fileName);
		  
		  	ObjectInputStream p = new ObjectInputStream(istream);
		      
				canvasAgent.setAgentList((AgentList)p.readObject());
				canvasMess.setMessageList((MessageList)p.readObject());
            
				p.close();
				System.out.println("Snapshot File Read.");
			}
		}
		catch (Exception e){
		    System.out.println("Error Reading Snapshot File" + e);
		}
	}
}