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

import javax.swing.AbstractAction;
import javax.swing.JScrollPane;
import javax.swing.Action;
import javax.swing.Icon;
import java.awt.event.ActionEvent;
import java.util.Vector;

/**
Javadoc documentation for the file
@author Gianluca Tanca
@version $Date$ $Revision$
*/
/** 
 * MMAbstractAction is the superclass of the actions 
 * performed by Sniffer GUI controls.
 *	
 * This class is abstract because it does not define the
 * ActionPerformed(ActionEvent evt) method. In every subClass of 
 * MMAbstractAction this method performs a specific action and updates 
 * the Sniffer GUI.
 *
 * Subclasses of MMAbstractAction are:
 * @see  jade.tools.sniffer.AboutBoxAction 
 * @see  jade.tools.sniffer.AddRemoveAgentAction 
 * @see  jade.tools.sniffer.ClearCanvasAction 
 * @see  jade.tools.sniffer.ExitAction 
 * @see  jade.tools.sniffer.WriteLogFileAction 
 * @see  jade.tools.sniffer.DisplayLogFileAction
 * @see  jade.tools.sniffer.WriteMessageListAction 
 */
public abstract class MMAbstractAction extends AbstractAction{
	/** Handler to the action icon */
	protected Icon img;
	/** Handler to the name of the action */
	protected String ActionName = "Action";
	/** Handler to the listeners vector */
	protected static Vector listeners;

  /** Handler to the Agent Canvas */
	protected static MMCanvas canvasAgent = null;
  /** Handler to the Message Canvas */	
	protected static MMCanvas canvasMess = null;
	protected static JScrollPane scrollAgent = null;
	protected static JScrollPane scrollMess = null;
  protected static MMTextMessage text = null;
  /** Handler to the Selection Agents frame */
	protected static AgentFrame selFrame = new AgentFrame("Selection Agents");
	/** Handler to the Selection Tree insidethe Selection Agents window */
  protected static MMTree selTree = new MMTree();

  /**
   * Constructor for creating a new action providing the name of the action, the path
   * of the relative icon and a listeners vector. The icon name is searched within the 
   * icon loaded from the GuiProperties class
   *
   * @see jade.tools.sniffer.GuiProperties
   * @param IconPath name of the icon representing the action
   * @param ActionName name of the action
   * @param listeners listeners vector
   */	
	public MMAbstractAction(String IconKey,String ActionName, Vector listeners){
		
		this.img = GuiProperties.getIcon("MMAbstractAction."+IconKey);
		this.ActionName = ActionName;
		this.listeners = listeners;

    putValue(Action.SMALL_ICON,img);
    putValue(Action.DEFAULT,img);
    putValue(Action.NAME,ActionName);
   }
	
  /**
   * Constructor for creating a new action providing the name of the action and the path
   * of the relative icon. The icon name is searched within the icon loaded from the 
   * GuiProperties class
   *
   * @see jade.tools.sniffer.GuiProperties
   * @param IconPath name of the icon representing the action
   * @param ActionName name of the action
   */	
	public MMAbstractAction (String IconPath,String ActionName){
		
		this(IconPath,ActionName,new Vector());
	}

  /**
   * Constructor for creating a new action providing the name of the action
   *
   * @param ActionName name of the action
   */
	public MMAbstractAction (String ActionName){
		
		this.ActionName = ActionName;
		this.listeners = listeners;
    putValue(Action.NAME,ActionName);		
	}

  /**
   * Returns the name of the current action
   *
   * @return name of the action
   */
	public String getActionName (){
		
		return ActionName;
	}

  /**
   * Clears the content of the listeners vector then adds the provided array to the 
   * mentioned vector
   *
   * @param listenersP array of objects to be inserted in the listeners vector
   */
	public synchronized static void setListeners(Object[] listenersP){
		
		listeners.removeAllElements();
		for (int i=0;i<listenersP.length;i++)
			listeners.addElement(listenersP[i]);
	}

  /**
   * Clears all the listeners in the relative vector
   */
	public synchronized static void removeAllListeners(){
		
		listeners.removeAllElements();
	}

  /**
   * Adds an item to the listeners vector
   *
   * @param current handle to the object to be put in the listeners vector
   */
	public synchronized static void AddListener(Object current){
		
		listeners.addElement(current);
	}

  /**
   * Removes an item from the listeners vector
   *
   * @param current handle to the object to be removed from the listeners vector
   */
	public synchronized static void removeListener (Object current){
		
		listeners.removeElement (current);
	}

  /**
   * Returns a handler to the vector containing all the registered listeners
   *
   * @return handler to the listeners vector
   */
	public synchronized static Vector getAllListeners(){
		
		return listeners;
	}

  /**
   * Returns the last of the registered listeners for an action. The listeners are the
   * selected agents in the selection tree in the Selection Agents window
   *
   * @return handle to the last registered listeners (use casting)
   */
	public synchronized static Object getLast(){
		
		return listeners.lastElement();
	}

  /**
   * Returns the first of the registered listeners for an action. The listeners are the
   * selected agents in the selection tree in the Selection Agents window
   *
   * @return handle to the first registered listeners (use casting)
   */
	public synchronized static Object getFirst(){
		
		return listeners.firstElement();
	}

  /**
   * Returns a handle to the agent names selection tree
   *
   * @return handle to the selection tree
   */
	protected static MMTree getTheTree(){
		return selTree;
	}

  /** 
   * Sets a handle to Agent Canvas
   *
   * @param MyCanvas handle to Agent Canvas
   */     
  public static void setMMCanvasAgent (MMCanvas MyCanvas){
  	
		canvasAgent = MyCanvas;
	}

  /** 
   * Sets a handle to Message Canvas
   *
   * @param MyCanvas handle to Message Canvas
   */     
	public static void setMMCanvasMess (MMCanvas MyCanvas){
		
		canvasMess = MyCanvas;
	}
	
  /** 
   * Returns a handle to Agent Canvas
   *
   * @return handle to Canvas Agent
   */  
	public static MMCanvas getMMCanvasAgent (){
		
		return canvasAgent;
	}

  /** 
   * Returns a handle to Message Canvas
   *
   * @return handle to Message Canvas
   */  
	public static MMCanvas getMMCanvasMess (){
		
		return canvasMess;
	}

  /** 
   * Sets a handle to scrollPane of canvasAgent
   * 
   * @param scroll handle to scrollPane of agent canvas 
   */     
	public static void setMMScrollAgent (JScrollPane scroll){
		
	    scrollAgent = scroll;
	}

  /** 
   * Sets a handle to scrollPane of canvasMessage
   *
   * @param scroll handle to scrollPane of the message canvas 
   */    
	public static void setMMScrollMess (JScrollPane scroll){
		
        scrollMess = scroll;
	}
     
  /** 
   * Set a handle to textMessage bar
   *
   * @param MyText handle to text message bar 
   */    
  public static void setMMTextMessage(MMTextMessage MyText){
  	 
		text = MyText;
	}

}