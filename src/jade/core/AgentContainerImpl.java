package jade.core;

import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import jade.lang.acl.*;

/***********************************************************************************

  Name: AgentContainerImpl

  Responsibilities and Collaborations:

  + Creates agents on the local Java VM, and starts the message dispatcher.
    (Agent, MessageDispatcher)

  + Connects with each newly created agent, to allow event-based
    interaction between the two.
    (Agent, MessageDispatcher)

  + Routes outgoing messages to the suitable message dispatcher, caching
    remote agent addresses.
    (Agent, AgentDescriptor, MessageDispatcher)

  + Holds an RMI object reference for the agent platform, used to
    retrieve the addresses of unknown agents.
    (AgentPlatform, MessageDispatcher)


**************************************************************************************/
public class AgentContainerImpl extends UnicastRemoteObject implements AgentContainer, CommListener {

  private static final int MAP_SIZE = 20;
  private static final float MAP_LOAD_FACTOR = 0.50f;

  // Local agents, indexed by agent name
  private Hashtable localAgents = new Hashtable(MAP_SIZE, MAP_LOAD_FACTOR);

  // Remote agents cache, indexed by agent name
  private Hashtable remoteAgentsCache = new Hashtable(MAP_SIZE, MAP_LOAD_FACTOR);

  // The message dispatcher of this container
  private MessageDispatcherImpl myDispatcher;

  // The agent platform this container belongs to
  private AgentPlatform myPlatform;

  public AgentContainerImpl() throws RemoteException {
  }

  public void joinPlatform(String platformURL, Vector agentNamesAndClasses) {

    Agent agent = null;
    AgentDescriptor desc = new AgentDescriptor();



    // Retrieve agent platform from RMI registry and register as agent container
    try {
      myDispatcher = new MessageDispatcherImpl(localAgents);
      myPlatform = (AgentPlatform) Naming.lookup(platformURL);
      myPlatform.addContainer(this);
    }
    catch(RemoteException re) {
      System.err.println("Communication failure while contacting agent platform.");
      re.printStackTrace();
    }
    catch(Exception e) {
      System.err.println("Some problem occurred while contacting agent platform.");
      e.printStackTrace();
    }

    /* Create all agents and set up necessary links for message passing.

       The link chain is:
       a) Agent 1 -> AgentContainer1 -- Through CommEvent
       b) AgentContainer 1 -> MessageDispatcher 2 -- Through RMI (cached or retreived from AgentPlatform)
       c) MessageDispatcher 2 -> Agent 2 -- Through postMessage() (direct insertion in message queue, no events here)

       agentNamesAndClasses is a Vector of String containing, orderly, the name of an agent and the name of Agent
       concrete subclass which implements that agent.
    */
    for( int i=0; i < agentNamesAndClasses.size(); i+=2 ) {
      String agentName = (String)agentNamesAndClasses.elementAt(i);
      String agentClass = (String)agentNamesAndClasses.elementAt(i+1);

      System.out.println("new agent: " + agentName + " : " + agentClass);

      try {
	agent = (Agent)Class.forName(new String(agentClass)).newInstance();
      }
      catch(ClassNotFoundException cnfe) {
	System.err.println("Class " + agentClass + " for agent " + agentName + " was not found.");
	continue;
      }
      catch( Exception e ){
	e.printStackTrace();
      }

      // Subscribe as a listener for the new agent
      agent.addCommListener(this);

      // Insert new agent into local agents table
      localAgents.put(agentName,agent);

      // Build an agent descriptor and send it to the centralized agent table
      desc.set(agentName,myDispatcher);
      try {
	myPlatform.bornAgent(desc); // RMI call
      }
      catch(RemoteException re) {
	System.out.println("Communication error while adding a new agent to the platform.");
	re.printStackTrace();
      }
    }

    // Now activate all agents (this call starts their embedded threads)
    Enumeration nameList = localAgents.keys();
    String currentName = null;
    while(nameList.hasMoreElements()) {
      currentName = (String)nameList.nextElement();
      agent = (Agent)localAgents.get(currentName);
      agent.doStart(currentName);
    }
  }

  public void shutDown() {
    Enumeration agentNames = localAgents.keys();

    try {

      // Remove all agents
      while(agentNames.hasMoreElements()) {
	String name = (String)agentNames.nextElement();
	Agent a = (Agent)localAgents.get(name);
	localAgents.remove(name);
	a.doDelete();
	myPlatform.deadAgent(name); // RMI call
      }

      // Deregister itself as a container
      myPlatform.removeContainer(this); // RMI call
    }
    catch(RemoteException re) {
      re.printStackTrace();
    }
  }

  protected void finalize() {
  }

  public void CommHandle(CommEvent event) {

    // Get ACL message from the event.
    ACLMessage msg = event.getMessage();

    // FIXME: Must use multicast also when more than a recipient is
    // present in ':receiver' field.
    if(event.isMulticast()) {
      AgentGroup group = event.getRecipients();
      while(group.hasMoreMembers()) {
	msg.setDest(group.getNextMember());
	unicastPostMessage(msg);
      }
    }
    else
      unicastPostMessage(msg);
  }

  private void unicastPostMessage(ACLMessage msg) {
    String receiverName = msg.getDest();

    // Look up in local agents.
    Agent receiver = (Agent)localAgents.get(receiverName);

    if(receiver != null)
      receiver.postMessage(msg);
    else
      // Search failed; look up in remote agents.
      postRemote(msg, receiverName);
      
    // If it fails again, ask the Agent Platform.
    // If still fails, raise NotFound exception.
  }

  private void postRemote(ACLMessage msg, String receiverName) {

    // Look first in descriptor cache
    AgentDescriptor desc = (AgentDescriptor)remoteAgentsCache.get(receiverName);
    try {

      if(desc == null) { // Cache miss :-( . Ask agent platform and update agent cache
	desc = myPlatform.lookup(receiverName); // RMI call
	remoteAgentsCache.put(receiverName,desc);  // FIXME: The cache grows indefinitely ...
      }
      MessageDispatcher md = desc.getDemux();
      md.dispatch(msg); // RMI call

    }
    catch(NotFoundException nfe) {
      System.err.println("Agent was not found on remote agent container");
      nfe.printStackTrace();
    }
    catch(RemoteException re) {
      System.out.println("Communication error while contacting remote agent container");
      re.printStackTrace();
    }
  }

}
