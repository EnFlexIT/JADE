package jade.tools.sniffer;

import javax.swing.JFileChooser;
import java.awt.event.ActionEvent;
import java.util.Vector;
import java.io.Serializable;
import java.io.FileInputStream;
import java.io.ObjectInputStream;

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