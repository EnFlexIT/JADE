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

package jade.imtp.leap.JICP;

//#J2ME_EXCLUDE_FILE

import jade.core.AgentContainer;
import jade.core.BackEndContainer;
import jade.core.BEConnectionManager;
import jade.core.BackEnd;
import jade.core.FrontEnd;
import jade.core.IMTPException;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.ProfileException;
import jade.core.ContainerID;
import jade.core.PlatformManager;
import jade.imtp.leap.FrontEndStub;
import jade.imtp.leap.MicroSkeleton;
import jade.imtp.leap.BackEndSkel;
import jade.imtp.leap.Dispatcher;
import jade.imtp.leap.ICP;
import jade.imtp.leap.ICPException;
import jade.util.Logger;
import jade.util.leap.Properties;

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
public class BackEndDispatcher extends EndPoint implements BEConnectionManager, Dispatcher, JICPMediator {
	
  private long              maxDisconnectionTime;

  private JICPMediatorManager        myMediatorManager;
  private String            myID;

  // The permanent connection to the remote FrontEnd
  private Connection        conn;
  private boolean           newConnectionReady = false;

  private MicroSkeleton mySkel = null;
  private FrontEndStub myStub = null;
  private BackEndContainer myContainer = null;

  /**
   * Constructor declaration
   */
  public BackEndDispatcher() {
  }
  
  /////////////////////////////////////
  // JICPMediator interface implementation
  /////////////////////////////////////
  public String getId() {
  	return myID;
  }
  
  /**
     Initialize parameters and start the embedded thread
   */
  public void init(JICPMediatorManager mgr, String id, Properties props) throws ICPException {

    myMediatorManager = mgr;
    myID = id;
    
		// Verbosity
    /*
      //Not available with new Logging mechanism
      try {
      verbosity = Integer.parseInt(props.getProperty("verbosity"));
      }
      catch (NumberFormatException nfe) {
      // Use default (1)
      }
    */
    
  	// Max disconnection time
    maxDisconnectionTime = JICPProtocol.DEFAULT_MAX_DISCONNECTION_TIME;
    try {
    	maxDisconnectionTime = Long.parseLong(props.getProperty(JICPProtocol.MAX_DISCONNECTION_TIME_KEY));
    }
    catch (Exception e) {
    	// Keep default
    }
    
    // Start the EndPoint embedded thread only on the master copy
    if(props.getProperty(Profile.MASTER_NODE_NAME) == null) {
	start();
    }

    //initCnt();
    
    log("Created BackEndDispatcher V1.0 ID = "+myID+" MaxDisconnectionTime = "+maxDisconnectionTime,Logger.INFO);  	
    startBackEndContainer(props);
  }

  protected final void startBackEndContainer(Properties props) throws ICPException {
    try {

    	myStub = new FrontEndStub(this);

    	props.setProperty(Profile.MAIN, "false");
    	props.setProperty("mobility", "jade.core.DummyMobilityManager");
	String masterNode = props.getProperty(Profile.MASTER_NODE_NAME);

	// Add the mediator ID to the profile (it's used as a token
	// to keep related replicas together)
	props.setProperty(Profile.BE_MEDIATOR_ID, myID);

    	myContainer = new BackEndContainer(props, this);
			// Check that the BackEndContainer has successfully joined the platform
			ContainerID cid = (ContainerID) myContainer.here();
			if (cid == null || cid.getName().equals(PlatformManager.NO_NAME)) {
				throw new ICPException("BackEnd container failed to join the platform");
			}
    	mySkel = new BackEndSkel(myContainer);

	if(masterNode == null) {
	    String masterAddr = myMediatorManager.getLocalHost() + ':' + myMediatorManager.getLocalPort();
	    props.put(Profile.BE_REPLICA_ZERO_ADDRESS, masterAddr);
	    myContainer.activateReplicas();
	}

    	log("BackEndContainer successfully joined the platform: name is "+cid.getName(),Logger.FINEST);
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
      // cause this BackEndDispatcher to terminate and deregister 
      // from the JICPServer
      myContainer.shutDown();
  }
  
  /**
     This is called by the JICPServer. In the case of the BackEndDispatcher
     it can happen when packets from the front end to the back end
     are delivered through a separate channel. 
     @see AsymFrontEndDispatcher#deliver(JICPPacket)
   */
  public JICPPacket handleJICPPacket(JICPPacket p, InetAddress addr, int port) throws ICPException {
  	servePacket(p);
  	// No response is returned as the actual response will go back
  	// through the permanent connection
  	return null;
  } 
  
  public void tick(long time) {
  	// Not used
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

	  // Open a Connection to the given JICP address and write the packet to it
	  int colonPos = addr.indexOf(':');
	  String host = addr.substring(0, colonPos);
	  String port = addr.substring(colonPos + 1, addr.length());
	  JICPAddress targetAddress = new JICPAddress(host, port, "", "");
	  Connection c = new JICPConnection(targetAddress);
	  c.writePacket(pkt);

	  // Read back the response
	  pkt = c.readPacket();
	  if (pkt.getType() == JICPProtocol.ERROR_TYPE) {
	      // The JICPServer refused to create the Mediator or didn't find myMediator anymore
	      byte[] data = pkt.getData();
	      String errorMsg = (data != null ? new String(data) : null);
	      throw new IMTPException(errorMsg);
	  }

	  c.close();
      }
      catch(IOException ioe) {
	  throw new IMTPException("An I/O error occurred", ioe);
      }
  }

  /**
     Make this BackEndDispatcher terminate.
   */
  public void shutdown() {
    log("Initiate BackEndDispatcher shutdown",Logger.FINEST);

    // Deregister from the JICPServer
    if (myID != null) {
	    myMediatorManager.deregisterMediator(myID);
  	  myID = null;
    }

    // Enable EndPoint shutdown
    super.shutdown();
  } 

  //////////////////////////////////////////
  // Dispatcher interface implementation
  //////////////////////////////////////////
	public byte[] dispatch(byte[] payload, boolean flush) throws ICPException {
  	// FIXME: Dispatching order is not guaranteed if this method
  	// is called after the device reconnects, but before flushing has
  	// started
  	JICPPacket p = new JICPPacket(JICPProtocol.COMMAND_TYPE, JICPProtocol.COMPRESSED_INFO, payload);
  	JICPPacket r = deliverCommand(p);
  	updateTransmitted(p.getLength());
  	updateReceived(r.getLength());
    return r.getData();
	}
	
  //////////////////////////////////////////
  // EndPoint abstract class implementation
  //////////////////////////////////////////
  
	/**
	 */
	protected JICPPacket handleCommand(JICPPacket cmd) throws Exception {
  	updateReceived(cmd.getLength());
  	byte[] rspData = mySkel.handleCommand(cmd.getData());
    JICPPacket rsp = new JICPPacket(JICPProtocol.RESPONSE_TYPE, JICPProtocol.DEFAULT_INFO, rspData);
    updateTransmitted(rsp.getLength());
    return rsp;
  }
  
  /**
   */
  protected synchronized void setup() throws ICPException {
    while (!newConnectionReady) {
      try {
        wait(maxDisconnectionTime);
        if (!newConnectionReady) {
        	throw new ICPException("The FrontEnd container is probably down!");
        }
      } 
      catch (InterruptedException ie) {
        log("InterruptedException while waiting for the FrontEnd container to (re)connect",Logger.WARNING);
      } 
    } 
    // If we get here there is a new connection ready --> Pass the connection to the EndPoint
    try {
	    setConnection(conn);
    	newConnectionReady = false;
    }
    catch (IOException ioe) {
    	// The new connection is already down. Ignore it. The embedded thread
    	// will call setup() again.
      log("New connection already down",Logger.WARNING);
    }
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
     The connection is up --> flush bufferd commands. 
   */
	protected void handleConnectionReady() {
		myStub.flush();
	}
	
  /**
   * Prepare to set the connection to the mediated container.
   * This is called by the JICPServer this BackEndDispatcher is 
   * attached to as soon as the FrontEnd container (re)connects.
   * @param c the connection to the FrontEnd container
   */
  public synchronized boolean handleIncomingConnection(Connection c, JICPPacket pkt, InetAddress addr, int port) {
   	if (isConnected()) {
      // If the connection seems to be still valid then reset it so that 
    	// the embedded thread realizes it is no longer valid.
      resetConnection();
    } 
    conn = c;
    newConnectionReady = true;

    // On reconnections, a back end container becomes the master node
    if((pkt.getType() == JICPProtocol.CONNECT_MEDIATOR_TYPE) && (!myContainer.isMaster())) {
	myContainer.becomeMaster();
	start();
    }

    notifyAll();
    return true;
  } 
  
  //////////////////////////////////////////////////////////////////////
  // This part is only related to counting transmitted/received bytes
  //////////////////////////////////////////////////////////////////////
  
  private int transmittedCnt = 0;
  private int receivedCnt = 0;
  private Object cntLock = new Object();
  
  private void updateTransmitted(int n) {
  	synchronized(cntLock) {
  		transmittedCnt += n;
  	}
  }
  
  private void updateReceived(int n) {
  	synchronized(cntLock) {
  		receivedCnt += n;
  	}
  }
  
  private void initCnt() {
  	if (myID != null) {
  		final String id = myID;
	  	Timer t = new Timer(System.currentTimeMillis()+30000, new TimerListener() {
	  		public void doTimeOut(Timer t) {
	  			synchronized (cntLock) {
	  				System.out.println("BackEndDispatcher "+id);
	  				System.out.println("Transmitted cnt = "+transmittedCnt);
	  				System.out.println("Received cnt    = "+receivedCnt);
	  				System.out.println("------------------------");
						transmittedCnt = 0;
						receivedCnt = 0;
	  			}
	  			initCnt();
	  		}
	  	} );
	  	TimerDispatcher td = Runtime.instance().getTimerDispatcher();
	  	td.add(t);
  	}
  }
}

