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

import java.io.Serializable;
import java.util.Vector;
import java.util.Enumeration;
import jade.core.AID;

/**
 * This is the list of the sniffed messages displayed on the Message Canvas as blue arrows.
 * Implements Serializable interface for saving object in the binary snapshot file.
 *
 * @author Gianluca Tanca
 * @version $Date$ $Revision$
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
      try 
      {
		    if( (agentName.equals(mess.getSender().getName())) || (agentName.equals(((AID)mess.getAllReceiver().next()).getName())) )
		    {
		  	 
				  messageVector.removeElement(mess);
				  e=messageVector.elements() ;
		    } 
      }catch (Exception e1) {e1.printStackTrace(); }
			 
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