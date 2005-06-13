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

import jade.core.ServiceFinder;

import jade.core.HorizontalCommand;
import jade.core.VerticalCommand;
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

import jade.lang.acl.ACLMessage;
import jade.domain.FIPAAgentManagement.Envelope;

import jade.util.Logger;



/**

   The JADE service to manage the persistent storage of undelivered
   ACL messages installed on the platform.

   @author Giovanni Rimassa - FRAMeTech s.r.l.

*/
public class PersistentDeliveryService extends BaseService {
  /**
     This constant is the name of the property whose value contains
     the name of the application-specific class that will be used by the
     PersistentDeliveryService on the local container as a filter for 
     undelivered ACL messages
  */
  public static final String PERSISTENT_DELIVERY_FILTER = "persistent-delivery-filter";

  /**
     This constant is the name of the property whose value contains an
     integer representing how often (in milliseconds) the 
     PersistentDeliveryService will try to
     send again previously undelivered ACL messages which have been
     buffered.
  */
  public static final String PERSISTENT_DELIVERY_SENDFAILUREPERIOD = "persistent-delivery-sendfailureperiod";

  /**
     This constant is the name of the property whose value contains
     the storage method used to persist undelivered ACL messages by
     the PersistentDeliveryService on the local container.
     The supported values for this parameter are:
     <ul>
     <li><b>file</b> - A directory tree on the local filesystem is used.</li>
     </ul>
     If this property is not specified undelivered ACL messages are
     kept in memory and not persisted at all.
  */
  public static final String PERSISTENT_DELIVERY_STORAGEMETHOD = "persistent-delivery-storagemethod";

  /**
     This constant is the name of the property whose value contains
     the root of the directory tree that is used to persist
     undelivered ACL messages when the <i>file</i> storage
     method is selected.
  */
  public static final String PERSISTENT_DELIVERY_BASEDIR = "persistent-delivery-basedir";


	static final String ACL_USERDEF_DUE_DATE = "JADE-persistentdelivery-duedate";

        private Logger logger = Logger.getMyLogger(this.getClass().getName());

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
       Outgoing command FILTER.
       Processes the NOTIFY_FAILURE command
     */
    private class CommandOutgoingFilter extends Filter {

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

	private boolean handleNotifyFailure(VerticalCommand cmd) throws IMTPException, ServiceException {
	    Object[] params = cmd.getParams();
	    GenericMessage msg = (GenericMessage)params[0];//FIXME: check object type
	    AID receiver = (AID)params[1];
    	ACLMessage acl = msg.getACLMessage();

      if(logger.isLoggable(Logger.FINE))
        logger.log(Logger.FINE,"Processing failed message "+MessageManager.stringify(msg)+" for agent "+receiver.getName());


	    // FIXME: We should check if the failure is due to a "not found receiver"

      // Ask all slices whether the failed message should be stored
	    Service.Slice[] slices = getAllSlices();
	    for(int i = 0; i < slices.length; i++) {
		PersistentDeliverySlice slice = (PersistentDeliverySlice)slices[i];
		try {
			boolean firstTime = (acl.getUserDefinedParameter(ACL_USERDEF_DUE_DATE) == null);
			  boolean accepted = false;
			  try {
			    accepted = slice.storeMessage(null, msg, receiver);
			  }
				catch(IMTPException imtpe) {
				    // Try to get a fresh slice and repeat...
				    slice = (PersistentDeliverySlice)getFreshSlice(slice.getNode().getName());
				    accepted = slice.storeMessage(null, msg, receiver);
				}

		    if(accepted) {
          logger.log((firstTime ? Logger.INFO : Logger.FINE) ,"Message "+MessageManager.stringify(msg)+" for agent "+receiver.getName()+" stored on node "+slice.getNode().getName());
		    	// The message was stored --> Veto the NOTIFY_FAILURE command
					return false;
		    }
		}
		catch(Exception e) {
        logger.log(Logger.WARNING,"Error trying to store message "+MessageManager.stringify(msg)+" for agent "+receiver.getName()+" on node "+slice.getNode().getName());
		    // Ignore it and try other slices...
		}
	    }

	    return true;
	}

    } // End of CommandOutgoingFilter class


    /**
       Outgoing command FILTER.
       Processes the INFORM_CREATED command. Note that we do this 
       in the postProcess() method so that we are sure the newly
       born agent is already in the GADT.
     */
    private class CommandIncomingFilter extends Filter {

	public void postProcess(VerticalCommand cmd) {
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
	}

	private void handleInformCreated(VerticalCommand cmd) throws IMTPException, ServiceException {
	    Object[] params = cmd.getParams();
	    final AID agentID = (AID)params[0];

	    // This happens on the main container only.
	    // Requests all slices to flush the stored messages for the newly born agent.
	    // Do it in a separated thread since this may take time
	    Thread t = new Thread() {
	    	public void run() {
	    		try {
				    Service.Slice[] slices = getAllSlices();
				    for(int i = 0; i < slices.length; i++) {
					    PersistentDeliverySlice slice = (PersistentDeliverySlice)slices[i];
							try {
							    slice.flushMessages(agentID);
							}
							catch(Exception e) {
                logger.log(Logger.WARNING,"Error trying to flush messages for agent "+agentID.getName()+" on node "+slice.getNode().getName());
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
		    // NOTE that we can't send the GenericMessage directly as a parameter
		    // since we would loose the embedded ACLMessage
		    ACLMessage acl = (ACLMessage) params[1];
		    Envelope env = (Envelope) params[2];
		    byte[] payload = (byte[]) params[3];
		    GenericMessage msg = new GenericMessage();
		    msg.update(acl, env, payload);
		    AID receiver = (AID)params[4];

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

            return result;
	}

	/**
	   This is called following a message delivery failure to check
	   whether or not the message must be stored.
	 */
	private boolean storeMessage(String storeName, GenericMessage msg, AID receiver) throws IMTPException, ServiceException {
		// We store a message only if there is a message filter
		if (messageFilter != null) {
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
				      if(logger.isLoggable(Logger.INFO))
				        logger.log(Logger.INFO,"Storing message\n"+MessageManager.stringify(msg)+" for agent "+receiver.getName()+"\nDue date is "+dueDate);
				    }
				    else {
		          if(logger.isLoggable(Logger.FINE))
				        logger.log(Logger.FINE,"Re-storing message\n"+MessageManager.stringify(msg)+" for agent "+receiver.getName()+"\nDue date is "+dueDate);
				    }
				    myManager.storeMessage(storeName, msg, receiver);
				    return true;
				}
				catch(IOException ioe) {
				    throw new ServiceException("I/O Error in message storage", ioe);
				}
	    }
		}
		return false;
	}

	/**
	   This is called when a new agent is born to send him the stored
	   messages (if any)
	 */
	private void flushMessages(AID receiver) {
	    int cnt = myManager.flushMessages(receiver);
	    if (cnt > 0) {
        logger.log(Logger.INFO,"Delivered "+cnt+" messages to agent "+receiver);
	    }
	}

    } // End of ServiceComponent class



    /**
       Activate the PersistentDeliveryManager and instantiate the 
       PersistentDeliveryFilter.
       Note that getting the MessagingService (required to instantiate
       the PersistentDeliveryManager) cannot be done in the init() method
       since at that time the MessagingService may not be installed yet.
     */
    public void boot(Profile myProfile) throws ServiceException {
    	// getting the delivery channel
    	try {
    	    MessageManager.Channel ch = (MessageManager.Channel)myServiceFinder.findService(MessagingSlice.NAME);
    	    if (ch == null)
    	    	throw new ServiceException("Can't locate delivery channel");
    	    myManager = PersistentDeliveryManager.instance(myProfile, ch);
    	    myManager.start();
    	}
    	catch(IMTPException imtpe) {
    	    imtpe.printStackTrace();
    	    throw new ServiceException("Cannot retrieve the delivery channel",imtpe);
    	}

    	try {
		    // Load the supplied class to filter messages if any
		    String className = myProfile.getParameter(PERSISTENT_DELIVERY_FILTER, null);
		    if(className != null) {
				Class c = Class.forName(className);
				messageFilter = (PersistentDeliveryFilter)c.newInstance();
				logger.log(Logger.INFO,"Using message filter of type "+messageFilter.getClass().getName());
		    }
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
