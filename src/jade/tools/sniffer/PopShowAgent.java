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
import java.util.List;
import java.util.LinkedList;

   /**
   Javadoc documentation for the file
   @author Francisco Regi, Andrea Soracchi - Universita` di Parma
   <Br>
   <a href="mailto:a_soracchi@libero.it"> Andrea Soracchi(e-mail) </a>
   @version $Date$ $Revision$
 */

   /**
   * This class includes the method ActionPerformed that is
   * associated with the PopupMenu of the Agent in the canvas.
   * @see jade.tools.sniffer.PopSniffAgent
   * @see jade.tools.sniffer.PopNoSniffAgent
   */

public class PopShowAgent extends AbstractPopup{
 private PopupAgent popAg;
 private List noSniffAgent=new LinkedList();
 private Sniffer mySniffer;
 private MMCanvas canvAgent;

 public PopShowAgent(PopupAgent popAg,Sniffer mySniffer,MMCanvas canvAgent){
  super("Show Only Agent");
  this.popAg=popAg;
  this.canvAgent=canvAgent;
  this.mySniffer=mySniffer;
  }

 public void actionPerformed(ActionEvent avt) {
   noSniffAgent.add(popAg.agent);
   canvAgent.repaintNoSniffedAgent(popAg.agent);
   mySniffer.sniffMsg(noSniffAgent,Sniffer.SNIFF_OFF);   // Sniff the Agents
   noSniffAgent.clear();
 }

} // End of class PopShowAgent
