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

import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Serializable;

import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

import java.io.IOException;
import java.io.InterruptedIOException;

import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;

import java.util.List;
import java.util.ArrayList;

import jade.core.behaviours.Behaviour;

import jade.lang.Codec;
import jade.lang.acl.*;

import jade.onto.Name;
import jade.onto.Frame;
import jade.onto.Ontology;
import jade.onto.OntologyException;

// Concepts from fipa-agent-management ontology
import jade.onto.basic.AID;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.domain.FIPAAgentManagement.DFAgentDescription;

// Actions from fipa-agent-management ontology
import jade.domain.FIPAAgentManagement.Deregister;
import jade.domain.FIPAAgentManagement.Modify;
import jade.domain.FIPAAgentManagement.Register;
import jade.domain.FIPAAgentManagement.Search;

import jade.domain.FIPAException;


/**
   The <code>Agent</code> class is the common superclass for user
   defined software agents. It provides methods to perform basic agent
   tasks, such as:
   <ul>
   <li> <b> Message passing using <code>ACLMessage</code> objects,
   both unicast and multicast with optional pattern matching. </b>
   <li> <b> Complete Agent Platform life cycle support, including
   starting, suspending and killing an agent. </b>
   <li> <b> Scheduling and execution of multiple concurrent activities. </b>
   <li> <b> Simplified interaction with <em>FIPA</em> system agents
   for automating common agent tasks (DF registration, etc.). </b>
   </ul>

   Application programmers must write their own agents as
   <code>Agent</code> subclasses, adding specific behaviours as needed
   and exploiting <code>Agent</code> class capabilities.
   
   @author Giovanni Rimassa - Universita` di Parma
   @version $Date$ $Revision$
 */

public class Agent implements Runnable, Serializable, CommBroadcaster {

  // This inner class is used to force agent termination when a signal
  // from the outside is received
  private class AgentDeathError extends Error {

    AgentDeathError() {
      super("Agent " + Thread.currentThread().getName() + " has been terminated.");
    }

  }

  private static class AgentInMotionError extends Error {
    AgentInMotionError() {
      super("Agent " + Thread.currentThread().getName() + " is about to move or be cloned.");
    }
  }

  // This class manages bidirectional associations between Timer and
  // Behaviour objects, using hash tables. This class is fully
  // synchronized because is accessed both by agent internal thread
  // and high priority Timer Dispatcher thread.
  private static class AssociationTB {
    private Map BtoT = new HashMap();
    private Map TtoB = new HashMap();

    public synchronized void addPair(Behaviour b, Timer t) {
      BtoT.put(b, t);
      TtoB.put(t, b);
    }

    public synchronized void removeMapping(Behaviour b) {
      Timer t = (Timer)BtoT.remove(b);
      if(t != null) {
	TtoB.remove(t);
      }
    }

    public synchronized void removeMapping(Timer t) {
      Behaviour b = (Behaviour)TtoB.remove(t);
      if(b != null) {
	BtoT.remove(b);
      }
    }

    public synchronized Timer getPeer(Behaviour b) {
      return (Timer)BtoT.get(b);
    }

    public synchronized Behaviour getPeer(Timer t) {
      return (Behaviour)TtoB.get(t);
    }

  }

  private static TimerDispatcher theDispatcher;

  static void setDispatcher(TimerDispatcher td) {
    if(theDispatcher == null) {
      theDispatcher = td;
      theDispatcher.start();
    }
  }

  static void stopDispatcher() {
    theDispatcher.stop();
  }

  /**
     Schedules a restart for a behaviour, after a certain amount of
     time has passed.
     @param b The behaviour to restart later.
     @param millis The amount of time to wait before restarting
     <code>b</code>.
     @see jade.core.behaviours.Behaviour#block(long millis)
  */
  public void restartLater(Behaviour b, long millis) {
    if(millis == 0)
      return;
    Timer t = new Timer(System.currentTimeMillis() + millis, this);
    pendingTimers.addPair(b, t);
    theDispatcher.add(t);
  }

  // Restarts the behaviour associated with t. This method runs within
  // the time-critical Timer Dispatcher thread.
  void doTimeOut(Timer t) {
    Behaviour b = pendingTimers.getPeer(t);
    if(b != null) {
      activateBehaviour(b);
    }
  }

  /**
     Notifies this agent that one of its behaviours has been restarted
     for some reason. This method clears any timer associated with
     behaviour object <code>b</code>, and it is unneeded by
     application level code. To explicitly schedule behaviours, use
     <code>block()</code> and <code>restart()</code> methods.
     @param b The behaviour object which was restarted.
     @see jade.core.behaviours.Behaviour#restart()
  */
  public void notifyRestarted(Behaviour b) {
    Timer t = pendingTimers.getPeer(b);
    if(t != null) {
      pendingTimers.removeMapping(b);
      theDispatcher.remove(t);
    }
  }

  /**
     Out of band value for Agent Platform Life Cycle states.
  */
  public static final int AP_MIN = 0;   // Hand-made type checking

  /**
     Represents the <em>initiated</em> agent state.
  */
  public static final int AP_INITIATED = 1;

  /**
     Represents the <em>active</em> agent state.
  */
  public static final int AP_ACTIVE = 2;

  /**
     Represents the <em>suspended</em> agent state.
  */
  public static final int AP_SUSPENDED = 3;

  /**
     Represents the <em>waiting</em> agent state.
  */
  public static final int AP_WAITING = 4;

  /**
     Represents the <em>deleted</em> agent state.
  */
  public static final int AP_DELETED = 5;

  /**
     Represents the <code>transit</code> agent state.
  */
  public static final int AP_TRANSIT = 6;

  // Non compliant states, used internally. Maybe report to FIPA...
  /**
     Represents the <code>copy</code> agent state.
  */
  static final int AP_COPY = 7;

  /**
     Represents the <code>gone</code> agent state. This is the state
     the original instance of an agent goes into when a migration
     transaction successfully commits.
  */
  static final int AP_GONE = 8;

  /**
     Out of band value for Agent Platform Life Cycle states.
  */
  public static final int AP_MAX = 9;    // Hand-made type checking


  /**
     These constants represent the various Domain Life Cycle states
  */

  /**
     Out of band value for Domain Life Cycle states.
  */
  public static final int D_MIN = 9;     // Hand-made type checking

  /**
     Represents the <em>active</em> agent state.
  */
  public static final int D_ACTIVE = 10;

  /**
     Represents the <em>suspended</em> agent state.
  */
  public static final int D_SUSPENDED = 20;

  /**
     Represents the <em>retired</em> agent state.
  */
  public static final int D_RETIRED = 30;

  /**
     Represents the <em>unknown</em> agent state.
  */
  public static final int D_UNKNOWN = 40;

  /**
     Out of band value for Domain Life Cycle states.
  */
  public static final int D_MAX = 41;    // Hand-made type checking

  /**
     Default value for message queue size. When the number of buffered
     messages exceeds this value, older messages are discarded
     according to a <b><em>FIFO</em></b> replacement policy.
     @see jade.core.Agent#setQueueSize(int newSize)
     @see jade.core.Agent#getQueueSize()
  */
  public static final int MSG_QUEUE_SIZE = 100;

  /**
     The Agent ID for the AMS of this platform.
   */
  public static final AID AMS;

  /**
     The Agent ID for the Default DF of this platform.
   */
  public static final AID DEFAULT_DF;

  /**
  @serial
  */
  private MessageQueue msgQueue = new MessageQueue(MSG_QUEUE_SIZE);
  private transient List listeners = new ArrayList();

  /**
  @serial
  */
  private String myName = null;
  
  private AID myAID = null;

  /**
  @serial
  */
  private String myHap = null;
  private transient Object stateLock = new Object(); // Used to make state transitions atomic
  private transient Object waitLock = new Object();  // Used for agent waiting
  private transient Object suspendLock = new Object(); // Used for agent suspension

  private transient Thread myThread;
  
  /**
  @serial
  */
  private Scheduler myScheduler;

  private transient AssociationTB pendingTimers = new AssociationTB();

  // Free running counter that increments by one for each message
  // received.
  /**
  @serial
  */
  private int messageCounter = 0 ;

  private transient Map languages = new HashMap();
  private transient Map ontologies = new HashMap();

  /**
     The <code>Behaviour</code> that is currently executing.
     @see jade.core.behaviours.Behaviour
     @serial
  */
  protected Behaviour currentBehaviour;

  /**
     Last message received.
     @see jade.lang.acl.ACLMessage
     @serial
  */
  protected ACLMessage currentMessage;

  // This variable is 'volatile' because is used as a latch to signal
  // agent suspension and termination from outside world.
  /**
  @serial
  */
  private volatile int myAPState;

  // These two variables are used as temporary buffers for
  // mobility-related parameters
  private transient Location myDestination;
  private transient String myNewName;

  // Temporary buffer for agent suspension
  /**
  @serial
  */
  private int myBufferedState = AP_MIN;

  /**
  @serial
  */
  private List blockedBehaviours = new ArrayList();

  /**
     Default constructor.
  */
  public Agent() {
    myAPState = AP_INITIATED;
    myScheduler = new Scheduler(this);
  }

  /**
     Method to query the agent local name.
     @return A <code>String</code> containing the local agent name
     (e.g. <em>peter</em>).
  */
  public final String getLocalName() {
    return myName;
  }

  /**
     Method to query the agent complete name (<em><b>GUID</b></em>).
     @return A <code>String</code> containing the complete agent name
     (e.g. <em>peter@fipa.org:50</em>).
  */
  public final String getName() {
    return myName + '@' + myHap;
  }

  /**
     Method to query the private Agent ID. Note that this Agent ID is
     <b>different</b> from the one that is registered with the
     platform AMS.
     @return An <code>Agent ID</code> object, containing the complete
     agent GUID, addresses and resolvers.
  */
  public final AID getAID() {
    return myAID;
  }

  /**
     Method to query the agent home address. This is the address of
     the platform where the agent was created, and will never change
     during the whole lifetime of the agent.

     @return A <code>String</code> containing the agent home address
     (e.g. <em>iiop://fipa.org:50/acc</em>).
  */
  public final String getHap() {
    return myHap;
  }

  static void initReservedAIDs(AID amsID, AID defaultDfID) {
    AMS = amsID;
    DEFAULT_DF = defaultDfID;
  }

  /**
     Adds a Content Language codec to the agent capabilities. When an
     agent wants to provide automatic support for a specific content
     language, it must use an implementation of the <code>Codec</code>
     interface for the specific content language, and add it to its
     languages table with this method.
     @param languageName The symbolic name to use for the language.
     @param translator A translator for the specific content language,
     able to translate back and forth between text strings and Frame
     objects.
     @see jade.core.Agent#deregisterLanguage(String languageName)
     @see jade.lang.Codec
  */
  public void registerLanguage(String languageName, Codec translator) {
    languages.put(new Name(languageName), translator);
  }


  /**
     Looks a content language up into the supported languages table.
     @param languageName The name of the desired content language.
     @return The translator for the given language, or
     <code>null</code> if no translator was found.
   */
  public Codec lookupLanguage(String languageName) {
    return (Codec)languages.get(new Name(languageName));
  }

  /**
     Removes a Content Language from the agent capabilities.
     @param languageName The name of the language to remove.
     @see jade.core.Agent#registerLanguage(String languageName, Codec translator)
   */
  public void deregisterLanguage(String languageName) {
    languages.remove(new Name(languageName));
  }

  /**
     Adds an Ontology to the agent capabilities. When an agent wants
     to provide automatic support for a specific ontology, it must use
     an implementation of the <code>Ontology</code> interface for the
     specific ontology and add it to its ontologies table with this
     method.
     @param ontologyName The symbolic name to use for the ontology
     @param o An ontology object, that is able to convert back and
     forth between Frame objects and application specific Java objects
     representing concepts.
     @see jade.core.Agent#deregisterOntology(String ontologyName)
     @see jade.onto.Ontology
   */
  public void registerOntology(String ontologyName, Ontology o) {
    ontologies.put(new Name(ontologyName), o);
  }

  /**
     Looks an ontology up into the supported ontologies table.
     @param ontologyName The name of the desired ontology.
     @return The given ontology, or <code>null</code> if no such named
     ontology was found.
   */
  public Ontology lookupOntology(String ontologyName) {
    return (Ontology)ontologies.get(new Name(ontologyName));
  }

  /**
     Removes an Ontology from the agent capabilities.
     @param ontologyName The name of the ontology to remove.
     @see jade.core.Agent#registerOntology(String ontologyName, Ontology o)
   */
  public void deregisterOntology(String ontologyName) {
    ontologies.remove(new Name(ontologyName));
  }

  /**
     Builds a Java object out of an ACL message. This method uses the
     <code>:language</code> slot to select a content language and the
     <code>:ontology</code> slot to select an ontology. Then the
     <code>:content</code> slot is interpreted according to the chosen
     language and ontology, to build an object of a user defined class.
     @param msg The ACL message from which a suitable Java object will
     be built.
     @return A new list of Java objects, each object representing an element
     of the t-uple of the the message content in the
     given content language and ontology.
     @exception jade.domain.FIPAException If some problem related to
     the content language or to the ontology is detected.
     @see jade.core.Agent#registerLanguage(String languageName, Codec translator)
     @see jade.core.Agent#registerOntology(String ontologyName, Ontology o)
     @see jade.core.Agent#fillContent(ACLMessage msg, Object content)
   */
  public List extractContent(ACLMessage msg) throws FIPAException {
    Codec c = lookupLanguage(msg.getLanguage());
    if(c == null)
      throw new FIPAException("Unknown Content Language");
    Ontology o = lookupOntology(msg.getOntology());
    if(o == null)
      throw new FIPAException("Unknown Ontology");
    try {
      List tuple = c.decode(msg.getContent(), o);
      return o.createObject(tuple);
    }
    catch(Codec.CodecException cce) {
      // cce.printStackTrace();
      throw new FIPAException("Codec error: " + cce.getMessage());
    }
    catch(OntologyException oe) {
      // oe.printStackTrace();
      throw new FIPAException("Ontology error: " + oe.getMessage());
    }

  }

  /**
    Fills the <code>:content</code> slot of an ACL message with the string
    representation of a t-uple of user defined ontological objects. Each 
    Java object in the given list
    is first converted into a <code>Frame</code> object according to the
    ontology present in the <code>:ontology</code> message slot, then the
    <code>Frame</code> is translated into a <code>String</code> using the codec
    for the content language indicated by the <code>:language</code> message
    slot.
    <p>
    Notice that this method works properly only if in the Ontology each
    Java class has been registered to play just one role, otherwise
    ambiguity of role playing cannot be solved automatically.
    @param msg The ACL message whose content will be filled.
    @param content A list of Java objects that will be converted into a string and
    written inti the <code>:content</code> slot. This object must be an instance
    of a class registered into the ontology named in the <code>:ontology</code>
    message slot.
    @exception jade.domain.FIPAException This exception is thrown if the
    <code>:language</code> or <code>:ontology</code> message slots contain an
    unknown name, or if some problem occurs during the various translation steps.
    @see jade.core.Agent#extractContent(ACLMessage msg)
    @see jade.core.Agent#registerLanguage(String languageName, Codec translator)
    @see jade.core.Agent#registerOntology(String ontologyName, Ontology o)
   */
  public void fillContent(ACLMessage msg, List content) throws FIPAException {
    Codec c = lookupLanguage(msg.getLanguage());
    if(c == null)
      throw new FIPAException("Unknown Content Language");
    Ontology o = lookupOntology(msg.getOntology());
    if(o == null)
      throw new FIPAException("Unknown Ontology");
    try {
      List l = new ArrayList();
      Frame f;
      for (int i=0; i<content.size(); i++) {
	f = o.createFrame(content.get(i), o.getRoleName(content.get(i).getClass()));
	l.add(f);
      }
      String s = c.encode(l, o);
      msg.setContent(s);
    }
    catch(OntologyException oe) {
      oe.printStackTrace();
      throw new FIPAException("Ontology error: " + oe.getMessage());
    }

  }


  // This is used by the agent container to wait for agent termination
  void join() {
    try {
      myThread.join();
    }
    catch(InterruptedException ie) {
      ie.printStackTrace();
    }

  }

  /**
     Set message queue size. This method allows to change the number
     of ACL messages that can be buffered before being actually read
     by the agent or discarded.
     @param newSize A non negative integer value to set message queue
     size to. Passing 0 means unlimited message queue.
     @throws IllegalArgumentException If <code>newSize</code> is negative.
     @see jade.core.Agent#getQueueSize()
  */
  public void setQueueSize(int newSize) throws IllegalArgumentException {
    msgQueue.setMaxSize(newSize);
  }

  /**
     Reads message queue size. A zero value means that the message
     queue is unbounded (its size is limited only by amount of
     available memory).
     @return The actual size of the message queue.
     @see jade.core.Agent#setQueueSize(int newSize)
  */
  public int getQueueSize() {
    return msgQueue.getMaxSize();
  }

  /**
     Read current agent state. This method can be used to query an
     agent for its state from the outside.
     @return the Agent Platform Life Cycle state this agent is currently in.
   */
  public int getState() {
    int state;
    synchronized(stateLock) {
      state = myAPState;
    }
    return state;
  }

  // State transition methods for Agent Platform Life-Cycle

  /**
     Make a state transition from <em>initiated</em> to
     <em>active</em> within Agent Platform Life Cycle. Agents are
     started automatically by JADE on agent creation and should not be
     used by application developers, unless creating some kind of
     agent factory. This method starts the embedded thread of the agent.
     @param name The local name of the agent.
  */
  public void doStart(String name) {
    AgentContainerImpl thisContainer = Starter.getContainer();
    try {
      thisContainer.initAgent(name, this, AgentContainer.START);
    }
    catch(java.rmi.RemoteException jrre) {
      jrre.printStackTrace();
    }
  }

  /**
     Make a state transition from <em>active</em> to
     <em>transit</em> within Agent Platform Life Cycle. This method
     is intended to support agent mobility and is called either by the
     Agent Platform or by the agent itself to start a migration process.
     @param destination The <code>Location</code> to migrate to.
  */
  public void doMove(Location destination) {
    synchronized(stateLock) {
      if((myAPState == AP_ACTIVE)||(myAPState == AP_WAITING)) {
	myBufferedState = myAPState;
	myAPState = AP_TRANSIT;
	myDestination = destination;

	// Real action will be executed in the embedded thread
	if(!myThread.equals(Thread.currentThread()))
	  myThread.interrupt();
      }
    }
    
  }

  /**
     Make a state transition from <em>active</em> to
     <em>copy</em> within Agent Platform Life Cycle. This method
     is intended to support agent mobility and is called either by the
     Agent Platform or by the agent itself to start a clonation process.
     @param destination The <code>Location</code> where the copy agent will start.
     @param newName The name that will be given to the copy agent.
  */
  public void doClone(Location destination, String newName) {
    synchronized(stateLock) {
      if((myAPState == AP_ACTIVE)||(myAPState == AP_WAITING)) {
	myBufferedState = myAPState;
	myAPState = AP_COPY;
	myDestination = destination;
	myNewName = newName;

	// Real action will be executed in the embedded thread
	if(!myThread.equals(Thread.currentThread()))
	  myThread.interrupt();
      }
    }
  }

  /**
     Make a state transition from <em>transit</em> or
     <code>copy</code> to <em>active</em> within Agent Platform Life
     Cycle. This method is intended to support agent mobility and is
     called by the destination Agent Platform when a migration process
     completes and the mobile agent is about to be restarted on its
     new location.
  */
  void doExecute() {
    synchronized(stateLock) {
      myAPState = myBufferedState;
      myBufferedState = AP_MIN;
      activateAllBehaviours();
    }
  }

  /**
     Make a state transition from <em>transit</em> to <em>gone</em>
     state. This state is only used to label the original copy of a
     mobile agent which migrated somewhere.
  */
  void doGone() {
    synchronized(stateLock) {
      myAPState = AP_GONE;
    }
  }

  /**
     Make a state transition from <em>active</em> or <em>waiting</em>
     to <em>suspended</em> within Agent Platform Life Cycle; the
     original agent state is saved and will be restored by a
     <code>doActivate()</code> call. This method can be called from
     the Agent Platform or from the agent iself and stops all agent
     activities. Incoming messages for a suspended agent are buffered
     by the Agent Platform and are delivered as soon as the agent
     resumes. Calling <code>doSuspend()</code> on a suspended agent
     has no effect.
     @see jade.core.Agent#doActivate()
  */
  public void doSuspend() {
    synchronized(stateLock) {
      if((myAPState == AP_ACTIVE)||(myAPState == AP_WAITING)) {
	myBufferedState = myAPState;
	myAPState = AP_SUSPENDED;
      }
    }
    if(myAPState == AP_SUSPENDED) {
      if(myThread.equals(Thread.currentThread())) {
	waitUntilActivate();
      }
    }
  }

  /**
     Make a state transition from <em>suspended</em> to
     <em>active</em> or <em>waiting</em> (whichever state the agent
     was in when <code>doSuspend()</code> was called) within Agent
     Platform Life Cycle. This method is called from the Agent
     Platform and resumes agent execution. Calling
     <code>doActivate()</code> when the agent is not suspended has no
     effect.
     @see jade.core.Agent#doSuspend()
  */
  public void doActivate() {
    synchronized(stateLock) {
      if(myAPState == AP_SUSPENDED) {
	myAPState = myBufferedState;
      }
    }
    if(myAPState != AP_SUSPENDED) {
      switch(myBufferedState) {
      case AP_ACTIVE:
	activateAllBehaviours();
	synchronized(suspendLock) {
	  myBufferedState = AP_MIN;
	  suspendLock.notifyAll();
	}
	break;
      case AP_WAITING:
	doWake();
	break;
      }
    }
  }

  /**
     Make a state transition from <em>active</em> to <em>waiting</em>
     within Agent Platform Life Cycle. This method can be called by
     the Agent Platform or by the agent itself and causes the agent to
     block, stopping all its activities until some event happens. A
     waiting agent wakes up as soon as a message arrives or when
     <code>doWake()</code> is called. Calling <code>doWait()</code> on
     a suspended or waiting agent has no effect.
     @see jade.core.Agent#doWake()
  */
  public void doWait() {
    doWait(0);
  }

  /**
     Make a state transition from <em>active</em> to <em>waiting</em>
     within Agent Platform Life Cycle. This method adds a timeout to
     the other <code>doWait()</code> version.
     @param millis The timeout value, in milliseconds.
     @see jade.core.Agent#doWait()
  */
  public void doWait(long millis) {
    synchronized(stateLock) {
      if(myAPState == AP_ACTIVE)
	myAPState = AP_WAITING;
    }
    if(myAPState == AP_WAITING) {
      if(myThread.equals(Thread.currentThread())) {
	waitUntilWake(millis);
      }
    }
  }

  /**
     Make a state transition from <em>waiting</em> to <em>active</em>
     within Agent Platform Life Cycle. This method is called from
     Agent Platform and resumes agent execution. Calling
     <code>doWake()</code> when an agent is not waiting has no effect.
     @see jade.core.Agent#doWait()
  */
  public void doWake() {
    synchronized(stateLock) {
      if(myAPState == AP_WAITING) {
	myAPState = AP_ACTIVE;
      }
    }
    if(myAPState == AP_ACTIVE) {
      activateAllBehaviours();
      synchronized(waitLock) {
        waitLock.notifyAll(); // Wakes up the embedded thread
      }
    }
  }

  // This method handles both the case when the agents decides to exit
  // and the case in which someone else kills him from outside.

  /**
     Make a state transition from <em>active</em>, <em>suspended</em>
     or <em>waiting</em> to <em>deleted</em> state within Agent
     Platform Life Cycle, thereby destroying the agent. This method
     can be called either from the Agent Platform or from the agent
     itself. Calling <code>doDelete()</code> on an already deleted
     agent has no effect.
  */
  public void doDelete() {
    synchronized(stateLock) {
      if(myAPState != AP_DELETED) {
	myAPState = AP_DELETED;
	if(!myThread.equals(Thread.currentThread()))
	  myThread.interrupt();
      }
    }
  }

  /**
     Write this agent to an output stream; this method can be used to
     record a snapshot of the agent state on a file or to send it
     through a network connection. Of course, the whole agent must
     be serializable in order to be written successfully.
     @param s The stream this agent will be sent to. The stream is
     <em>not</em> closed on exit.
     @exception IOException Thrown if some I/O error occurs during
     writing.
     @see jade.core.Agent#read(InputStream s)
  */
  public void write(OutputStream s) throws IOException {
    ObjectOutput out = new ObjectOutputStream(s);
    out.writeUTF(myName);
    out.writeObject(this);
  }

  /**
     Read a previously saved agent from an input stream and restarts
     it under its former name. This method can realize some sort of
     mobility through time, where an agent is saved, then destroyed
     and then restarted from the saved copy.
     @param s The stream the agent will be read from. The stream is
     <em>not</em> closed on exit.
     @exception IOException Thrown if some I/O error occurs during
     stream reading.
     @see jade.core.Agent#write(OutputStream s)
  */
  public static void read(InputStream s) throws IOException {
    try {
      ObjectInput in = new ObjectInputStream(s);
      String name = in.readUTF();
      Agent a = (Agent)in.readObject();
      a.doStart(name);
    }
    catch(ClassNotFoundException cnfe) {
      cnfe.printStackTrace();
    }
  }

  /**
     Read a previously saved agent from an input stream and restarts
     it under a different name. This method can realize agent cloning
     through streams, where an agent is saved, then an exact copy of
     it is restarted as a completely separated agent, with the same
     state but with different identity and address.
     @param s The stream the agent will be read from. The stream is
     <em>not</em> closed on exit.
     @param agentName The name of the new agent, copy of the saved
     original one.
     @exception IOException Thrown if some I/O error occurs during
     stream reading.
     @see jade.core.Agent#write(OutputStream s)
  */
  public static void read(InputStream s, String agentName) throws IOException {
    try {
      ObjectInput in = new ObjectInputStream(s);
      String name = in.readUTF();
      Agent a = (Agent)in.readObject();
      a.doStart(agentName);
    }
    catch(ClassNotFoundException cnfe) {
      cnfe.printStackTrace();
    }
  }

  /**
     This method reads a previously saved agent, replacing the current
     state of this agent with the one previously saved. The stream
     must contain the saved state of <b>the same agent</b> that it is
     trying to restore itself; that is, <em>both</em> the Java object
     <em>and</em> the agent name must be the same.
     @param s The input stream the agent state will be read from.
     @exception IOException Thrown if some I/O error occurs during
     stream reading.
     <em>Note: This method is currently not implemented</em>
  */
  public void restore(InputStream s) throws IOException {
    // FIXME: Not implemented
  }

  /**
     This method is the main body of every agent. It can handle
     automatically <b>AMS</b> registration and deregistration and
     provides startup and cleanup hooks for application programmers to
     put their specific code into.
     @see jade.core.Agent#setup()
     @see jade.core.Agent#takeDown()
  */
  public final void run() {

    try {

      switch(myAPState) {
      case AP_INITIATED:
	myAPState = AP_ACTIVE;
	// No 'break' statement - fall through
      case AP_ACTIVE:
	registerWithAMS(new AMSAgentDescription());
	setup();
	break;
      case AP_TRANSIT:
	doExecute();
	afterMove();
	break;
      case AP_COPY:
	doExecute();
	registerWithAMS(new AMSAgentDescription());
	afterClone();
	break;
      }

      mainLoop();

    }
    catch(InterruptedException ie) {
      // Do Nothing, since this is a killAgent from outside
    }
    catch(InterruptedIOException iioe) {
      // Do nothing, since this is a killAgent from outside
    }
    catch(Exception e) {
      System.err.println("***  Uncaught Exception for agent " + myName + "  ***");
      e.printStackTrace();
    }
    catch(AgentDeathError ade) {
      // Do Nothing, since this is a killAgent from outside
    }
    finally {
      switch(myAPState) {
      case AP_DELETED:
	int savedState = getState();
	myAPState = AP_ACTIVE;
	takeDown();
	destroy();
	myAPState = savedState;
	break;
      case AP_GONE:
	break;
      default:
	System.out.println("ERROR: Agent " + myName + " died without being properly terminated !!!");
	System.out.println("State was " + myAPState);
	savedState = getState();
	myAPState = AP_ACTIVE;
	takeDown();
	destroy();
	myAPState = savedState;
      }
    }

  }

  /**
     This protected method is an empty placeholder for application
     specific startup code. Agent developers can override it to
     provide necessary behaviour. When this method is called the agent
     has been already registered with the Agent Platform <b>AMS</b>
     and is able to send and receive messages. However, the agent
     execution model is still sequential and no behaviour scheduling
     is active yet.

     This method can be used for ordinary startup tasks such as
     <b>DF</b> registration, but is essential to add at least a
     <code>Behaviour</code> object to the agent, in order for it to be
     able to do anything.
     @see jade.core.Agent#addBehaviour(Behaviour b)
     @see jade.core.behaviours.Behaviour
  */
  protected void setup() {}

  /**
     This protected method is an empty placeholder for application
     specific cleanup code. Agent developers can override it to
     provide necessary behaviour. When this method is called the agent
     has not deregistered itself with the Agent Platform <b>AMS</b>
     and is still able to exchange messages with other
     agents. However, no behaviour scheduling is active anymore and
     the Agent Platform Life Cycle state is already set to
     <em>deleted</em>.

     This method can be used for ordinary cleanup tasks such as
     <b>DF</b> deregistration, but explicit removal of all agent
     behaviours is not needed.
  */
  protected void takeDown() {}

  /**
    Actions to perform before moving. This empty placeholder method can be
    overridden by user defined agents to execute some actions just before
    leaving an agent container for a migration.
  */
  protected void beforeMove() {}

  /**
    Actions to perform after moving. This empty placeholder method can be
    overridden by user defined agents to execute some actions just after
    arriving to the destination agent container for a migration.
  */
  protected void afterMove() {}

  /**
    Actions to perform before cloning. This empty placeholder method can be
    overridden by user defined agents to execute some actions just before
    copying an agent to another agent container.
  */
  protected void beforeClone() {}

  /**
    Actions to perform after cloning. This empty placeholder method can be
    overridden by user defined agents to execute some actions just after
    creating an agent copy to the destination agent container.
  */
  protected void afterClone() {}

  // This method is used by the Agent Container to fire up a new agent for the first time
  void powerUp(String name, String platformID, ThreadGroup myGroup) {

    // Set this agent's name and address and start its embedded thread
    if((myAPState == AP_INITIATED)||(myAPState == AP_TRANSIT)||(myAPState == AP_COPY)) {
      myName = name;
      myHap = platformID;
      myAID = new AID();
      myAID.setName(getName());
      myThread = new Thread(myGroup, this);    
      myThread.setName(getLocalName());
      myThread.setPriority(myGroup.getMaxPriority());
      myThread.start();
    }
  }

  private void writeObject(ObjectOutputStream out) throws IOException {
    out.defaultWriteObject();
  }

  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    in.defaultReadObject();

    // Restore transient fields (apart from myThread, which will be set by doStart())
    listeners = new ArrayList();
    stateLock = new Object();
    suspendLock = new Object();
    waitLock = new Object();
    pendingTimers = new AssociationTB();
    languages = new HashMap();
    ontologies = new HashMap();
  }

  private void mainLoop() throws InterruptedException, InterruptedIOException {
    while(myAPState != AP_DELETED) {
      try {

	// Check for Agent state changes
	switch(myAPState) {
	case AP_WAITING:
	  waitUntilWake(0);
	  break;
	case AP_SUSPENDED:
	  waitUntilActivate();
	  break;
	case AP_TRANSIT:
	  notifyMove();
	  if(myAPState == AP_GONE) {
	    beforeMove();
	    return;
	  }
	  break;
	case AP_COPY:
	  beforeClone();
	  notifyCopy();
	  doExecute();
	  break;
	case AP_ACTIVE:
	  try {
	    // Select the next behaviour to execute
	    currentBehaviour = myScheduler.schedule();
	  }
	  // Someone interrupted the agent. It could be a kill or a
	  // move/clone request...
	  catch(InterruptedException ie) {
	    switch(myAPState) {
	    case AP_DELETED:
	      throw new AgentDeathError();
	    case AP_TRANSIT:
	    case AP_COPY:
	      throw new AgentInMotionError();
	    }
	  }


	  // Remember how many messages arrived
	  int oldMsgCounter = messageCounter;

	  // Just do it!
	  currentBehaviour.action();

	  // If the current Behaviour is blocked and more messages
	  // arrived, restart the behaviour to give it another chance
	  if((oldMsgCounter != messageCounter) && (!currentBehaviour.isRunnable()))
	    currentBehaviour.restart();


	  // When it is needed no more, delete it from the behaviours queue
	  if(currentBehaviour.done()) {
	    myScheduler.remove(currentBehaviour);
	    currentBehaviour = null;
	  }
	  else if(!currentBehaviour.isRunnable()) {
	    // Remove blocked behaviours from scheduling queue and put it
	    // in blocked behaviours queue
	    myScheduler.remove(currentBehaviour);
	    blockedBehaviours.add(currentBehaviour);
	    currentBehaviour = null;
	  }
	  break;
	}

	// Now give CPU control to other agents
	Thread.yield();
      }
      catch(AgentInMotionError aime) {
	// Do nothing, since this is a doMove() or doClone() from the outside.
      }
    }

  }

  private void waitUntilWake(long millis) {
    synchronized(waitLock) {

      long timeToWait = millis;
      while(myAPState == AP_WAITING) {
	try {

	  long startTime = System.currentTimeMillis();
	  waitLock.wait(timeToWait); // Blocks on waiting state monitor for a while
	  long elapsedTime = System.currentTimeMillis() - startTime;

	  // If this was a timed wait, update time to wait; if the
	  // total time has passed, wake up.
	  if(millis != 0) {
	    timeToWait -= elapsedTime;

	    if(timeToWait <= 0)
	    myAPState = AP_ACTIVE;
	  }

	}
	catch(InterruptedException ie) {
	  switch(myAPState) {
	  case AP_DELETED:
	    throw new AgentDeathError();
	  case AP_TRANSIT:
	  case AP_COPY:
	    throw new AgentInMotionError();
	  }
	}
      }
    }
  }

  private void waitUntilActivate() {
    synchronized(suspendLock) {
      while(myAPState == AP_SUSPENDED) {
	try {
	  suspendLock.wait(); // Blocks on suspended state monitor
	}
	catch(InterruptedException ie) {
	  switch(myAPState) {
	  case AP_DELETED:
	    throw new AgentDeathError();
	  case AP_TRANSIT:
	  case AP_COPY:
	    // Undo the previous clone or move request
	    myAPState = AP_SUSPENDED;
	  }
	}
      }
    }
  }

  private void destroy() { 

    try {
      deregisterWithAMS();
    }
    catch(FIPAException fe) {
      fe.printStackTrace();
    }
    notifyDestruction();
  }

  /**
     This method adds a new behaviour to the agent. This behaviour
     will be executed concurrently with all the others, using a
     cooperative round robin scheduling.  This method is typically
     called from an agent <code>setup()</code> to fire off some
     initial behaviour, but can also be used to spawn new behaviours
     dynamically.
     @param b The new behaviour to add to the agent.
     @see jade.core.Agent#setup()
     @see jade.core.behaviours.Behaviour
  */
  public void addBehaviour(Behaviour b) {
    myScheduler.add(b);
  }

  /**
     This method removes a given behaviour from the agent. This method
     is called automatically when a top level behaviour terminates,
     but can also be called from a behaviour to terminate itself or
     some other behaviour.
     @param b The behaviour to remove.
     @see jade.core.behaviours.Behaviour
  */
  public void removeBehaviour(Behaviour b) {
    myScheduler.remove(b);
  }


  /**
     Send an <b>ACL</b> message to another agent. This methods sends
     a message to the agent specified in <code>:receiver</code>
     message field (more than one agent can be specified as message
     receiver).
     @param msg An ACL message object containing the actual message to
     send.
     @see jade.lang.acl.ACLMessage
  */
  public final void send(ACLMessage msg) {
    try {
      if(msg.getSender().getName().length() < 1)
	msg.setSender(myAID);
    } catch (NullPointerException e) {
	msg.setSender(myAID);
    }
    CommEvent event = new CommEvent(this, msg);
    broadcastEvent(event);
  }

  /**
     Receives an <b>ACL</b> message from the agent message
     queue. This method is non-blocking and returns the first message
     in the queue, if any. Therefore, polling and busy waiting is
     required to wait for the next message sent using this method.
     @return A new ACL message, or <code>null</code> if no message is
     present.
     @see jade.lang.acl.ACLMessage
  */
  public final ACLMessage receive() {
    synchronized(waitLock) {
      if(msgQueue.isEmpty()) {
	return null;
      }
      else {
	currentMessage = msgQueue.removeFirst();
	return currentMessage;
      }
    }
  }

  /**
     Receives an <b>ACL</b> message matching a given template. This
     method is non-blocking and returns the first matching message in
     the queue, if any. Therefore, polling and busy waiting is
     required to wait for a specific kind of message using this method.
     @param pattern A message template to match received messages
     against.
     @return A new ACL message matching the given template, or
     <code>null</code> if no such message is present.
     @see jade.lang.acl.ACLMessage
     @see jade.lang.acl.MessageTemplate
  */
  public final ACLMessage receive(MessageTemplate pattern) {
    ACLMessage msg = null;
    synchronized(waitLock) {
      Iterator messages = msgQueue.iterator();

      while(messages.hasNext()) {
	ACLMessage cursor = (ACLMessage)messages.next();
	if(pattern.match(cursor)) {
	  msg = cursor;
	  msgQueue.remove(cursor);
	  currentMessage = cursor;
	  break; // Exit while loop
	}
      }
    }

    return msg;
  }

  /**
     Receives an <b>ACL</b> message from the agent message
     queue. This method is blocking and suspends the whole agent until
     a message is available in the queue. JADE provides a special
     behaviour named <code>ReceiverBehaviour</code> to wait for a
     message within a behaviour without suspending all the others and
     without wasting CPU time doing busy waiting.
     @return A new ACL message, blocking the agent until one is
     available.
     @see jade.lang.acl.ACLMessage
     @see jade.core.behaviours.ReceiverBehaviour
  */
  public final ACLMessage blockingReceive() {
    ACLMessage msg = null;
    while(msg == null) {
      msg = blockingReceive(0);
    }
    return msg;
  }

  /**
     Receives an <b>ACL</b> message from the agent message queue,
     waiting at most a specified amount of time.
     @param millis The maximum amount of time to wait for the message.
     @return A new ACL message, or <code>null</code> if the specified
     amount of time passes without any message reception.
   */
  public final ACLMessage blockingReceive(long millis) {
    synchronized(waitLock) {
      ACLMessage msg = receive();
      if(msg == null) {
	doWait(millis);
	msg = receive();
      }
      return msg;
    }
  }

  /**
     Receives an <b>ACL</b> message matching a given message
     template. This method is blocking and suspends the whole agent
     until a message is available in the queue. JADE provides a
     special behaviour named <code>ReceiverBehaviour</code> to wait
     for a specific kind of message within a behaviour without
     suspending all the others and without wasting CPU time doing busy
     waiting.
     @param pattern A message template to match received messages
     against.
     @return A new ACL message matching the given template, blocking
     until such a message is available.
     @see jade.lang.acl.ACLMessage
     @see jade.lang.acl.MessageTemplate
     @see jade.core.behaviours.ReceiverBehaviour
  */
  public final ACLMessage blockingReceive(MessageTemplate pattern) {
    ACLMessage msg = null;
    while(msg == null) {
      msg = blockingReceive(pattern, 0);
    }
    return msg;
  }


  /**
     Receives an <b>ACL</b> message matching a given message template,
     waiting at most a specified time.
     @param pattern A message template to match received messages
     against.
     @param millis The amount of time to wait for the message, in
     milliseconds.
     @return A new ACL message matching the given template, or
     <code>null</code> if no suitable message was received within
     <code>millis</code> milliseconds.
     @see jade.core.Agent#blockingReceive()
  */
  public final ACLMessage blockingReceive(MessageTemplate pattern, long millis) {
    ACLMessage msg = null;
    synchronized(waitLock) {
      msg = receive(pattern);
      long timeToWait = millis;
      while(msg == null) {
	long startTime = System.currentTimeMillis();
	doWait(timeToWait);
	long elapsedTime = System.currentTimeMillis() - startTime;

	msg = receive(pattern);

	if(millis != 0) {
	  timeToWait -= elapsedTime;
	  if(timeToWait <= 0)
	    break;
	}

      }
    }
    return msg;
  }

  /**
     Puts a received <b>ACL</b> message back into the message
     queue. This method can be used from an agent behaviour when it
     realizes it read a message of interest for some other
     behaviour. The message is put in front of the message queue, so
     it will be the first returned by a <code>receive()</code> call.
     @see jade.core.Agent#receive()
  */
  public final void putBack(ACLMessage msg) {
    synchronized(waitLock) {
      msgQueue.addFirst(msg);
    }
  }

  private ACLMessage FipaRequestMessage(AID dest, String replyString) {
    ACLMessage request = new ACLMessage(ACLMessage.REQUEST);

    request.setSender(myAID);
    request.clearAllReceiver();
    request.addReceiver(dest);
    request.setLanguage("SL0");
    request.setOntology("fipa-agent-management");
    request.setProtocol("fipa-request");
    request.setReplyWith(replyString);

    return request;
  }

  private String doFipaRequestClient(ACLMessage request, String replyString) throws FIPAException {

    send(request);
    ACLMessage reply = blockingReceive(MessageTemplate.MatchInReplyTo(replyString));
    if(reply.getPerformative() == ACLMessage.AGREE) {
      reply =  blockingReceive(MessageTemplate.MatchInReplyTo(replyString));
      if(reply.getPerformative() != ACLMessage.INFORM) {
	String content = reply.getContent();
	throw new FIPAException(content);
      }
      else {
	String content = reply.getContent();
	return content;
      }
    }
    else {
      String content = reply.getContent();
      throw new FIPAException(content);
    }

  }


  /**
     Register this agent with Agent Platform <b>AMS</b>. While this
     task can be accomplished with regular message passing according
     to <b>FIPA</b> protocols, this method is meant to ease this
     common duty. However, since <b>AMS</b> registration and
     deregistration are automatic in JADE, this method should not be
     used by application programmers.
     Some parameters here are optional, and <code>null</code> can
     safely be passed for them.
     @param signature An optional signature string, used for security reasons.
     @param APState The Agent Platform state of the agent; must be a
     valid state value (typically, <code>Agent.AP_ACTIVE</code>
     constant is passed).
     @param delegateAgent An optional delegate agent name.
     @param forwardAddress An optional forward address.
     @param ownership An optional ownership string.
     @exception FIPAException A suitable exception can be thrown when
     a <code>refuse</code> or <code>failure</code> messages are
     received from the AMS to indicate some error condition.
  */
  public void registerWithAMS(AMSAgentDescription amsd) throws FIPAException {

    String replyString = myName + "-ams-registration-" + (new Date()).getTime();
    ACLMessage request = FipaRequestMessage(AMS, replyString);

    // Build an AMS action object for the request
    Register a = new Register();

    // Use the agent name to fill in an AID to put in the :name slot
    AID myID = new AID();
    myID.setName(getName());
    amsd.setName(myID);
    a.set_0(amsd);

    // Convert action to String and write it in the :content slot of the request
    fillContent(request, new ArrayList());

    // Send message and collect reply
    doFipaRequestClient(request, replyString);

  }

  /**
     Deregister this agent with Agent Platform <b>AMS</b>. While this
     task can be accomplished with regular message passing according
     to <b>FIPA</b> protocols, this method is meant to ease this
     common duty. However, since <b>AMS</b> registration and
     deregistration are automatic in JADE, this method should not be
     used by application programmers.
     @exception FIPAException A suitable exception can be thrown when
     a <code>refuse</code> or <code>failure</code> messages are
     received from the AMS to indicate some error condition.
  */
  public void deregisterWithAMS() throws FIPAException {

    String replyString = myName + "-ams-deregistration-" + (new Date()).getTime();

    // Get a semi-complete request message
    ACLMessage request = FipaRequestMessage(AMS, replyString);

    // Build an AMS action object for the request
    Deregister a = new Deregister();

    // Use the agent name to fill in an AID to put in the :name slot
    AID myID = new AID();
    myID.setName(getName());

    AMSAgentDescription amsd = new AMSAgentDescription();
    amsd.setName(myID);
    a.set_0(amsd);

    // Convert it to a String and write it in content field of the request
    fillContent(request, new ArrayList());

    // Send message and collect reply
    doFipaRequestClient(request, replyString);

  }

  /**
     Modifies the data about this agent kept by Agent Platform
     <b>AMS</b>. While this task can be accomplished with regular
     message passing according to <b>FIPA</b> protocols, this method
     is meant to ease this common duty. Some parameters here are
     optional, and <code>null</code> can safely be passed for them.
     When a non null parameter is passed, it replaces the value
     currently stored inside <b>AMS</b> agent.
     @param signature An optional signature string, used for security reasons.
     @param APState The Agent Platform state of the agent; must be a
     valid state value (typically, <code>Agent.AP_ACTIVE</code>
     constant is passed).
     @param delegateAgent An optional delegate agent name.
     @param forwardAddress An optional forward address.
     @param ownership An optional ownership string.
     @exception FIPAException A suitable exception can be thrown when
     a <code>refuse</code> or <code>failure</code> messages are
     received from the AMS to indicate some error condition.
  */
  public void modifyAMSData(AMSAgentDescription amsd) throws FIPAException {

    String replyString = myName + "-ams-modify-" + (new Date()).getTime();
    ACLMessage request = FipaRequestMessage(AMS, replyString);

    // Build an AMS action object for the request
    Modify a = new Modify();
    a.set_0(amsd);

    // Convert it to a String and write it in content field of the request
    fillContent(request, new ArrayList());

    // Send message and collect reply
    doFipaRequestClient(request, replyString);

  }

  /**
     Searches the AMS for data.
   */
  public void searchAMS(AMSAgentDescription amsd) throws FIPAException {

    String replyString = myName + "-ams-search-" + (new Date()).getTime();
    ACLMessage request = FipaRequestMessage(AMS, replyString);

    // Build an AMS action object for the request
    Search a = new Search();
    a.set_0(amsd);

    // Convert it to a String and write it in content field of the request
    fillContent(request, new ArrayList());

    // Send message and collect reply
    doFipaRequestClient(request, replyString);

  }

  /**
     Register this agent with a <b>DF</b> agent. While this task can
     be accomplished with regular message passing according to
     <b>FIPA</b> protocols, this method is meant to ease this common
     duty.
     @param dfName The GUID of the <b>DF</b> agent to register with.
     @param dfd A <code>DFAgentDescriptor</code> object containing all
     data necessary to the registration.
     @exception FIPAException A suitable exception can be thrown when
     a <code>refuse</code> or <code>failure</code> messages are
     received from the DF to indicate some error condition.
   */
  public void registerWithDF(AID dfName, DFAgentDescription dfd) throws FIPAException {

    String replyString = myName + "-df-register-" + (new Date()).getTime();
    ACLMessage request = FipaRequestMessage(dfName, replyString);

    // Build a DF action object for the request
    Register a = new Register();
    a.set_0(dfd);

    // Convert it to a String and write it in content field of the request
    fillContent(request, new ArrayList());

    // Send message and collect reply
    doFipaRequestClient(request, replyString);

  }

  /**
     Deregister this agent from a <b>DF</b> agent. While this task can
     be accomplished with regular message passing according to
     <b>FIPA</b> protocols, this method is meant to ease this common
     duty.
     @param dfName The GUID of the <b>DF</b> agent to deregister from.
     @param dfd A <code>DFAgentDescriptor</code> object containing all
     data necessary to the deregistration.
     @exception FIPAException A suitable exception can be thrown when
     a <code>refuse</code> or <code>failure</code> messages are
     received from the DF to indicate some error condition.
  */
  public void deregisterWithDF(AID dfName, DFAgentDescription dfd) throws FIPAException {

    String replyString = myName + "-df-deregister-" + (new Date()).getTime();
    ACLMessage request = FipaRequestMessage(dfName, replyString);

    // Build a DF action object for the request
    Deregister a = new Deregister();
    a.set_0(dfd);

    // Convert it to a String and write it in content field of the request
    fillContent(request, new ArrayList());

    // Send message and collect reply
    doFipaRequestClient(request, replyString);

  }

  /**
     Modifies data about this agent contained within a <b>DF</b>
     agent. While this task can be accomplished with regular message
     passing according to <b>FIPA</b> protocols, this method is
     meant to ease this common duty.
     @param dfName The GUID of the <b>DF</b> agent holding the data
     to be changed.
     @param dfd A <code>DFAgentDescriptor</code> object containing all
     new data values; every non null slot value replaces the
     corresponding value held inside the <b>DF</b> agent.
     @exception FIPAException A suitable exception can be thrown when
     a <code>refuse</code> or <code>failure</code> messages are
     received from the DF to indicate some error condition.
  */
  public void modifyDFData(AID dfName, DFAgentDescription dfd) throws FIPAException {

    String replyString = myName + "-df-modify-" + (new Date()).getTime();
    ACLMessage request = FipaRequestMessage(dfName, replyString);

    // Build a DF action object for the request
    Modify a = new Modify();
    a.set_0(dfd);

    // Convert it to a String and write it in content field of the request
    fillContent(request, new ArrayList());

    // Send message and collect reply
    doFipaRequestClient(request, replyString);

  }

  /**
     Searches for data contained within a <b>DF</b> agent. While
     this task can be accomplished with regular message passing
     according to <b>FIPA</b> protocols, this method is meant to
     ease this common duty. Nevertheless, a complete, powerful search
     interface is provided; search constraints can be given and
     recursive searches are possible. The only shortcoming is that
     this method blocks the whole agent until the search terminates. A
     special <code>SearchDFBehaviour</code> can be used to perform
     <b>DF</b> searches without blocking.
     @param dfName The GUID of the <b>DF</b> agent to start search from.
     @param dfd A <code>DFAgentDescriptor</code> object containing
     data to search for; this parameter is used as a template to match
     data against.
     @param constraints A <code>List</code> that must be filled with
     all <code>Constraint</code> objects to apply to the current
     search. This can be <code>null</code> if no search constraints
     are required.
     @return A <code>DFSearchResult</code> object containing all found
     <code>DFAgentDescriptor</code> objects matching the given
     descriptor, subject to given search constraints for search depth
     and result size.
     @exception FIPAException A suitable exception can be thrown when
     a <code>refuse</code> or <code>failure</code> messages are
     received from the DF to indicate some error condition.
  */
  public void searchDF(AID dfName, DFAgentDescription dfd, List constraints) throws FIPAException {

    String replyString = myName + "-df-search-" + (new Date()).getTime();
    ACLMessage request = FipaRequestMessage(dfName, replyString);

    // Build a DF action object for the request
    Search a = new Search();
    a.set_0(dfd);

    // Convert it to a String and write it in content field of the request
    fillContent(request, new ArrayList());

    // Send message and collect reply
    doFipaRequestClient(request, replyString);

  }


  // Event handling methods


  // Broadcast communication event to registered listeners
  private void broadcastEvent(CommEvent event) {
    synchronized(listeners) {
      Iterator i = listeners.iterator();
      while(i.hasNext()) {
	CommListener l = (CommListener)i.next();
	l.CommHandle(event);
      }
    }
  }

  // Register a new listener
  public final void addCommListener(CommListener l) {
    synchronized(listeners) {
      listeners.add(l);
    }
  }

  // Remove a registered listener
  public final void removeCommListener(CommListener l) {
    synchronized(listeners) {
      listeners.remove(l);
    }
  }

  // Notify listeners of the destruction of the current agent
  private void notifyDestruction() {
    synchronized(listeners) {
      Iterator i = listeners.iterator();
      while(i.hasNext()) {
	CommListener l = (CommListener)i.next();
	l.endSource(myName);
      }
    }
  }

  // Notify listeners of the need to move the current agent
  private void notifyMove() {
    synchronized(listeners) {
      Iterator i = listeners.iterator();
      while(i.hasNext()) {
	CommListener l = (CommListener)i.next();
	l.moveSource(myName, myDestination);
      }
    }
  }

  // Notify listeners of the need to copy the current agent
  private void notifyCopy() {
    synchronized(listeners) {
      Iterator i = listeners.iterator();
      while(i.hasNext()) {
	CommListener l = (CommListener)i.next();
	l.copySource(myName, myDestination, myNewName);
      }
    }
  }

  private void activateBehaviour(Behaviour b) {
    Behaviour root = b.root();
    blockedBehaviours.remove(root);
    b.restart();
    myScheduler.add(root);
  }

  private void activateAllBehaviours() {
    // Put all blocked behaviours back in ready queue,
    // atomically with respect to the Scheduler object
    synchronized(myScheduler) {
      while(!blockedBehaviours.isEmpty()) {
	Behaviour b = (Behaviour)blockedBehaviours.remove(blockedBehaviours.size() - 1);
	b.restart();
	myScheduler.add(b);
      }
    }
  }


  /**
     Put a received message into the agent message queue. The message
     is put at the back end of the queue. This method is called by
     JADE runtime system when a message arrives, but can also be used
     by an agent, and is just the same as sending a message to oneself
     (though slightly faster).
     @param msg The ACL message to put in the queue.
     @see jade.core.Agent#send(ACLMessage msg)
  */
  public final void postMessage (ACLMessage msg) {
    synchronized(waitLock) {
      /*
      try {
	java.io.FileWriter f = new java.io.FileWriter("logs/" + getLocalName(), true);
	f.write("waitLock taken in postMessage() [thread " + Thread.currentThread().getName() + "]\n");
	msg.toText(f);
	f.close();
      }
      catch(java.io.IOException ioe) {
	  System.out.println(ioe.getMessage());
      }
      */

      if(msg != null) msgQueue.addLast(msg);
      doWake();
      messageCounter++;
    }

    /*
    try {
      java.io.FileWriter f = new java.io.FileWriter("logs/" + getLocalName(), true);
      f.write("waitLock dropped in postMessage() [thread " + Thread.currentThread().getName() + "]\n");
      f.close();
    }
    catch(java.io.IOException ioe) {
      System.out.println(ioe.getMessage());
    }
    */

  }

  Iterator messages() {
    return msgQueue.iterator();
  }

}
