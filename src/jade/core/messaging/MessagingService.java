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

import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;

import java.util.Date;


import jade.core.ServiceFinder;

import jade.core.HorizontalCommand;
import jade.core.VerticalCommand;
import jade.core.GenericCommand;
import jade.core.Service;
import jade.core.BaseService;
import jade.core.ServiceException;
import jade.core.Sink;
import jade.core.Filter;
import jade.core.Node;

import jade.core.AgentContainer;
import jade.core.MainContainer;
import jade.core.CaseInsensitiveString;
import jade.core.AID;
import jade.core.ContainerID;
import jade.core.Profile;
import jade.core.Specifier;
import jade.core.ProfileException;
import jade.core.IMTPException;
import jade.core.NotFoundException;
import jade.core.UnreachableException;

import jade.domain.FIPAAgentManagement.InternalError;
import jade.domain.FIPAAgentManagement.Envelope;
import jade.domain.FIPAAgentManagement.ReceivedObject;

import jade.security.Authority;
import jade.security.AgentPrincipal;
import jade.security.PrivilegedExceptionAction;
import jade.security.AuthException;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.ACLCodec;

import jade.lang.acl.StringACLCodec;

import jade.mtp.MTP;
import jade.mtp.MTPDescriptor;
import jade.mtp.MTPException;
import jade.mtp.InChannel;
import jade.mtp.TransportAddress;

import jade.util.leap.Iterator;
import jade.util.leap.Map;
import jade.util.leap.HashMap;
import jade.util.leap.List;



/**

   The JADE service to manage the message passing subsystem installed
   on the platform.

   @author Giovanni Rimassa - FRAMeTech s.r.l.

*/
public class MessagingService extends BaseService implements MessageManager.Channel {

    public static class UnknownACLEncodingException extends NotFoundException {
      UnknownACLEncodingException(String msg) {
	super(msg);
      }
    } // End of UnknownACLEncodingException class


    public static final String MAIN_SLICE = "Main-Container";

    private static final String[] OWNED_COMMANDS = new String[] {
	MessagingSlice.SEND_MESSAGE,
	MessagingSlice.INSTALL_MTP,
	MessagingSlice.UNINSTALL_MTP,
	MessagingSlice.SET_PLATFORM_ADDRESSES
    };

    public MessagingService(AgentContainer ac, Profile p) throws ProfileException {
	super(p);

	myContainer = ac;

	//#MIDP_EXCLUDE_BEGIN
	cachedSlices = new jade.util.HashCache(100); // FIXME: Cache size should be taken from the profile
	//#MIDP_EXCLUDE_END

	/*#MIDP_INCLUDE_BEGIN
	cachedSlices = new HashMap();
	#MIDP_INCLUDE_END*/

	// Initialize its own ID
	String platformID = myContainer.getPlatformID();
	accID = "fipa-mts://" + platformID + "/acc";

	myMessageManager = MessageManager.instance(p);

	// Create a local slice
	localSlice = new ServiceComponent();

	// Create the two command sinks for this service
	senderSink = new CommandSourceSink();
	receiverSink = new CommandTargetSink();

    }

    public String getName() {
	return MessagingSlice.NAME;
    }

    public Class getHorizontalInterface() {
	try {
	    return Class.forName(MessagingSlice.NAME + "Slice");
	}
	catch(ClassNotFoundException cnfe) {
	    return null;
	}
    }

    public Service.Slice getLocalSlice() {
	return localSlice;
    }

    public Filter getCommandFilter(boolean direction) {
	return null;
    }

    public Sink getCommandSink(boolean side) {
	if(side == Sink.COMMAND_SOURCE) {
	    return senderSink;
	}
	else {
	    return receiverSink;
	}
    }

    public String[] getOwnedCommands() {
	return OWNED_COMMANDS;
    }

    // This inner class handles the messaging commands on the command
    // issuer side, turning them into horizontal commands and
    // forwarding them to remote slices when necessary.
    private class CommandSourceSink implements Sink {

	// Implementation of the Sink interface

	public void consume(VerticalCommand cmd) {

	    try {
		String name = cmd.getName();
		if(name.equals(MessagingSlice.SEND_MESSAGE)) {
		    handleSendMessage(cmd);
		}
		if(name.equals(MessagingSlice.INSTALL_MTP)) {
		    Object result = handleInstallMTP(cmd);
		    cmd.setReturnValue(result);
		}
		else if(name.equals(MessagingSlice.UNINSTALL_MTP)) {
		    handleUninstallMTP(cmd);
		}
		else if(name.equals(MessagingSlice.SET_PLATFORM_ADDRESSES)) {
		    handleSetPlatformAddresses(cmd);
		}
	    }
	    catch(AuthException ae) {
		cmd.setReturnValue(ae);
	    }
	    catch(IMTPException imtpe) {
		imtpe.printStackTrace();
	    }
	    catch(NotFoundException nfe) {
		nfe.printStackTrace();
	    }
	    catch(ServiceException se) {
		se.printStackTrace();
	    }
	    catch(MTPException mtpe) {
		mtpe.printStackTrace();
	    }
	}

	// Vertical command handler methods

	public void handleSendMessage(VerticalCommand cmd) throws AuthException {
	    Object[] params = cmd.getParams();
	    ACLMessage msg = (ACLMessage)params[0];
	    AID sender = (AID)params[1];

	    // Set the sender unless already set
	    try {
		if (msg.getSender().getName().length() < 1)
		    msg.setSender(sender);
	    }
	    catch (NullPointerException e) {
		msg.setSender(sender);
	    }


	    // --- This code could go into a Security Service, intercepting the message sending...

	    AgentPrincipal target1 = myContainer.getAgentPrincipal(msg.getSender());

	    Authority authority = myContainer.getAuthority();
	    authority.checkAction(Authority.AGENT_SEND_AS, target1, null);

	    // --- End of security code		


	    AuthException lastException = null;

	    // 26-Mar-2001. The receivers set into the Envelope of the message, 
	    // if present, must have precedence over those set into the ACLMessage.
	    // If no :intended-receiver parameter is present in the Envelope, 
	    // then the :to parameter
	    // is used to generate :intended-receiver field. 
	    //
	    // create an Iterator with all the receivers to which the message must be 
	    // delivered
	    Iterator it = msg.getAllIntendedReceiver();

	    while (it.hasNext()) {
		AID dest = (AID)it.next();
		try {
		    AgentPrincipal target2 = myContainer.getAgentPrincipal(dest);
		    authority.checkAction(Authority.AGENT_SEND_TO, target2, null);
		    ACLMessage copy = (ACLMessage)msg.clone();

		    boolean found = myContainer.postMessageToLocalAgent(copy, dest);
		    if(!found) {
			myMessageManager.deliver(copy, dest, MessagingService.this);
		    }
		}
		catch (AuthException ae) {
		    lastException = ae;
		    notifyFailureToSender(msg, dest, new InternalError(ae.getMessage()));
		}
	    }

	    if(lastException != null)
		throw lastException;
	}

	private MTPDescriptor handleInstallMTP(VerticalCommand cmd) throws IMTPException, ServiceException, NotFoundException, MTPException {
	    Object[] params = cmd.getParams();
	    String address = (String)params[0];
	    ContainerID cid = (ContainerID)params[1];
	    String className = (String)params[2];

	    MessagingSlice targetSlice = (MessagingSlice)getSlice(cid.getName());
	    return targetSlice.installMTP(address, className);
	}

	private void handleUninstallMTP(VerticalCommand cmd) throws IMTPException, ServiceException, NotFoundException, MTPException {
	    Object[] params = cmd.getParams();
	    String address = (String)params[0];
	    ContainerID cid = (ContainerID)params[1];

	    MessagingSlice targetSlice = (MessagingSlice)getSlice(cid.getName());
	    targetSlice.uninstallMTP(address);
	}

	private void handleSetPlatformAddresses(VerticalCommand cmd) {
	    Object[] params = cmd.getParams();
	    AID id = (AID)params[0];
	    id.clearAllAddresses();
	    addPlatformAddresses(id);
	}

    } // End of CommandSourceSink class


    private class CommandTargetSink implements Sink {

	public void consume(VerticalCommand vCmd) {
	    System.out.println("--- Consuming command <" + vCmd.getName() + "> ---");
	}

    } // End of CommandTargetSink class


    /**
       Inner class for this service: this class receives commands from
       service <code>Sink</code> and serves them, coordinating with
       remote parts of this service through the <code>Slice</code>
       interface (that extends the <code>Service.Slice</code>
       interface).
    */
    private class ServiceComponent implements MessagingSlice {

	public Iterator getAddresses() {
	    return routes.getAddresses();
	}

	// Entry point for the ACL message dispatching process
	public void deliverNow(ACLMessage msg, AID receiverID) throws UnreachableException, NotFoundException {
	    try {
		MainContainer impl = myContainer.getMain();
		if(impl != null) {
		    while(true) {
			// Directly use the GADT on the main container
			ContainerID cid = impl.getContainerID(receiverID);
			MessagingSlice targetSlice = (MessagingSlice)getSlice(cid.getName());
			try {
			    targetSlice.dispatchLocally(msg, receiverID);
			    return; // Message dispatched
			}
			catch(NotFoundException nfe) {
			    // The agent was found in he GADT, but not in the target LADT => try again
			}
		    }
		}
		else {

		    // Try first with the cached <AgentID;Container ID> pairs
		    MessagingSlice cachedSlice = (MessagingSlice)cachedSlices.get(receiverID);
		    if(cachedSlice != null) { // Cache hit :-)
			try {
			    //System.out.println("--- Cache Hit for AID [" + receiverID.getLocalName() + "] ---");
			    cachedSlice.dispatchLocally(msg, receiverID);
			}
			catch(IMTPException imtpe) {
			    cachedSlices.remove(receiverID); // Eliminate stale cache entry
			    deliverUntilOK(msg, receiverID);
			}
			catch(NotFoundException nfe) {
			    cachedSlices.remove(receiverID); // Eliminate stale cache entry
			    deliverUntilOK(msg, receiverID);
			}
		    }
		    else { // Cache miss :-(
			//System.out.println("--- Cache Miss for AID [" + receiverID.getLocalName() + "] ---");
			deliverUntilOK(msg, receiverID);
		    }
		}
	    }
	    catch(IMTPException imtpe) {
		throw new UnreachableException("Unreachable network node", imtpe);
	    }
	    catch(ServiceException se) {
		throw new UnreachableException("Unreachable service slice:", se);
	    }
	}

	private void deliverUntilOK(ACLMessage msg, AID receiverID) throws IMTPException, NotFoundException, ServiceException {
	    boolean ok = false;
	    do {
		MessagingSlice mainSlice = (MessagingSlice)getSlice(MAIN_SLICE);
		ContainerID cid = mainSlice.getAgentLocation(receiverID);

		MessagingSlice targetSlice = (MessagingSlice)getSlice(cid.getName());
		try {
		    targetSlice.dispatchLocally(msg, receiverID);
		    System.out.println("--- New Container for AID " + receiverID.getLocalName() + " is " + cid.getName() + " ---");
		    // On successful message dispatch, put the slice into the slice cache
		    cachedSlices.put(receiverID, targetSlice);
		    ok = true;
		}
		catch(NotFoundException nfe) {
		    ok = false; // Stale proxy again, maybe the receiver is running around. Try again...
		}

	    } while(!ok);
	}


	// Implementation of the Service.Slice interface

	public Service getService() {
	    return MessagingService.this;
	}

	public Node getNode() throws ServiceException {
	    try {
		return MessagingService.this.getLocalNode();
	    }
	    catch(IMTPException imtpe) {
		throw new ServiceException("Problem in contacting the IMTP Manager", imtpe);
	    }
	}

	public VerticalCommand serve(HorizontalCommand cmd) {
	    VerticalCommand result = null;
	    try {
		String cmdName = cmd.getName();
		Object[] params = cmd.getParams();

		if(cmdName.equals(H_DISPATCHLOCALLY)) {
		    ACLMessage msg = (ACLMessage)params[0];
		    AID receiverID = (AID)params[1];

		    dispatchLocally(msg, receiverID);
		    result = new GenericCommand(MessagingSlice.SEND_MESSAGE, MessagingSlice.NAME, null);
		}
		else if(cmdName.equals(H_ROUTEOUT)) {
		    ACLMessage msg = (ACLMessage)params[0];
		    AID receiverID = (AID)params[1];
		    String address = (String)params[2];

		    routeOut(msg, receiverID, address);
		}
		else if(cmdName.equals(H_GETAGENTLOCATION)) {
		    AID agentID = (AID)params[0];

		    cmd.setReturnValue(getAgentLocation(agentID));
		}
		else if(cmdName.equals(H_INSTALLMTP)) {
		    String address = (String)params[0];
		    String className = (String)params[1];

		    cmd.setReturnValue(installMTP(address, className));
		}
		else if(cmdName.equals(H_UNINSTALLMTP)) {
		    String address = (String)params[0];

		    uninstallMTP(address);
		}
		else if(cmdName.equals(H_NEWMTP)) {
		    MTPDescriptor mtp = (MTPDescriptor)params[0];
		    ContainerID cid = (ContainerID)params[1];

		    newMTP(mtp, cid);
		}
		else if(cmdName.equals(H_DEADMTP)) {
		    MTPDescriptor mtp = (MTPDescriptor)params[0];
		    ContainerID cid = (ContainerID)params[1];

		    deadMTP(mtp, cid);
		}
		else if(cmdName.equals(H_ADDROUTE)) {
		    MTPDescriptor mtp = (MTPDescriptor)params[0];
		    String sliceName = (String)params[1];

		    addRoute(mtp, sliceName);
		}
		else if(cmdName.equals(H_REMOVEROUTE)) {
		    MTPDescriptor mtp = (MTPDescriptor)params[0];
		    String sliceName = (String)params[1];

		    removeRoute(mtp, sliceName);
		}
	    }
	    catch(Throwable t) {
		cmd.setReturnValue(t);
	    }
	    finally {
		return result;
	    }
	}


	// Implementation of the service-specific horizontal interface MessagingSlice

	public void dispatchLocally(ACLMessage msg, AID receiverID) throws IMTPException, NotFoundException {
	    boolean found = myContainer.postMessageToLocalAgent(msg, receiverID);
	    if(!found) {
		throw new NotFoundException("Messaging service slice failed to find " + receiverID);
	    }
	}

	public void routeOut(ACLMessage msg, AID receiverID, String address) throws IMTPException, MTPException {
	    RoutingTable.OutPort out = routes.lookup(address);
	    if(out != null)
		out.route(msg, receiverID, address);
	    else
		throw new MTPException("No suitable route found for address " + address + ".");
	}

	public ContainerID getAgentLocation(AID agentID) throws IMTPException, NotFoundException {
	    MainContainer impl = myContainer.getMain();
	    if(impl != null) {
		return impl.getContainerID(agentID);
	    }
	    else {
		// Do nothing for now, but could also have a local GADT copy, thus enabling e.g. Main Container replication
		return null;
	    }
	}

	public MTPDescriptor installMTP(String address, String className) throws IMTPException, ServiceException, MTPException {

	    try {
		// Create the MTP
		Class c = Class.forName(className);
		MTP proto = (MTP)c.newInstance();

		InChannel.Dispatcher dispatcher = new InChannel.Dispatcher() {
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

				    boolean found = myContainer.postMessageToLocalAgent(msg, receiver);
				    if(!found) {
					myMessageManager.deliver(msg, receiver, MessagingService.this);
				    }
				}

			    }
			    catch(ACLCodec.CodecException ce) {
				ce.printStackTrace();
			    }
			}
		    };

		if(address == null) { 
		    // Let the MTP choose the address
		    TransportAddress ta = proto.activate(dispatcher);
		    address = proto.addrToStr(ta);
		}
		else { 
		    // Convert the given string into a TransportAddress object and use it
		    TransportAddress ta = proto.strToAddr(address);
		    proto.activate(dispatcher, ta);
		}
		routes.addLocalMTP(address, proto);
		MTPDescriptor result = new MTPDescriptor(proto.getName(), new String[] {address}, proto.getSupportedProtocols());

		MessagingSlice mainSlice = (MessagingSlice)getSlice(MAIN_SLICE);
		mainSlice.newMTP(result, myContainer.getID());
		return result;
	    }
	    catch(ClassNotFoundException cnfe) {
		throw new MTPException("ERROR: The class " + className + " for the " + address  + " MTP was not found");
	    }
	    catch(InstantiationException ie) {
		throw new MTPException("The class " + className + " raised InstantiationException (see nested exception)", ie);
	    }
	    catch(IllegalAccessException iae) {
		throw new MTPException("The class " + className  + " raised IllegalAccessException (see nested exception)", iae);
	    }
	}

	public void uninstallMTP(String address) throws IMTPException, ServiceException, NotFoundException, MTPException {

	    MTP proto = routes.removeLocalMTP(address);
	    if(proto != null) {
		TransportAddress ta = proto.strToAddr(address);
		proto.deactivate(ta);
		MTPDescriptor desc = new MTPDescriptor(proto.getName(), new String[] {address}, proto.getSupportedProtocols());

		MessagingSlice mainSlice = (MessagingSlice)getSlice(MAIN_SLICE);
		mainSlice.deadMTP(desc, myContainer.getID());
	    }
	    else {
		throw new MTPException("No such address was found on this container: " + address);
	    }
	}

	public void newMTP(MTPDescriptor mtp, ContainerID cid) throws IMTPException, ServiceException {
	    MainContainer impl = myContainer.getMain();

	    if(impl != null) {

		// Update the routing tables of all the other slices
		Service.Slice[] slices = getAllSlices();
		for(int i = 0; i < slices.length; i++) {
		    try {
			MessagingSlice slice = (MessagingSlice)slices[i];
			String sliceName = slice.getNode().getName();
			if(!sliceName.equals(cid.getName())) {
			    slice.addRoute(mtp, cid.getName());
			}
		    }
		    catch(Throwable t) {
			// Re-throw allowed exceptions
			if(t instanceof IMTPException) {
			    throw (IMTPException)t;
			}
			if(t instanceof ServiceException) {
			    throw (ServiceException)t;
			}
			System.out.println("### addRoute() threw " + t.getClass().getName() + " ###");
		    }
		}
		impl.newMTP(mtp, cid);
	    }
	    else {
		// Do nothing for now, but could also route the command to the main slice, thus enabling e.g. AMS replication
	    }
	}

	public void deadMTP(MTPDescriptor mtp, ContainerID cid) throws IMTPException, ServiceException {
	    MainContainer impl = myContainer.getMain();

	    if(impl != null) {

		// Update the routing tables of all the other slices
		Service.Slice[] slices = getAllSlices();
		for(int i = 0; i < slices.length; i++) {
		    try {
			MessagingSlice slice = (MessagingSlice)slices[i];
			String sliceName = slice.getNode().getName();
			if(!sliceName.equals(cid.getName())) {
			    slice.removeRoute(mtp, cid.getName());
			}
		    }
		    catch(Throwable t) {
			// Re-throw allowed exceptions
			if(t instanceof IMTPException) {
			    throw (IMTPException)t;
			}
			if(t instanceof ServiceException) {
			    throw (ServiceException)t;
			}
			System.out.println("### removeRoute() threw " + t.getClass().getName() + " ###");
		    }
		}
		impl.deadMTP(mtp, cid);
	    }
	    else {
		// Do nothing for now, but could also route the command to the main slice, thus enabling e.g. AMS replication
	    }
	}

	public void addRoute(MTPDescriptor mtp, String sliceName) throws IMTPException, ServiceException {

	    MessagingSlice slice = (MessagingSlice)getSlice(sliceName);
	    routes.addRemoteMTP(mtp, sliceName, slice);

	    String[] addresses = mtp.getAddresses();
	    for(int i = 0; i < addresses.length; i++) {
		myContainer.addAddressToLocalAgents(addresses[i]);
	    }
	}

	public void removeRoute(MTPDescriptor mtp, String sliceName) throws IMTPException, ServiceException {
	    MessagingSlice slice = (MessagingSlice)getSlice(sliceName);
	    routes.removeRemoteMTP(mtp, sliceName, slice);

	    String[] addresses = mtp.getAddresses();
	    for(int i = 0; i < addresses.length; i++) {
		myContainer.removeAddressFromLocalAgents(addresses[i]);
	    }
	}


	private RoutingTable routes = new RoutingTable(MessagingService.this);

    } // End of ServiceComponent class


    /**
       Activates the ACL codecs and MTPs as specified in the given
       <code>Profile</code> instance.
       @param myProfile The <code>Profile</code> instance containing
       the list of ACL codecs and MTPs to activate on this node.
    **/
    public void activateProfile(Profile myProfile) {

	try {

	    // Activate the default ACL String codec anyway
	    ACLCodec stringCodec = new StringACLCodec();
	    messageEncodings.put(stringCodec.getName().toLowerCase(), stringCodec);

	    // Codecs
	    List l = myProfile.getSpecifiers(Profile.ACLCODECS);
	    Iterator codecs = l.iterator();
	    while (codecs.hasNext()) {
		Specifier spec = (Specifier) codecs.next();
		String className = spec.getClassName();
		try{
		    Class c = Class.forName(className);
		    ACLCodec codec = (ACLCodec)c.newInstance(); 
		    messageEncodings.put(codec.getName().toLowerCase(), codec);
		    System.out.println("Installed "+ codec.getName()+ " ACLCodec implemented by " + className + "\n");
		    // FIXME: notify the AMS of the new Codec to update the APDescritption.
		}
		catch(ClassNotFoundException cnfe){
		    throw new jade.lang.acl.ACLCodec.CodecException("ERROR: The class " +className +" for the ACLCodec not found.", cnfe);
		}
		catch(InstantiationException ie) {
		    throw new jade.lang.acl.ACLCodec.CodecException("The class " + className + " raised InstantiationException (see NestedException)", ie);
		}
		catch(IllegalAccessException iae) {
		    throw new jade.lang.acl.ACLCodec.CodecException("The class " + className  + " raised IllegalAccessException (see nested exception)", iae);
		}
	    }

	    // MTPs
	    l = myProfile.getSpecifiers(Profile.MTPS);
	    String fileName = myProfile.getParameter(Profile.FILE_DIR, "") + "MTPs-" + myContainer.getID().getName() + ".txt";
	    PrintWriter f = new PrintWriter(new FileWriter(fileName));

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

		MTPDescriptor mtp = localSlice.installMTP(addressURL, className);
		String[] mtpAddrs = mtp.getAddresses();
		f.println(mtpAddrs[0]);
		System.out.println(mtpAddrs[0]);
	    }

	    f.close();      
	}
	catch (ProfileException pe1) {
	    System.out.println("Error reading MTPs/Codecs");
	    pe1.printStackTrace();
	}
	catch(ServiceException se) {
	    System.out.println("Error installing local MTPs");
	    se.printStackTrace();
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

    public void deliverNow(ACLMessage msg, AID receiverID) throws UnreachableException {
	try {
	    if(myContainer.livesHere(receiverID)) {
		localSlice.deliverNow(msg, receiverID);
	    }
	    else {
		// Dispatch it through the ACC
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
		notifyFailureToSender(msg, receiverID, new InternalError("No valid address contained within the AID " + receiverID.getName()));
	    }
	}
	catch(NotFoundException nfe) {
	    // The receiver does not exist --> Send a FAILURE message
	    notifyFailureToSender(msg, receiverID, new InternalError("Agent not found: " + nfe.getMessage()));
	}
    }


    private void forwardMessage(ACLMessage msg, AID receiver, String address) throws MTPException {

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

	try {
	    localSlice.routeOut(msg, receiver, address);
	}
	catch(IMTPException imtpe) {
	    throw new MTPException("Error during message routing", imtpe);
	}

    }


    /**
     * This method is used internally by the platform in order
     * to notify the sender of a message that a failure was reported by
     * the Message Transport Service.
     * Package scoped as it can be called by the MessageManager
     */
    public void notifyFailureToSender(ACLMessage msg, AID receiver, InternalError ie) {

	//if (the sender is not the AMS and the performative is not FAILURE)
	if ( (msg.getSender()==null) || ((msg.getSender().equals(myContainer.getAMS())) && (msg.getPerformative()==ACLMessage.FAILURE))) // sanity check to avoid infinte loops
	    return;
	// else send back a failure message
	final ACLMessage failure = msg.createReply();
	failure.setPerformative(ACLMessage.FAILURE);
	//System.err.println(failure.toString());
	final AID theAMS = myContainer.getAMS();
	failure.setSender(theAMS);

	// FIXME: the content is not completely correct, but that should
	// also avoid creating wrong content
	// FIXME: the content should include the indication about the 
      // receiver to wich dispatching failed.
	String content = "( (action " + msg.getSender().toString();
	content = content + " ACLMessage ) " + ie.getMessage() + ")";
	failure.setContent(content);

	try {
	    Authority authority = myContainer.getAuthority();
	    authority.doPrivileged(new PrivilegedExceptionAction() {
		    public Object run() {
			try {
			    // FIXME: Having a custom code path to send failure notifications would be better...
			    GenericCommand cmd = new GenericCommand(MessagingSlice.SEND_MESSAGE, MessagingSlice.NAME, null);
			    cmd.addParam(failure);
			    cmd.addParam(theAMS);
			    senderSink.handleSendMessage(cmd);
			} catch (AuthException ae) {
			    // it does not have permission to notify the failure 
			    // it never happens if the policy file gives 
			    // enough permission to the jade.jar 
			    System.out.println( ae.getMessage() );
			}
			return null; // nothing to return
		    }
		});
	} catch(Exception e) {
	    // should be never thrown
	    e.printStackTrace();
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

	if(enc != null) { // A Codec was selected
	    ACLCodec codec =(ACLCodec)messageEncodings.get(enc.toLowerCase());
	    if(codec!=null) {
    		// Supported Codec
    		// FIXME: should verifY that the recevivers supports this Codec
    		return codec.encode(msg);
	    }
	    else {
    		// Unsupported Codec
    		//FIXME: find the best according to the supported, the MTP (and the receivers Codec)
    		throw new UnknownACLEncodingException("Unknown ACL encoding: " + enc + ".");
	    }
	}
	else {
	    // no codec indicated. 
	    //FIXME: find the better according to the supported Codec, the MTP (and the receiver codec)
	    throw new UnknownACLEncodingException("No ACL encoding set.");
	}
    }


    /*
     * This method is called before preparing the Envelope of an outgoing message.
     * It checks for all the AIDs present in the message and adds the addresses, if not present
     **/
    private void addPlatformAddresses(AID id) {
	Iterator it = localSlice.getAddresses();
	while(it.hasNext()) {
	    String addr = (String)it.next();
	    id.addAddresses(addr);
	}
    }


    // The concrete agent container, providing access to LADT, etc.
    private final AgentContainer myContainer;

    // The local slice for this service
    private final ServiceComponent localSlice;

    // The command sink, source side
    private final CommandSourceSink senderSink;

    // The command sink, target side
    private final CommandTargetSink receiverSink;

    // The cached AID -> MessagingSlice associations
    private final Map cachedSlices;

    private final static int EXPECTED_ACLENCODINGS_SIZE = 3;
    // The table of the locally installed ACL message encodings
    private final Map messageEncodings = new HashMap(EXPECTED_ACLENCODINGS_SIZE);

    // The platform ID, to be used in inter-platform dispatching
    private final String accID;

    // The component managing asynchronous message delivery and retries
    private final MessageManager myMessageManager;


}
