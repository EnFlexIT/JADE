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

import jade.mtp.MTPDescriptor;

import jade.util.leap.Iterator;
import jade.util.leap.List;
import jade.util.leap.LinkedList;
import jade.util.leap.Map;
import jade.util.leap.HashMap;

class ContainerTable {

  // Initial size of containers hash table
  private static final int CONTAINERS_SIZE = 10;

  private static class Entry {
    private AgentContainer container;
    private List mtps = new LinkedList();

    public Entry(AgentContainer ac) {
      container = ac;
    }

    public void addMTP(MTPDescriptor mtp) {
      mtps.add(mtp);
    }

    public void removeMTP(MTPDescriptor mtp) {
      mtps.remove(mtp);
    }

    public AgentContainer getContainer() {
      return container;
    }

    public List getMTPs() {
      return mtps;
    }

  } // End of Entry class


  private Map entries = new HashMap(CONTAINERS_SIZE);


  public synchronized void addContainer(ContainerID cid, AgentContainer ac) {
    Entry e = new Entry(ac);
    entries.put(cid, e);
  }

  public synchronized void addMTP(ContainerID cid, MTPDescriptor mtp) throws NotFoundException {
    Entry e = (Entry)entries.get(cid);
    if(e == null)
      throw new NotFoundException("No container named " + cid.getName() + " was found.");
    List l = e.getMTPs();
    l.add(mtp);
  }

  public synchronized void removeContainer(ContainerID cid) {
    entries.remove(cid);
    if(entries.isEmpty())
      notifyAll();
  }

  public synchronized void removeMTP(ContainerID cid, MTPDescriptor mtp) throws NotFoundException {
    Entry e = (Entry)entries.get(cid);
    if(e == null)
      throw new NotFoundException("No container named " + cid.getName() + " was found.");
    List l = e.getMTPs();
    l.remove(mtp);
  }

  public synchronized AgentContainer getContainer(ContainerID cid) throws NotFoundException {
    Entry e = (Entry)entries.get(cid);
    if(e == null)
      throw new NotFoundException("No container named " + cid.getName() + " was found.");
    return e.getContainer();
  }

  public synchronized List getMTPs(ContainerID cid) throws NotFoundException {
    Entry e = (Entry)entries.get(cid);
    if(e == null)
      throw new NotFoundException("No container named " + cid.getName() + " was found.");
    return e.getMTPs();
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

  public synchronized ContainerID[] names() {
    ContainerID[] result = new ContainerID[entries.size()];
    Iterator it = entries.keySet().iterator();
    int i = 0;
    while(it.hasNext()) {
      result[i++] = (ContainerID) it.next();
    }
    return result;
  }

  synchronized void waitUntilEmpty() {
    while(!entries.isEmpty()) {
      try {
        wait();
      }
      catch(InterruptedException ie) {
        // Do nothing...
      }
    }
  }

}
