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

import javax.swing.JPanel;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

  /**
   Javadoc documentation for the file
   @author Francisco Regi, Andrea Soracchi - Universita` di Parma
   <Br>
   <a href="mailto:a_soracchi@libero.it"> Andrea Soracchi(e-mail) </a>
   @version $Date$ $Revision$
 */

 /**
  * Makes the two canvas.One is the agent canvas  which is useful
  * for drawing the agent's box and other is the message canvas
  * which useful for drawing the message with blue arrows.
  * @see jade.sniffer.tools.MainPanel.
  */

public class PanelCanvas extends JPanel {

  protected MMCanvas canvAgent;
  protected MMCanvas canvMess;
  private PopMouserMessage popMess;
  private PopMouserAgent popAgent;
  private Sniffer mySniffer;

  public PanelCanvas(MainWindow mWnd,MainPanel mPan,Sniffer mySniffer) {
   GridBagConstraints gbc;
   this.mySniffer=mySniffer;
   setLayout(new GridBagLayout());
   gbc = new GridBagConstraints();
   gbc.gridx = 0;
   gbc.gridy = 0;
   gbc.gridwidth = GridBagConstraints.REMAINDER;
   gbc.gridheight = 1;
   gbc.anchor = GridBagConstraints.NORTHWEST;
   gbc.weightx = 0.5;
   gbc.weighty = 0;
   gbc.fill = GridBagConstraints.BOTH;
   canvAgent=new MMCanvas(true,mWnd,this,mPan,null);
   popAgent=new PopMouserAgent(canvAgent,mySniffer);
   canvAgent.addMouseListener(popAgent);
   add(canvAgent,gbc);

   gbc = new GridBagConstraints();
   gbc.gridx = 0;
   gbc.gridy = 1;
   gbc.gridwidth = GridBagConstraints.REMAINDER;
   gbc.gridheight = 100;
   gbc.anchor = GridBagConstraints.NORTHWEST;
   gbc.fill = GridBagConstraints.BOTH;
   gbc.weightx = 0.5;
   gbc.weighty = 1;

     //E' il canvas per i messaggi

   canvMess = new MMCanvas(false,mWnd,this,mPan,canvAgent);

   popMess=new PopMouserMessage(canvMess,mWnd);
   canvMess.addMouseListener(popMess);
   add(canvMess,gbc);
 }

} // End of class PanelCanvas