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
import jade.imtp.leap.FrontEndStub;
import jade.imtp.leap.MicroSkeleton;
import jade.imtp.leap.BackEndSkel;
import jade.imtp.leap.Dispatcher;
import jade.imtp.leap.ICPException;
import jade.util.leap.Properties;
import jade.util.Logger;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Class declaration
 * @author Giovanni Caire - TILAB
 */
public class BIBEDispatcher extends Thread implements BEConnectionManager, Dispatcher, JICPMediator {
	private static final long RESPONSE_TIMEOUT = 60000;
	
	private static final int REACHABLE = 1;
	private static final int UNREACHABLE = 0;
	
	private int frontEndStatus = UNREACHABLE;
  private long              maxDisconnectionTime;
  private long              keepAliveTime;
  private long              lastReceivedTime;

  private JICPServer        myJICPServer;
  private String            myID;

  private int inpCnt = 0;
  private boolean active = true;

  private InpConnectionHolder  inpHolder = new InpConnectionHolder();
  private OutConnectionHolder  outHolder = new OutConnectionHolder();

  private MicroSkeleton mySkel = null;
  private FrontEndStub myStub = null;
  private BackEndContainer myContainer = null;

	private Logger myLogger;
	
  /**
   * Constructor declaration
   */
  public BIBEDispatcher() {
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
    
		// Verbosity
    int verbosity = 1;
  	try {
  		verbosity = Integer.parseInt(props.getProperty("verbosity"));
  	}
  	catch (NumberFormatException nfe) {
      // Use default (1)
  	}
  	/*#CUSTOMJ2SE_INCLUDE_BEGIN
  	verbosity = 4;
  	#CUSTOMJ2SE_INCLUDE_END*/
  	myLogger = new Logger(myID, verbosity);

  	// Max disconnection time
    maxDisconnectionTime = JICPProtocol.DEFAULT_MAX_DISCONNECTION_TIME;
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
    
		// Start the embedded thread dealing with outgoing commands only on the master copy
    if(props.getProperty(Profile.MASTER_NODE_NAME) == null) {
    	start();
    }

    myLogger.log("Created BIBEDispatcher V2.0 ID = "+myID, 1);
    myLogger.log("Max-disconnection-time = "+maxDisconnectionTime, 1);  	
    myLogger.log("Keep-alive-time = "+keepAliveTime, 1);  
    
    startBackEndContainer(props);
  }

  protected final void startBackEndContainer(Properties props) throws ICPException {
    try {
    	myStub = new FrontEndStub(this);

    	props.setProperty(Profile.MAIN, "false");
    	String nodeName = "Back-End[" + myID.replace(':', '_') + "]";
    	props.setProperty(Profile.CONTAINER_NAME, nodeName);
			String masterNode = props.getProperty(Profile.MASTER_NODE_NAME);

			// Add the mediator ID to the profile (it's used as a token
			// to keep related replicas together)
			props.setProperty(Profile.BE_MEDIATOR_ID, myID);

    	myContainer = new BackEndContainer(props, this);
			// Check that the BackEndContainer has successfully joined the platform
			ContainerID cid = (ContainerID) myContainer.here();
			if (cid == null || cid.getName().equals(AgentContainer.UNNAMED_CONTAINER_NAME)) {
				throw new ICPException("BackEnd container failed to join the platform");
			}
    	mySkel = new BackEndSkel(myContainer);

			if(masterNode == null) {
		    String masterAddr = InetAddress.getLocalHost().getHostName() + ':' + myJICPServer.getLocalPort();
		    props.put(Profile.BE_REPLICA_ZERO_ADDRESS, masterAddr);
		    myContainer.activateReplicas();
			}

    	myLogger.log("BackEndContainer successfully joined the platform: name is "+cid.getName(), 2);
    }
    catch (ProfileException pe) {
    	// should never happen
    	pe.printStackTrace();
			throw new ICPException("Error creating profile");
    }
    catch(UnknownHostException uhe) {
			uhe.printStackTrace();
    }
  }
  
  private Object shutdownLock = new Object();
  
  /**
     Shutdown self initiated or forced by the JICPServer this 
     BackEndContainer is attached to.
   */
  public void kill() {
  	// Avoid killing two times
  	synchronized (shutdownLock) {
	  	if (active) {
	  		active = false;
		    // Force the BackEndContainer to terminate. This will also
		    // cause this BIBEDispatcher to terminate and deregister 
		    // from the JICPServer
		    myContainer.shutDown();
	  	}
  	}
  }
  
  /**
     This is called by the JICPServer when a JICP packet addressing this
     mediator as recipient-ID is received. In the case of the BIBEDispatcher
     this should never happen.
   */
  public JICPPacket handleJICPPacket(JICPPacket p, InetAddress addr, int port) throws ICPException {
  	return null;
  } 

  /**
     This is called by the JICPServer when a JICP CREATE_MEDIATOR or
     CONNECT_MEDIATOR is received.
   */
  public JICPPacket handleIncomingConnection(Connection c, JICPPacket pkt, InetAddress addr, int port) {
  	boolean inp = false;
  	byte[] data = pkt.getData();
  	if (data.length == 1) {
  		inp = (data[0] == 1);
  	}
  	else {
  		// Backward compatibility
	   	try {
	   		inp = (new String(data)).equals("inp");
	   	}
	   	catch (Exception e) {}
   	}
   	if (inp) {
   		inpHolder.setConnection(c);
   	}
   	else {
   		outHolder.setConnection(c);
   	}

  	// Update keep-alive info
  	lastReceivedTime = System.currentTimeMillis();
  	
    // On reconnections, a back end container becomes the master node
    if((pkt.getType() == JICPProtocol.CONNECT_MEDIATOR_TYPE) && (!myContainer.isMaster())) {
    	myContainer.becomeMaster();
			start();
    }
    return new JICPPacket(JICPProtocol.RESPONSE_TYPE, JICPProtocol.DEFAULT_INFO, null);
  } 

  public void tick(long currentTime) {
  	if ((currentTime - lastReceivedTime) > (keepAliveTime + 60000)) {
  		// Missing keep-alive.
  		// The OUT connection is no longer valid
  		if (outHolder.isConnected()) {
	  		myLogger.log("Missing keep-alive", 2);
	  		outHolder.resetConnection();
  		}
  		// Check the INP connection. Since this method must return
  		// asap, does it in a separated Thread
  		if (inpHolder.isConnected()) {
	  		Thread t = new Thread() {
	  			public void run() {
	  				try {
		  				JICPPacket pkt = new JICPPacket(JICPProtocol.KEEP_ALIVE_TYPE, JICPProtocol.DEFAULT_INFO, null);
		  				dispatchPacket(pkt, false);
	  					myLogger.log("IC valid", 2);
	  				}
	  				catch (Exception e) {
	  					// Just do nothing
	  				}
	  			}
	  		};
	  		t.start();
  		}
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
    myLogger.log("Initiate BIBEDispatcher shutdown", 2);

    // Deregister from the JICPServer
    if (myID != null) {
	    myJICPServer.deregisterMediator(myID);
  	  myID = null;
    }

		active = false;
		inpHolder.resetConnection();
		outHolder.resetConnection();
  } 

  //////////////////////////////////////////
  // Dispatcher interface implementation
  //////////////////////////////////////////
  public byte[] dispatch(byte[] payload, boolean flush) throws ICPException {
	  JICPPacket pkt = new JICPPacket(JICPProtocol.COMMAND_TYPE, JICPProtocol.DEFAULT_INFO, payload);
	  pkt = dispatchPacket(pkt, flush);
	  return pkt.getData();
  }
  
  private synchronized JICPPacket dispatchPacket(JICPPacket pkt, boolean flush) throws ICPException {
  	Connection inpConnection = inpHolder.getConnection(flush);
  	if (inpConnection != null && active) {
  		int status = 0;
  		if (pkt.getType() == JICPProtocol.KEEP_ALIVE_TYPE) {
		  	myLogger.log("Issuing Keep-alive to FE "+inpCnt, 2);
  		}
  		else {
		  	myLogger.log("Issuing command to FE "+inpCnt, 3);
  		}
	  	pkt.setSessionID((byte) inpCnt);
	  	try {
		  	inpConnection.writePacket(pkt);
		  	status = 1;
		  	
		  	// Create a watch-dog to avoid waiting forever
		  	inpHolder.startWatchDog(RESPONSE_TIMEOUT);
		  	pkt = readPacket(inpConnection);
		  	// Reply received --> Remove the watch-dog
		  	inpHolder.stopWatchDog();
		  	status = 2;
		  	
	  		myLogger.log("Response received from FE "+pkt.getSessionID(), 3); 
		    if (pkt.getType() == JICPProtocol.ERROR_TYPE) {
		    	// Communication OK, but there was a JICP error on the peer
		      throw new ICPException(new String(pkt.getData()));
		    }
		    if ((pkt.getInfo() & JICPProtocol.TERMINATED_INFO) != 0) {
		    	// This is the response to an exit command --> Suicide
		    	shutdown();
		    }
		  	inpCnt = (inpCnt+1) & 0x0f;
		    return pkt;
	  	}
	  	catch (IOException ioe) {
	  		// Can't reach the FrontEnd. 
	  		myLogger.log("IOException IC["+status+"]"+ioe, 2);
	  		inpHolder.resetConnection();
	  		throw new ICPException("Dispatching error.", ioe);
	  	}
  	}
  	else {
  		throw new ICPException("Unreachable");
  	}
  } 
	
  //////////////////////////////////////////////////
  // The embedded Thread handling outgoing commands
  //////////////////////////////////////////////////
  public void run() {
  	JICPPacket lastResponse = null;
  	byte lastSid = 0x10;
  	int status = 0;
  	
		myLogger.log("BIBEDispatcher thread started", 2);
  	while (active) {
  		try {
				while (active) {
					status = 0;
					Connection outConnection = outHolder.getConnection();
					if (outConnection != null) {
						JICPPacket pkt = readPacket(outConnection);
						status = 1;
						
			    	if (pkt.getType() == JICPProtocol.KEEP_ALIVE_TYPE) {
			    		// Keep-alive packet
			    		myLogger.log("Keep-alive received", 4);
						  pkt = new JICPPacket(JICPProtocol.RESPONSE_TYPE, getReconnectInfo(), null);
			    	}
			    	else {
			    		// Outgoing command
				    	if ((pkt.getInfo() & JICPProtocol.TERMINATED_INFO) != 0) {
				    		// PEER TERMINATION NOTIFICATION
				    		// The remote FrontEnd has terminated spontaneously -->
				    		// Terminate and notify up.
				    		handlePeerExited("Peer termination notification received");
				    		break;
				    	}
		  				byte sid = pkt.getSessionID();
		  				if (sid == lastSid) {
		  					myLogger.log("Duplicated command from FE "+sid, 2);
		  					pkt = lastResponse;
		  				}
		  				else {
			      		myLogger.log("Command from FE received "+sid, 3);
								byte[] rspData = mySkel.handleCommand(pkt.getData());
			      		myLogger.log("Command from FE served "+ sid, 3);
							  pkt = new JICPPacket(JICPProtocol.RESPONSE_TYPE, getReconnectInfo(), rspData);
							  pkt.setSessionID(sid);
							  lastSid = sid;
							  lastResponse = pkt;
		  				}
			    	}
	  				status = 2;
	  				
	  				outConnection.writePacket(pkt);
	  				status = 3;
					}
					else {
				    handlePeerExited("Max disconnection timeout expired");
					}
  			}
  		}
  		catch (IOException ioe) {
				if (active) {
  				myLogger.log("IOException OC["+status+"]"+ioe, 2);
  				outHolder.resetConnection();
				}
  		}
  	}
    myLogger.log("BIBEDispatcher Thread terminated", 1);
  }

  private byte getReconnectInfo() {
  	byte info = JICPProtocol.DEFAULT_INFO;
		// If the inpConnection is null request the FrontEnd to reconnect
		if (!inpHolder.isConnected()) {
			info |= JICPProtocol.RECONNECT_INFO;
		}
		return info;
  }
  
  private void handlePeerExited(String msg) {
		myLogger.log(msg, 2);
  	kill();
  }
  
  private JICPPacket readPacket(Connection c) throws IOException {
  	JICPPacket pkt = c.readPacket();
  	// Update keep-alive info
  	lastReceivedTime = System.currentTimeMillis();
  	return pkt;
  }
  	
  /**
     Inner class InpConnectionHolder.
     Wrapper for the connection used to deliver commands to the FrontEnd
   */
  private class InpConnectionHolder {
  	private Connection myConnection;
  	private boolean connectionRefreshed;
  	private boolean waitingForFlush = false;
  	private Thread watchDog = null;
  	
  	private synchronized void setConnection(Connection c) {
  		myLogger.log("New input connection.", 2);
  		// Close the old connection
  		if (myConnection != null) {
  			close(myConnection);
  		}
  		// Stop the WatchDog if any
  		stopWatchDog();
  		// Set the new connection
  		myConnection = c;
  		connectionRefreshed = true;
  		waitingForFlush = myStub.flush();
  	}
  	
  	private synchronized Connection getConnection(boolean flush) {
  		if (waitingForFlush && (!flush)) {
  			return null;
  		}
  		waitingForFlush = false;
  		connectionRefreshed = false;
  		return myConnection;
  	}
  	
  	private synchronized void resetConnection() {
  		if (!connectionRefreshed) {
	  		if (myConnection != null) {
	  			close(myConnection);
	  		}
	  		myConnection = null;
  		}
  	}
  	
  	private synchronized boolean isConnected() {
  		return myConnection != null;
  	}
  	
  	private synchronized void startWatchDog(final long timeout) {
  		watchDog = new Thread() {
		  	public void run() {
		  		try {
			  		Thread.sleep(timeout);
			  		// WatchDog expired --> close the connection
			  		myLogger.log("Response timeout expired", 2);
			  		resetConnection();
		  		}
		  		catch (InterruptedException ie) {
		  			// Watch dog removed. Just do nothing
		  		}
		  	}
  		};
  		watchDog.start();
  	}
  	
  	private synchronized void stopWatchDog() {
  		if (watchDog != null) {
  			watchDog.interrupt();
  			watchDog = null;
  		}
  	}  	
  } // END of inner class InpConnectionHolder

  
  /**
     Inner class OutConnectionHolder
     Wrapper for the connection used to receive commands from the FrontEnd
   */
  private class OutConnectionHolder {
  	private Connection myConnection;
  	private boolean connectionRefreshed;
  	
  	private synchronized void setConnection(Connection c) {
  		myLogger.log("New output connection.", 2);
  		if (myConnection != null) {
  			close(myConnection);
  		}
  		myConnection = c;
  		connectionRefreshed = true;
  		notifyAll();
  	}
  	
  	private synchronized Connection getConnection() {
  		while (myConnection == null) {
  			try {
  				wait(maxDisconnectionTime);
  				if (myConnection == null) {
  					return null;
  				}
  			}
  			catch (Exception e) {
  				myLogger.log("Spurious wake up", 1);
  			}
  		}
  		connectionRefreshed = false;
  		return myConnection;
  	}
  	
  	private synchronized void resetConnection() {
  		if (!connectionRefreshed) {
	  		if (myConnection != null) {
  				close(myConnection);
	  		}
	  		myConnection = null;
  		}
  	}
  	
  	private synchronized boolean isConnected() {
  		return myConnection != null;
  	}
  } // END of inner class OutConnectionHolder

  private void close(Connection c) {
  	try {
  		c.close();
  	}
  	catch (IOException ioe) {
  	}
  }
}

