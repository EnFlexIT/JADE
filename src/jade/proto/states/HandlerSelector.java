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

package jade.proto.states;

import jade.core.Agent;
import jade.core.behaviours.*;
import jade.util.leap.Map;
import jade.util.leap.HashMap;

public abstract class HandlerSelector extends FSMBehaviour {
	private Map handlers = new HashMap();
	
	// FSM states names
	public static final String SELECT = "Select";
	public static final String HANDLE = "Handle";
	public static final String DUMMY = "Dummy";
	
	// States exit values
	public static final int SELECTION_OK = 1;
	public static final int SELECTION_NOK = 0;
	
	// FIXME: We should include the key to retrieve from the 
	// private data store the object to be passes to the getSelectionKey() 
	// method 
	public HandlerSelector(Agent a, DataStore s) {
		super(a);
		
		setDataStore(s);
		
		Behaviour b = null;
		// Create and register the states that make up the FSM
		// SELECT
		b = new OneShotBehaviour(myAgent) {
			int ret; 
			
			public void action() {
				ret = SELECTION_NOK;
				// Modify as FIXME above
				Object key = getSelectionKey();
				if (key != null) {
					Behaviour b1 = (Behaviour) handlers.get(key);
					if (b1 != null) {
						// The HANDLE state is registered on the fly
						registerLastState(b1, HANDLE);
						ret = SELECTION_OK;
					}
				}
			}
			
			public int onEnd() {
				return ret;
			}
		};
		b.setDataStore(getDataStore());		
		registerFirstState(b, SELECT);
				
		// DUMMY
		b = new OneShotBehaviour(myAgent) {
			public void action() {}
		};
		registerLastState(b, DUMMY);
		
		// Register the FSM transitions
		registerTransition(SELECT, HANDLE, SELECTION_OK);
		registerDefaultTransition(SELECT, DUMMY);
	}
	
	// Modify as FIXME above
	protected abstract Object getSelectionKey();
	
	public void registerHandler(Object key, Behaviour h) {
		handlers.put(key, h);
	}
}
		