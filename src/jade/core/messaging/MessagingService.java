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

import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.InternalError;
import jade.domain.FIPAAgentManagement.Envelope;
import jade.domain.FIPAAgentManagement.ReceivedObject;

import jade.security.Authority;
import jade.security.AgentPrincipal;
import jade.security.PrivilegedExceptionAction;
import jade.security.AuthException;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.ACLCodec;

import jade.lang.acl.LEAPACLCodec;
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
 *
 * The JADE service to manage the message passing subsystem installed
 * on the platform.
 *
 * @author Giovanni Rimassa - FRAMeTech s.r.l.
 * @author Nicolas Lhuillier - Motorola Labs
 * @author Jerome Picault - Motorola Labs
 */
public class MessagingService extends BaseService implements MessageManager.Channel {

  // The profile passed to this object
  private Profile myProfile;

  // The concrete agent container, providing access to LADT, etc.
  private AgentContainer myContainer;

  // A handle to the persistent delivery service
  private Service persistentDeliveryService;

  // The local slice for this service
  private final ServiceComponent localSlice = new ServiceComponent();

  // The command sink, source side
  private final CommandSourceSink senderSink = new CommandSourceSink();

  // The command sink, target side
  private final CommandTargetSink receiverSink = new CommandTargetSink();

  // The filter for incoming commands related to ACL encoding
  private Filter encOutFilter;

  // The filter for outgoing commands related to ACL encoding
  private Filter encInFilter;

  // The cached AID -> MessagingSlice associations
  //#MIDP_EXCLUDE_BEGIN
  private final Map cachedSlices = new jade.util.HashCache(100); // FIXME: Cache size should be taken from the profile
  //#MIDP_EXCLUDE_END
	
  /*#MIDP_INCLUDE_BEGIN
		private final Map cachedSlices = new HashMap();
		#MIDP_INCLUDE_END*/

  // The routing table mapping MTP addresses to their hosting slice
  private RoutingTable routes = new RoutingTable();

  private final static int EXPECTED_ACLENCODINGS_SIZE = 3;
  // The table of the locally installed ACL message encodings
  private final Map messageEncodings = new HashMap(EXPECTED_ACLENCODINGS_SIZE);

  // The platform ID, to be used in inter-platform dispatching
  private String accID;

  // The component managing asynchronous message delivery and retries
  private MessageManager myMessageManager;


  public static class UnknownACLEncodingException extends NotFoundException {
    UnknownACLEncodingException(String msg) {
      super(msg);
    }
  } // End of UnknownACLEncodingException class


  private static final String[] OWNED_COMMANDS = new String[] {
    MessagingSlice.SEND_MESSAGE,
    MessagingSlice.NOTIFY_FAILURE,
    MessagingSlice.INSTALL_MTP,
    MessagingSlice.UNINSTALL_MTP,
    MessagingSlice.NEW_MTP,
    MessagingSlice.DEAD_MTP,
    MessagingSlice.SET_PLATFORM_ADDRESSES
  };

  public MessagingService() {
  }


  /**
   * Performs the passive initialization step of the service. This
   * method is called <b>before</b> activating the service. Its role
   * should be simply the one of a constructor, setting up the
   * internal data as needed.
   * Service implementations should not use the Service Manager and
   * Service Finder facilities from within this method. A
   * distributed initialization protocol, if needed, should be
   * exectuted within the <code>boot()</code> method.
   * @param ac The agent container this service is activated on.
   * @param p The configuration profile for this service.
   * @throws ProfileException If the given profile is not valid.
   */
  public void init(AgentContainer ac, Profile p) throws ProfileException {
    super.init(ac, p);
    this.myProfile = p;
    myContainer = ac;

    // Initialize its own ID
    String platformID = myContainer.getPlatformID();
    accID = "fipa-mts://" + platformID + "/acc";

    // Activate the default ACL String codec anyway
    ACLCodec stringCodec = new StringACLCodec();
    messageEncodings.put(stringCodec.getName().toLowerCase(), stringCodec);
    
    // Activate the efficient encoding for intra-platform encoding
    ACLCodec efficientCodec = new LEAPACLCodec();
    messageEncodings.put(efficientCodec.getName().toLowerCase(), efficientCodec);
    
    // create the command filters related to the encoding of ACL messages
    encOutFilter = new OutgoingEncodingFilter(messageEncodings, myContainer);
    encInFilter = new IncomingEncodingFilter(messageEncodings);

    myMessageManager = MessageManager.instance(p);
  }

  /**
   * Retrieve the name of this service, that can be used to look up
   * its slices in the Service Finder.
   * @return The name of this service.
   * @see jade.core.ServiceFinder
   */
  public String getName() {
    return MessagingSlice.NAME;
  }

  /**
   * Retrieve the interface through which the different service
   * slices will communicate, that is, the service <i>Horizontal
   * Interface</i>.
   * @return A <code>Class</code> object, representing the interface
   * that is implemented by the slices of this service.
   */
  public Class getHorizontalInterface() {
    try {
	    return Class.forName(MessagingSlice.NAME + "Slice");
    }
    catch(ClassNotFoundException cnfe) {
	    return null;
    }
  }

  /**
   * Retrieve the locally installed slice of this service. 
   */
  public Service.Slice getLocalSlice() {
    return localSlice;
  }


  /**
   * Access the command filter this service needs to perform its
   * tasks. This filter will be installed within the local command
   * processing engine.
   * @param direction One of the two constants
   * <code>Filter.INCOMING</code> and <code>Filter.OUTGOING</code>,
   * distinguishing between the two filter chains managed by the
   * command processor.
   * @return A <code>Filter</code> object, used by this service to
   * intercept and process kernel-level commands.
   * @see jade.core.CommandProcessor
   */
  public Filter getCommandFilter(boolean direction){
    if (direction == Filter.OUTGOING){
      return encOutFilter;
    } else {
      return encInFilter;
    }
  }

  /**
   * Access the command sink this service uses to handle its own
   * vertical commands.
   */
  public Sink getCommandSink(boolean side) {
    if(side == Sink.COMMAND_SOURCE) {
	    return senderSink;
    }
    else {
	    return receiverSink;
    }
  }

  /**
   * Access the names of the vertical commands this service wants to
   * handle as their final destination. This set must not overlap
   * with the owned commands set of any previously installed
   * service, or an exception will be raised and service
   * activation will fail.
   *
   * @see jade.core.Service#getCommandSink()
   */
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
        else if(name.equals(MessagingSlice.NOTIFY_FAILURE)) {
          handleNotifyFailure(cmd);
        }
        else if(name.equals(MessagingSlice.INSTALL_MTP)) {
          Object result = handleInstallMTP(cmd);
          cmd.setReturnValue(result);
        }
        else if(name.equals(MessagingSlice.UNINSTALL_MTP)) {
          handleUninstallMTP(cmd);
        }
        else if(name.equals(MessagingSlice.NEW_MTP)) {
          handleNewMTP(cmd);
        }
        else if(name.equals(MessagingSlice.DEAD_MTP)) {
          handleDeadMTP(cmd);
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

    private void handleSendMessage(VerticalCommand cmd) throws AuthException {
	    Object[] params = cmd.getParams();
	    AID sender = (AID)params[0];
	    GenericMessage msg = (GenericMessage)params[1];
      AID dest = (AID)params[2];

	    // --- This code could go into a Security Service, intercepting the message sending...

	    AgentPrincipal target1 = myContainer.getAgentPrincipal(sender);

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
      
      /*
      // FIXME: currently there is one receiver per command
      Iterator it = msg.getAllIntendedReceiver();

      while (it.hasNext()) {
      AID dest = (AID)it.next();
      try {
      AgentPrincipal target2 = myContainer.getAgentPrincipal(dest);
      authority.checkAction(Authority.AGENT_SEND_TO, target2, null);
      GenericMessage copy = (GenericMessage)msg.clone();
        
      myMessageManager.deliver(copy, dest, MessagingService.this);
      }
      catch (AuthException ae) {
      lastException = ae;
      notifyFailureToSender(msg, dest, new InternalError(ae.getMessage()), false);
      }
      }    
      */
      
      try {
        AgentPrincipal target2 = myContainer.getAgentPrincipal(dest);
        authority.checkAction(Authority.AGENT_SEND_TO, target2, null);
        myMessageManager.deliver(msg, dest, MessagingService.this);  
      }
      catch (AuthException ae) {
        lastException = ae;
        notifyFailureToSender(msg, dest, new InternalError(ae.getMessage()), false);
      }
      
	    if(lastException != null)
        throw lastException;
    }
    
    private void handleNotifyFailure(VerticalCommand cmd) throws AuthException {
      // FIXME: the message inside the command is a generic message, i.e.
      // can either be an ACLMessage or payload+envelope
      // open question: adjust it to provide interesting information to the
      // user

	    Object[] params = cmd.getParams();
	    GenericMessage msg = (GenericMessage)params[0];
	    AID receiver = (AID)params[1];
	    InternalError ie = (InternalError)params[2];	    

	    // If (the sender is not the AMS and the performative is not FAILURE)
      // The acl message contained inside the GenericMessage should never
      // be null (it is used to generate the failure message)
      ACLMessage aclmsg = msg.getACLMessage();
      if((aclmsg.getSender()==null) || ((aclmsg.getSender().equals(myContainer.getAMS())) && (aclmsg.getPerformative()==ACLMessage.FAILURE))) // sanity check to avoid infinite loops
        return;

	    // Send back a failure message
	    final ACLMessage failure = aclmsg.createReply();
	    failure.setPerformative(ACLMessage.FAILURE);
	    final AID theAMS = myContainer.getAMS();
	    failure.setSender(theAMS);
	    failure.setLanguage(FIPANames.ContentLanguage.FIPA_SL);

	    // FIXME: the content is not completely correct, but that should
	    // also avoid creating wrong content
	    String content = "( (action " + msg.getSender().toString();
	    content = content + " (ACLMessage) ) (MTS-error "+receiver+" \""+ie.getMessage() + "\") )";
	    failure.setContent(content);

	    try {
        Authority authority = myContainer.getAuthority();
        authority.doPrivileged(new PrivilegedExceptionAction() {
            public Object run() {
              try {
                GenericCommand command = new GenericCommand(MessagingSlice.SEND_MESSAGE, MessagingSlice.NAME, null);
                command.addParam(theAMS);
                command.addParam(new GenericMessage(failure));
                command.addParam((AID)(failure.getAllReceiver().next()));
                submit(command);
              }
              catch(ServiceException se) {
                // It should never happen
                se.printStackTrace();
              }
              return null; // nothing to return
            }
          });
	    } catch(Exception e) {
        // should be never thrown
        e.printStackTrace();
	    }
    }

    private MTPDescriptor handleInstallMTP(VerticalCommand cmd) throws IMTPException, ServiceException, NotFoundException, MTPException {
	    Object[] params = cmd.getParams();
	    String address = (String)params[0];
	    ContainerID cid = (ContainerID)params[1];
	    String className = (String)params[2];

	    MessagingSlice targetSlice = (MessagingSlice)getSlice(cid.getName());
	    try {
        return targetSlice.installMTP(address, className);
	    }
	    catch(IMTPException imtpe) {
        targetSlice = (MessagingSlice)getFreshSlice(cid.getName());
        return targetSlice.installMTP(address, className);
	    }
    }

    private void handleUninstallMTP(VerticalCommand cmd) throws IMTPException, ServiceException, NotFoundException, MTPException {
	    Object[] params = cmd.getParams();
	    String address = (String)params[0];
	    ContainerID cid = (ContainerID)params[1];

	    MessagingSlice targetSlice = (MessagingSlice)getSlice(cid.getName());
	    try {
        targetSlice.uninstallMTP(address);
	    }
	    catch(IMTPException imtpe) {
        targetSlice = (MessagingSlice)getFreshSlice(cid.getName());
        targetSlice.uninstallMTP(address);
	    }
    }

    private void handleNewMTP(VerticalCommand cmd) throws IMTPException, ServiceException {
	    Object[] params = cmd.getParams();
	    MTPDescriptor mtp = (MTPDescriptor)params[0];
	    ContainerID cid = (ContainerID)params[1];

	    MessagingSlice mainSlice = (MessagingSlice)getSlice(MAIN_SLICE);
	    try {
        mainSlice.newMTP(mtp, cid);
	    }
	    catch(IMTPException imtpe) {
        mainSlice = (MessagingSlice)getFreshSlice(MAIN_SLICE);
        mainSlice.newMTP(mtp, cid);
	    }
    }

    private void handleDeadMTP(VerticalCommand cmd) throws IMTPException, ServiceException {
	    Object[] params = cmd.getParams();
	    MTPDescriptor mtp = (MTPDescriptor)params[0];
	    ContainerID cid = (ContainerID)params[1];

	    MessagingSlice mainSlice = (MessagingSlice)getSlice(MAIN_SLICE);
	    try {
        mainSlice.deadMTP(mtp, cid);
	    }
	    catch(IMTPException imtpe) {
        mainSlice = (MessagingSlice)getFreshSlice(MAIN_SLICE);
        mainSlice.deadMTP(mtp, cid);
	    }

    }

    private void handleSetPlatformAddresses(VerticalCommand cmd) {
	    Object[] params = cmd.getParams();
	    AID id = (AID)params[0];
	    id.clearAllAddresses();
	    addPlatformAddresses(id);
    }

  } // End of CommandSourceSink class


  private class CommandTargetSink implements Sink {

    public void consume(VerticalCommand cmd) {
		
	    try {
        String name = cmd.getName();
        if(name.equals(MessagingSlice.SEND_MESSAGE)) {
          handleSendMessage(cmd);
        }
        else if(name.equals(MessagingSlice.INSTALL_MTP)) {
          Object result = handleInstallMTP(cmd);
          cmd.setReturnValue(result);
        }
        else if(name.equals(MessagingSlice.UNINSTALL_MTP)) {
          handleUninstallMTP(cmd);
        }
        else if(name.equals(MessagingSlice.NEW_MTP)) {
          handleNewMTP(cmd);
        }
        else if(name.equals(MessagingSlice.DEAD_MTP)) {
          handleDeadMTP(cmd);
        }
        else if(name.equals(MessagingSlice.SET_PLATFORM_ADDRESSES)) {
          handleSetPlatformAddresses(cmd);
        }
        else if(name.equals(Service.NEW_SLICE)) {
          handleNewSlice(cmd);
        }
	    }
	    catch(IMTPException imtpe) {
        cmd.setReturnValue(imtpe);
	    }
	    catch(NotFoundException nfe) {
        cmd.setReturnValue(nfe);
	    }
	    catch(ServiceException se) {
        cmd.setReturnValue(se);
	    }
	    catch(MTPException mtpe) {
        cmd.setReturnValue(mtpe);
	    }
    }

    private void handleSendMessage(VerticalCommand cmd) throws NotFoundException {
	    Object[] params = cmd.getParams();
	    AID receiverID = (AID)params[0];
	    GenericMessage msg = (GenericMessage)params[1];
      AID senderID = (AID)params[2];
	    dispatchLocally(msg.getACLMessage(), senderID);
    }

    private MTPDescriptor handleInstallMTP(VerticalCommand cmd) throws IMTPException, ServiceException, MTPException {
	    Object[] params = cmd.getParams();
	    String address = (String)params[0];
	    String className = (String)params[1];

	    return installMTP(address, className);
    }

    private void handleUninstallMTP(VerticalCommand cmd) throws IMTPException, ServiceException, NotFoundException, MTPException {
	    Object[] params = cmd.getParams();
	    String address = (String)params[0];

	    uninstallMTP(address);
    }

    private void handleNewMTP(VerticalCommand cmd) throws IMTPException, ServiceException {
	    Object[] params = cmd.getParams();
	    MTPDescriptor mtp = (MTPDescriptor)params[0];
	    ContainerID cid = (ContainerID)params[1];

	    newMTP(mtp, cid);
    }

    private void handleDeadMTP(VerticalCommand cmd) throws IMTPException, ServiceException {
	    Object[] params = cmd.getParams();
	    MTPDescriptor mtp = (MTPDescriptor)params[0];
	    ContainerID cid = (ContainerID)params[1];

	    deadMTP(mtp, cid);
    }

    private void handleSetPlatformAddresses(VerticalCommand cmd) {

    }

    private void handleNewSlice(VerticalCommand cmd) {
      MainContainer impl = myContainer.getMain();
      if(impl != null) {		
        Object[] params = cmd.getParams();
        String newSliceName = (String) params[0];
        try {
        	// Be sure to get the new (fresh) slice --> Bypass the service cache  
          MessagingSlice newSlice = (MessagingSlice) getFreshSlice(newSliceName);
	    	
          // Send all possible routes to the new slice 
          ContainerID[] cids = impl.containerIDs();
          for(int i = 0; i < cids.length; i++) {
            ContainerID cid = cids[i];
					
            try {
              List mtps = impl.containerMTPs(cid);
              Iterator it = mtps.iterator();
              while(it.hasNext()) {
                MTPDescriptor mtp = (MTPDescriptor)it.next();
                newSlice.addRoute(mtp, cid.getName());
              }
            }
            catch(NotFoundException nfe) {
              // Should never happen
              nfe.printStackTrace();
            }
          }
        }
        catch (ServiceException se) {
          // Should never happen since getSlice() should always work on the Main container
          se.printStackTrace();
        }
        catch (IMTPException imtpe) {
          // Should never happen since the new slice should be always valid at this time
          imtpe.printStackTrace();
        }
      }
    }

    private void dispatchLocally(ACLMessage msg, AID receiverID) throws NotFoundException {
	    boolean found = myContainer.postMessageToLocalAgent(msg, receiverID);
	    if(!found) {
        throw new NotFoundException("Messaging service slice failed to find " + receiverID);
	    }
    }

    private MTPDescriptor installMTP(String address, String className) throws IMTPException, ServiceException, MTPException {

	    try {
        // Create the MTP
        Class c = Class.forName(className);
        MTP proto = (MTP)c.newInstance();

        InChannel.Dispatcher dispatcher = new InChannel.Dispatcher() {
            public void dispatchMessage(Envelope env, byte[] payload) {
              log("Message from remote platform received", 2);

              // To avoid message loops, make sure that the ID of this ACC does
              // not appear in a previous 'received' stamp

              ReceivedObject[] stamps = env.getStamps();
              for(int i = 0; i < stamps.length; i++) {
                String id = stamps[i].getBy();
                if(CaseInsensitiveString.equalsIgnoreCase(id, accID)) {
                  System.err.println("ERROR: Message loop detected !!!");
                  System.err.println("Route is: ");
                  for(int j = 0; j < stamps.length; j++)
                    System.err.println("[" + j + "]" + stamps[j].getBy());
                  System.err.println("Message dispatch aborted.");
                  return;
                }
              }

              // Put a 'received-object' stamp in the envelope
              ReceivedObject ro = new ReceivedObject();
              ro.setBy(accID);
              ro.setDate(new Date());
              env.setReceived(ro);

              Iterator it = env.getAllIntendedReceiver();
              // FIXME: There is a problem if no 'intended-receiver' is present,
              // but this should not happen
              while (it.hasNext()) {
                AID rcv = (AID)it.next();
                GenericMessage msg = new GenericMessage(env,payload);
                myMessageManager.deliver(msg, rcv, MessagingService.this);
              }
            }
          };  
        
        if(address == null) { 
          // Let the MTP choose the address
          TransportAddress ta = proto.activate(dispatcher, myProfile);
          address = proto.addrToStr(ta);
        }
        else { 
          // Convert the given string into a TransportAddress object and use it
          TransportAddress ta = proto.strToAddr(address);
          proto.activate(dispatcher, ta, myProfile);
        }
        routes.addLocalMTP(address, proto);
        MTPDescriptor result = new MTPDescriptor(proto.getName(), className, new String[] {address}, proto.getSupportedProtocols());

        String[] pp = result.getSupportedProtocols();
        for (int i = 0; i < pp.length; ++i) {
          log("Added Route-Via-MTP for protocol "+pp[i], 1);
        }

        GenericCommand gCmd = new GenericCommand(MessagingSlice.NEW_MTP, MessagingSlice.NAME, null);
        gCmd.addParam(result);
        gCmd.addParam(myContainer.getID());
        submit(gCmd);
        
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

    private void uninstallMTP(String address) throws IMTPException, ServiceException, NotFoundException, MTPException {

      MTP proto = routes.removeLocalMTP(address);
      if(proto != null) {
        TransportAddress ta = proto.strToAddr(address);
        proto.deactivate(ta);
        MTPDescriptor desc = new MTPDescriptor(proto.getName(), proto.getClass().getName(), new String[] {address}, proto.getSupportedProtocols());


        GenericCommand gCmd = new GenericCommand(MessagingSlice.DEAD_MTP, MessagingSlice.NAME, null);
        gCmd.addParam(desc);
        gCmd.addParam(myContainer.getID());
        submit(gCmd);

 	    }
      else {
        throw new MTPException("No such address was found on this container: " + address);
      }
    }

    private  void newMTP(MTPDescriptor mtp, ContainerID cid) throws IMTPException, ServiceException {
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
            System.err.println("### addRoute() threw " + t.getClass().getName() + " ###");
          }
        }
        impl.newMTP(mtp, cid);
      }
      else {
        // Do nothing for now, but could also route the command to the main slice, thus enabling e.g. AMS replication
      }
    }

    private void deadMTP(MTPDescriptor mtp, ContainerID cid) throws IMTPException, ServiceException {
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
            System.err.println("### removeRoute() threw " + t.getClass().getName() + " ###");
            // Re-throw allowed exceptions
            if(t instanceof IMTPException) {
              throw (IMTPException)t;
            }
            if(t instanceof ServiceException) {
              throw (ServiceException)t;
            }
          }
        }
        impl.deadMTP(mtp, cid);
      }
      else {
        // Do nothing for now, but could also route the command to the main slice, thus enabling e.g. AMS replication
      }
    }


  } // End of CommandTargetSink class


  /**
     Inner class for this service: this class receives commands from
     service <code>Sink</code> and serves them, coordinating with
     remote parts of this service through the <code>Slice</code>
     interface (that extends the <code>Service.Slice</code>
     interface).
  */
  private class ServiceComponent implements Service.Slice {

    public Iterator getAddresses() {
	    return routes.getAddresses();
    }

    // Entry point for the ACL message dispatching process
    public void deliverNow(GenericMessage msg, AID receiverID) throws UnreachableException, NotFoundException {
	    try {
        MainContainer impl = myContainer.getMain();
        if(impl != null) {
          while(true) {
            // Directly use the GADT on the main container
            MessagingSlice targetSlice = null;
            ContainerID cid = impl.getContainerID(receiverID);
            targetSlice = (MessagingSlice)getSlice(cid.getName());
            try {
              try {
                targetSlice.dispatchLocally(msg.getSender(), msg, receiverID);
              }
              catch(IMTPException imtpe) {
                targetSlice = (MessagingSlice)getFreshSlice(cid.getName());
                targetSlice.dispatchLocally(msg.getSender(), msg, receiverID);
              }
              return; // Message dispatched
            }
            catch(NotFoundException nfe) {
              // The agent was found in the GADT, but not in the target LADT.
              // The agent has moved in the meanwhile or the slice may be obsolete 
              // => try again
            }
          }
        }
        else {
          // Try first with the cached <AgentID;MessagingSlice> pairs
          MessagingSlice cachedSlice = (MessagingSlice)cachedSlices.get(receiverID);
          if(cachedSlice != null) { // Cache hit :-)
            try {
              //System.out.println("--- Cache Hit for AID [" + receiverID.getLocalName() + "] ---");
              cachedSlice.dispatchLocally(msg.getSender(), msg, receiverID);
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

    private void deliverUntilOK(GenericMessage msg, AID receiverID) throws IMTPException, NotFoundException, ServiceException {
	    boolean ok = false;
	    do {
        MessagingSlice mainSlice = (MessagingSlice)getSlice(MAIN_SLICE);
        ContainerID cid;
        try {
          cid = mainSlice.getAgentLocation(receiverID);
        }
        catch(IMTPException imtpe) {
          // Try to get a newer slice and repeat...
          mainSlice = (MessagingSlice)getFreshSlice(MAIN_SLICE);
          cid = mainSlice.getAgentLocation(receiverID);
        }

        MessagingSlice targetSlice = (MessagingSlice)getSlice(cid.getName());
        try {
          try {
            targetSlice.dispatchLocally(msg.getSender(), msg, receiverID);
          }
          catch (IMTPException imtpe) {
            // Try to get a newer slice and repeat...
            targetSlice = (MessagingSlice) getFreshSlice(cid.getName());
            targetSlice.dispatchLocally(msg.getSender(), msg, receiverID);
          }
          // System.out.println("--- New Container for AID " + receiverID.getLocalName() + " is " + cid.getName() + " ---");
          // On successful message dispatch, put the slice into the slice cache
          cachedSlices.put(receiverID, targetSlice);
          ok = true;
        }
        catch(NotFoundException nfe) {
          // The agent was found in the GADT, but his container has probably 
          // disappeared in the meanwhile ==> Try again.
          ok = false;
        }
        catch(NullPointerException npe) {
          // The agent was found in the GADT, but his container has probably 
          // disappeared in the meanwhile ==> Try again.
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

        if(cmdName.equals(MessagingSlice.H_DISPATCHLOCALLY)) {
          GenericCommand gCmd = new GenericCommand(MessagingSlice.SEND_MESSAGE, MessagingSlice.NAME, null);
          AID senderAID = (AID)params[0];
          GenericMessage msg = (GenericMessage)params[1];
          AID receiverID = (AID)params[2];
          gCmd.addParam(senderAID);
          gCmd.addParam(msg);
          gCmd.addParam(receiverID);
          result = gCmd;
        }
        else if(cmdName.equals(MessagingSlice.H_ROUTEOUT)) {
          Envelope env = (Envelope)params[0];
          byte[] payload = (byte[])params[1];
          AID receiverID = (AID)params[2];
          String address = (String)params[3];

          routeOut(env, payload, receiverID, address);
        }
        else if(cmdName.equals(MessagingSlice.H_GETAGENTLOCATION)) {
          AID agentID = (AID)params[0];

          cmd.setReturnValue(getAgentLocation(agentID));
        }
        else if(cmdName.equals(MessagingSlice.H_INSTALLMTP)) {
          GenericCommand gCmd = new GenericCommand(MessagingSlice.INSTALL_MTP, MessagingSlice.NAME, null);
          String address = (String)params[0];
          String className = (String)params[1];
          gCmd.addParam(address);
          gCmd.addParam(className);

          result = gCmd;
        }
        else if(cmdName.equals(MessagingSlice.H_UNINSTALLMTP)) {
          GenericCommand gCmd = new GenericCommand(MessagingSlice.UNINSTALL_MTP, MessagingSlice.NAME, null);
          String address = (String)params[0];
          gCmd.addParam(address);

          result = gCmd;
        }
        else if(cmdName.equals(MessagingSlice.H_NEWMTP)) {
          MTPDescriptor mtp = (MTPDescriptor)params[0];
          ContainerID cid = (ContainerID)params[1];

          GenericCommand gCmd = new GenericCommand(MessagingSlice.NEW_MTP, MessagingSlice.NAME, null);
          gCmd.addParam(mtp);
          gCmd.addParam(cid);

          result = gCmd;
        }
        else if(cmdName.equals(MessagingSlice.H_DEADMTP)) {
          MTPDescriptor mtp = (MTPDescriptor)params[0];
          ContainerID cid = (ContainerID)params[1];

          GenericCommand gCmd = new GenericCommand(MessagingSlice.DEAD_MTP, MessagingSlice.NAME, null);
          gCmd.addParam(mtp);
          gCmd.addParam(cid);

          result = gCmd;
        }
        else if(cmdName.equals(MessagingSlice.H_ADDROUTE)) {
          MTPDescriptor mtp = (MTPDescriptor)params[0];
          String sliceName = (String)params[1];

          addRoute(mtp, sliceName);
        }
        else if(cmdName.equals(MessagingSlice.H_REMOVEROUTE)) {
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


    private void routeOut(Envelope env, byte[] payload, AID receiverID, String address) throws IMTPException, MTPException {
	    RoutingTable.OutPort out = routes.lookup(address);
      log("Routing message to "+receiverID.getName()+" towards port "+out, 2);
	    if(out != null)
        out.route(env, payload, receiverID, address);
	    else
        throw new MTPException("No suitable route found for address " + address + ".");
    }

    private ContainerID getAgentLocation(AID agentID) throws IMTPException, NotFoundException {
	    MainContainer impl = myContainer.getMain();
	    if(impl != null) {
        return impl.getContainerID(agentID);
	    }
	    else {
        // Do nothing for now, but could also have a local GADT copy, thus enabling e.g. Main Container replication
        return null;
	    }
    }

    private void addRoute(MTPDescriptor mtp, String sliceName) throws IMTPException, ServiceException {
	    // Be sure the slice is fresh --> bypass the service cache
    	MessagingSlice slice = (MessagingSlice)getFreshSlice(sliceName);
	    routes.addRemoteMTP(mtp, sliceName, slice);
	    
	    String[] pp = mtp.getSupportedProtocols();
	    for (int i = 0; i < pp.length; ++i) {
		    log("Added Route-Via-Slice("+sliceName+") for protocol "+pp[i], 1);
	    }

	    String[] addresses = mtp.getAddresses();
	    for(int i = 0; i < addresses.length; i++) {
        myContainer.addAddressToLocalAgents(addresses[i]);
	    }
    }

    private void removeRoute(MTPDescriptor mtp, String sliceName) throws IMTPException, ServiceException {
	    // Don't care about whether or not the slice is fresh. Only the name matters.
    	MessagingSlice slice = (MessagingSlice)getSlice(sliceName);
	    routes.removeRemoteMTP(mtp, sliceName, slice);

	    String[] pp = mtp.getSupportedProtocols();
	    for (int i = 0; i < pp.length; ++i) {
		    log("Removed Route-Via-Slice("+sliceName+") for protocol "+pp[i], 1);
	    }

	    String[] addresses = mtp.getAddresses();
	    for(int i = 0; i < addresses.length; i++) {
        myContainer.removeAddressFromLocalAgents(addresses[i]);
	    }
    }

  } // End of ServiceComponent class


 
  /**
   * Performs the active initialization step of a kernel-level
   * service: Activates the ACL codecs and MTPs as specified in the given
   * <code>Profile</code> instance.
   *
   * @param myProfile The <code>Profile</code> instance containing
   * the list of ACL codecs and MTPs to activate on this node.
   * @throws ServiceException If a problem occurs during service
   * initialization.
   */
  public void boot(Profile myProfile) throws ServiceException {
    this.myProfile = myProfile;

    try {
	    // Activate the default ACL String codec anyway
	    ACLCodec stringCodec = new StringACLCodec();
	    messageEncodings.put(stringCodec.getName().toLowerCase(), stringCodec);

      // Activate the efficient encoding for intra-platform encoding
      ACLCodec efficientCodec = new LEAPACLCodec();
      messageEncodings.put(efficientCodec.getName().toLowerCase(), efficientCodec);

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

        MessagingSlice s = (MessagingSlice)getSlice(getLocalNode().getName());
        MTPDescriptor mtp = s.installMTP(addressURL, className);
        String[] mtpAddrs = mtp.getAddresses();
        f.println(mtpAddrs[0]);
        System.out.println(mtpAddrs[0]);
	    }

	    f.close();      
    }
    catch (ProfileException pe1) {
	    System.err.println("Error reading MTPs/Codecs");
	    pe1.printStackTrace();
    }
    catch(ServiceException se) {
	    System.err.println("Error installing local MTPs");
	    se.printStackTrace();
    }
    catch(jade.lang.acl.ACLCodec.CodecException ce) {
	    System.err.println("Error installing ACL Codec");
	    ce.printStackTrace();
    }
    catch(MTPException me) {
	    System.err.println("Error installing MTP");
	    me.printStackTrace();
    }    	
    catch(IOException ioe) {
	    System.err.println("Error writing platform address");
	    ioe.printStackTrace();
    } 	
    catch(IMTPException imtpe) {
	    // Should never happen as this is a local call
	    imtpe.printStackTrace();
    }
  }

  public void deliverNow(GenericMessage msg, AID receiverID) throws UnreachableException {
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
            System.err.println("Cannot deliver message to address: "+address+" ["+mtpe.toString()+"]. Trying the next one...");
          }
        }
        notifyFailureToSender(msg, receiverID, new InternalError("No valid address contained within the AID " + receiverID.getName()), false);
	    }
    }
    catch(NotFoundException nfe) {
	    // The receiver does not exist --> Send a FAILURE message
	    notifyFailureToSender(msg, receiverID, new InternalError("Agent not found: " + nfe.getMessage()), false);
    }
    catch(UnreachableException ue) {
	    // Can't reach the destination container --> Send a FAILURE message
	    notifyFailureToSender(msg, receiverID, new InternalError("Agent unreachable: " + ue.getMessage()), false);
    }
  }


  private void forwardMessage(GenericMessage msg, AID receiver, String address) throws MTPException {
    // FIXME what if there is no envelope?
    AID aid = msg.getEnvelope().getFrom();
    
    if (aid == null) {
	    System.err.println("ERROR: null message sender. Aborting message dispatch...");
	    return;
    }

    // FIXME The message can no longer be updated
    // if has no address set, then adds the addresses of this platform
    if(!aid.getAllAddresses().hasNext())
	    addPlatformAddresses(aid);

    // FIXME (NL) Why was this done ????
    /*
      Iterator it1 = msg.getACLMessage().getAllReceiver();
      while(it1.hasNext()) {
	    AID id = (AID)it1.next();
	    if(!id.getAllAddresses().hasNext())
      addPlatformAddresses(id);
      }
    */

    // FIXME Cannot do any more (shall the encoding service do?)
    /*
      Iterator it2 = msg.getAllReplyTo();
      while(it2.hasNext()) {
	    AID id = (AID)it2.next();
	    if(!id.getAllAddresses().hasNext())
      addPlatformAddresses(id);
      }
    */
    
    try {
	    localSlice.routeOut(msg.getEnvelope(),msg.getPayload(), receiver, address);
    }
    catch(IMTPException imtpe) {
	    throw new MTPException("Error during message routing", imtpe);
    }

  }


  /**
   * This method is used internally by the platform in order
   * to notify the sender of a message that a failure was reported by
   * the Message Transport Service.
   */
  public void notifyFailureToSender(GenericMessage msg, AID receiver, InternalError ie, boolean force) {
    GenericCommand cmd = new GenericCommand(MessagingSlice.NOTIFY_FAILURE, MessagingSlice.NAME, null);
    cmd.addParam(msg);
    cmd.addParam(receiver);
    cmd.addParam(ie);

    try {
	    submit(cmd);
    }
    catch(ServiceException se) {
	    // It should never happen
	    se.printStackTrace();
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


  // Work-around for PJAVA compilation
  protected Service.Slice getFreshSlice(String name) throws ServiceException {
    return super.getFreshSlice(name);
  }
}
