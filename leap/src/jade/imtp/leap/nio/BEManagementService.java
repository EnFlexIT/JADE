package jade.imtp.leap.nio;

//#J2ME_EXCLUDE_FILE

import jade.core.Profile;
import jade.core.BaseService;
import jade.core.ServiceException;
import jade.util.Logger;
import jade.util.leap.Properties;
import jade.imtp.leap.ICPException;
import jade.imtp.leap.TransportProtocol;
import jade.imtp.leap.JICP.*;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.net.*;

import java.util.Iterator;
import java.util.Hashtable;
import java.util.Set;
import java.util.List;
import java.util.LinkedList;
import java.util.Vector;
import java.util.Enumeration;
import java.util.StringTokenizer;

/**
   This service handles BEContainer creation requests and manages IO operations
   for data exchanged between the created BEContainers and their FEContainers
   using java.nio
   
   Having this functionality implemented as a service allows propagating
   (through the ServiceHelper) BackEnd related events (e.g. disconnections and 
   reconnections) at the agent level.
   
   This service accepts the following configuration parameters:
   jade_imtp_leap_nio_BEManagementService_servers: <list of of IOEventServer ids separated by ';'>
   Actually this service is a collection of IOEventServer objects.
   Each IOEventServer opens and manages a server socket, accepts 
   BackEnd creation/connection requests and passes incoming data to BEDispatchers
   independently from the others.
   If the above parameter is not specified a single IOEventServer will
   be started and its ID will be <code>jade_imtp_leap_nio_BEManagementService</code>.
   All other parameters are related to a single IOEventServer and must 
   be specified in the form 
   <server-id>_<parameter>
   They are:
   <server-id>_local-host: 
   <server-id>_local-port:
   <server-id>_protocol: 
   <server-id>_leap-property-file:
   <server-id>_poolsize:
   
   @author Giovanni Caire - TILAB
 */
public class BEManagementService extends BaseService {
  public static final String NAME = "BEManagement";  
  private static final String PREFIX = "jade_imtp_leap_nio_BEManagementService_";
	
  private static final int DEFAULT_PORT = 2099;  
  private static final int DEFAULT_POOL_SIZE = 5;  
	
	private static final int INIT_STATE = 0;
	private static final int ACTIVE_STATE = 1;
	private static final int TERMINATING_STATE = 2;
	private static final int TERMINATED_STATE = 3;
	private static final int ERROR_STATE = -1;
 
  private Hashtable servers = new Hashtable(2);
  private Ticker myTicker;
  
	private Logger myLogger = Logger.getMyLogger(getClass().getName());
	
	/**
	   @return The name of this service.
	 */
	public String getName() {
		String className = getClass().getName();	
		return className.substring(0, className.indexOf("Service"));
	}
	
	/** 
	   This method is called by the JADE runtime just after this service 
	   has been installed.
	   It activates the IO event servers specified in the Profile and the
	   Ticker thread.
	 */	   
	public void boot(Profile p) throws ServiceException {
		// Get IDs of servers to install
		String defaultServerIDs = PREFIX.substring(0, PREFIX.length()-1);
		String serverIDs = p.getParameter(PREFIX+"servers", defaultServerIDs);
		Vector v = parseStringList(serverIDs, ';'); // FIXME: this method should go in Specifier and should be used in parseSpecifierList()
		
		// Activate all servers
		Enumeration e = v.elements();
		while (e.hasMoreElements()) {
			String id = (String) e.nextElement();
			try {
				IOEventServer srv = new IOEventServer();
				srv.init(id, p);
				srv.activate();
				servers.put(id, srv);
			}
			catch (Throwable t) {
				myLogger.log(Logger.WARNING, "Error activating IOEventServer "+id+". "+t);
				t.printStackTrace();
			}
		}
		if (servers.size() == 0) {
			throw new ServiceException("NO IO-Event-Server active");
		}
		
		// Activate the ticker
		long tickTime = 60000;
		try {
			tickTime = Long.parseLong(p.getParameter(PREFIX+"ticktime", null));
		}
		catch (Exception ex) {}
		myTicker = new Ticker(tickTime);
		myTicker.start();
	}
	
	/**
	   This is called by the JADE Runtime when this service is deinstalled.
	   It stops the Ticker thread and all active IO event servers.
	 */	   
	public void shutdown() {
		// Shutdown the Ticker
		if (myTicker != null) {
			myTicker.shutdown();
		}
		
		// Shutdown all servers
		Object[] ss = servers.values().toArray();
		for (int i = 0; i < ss.length; ++i) {
			((IOEventServer) ss[i]).shutdown();
		}
	}
	
	/**
	   Inner class IOEventServer.
	   This class asynchronously manages a server socket and all IO Events 
	   that happen on it and on all sockets opened through it.
	   The BEManagementService is basically a collection of these servers.
	 */
	private class IOEventServer implements PDPContextManager.Listener, JICPMediatorManager, Runnable {
		private String myID;
		private String myLogPrefix;
		private int state = INIT_STATE;
		
		private ServerSocketChannel mySSChannel;
		private Selector mySelector;
		private SelectionKey acceptSK;
	
  	private int mediatorCnt = 1;
  	private Hashtable mediators = new Hashtable();
		private Thread myThread;
		
		private String host;
		private int port;
		private Properties leapProps = new Properties();
	  private PDPContextManager  myPDPContextManager;
	  private TransportProtocol myProtocol;
	  private ConnectionFactory myConnectionFactory;
	  private ServerThreadPool myPool;

	  /**
	     Initialize this IOEventServer according to the Profile
	   */
		public void init(String id, Profile p) {
			myID = id;
			myLogPrefix = (PREFIX.startsWith(myID) ? "" : "Server "+myID+": ");
			
			// Local host
			host = p.getParameter(id+'_'+JICPProtocol.LOCAL_HOST_KEY, null);
			
			// Local port
	    port = DEFAULT_PORT;
			String strPort = p.getParameter(id+'_'+JICPProtocol.LOCAL_PORT_KEY, myID);
	    try {
	      port = Integer.parseInt(strPort);
	    } 
	    catch (Exception e) {
	  		// Keep default
	    } 
		  	
			// Protocol
			String protoManagerClass = p.getParameter(id+'_'+"protocol", null);
			ProtocolManager pm = null;
	    try {
	    	pm = (ProtocolManager) Class.forName(protoManagerClass).newInstance();
	    } 
	    catch (Exception e) {
	  		// Use JICP as default
	    	pm = new JICPPeer();
	    } 
	    myProtocol = pm.getProtocol();
	    myConnectionFactory = pm.getConnectionFactory();
		  	
			// Read the LEAP configuration properties
	    String fileName = p.getParameter(id+'_'+LEAP_PROPERTY_FILE, LEAP_PROPERTY_FILE_DEFAULT); 
			try {
				leapProps.load(fileName);
			}
			catch (Exception e) {
				myLogger.log(Logger.FINE, myLogPrefix+"Can't read LEAP property file "+fileName+". "+e);
				// Ignore: no back end properties specified
			}
			
			// Initialize the PDPContextManager if specified
			String pdpContextManagerClass = leapProps.getProperty(PDP_CONTEXT_MANAGER_CLASS);
			if (pdpContextManagerClass != null) {
				try {
					myLogger.log(Logger.INFO, myLogPrefix+"Loading PDPContextManager of class "+pdpContextManagerClass);
					myPDPContextManager = (PDPContextManager) Class.forName(pdpContextManagerClass).newInstance();
					myPDPContextManager.init(leapProps); 
					myPDPContextManager.registerListener(this);
				}
				catch (Throwable t) {
					myLogger.log(Logger.WARNING, myLogPrefix+" Cannot load PDPContext manager "+pdpContextManagerClass);
					t.printStackTrace();
					myPDPContextManager = null;
				}
			}
			
			// Servers pool size
			int poolSize = DEFAULT_POOL_SIZE;
			String strPoolSize = p.getParameter(id+'_'+"poolsize", null);
	    try {
	      poolSize = Integer.parseInt(strPoolSize);
	    } 
	    catch (Exception e) {
	  		// Keep default
	    } 
	    myPool = new ServerThreadPool(poolSize, this);				
		}
		
		/**
		   Start listening for IO events
		 */
		public synchronized void activate() throws Throwable {
			// Start the pool size
			myPool.start();
			
			// Create the ServerSocketChannel
			if (myLogger.isLoggable(Logger.CONFIG)) {
				myLogger.log(Logger.CONFIG, myLogPrefix+"Opening server socket channel.");
			}
			mySSChannel = ServerSocketChannel.open();
			mySSChannel.configureBlocking(false);

			// Bind the server socket to the proper host and port
			if (myLogger.isLoggable(Logger.CONFIG)) {
				myLogger.log(Logger.CONFIG, myLogPrefix+"Binding server socket to."+host+":"+port);
			}
			ServerSocket ss = mySSChannel.socket();
			InetSocketAddress addr = null;
			if (host != null) {
				addr = new InetSocketAddress(host, port);
			}
			else {
				addr = new InetSocketAddress(port);
				host = Profile.getDefaultNetworkName();
			}
			ss.bind(addr);
			
			// Create the selector
			if (myLogger.isLoggable(Logger.CONFIG)) {
				myLogger.log(Logger.CONFIG, myLogPrefix+"Registering for asynchronous IO events.");
			}
			mySelector = Selector.open();
			
			// Register with the selector
			acceptSK = mySSChannel.register(mySelector, SelectionKey.OP_ACCEPT);

			myLogger.log(Logger.INFO, myLogPrefix+"Ready to accept I/O events on address "+myProtocol.buildAddress(host, String.valueOf(port), null, null));

			// Start the embedded thread
			state = ACTIVE_STATE;
			myThread = new Thread(this);
			String threadName = (PREFIX.startsWith(myID) ? "BEManagementService-loop-manager" : "BEManagementService-"+myID+"-loop-manager");
			myThread.setName(threadName);
			myThread.start();
	  }

	
	  /**
	     @return The port this server is listening for connection on
	   */
	  public int getLocalPort() {
	  	return mySSChannel.socket().getLocalPort();
	  }
	  
	  /**
	     @return The host this server is listening for connection on
	   */
	  public String getLocalHost() {
	  	return host;
	  }
	  
	  /**
	     Make this IOEventServer terminate
	   */
	  public synchronized void shutdown() {
	    if (state != TERMINATING_STATE && state != TERMINATED_STATE) {
		  	state = TERMINATING_STATE;
			  if(myLogger.isLoggable(Logger.CONFIG)) {
			  	myLogger.log(Logger.CONFIG, myLogPrefix+"Shutting down...");
			  }
		
		    try {
		      // Force the embedded thread to terminate by closing the 
		    	// server socket channel
		    	if (mySSChannel != null) {
			    	mySSChannel.close();
		    	}
		      
		      // Wait for the loop manager thread to complete. This will 
		    	// also stop the ServerThreadPool
		    	if (myThread != null) {
			      myThread.join();
		    	}
		    } 
		    catch (IOException ioe) {
		      ioe.printStackTrace();
		    } 
		    catch (InterruptedException ie) {
		      ie.printStackTrace();
		    }
		    
		    state = TERMINATED_STATE;
	    }
	  } 
		
	  /**
	     IOEventServer Therad entry point.
	     This is the Thread that executes the NIO inner loop
	   */
	  public void run() {
	    while (state == ACTIVE_STATE) {
	      try {
	      	// Wait for the next IO events 
	      	int n = mySelector.select();
	      	if (n > 0) {
		      	Set keys = mySelector.selectedKeys();
		      	Iterator it = keys.iterator();
		      	while (it.hasNext()) {
		      		SelectionKey key = (SelectionKey) it.next();
		      		if ((key.readyOps() & SelectionKey.OP_ACCEPT) != 0) {
		      			// This is an incoming connection. The channel must be 
		      			// the SerevrSocketChannel server
		      			handleAcceptOp(key);
		      		}
		      		else if ((key.readyOps() & SelectionKey.OP_READ) != 0) {
		      			// This is some incoming data for one of the BE
		      			handleReadOp(key);
		      		}
		      		it.remove();
		      	}
	      	}
	      } 
	      catch (Exception e) {
	      	if (state == ACTIVE_STATE) {
	        	myLogger.log(Logger.WARNING, myLogPrefix+"Error waiting for the next IO event. ");
	          e.printStackTrace();
	
	          // Abort
	          state = ERROR_STATE;
	      	}
	      } 
	    } // END of while
	    
	    // Close all mediators
		  synchronized (mediators) {
		    Iterator it = mediators.values().iterator();
		    while (it.hasNext()) {
		      NIOMediator m = (NIOMediator) it.next();
		      m.kill();
		    } 
		  }
	    mediators.clear();
		  
		  // Stop the server pool
		  myPool.stop();
	  }

	  String getId() {
	  	return myID;
	  }
	  
	  String getLogPrefix() {
	  	return myLogPrefix;
	  }
	  
	  private void handleAcceptOp(SelectionKey key) {
	  	try {
		  	SocketChannel sc = ((ServerSocketChannel) key.channel()).accept();
		  	
		  	sc.configureBlocking(false);
		  	sc.register(mySelector, SelectionKey.OP_READ);
	  	}
	  	catch (Exception e) {
	  		myLogger.log(Logger.WARNING, myLogPrefix+" Error accepting incoming connection");
	  		e.printStackTrace();
	  	}
	  }
	  
	  private void handleReadOp(SelectionKey key) {
	  	// Read the packet through a proper connection and then let the 
	  	// therad pool process it
	  	KeyManager mgr = (KeyManager) key.attachment();
	  	if (mgr == null) {
	  		// FIXME: Use the connection factory
	  		mgr = new KeyManager(key, new NIOJICPConnection((SocketChannel) key.channel()));
	  		key.attach(mgr);
	  	}
	  	mgr.readPacket();
    	myPool.enqueue(key);
	  }
	  
	  /**
	     Serve an IO event conveied by a SelectionKey (a JICPPacket or an IO exception).
	     This method is executed by one of the threads in the 
	     server Thread pool.
	   */
	  public void serveKey(SelectionKey key) {
	  	SocketChannel sc = (SocketChannel) key.channel();
	  	Socket s = sc.socket();
      InetAddress address = s.getInetAddress();
      int port = s.getPort();
      
      KeyManager mgr = (KeyManager) key.attachment();
      Connection connection = mgr.getConnection();
      JICPPacket pkt = mgr.getPacket();
      NIOMediator mediator = mgr.getMediator();
      JICPPacket reply = null;
      // If there is no mediator associated to this key prepare to close 
      // the connection when the packet will have been processed
      boolean closeConnection = (mediator == null);
      
      // STEP 1) Serve the received packet
      if (pkt != null) {
	      int type = pkt.getType();	  
        String recipientID = pkt.getRecipientID();
        try {
		      switch (type) {
			      case JICPProtocol.GET_ADDRESS_TYPE: {
			        // Respond sending back the caller address
			        if(myLogger.isLoggable(Logger.INFO)) {
			        	myLogger.log(Logger.INFO, myLogPrefix+"GET_ADDRESS request received from "+address+":"+port);
			        }
			        reply = new JICPPacket(JICPProtocol.GET_ADDRESS_TYPE, JICPProtocol.DEFAULT_INFO, address.getHostAddress().getBytes());
			        break;
			      }
			      case JICPProtocol.CREATE_MEDIATOR_TYPE: {
			      	if (mediator == null) {
				        if(myLogger.isLoggable(Logger.INFO)) {
				        	myLogger.log(Logger.INFO, myLogPrefix+"CREATE_MEDIATOR request received from "+ address + ":" + port);
				        }
				
				        // Create a new Mediator
				        Properties p = parseProperties(new String(pkt.getData()));
				        
				        // If there is a PDPContextManager add the PDP context properties
				        if (myPDPContextManager != null) {
				        	Properties pdpContextInfo = myPDPContextManager.getPDPContextInfo(address);
				        	if (pdpContextInfo != null) {
				          	mergeProperties(p, pdpContextInfo);
				        	}
				        	else {
			        			myLogger.log(Logger.WARNING, myLogPrefix+"Security attack! CREATE_MEDIATOR request from non authorized address: "+address);
				        		reply = new JICPPacket("Not authorized", null);
				        		break;
				        	}
				        }
				
							  // Get mediator ID from the passed properties (if present)
				        String id = p.getProperty(JICPProtocol.MEDIATOR_ID_KEY); 
				        String msisdn = p.getProperty(PDPContextManager.MSISDN);
							  if(id != null) {
							  	if (msisdn != null && !msisdn.equals(id)) {
							  		// Security attack: Someone is pretending to be someone other
			        			myLogger.log(Logger.WARNING, myLogPrefix+"Security attack! CREATE_MEDIATOR request with mediator-id != MSISDN. Address is: "+address);
										reply = new JICPPacket("Not authorized", null);
				        		break;
							  	}	
							  	// An existing front-end whose back-end was lost. The BackEnd must resynch 
							  	p.setProperty(jade.core.BackEndContainer.RESYNCH, "true");
							  }
							  else {
							  	// Use the MSISDN (if present) 
							  	id = msisdn;
							  	if (id == null) {
							      // Construct a default id using the string representation of the server's TCP endpoint
							      id = "BE-"+getLocalHost() + ':' + getLocalPort() + '-' + String.valueOf(mediatorCnt++);
							  	}
							  }
							  
							  // If last connection from the same device aborted, the old 
							  // BackEnd may still exist as a zombie. In case ids are assigned
							  // using the MSISDN the new name is equals to the old one.
							  if (id.equals(msisdn)) {
							  	NIOMediator old = (NIOMediator) mediators.get(id);	
							  	if (old != null) {
							  		// This is a zombie mediator --> kill it
				  					myLogger.log(Logger.INFO, myLogPrefix+"Replacing old mediator "+id);
							  		old.kill();
							  		// Be sure the zombie container has been removed
							  		waitABit(1000);
							  	}
							  }
				
				        // Create and start the new mediator
				        mediator = startMediator(id, p);
				  			closeConnection = !mediator.handleIncomingConnection(connection, pkt, address, port);
				  			mediators.put(mediator.getId(), mediator);
								if (!closeConnection) {
									// The mediator wants to keep this connection open --> associate 
									// it to the current key
									mgr.setMediator(mediator);
								}
										        
				        // Create an ad-hoc reply including the assigned mediator-id and the IP address
				        String replyMsg = id+'#'+address.getHostAddress();
				        reply = new JICPPacket(JICPProtocol.RESPONSE_TYPE, JICPProtocol.DEFAULT_INFO, replyMsg.getBytes());
				        reply.setSessionID((byte) 31); // Dummy session ID != from valid ones
			      	}
			      	else {
			      		myLogger.log(Logger.WARNING, myLogPrefix+" CREATE_MEDIATOR request received on a connection already linked to an existing mediator");
			        	reply = new JICPPacket("Unexpected packet type", null);
			      	}
			      	break;
			      }
			      case JICPProtocol.CONNECT_MEDIATOR_TYPE: {
			      	if (mediator == null) {
				        if(myLogger.isLoggable(Logger.INFO)) {
				        	myLogger.log(Logger.INFO, myLogPrefix+"CONNECT_MEDIATOR request received from "+address+":"+port+". ID="+recipientID);
				        }
				        
				        // If there is a PDPContextManager  check that the recipientID is the MSISDN
				        if (myPDPContextManager != null) {
				        	Properties pdpContextInfo = myPDPContextManager.getPDPContextInfo(address);
				        	if (pdpContextInfo != null) {
				        		String msisdn = pdpContextInfo.getProperty(PDPContextManager.MSISDN);
				        		if (msisdn == null || !msisdn.equals(recipientID)) {
				        			myLogger.log(Logger.WARNING, myLogPrefix+"Security attack! CONNECT_MEDIATOR request with mediator-id != MSISDN. Address is: "+address);
					        		reply = new JICPPacket("Not authorized", null);
					        		break;
				        		}
				        	}
				        	else {
			        			myLogger.log(Logger.WARNING, myLogPrefix+"Security attack! CONNECT_MEDIATOR request from non authorized address: "+address);
				        		reply = new JICPPacket("Not authorized", null);
				        		break;
				        	}
				        }

				        // Retrieve the mediator to connect to
				        mediator = getFromID(recipientID);
				        
				        if (mediator != null) {
				        	closeConnection = !mediator.handleIncomingConnection(connection, pkt, address, port);
									if (!closeConnection) {
										// The mediator wants to keep this connection open --> associate 
										// it to the current key
										mgr.setMediator(mediator);
									}
				          reply = new JICPPacket(JICPProtocol.RESPONSE_TYPE, JICPProtocol.DEFAULT_INFO, address.getHostAddress().getBytes());
				        }
				        else {
			          	myLogger.log(Logger.WARNING, myLogPrefix+"Mediator "+recipientID+" not found");
				        	reply = new JICPPacket("Mediator "+recipientID+" not found", null);
				        }
			      	}
			      	else {
			      		myLogger.log(Logger.WARNING, myLogPrefix+" CONNECT_MEDIATOR request received on a connection already linked to an existing mediator");
			        	reply = new JICPPacket("Unexpected packet type", null);
			      	}
			        break;
			      }        
			      default: {
			      	// Pass all other JICP packets (command, responses, keep-alives ...)
			      	// to the proper mediator.
			        if (mediator == null) {
			        	mediator = getFromID(recipientID);
			        }
			        
			        if (mediator != null) {
			        	if(myLogger.isLoggable(Logger.FINEST)) {
			        		myLogger.log(Logger.FINEST, myLogPrefix+"Passing packet of type "+type+" to mediator "+mediator.getId());
			        	}
			          reply = mediator.handleJICPPacket(connection, pkt, address, port);
			        } 
			        else {
			      		myLogger.log(Logger.WARNING, myLogPrefix+" No mediator for incoming packet of type "+type);
			        	if (type == JICPProtocol.COMMAND_TYPE) {
				        	reply = new JICPPacket(myLogPrefix+"Mediator not found", null);
			        	}
			        }
			      }
		      } // END of switch
        }
        catch (Exception e) {
        	// Error handling the received packet
      		myLogger.log(Logger.WARNING, myLogPrefix+"Error handling incoming packet");
        	e.printStackTrace();
	      	// If the incoming packet was a command, send back a generic error response
	        if (type == JICPProtocol.COMMAND_TYPE) {
	        	reply = new JICPPacket("Unexpected error", e);
	        }
        }
      }
      else {
      	// There was an IO error reading the packet. If the current key
      	// is associated to a mediator, let it process the error. Otherwise
      	// print a warning.
      	Exception e = mgr.getException();
  	    if (mediator != null) {
  	    	mediator.handleConnectionError(connection, e);
  	    }
  	    else {
    	    myLogger.log(Logger.WARNING, myLogPrefix+"Communication error reading incoming packet from "+address+":"+port+" ["+e+"]");
  	    }
  	    closeConnection = true;
      }
	      
      // STEP 2) Send back the response if any
      if (reply != null) {
      	try {
	      	connection.writePacket(reply);
      	}
      	catch (IOException ioe) {
      		myLogger.log(Logger.WARNING, myLogPrefix+"Communication error writing return packet to "+address+":"+port+" ["+ioe+"]");
	      	closeConnection = true;
      	}	      	
      }
      else {
      	// The mediator will reply asynchronously --> keep the connection open
      	closeConnection = false;
      }
      
			// STEP 3) Unlock the KeyManager and close the connection if necessary
      mgr.reset();
      if (closeConnection) {
	      try {
	        // Close connection
	      	if(myLogger.isLoggable(Logger.FINEST)) {
	      		myLogger.log(Logger.FINEST, myLogPrefix+"Closing connection with "+address+":"+port);
	      	}
	      	connection.close();
	      } 
	      catch (IOException io) {
	        if(myLogger.isLoggable(Logger.WARNING)) {
	        	myLogger.log(Logger.WARNING, myLogPrefix+"I/O error while closing connection with "+address+":"+port);
	        }
	        io.printStackTrace();
	      }
      }      
	  }

	  private NIOMediator getFromID(String recipientID) {
      if (recipientID != null) {
		  	return (NIOMediator) mediators.get(recipientID);
      }
      return null;
	  }
	  
	  /**
	     Called by a Mediator to notify that it is no longer active
	   */
	  public void deregisterMediator(String id) {
	  	if (myLogger.isLoggable(Logger.CONFIG)) {
		  	myLogger.log(Logger.CONFIG, myLogPrefix+"Deregistering mediator "+id);
	  	}
	    mediators.remove(id);
	  } 
	  
	  public void tick(long currentTime) {
	  	synchronized (mediators) {
		    Iterator it = mediators.values().iterator();
		    while (it.hasNext()) {
		      NIOMediator m = (NIOMediator) it.next();
		      m.tick(currentTime);
		    }
	  	}
	  }
	  
	  /**
	     Called by the PDPContextManager (if any)
	   */
	  public void handlePDPContextClosed(String id) {
	  	// FIXME: to be implemented
	  }	
	  
	  protected NIOMediator startMediator(String id, Properties p) throws Exception {
			String className = p.getProperty(JICPProtocol.MEDIATOR_CLASS_KEY);
			if (className != null) {
	  		NIOMediator m = (NIOMediator) Class.forName(className).newInstance();
	  		mergeProperties(p, leapProps);
	  		if (myLogger.isLoggable(Logger.CONFIG)) {
		  		myLogger.log(Logger.CONFIG, myLogPrefix+"Initializing mediator "+id+" with properties "+p);
	  		}
	  		m.init(this, id, p);
	  		return m;
			}
			else {
				throw new ICPException("No mediator class specified.");
			}
	  }	  
	} // END of inner class IOEventServer
	
	
	/**
	   Inner class KeyManager
	   Keep a SelectionKey together with the information associated to it
	   such as the connection wrapping the key channel and the mediator 
	   using that connection (if any).
	 */
	private class KeyManager {
		private SelectionKey key;
		private Connection connection;
		private NIOMediator mediator;
		private JICPPacket pkt;
		private Exception exc;
		
		private boolean locked = false;
		
		public KeyManager(SelectionKey k, Connection c) {
			key = k;
			connection = c;
		}
		
		public final NIOMediator getMediator() {
			return mediator;
		}
		
		public final void setMediator(NIOMediator m) {
			mediator = m;
		}

		/**
		   Read a packet or an IO Exception event from the connection 
		   associated to the managed key.
		   Locking before reading the packet is required to avoid a new
		   packet to be read before the previous one has been consumend.
		 */
		public final void readPacket() {
			lock();
			try {
				pkt = connection.readPacket();
			}
			catch (IOException ioe) {
				exc = ioe;
				try {
					connection.close();
				}
				catch (Exception e) {}
			}
		}
				
		private synchronized final void lock() {
			try {
				while (locked) {
					wait();
				}
				locked = true;
			}
			catch (InterruptedException ie) {
				// There is nothing meaningful we can do
				ie.printStackTrace();
			}
		}
		
		private synchronized final void unlock() {
			locked = false;
			notifyAll();
		}
		
		public final void reset() {
			pkt = null;
			exc = null;
			unlock();
		}
		
		public final Connection getConnection() {
			return connection;
		}
		
		public final JICPPacket getPacket() {
			return pkt;
		}
		
		public final Exception getException() {
			return exc;
		}
	} // END of inner class KeyManager
	
	
	/**
	   Inner class ServerThreadPool
	 */
	private class ServerThreadPool implements Runnable {
		private int size;
		private int terminatedCnt = 0;
		private IOEventServer myServer;
		private int state = INIT_STATE;
		private List pendingKeys = new LinkedList();
		
		public ServerThreadPool(int s, IOEventServer server) {
			myServer = server;
			size = s;
		}
		
		public void start() {
			state = ACTIVE_STATE;
			for (int i = 0; i < size; ++i) {
				Thread t = new Thread(this);
				String id = myServer.getId();
				String serverId = (PREFIX.startsWith(id) ? "" : "-"+id);
				t.setName("BEManagementService"+serverId+"-T"+i);
				t.start();
			}
		}
		
		public synchronized void stop() {
			state = TERMINATING_STATE;
			notifyAll();
		}
		
		public void run() {
			while (state == ACTIVE_STATE) {
				SelectionKey key = dequeue();
				if (key != null) {
					myServer.serveKey(key);
				}
			}
			terminatedCnt++;
			if (terminatedCnt >= size) {
				state = TERMINATED_STATE;
			}
		}
		
		public synchronized final void enqueue(SelectionKey key) {
			pendingKeys.add(key);
			int size = pendingKeys.size();
			if (size > 20) {
				myLogger.log(Logger.WARNING, myServer.getLogPrefix()+"# "+size+" pending keys");
			}
			notifyAll();
		}
		
		public synchronized SelectionKey dequeue() {
			try {
				while (state == ACTIVE_STATE && pendingKeys.isEmpty()) {
					wait();
				}
			}
			catch (InterruptedException ie) {
				myLogger.log(Logger.WARNING, Thread.currentThread().getName()+": unexpected InterruptedException"); 
			}
			
			if (!pendingKeys.isEmpty()) {
				return (SelectionKey) pendingKeys.remove(0);
			}
			
			// The pool was stopped or the thread was interrupted
			return null;
		}				
	} // END of inner class ServerThreadPool
	
	
  /**
     Inner class Ticker
   */
  private class Ticker extends Thread {
  	private long period;
  	private boolean active = false;
  	
  	private Ticker(long period) {
  		super();
  		this.period = period;
  	}
  	
  	public void start() {
  		active = true;
  		setName("BEManagementService-ticker");
  		super.start();
  	}
  	
  	public void run() {
  		while (active) {
  			try {
  				Thread.sleep(period);
  				long currentTime = System.currentTimeMillis();
  				Object[] ss = servers.values().toArray();
  				for (int i = 0; i < ss.length; ++i) {
  					((IOEventServer) ss[i]).tick(currentTime);
  				}
  			}
  			catch (InterruptedException ie) {
  			}
  		}
  	}
  	
  	public void shutdown() {
  		active = false;
  		interrupt();
  	}
  } // END of inner class Ticker

	
			
  protected Properties parseProperties(String s) throws ICPException {
  	StringTokenizer st = new StringTokenizer(s, "=#");
  	Properties p = new Properties();
  	while (st.hasMoreTokens()) {
  		String key = st.nextToken();
  		if (!st.hasMoreTokens()) {
  			throw new ICPException("Wrong initialization properties format.");
  		}
  		p.setProperty(key, st.nextToken());
  	}
  	return p;
  }
  
  private void mergeProperties(Properties p1, Properties p2) {
		Enumeration e = p2.propertyNames();
		while (e.hasMoreElements()) {
			String key = (String) e.nextElement();
			p1.setProperty(key, p2.getProperty(key));
		}
  }
  
  private void waitABit(long t) {
  	try {
  		Thread.sleep(t);
  	}
  	catch (InterruptedException ie) {
  	}
  }
  
  private Vector parseStringList(String strList, char separator) {
  	// FIXME: to be implemented
  	Vector v = new Vector();
  	v.addElement(strList);
  	return v;
  }
}

