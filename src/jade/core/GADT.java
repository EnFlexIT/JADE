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

import jade.util.leap.Map;
import jade.util.leap.HashMap;


// Class for the Global Agent Descriptor Table.
class GADT {

  private Map descriptors = new HashMap();

  public synchronized AgentDescriptor put(AID aid, AgentDescriptor desc) {
    return (AgentDescriptor)descriptors.put(aid, desc);
  }

  public synchronized AgentDescriptor get(AID key) {
    return (AgentDescriptor)descriptors.get(key);
  }

  public synchronized AgentDescriptor remove(AID key) {
    return (AgentDescriptor)descriptors.remove(key);
  }

  public synchronized AID[] keys() {
    Object[] objs = descriptors.keySet().toArray();
    AID[] result = new AID[objs.length];
    System.arraycopy(objs, 0, result, 0, result.length);
    return result;
  }

  public synchronized AgentDescriptor[] values() {
    Object[] objs = descriptors.values().toArray();
    AgentDescriptor[] result = new AgentDescriptor[objs.length];
    System.arraycopy(objs, 0, result, 0, result.length);
    return result;
  }

}
