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

import jade.gui.AgentTree;
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
  * For don't sniff the Agent in the tree.
  * @see jade.tools.sniffer.DoSniffAction
  * @see jade.tools.sniffer.ShowOnlyAction
  */


public class DoNotSnifferAction extends AgentAction {

 private MainPanel mainPanel;
 private Sniffer mySniffer;
 private List  noSniffedAgents = new LinkedList();
 private Agent agent;

 public DoNotSnifferAction(ActionProcessor actPro, MainPanel mainPanel, Sniffer mySniffer) {
  super("DoNotSnifferActionIcon", "Do not sniff this agent(s)", actPro);
  this.mySniffer=mySniffer;
  this.mainPanel=mainPanel;
 }

 public void doAction(AgentTree.AgentNode node) {
  if(!noSniffedAgents.contains(agent)) {
    agent = new Agent(node.getName());
    noSniffedAgents.add(agent);
    mainPanel.panelcan.canvAgent.removeAgent(agent.getName());
  }
 }

 public void sendAgents() {
  mySniffer.sniffMsg(noSniffedAgents, Sniffer.SNIFF_OFF);   // Sniff the Agents
  noSniffedAgents.clear();
 }

} // End of class DoNotSnifferAction
