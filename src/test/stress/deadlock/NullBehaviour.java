/*****************************************************************
Copyright (C) 2004 Mooter Pty Ltd.

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
package test.stress.deadlock;

import jade.core.Agent;

import jade.core.behaviours.CyclicBehaviour;
import jade.util.Logger;


/**
 * <p>Title: NullBehaviour</p>
 * <p>Description: Reads a message from the O2A queue, logs a message
 *  and then blocks.</p>
 * @author Richard Heycock
 */
public class NullBehaviour extends CyclicBehaviour {
	private Agent  agent = null;
	private Logger logger = Logger.getMyLogger(this.getClass().getName());

	/** Constructor. */
	public NullBehaviour(Agent agent) {
		this.agent = agent;
	}

	public void action() {
		Object obj = agent.getO2AObject();

		if(obj != null) {
			MessageBean		  mb = (MessageBean)obj;

			ConditionVariable condVar = mb.getCondVar();
			int n = mb.getN();

			condVar.signal();
		}
		else {
			block();
		}
	}
}