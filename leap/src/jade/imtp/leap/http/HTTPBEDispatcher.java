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
public class HTTPBEDispatcher implements BEConnectionManager, Dispatcher, JICPMediator, TimerListener {
	
	private int verbosity = 1;
	
  private long maxDisconnectionTime;
  private Timer disconnectionTimer;
  private Object timerLock = new Object();
  private boolean connectionUp = true;
  
  private boolean terminating = false;
  
  private boolean busy = true;
  private Object busyLock = new Object();
  
  private JICPPacket currentCommand, currentResponse;
  private boolean commandReady = false;
  private boolean responseReady = false;
  private Object commandLock = new Object();
  private Object responseLock = new Object();
  private long responseTimeout = 20000;

  private JICPServer myJICPServer;
  private String myID;

  private MicroSkeleton mySkel = null;
  private FrontEndStub myStub = null;
  private BackEndContainer myContainer = null;

  private static final boolean RECONNECTION_TEST_ACTIVE = false;
  private static final long RECONNECTION_TEST_PERIOD = 60000;
  
  /////////////////////////////////////
  // JICPMediator interface implementation
  /////////////////////////////////////
  /**
     Initialize parameters and start the embedded thread
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
    maxDisconnectionTime = JICPProtocol.DEFAULT_MAX_DISCONNECTION_TIME;
    try {
    	maxDisconnectionTime = Long.parseLong(props.getProperty(JICPProtocol.MAX_DISCONNECTION_TIME_KEY));
    }
    catch (Exception e) {
    	// Keep default
    }
    
    log("Created HTTPBEDispatcher V1.0 ID = "+myID+" MaxDisconnectionTime = "+maxDisconnectionTime, 1);
  	
    startBackEndContainer(props);
    
    if (RECONNECTION_TEST_ACTIVE) {
    	activateReconnectionTest();
    }
  }
  
  protected final void startBackEndContainer(Properties props) throws ICPException {
    try {
    	myStub = new FrontEndStub(this);
    	props.setProperty(Profile.MAIN, "false");
    	props.setProperty("mobility", "jade.core.DummyMobilityManager");
    	myContainer = new BackEndContainer(new ProfileImpl(props), this);
			// Check that the BackEndContainer has successfully joined the platform
			ContainerID cid = (ContainerID) myContainer.here();
			if (cid == null || cid.getName().equals("No-Name")) {
				throw new ICPException("BackEnd container failed to join the platform");
			}
    	mySkel = new BackEndSkel(myContainer);
    	log("BackEndContainer successfully joined the platform: name is "+cid.getName(), 2);
    }
    catch (ProfileException pe) {
    	// should never happen
    	pe.printStackTrace();
			throw new ICPException("Error creating profile");
    }
  }
  
  /**
     Shutdown forced by the JICPServer this BackEndContainer is attached 
     to
   */
  public void kill() {
  	// Force the BackEndContainer to terminate. This will also
  	// cause this HTTPBEDispatcher to terminate 
  	try {
  		myContainer.exit();
  	}
  	catch (IMTPException imtpe) {
  		// Should never happen as this is a local call
  		imtpe.printStackTrace();
  	}
  }
  
  /**
     Handle an incoming JICP packet received by the JICPServer.
   */
  public JICPPacket handleJICPPacket(JICPPacket pkt) throws ICPException {
  	if (pkt.getType() == JICPProtocol.COMMAND_TYPE) {
    	if ((pkt.getInfo() & JICPProtocol.TERMINATED_INFO) != 0) {
    		// PEER TERMINATION NOTIFICATION
    		// The remote FrontEnd has terminated spontaneously -->
    		// Terminate and notify up.
    		log("Peer termination notification received", 2);
    		shutdown();
    		handlePeerExited();
    		return null;
    	}
    	else {
    		// NORMAL COMMAND
    		// Serve the incoming command and send back the response
      	log("Incoming command received", 3);
		  	byte[] rspData = mySkel.handleCommand(pkt.getData());
      	log("Incoming command served", 3);
		    return new JICPPacket(JICPProtocol.RESPONSE_TYPE, JICPProtocol.DEFAULT_INFO, rspData);
    	}
  	}
  	else {
  		// RESPONSE
    	byte data[] = pkt.getData();
    	if (data != null && data.length == 1 && data[0] == (byte) 0xff) {
    		// It's the dummy initial response --> no one is waiting for it
    		log("Dummy response received", 2);
    	}
    	else {
  			// It's a real response --> Pass the response to the issuer of 
    		// the command 
    		log("Response received", 3);
  			setResponse(pkt);
    	}
    	
    	if ((pkt.getInfo() & JICPProtocol.TERMINATED_INFO) != 0) {
    		// It was the last response (the FrontEnd has terminated as a consequence
    		// of a command issued by the local BackEnd) --> Terminate
    		log("Last response detected", 2);
    		shutdown();
    		return null;
    	}
    	else {
				// It was not the last response --> unlock the connection 
    		// and wait for the next outgoing command
    		unlockConnection();
    		pkt = waitForCommand();
    		if (pkt != null) {
	    		log("Issuing outgoing command", 3); 
    		}
    		return pkt;
    	}
  	}
  } 

  /**
     Handle an incoming connection. This is called by the JICPServer
     when a CREATE or CONNECT_MEDIATOR is received.
   */
  public synchronized JICPPacket handleIncomingConnection(Connection c) {
  	// The connection is up again. Remove the Timer for max
  	// disconnection time (if any)
		connectionUp = true;
		if (disconnectionTimer != null) {
			Runtime.instance().getTimerDispatcher().remove(disconnectionTimer);
			disconnectionTimer = null;
		}
  	
  	// Activate buffered commands flushing
  	myStub.flush();
  	
  	// Just return an OK response
    return new JICPPacket(JICPProtocol.RESPONSE_TYPE, JICPProtocol.DEFAULT_INFO, null);
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
     Make this HTTPBEDispatcher terminate.
     The shutdown process can be activated in the following cases:
     1) The local container is requested to exit --> The exit commad
     is forwarded to the FrontEnd 
     1.a) Forwarding OK. The FrontEnd sends back a response with 
     the TERMINATED_INFO set. When this response is received the 
     shutdown() method is called.
     1.b) Forwarding failed. The BackEndContainer ignores the 
     exception and directly calls the shutdown() method.
     2) The FrontEnd spontaneously terminates. When the termination 
     notification is received the shutdown() method is called.
     3) A disconnection is detected and the FrontEnd no longer 
     reconnects. When the maxDisconnectionTime expires the shutdown()
     method is called. 
   */
  public void shutdown() {
    log("Initiate HTTPBEDispatcher shutdown", 2);

    // Deregister from the JICPServer
    if (myID != null) {
	    myJICPServer.deregisterMediator(myID);
  	  myID = null;
    }

    // This makes the thread waiting for an outgoing command wake up
    // and close the connection
    synchronized (commandLock) {
    	terminating = true;
    	currentCommand = null;
    	commandLock.notifyAll();
    }
  } 

  //////////////////////////////////////////
  // Dispatcher interface implementation
  //////////////////////////////////////////
	public synchronized byte[] dispatch(byte[] payload) throws ICPException {
		if (connectionUp && !terminating) {
	  	JICPPacket cmd = new JICPPacket(JICPProtocol.COMMAND_TYPE, JICPProtocol.COMPRESSED_INFO, payload);
	  	lockConnection();
	  	setCommand(cmd);
	  	JICPPacket rsp = waitForResponse();
	    return rsp.getData();
		}
		else {
			throw new ICPException("Disconnected");
		}
	}
	
	/**
	   Lock the connection (only one outgoing command at 
	   a time can be dispatched)
	 */
	private void lockConnection() {
  	synchronized (busyLock) {
			while (busy) {
				try {
	  			busyLock.wait();
				}
				catch (InterruptedException ie) {
				}
			}
			busy = true;
  	}
	}
	
	/**
	   Unlock the connection 
	 */
	private void unlockConnection() {
  	synchronized (busyLock) {
			busy = false;
			busyLock.notifyAll();
  	}
	}
	
	/**
	   Notify the thread that will actually deliver the command
	   over the network
	 */
  private void setCommand(JICPPacket cmd) {
		synchronized (commandLock) {
			commandReady = true;
			currentCommand = cmd;
			commandLock.notifyAll();
		}
  }
  
  /**
     Wait for the response to a previously dispatched command
   */
  private JICPPacket waitForResponse() throws ICPException {
  	synchronized (responseLock) {
  		while (!responseReady) {
  			try {
	  			responseLock.wait(responseTimeout);
	  			if (!responseReady) {
	  				// The response timeout has expired --> The FrontEnd
	  				// is disconnected
	  				connectionUp = false;		  				
	  				disconnectionTimer = Runtime.instance().getTimerDispatcher().add(new Timer(System.currentTimeMillis()+maxDisconnectionTime, this));
	  				throw new ICPException("Response timeout expired");
	  			}
  			}
				catch (InterruptedException ie) {
				}
  		}
  		responseReady = false;
  		return currentResponse;
  	}
  }
  
  /**
     Notify the thread that is waiting for the response
   */
  private void setResponse(JICPPacket rsp) {
  	synchronized (responseLock) {
  		responseReady = true;
  		currentResponse = rsp;
  		responseLock.notifyAll();
  	}
  }

  /**
  	 Wait for the next command
   */
  private JICPPacket waitForCommand() {
  	synchronized (commandLock) {
  		while (!commandReady && !terminating) {
  			try {
	  			commandLock.wait();
  			}
				catch (InterruptedException ie) {
				}
  		}
  		commandReady = false;
  		return currentCommand;
  	}
  }
  
  /////////////////////////////////////////
  // TimerListener interface implementation
  /////////////////////////////////////////
  public void doTimeOut(Timer t) {
  	log("Max disconnection timeout expired", 1);
		// The remote FrontEnd is probably down -->
		// Terminate and notify up.
		shutdown();
		handleConnectionError();
  }
  
  protected void handlePeerExited() {
		// The FrontEnd has exited --> suicide!
  	kill();
  }
  
  protected void handleConnectionError() {
		// The FrontEnd is probably dead --> suicide!
		// FIXME: If there are pending messages that will never be delivered
		// we should notify a FAILURE to the sender
		kill();
  }
  
  /**
   */
  void log(String s, int level) {
    if (verbosity >= level) {
      String name = Thread.currentThread().toString();
      jade.util.Logger.println(name+": "+s);
    } 
  } 
  
  private void activateReconnectionTest() {
  	if (myID != null) {
  		final String id = myID;
	  	Timer t = new Timer(System.currentTimeMillis()+RECONNECTION_TEST_PERIOD, new TimerListener() {
	  		public void doTimeOut(Timer t) {
	  			log("Reconnection test: simulating a disconnection", 2);
			    // This makes the thread waiting for an outgoing command wake up
			    // and close the connection
			    synchronized (commandLock) {
			    	commandReady = true;
			    	currentCommand = null;
			    	commandLock.notifyAll();
			    }
	  			activateReconnectionTest();
	  		}
	  	} );
	  	TimerDispatcher td = Runtime.instance().getTimerDispatcher();
	  	td.add(t);
  	}
  }
}

