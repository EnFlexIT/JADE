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

import java.util.Map;
import java.util.HashMap;


// Class for the Local Agent Descriptor Table.

/**
   @author Giovanni Rimassa - Universita` di Parma
   @version $Date$ $Revision$
 */
class LADT {

  // Initial size of agent hash table
  private static final int MAP_SIZE = 50;

  // Load factor of agent hash table
  private static final float MAP_LOAD_FACTOR = 0.50f;


  private Map agents = new HashMap(MAP_SIZE, MAP_LOAD_FACTOR);

  public synchronized Agent put(AID aid, Agent a) {
    return (Agent)agents.put(aid, a);
  }

  public synchronized Agent get(AID key) {
    return (Agent)agents.get(key);
  }

  public synchronized Agent remove(AID key) {
    return (Agent)agents.remove(key);
  }

  public synchronized AID[] keys() {
    Object[] objs = agents.keySet().toArray();
    AID[] result = new AID[objs.length];
    System.arraycopy(objs, 0, result, 0, result.length);
    return result;
  }

  public synchronized Agent[] values() {
    Object[] objs = agents.values().toArray();
    Agent[] result = new Agent[objs.length];
    System.arraycopy(objs, 0, result, 0, result.length);
    return result;
  }

}
