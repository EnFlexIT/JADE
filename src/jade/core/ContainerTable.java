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

import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;


class ContainerTable {

  // Initial size of containers hash table
  private static final int CONTAINERS_SIZE = 10;

  private static class Entry {
    private AgentContainer container;
    private List addresses = new LinkedList();

    public Entry(AgentContainer ac) {
      container = ac;
    }

    public void addAddress(String a) {
      addresses.add(a);
    }

    public void removeAddress(String a) {
      addresses.remove(a);
    }

    public AgentContainer getContainer() {
      return container;
    }

    public List getAddresses() {
      return addresses;
    }

  } // End of Entry class


  private Map entries = new HashMap(CONTAINERS_SIZE);


  public synchronized void addContainer(String containerName, AgentContainer ac) {
    Entry e = new Entry(ac);
    entries.put(containerName, e);
  }

  public synchronized void addAddress(String containerName, String address) throws NotFoundException {
    Entry e = (Entry)entries.get(containerName);
    if(e == null)
      throw new NotFoundException("No container named " + containerName + " was found.");
    List l = e.getAddresses();
    l.add(address);
  }

  public synchronized void removeContainer(String containerName) {
    entries.remove(containerName);
  }


  public synchronized AgentContainer getContainer(String containerName) throws NotFoundException {
    Entry e = (Entry)entries.get(containerName);
    if(e == null)
      throw new NotFoundException("No container named " + containerName + " was found.");
    return e.getContainer();
  }

  public synchronized List getAddresses(String containerName) throws NotFoundException {
    Entry e = (Entry)entries.get(containerName);
    if(e == null)
      throw new NotFoundException("No container named " + containerName + " was found.");
    return e.getAddresses();
  }

  public int size() {
    return entries.size();
  }

  public synchronized AgentContainer[] containers() {
    AgentContainer[] result = new AgentContainer[entries.size()];
    Iterator it = entries.values().iterator();
    int i = 0;
    while(it.hasNext()) {
      Entry e = (Entry)it.next();
      result[i++] = e.getContainer();
    }
    return result;
  }

  public synchronized String[] names() {
    String[] result = new String[entries.size()];
    Iterator it = entries.keySet().iterator();
    int i = 0;
    while(it.hasNext()) {
      String s = (String)it.next();
      result[i++] = s;
    }
    return result;
  }

}
