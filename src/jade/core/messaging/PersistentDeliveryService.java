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

//#J2ME_EXCLUDE_FILE

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
import jade.core.AID;
import jade.core.Profile;
import jade.core.ProfileException;
import jade.core.IMTPException;
import jade.core.NotFoundException;
import jade.core.NameClashException;
import jade.core.UnreachableException;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import jade.util.leap.Iterator;
import jade.util.leap.Map;
import jade.util.leap.HashMap;
import jade.util.leap.List;
import jade.util.leap.LinkedList;



/**

   The JADE service to manage the persistent storage of undelivered
   ACL messages installed on the platform.

   @author Giovanni Rimassa - FRAMeTech s.r.l.

*/
public class PersistentDeliveryService extends BaseService {
    
	static final String ACL_USERDEF_DUE_DATE = "JADE-persistentdelivery-duedate";

    private static final String[] OWNED_COMMANDS = new String[] {
	/*PersistentDeliverySlice.ACTIVATE_MESSAGE_STORE,
	PersistentDeliverySlice.DEACTIVATE_MESSAGE_STORE,
	PersistentDeliverySlice.REGISTER_MESSAGE_TEMPLATE,
	PersistentDeliverySlice.DEREGISTER_MESSAGE_TEMPLATE*/
    };



    public void init(AgentContainer ac, Profile p) throws ProfileException {
	super.init(ac, p);
	myContainer = ac;
	myServiceFinder = myContainer.getServiceFinder();

	try {
	    MessageManager.Channel ch = (MessageManager.Channel)myServiceFinder.findService(MessagingSlice.NAME);
	    myManager = PersistentDeliveryManager.instance(p, ch);
	    myManager.start();
	}
	catch(IMTPException imtpe) {
	    imtpe.printStackTrace();
	}
	catch(ServiceException se) {
	    se.printStackTrace();
	}
    }

    public String getName() {
	return PersistentDeliverySlice.NAME;
    }

    public Class getHorizontalInterface() {
	try {
	    return Class.forName(PersistentDeliverySlice.NAME + "Slice");
	}
	catch(ClassNotFoundException cnfe) {
	    return null;
	}
    }

    public Service.Slice getLocalSlice() {
	return localSlice;
    }

    public Filter getCommandFilter(boolean direction) {
	if(direction == Filter.INCOMING) {
	    return inFilter;
	}
	else {
	    return outFilter;
	}
    }

    public Sink getCommandSink(boolean side) {
	/*if(side == Sink.COMMAND_SOURCE) {
	    return senderSink;
	}
	else {
	    return receiverSink;
	}*/
	return null;
    }

    public String[] getOwnedCommands() {
	return OWNED_COMMANDS;
    }

/*
    // This inner class handles the messaging commands on the command
    // issuer side, turning them into horizontal commands and
    // forwarding them to remote slices when necessary.
    private class CommandSourceSink implements Sink {

	// Implementation of the Sink interface

	public void consume(VerticalCommand cmd) {
		
	    try {
		String name = cmd.getName();
		if(name.equals(PersistentDeliverySlice.ACTIVATE_MESSAGE_STORE)) {
		    handleActivateMessageStore(cmd);
		}
		if(name.equals(PersistentDeliverySlice.DEACTIVATE_MESSAGE_STORE)) {
		    handleDeactivateMessageStore(cmd);
		}
		else if(name.equals(PersistentDeliverySlice.REGISTER_MESSAGE_TEMPLATE)) {
		    handleRegisterMessageTemplate(cmd);
		}
		else if(name.equals(PersistentDeliverySlice.DEREGISTER_MESSAGE_TEMPLATE)) {
		    handleDeregisterMessageTemplate(cmd);
		}
	    }
	    catch(IMTPException imtpe) {
		cmd.setReturnValue(imtpe);
	    }
	    catch(NotFoundException nfe) {
		cmd.setReturnValue(nfe);
	    }
	    catch(NameClashException nce) {
		cmd.setReturnValue(nce);
	    }
	    catch(ServiceException se) {
		cmd.setReturnValue(new IMTPException("A Service Exception occurred", se));		
	    }
	}


	// Vertical command handler methods

	private void handleActivateMessageStore(VerticalCommand cmd) throws IMTPException, ServiceException, NameClashException {
	    Object[] params = cmd.getParams();
	    String sliceName = (String)params[0];
	    String storeName = (String)params[1];

	    PersistentDeliverySlice targetSlice = (PersistentDeliverySlice)getSlice(sliceName);
	    try {
		targetSlice.activateMsgStore(storeName);
	    }
	    catch(IMTPException imtpe) {
		targetSlice = (PersistentDeliverySlice)getFreshSlice(sliceName);
		targetSlice.activateMsgStore(storeName);
	    }

	}

	private void handleDeactivateMessageStore(VerticalCommand cmd) throws IMTPException, ServiceException, NotFoundException {
	    Object[] params = cmd.getParams();
	    String sliceName = (String)params[0];
	    String storeName = (String)params[1];

	    PersistentDeliverySlice targetSlice = (PersistentDeliverySlice)getSlice(sliceName);
	    try {
		targetSlice.deactivateMsgStore(storeName);
	    }
	    catch(IMTPException imtpe) {
		targetSlice = (PersistentDeliverySlice)getFreshSlice(sliceName);
		targetSlice.deactivateMsgStore(storeName);
	    }

	}

	private void handleRegisterMessageTemplate(VerticalCommand cmd) throws IMTPException, ServiceException, NotFoundException, NameClashException {
	    Object[] params = cmd.getParams();
	    String sliceName = (String)params[0];
	    String storeName = (String)params[1];
	    MessageTemplate mt = (MessageTemplate)params[2];

	    PersistentDeliverySlice targetSlice = (PersistentDeliverySlice)getSlice(sliceName);
	    try {
		targetSlice.registerTemplate(storeName, mt);
	    }
	    catch(IMTPException imtpe) {
		targetSlice = (PersistentDeliverySlice)getFreshSlice(sliceName);
		targetSlice.registerTemplate(storeName, mt);
	    }

	}

	private void handleDeregisterMessageTemplate(VerticalCommand cmd) throws IMTPException, ServiceException, NotFoundException {
	    Object[] params = cmd.getParams();
	    String sliceName = (String)params[0];
	    String storeName = (String)params[1];
	    MessageTemplate mt = (MessageTemplate)params[2];

	    PersistentDeliverySlice targetSlice = (PersistentDeliverySlice)getSlice(sliceName);
	    try {
		targetSlice.deregisterTemplate(storeName, mt);
	    }
	    catch(IMTPException imtpe) {
		targetSlice = (PersistentDeliverySlice)getFreshSlice(sliceName);
		targetSlice.deregisterTemplate(storeName, mt);
	    }
	}

    } // End of CommandSourceSink class


    private class CommandTargetSink implements Sink {

	// Implementation of the Sink interface

	public void consume(VerticalCommand cmd) {
		
		String name = cmd.getName();
		if(name.equals(PersistentDeliverySlice.ACTIVATE_MESSAGE_STORE)) {
		    handleActivateMessageStore(cmd);
		}
		if(name.equals(PersistentDeliverySlice.DEACTIVATE_MESSAGE_STORE)) {
		    handleDeactivateMessageStore(cmd);
		}
		else if(name.equals(PersistentDeliverySlice.REGISTER_MESSAGE_TEMPLATE)) {
		    handleRegisterMessageTemplate(cmd);
		}
		else if(name.equals(PersistentDeliverySlice.DEREGISTER_MESSAGE_TEMPLATE)) {
		    handleDeregisterMessageTemplate(cmd);
		}
	}


	// Vertical command handler methods

	private void handleActivateMessageStore(VerticalCommand cmd) {
	    Object[] params = cmd.getParams();
	    String sliceName = (String)params[0];
	    String storeName = (String)params[1];

	    System.out.println("--- ACTIVATE_MESSAGE_STORE: Not Implemented ---");

	}

	private void handleDeactivateMessageStore(VerticalCommand cmd) {
	    Object[] params = cmd.getParams();
	    String sliceName = (String)params[0];
	    String storeName = (String)params[1];

	    System.out.println("--- DEACTIVATE_MESSAGE_STORE: Not Implemented ---");

	}

	private void handleRegisterMessageTemplate(VerticalCommand cmd) {
	    Object[] params = cmd.getParams();
	    String sliceName = (String)params[0];
	    String storeName = (String)params[1];
	    MessageTemplate mt = (MessageTemplate)params[2];

	    System.out.println("--- REGISTER_MESSAGE_TEMPLATE: Not Implemented ---");

	}

	private void handleDeregisterMessageTemplate(VerticalCommand cmd) {
	    Object[] params = cmd.getParams();
	    String sliceName = (String)params[0];
	    String storeName = (String)params[1];
	    MessageTemplate mt = (MessageTemplate)params[2];

	    System.out.println("--- DEREGISTER_MESSAGE_TEMPLATE: Not Implemented ---");

	}


    } // End of CommandTargetSink class
*/

    /**
       Outgoing commanf FILTER.
       Processes the NOTIFY_FAILURE command
     */
    private class CommandOutgoingFilter implements Filter {

	public boolean accept(VerticalCommand cmd) {

	    try {
		String name = cmd.getName();

		if(name.equals(jade.core.messaging.MessagingSlice.NOTIFY_FAILURE)) {
		    return handleNotifyFailure(cmd);
		}
	    }
	    catch(IMTPException imtpe) {
		cmd.setReturnValue(imtpe);
	    }
	    catch(ServiceException se) {
		cmd.setReturnValue(se);
	    }

	    // Let the command through
	    return true;
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

	private boolean handleNotifyFailure(VerticalCommand cmd) throws IMTPException, ServiceException {
	    Object[] params = cmd.getParams();
	    GenericMessage msg = (GenericMessage)params[0];//FIXME: check object type
	    AID receiver = (AID)params[1];

	    // FIXME: We should check if the failure is due to a "not found receiver"
	    
	    // Ask all slices whether the failed message should be stored
	    Service.Slice[] slices = getAllSlices();
	    for(int i = 0; i < slices.length; i++) {
		try {
		    PersistentDeliverySlice slice = (PersistentDeliverySlice)slices[i];
		    boolean accepted = slice.storeMessage(null, msg, receiver);

		    if(accepted) {
		    	// The message was stored --> Veto the NOTIFY_FAILURE command
					return false;
		    }
		}
		catch(Exception e) {
		    // Ignore it and try other slices...
		}
	    }

	    return true;
	}

    } // End of CommandOutgoingFilter class


    /**
       Outgoing commanf FILTER.
       Processes the INFORM_CREATED command
     */
    private class CommandIncomingFilter implements Filter {

	public boolean accept(VerticalCommand cmd) {

	    try {
		String name = cmd.getName();

		if(name.equals(jade.core.management.AgentManagementSlice.INFORM_CREATED)) {
		    handleInformCreated(cmd);
		}
	    }
	    catch(IMTPException imtpe) {
		cmd.setReturnValue(imtpe);
	    }
	    catch(ServiceException se) {
		cmd.setReturnValue(se);
	    }

	    return true;
	}

	private void handleInformCreated(VerticalCommand cmd) throws IMTPException, ServiceException {
	    Object[] params = cmd.getParams();
	    final AID agentID = (AID)params[0];

	    // This happens on the main container only.
	    // Requests all slices to flush the stored messages for the newly born agent.
	    // Do it in a separated thread since the new agent has not been inserted 
	    // in the GADT yet.
	    Thread t = new Thread() {
	    	public void run() {
	    		// Wait a bit to be sure the new agent is in the GADT
	    		try {
	    			Thread.sleep(500);
	    		}
	    		catch (Exception e) {}
	    		
	    		try {
				    Service.Slice[] slices = getAllSlices();
				    for(int i = 0; i < slices.length; i++) {
							try {
							    PersistentDeliverySlice slice = (PersistentDeliverySlice)slices[i];
							    slice.flushMessages(agentID);
							}
							catch(Exception e) {
							    // Ignore it and try other slices...
							}
				    }
	    		}
	    		catch (ServiceException se) {
	    			se.printStackTrace();
	    		}
	    	}
	    };
	    t.start();
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

    } // End of CommandIncomingFilter class


    /**
       The SLICE.
     */
    private class ServiceComponent implements Service.Slice {
	
  // Implementation of the Service.Slice interface
	public Service getService() {
	    return PersistentDeliveryService.this;
	}

	public Node getNode() throws ServiceException {
	    try {
		return PersistentDeliveryService.this.getLocalNode();
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

		/*if(cmdName.equals(PersistentDeliverySlice.H_ACTIVATEMSGSTORE)) {
		    GenericCommand gCmd = new GenericCommand(PersistentDeliverySlice.ACTIVATE_MESSAGE_STORE, PersistentDeliverySlice.NAME, null);
		    String name = (String)params[0];
		    gCmd.addParam(name);

		    result = gCmd;
		}
		else if(cmdName.equals(PersistentDeliverySlice.H_DEACTIVATEMSGSTORE)) {
		    GenericCommand gCmd = new GenericCommand(PersistentDeliverySlice.DEACTIVATE_MESSAGE_STORE, PersistentDeliverySlice.NAME, null);
		    String name = (String)params[0];
		    gCmd.addParam(name);

		    result = gCmd;
		}
		else if(cmdName.equals(PersistentDeliverySlice.H_REGISTERTEMPLATE)) {
		    GenericCommand gCmd = new GenericCommand(PersistentDeliverySlice.REGISTER_MESSAGE_TEMPLATE, PersistentDeliverySlice.NAME, null);
		    String storeName = (String)params[0];
		    MessageTemplate mt = (MessageTemplate)params[1];
		    gCmd.addParam(storeName);
		    gCmd.addParam(mt);

		    result = gCmd;
		    
		}
		else if(cmdName.equals(PersistentDeliverySlice.H_DEREGISTERTEMPLATE)) {
		    GenericCommand gCmd = new GenericCommand(PersistentDeliverySlice.DEREGISTER_MESSAGE_TEMPLATE, PersistentDeliverySlice.NAME, null);
		    String storeName = (String)params[0];
		    MessageTemplate mt = (MessageTemplate)params[1];
		    gCmd.addParam(storeName);
		    gCmd.addParam(mt);

		    result = gCmd;
		}*/
		if (cmdName.equals(PersistentDeliverySlice.H_STOREMESSAGE)) {
		    String storeName = (String)params[0];
		    GenericMessage msg = (GenericMessage)params[1];//FIXME: check object type(should be GenericMessage instead of ACLMessage
		    AID receiver = (AID)params[2];

		    boolean stored = storeMessage(storeName, msg, receiver);
		    cmd.setReturnValue(new Boolean(stored));
		}
		else if(cmdName.equals(PersistentDeliverySlice.H_FLUSHMESSAGES)) {
		    AID receiver = (AID)params[0];

		    flushMessages(receiver);
		}
	    }
	    catch(Throwable t) {
		cmd.setReturnValue(t);
	    }
	    finally {
		return result;
	    }
	}

	/**
	   This is called following a message delivery failure to check 
	   whether or not the message must be stored.
	 */
	private boolean storeMessage(String storeName, GenericMessage msg, AID receiver) throws IMTPException, ServiceException {

		boolean	firstTime = false;
		long now = System.currentTimeMillis();
		long dueDate = now; 
		try {
			// If the due-date parameter is already set, this is a re-transmission 
			// attempt --> Use the due-date value
		    String dd = msg.getACLMessage().getUserDefinedParameter(ACL_USERDEF_DUE_DATE);
			dueDate = Long.parseLong(dd);
		}
		catch (Exception e) {
			// Due date not yet set (or unknown value)
		    long delay = messageFilter.delayBeforeExpiration(msg.getACLMessage());
	    if (delay != PersistentDeliveryFilter.NOW) {
	    	dueDate = (delay == PersistentDeliveryFilter.NEVER ? delay : now+delay);
        msg.getACLMessage().addUserDefinedParameter(ACL_USERDEF_DUE_DATE, String.valueOf(dueDate));
		    firstTime = true;
	    }
		}
		
		if (dueDate > now || dueDate == PersistentDeliveryFilter.NEVER) { 
			try {
			    if (firstTime) {
			    	log("Storing message\n"+msg+"\nDue date is "+dueDate, 1);
			    }
			    else {
			    	log("Re-Storing message\n"+msg+"\nDue date is "+dueDate, 2);
			    }
			    myManager.storeMessage(storeName, msg, receiver);
			    return true;
			}
			catch(IOException ioe) {
			    throw new ServiceException("I/O Error in message storage", ioe);
			}
    }
    else {
			return false;
    }
	}

	/**
	   This is called when a new agent is born to send him the stored 
	   messages (if any)
	 */
	private void flushMessages(AID receiver) {
	    int cnt = myManager.flushMessages(receiver);
	    if (cnt > 0) {
	    	log("Flushed "+cnt+" messages for agent "+receiver, 1);
	    }
	}

    } // End of ServiceComponent class


    private class DefaultMessageFilter implements PersistentDeliveryFilter {

	// Never store messages
	public long delayBeforeExpiration(ACLMessage msg) {
	    return NOW;
	}
    } // End of DefaultMessageFilter class


    /**
       Activates the ACL codecs and MTPs as specified in the given
       <code>Profile</code> instance.
       @param myProfile The <code>Profile</code> instance containing
       the list of ACL codecs and MTPs to activate on this node.
    **/
    public void boot(Profile myProfile) throws ServiceException {
	try {
	    // Load the supplied class to filter messages, or use the default
	    String className = myProfile.getParameter(Profile.PERSISTENT_DELIVERY_FILTER, null);
	    if(className != null) {
		Class c = Class.forName(className);
		messageFilter = (PersistentDeliveryFilter)c.newInstance();
	    }
	    else {
		messageFilter = new DefaultMessageFilter();
	    }
	    log("Using message filter of type "+messageFilter.getClass().getName(), 1);
	}
	catch(Exception e) {
	    throw new ServiceException("Exception in message filter initialization", e);
	}

    }


    // The concrete agent container, providing access to LADT, etc.
    private AgentContainer myContainer;

    // The service finder component
    private ServiceFinder myServiceFinder;

    // The component managing ACL message storage and delayed delivery
    private PersistentDeliveryManager myManager;

    // The local slice for this service
    private final ServiceComponent localSlice = new ServiceComponent();

    // The command sink, source side
    //private final CommandSourceSink senderSink = new CommandSourceSink();

    // The command sink, target side
    //private final CommandTargetSink receiverSink = new CommandTargetSink();

    // The command filter, outgoing direction
    private final CommandOutgoingFilter outFilter = new CommandOutgoingFilter();

    // The command filter, incoming direction
    private final CommandIncomingFilter inFilter = new CommandIncomingFilter();

    // Service-specific data

    // The filter to be matched by undelivered ACL messages
    private PersistentDeliveryFilter messageFilter;

}
