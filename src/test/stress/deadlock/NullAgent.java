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
import jade.util.Logger;

/**
 * <p>Title: NullAgent</p>
 * <p>This agent does very little apart from create a NullBehaviour,
 * which does even less! This is used to detect a deadlock in Jade</p>
 */
public class NullAgent extends Agent {

	private transient Logger logger = Logger.getMyLogger(this.getClass().getName());

	protected void setup() {
		// Enable object to agent communication
		setEnabledO2ACommunication(true, 0);

		System.out.println("Agent: " + getLocalName() + " started");

		addBehaviour(new NullBehaviour(this));

		// Get any arguments passed to the agent.
		Object[] args = getArguments();

		// Notify the caller that the agent has been set up properly.
		if(args.length > 0) {
			ConditionVariable condVar = (ConditionVariable)args[0];
			condVar.signal();
		}
	}

	public void takeDown() {
		setEnabledO2ACommunication(false, 0);
	}
}