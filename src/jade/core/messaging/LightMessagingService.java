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
import jade.core.ProfileException;
import jade.core.IMTPException;
import jade.core.NotFoundException;
import jade.core.UnreachableException;

import jade.domain.FIPAAgentManagement.InternalError;

import jade.security.Authority;
import jade.security.AgentPrincipal;
import jade.security.PrivilegedExceptionAction;
import jade.security.AuthException;

import jade.lang.acl.ACLMessage;

import jade.mtp.MTPDescriptor;
import jade.mtp.MTPException;

import jade.util.leap.Iterator;



/**

   A minimal version of the JADE service to manage the message passing
   subsystem installed on the platform. This clsss just supports
   direct ACL message delivery, and relies on another one for any
   other feature (such as message routing and MTP management).

   @author Giovanni Rimassa - FRAMeTech s.r.l.

*/
public class LightMessagingService extends BaseService implements MessageManager.Channel {

    public static final String MAIN_SLICE = "Main-Container";

    private static final String[] OWNED_COMMANDS = new String[] {
	MessagingSlice.SEND_MESSAGE,
	MessagingSlice.INSTALL_MTP,
	MessagingSlice.UNINSTALL_MTP,
	MessagingSlice.SET_PLATFORM_ADDRESSES
    };

    public void init(AgentContainer ac, Profile p) throws ProfileException {
	super.init(ac, p);

	myContainer = ac;

	// Initialize its own ID
	// String platformID = myContainer.getPlatformID();

	myMessageManager = MessageManager.instance(p);

	String helperSliceName = p.getParameter("accRouter", MAIN_SLICE);

	// Create a local slice
	localSlice = new ServiceComponent(helperSliceName);

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
	if(direction == Filter.OUTGOING) {
	    return localSlice;
	}
	else {
	    return null;
	}
    }

    public Sink getCommandSink(boolean side) {
	return null; 
    }

    public String[] getOwnedCommands() {
	return OWNED_COMMANDS;
    }

    /**
       Inner mix-in class for this service: this class receives
       commands through its <code>Filter</code> interface and serves
       them, coordinating with remote parts of this service through
       the <code>Slice</code> interface (that extends the
       <code>Service.Slice</code> interface).
    */
    private class ServiceComponent implements Filter, MessagingSlice {

	/**
	   Builds a new messaging service lightweight component,
	   relying on a remote slice for most operations.
	**/
	public ServiceComponent(String helperName) {
	    myHelperName = helperName;
	}

	// Entry point for the ACL message dispatching process
	public void deliverNow(ACLMessage msg, AID receiverID) throws UnreachableException, NotFoundException {
	    try {
		if(myHelper == null) {
		    myHelper = (MessagingSlice)getSlice(myHelperName);
		}

		deliverUntilOK(msg, receiverID);
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
		    ok = true;
		}
		catch(NotFoundException nfe) {
		    ok = false; // Stale proxy again, maybe the receiver is running around. Try again...
		}

	    } while(!ok);
	}

	// Implementation of the Filter interface

	public void accept(VerticalCommand cmd) { // FIXME: Should set the exception somehow...

	    try {
		String name = cmd.getName();
		if(name.equals(SEND_MESSAGE)) {
		    handleSendMessage(cmd);
		}
		if(name.equals(INSTALL_MTP)) {
		    Object result = handleInstallMTP(cmd);
		    cmd.setReturnValue(result);
		}
		else if(name.equals(UNINSTALL_MTP)) {
		    handleUninstallMTP(cmd);
		}
		else if(name.equals(SET_PLATFORM_ADDRESSES)) {
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

	public void setBlocking(boolean newState) {
	    // Do nothing. Blocking and Skipping not supported
	}

    	public boolean isBlocking() {
	    return false; // Blocking and Skipping not implemented
	}

	public void setSkipping(boolean newState) {
	    // Do nothing. Blocking and Skipping not supported
	}

	public boolean isSkipping() {
	    return false; // Blocking and Skipping not implemented
	}


	// Implementation of the Service.Slice interface

	public Service getService() {
	    return LightMessagingService.this;
	}

	public Node getNode() throws ServiceException {
	    try {
		return LightMessagingService.this.getLocalNode();
	    }
	    catch(IMTPException imtpe) {
		throw new ServiceException("Problem in contacting the IMTP Manager", imtpe);
	    }
	}

	public VerticalCommand serve(HorizontalCommand cmd) {
	    try {
		String cmdName = cmd.getName();
		Object[] params = cmd.getParams();

		if(cmdName.equals(H_DISPATCHLOCALLY)) {
		    ACLMessage msg = (ACLMessage)params[0];
		    AID receiverID = (AID)params[1];

		    dispatchLocally(msg, receiverID);
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
		if(cmd instanceof VerticalCommand) {
		    return (VerticalCommand)cmd;
		}
		else {
		    return null;
		}
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
	    try {
		if(myHelper == null) {
		    myHelper = (MessagingSlice)getSlice(myHelperName);
		}

		myHelper.routeOut(msg, receiverID, address);
	    }
	    catch(ServiceException se) {
		throw new MTPException("No suitable route found for address " + address + ".");
	    }
	}

	public ContainerID getAgentLocation(AID agentID) throws IMTPException, NotFoundException {
	    throw new NotFoundException("Agent location lookup not supported by this slice");
	}

	public MTPDescriptor installMTP(String address, String className) throws IMTPException, ServiceException, MTPException {
	    throw new MTPException("Installing MTPs is not supported by this slice");
	}

	public void uninstallMTP(String address) throws IMTPException, ServiceException, NotFoundException, MTPException {
	    throw new MTPException("Uninstalling MTPs is not supported by this slice");
	}

	public void newMTP(MTPDescriptor mtp, ContainerID cid) throws IMTPException, ServiceException {
	    // Do nothing
	}

	public void deadMTP(MTPDescriptor mtp, ContainerID cid) throws IMTPException, ServiceException {
	    // Do nothing
	}

	public void addRoute(MTPDescriptor mtp, String sliceName) throws IMTPException, ServiceException {
	    // Do nothing
	}

	public void removeRoute(MTPDescriptor mtp, String sliceName) throws IMTPException, ServiceException {
	    // Do nothing
	}

	private String myHelperName;
	private MessagingSlice myHelper;



    } // End of ServiceComponent class


    /**
       Activates the ACL codecs and MTPs as specified in the given
       <code>Profile</code> instance.
       @param myProfile The <code>Profile</code> instance containing
       the list of ACL codecs and MTPs to activate on this node.
    **/
    public void boot(Profile myProfile) {
	// Do nothing
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
			    // FIXME: Having a custom code path for send failure notifications would be better...
			    GenericCommand cmd = new GenericCommand(MessagingSlice.SEND_MESSAGE, MessagingSlice.NAME, null);
			    cmd.addParam(failure);
			    cmd.addParam(theAMS);
			    handleSendMessage(cmd);
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


    // Vertical command handler methods


    private void handleSendMessage(VerticalCommand cmd) throws AuthException {
	Object[] params = cmd.getParams();
	ACLMessage msg = (ACLMessage)params[0];
	AID sender = (AID)params[1];

	// Set the sender unless already set
	try {
	    if (msg.getSender() == null) 
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
		    myMessageManager.deliver(copy, dest, this);
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
	// Do nothing...
    }


    // The concrete agent container, providing access to LADT, etc.
    private AgentContainer myContainer;

    // The local slice for this service
    private ServiceComponent localSlice;

    // The component managing asynchronous message delivery and retries
    private MessageManager myMessageManager;

}
