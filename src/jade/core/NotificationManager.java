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

package jade.core;

/** 
   This is the interface that must be implemented by a class 
   managing all the operations related to notifications about 
   events that happen on a container (e.g. messages sent/received
   by an agent, messages routed by a container, agent state changes,
   behaviour added/removed)
   Platform level events (e.g. container added/removed, agent 
   born/dead) are not considered by the notification manager as they
   are managed directly by the <code>MainContainerImpl</code>.
   @see RealNotificationManager;
   @author Giovanni Caire - TILAB
  */
interface NotificationManager {
	public static final int SENT_MESSAGE = 1;
	public static final int POSTED_MESSAGE = 2;
	public static final int RECEIVED_MESSAGE = 3;
	public static final int ROUTED_MESSAGE = 4;
	public static final int CHANGED_AGENT_STATE = 5;

  void initialize(AgentContainerImpl ac, LADT ladt);
  
  // ACTIVATION/DEACTIVATION METHODS
  void enableSniffer(AID snifferName, AID toBeSniffed);
  void disableSniffer(AID snifferName, AID notToBeSniffed);
  void enableDebugger(AID debuggerName, AID toBeDebugged);
  void disableDebugger(AID debuggerName, AID notToBeDebugged);
  
  // NOTIFICATION METHODS
  void fireEvent(int eventType, Object[] param);
  //void fireSentMessage(ACLMessage msg, AID sender);
  //void firePostedMessage(ACLMessage msg, AID receiver);
  //void fireReceivedMessage(ACLMessage msg, AID receiver);
  //void fireRoutedMessage(ACLMessage msg, Channel from, Channel to);
  //void fireChangedAgentState(AID agentID, AgentState from, AgentState to);

}