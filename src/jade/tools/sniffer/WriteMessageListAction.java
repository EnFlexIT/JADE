package jade.tools.sniffer;

import java.awt.event.ActionEvent;
import javax.swing.JFileChooser;

import java.util.Vector;

import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.Serializable;

/**
 * Writes a text file with all sniffed messages showed on the Message Canvas. A dialog box
 * asks the user the name of the file.
 *
 * @author <a href="mailto:alessandro.beneventi@re.nettuno.it"> Alessandro Beneventi </a>
 * 
 * @see jade.Sniffer.MMAbstractAction
 * @see jade.Sniffer.AgentList
 * @see jade.Sniffer.MessageList
 */
public class WriteMessageListAction extends MMAbstractAction implements Serializable {
	private PrintWriter out;
	
  public WriteMessageListAction(){
  	super ("MessageFileActionIcon","Write Message List File");
	}
    
	public void actionPerformed (ActionEvent evt){
       
		try{
		  
	   	JFileChooser fileDialog = new JFileChooser();
  	 	int returnVal = fileDialog.showSaveDialog(null); 
		  
		  if(returnVal == JFileChooser.APPROVE_OPTION){ 
		  
		  	String fileName = fileDialog.getSelectedFile().getAbsolutePath();
		  		  
  	 		out = new PrintWriter(new BufferedWriter(new FileWriter(fileName)));
		  
				MessageList msgList = canvasMess.getMessageList();
				Vector messages = msgList.getMessagesVector();
				
				for (int i = 0; i < messages.size(); i++) {
					Message curMsg = (Message)messages.elementAt(i);
					out.println(curMsg.toString());
				}
				
				out.close();
				
				System.out.println("Message List File Written.");
		  	
		  }
		} 
		catch (Exception e){
		  System.out.println("Error Writing List File:" + e);
		}
	}

}