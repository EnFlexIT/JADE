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
 * @see jade.Sniffer.MMAbstractAction
 * @see jade.Sniffer.AgentList
 * @see jade.Sniffer.MessageList
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