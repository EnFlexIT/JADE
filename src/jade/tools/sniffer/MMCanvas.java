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


import java.awt.Graphics;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;

import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;

import java.util.Enumeration;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import java.io.Serializable;

import jade.gui.AclGui; 

/**
Javadoc documentation for the file
@author Gianluca Tanca
@version $Date$ $Revision$
*/

/**
 * Manages agents and messages on both canvas. It holds an agent list, a message
 * list and all necessary methods for adding, removing and drawing these object.
 * It also registers ad handles events from mouse
 *
 * @see javax.swing.JPanel
 * @see java.awt.event.MouseListener
 */

public class MMCanvas extends JPanel implements MouseListener, Serializable {
		
  private final static Color blue = new Color(  0,   0, 255);
  private final static Color gray = new Color(128, 128, 128);
  private final static Color white = new Color(255, 255, 255);
  
  /* I due campi successivi sono le tolleranze che si possono rispettare quando si
     fa doppio click sull'icona del messaggio sniffato */
  
  private static final int V_TOL = 4; 
  private static final int H_TOL = 4; 
  private static final int timeUnitWidth = 20; 
  private static final int xOffset = 38;
  
	public static AgentList al = new AgentList();     // this was transient
	public static MessageList ml = new MessageList(); // this was transient


  // State varaiable: must be true for the canvasAgent, false for the canvasMess
	private boolean typeCanv;
    
  //coordinate dei messaggi sul canvas
	private int x1,x2,y;
	private SnifferGUI myGui; 
	private MMCanvas thisCanvas = null;
	private JPanel myPanel; /* To resize and modify the scroll bars */
	private int horDim = 400;
	private int vertDim = 200;
	
	/**
	 * Crates a new canvas: it can be an Agent Canvas or a Message Canvas
	 *
	 * @param type this has to be <em>true</em> for the Agent Canvas and <em>false</em> for the Message Canvas
	 * @param theGui handler to the main windows (for callback purpose)
	 * @param thePanel handler to the panel (Container) in which the canavs is created
	 */	
  public MMCanvas(boolean type, SnifferGUI theGui, JPanel thePanel){
  	
		super();
		typeCanv = type;
		myPanel = thePanel;
		/* Il double buffer sembra essere la causa del problema nel repaint che comunque
		   sotto win32 si manifesta in modo diverso che sotto Sparc/Solaris */
		setDoubleBuffered(false);
		addMouseListener(this);
		myGui = theGui;
		thisCanvas = this;

   	if (typeCanv)
   		setPreferredSize( new Dimension(horDim,50));
   	else
   		setPreferredSize( new Dimension(horDim,vertDim));
		
	}

	
	/**
	 * Default method for custom painting. Every statement that paint something on both 
	 * canvas is included here.
	 *
	 * @param g graphic context
	 */
	public void paintComponent(Graphics g){
		
		super.paintComponent(g);
		
		int yDim = 0;
    int xSource = 0; 
	  int xDest = 0;	
	  
	  //larghezza dei Canvas 
	  int xCanvDim = 0;
	          
		if(typeCanv == true){
			   
		  /* This is the Agent Canvas: so let's paint the rectangles with agent
		     name inside */		    
			for(Enumeration e=al.agentVector.elements() ; e.hasMoreElements();){
				
				Agent agent = (Agent)e.nextElement();
			  
    		int x = Agent.yRet+(xCanvDim++)*80;

				if(agent.onCanv == false){
					g.setColor(Agent.color[Agent.DUMMY]);
				}
				else if(agent.getStateName() == Agent.statesNames[Agent.RUNNING]){
					g.setColor(Agent.color[1]);
				}
				else if(agent.getStateName() == Agent.statesNames[Agent.SUSPENDED]){
					g.setColor(Agent.color[agent.currentState]);
				}
		
				g.draw3DRect(x,Agent.yRet,Agent.bRet,Agent.hRet,true); 
				g.fill3DRect(x,Agent.yRet,Agent.bRet,Agent.hRet,true); 
            
				g.setColor(Color.black);
	  		FontMetrics fm = g.getFontMetrics();
				g.drawString(agent.name,x+(Agent.bRet-fm.stringWidth(agent.name))/2,Agent.yRet+(Agent.hRet/2) + (fm.getAscent()/2));
			  
			}
      
      horDim = 100+(xCanvDim*80);	
			
		}

    if((typeCanv == false)) {
    	
    	/* This is the Message Canvas: so let's paint all the messages */
      
      int x1,x2,y;
			int xCoords[] = new int[3];
			int yCoords[] = new int[3];
     
			xCanvDim = al.agentVector.size();
		  for(Enumeration e=ml.messageVector.elements() ; e.hasMoreElements();){
		  	
			    Message mess = (Message)e.nextElement();				
			    xSource = al.getPos(mess.getSource());
			    xDest = al.getPos(mess.getFirstDest());

   				x1 = mess.getInitSeg(xSource); 
					x2 = mess.getEndSeg(xDest);  
    			y = mess.getOrdSeg(yDim++); 

					/* Were we fill the coordinate array for the arrow tip */
	  			xCoords[0] = x2-6;
					xCoords[1] = x2-6;
					xCoords[2] = x2+2;
    			yCoords[0] = y-5;
					yCoords[1] = y+5;
					yCoords[2] = y;
		
					if (x1 > x2){
						xCoords[0] = x2+10;
		  			xCoords[1] = x2+10;
		  			xCoords[2] = x2+2;
   				}   	

					g.setColor(Color.blue);
        
        	g.drawRect(x1-3,y-4,4,8);
        	g.fillRect(x1-3,y-4,4,8);
        
	  			//disegno segmento messaggio
					for(int k=-1; k<=1; k++){
						if (x2 > x1)
							g.drawLine(x1,y+k,x2,y+k);
						else
							g.drawLine(x1,y+k,x2+4,y+k);
    			}

	  			//disegno freccetta del receiver
					g.drawPolygon(xCoords,yCoords,3);
					g.fillPolygon(xCoords,yCoords,3);

                
			}
			
			
			int msgNum = ml.messageVector.size();
			
			for (int num = 0; num < xCanvDim; num++) {
				// Here we update the green lines of the timeline
				int x =  jade.tools.sniffer.Agent.yRet/2+num*80;
				g.setColor(new Color(0,100,50));				
				g.drawLine(x+xOffset,1,x+xOffset,timeUnitWidth*(msgNum+1));
			}
			
			g.setColor(new Color(150,50,50));				
			
			Integer msgNumWrapped;
			for (int t=0; t <= msgNum; t++) {
				// Here we update the red numbers of the timeline
				msgNumWrapped = new Integer(t);
				g.drawString(msgNumWrapped.toString(),10,timeUnitWidth*(t)+15);
			}
			
      
      	horDim = 100+(xCanvDim*80);
      
      	vertDim = 100+(yDim*20);	
      
		}
	}
    

  /**
   * Method invoked everytime the use clicks on a blue arrow: it updates the TextMessage
   * component displaying the type of the message.
   *
   * @param evt mouse event
   */
	public void mousePressed(MouseEvent evt){
		
	  Message mess;
		String info;
		
   	if( ((mess = selMessage(evt)) != null) && (typeCanv == false)){
   		
			info = "Message Type: "+mess.getType();
   			MMAbstractAction.text.setText(info);
		}
	}
    
  /**
   * This method is invoked every time a user double-click on an agent on the agent 
   * canvas or on a blue arrow in the message canvas: if the double-click event occurs
   * on an agent, this one is removed from the agent canvas; if the double-click occurs
   * on a blue arrow in the message canavs, a dialog box is displayed with the entire 
   * message.
   *
   * @param evt mouse event
   */
	public void mouseClicked(MouseEvent evt){   
		Agent ag;
		Message mess;
		String info;
		if(evt.getClickCount() == 2){
			
   		if( ((mess = selMessage(evt)) != null) && (typeCanv == false)){  
			   AclGui.showMsgInDialog(mess,myGui);
			}
		
	    if( ((ag = selAgent(evt)) != null) && (typeCanv == true)){
	    	   
			  //selezione di un agente
  			removeAgent(ag.name);
			}
		}
	}

	public void mouseEntered(MouseEvent evt) {}
	public void mouseExited(MouseEvent evt) {}	
	public void mouseReleased(MouseEvent evt) {}

  /** 
   * Returns a Message if a message has been selected form the user, otherwise
   * returns null. 
   * 
   * @param evt mouse event
   * @return Message selected or null if no Message was selected
   */
  public Message selMessage(MouseEvent evt){
  	
		int j = 0;

		for(Enumeration e=ml.messageVector.elements() ; e.hasMoreElements();){   
			
			Message mess = (Message)e.nextElement();
            
			x1 = mess.getInitSeg(al.getPos(mess.getSource()));
			x2 = mess.getEndSeg(al.getPos(mess.getFirstDest()));
   		y = mess.getOrdSeg(j++);
		    
			if(x1 < x2){
		        if((evt.getX() >= x1+H_TOL) && (evt.getX() <= x2+H_TOL) 
			    		&& (evt.getY() >= y - V_TOL) && (evt.getY() <= y + V_TOL)){
			    			return mess;
						}
			}
			else {
				if((evt.getX() >= x2 - H_TOL) && (evt.getX() <= x1 + H_TOL) 
			    && (evt.getY() >= y - V_TOL) && (evt.getY() <= y + V_TOL)){
			    	return mess;
				}
			}
		}
		return null;
	}

  /** 
   * Returns an Agent if an Agent has been selected form the user, otherwise
   * returns null. 
   * 
   * @param evt mouse event
   * @return Agent selected or null if no Agent was selected
   */
	public Agent selAgent(MouseEvent evt){
		int j = 0;
        int y1 = Agent.yRet;
		int y2 = y1 + Agent.yRet;

		for(Enumeration e=al.agentVector.elements() ; e.hasMoreElements();){   
			
			Agent ag = (Agent)e.nextElement();

			x1 = Agent.yRet + j*80;
			x2 = x1 + Agent.bRet;
			
			
		  	if((evt.getX() >= x1) && (evt.getX() <= x2) 
			    && (evt.getY() >= y1) && (evt.getY() <= y2)){
			    	return ag;
				}
			j++;
		}
		return null;
	}
	
	
	/*private void safePaint(){
			
		final Graphics myGraphics = thisCanvas.getGraphics();
				
   	Runnable updateThisCanvas = new Runnable() { 
   		public void run() { 
   			thisCanvas.paint(myGraphics); 
   		}
		};
		
		SwingUtilities.invokeLater(updateThisCanvas);
		
	}*/
	
	/**
	 * This method repaint both canvas checking the size of the scrollbars. The
	 * right procedure to follow is to call method setPreferredSize() the revalidate()
	 * method. 
	 */
	private void repaintBothCanvas(){
		MMCanvas c1 = MMAbstractAction.getMMCanvasAgent();
		MMCanvas c2 = MMAbstractAction.getMMCanvasMess();

		myPanel.setPreferredSize(new Dimension(horDim,vertDim+50));
		c1.setPreferredSize(new Dimension(horDim,50));		
    c2.setPreferredSize(new Dimension(horDim,vertDim));
		myPanel.revalidate();

		c1.repaint();
		c2.repaint();		
	}


  /** 
   * Adds an agent to canvas agent then repaints it
   *
   * @param agent agent to be add
   */
	public void addAgent (Agent agent){
		
		al.addAgent(agent);
		repaintBothCanvas();
		
	}

  /** 
   * Removes an agent from the canvas agent then repaints it
   *
   * @param agentName agent to be removed
   */
	public void removeAgent (String agentName){   
		try{
			
	      al.removeAgent(agentName);
		    repaintBothCanvas();
		}
		catch(Exception e) {}
	}

  /** 
   * Removes all the agents and messages from their lists then repaints the canvas
   */
	public void removeAllAgents (){
		try{
			
		  al.removeAllAgents();
		  ml.removeAllMessages();
			repaintBothCanvas();
				
		}
		catch(Exception e) {}
	}
  
  /** 
   * Adds a message to canvas message then repaints the canvas
   * 
   * @param mess message to be added 
   */
	public void addMessage (Message mess){
		
		ml.addMessage(mess);
		repaintBothCanvas();
	}

  /** 
   * Removes all the messages in the message list then repaints the canvas
   */
	public void removeAllMessages(){
		
		try{
		    ml.removeAllMessages();
		    repaintBothCanvas();
		}
		catch (Exception e) {}
	}

  /** 
   * Looks if an agent is present on Agent Canvas 
   *
   * @param agName agent name to look for
   * @return true if agent is present, false otherwise
   */
	public boolean isPresent (String agName){
		
		return al.isPresent(agName);
	}

  /**
   * Returns an handler to the agent list. The agent list contains all the agents 
   * contained in the Agent Canvas displayed by grey or red boxes
   *
   * @return handler to agent list
   */
	public AgentList getAgentList(){
		
		return al;
	}

  /**
   * Returns an handler to the message list. The message list contains all
   * sniffed messages displayed on the Message Canavs as blue arrows
   *
   * @return handler to the message list
   */
	public MessageList getMessageList(){
		return ml;
	}

  /**
   * Set the agent list handler as the parameter passed then repaints the canvas
   * 
   * @param savedList new list of agents
   */
	public void setAgentList(AgentList savedList){		
		al = savedList;
		repaintBothCanvas();
	}

  /**
   * Set the message list handler as the parameter passed then repaints the canvas
   * 
   * @param savedList new list of messages
   */
	public void setMessageList(MessageList savedList){
		
		ml = savedList;
		repaintBothCanvas();
	}	

  /** 
   * Returns new messages and put them into canvas agent 
   *
   * @param newMess new message
   */ 
	public void recMessage(Message newMess){
		
	    addMessage(newMess); 
	}
}