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

import java.awt.event.ActionEvent;
import javax.swing.JFileChooser;
import java.util.Vector;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;


/**  
 * Creates a serialized snapshot of the agents and sniffed messages in both canvases for
 * later recall.
 *
 * @see jade.tools.sniffer.MMAbstractAction
 * @see jade.tools.sniffer.AgentList
 * @see jade.tools.sniffer.MessageList
 *
 * @author Gianluca Tanca
 * @version $Date$ $Revision$
 */

public class WriteLogFileAction extends MMAbstractAction implements Serializable{
	
  public WriteLogFileAction(){
  	
    super ("WriteLogFileActionIcon","Save Snapshot File");
	}
    
	public void actionPerformed (ActionEvent evt){
   	
		try{
		
	   	JFileChooser fileDialog = new JFileChooser();
  	 	int returnVal = fileDialog.showSaveDialog(null); 
				  		  
		  if(returnVal == JFileChooser.APPROVE_OPTION){ 
		  
		  	String fileName = fileDialog.getSelectedFile().getAbsolutePath();
		  
		  	FileOutputStream istream = new FileOutputStream(fileName);
		  	ObjectOutputStream p = new ObjectOutputStream(istream);
		  
		  	p.writeObject(canvasAgent.getAgentList());
		  	p.writeObject(canvasMess.getMessageList()); 
		  
		  	p.close();
				
				System.out.println("Serialized Snapshot File Written.");
		  	
		  }
		} 
		catch (Exception e){
		  System.out.println("Error Writing Snapshot File:" + e);
		}
	}

}