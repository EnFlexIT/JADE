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
// Take care that the DOTNET build file (dotnet.xml) uses this file (it is copied just after the preprocessor excluded it)
//#J2ME_EXCLUDE_FILE

import jade.core.Profile;

import java.io.IOException;
import java.nio.ByteBuffer;

//#DOTNET_EXCLUDE_BEGIN
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
//#DOTNET_EXCLUDE_END

/*#DOTNET_INCLUDE_BEGIN
import System.Net.*;
import System.Net.Sockets.*;
#DOTNET_INCLUDE_END*/

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
 * @author Federico Pieri - ERXA
 * @since JADE 3.3.NET
 */
class UDPMonitorServer {

  private static UDPMonitorServer instance;
  private Logger logger;
  
  private static int port;
  private static int pingDelayLimit;
  private static int unreachLimit;
  
  //#DOTNET_EXCLUDE_BEGIN
  private DatagramChannel server;
  private Selector selector; 
  //#DOTNET_EXCLUDE_END
  private Map targets;
   
  private PingHandler pingHandler;
  private Timer timer;
  private HashMap deadlines;
  
  /*#DOTNET_INCLUDE_BEGIN
  private Socket server;
  #DOTNET_INCLUDE_END*/

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
			
		  //#DOTNET_EXCLUDE_BEGIN
		  SocketAddress address = server.receive(datagramBuffer);
		  //#DOTNET_EXCLUDE_END

		  /*#DOTNET_INCLUDE_BEGIN
		   ubyte[] recData = new ubyte[datagramBuffer.getUByte().length];

		   if ( server != null)
		   {
		   try
		   {
		   if (server.get_Available() <= 0)
		   return;
		   }
		   catch (System.ObjectDisposedException ode)
		   {
		   return;
		   }
		   }
		   else
		   return;

		   try
		   {
		   server.Receive(recData, 0, server.get_Available(), SocketFlags.None);
		   }
		   catch (SocketException se)
		   {
		   int socketError = se.get_ErrorCode();
		   return;
		   }
		   IPEndPoint IPendPt  = (IPEndPoint) server.get_LocalEndPoint();
		   IPAddress address	= IPendPt.get_Address();
		   datagramBuffer.copyUByte(recData);
		   #DOTNET_INCLUDE_END*/

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
			  try  {
						//#DOTNET_EXCLUDE_BEGIN
				  selector.select();
          
				  Set keys = selector.selectedKeys();
				  interrupted = keys.size() == 0;
				  Iterator i = keys.iterator();
          
          while (i.hasNext()) {
					  SelectionKey key = (SelectionKey) i.next();
					  i.remove();
            if (key.isValid() && key.isReadable()) {
						//#DOTNET_EXCLUDE_END
						  handlePing();
						//#DOTNET_EXCLUDE_BEGIN
					  }
				  }
						//#DOTNET_EXCLUDE_END 
			  } 
			  catch (Exception e)  // .net requires I catch Exception instead of IOException
			  {
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
		//#DOTNET_EXCLUDE_BEGIN
		server = DatagramChannel.open();  
		//#DOTNET_EXCLUDE_END

		/*#DOTNET_INCLUDE_BEGIN
		server = new Socket(AddressFamily.InterNetwork, SocketType.Dgram, ProtocolType.Udp);
		#DOTNET_INCLUDE_END*/
	} catch (Exception e) { // .net requires I catch Exception instead of IOException
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

		//#DOTNET_EXCLUDE_BEGIN
		server.configureBlocking(false);
		server.socket().setReuseAddress(true);
		server.socket().bind(new InetSocketAddress(Profile.getDefaultNetworkName(), port));
		//#DOTNET_EXCLUDE_END

		/*#DOTNET_INCLUDE_BEGIN
		server = new Socket(AddressFamily.InterNetwork, SocketType.Dgram, ProtocolType.Udp);
		server.set_Blocking( false );
		server.SetSocketOption(SocketOptionLevel.Socket, SocketOptionName.ReuseAddress, 1);
		String defaultNetworkName = Profile.getDefaultNetworkName();
		System.Net.IPHostEntry hostEntry = System.Net.Dns.GetHostByName( defaultNetworkName );
		System.Net.IPAddress[] ipAddresses = hostEntry.get_AddressList();
		long ipAddressLong = ipAddresses[0].get_Address();
		if ( !server.get_Connected() )
		{
			IPEndPoint ipPoint = new IPEndPoint(IPAddress.Any, port);
			try
			{
				server.Bind( ipPoint );
			}
			catch(SocketException se)
			{
				int socketError = se.get_ErrorCode();
			}
		 }
		 #DOTNET_INCLUDE_END*/
      
		//#DOTNET_EXCLUDE_BEGIN
		// Create and register Selector
		selector = Selector.open();
		server.register(selector, SelectionKey.OP_READ);
		//#DOTNET_EXCLUDE_END

        // Start PingHandler thread
        pingHandler = new PingHandler("UDPNodeFailureMonitor-PingHandler");
        pingHandler.start();
      
        // start timer for deadlines
        timer = new Timer();
        
        if(logger.isLoggable(Logger.INFO))
          logger.log(Logger.INFO,"UDP monitoring server has been initialized and started."); 
     
        if(logger.isLoggable(Logger.CONFIG))
          logger.log(Logger.CONFIG,"(UDP port: " + port + ", ping_delay_limit: " + pingDelayLimit + ", unreachable_limit: " + unreachLimit + ")"); 
		} catch (Exception e) { // .net requires I catch Exception instead of IOException
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
      
	  //#DOTNET_EXCLUDE_BEGIN
	  server.disconnect();
	  //#DOTNET_EXCLUDE_END

	  /*#DOTNET_INCLUDE_BEGIN
	  server.Shutdown(SocketShutdown.Both);
	  server.Close();
	  #DOTNET_INCLUDE_END*/
       
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
}

