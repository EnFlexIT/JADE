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

import jade.util.leap.Serializable;

import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

import java.io.IOException;
import java.io.InterruptedIOException;

import jade.util.leap.Iterator;
import jade.util.leap.Map;
import jade.util.leap.HashMap;

import jade.util.leap.List;
import jade.util.leap.ArrayList;
import java.util.Vector;

import jade.core.behaviours.Behaviour;

import jade.lang.Codec;
import jade.lang.acl.*;

import jade.onto.Frame;
import jade.onto.Ontology;
import jade.onto.OntologyException;

import jade.domain.AMSService;
// Concepts from fipa-agent-management ontology
import jade.domain.FIPAAgentManagement.AMSAgentDescription;

import jade.domain.FIPAException;

import jade.content.ContentManager;

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

public class Agent implements Runnable, Serializable {

  private final static short UNPROTECTMYPOINTER = 0;
  /**
  @serial
  */

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

    public synchronized Iterator timers() {
      return TtoB.keySet().iterator();
    }

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
      b.restart();
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

    // Did this restart() cause the root behaviour to become runnable ?
    // If so, put the root behaviour back into the ready queue.
    Behaviour root = b.root();
    if(root.isRunnable())
      myScheduler.restart(root);
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
     Represents the <em>idle</em> agent state.
  */
  public static final int AP_IDLE = 3;

  /**
     Represents the <em>suspended</em> agent state.
  */
  public static final int AP_SUSPENDED = 4;

  /**
     Represents the <em>waiting</em> agent state.
  */
  public static final int AP_WAITING = 5;

  /**
     Represents the <em>deleted</em> agent state.
  */
  public static final int AP_DELETED = 6;

  /**
     Represents the <code>transit</code> agent state.
  */
  public static final int AP_TRANSIT = 7;

  // Non compliant states, used internally. Maybe report to FIPA...
  /**
     Represents the <code>copy</code> agent state.
  */
  static final int AP_COPY = 8;

  /**
     Represents the <code>gone</code> agent state. This is the state
     the original instance of an agent goes into when a migration
     transaction successfully commits.
  */
  static final int AP_GONE = 9;

  /**
     Out of band value for Agent Platform Life Cycle states.
  */
  public static final int AP_MAX = 10;    // Hand-made type checking

  private static final AgentState[] STATES = new AgentState[] { 
    new AgentState("Illegal MIN state"),
    new AgentState("Initiated"),
    new AgentState("Active"),
    new AgentState("Idle"),
    new AgentState("Suspended"),
    new AgentState("Waiting"),
    new AgentState("Deleted"),
    new AgentState("Transit"),
    new AgentState("Copy"),
    new AgentState("Gone"),
    new AgentState("Illegal MAX state")
  };

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
     The Agent ID for the AMS of this platform.
   */
  static AID AMS;

  /**
     The Agent ID for the Default DF of this platform.
   */
  static AID DEFAULT_DF;

  /**
     Get the Agent ID for the platform AMS.
     @return An <code>AID</code> object, that can be used to contact
     the AMS of this platform.
  */
  public static final AID getAMS() {
    return AMS;
  }

  /**
     Get the Agent ID for the platform default DF.
     @return An <code>AID</code> object, that can be used to contact
     the default DF of this platform.
  */
  public static final AID getDefaultDF() {
    return DEFAULT_DF;
  }

  private int       msgQueueMaxSize = 0;
  private transient MessageQueue msgQueue = new MessageQueue(msgQueueMaxSize);
  private transient AgentToolkit myToolkit;

  /**
  @serial
  */
  private String myName = null;
  
  /**
  @serial
  */
  private AID myAID = null;

  /**
  @serial
  */
  private String myHap = null;

  private transient Object stateLock = new Object(); // Used to make state transitions atomic
  private transient Object waitLock = new Object();  // Used for agent waiting
  private transient Object suspendLock = new Object(); // Used for agent suspension

  private transient Thread myThread;
  private transient TimerDispatcher theDispatcher;

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


  // Individual agent capabilities
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
  
  /**
     This flag is used to distinguish the normal AP_ACTIVE state from
     the particular case in which the agent state is set to AP_ACTIVE
     during agent termination to allow it to deregister with the AMS. 
     In this case in fact a call to <code>doDelete()</code>, 
     <code>doMove()</code>, <code>doClone()</code> and <code>doSuspend()</code>
     should have no effect.
  */
  private boolean terminating = false;

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
     Default constructor.
  */
  public Agent() {
    setState(AP_INITIATED);
    myScheduler = new Scheduler(this);
    Runtime rt = Runtime.instance();
    theDispatcher = rt.getTimerDispatcher();
  }

  /**
  * This method must be overridden by programmers in order to pass 
  * arguments to the agent.
  * Otherwise, to pass argument to the agent by command line or using the RMA GUI
  * see the programmer's guide for a better documentation.
  *
  * @param args an array of string (as passed on the command line - Unix-like syntax).
  * @deprecated use the method <code>getArguments</code> instead
  */
    public void setArguments(String args[]) {}
  
    private transient Object[] arguments = null;  // array of arguments
    /**
     * Called by AgentContainerImpl in order to pass arguments to a
     * just created Agent.
     **/
    public final void setArguments(Object args[]) {
	// I have declared the method final otherwise getArguments would not work!
	arguments=args;
	if (arguments != null) { //FIXME. This code goes away with the depcreated setArguments(String[]) method
	    String sargs[] = new String[args.length];
	    for (int i=0; i<args.length; i++)
		sargs[i]=args[i].toString();
	    setArguments(sargs);
	}
    }

    /**
     * Return the array of arguments as they were set by the previous 
     * call of the method <code>setArguments</code>.
     * <p> Take care that the arguments are transient and they do not
     * migrate with the agent neither are cloned with the agent!
     **/
    protected Object[] getArguments() {
	return arguments;
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

  /**
     Method to retrieve the location this agent is currently at.
     @return A <code>Location</code> object, describing the location
     where this agent is currently running.
   */
  public Location here() {
    return myToolkit.here();
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
    languages.put(new CaseInsensitiveString(languageName), translator);
  }


  /**
     Looks a content language up into the supported languages table.
     @param languageName The name of the desired content language.
     @return The translator for the given language, or
     <code>null</code> if no translator was found.
   */
  public Codec lookupLanguage(String languageName) {
    Codec result = (Codec)languages.get(new CaseInsensitiveString(languageName));
    return result;
  }

  /**
     Removes a Content Language from the agent capabilities.
     @param languageName The name of the language to remove.
     @see jade.core.Agent#registerLanguage(String languageName, Codec translator)
   */
  public void deregisterLanguage(String languageName) {
    languages.remove(new CaseInsensitiveString(languageName));
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
    ontologies.put(new CaseInsensitiveString(ontologyName), o);
  }

  /**
     Looks an ontology up into the supported ontologies table.
     @param ontologyName The name of the desired ontology.
     @return The given ontology, or <code>null</code> if no such named
     ontology was found.
   */
  public Ontology lookupOntology(String ontologyName) {
    Ontology result = (Ontology)ontologies.get(new CaseInsensitiveString(ontologyName));
    return result;
  }

  /**
     Removes an Ontology from the agent capabilities.
     @param ontologyName The name of the ontology to remove.
     @see jade.core.Agent#registerOntology(String ontologyName, Ontology o)
   */
  public void deregisterOntology(String ontologyName) {
    ontologies.remove(new CaseInsensitiveString(ontologyName));
  }

  //__BACKWARD_COMPATIBILITY__BEGIN
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
     @see jade.core.Agent#fillContent(ACLMessage msg, java.util.List content)
		 @deprecated This support to message-content (both <code>fillContent</code> 
		 and <code>extractContent</code>) will not 
		 be ported into the CLDC-J2ME environment. In the long-term, it will be
		 replaced with the new message-content support implemented
		 by jade.content.ContentManager. In the short-term, 
		 <ul>
		 <li> in the J2SE environment this deprecated method can 
		 temporarily continue to be used
		 <li> in the PersonalJava environment the equivalent methods 
		 <code>fillMsgContent</code> and <code>extractMsgContent</code>
		 should be instead used that use
		 jade.util.leap.List instead of java.util.List, the latter being not supported in PersonalJava 
		 </ul>
   */
  public java.util.List extractContent(ACLMessage msg) throws FIPAException {
  	ArrayList l = (ArrayList) extractMsgContent(msg);
  	return l.toList();
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
		 @deprecated This support to message-content (both <code>fillContent</code> 
		 and <code>extractContent</code>) will not 
		 be ported into the CLDC-J2ME environment. In the long-term, it will be
		 replaced with the new message-content support implemented
		 by jade.content.ContentManager. In the short-term, 
		 <ul>
		 <li> in the J2SE environment this deprecated method can 
		 temporarily continue to be used
		 <li> in the PersonalJava environment the equivalent methods 
		 <code>fillMsgContent</code> and <code>extractMsgContent</code>
		 should be instead used that use
		 jade.util.leap.List instead of java.util.List, the latter being not supported in PersonalJava 
		 </ul>
   */
  public void fillContent(ACLMessage msg, java.util.List content) throws FIPAException {
    ArrayList l = new ArrayList();
    l.fromList(content);
    fillMsgContent(msg, l);
  }
  //__BACKWARD_COMPATIBILITY__END
  	

  /**
     Builds a Java object out of an ACL message. This method uses the
     <code>:language</code> slot to select a content language and the
     <code>:ontology</code> slot to select an ontology. Then the
     <code>:content</code> slot is interpreted according to the chosen
     language and ontology, to build an object of a user defined class.
     <br>
		 <i>This support to message-content (both <code>fillContent</code> 
		 and <code>extractContent</code>) will not 
		 be ported into the CLDC-J2ME environment. In the long-term, it will be
		 replaced with the new message-content support implemented
		 by jade.content.ContentManager. In the short-term, 
		 <ul>
		 <li> in the J2SE environment this deprecated method can 
		 temporarily continue to be used
		 <li> in the PersonalJava environment the equivalent methods 
		 <code>fillMsgContent</code> and <code>extractMsgContent</code>
		 should be instead used that use
		 jade.util.leap.List instead of java.util.List, the latter being not supported in PersonalJava 
		 </ul>
		 </i>
     @param msg The ACL message from which a suitable Java object will
     be built.
     @return A new list of Java objects, each object representing an element
     of the t-uple of the the message content in the
     given content language and ontology.
     @exception jade.domain.FIPAException If some problem related to
     the content language or to the ontology is detected.
     @see jade.core.Agent#registerLanguage(String languageName, Codec translator)
     @see jade.core.Agent#registerOntology(String ontologyName, Ontology o)
     @see jade.core.Agent#fillMsgContent(ACLMessage msg, List content)
   */
  public List extractMsgContent(ACLMessage msg) throws FIPAException {
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
      cce.getNested().printStackTrace();
      throw new FIPAException("Codec error: " + cce.getMessage());
    }
    catch(OntologyException oe) {
      oe.printStackTrace();
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
     <br>
		 <i>This support to message-content (both <code>fillContent</code> 
		 and <code>extractContent</code>) will not 
		 be ported into the CLDC-J2ME environment. In the long-term, it will be
		 replaced with the new message-content support implemented
		 by jade.content.ContentManager. In the short-term, 
		 <ul>
		 <li> in the J2SE environment this deprecated method can 
		 temporarily continue to be used
		 <li> in the PersonalJava environment the equivalent methods 
		 <code>fillMsgContent</code> and <code>extractMsgContent</code>
		 should be instead used that use
		 jade.util.leap.List instead of java.util.List, the latter being not supported in PersonalJava 
		 </ul>
		 </i>
    @param msg The ACL message whose content will be filled.
    @param content A list of Java objects that will be converted into a string and
    written inti the <code>:content</code> slot. This object must be an instance
    of a class registered into the ontology named in the <code>:ontology</code>
    message slot.
    @exception jade.domain.FIPAException This exception is thrown if the
    <code>:language</code> or <code>:ontology</code> message slots contain an
    unknown name, or if some problem occurs during the various translation steps.
    @see jade.core.Agent#extractMsgContent(ACLMessage msg)
    @see jade.core.Agent#registerLanguage(String languageName, Codec translator)
    @see jade.core.Agent#registerOntology(String ontologyName, Ontology o)
   */
  public void fillMsgContent(ACLMessage msg, List content) throws FIPAException {
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
     size to. Passing 0 means unlimited message queue.  When the number of 
     buffered
     messages exceeds this value, older messages are discarded
     according to a <b><em>FIFO</em></b> replacement policy.
     @throws IllegalArgumentException If <code>newSize</code> is negative.
     @see jade.core.Agent#getQueueSize()
  */
  public void setQueueSize(int newSize) throws IllegalArgumentException {
    msgQueue.setMaxSize(newSize);
    msgQueueMaxSize = newSize;
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

  private void setState(int state) {
    synchronized(stateLock) {
      int oldState = myAPState;
      myAPState = state;
      notifyChangedAgentState(oldState, myAPState);
    }
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

  AgentState getAgentState() {
    return STATES[getState()];
  }

  // State transition methods for Agent Platform Life-Cycle

  /**
     Make a state transition from <em>initiated</em> to
     <em>active</em> within Agent Platform Life Cycle. Agents are
     started automatically by JADE on agent creation and 
     this method should not be
     used by application developers, unless creating some kind of
     agent factory. This method starts the embedded thread of the agent.
     <b> It is highly descouraged the usage of this method </b> because it
     does not guarantee agent autonomy; soon this policy will
     be enfored by removing or restricting the scope of the method
     @param name The local name of the agent.
  */
  public void doStart(String name) {

    // FIXME: Temporary hack for JSP example
      if(myToolkit == null) {
	  Runtime rt = Runtime.instance();
	  setToolkit(rt.getDefaultToolkit());
	  theDispatcher = rt.getTimerDispatcher();
      }

    if(myToolkit == null)
      throw new InternalError("Trying to start an agent without proper runtime support.");
    myToolkit.handleStart(name, this);
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
      if(((myAPState == AP_ACTIVE)||(myAPState == AP_WAITING)||(myAPState == AP_IDLE)) && !terminating) {
	myBufferedState = myAPState;
	setState(AP_TRANSIT);
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
      if(((myAPState == AP_ACTIVE)||(myAPState == AP_WAITING)||(myAPState == AP_IDLE)) && !terminating) {
	myBufferedState = myAPState;
	setState(AP_COPY);
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
      // FIXME: Hack to manage agents moving while in AP_IDLE state,
      // but with pending timers. The correct solution would be to
      // restore all pending timers.
      if(myBufferedState == AP_IDLE)
	myBufferedState = AP_ACTIVE;

      setState(myBufferedState);
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
      setState(AP_GONE);
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
      if((myAPState == AP_ACTIVE)||(myAPState == AP_WAITING)||(myAPState == AP_IDLE) && !terminating) {
	myBufferedState = myAPState;
	setState(AP_SUSPENDED);
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
	setState(myBufferedState);
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
	setState(AP_WAITING);
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
      if((myAPState == AP_WAITING) || (myAPState == AP_IDLE)) {
	setState(AP_ACTIVE);
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
      if(myAPState != AP_DELETED && !terminating) {
	setState(AP_DELETED);
	if(!myThread.equals(Thread.currentThread()))
	  myThread.interrupt();
      }
    }
  }

  // This is to be called only by the scheduler
  void doIdle() {
    synchronized(stateLock) {
      if(myAPState != AP_IDLE)
	setState(AP_IDLE);
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
      AMSAgentDescription amsd = new AMSAgentDescription();
      amsd.setName(myAID);
      amsd.setOwnership("JADE");
      amsd.setState(AMSAgentDescription.ACTIVE);
      switch(myAPState) {
      case AP_INITIATED:
	setState(AP_ACTIVE);
	// No 'break' statement - fall through
      case AP_ACTIVE:
	if (myAID.equals(getAMS())) //special version for the AMS to avoid deadlock
	  ((jade.domain.ams)this).AMSRegister(amsd);
	else
	  AMSService.register(this,amsd);
        notifyStarted();
	setup();
	break;
      case AP_TRANSIT:
	doExecute();
	afterMove();
	break;
      case AP_COPY:
	doExecute();
	if (myAID.equals(getAMS())) //special version for the AMS to avoid deadlock
	  ((jade.domain.ams)this).AMSRegister(amsd);
	else
	  AMSService.register(this,amsd);
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
      	terminating = true;
	int savedState = getState();
	setState(AP_ACTIVE);
	takeDown();
	destroy();
	setState(savedState);
	break;
      case AP_GONE:
	break;
      default:
      	terminating = true;
	System.out.println("ERROR: Agent " + myName + " died without being properly terminated !!!");
	System.out.println("State was " + myAPState);
	savedState = getState();
	setState(AP_ACTIVE);
	takeDown();
	destroy();
	setState(savedState);
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
  void powerUp(AID id, ResourceManager rm) {

    // Set this agent's name and address and start its embedded thread
    if((myAPState == AP_INITIATED)||(myAPState == AP_TRANSIT)||(myAPState == AP_COPY)) {
      myName = id.getLocalName();
      myHap = id.getHap();
      myAID = id;

      //myThread = new Thread(myGroup, this);    
      myThread = rm.getThread(ResourceManager.USER_AGENTS, getLocalName(), this);    
      //myThread.setName(getLocalName());
      //myThread.setPriority(myGroup.getMaxPriority());
      myThread.start();
    }
  }

  private void writeObject(ObjectOutputStream out) throws IOException {
  	// Updates the queue maximum size field, before serialising
  	msgQueueMaxSize = msgQueue.getMaxSize();

    out.defaultWriteObject();
  }

  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    in.defaultReadObject();

    // Restore transient fields (apart from myThread, which will be set by doStart())
    msgQueue = new MessageQueue(msgQueueMaxSize);
    stateLock = new Object();
    suspendLock = new Object();
    waitLock = new Object();
    pendingTimers = new AssociationTB();
    languages = new HashMap();
    ontologies = new HashMap();
    theDispatcher = Runtime.instance().getTimerDispatcher();
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
	    int oldState = myAPState;
	    currentBehaviour = myScheduler.schedule();
	    if(myAPState != oldState)
	      setState(oldState);
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
	    case AP_ACTIVE:
	      System.out.println("WARNING: Spurious wakeup for agent " + getLocalName());
	    }
	  }


	  // Remember how many messages arrived
	  int oldMsgCounter = messageCounter;

	  // Just do it!
	  currentBehaviour.actionWrapper();

	  // If the current Behaviour is blocked and more messages
	  // arrived, restart the behaviour to give it another chance
	  if((oldMsgCounter != messageCounter) && (!currentBehaviour.isRunnable()))
	    currentBehaviour.restart();


	  // When it is needed no more, delete it from the behaviours queue
	  if(currentBehaviour.done()) {
	  	currentBehaviour.onEnd();
	    myScheduler.remove(currentBehaviour);
	    currentBehaviour = null;
	  }
	  else if(!currentBehaviour.isRunnable()) {
	    // Remove blocked behaviour from ready behaviours queue
	    // and put it in blocked behaviours queue
	    myScheduler.block(currentBehaviour);
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
	    setState(AP_ACTIVE);
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
	    setState(AP_SUSPENDED);
	  }
	}
      }
    }
  }

  private void destroy() { 
    try {
      if (myAID.equals(getAMS())) { //special version for the AMS to avoid deadlock 
	AMSAgentDescription amsd = new AMSAgentDescription();
	amsd.setName(getAID());
	((jade.domain.ams)this).AMSDeregister(amsd);
      } else
	AMSService.deregister(this);
    }
    catch(FIPAException fe) {
      fe.printStackTrace();
    }

    // Remove all pending timers
    Iterator it = pendingTimers.timers();
    while(it.hasNext()) {
      Timer t = (Timer)it.next();
      theDispatcher.remove(t);
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
    b.setAgent(this);
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
    b.setAgent(null);
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
    notifySend(msg);
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
	notifyReceived(currentMessage);
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
	  notifyReceived(msg);
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



  final void setToolkit(AgentToolkit at) {
    myToolkit = at;
  }

  final void resetToolkit() {
    myToolkit = null;
  }

  /**
    This method blocks until the agent has finished its start-up phase
    (i.e. until just before its setup() method is called.
    When this method returns, the target agent is registered with the
   AMS and the JADE platform is aware of it.
  */
  public synchronized void waitUntilStarted() {
    while(getState() == AP_INITIATED) {
      try {
        wait();
      }
      catch(InterruptedException ie) {
        // Do nothing...
      }
    }
  }
  
  // Event firing methods

  // Notify creator that the start-up phase has completed
  private synchronized void notifyStarted() {
    notifyAll();
  }

  // Notify toolkit that a message was posted in the message queue
  private void notifyPosted(ACLMessage msg) {
    myToolkit.handlePosted(myAID, msg);
  }

  // Notify toolkit that a message was extracted from the message
  // queue
  private void notifyReceived(ACLMessage msg) {
    myToolkit.handleReceived(myAID, msg);
  }

  // Notify toolkit of the need to send a message
  private void notifySend(ACLMessage msg) {
    myToolkit.handleSend(msg);
  }

  // Notify toolkit of the destruction of the current agent
  private void notifyDestruction() {
    myToolkit.handleEnd(myAID);
  }

  // Notify toolkit of the need to move the current agent
  private void notifyMove() {
    myToolkit.handleMove(myAID, myDestination);
  }

  // Notify toolkit of the need to copy the current agent
  private void notifyCopy() {
    myToolkit.handleClone(myAID, myDestination, myNewName);
  }

  // Notify toolkit that the current agent has changed its state
  private void notifyChangedAgentState(int oldState, int newState) {
    AgentState from = STATES[oldState];
    AgentState to = STATES[newState];
    if(myToolkit != null)
      myToolkit.handleChangedAgentState(myAID, from, to);
  }

  private void activateAllBehaviours() {
    myScheduler.restartAll();
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
	f.write(msg.toString());
	f.close();
      }
      catch(java.io.IOException ioe) {
	  System.out.println(ioe.getMessage());
      }
      */

      if(msg != null) {
	msgQueue.addLast(msg);
	notifyPosted(msg);
      }
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

	private ContentManager theContentManager = new ContentManager();

	/**
	* Retrieves the content manager 
	*
	* @return The content manager.
	*/
	public ContentManager getContentManager() {
		return theContentManager;
	} 
}
