/*
  $Log$
  Revision 1.68  1999/10/08 08:27:35  rimassa
  Fixed a problem, when an unhandled exception caused the agent to die
  without deregistering itself with the AMS.

  Revision 1.67  1999/09/03 13:05:49  rimassa
  Fixed a bug in send() methods, due to the fact that now an ACL message
  slot is never null.

  Revision 1.66  1999/09/01 00:14:00  rimassa
  Added a package-scoped messages() method.

  Revision 1.65  1999/08/31 17:19:49  rimassa
  Added complete support for agent migration.

  Revision 1.64  1999/08/27 15:40:55  rimassa
  Implemented doMove() state transition.

  Revision 1.63  1999/08/10 15:42:23  rimassa
  Removed debugging printouts.

  Revision 1.62  1999/08/10 15:26:21  rimassa
  Added support for agent cloning.

  Revision 1.61  1999/07/26 15:33:59  rimassa
  Added complete support for serialization.
  Added support for agent reading and writing from/to Java streams.

  Revision 1.60  1999/07/25 23:54:02  rimassa
  Added a state and two transiton functions for mobility support within
  Agent Platform Life Cycle.

  Revision 1.59  1999/06/30 10:34:37  rimassa
  Fixed erroneous Javadoc '@see' tag.

  Revision 1.58  1999/06/25 12:26:56  rimassa
  Fixed a bug with timed blockingReceive(): when called with a 0 timeout
  (i.e. a 'forever' timeout), sometimes an IllegalArgumentException was
  thrown.

  Revision 1.57  1999/06/16 12:14:48  rimassa
  Corrected an error in a Javadoc comment.

  Revision 1.56  1999/06/16 00:16:00  rimassa
  Handled special case of 0 timeout in restartLater() method.
  Fixed a bug in activateBehaviour() method.

   Revision 1.55  1999/06/15 14:30:57  rimassa
   Added support for timeouts. Now every agent maintains a bidirectional
   mapping between Timer objects and behaviours. Furthermore, a static
   instance of a TimerDispatcher active object is used by agents to
   schedule behaviour restarts.

  Revision 1.54  1999/06/09 16:13:16  rimassa
  Added an empty restartLater() method.

  Revision 1.53  1999/06/09 12:53:54  rimassa
  Completed implementation of blockingReceive() with timeouts. Now
  elapsed time is correctly tracked to handle spurious wakeups.

  Revision 1.52  1999/06/08 23:46:17  rimassa
  Fixed some problems in new blockingReceive() with timeout methods.

  Revision 1.51  1999/06/08 15:48:50  rimassa
  Added a timeout version of blockingReceive() methods.

  Revision 1.50  1999/06/04 12:03:48  rimassa
  Removed global lock in setup() method, since the GUI problem turned to
  be unrelated.

  Revision 1.49  1999/06/04 07:41:44  rimassa
  Changed a line to use Starter class instead of jade.Boot class.

  Revision 1.48  1999/05/20 13:43:13  rimassa
  Moved all behaviour classes in their own subpackage.

  Revision 1.47  1999/04/13 15:58:05  rimassa
  Added a fix for GUI deadlock problem during platform startup: a global
  lock is acquired to force mutual exclusion among all setup() methods.

  Revision 1.46  1999/04/06 00:09:33  rimassa
  Documented public classes with Javadoc. Reduced access permissions wherever possible.

  Revision 1.45  1999/03/31 15:55:28  rimassa
  Fixed a problem with doActivate: now waiting agents are correctly
  waked up.
  Removed some debugging printouts.

  Revision 1.44  1999/03/30 15:40:31  rimassa
  Some debugging printouts.

  Revision 1.43  1999/03/30 06:48:14  rimassa
  Removed a deprecated ACLMessage.setDest() call.
  Removed some debugging printouts.

  Revision 1.42  1999/03/29 10:37:04  rimassa
  Added a public doStart() method to start agents from within others.
  Changed doSuspend() and doActivate() methods to buffer the agent state
  when suspending it and to restore it back on activation.

  Revision 1.41  1999/03/25 16:49:28  rimassa
  Added message queue management operations.

  Revision 1.40  1999/03/24 12:12:12  rimassa
  Ported most of Agent data structures to new Java 2 Collection
  framework.

  Revision 1.39  1999/03/15 15:20:33  rimassa
  Added automatic sender setting when no one is specified.

  Revision 1.38  1999/03/10 06:55:05  rimassa
  Removed a useless import clause.

  Revision 1.37  1999/03/03 16:14:19  rimassa
  Used split synchronization locks for different agent parts
  (suspending, waiting for events, message queue handling, ...).
  Rewritten Agen Platform Life Cycle state transition methods: now each
  one of them can be called both from the agent and from the Agent
  Platform. Besides, each methods guards itself against being invoked
  whan the agent is not in the correct state.

  Revision 1.36  1999/03/01 23:23:55  rimassa
  Completed Javadoc comments for Agent class.
  Changed addCommListener() and removeCommListener() methods from public
  to package scoped.

  Revision 1.35  1999/03/01 13:09:53  rimassa
  Started to write Javadoc comments for Agent class.

  Revision 1.34  1999/02/25 08:12:35  rimassa
  Instance variables 'myName' and 'myAddress' made private.
  Made variable 'myAPState' volatile.
  Made join() work without timeout, after resolving termination
  problems.
  Handled InterruptedIOException correctly.
  Removed doFipaRequestClientNB() and made all System Agents Access API
  blocking again.

  Revision 1.33  1999/02/15 11:41:46  rimassa
  Changed some code to use getXXX() naming methods.

  Revision 1.32  1999/02/14 23:06:15  rimassa
  Changed agent name handling: now getName() returns the GUID, whereas
  getLocalName() yields only the agent name.
  Fixed a problem with erroneous throwing of AgentDeathError during
  agent destruction.
  deregisterWithDF() now is again a blocking call.

  Revision 1.31  1999/02/03 09:48:05  rimassa
  Added a timestamp to 'failure' and 'refuse' messages.
  Added a non blocking 'doFipaRequestClientNB()' method, as a temporary
  hack for agent management actions, and made non blocking API for DF
  registration, deregistration and modification

  Revision 1.30  1998/12/07 23:42:35  rimassa
  Added a getAddress() method.
  Fixed by-hand parsing of message content beginning "( action ams ";
  now this is embedded within a suitable AgentManagementOntology inner
  class.

  Revision 1.29  1998/12/01 23:35:55  rimassa
  Changed a method name from 'modifyDFRegistration()' to
  'modifyDFData()'.
  Added a clause to insert a ':df-depth Exactly 1' search constraint
  when no one is given.

  Revision 1.28  1998/11/30 00:15:34  rimassa
  Completed API to use FIPA system agents: now all 'refuse' and
  'failure' reply messages are unmarshaled into Java exceptions.

  Revision 1.27  1998/11/15 23:00:20  rimassa
  Added a new private inner class, named AgentDeathError. Now when an
  Agent is killed from the AgentPlatform while in waiting state, a new
  AgentDeathError is raised, and the Agent thread can unblock and
  terminate.

  Revision 1.26  1998/11/09 00:02:25  rimassa
  Modified doWait() method to avoid missing notifications.
  A 'finally' clause is used to execute user-specific and JADE system
  cleanup both when an agent terminates naturally and when it is killed
  from RMA.

  Revision 1.25  1998/11/08 23:57:50  rimassa
  Added a join() method to allow AgentContainer objects to wait for all
  their agents to terminate before exiting.

  Revision 1.24  1998/11/05 23:31:20  rimassa
  Added a protected takeDown() method as a placeholder for
  agent-specific destruction actions.
  Added automatic AMS deregistration on agent exit.

  Revision 1.23  1998/11/01 19:11:19  rimassa
  Made doWake() activate all blocked behaviours.

  Revision 1.22  1998/10/31 16:27:36  rimassa
  Completed doDelete() method: now an Agent can be explicitly terminated
  from outside or end implicitly when one of its Behaviours calls
  doDelete(). Besides, when an agent dies informs its CommListeners.

  Revision 1.21  1998/10/25 23:54:30  rimassa
  Added an 'implements Serializable' clause to support Agent code
  downloading through RMI.

  Revision 1.20  1998/10/18 15:50:08  rimassa
  Method parse() is now deprecated, since ACLMessage class provides a
  fromText() static method.
  Removed any usage of deprecated ACLMessage default constructor.

  Revision 1.19  1998/10/11 19:11:13  rimassa
  Written methods to access Directory Facilitator and removed some dead
  code.

  Revision 1.18  1998/10/07 22:13:12  Giovanni
  Added a correct prototype to DF access methods in Agent class.

  Revision 1.17  1998/10/05 20:09:02  Giovanni
  Fixed comment indentation.

  Revision 1.16  1998/10/05 20:07:53  Giovanni
  Removed System.exit() in parse() method. Now it simply prints
  exception stack trace on failure.

  Revision 1.15  1998/10/04 18:00:55  rimassa
  Added a 'Log:' field to every source file.

  revision 1.14
  date: 1998/09/28 22:33:10;  author: Giovanni;  state: Exp;  lines: +154 -40
  Changed registerWithAMS() method to take advantage of new
  AgentManagementOntology class.
  Added public methods to access ACC, AMS and DF agents without explicit
  message passing.

  revision 1.13
  date: 1998/09/28 00:13:41;  author: rimassa;  state: Exp;  lines: +20 -16
  Added a name for the embedded thread (same name as the agent).
  Changed parameters ordering and ACL message format to comply with new
  FIPA 98 AMS.

  revision 1.12
  date: 1998/09/23 22:59:47;  author: Giovanni;  state: Exp;  lines: +3 -1
  *** empty log message ***

  revision 1.11
  date: 1998/09/16 20:05:20;  author: Giovanni;  state: Exp;  lines: +2 -2
  Changed code to reflect a name change in Behaviour class from
  execute() to action().

  revision 1.10
  date: 1998/09/09 01:37:04;  author: rimassa;  state: Exp;  lines: +30 -10
  Added support for Behaviour blocking and restarting. Now when a
  behaviour blocks it is removed from the Scheduler and put into a
  blocked behaviour queue (actually a Vector). When a message arrives,
  postMessage() method puts all blocked behaviours back in the Scheduler
  and calls restart() on each one of them.
  Since when the Scheduler is empty the agent thread is blocked, the
  outcome is that an agent whose behaviours are all waiting for messages
  (e.g. the AMS) does not waste CPU cycles.

  revision 1.9
  date: 1998/09/02 23:56:02;  author: rimassa;  state: Exp;  lines: +4 -1
  Added a 'Thread.yield() call in Agent.mainLoop() to improve fairness
  among different agents and thus application responsiveness.

  revision 1.8
  date: 1998/09/02 00:30:22;  author: rimassa;  state: Exp;  lines: +61 -38
  Now using jade.domain.AgentManagementOntology class to map AP
  Life-Cycle states to their names.

  AP Life-Cycle states now made public. Changed protection level for
  some instance variables. Better error handling during AMS registration.

  revision 1.7
  date: 1998/08/30 23:57:10;  author: rimassa;  state: Exp;  lines: +8 -1
  Fixed a bug in Agent.registerWithAMS() method, where reply messages
  from AMS were ignored. Now the agent receives the replies, but still
  does not do a complete error checking.

  revision 1.6
  date: 1998/08/30 22:52:18;  author: rimassa;  state: Exp;  lines: +71 -9
  Improved Agent Platform Life-Cycle management. Added constants for
  Domain Life-Cycle. Added support for IIOP address. Added automatic
  registration with platform AMS.

  revision 1.5
  date: 1998/08/25 18:08:43;  author: Giovanni;  state: Exp;  lines: +5 -1
  Added Agent.putBack() method to insert a received message back in the
  message queue.

  revision 1.4
  date: 1998/08/16 12:34:56;  author: rimassa;  state: Exp;  lines: +26 -7
  Communication event broadcasting is now done in a separate
  broadcastEvent() method. Added a multicast send() method using
  AgentGroup class.

  revision 1.3
  date: 1998/08/16 10:30:15;  author: rimassa;  state: Exp;  lines: +48 -18
  Added AP_DELETED state to Agent Platform life cycle, and made
  Agent.mainLoop() exit only when the state is AP_DELETED.
  Agent.doWake() is now synchronized, and so are all kinds of receive
  operations.

  Agent class now has two kinds of message receive operations: 'get
  first available message' and 'get first message matching a particular
  template'; both kinds come in blocking and nonblocking
  flavour. Blocking receives mplementations rely on nonblocking
  versions.

  revision 1.2
  date: 1998/08/08 17:23:31;  author: rimassa;  state: Exp;  lines: +3 -3
  Changed 'fipa' to 'jade' in package and import directives.

  revision 1.1
  date: 1998/08/08 14:27:50;  author: rimassa;  state: Exp;
  Renamed 'fipa' to 'jade'.

  */


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
import java.util.Vector;

import jade.core.behaviours.Behaviour;
import jade.lang.acl.*;
import jade.domain.AgentManagementOntology;
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

  private MessageQueue msgQueue = new MessageQueue(MSG_QUEUE_SIZE);
  private transient Vector listeners = new Vector();

  private String myName = null;
  private String myAddress = null;
  private transient Object stateLock = new Object(); // Used to make state transitions atomic
  private transient Object waitLock = new Object();  // Used for agent waiting
  private transient Object suspendLock = new Object(); // Used for agent suspension

  private transient Thread myThread;
  private Scheduler myScheduler;

  private transient AssociationTB pendingTimers = new AssociationTB();

  /**
     The <code>Behaviour</code> that is currently executing.
     @see jade.core.behaviours.Behaviour
  */
  protected Behaviour currentBehaviour;

  /**
     Last message received.
     @see jade.lang.acl.ACLMessage
  */
  protected ACLMessage currentMessage;

  // This variable is 'volatile' because is used as a latch to signal
  // agent suspension and termination from outside world.
  private volatile int myAPState;

  // These two variables are used as temporary buffers for
  // mobility-related parameters
  private transient String myDestination;
  private transient String myNewName;

  // Temporary buffer for agent suspension
  private int myBufferedState = AP_MIN;

  private int myDomainState;
  private Vector blockedBehaviours = new Vector();

  private transient ACLParser myParser = ACLParser.create();

  /**
     Default constructor.
  */
  public Agent() {
    myAPState = AP_INITIATED;
    myDomainState = D_UNKNOWN;
    myScheduler = new Scheduler(this);
  }

  /**
     Method to query the agent local name.
     @return A <code>String</code> containing the local agent name
     (e.g. <em>peter</em>).
  */
  public String getLocalName() {
    return myName;
  }

  /**
     Method to query the agent complete name (<em><b>GUID</b></em>).

     @return A <code>String</code> containing the complete agent name
     (e.g. <em>peter@iiop://fipa.org:50/acc</em>).
  */
  public String getName() {
    return myName + '@' + myAddress;
  }

  /**
     Method to query the agent home address. This is the address of
     the platform where the agent was created, and will never change
     during the whole lifetime of the agent.

     @return A <code>String</code> containing the agent home address
     (e.g. <em>iiop://fipa.org:50/acc</em>).
  */
  public String getAddress() {
    return myAddress;
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
      thisContainer.createAgent(name, this, AgentContainer.START);
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
     <em>This method is currently not implemented.</em>
  */
  public void doMove(String destination) {
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
  */
  public void doClone(String destination, String newName) {
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
	  suspendLock.notify();
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
        waitLock.notify(); // Wakes up the embedded thread
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
     @see jade.core.Agent#write(Outputstream s)
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
  public void restore(InputStream s) {
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
	registerWithAMS(null,Agent.AP_ACTIVE,null,null,null);
	setup();
	break;
      case AP_TRANSIT:
	doExecute();
	afterMove();
	break;
      case AP_COPY:
	doExecute();
	registerWithAMS(null,Agent.AP_ACTIVE,null,null,null);
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
	takeDown();
	destroy();
	break;
      case AP_GONE:
	break;
      default:
	System.out.println("ERROR: Agent " + myName + " died without being properly terminated !!!");
	System.out.println("State was " + myAPState);
	takeDown();
	destroy();
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
     TO DO
  */
  protected void beforeMove() {}

  /**
     TO DO
  */
  protected void afterMove() {}

  /**
     TO DO
  */
  protected void beforeClone() {}

  /**
     TO DO
  */
  protected void afterClone() {}

  // This method is used by the Agent Container to fire up a new agent for the first time
  void powerUp(String name, String platformAddress, ThreadGroup myGroup) {

    // Set this agent's name and address and start its embedded thread
    if((myAPState == AP_INITIATED)||(myAPState == AP_TRANSIT)||(myAPState == AP_COPY)) {
      myName = new String(name);
      myAddress = new String(platformAddress);
      myThread = new Thread(myGroup, this);    
      myThread.setName(myName);
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
    listeners = new Vector();
    stateLock = new Object();
    suspendLock = new Object();
    waitLock = new Object();
    pendingTimers = new AssociationTB();
    myParser = ACLParser.create();
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

	  // Just do it!
	  currentBehaviour.action();

	  // When it is needed no more, delete it from the behaviours queue
	  if(currentBehaviour.done()) {
	    myScheduler.remove(currentBehaviour);
	    currentBehaviour = null;
	  }
	  else if(!currentBehaviour.isRunnable()) {
	    // Remove blocked behaviours from scheduling queue and put it
	    // in blocked behaviours queue
	    myScheduler.remove(currentBehaviour);
	    blockedBehaviours.addElement(currentBehaviour);
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
    if(msg.getSource().length() < 1)
      msg.setSource(myName);
    CommEvent event = new CommEvent(this, msg);
    broadcastEvent(event);
  }

  /**
     Send an <b>ACL</b> message to the agent contained in a given
     <code>AgentGroup</code>. This method allows simple message
     multicast to be done. A similar result can be obtained putting
     many agent names in <code>:receiver</code> message field; the
     difference is that in that case every receiver agent can read all
     other receivers' names, whereas this method hides multicasting to
     receivers.
     @param msg An ACL message object containing the actual message to
     send.
     @param g An agent group object holding all receivers names.
     @see jade.lang.acl.ACLMessage
     @see jade.core.AgentGroup
  */
  public final void send(ACLMessage msg, AgentGroup g) {
    if(msg.getSource().length() < 1)
      msg.setSource(myName);
    CommEvent event = new CommEvent(this, msg, g);
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
    ACLMessage msg = receive();
    if(msg == null) {
      doWait(millis);
      msg = receive();
    }
    return msg;
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
    ACLMessage msg = receive(pattern);
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


  /**
     @deprecated Builds an ACL message from a character stream. Now
     <code>ACLMessage</code> class has this capabilities itself,
     through <code>fromText()</code> method.
     @see jade.lang.acl.ACLMessage#fromText(Reader r)
  */
  public ACLMessage parse(Reader text) {
    ACLMessage msg = null;
    try {
      msg = myParser.parse(text);
    }
    catch(ParseException pe) {
      pe.printStackTrace();
    }
    catch(TokenMgrError tme) {
      tme.printStackTrace();
    }
    return msg;
  }

  private ACLMessage FipaRequestMessage(String dest, String replyString) {
    ACLMessage request = new ACLMessage("request");

    request.setSource(myName);
    request.removeAllDests();
    request.addDest(dest);
    request.setLanguage("SL0");
    request.setOntology("fipa-agent-management");
    request.setProtocol("fipa-request");
    request.setReplyWith(replyString);

    return request;
  }

  private String doFipaRequestClient(ACLMessage request, String replyString) throws FIPAException {

    send(request);
    ACLMessage reply = blockingReceive(MessageTemplate.MatchReplyTo(replyString));
    if(reply.getType().equalsIgnoreCase("agree")) {
      reply =  blockingReceive(MessageTemplate.MatchReplyTo(replyString));
      if(!reply.getType().equalsIgnoreCase("inform")) {
	String content = reply.getContent();
	StringReader text = new StringReader(content);
	throw FIPAException.fromText(text);
      }
      else {
	String content = reply.getContent();
	return content;
      }
    }
    else {
      String content = reply.getContent();
      StringReader text = new StringReader(content);
      throw FIPAException.fromText(text);
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
  public void registerWithAMS(String signature, int APState, String delegateAgent,
			      String forwardAddress, String ownership) throws FIPAException {

    String replyString = myName + "-ams-registration-" + (new Date()).getTime();
    ACLMessage request = FipaRequestMessage("ams", replyString);

    // Build an AMS action object for the request
    AgentManagementOntology.AMSAction a = new AgentManagementOntology.AMSAction();
    AgentManagementOntology.AMSAgentDescriptor amsd = new AgentManagementOntology.AMSAgentDescriptor();

    amsd.setName(getName());
    amsd.setAddress(getAddress());
    amsd.setAPState(APState);
    amsd.setDelegateAgentName(delegateAgent);
    amsd.setForwardAddress(forwardAddress);
    amsd.setOwnership(ownership);

    a.setName(AgentManagementOntology.AMSAction.REGISTERAGENT);
    a.setArg(amsd);

    // Convert it to a String and write it in content field of the request
    StringWriter text = new StringWriter();
    a.toText(text);
    request.setContent(text.toString());
    // Send message and collect reply
    doFipaRequestClient(request, replyString);

  }

  /**
     Authenticate this agent with Agent Platform <b>AMS</b>. While this
     task can be accomplished with regular message passing according
     to <b>FIPA</b> protocols, this method is meant to ease this
     common duty.
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
     <em>This method is currently not implemented.</em>
  */
  public void authenticateWithAMS(String signature, int APState, String delegateAgent,
				  String forwardAddress, String ownership) throws FIPAException {
    // FIXME: Not implemented
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
    ACLMessage request = FipaRequestMessage("ams", replyString);

    // Build an AMS action object for the request
    AgentManagementOntology.AMSAction a = new AgentManagementOntology.AMSAction();
    AgentManagementOntology.AMSAgentDescriptor amsd = new AgentManagementOntology.AMSAgentDescriptor();

    amsd.setName(getName());
    a.setName(AgentManagementOntology.AMSAction.DEREGISTERAGENT);
    a.setArg(amsd);

    // Convert it to a String and write it in content field of the request
    StringWriter text = new StringWriter();
    a.toText(text);
    request.setContent(text.toString());

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
  public void modifyAMSRegistration(String signature, int APState, String delegateAgent,
				    String forwardAddress, String ownership) throws FIPAException {

    String replyString = myName + "-ams-modify-" + (new Date()).getTime();
    ACLMessage request = FipaRequestMessage("ams", replyString);

    // Build an AMS action object for the request
    AgentManagementOntology.AMSAction a = new AgentManagementOntology.AMSAction();
    AgentManagementOntology.AMSAgentDescriptor amsd = new AgentManagementOntology.AMSAgentDescriptor();

    amsd.setName(getName());
    amsd.setAddress(getAddress());
    amsd.setAPState(APState);
    amsd.setDelegateAgentName(delegateAgent);
    amsd.setForwardAddress(forwardAddress);
    amsd.setOwnership(ownership);

    a.setName(AgentManagementOntology.AMSAction.MODIFYAGENT);
    a.setArg(amsd);

    // Convert it to a String and write it in content field of the request
    StringWriter text = new StringWriter();
    a.toText(text);
    request.setContent(text.toString());

    // Send message and collect reply
    doFipaRequestClient(request, replyString);

  }

  /**
     This method uses Agent Platform <b>ACC</b> agent to forward an
     ACL message. Calling this method is exactly the same as calling
     <code>send()</code>, only slower, since the message is first sent
     to the ACC using a <code>fipa-request</code> standard protocol,
     and then bounced to actual destination agent.
     @param msg The ACL message to send.
     @exception FIPAException A suitable exception can be thrown when
     a <code>refuse</code> or <code>failure</code> messages are
     received from the ACC to indicate some error condition.
     @see jade.core.Agent#send(ACLMessage msg)
  */
  public void forwardWithACC(ACLMessage msg) throws FIPAException {

    String replyString = myName + "-acc-forward-" + (new Date()).getTime();
    ACLMessage request = FipaRequestMessage("acc", replyString);

    // Build an ACC action object for the request
    AgentManagementOntology.ACCAction a = new AgentManagementOntology.ACCAction();
    a.setName(AgentManagementOntology.ACCAction.FORWARD);
    a.setArg(msg);

    // Convert it to a String and write it in content field of the request
    StringWriter text = new StringWriter();
    a.toText(text);
    request.setContent(text.toString());

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
     @see jade.domain.AgentManagementOntology.DFAgentDescriptor
   */
  public void registerWithDF(String dfName, AgentManagementOntology.DFAgentDescriptor dfd) throws FIPAException {

    String replyString = myName + "-df-register-" + (new Date()).getTime();
    ACLMessage request = FipaRequestMessage(dfName, replyString);

    // Build a DF action object for the request
    AgentManagementOntology.DFAction a = new AgentManagementOntology.DFAction();
    a.setName(AgentManagementOntology.DFAction.REGISTER);
    a.setActor(dfName);
    a.setArg(dfd);

    // Convert it to a String and write it in content field of the request
    StringWriter text = new StringWriter();
    a.toText(text);
    request.setContent(text.toString());

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
     @see jade.domain.AgentManagementOntology.DFAgentDescriptor
  */
  public void deregisterWithDF(String dfName, AgentManagementOntology.DFAgentDescriptor dfd) throws FIPAException {

    String replyString = myName + "-df-deregister-" + (new Date()).getTime();
    ACLMessage request = FipaRequestMessage(dfName, replyString);

    // Build a DF action object for the request
    AgentManagementOntology.DFAction a = new AgentManagementOntology.DFAction();
    a.setName(AgentManagementOntology.DFAction.DEREGISTER);
    a.setActor(dfName);
    a.setArg(dfd);

    // Convert it to a String and write it in content field of the request
    StringWriter text = new StringWriter();
    a.toText(text);
    request.setContent(text.toString());

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
     @see jade.domain.AgentManagementOntology.DFAgentDescriptor
  */
  public void modifyDFData(String dfName, AgentManagementOntology.DFAgentDescriptor dfd) throws FIPAException {

    String replyString = myName + "-df-modify-" + (new Date()).getTime();
    ACLMessage request = FipaRequestMessage(dfName, replyString);

    // Build a DF action object for the request
    AgentManagementOntology.DFAction a = new AgentManagementOntology.DFAction();
    a.setName(AgentManagementOntology.DFAction.MODIFY);
    a.setActor(dfName);
    a.setArg(dfd);

    // Convert it to a String and write it in content field of the request
    StringWriter text = new StringWriter();
    a.toText(text);
    request.setContent(text.toString());

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
     @param constraints A <code>Vector</code> that must be filled with
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
     @see jade.domain.AgentManagementOntology.DFAgentDescriptor
     @see java.util.Vector
     @see jade.domain.AgentManagementOntology.Constraint
     @see jade.domain.AgentManagementOntology.DFSearchResult
  */
  public AgentManagementOntology.DFSearchResult searchDF(String dfName, AgentManagementOntology.DFAgentDescriptor dfd, Vector constraints) throws FIPAException {

    String replyString = myName + "-df-search-" + (new Date()).getTime();
    ACLMessage request = FipaRequestMessage(dfName, replyString);

    // Build a DF action object for the request
    AgentManagementOntology.DFSearchAction a = new AgentManagementOntology.DFSearchAction();
    a.setName(AgentManagementOntology.DFAction.SEARCH);
    a.setActor(dfName);
    a.setArg(dfd);

    if(constraints == null) {
      AgentManagementOntology.Constraint c = new AgentManagementOntology.Constraint();
      c.setName(AgentManagementOntology.Constraint.DFDEPTH);
      c.setFn(AgentManagementOntology.Constraint.EXACTLY);
      c.setArg(1);
      a.addConstraint(c);
    }
    else {
      // Put constraints into action
      Iterator i = constraints.iterator();
      while(i.hasNext()) {
	AgentManagementOntology.Constraint c = (AgentManagementOntology.Constraint)i.next();
	a.addConstraint(c);
      }
    }

    // Convert it to a String and write it in content field of the request
    StringWriter textOut = new StringWriter();
    a.toText(textOut);
    request.setContent(textOut.toString());

    // Send message and collect reply
    String content = doFipaRequestClient(request, replyString);

    // Extract agent descriptors from reply message
    AgentManagementOntology.DFSearchResult found = null;
    StringReader textIn = new StringReader(content);
    try {
      found = AgentManagementOntology.DFSearchResult.fromText(textIn);
    }
    catch(jade.domain.ParseException jdpe) {
      jdpe.printStackTrace();
    }
    catch(jade.domain.TokenMgrError jdtme) {
      jdtme.printStackTrace();
    }

    return found;

  }


  // Event handling methods


  // Broadcast communication event to registered listeners
  private void broadcastEvent(CommEvent event) {
    Iterator i = listeners.iterator();
    while(i.hasNext()) {
      CommListener l = (CommListener)i.next();
      l.CommHandle(event);
    }
  }

  // Register a new listener
  public final void addCommListener(CommListener l) {
    listeners.add(l);
  }

  // Remove a registered listener
  public final void removeCommListener(CommListener l) {
    listeners.remove(l);
  }

  // Notify listeners of the destruction of the current agent
  private void notifyDestruction() {
    Iterator i = listeners.iterator();
    while(i.hasNext()) {
      CommListener l = (CommListener)i.next();
      l.endSource(myName);
    }
  }

  // Notify listeners of the need to copy the current agent
  private void notifyMove() {
    Iterator i = listeners.iterator();
    while(i.hasNext()) {
      CommListener l = (CommListener)i.next();
      l.moveSource(myName, myDestination);
    }
  }

  // Notify listeners of the need to copy the current agent
  private void notifyCopy() {
    Iterator i = listeners.iterator();
    while(i.hasNext()) {
      CommListener l = (CommListener)i.next();
      l.copySource(myName, myDestination, myNewName);
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
	Behaviour b = (Behaviour)blockedBehaviours.lastElement();
	blockedBehaviours.removeElementAt(blockedBehaviours.size() - 1);
	b.restart();
	myScheduler.add(b);
      }
    }
  }


  /**
     Put a received message into the agent message queue. The mesage
     is put at the back end of the queue. This method is called by
     JADE runtime system when a message arrives, but can also be used
     by an agent, and is just the same as sending a message to oneself
     (though slightly faster).
     @param msg The ACL message to put in the queue.
     @see jade.core.Agent#send(ACLMessage msg)
  */
  public final void postMessage (ACLMessage msg) {
    synchronized(waitLock) {
      if(msg != null) msgQueue.addLast(msg);
      doWake();
    }
  }

  Iterator messages() {
    return msgQueue.iterator();
  }

}
