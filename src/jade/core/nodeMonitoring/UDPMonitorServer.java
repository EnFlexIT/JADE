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

package jade.core.nodeMonitoring;

//#APIDOC_EXCLUDE_FILE
//#MIDP_EXCLUDE_FILE

import jade.core.Profile;

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
 * 
 * @author Roland Mungenast - Profactor
 * @since JADE 3.3
 */
class UDPMonitorServer {

  private static UDPMonitorServer instance;
  private Logger logger;
  
  private static int port;
  private static int pingDelayLimit;
  private static int unreachLimit;
  
  private DatagramChannel server;
  private Selector selector; 
  private Map targets;
 
  private PingHandler pingHandler;
  private Timer timer;
  private HashMap deadlines;
  
  
  /**
   * Class to store a deadline for the next ping
   * of a targeted node
   */
  private class Deadline extends TimerTask {
    
    private String nodeID;
    private long time;
    
    public Deadline(String nodeID) {
      this.nodeID = nodeID;
      this.time = System.currentTimeMillis();
    }
    
    public long getTime() {
      return time; 
    }
    
    public String getNodeID() {
      return nodeID;
    }

    public void run() {
      synchronized (targets) {
        UDPNodeFailureMonitor mon = (UDPNodeFailureMonitor)targets.get(nodeID);
        
        // node is still supervised and there are no new deadlines
        if (mon != null && mon.getDeadline() == time) {
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
    private boolean interrupted = false;
    private Thread thread;
    
    public PingHandler(String name) {
      thread = new Thread(this, name);
    }
    
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
      while (!interrupted) { // endless loop
        try {
           
          selector.select();
          
          Set keys = selector.selectedKeys();
          interrupted = keys.size() == 0;
          Iterator i = keys.iterator();
          
          while (i.hasNext()) {
            SelectionKey key = (SelectionKey) i.next();
            i.remove();
            if (key.isValid() && key.isReadable()) {
              handlePing();
            }
          } 
          
        } catch (IOException e) {
          if(logger.isLoggable(Logger.SEVERE))
            logger.log(Logger.SEVERE,"UDP Connection error ");
        }
      } // for
    }
    
    public void start() {
      thread.start();
    }
    
    public void stop() {
      interrupted = true;
    }
  }
 
  
  /**
   * Constructs a new UDPMonitorServer object
   * @param p Profile including settings for the UDP monitor server
   * @throws IOException if the UDP server cannot be started up
   * 
   */
  UDPMonitorServer(int p, int pdl, int ul) {
  	port = p;
  	pingDelayLimit = pdl;
  	unreachLimit = ul;
  	
    logger = Logger.getMyLogger(this.getClass().getName());
    deadlines = new HashMap();
    targets = new HashMap();
    try {
      server = DatagramChannel.open();  
    } catch (IOException e) {
       if(logger.isLoggable(Logger.SEVERE))
        logger.log(Logger.SEVERE,"Cannot open UDP channel.");
    }
  }
  
  /**
   * Starts the UDP server
   */
  private synchronized void startServer() {
    try {
        // Start UDP server
        server.configureBlocking(false);
        server.socket().setReuseAddress(true);
        server.socket().bind(new InetSocketAddress(Profile.getDefaultNetworkName(), port));
      
        // Create and register Selector
        selector = Selector.open();
        server.register(selector, SelectionKey.OP_READ);

        // Start PingHandler thread
        pingHandler = new PingHandler("UDPNodeFailureMonitor-PingHandler");
        pingHandler.start();
      
        // start timer for deadlines
        timer = new Timer();
        
        if(logger.isLoggable(Logger.INFO))
          logger.log(Logger.INFO,"UDP monitoring server has been initialized and started."); 
     
        if(logger.isLoggable(Logger.CONFIG))
          logger.log(Logger.CONFIG,"(UDP port: " + port + ", ping_delay_limit: " + pingDelayLimit + ", unreachable_limit: " + unreachLimit + ")"); 
     
        
        
    } catch (IOException e) {
      if(logger.isLoggable(Logger.SEVERE))
        logger.log(Logger.SEVERE,"UDP monitoring server cannot be started");
    }
  }
  
  /**
   * Stops the UDP server
   */
  private synchronized void stopServer() {
    try {
      pingHandler.stop();
      timer.cancel();
      deadlines.clear();
      server.disconnect();
       
      if(logger.isLoggable(Logger.INFO))
        logger.log(Logger.INFO,"UDP monitoring server has been stopped.");
   
    } catch (Exception e) {
      if(logger.isLoggable(Logger.SEVERE))
        logger.log(Logger.SEVERE,"Error shutting down the UDP monitor server");
    }
  }
  
  /**
   * Returns an instance of the <code>UDPMonitorServer</code>
   * @param profile profile including settings for the monitoring
   * @throws IOException if the UDP server cannot be started up
   *
  public static UDPMonitorServer getInstance(Profile profile) {
    if (instance == null) {
      instance = new UDPMonitorServer(profile);
    }
    return instance;
  }*/
  
  /**
   * Registers a <code>UDPNodeFailureMonitor</code>.
   * All nodes targeted by this monitor are now supervised
   * by the <code>UDPMonitorServer</code>. Its listener
   * gets informed about any state changes.
   */
  public synchronized void register(UDPNodeFailureMonitor m) {
    if (targets.size() == 0)  {
      startServer();
    }
    String nodeID = m.getNode().getName();
    targets.put(nodeID, m);
    addDeadline(nodeID, pingDelayLimit);
  }
  
  /**
   * Deregisters a <code>UDPNodeFailureMonitor</code> and all
   * its targeted nodes from monitoring.
   */
  public synchronized void deregister(UDPNodeFailureMonitor m) {
      String nodeId = m.getNode().getName();
      targets.remove(nodeId);
      if (targets.size() == 0) {
        stopServer();
      }
  }
  
  /**
   * This method is invoced by a PingHandler thread when a 
   * new ping message has been received
   * @param nodeID identification of the sender node
   * @param isTerminating true if the sender is currently shutting down
   */
  protected synchronized void pingReceived(String nodeID, boolean isTerminating) {
      
      if (logger.isLoggable(Logger.FINEST)) {
        logger.log(Logger.FINEST, "UDP ping message for node '" + nodeID + 
            "' received. (termination-flag: " + isTerminating + ")");
      }
      
      UDPNodeFailureMonitor mon = (UDPNodeFailureMonitor)targets.get(nodeID);
      
      if (mon != null) {
        mon.setLastPing(System.currentTimeMillis()); // update time for last ping
        addDeadline(nodeID, pingDelayLimit);
        int state = mon.getState();
      
        // state transitions
        if (state == UDPNodeFailureMonitor.STATE_CONNECTED && isTerminating) {
          mon.setState(UDPNodeFailureMonitor.STATE_FINAL);
      
        } else if (state == UDPNodeFailureMonitor.STATE_UNREACHABLE && !isTerminating) {
          mon.setState(UDPNodeFailureMonitor.STATE_CONNECTED);
          addDeadline(nodeID, pingDelayLimit);
      
        } else if (state == UDPNodeFailureMonitor.STATE_UNREACHABLE && isTerminating) {
          mon.setState(UDPNodeFailureMonitor.STATE_FINAL);
        } 
    
      } else {
        if(logger.isLoggable(Logger.INFO))
          logger.log(Logger.INFO,"UDP ping message with the unknown node ID '" + nodeID + "' has been received");
      }
  }
  
  /**
   * This method is invoced by a TimeoutHandler at a timeout
   */
  protected synchronized void timeout(String nodeID) {
    UDPNodeFailureMonitor mon = (UDPNodeFailureMonitor)targets.get(nodeID);
    int oldState = mon.getState();
    int newState = oldState;
    
    if (logger.isLoggable(Logger.FINEST)) {
        logger.log(Logger.FINEST, "Timeout for '" + nodeID + "'");
    }
    
    if (oldState == UDPNodeFailureMonitor.STATE_CONNECTED) {
      newState = UDPNodeFailureMonitor.STATE_UNREACHABLE;
      addDeadline(nodeID, unreachLimit);
      
    } else if (oldState == UDPNodeFailureMonitor.STATE_UNREACHABLE) {
      newState = UDPNodeFailureMonitor.STATE_FINAL;
    }
    
    if (newState != oldState)
      mon.setState(newState);
  }
  
  
  private void addDeadline(String nodeID, int delay) {
    Calendar now = Calendar.getInstance();
    now.add(Calendar.MILLISECOND, delay);
    Date time = now.getTime();
    
    Deadline deadline = new Deadline(nodeID);
    UDPNodeFailureMonitor mon = (UDPNodeFailureMonitor)targets.get(nodeID);
    if (mon != null) {
      mon.setDeadline(deadline.getTime());
      deadlines.put(nodeID, deadline);
      timer.schedule(deadline, delay);
    }
  }
  
  /**
   * Initializes the server by overwriting default settings
   * @param p Profile including the setting
   *
  public static void init(Profile p) {
    port = getPosIntValue(p, UDPNodeFailureMonitor.UDP_MONITORING_PORT, DEFAULT_PORT);
    pingDelayLimit = getPosIntValue(p, UDPNodeFailureMonitor.UDP_MONITORING_PING_DELAY_LIMIT, DEFAULT_PING_DELAY_LIMIT);
    unreachLimit= getPosIntValue(p, UDPNodeFailureMonitor.UDP_MONITORING_UNREACHABLE_LIMIT, DEFAULT_UNREACHABLE_LIMIT);
  }*/
  
  ///////////////////////////////////////////////////
  // Helper methods                                //
  ///////////////////////////////////////////////////
  
  /**
   * Extracts an integer value from a given profile. If the value
   * is less than zero it returns the specified default value
   * @param p profile
   * @param paramName name of the parameter in the profile
   * @param defaultValue default value
   *
  private static int getPosIntValue(Profile p, String paramName, int defaultValue) {
    int value = Integer.valueOf(p.getParameter(paramName, "-1")).intValue();
    if (value >= 0) {
      return value;   
    } else {
     return defaultValue; 
    }
  }*/
}

