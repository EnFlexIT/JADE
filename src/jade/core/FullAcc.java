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
import java.util.HashMap;
import jade.util.leap.List;
import jade.util.leap.ArrayList;
import jade.util.leap.Iterator;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;

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
class FullAcc implements acc, InChannel.Dispatcher {

  private Map messageEncodings = new HashMap();
  private RoutingTable routes = new RoutingTable(this);

  private List localAddresses = new ArrayList();
  private AgentContainerImpl myContainer;
  private String accID;

  public FullAcc() {
    ACLCodec stringCodec = new StringACLCodec();
    messageEncodings.put(stringCodec.getName().toLowerCase(), stringCodec);
    //addACLCodec(stringCodec);
  }

  public void initialize(AgentContainerImpl ac, Profile p) {
  	// Initialize its own ID
  	try {
      String platformID = p.getParameter(Profile.PLATFORM_ID);
      myContainer = ac;
      accID = "fipa-mts://" + platformID + "/acc";
  	}
    catch(ProfileException pe) {
      accID = "fipa-mts://" + "localhost:0/JADE";
    }
      	
    // Install all ACL Codecs and MTPs specified in the Profile
    try {
    	// Codecs
    	List l = p.getSpecifiers(Profile.ACLCODECS);
    	Iterator codecs = l.iterator();
    	while (codecs.hasNext()) {
    		Specifier codec = (Specifier) codecs.next();
	      myContainer.installACLCodec(codec.getClassName());
      }

      // MTPs
      l = p.getSpecifiers(Profile.MTPS);
      PrintWriter f = new PrintWriter(new FileWriter("MTPs-" + myContainer.here().getName() + ".txt"));

      Iterator mtps = l.iterator();
      while (mtps.hasNext()) {
      	Specifier spec = (Specifier) mtps.next();
				String className = spec.getClassName();
				String addressURL = null;
				Object[] args = spec.getArgs();
				if (args != null && args.length > 0) {
					addressURL = args[0].toString();
					if(addressURL.equals("")) {
	  				addressURL = null;
					}
				}
				String s = myContainer.installMTP(addressURL, className);
				f.println(s);
				System.out.println(s);
      }

      f.close();      
    }
    catch (ProfileException pe1) {
    	System.out.println("Error reading MTPs/Codecs");
    	pe1.printStackTrace();
    }
    catch(jade.lang.acl.ACLCodec.CodecException ce) {
    	System.out.println("Error installing ACL Codec");
    	ce.printStackTrace();
		}    	
    catch(MTPException me) {
    	System.out.println("Error installing MTP");
    	me.printStackTrace();
		}    	
    catch(IOException ioe) {
    	System.out.println("Error writing platform address");
    	ioe.printStackTrace();
		}    	
    catch(IMTPException imtpe) {
    	// Should never happen as this is a local call
    	imtpe.printStackTrace();
		}    	
  }

  public void addACLCodec(String codecClassName) throws jade.lang.acl.ACLCodec.CodecException{
    try{
      Class c = Class.forName(codecClassName);
      ACLCodec codec = (ACLCodec)c.newInstance(); 
    	messageEncodings.put(codec.getName().toLowerCase(), codec);
      System.out.println("Installed "+ codec.getName()+ " ACLCodec implemented by " + codecClassName +"\n");
      // FIXME: notify the AMS of the new Codec to update the APDescritption.
    }
    catch(ClassNotFoundException cnfe){
      throw new jade.lang.acl.ACLCodec.CodecException("ERROR: The class " +codecClassName +" for the ACLCodec not found.",cnfe);
    }
    catch(InstantiationException ie) {
      throw new jade.lang.acl.ACLCodec.CodecException("The class " + codecClassName + " raised InstantiationException (see NestedException)",ie);
    }
    catch(IllegalAccessException iae) {
      throw new jade.lang.acl.ACLCodec.CodecException("The class " + codecClassName  + " raised IllegalAccessException (see nested exception)", iae);
    }
  		
  }

  /*
  * This method is called before preparing the Envelope of an outgoing message.
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
    		throw new acc.UnknownACLEncodingException("Unknown ACL encoding: " + enc + ".");
    	}
    }else{
    	//no codec indicated. 
    	//FIXME:find the better according to the supported Codec, the MTP (and the receiver codec)
    	throw new acc.UnknownACLEncodingException("No ACL encoding set.");
    }
  }

  public void dispatch(ACLMessage msg, AID receiverID) throws NotFoundException {

    Iterator addresses = receiverID.getAllAddresses();
    while(addresses.hasNext()) {
      String address = (String)addresses.next();
      try {
	forwardMessage(msg, receiverID, address);
	return;
      }
      catch(MTPException mtpe) {
	System.out.println("Bad address [" + address + "]: trying the next one...");
      }
    }
    throw new NotFoundException("No valid address contained within the AID.");
  }

  public void forwardMessage(ACLMessage msg, AID receiver, String address) throws MTPException {

    AID aid = msg.getSender();
    if(aid == null) {
      System.out.println("ERROR: null message sender. Aborting message dispatch...");
      return;
    }

    // if has no address set, then adds the addresses of this platform
    if(!aid.getAllAddresses().hasNext())
      addPlatformAddresses(aid);

    Iterator it1 = msg.getAllReceiver();
    while(it1.hasNext()) {
      AID id = (AID)it1.next();
      if(!id.getAllAddresses().hasNext())
	addPlatformAddresses(id);
    }

    Iterator it2 = msg.getAllReplyTo();
    while(it2.hasNext()) {
      AID id = (AID)it2.next();
      if(!id.getAllAddresses().hasNext())
	addPlatformAddresses(id);
    }

    OutPort out = routes.lookup(address);
    if(out != null)
      out.route(msg, receiver, address);
    else
      throw new MTPException("No suitable route found for address " + address + ".");
  }

  public String addMTP(String mtpClassName, String address) throws MTPException { 
  	try {
  		// Create the MTP
      Class c = Class.forName(mtpClassName);
      MTP proto = (MTP)c.newInstance();
      
    	if(address == null) { 
    		// Let the MTP choose the address
      	TransportAddress ta = proto.activate(this);
      	address = proto.addrToStr(ta);
    	}
    	else { 
    		// Convert the given string into a TransportAddress object and use it
      	TransportAddress ta = proto.strToAddr(address);
      	proto.activate(this, ta);
    	}
      routes.addLocalMTP(address, proto);
      localAddresses.add(address); // FIXME: This is for the temporary fault-tolerance support
      return address;
    }
    catch(ClassNotFoundException cnfe) {
      throw new MTPException("ERROR: The class " + mtpClassName + " for the " + address  + " MTP was not found");
    }
    catch(InstantiationException ie) {
      throw new MTPException("The class " + mtpClassName + " raised InstantiationException (see nested exception)", ie);
    }
    catch(IllegalAccessException iae) {
      throw new MTPException("The class " + mtpClassName  + " raised IllegalAccessException (see nested exception)", iae);
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
    // Close all MTP links to the outside world
    Object[] addresses = localAddresses.toArray();
    for(int i = 0; i < addresses.length; i++) {
      try {
				String addr = (String) addresses[i];
				myContainer.uninstallMTP(addr);
      }
      catch(IMTPException imtpe) {
				// It should never happen as this is a local call
      	imtpe.printStackTrace();
      }
      catch(NotFoundException nfe) {
				nfe.printStackTrace();
      }
      catch(MTPException mtpe) {
				mtpe.printStackTrace();
      }
    }
  }

  public void dispatchMessage(Envelope env, byte[] payload) {

    // To avoid message loops, make sure that the ID of this ACC does
    // not appear in a previous 'received' stamp
  	
  	
    ReceivedObject[] stamps = env.getStamps();
    for(int i = 0; i < stamps.length; i++) {
      String id = stamps[i].getBy();
      if(CaseInsensitiveString.equalsIgnoreCase(id, accID)) {
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
	myContainer.routeIn(msg, receiver);
      }

    }
    catch(ACLCodec.CodecException ce) {
      ce.printStackTrace();
    }
  }

}
