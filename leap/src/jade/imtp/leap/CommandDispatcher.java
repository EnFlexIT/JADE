/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

/**
 * ***************************************************************
 * The LEAP libraries, when combined with certain JADE platform components,
 * provide a run-time environment for enabling FIPA agents to execute on
 * lightweight devices running Java. LEAP and JADE teams have jointly
 * designed the API for ease of integration and hence to take advantage
 * of these dual developments and extensions so that users only see
 * one development platform and a
 * single homogeneous set of APIs. Enabling deployment to a wide range of
 * devices whilst still having access to the full development
 * environment and functionalities that JADE provides.
 * Copyright (C) 2001 Telecom Italia LAB S.p.A.
 * Copyright (C) 2001 Siemens AG.
 * 
 * GNU Lesser General Public License
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation,
 * version 2.1 of the License.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 * **************************************************************
 */


package jade.imtp.leap;

import jade.core.AgentContainer;
import jade.core.MainContainer;
import jade.core.IMTPException;
import jade.core.Profile;
import jade.core.ProfileException;
import jade.core.Runtime;
import jade.core.UnreachableException;
import jade.mtp.TransportAddress;
import jade.util.leap.ArrayList;
import jade.util.leap.List;
import jade.util.Logger;

/**
 * This class provides a lightweight implementation of a command
 * dispatcher. The command dispatcher misses support for multiple remote
 * objects, multiple ICPs and command routing.
 * 
 * <p>The command dispatcher is based on an implementation written by
 * Michael Watzke and Giovanni Caire (TILAB), 09/11/2000.</p>
 * 
 * @see FullCommandDispatcher
 * @author Tobias Schaefer
 * @version 1.0
 */
class CommandDispatcher implements StubHelper, ICP.Listener {
	private static final String COMMAND_DISPATCHER_CLASS = "dispatcher-class";
	private static final String MAIN_PROTO_CLASS = "main-proto-class";
	
  /**
   * The default name for new instances of the class
   * <tt>CommandDispatcher</tt> that have not get an unique name by
   * their container yet.
   */
  protected static final String      DEFAULT_NAME = "Default";

  /**
   * A singleton instance of the command dispatcher.
   */
  protected static CommandDispatcher commandDispatcher;

  /**
   * The unique name of this command dispatcher used to avoid loops in
   * the forwarding mechanism.
   */
  protected String                   name;

  /**
   * The transport address of the default router. Commands that cannot
   * be dispatched directly will be sent to this address.
   */
  protected TransportAddress         routerTA = null;

  /**
   * The skeleton of the object remotized by this command dispatcher.
   * It is used when a command is received from a remote JVM.
   */
  protected Skeleton                 skeleton;

  /**
   * The objects remotized by this command dispatcher. It is used when
   * a Stub of a remotized object must be built to be sent to a remote
   * JVM.
   */
  protected Object                   remoteObject;

  /**
   * The ID of remotized object. Everytime the old remotized object is
   * replaced by a new one the field is increased.
   */
  protected int                      id;

  /**
   * The ICP object used by this command dispatcher to actually
   * send/receive data over the network.
   */
  protected ICP                      icp;

  /**
   * The TransportAddress the ICP managed by this command dispatcher is
   * listening for commands on.
   */
  protected TransportAddress         icpTA;

  /**
   * The URL corresponding to the local TransportAddress.
   */
  protected String                   url;

  /**
   * Tries to create a new command dispatcher and returns whether the
   * creation was successful. The implementation of the command
   * dispatcher is determined by the specified profile. The profile
   * must contain a parameter with the key <tt>"commandDispatcher"</tt>
   * and a value containing the fully qualified class name of the
   * desired command dispatcher.
   * 
   * <p>When the profile contains no argument with key
   * <tt>"commandDispatcher"</tt> the class
   * {@link FullCommandDispatcher jade.imtp.leap.FullCommandDispatcher}
   * will be used per default.</p>
   * 
   * @param p a profile determining the implementation of the command
   * dispatcher.
   * @return <tt>true</tt>, if a command dispatcher of the desired
   * class is created or already exists, otherwise
   * <tt>false</tt>.
   */
  public static final boolean create(Profile p) {
    
    String implementation = null;
    // Set CommandDispatcher class name
    // Default is FullCommandDispatcher in J2SE and PJAVA, CommandDispatcher in MIDP
    if (p.getParameter(Profile.JVM, Profile.J2SE).equals(Profile.MIDP)) {
        implementation = p.getParameter(COMMAND_DISPATCHER_CLASS, "jade.imtp.leap.CommandDispatcher");
    }
    else {
        implementation = p.getParameter(COMMAND_DISPATCHER_CLASS, "jade.imtp.leap.FullCommandDispatcher");
    }
    
    if (commandDispatcher == null) {
      try {
        commandDispatcher = (CommandDispatcher) Class.forName(implementation).newInstance();

        // DEBUG
        // System.out.println("Using command dispatcher '" + implementation + "'");

        return true;
      } 
      catch (Exception e) {
        Logger.println("Instantiation of class "+implementation+" failed ["+e+"].");
      } 

      return false;
    } 
    else {
      return commandDispatcher.getClass().getName().equals(implementation);
    } 
  } 

  /**
   * Returns a reference to the singleton instance of the command
   * dispatcher. When no such instance exists, <tt>null</tt> is
   * returned.
   * 
   * @return a reference to the singleton instance of the command
   * dispatcher or <tt>null</tt>, if no such instance exists.
   */
  public static final CommandDispatcher getDispatcher() {
    return commandDispatcher;
  } 

  /**
   * A sole constructor. To get a command dispatcher the constructor
   * should not be called directly but the static <tt>create</tt> and
   * <tt>getDispatcher</tt> methods should be used. Thereby the
   * existence of a singleton instance of the command dispatcher will
   * be guaranteed.
   */
  public CommandDispatcher() {

    // Set a temporary name. Will be substituted as soon as the first
    // container attached to this CommandDispatcher will receive a
    // unique name from the main.
    name = DEFAULT_NAME;
    id = 1;
  }

    public ServiceManagerStub getServiceManagerStub(Profile p) throws IMTPException {
	ServiceManagerStub stub = new ServiceManagerStub();
	TransportAddress mainTA = initMainTA(p);
	stub.bind(this);
	stub.addTA(mainTA);

	return stub;
    }

    public ServiceManagerStub getServiceManagerStub(String addr) throws IMTPException {

	// Try to translate the address into a TransportAddress
	// using a protocol supported by this CommandDispatcher
	try {
	    ServiceManagerStub stub = new ServiceManagerStub();
	    TransportAddress ta = stringToAddr(addr);
	    stub.bind(this);
	    stub.addTA(ta);
	    return stub;
	}
	catch (DispatcherException de) {
	    throw new IMTPException("Invalid address for a Service Manager", de);
	}
	
    }

    public void addAddressToStub(Stub target, String toAdd) {
	try {
	    System.out.println("--- Adding address <" + toAdd + "> ---");
	    TransportAddress ta = stringToAddr(toAdd);
	    target.addTA(ta);
	}
	catch(DispatcherException de) {
	    de.printStackTrace();
	}
    }

    public void removeAddressFromStub(Stub target, String toRemove) {
	try {
	    System.out.println("--- Removing address <" + toRemove + "> ---");
	    TransportAddress ta = stringToAddr(toRemove);
	    target.removeTA(ta);
	}
	catch(DispatcherException de) {
	    de.printStackTrace();
	}
    }

    public void clearStubAddresses(Stub target) {
	target.clearTAs();
    }

  /**
   * Sets the transport address of the default router used for the
   * forwarding mechanism.
   * 
   * @param url the URL of the default router.
   */
  void setRouterAddress(String url) {
    if (url != null) {
      // The default router must be directly reachable -->
      // Its URL can be converted into a TransportAddress by
      // the ICP registered to this CommandDispatcher
    	try {
	      TransportAddress ta = stringToAddr(url);
    		if (routerTA != null && !routerTA.equals(ta)) {
      		Logger.println("WARNING : transport address of current router has been changed");
    		} 
    		routerTA = ta;
    	}
    	catch (Exception e) {
	      // Just print a warning: default (i.e. main TA) will be used
	      Logger.println("Can't initialize router address");
    	}
    }    		
  } 

  /**
   * Adds (and activates) an ICP to this command dispatcher.
   * 
   * @param peer the ICP to add.
   * @param args the arguments required by the ICP for the activation.
   * These arguments are ICP specific.
   */
  public void addICP(ICP peer, String peerID, Profile p) {
    try {
      boolean replace = icp != null;

      // Activate the peer and replace the local address by the
      // listening address
      icpTA = (icp = peer).activate(this, peerID, p);
      url = icp.getProtocol().addrToString(icpTA);

      if (replace) {
        Logger.println("WARNING : icp has been changed");
      } 
    } 
    catch (ICPException icpe) {

      // Print a warning
      Logger.println("Error setting ICP "+peer+"["+icpe.getMessage()+"].");
    } 
  } 

  /**
   * Returns the list of local addresses.
   * 
   * @return the list of local addresses.
   */
  public List getLocalTAs() {
    ArrayList adresses = new ArrayList();
    if (icpTA != null) {
      adresses.add(icpTA);
    } 

    return adresses;
  } 

  /**
   * Returns the list of URLs corresponding to the local addresses.
   * 
   * @return the list of URLs corresponding to the local addresses.
   */
  public List getLocalURLs() {
    ArrayList urls = new ArrayList();
    if (url != null) {
      urls.add(url);
    } 

    return urls;
  } 

  /**
   * Returns the ID of the specified remotized object.
   * 
   * @param remoteObject the object whose ID should be returned.
   * @return the ID of the reomte object.
   * @throws RuntimeException if the specified object is not
   * remotized by this command dispatcher.
   */
  public int getID(Object obj) throws IMTPException {
    if (remoteObject.equals(obj)) {
      return id;
    } 

    throw new IMTPException("Specified object is not remotized by this command dispatcher.");
  } 

  /**
   * Converts an URL into a transport address using the transport
   * protocol supported by the ICPs currently installed in the command
   * dispatcher. If there is no ICP installed to the command dispatcher
   * or their transport protocols are not able to convert the specified
   * URL a <tt>DispatcherException</tt> is thrown.
   * 
   * @param url a <tt>String</tt> object specifying the URL to convert.
   * @return the converted URL.
   * @throws DispatcherException if there is no ICP installed to the
   * command dispatcher or the transport protocols of the ICPs
   * are not able to convert the specified URL.
   */
  protected TransportAddress stringToAddr(String url) throws DispatcherException {

    // Try to convert the url using the TransportProtocol supported
    // by this ICP.
    try {
      return icp.getProtocol().stringToAddr(url);
    } 
    catch (Exception e) {
      throw new DispatcherException("can't convert URL "+url+'.');
    } 
  } 

  /**
   * Registers the specified skeleton to the command dispatcher.
   * 
   * @param skeleton a skeleton to be managed by the command
   * dispatcher.
   * @param remoteObject the remote object related to the specified
   * skeleton.
   */
  public void registerSkeleton(Skeleton skeleton, Object remoteObject) {
    this.skeleton = skeleton;
    this.remoteObject = remoteObject;
    id++;

    if (id > 2) {
      Logger.println("WARNING : remotized object has been changed");
    } 
  } 

  /**
   * Deregisters the specified remote object from the command dispatcher.
   * 
   * @param remoteObject the remote object related to the specified
   * skeleton.
   */
  public void deregisterSkeleton(Object remoteObject) {
    if (this.remoteObject == remoteObject) {
      skeleton = null;
      this.remoteObject = null;
      id = 1;
    } 

    shutDown();
  } 



  public Stub buildLocalStub(Object remoteObject) throws IMTPException {

    if(remoteObject instanceof NodeAdapter) {

	NodeAdapter na = (NodeAdapter)remoteObject;
	NodeLEAP nl = na.getAdaptee();
	NodeStub stub;
	if(nl instanceof NodeStub) {
	    stub = (NodeStub)nl;
	}
	else {
	    stub = new NodeStub(getID(remoteObject));

	    // Add the local address
	    stub.addTA(icpTA);
	}

      return stub;
    }

    throw new IMTPException("can't create a stub for object " + remoteObject + ".");
  } 


  /**
   * This method dispatches the specified command to the first address
   * (among those specified) to which dispatching succeeds.
   * 
   * @param destTAs a list of transport addresses where the command
   * dispatcher should try to dispatch the command.
   * @param command the command that is to be dispatched.
   * @return a response command from the receiving container.
   * @throws DispatcherException if an error occurs during dispatching.
   * @throws UnreachableException if none of the destination addresses
   * is reachable.
   */
  public Command dispatchCommand(List destTAs, 
                                 Command command) throws DispatcherException, UnreachableException {

    // DEBUG
    //System.out.println("Dispatching command of type " + command.getCode());
    try {
      Command response = dispatchSerializedCommand(destTAs, serializeCommand(command), name);

      // If the dispatched command was an ADD_CONTAINER --> get the
      // name from the response and use it as the name of the CommandDispatcher
      if (command.getCode() == Command.ADD_NODE && name.equals(DEFAULT_NAME)) {
        name = (String) response.getParamAt(0);
      }

      return response;
    } 
    catch (LEAPSerializationException lse) {
      throw new DispatcherException("Error serializing command "+command+" ["+lse.getMessage()+"]");
    } 
  } 

  /**
   * Dispatches the specified serialized command to one of the
   * specified transport addresses (the first where dispatching
   * succeeds) directly or through the router.
   * 
   * @param destTAs a list of transport addresses where the command
   * dispatcher should try to dispatch the command.
   * @param commandPayload the serialized command that is to be
   * dispatched.
   * @param origin a <tt>String</tt> object describing the origin of
   * the command to be dispatched.
   * @return a response command from the receiving container.
   * @throws DispatcherException if an error occurs during dispatching.
   * @throws UnreachableException if none of the destination addresses
   * is reachable.
   */
  protected Command dispatchSerializedCommand(List destTAs, byte[] commandPayload, String origin) 
          throws DispatcherException, UnreachableException {

    // Be sure that the destination addresses are correctly specified
    if (destTAs == null || destTAs.size() == 0) {
      throw new DispatcherException("no destination address specified.");

    } 

    byte[] responsePayload = null;
    try {

      // Try to dispatch the command directly
      responsePayload = dispatchDirectly(destTAs, commandPayload);

      // Runtime.instance().gc(23);
    } 
    catch (UnreachableException ue) {
      // Direct dispatching failed --> Try through the router
      // DEBUG
      // System.out.println("Dispatch command through router");
      responsePayload = dispatchThroughRouter(destTAs, commandPayload, origin);

      // Runtime.instance().gc(24);
    } 

    // Deserialize the response
    try {
      Command response = deserializeCommand(responsePayload);
      // Runtime.instance().gc(25);

      // Check whether some exceptions to be handled by the
      // CommandDispatcher occurred on the remote site
      checkRemoteExceptions(response);

      return response;
    } 
    catch (LEAPSerializationException lse) {
      throw new DispatcherException("error deserializing response ["+lse.getMessage()+"].");
    } 
  } 

  /**
   * Dispatches the specified serialized command to one of the
   * specified transport addresses (the first where dispatching
   * succeeds) directly.
   * 
   * @param destTAs a list of transport addresses where the command
   * dispatcher should try to dispatch the command.
   * @param commandPayload the serialized command that is to be
   * dispatched.
   * @return a serialized response command from the receiving
   * container.
   * @throws UnreachableException if none of the destination addresses
   * is reachable.
   */
  protected byte[] dispatchDirectly(List destTAs, 
                                    byte[] commandPayload) throws UnreachableException {

    // Loop on destinaltion addresses (No need to check again
    // that the list of addresses is not-null and not-empty)
    for (int i = 0; i < destTAs.size(); i++) {
      try {
        return send((TransportAddress) destTAs.get(i), commandPayload);
      } 
      catch (UnreachableException ue) {
        // Can't send command to this address --> try the next one
        // DEBUG
        // TransportAddress ta = (TransportAddress)destTAs.get(i);
        // System.out.println("Sending command to " + ta.getProto() + "://" + ta.getHost() + ":" + ta.getPort() + " failed [" + ue.getMessage() + "]");
        // if (i < destTAs.size() - 1)
        // System.out.println("Try next address");
      } 
    } 

    // Sending failed to all addresses.
    // No need for a meaningful message as this exception will
    // trigger dispatching through router
    throw new UnreachableException("");
  } 

  /**
   * Dispatches the specified serialized command to one of the
   * specified transport addresses (the first where dispatching
   * succeeds) through the router.
   * 
   * @param destTAs a list of transport addresses where the command
   * dispatcher should try to dispatch the command.
   * @param commandPayload the serialized command that is to be
   * dispatched.
   * @param origin a <tt>String</tt> object describing the origin of
   * the command to be dispatched.
   * @return a serialized response command from the receiving
   * container.
   * @throws DispatcherException if an error occurs during dispatching.
   * @throws UnreachableException if none of the destination addresses
   * is reachable.
   */
  protected byte[] dispatchThroughRouter(List destTAs, byte[] commandPayload, String origin) 
          throws DispatcherException, UnreachableException {
    if (routerTA == null) {
      throw new UnreachableException("destination unreachable.");

      // Build a FORWARD command
    } 

    Command forward = new Command(Command.FORWARD);
    forward.addParam(commandPayload);
    forward.addParam(destTAs);
    forward.addParam(origin);

    // Runtime.instance().gc(26);

    try {
      return send(routerTA, serializeCommand(forward));
    } 
    catch (LEAPSerializationException lse) {
      throw new DispatcherException("error serializing FORWARD command ["+lse.getMessage()+"].");
    } 
  } 

  /**
   * Checks whether some exceptions to be handled by the command
   * dispatcher occurred on the remote site. If this is the case the
   * command dispatcher throws the corresponding exception locally.
   * 
   * @param response the resonse comman from the receiving container.
   * @throws DispatcherException if an error occurs on the remote site
   * during dispatching.
   * @throws UnreachableException if the destination address is not
   * reachable.
   */
  protected void checkRemoteExceptions(Command response) 
          throws DispatcherException, UnreachableException {
    if (response.getCode() == Command.ERROR) {
      String exception = (String) response.getParamAt(0);

      // DispatcherException (some error occurred in the remote
      // CommandDispatcher) --> throw a DispatcherException.
      if (exception.equals("jade.imtp.leap.DispatcherException")) {
        throw new DispatcherException("DispatcherException in remote site.");
      } 
      else    // UnreachableException (the Command was sent to the router,
      // but the final destination was unreachable from there)
      // --> throw an UnreachableException
      if (exception.equals("jade.core.UnreachableException")) {
        throw new UnreachableException((String) response.getParamAt(1));
      } 
    } 
  } 

  /**
   * Selects a suitable peer and sends the specified serialized command
   * to the specified transport address.
   * 
   * @param ta the transport addresses where the command should be
   * sent.
   * @param commandPayload the serialized command that is to be
   * sent.
   * @return a serialized response command from the receiving
   * container.
   * @throws UnreachableException if the destination address is not
   * reachable.
   */
  protected byte[] send(TransportAddress ta, byte[] commandPayload) throws UnreachableException {

    // Check if the ICP is suitable for the given TransportAddress
    if (icp == null ||!icp.getProtocol().getName().equals(ta.getProto())) {
      throw new UnreachableException("no ICP suitable for protocol "+ta.getProto()+".");
    } 

    try {
      return icp.deliverCommand(ta, commandPayload);
    } 
    catch (ICPException icpe) {
      throw new UnreachableException("ICPException delivering command to address "+ta+".");
    } 
  } 

  /**
   * Serializes a <tt>Command</tt> object into a <tt>byte</tt> array.
   * 
   * @param command the command to be serialized.
   * @return the serialized command.
   * @throws LEAPSerializationException if the command cannot be
   * serialized.
   */
  protected byte[] serializeCommand(Command command) throws LEAPSerializationException {
    DeliverableDataOutputStream ddout = new DeliverableDataOutputStream(this);
    ddout.serializeCommand(command);

    return ddout.getSerializedByteArray();
  } 

  /**
   * Deserializes a <tt>Command</tt> object from a <tt>byte</tt> array.
   * 
   * @param data the <tt>byte</tt> array containing serialized command.
   * @return the deserialized command.
   * @throws LEAPSerializationException if the command cannot be
   * deserialized.
   */
  protected Command deserializeCommand(byte[] data) throws LEAPSerializationException {
    return new DeliverableDataInputStream(data, this).deserializeCommand();
  } 

  /**
   * Builds a command that carries an exception.
   * 
   * @param exception the exception to be carried.
   * @return the command carrying the exception.
   */
  protected Command buildExceptionResponse(Exception exception) {
    Command response = new Command(Command.ERROR);
    response.addParam(exception.getClass().getName());
    response.addParam(exception.getMessage());

    return response;
  } 

  /**
   * Shuts the command dipatcher down and deactivates the local ICPs.
   */
  public void shutDown() {
    try {

      if (icp != null) {
        // This call interrupts the listening thread of this peer
        // and waits for its completion.
        icp.deactivate();
				icp = null;
        // DEBUG
        // System.out.println("ICP deactivated.");
      } 
    } 
    catch (ICPException icpe) {

      // Do nothing as this means that this peer had never been
      // activated.
    } 
  } 

  // /////////////////////////////////////////
  // ICP.Listener INTERFACE
  // /////////////////////////////////////////

  /**
   * Handles a received (still serialized) command object, i.e.
   * deserialize it and launch processing of the command.
   * 
   * @param commandPayload the command to be deserialized and
   * processed.
   * @return a <tt>byte</tt> array containing the serialized response
   * command.
   * @throws LEAPSerializationException if the command cannot be
   * (de-)serialized.
   */
  public byte[] handleCommand(byte[] commandPayload) throws LEAPSerializationException {
    try {

      // DEBUG
      //System.out.println("Received command of type " + deserializeCommand(commandPayload).getCode());

      // Deserialize the incoming command and let the Skeleton
      // process it.
      return serializeCommand(skeleton.processCommand(deserializeCommand(commandPayload)));
    } 
    catch (Exception e) {
      e.printStackTrace();

      // FIXME. If this throws an exception this is not
      // handled by the CommandDispatcher
      return serializeCommand(buildExceptionResponse(new DispatcherException(e.getMessage())));
    } 
  } 

    private TransportAddress initMainTA(Profile p) throws IMTPException {

	TransportAddress mainTA = null;

	try {
	    String mainURL = p.getParameter(LEAPIMTPManager.MAIN_URL, null);

	    // Try to translate the mainURL into a TransportAddress
	    // using a protocol supported by this CommandDispatcher
	    try {
		mainTA = stringToAddr(mainURL);
	    }
	    catch (DispatcherException de) {
		// Failure --> A suitable protocol class may be explicitly
		// indicated in the profile (otherwise rethrow the exception)
		String mainTPClass = p.getParameter(MAIN_PROTO_CLASS, null);
		if (mainTPClass != null) {
		    TransportProtocol tp = (TransportProtocol) Class.forName(mainTPClass).newInstance();
		    mainTA = tp.stringToAddr(mainURL);
		}
		else {
		    throw de;
		}
	    }

	    // If the router TA was not set --> use the mainTA as default
	    if (routerTA == null) {
    		routerTA = mainTA;
	    }

	    return mainTA;

	}
	catch (Exception e) {
	    throw new IMTPException("Error getting Main Container address", e);
	}

    }

    /***

  private MainContainer createMainContainerStub(TransportAddress mainTA) {
    MainContainerStub stub = new MainContainerStub();
    stub.bind(this);
    stub.addTA(mainTA);

    return stub;
  } 
    ***/
}

