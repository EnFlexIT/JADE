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

import java.awt.Graphics;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;

import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseEvent;

import java.io.Serializable;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.ConcurrentModificationException;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import jade.core.AID;

import jade.gui.AclGui;

import jade.lang.acl.ACLMessage;

/**
   Javadoc documentation for the file
   @author Francisco Regi, Andrea Soracchi - Universita` di Parma
   <Br>
   <a href="mailto:a_soracchi@libero.it"> Andrea Soracchi(e-mail) </a>
   @version $Date$ $Revision$
   @version $Date$ Modified by RR Kessler and ML Griss
   @version $Date$ Mofified by ML Griss - 3 line folding
 */

 /**
  * Manages agents and messages on both canvas. It holds an agent list, a message
  * list and all necessary methods for adding, removing and drawing these object.
  * It also registers ad handles events from mouse
  *
  * @see javax.swing.JPanel
  * @see java.awt.event.MouseListener
 */

public class MMCanvas extends JPanel implements MouseListener, MouseMotionListener, Serializable {

  private static final int V_TOL = 4;
  private static final int H_TOL = 4;
  private static final int timeUnitWidth = 20;
  private static final int xOffset = 38;
  private int positionAgent=0;


  private int x1,x2,y;
  private MainWindow mWnd;
  private PanelCanvas panCan; /* To resize and modify the scroll bars */
  private MainPanel mPan;
  private int horDim = 400;
  private int vertDim = 200;
  private boolean typeCanv;
  private boolean nameShown = false;
  private List noSniffAgents=new ArrayList();
  private Font font1 = new Font("Helvetica",Font.ITALIC,12);
  private Font font2 = new Font("SanSerif",Font.BOLD,12);
  // font3 is used to display the name of the performative above the messages.
  // Needed something a bit smaller than 1 or 2 above so it isn't too obtrusive.
  private Font font3 = new Font("SanSerif", Font.PLAIN, 10);
  private MMCanvas otherCanv;
  public AgentList al;
  public MessageList ml;
  
  // These vars are used to make messages grouped by conversationID appear as the
  // same color.  It makes it easier to pick out various conversations.
  private HashMap mapToColor = new HashMap();
  // Removed green, orange, and pink.  They were too hard to see.
  private Color colorTable[] = {Color.blue, Color.black, Color.cyan, 
  Color.magenta, Color.red, Color.white, Color.yellow};
  private Integer colorCounter = new Integer(-1);
  
  public MMCanvas(boolean type,MainWindow mWnd, PanelCanvas panCan, MainPanel mPan, MMCanvas other ) {
   super();
   otherCanv=other;
   typeCanv=type;
   al=new AgentList();
   ml=new MessageList();
   this.panCan = panCan;
   setDoubleBuffered(false);
   addMouseListener(this);
   addMouseMotionListener(this);
   this.mWnd = mWnd;
   this.mPan=mPan;

   if (typeCanv)
     setPreferredSize( new Dimension(horDim,50));
   else
     setPreferredSize( new Dimension(horDim,vertDim));
   }

  // drawing is all here

  public void paintComponent(Graphics g) {

   super.paintComponent(g);

   int yDim = 0;
   int xSource = 0;
   int xDest = 0;

   int xCanvDim = 0;
   try {
     if(typeCanv == true) {
 
       Iterator it = al.getAgents();
       while(it.hasNext()) {

         Agent agent = (Agent)it.next();

         int x = Agent.yRet+(xCanvDim++)*80;

         if(agent.onCanv == false) g.setColor(Color.gray);
         else g.setColor(Color.red);

         if(checkNoSniffedVector(agent)) g.setColor(Color.yellow);

         g.draw3DRect(x,Agent.yRet,Agent.bRet,Agent.hRet,true);
         g.fill3DRect(x,Agent.yRet,Agent.bRet,Agent.hRet,true);

         g.setColor(Color.black);
         FontMetrics fm = g.getFontMetrics();
         String aName=agent.agentName;
         aName=nameClip(aName);
         int nameWidth = fm.stringWidth(aName);
         if (nameWidth < Agent.bRet) {
            g.drawString(aName,x+(Agent.bRet-nameWidth)/2,Agent.yRet+(Agent.hRet/2) + (fm.getAscent()/2));
         } else {
           // Need to chop the string up into at most 2 or 3 pieces, truncating the rest.
           int len = aName.length();
           String part1;
           String part2;
           String part3;
           if (nameWidth < Agent.bRet * 2) {
               // Ok, it is not quite twice as big, so cut in half
               part1 = aName.substring(0, len/2);
               part2 = aName.substring(len/2);
               g.drawString(part1, x+(Agent.bRet-fm.stringWidth(part1))/2,Agent.yRet+(Agent.hRet/2) - (int)(fm.getAscent() * 0.2));
               g.drawString(part2, x+(Agent.bRet-fm.stringWidth(part2))/2,Agent.yRet+(Agent.hRet/2) + (int)(fm.getAscent() * 0.9));

           } else if (nameWidth < Agent.bRet * 3) {
               // Ok, it is not quite thrice as big, so cut in three
               part1 = aName.substring(0, len/3);
               part2 = aName.substring(len/3, 2*len/3);
               part3 = aName.substring(2*len/3);
               g.drawString(part1, x+(Agent.bRet-fm.stringWidth(part1))/2,Agent.yRet+(Agent.hRet/2) - (int)(fm.getAscent() * 0.65));
               g.drawString(part2, x+(Agent.bRet-fm.stringWidth(part2))/2,Agent.yRet+(Agent.hRet/2) + (int)(fm.getAscent() * 0.3));
               g.drawString(part3, x+(Agent.bRet-fm.stringWidth(part3))/2,Agent.yRet+(Agent.hRet/2) + (int)(fm.getAscent() * 0.95));

           } else {
               // This is rounded down the size of each char.
               int approxCharWidth = nameWidth / agent.agentName.length();
               int charCount = Agent.bRet / approxCharWidth;
               part1 = aName.substring(0, charCount);
               if (aName.length() < (charCount * 2) ) {
                   part2 = aName.substring(charCount);
                   part3 = "";
               } else {
                   part2 = aName.substring(charCount, (charCount * 2));
                   if (charCount * 3 > aName.length()) {
                       part3 = aName.substring(charCount * 2);
                   } else {
                    part3 = aName.substring(charCount*2, (charCount * 3));
                   }
               }
               g.drawString(part1, x+(Agent.bRet-fm.stringWidth(part1))/2,Agent.yRet+(Agent.hRet/2) - (int)(fm.getAscent() * 0.65));
               g.drawString(part2, x+(Agent.bRet-fm.stringWidth(part2))/2,Agent.yRet+(Agent.hRet/2) + (int)(fm.getAscent() * 0.3));
               g.drawString(part3, x+(Agent.bRet-fm.stringWidth(part3))/2,Agent.yRet+(Agent.hRet/2) + (int)(fm.getAscent() * 0.95));
  
           }
         }
       }

       horDim = 100+(xCanvDim*80);

     }

     if((typeCanv == false)) {

       /* This is the Message Canvas: so let's paint all the messages */

       int x1,x2,y;
       int xCoords[] = new int[3];
       int yCoords[] = new int[3];
       xCanvDim = otherCanv.al.size();

       Iterator it = ml.getMessages();
       int AllReceiver = 0;  
       while(it.hasNext()) {
         Message mess = (Message)it.next();
         String senderName = mess.getSender().getName();    
         xSource = otherCanv.al.getPos(senderName);
         //int receiverForAMessage = 0;
         //for(Iterator i = mess.getAllReceiver(); i.hasNext();) {
       	   //receiverForAMessage++;
       	   //String receiverName = ((AID)i.next()).getName();
         String receiverName = mess.getUnicastReceiver().getName();
           xDest = otherCanv.al.getPos(receiverName);
       
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

           if(x1 > x2) {
	         xCoords[0] = x2+10;
	         xCoords[1] = x2+10;
	         xCoords[2] = x2+2;
           }

           // First we lookup convID, replywith and replyto to see if any of them
           // have a colorindex.  If any of them do, then that becomes the one that
           // we will use.
           Integer colorIndex = new Integer(-1);
           //System.out.println("Starting color:" + mess.getPerformative() +
           //    " CID:" + mess.getConversationId() +
           //    " RW:" + mess.getReplyWith() +
           //    " RT:" + mess.getInReplyTo());
           if (mess.getConversationId() != null) {
             if (mapToColor.containsKey(mess.getConversationId())) {
                colorIndex = (Integer)mapToColor.get(mess.getConversationId());
                //System.out.println("Found CID:" + colorIndex);
             }
           }
           if (mess.getReplyWith() != null && colorIndex.intValue() == -1) {
             if (mapToColor.containsKey(mess.getReplyWith())) {
                colorIndex = (Integer)mapToColor.get(mess.getReplyWith());
                //System.out.println("Found RW:" + colorIndex);
             }
           } 
           if (mess.getInReplyTo() != null && colorIndex.intValue() == -1) {
             if (mapToColor.containsKey(mess.getInReplyTo())) {
                colorIndex = (Integer)mapToColor.get(mess.getInReplyTo());
                //System.out.println("Found RT:" + colorIndex);
             }
           }

           // If not, then we get the next color value.
           if (colorIndex.intValue() == -1) {
             colorCounter = new Integer(colorCounter.intValue() + 1);
             colorIndex = colorCounter;
             //System.out.println("Making new:" + colorIndex);
           }

           // Now, we store this value on all non-null ids.
           if (mess.getConversationId() != null) {
             //System.out.println("CID:" + mess.getConversationId()+ " was: " + mapToColor.get(mess.getConversationId()));
             mapToColor.put(mess.getConversationId(), colorIndex);
           }
           if (mess.getReplyWith() != null) {
             //System.out.println("RW:" + mess.getReplyWith()+ " was: " + mapToColor.get(mess.getReplyWith()));
             mapToColor.put(mess.getReplyWith(), colorIndex);
           }
           if (mess.getInReplyTo() != null) {
             //System.out.println("RT:" + mess.getInReplyTo() + " was: " + mapToColor.get(mess.getInReplyTo()));
             mapToColor.put(mess.getInReplyTo(), colorIndex);
           }
           //System.out.println("Done");
           g.setColor(colorTable[colorIndex.intValue() % colorTable.length]);
           g.drawRect(x1-3,y-4,4,8);
           g.fillRect(x1-3,y-4,4,8);

           // This code displays the name of the performative centered above the
           // arrow.  At some point, might want to make this optional.
           g.setFont(font3);
           FontMetrics fmPerf = g.getFontMetrics();
           String perf = mess.getPerformative(mess.getPerformative());
           // Add ConversationId and ReplyWith
           int numberToShow=3;
           perf=perf + ":" + colorIndex
                     + " (" + tail(numberToShow,mess.getConversationId()) 
                     + "  " + tail(numberToShow,mess.getReplyWith()) 
                     + "  " + tail(numberToShow,mess.getInReplyTo()) + " )";

           int perfWidth = fmPerf.stringWidth(perf);
           if (x2 > x1) {
             g.drawString(perf, x1+((x2-x1)/2)-perfWidth/2, y-4);
           } else {
             g.drawString(perf, x2+((x1-x2)/2)-perfWidth/2, y-4);
           }
        
           // disegno segmento messaggio
           for(int k=-1; k<=1; k++) {
             if (x2 > x1) {
	           g.drawLine(x1,y+k,x2,y+k);
             } else {
	           g.drawLine(x1,y+k,x2+4,y+k);
             }
           }

           // disegno freccetta del receiver
           g.drawPolygon(xCoords,yCoords,3);
           g.fillPolygon(xCoords,yCoords,3);
         //}
         AllReceiver++;
         //AllReceiver = AllReceiver+receiverForAMessage;
       } // while

       int msgNum = ml.size();
       for(int num = 0; num < xCanvDim; num++) {
         // Here we update the green lines of the timeline
         int x =  jade.tools.sniffer.Agent.yRet/2+num*80;
         g.setColor(new Color(0,100,50));
         //g.drawLine(x+xOffset,1,x+xOffset,timeUnitWidth*(msgNum+1));
         int counter = 0;
         for(Iterator i = ml.getMessages(); i.hasNext(); ) {
         	Message msg = (Message)i.next();
         	//int singleMsgCounter =0;
         	//for(Iterator j = msg.getAllReceiver(); j.hasNext(); )
         	//{  j.next();
         	//   singleMsgCounter++;
          //         msg.setMessageNumber(counter + singleMsgCounter);
         	//}
          //counter = counter + singleMsgCounter;        
         	msg.setMessageNumber(counter++);
         }
         g.drawLine(x+xOffset,1,x+xOffset,timeUnitWidth*(counter+1));
       }

       g.setColor(new Color(150,50,50));
       Integer msgNumWrapped;
       for (int t=0; t <=AllReceiver; t++) {
          // Here we update the red numbers of the timeline
          msgNumWrapped = new Integer(t);
          g.drawString(msgNumWrapped.toString(),10,timeUnitWidth*(t)+15);
       }
       horDim = 100+(xCanvDim*80);
       vertDim = 100+(yDim*20);
    }// if
  } catch (ConcurrentModificationException cme) {
     // Ignore - next repaint will correct things
  }
 } // Method

  /**
   * Method invoked everytime the use clicks on a blue arrow: it updates the TextMessage
   * component displaying the type of the message.
   *
   * @param evt mouse event
   */
  public void mousePressed(MouseEvent evt) {
   Message mess;
   String info;
   int numberToShow=5;

    if( ((mess = selMessage(evt)) != null) && (typeCanv == false)) {
       info = "  Message:" + mess.getMessageNumber() + " ";
       mPan.textArea.setText(" ");
       //mPan.textArea.setFont(font1);
       mPan.textArea.setText(info);
       mPan.textArea.setFont(font2);
       mPan.textArea.append(ACLMessage.getPerformative(
        mess.getPerformative())
         + " ( cid=" + tail(numberToShow,mess.getConversationId()) 
         + " rw="   + tail(numberToShow,mess.getReplyWith()) 
         + " irt="   + tail(numberToShow,mess.getInReplyTo()) 
         + " proto=" + mess.getProtocol()
         + " onto=" + mess.getOntology()
         + " )" );
    } else {
        Agent selectedAgent = selAgent(evt);
        if ((selectedAgent != null) && (typeCanv == true)) {
            mPan.textArea.setText("Agent: ");
            mPan.textArea.setFont(font2);
            mPan.textArea.append(selectedAgent.agentName);
        }
    }

   }

  /**
   * This method is invoked every time a user double-click on a blue arrow in the message canvas: the double-click occurs
   * on a blue arrow in the message canavs, a dialog box is displayed with the entire
   * message.
   *
   * @param evt mouse event
   */
   public void mouseClicked(MouseEvent evt) {
    Agent ag;
    Message mess;
    String info;
      if(evt.getClickCount() == 2) {

       if( ((mess = selMessage(evt)) != null) && (typeCanv == false)) {
			   AclGui.showMsgInDialog(mess,mWnd);
       }
    }
   }

 public void mouseEntered(MouseEvent evt) {}
 public void mouseExited(MouseEvent evt) {}
 public void mouseReleased(MouseEvent evt) {}

 public void mouseDragged(MouseEvent evt) {}
 
  public void mouseMoved(MouseEvent evt) {
    Agent selectedAgent = selAgent(evt);
    if ((selectedAgent != null) && (typeCanv == true)) {
      if (!nameShown) {
        nameShown = true;
        mPan.textArea.setText("Agent: ");
        mPan.textArea.setFont(font2);
        mPan.textArea.append(selectedAgent.agentName);
      }
    } else {
      if (nameShown) {
        nameShown = false;
        mPan.textArea.setText(null);
      }
    }
  }

 private boolean checkNoSniffedVector(Agent agent) {
  boolean isPresent=false;
  Agent agentToCompare;

  if(noSniffAgents.size()==0) return false;
   else {
    for (int i=0; i<noSniffAgents.size();i++) {
     agentToCompare=(Agent)noSniffAgents.get(i);
     if(agentToCompare.agentName.equals(agent.agentName)) {
      isPresent=true;
      positionAgent=i;
      break;
     }
    }
    if (isPresent) return true;
    else return false;
   }
 }

 public Message selMessage(MouseEvent evt) {
   int j = 0;

   Iterator it = ml.getMessages();
   while(it.hasNext()) {
     Message mess = (Message)it.next();
     String senderName = mess.getSender().getName();
     
     //for(Iterator i = mess.getAllReceiver();i.hasNext(); )
     //{
     	//String receiverName = ((AID)i.next()).getName();
     	String receiverName = mess.getUnicastReceiver().getName();
      x1 = mess.getInitSeg(otherCanv.al.getPos(senderName));
      x2 = mess.getEndSeg(otherCanv.al.getPos(receiverName));
      y = mess.getOrdSeg(j++);
      if(x1 < x2) {
       if((evt.getX() >= x1+H_TOL) && (evt.getX() <= x2+H_TOL) &&
	    (evt.getY() >= y - V_TOL) && (evt.getY() <= y + V_TOL)) {
	      return mess;
       }
      }
      else {
       if((evt.getX() >= x2 - H_TOL) && (evt.getX() <= x1 + H_TOL) &&
	      (evt.getY() >= y - V_TOL) && (evt.getY() <= y + V_TOL)) {
	       return mess;
       }
      }
     //}//for
   }//while

   return null;

 }

  /**
   * Returns an Agent if an Agent has been selected form the user, otherwise
   * returns null.
   *
   * @param evt mouse event
   * @return Agent selected or null if no Agent was selected
   */
 public Agent selAgent(MouseEvent evt) {
   int j = 0;
   int y1 = Agent.yRet;
   int y2 = y1 + Agent.yRet;

   try {
     Iterator it = al.getAgents();
     while(it.hasNext()) {
       Agent ag = (Agent)it.next();
       x1 = Agent.yRet + j*80;
       x2 = x1 + Agent.bRet;

       if((evt.getX() >= x1) && (evt.getX() <= x2) &&
	      (evt.getY() >= y1) && (evt.getY() <= y2)) {
	     if (ag.agentName.equals("Other")) {
	        return null;
	     } else {
	        return ag;
	     }
       }
       j++;
     }
   } catch (ConcurrentModificationException cme) {
      //  Ignore - next repaint will correct things
   }
   return null;
 }


  /**
   * This method repaint both canvas checking the size of the scrollbars. The
   * right procedure to follow is to call method setPreferredSize() the revalidate()
   * method.
   */
  private void repaintBothCanvas() {
    MMCanvas c1 = panCan.canvAgent;
    MMCanvas c2 = panCan.canvMess;

    panCan.setPreferredSize(new Dimension(horDim,vertDim+50));
    c1.setPreferredSize(new Dimension(horDim,50));
    c2.setPreferredSize(new Dimension(horDim,vertDim));
    panCan.revalidate();
    c1.repaint();
    c2.repaint();
  }


  /**
   * Adds an agent to canvas agent then repaints it
   *
   * @param agent agent to be add
   */

 public void rAgfromNoSniffVector(Agent agent) {
  if (checkNoSniffedVector(agent)) {
   noSniffAgents.remove(positionAgent);
   repaintBothCanvas();
  }
 }

 public void addAgent (Agent agent) {
   al.addAgent(agent);
   repaintBothCanvas();
 }

  /**
   * Removes an agent from the canvas agent then repaints it
   *
   * @param agentName agent to be removed
   */
  public void removeAgent (String agentName) {
   try{
    al.removeAgent(agentName);
    repaintBothCanvas();
   }
   catch(Exception e) {}
  }

  /**
   * Removes all the agents and messages from their lists then repaints the canvas
   */
  public void removeAllAgents () {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
	al.removeAllAgents();
	ml.removeAllMessages();
	repaintBothCanvas();
      }
    });
  }

  // method to repaint the  NoSniffed agent

  public void repaintNoSniffedAgent(Agent agent) {
    if(!checkNoSniffedVector(agent)) noSniffAgents.add(agent);
    repaintBothCanvas();
  }

  /**
   * Adds a message to canvas message then repaints the canvas
   *
   * @param mess message to be added
   */

  public void addMessage (final Message mess) {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        ml.addMessage(mess);
        repaintBothCanvas();
      }
    });
  }

  /**
   * Removes all the messages in the message list then repaints the canvas
   */

 public void removeAllMessages() {
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
  public boolean isPresent (String agName) {
    return al.isPresent(agName);
  }

  /**
   * Returns an handler to the agent list. The agent list contains all the agents
   * contained in the Agent Canvas displayed by grey or red boxes
   *
   * @return handler to agent list
   */
  public AgentList getAgentList() {
   return al;
  }

  /**
   * Returns an handler to the message list. The message list contains all
   * sniffed messages displayed on the Message Canavs as blue arrows
   *
   * @return handler to the message list
   */
 public MessageList getMessageList() {
  return ml;
 }

  /**
   * Set the agent list handler as the parameter passed then repaints the canvas
   *
   * @param savedList new list of agents
   */

 public void setAgentList(AgentList savedList) {
   al = savedList;
   repaintBothCanvas();
 }

  /**
   * Set the message list handler as the parameter passed then repaints the canvas
   *
   * @param savedList new list of messages
   */
 public void setMessageList(MessageList savedList) {
  ml = savedList;
  repaintBothCanvas();
 }	

  /** 
   * Returns new messages and put them into canvas agent 
   *
   * @param newMess new message
   */ 

  public void recMessage(Message newMess) {
   addMessage(newMess);
  }

  private String tail(int n, String s) {
      try {
	  return s.substring(s.length()-n,s.length());
      } catch (Exception any) {
	  return " ";
      }
  }


  /**
   * Trim off known prefixes.
   */
  private String nameClip(String aName) {
    String clipNames = mWnd.getProperties().getProperty("clip", null);
    if (clipNames == null) {
      return aName;
    }
    StringTokenizer parser = new StringTokenizer(clipNames, ";");
    while (parser.hasMoreElements()) {
      String clip = parser.nextToken();
      if (aName.startsWith(clip)) {
        return aName.substring(clip.length());
      }
    }
    return aName;
  }

} 
