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

import java.net.InetAddress;
import java.net.UnknownHostException;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;

import jade.domain.ams;
import jade.domain.df;

import jade.lang.acl.ACLMessage;


class MainContainerImpl extends AgentContainerImpl implements MainContainer, AgentManager {

  private ThreadGroup systemAgentsThreads = new ThreadGroup("JADE System Agents");

  // The two mandatory system agents.
  private ams theAMS;
  private df defaultDF;

  private List platformListeners = new LinkedList();
  private List platformAddresses = new LinkedList();
  private ContainerTable containers = new ContainerTable();
  private GADT platformAgents = new GADT();

  public MainContainerImpl() throws RemoteException {
    super();
    myName = MAIN_CONTAINER_NAME;
    systemAgentsThreads.setMaxPriority(Thread.NORM_PRIORITY + 1);
  }

  private void initAMS() {

    theAMS = new ams(this);

    // Subscribe as a listener for the AMS agent
    Agent a = theAMS;
    a.setToolkit(this);

    // Insert AMS into local agents table
    localAgents.put(Agent.AMS, theAMS);

    AgentDescriptor desc = new AgentDescriptor();
    RemoteProxyRMI rp = new RemoteProxyRMI(this, Agent.AMS);
    desc.setContainerName(myName);
    desc.setProxy(rp);

    platformAgents.put(Agent.AMS, desc);

  }

  private void initDF() {

    defaultDF = new df();

    // Subscribe as a listener for the DF agent
    Agent a = defaultDF;
    a.setToolkit(this);

    // Insert DF into local agents table
    localAgents.put(Agent.DEFAULT_DF, defaultDF);

    AgentDescriptor desc = new AgentDescriptor();
    RemoteProxyRMI rp = new RemoteProxyRMI(this, Agent.DEFAULT_DF);
    desc.setContainerName(myName);
    desc.setProxy(rp);

    platformAgents.put(Agent.DEFAULT_DF, desc);

  }

  // this variable holds a progressive number just used to name new containers
  private static int containersProgNo = 0;

  public void joinPlatform(String pID, Iterator agentSpecifiers) {

    // This string will be used to build the GUID for every agent on
    // this platform.
    platformID = pID;

    String platformRMI = "rmi://" + platformID;

    // Build the Agent IDs for the AMS and for the Default DF.
    Agent.initReservedAIDs(globalAID("ams"), globalAID("df"));

    initAMS();
    initDF();

    try {
      myPlatform = (MainContainer)Naming.lookup(platformRMI);
    }
    catch(Exception e) {
      // Should never happen
      e.printStackTrace();
    }

    theACC = new acc(this);

    containers.addContainer(MAIN_CONTAINER_NAME, this);
    containersProgNo++;

    try {
      String s = installMTP(null, myName, "jade.mtp.iiop.MessageTransportProtocol"); // FIXME: Now hardwired, will become configurable

      try {
	FileWriter f = new FileWriter("JADEaddresses.txt");
	f.write(s, 0, s.length());
	f.write('\n');
	f.close();
      }
      catch (IOException io) {
	io.printStackTrace();
      }

      System.out.println(s);

    }
    catch(NotFoundException nfe) {
      System.out.println("ERROR: Could not initialize IIOP MTP !!!");
      nfe.printStackTrace();
    }
    catch(UnreachableException ue) {
      System.out.println("The container is unreachable.");
      ue.printStackTrace();
    }

    // Notify platform listeners
    try {
      InetAddress netAddr = InetAddress.getLocalHost();
      postNewContainer(MAIN_CONTAINER_NAME, netAddr);
    }
    catch(UnknownHostException uhe) {
      uhe.printStackTrace();
    }

    Agent a = theAMS;
    a.powerUp(Agent.AMS, systemAgentsThreads);
    a = defaultDF;
    a.powerUp(Agent.DEFAULT_DF, systemAgentsThreads);

    while(agentSpecifiers.hasNext()) 
    {
      Iterator i = ((List)agentSpecifiers.next()).iterator();
    	String agentName =(String)i.next();
    	String agentClass = (String)i.next();
      List tmp = new ArrayList(); 
    	for ( ; i.hasNext(); )	         
    	  tmp.add((String)i.next());
    	  
      //verify is possible to use toArray() on tmp
    	int size = tmp.size();
      String arguments[] = new String[size];
      Iterator it = tmp.iterator();
      for(int n = 0; it.hasNext(); n++)
        arguments[n] = (String)it.next();

 
    	AID agentID = globalAID(agentName);
      try {
	      createAgent(agentID, agentClass,arguments, START);
      }
      catch(RemoteException re) { // It should never happen
	      re.printStackTrace();
      }
    }


    System.out.println("Agent Platform ready to accept new containers...");


  }

  AgentContainer getContainerFromAgent(AID agentID) throws NotFoundException {
    AgentDescriptor ad = platformAgents.get(agentID);
    if(ad == null) {
      throw new NotFoundException("Agent " + agentID.getName() + " not found in getContainerFromAgent()");
    }
    ad.lock();
    String name = ad.getContainerName();
    AgentContainer ac = containers.getContainer(name);
    ad.unlock();
    return ac;
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

	    containers.removeContainer(targetName);
	    postDeadContainer(targetName);

	    active = false;
	  }
	}
	catch(Throwable t) {
	  t.printStackTrace();
	}
      }
    }
  }



  // Private methods to notify platform listeners of a significant event.

  private void postNewContainer(String name, InetAddress host) {
    for(int i = 0; i < platformListeners.size(); i++) {
      AgentManager.Listener l = (AgentManager.Listener)platformListeners.get(i);
      l.handleNewContainer(name, host);
    }
  }

  private void postDeadContainer(String name) {
    for(int i = 0; i < platformListeners.size(); i++) {
      AgentManager.Listener l = (AgentManager.Listener)platformListeners.get(i);
      l.handleDeadContainer(name);
    }
  }

  private void postNewAgent(String containerName, AID agentID) {
    for(int i = 0; i < platformListeners.size(); i++) {
      AgentManager.Listener l = (AgentManager.Listener)platformListeners.get(i);
      l.handleNewAgent(containerName, agentID);
    }
  }

  private void postDeadAgent(String containerName, AID agentID) {
    for(int i = 0; i < platformListeners.size(); i++) {
      AgentManager.Listener l = (AgentManager.Listener)platformListeners.get(i);
      l.handleDeadAgent(containerName, agentID);
    }
  }

  private void postMovedAgent(String fromContainer, String toContainer, AID agentID) {
    for(int i = 0; i < platformListeners.size(); i++) {
      AgentManager.Listener l = (AgentManager.Listener)platformListeners.get(i);
      l.handleMovedAgent(fromContainer, toContainer, agentID);
    }
  }

  private void postNewAddress(String address) {
    for(int i = 0; i < platformListeners.size(); i++) {
      AgentManager.Listener l = (AgentManager.Listener)platformListeners.get(i);
      l.handleNewAddress(address);
   } 
  }

  private void postDeadAddress(String address) {
    for(int i = 0; i < platformListeners.size(); i++) {
      AgentManager.Listener l = (AgentManager.Listener)platformListeners.get(i);
      l.handleDeadAddress(address);
   } 
  }


  public String addContainer(AgentContainer ac, InetAddress addr) throws RemoteException {

    // Send all platform addresses to the new container
    String[] containerNames = containers.names();
    for(int i = 0; i < containerNames.length; i++) {
      String name = containerNames[i];
      
      try {
	AgentContainer cont = containers.getContainer(name);
	List addresses = containers.getAddresses(name);
	Iterator it = addresses.iterator();
	while(it.hasNext()) {
	  String a = (String)it.next();
	  ac.updateRoutingTable(ADD_RT, a, cont);
	}
      }
      catch(NotFoundException nfe) {
	nfe.printStackTrace();
      }
    }



    String name = AUX_CONTAINER_NAME + containersProgNo;
    containers.addContainer(name, ac);
    containersProgNo++;

    // Spawn a blocking RMI call to the remote container in a separate
    // thread. This is a failure notification technique.
    Thread t = new Thread(new FailureMonitor(ac, name));
    t.start();

    // Notify listeners
    postNewContainer(name, addr);

    // Return the name given to the new container
    return name;

  }

  public void removeContainer(String name) throws RemoteException {
    containers.removeContainer(name);

    // Notify listeners
    postDeadContainer(name);
  }

  public AgentContainer lookup(String name) throws RemoteException, NotFoundException {
    AgentContainer ac = containers.getContainer(name);
    return ac;
  }

  public void bornAgent(AID name, RemoteProxy rp, String containerName) throws RemoteException, NameClashException {
    AgentDescriptor desc = new AgentDescriptor();
    desc.setProxy(rp);
    desc.setContainerName(containerName);
    AgentDescriptor old = platformAgents.put(name, desc);

    // If there's already an agent with name 'name' throw a name clash
    // exception unless the old agent's container is dead.
    if(old != null) {
      RemoteProxy oldProxy = old.getProxy();
      try {
	oldProxy.ping(); // Make sure agent is reachable, then raise a name clash exception
	platformAgents.put(name, old);
	throw new NameClashException("Agent " + name + " already present in the platform ");
      }
      catch(UnreachableException ue) {
	System.out.println("Replacing a dead agent ...");
	postDeadAgent(old.getContainerName(), name);
      }
    }

    // Notify listeners
    postNewAgent(containerName, name);

  }

  public void deadAgent(AID name) throws RemoteException, NotFoundException {
    AgentDescriptor ad = platformAgents.get(name);
    if(ad == null)
      throw new NotFoundException("DeadAgent failed to find " + name);
    String containerName = ad.getContainerName();
    platformAgents.remove(name);

    // Notify listeners
    postDeadAgent(containerName, name);
  }

  public RemoteProxy getProxy(AID agentID) throws RemoteException, NotFoundException {
    RemoteProxy rp;
    AgentDescriptor ad = platformAgents.get(agentID);

    if(ad == null)
      throw new NotFoundException("getProxy() failed to find " + agentID.getName());
    else {
      ad.lock();
      rp = ad.getProxy();
      ad.unlock();
      try {
	rp.ping();
      }
      catch(UnreachableException ue) {
	throw new NotFoundException("Container for " + agentID.getName() + " is unreachable");
      }
      return rp;
    }
  }

  public boolean transferIdentity(AID agentID, String src, String dest) throws RemoteException, NotFoundException {
    AgentDescriptor ad = platformAgents.get(agentID);
    if(ad == null)
      throw new NotFoundException("transferIdentity() unable to find agent " + agentID.getName());
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

    // Commit transaction and notify listeners
    ad.lock();
    ad.setProxy(new RemoteProxyRMI(destAC, agentID));
    ad.setContainerName(dest);
    postMovedAgent(src, dest, agentID);
    ad.unlock();
    return true;
  }

  // This method overrides AgentContainerImpl.shutDown(); besides
  // behaving like the normal AgentContainer version, it makes all
  // other agent containers exit.
  public void shutDown() {

    // Deregister yourself as a container
    containers.removeContainer(MAIN_CONTAINER_NAME);

    // Kill every other container
    AgentContainer[] allContainers = containers.containers();
    for(int i = 0; i < allContainers.length; i++) {
      AgentContainer ac = allContainers[i];
      try {
	APKillContainer(ac); // This call removes 'ac' from 'container' map and from the collection 'c'
      }
      catch(RemoteException re) {
	System.out.println("Container is unreachable. Ignoring...");
      } 
    }

    // Kill all non-system agents
    AID[] allLocalNames = localAgents.keys();
    for(int i = 0; i < allLocalNames.length; i++) {
      AID id = allLocalNames[i];
      if(id.equals(Agent.AMS) || 
	 id.equals(Agent.DEFAULT_DF))
	  continue;

      // Kill agent and wait for its termination
      Agent a = localAgents.get(id);
      a.doDelete();
      a.join();
    }


    // Kill system agents, at last

    Agent systemAgent = defaultDF;
    systemAgent.doDelete();
    systemAgent.join();
    systemAgent.resetToolkit();

    systemAgent = theAMS;
    systemAgent.doDelete();
    systemAgent.join();
    systemAgent.resetToolkit();
    removeListener(theAMS);

    // Now, close all MTP links to the outside world
    theACC.shutdown();
  }

  // These methods dispatch agent management operations to
  // appropriate Agent Container through RMI.

  public void kill(AID agentID, String password) throws NotFoundException, UnreachableException {
    try {
      AgentContainer ac = getContainerFromAgent(agentID);
      ac.killAgent(agentID); // RMI call
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

  public void suspend(AID agentID, String password) throws NotFoundException, UnreachableException {
    try {
      AgentContainer ac = getContainerFromAgent(agentID);
      ac.suspendAgent(agentID); // RMI call
    }
    catch(RemoteException re) {
      throw new UnreachableException(re.getMessage());
    }
  }

  public void activate(AID agentID, String password) throws NotFoundException, UnreachableException {
    try {
      AgentContainer ac = getContainerFromAgent(agentID);
      ac.resumeAgent(agentID); // RMI call
    }
    catch(RemoteException re) {
      throw new UnreachableException(re.getMessage());
    }
  }

  public void wait(AID agentID, String password) throws NotFoundException, UnreachableException {
    try {
      AgentContainer ac = getContainerFromAgent(agentID);
      ac.waitAgent(agentID); // RMI call
    }
    catch(RemoteException re) {
      throw new UnreachableException(re.getMessage());
    }
  }

  public void wake(AID agentID, String password) throws NotFoundException, UnreachableException {
    try {
      AgentContainer ac = getContainerFromAgent(agentID);
      ac.wakeAgent(agentID); // RMI call
    }
    catch(RemoteException re) {
      throw new UnreachableException(re.getMessage());
    }
  }

  public void move(AID agentID, Location where, String password) throws NotFoundException, UnreachableException {
    // Retrieve the container for the original agent
    AgentContainer src = getContainerFromAgent(agentID);
    try {
      src.moveAgent(agentID, where);
    }
    catch(RemoteException re) {
      throw new UnreachableException(re.getMessage());
    }
  }

  public void copy(AID agentID, Location where, String newAgentID, String password) throws NotFoundException, UnreachableException {
    // Retrieve the container for the original agent
    AgentContainer src = getContainerFromAgent(agentID);
    try {
      src.copyAgent(agentID, where, newAgentID); // RMI call
    }
    catch(RemoteException re) {
      throw new UnreachableException(re.getMessage());
    }
  }


  // Methods for Message Transport Protocols management


  public String installMTP(String address, String containerName, String className) throws NotFoundException, UnreachableException {
    AgentContainer target = containers.getContainer(containerName);

    try {
      String result = target.installMTP(address, className);
      platformAddresses.add(result);
      containers.addAddress(containerName, result);

      // To avoid additions/removals of containers during MTP tables update
      synchronized(containers) {
	AgentContainer[] allContainers = containers.containers();
	for(int i = 0; i < allContainers.length; i++) {
	  AgentContainer ac = allContainers[i];
	  ac.updateRoutingTable(ADD_RT, result, target);
	}

      }

      postNewAddress(result);

      return result;
    }
    catch(RemoteException re) {
      throw new UnreachableException("Container " + containerName + " is unreachable.");
    }

  }

  public void uninstallMTP(String address, String containerName) throws NotFoundException, UnreachableException {
    // FIXME: To be implemented
  }


  // These methods are to be used only by AMS agent.

  public void addListener(AgentManager.Listener l) {
    platformListeners.add(l);
  }

  public void removeListener(AgentManager.Listener l) {
    platformListeners.remove(l);
  }

  // This is used by AMS to obtain the set of all the Agent Containers of the platform.
  public String[] containerNames() {
    return containers.names();
  }

  // This is used by AMS to obtain the list of all the agents of the platform.
  public AID[] agentNames() {
    return platformAgents.keys();
  }

  public String[] platformAddresses() {
    Object[] objs = platformAddresses.toArray();
    String[] result = new String[objs.length];
    System.arraycopy(objs, 0, result, 0, result.length);
    return result;
  }

  // This maps the name of an agent to the name of the Agent Container the agent lives in.
  public String getContainerName(AID agentID) throws NotFoundException {
    AgentDescriptor ad = platformAgents.get(agentID);
    if(ad == null)
      throw new NotFoundException("Agent " + agentID.getName() + " not found in getContainerName()");
    ad.lock();
    String result = ad.getContainerName();
    ad.unlock();
    return result;
  }

  // This is called in response to a 'create-agent' action
  public void create(String agentName, String className, String args[],String containerName) throws UnreachableException {
    try {
      AgentContainer ac;
      // If no name is given, the agent is started on the MainContainer itself
      if(containerName == null)
	ac = this; 
      else {
	try {
	  ac = containers.getContainer(containerName);
	}
	catch(NotFoundException nfe) {
	  // If a wrong name is given, then again the agent starts on the MainContainer itself
	  ac = this;
	}
      }
      AID id = globalAID(agentName);
      ac.createAgent(id, className, args,START); // RMI call
    }
    catch(RemoteException re) {
      throw new UnreachableException(re.getMessage());
    }
  }

  public void killContainer(String containerName) {

    // This call spawns a separate thread in order to avoid deadlock.
    try {
      final AgentContainer ac = containers.getContainer(containerName);
      final String cName = containerName;
      Thread auxThread = new Thread(new Runnable() {
	 public void run() {
	   try {
	     APKillContainer(ac);
	   }
	   catch(RemoteException re) {
	     System.out.println("Container " + cName + " is unreachable.");
	     containers.removeContainer(cName);
	     postDeadContainer(cName);
	   }
	 }
      });
      auxThread.start();
    }
    catch(NotFoundException nfe) {
      nfe.printStackTrace();
    }
  }


  public void sniffOn(AID snifferName, List toBeSniffed) throws UnreachableException  {

    AgentContainer[] allContainers = containers.containers();
    for(int i = 0; i < allContainers.length; i++) {
      try {
	AgentContainer ac = allContainers[i]; 
	ac.enableSniffer(snifferName, toBeSniffed); // RMI call
      }
      catch (RemoteException re) {
	throw new UnreachableException(re.getMessage());
      } 
    }
  }

  public void sniffOff(AID snifferName, List notToBeSniffed) throws UnreachableException {

    AgentContainer[] allContainers = containers.containers();
    for(int i = 0; i < allContainers.length; i++) {
      try {
	AgentContainer ac = allContainers[i];
	ac.disableSniffer(snifferName, notToBeSniffed); // RMI call
      }
      catch (RemoteException re) {
	throw new UnreachableException(re.getMessage());
      }
    }
  }

}
