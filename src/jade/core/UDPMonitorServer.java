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

//#MIDP_EXCLUDE_FILE
//#APIDOC_EXCLUDE_FILE

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import jade.util.Logger;


/**
 * The <code>UDPMonitorServer</code> is used by any instance of the class
 * <code>UDPNodeFailureMonitor</code> to receive UDP ping messages from nodes.
 * 
 * The server is only running if there are nodes to supervise. By default it
 * waits at port 28000 for incomming UDP datagrams. The maximum time between
 * two ping messages is by default 3 second. After 5 seconds a node is marked 
 * for removing from the platform.
 * <p>
 * The server can be configured with the following profile parameters:
 * <p>
 * <table border="1" cellspacing="0">
 *  <tr>
 *    <td>-udpmonitoring_port:</td>
 *    <td>number of the port on which the server is waiting for ping messages.</td>
 *  </tr>
 *  <tr> 
 *    <td>-udpmonitoring_ping_delay_limit:</td>
 *    <td>maximum time between two pings messages.</td>
 *  </tr>
 *  <tr>
 *    <td>-udpmonitoring_unreachable_limit:</td>
 *    <td>maximum time a node can be in the state unreachable, before it gets marked for removing from the platform.</td>
 *  </tr>
 * </table>
 * 
 * @author Roland Mungenast - Profactor
 * @since JADE 3.3
 */
public class UDPMonitorServer {

  /**
   * Default port on which the server is waiting for ping messages
   */
  public static int DEFAULT_PORT = 28000;
  
  /**
   * Default maximum time the server waits for a ping
   */
  public static int DEFAULT_PING_DELAY_LIMIT = 3000;
  
  /**
   * Default maximum time a node can stay unreachable
   */
  public static int DEFAULT_UNREACHABLE_LIMIT = 5000;
  
  private static UDPMonitorServer instance;
  private Logger logger;
  
  private int port = DEFAULT_PORT;
  private int pingDelayLimit = DEFAULT_PING_DELAY_LIMIT;
  private int unreachLimit= DEFAULT_UNREACHABLE_LIMIT;
  
  private DatagramChannel server;
  private Selector selector; 
  private Map targets;
 
  private Thread pingHandler;
  
  private Timer timer;
  private HashMap deadlines;
  
  private Object shuttingDown = new Object();
  
  /**
   * Class to store a deadline for the next ping
   * of a targeted node
   */
  private class Deadline extends TimerTask {
    
    private String nodeID;
    
    public Deadline(String nodeID) {
      this.nodeID = nodeID;
    }
    
    public String getNodeID() {
      return nodeID;
    }

    public void run() {
      UDPNodeFailureMonitor mon;
      synchronized (targets) {
        mon = (UDPNodeFailureMonitor)targets.get(nodeID);
      }
      
      if (mon != null && mon.getDeadline() < System.currentTimeMillis()) {
        boolean newPing;
      
        boolean contains = false;
        synchronized(targets) {
          contains = targets.containsKey(nodeID);
        }
        
        if (contains) { // node is still supervised 
          timeout(nodeID); 
        }
      }
    }
  }
  
  /**
   * Class to handles incomming ping messages
   */
  private class PingHandler implements Runnable {

    private final byte TERMINATING_INFO = 1;  // bit 1
    
    private void handlePing() throws IOException {
      // allocate maximum size of one UDP packet
      ByteBuffer datagramBuffer = ByteBuffer.allocate(1<<16);
      SocketAddress address = server.receive(datagramBuffer);
      datagramBuffer.position(0);
      
      if (address != null) {
    
        int nodeIDLength = datagramBuffer.getInt();
        
        // get node ID
        byte[] bb = new byte[nodeIDLength];
        datagramBuffer.get(bb, 0, nodeIDLength);
        String nodeID = new String(bb);
        
        // analyse info byte
        byte info = datagramBuffer.get();
        boolean isTerminating = false;
        if ((info & TERMINATING_INFO) != 0) {
          isTerminating = true;
        }
        
        // cancel arleady existing deadline
        TimerTask currDeadline = (TimerTask) deadlines.get(nodeID);
        if (currDeadline != null) {
          currDeadline.cancel();
        }
            
        pingReceived(nodeID, isTerminating);
      }
    }
   
    public void run() {
      for(;;) { // endless loop
        try {
          selector.select();

          Set keys = selector.selectedKeys();
          Iterator i = keys.iterator();
        
          while (i.hasNext()) {
            SelectionKey key = (SelectionKey) i.next();
            i.remove();
            handlePing();
          } 
        } catch (IOException e) {
          if(logger.isLoggable(Logger.SEVERE))
            logger.log(Logger.SEVERE,"UDP Connection error. "+ e);
        }
      } // for
    }
  }
 
  
  /**
   * Constructs a new UDPMonitorServer object
   * @throws IOException if the UDP server cannot be started up
   */
  private UDPMonitorServer() {
    logger = Logger.getMyLogger(this.getClass().getName());
    deadlines = new HashMap();
    targets = new HashMap();
    timer = new Timer();
  }
  
  /**
   * Starts the UDP server
   */
  private synchronized void startServer() {
    try {
      // Start UDP server
        server = DatagramChannel.open();
        server.configureBlocking(false);
        server.socket().setReuseAddress(true);
        server.socket().bind(new InetSocketAddress(Profile.getDefaultNetworkName(), port));
      
        // Create and register Selector
        selector = Selector.open();
        server.register(selector, SelectionKey.OP_READ);
      
        // Start PingHandler thread
        pingHandler = new Thread(new PingHandler());
        pingHandler.setName("UDPNodeFailureMonitor-PingHandler");
        pingHandler.start();
      
        if(logger.isLoggable(Logger.INFO))
          logger.log(Logger.INFO,"UDP monitoring server initialized and started. (port: " + port + 
              ", ping_delay_limit: " + pingDelayLimit + ", unreachable_limit: " + unreachLimit + ")"); 
     
    } catch (IOException e) {
      if(logger.isLoggable(Logger.SEVERE))
        logger.log(Logger.SEVERE,"UDP monitoring server cannot be started. "+ e);
    }
  }
  
  /**
   * Stops the UDP server
   */
  private synchronized void stopServer() {
    try {
//        pingHandler.stop();
//        server.close();
//        deadlines.clear();
//        targets.clear();
     
        if(logger.isLoggable(Logger.INFO))
          logger.log(Logger.INFO,"UDP monitoring server has been stopped.");
   
    } catch (Exception e) {
      if(logger.isLoggable(Logger.SEVERE))
        logger.log(Logger.SEVERE,"Error shutting down the UDP monitor server. "+ e);
    }
  }
  
  /**
   * Returns an instance of the <code>UDPMonitorServer</code>
   * @throws IOException if the UDP server cannot be started up
   */
  public static UDPMonitorServer getInstance(Profile p) {
    if (instance == null) {
      instance = new UDPMonitorServer();
      instance.init(p);
    }
    return instance;
  }
  
  /**
   * Registers a <code>UDPNodeFailureMonitor</code>.
   * All nodes targeted by this monitor are now supervised
   * by the <code>UDPMonitorServer</code>. Its listener
   * gets informed about any state changes.
   */
  public void register(UDPNodeFailureMonitor m) {
    
    int size = 0;
    synchronized (targets) {
      size = targets.size();
    }
    if (size == 0)  {
      startServer();
    }
    String nodeID = m.getNode().getName();
    synchronized(targets) {
      targets.put(nodeID, m);
    }
    addDeadline(nodeID, pingDelayLimit);
  }
  
  /**
   * Deregisters a <code>UDPNodeFailureMonitor</code> and all
   * its targeted nodes from monitoring.
   */
  public void deregister(UDPNodeFailureMonitor m) {
    int size = 0;
    synchronized (targets) {
      String nodeId = m.getNode().getName();
      targets.remove(nodeId);
      Deadline oldDeadline = (Deadline) deadlines.remove(nodeId);
      if (oldDeadline != null)
        oldDeadline.cancel();
      size = targets.size();
    }

    if (size == 0) {
      stopServer();
    }
  }
  
  /**
   * This method is invoced by a PingHandler thread when a 
   * new ping message has been received
   * @param nodeID identification of the sender node
   * @param isTerminating true if the sender is currently shutting down
   */
  protected void pingReceived(String nodeID, boolean isTerminating) {
      
      if (logger.isLoggable(Logger.FINEST)) {
        logger.log(Logger.FINEST, "UDP ping message for node '" + nodeID + 
            "' received. (termination-flag: " + isTerminating + ")");
      }
      
      UDPNodeFailureMonitor mon;
      synchronized (targets) {
        mon = (UDPNodeFailureMonitor)targets.get(nodeID);
      }
      if (mon != null) {
        mon.setLastPing(System.currentTimeMillis()); // update time for last ping

        addDeadline(nodeID, pingDelayLimit);
        
        int state = mon.getState();
        
        // state transitions
        if (state == UDPNodeFailureMonitor.CONNECTED && isTerminating) {
          mon.setState(UDPNodeFailureMonitor.FINAL);
        
        } else if (state == UDPNodeFailureMonitor.UNREACHABLE && !isTerminating) {
          mon.setState(UDPNodeFailureMonitor.CONNECTED);
          addDeadline(nodeID, pingDelayLimit);
        
        } else if (state == UDPNodeFailureMonitor.UNREACHABLE && isTerminating) {
          mon.setState(UDPNodeFailureMonitor.FINAL);
        } 
    
      } else {
        if(logger.isLoggable(Logger.INFO))
          logger.log(Logger.INFO,"UDP ping message with the unknown node ID '" + nodeID + "' has been received");
      }
  }
  
  /**
   * This method is invoced by a TimeoutHandler at a timeout
   */
  protected void timeout(String nodeID) {
    UDPNodeFailureMonitor mon = null;
    synchronized (targets) {
      mon = (UDPNodeFailureMonitor)targets.get(nodeID);
    }
    int oldState = mon.getState();
    int newState = oldState;
    
    if (logger.isLoggable(Logger.FINEST)) {
        logger.log(Logger.FINEST, "Timeout for '" + nodeID + "'");
    }
    
    if (oldState == UDPNodeFailureMonitor.CONNECTED) {
      newState = UDPNodeFailureMonitor.UNREACHABLE;
      addDeadline(nodeID, unreachLimit);
      
    } else if (oldState == UDPNodeFailureMonitor.UNREACHABLE) {
      newState = UDPNodeFailureMonitor.FINAL;
    }
    
    if (newState != oldState)
      mon.setState(newState);
  }
  
  
  private void addDeadline(String nodeID, int delay) {
    Calendar now = Calendar.getInstance();
    now.add(Calendar.MILLISECOND, delay);
    Date time = now.getTime();
    
    TimerTask task = new Deadline(nodeID);
    
    
    deadlines.put(nodeID, task);
    timer.schedule(task, delay);
    synchronized (targets) {
      UDPNodeFailureMonitor mon = (UDPNodeFailureMonitor)targets.get(nodeID);
      mon.setDeadline(System.currentTimeMillis());
    }
  }
  
  ///////////////////////////////////////////////////
  // Methods used by jade.core.PlatformManagerImpl //
  // to set communication properies                //
  ///////////////////////////////////////////////////
  
  /**
   * Extracts an integer value from a given profile. If the value
   * is less than zero it returns the specified default value
   * @param p profile
   * @param paramName name of the parameter in the profile
   * @param defaultValue default value
   */
  private static int getPosIntValue(Profile p, String paramName, int defaultValue) {
    int value = Integer.valueOf(p.getParameter(paramName, "-1")).intValue();
    if (value >= 0) {
      return value;   
    } else {
     return defaultValue; 
    }
  }
  
  /**
   * Overwrites default values of with parameters specified in the profile
   * @param p profile with new values
   */
  private void init(Profile p) {
    port = getPosIntValue(p, Profile.UDP_MONITORING_PORT, DEFAULT_PORT);
    pingDelayLimit = getPosIntValue(p, Profile.UDP_MONITORING_PING_DELAY_LIMIT, DEFAULT_PING_DELAY_LIMIT);
    unreachLimit= getPosIntValue(p, Profile.UDP_MONITORING_UNREACHABLE_LIMIT, DEFAULT_UNREACHABLE_LIMIT);
  }
}

