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

import jade.util.leap.Iterator;
import jade.util.leap.List;
import jade.util.leap.ArrayList;
import jade.util.leap.Map;
import jade.util.leap.HashMap;

import jade.domain.FIPAAgentManagement.Envelope;

import jade.lang.acl.ACLMessage;

import jade.mtp.OutChannel;
import jade.mtp.MTP;
import jade.mtp.MTPException;
import jade.mtp.MTPDescriptor;

class RoutingTable {

  // This class wraps an MTP installed on a remote container, using
  // RMI to forward the deliver() operation
  private static class OutViaContainer implements OutPort {

    private AgentContainer container;

    public OutViaContainer(AgentContainer ac) {
      container = ac;
    }

    public void route(ACLMessage msg, AID receiver, String address) throws MTPException {
      try {
	container.routeOut(msg, receiver, address);
      }
      catch(IMTPException imtpe) {
	throw new MTPException("Container unreachable during routing", imtpe);
      }
    }

    public boolean equals(Object o) {
      try {
	OutViaContainer rhs = (OutViaContainer)o;
	AgentContainer ac = rhs.container;
	if(container.equals(ac))
	  return true;
	else
	  return false;
      }
      catch(ClassCastException cce) {
	return false;
      }
    }
    
  } // End of OutViaContainer class


  // This class wraps an MTP installed locally, using the ACC to encode
  // the message into an MTP payload.
  private static class OutViaMTP implements OutPort {

    private FullAcc myACC;
    private OutChannel myChannel;
      
    public OutViaMTP(FullAcc localACC, OutChannel proto) {
      myACC = localACC;
      myChannel = proto;
    }

    public void route(ACLMessage msg, AID receiver, String address) throws MTPException {
      try {
        myACC.prepareEnvelope(msg, receiver);
        Envelope env = msg.getEnvelope();
        byte[] payload = myACC.encodeMessage(msg);
        myChannel.deliver(address, env, payload);
      }
      catch(NotFoundException nfe) {
        throw new MTPException("ACL encoding not found.");
      }
    }

    public boolean equals(Object o) {
      try {
	OutViaMTP rhs = (OutViaMTP)o;
	OutChannel ch = rhs.myChannel;
	if(myChannel.equals(ch))
	  return true;
	else
	  return false;
      }
      catch(ClassCastException cce) {
	return false;
      }
    }
  }

  private static final boolean LOCAL = true;
  private static final boolean REMOTE = false;

  private static class OutPortList {

    private List local = new ArrayList();
    private List remote = new ArrayList();

    public void add(OutPort port, boolean location) {
      if(location == LOCAL) {
	local.add(port);
      }
      else {
	remote.add(port);
      }
    }

    public void remove(OutPort port) {
      local.remove(port);
      remote.remove(port);
    }

    public OutPort get() {
      // Look first in the local list
      if(!local.isEmpty())
	return (OutPort)local.get(0);
      // Then look in the remote list
      else
	if(!remote.isEmpty())
	  return (OutPort)remote.get(0);
      return null;
    }

    public boolean isEmpty() {
      return local.isEmpty() && remote.isEmpty();
    }

    public String size() {
      return "[ local: " + local.size() + "  remote: " + remote.size() + " ]";
    }

  } // End of OutPortList class


  private FullAcc myACC;
  private Map inPorts = new HashMap();
  private Map outPorts = new HashMap();
  private List platformAddresses = new ArrayList();

  
  public RoutingTable(FullAcc fa) {
    myACC = fa;   
  }

  /**
     Adds a new locally installed MTP for the URL named
     <code>url</code>.
   */
  public synchronized void addLocalMTP(String url, MTP proto) {
    url = url.toLowerCase();
    // A local MTP can receive messages
    inPorts.put(url, proto);

    // A local MTP can also send messages, over all supported protocols
    OutPort out = new OutViaMTP(myACC, proto);
    String[] protoNames = proto.getSupportedProtocols();
    for(int i = 0; i < protoNames.length; i++) {
      addOutPort(protoNames[i], out, LOCAL);
    }

    // The new MTP is a valid address for the platform
    platformAddresses.add(url);

    /*
    java.util.Iterator it = outPorts.keySet().iterator();
    while(it.hasNext()) {
      String name = (String)it.next();
      OutPortList l = (OutPortList)outPorts.get(name);
      System.out.println("<" + name + "> ==> " + l.size());
    }
    */

  }

  /**
     Removes a locally installed MTP for the URL named
     <code>url</code>.
   */
  public synchronized MTP removeLocalMTP(String url) {
    url = url.toLowerCase();
    // A local MTP appears both in the input and output port tables
    MTP proto = (MTP)inPorts.remove(url);
    if(proto != null) {
      // Remove all outgoing ports associated with this MTP
      String[] protoNames = proto.getSupportedProtocols();
      for(int i = 0; i < protoNames.length; i++) {
	OutPort out = new OutViaMTP(myACC, proto);
	removeOutPort(protoNames[i], out);
      }
    }

    // The MTP address is not a platform address anymore
    platformAddresses.remove(url);

    /*
    java.util.Iterator it = outPorts.keySet().iterator();
    while(it.hasNext()) {
      String name = (String)it.next();
      OutPortList l = (OutPortList)outPorts.get(name);
      System.out.println("<" + name + "> ==> " + l.size());
    }
    */

    return proto;
  }

  public synchronized void addRemoteMTP(MTPDescriptor mtp, AgentContainer where) {

    // A remote MTP can be used only for outgoing messages, through an
    // OutPort that routes messages through a container
    OutPort out = new OutViaContainer(where);
    String[] protoNames = mtp.getSupportedProtocols();
    for(int i = 0; i < protoNames.length; i++) {
      addOutPort(protoNames[i], out, REMOTE);
    }

    // Remote MTPs are valid platform addresses
    platformAddresses.add(mtp.getAddress());
  }

  /**
     Removes the MTP for the URL named <code>name</code>.
   */
  public synchronized void removeRemoteMTP(MTPDescriptor mtp, AgentContainer where) {
    OutPort ch = new OutViaContainer(where);
    String[] protoNames = mtp.getSupportedProtocols();
    for(int i = 0; i < protoNames.length; i++) {
      removeOutPort(protoNames[i], ch);
    }

    // Remote MTPs are valid platform addresses
    platformAddresses.remove(mtp.getAddress());
  }

  /**
     Retrieves an outgoing channel object suitable for
     reaching the address <code>url</code>.
   */
  public synchronized OutPort lookup(String url) {
    url = url.toLowerCase();
    String proto = extractProto(url);
    OutPortList l = (OutPortList)outPorts.get(proto);
    if(l != null)
      return l.get();
    else
      return null;
  }

  public synchronized Iterator getAddresses() {
    return platformAddresses.iterator();
  }

  private void addOutPort(String proto, OutPort port, boolean location) {
    proto = proto.toLowerCase();
    OutPortList l = (OutPortList)outPorts.get(proto);
    if(l != null)
      l.add(port, location);
    else {
      l = new OutPortList();
      l.add(port, location);
      outPorts.put(proto, l);
    }
  }

  private void removeOutPort(String proto, OutPort port) {
    proto = proto.toLowerCase();
    OutPortList l = (OutPortList)outPorts.get(proto);
    if(l != null)
      l.remove(port);
  }

  private String extractProto(String address) {
    int colonPos = address.indexOf(':');
    if(colonPos == -1)
      return null;
    return address.substring(0, colonPos);
  }

}

/*__J2ME_COMPATIBILITY__BEGIN
package jade.core;

import jade.util.leap.Iterator;
import jade.util.leap.List;
import jade.util.leap.ArrayList;
import java.util.Hashtable;

import jade.domain.FIPAAgentManagement.Envelope;

import jade.lang.acl.ACLMessage;

import jade.mtp.OutChannel;
import jade.mtp.MTP;
import jade.mtp.MTPException;

class RoutingTable {

  // This class wraps an MTP installed on a remote container, using
  // RMI to forward the deliver() operation
  private static class OutViaContainer implements OutPort {

    private AgentContainer container;

    public OutViaContainer(AgentContainer ac) {
      container = ac;
    }

    public void route(ACLMessage msg, AID receiver, String address) throws MTPException {
      try {
	container.routeOut(msg, receiver, address);
      }
      catch(IMTPException imtpe) {
	throw new MTPException("Container unreachable during routing", imtpe);
      }
    }

    public boolean equals(Object o) {
      try {
	OutViaContainer rhs = (OutViaContainer)o;
	AgentContainer ac = rhs.container;
	if(container.equals(ac))
	  return true;
	else
	  return false;
      }
      catch(ClassCastException cce) {
	return false;
      }
    }
    
  } // End of OutViaContainer class


  // This class wraps an MTP installed locally, using the ACC to encode
  // the message into an MTP payload.
  private static class OutViaMTP implements OutPort {

    private FullAcc myACC;
    private OutChannel myChannel;
      
    public OutViaMTP(FullAcc localACC, OutChannel proto) {
      myACC = localACC;
      myChannel = proto;
    }

    public void route(ACLMessage msg, AID receiver, String address) throws MTPException {
      try {
        myACC.prepareEnvelope(msg, receiver);
        Envelope env = msg.getEnvelope();
        byte[] payload = myACC.encodeMessage(msg);
        myChannel.deliver(address, env, payload);
      }
      catch(NotFoundException nfe) {
        throw new MTPException("ACL encoding not found.");
      }
    }

    public boolean equals(Object o) {
      try {
	OutViaMTP rhs = (OutViaMTP)o;
	OutChannel ch = rhs.myChannel;
	if(myChannel.equals(ch))
	  return true;
	else
	  return false;
      }
      catch(ClassCastException cce) {
	return false;
      }
    }
  }

  private static final boolean LOCAL = true;
  private static final boolean REMOTE = false;

  private static class OutPortList {

    private List local = new ArrayList();
    private List remote = new ArrayList();

    public void add(OutPort port, boolean location) {
      if(location == LOCAL) {
	local.add(port);
      }
      else {
	remote.add(port);
      }
    }

    public void remove(OutPort port) {
      local.remove(port);
      remote.remove(port);
    }

    public OutPort get() {
      // Look first in the local list
      if(!local.isEmpty())
	return (OutPort)local.get(0);
      // Then look in the remote list
      else
	if(!remote.isEmpty())
	  return (OutPort)remote.get(0);
      return null;
    }

    public boolean isEmpty() {
      return local.isEmpty() && remote.isEmpty();
    }

  } // End of OutPortList class


  private FullAcc myACC;
  private Hashtable inPorts = new Hashtable();
  private Hashtable outPorts = new Hashtable();
  private List platformAddresses = new ArrayList();

  
  public RoutingTable(FullAcc fa) {
    myACC = fa;   
  }

  // Adds a new locally installed MTP for the URL named
  // <code>url</code>.
  public void addLocalMTP(String url, MTP proto) {
    // A local MTP can receive messages
    inPorts.put(url, proto);

    // A local MTP can also send messages
    OutPort out = new OutViaMTP(myACC, proto);
    addOutPort(url, out, LOCAL);

    // The new MTP is a valid address for the platform
    platformAddresses.add(url);
  }

  // Removes a locally installed MTP for the URL named
  // <code>url</code>.
  public MTP removeLocalMTP(String url) {
    // A local MTP appears both in the input and output port tables
    MTP proto = (MTP)inPorts.remove(url);
    if(proto != null) {
      OutPort out = new OutViaMTP(myACC, proto);
      removeOutPort(url, out);
    }

    // The MTP address is not a platform address anymore
    platformAddresses.remove(url);

    return proto;
  }

  public void addRemoteMTP(String url, AgentContainer where) {

    // A remote MTP can be used only for outgoing messages, through a
    // RoutedChannel
    OutPort out = new OutViaContainer(where);
    addOutPort(url, out, REMOTE);

    // Remote MTPs are valid platform addresses
    platformAddresses.add(url);
  }

  // Removes the MTP for the URL named <code>name</code>.
  public void removeRemoteMTP(String url, AgentContainer where) {
    OutPort ch = new OutViaContainer(where);
    removeOutPort(url, ch);

    // Remote MTPs are valid platform addresses
    platformAddresses.remove(url);
  }

  
  // Retrieves an outgoing channel object suitable for
  // reaching the address <code>url</code>.
  public OutPort lookup(String url) {
    String proto = extractProto(url);
    OutPortList l = (OutPortList)outPorts.get(proto);
    if(l != null)
      return l.get();
    else
      return null;
  }

  public Iterator getAddresses() {
    return platformAddresses.iterator();
  }

  private void addOutPort(String url, OutPort port, boolean location) {
    String proto = extractProto(url);
    OutPortList l = (OutPortList)outPorts.get(proto);
    if(l != null)
      l.add(port, location);
    else {
      l = new OutPortList();
      l.add(port, location);
      outPorts.put(proto, l);
    }
  }

  private void removeOutPort(String url, OutPort port) {
    String proto = extractProto(url);
    OutPortList l = (OutPortList)outPorts.get(proto);
    if(l != null)
      l.remove(port);
  }

  private String extractProto(String address) {
    int colonPos = address.indexOf(':');
    if(colonPos == -1)
      return null;
    return address.substring(0, colonPos);
  }

}
__J2ME_COMPATIBILITY__END*/


