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
import java.io.PrintWriter;
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

import jade.core.event.PlatformEvent;
import jade.core.event.MTPEvent;

import jade.domain.ams;
import jade.domain.df;

import jade.lang.acl.ACLMessage;

import jade.mtp.MTPException;


/**
   This class is a concrete implementation of the JADE main container,
   providing runtime support to JADE agents, and the special, front
   end container where the AMS and the Default DF can run.
   This class cannot be instantiated from applications. Instead, the
   <code>Runtime.createMainContainer(Profile p)</code> method must be
   called.

   @see Runtime#createMainContainer(Profile p);

   @author Giovanni Rimassa - Universita` di Parma
   @version $Date$ $Revision$

*/
public class MainContainerImpl extends AgentContainerImpl implements MainContainer, AgentManager {

  private ThreadGroup systemAgentsThreads = new ThreadGroup("JADE System Agents");

  // The two mandatory system agents.
  private ams theAMS;
  private df defaultDF;

  private List platformListeners = new LinkedList();
  private List platformAddresses = new LinkedList();
  private ContainerTable containers = new ContainerTable();
  private GADT platformAgents = new GADT();

  MainContainerImpl( String pID ) throws RemoteException {
    super();
    systemAgentsThreads.setMaxPriority(Thread.NORM_PRIORITY + 1);

    // This string will be used to build the GUID for every agent on
    // this platform.
    platformID = pID;
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
    desc.setContainerID(myID);
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
    desc.setContainerID(myID);
    desc.setProxy(rp);

    platformAgents.put(Agent.DEFAULT_DF, desc);

  }

  // this variable holds a progressive number just used to name new containers
  private static int containersProgNo = 0;

  public void joinPlatform( String pRMI, Iterator agentSpecifiers, String[] MTPs, String[] ACLCodecs) {

    // This string will be used as the transport address for the main container
    platformRMI = pRMI;

    try {
      InetAddress netAddr = InetAddress.getLocalHost();
      myID = new ContainerID(MAIN_CONTAINER_NAME, netAddr);
    }
    catch(UnknownHostException uhe) {
      uhe.printStackTrace();
    }

    // Build the Agent IDs for the AMS and for the Default DF.
    Agent.initReservedAIDs(new AID("ams", AID.ISLOCALNAME), new AID("df", AID.ISLOCALNAME));

    initAMS();
    initDF();

    try {
      myPlatform = (MainContainer)Naming.lookup(platformRMI);
    }
    catch(Exception e) {
      // Should never happen
      e.printStackTrace();
    }

    theACC = new acc(this, platformID);
		try{
    
			for(int i =0; i<ACLCodecs.length;i++){
				String className = ACLCodecs[i];
				installACLCodec(className);
			}

    containers.addContainer(MAIN_CONTAINER_NAME, this);
    containersProgNo++;

      PrintWriter f = new PrintWriter(new FileWriter("MTPs-" + MAIN_CONTAINER_NAME + ".txt"));

      for(int i = 0; i < MTPs.length; i += 2) {

				String className = MTPs[i];
				String addressURL = MTPs[i+1];
				if(addressURL.equals(""))
	  			addressURL = null;
				String s = installMTP(addressURL, className);

				f.println(s);
				System.out.println(s);
      }

      f.close();

    }

    catch(RemoteException re) {
      // This should never happen...
      re.printStackTrace();
    }
    catch (IOException io) {
      io.printStackTrace();
    }
    catch(MTPException mtpe) {
      mtpe.printStackTrace();
      Runtime.instance().endContainer();
    }catch(jade.lang.acl.ACLCodec.CodecException ce){
    	ce.printStackTrace();
    	Runtime.instance().endContainer();
    }

    // Notify platform listeners
    fireAddedContainer(myID);

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

 
    	AID agentID = new AID(agentName, AID.ISLOCALNAME);
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
    ContainerID cid = ad.getContainerID();
    AgentContainer ac = containers.getContainer(cid.getName());
    ad.unlock();
    return ac;
  }

  // Inner class to detect agent container failures
  private class FailureMonitor implements Runnable {

    private AgentContainer target;
    private ContainerID targetID;
    private boolean active = true;

    public FailureMonitor(AgentContainer ac, ContainerID cid) {
      target = ac;
      targetID = cid;
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

	    containers.removeContainer(targetID.getName());
	    fireRemovedContainer(targetID);

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

  private void fireAddedContainer(ContainerID cid) {
    PlatformEvent ev = new PlatformEvent(PlatformEvent.ADDED_CONTAINER, cid);
    for(int i = 0; i < platformListeners.size(); i++) {
      AgentManager.Listener l = (AgentManager.Listener)platformListeners.get(i);
      l.addedContainer(ev);
    }
  }

  private void fireRemovedContainer(ContainerID cid) {
    PlatformEvent ev = new PlatformEvent(PlatformEvent.REMOVED_CONTAINER, cid);

    for(int i = 0; i < platformListeners.size(); i++) {
      AgentManager.Listener l = (AgentManager.Listener)platformListeners.get(i);
      l.removedContainer(ev);
    }
  }

  private void fireBornAgent(ContainerID cid, AID agentID) {
    PlatformEvent ev = new PlatformEvent(PlatformEvent.BORN_AGENT, agentID, cid);

    for(int i = 0; i < platformListeners.size(); i++) {
      AgentManager.Listener l = (AgentManager.Listener)platformListeners.get(i);
      l.bornAgent(ev);
    }
  }

  private void fireDeadAgent(ContainerID cid, AID agentID) {
    PlatformEvent ev = new PlatformEvent(PlatformEvent.DEAD_AGENT, agentID, cid);

    for(int i = 0; i < platformListeners.size(); i++) {
      AgentManager.Listener l = (AgentManager.Listener)platformListeners.get(i);
      l.deadAgent(ev);
    }
  }

  private void fireMovedAgent(ContainerID from, ContainerID to, AID agentID) {
    PlatformEvent ev = new PlatformEvent(agentID, from, to);

    for(int i = 0; i < platformListeners.size(); i++) {
      AgentManager.Listener l = (AgentManager.Listener)platformListeners.get(i);
      l.movedAgent(ev);
    }
  }

  private void fireAddedMTP(String address, ContainerID cid) {
    Channel ch = new Channel("FIXME: missing channel name", "FIXME: missing channel protocol", address);
    MTPEvent ev = new MTPEvent(MTPEvent.ADDED_MTP, cid, ch);

    for(int i = 0; i < platformListeners.size(); i++) {
      AgentManager.Listener l = (AgentManager.Listener)platformListeners.get(i);
      l.addedMTP(ev);
   } 
  }

  private void fireRemovedMTP(String address, ContainerID cid) {
    Channel ch = new Channel("FIXME: missing channel name", "FIXME: missing channel protocol", address);
    MTPEvent ev = new MTPEvent(MTPEvent.REMOVED_MTP, cid, ch);

    for(int i = 0; i < platformListeners.size(); i++) {
      AgentManager.Listener l = (AgentManager.Listener)platformListeners.get(i);
      l.removedMTP(ev);
   }
  }

    public String getPlatformName() throws RemoteException {
	return platformID;
    }

  public String addContainer(AgentContainer ac, ContainerID cid) throws RemoteException {

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
    cid.setName(name);
    containers.addContainer(name, ac);
    containersProgNo++;

    // Spawn a blocking RMI call to the remote container in a separate
    // thread. This is a failure notification technique.
    Thread t = new Thread(new FailureMonitor(ac, cid));
    t.start();

    // Notify listeners
    fireAddedContainer(cid);

    // Return the name given to the new container
    return name;

  }

  public void removeContainer(ContainerID cid) throws RemoteException {
    containers.removeContainer(cid.getName());

    // Notify listeners
    fireRemovedContainer(cid);
  }

  public AgentContainer lookup(ContainerID cid) throws RemoteException, NotFoundException {
    AgentContainer ac = containers.getContainer(cid.getName());
    return ac;
  }

  public void bornAgent(AID name, RemoteProxy rp, ContainerID cid) throws RemoteException, NameClashException {
    AgentDescriptor desc = new AgentDescriptor();
    desc.setProxy(rp);
    desc.setContainerID(cid);
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
	fireDeadAgent(old.getContainerID(), name);
      }
    }

    // Notify listeners
    fireBornAgent(cid, name);

  }

  public void deadAgent(AID name) throws RemoteException, NotFoundException {
    AgentDescriptor ad = platformAgents.get(name);
    if(ad == null)
      throw new NotFoundException("DeadAgent failed to find " + name);
    ContainerID cid = ad.getContainerID();
    platformAgents.remove(name);

    // Notify listeners
    fireDeadAgent(cid, name);
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

  public boolean transferIdentity(AID agentID, ContainerID src, ContainerID dest) throws RemoteException, NotFoundException {
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
    ad.setContainerID(dest);
    fireMovedAgent(src, dest, agentID);
    ad.unlock();
    return true;
  }

  // This method overrides AgentContainerImpl.shutDown(); besides
  // behaving like the normal AgentContainer version, it makes all
  // other agent containers exit.
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
      if(a != null) {
	a.doDelete();
	a.join();
      }
      else // FIXME: Should not happen, but it does when there are sniffers around...
	System.out.println("Zombie agent [" + id + "]");
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

    try {
      // Unexport the container, without waiting for pending calls to
      // complete.
      unexportObject(this, true);
    }
    catch(RemoteException re) {
      re.printStackTrace();
    }

    // Notify the JADE Runtime that the container has terminated
    // execution
    Runtime.instance().endContainer();

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


  public void newMTP(String mtpAddress, ContainerID cid) throws RemoteException {
    try {
      String containerName = cid.getName();
      platformAddresses.add(mtpAddress);
      containers.addAddress(containerName, mtpAddress);
      AgentContainer target = containers.getContainer(containerName);

      // To avoid additions/removals of containers during MTP tables update
      synchronized(containers) {

	// Add the new MTP to the routing tables of all the containers. 
	AgentContainer[] allContainers = containers.containers();
	for(int i = 0; i < allContainers.length; i++) {
	  AgentContainer ac = allContainers[i];
	  // Skip target container
	  if(ac != target)
	    ac.updateRoutingTable(ADD_RT, mtpAddress, target);
	}

      }

      // Notify listeners (typically the AMS)
      fireAddedMTP(mtpAddress, cid);
    }
    catch(NotFoundException nfe) {
      System.out.println("Error: the container " + cid.getName() + " was not found.");
    }
  }

  public void deadMTP(String mtpAddress, ContainerID cid) throws RemoteException {
    try {
      String containerName = cid.getName();
      platformAddresses.remove(mtpAddress);
      containers.removeAddress(containerName, mtpAddress);
      AgentContainer target = containers.getContainer(containerName);

      // To avoid additions/removals of containers during MTP tables update
      synchronized(containers) {

	// Remove the dead MTP from the routing tables of all the containers. 
	AgentContainer[] allContainers = containers.containers();
	for(int i = 0; i < allContainers.length; i++) {
	  AgentContainer ac = allContainers[i];
	  // Skip target container
	  if(ac != target)
	    ac.updateRoutingTable(DEL_RT, mtpAddress, target);
	}

      }

      // Notify listeners (typically the AMS)
      fireRemovedMTP(mtpAddress, cid);

    }
    catch(NotFoundException nfe) {
      System.out.println("Error: the container " + cid.getName() + " was not found.");
      nfe.printStackTrace();
    }

  }

  public String installMTP(String address, ContainerID cid, String className) throws NotFoundException, UnreachableException, MTPException {
    String containerName = cid.getName();
    AgentContainer target = containers.getContainer(containerName);
    try {
      return target.installMTP(address, className);
    }
    catch(RemoteException re) {
      throw new UnreachableException("Container " + containerName + " is unreachable.");
    }

  }

  public void uninstallMTP(String address, ContainerID cid) throws NotFoundException, UnreachableException, MTPException {
    String containerName = cid.getName();
    AgentContainer target = containers.getContainer(containerName);
    try {
      target.uninstallMTP(address);
    }
    catch(RemoteException re) {
      throw new UnreachableException("Container " + containerName + " is unreachable.");
    }

  }


  // These methods are to be used only by AMS agent.

  public void addListener(AgentManager.Listener l) {
    platformListeners.add(l);
  }

  public void removeListener(AgentManager.Listener l) {
    platformListeners.remove(l);
  }

  // This is used by AMS to obtain the set of all the Agent Containers of the platform.
  public ContainerID[] containerIDs() {
    String[] names = containers.names();
    ContainerID[] ids = new ContainerID[names.length];
    for(int i = 0; i < names.length; i++) {
      ids[i] = new ContainerID(names[i], null);
    }
    return ids;
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

  // This maps the name of an agent to the ID of the Agent Container the agent lives in.
  public ContainerID getContainerID(AID agentID) throws NotFoundException {
    AgentDescriptor ad = platformAgents.get(agentID);
    if(ad == null)
      throw new NotFoundException("Agent " + agentID.getName() + " not found in getContainerID()");
    ad.lock();
    ContainerID result = ad.getContainerID();
    ad.unlock();
    return result;
  }

  // This is called in response to a 'create-agent' action
  public void create(String agentName, String className, String args[], ContainerID cid) throws UnreachableException {
    try {
      String containerName = cid.getName();
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
      AID id = new AID(agentName, AID.ISLOCALNAME);
      ac.createAgent(id, className, args,START); // RMI call
    }
    catch(RemoteException re) {
      throw new UnreachableException(re.getMessage());
    }
  }

  public void killContainer(ContainerID cid) {

    // This call spawns a separate thread in order to avoid deadlock.
    try {
      String containerName = cid.getName();
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
	     fireRemovedContainer(new ContainerID(cName, null));
	   }
	 }
      });
      auxThread.start();
    }
    catch(NotFoundException nfe) {
      nfe.printStackTrace();
    }
  }


  public void sniffOn(AID snifferName, List toBeSniffed) throws NotFoundException, UnreachableException  {
    Iterator it = toBeSniffed.iterator();
    try {
      while(it.hasNext()) {
	AID id = (AID)it.next();
	AgentContainer ac = getContainerFromAgent(id);
	ac.enableSniffer(snifferName, id); // RMI call
      }
    }
    catch(RemoteException re) {
      throw new UnreachableException(re.getMessage());
    }

  }

  public void sniffOff(AID snifferName, List notToBeSniffed) throws NotFoundException, UnreachableException {
    Iterator it = notToBeSniffed.iterator();
    try {
      while(it.hasNext()) {
	AID id = (AID)it.next();
	AgentContainer ac = getContainerFromAgent(id);
	ac.disableSniffer(snifferName, id); // RMI call
      }
    }
    catch(RemoteException re) {
      throw new UnreachableException(re.getMessage());
    }

  }

  public void debugOn(AID debuggerName, List toBeDebugged) throws NotFoundException, UnreachableException {
    Iterator it = toBeDebugged.iterator();
    try {
      while(it.hasNext()) {
	AID id = (AID)it.next();
	AgentContainer ac = getContainerFromAgent(id);
	ac.enableDebugger(debuggerName, id); // RMI call
      }
    }
    catch(RemoteException re) {
      throw new UnreachableException(re.getMessage());
    }
  }

  public void debugOff(AID debuggerName, List notToBeDebugged) throws NotFoundException, UnreachableException {
    Iterator it = notToBeDebugged.iterator();
    try {
      while(it.hasNext()) {
	AID id = (AID)it.next();
	AgentContainer ac = getContainerFromAgent(id);
	ac.disableDebugger(debuggerName, id); // RMI call
      }
    }
    catch(RemoteException re) {
      throw new UnreachableException(re.getMessage());
    }
  }


}
