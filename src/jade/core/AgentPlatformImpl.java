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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import java.util.Vector;    // FIXME: This will go away

import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;

import jade.domain.ams;
import jade.domain.acc;
import jade.domain.df;
import jade.domain.AgentManagementOntology;

import jade.lang.acl.ACLMessage;

import _FIPA_Agent_97ImplBase;

class AgentPlatformImpl extends AgentContainerImpl implements AgentPlatform, AgentManager {

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

  private Map containers = Collections.synchronizedMap(new HashMap(CONTAINERS_SIZE));
  private Map platformAgents = Collections.synchronizedMap(new HashMap(GLOBALMAP_SIZE, GLOBALMAP_LOAD_FACTOR));

  private class InComingIIOP extends _FIPA_Agent_97ImplBase {
    public void message(String acl_message) {
      System.out.println("\n\n"+(new java.util.Date()).toString()+" INCOMING IIOP MESSAGE: "+acl_message);
      try {
      // Recover ACL message object from String
      ACLMessage msg = ACLMessage.fromText(new StringReader(acl_message));
      // Create and handle a suitable communication event
      CommEvent ev = new CommEvent(theACC, msg);
      CommHandle(ev);
      }
      catch (jade.lang.acl.ParseException e) {
	e.printStackTrace();
      }
    }
  }

  public AgentPlatformImpl(String args[]) throws RemoteException {
    super(args);
    myName = AgentManagementOntology.PlatformProfile.MAIN_CONTAINER_NAME;
    systemAgentsThreads.setMaxPriority(Thread.NORM_PRIORITY + 1);
    initIIOP();
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
    RemoteProxyRMI rp = new RemoteProxyRMI(this);
    desc.setContainerName(myName);
    desc.setProxy(rp);

    String amsName = AMS_NAME + '@' + platformAddress;
    platformAgents.put(amsName.toLowerCase(), desc);

  }

  private void initACC() {
    theACC = new acc();

    // Subscribe as a listener for the AMS agent
    theACC.addCommListener(this);

    // Insert AMS into local agents table
    localAgents.put(ACC_NAME, theACC);

    AgentDescriptor desc = new AgentDescriptor();
    RemoteProxyRMI rp = new RemoteProxyRMI(this);
    desc.setContainerName(myName);
    desc.setProxy(rp);

    String accName = ACC_NAME + '@' + platformAddress;
    platformAgents.put(accName.toLowerCase(), desc);

  }

  private void initIIOP() {

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
      	f = new FileWriter("JADE.URL");
	String iiopAddress = dummyChannel.getURL();
      	f.write(iiopAddress,0,iiopAddress.length());
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
    RemoteProxyRMI rp = new RemoteProxyRMI(this);
    desc.setContainerName(myName);
    desc.setProxy(rp);

    String defaultDfName = DEFAULT_DF_NAME + '@' + platformAddress;
    platformAgents.put(defaultDfName.toLowerCase(), desc);

  }

  public void joinPlatform(String platformRMI, Vector agentNamesAndClasses) {
    try {
      myPlatform = (AgentPlatform)Naming.lookup(platformRMI);
    }
    catch(Exception e) {
      // Should never happen
      e.printStackTrace();
    }

    containers.put(AgentManagementOntology.PlatformProfile.MAIN_CONTAINER_NAME, this);

    // Notify AMS
    theAMS.postNewContainer(AgentManagementOntology.PlatformProfile.MAIN_CONTAINER_NAME);

    Agent a = theAMS;
    a.powerUp(AMS_NAME, platformAddress, systemAgentsThreads);
    a = theACC;
    a.powerUp(ACC_NAME, platformAddress, systemAgentsThreads);
    a = defaultDF;
    a.powerUp(DEFAULT_DF_NAME, platformAddress, systemAgentsThreads);

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

    System.out.println("Agent Platform ready to accept new containers...");


  }

  AgentContainer getContainerFromAgent(String agentName) throws NotFoundException {
    AgentDescriptor ad = (AgentDescriptor)platformAgents.get(agentName.toLowerCase());
    if(ad == null) {
      throw new NotFoundException("Agent " + agentName + " not found in getContainerFromAgent()");
    }
    ad.lock();
    String name = ad.getContainerName();
    AgentContainer ac = (AgentContainer)containers.get(name);
    ad.unlock();
    return ac;
  }

  public String getAddress() throws RemoteException {
    return platformAddress;
  }

  // Inner class to detect agent container failures
  private class FailureMonitor implements Runnable {

    private AgentContainer target;
    private String targetName;
    private boolean active = true;

    public FailureMonitor(AgentContainer ac, String name) {
      target = ac;
      targetName = name;
    }

    public void run() {
      while(active) {
	try {
	  target.ping(true); // Hang on this RMI call
	}
	catch(RemoteException re1) { // Connection down
	  try {
	    target.ping(false); // Try a non blocking ping to check
	  }
	  catch(RemoteException re2) { // Object down

	    containers.remove(targetName);
	    theAMS.postDeadContainer(targetName);

	    active = false;
	  }
	}
	catch(Throwable t) {
	  t.printStackTrace();
	}
      }
    }
  }

  public String addContainer(AgentContainer ac) throws RemoteException {

    String name = AgentManagementOntology.PlatformProfile.AUX_CONTAINER_NAME + new Integer(containers.size()).toString();
    containers.put(name, ac);

    // Spawn a blocking RMI call to the remote container in a separate
    // thread. This is a failure notification technique.
    Thread t = new Thread(new FailureMonitor(ac, name));
    t.start();

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

  public AgentContainer lookup(String name) throws RemoteException, NotFoundException {
    AgentContainer ac = (AgentContainer)containers.get(name);
    if(ac == null)
      throw new NotFoundException("Name Lookup failed: no such container");
    return ac;
  }

  public void bornAgent(String name, RemoteProxy rp, String containerName) throws RemoteException, NameClashException {
    AgentDescriptor desc = new AgentDescriptor();
    desc.setProxy(rp);
    desc.setContainerName(containerName);
    java.lang.Object old = platformAgents.put(name.toLowerCase(), desc);

    // If there's already an agent with name 'name' throw a name clash
    // exception unless the old agent's container is dead.
    if(old != null) {
      AgentDescriptor ad = (AgentDescriptor)old;
      RemoteProxy oldProxy = ad.getProxy();
      try {
	oldProxy.ping(); // Make sure agent is reachable, then raise a name clash exception
	platformAgents.put(name.toLowerCase(), ad);
	throw new NameClashException("Agent " + name + " already present in the platform ");
      }
      catch(UnreachableException ue) {
	System.out.println("Replacing a dead agent ...");
	theAMS.postDeadAgent(ad.getContainerName(), name);
      }
    }

    // Notify AMS
    theAMS.postNewAgent(containerName, name);

  }

  public void deadAgent(String name) throws RemoteException, NotFoundException {
    AgentDescriptor ad = (AgentDescriptor)platformAgents.get(name.toLowerCase());
    if(ad == null)
      throw new NotFoundException("DeadAgent failed to find " + name);
    String containerName = ad.getContainerName();
    platformAgents.remove(name.toLowerCase());

    // Notify AMS
    theAMS.postDeadAgent(containerName, name);
  }

  public RemoteProxy getProxy(String agentName, String agentAddress) throws RemoteException, NotFoundException {

    RemoteProxy rp;
    AgentDescriptor ad = (AgentDescriptor)platformAgents.get(agentName + '@' + agentAddress);

    if(ad == null)
      throw new NotFoundException("getProxy() failed to find " + agentName);
    else {
      ad.lock();
      rp = ad.getProxy();
      ad.unlock();
      try {
	rp.ping();
      }
      catch(UnreachableException ue) {
	throw new NotFoundException("Container for " + agentName + " is unreachable");
      }
      return rp;
    }
  }

  public boolean transferIdentity(String agentName, String src, String dest) throws RemoteException, NotFoundException {
    AgentDescriptor ad = (AgentDescriptor)platformAgents.get(agentName.toLowerCase());
    if(ad == null)
      throw new NotFoundException("transferIdentity() unable to find agent " + agentName);
    AgentContainer srcAC = lookup(src);
    AgentContainer destAC = lookup(dest);
    try {
      srcAC.ping(false);
      destAC.ping(false);
    }
    catch(RemoteException re) {
      // Abort transaction
      return false;
    }

    // Commit transaction and notify AMS
    ad.lock();
    ad.setProxy(new RemoteProxyRMI(destAC));
    theAMS.postMovedAgent(agentName, src, dest);
    ad.unlock();
    return true;
  }

  // This method overrides AgentContainerImpl.shutDown(); besides
  // behaving like the normal AgentContainer version, it makes all
  // other agent containers exit.
  public void shutDown() {

    System.out.println("Step 1");

    // Deregister yourself as a container
    containers.remove(AgentManagementOntology.PlatformProfile.MAIN_CONTAINER_NAME);

    System.out.println("Step 2");

    // Kill every other container
    Collection c = containers.values();
    Object[] allContainers = c.toArray();
    for(int i = 0; i < allContainers.length; i++) {
      AgentContainer ac = (AgentContainer)allContainers[i];
      try {
	APKillContainer(ac); // This call removes 'ac' from 'container' map and from the collection 'c'
      }
      catch(RemoteException re) {
	System.out.println("Container is unreachable. Ignoring...");
      } 
    }

    System.out.println("Step 3");

    // Kill all non-system agents
    Set s = localAgents.keySet();
    Object[] allLocalAgents = s.toArray(); 
    for(int i = 0; i < allLocalAgents.length; i++) {
      String name = (String)allLocalAgents[i];
      if(name.equalsIgnoreCase(theAMS.getLocalName()) || 
				 name.equalsIgnoreCase(theACC.getLocalName()) ||
				 name.equalsIgnoreCase(defaultDF.getLocalName()))
					continue;

      // Kill agent and wait for its termination
      Agent a = (Agent)localAgents.get(name);
      a.doDelete();
      a.join();
    }


    System.out.println("Step 4");

    // Kill system agents, at last

    Agent systemAgent = defaultDF;
    systemAgent.doDelete();
    systemAgent.join();

    System.out.println("Step 5");

    systemAgent = theACC;
    systemAgent.doDelete();
    systemAgent.join();

    System.out.println("Step 6");

    theAMS.removeCommListener(this);
    systemAgent = theAMS;
    systemAgent.doDelete();
    systemAgent.join();

    System.out.println("Step 7");

    // Now, close CORBA link to outside world
    myORB.disconnect(frontEndACC);

  }

  // These methods dispatch agent management operations to
  // appropriate Agent Container through RMI.

  public void kill(String agentName, String password) throws NotFoundException, UnreachableException {
    try {
      AgentContainer ac = getContainerFromAgent(agentName);
      String simpleName = agentName.substring(0,agentName.indexOf('@'));
      ac.killAgent(simpleName); // RMI call
    }
    catch(RemoteException re) {
      throw new UnreachableException(re.getMessage());
    }
  }

  public void APKillContainer(AgentContainer ac) throws RemoteException {
    try {
      ac.exit(); // RMI call
    }
    catch(UnmarshalException ue) {
      // FIXME: This is ignored, since we'd need oneway calls to
      // perform exit() remotely
    }
  }

  public void suspend(String agentName, String password) throws NotFoundException, UnreachableException {
    try {
      AgentContainer ac = getContainerFromAgent(agentName);
      String simpleName = agentName.substring(0,agentName.indexOf('@'));
      ac.suspendAgent(simpleName); // RMI call
    }
    catch(RemoteException re) {
      throw new UnreachableException(re.getMessage());
    }
  }

  public void activate(String agentName, String password) throws NotFoundException, UnreachableException {
    try {
      AgentContainer ac = getContainerFromAgent(agentName);
      String simpleName = agentName.substring(0,agentName.indexOf('@'));
      ac.resumeAgent(simpleName); // RMI call
    }
    catch(RemoteException re) {
      throw new UnreachableException(re.getMessage());
    }
  }

  public void wait(String agentName, String password) throws NotFoundException, UnreachableException {
    try {
      AgentContainer ac = getContainerFromAgent(agentName);
      String simpleName = agentName.substring(0,agentName.indexOf('@'));
      ac.waitAgent(simpleName); // RMI call
    }
    catch(RemoteException re) {
      throw new UnreachableException(re.getMessage());
    }
  }

  public void wake(String agentName, String password) throws NotFoundException, UnreachableException {
    try {
      AgentContainer ac = getContainerFromAgent(agentName);
      String simpleName = agentName.substring(0,agentName.indexOf('@'));
      ac.wakeAgent(simpleName); // RMI call
    }
    catch(RemoteException re) {
      throw new UnreachableException(re.getMessage());
    }
  }

  public void move(String agentName, String containerName, String password ) throws NotFoundException, UnreachableException {
    // FIXME: Not implemented
    // Lookup the container for 'agentName', throwing NotFoundException on failure
    // Tell the src container to send the agent code and data to the dest container
    // Update GADT to reflect new agent location
  }

  public void copy(String agentName, String containerName, String newAgentName, String password) throws NotFoundException, UnreachableException {
    // Retrieve the container for the original agent
    AgentContainer src = getContainerFromAgent(agentName);
    try {
      int atPos = agentName.indexOf('@');
      if(atPos != -1)
	agentName = agentName.substring(0,atPos);

      src.copyAgent(agentName, containerName, newAgentName); // RMI call
    }
    catch(RemoteException re) {
      throw new UnreachableException(re.getMessage());
    }
  }

  // These methods are to be used only by AMS agent.


  // This is used by AMS to obtain the set of all the Agent Containers of the platform.
  public String[] containerNames() {
    Object[] objs = containers.keySet().toArray();
    String[] names = new String[objs.length];
    System.arraycopy(objs, 0, names, 0, names.length);
    return names;
  }

  // This is used by AMS to obtain the list of all the agents of the platform.
  public String[] agentNames() {
    Object[] objs = platformAgents.keySet().toArray();
    String[] names = new String[objs.length];
    System.arraycopy(objs, 0, names, 0, names.length);
    return names;
  }

  // This maps the name of an agent to the name of the Agent Container the agent lives in.
  public String getContainerName(String agentName) throws NotFoundException {
    AgentDescriptor ad = (AgentDescriptor)platformAgents.get(agentName.toLowerCase());
    if(ad == null)
      throw new NotFoundException("Agent " + agentName + " not found in getContainerName()");
    ad.lock();
    String result = ad.getContainerName();
    ad.unlock();
    return result;
  }

  // This maps the name of an agent to its IIOP address.
  public String getAddress(String agentName) {
    // FIXME: Should not even exist; it would be better to put the
    // complete agent name in the hash table
    return platformAddress; 
  }

  // This is called in response to a 'create-agent' action
  public void create(String agentName, String className, String containerName) throws UnreachableException {
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
      throw new UnreachableException(re.getMessage());
    }
  }

  public void create(String agentName, Agent instance, String containerName) throws UnreachableException {
    String simpleName = agentName.substring(0,agentName.indexOf('@'));
    try {
      AgentContainer ac = (AgentContainer)containers.get(containerName);
      ac.createAgent(simpleName, instance, START); // RMI call, 'instance' is serialized
    }
    catch(ArrayIndexOutOfBoundsException aioobe) {
      throw new UnreachableException(aioobe.getMessage());
    }
    catch(RemoteException re) {
      throw new UnreachableException(re.getMessage());
    }
  }

  public void killContainer(String containerName) {

    // This call spawns a separate thread in order to avoid deadlock.
    final AgentContainer ac = (AgentContainer)containers.get(containerName);
    final String cName = containerName;
    Thread auxThread = new Thread(new Runnable() {
      public void run() {
	try {
	  APKillContainer(ac);
	}
	catch(RemoteException re) {
	  System.out.println("Container " + cName + " is unreachable.");
	  containers.remove(cName);
	  theAMS.postDeadContainer(cName);

	}
      }
    });
    auxThread.start();
  }


  public void sniffOn(String SnifferName, Map ToBeSniffed) throws UnreachableException  {

    Collection myContainersColl = containers.values();
    Iterator myContainers = myContainersColl.iterator();

    while (myContainers.hasNext()) {
      try {
	AgentContainer ac = (AgentContainer)myContainers.next();
	ac.enableSniffer(SnifferName, ToBeSniffed); // RMI call
      }
      catch (RemoteException re) {
	throw new UnreachableException(re.getMessage());
      } 
    }
  }

  public void sniffOff(String SnifferName, Map NotToBeSniffed) throws UnreachableException {

    Collection myContainersColl = containers.values();
    Iterator myContainers = myContainersColl.iterator();

    while (myContainers.hasNext()) {
      try {
	AgentContainer ac = (AgentContainer)myContainers.next();
	ac.disableSniffer(SnifferName, NotToBeSniffed); // RMI call
      }
      catch (RemoteException re) {
	throw new UnreachableException(re.getMessage());
      }
    }
  }


}

