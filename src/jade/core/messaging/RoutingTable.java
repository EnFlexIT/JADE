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

package jade.core.messaging;

//#MIDP_EXCLUDE_FILE

import jade.util.leap.Iterator;
import jade.util.leap.List;
import jade.util.leap.ArrayList;
import jade.util.leap.Map;
import jade.util.leap.HashMap;

import jade.core.AID;
import jade.core.CaseInsensitiveString;
import jade.core.IMTPException;
import jade.core.NotFoundException;


import jade.domain.FIPAAgentManagement.Envelope;

import jade.lang.acl.ACLMessage;

import jade.mtp.OutChannel;
import jade.mtp.MTP;
import jade.mtp.MTPException;
import jade.mtp.MTPDescriptor;

class RoutingTable {

    public interface OutPort {
	void route(ACLMessage msg, AID receiver, String address) throws MTPException;
    }


  // This class wraps an MTP installed on a remote container, using
  // RMI to forward the deliver() operation
  private static class OutViaSlice implements OutPort {

    private String sliceName;
    private MessagingSlice slice;

    public OutViaSlice(String sn, MessagingSlice ms) {
	sliceName = sn;
	slice = ms;
    }

    public void route(ACLMessage msg, AID receiver, String address) throws MTPException {
      try {
	slice.routeOut(msg, receiver, address);
      }
      catch(IMTPException imtpe) {
	throw new MTPException("Container unreachable during routing", imtpe);
      }
    }

    public boolean equals(Object o) {
      try {
	OutViaSlice rhs = (OutViaSlice)o;
	String sn = rhs.sliceName;
	if(sliceName.equals(sn))
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

    private MessagingService myService;
    private OutChannel myChannel;

    public OutViaMTP(MessagingService ms, OutChannel proto) {
      myService = ms;
      myChannel = proto;
    }

    public void route(ACLMessage msg, AID receiver, String address) throws MTPException {
      try {
        myService.prepareEnvelope(msg, receiver);
        Envelope env = msg.getEnvelope();
        byte[] payload = myService.encodeMessage(msg);
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


  private Map inPorts = new HashMap();
  private Map outPorts = new HashMap();
  private List platformAddresses = new ArrayList();
  private MessagingService myService;

  public RoutingTable(MessagingService ms) {
    myService = ms;
  }

  /**
     Adds a new locally installed MTP for the URL named
     <code>url</code>.
   */
  public synchronized void addLocalMTP(String url, MTP proto) {
    
      CaseInsensitiveString urlTmp = new CaseInsensitiveString(url);  
      // url = url.toLowerCase();
    // A local MTP can receive messages
    inPorts.put(urlTmp, proto);

    // A local MTP can also send messages, over all supported protocols
    OutPort out = new OutViaMTP(myService, proto);
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
      // url = url.toLowerCase();
      CaseInsensitiveString urlTmp = new CaseInsensitiveString(url);
    // A local MTP appears both in the input and output port tables
    MTP proto = (MTP)inPorts.remove(urlTmp);
    if(proto != null) {
      // Remove all outgoing ports associated with this MTP
      String[] protoNames = proto.getSupportedProtocols();
      for(int i = 0; i < protoNames.length; i++) {
	OutPort out = new OutViaMTP(myService, proto);
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

  public synchronized void addRemoteMTP(MTPDescriptor mtp, String sliceName, MessagingSlice where) {

    // A remote MTP can be used only for outgoing messages, through an
    // OutPort that routes messages through a container
    OutPort out = new OutViaSlice(sliceName, where);
    String[] protoNames = mtp.getSupportedProtocols();
    for(int i = 0; i < protoNames.length; i++) {
      addOutPort(protoNames[i], out, REMOTE);
    }

    // Remote MTPs are valid platform addresses
    String[] mtpAddrs = mtp.getAddresses();
    platformAddresses.add(mtpAddrs[0]); 
  }

  /**
     Removes the MTP for the URL named <code>name</code>.
   */
  public synchronized void removeRemoteMTP(MTPDescriptor mtp, String sliceName, MessagingSlice where) {
    OutPort ch = new OutViaSlice(sliceName, where);
    String[] protoNames = mtp.getSupportedProtocols();
    for(int i = 0; i < protoNames.length; i++) {
      removeOutPort(protoNames[i], ch);
    }

    // Remote MTPs are valid platform addresses
    String[] mtpAddrs = mtp.getAddresses();
    platformAddresses.remove(mtpAddrs[0]);
  }

  /**
     Retrieves an outgoing channel object suitable for
     reaching the address <code>url</code>.
   */
  public synchronized OutPort lookup(String url) {
      //url = url.toLowerCase();
    String proto = extractProto(url);
    CaseInsensitiveString protoTmp = new CaseInsensitiveString(proto);
    OutPortList l = (OutPortList)outPorts.get(protoTmp);
    if(l != null)
      return l.get();
    else
      return null;
  }

  public synchronized Iterator getAddresses() {
    return platformAddresses.iterator();
  }

  private void addOutPort(String proto, OutPort port, boolean location) {
      //proto = proto.toLowerCase();
      CaseInsensitiveString protoTmp = new CaseInsensitiveString(proto);
    OutPortList l = (OutPortList)outPorts.get(protoTmp);
    if(l != null)
      l.add(port, location);
    else {
      l = new OutPortList();
      l.add(port, location);
      outPorts.put(protoTmp, l);
    }
  }

  private void removeOutPort(String proto, OutPort port) {
      //proto = proto.toLowerCase();
      CaseInsensitiveString protoTmp = new CaseInsensitiveString(proto);
    OutPortList l = (OutPortList)outPorts.get(protoTmp);
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

