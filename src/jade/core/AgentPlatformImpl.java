/*
  $Log$
  Revision 1.29  1999/03/09 13:30:14  rimassa
  Used String constants for system agent names.
  Completely redesigned AgentPlatform startup: now system agents (AMS,
  ACC and default DF) run in a separate ThreadGroup.
  Added a custom joinPlatform() method to perform specific
  initialization.
  Now removeContainer() takes a String argument and no more an
  AgentContainer.

  Revision 1.28  1999/03/07 22:51:43  rimassa
  Added a debugging printout.

  Revision 1.27  1999/03/03 16:07:45  rimassa
  Added a getContainerFromAgent() method.
  Added methods to dispatch a suspend()/resume() request to the
  appropriate agent container.
  Improved method naming, distinguishing between AMS and AP.

  Revision 1.26  1999/02/25 08:26:12  rimassa
  Removed useless FIXME.
  Clarified various error messages appearing when an agent was not
  found.
  Completely redesigned platform shutdown procedure. Now it works like
  this:
    - Remove front end container from container list.
    - Shutdown all other container.
    - Kill all non system agents on front end container.
    - Kill default DF.
    - Kill ACC.
    - Kill AMS.
    - Disconnect platform IIOP server.

  Fixed a nasty deadlock problem: now AMSKillContainer spawns a separate
  thread calling RMI method AgentContainer.exit(), so the AMS is free to
  answer to 'deregister' messages from about-to-die agents.

  Revision 1.25  1999/02/15 11:43:21  rimassa
  Fixed a bug: a case sensitive comparison was incorrectly made on agent
  name during deregistration with AMS agent.

  Revision 1.24  1999/02/14 23:13:34  rimassa
  Removed an useless printout. Changed IOR file name from 'CSELT.IOR' to
  'JADE.IOR'.

  Revision 1.23  1999/02/04 12:57:07  rimassa
  Added a 'FIXME:' reminder.

  Revision 1.22  1999/02/03 10:13:58  rimassa
  Added server side CORBA support: now the AgentPlatform contains a
  CORBA object implementation for FIPA_Agent_97 IDL interface.
  During platform startup, the IOR or the URL for that CORBA object
  implementation is stored as the IIOP address of the whole agent
  platform.
  Modified addContainer() method to comply with new AgentPLatform
  interface.

  Revision 1.21  1998/11/15 23:03:33  rimassa
  Removed old printed messages about system agents, since now the Remote
  Agent Management GUI shows all agents present on the platform.
  Added a new AMSKillContainer method to be used from AMS agent to
  terminate Agent Containers.

  Revision 1.20  1998/11/09 22:15:58  Giovanni
  Added an overridden version of AgentContainerImpl shutDown() method:
  an AgentPlatform firstly shuts itself down as an ordinary
  AgentContainer (i.e. it removes itself from container list), then
  calls exit() remote method for every other AgentContainer in the
  platform, thereby completely terminating the Agent Platform.

  Revision 1.19  1998/11/09 00:13:46  rimassa
  Container list now is an Hashtable instead of a Vector, indexed by a
  String, which is used also as container name in RMA GUI; various
  changes throughout the code to support the new data structure.
  Added some public methods for the AMS to use, such as a method to
  obtain an Enumeration of container names or agent names.

  Revision 1.18  1998/11/03 00:30:25  rimassa
  Added AMS notifications for new agents and dead agents.

  Revision 1.17  1998/11/02 01:58:23  rimassa
  Removed every reference to deleted MessageDispatcher class; now
  AgentContainer is directly responsible for message dispatching.
  Added AMS notifications when an AgentContainer is created or
  deleted.

  Revision 1.16  1998/10/31 16:33:36  rimassa
  Changed AMSKillAgent() prototype, since now it accept also a password
  String (ignored for now).
  Fixed a tiny bug in AMSKillAgent(): 'agentName' was to be
  'simpleName'.

  Revision 1.15  1998/10/26 00:00:30  rimassa
  Added some methods for AMS to use in platform administration. When the
  AMS wants to create or kill an agent it relies on methods such as
  AMSCreateAgent() and AMSKillAgent() to actually do the job.

  Revision 1.14  1998/10/14 21:24:11  Giovanni
  Added a line to restore platform state when a new agent has a name
  clashing with a previous agent's name.

  Revision 1.13  1998/10/11 19:32:30  rimassa
  In method bornAgent() a sensible strategy has been implemented to
  recover from agent name collisions. When a new agent has a name
  already present in Global Agent Table, a name clash exception is
  raised, unless the old agent's container crashed, in which case the
  newer agent simply replaces the older one.
  Now lookup() method is able to distinguish between an unknown agent
  and an agent whose container has crashed, writing a suitable error
  message in the NotFoundException it raises.
  Fixed a missing toLowerCase().

  Revision 1.12  1998/10/07 22:16:21  Giovanni
  Changed code in various places to make agent descriptor tables
  case-insensitive. Now upper or lower case in agent names and addresses
  make no more difference; this is to comply with FIPA specification.

  Revision 1.11  1998/10/04 18:01:01  rimassa
  Added a 'Log:' field to every source file.

*/

package jade.core;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;

import jade.domain.ams;
import jade.domain.acc;
import jade.domain.df;
import jade.domain.AgentManagementOntology;

import jade.domain.FIPAException;
import jade.domain.NoCommunicationMeansException;
import jade.domain.AgentAlreadyRegisteredException;

import jade.lang.acl.ACLMessage;

import _FIPA_Agent_97ImplBase;

public class AgentPlatformImpl extends AgentContainerImpl implements AgentPlatform {

  private static final String AMS_NAME = "ams";
  private static final String ACC_NAME = "acc";
  private static final String DEFAULT_DF_NAME = "df";

  // Initial size of containers hash table
  private static final int CONTAINERS_SIZE = 10;

  // Initial size of agent hash table
  private static final int GLOBALMAP_SIZE = 100;

  // Load factor of agent hash table
  private static final float GLOBALMAP_LOAD_FACTOR = 0.25f;
  private ThreadGroup systemAgentsThreads = new ThreadGroup("JADE System Agents");


  private ams theAMS;
  private df defaultDF;

  // For now ACC agent and ACC CORBA server are different objects and run
  // within different threads of control.
  private acc theACC;
  private InComingIIOP frontEndACC;

  private Hashtable containers = new Hashtable(CONTAINERS_SIZE);
  private Hashtable platformAgents = new Hashtable(GLOBALMAP_SIZE, GLOBALMAP_LOAD_FACTOR);

  private class InComingIIOP extends _FIPA_Agent_97ImplBase {
    public void message(String acl_message) {

      // Recover ACL message object from String
      ACLMessage msg = ACLMessage.fromText(new StringReader(acl_message));

      // Create and handle a suitable communication event
      CommEvent ev = new CommEvent(theACC, msg);
      CommHandle(ev);
    }
  }

  public AgentPlatformImpl(String args[]) throws RemoteException {
    super(args);
    myName = MAIN_CONTAINER_NAME;
    systemAgentsThreads.setMaxPriority(Thread.MAX_PRIORITY);
    initAMS();
    initACC();
    initDF();
  }

  private void initAMS() {

    theAMS = new ams(this);

    // Subscribe as a listener for the AMS agent
    theAMS.addCommListener(this);

    // Insert AMS into local agents table
    localAgents.put(AMS_NAME, theAMS);

    AgentDescriptor desc = new AgentDescriptor();
    desc.setContainer(this, myName);

    platformAgents.put(AMS_NAME, desc);

  }

  private void initACC() {
    theACC = new acc(this);

    // Subscribe as a listener for the AMS agent
    theACC.addCommListener(this);

    // Insert AMS into local agents table
    localAgents.put(ACC_NAME, theACC);

    AgentDescriptor desc = new AgentDescriptor();
    desc.setContainer(this, myName);

    platformAgents.put(ACC_NAME, desc);

    // Setup CORBA server
    frontEndACC = new InComingIIOP();
    myORB.connect(frontEndACC);

    // Generate and store IIOP URL for the platform
    try {
      OutGoingIIOP dummyChannel = new OutGoingIIOP(myORB, frontEndACC);
      platformAddress = dummyChannel.getIOR();
      System.out.println(platformAddress);

      try {
      	FileWriter f = new FileWriter("JADE.IOR");
      	f.write(platformAddress,0,platformAddress.length());
      	f.close();
      }
      catch (IOException io) {
      	io.printStackTrace();
      }
    }
    catch(IIOPFormatException iiopfe) {
      System.err.println("FATAL ERROR: Could not create IIOP server for the platform");
      iiopfe.printStackTrace();
      System.exit(0);
    }

  }

  private void initDF() {

    defaultDF = new df();

    // Subscribe as a listener for the AMS agent
    defaultDF.addCommListener(this);

    // Insert DF into local agents table
    localAgents.put(DEFAULT_DF_NAME, defaultDF);

    AgentDescriptor desc = new AgentDescriptor();
    desc.setContainer(this, myName);

    platformAgents.put(DEFAULT_DF_NAME, desc);

  }

  public void joinPlatform(String platformRMI, Vector agentNamesAndClasses) {
    try {
      myPlatform = (AgentPlatform)Naming.lookup(platformRMI);
    }
    catch(Exception e) {
      // Should never happen
      e.printStackTrace();
    }

    containers.put(MAIN_CONTAINER_NAME, this);

    // Notify AMS
    theAMS.postNewContainer(MAIN_CONTAINER_NAME);

    theAMS.doStart(AMS_NAME, platformAddress, systemAgentsThreads);
    theACC.doStart(ACC_NAME, platformAddress, systemAgentsThreads);
    defaultDF.doStart(DEFAULT_DF_NAME, platformAddress, systemAgentsThreads);

    for(int i = 0; i < agentNamesAndClasses.size(); i += 2) {
      String agentName = (String)agentNamesAndClasses.elementAt(i);
      String agentClass = (String)agentNamesAndClasses.elementAt(i+1);
      try {
	createAgent(agentName, agentClass, START);
      }
      catch(RemoteException re) {
	// It should never happen ...
	re.printStackTrace();
      }

    }

  }

  AgentContainer getContainerFromAgent(String agentName) throws NotFoundException {
    AgentDescriptor ad = (AgentDescriptor)platformAgents.get(agentName.toLowerCase());
    if(ad == null)
      throw new NotFoundException("Agent " + agentName + " not found in getContainerFromAgent()");
    AgentContainer ac = ad.getContainer();
    return ac;
  }

  public String getAddress() throws RemoteException {
    return platformAddress;
  }

  public String addContainer(AgentContainer ac) throws RemoteException {

    String name = AUX_CONTAINER_NAME + new Integer(containers.size()).toString();
    containers.put(name, ac);

    // Notify AMS
    theAMS.postNewContainer(name);

    // Return the name given to the new container
    return name;

  }

  public void removeContainer(String name) throws RemoteException {
    containers.remove(name);

    // Notify AMS
    theAMS.postDeadContainer(name);
  }

  public void bornAgent(String name, AgentDescriptor desc) throws RemoteException, NameClashException {
    java.lang.Object old = platformAgents.put(name.toLowerCase(), desc);

    // If there's already an agent with name 'name' throw a name clash
    // exception unless the old agent's container is dead.
    if(old != null) {
      AgentDescriptor ad = (AgentDescriptor)old;
      AgentContainer ac = ad.getContainer();
      try {
	ac.ping(); // Make sure container is alive, then raise a name clash exception
	platformAgents.put(name.toLowerCase(), ad);
	throw new NameClashException("Agent " + name + " already present in the platform ");
      }
      catch(RemoteException re) {
	System.out.println("Replacing a dead agent ...");
      }
    }

  }

  public void deadAgent(String name) throws RemoteException, NotFoundException {
    AgentDescriptor ad = (AgentDescriptor)platformAgents.get(name.toLowerCase());
    if(ad == null)
      throw new NotFoundException("DeadAgent failed to find " + name);
    String containerName = ad.getContainerName();
    AgentManagementOntology.AMSAgentDescriptor amsd = ad.getDesc();
    platformAgents.remove(name.toLowerCase());

    // Notify AMS
    theAMS.postDeadAgent(containerName, amsd);
  }

  public AgentDescriptor lookup(String agentName) throws RemoteException, NotFoundException {
    AgentDescriptor ad = (AgentDescriptor)platformAgents.get(agentName.toLowerCase());
    if(ad == null)
      throw new NotFoundException("Lookup failed to find " + agentName);
    else {
      AgentContainer ac = ad.getContainer();
      try {
	ac.ping(); // RMI call
      }
      catch(RemoteException re) {
	throw new NotFoundException("Container for " + agentName + " is unreachable");
      }
      return ad;
    }
  }

  // This method overrides AgentContainerImpl.shutDown(); besides
  // behaving like the normal AgentContainer version, it makes all
  // other agent containers exit.
  public void shutDown() {

    // Deregister yourself as a container
    containers.remove(MAIN_CONTAINER_NAME);

    // Kill every other container
    Enumeration e = containers.keys();
    while(e.hasMoreElements()) {
      String containerName = (String)e.nextElement();
      AgentContainer ac = (AgentContainer)containers.get(containerName);
      APKillContainer(ac);
    }

    // Kill all non-system agents
    Enumeration agentNames = localAgents.keys();

    while(agentNames.hasMoreElements()) {
      String name = (String)agentNames.nextElement();
      if(name.equalsIgnoreCase(theAMS.getLocalName()) || 
	 name.equalsIgnoreCase(theACC.getLocalName()) ||
	 name.equalsIgnoreCase(defaultDF.getLocalName()))
	  continue;

      // Kill agent and wait for its termination
      Agent a = (Agent)localAgents.get(name);
      a.doDelete();
      a.join();
    }

    // Kill system agents, at last

    Agent systemAgent = defaultDF;
    systemAgent.doDelete();
    systemAgent.join();

    systemAgent = theACC;
    systemAgent.doDelete();
    systemAgent.join();

    theAMS.removeCommListener(this);
    systemAgent = theAMS;
    systemAgent.doDelete();
    systemAgent.join();

    // Now, close CORBA link to outside world
    myORB.disconnect(frontEndACC);

  }

  // These methods dispatch agent management operations to
  // appropriate Agent Container through RMI.

  public void APKillAgent(String name) throws NoCommunicationMeansException {
    try {
      AgentContainer ac = getContainerFromAgent(name);
      ac.killAgent(name); // RMI call
    }
    catch(NotFoundException nfe) {
      throw new NoCommunicationMeansException();
    }
    catch(RemoteException re) {
      throw new NoCommunicationMeansException();
    }
  }

  public void APKillContainer(AgentContainer ac) {
    try {
      ac.exit(); // RMI call
    }
    catch(UnmarshalException ue) {
      // FIXME: This is ignored, since we'd need oneway calls to
      // perform exit() remotely
    }
    catch(RemoteException re) {
      re.printStackTrace();
    }
  }

  public void APSuspendAgent(String name) throws NoCommunicationMeansException {
    try {
      AgentContainer ac = getContainerFromAgent(name);
      ac.suspendAgent(name); // RMI call
    }
    catch(NotFoundException nfe) {
      throw new NoCommunicationMeansException();
    }
    catch(RemoteException re) {
      throw new NoCommunicationMeansException();
    }
  }

  public void APActivateAgent(String name) throws NoCommunicationMeansException {
    try {
      AgentContainer ac = getContainerFromAgent(name);
      ac.resumeAgent(name); // RMI call
    }
    catch(NotFoundException nfe) {
      throw new NoCommunicationMeansException();
    }
    catch(RemoteException re) {
      throw new NoCommunicationMeansException();
    }
  }

  public void APWaitAgent(String name) throws NoCommunicationMeansException {
    try {
      AgentContainer ac = getContainerFromAgent(name);
      ac.waitAgent(name); // RMI call
    }
    catch(NotFoundException nfe) {
      throw new NoCommunicationMeansException();
    }
    catch(RemoteException re) {
      throw new NoCommunicationMeansException();
    }
  }

  public void APWakeAgent(String name) throws NoCommunicationMeansException {
    try {
      AgentContainer ac = getContainerFromAgent(name);
      ac.wakeAgent(name); // RMI call
    }
    catch(NotFoundException nfe) {
      throw new NoCommunicationMeansException();
    }
    catch(RemoteException re) {
      throw new NoCommunicationMeansException();
    }
  }

  // These methods are to be used only by AMS agent.


  // This is used by AMS to obtain the list of all the Agent Containers of the platform.
  public Enumeration AMSContainerNames() {
    return containers.keys();
  }

  // This is used by AMS to obtain the list of all the agents of the platform.
  public Enumeration AMSAgentNames() {
    return platformAgents.keys();
  }

  // This maps the name of an agent to the name of the Agent Container the agent lives in.
  public String AMSGetContainerName(String agentName) throws FIPAException {
    AgentDescriptor ad = (AgentDescriptor)platformAgents.get(agentName.toLowerCase());
    if(ad == null)
      throw new FIPAException("Agent " + agentName + " not found in AMSGetContainerName()");
    return ad.getContainerName();
  }

  // This maps the name of an agent to its IIOP address.
  public String AMSGetAddress(String agentName) {
    // FIXME: Should not even exist; it would be better to put the
    // complete agent name in the hash table
    return platformAddress; 
  }

  // This is called in response to a 'create-agent' action
  public void AMSCreateAgent(String agentName, String className, String containerName) throws NoCommunicationMeansException {
    String simpleName = agentName.substring(0,agentName.indexOf('@'));
    try {
      AgentContainer ac;
      // If no name is given, the agent is started on the AgentPlatform itself
      if(containerName == null)
	ac = this; 
      else
	ac = (AgentContainer)containers.get(containerName);

      // If a wrong name is given, then again the agent starts on the AgentPlatform itself
      if(ac == null)
	ac = this;
      ac.createAgent(simpleName, className, START); // RMI call
    }
    catch(RemoteException re) {
      throw new NoCommunicationMeansException();
    }
  }

  public void AMSCreateAgent(String agentName, Agent instance, String containerName) throws NoCommunicationMeansException {
    String simpleName = agentName.substring(0,agentName.indexOf('@'));
    try {
      AgentContainer ac = (AgentContainer)containers.get(containerName);
      ac.createAgent(simpleName, instance, START); // RMI call, 'instance' is serialized
    }
    catch(ArrayIndexOutOfBoundsException aioobe) {
      throw new NoCommunicationMeansException();
    }
    catch(RemoteException re) {
      throw new NoCommunicationMeansException();
    }
  }

  public void AMSKillContainer(String containerName) {

    // This call spawns a separate thread in order to avoid deadlock.
      //    System.out.println("About to kill " + containerName);
    final AgentContainer ac = (AgentContainer)containers.get(containerName);
    Thread auxThread = new Thread(new Runnable() {
      public void run() {
	APKillContainer(ac);
      }
    });
    auxThread.start();
  }

  // This one is called in response to a 'kill-agent' action
  public void AMSKillAgent(String agentName, String password) throws NoCommunicationMeansException {
    String simpleName = agentName.substring(0,agentName.indexOf('@'));
    APKillAgent(simpleName);
  }

  // This one is called in response to a 'register-agent' action
  public void AMSNewData(String agentName, String address, String signature, String APState,
			 String delegateAgentName, String forwardAddress, String ownership)
    throws FIPAException, AgentAlreadyRegisteredException {

    try {
      // Extract the agent name from the beginning to the '@'
      // FIXME: Platform crash on missing '@'
      String simpleName = agentName.substring(0,agentName.indexOf('@'));
      AgentDescriptor ad = (AgentDescriptor)platformAgents.get(simpleName.toLowerCase());
      if(ad == null)
	throw new NotFoundException("AMSNewData failed to find " + agentName);

      if(ad.getDesc() != null) {
	throw new AgentAlreadyRegisteredException();
      }

      AgentManagementOntology o = AgentManagementOntology.instance();
      int state = o.getAPStateByName(APState);

      AgentManagementOntology.AMSAgentDescriptor amsd = new AgentManagementOntology.AMSAgentDescriptor();

      amsd.setName(agentName); // FIXME: When name changes Global Descriptor Table should be updated
      amsd.setAddress(address);
      amsd.setSignature(signature);
      amsd.setAPState(state);
      amsd.setDelegateAgentName(delegateAgentName);
      amsd.setForwardAddress(forwardAddress);
      amsd.setOwnership(ownership);

      ad.setDesc(amsd);

      // Notify AMS
      String containerName = ad.getContainerName();
      theAMS.postNewAgent(containerName, amsd);
    }
    catch(NotFoundException nfe) {
      nfe.printStackTrace();
    }

  }

  // This one is called in response to a 'modify-agent' action
  public void AMSChangeData(String agentName, String address, String signature, String APState,
			    String delegateAgentName, String forwardAddress, String ownership)
    throws FIPAException {

    try {
      // Extract the agent name from the beginning to the '@'
      String simpleName = agentName.substring(0,agentName.indexOf('@'));
      AgentDescriptor ad = (AgentDescriptor)platformAgents.get(simpleName.toLowerCase());
      if(ad == null)
	throw new NotFoundException("AMSChangeData ailed to find " + agentName);

      AgentManagementOntology.AMSAgentDescriptor amsd = ad.getDesc();
      
      if(address != null)
	amsd.setAddress(address);
      if(signature != null)
	amsd.setSignature(signature);
      if(delegateAgentName != null)
	amsd.setDelegateAgentName(delegateAgentName);
      if(forwardAddress != null)
	amsd.setAddress(forwardAddress);
      if(ownership != null)
	amsd.setOwnership(ownership);
      if(APState != null) {
	AgentManagementOntology o = AgentManagementOntology.instance();
	int state = o.getAPStateByName(APState);
	int oldState = o.getAPStateByName(amsd.getAPState());
	switch(state) {
	case Agent.AP_SUSPENDED:
	  APSuspendAgent(simpleName);
	  break;
	case Agent.AP_WAITING:
	  APWaitAgent(simpleName);
	  break;
	case Agent.AP_ACTIVE:
	  if(oldState == Agent.AP_WAITING)
	    APWakeAgent(simpleName);
	  else
	    APActivateAgent(simpleName);
	}

	amsd.setAPState(state);

      }
    }
    catch(NotFoundException nfe) {
      nfe.printStackTrace();
    }

  }


  // This one is called in response to a 'deregister-agent' action
  public void AMSRemoveData(String agentName, String address, String signature, String APState,
			    String delegateAgentName, String forwardAddress, String ownership)
    throws FIPAException {
    // Extract the agent name from the beginning to the '@'
    agentName = agentName.substring(0,agentName.indexOf('@'));
    AgentDescriptor ad = (AgentDescriptor)platformAgents.get(agentName.toLowerCase());
    if(ad == null) {
      throw new jade.domain.UnableToDeregisterException();
    }
    AgentManagementOntology.AMSAgentDescriptor amsd = ad.getDesc();
    amsd.setAPState(Agent.AP_DELETED);
  }

  public void AMSDumpData() {
    Enumeration descriptors = platformAgents.elements();
    while(descriptors.hasMoreElements()) {
      AgentDescriptor desc = (AgentDescriptor)descriptors.nextElement();
      AgentManagementOntology.AMSAgentDescriptor amsd = desc.getDesc();
      amsd.toText(new BufferedWriter(new OutputStreamWriter(System.out)));
    }
  }

  public void AMSDumpData(String agentName) {
    // Extract the agent name from the beginning to the '@'
    agentName = agentName.substring(0,agentName.indexOf('@'));
    AgentDescriptor desc = (AgentDescriptor)platformAgents.get(agentName.toLowerCase());
    AgentManagementOntology.AMSAgentDescriptor amsd = desc.getDesc();
    amsd.toText(new BufferedWriter(new OutputStreamWriter(System.out)));
  }

}

