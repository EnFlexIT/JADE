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

import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.*;

/**
   @author Giovanni Caire - TILAB
 */
public class SenderAgent extends Agent {
	private ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
	int nMessages = 100;
	int period = 5000;
	
	protected void setup() {
		Object[] args = getArguments();
		try {
			AID receiver = new AID((String) args[0], AID.ISLOCALNAME);
			try {
				nMessages = Integer.parseInt((String) args[1]);
			}
			catch (Exception e1) {
				// Keep default
			}
			try {
				period = Integer.parseInt((String) args[2]);
			}
			catch (Exception e1) {
				// Keep default
			}
			msg.addReceiver(receiver);
			addBehaviour(new TickerBehaviour(this, period) {
				protected void onTick() {
					myAgent.send(msg);
					if (getTickCount() >= nMessages) {
						stop();
					}
				}
			} );
		}
		catch (Exception e) {
			jade.util.Logger.println("Receiver name not specified");
			doDelete();
		}
	}
}

