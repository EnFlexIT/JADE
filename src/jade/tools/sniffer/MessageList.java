package jade.tools.sniffer;

import java.io.Serializable;
import java.util.Vector;
import java.util.Enumeration;

/**
 * This is the list of the sniffed messages displayed on the Message Canvas as blue arrows.
 * Implements Serializable interface for saving object in the binary snapshot file.
 *
 */
public class MessageList implements Serializable{
	
  public Vector messageVector;
	
	public MessageList(){
		   
		messageVector = new Vector(50);
	}
    
	
  /**
   * Adds a sniffed message to the list.
   *
   * @param mess sniffed message to put in the vector
   */ 
	public void addMessage(Message mess){
		
		messageVector.addElement(mess);
	}
	
	/**
	 * Removes a message from the vector
	 *
	 * @param agentName name of the agent to remove from the vector
	 */
	public void removeMessages(String agentName){   
		
  	for(Enumeration e=messageVector.elements() ; e.hasMoreElements() ;){
  		
      Message mess = (Message)e.nextElement();
		  if( (agentName.equals(mess.getSource())) || (agentName.equals(mess.getFirstDest())) ){
		  	 
				messageVector.removeElement(mess);
			    e=messageVector.elements() ;
			}
		}
  }

  /**
   * Clear all messages in the vector.
   */
  public void removeAllMessages(){
  	
		messageVector.removeAllElements();
	}

  /**
   * Returns the messages vector
   */
	public Vector getMessagesVector(){ 
		return messageVector;
	}

}