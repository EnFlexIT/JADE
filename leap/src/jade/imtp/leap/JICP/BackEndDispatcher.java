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

  private JICPServer        myJICPServer;
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
  /**
     Initialize parameters and start the embedded thread
   */
  public void init(JICPServer srv, String id, Properties props) throws ICPException {
    myJICPServer = srv;
    myID = id;
    maxDisconnectionTime = JICPProtocol.DEFAULT_MAX_DISCONNECTION_TIME;
    try {
    	maxDisconnectionTime = Long.parseLong(props.getProperty(JICPProtocol.MAX_DISCONNECTION_TIME_KEY));
    }
    catch (Exception e) {
    	// Keep default
    }
    
    // Start the EndPoit embedded thread
    start();

    //initCnt();
    
    log("Created BackEndDispatcher V1.0 ID = "+myID+" MaxDisconnectionTime = "+maxDisconnectionTime, 1);
  	
    try {
    	myStub = new FrontEndStub(this);
    	props.setProperty(Profile.MAIN, "false");
    	// FIXME: Get main host and port from the command dispatcher
    	props.setProperty(Profile.MAIN_HOST, "localhost");
    	props.setProperty(Profile.MAIN_PORT, String.valueOf(JICPProtocol.DEFAULT_PORT));
    	props.setProperty("mobility", "jade.core.DummyMobilityManager");
    	myContainer = new BackEndContainer(new ProfileImpl(props), this);
			// Check that the BackEndContainer has successfully joined the platform
			ContainerID cid = (ContainerID) myContainer.here();
			if (cid == null || cid.getName().equals("No-Name")) {
				throw new ICPException("BackEnd container failed to join the platform");
			}
    	mySkel = new BackEndSkel(myContainer);
    	log("BackEndContainer successfully joined the platform: name is "+cid.getName());
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
  	try {
  		myContainer.exit();
  	}
  	catch (IMTPException imtpe) {
  		// Should never happen as this is a local call
  		imtpe.printStackTrace();
  	}
  }
  
  /**
     This is called by the JICPServer, but in the case of the BackEndDispatcher
     it should never happen
   */
  public JICPPacket handleJICPPacket(JICPPacket p) throws ICPException {
  	throw new ICPException("Unexpected packet");
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
     Make this BackEndDispatcher terminate.
     In this implementation self is always false.
   */
  public void shutdown(boolean self) {
    log("Initiate BackEndDispatcher shutdown");

    // Deregister from the JICPServer
    if (myID != null) {
	    myJICPServer.deregisterMediator(myID);
  	  myID = null;
    }

    // Enable EndPoint shutdown
    super.shutdown(self);
  } 

  //////////////////////////////////////////
  // Dispatcher interface implementation
  //////////////////////////////////////////
	public byte[] dispatch(byte[] payload) throws ICPException {
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
    JICPPacket rsp = new JICPPacket(JICPProtocol.RESPONSE_TYPE, JICPProtocol.UNCOMPRESSED_INFO, rspData);
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
        log("InterruptedException while waiting for the FrontEnd container to (re)connect", 1);
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
   * Sets the socket connected to the mediated container.
   * This is called by the JICPServer this BackEndDispatcher is 
   * attached to as soon as the FrontEnd container (re)connects.
   * @param s the socket connected to the FrontEnd container
   */
  public synchronized void setConnection(Socket s) {
    if (isConnected()) {
      // If the connection seems to be still valid then reset it so that 
    	// the embedded thread realizes it is no longer valid.
      resetConnection();
    } 
    conn = new Connection(s);
    newConnectionReady = true;
    notifyAll();
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

