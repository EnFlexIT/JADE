package jade.imtp.leap.nio;

//#J2ME_EXCLUDE_FILE

import jade.core.FrontEnd;
import jade.core.BackEnd;
import jade.core.BackEndContainer;
import jade.core.BEConnectionManager;
import jade.core.Profile;
import jade.core.ProfileException;
import jade.core.IMTPException;
import jade.imtp.leap.MicroSkeleton;
import jade.imtp.leap.BackEndSkel;
import jade.imtp.leap.FrontEndStub;
import jade.imtp.leap.Dispatcher;
import jade.imtp.leap.ICPException;
import jade.imtp.leap.JICP.JICPProtocol;
import jade.imtp.leap.JICP.JICPMediator;
import jade.imtp.leap.JICP.JICPMediatorManager;
import jade.imtp.leap.JICP.JICPPacket;
import jade.imtp.leap.JICP.Connection;
import jade.util.leap.Properties;
import jade.util.Logger;

import java.io.IOException;
import java.net.InetAddress;

/**
   This class implements the BEManagementService managable BackEnd dispatcher 
   for BIFEDispatcher-s.
   @author Giovanni Caire - Telecom Italia LAB S.p.A.
 */
public class NIOBEDispatcher implements NIOMediator, BEConnectionManager, Dispatcher {
	private static final long RESPONSE_TIMEOUT = 60000;
	
  private long keepAliveTime;
  private long lastReceivedTime;
  private boolean active = true;
  private boolean peerActive = true;

  private JICPMediatorManager myMediatorManager;
  private String myID;
  private BackEndContainer myContainer = null;

  protected InputManager  inpManager;
  protected OutputManager  outManager;

	private Logger myLogger = Logger.getMyLogger(getClass().getName());
	
	/**
	   Retrieve the ID of this mediator
	 */
	public String getId() {
		return myID;
	}
	
	/**
	   Initialize this JICPMediator
	 */
	public void init(JICPMediatorManager mgr, String id, Properties props) throws ICPException {
		myMediatorManager = mgr;
		myID = id;
		
  	// Max disconnection time
    long maxDisconnectionTime = JICPProtocol.DEFAULT_MAX_DISCONNECTION_TIME;
    try {
    	maxDisconnectionTime = Long.parseLong(props.getProperty(JICPProtocol.MAX_DISCONNECTION_TIME_KEY));
    }
    catch (Exception e) {
    	// Keep default
    }

  	// Keep-alive time
    keepAliveTime = JICPProtocol.DEFAULT_KEEP_ALIVE_TIME;
    try {
    	keepAliveTime = Long.parseLong(props.getProperty(JICPProtocol.KEEP_ALIVE_TIME_KEY));
    }
    catch (Exception e) {
    	// Keep default
    }

    // inpCnt
    int inpCnt = 0;
    try {
    	inpCnt = (Integer.parseInt(props.getProperty("lastsid")) + 1) & 0x0f;
    }
    catch (Exception e) {
    	// Keep default
    }

    // lastSid
    int lastSid = 0x0f;
    try {
    	lastSid = (byte) (Integer.parseInt(props.getProperty("outcnt")) -1);
    	if (lastSid < 0) {
    		lastSid = 0x0f;
    	}
    }
    catch (Exception e) {
    	// Keep default
    }

    FrontEndStub st = new FrontEndStub(this);
    inpManager = new InputManager(inpCnt, st);
    
    BackEndSkel sk = startBackEndContainer(props);
    outManager = new OutputManager(lastSid, sk, maxDisconnectionTime);
	}
	
  protected final BackEndSkel startBackEndContainer(Properties props) throws ICPException {
    try {
    	String nodeName = myID.replace(':', '_');
    	props.setProperty(Profile.CONTAINER_NAME, nodeName);

    	myContainer = new BackEndContainer(props, this);
    	if (!myContainer.connect()) {
				throw new ICPException("BackEnd container failed to join the platform");
			}
      if(myLogger.isLoggable(Logger.CONFIG)) {
      	myLogger.log(Logger.CONFIG,"BackEndContainer "+myContainer.here().getName()+" successfully joined the platform");
      }
    	return new BackEndSkel(myContainer);
    }
    catch (ProfileException pe) {
    	// should never happen
    	pe.printStackTrace();
			throw new ICPException("Error creating profile");
    }
  }

  // Local variable only used in the kill() method
  private Object shutdownLock = new Object();

	/**
	   Kill the above container.
	   This may be called by the JICPMediatorManager or when 
	   a peer termination notification is received.
	 */
	public void kill() {
  	// Avoid killing the above container two times
  	synchronized (shutdownLock) {
	  	if (active) {
	  		active = false;
		    myContainer.shutDown();
	  	}
  	}
	}
	
	/**
	* Passes to this JICPMediator the connection opened by the mediated 
	* entity.
	* This is called by the JICPServer this Mediator is attached to
	* as soon as the mediated entity (re)connects.
	* @param c the connection to the mediated entity
	* @param pkt the packet that was sent by the mediated entity when 
	* opening this connection
	* @param addr the address of the mediated entity
	* @param port the local port used by the mediated entity
	* @return an indication to the JICPMediatorManager to keep the 
	* connection open.
	*/
	public boolean handleIncomingConnection(Connection c, JICPPacket pkt, InetAddress addr, int port) {
  	// Update keep-alive info
  	lastReceivedTime = System.currentTimeMillis();
  	
  	boolean inp = false;
  	byte[] data = pkt.getData();
  	if (data.length == 1) {
  		inp = (data[0] == 1);
  	}
   	if (inp) {
   		inpManager.setConnection(c);
   	}
   	else {
   		outManager.setConnection(c);
   	}

    return true;
	}
	
	/**
	   Notify this NIOMediator that an error occurred on one of the 
	   Connections it was using. This information is important since, 
	   unlike normal mediators, a NIOMediator never reads packets from 
	   connections on its own (the JICPMediatorManager always does that).
	 */
	public void handleConnectionError(Connection c, Exception e) {
		if (peerActive) {
			// Try assuming it is the input connection
			try {
				inpManager.checkConnection(c);
				myLogger.log(Logger.INFO, myID+": IC Disconnection detected");
				inpManager.resetConnection();
			}
			catch (ICPException icpe) {
				// Then try assuming it is the output connection
				try {
					outManager.checkConnection(c);
					myLogger.log(Logger.INFO, myID+": OC Disconnection detected");
					outManager.resetConnection();
				}
				catch (ICPException icpe2) {
					// Ignore it
				}
			}
		}
	}
	
	/**
	   Passes to this mediator a JICPPacket received by the 
	   JICPMediatorManager this mediator is attached to.
	   In a NIOMediator this should never be called.
	 */
	public JICPPacket handleJICPPacket(JICPPacket p, InetAddress addr, int port) throws ICPException {
		throw new ICPException("Unexpected call");
	}
	
	/**
	   Overloaded version of the handleJICPPacket() method including
	   the <code>Connection</code> the incoming JICPPacket was received
	   from. This information is important since, unlike normal mediators,
	   a NIOMediator never reads packets from connections on its own (the
	   JICPMediatorManager always does that).
	 */
	public JICPPacket handleJICPPacket(Connection c, JICPPacket pkt, InetAddress addr, int port) throws ICPException {
		if ((pkt.getInfo() & JICPProtocol.TERMINATED_INFO) != 0) {
			peerActive = false;
			if (myLogger.isLoggable(Logger.CONFIG)) {
				myLogger.log(Logger.CONFIG, myID+": Peer termination notification received");
			}
		}
		
  	// Update keep-alive info
  	lastReceivedTime = System.currentTimeMillis();
  	
		byte type = pkt.getType();
		if (type == JICPProtocol.COMMAND_TYPE) {
			return outManager.handleCommand(c, pkt);
		}
		else if (type == JICPProtocol.KEEP_ALIVE_TYPE) {
			return outManager.handleKeepAlive(c, pkt);
		}
		else if (type == JICPProtocol.RESPONSE_TYPE || type == JICPProtocol.ERROR_TYPE) {
			inpManager.handleResponse(c, pkt);
			return null;
		}
		else {
			throw new ICPException("Unexpected packet type "+type);
		}
	}
		
	/**
	  This is periodically called by the JICPMediatorManager and is
	  used by this NIOMediator to evaluate the elapsed time without
	  the need of a dedicated thread or timer.
	 */
	public void tick(long currentTime) {
		// Evaluate the keep alive
  	if (keepAliveTime > 0) {
	  	if ((currentTime - lastReceivedTime) > (keepAliveTime + 50000)) {
	  		// Missing keep-alive.
	  		// The OUT connection is no longer valid
	  		if (outManager.isConnected()) {
					myLogger.log(Logger.WARNING, myID+": Missing keep-alive");
		  		outManager.resetConnection();
	  		}
	  		// Check the INP connection. Since this method must return
	  		// asap, does it in a separated Thread
	  		if (inpManager.isConnected()) {
		  		Thread t = new Thread() {
		  			public void run() {
		  				try {
			  				JICPPacket pkt = new JICPPacket(JICPProtocol.KEEP_ALIVE_TYPE, JICPProtocol.DEFAULT_INFO, null);
			  				inpManager.dispatch(pkt, false);
	              if(myLogger.isLoggable(Logger.CONFIG)) {
	              	myLogger.log(Logger.CONFIG, myID+": IC valid");
	              }
		  				}
		  				catch (Exception e) {
		  					// Just do nothing: the INP connection has been reset
		  				}
		  			}
		  		};
		  		t.start();
	  		}
	  	}
  	}
  	
		// Evaluate the max disconnection time
  	if (outManager.checkMaxDisconnectionTime(currentTime)) {
  		myLogger.log(Logger.SEVERE,  myID+": Max disconnection time expired."); 
  		// Consider as if the FrontEnd has terminated spontaneously -->
  		// Kill the above container (this will also kill this NIOBEDispatcher).
  		kill();
  	}  	
	}
	
	
  ////////////////////////////////////////////////
  // BEConnectionManager interface implementation
  ////////////////////////////////////////////////
	/**
	   Return a stub of the remote FrontEnd that is connected to the
	   local BackEnd.
	   @param be The local BackEnd
	   @param props Additional (implementation dependent) connection
	   configuration properties.
	   @return A stub of the remote FrontEnd.
	 */
  public FrontEnd getFrontEnd(BackEnd be, Properties props) throws IMTPException {
  	return inpManager.getStub();
  }

  public void activateReplica(String addr, Properties props) throws IMTPException {
  }

  /**
     Make this NIOBEDispatcher terminate.
   */
  public void shutdown() {
		active = false;
    if(myLogger.isLoggable(Logger.INFO)) {
       myLogger.log(Logger.INFO, myID+": shutting down");
    }

    // Deregister from the JICPServer
    if (myID != null) {
	    myMediatorManager.deregisterMediator(myID);
    }

		inpManager.shutdown();
		outManager.shutdown();
  }

  
  //////////////////////////////////////////
  // Dispatcher interface implementation
  //////////////////////////////////////////
  public byte[] dispatch(byte[] payload, boolean flush) throws ICPException {
	  JICPPacket pkt = new JICPPacket(JICPProtocol.COMMAND_TYPE, JICPProtocol.DEFAULT_INFO, payload);
	  pkt = inpManager.dispatch(pkt, flush);
	  return pkt.getData();
  }

  
  /**
     Inner class InputManager.
     This class manages the delivery of commands to the FrontEnd
   */
  protected class InputManager {
  	private Connection myConnection;
  	private boolean connectionRefreshed;
  	private boolean waitingForFlush;
  	private JICPPacket currentReply;
  	
  	private int inpCnt;
  	private FrontEndStub myStub;
  	
  	InputManager(int c, FrontEndStub s) {
  		inpCnt = c;
  		myStub = s;
  	}
  	
  	FrontEndStub getStub() {
  		return myStub;
  	}
  	
  	synchronized void setConnection(Connection c) {
			// Reset the old connection
  		resetConnection();
  		
  		// Set the new connection
  		myConnection = c;
  		connectionRefreshed = true;
  		waitingForFlush = myStub.flush();
  		myContainer.notifyInputConnectionReady();
  	}
  	
  	synchronized void resetConnection() {
  		// Close the connection if it was in place
  		if (myConnection != null) {
  			close(myConnection);
	  		myConnection = null;
  		}
  		// If there was someone waiting for a response on the 
  		// connection notify it.
  		notifyAll();
  	}

	  final void checkConnection(Connection c) throws ICPException {
  		if (c != myConnection) {
  			throw new ICPException("Wrong connection");
  		}
	  }
	  
  	final boolean isConnected() {
  		return myConnection != null;
  	}
  	
  	void shutdown() {
  		resetConnection();
  	}
  	
  	final synchronized JICPPacket dispatch(JICPPacket pkt, boolean flush) throws ICPException {
  		if ((!active) || (myConnection == null) || (waitingForFlush && (!flush))) {
	  		// If we are waiting for flushed packets and the current packet
	  		// is a normal (i.e. non-flushed) one, then throw an exception -->
	  		// The packet will be put in the queue of packets to be flushed
	  		throw new ICPException("Unreachable");
  		}
  		
  		waitingForFlush = false;
  		connectionRefreshed = false;
  		try {
		  	pkt.setSessionID((byte) inpCnt);
		  	if (myLogger.isLoggable(Logger.FINE)) {
			  	myLogger.log(Logger.FINE, myID+": Sending command "+inpCnt+" to FE");
		  	}
				myConnection.writePacket(pkt);
				JICPPacket reply = waitForReply(RESPONSE_TIMEOUT);
		  	if (myLogger.isLoggable(Logger.FINER)) {
			  	myLogger.log(Logger.FINER, myID+": Received response "+inpCnt+" from FE");
		  	}
		    if (reply.getType() == JICPProtocol.ERROR_TYPE) {
		    	// Communication OK, but there was a JICP error on the peer
		      throw new ICPException(new String(pkt.getData()));
		    }
		    if (!peerActive) {
		    	// This is the response to an exit command --> Suicide, without
		    	// killing the above container since it is already dying. 
		    	shutdown();
		    }
		  	inpCnt = (inpCnt+1) & 0x0f;
		  	return reply;
  		}
	  	catch (IOException ioe) {
	  		// Either there was an IO exception writing data to the connection
	  		// or the response timeout expired --> reset the connection.
	  		// Note that if the connection was refreshed/reset while we were 
	  		// waiting for the response there is an ICPException --> we don't 
	  		// get here 
				myLogger.log(Logger.WARNING,myID+": IOException IC. "+ioe);
	  		resetConnection();
	  		throw new ICPException("Dispatching error.", ioe);
	  	}  		
  	}
  	
  	final synchronized void handleResponse(Connection c, JICPPacket reply) throws ICPException {
  		checkConnection(c);
  		currentReply = reply;
  		notifyAll();
  	}

  	private synchronized JICPPacket waitForReply(long timeout) throws ICPException, IOException {
  		try {
	  		if (currentReply == null) {
	  			wait(timeout);
		  		if (currentReply == null) {
		  			if (isConnected()) {
			  			if (connectionRefreshed) {
				  			throw new ICPException("Connection refreshed");
			  			}
			  			else {
			  				throw new IOException("Response timeout expired");
			  			}
		  			}
		  			else {
		  				throw new ICPException("Connection reset");
		  			}
		  		}	  			
	  		}
	  		JICPPacket tmp = currentReply;
	  		currentReply = null;
	  		return tmp;
  		}
  		catch (InterruptedException ie) {
  			throw new ICPException("Interrupted");
  		}
  	}  	  	  	
  } // END of inner class InputManager
  
  
  /**
     Inner class OutputManager
     This class manages the reception of commands and keep-alive
     packets from the FrontEnd.
     This class also manages the maxDisconnectionTime, i.e. the remote
     FrontEnd is considered dead if it cannot re-establish the 
     OUT connection within the maxDisconnectionTime.
   */
  protected class OutputManager {
  	private Connection myConnection;
  	private JICPPacket lastResponse;
  	private int lastSid;
  	private BackEndSkel mySkel;
  	private long maxDisconnectionTime, expirationDeadline;
  	
  	OutputManager(int n, BackEndSkel s, long t) {
  		lastSid = n;
  		mySkel = s;
  		maxDisconnectionTime = t;
  	}
  	
  	synchronized void setConnection(Connection c) {
  		// Close the old connection if any
  		if (myConnection != null) {
  			close(myConnection);
  		}
  		// Set the new connection
  		myConnection = c;
  	}
  	
  	synchronized void resetConnection() {
  		if (myConnection != null) {
  			expirationDeadline = System.currentTimeMillis() + maxDisconnectionTime;
				close(myConnection);
  		}
  		myConnection = null;
  	}
  	
	  final void checkConnection(Connection c) throws ICPException {
  		if (c != myConnection) {
  			throw new ICPException("Wrong connection");
  		}
	  }
	  
	  final boolean isConnected() {
	  	return (myConnection != null);
	  }
	  
  	void shutdown() {
  		resetConnection();
  	}
  	
  	final synchronized JICPPacket handleCommand(Connection c, JICPPacket cmd) throws ICPException {
  		checkConnection(c);
  		JICPPacket reply = null;
     	if (peerActive) {
				byte sid = cmd.getSessionID();
				if (sid == lastSid) {
					myLogger.log(Logger.WARNING,myID+": Duplicated command from FE "+sid);
					reply = lastResponse;
				}
				else {
			    if(myLogger.isLoggable(Logger.FINE)) {
						myLogger.log(Logger.FINE, myID+": Received command "+sid+" from FE");
			    }
	
					byte[] rspData = mySkel.handleCommand(cmd.getData());
	        if(myLogger.isLoggable(Logger.FINER)) {
						myLogger.log(Logger.FINER, myID+": Command "+sid+" from FE served ");
	        }
	
				  reply = new JICPPacket(JICPProtocol.RESPONSE_TYPE, getReconnectInfo(), rspData);
				  reply.setSessionID(sid);
				  lastSid = sid;
				  lastResponse = reply;
				}
	  	}
	  	else {
    		// The remote FrontEnd has terminated spontaneously -->
    		// Kill the above container (this will also kill this NIOBEDispatcher).
    		kill();
    	}
	  	return reply;
 		}
  	
  	synchronized JICPPacket handleKeepAlive(Connection c, JICPPacket command) throws ICPException {
  		checkConnection(c);  		
      if(myLogger.isLoggable(Logger.FINEST)) {
				myLogger.log(Logger.FINEST,myID+": Keep-alive received");
      }
		  return new JICPPacket(JICPProtocol.RESPONSE_TYPE, getReconnectInfo(), null);
  	}
  	
  	synchronized boolean checkMaxDisconnectionTime(long currentTime) {
  		return (!isConnected()) && (currentTime > expirationDeadline);
  	}
  			
	  private final byte getReconnectInfo() {
	  	byte info = JICPProtocol.DEFAULT_INFO;
			// If the inpConnection is null request the FrontEnd to reconnect
			if (!inpManager.isConnected()) {
				info |= JICPProtocol.RECONNECT_INFO;
			}
			return info;
	  }
  } // END of inner class OutputManager
  
  
  private void close(Connection c) {
  	try {
  		c.close();
  	}
  	catch (IOException ioe) {
  	}
  }
}

