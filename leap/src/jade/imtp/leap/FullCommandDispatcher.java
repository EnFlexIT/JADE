/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

/*
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
import jade.core.CaseInsensitiveString;
import jade.core.IMTPException;
import jade.core.UnreachableException;
import jade.core.Profile;
import jade.mtp.TransportAddress;
import jade.util.leap.ArrayList;
import jade.util.leap.Iterator;
import jade.util.leap.List;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * This class provides a full implementation of a command dispatcher. The
 * command dispatcher provides support for multiple remote objects,
 * multiple ICPs and command routing.
 * 
 * <p>The command dispatcher is based on an implementation written by
 * Michael Watzke and Giovanni Caire (TILAB), 09/11/2000.</p>
 * 
 * @author Tobias Schaefer
 * @version 1.0
 */
class FullCommandDispatcher extends CommandDispatcher {

	/**
	   The MainContainer object (the real one or a stub of it) 
	 */
	private MainContainer theMain = null;
	
  /**
   * This hashtable maps the IDs of the objects remotized by this
   * command dispatcher to the skeletons for these objects. It is used
   * when a command is received from a remote JVM.
   */
  protected Hashtable skeletons = new Hashtable();

  /**
   * This hashtable maps the objects remotized by this command
   * dispatcher to their IDs. It is used when a stub of a remotized
   * object must be built to be sent to a remote JVM.
   */
  protected Hashtable ids = new Hashtable();

  /**
   * A counter that is used for determining IDs for remotized objects.
   * Everytime a new object is registered by the command dispatcher it
   * gets the value of this field as ID and the field is increased.
   */
  protected int       nextID;

  /**
   * The pool of ICP objects used by this command dispatcher to
   * actually send/receive data over the network. It is a table that
   * associates a <tt>String</tt> representing a protocol (e.g. "http")
   * to a list of ICPs supporting that protocol.
   */
  protected Hashtable icps = new Hashtable();

  /**
   * The transport addresses the ICPs managed by this command
   * dispatcher are listening for commands on.
   */
  protected List      addresses = new ArrayList();

  /**
   * The URLs corresponding to the local transport addresses.
   */
  protected List      urls = new ArrayList();

  /**
   * A sole constructor. To get a command dispatcher the constructor
   * should not be called directly but the static <tt>create</tt> and
   * <tt>getDispatcher</tt> methods should be used. Thereby the
   * existence of a singleton instance of the command dispatcher will
   * be guaranteed.
   */
  public FullCommandDispatcher() {

    // Set a temporary name. Will be substituted as soon as the first
    // container attached to this CommandDispatcher will receive a
    // unique name from the main.
    name = DEFAULT_NAME;
    nextID = 1;

    // DEBUG
    // System.out.println("full command dispatcher initialized");
  }

  /**
   * Return a MainContainer object to call methods on the Main container
   */
  public MainContainer getMain(Profile p) throws IMTPException {
  	if (theMain == null) {
  		theMain = super.getMain(p);
  	}
  	return theMain;
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

      // Activate the peer.
      TransportAddress  ta = peer.activate(this, peerID, p);

      // Add the listening address to the list of local addresses.
      TransportProtocol tp = peer.getProtocol();
      String            url = tp.addrToString(ta);

      addresses.add(ta);
      urls.add(url);

      // Put the peer in the table of local ICPs.
      String proto = tp.getName().toLowerCase();
      List                  list = (List) icps.get(proto);
      if (list == null) {
        icps.put(proto, (list = new ArrayList()));
      } 

      list.add(peer);
    } 
    catch (ICPException icpe) {

      // Print a warning.
      System.out.println("Error adding ICP "+peer+"["+icpe.getMessage()+"].");
    } 
  } 

  /**
   * Returns the ID of the specified remotized object.
   * 
   * @param remoteObject the object whose ID should be returned.
   * @return the ID of the reomte object.
   * @throws RuntimeException if the specified object is not
   * remotized by this command dispatcher.
   */
  public int getID(Object remoteObject) throws IMTPException {
    Integer id = (Integer) ids.get(remoteObject);
    if (id != null) {
      return id.intValue();
    } 

    throw new IMTPException("specified object is not remotized by this command dispatcher.");
  } 

  /**
   * Returns the list of local addresses.
   * 
   * @return the list of local addresses.
   */
  public List getLocalTAs() {
    return addresses;
  } 

  /**
   * Returns the list of URLs corresponding to the local addresses.
   * 
   * @return the list of URLs corresponding to the local addresses.
   */
  public List getLocalURLs() {
    return urls;
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
    Enumeration peers = icps.elements();

    while (peers.hasMoreElements()) {

      // Try to convert the url using the TransportProtocol
      // supported by this ICP.
      try {
        // There can be more than one peer supporting the same
        // protocol. Use the first one.
        return ((ICP) ((List) peers.nextElement()).get(0)).getProtocol().stringToAddr(url);
      } 
      catch (ICPException icpe) {

        // Do nothing and try the next one.
      } 
    } 

    // If we reach this point the url can't be converted.
    throw new DispatcherException("can't convert URL "+url+".");
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
  	Integer id = null;
  	if (remoteObject instanceof MainContainer) {
    	id = new Integer(0);
    	name = "Main-Container";
    	if (theMain != null) {
    		System.out.println("WARNING: Replacing main in CommandDispatcher!!!");
    	}
    	theMain = (MainContainer) remoteObject;
  	}
  	else {
    	id = new Integer(nextID++);
  	}
    skeletons.put(id, skeleton);
    ids.put(remoteObject, id);
  } 

  /**
   * Deregisters the specified remote object from the command dispatcher.
   * 
   * @param remoteObject the remote object related to the specified
   * skeleton.
   */
  public void deregisterSkeleton(Object remoteObject) {
    try {
      skeletons.remove(ids.remove(remoteObject));
    } 
    catch (NullPointerException npe) {
    } 

    if (ids.isEmpty()) {
      //System.out.println("CommandDispatcher shutting down");
      shutDown();
    } 
  } 

  /**
   * Builds a stub for an already remotized object.
   * 
   * @param remoteObject the remote object the stub depends on.
   * @return a new stub depending on the specified remote object.
   * @throws IMTPException if the stub cannot be created.
   */
  public Stub buildLocalStub(Object remoteObject) throws IMTPException {
    Stub stub = null;

    if (remoteObject instanceof MainContainer) {
      //stub = new MainContainerStub((MainContainer) remoteObject);
      stub = new MainContainerStub(getID(remoteObject));
    } 
    else if (remoteObject instanceof AgentContainer) {
      //stub = new AgentContainerStub((AgentContainer) remoteObject);
      stub = new AgentContainerStub(getID(remoteObject));
    } 
    else {
      throw new IMTPException("can't create a stub for object "+remoteObject+".");
    } 

    // Add the local addresses.
    Iterator it = addresses.iterator();

    while (it.hasNext()) {
      stub.addTA((TransportAddress) it.next());
    } 

    return stub;
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

    // Get the ICPs suitable for the given TransportAddress.
    List list = (List) icps.get(ta.getProto().toLowerCase());

    if (list == null) {
      throw new UnreachableException("no ICP suitable for protocol "+ta.getProto()+".");

    } 

    for (int i = 0; i < list.size(); i++) {
      try {
        return ((ICP) list.get(i)).deliverCommand(ta, commandPayload);
      } 
      catch (ICPException icpe) {
        // Print a warning and try next address
      	System.out.println("Warning: can't deliver command to "+ta+". "+icpe.getMessage());
      } 
    } 

    throw new UnreachableException("ICPException delivering command to address "+ta+".");
  } 

  /**
   * Shuts the command dipatcher down and deactivates the local ICPs.
   */
  public void shutDown() {
    Enumeration peersKeys = icps.keys();

    while (peersKeys.hasMoreElements()) {
      List list = (List) icps.remove(peersKeys.nextElement());

      for (int i = 0; i < list.size(); i++) {
        try {

          // This call interrupts the listening thread of this peer
          // and waits for its completion.
          ((ICP) list.get(i)).deactivate();

          // DEBUG
          // System.out.println("ICP deactivated.");
        } 
        catch (ICPException icpe) {

          // Do nothing as this means that this peer had never been
          // activated.
        } 
      } 
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

      // Deserialize the incoming command.
      Command command = deserializeCommand(commandPayload);
      Command response = null;

      // DEBUG
      // System.out.println("Received command of type " + command.getCode());
      if (command.getCode() == Command.FORWARD) {

        // DEBUG
        // System.out.println("Routing command");

        // If this is a FORWARD command then handle it directly.
        byte[] originalPayload = (byte[]) command.getParamAt(0);
        List   destTAs = (List) command.getParamAt(1);
        String origin = (String) command.getParamAt(2);

        if (origin.equals(name)) {

          // The forwarding mechanism is looping.
          response = 
            buildExceptionResponse(new UnreachableException("destination unreachable (and forward loop)."));
        } 
        else {
          try {
            response = dispatchSerializedCommand(destTAs, originalPayload, origin);
          } 
          catch (UnreachableException ue) {

            // rsp = buildExceptionResponse("jade.core.UnreachableException", ue.getMessage());
            response = buildExceptionResponse(ue);
          } 
        } 
      } 
      else {

        // If this is a normal Command, let the proper Skeleton
        // process it.
      	Integer id = new Integer(command.getObjectID());
      	Skeleton s = (Skeleton) skeletons.get(id);
      	if (s == null) {
      		throw new DispatcherException("No skeleton for object-id "+id);
      	}
        response = s.processCommand(command);
      } 

      return serializeCommand(response);
    } 
    catch (Exception e) {
      e.printStackTrace();

      // FIXME. If this throws an exception this is not handled by
      // the CommandDispatcher.
      return serializeCommand(buildExceptionResponse(new DispatcherException(e.getMessage())));
    } 
  } 

}

