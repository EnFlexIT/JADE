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

//#MIDP_EXCLUDE_FILE

import jade.mtp.MTPDescriptor;

import jade.util.leap.Iterator;
import jade.util.leap.List;
import jade.util.leap.LinkedList;
import jade.util.leap.Map;
import jade.util.leap.HashMap;

import jade.security.JADEPrincipal;
import jade.security.Credentials;


class ContainerTable {

  // Initial size of containers hash table
  private static final int CONTAINERS_SIZE = 10;

  private static class Entry {
    private Node node;
    private List mtps = new LinkedList();
    private JADEPrincipal principal;
    private Credentials credentials;

    public Entry(Node n) {
      node = n;
      principal = null;
      credentials = null;
    }

    public Entry(Node n, JADEPrincipal cp, Credentials cr) {
      node = n;
      principal = cp;
      credentials = cr;
    }

    public void addMTP(MTPDescriptor mtp) {
      mtps.add(mtp);
    }

    public void removeMTP(MTPDescriptor mtp) {
      mtps.remove(mtp);
    }

    public void setPrincipal(JADEPrincipal cp) {
      principal = cp;
    }

    public JADEPrincipal getPrincipal() {
      return principal;
    }

    public void setCredentials(Credentials cr) {
      credentials = cr;
    }

    public Credentials getCredentials() {
      return credentials;
    }

    public Node getNode() {
      return node;
    }

    public List getMTPs() {
      return mtps;
    }

  } // End of Entry class


  private Map entries = new HashMap(CONTAINERS_SIZE);


  public synchronized void addContainer(ContainerID cid, Node n) {
    Entry e = new Entry(n);
    entries.put(cid, e);
  }

  public synchronized void addContainer(ContainerID cid, Node n, JADEPrincipal cp, Credentials cr) {
    Entry e = new Entry(n, cp, cr);
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
    notifyAll();
  }

  public synchronized void removeMTP(ContainerID cid, MTPDescriptor mtp) throws NotFoundException {
    Entry e = (Entry)entries.get(cid);
    if(e == null)
      throw new NotFoundException("No container named " + cid.getName() + " was found.");
    List l = e.getMTPs();
    l.remove(mtp);
  }

  public synchronized Node getContainerNode(ContainerID cid) throws NotFoundException {
    Entry e = (Entry)entries.get(cid);
    if(e == null)
      throw new NotFoundException("No container named " + cid.getName() + " was found.");
    return e.getNode();
  }

  public synchronized void setPrincipal(ContainerID cid, JADEPrincipal cp) throws NotFoundException {
    Entry e = (Entry)entries.get(cid);
    if(e == null)
      throw new NotFoundException("No container named " + cid.getName() + " was found.");
    e.setPrincipal(cp);
  }

  public synchronized JADEPrincipal getPrincipal(ContainerID cid) throws NotFoundException {
    Entry e = (Entry)entries.get(cid);
    if(e == null)
      throw new NotFoundException("No container named " + cid.getName() + " was found.");
    return e.getPrincipal();
  }

  public synchronized void setCredentials(ContainerID cid, Credentials cr) throws NotFoundException {
    Entry e = (Entry)entries.get(cid);
    if(e == null)
      throw new NotFoundException("No container named " + cid.getName() + " was found.");
    e.setCredentials(cr);
  }

  public synchronized Credentials getCredentials(ContainerID cid) throws NotFoundException {
    Entry e = (Entry)entries.get(cid);
    if(e == null)
      throw new NotFoundException("No container named " + cid.getName() + " was found.");
    return e.getCredentials();
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

  public synchronized Node[] containers() {
    Node[] result = new Node[entries.size()];
    Iterator it = entries.values().iterator();
    int i = 0;
    while(it.hasNext()) {
      Entry e = (Entry)it.next();
      result[i++] = e.getNode();
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

  synchronized void waitForRemoval(ContainerID cid) {
      while(entries.containsKey(cid)) {
	  try {
	      wait();
	  }
	  catch(InterruptedException ie) {
	      // Do nothing...
	  }
      }
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
