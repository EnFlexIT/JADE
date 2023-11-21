package test.udpmonitor;

import java.util.HashMap;
import java.util.Map;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * This agent receives events from a <code>AMSEventListenerAgent</code>
 * and provides static methods to get information about received events.
 * 
 * (used by tests in the <code>test.udpmonitor.tests</code> package)
 * 
 * @author Roland Mungenast - Profactor
 */
public class EventReceiverAgent extends Agent {

	// Map a type of event (e.g. ADDED_CONTAINER) to the number of events 
	// of that type stored as an Integer object.
  private static Map evMap = new HashMap();
  private static Object lock = new Object();
  
  /**
   * Gets the number of received events with the specified name
   * @param evName Name/Type of an event
   */
  public static synchronized int getEventCnt(String evName) {
  	Integer ii = (Integer) evMap.get(evName);
  	if (ii != null) {
  		return ii.intValue();
  	}
  	else {
  		return 0;
  	}
  }

  /**
   * Clears all the information about received events
   */
  public static synchronized void clear() {
    evMap.clear();
  }
  
  /**
   * Suspends the current thread until a new event has been received
   */
  public static void waitForEvent() {
    synchronized(lock) {
      try {
        lock.wait();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }
  
  /**
   * Suspends the current thread until a new event has been received
   * or the timeout has been reached
   */
  public static void waitForEvent(int timeout) {
    synchronized(lock) {
      try {
        lock.wait(timeout);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }
  
  // stores a new event in an internal map
  private static synchronized void storeEvent(String evName) {
    Integer ii = (Integer) evMap.get(evName);
    if (ii == null) {
      evMap.put(evName, new Integer(1));
    } 
    else {
      evMap.put(evName, new Integer(ii.intValue()+1));
    }

    // Notify threads blocked in waitForEvent()
    synchronized(lock) {
      lock.notifyAll();
    }
  }
  
  protected void setup() {
    addBehaviour(new CyclicBehaviour() {

      public void action() {
        // wait for new ACL messages including events
        ACLMessage msgIn = myAgent.receive();
        if (msgIn != null) {
	        storeEvent(msgIn.getContent());
        }
        else {
        	block();
        }
      }
    }
    );
  }
}

