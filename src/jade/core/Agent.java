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

import java.io.IOException;
import java.io.InterruptedIOException;

import jade.util.leap.Serializable;
import jade.util.leap.Iterator;
import java.util.Hashtable;
import java.util.Enumeration;

import jade.core.behaviours.Behaviour;

import jade.lang.acl.*;
import jade.domain.FIPAException;

import jade.security.AuthException;

//#MIDP_EXCLUDE_BEGIN
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

import jade.core.mobility.AgentMobilityHelper;

import jade.util.leap.List;
import jade.util.leap.ArrayList;
import jade.util.leap.Map;
import jade.util.leap.HashMap;

import java.util.Vector;

import jade.security.Authority;
/*import jade.security.AgentPrincipal;
import jade.security.DelegationCertificate;
import jade.security.IdentityCertificate;
import jade.security.CertificateFolder;
import jade.security.PrivilegedExceptionAction;*/
//#MIDP_EXCLUDE_END

/*#MIDP_INCLUDE_BEGIN
import javax.microedition.midlet.*;
#MIDP_INCLUDE_END*/

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
   
   @author Giovanni Rimassa - Universita' di Parma
   @version $Date$ $Revision$
 */
public class Agent implements Runnable, Serializable 
	//#APIDOC_EXCLUDE_BEGIN
	, TimerListener 
	//#APIDOC_EXCLUDE_END
	{
  private static final long     serialVersionUID = 3487495895819000L;
	
  // This inner class is used to force agent termination when a signal
  // from the outside is received
  private class AgentDeathError extends Error {
  	//#MIDP_EXCLUDE_BEGIN
    AgentDeathError() {
      super("Agent " + Thread.currentThread().getName() + " has been terminated.");
    }
  	//#MIDP_EXCLUDE_END
  }

  //#MIDP_EXCLUDE_BEGIN
  private static class AgentInMotionError extends Error {
    AgentInMotionError() {
      super("Agent " + Thread.currentThread().getName() + " is about to move or be cloned.");
    }
  }
 	//#MIDP_EXCLUDE_END


  // This class manages bidirectional associations between Timer and
  // Behaviour objects, using hash tables. This class is fully
  // synchronized because is accessed both by agent internal thread
  // and high priority Timer Dispatcher thread.
  private class AssociationTB {
      private Hashtable BtoT = new Hashtable();
      private Hashtable TtoB = new Hashtable();

      public void clear() {

	  Enumeration e = timers();
	  while (e.hasMoreElements()) {
	      Timer t = (Timer) e.nextElement();
	      theDispatcher.remove(t);
	  }

	  BtoT.clear();
	  TtoB.clear();

	  //#MIDP_EXCLUDE_BEGIN

	  // For persistence service
	  persistentPendingTimers.clear();

	  //#MIDP_EXCLUDE_END

      }

      public synchronized void addPair(Behaviour b, Timer t) {
	  TBPair pair = new TBPair(Agent.this, t, b);
	  addPair(pair);
      }

      public synchronized void addPair(TBPair pair) {
	  if(pair.getOwner() == null) {
	      pair.setOwner(Agent.this);
	  }

	  pair.setTimer(theDispatcher.add(pair.getTimer()));
	  TBPair old = (TBPair)BtoT.put(pair.getBehaviour(), pair);
	  if(old != null) {
	      theDispatcher.remove(old.getTimer());
	      //#MIDP_EXCLUDE_BEGIN
	      persistentPendingTimers.remove(old);
	      //#MIDP_EXCLUDE_END
	  }
	  old = (TBPair)TtoB.put(pair.getTimer(), pair);
	  if(old != null) {
	      theDispatcher.remove(old.getTimer());
	      //#MIDP_EXCLUDE_BEGIN
	      persistentPendingTimers.remove(old);
	      //#MIDP_EXCLUDE_END
	  }

	  //#MIDP_EXCLUDE_BEGIN

	  // For persistence service
	  persistentPendingTimers.add(pair);

	  //#MIDP_EXCLUDE_END
      }

      public synchronized void removeMapping(Behaviour b) {
	  TBPair pair = (TBPair)BtoT.remove(b);
	  if(pair != null) {
	      TtoB.remove(pair.getTimer());

	      //#MIDP_EXCLUDE_BEGIN

	      // For persistence service
	      persistentPendingTimers.remove(pair);

	      //#MIDP_EXCLUDE_END

	      theDispatcher.remove(pair.getTimer());
	  }
      }

      public synchronized void removeMapping(Timer t) {
	  TBPair pair = (TBPair)TtoB.remove(t);
	  if(pair != null) {
	      BtoT.remove(pair.getBehaviour());

	      //#MIDP_EXCLUDE_BEGIN

	      // For persistence service
	      persistentPendingTimers.remove(pair);

	      //#MIDP_EXCLUDE_END

	      theDispatcher.remove(pair.getTimer());
	  }
      }

      public synchronized Timer getPeer(Behaviour b) {
	  TBPair pair = (TBPair)BtoT.get(b);
	  if(pair != null) {
	      return pair.getTimer();
	  }
	  else {
	      return null;
	  }
      }

      public synchronized Behaviour getPeer(Timer t) {
	  TBPair pair = (TBPair)TtoB.get(t);
	  if(pair != null) {
	      return pair.getBehaviour();
	  }
	  else {
	      return null;
	  }
      }

      public synchronized Enumeration timers() {
	  return TtoB.keys();
      }

  } // End of AssociationTB class

    private static class TBPair {

	public TBPair() {
	    expirationTime = -1;
	}

	public TBPair(Agent a, Timer t, Behaviour b) {
	    owner = a;
	    myTimer = t;
	    expirationTime = t.expirationTime();
	    myBehaviour = b;
	}

	public void setTimer(Timer t) {
	    myTimer = t;
	}

	public Timer getTimer() {
	    return myTimer;
	}

	public Behaviour getBehaviour() {
	    return myBehaviour;
	}

	public void setBehaviour(Behaviour b) {
	    myBehaviour = b;
	}


	public Agent getOwner() {
	    return owner;
	}

	public void setOwner(Agent o) {
	    owner = o;
	    createTimerIfNeeded();
	}

	public long getExpirationTime() {
	    return expirationTime;
	}

	public void setExpirationTime(long when) {
	    expirationTime = when;
	    createTimerIfNeeded();
	}

	// If both the owner and the expiration time have been set,
	// but the Timer object is still null, create one
	private void createTimerIfNeeded() {
	    if(myTimer == null) {
		if((owner != null) && (expirationTime > 0)) {
		    myTimer = new Timer(expirationTime, owner);
		}
	    }
	}

	private Timer myTimer;
	private long expirationTime;
	private Behaviour myBehaviour;
	private Agent owner;

    } // End of TBPair class


	//#MIDP_EXCLUDE_BEGIN
  // A simple class for a boolean condition variable
  private static class CondVar {
    private boolean value = false;

    public synchronized void waitOn() throws InterruptedException {
      while(!value) {
	wait();
      }
    }

    public synchronized void set() {
      value = true;
      notifyAll();
    }

  } // End of CondVar class
	//#MIDP_EXCLUDE_END


    //#APIDOC_EXCLUDE_BEGIN

  /**
     Schedules a restart for a behaviour, after a certain amount of
     time has passed.
     @param b The behaviour to restart later.
     @param millis The amount of time to wait before restarting
     <code>b</code>.
     @see jade.core.behaviours.Behaviour#block(long millis)
  */
  public void restartLater(Behaviour b, long millis) {
    if (millis <= 0) 
    	return;
    Timer t = new Timer(System.currentTimeMillis() + millis, this);
    // The following block of code must be synchronized with the operations
  	// carried out by the TimerDispatcher. In fact it could be the case that
  	// 1) A behaviour blocks for a very short time --> A Timer is added
    // to the TimerDispatcher
  	// 2) The Timer immediately expires and the TimerDispatcher try to 
    // restart the behaviour before the pair (b, t) is added to the 
    // pendingTimers of this agent.
  	synchronized (theDispatcher) {
	    pendingTimers.addPair(b, t);
    }
  }

  /**
     Restarts the behaviour associated with t. 
     This method runs within the time-critical Timer Dispatcher thread and
     is not intended to be called by users. It is defined public only because
     is part of the <code>TimerListener</code> interface.
   */
  public void doTimeOut(Timer t) {
    Behaviour b = pendingTimers.getPeer(t);
    if(b != null) {
      b.restart();
    }
    //#MIDP_EXCLUDE_BEGIN
    else {
    	System.out.println("Warning: No mapping found for expired timer "+t.expirationTime());
    }
    //#MIDP_EXCLUDE_END
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
    }

    // Did this restart() cause the root behaviour to become runnable ?
    // If so, put the root behaviour back into the ready queue.
    Behaviour root = b.root();
    if(root.isRunnable())
    {
      myScheduler.restart(root);
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

  //#MIDP_EXCLUDE_BEGIN
  /**
     Represents the <code>transit</code> agent state.
  */
  public static final int AP_TRANSIT = 7;

  // Non compliant states, used internally. Maybe report to FIPA...
  /**
     Represents the <code>copy</code> agent state.
  */
  public static final int AP_COPY = 8;

  /**
     Represents the <code>gone</code> agent state. This is the state
     the original instance of an agent goes into when a migration
     transaction successfully commits.
  */
  static final int AP_GONE = 9;

  /**
     Represents the <code>saving</code> agent state. This is the state
     an agent goes into when its actual storage within a persistent
     data repository takes place.
  */
  static final int AP_SAVING = 10;

  /**
     Represents the <code>loading</code> agent state. This is the
     state an agent goes into when its current state is about to be
     replaced with a new one retrieved from a persistent data
     repository.
  */
  static final int AP_RELOADING = 11;

  /**
     Represents the <code>frozen</code> agent state. This is similar
     to the suspended state, but the agent is actually removed from
     its container and kept on a persistent store.
  */
  static final int AP_FROZEN = 12;


  //#MIDP_EXCLUDE_END

  /**
     Out of band value for Agent Platform Life Cycle states.
  */
  public static final int AP_MAX = 13;    // Hand-made type checking

  //#MIDP_EXCLUDE_BEGIN  

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
  //#MIDP_EXCLUDE_END
  //#APIDOC_EXCLUDE_END


  /**
     Get the Agent ID for the platform AMS.
     @return An <code>AID</code> object, that can be used to contact
     the AMS of this platform.
  */
	public final AID getAMS() {
		return myToolkit.getAMS();  
	}
  /**
     Get the Agent ID for the platform default DF.
     @return An <code>AID</code> object, that can be used to contact
     the default DF of this platform.
  */
	public final AID getDefaultDF() {
		return myToolkit.getDefaultDF();
	}

  //#MIDP_EXCLUDE_BEGIN
  private int       msgQueueMaxSize = 0;
  private transient MessageQueue msgQueue = new MessageQueue(msgQueueMaxSize);
  private transient List o2aQueue;
  private int o2aQueueSize = 0;
  private transient Map o2aLocks = new HashMap();
  private transient AgentToolkit myToolkit = DummyToolkit.instance();
  //#MIDP_EXCLUDE_END
  /*#MIDP_INCLUDE_BEGIN
  private transient MessageQueue    msgQueue = new MessageQueue(0);
  private transient AgentToolkit    myToolkit;
	#MIDP_INCLUDE_END*/
  
  private String myName = null;  
  private AID myAID = null;
  private String myHap = null;

  private transient Object stateLock = new Object(); // Used to make state transitions atomic
  private transient Object suspendLock = new Object(); // Used for agent suspension
  //#MIDP_EXCLUDE_BEGIN
  //private transient Object principalLock = new Object(); // Used to make principal transitions atomic
  //#MIDP_EXCLUDE_END

  private transient Thread myThread;
  private transient TimerDispatcher theDispatcher;

  private Scheduler myScheduler;

  private transient AssociationTB pendingTimers = new AssociationTB();

  // Free running counter that increments by one for each message
  // received.
  private int messageCounter = 0 ;

  
  //#APIDOC_EXCLUDE_BEGIN
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
  //#APIDOC_EXCLUDE_END

  // This variable is 'volatile' because is used as a latch to signal
  // agent suspension and termination from outside world.
  private volatile int myAPState;

  //#MIDP_EXCLUDE_BEGIN
  //private Authority authority;
  //private String ownership = jade.security.JADEPrincipal.NONE;
  //private AgentPrincipal principal = null;
  //private CertificateFolder certs = new CertificateFolder();
  //#MIDP_EXCLUDE_END
  
  /**
     This flag is used to distinguish the normal AP_ACTIVE state from
     the particular case in which the agent state is set to AP_ACTIVE
     during agent termination to allow it to deregister with the AMS. 
     In this case in fact a call to <code>doDelete()</code>, 
     <code>doMove()</code>, <code>doClone()</code> and <code>doSuspend()</code>
     should have no effect.
  */
  private boolean terminating = false;
  
  // For persistence service
  private void setTerminating(boolean b) {
      terminating = b;
  }

  // For persistence service
  private boolean getTerminating() {
      return terminating;
  }

  //#MIDP_EXCLUDE_BEGIN
  /** 
     When set to false (default) all behaviour-related events (such as ADDED_BEHAVIOUR
     or CHANGED_BEHAVIOUR_STATE) are not generated in order to improve performances.
     These events in facts are very frequent.
   */
  private boolean generateBehaviourEvents = false;

  // These two variables are used as temporary buffers for
  // mobility-related parameters
  private transient Location myDestination;
  private transient String myNewName;


  // This variables are used as a temporary buffer for
  // persistence-related parameters
  private transient String myRepository;
  private transient ContainerID myBufferContainer;

  //#MIDP_EXCLUDE_END

  // Temporary buffer for agent suspension
  private int myBufferedState = AP_MIN;

	/*#MIDP_INCLUDE_BEGIN
  public static MIDlet midlet;
  
  // Flag for agent interruption (necessary as Thread.interrupt()
  // is not available in MIDP)
  private boolean isInterrupted = false;
	#MIDP_INCLUDE_END*/

  /**
     Default constructor.
  */
  public Agent() {
    changeStateTo(AP_INITIATED);
    myScheduler = new Scheduler(this);
    theDispatcher = TimerDispatcher.getTimerDispatcher();
  }
  
  //#MIDP_EXCLUDE_BEGIN
  /**
     Constructor to be used by special "agents" that will never powerUp.
   */
    Agent(AID id) {
    myName = id.getLocalName();
    myHap = id.getHap();
    myAID = id;
    }

    // For persistence service
    private Long persistentID;

    // For persistence service
    private Long getPersistentID() {
	return persistentID;
    }

    // For persistence service
    private void setPersistentID(Long l) {
	persistentID = l;
    }

    
    /** Declared transient because the container changes in case
     * of agent migration.
     **/
    private transient jade.wrapper.AgentContainer myContainer = null;

   /**
    * Return a controller for this agents container. 
    * <br>
    * <b>NOT available in MIDP</b>
    * <br>
    * @return jade.wrapper.AgentContainer The proxy container for this agent.
    */
   public final jade.wrapper.AgentContainer getContainerController() {
     if (myContainer == null) {  // first time called
	 try {
	     myContainer = new jade.wrapper.AgentContainer((AgentContainerImpl)myToolkit, getHap());
	 } catch (Exception e) {
	     throw new IllegalStateException("A ContainerController cannot be got for this agent. Probably the method has been called at an appropriate time before the complete initialization of the agent.");
	 }
     }
     return myContainer;
   }



  //#MIDP_EXCLUDE_END
  
    private transient Object[] arguments = null;  // array of arguments
    //#APIDOC_EXCLUDE_BEGIN
    /**
     * Called by AgentContainerImpl in order to pass arguments to a
     * just created Agent. 
     * <p>Usually, programmers do not need to call this method in their code.
     * @see #getArguments() how to get the arguments passed to an agent
     **/
    public final void setArguments(Object args[]) {
	// I have declared the method final otherwise getArguments would not work!
	arguments=args;
    }
  	//#APIDOC_EXCLUDE_END

    /**
     * Get the array of arguments passed to this agent.
     * <p> Take care that the arguments are transient and they do not
     * migrate with the agent neither are cloned with the agent!
     * @return the array of arguments passed to this agent.
     * @see <a href=../../../tutorials/ArgsAndPropsPassing.htm>How to use arguments or properties to configure your agent.</a>
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

  private void setAID(AID id) {
      myAID = id;
  }

  /**
     This method adds a new platform address to the AID of this Agent.
     It is called by the container when a new MTP is activated
     in the platform (in the local container - installMTP() -  
     or in a remote container - updateRoutingTable()) to keep the 
     Agent AID updated.
   */
  synchronized void addPlatformAddress(String address) { // Mutual exclusion with Agent.powerUp()
		if (myAID != null) {
			// Cloning the AID is necessary as the agent may be using its AID.
			// If this is the case a ConcurrentModificationException would be thrown
			myAID = (AID)myAID.clone(); 
			myAID.addAddresses(address);
		}
  }
  
  /**
     This method removes an old platform address from the AID of this Agent.
     It is called by the container when a new MTP is deactivated
     in the platform (in the local container - uninstallMTP() -  
     or in a remote container - updateRoutingTable()) to keep the 
     Agent AID updated.
   */
  synchronized void removePlatformAddress(String address) { // Mutual exclusion with Agent.powerUp()
		if (myAID != null) {
			// Cloning the AID is necessary as the agent may be using its AID.
			// If this is the case a ConcurrentModificationException would be thrown
			myAID = (AID)myAID.clone(); 
			myAID.removeAddresses(address);
		}
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
  
    //#APIDOC_EXCLUDE_BEGIN

	//#MIDP_EXCLUDE_BEGIN
  /*public Authority getAuthority() {
    return myToolkit.getAuthority();
  }*/
	//#MIDP_EXCLUDE_END

    //#APIDOC_EXCLUDE_END

  /**
   * This is used by the agent container to wait for agent termination.
   * We have alreader called doDelete on the thread which would have
   * issued an interrupt on it. However, it still may decide not to exit.
   * So we will wait no longer than 5 seconds for it to exit and we
   * do not care of this zombie agent.
   * FIXME: we must further isolate container and agents, for instance
   * by using custom class loader and dynamic proxies and JDK 1.3.
   * FIXME: the timeout value should be got by Profile
   */
  void join() {
  	//#MIDP_EXCLUDE_BEGIN
    try {
      if(myThread == null) {
	  return;
      }
      myThread.join(5000);
      if (myThread.isAlive()) {
        System.out.println("*** Warning: Agent " + myName + " did not terminate when requested to do so.");
   	    if(!myThread.equals(Thread.currentThread())) {
	      myThread.interrupt();
	      System.out.println("*** Second interrupt issued.");
        }
      }
    }
    catch(InterruptedException ie) {
      ie.printStackTrace();
    }
  	//#MIDP_EXCLUDE_END
  	/*#MIDP_INCLUDE_BEGIN
    if (myThread != null && myThread.isAlive()) {
      try {
        myThread.join();
      } 
      catch (InterruptedException ie) {
        ie.printStackTrace();
      } 
    } 
  	#MIDP_INCLUDE_END*/
  }
//#CUSTOM_EXCLUDE_BEGIN
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
  	//#MIDP_EXCLUDE_BEGIN
    msgQueueMaxSize = newSize;
  	//#MIDP_EXCLUDE_END
  }

    /**
     * This method retrieves the current lenght of the message queue
     * of this agent.
     * @return The number of messages that are currently stored into the
     * message queue.
     **/
    public int getCurQueueSize() {
	return msgQueue.size();
    }

  /**
     Reads message queue size. A zero value means that the message
     queue is unbounded (its size is limited only by amount of
     available memory).
     @return The actual size of the message queue (i.e. the max number
     of messages that can be stored into the queue)
     @see jade.core.Agent#setQueueSize(int newSize)
     @see jade.core.Agent#getCurQueueSize()
  */
  public int getQueueSize() {
    return msgQueue.getMaxSize();
  }
//#CUSTOM_EXCLUDE_END


  /*
  //#MIDP_EXCLUDE_BEGIN
  //#APIDOC_EXCLUDE_BEGIN
	public void setOwnership(String ownership) {
	  this.ownership = ownership;
	}

	public static String extractUsername(String ownership) {
		int dot2 = ownership.indexOf(':');
		return (dot2 != -1) ?
				ownership.substring(0, dot2) : ownership;
	}

	public static byte[] extractPassword(String ownership) {
		int dot2 = ownership.indexOf(':');
		return (dot2 != -1 && dot2 < ownership.length() - 1) ?
				ownership.substring(dot2 + 1, ownership.length()).getBytes() : new byte[] {};
	}

	public void setPrincipal(CertificateFolder certs) {
		AgentPrincipal old = getPrincipal();
		synchronized (principalLock) {
			this.certs = certs;
			this.principal = (AgentPrincipal)certs.getIdentityCertificate().getSubject();
			notifyChangedAgentPrincipal(old, certs);
		}
	}

	public AgentPrincipal getPrincipal() {
		AgentPrincipal p = null;
		if (!(myToolkit instanceof DummyToolkit)) {
			synchronized (principalLock) {
				Authority authority = getAuthority();
				if (principal == null) {
					String user = extractUsername(ownership);
					principal = authority.createAgentPrincipal(myAID, user);
				}
				p = principal;
			}
		}
		return p;
	}
	
	public CertificateFolder getCertificateFolder() {
		return certs;
	}

    //#APIDOC_EXCLUDE_END
	
	private void doPrivileged(PrivilegedExceptionAction action) throws Exception {
		getAuthority().doAsPrivileged(action, getCertificateFolder());
	}
  //#MIDP_EXCLUDE_END
  */

  private void changeStateTo(int state) {
      synchronized (stateLock) {
	  if (state != myAPState) {
	      int oldState = myAPState;
	      setState(state);
	      //#MIDP_EXCLUDE_BEGIN
	      notifyChangedAgentState(oldState, myAPState);
	      //#MIDP_EXCLUDE_END
	      /*#MIDP_INCLUDE_BEGIN
	      //myToolkit.handleChangedAgentState(myAID, oldState, myAPState);
	      #MIDP_INCLUDE_END*/
	  }
      }
  }

  //#APIDOC_EXCLUDE_BEGIN
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

  // For persistence service
  private void setState(int state) {
      myAPState = state;
  }


    //#MIDP_EXCLUDE_BEGIN

  public AgentState getAgentState() {
    return AgentState.getInstance(getState());
  }
    //#APIDOC_EXCLUDE_END
  
  /**
     This is only called by the RealNotificationManager to provide the Introspector
     agent with a snapshot of the behaviours currently loaded in the agent
   */
  Scheduler getScheduler() {
  	return myScheduler;
  }

  /**
     This is only called by the RealNotificationManager to provide the Introspector
     agent with a snapshot of the messages currently pending in the queue and by
     the RealMobilityManager to transfer messages in the queue
   */
  MessageQueue getMessageQueue() {
      return msgQueue;
  }

  // For persistence service
  private void setMessageQueue(MessageQueue mq) {
      msgQueue = mq;
  }

  // State transition methods for Agent Platform Life-Cycle

  //#APIDOC_EXCLUDE_BEGIN
  /**
     Make a state transition from <em>initiated</em> to
     <em>active</em> within Agent Platform Life Cycle. Agents are
     started automatically by JADE on agent creation and 
     this method should not be
     used by application developers, unless creating some kind of
     agent factory. This method starts the embedded thread of the agent.
     <b> It is highly descouraged the usage of this method </b> because it
     does not guarantee agent autonomy. It is expected that in the
     next releases this method might be removed or its scope restricted.
     @param name The local name of the agent.
  */
  public void doStart(String name) {
    myToolkit.handleStart(name, this);
  }
  //#APIDOC_EXCLUDE_END

  /**
     Make a state transition from <em>active</em> to
     <em>transit</em> within Agent Platform Life Cycle. This method
     is intended to support agent mobility and is called either by the
     Agent Platform or by the agent itself to start a migration process.
     <br>
     <b>NOT available in MIDP</b>
     <br>
     @param destination The <code>Location</code> to migrate to.
  */
  public void doMove(Location destination) {
      // Do nothing if the mobility service is not installed
      try {
	  getHelper(jade.core.mobility.AgentMobilityHelper.NAME);
      }
      catch(ServiceException se) {
	  // Do nothing
	  return;
      }

      synchronized(stateLock) {
	  if(((myAPState == AP_ACTIVE)||(myAPState == AP_WAITING)||(myAPState == AP_IDLE)) && !terminating) {
	      myBufferedState = myAPState;
	      changeStateTo(AP_TRANSIT);
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
     <br>
     <b>NOT available in MIDP</b>
     <br>
     @param destination The <code>Location</code> where the copy agent will start.
     @param newName The name that will be given to the copy agent.
  */
  public void doClone(Location destination, String newName) {

      // Do nothing if the mobility service is not installed
      try {
	  getHelper(jade.core.mobility.AgentMobilityHelper.NAME);
      }
      catch(ServiceException se) {
	  // Do nothing
	  return;
      }

      synchronized(stateLock) {
	  if(((myAPState == AP_ACTIVE)||(myAPState == AP_WAITING)||(myAPState == AP_IDLE)) && !terminating) {
	      myBufferedState = myAPState;
	      changeStateTo(AP_COPY);
	      myDestination = destination;
	      myNewName = newName;

	      // Real action will be executed in the embedded thread
	      if(!myThread.equals(Thread.currentThread()))
		  myThread.interrupt();
	  }
      }
  }

  //#APIDOC_EXCLUDE_BEGIN

  // External method, is to be called from other threads
  public void requestSave(String repository) {
      // Do nothing if the persistence service is not installed
      try {
	  // FIXME: There should be a constant defined somewhere in the JADE distribution
	  getHelper("jade.core.persistence.Persistence");
      }
      catch(ServiceException se) {
	  // Do nothing
	  return;
      }

      synchronized(stateLock) {
	  if(((myAPState == AP_ACTIVE)||(myAPState == AP_WAITING)||(myAPState == AP_IDLE)) && !terminating) {
	      myBufferedState = myAPState;
	      changeStateTo(AP_SAVING);
	      myRepository = repository;

	      // Real action will be executed in the embedded thread
	      if(!myThread.equals(Thread.currentThread()))
		  myThread.interrupt();
	  }
      }      
  }

  public void requestReload(String repository) {
      // Do nothing if the persistence service is not installed
      try {
	  // FIXME: There should be a constant defined somewhere in the JADE distribution
	  getHelper("jade.core.persistence.Persistence");
      }
      catch(ServiceException se) {
	  // Do nothing
	  return;
      }

      synchronized(stateLock) {
	  if(((myAPState == AP_ACTIVE)||(myAPState == AP_WAITING)||(myAPState == AP_IDLE)) && !terminating) {
	      myBufferedState = myAPState;
	      changeStateTo(AP_RELOADING);
	      myRepository = repository;

	      // Real action will be executed in the embedded thread
	      if(!myThread.equals(Thread.currentThread()))
		  myThread.interrupt();
	  }
      }
  }

  // External method, is to be called from other threads
  public void requestFreeze(String repository, ContainerID bufferContainer) {
      // Do nothing if the persistence service is not installed
      try {
	  // FIXME: There should be a constant defined somewhere in the JADE distribution
	  getHelper("jade.core.persistence.Persistence");
      }
      catch(ServiceException se) {
	  // Do nothing
	  return;
      }
      synchronized(stateLock) {
	  if(((myAPState == AP_ACTIVE)||(myAPState == AP_WAITING)||(myAPState == AP_IDLE)) && !terminating) {
	      myBufferedState = myAPState;
	      changeStateTo(AP_FROZEN);

	      myRepository = repository;
	      myBufferContainer = bufferContainer;

	      // Real action will be executed in the embedded thread
	      if(!myThread.equals(Thread.currentThread()))
		  myThread.interrupt();
	  }
      }      
  }




  //#APIDOC_EXCLUDE_END
  
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

      changeStateTo(myBufferedState);
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
      changeStateTo(AP_GONE);
    }
  }
  //#MIDP_EXCLUDE_END

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
      if(((myAPState == AP_ACTIVE)||(myAPState == AP_WAITING)||(myAPState == AP_IDLE)) && !terminating) {
	myBufferedState = myAPState;
	changeStateTo(AP_SUSPENDED);
      }
    }
    if(myAPState == AP_SUSPENDED) {
      if(myThread.equals(Thread.currentThread())) {
	waitUntilActivate();
      }
      else {
      	synchronized (stateLock) {
      		if (myBufferedState == AP_IDLE) {
      			// When we resume we must go to the ACTIVE state (not IDLE)
      			myBufferedState = AP_ACTIVE;
      			interruptThread();
      		}
      	}
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
	changeStateTo(myBufferedState);
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
	changeStateTo(AP_WAITING);
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
	changeStateTo(AP_ACTIVE);
      }
    }
    if(myAPState == AP_ACTIVE) {
      activateAllBehaviours();
      synchronized(msgQueue) {
        msgQueue.notifyAll(); // Wakes up the embedded thread
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
	changeStateTo(AP_DELETED);
	if((myThread != null) && !myThread.equals(Thread.currentThread()))
          interruptThread();
      }
    }
  }

  // This is to be called only by the scheduler
  void idle() throws InterruptedException {
    synchronized(stateLock) {
    	if (myAPState != AP_ACTIVE) {
    		// We have been suspended from the outside just before entering this method
    		throw new InterruptedException();
    	}
			changeStateTo(AP_IDLE);
    }
    // No need for synchronized block since this is only called by the 
    // scheduler itself in the synchronized schedule() method
    waitOn(myScheduler, 0);
    synchronized(stateLock) {
	if(myAPState == AP_IDLE) {
	    changeStateTo(AP_ACTIVE);
	}
    }
  }

  //#MIDP_EXCLUDE_BEGIN
  /**
     Write this agent to an output stream; this method can be used to
     record a snapshot of the agent state on a file or to send it
     through a network connection. Of course, the whole agent must
     be serializable in order to be written successfully.
     <br>
     <b>NOT available in MIDP</b>
     <br>
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
     <br>
     <b>NOT available in MIDP</b>
     <br>
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
     <br>
     <b>NOT available in MIDP</b>
     <br>
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
      String oldName = in.readUTF();
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
     <br>
     <b>NOT available in MIDP</b>
     <br>
     @param s The input stream the agent state will be read from.
     @exception IOException Thrown if some I/O error occurs during
     stream reading.
     <em>Note: This method is currently not implemented</em>
  */
  public void restore(InputStream s) throws IOException {
    // FIXME: Not implemented
  }

  /**
     This method should not be used by application code. Use the
     same-named method of <code>jade.wrapper.Agent</code> instead.
     <br>
     <b>NOT available in MIDP</b>
     <br>
     @see jade.wrapper.Agent#putO2AObject(Object o, boolean blocking)
   */
  public void putO2AObject(Object o, boolean blocking) throws InterruptedException {
    // Drop object on the floor if object-to-agent communication is
    // disabled.
    if(o2aQueue == null)
      return;

    // If the queue has a limited capacity and it is full, discard the
    // first element
    if((o2aQueueSize != 0) && (o2aQueue.size() == o2aQueueSize))
      o2aQueue.remove(0);

    o2aQueue.add(o);

    // Reactivate the agent
    activateAllBehaviours();

    // Synchronize the calling thread on a condition associated to the
    // object
    if(blocking) {
      CondVar cond = new CondVar();

      // Store lock for later, when getO2AObject will be called
      o2aLocks.put(o, cond);

      // Sleep on the condition
      cond.waitOn();

    }

  }

  /**
     This method picks an object (if present) from the internal
     object-to-agent communication queue. In order for this method to
     work, the agent must have declared its will to accept objects
     from other software components running within its JVM. This can
     be achieved by calling the
     <code>jade.core.Agent.setEnabledO2ACommunication()</code> method.
     If the retrieved object was originally inserted by an external
     component using a blocking call, that call will return during the
     execution of this method.
     <br>
     <b>NOT available in MIDP</b>
     <br>
     @return the first object in the queue, or <code>null</code> if
     the queue is empty.
     @see jade.wrapper.Agent#putO2AObject(Object o, boolean blocking)
     @see jade.core.Agent#setEnabledO2ACommunication(boolean enabled, int queueSize)
   */
  public Object getO2AObject() {

    // Return 'null' if object-to-agent communication is disabled
    if(o2aQueue == null)
      return null;

    if(o2aQueue.isEmpty())
      return null;

    // Retrieve the first object from the object-to-agent
    // communication queue
    Object result = o2aQueue.remove(0);

    // If some thread issued a blocking putO2AObject() call with this
    // object, wake it up
    CondVar cond = (CondVar)o2aLocks.remove(result);
    if(cond != null) {
      cond.set();
    }

    return result;
    
  }


  /**
     This method declares this agent attitude towards object-to-agent
     communication, that is, whether the agent accepts to communicate
     with other non-JADE components living within the same JVM.
     <br>
     <b>NOT available in MIDP</b>
     <br>
     @param enabled Tells whether Java objects inserted with
     <code>putO2AObject()</code> will be accepted.
     @param queueSize If the object-to-agent communication is enabled,
     this parameter specifiies the maximum number of Java objects that
     will be queued. If the passed value is 0, no maximum limit is set
     up for the queue.

     @see jade.wrapper.Agent#putO2AObject(Object o, boolean blocking)
     @see jade.core.Agent#getO2AObject()

   */
  public void setEnabledO2ACommunication(boolean enabled, int queueSize) {
    if(enabled) {
      if(o2aQueue == null)
	o2aQueue = new ArrayList(queueSize);

      // Ignore a negative value
      if(queueSize >= 0)
	o2aQueueSize = queueSize;
    }
    else {

      // Wake up all threads blocked in putO2AObject() calls
      Iterator it = o2aLocks.values().iterator();
      while(it.hasNext()) {
	CondVar cv = (CondVar)it.next();
	cv.set();
      }

      o2aQueue = null;
    }

  }
  //#MIDP_EXCLUDE_END


  //#APIDOC_EXCLUDE_BEGIN

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
				changeStateTo(AP_ACTIVE);
				// No 'break' statement - fall through
      case AP_ACTIVE:
        notifyStarted();
				setup();
				break;
			//#MIDP_EXCLUDE_BEGIN
      case AP_TRANSIT:
	doExecute();
	afterMove();
	break;
      case AP_COPY:
	doExecute();
	afterClone();
	break;
      case AP_SAVING:
	  doExecute();
	  afterLoad();
	  break;
      case AP_RELOADING:
	  doExecute();
	  afterReload();
	  break;
      case AP_FROZEN:
	  doExecute();
	  afterThaw();
	  break;
			//#MIDP_EXCLUDE_END
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
    //#MIDP_EXCLUDE_BEGIN
    catch(AgentInMotionError aime) {
      // This is a move from the outside while the agent was waiting 
    	// (blockingReceive()) outside the mainLoop(). There is nothing 
    	// we can do
    }
    //#MIDP_EXCLUDE_END
    finally {
			//#MIDP_EXCLUDE_BEGIN
      switch(myAPState) {
      case AP_TRANSIT:
      case AP_COPY:
      	System.err.println("***  Agent " + myName + " moved in a forbidden situation ***");
      	// No break statement --> fall through
      case AP_DELETED:
      	terminating = true;
	int savedState = getState();
	changeStateTo(AP_ACTIVE);
	takeDown();
	destroy();
	changeStateTo(savedState);
	break;
      case AP_GONE:
	break;
      case AP_FROZEN:
	  break;
      case AP_RELOADING:
	  break;
      default:
      	terminating = true;
	System.out.println("ERROR: Agent " + myName + " died without being properly terminated !!!");
	System.out.println("State was " + myAPState);
	savedState = getState();
	changeStateTo(AP_ACTIVE);
	takeDown();
	destroy();
	changeStateTo(savedState);
      }
			//#MIDP_EXCLUDE_END
			/*#MIDP_INCLUDE_BEGIN
      if (myAPState != AP_DELETED) {
        System.out.println("ERROR: Agent "+myName+" died without being properly terminated !!!");
        System.out.println("State was "+myAPState);
      } 

      terminating = true;

      int savedState = getState();
      changeStateTo(AP_ACTIVE);
      takeDown();
      destroy();
      changeStateTo(savedState);
			#MIDP_INCLUDE_END*/
    }
  }

    //#APIDOC_EXCLUDE_END


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

	//#MIDP_EXCLUDE_BEGIN
  /**
   * This empty placeholder shall be overridden by user defined agents 
   * to execute some actions before the original agent instance on the 
   * source container is stopped (e.g. releasing local resources such 
   * as a GUI).<br>
   * <b>IMPORTANT:</b> At this point, it is ensured that the move process
   * is successful and that a moved agent instance has been created on the 
   * destination container 
   * Therefore setting the value of a class field in this method will have
   * no impact on the moved agent instance. Such parameters must indeed be 
   * set <b>before</b> the <code>doMove()</code> method is called.
    <br>
    <b>NOT available in MIDP</b>
    <br>
  */
  protected void beforeMove() {}

  /**
    Actions to perform after moving. This empty placeholder method can be
    overridden by user defined agents to execute some actions just after
    arriving to the destination agent container for a migration.
    <br>
    <b>NOT available in MIDP</b>
    <br>
  */
  protected void afterMove() {}

  /**
   * This empty placeholder method shall be overridden by user defined agents 
   * to execute some actions before copying an agent to another agent container.
   * <br>
   * <b>NOT available in MIDP</b>
   * <br>
   * @see beforeMove()
   * @see afterClone()
  */
  protected void beforeClone() {}

  /**
    Actions to perform after cloning. This empty placeholder method can be
    overridden by user defined agents to execute some actions just after
    creating an agent copy to the destination agent container.
    <br>
    <b>NOT available in MIDP</b>
    <br>
  */
  protected void afterClone() {}

  /**
     Actions to perform before saving an agent. This empty placeholder
     method can be overridden by user defined agents to execute some
     actions just before the current agent is saved to a persistent
     store.
     <br>
     <b>Not available in MIDP. Requires the Persistence JADE add-on</b>
     <br>
  */
  protected void beforeSave() {}

  /**
     Actions to perform after loading an agent. This empty placeholder
     method can be overridden by user defined agents to execute some
     actions just after the current agent is loaded from a persistent
     store.
     <br>
     <b>Not available in MIDP. Requires the Persistence JADE add-on</b>
     <br>
  */
  protected void afterLoad() {}

  /**
    Actions to perform before reloading. This empty placeholder method
    can be overridden by user defined agents to execute some actions
    just before the agent thread terminates (and before a new thread
    is started that will execute the newly reloaded agent).
  */
  protected void beforeReload() {}

  /**
     Actions to perform after reloading. This empty placeholder method
     can be overridden by user defined agents to execute some actions
     just after a new thread has started executing the newly reloaded
     agent (and before the thread enters the main agent execution
     loop.
  */
  protected void afterReload() {}

  /**
    Actions to perform before freezing. This empty placeholder method can be
    overridden by user defined agents to execute some actions just before
    an agent is frozen on a persistent store and its thread terminates.
    <br>
    <b>NOT available in MIDP</b>
    <br>
  */
  protected void beforeFreeze() {}

  /**
    Actions to perform after thawing. This empty placeholder method
    can be overridden by user defined agents to execute some actions
    just after a previously frozen agent is thawed and its thread is
    started on the proper agent container.
    <br>
    <b>NOT available in MIDP</b>
    <br>
  */
  protected void afterThaw() {}

	//#MIDP_EXCLUDE_END

  // This method is used by the Agent Container to fire up a new agent for the first time
  void powerUp(AID id, Thread t) {

    // Set this agent's name and address and start its embedded thread
    if ( (myAPState == AP_INITIATED) 
			//#MIDP_EXCLUDE_BEGIN
	|| (myAPState == AP_SAVING)
        || (myAPState == AP_FROZEN)
    	|| (myAPState == AP_TRANSIT) 
    	|| (myAPState == AP_COPY)
			//#MIDP_EXCLUDE_END
    		) {
      myName = id.getLocalName();
      myHap = id.getHap();
      
      synchronized (this) { // Mutual exclusion with Agent.addPlatformAddress()
        myAID = id;
        myToolkit.setPlatformAddresses(myAID);
      }

      //myThread = rm.getThread(ResourceManager.USER_AGENTS, getLocalName(), this);    
      myThread = t;
      myThread.start();
    }
  }

	//#MIDP_EXCLUDE_BEGIN
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
    //principalLock = new Object();
    pendingTimers = new AssociationTB();
    theDispatcher = TimerDispatcher.getTimerDispatcher();
    // restore O2AQueue
    if (o2aQueueSize > 0) 
	o2aQueue = new ArrayList(o2aQueueSize);
    o2aLocks = new HashMap();
    myToolkit = DummyToolkit.instance();

     //For persistence service
     persistentPendingTimers = new java.util.HashSet();

  }


	//#MIDP_EXCLUDE_END

  private void mainLoop() throws InterruptedException, InterruptedIOException {
  	while(myAPState != AP_DELETED) {
			//#MIDP_EXCLUDE_BEGIN
      try {
			//#MIDP_EXCLUDE_END

	// Check for Agent state changes
	switch(myAPState) {
	case AP_WAITING:
	  waitUntilWake(0);
	  break;
	case AP_SUSPENDED:
	  waitUntilActivate();
	  break;
	//#MIDP_EXCLUDE_BEGIN
	case AP_TRANSIT:
	  try {
		notifyMove();
	  } catch (Exception e) {
	  	// something went wrong
	  	changeStateTo(myBufferedState);
			myDestination = null;
			if (e instanceof AuthException) {
				// Will be caught together with all other AuthException-s
				throw (AuthException) e;
	  	}
	  	else {
	  		e.printStackTrace();
	  	}
	  }  
	  if(myAPState == AP_GONE) {
	    beforeMove();
	    return;
	  }
	  break;
	case AP_COPY:
	  beforeClone();
	  try {
	  notifyCopy();
	  } catch (Exception e) {
	  	// something went wrong
	  	changeStateTo(myBufferedState);
			myDestination = null;
			if (e instanceof AuthException) {
				// Will be catched together with all other AuthException-s
				throw (AuthException) e;
	  	}
	  	else {
	  		e.printStackTrace();
	  	}
	  }
	  doExecute();
	  break;
	case AP_SAVING:
	    try {
		beforeSave();
		notifySave();
	    }
	    catch(Exception e) {
		// Something went wrong
		e.printStackTrace();
	    }
	    finally {
		doExecute();
		myRepository = null;
	    }
	    // FIXME: Should an user-defined afterSave() method be called?
	    break;
	case AP_RELOADING:
	    try {
		beforeReload();
		notifyReload();
		return;
	    }
	    catch(Exception e) {
		// Something went wrong
		e.printStackTrace();
		doExecute();
	    }
	    break;
	case AP_FROZEN:
	  try {
	      beforeFreeze();
	      notifyFreeze();
	      return;
	  }
	  catch (Exception e) {
	      // something went wrong
	      e.printStackTrace();
	      doExecute();
	  }
	  break;
	//#MIDP_EXCLUDE_END
	case AP_ACTIVE:
	  try {
	    // Select the next behaviour to execute
	    currentBehaviour = myScheduler.schedule();

	    // Remember how many messages arrived
	    int oldMsgCounter = messageCounter;

	    // Just do it!
	    currentBehaviour.actionWrapper();

	    // If the current Behaviour has blocked and more messages arrived
	    // in the meanwhile, restart the behaviour to give it another chance
	    if((oldMsgCounter != messageCounter) && (!currentBehaviour.isRunnable()))
	      currentBehaviour.restart();

	    // When it is needed no more, delete it from the behaviours queue
	    if(currentBehaviour.done()) {
	      currentBehaviour.onEnd();
	      myScheduler.remove(currentBehaviour);
	      currentBehaviour = null;
	    }
	    else {
	      synchronized(myScheduler) {
					// Need synchronized block (Crais Sayers, HP): What if
					// 1) it checks to see if its runnable, sees its not,
					//    so it begins to enter the body of the if clause
					// 2) meanwhile, in another thread, a message arrives, so
					//    the behaviour is restarted and moved to the ready list.
					// 3) now back in the first thread, the agent executes the
					//    body of the if clause and, by calling block(), moves
					//   the behaviour back to the blocked list.
					if(!currentBehaviour.isRunnable()) {
		  			// Remove blocked behaviour from ready behaviours queue
		  			// and put it in blocked behaviours queue
		  			myScheduler.block(currentBehaviour);
		  			currentBehaviour = null;
					}
	      }
	    }
	  }
	  // Someone interrupted the agent. It could be a kill, a
	  // move/clone or a suspend-while-idle request from the outside...
	  catch(InterruptedException ie) {
	    switch(myAPState) {
	    case AP_DELETED:
	      throw new AgentDeathError();
	    //#MIDP_EXCLUDE_BEGIN
	    case AP_TRANSIT:
	    case AP_COPY:
	      throw new AgentInMotionError();
	    //#MIDP_EXCLUDE_END
	    case AP_ACTIVE:
	    case AP_IDLE:
	      System.out.println("WARNING: Spurious wakeup for agent " + getLocalName());
	      break;
	    }
	  } // end catch
	  break;
	}  // END of switch on agent state

	// Now give CPU control to other agents
	Thread.yield();
	//#MIDP_EXCLUDE_BEGIN
    }
    catch(AgentInMotionError aime) {
	// Do nothing, since this is a doMove() or doClone() from the outside.
    }
    catch(AuthException e) {
    	// If there is an AuthException we don't want the agent to be killed
    	// as for other unexpected exceptions.
		  // FIXME: maybe should send a message to the agent
		  System.out.println("AuthException: "+e.getMessage() );
		}
	//#MIDP_EXCLUDE_END
    } // END of while
  }

  private void waitUntilWake(long millis) {
    synchronized(msgQueue) {
      //long timeToWait = millis;
      //while(myAPState == AP_WAITING) {
			try {
			  //long startTime = System.currentTimeMillis();
			  // Blocks on msgQueue monitor for a while
			  waitOn(msgQueue, /*timeToWait*/ millis);
		    changeStateTo(AP_ACTIVE);
			  //long elapsedTime = System.currentTimeMillis() - startTime;

			  // If this was a timed wait, update time to wait; if the
			  // total time has passed, wake up.
			  /*if(millis != 0) {
			    timeToWait -= elapsedTime;
		
			    if(timeToWait <= 0)
			    changeStateTo(AP_ACTIVE);
			  }*/
			}
			catch(InterruptedException ie) {
			  switch(myAPState) {
			  case AP_DELETED:
			    throw new AgentDeathError();
			  //#MIDP_EXCLUDE_BEGIN
			  case AP_TRANSIT:
			  case AP_COPY:
			  case AP_FROZEN:
			  case AP_SAVING:
			    throw new AgentInMotionError();
			  //#MIDP_EXCLUDE_END
			  default:
			  	if (Thread.currentThread().equals(myThread)) {
			  		System.out.println("Agent "+getName()+" interrupted in waiting state");
			  	}
			  }
			}
    }
  }

  private void waitUntilActivate() {
    synchronized(suspendLock) {
      while(myAPState == AP_SUSPENDED) {
  try {
  	waitOn(suspendLock, 0);
	}
	catch(InterruptedException ie) {
	  switch(myAPState) {
	  case AP_DELETED:
	    throw new AgentDeathError();
	  //#MIDP_EXCLUDE_BEGIN
	  case AP_TRANSIT:
	  case AP_COPY:
	    // Undo the previous clone or move request
	    changeStateTo(AP_SUSPENDED);
	  //#MIDP_EXCLUDE_END
	  }
	}
      }
    }
  }

	private void destroy() { 
	    // Remove all pending timers
	    pendingTimers.clear();
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
	public final void send(final ACLMessage msg) {
			// set the sender of the message if not yet set
			// FIXME. Probably we should always set the sender of the message!
			try {
				msg.getSender().getName().charAt(0);
			}
			catch (Exception e) {
				msg.setSender(myAID);
			}
		//#MIDP_EXCLUDE_BEGIN
		notifySend(msg);
		/*try {
			doPrivileged(new jade.security.PrivilegedExceptionAction() {
				public Object run() throws AuthException {
					notifySend(msg);
					return null;
				}
			});
		}
		catch (AuthException e) {
			System.out.println("AuthException: "+e.getMessage() );;
		} 
		catch (Exception e) {
			e.printStackTrace();
		}*/ 
		//#MIDP_EXCLUDE_END
		/*#MIDP_INCLUDE_BEGIN
		myToolkit.handleSend(msg, myAID);
    //try {
    //  myToolkit.handleSend(msg, myAID);
    //} 
    //catch (AuthException ae) {
    //} 
		#MIDP_INCLUDE_END*/
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
		return receive(null);
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
		synchronized (msgQueue) {
			for (Iterator messages = msgQueue.iterator(); messages.hasNext(); ) {
				final ACLMessage cursor = (ACLMessage)messages.next();
				if (pattern == null || pattern.match(cursor)) {
					//try {
						//messages.remove(); 
						msgQueue.remove(cursor);
						//#MIDP_EXCLUDE_BEGIN
						notifyReceived(cursor);
						//#MIDP_EXCLUDE_END
						currentMessage = cursor;
						msg = cursor;
						break; // Exit while loop
					//}
					//catch (Exception e) {
					//		e.printStackTrace();
						// Continue loop, discard message
					//}
				}
			}
		}
		return msg;
	}
  /**
     Receives an <b>ACL</b> message from the agent message
     queue. This method is blocking and suspends the whole agent until
     a message is available in the queue. 
     @return A new ACL message, blocking the agent until one is
     available.
     @see jade.core.Agent#receive()
     @see jade.lang.acl.ACLMessage
  */
  public final ACLMessage blockingReceive() {
    //ACLMessage msg = null;
    //while(msg == null) {
      return blockingReceive(null, 0);
    //}
    //return msg;
  }

  /**
     Receives an <b>ACL</b> message from the agent message queue,
     waiting at most a specified amount of time.
     @param millis The maximum amount of time to wait for the message.
     @return A new ACL message, or <code>null</code> if the specified
     amount of time passes without any message reception.
   */
  public final ACLMessage blockingReceive(long millis) {
    /*synchronized(msgQueue) {
      ACLMessage msg = receive();
      if(msg == null) {
	doWait(millis);
	msg = receive();
      }
      return msg;
    }*/
    return blockingReceive(null, millis);
  }

  /**
     Receives an <b>ACL</b> message matching a given message
     template. This method is blocking and suspends the whole agent
     until a message is available in the queue. 
     @param pattern A message template to match received messages
     against.
     @return A new ACL message matching the given template, blocking
     until such a message is available.
     @see jade.core.Agent#receive(MessageTemplate)
     @see jade.lang.acl.ACLMessage
     @see jade.lang.acl.MessageTemplate
  */
  public final ACLMessage blockingReceive(MessageTemplate pattern) {
    //ACLMessage msg = null;
    //while(msg == null) {
    	return blockingReceive(pattern, 0);
    //}
    //return msg;
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
    synchronized(msgQueue) {
      msg = receive(pattern);
      long timeToWait = millis;
      while(msg == null) {
				long startTime = System.currentTimeMillis();
				//#MIDP_EXCLUDE_BEGIN
				if (Thread.currentThread().equals(myThread)) {
					doWait(timeToWait);
				}
				else {
					// blockingReceive() called from an external thread --> Do not change the agent state
					waitUntilWake(timeToWait);
				}
				//#MIDP_EXCLUDE_END
				/*#MIDP_INCLUDE_BEGIN
			  // As Thread.interrupt() is substituted by interruptThread(),
			  // it is possible to enter this method with the agent state
			  // equals to AP_DELETED. If this is the case the loop
			  // lasts forever as doWait() does nothing -->the monitor
			  // of the msgQueue is not released --> even if a message arrive
			  // the postMessage method can't be executed.
			  // Throwing AgentDeathError is necessary to exit this infinite loop.
			  if (myAPState == AP_ACTIVE) {
					if (Thread.currentThread().equals(myThread)) {
						doWait(timeToWait);
					}
					else {
						// blockingReceive() called from an external thread --> Do not change the agent state
						waitUntilWake(timeToWait);
					}
			  } 
			  else {
			    throw new AgentDeathError();
			  }
				#MIDP_INCLUDE_END*/
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
    synchronized(msgQueue) {
      msgQueue.addFirst(msg);
    }
  }



  final void setToolkit(AgentToolkit at) {
    myToolkit = at;
  }

  final void resetToolkit() {
  	//#MIDP_EXCLUDE_BEGIN
    myToolkit = DummyToolkit.instance();
  	//#MIDP_EXCLUDE_END
  	/*#MIDP_INCLUDE_BEGIN
    myToolkit = null;
  	#MIDP_INCLUDE_END*/
  }


    //#APIDOC_EXCLUDE_BEGIN

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

    //#APIDOC_EXCLUDE_END

  
  // Event firing methods

  // Notify creator that the start-up phase has completed
  private synchronized void notifyStarted() {
    notifyAll();
  }

  // Notify toolkit of the destruction of the current agent
  private void notifyDestruction() {
    myToolkit.handleEnd(myAID);
  }

  //#MIDP_EXCLUDE_BEGIN
  // Notify toolkit that a message was posted in the message queue
  private void notifyPosted(ACLMessage msg) /*throws AuthException*/ {
    myToolkit.handlePosted(myAID, msg);
  }

  // Notify toolkit that a message was extracted from the message
  // queue
  private void notifyReceived(ACLMessage msg) /*throws AuthException*/ {
    myToolkit.handleReceived(myAID, msg);
  }

  // Notify toolkit of the need to send a message
  private void notifySend(ACLMessage msg) /*throws AuthException*/ {
  	myToolkit.handleSend(msg, myAID);
  }

  // Notify toolkit of the need to move the current agent
  private void notifyMove() throws AuthException, IMTPException, NotFoundException {
      try {
	  AgentMobilityHelper h = (AgentMobilityHelper)getHelper(AgentMobilityHelper.NAME);
	  h.informMoved(myAID, myDestination);
      }
      catch(ServiceNotActiveException snae) {
	  // The mobility service is not installed. Abort migration.
	  doExecute();
      }
      catch(ServiceException se) {
	  // Some other error occurred. Dump the exception and abort migration.
	  se.printStackTrace();
	  doExecute();
      }
  }

  // Notify toolkit of the need to copy the current agent
  private void notifyCopy() throws AuthException, IMTPException, NotFoundException, NameClashException {
      try {
	  AgentMobilityHelper h = (AgentMobilityHelper)getHelper(AgentMobilityHelper.NAME);
	  h.informCloned(myAID, myDestination, myNewName);
      }
      catch(ServiceNotActiveException snae) {
	  // The mobility service is not installed. Abort migration.
	  doExecute();
      }
      catch(ServiceException se) {
	  // Some other error occurred. Dump the exception and abort migration.
	  se.printStackTrace();
	  doExecute();
      }
  }

  private void notifySave() throws ServiceException, NotFoundException, IMTPException {
      try {

	  // FIXME: There should be a constant defined somewhere in the JADE distribution
	  getHelper("jade.core.persistence.Persistence");
	  myToolkit.handleSave(myAID, myRepository);
      }
      catch(ServiceException se) {
	  // Do nothing...
      }
  }

  private void notifyReload() throws ServiceException, NotFoundException, IMTPException {
      try {

	  // FIXME: There should be a constant defined somewhere in the JADE distribution
	  getHelper("jade.core.persistence.Persistence");
	  myToolkit.handleReload(myAID, myRepository);
      }
      catch(ServiceException se) {
	  // Do nothing...
      }
  }

  private void notifyFreeze() throws ServiceException, NotFoundException, IMTPException {
      try {

	  // FIXME: There should be a constant defined somewhere in the JADE distribution
	  getHelper("jade.core.persistence.Persistence");
	  myToolkit.handleFreeze(myAID, myRepository, myBufferContainer);
      }
      catch(ServiceException se) {
	  // Do nothing...
      }
  }


  // Notify toolkit of the added behaviour
  // Package scooped as it is called by the Scheduler
  void notifyAddBehaviour(Behaviour b) {
  	if (generateBehaviourEvents) {
	    myToolkit.handleBehaviourAdded(myAID, b);
  	}
  }
  
  // Notify the toolkit of the removed behaviour
  // Package scooped as it is called by the Scheduler
 	void notifyRemoveBehaviour(Behaviour b) {
  	if (generateBehaviourEvents) {
    	myToolkit.handleBehaviourRemoved(myAID, b);
  	}
  }
  

    //#APIDOC_EXCLUDE_BEGIN

  // Notify the toolkit of the change in behaviour state
  // Public as it is called by the Scheduler and by the Behaviour class 
  public void notifyChangeBehaviourState(Behaviour b, String from, String to) {
  	if (generateBehaviourEvents) {
    	myToolkit.handleChangeBehaviourState(myAID, b, from, to);
  	}
  }

  public void setGenerateBehaviourEvents(boolean b) {
  	generateBehaviourEvents = b;
  }

    //#APIDOC_EXCLUDE_END


  // For persistence service
  private boolean getGenerateBehaviourEvents() {
      return generateBehaviourEvents;
  }

  
  // Notify toolkit that the current agent has changed its state
  private void notifyChangedAgentState(int oldState, int newState) {
    AgentState from = AgentState.getInstance(oldState);
    AgentState to = AgentState.getInstance(newState);
    myToolkit.handleChangedAgentState(myAID, from, to);
  }
  
  // Notify toolkit that the current agent has changed its principal
  /*private void notifyChangedAgentPrincipal(AgentPrincipal from, CertificateFolder certs) {
    myToolkit.handleChangedAgentPrincipal(myAID, from, certs);
  }*/
  //#MIDP_EXCLUDE_END

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
	public final void postMessage(final ACLMessage msg) {
		synchronized (msgQueue) {
			if (msg != null) {
				//#MIDP_EXCLUDE_BEGIN
				notifyPosted(msg);
				/*try {
					doPrivileged(new PrivilegedExceptionAction() {
						public Object run() throws AuthException {
							// notification appens first so, if an exception
							// is thrown, then message isn't appended to queue
							notifyPosted(msg);
							msgQueue.addLast(msg);
							return null;
						}
					});
				}
				catch (AuthException e) {
		  			System.out.println("AuthException: "+e.getMessage() );
					//e.printStackTrace();
				}
				catch (Exception e) {
					e.printStackTrace();
				}*/
				//#MIDP_EXCLUDE_END
				msgQueue.addLast(msg);
				doWake();
				messageCounter++;
			}
		}
	}

	//#CUSTOM_EXCLUDE_BEGIN
	private jade.content.ContentManager theContentManager = null;

	/**
	* Retrieves the agent's content manager 
	* @return The content manager.
	*/
	public jade.content.ContentManager getContentManager() {
		if (theContentManager == null) {
			theContentManager = new jade.content.ContentManager();
		}
		return theContentManager;
	}

//#APIDOC_EXCLUDE_BEGIN

//#MIDP_EXCLUDE_BEGIN

  // all the agent's service helper
  private transient Hashtable helpersTable;

  /**
  * Retrieves the agent's service helper
  * @return The service helper.
  */
  public ServiceHelper getHelper( String serviceName ) throws ServiceException {

      ServiceHelper se = null;

  	if (helpersTable == null) {
  		helpersTable = new Hashtable();
  	}

          try {
          // is the helper already into the agent's helpersTable ?
          if (helpersTable.get(serviceName)!=null) {
                  // there is already one into the helpersTable
                  se = (ServiceHelper) helpersTable.get(serviceName);
          } else {
                  // there isn't, request its creation
                  se = myToolkit.getHelper(this, serviceName);
                  se.init(this);
                  helpersTable.put(serviceName, se);

          }
          } catch(Exception e ) {
                  System.out.println(" ServiceHelper could not be created:"+ serviceName);
                  e.printStackTrace();
          }

      return se;
  }

//#MIDP_EXCLUDE_END

//#APIDOC_EXCLUDE_END

	//#CUSTOM_EXCLUDE_END
	
	/**
	   Retrieve a configuration property set in the <code>Profile</code>
	   of the local container (first) or as a System property.
	   @param key the key that maps to the property that has to be 
	   retrieved.
	   @param aDefault a default value to be returned if there is no mapping
	   for <code>key</code>
	 */
	public String getProperty(String key, String aDefault) {
		String val = myToolkit.getProperty(key, aDefault);
		if (val == null || val.equals(aDefault)) {
			// Try among the System properties
			String sval = System.getProperty(key);
			if (sval != null) {
				val = sval;
			}
		}
		return val;
	}
	
  /**
     This method is used to interrupt the agent's thread.
     In J2SE/PJAVA it just calls myThread.interrupt(). In MIDP, 
     where interrupt() is not supported the thread interruption is 
     simulated as described below.
     The agent thread can be in one of the following four states:
     1) Running a behaviour.
     2) Sleeping on msgQueue due to a doWait()
     3) Sleeping on suspendLock due to a doSuspend()
     4) Sleeping on myScheduler due to a schedule() with no active behaviours
     The idea is: set the 'isInterrupted' flag, then wake up the
     thread wherever it may be
   */
  private void interruptThread() {
  	//#MIDP_EXCLUDE_BEGIN
  	myThread.interrupt();
  	//#MIDP_EXCLUDE_END
  	/*#MIDP_INCLUDE_BEGIN
  	synchronized (this) {
	    isInterrupted = true;
	
	    // case 1: Nothing to do.
	    // case 2: Signal on msgQueue.
	    synchronized (msgQueue) {msgQueue.notifyAll();} 
	    // case 3: Signal on suspendLock object.
	    synchronized (suspendLock) {suspendLock.notifyAll();} 
			// case 4: Signal on the Scheduler
			synchronized (myScheduler) {myScheduler.notifyAll();}
  	}
  	#MIDP_INCLUDE_END*/
  } 

  /**
     Since in MIDP Thread.interrupt() does not exist and a simulated
     interruption is used to "interrupt" the agent's thread, we must 
     check whether the simulated interruption happened just before and
     after going to sleep.
   */
  void waitOn(Object lock, long millis) throws InterruptedException {
  	/*#MIDP_INCLUDE_BEGIN
  	synchronized (this) {
	    if (isInterrupted) {
      	isInterrupted = false;
      	throw new InterruptedException();
			}
    } 
  	#MIDP_INCLUDE_END*/
    lock.wait(millis);
  	/*#MIDP_INCLUDE_BEGIN
  	synchronized (this) {
	    if (isInterrupted) {
      	isInterrupted = false;
      	throw new InterruptedException();
			}
    } 
  	#MIDP_INCLUDE_END*/
  }

    //#MIDP_EXCLUDE_BEGIN

    // For persistence service
    private void setBufferedState(int bs) {
	myBufferedState = bs;
    }

    // For persistence service
    private int getBufferedState() {
	return myBufferedState;
    }

    // For persistence service -- Hibernate needs java.util collections
    private java.util.Set getBehaviours() {
	Behaviour[] behaviours = myScheduler.getBehaviours();
	java.util.Set result = new java.util.HashSet();
	result.addAll(java.util.Arrays.asList(behaviours));

	return result;
    }

    // For persistence service -- Hibernate needs java.util collections
    private void setBehaviours(java.util.Set behaviours) {
	Behaviour[] arr = new Behaviour[behaviours.size()];

	arr = (Behaviour[])behaviours.toArray(arr);

	// Reconnect all the behaviour -> agent pointers
	for(int i = 0; i < arr.length; i++) {
	    arr[i].setAgent(this);
	}

	myScheduler.setBehaviours(arr);
    }


    // For persistence service -- Hibernate needs java.util collections
    private transient java.util.Set persistentPendingTimers = new java.util.HashSet();


    // For persistence service -- Hibernate needs java.util collections
    private java.util.Set getPendingTimers() {
	return persistentPendingTimers;
    }

    // For persistence service -- Hibernate needs java.util collections
    private void setPendingTimers(java.util.Set timers) {

	if(!persistentPendingTimers.equals(timers)) {
	    // Clear the timers table, and install the new timers.
	    pendingTimers.clear();

	    java.util.Iterator it = timers.iterator();
	    while(it.hasNext()) {
		TBPair pair = (TBPair)it.next();
		pendingTimers.addPair(pair);
	    }
	}

	persistentPendingTimers = timers;
	
    }

    //#MIDP_EXCLUDE_END

}
