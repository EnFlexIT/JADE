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

package jade.imtp.leap.http;

//#MIDP_EXCLUDE_FILE

import jade.core.AgentManager;
import jade.core.BackEndContainer;
import jade.core.BEConnectionManager;
import jade.core.BackEnd;
import jade.core.FrontEnd;
import jade.core.IMTPException;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.ProfileException;
import jade.core.ContainerID;
import jade.imtp.leap.FrontEndStub;
import jade.imtp.leap.MicroSkeleton;
import jade.imtp.leap.BackEndSkel;
import jade.imtp.leap.Dispatcher;
import jade.imtp.leap.ICP;
import jade.imtp.leap.ICPException;
import jade.util.leap.Properties;
import jade.imtp.leap.JICP.*;
import jade.core.TimerDispatcher;
import jade.core.Timer;
import jade.core.TimerListener;
import jade.core.Runtime;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Class declaration
 * @author Giovanni Caire - TILAB
 */
public class HTTPBEDispatcher implements BEConnectionManager, Dispatcher, JICPMediator {
	
	private int verbosity = 1;

  private JICPServer myJICPServer;
  private String myID;

  private MicroSkeleton mySkel = null;
  private FrontEndStub myStub = null;
  private BackEndContainer myContainer = null;

	private OutgoingsHandler myOutgoingsHandler;
	private InetAddress lastRspAddr;
	private int lastRspPort;
  
  /////////////////////////////////////
  // JICPMediator interface implementation
  /////////////////////////////////////
  /**
     Initialize parameters and activate the BackEndContainer
   */
  public void init(JICPServer srv, String id, Properties props) throws ICPException {
    myJICPServer = srv;
    myID = id;
    
		// Verbosity
  	try {
  		verbosity = Integer.parseInt(props.getProperty("verbosity"));
  	}
  	catch (NumberFormatException nfe) {
      // Use default (1)
  	}
  	
  	// Max disconnection time
    long maxDisconnectionTime = JICPProtocol.DEFAULT_MAX_DISCONNECTION_TIME;
    try {
    	maxDisconnectionTime = Long.parseLong(props.getProperty(JICPProtocol.MAX_DISCONNECTION_TIME_KEY));
    }
    catch (Exception e) {
    	// Keep default
    }
    
    myOutgoingsHandler = new OutgoingsHandler(maxDisconnectionTime);
  	
    log("Created HTTPBEDispatcher V2.0 ID = "+myID+" MaxDisconnectionTime = "+maxDisconnectionTime, 1);
  	
    startBackEndContainer(props);
  }

  protected final void startBackEndContainer(Properties props) throws ICPException {
    try {

    	myStub = new FrontEndStub(this);

    	props.setProperty(Profile.MAIN, "false");
    	props.setProperty("mobility", "jade.core.DummyMobilityManager");
	String masterNode = props.getProperty(Profile.MASTER_NODE_NAME);
	if(masterNode == null) {
	    props.setProperty(Profile.CONTAINER_NAME, "BackEnd-" + myID);
	}

	// Add the mediator ID to the profile (it's used as a token
	// to keep related replicas together)
	props.setProperty(Profile.BE_MEDIATOR_ID, myID);

    	myContainer = new BackEndContainer(new ProfileImpl(props), this);
			// Check that the BackEndContainer has successfully joined the platform
			ContainerID cid = (ContainerID) myContainer.here();
			if (cid == null || cid.getName().equals(AgentManager.UNNAMED_CONTAINER_NAME)) {
				throw new ICPException("BackEnd container failed to join the platform");
			}
    	mySkel = new BackEndSkel(myContainer);

	if(masterNode == null) {
	    myContainer.activateReplicas(props);
	}

    	log("BackEndContainer successfully joined the platform: name is "+cid.getName(), 2);
    }
    catch (ProfileException pe) {
    	// should never happen
    	pe.printStackTrace();
	throw new ICPException("Error creating profile");
    }
  }

  public void activateReplica(String addr, Properties props) throws IMTPException {
      try {

	  // Build a CREATE_MEDIATOR packet with the given properties as payload
	  StringBuffer sb = new StringBuffer();
	  Enumeration e = props.propertyNames();
	  while(e.hasMoreElements()) {

	      String key = (String)e.nextElement();
	      String value = props.getProperty(key);
	      sb.append(key);
	      sb.append('=');
	      sb.append(value);
	      sb.append('#');

	  }

	  JICPPacket pkt = new JICPPacket(JICPProtocol.CREATE_MEDIATOR_TYPE, JICPProtocol.DEFAULT_INFO, null, sb.toString().getBytes());

	  // Open a Connection to the given HTTP address and write the packet to it
	  int colonPos = addr.indexOf(':');
	  String host = addr.substring(0, colonPos);
	  String port = addr.substring(colonPos + 1, addr.length());
	  JICPAddress targetAddress = new JICPAddress(host, port, "", "");
	  Connection c = new HTTPClientConnection(targetAddress);
	  OutputStream out = c.getOutputStream();
	  pkt.writeTo(out);

	  // Read back the response
	  InputStream inp = c.getInputStream();
	  pkt = JICPPacket.readFrom(inp);
	  if (pkt.getType() == JICPProtocol.ERROR_TYPE) {
	      // The JICPServer refused to create the Mediator or didn't find myMediator anymore
	      byte[] data = pkt.getData();
	      String errorMsg = (data != null ? new String(data) : null);
	      throw new IMTPException(errorMsg);
	  }
      }
      catch(IOException ioe) {
	  throw new IMTPException("An I/O error occurred", ioe);
      }
  }

  /**
     Shutdown forced by the JICPServer this BackEndContainer is attached 
     to. This method is also called when the FrontEnd spontaneously exits
     and when the the communication with the FrontEnd cannot be 
     re-established.
   */
  public void kill() {
      // Force the BackEndContainer to terminate. This will also
      // cause this HTTPBEDispatcher to terminate and deregister 
      // from the JICPServer
      myContainer.shutDown();
  }
  
  /**
     Handle an incoming JICP packet received by the JICPServer.
   */
  public JICPPacket handleJICPPacket(JICPPacket pkt, InetAddress addr, int port) throws ICPException {
  	if (pkt.getType() == JICPProtocol.COMMAND_TYPE) {
  		// COMMAND
    	if ((pkt.getInfo() & JICPProtocol.TERMINATED_INFO) != 0) {
    		// PEER TERMINATION NOTIFICATION
    		// The remote FrontEnd has terminated spontaneously -->
    		// Terminate and notify up.
    		log("Peer termination notification received", 2);
    		handlePeerExited();
    		return null;
    	}
    	else {
    		// NORMAL COMMAND
    		// Serve the incoming command and send back the response
    		byte sid = pkt.getSessionID();
      	log("Incoming command received "+sid, 3);
		  	byte[] rspData = mySkel.handleCommand(pkt.getData());
      	log("Incoming command served "+sid, 3);
		    pkt = new JICPPacket(JICPProtocol.RESPONSE_TYPE, JICPProtocol.DEFAULT_INFO, rspData);
		    pkt.setSessionID(sid);
		    return pkt;
    	}
  	}
  	else {
  		// RESPONSE
  		lastRspAddr = addr;
  		lastRspPort = port;
  		return myOutgoingsHandler.dispatchResponse(pkt);
  	}
  } 

  /**
     Handle an incoming connection. This is called by the JICPServer
     when a CREATE or CONNECT_MEDIATOR is received.
     The HTTPBEDispatcher reacts to this call by resetting the current
     situation
   */
  public JICPPacket handleIncomingConnection(Connection c, InetAddress addr, int port, byte pktKind) {
      myOutgoingsHandler.setConnecting();

    // On reconnections, a back end container becomes the master node
    if(pktKind == JICPProtocol.CONNECT_MEDIATOR_TYPE) {
	myContainer.becomeMaster();
    }

    // Return an OK response
    return new JICPPacket(JICPProtocol.RESPONSE_TYPE, JICPProtocol.DEFAULT_INFO, null);
  }

  private void dummyResponse() {
      myJICPServer.dummyReply(lastRspAddr, lastRspPort);
  }
  
  //////////////////////////////////////////
  // Dispatcher interface implementation
  //////////////////////////////////////////
  /** 
     This is called by the Stub using this Dispatcher to dispatch 
     a serialized command to the FrontEnd. 
     Mutual exclusion with itself to ensure one command at a time
     is dispatched.
   */
	public synchronized byte[] dispatch(byte[] payload, boolean flush) throws ICPException {
  	JICPPacket pkt = new JICPPacket(JICPProtocol.COMMAND_TYPE, JICPProtocol.COMPRESSED_INFO, payload);
  	pkt = myOutgoingsHandler.deliverCommand(pkt, flush);
    if (pkt.getType() == JICPProtocol.ERROR_TYPE) {
    	// Communication OK, but there was a JICP error on the peer
      throw new ICPException(new String(pkt.getData()));
    }
    return pkt.getData();
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
  	return myStub;
  }

  /**
     Clean up this HTTPBEDispatcher.
     The shutdown process can be activated in the following cases:
     1) The local container is requested to exit --> The exit commad
     is forwarded to the FrontEnd 
     1.a) Forwarding OK. The FrontEnd sends back a response with 
     the TERMINATED_INFO set. When this response is received the 
     shutdown() method is called (see handleJICPPacket()).
     1.b) Forwarding failed. The BackEndContainer ignores the 
     exception and directly calls the shutdown() method.
     
     Note that in the case the FrontEnd spontaneously exits and in the
     case the max disconnection time expires the kill() method is 
     called --> see case 1. 
   */
  public void shutdown() {
    log("Initiate HTTPBEDispatcher shutdown", 2);

    // Deregister from the JICPServer
    if (myID != null) {
	    myJICPServer.deregisterMediator(myID);
  	  myID = null;
    }

    // In case shutdown() is called while the device is disconnected
    // this resets the disconnection timer (if any).
  	myOutgoingsHandler.setTerminating();
  } 

  protected void handlePeerExited() {
		// The FrontEnd has exited --> suicide!
  	myOutgoingsHandler.setTerminating();
  	kill();
  }
  
  protected void handleConnectionError() {
		// The FrontEnd is probably dead --> suicide!
		// FIXME: If there are pending messages that will never be delivered
		// we should notify a FAILURE to the sender
  	myOutgoingsHandler.setTerminating();
		kill();
  }
  
	
  /**
     Inner class OutgoingsHandler.
     This class manages outgoing commands i.e. commands that must be
     sent to the FrontEnd.
     
     NOTE that, since HTTPBEDispatcher is synchronized only one thread at 
     a time can execute the deliverCommand() method. This also ensures 
     that only one thread at a time can execute the dispatchResponse() 
     method. As a consequence it's impossible that at a certain point in 
     time there is both a thread waiting for a command and a therad waiting
     for a response.
     
     @author Giovanni Caire - TILAB
   */
  private class OutgoingsHandler implements TimerListener {
  	private static final int REACHABLE = 0;
  	private static final int CONNECTING = 1;
  	private static final int UNREACHABLE = 2;
  	private static final int TERMINATED = 3;
  	
  	private static final long RESPONSE_TIMEOUT = 30000; // 30 sec
  	
  	private static final int MAX_SID = 0x0f;
  	
  	private int frontEndStatus = CONNECTING;
  	private int outCnt = 0;
  	private Thread commandWaiter = null;
  	private Thread responseWaiter = null;
  	private JICPPacket currentCommand = null;
  	private JICPPacket currentResponse = null;
  	private boolean commandReady = false;
  	private boolean responseReady = false;
  	
  	private long maxDisconnectionTime;
  	private Timer disconnectionTimer = null;
  	
  	private boolean waitingForFlush = false;
  	
  	public OutgoingsHandler(long maxDisconnectionTime) {
  		this.maxDisconnectionTime = maxDisconnectionTime;
  	}
  	
  	/**
  	   Schedule a command for delivery, wait for the response from the
  	   FrontEnd and return it.
  	   @exception ICPException if 1) the frontEndStatus is not REACHABLE,
  	   2) the response timeout expires (the frontEndStatus is set to
  	   UNREACHABLE) or 3) the OutgoingsHandler is reset (the frontEndStatus
  	   is set to CONNECTING).
  	   Called by HTTPBEDispatcher#dispatch()
  	 */
  	public synchronized JICPPacket deliverCommand(JICPPacket cmd, boolean flush) throws ICPException {
  		if (frontEndStatus == REACHABLE) {
	  		// The following check preserves dispatching order when the 
	  		// device has just reconnected but flushing has not started yet
	  		if (waitingForFlush && !flush) {
					throw new ICPException("Upsetting dispatching order");
	  		}
	  		waitingForFlush = false;
	  		
  			// 1) Schedule the command for delivery
			  int sid = outCnt;
			  outCnt = (outCnt+1) & MAX_SID;
			  log("Scheduling outgoing command for delivery "+sid, 3);
			  cmd.setSessionID((byte) sid);
  			currentCommand = cmd;
  			commandReady = true;
  			notifyAll();
  			
  			// 2) Wait for the response
  			while (!responseReady) {
	  			try {
  					responseWaiter = Thread.currentThread();
	  				wait(RESPONSE_TIMEOUT);
	  				responseWaiter = null;
	  				if (!responseReady) {
	  					if (frontEndStatus == CONNECTING) {
	  						// The connection was reset
			  				log("Response will never arrive "+sid, 2);
	  					}
	  					else {
	  						if (frontEndStatus != TERMINATED) {
			  					// Response Timeout expired
					  			log("Response timeout expired "+sid, 2);
					  			frontEndStatus = UNREACHABLE;
					  			activateTimer();
	  						}
	  					}
			  			outCnt--;
			  			if (outCnt < 0) {outCnt = MAX_SID;}
			  			throw new ICPException("Missing response");
	  				}
	  			}
	  			catch (InterruptedException ie) {}
  			}
	  		log("Expected response arrived "+currentResponse.getSessionID(), 3);
	  		responseReady = false;
	  		return currentResponse;
  		}
  		else {
  			throw new ICPException("Unreachable");
  		}
  	}

  	/**
  	   Dispatch a response from the FrontEnd to the issuer of the command
  	   this response refers to.
  	   If no one is waiting for this response (the frontEndStatus must be
  	   different from REACHABLE), set the frontEndStatus to REACHABLE.
  	   @return the next outgoing command to be delivered to the FrontEnd 
  	   or null if the OutgoingsHandler is reset.
  	   Called by HTTPBEDispatcher#handleJICPPacket()
  	 */
  	public synchronized JICPPacket dispatchResponse(JICPPacket rsp) {
  		// 1) Handle the response
  		if (responseWaiter != null) {
  			// There was someone waiting for this response. Dispatch it
	    	log("Response received "+rsp.getSessionID(), 3);
	    	responseReady = true;
	    	currentResponse = rsp;
  			notifyAll();
  		}
  		else {
  			// No one was waiting for this response. It must be the
    		// initial dummy response or a response that arrives after 
  			// the timeout has expired. 
	    	log("Spurious response received", 2);
  		}
  		if (frontEndStatus != REACHABLE) {
  			frontEndStatus = REACHABLE;
  			resetTimer();
  			waitingForFlush = myStub.flush();
  		}
  		
  		// 2) Check if this is the last response
    	if ((rsp.getInfo() & JICPProtocol.TERMINATED_INFO) != 0) {
    		// The FrontEnd has terminated as a consequence of a command issued 
    		// by the local BackEnd. Terminate
    		log("Last response detected", 2);
    		shutdown();
    		return null;
    	}
    	
  		// 3) Wait for the next command
			while (!commandReady) {
  			try {
					commandWaiter = Thread.currentThread();
  				wait();
  				commandWaiter = null;
  				if (!commandReady) {
  					// The connection was reset
			  		log("Return with no command to deliver ", 2);
  					return null;
  				}
  			}
  			catch (InterruptedException ie) {}
			}
  		log("Delivering outgoing command "+currentCommand.getSessionID(), 3);
  		commandReady = false;
  		return currentCommand;
  	}
  	
  	/**
  	   Reset this OutgoingsHandler and set the frontEndStatus to CONNECTING. 
  	   If there is a thread waiting for a command to deliver to the 
  	   FrontEnd it will return null.
  	   If there is a thread waiting for a response it will exit with 
  	   an Exception.
  	   The frontEndStatus is set to CONNECTING.
  	   Called by HTTPBEDispatcher#handleIncomingConnection()
  	 */
  	public synchronized void setConnecting() {
  		log("Resetting the connection ", 2);
  		dummyResponse();
  		frontEndStatus = CONNECTING;
  		reset();
  	}
  	
  	/**
  	   Reset this OutgoingsHandler and set the frontEndStatus to TERMINATED. 
  	 */
  	public synchronized void setTerminating() {
  		frontEndStatus = TERMINATED;
  		reset();
  	}
  	
  	private void reset() {
  		commandReady = false;
  		responseReady = false;
  		currentCommand = null;
  		currentResponse = null;
  		resetTimer();
  		notifyAll();
  	}
  	
  	private void activateTimer() {
  		// Set the disconnection timer
  		long now = System.currentTimeMillis();
			disconnectionTimer = new Timer(now+maxDisconnectionTime, this);
			disconnectionTimer = Runtime.instance().getTimerDispatcher().add(disconnectionTimer);
  		log("Disconnection timer activated.", 2); 
  	}
  	
  	private void resetTimer() {
			if (disconnectionTimer != null) {
				Runtime.instance().getTimerDispatcher().remove(disconnectionTimer);
				disconnectionTimer = null;
			}
  	}
  	
  	public synchronized void doTimeOut(Timer t) {
	  	if (frontEndStatus != REACHABLE) {
		  	log("Max disconnection timeout expired.", 1);
				// The remote FrontEnd is probably down --> notify up.
				handleConnectionError();
	  	}
  	}
  } // END of inner class OutgoingsHandler
  	  	
  
  
  /**
   */
  void log(String s, int level) {
    if (verbosity >= level) {
      String name = Thread.currentThread().toString();
      jade.util.Logger.println(name+"[LVL-"+level+"]["+System.currentTimeMillis()+"]: "+s);
    } 
  }   
}

