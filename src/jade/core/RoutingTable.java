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

import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;


class RoutingTable {

  private Map MTPs = new TreeMap(String.CASE_INSENSITIVE_ORDER);

  /**
     Adds a new MTP for the protocol named <code>name</code> on the
     Agent Container <code>where</code>.
   */
  public void addMTP(String name, AgentContainer where) {
    List l = (List)MTPs.get(name);
    if(l != null)
      l.add(where);
    else {
      l = new LinkedList();
      l.add(where);
      MTPs.put(name, l);
    }
  }

  /**
     Removes the MTP for the protocol named <code>name</code> on the
     Agent Container <code>where</code>.
   */
  public void removeMTP(String name, AgentContainer where) {
    List l = (List)MTPs.get(name);
    if(l != null)
      l.remove(where);
  }

  /**
     Retrieves an Agent Container where an instance of the MTP for the
     protocol <code>name</code> is available.
   */
  public AgentContainer lookup(String protoName) {
    List l = (List)MTPs.get(protoName);
    if(l != null)
      return (AgentContainer)l.get(0);
    else
      return null;
  }

}
