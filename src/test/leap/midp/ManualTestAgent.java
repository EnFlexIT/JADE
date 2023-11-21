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

package test.leap.midp;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.lang.acl.*;

import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;

import java.util.Vector;

/**
   @author Giovanni Caire - TILAB
 */
public class ManualTestAgent extends Agent {
	private ReceivedList receivedList;
	private Vector messages = new Vector();
	
	protected void setup() { 
		// Show the Received form
		receivedList = new ReceivedList();
		Display.getDisplay(Agent.midlet).setCurrent(receivedList); 

		// Add the beahaviour that receives messages
		addBehaviour(new CyclicBehaviour(this) {
			public void action() {
				ACLMessage msg = myAgent.receive();
				if (msg != null) {
					receivedList.addMessage(msg);
				}
				else { 
					block();
				}
			}
		} );
	}
	
	/**
	   Inner class ReceivedList
	 */
	private class ReceivedList extends List implements CommandListener {
		private Command openCmd, writeCmd;
		
		public ReceivedList() {
			super("Received messages", List.EXCLUSIVE);
			openCmd = new Command("Open", Command.OK, 0);
			addCommand(openCmd);
			writeCmd = new Command("Write", Command.SCREEN, 1);
			addCommand(writeCmd);
			
			setCommandListener(this);
		}
		
		public void addMessage(ACLMessage msg) {
			messages.addElement(msg);
			append(stringify(msg), null);
		}
		
		public void commandAction (Command c, Displayable d) {
			if (c == openCmd) {
				int i = getSelectedIndex();
				if (i >= 0) {
					ACLMessage msg = (ACLMessage) messages.elementAt(i);
					Form f = new ViewForm(msg);
					Display.getDisplay(Agent.midlet).setCurrent(f);
				}
			}
			else if (c == writeCmd) {
				Form f = new WriteForm();
				Display.getDisplay(Agent.midlet).setCurrent(f);
			}				
		}
	} // END of inner class ReceivedList
	
	
	/**
	   Inner class ViewForm
	 */
	private class ViewForm extends Form implements CommandListener {
		private Command deleteCmd, backCmd;
		private ACLMessage myMessage;
		
		public ViewForm(ACLMessage msg) {
			super(stringify(msg));
			myMessage = msg;
			append(ManualTestAgent.this.toString(msg));
			
			backCmd = new Command("Back", Command.CANCEL, 0);
			addCommand(backCmd);
			deleteCmd = new Command("Delete", Command.SCREEN, 1);
			addCommand(deleteCmd);
			
			setCommandListener(this);
		}
		
		public void commandAction (Command c, Displayable d) {
			if (c == deleteCmd) {
				int i = messages.indexOf(myMessage);
				messages.removeElementAt(i);
				receivedList.delete(i);
				Display.getDisplay(Agent.midlet).setCurrent(receivedList);
			}
			else if (c == backCmd) {
				Display.getDisplay(Agent.midlet).setCurrent(receivedList);
			}				
		}
	} // END of inner class ViewForm	
	
	
	/**
	   Inner class WriteForm
	 */
	private class WriteForm extends Form implements CommandListener {
		private Command backCmd;
		private Command sendCmd;
		
		private TextField recvTF, ontoTF, langTF, protoTF, convTF, contTF;
		
		public WriteForm() {
			super("Write");
			
			backCmd = new Command("Back", Command.CANCEL, 0);
			addCommand(backCmd);
			sendCmd = new Command("Send", Command.SCREEN, 0);
			addCommand(sendCmd);
			
			setCommandListener(this);
		
			recvTF = new TextField("Receiver", null, 30, TextField.ANY);
			append(recvTF);
			ontoTF = new TextField("Ontology", null, 30, TextField.ANY);
			append(ontoTF);
			langTF = new TextField("Language", null, 30, TextField.ANY);
			append(langTF);
			protoTF = new TextField("Protocol", null, 30, TextField.ANY);
			append(protoTF);
			convTF = new TextField("Conversation-id", null, 30, TextField.ANY);
			append(convTF);
			contTF = new TextField("Content", null, 60, TextField.ANY);
			append(contTF);
		}
		
		public void commandAction (Command c, Displayable d) {
			if (c == sendCmd) {
				String recv = recvTF.getString();
				if (recv != null) {
					ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
					msg.addReceiver(new AID(recv, AID.ISLOCALNAME));
					msg.setOntology(ontoTF.getString());
					msg.setLanguage(langTF.getString());
					msg.setProtocol(protoTF.getString());
					msg.setConversationId(convTF.getString());
					msg.setContent(contTF.getString());
					send(msg);
					Display.getDisplay(Agent.midlet).setCurrent(receivedList);
				}
			}					
			else if (c == backCmd) {
				Display.getDisplay(Agent.midlet).setCurrent(receivedList);
			}				
		}
	} // END of inner class WriteForm	
	
	private String stringify(ACLMessage msg) {
		return ACLMessage.getPerformative(msg.getPerformative());
	}
	
	private String toString(ACLMessage msg) {
		StringBuffer sb = new StringBuffer("(");
		sb.append(stringify(msg));
		sb.append(" sender: ");
		sb.append(msg.getSender().getName());
		String tmp = msg.getOntology();
		if (tmp != null) {
			sb.append(" ontology: ");
			sb.append(tmp);
		}
		tmp = msg.getLanguage();
		if (tmp != null) {
			sb.append(" language: ");
			sb.append(tmp);
		}
		tmp = msg.getProtocol();
		if (tmp != null) {
			sb.append(" protocol: ");
			sb.append(tmp);
		}
		tmp = msg.getConversationId();
		if (tmp != null) {
			sb.append(" conversation-id: ");
			sb.append(tmp);
		}
		tmp = msg.getContent();
		if (tmp != null) {
			sb.append(" content: ");
			sb.append(tmp);
		}
		sb.append(")");
		return sb.toString();
	}
}

