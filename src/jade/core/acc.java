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

import jade.mtp.InChannel;
import jade.mtp.OutChannel;
import jade.mtp.MTP;
import jade.mtp.MTPException;
import jade.mtp.TransportAddress;

import jade.domain.FIPAAgentManagement.Envelope;
import jade.domain.FIPAAgentManagement.ReceivedObject;

/**
  Standard <em>Agent Communication Channel</em>. This class implements
  <em><b>FIPA</b></em> <em>ACC</em> service. <b>JADE</b> applications
  cannot use this class directly, but interact with it transparently
  when using <em>ACL</em> message passing.

  @author Giovanni Rimassa - Universita` di Parma
  @version $Date$ $Revision$

*/
class acc implements InChannel.Dispatcher {

  public static class NoMoreAddressesException extends NotFoundException {
    NoMoreAddressesException(String msg) {
      super(msg);
    }
  }
  public static class UnknownACLEncodingException extends NotFoundException {
    UnknownACLEncodingException(String msg) {
      super(msg);
    }
  }

  private Map messageEncodings = new TreeMap(String.CASE_INSENSITIVE_ORDER);
  private RoutingTable routes = new RoutingTable();

  private List localAddresses = new LinkedList();
  private AgentContainerImpl myContainer;
  private String accID;
  private AIDTranslator translator;

  public acc(AgentContainerImpl ac, String platformID) {
  	
  	ACLCodec stringCodec = new StringACLCodec();
    addACLCodec(stringCodec);
    myContainer = ac;
    accID = "fipa-mts://" + platformID + "/acc";
    translator = new AIDTranslator(platformID);
  }
  
  protected void addACLCodec(ACLCodec codec){
  
  	messageEncodings.put(codec.getName().toLowerCase(),codec);
  	
  }

  /*
  * This method is called by ACCProxy before preparing the Envelope of an outgoing message.
  * It checks for all the AIDs present in the message and adds the addresses, if not present
  **/
  public void addPlatformAddresses(AID id) {
    Iterator it = routes.getAddresses();
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
    if(from == null) {
      env.setFrom(msg.getSender());
    }

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

    Long payloadLength = env.getPayloadLength();
    if(payloadLength == null)
      env.setPayloadLength(new Long(-1));

    String payloadEncoding = env.getPayloadEncoding();
    if(payloadEncoding == null)
      env.setPayloadEncoding("");

  }

  public byte[] encodeMessage(ACLMessage msg) throws NotFoundException {
    Envelope env = msg.getEnvelope();
    String enc = env.getAclRepresentation();
    //System.out.println("Using coding: " + enc);
 		/*ACLCodec codec = (ACLCodec)messageEncodings.get(enc.toLowerCase());
    if(codec == null) 
      throw new UnknownACLEncodingException("Unknown ACL encoding: " + enc + ".");
    return codec.encode(msg);*/
    
    if(enc != null)
    {//a Codec has been selected
    	ACLCodec codec =(ACLCodec)messageEncodings.get(enc.toLowerCase());
    	if(codec!=null){
    		//supported Codec
    		//FIXME:should be verified that the recevivers supports this Codec
    		return codec.encode(msg);
    	}else{
    		//unsupported Codec
    		//FIXME:find the best according to the supported, the MTP (and the receivers Codec)
    		throw new UnknownACLEncodingException("Unknown ACL encoding: " + enc + ".");
    	}
    }else{
    	//no codec indicated. 
    	//FIXME:find the better according to the supported Codec, the MTP (and the receiver codec)
    	throw new UnknownACLEncodingException("No ACL encoding set.");
    }
  }

  public void forwardMessage(Envelope env, byte[] payload, String address) throws MTPException {
    OutChannel out = routes.lookup(address);
    if(out != null)
      out.deliver(address, env, payload);
    else
      throw new MTPException("No suitable MTP found for address " + address + ".");
  }

  public AgentProxy getProxy(AID agentID) throws NotFoundException {
    return new ACCProxy(agentID, this);
  }

  public TransportAddress addMTP(MTP proto, String address) throws MTPException {
    if(address == null) { // Let the protocol choose the address
	/*
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
	*/
      TransportAddress ta = proto.activate(this);
      address = proto.addrToStr(ta);
      routes.addLocalMTP(address, proto);
      localAddresses.add(address); // FIXME: This is for the temporary fault-tolerance support
      return ta;
    }
    else { // Convert the given string into a TransportAddress object and use it
      routes.addLocalMTP(address, proto);
      localAddresses.add(address); // FIXME: This is for the temporary fault-tolerance support
      /*
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
      */
      TransportAddress ta = proto.strToAddr(address);
      proto.activate(this, ta);
      return ta;
    }
  }

  public void removeMTP(String address) throws MTPException {
    MTP proto = routes.removeLocalMTP(address);
    if(proto != null) {
      TransportAddress ta = proto.strToAddr(address);
      proto.deactivate(ta);
    }
    localAddresses.remove(address); // FIXME: This is for temporary fault-tolerance support
  }

  // FIXME: This info will be cached within the agent platform Smart
  // Proxy (handling caching and reconnection). Then this method won't
  // be needed anymore.
  public List getLocalAddresses() {
    return localAddresses;
  }

  public void addRoute(String address, AgentContainer ac) {
    routes.addRemoteMTP(address, ac);
  }

  public void removeRoute(String address, AgentContainer ac) {
    routes.removeRemoteMTP(address, ac);
  }

  public void shutdown() {
      /*
    // Remove all locally installed MTPs
    while(!localAddresses.isEmpty()) {
      String addr = (String)localAddresses.get(0);
      try {
	myContainer.uninstallMTP(addr);
      }
      catch(java.rmi.RemoteException re) {
	// It should never happen
	re.printStackTrace();
      }
      catch(NotFoundException nfe) {
	System.out.println("Failed to find MTP [" + addr + "]");
	nfe.printStackTrace();
      }
    }
      */
  }

  public void dispatchMessage(Envelope env, byte[] payload) {

    // To avoid message loops, make sure that the ID of this ACC does
    // not appear in a previous 'received' stamp
  	
  	
    ReceivedObject[] stamps = env.getStamps();
    for(int i = 0; i < stamps.length; i++) {
      String id = stamps[i].getBy();
      if(id.equalsIgnoreCase(accID)) {
	System.out.println("ERROR: Message loop detected !!!");
	System.out.println("Route is: ");
	for(int j = 0; j < stamps.length; j++)
	  System.out.println("[" + j + "]" + stamps[j].getBy());
	System.out.println("Message dispatch aborted.");
	return;
      }
    }

    // Put a 'received-object' stamp in the envelope
    ReceivedObject ro = new ReceivedObject();
    ro.setBy(accID);
    ro.setDate(new Date());

    env.setReceived(ro);

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
      msg.setEnvelope(env);

      // Perform AID translations in ':sender' and ':receiver' slots.
      translator.translateRouted(msg);

      // If the 'sender' AID has no addresses, replace it with the
      // 'from' envelope slot
      AID sender = msg.getSender();
      if(sender == null) {
	System.out.println("ERROR: Trying to dispatch a message with a null sender.");
	System.out.println("Aborting send operation...");
	return;
      }
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

}
