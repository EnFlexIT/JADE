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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.FileWriter;
import java.io.StringWriter;

import java.net.InetAddress;
import java.net.MalformedURLException;

import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import java.lang.reflect.*;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Set;

import jade.lang.acl.ACLMessage;

import jade.domain.MobilityOntology;
import jade.domain.FIPAAgentManagement.InternalError;
import jade.domain.FIPAAgentManagement.Envelope;

import jade.mtp.MTP;
import jade.mtp.MTPException;
import jade.mtp.TransportAddress;
import jade.lang.acl.ACLCodec;

/**
@author Giovanni Rimassa - Universita` di Parma
@version $Date$ $Revision$
*/

class AgentContainerImpl extends UnicastRemoteObject implements AgentContainer, AgentToolkit {

  private static final int CACHE_SIZE = 10;

  
  // Local agents, indexed by agent name
  protected LADT localAgents = new LADT();

  // Agents cache, indexed by agent name
  private AgentCache cachedProxies = new AgentCache(CACHE_SIZE);

  // ClassLoader table, used for agent mobility
  private Map loaders = new HashMap();

  // This Map holds the mapping between an agent that arrived on this
  // container and the container where its classes can be retrieved
  private Map sites = new HashMap();

  // The agent platform this container belongs to
  protected MainContainer myPlatform;

  protected String myName;

  // The Agent Communication Channel, managing the external MTPs.
  protected acc theACC;

  // Unique ID of the platform, used to build the GUID of resident
  // agents.
  protected String platformID;

  // An object used to manage Agent IDs using nicknames
  protected AIDTranslator translator;

  private Map SniffedAgents = new HashMap();
  private String theSniffer;           

  // This monitor is used to hang a remote ping() call from the front
  // end, in order to detect container failures.
  private java.lang.Object pingLock = new java.lang.Object();

  private ThreadGroup agentThreads = new ThreadGroup("JADE Agents");
  private ThreadGroup criticalThreads = new ThreadGroup("JADE time-critical threads");

  public AgentContainerImpl() throws RemoteException {

    // Set up attributes for agents thread group
    agentThreads.setMaxPriority(Thread.NORM_PRIORITY);

    // Set up attributes for time critical threads
    criticalThreads.setMaxPriority(Thread.MAX_PRIORITY);

    // Initialize timer dispatcher
    TimerDispatcher td = new TimerDispatcher();
    Thread t = new Thread(criticalThreads, td);
    t.setPriority(criticalThreads.getMaxPriority());

    td.setThread(t);
    // This call starts the timer dispatcher thread
    Agent.setDispatcher(td);

  }


  // Interface AgentContainer implementation

  public void createAgent(AID agentID, String className,String[] args, boolean startIt) throws RemoteException {

    Agent agent = null;
    try {
        agent = (Agent)Class.forName(new String(className)).newInstance();
        agent.setArguments(args);
    }
    catch(ClassNotFoundException cnfe) {
      System.err.println("Class " + className + " for agent " + agentID + " was not found.");
      return;
    }
    catch( Exception e ){
      e.printStackTrace();
    }

    initAgent(agentID, agent, startIt);
  }

  public void createAgent(AID agentID, byte[] serializedInstance, AgentContainer classSite, boolean startIt) throws RemoteException {

    final AgentContainer ac = classSite;

    class Deserializer extends ObjectInputStream {

      public Deserializer(InputStream inner) throws IOException {
	super(inner);
      }

      protected Class resolveClass(ObjectStreamClass v) throws IOException, ClassNotFoundException {
	ClassLoader cl = (ClassLoader)loaders.get(ac);
	if(cl == null) {
	  cl = new JADEClassLoader(ac);
	  loaders.put(ac, cl);
	}
	return(cl.loadClass(v.getName()));
      }

    }

    try {
      ObjectInputStream in = new Deserializer(new ByteArrayInputStream(serializedInstance));

      Agent instance = (Agent)in.readObject();
      // Store the container where the classes for this agent can be
      // retrieved
      sites.put(instance, classSite);
      initAgent(agentID, instance, startIt);

    }
    catch(IOException ioe) {
      ioe.printStackTrace();
    }
    catch(ClassNotFoundException cnfe) {
      cnfe.printStackTrace();
    }

  }

  // Accepts the fully qualified class name as parameter and searches
  // the class file in the classpath
  public byte[] fetchClassFile(String name) throws RemoteException, ClassNotFoundException {
    name = name.replace( '.' , '/') + ".class";
    InputStream classStream = ClassLoader.getSystemResourceAsStream(name);
    if (classStream == null) 
      throw new ClassNotFoundException();
    try {
      byte[] bytes = new byte[classStream.available()];
      classStream.read(bytes);
      return(bytes);
    } catch (IOException ioe) {
	throw new ClassNotFoundException();
    }
  }

  void initAgent(AID agentID, Agent instance, boolean startIt) {

    // Subscribe as a listener for the new agent
    instance.setToolkit(this);

    // put the agent in the local table and get the previous one, if any
    Agent previous = localAgents.put(agentID, instance);
    if(startIt) {
      try {
	RemoteProxyRMI rp = new RemoteProxyRMI(this, agentID);
	myPlatform.bornAgent(agentID, rp, myName); // RMI call
	instance.powerUp(agentID, agentThreads);
      }
      catch(NameClashException nce) {
	System.out.println("Agentname already in use:"+nce.getMessage());
	localAgents.remove(agentID);
	localAgents.put(agentID,previous);
      }
      catch(RemoteException re) {
	System.out.println("Communication error while adding a new agent to the platform.");
	re.printStackTrace();
      }
    }
  }

  public void suspendAgent(AID agentID) throws RemoteException, NotFoundException {
    Agent agent = localAgents.get(agentID);
    if(agent == null)
      throw new NotFoundException("SuspendAgent failed to find " + agentID);
    agent.doSuspend();
  }

  public void resumeAgent(AID agentID) throws RemoteException, NotFoundException {
    Agent agent = localAgents.get(agentID);
    if(agent == null)
      throw new NotFoundException("ResumeAgent failed to find " + agentID);
    agent.doActivate();
  }

  public void waitAgent(AID agentID) throws RemoteException, NotFoundException {
    Agent agent = localAgents.get(agentID);
    if(agent==null)
      throw new NotFoundException("WaitAgent failed to find " + agentID);
    agent.doWait();
  }

  public void wakeAgent(AID agentID) throws RemoteException, NotFoundException {
    Agent agent = localAgents.get(agentID);
    if(agent==null)
      throw new NotFoundException("WakeAgent failed to find " + agentID);
    agent.doWake();
  }

  public void moveAgent(AID agentID, Location where) throws RemoteException, NotFoundException {
    Agent agent = localAgents.get(agentID);
    if(agent==null)
      throw new NotFoundException("MoveAgent failed to find " + agentID);
    agent.doMove(where);
  }

  public void copyAgent(AID agentID, Location where, String newName) throws RemoteException, NotFoundException {
    Agent agent = localAgents.get(agentID);
    if(agent == null)
      throw new NotFoundException("CopyAgent failed to find " + agentID);
    agent.doClone(where, newName);
  }

  public void killAgent(AID agentID) throws RemoteException, NotFoundException {
    Agent agent = localAgents.get(agentID);
    if(agent == null)
      throw new NotFoundException("KillAgent failed to find " + agentID);
    agent.doDelete();
  }

  public void exit() throws RemoteException {
    shutDown();
    System.exit(0);
  }

  public void postTransferResult(AID agentID, boolean result, List messages) throws RemoteException, NotFoundException {
    synchronized(localAgents) {
      Agent agent = localAgents.get(agentID);
      if((agent == null)||(agent.getState() != Agent.AP_TRANSIT)) {
	throw new NotFoundException("postTransferResult() unable to find a suitable agent.");
      }
      if(result == TRANSFER_ABORT)
	localAgents.remove(agentID);
      else {
	// Insert received messages at the start of the queue
	for(int i = messages.size(); i > 0; i--)
	  agent.putBack((ACLMessage)messages.get(i - 1));
	agent.powerUp(agentID, agentThreads);
      }
    }
  }

  /**
    @param toBeSniffer is an Iterator over the AIDs of agents to be sniffed
  **/
  public void enableSniffer(AID snifferName , List toBeSniffed) throws RemoteException {
    // In the SniffedAgents hashmap the key is the agent name and the value 
    // is a list containing the sniffer names for that agent 
    Iterator iOnToBeSniffed = toBeSniffed.iterator();
    while(iOnToBeSniffed.hasNext()) {
      AID aid = (AID)iOnToBeSniffed.next();
      ArrayList l;
      if (SniffedAgents.containsKey(aid)) {
	l = (ArrayList)SniffedAgents.get(aid);
	if (!l.contains(snifferName))
	  l.add(snifferName);
      }
      else {
	l = new ArrayList(1);
	l.add(snifferName);
	SniffedAgents.put(aid,l);
      }
    }
  }


  public void disableSniffer(AID snifferName, List notToBeSniffed) throws RemoteException {
    // In the SniffedAgents hashmap the key is the agent name and the value 
    // is a list containing the sniffer names for that agent 
    Iterator iOnNotToBeSniffed = notToBeSniffed.iterator();
    while(iOnNotToBeSniffed.hasNext()) {
      AID aid = (AID)iOnNotToBeSniffed.next();
      ArrayList l;
      if (SniffedAgents.containsKey(aid)) {
	l = (ArrayList)SniffedAgents.get(aid);
	int ind = l.indexOf(snifferName);
	if (ind >= 0)
	  l.remove(ind);
      } 
    }
  }



  public void dispatch(ACLMessage msg, AID receiverID) throws RemoteException, NotFoundException {

    // Mutual exclusion with handleMove() method
    synchronized(localAgents) {
      Agent receiver = localAgents.get(receiverID);

      if(receiver == null) {
	throw new NotFoundException("DispatchMessage failed to find " + receiverID);
      }

      receiver.postMessage(msg);
    }

  }

  public void ping(boolean hang) throws RemoteException {
    if(hang) {
      synchronized(pingLock) {
	try {
	  pingLock.wait();
	}
	catch(InterruptedException ie) {
	  // Do nothing
	}
      }
    }
  }

  protected void installACLCodec(String className) throws jade.lang.acl.ACLCodec.CodecException{
  
  	try{
  			Class c = Class.forName(className);
  			ACLCodec codec = (ACLCodec)c.newInstance(); 
  			theACC.addACLCodec(codec);
  			System.out.println("Added ACLCodec: " + className + " implementing " + codec.getName()+"\n");
  			//FIXME: notify the AMS of the new Codec to update the APDescritption.
  		}catch(ClassNotFoundException cnfe){
  			throw new jade.lang.acl.ACLCodec.CodecException("ERROR: The class " +className +" for the ACLCodec not found.",cnfe);
  		}catch(InstantiationException ie){
  			throw new jade.lang.acl.ACLCodec.CodecException("The class " + className + " raised InstantiationException (see NestedException)",ie);
  		}catch(IllegalAccessException iae){
  			throw new jade.lang.acl.ACLCodec.CodecException("The class " + className  + " raised IllegalAccessException (see nested exception)", iae);
  		}
 		
  }
  
  public String installMTP(String address, String className) throws RemoteException, MTPException {
    try {
      Class c = Class.forName(className);
      MTP proto = (MTP)c.newInstance();
      TransportAddress ta = theACC.addMTP(proto, address);
      String result = proto.addrToStr(ta);
      myPlatform.newMTP(result, myName);
      return result;
    }
    catch(ClassNotFoundException cnfe) {
      throw new MTPException("ERROR: The class " + className  + " for the " + address  + " MTP was not found");
    }
    catch(InstantiationException ie) {
      throw new MTPException("The class " + className + " raised InstantiationException (see nested exception)", ie);
    }
    catch(IllegalAccessException iae) {
      throw new MTPException("The class " + className  + " raised IllegalAccessException (see nested exception)", iae);
    }
  }

  public void uninstallMTP(String address) throws RemoteException, NotFoundException, MTPException {
    theACC.removeMTP(address);
    myPlatform.deadMTP(address, myName);
  }

  public void updateRoutingTable(int op, String address, AgentContainer ac) throws RemoteException {
    switch(op) {
    case ADD_RT:
      theACC.addRoute(address, ac);
      break;
    case DEL_RT:
      theACC.removeRoute(address, ac);
      break;
    }

  }

  public void route(Object env, byte[] payload, String address) throws RemoteException, MTPException {
    theACC.forwardMessage((jade.domain.FIPAAgentManagement.Envelope)env, payload, address);
  }

  public void joinPlatform(String pID, Iterator agentSpecifiers, String[] MTPs,String[] ACLCodecs) {

    // This string will be used to build the GUID for every agent on this platform.
    platformID = pID;

    translator = new AIDTranslator(platformID);

    // Build the Agent IDs for the AMS and for the Default DF.
    Agent.initReservedAIDs(globalAID("ams"), globalAID("df"));

    try {
      // Retrieve agent platform from RMI registry and register as agent container
      String platformRMI = "rmi://" + platformID;
      myPlatform = lookup3(platformRMI);

      theACC = new acc(this, platformID);

      //Install the ACLCodecs inserted by command line.
      for(int i =0; i<ACLCodecs.length;i++){
      	String className = ACLCodecs[i];
      	installACLCodec(className);
      }
      
      InetAddress netAddr = InetAddress.getLocalHost();
      myName = myPlatform.addContainer(this, netAddr); // RMI call

      // Install required MTPs
      FileWriter f = new FileWriter("MTPs-" + myName + ".txt");

      for(int i = 0; i < MTPs.length; i += 2) {

				String className = MTPs[i];
				String addressURL = MTPs[i+1];
				if(addressURL.equals(""))
	  			addressURL = null;
				String s = installMTP(addressURL, className);

				f.write(s, 0, s.length());
				f.write('\n');
				System.out.println(s);
      }

      f.close();
    }
    catch(RemoteException re) {
      System.err.println("Communication failure while contacting agent platform.");
      re.printStackTrace();
      System.exit(0);
    }
    catch(Exception e) {
      System.err.println("Some problem occurred while contacting agent platform.");
      e.printStackTrace();
      System.exit(0);
    }


    /* Create all agents and set up necessary links for message passing.

       The link chain is:
       a) Agent 1 -> AgentContainer1 -- Through CommEvent
       b) AgentContainer 1 -> AgentContainer 2 -- Through RMI (cached or retreived from MainContainer)
       c) AgentContainer 2 -> Agent 2 -- Through postMessage() (direct insertion in message queue, no events here)

       agentSpecifiers is a List of List.Every list contains ,orderly, th a agent name the agent class and the arguments (if any).
    */
    
      while(agentSpecifiers.hasNext()) 
    {
      Iterator i = ((List)agentSpecifiers.next()).iterator();
    	String agentName =(String)i.next();
    	String agentClass = (String)i.next();
      List tmp = new ArrayList(); 
    	for ( ; i.hasNext(); )	         
    	  tmp.add((String)i.next());
    	  
      //Create the String[] args to pass to the createAgent method  
    	int size = tmp.size();
      String arguments[] = new String[size];
      Iterator it = tmp.iterator();
      for(int n = 0; it.hasNext(); n++)
        arguments[n] = (String)it.next();
      
    	AID agentID = globalAID(agentName);
      try {
	      createAgent(agentID, agentClass, arguments, NOSTART);
	      RemoteProxyRMI rp = new RemoteProxyRMI(this, agentID);
	      myPlatform.bornAgent(agentID, rp, myName);
      }
      catch(RemoteException re) { // It should never happen
	      re.printStackTrace();
      }
      catch(NameClashException nce) {
	    System.out.println("Agent name already in use: "+nce.getMessage());
	    localAgents.remove(agentID);
      }
	
    }

    // Now activate all agents (this call starts their embedded threads)
    AID[] allLocalNames = localAgents.keys();
    for(int i = 0; i < allLocalNames.length; i++) {
      AID id = allLocalNames[i];
      Agent agent = localAgents.get(id);
      agent.powerUp(id, agentThreads);
    }

    System.out.println("Agent container " + myName + " is ready.");

  }

  public void shutDown() {

    // Close all MTP links to the outside world
    List l = theACC.getLocalAddresses();
    String[] addresses = (String[])l.toArray(new String[0]);
    for(int i = 0; i < addresses.length; i++) {
      try {
	String addr = addresses[i];
	uninstallMTP(addr);
      }
      catch(RemoteException re) {
	// It should never happen
	System.out.println("ERROR: Remote Exception thrown for a local call.");
      }
      catch(NotFoundException nfe) {
	nfe.printStackTrace();
      }
      catch(MTPException mtpe) {
	mtpe.printStackTrace();
      }

    }

    // Close down the ACC
    theACC.shutdown();

    // Shuts down the Timer Dispatcher
    Agent.stopDispatcher();

    // Remove all agents
    Agent[] allLocalAgents = localAgents.values();

    for(int i = 0; i < allLocalAgents.length; i++) {
      // Kill agent and wait for its termination
      Agent a = allLocalAgents[i];
      a.doDelete();
      a.join();
      a.resetToolkit();
    }

    // Unblock threads hung in ping() method (this will deregister the container)
    synchronized(pingLock) {
      pingLock.notifyAll();
    }

  }


/*
 * This method returns the vector of the sniffers registered for 
 * theAgent
 */
private List getSniffer(AID id, java.util.Map theMap) {
  ArrayList tmp = (ArrayList)theMap.get(id);
  if (tmp == null) { 
    //might be that the AID is a local agent without '@hap' in its name
    // then I try it
    AID fullId = new AID(id.getName()+'@'+getPlatformID());
    tmp = (ArrayList)theMap.get(fullId);
  }
  return tmp;
}

  /*
   * Creates the message to be sent to the sniffer. The ontology must be set to 
   * "sniffed-message" otherwise the sniffer doesn't recognize it. The sniffed 
   * message is put in the content field of this message.
   *
   * @param theMsg handler of the sniffed message
   * @param theDests list of the destination (sniffers)
   */
  private void sendMsgToSniffers(ACLMessage theMsg, List theDests){

    AID currentSniffer;

    for (int z = 0; z < theDests.size(); z++) {
      currentSniffer = (AID)theDests.get(z);
      ACLMessage SniffedMessage = new ACLMessage(ACLMessage.INFORM);
      SniffedMessage.clearAllReceiver();
      SniffedMessage.addReceiver(currentSniffer);
      SniffedMessage.setSender(null);
      SniffedMessage.setContent(theMsg.toString());
      SniffedMessage.setOntology("sniffed-message");
      unicastPostMessage(SniffedMessage,currentSniffer);
    
    }
  }


  // Implementation of AgentToolkit interface

  public void handleSend(ACLMessage msg) {

    String currentSniffer;
    List currentSnifferVector;

    boolean sniffedSource = false;

    translator.translateOutgoing(msg);

    ArrayList sniffersToNotify = new ArrayList(SniffedAgents.size());
    AID msgSource = msg.getSender();
    currentSnifferVector = getSniffer(msgSource, SniffedAgents);
    if (currentSnifferVector != null) {
    	for (Iterator i=currentSnifferVector.iterator(); i.hasNext(); ) {
    		AID aSniffer = (AID)i.next();
    		if (!sniffersToNotify.contains(aSniffer))
    			sniffersToNotify.add(aSniffer);
    	}
    }
    //System.out.println("Sniffer to Notify- sender: "+ sniffersToNotify.size());
    // 26-Mar-2001. The receivers set into the Envelope of the message, 
    // if present, must have precedence over those set into the ACLMessage.
    // If no :intended-receiver parameter is present in the Envelope, 
    // then the :to parameter
    // is used to generate :intended-receiver field. 
    //
    // create an Iterator with all the receivers to which the message must be 
    // delivered
    Iterator it=null;
    Envelope env = msg.getEnvelope();
    if (env != null) {
	it = env.getAllIntendedReceiver();
	if ( (it != null) && (it.hasNext()) ) {
	    System.out.println("WARNING: Envelope.intendedReceiver taking precedence over ACLMessage.to");
	    // ok. use the intendedreceiver
	} else {
	    it = env.getAllTo();
	    if ( (it != null) && (it.hasNext()) ) {
		System.out.println("WARNING: Envelope.to taking precedence over ACLMessage.to");
		// ok. use the :to
		// FIXME. Should I copy all the :to values in the :IntendedReceiver?
	    } else {
		it = msg.getAllReceiver();
		// ok. use the receivers set in the ACLMessage
	    }
	}
    } else 
	it = msg.getAllReceiver(); //use the receivers set in the ACLMessage
    if (it == null)
	return; //No Message is sent in this case because no receiver was found
    // now it contains the Iterator with all the receivers of this message
    // Iterator it = msg.getAllReceiver();
    while(it.hasNext()) {
      AID dest = (AID)it.next();
      currentSnifferVector = getSniffer(dest, SniffedAgents);	    
      if (currentSnifferVector != null) {
    	 for (Iterator i=currentSnifferVector.iterator(); i.hasNext(); ) {
    		AID aSniffer = (AID)i.next();
    		if (!sniffersToNotify.contains(aSniffer))
    			sniffersToNotify.add(aSniffer);
    	 }
    	//System.out.println("Sniffer to Notify- in while: "+ sniffersToNotify.size() + dest.toString());
      }

      ACLMessage copy = (ACLMessage)msg.clone();
      unicastPostMessage(copy, dest);
    }
    //System.out.println("handle send per msg="+msg.toString());
    sendMsgToSniffers(msg,sniffersToNotify);	    		
    
  }

  public void handleStart(String localName, Agent instance) {
    AID agentID = globalAID(localName);
    initAgent(agentID, instance, START);
  }

  public void handleEnd(AID agentID) {
    try {
      localAgents.remove(agentID);
      myPlatform.deadAgent(agentID); // RMI call
      cachedProxies.remove(agentID); // FIXME: It shouldn't be needed
    }
    catch(RemoteException re) {
      re.printStackTrace();
    }
    catch(NotFoundException nfe) {
      nfe.printStackTrace();
    }
  }

  public void handleMove(AID agentID, Location where) {
    // Mutual exclusion with dispatch() method
    synchronized(localAgents) {
      try {
        String proto = where.getProtocol();
	if(!proto.equalsIgnoreCase(MobilityOntology.Location.DEFAULT_LOCATION_TP))
	  throw new NotFoundException("Internal error: Mobility protocol not supported !!!");

	String destName = where.getName();
	AgentContainer ac = myPlatform.lookup(destName);
	Agent a = localAgents.get(agentID);
	if(a == null)
	  throw new NotFoundException("Internal error: handleMove() called with a wrong name !!!");

	// Handle special 'running to stand still' case
	if(where.getName().equalsIgnoreCase(myName)) {
	  a.doExecute();
	  return;
	}

	ByteArrayOutputStream out = new ByteArrayOutputStream();
	try {
	  ObjectOutputStream encoder = new ObjectOutputStream(out);
	  encoder.writeObject(a);
	}
	catch(IOException ioe) {
	  ioe.printStackTrace();
	}

	byte[] bytes = out.toByteArray();
    // Gets the container where the agent classes can be retrieved
    AgentContainer classSite = (AgentContainer) sites.get(a);
    if (classSite == null) {    // The agent was born on this container
      classSite = this;
    } 
	ac.createAgent(agentID, bytes, classSite, NOSTART);

	// Perform an atomic transaction for agent identity transfer
	boolean transferResult = myPlatform.transferIdentity(agentID, myName, destName);
	List messages = new ArrayList();
	if(transferResult == TRANSFER_COMMIT) {

	  // Send received messages to the destination container
	  Iterator i = a.messages();
	  while(i.hasNext())
	    messages.add(i.next());
	  ac.postTransferResult(agentID, transferResult, messages);

	  // From now on, messages will be routed to the new agent
	  a.doGone();
	  localAgents.remove(agentID);
	  cachedProxies.remove(agentID); // FIXME: It shouldn't be needed
      sites.remove(a);
	}
	else {
	  a.doExecute();
	  ac.postTransferResult(agentID, transferResult, messages);
	}
      }
      catch(RemoteException re) {
	re.printStackTrace();
	// FIXME: Complete undo on exception
	Agent a = localAgents.get(agentID);
	if(a != null)
	  a.doDelete();
      }
      catch(NotFoundException nfe) {
	nfe.printStackTrace();
	// FIXME: Complete undo on exception
	Agent a = localAgents.get(agentID);
	if(a != null)
	  a.doDelete();
      }
    }
  }

  public void handleClone(AID agentID, Location where, String newName) {
    try {
      String proto = where.getProtocol();
      if(!proto.equalsIgnoreCase(MobilityOntology.Location.DEFAULT_LOCATION_TP))
	throw new NotFoundException("Internal error: Mobility protocol not supported !!!");
      AgentContainer ac = myPlatform.lookup(where.getName());
      Agent a = localAgents.get(agentID);
      if(a == null)
	throw new NotFoundException("Internal error: handleCopy() called with a wrong name !!!");

      ByteArrayOutputStream out = new ByteArrayOutputStream();
      try {
	ObjectOutputStream encoder = new ObjectOutputStream(out);
	encoder.writeObject(a);
      }
      catch(IOException ioe) {
	ioe.printStackTrace();
      }

      AID newID = globalAID(newName);
      byte[] bytes = out.toByteArray();
      // Gets the container where the agent classes can be retrieved
      AgentContainer classSite = (AgentContainer) sites.get(a);
      if (classSite == null) {    // The agent was born on this container
        classSite = this;
      } 
      ac.createAgent(newID, bytes, classSite, START);

    }
    catch(RemoteException re) {
      re.printStackTrace();
    }
    catch(NotFoundException nfe) {
      nfe.printStackTrace();
    }
  }

  // Private methods

  protected AID globalAID(String nickName) {
    return translator.globalAID(nickName);
  }

  // This hack is needed to overcome a bug in java.rmi.Naming class:
  // when an object reference is bound, unbound and then rebound
  // with the same URL, the next two lookup() calls will throw an
  // Exception without a reason.
  private MainContainer lookup3(String URL)
    throws RemoteException, NotBoundException, MalformedURLException, UnknownHostException {
    java.lang.Object o = null;
    try {
      o = Naming.lookup(URL);
    }
    catch(RemoteException re1) { // First one
      try {
	o = Naming.lookup(URL);
      }
      catch(RemoteException re2) { // Second one
	// Third attempt. If this one fails, there's really
	// something wrong, so we let the RemoteException go.
	o = Naming.lookup(URL);
      }
    }
    return (MainContainer)o;
  }


  String getPlatformID()
  {
  	return platformID;
  }
  
  // FIXME: Temporary hack (this should be private)
  void unicastPostMessage(ACLMessage msg, AID receiverID) {

    AgentProxy ap = cachedProxies.get(receiverID);
    if(ap != null) { // Cache hit :-)
      try {
	ap.dispatch(msg);
      }
      catch(NotFoundException nfe) { // Stale cache entry
	cachedProxies.remove(receiverID);
	dispatchUntilOK(msg, receiverID);
      }
    }
    else { // Cache miss :-(
      dispatchUntilOK(msg, receiverID);
    }

  }

    /**
     * This private method is used internally by the platform in order
     * to notify the sender of a message that a failure was reported by
     * the Message Transport Service.
     **/
    private void notifyFailureToSender(ACLMessage msg, InternalError ie) {
	//if (the sender is not the AMS and the performative is not FAILURE)
	if ( (msg.getSender().equals(Agent.getAMS())) && (msg.getPerformative()==ACLMessage.FAILURE) ) // sanity check to avoid infinte loops
	    return;
	// else send back a failure message
	ACLMessage failure = msg.createReply();
	failure.setPerformative(ACLMessage.FAILURE);
	//System.err.println(failure.toString());
	failure.setSender(Agent.getAMS());
	// FIXME the content is not completely correct, but that should
	// also avoid creating wrong content
	StringWriter content = new StringWriter();
	content.write("( (action ");
	msg.getSender().toText(content); 
	content.write(" ACLMessage ) "+ie.getMessage()+")");
	failure.setContent(content.toString());
	handleSend(failure);
    }

  private void dispatchUntilOK(ACLMessage msg, AID receiverID) {
    boolean ok;
    int i = 0;
    do {
      AgentProxy proxy;
      try {
	proxy = getFreshProxy(receiverID);
      }
      catch(NotFoundException nfe) { // Agent not found in GADT: error !!!
	System.err.println("Agent " + receiverID.getLocalName() + " was not found on agent platform.");
	System.err.println("Message from platform was: " + nfe.getMessage());
	notifyFailureToSender(msg, new InternalError("LocalAgentNotFound"));
	return;
      }
      try {
	proxy.dispatch(msg);
	cachedProxies.put(receiverID, proxy);
	ok = true;
      }
      catch(acc.NoMoreAddressesException nmae) { // The AID has no more valid addresses
	System.err.println("Agent " + receiverID.getLocalName() + " has no valid addresses.");
	notifyFailureToSender(msg, new InternalError("RemoteAgentNotFound"));
	return;
      }      
      catch(acc.UnknownACLEncodingException uae) { // No ACLcodec available 
	System.err.println(uae.getMessage()+" - message is undeliverable to " + receiverID.getLocalName());
	notifyFailureToSender(msg, new InternalError("NoACLCodec_Available"));
	return;
      }

      catch(NotFoundException nfe) { // Agent not found in destination LADT: need to recheck GADT
	ok = false;
      }
      /*
      i++;
      if(i > 100) { // Watchdog counter...
	System.out.println("===================================================================");
	System.out.println(" Possible livelock in message dispatching:");
	System.out.println(" Receiver is:");
	receiverID.toText(new java.io.OutputStreamWriter(System.out));
	System.out.println();
	System.out.println();
	System.out.println(" Message is:");
	msg.toText(new java.io.OutputStreamWriter(System.out));
	System.out.println();
	System.out.println();
	System.out.println("===================================================================");
	try {
	  Thread.sleep(3000);
	}
	catch(InterruptedException ie) {
	  System.out.println("Interrupted !!!");
	}
	return;
      }
      */
    } while(!ok);
  }

  private AgentProxy getFreshProxy(AID id) throws NotFoundException {
    AgentProxy result = null;
  if(translator.livesHere(id)) { // the receiver agent lives in this platform...
      // Look first in local agents
      Agent a = localAgents.get(id);
      if(a != null) {
	result = new LocalProxy(localAgents, id);
      }
      else { // Agent is not local
      
	// Maybe it's registered with this AP on some other container...
        try {
	  result = myPlatform.getProxy(id); // RMI call
	}
	catch(RemoteException re) {
	  System.out.println("Communication error while contacting agent platform");
	  System.out.print("Trying to reconnect... ");
	  try {
	    restoreMainContainer();
	    result = myPlatform.getProxy(id); // RMI call
	    System.out.println("OK.");
	  }
	  catch(RemoteException rex) {
	    throw new NotFoundException("The Main Container is unreachable (again).");
	  }
	}

      }
    }
    else { // It lives outside: then it's a job for the ACC...
      result = theACC.getProxy(id);
    }

    return result;

  }

  private void restoreMainContainer() throws NotFoundException {
    try {
      myPlatform = lookup3("rmi://" + platformID);

      // Register again with the Main Container.
      InetAddress netAddr = InetAddress.getLocalHost();
      myName = myPlatform.addContainer(this, netAddr); // RMI call

      ACLMessage regMsg = new ACLMessage(ACLMessage.REQUEST);
      regMsg.setSender(Agent.getAMS());
      regMsg.addReceiver(Agent.getAMS());
      regMsg.setLanguage(jade.lang.sl.SL0Codec.NAME);
      regMsg.setOntology(jade.domain.FIPAAgentManagement.FIPAAgentManagementOntology.NAME);
      regMsg.setProtocol("fipa-request");

      // Restore Main Container state of agents and containers
      AID[] agentIDs = localAgents.keys();
      for(int i = 0; i < agentIDs.length; i++) {

	AID agentID = agentIDs[i];

	// Register again the agent with the Main Container.
	RemoteProxyRMI rp = new RemoteProxyRMI(this, agentID);
	try {
	  myPlatform.bornAgent(agentID, rp, myName); // RMI call
	}
	catch(NameClashException nce) {
	  throw new NotFoundException("Agent name already in use: "+ nce.getMessage());
	}

	String content = "((action (agent-identifier :name " + Agent.getAMS().getName() + " ) (register (ams-agent-description :name (agent-identifier :name " + agentID.getName() + " ) :ownership JADE :state active ) ) ))";
	// Register again the agent with the AMS
	regMsg.setContent(content);
	unicastPostMessage(regMsg, Agent.getAMS());

      }

      // Register again all MTPs with the Main Container
      List localAddresses = theACC.getLocalAddresses();
      for(int i = 0; i < localAddresses.size(); i++) {
	myPlatform.newMTP((String)localAddresses.get(i), myName);
      }

    }
    catch(RemoteException re) {
      System.out.println("The Main Container is down again. Aborting this send operation.");
      throw new NotFoundException("The Main Container is unreachable.");
    }
    catch(NotBoundException nbe) {
      nbe.printStackTrace();
      throw new NotFoundException("The Main Container is not bound with the RMI registry.");
    }
    catch(MalformedURLException murle) {
      murle.printStackTrace();
    }
    catch(java.net.UnknownHostException jnue) {
      jnue.printStackTrace();
    }
  }

}
