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

import java.util.Date;
import java.util.Map;
import java.util.TreeMap;
import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.ArrayList;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.ACLCodec;
import jade.lang.acl.StringACLCodec;

import jade.mtp.MTP;
import jade.mtp.TransportAddress;

import jade.domain.FIPAAgentManagement.Envelope;

/**
  Standard <em>Agent Communication Channel</em>. This class implements
  <em><b>FIPA</b></em> <em>ACC</em> service. <b>JADE</b> applications
  cannot use this class directly, but interact with it transparently
  when using <em>ACL</em> message passing.

  @author Giovanni Rimassa - Universita` di Parma
  @version $Date$ $Revision$

*/
class acc implements MTP.Dispatcher {

  public static class NoMoreAddressesException extends NotFoundException {
    NoMoreAddressesException(String msg) {
      super(msg);
    }
  }

  private Map messageEncodings = new TreeMap(String.CASE_INSENSITIVE_ORDER);
  private Map localMTPs = new TreeMap(String.CASE_INSENSITIVE_ORDER);
  private RoutingTable routes = new RoutingTable();
  private List addresses = new LinkedList();
  private List localAddresses = new LinkedList();
  private AgentContainerImpl myContainer;

  public acc(AgentContainerImpl ac) {
    ACLCodec stringCodec = new StringACLCodec();
    messageEncodings.put(stringCodec.getName().toLowerCase(), stringCodec);
    myContainer = ac;
  }

  /*
  * This method is called by ACCProxy before preparing the Envelope of an outgoing message.
  * It checks for all the AIDs present in the message and adds the addresses, if not present
  **/
  public void addPlatformAddresses(AID id) {
    Iterator it = addresses.iterator();
    while(it.hasNext()) {
      String addr = (String)it.next();
      id.addAddresses(addr);
    }
  }

  public void prepareEnvelope(ACLMessage msg, AID receiver) {
    Envelope env = msg.getEnvelope();
    if(env == null) {
      msg.setDefaultEnvelope();
      env = msg.getEnvelope();
    }

    // If no 'to' slot is present, copy the 'to' slot from the
    // 'receiver' slot of the ACL message
    Iterator itTo = env.getAllTo();
    if(!itTo.hasNext()) {
      Iterator itReceiver = msg.getAllReceiver();
      while(itReceiver.hasNext())
	env.addTo((AID)itReceiver.next());
    }

    // If no 'from' slot is present, copy the 'from' slot from the
    // 'sender' slot of the ACL message
    AID from = env.getFrom();
    if(from == null)
      env.setFrom(msg.getSender());

    // Set the 'date' slot to 'now' if not present already
    Date d = env.getDate();
    if(d == null)
      env.setDate(new Date());

    // If no ACL representation is found, then default to String
    // representation
    String rep = env.getAclRepresentation();
    if(rep == null)
      env.setAclRepresentation(StringACLCodec.NAME);

    // Write 'intended-receiver' slot as per 'FIPA Agent Message
    // Transport Service Specification': this ACC splits all
    // multicasts, since JADE has already split them in the
    // handleSend() method
    env.clearAllIntendedReceiver();
    env.addIntendedReceiver(receiver);

    String comments = env.getComments();
    if(comments == null)
      env.setComments("");

    String aclRepresentation = env.getAclRepresentation();
    if(aclRepresentation == null)
      env.setAclRepresentation("");

    String payloadLength = env.getPayloadLength();
    if(payloadLength == null)
      env.setPayloadLength("-1");

    String payloadEncoding = env.getPayloadEncoding();
    if(payloadEncoding == null)
      env.setPayloadEncoding("");

  }

  public byte[] encodeMessage(ACLMessage msg) throws NotFoundException {
    Envelope env = msg.getEnvelope();
    String enc = env.getAclRepresentation();
    ACLCodec codec = (ACLCodec)messageEncodings.get(enc.toLowerCase());
    if(codec == null)
      throw new NotFoundException("Unknown ACL encoding: " + enc + ".");
    return codec.encode(msg);
  }

  public void forwardMessage(Envelope env, byte[] payload, String address) throws MTP.MTPException {

    boolean ok = routeMessage(env, payload, address);

    if(!ok) { // Try to find another container who can route the outgoing message
      String proto = extractProto(address);
      if(proto == null)
	throw new MTP.MTPException("Missing protocol delimiter");
      AgentContainer ac = routes.lookup(proto);
      if(ac == null)
	throw new MTP.MTPException("MTP not available in this platform");
      try {
	ac.route(env, payload, address);
      }
      catch(java.rmi.RemoteException re) {
	throw new MTP.MTPException("Container unreachable during routing");
      }
      catch(NotFoundException nfe) {
	throw new MTP.MTPException("MTP not available in this platform [after routing]");
      }

    }

  }

  // This method returns true when a local MTP is found and the
  // message is delivered.
  public boolean routeMessage(Object o, byte[] payload, String address) {
    String proto = extractProto(address);
    if(proto == null)
      return false;

    MTP outGoing = (MTP)localMTPs.get(proto);
    if(outGoing != null) { // A suitable MTP is installed in this container.
      try {
	TransportAddress ta = outGoing.strToAddr(address);
	Envelope env = (Envelope)o;
	outGoing.deliver(ta, env, payload);
	return true;
      }
      catch(MTP.MTPException mtpe) {
	return false;
      }
    }
    else
      return false;
  }

  public AgentProxy getProxy(AID agentID) throws NotFoundException {
    return new ACCProxy(agentID, this);
  }

  public TransportAddress addMTP(MTP proto, String address) throws MTP.MTPException {
    if(address == null) { // Let the protocol choose the address
      localMTPs.put(proto.getName(), proto);
      if(proto.getName().equalsIgnoreCase("iiop")) {
	localMTPs.put("ior", proto);
	localMTPs.put("corbaloc", proto);
	localMTPs.put("corbaname", proto);
      }
      if(proto.getName().equalsIgnoreCase("ior")) {
	localMTPs.put("iiop", proto);
	localMTPs.put("corbaloc", proto);
	localMTPs.put("corbaname", proto);
      }
      if(proto.getName().equalsIgnoreCase("corbaloc")) {
	localMTPs.put("ior", proto);
	localMTPs.put("iiop", proto);
	localMTPs.put("corbaname", proto);
      }
      if(proto.getName().equalsIgnoreCase("corbaname")) {
	localMTPs.put("ior", proto);
	localMTPs.put("iiop", proto);
	localMTPs.put("corbaloc", proto);
      }
      TransportAddress ta = proto.activate(this);
      localAddresses.add(proto.addrToStr(ta));
      return ta;
    }
    else { // Convert the given string into a TransportAddress object and use it
      TransportAddress ta = proto.strToAddr(address);
      localMTPs.put(ta.getProto(), proto);
      if(ta.getProto().equalsIgnoreCase("iiop")) {
	localMTPs.put("ior", proto);
	localMTPs.put("corbaloc", proto);
	localMTPs.put("corbaname", proto);
      }
      if(ta.getProto().equalsIgnoreCase("ior")) {
	localMTPs.put("iiop", proto);
	localMTPs.put("corbaloc", proto);
	localMTPs.put("corbaname", proto);
      }
      if(ta.getProto().equalsIgnoreCase("corbaloc")) {
	localMTPs.put("ior", proto);
	localMTPs.put("iiop", proto);
	localMTPs.put("corbaname", proto);
      }
      if(ta.getProto().equalsIgnoreCase("corbaname")) {
	localMTPs.put("ior", proto);
	localMTPs.put("iiop", proto);
	localMTPs.put("corbaloc", proto);
      }
      proto.activate(this, ta);
      return ta;
    }

  }

  public List getLocalAddresses() {
    return localAddresses;
  }

  public void addRoute(String address, AgentContainer ac) {
    String proto = extractProto(address);
    routes.addMTP(proto, ac);
    addresses.add(address);
  }

  public void removeRoute(String address, AgentContainer ac) {
    String proto = extractProto(address);
    routes.removeMTP(proto, ac);
    addresses.remove(address);
  }

  public void shutdown() {
    // FIXME: To be implemented
  }

  public void dispatchMessage(Envelope env, byte[] payload) {

    // Decode the message, according to the 'acl-representation' slot
    String aclRepresentation = env.getAclRepresentation();

    // Default to String representation
    if(aclRepresentation == null)
      aclRepresentation = StringACLCodec.NAME;

    ACLCodec codec = (ACLCodec)messageEncodings.get(aclRepresentation.toLowerCase());
    if(codec == null) {
      System.out.println("Unknown ACL codec: " + aclRepresentation);
      return;
    }

    try {
      ACLMessage msg = codec.decode(payload);

      // If the 'sender' AID has no addresses, replace it with the
      // 'from' envelope slot
      AID sender = msg.getSender();
      Iterator itSender = sender.getAllAddresses();
      if(!itSender.hasNext())
	       msg.setSender(env.getFrom());

      Iterator it = env.getAllIntendedReceiver();
      // If no 'intended-receiver' is present, use the 'to' slot (but
      // this should not happen).
      if(!it.hasNext())
  	    it = env.getAllTo();
      while(it.hasNext()) {
	AID receiver = (AID)it.next();
	myContainer.unicastPostMessage(msg, receiver);
      }

    }
    catch(ACLCodec.CodecException ce) {
      ce.printStackTrace();
    }
  }

  private String extractProto(String address) {
    int colonPos = address.indexOf(':');
    if(colonPos == -1)
      return null;
    return address.substring(0, colonPos);
  }

}
