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

import jade.core.FEConnectionManager;
import jade.core.FrontEnd;
import jade.core.BackEnd;
import jade.core.IMTPException;
import jade.core.TimerDispatcher;
import jade.core.Timer;
import jade.core.TimerListener;
import jade.mtp.TransportAddress;
import jade.imtp.leap.BackEndStub;
import jade.imtp.leap.MicroSkeleton;
import jade.imtp.leap.FrontEndSkel;
import jade.imtp.leap.Dispatcher;
import jade.imtp.leap.ICPException;
import jade.imtp.leap.ConnectionListener;
import jade.util.leap.Properties;

import java.io.*;
import java.util.Vector;
import java.util.Enumeration;

/**
 * Class declaration
 * @author Giovanni Caire - TILAB
 */
public class BIFEDispatcher implements FEConnectionManager, Dispatcher, TimerListener, Runnable {
	private static final byte INP = (byte) 1;
	private static final byte OUT = (byte) 0;
	
  private MicroSkeleton mySkel = null;
  private BackEndStub myStub = null;

  // Variables related to the connection with the Mediator
  protected TransportAddress mediatorTA;
  private String myMediatorID;
  private long retryTime = JICPProtocol.DEFAULT_RETRY_TIME;
  private long maxDisconnectionTime = JICPProtocol.DEFAULT_MAX_DISCONNECTION_TIME;
  private long keepAliveTime = JICPProtocol.DEFAULT_KEEP_ALIVE_TIME;
  private Timer kaTimer;
  private String owner;
  private Properties props;

  private Connection outConnection;
  private InputManager myInputManager;
  private ConnectionListener myConnectionListener;
  
  private boolean active = true;
  private boolean waitingForFlush = false;
  private boolean refreshingInput = false;
  private int outCnt = 0;
  private Thread terminator;
  
  private int verbosity = 1;

  private String beAddrsText;
  private String[] backEndAddresses;
  
  
  //////////////////////////////////////////////
  // FEConnectionManager interface implementation
  //////////////////////////////////////////////
  
  /**
   * Connect to a remote BackEnd and return a stub to communicate with it
   */
  public BackEnd getBackEnd(FrontEnd fe, Properties props) throws IMTPException {  	
  	this.props = props;
  	try {

	    beAddrsText = props.getProperty(FrontEnd.REMOTE_BACK_END_ADDRESSES);
	    backEndAddresses = parseBackEndAddresses(beAddrsText);

	    // Verbosity
	    try {
				verbosity = Integer.parseInt(props.getProperty("jade_imtp_leap_JICP_BIFEDispatcher_verbosity"));
	    }
	    catch (NumberFormatException nfe) {
				// Use default (1)
	    }
	  		    
	    // Host
	    String host = props.getProperty("host");
	    if (host == null) {
				host = "localhost";
	    }
	    // Port
	    int port = JICPProtocol.DEFAULT_PORT;
	    try {
				port = Integer.parseInt(props.getProperty("port"));
	    }
	    catch (NumberFormatException nfe) {
				// Use default
	    }

	    // Compose URL 
	    mediatorTA = JICPProtocol.getInstance().buildAddress(host, String.valueOf(port), null, null);
	    log("Remote URL="+JICPProtocol.getInstance().addrToString(mediatorTA), 2);

	    // Read (re)connection retry time
	    String tmp = props.getProperty(JICPProtocol.RECONNECTION_RETRY_TIME_KEY);
	    try {
				retryTime = Long.parseLong(tmp);
	    }
	    catch (Exception e) {
				// Use default
	    }
	    log("Recon. time="+retryTime, 2);

	    // Read Max disconnection time
	    tmp = props.getProperty(JICPProtocol.MAX_DISCONNECTION_TIME_KEY);
	    try {
				maxDisconnectionTime = Long.parseLong(tmp);
	    } 
	    catch (Exception e) {
				// Use default
	    }
	    log("Max discon. time="+maxDisconnectionTime, 2);

	    // Read Keep-alive time
	    tmp = props.getProperty(JICPProtocol.KEEP_ALIVE_TIME_KEY);
	    try {
				keepAliveTime = Long.parseLong(tmp);
	    } 
	    catch (Exception e) {
				// Use default
	    }
	    log("Keep-alive time="+keepAliveTime, 2);

	    // Create the BackEnd stub and the FrontEnd skeleton
	    myStub = new BackEndStub(this);
	    mySkel = new FrontEndSkel(fe);

	    // Read the owner if any
	    owner = props.getProperty("owner");

	    createBackEnd();
	    log("Connection OK", 1);

	    // Start the InputManager dealing with incoming commands
	    refreshInp();

	    return myStub;
  	}
  	catch (ICPException icpe) {
	    throw new IMTPException("Connection error", icpe);
  	}
  }

  /**
     Make this BIFEDispatcher terminate.
	 */
  public synchronized void shutdown() {
  	active = false;
  	
  	terminator = Thread.currentThread();
  	if (terminator != myInputManager) {
	  	// This is a self-initiated shut down --> we must explicitly
	  	// notify the BackEnd.
  		if (outConnection != null) {
		  	log("Sending termination notification", 2);
	  		JICPPacket pkt = new JICPPacket(JICPProtocol.COMMAND_TYPE, JICPProtocol.TERMINATED_INFO, null);
	  		try {
	  			writePacket(pkt, outConnection);
	  		}
	  		catch (Exception e) {
	  			// When the BackEnd receives the termination notification,
	  			// it just closes the connection --> we always have this
	  			// exception
	  			log("BackEnd closed", 2);
	  		}
  		}
  	} 		
  }
  
  /**
     Send the CREATE_MEDIATOR command with the necessary parameter
     in order to create the BackEnd in the fixed network.
     Executed at bootstrap time by the thread that creates the 
     FrontEndContainer.
   */
  private void createBackEnd() throws IMTPException {
    StringBuffer sb = new StringBuffer();
    appendProp(sb, JICPProtocol.MEDIATOR_CLASS_KEY, "jade.imtp.leap.JICP.BIBEDispatcher");
    appendProp(sb, "verbosity", String.valueOf(verbosity));
    appendProp(sb, JICPProtocol.MAX_DISCONNECTION_TIME_KEY, String.valueOf(maxDisconnectionTime));
    appendProp(sb, JICPProtocol.KEEP_ALIVE_TIME_KEY, String.valueOf(keepAliveTime));
    if(beAddrsText != null) {
		  appendProp(sb, FrontEnd.REMOTE_BACK_END_ADDRESSES, beAddrsText);
    }
    if (owner != null) {
		  appendProp(sb, "owner", owner);
    }
    JICPPacket pkt = new JICPPacket(JICPProtocol.CREATE_MEDIATOR_TYPE, JICPProtocol.DEFAULT_INFO, null, sb.toString().getBytes());

    // Try first with the current transport address, then with the various backup addresses
    for(int i = -1; i < backEndAddresses.length; i++) {
	
		  if(i >= 0) {
	      // Set the mediator address to a new address..
	      String addr = backEndAddresses[i];
	      int colonPos = addr.indexOf(':');
	      String host = addr.substring(0, colonPos);
	      String port = addr.substring(colonPos + 1, addr.length());
	      mediatorTA = new JICPAddress(host, port, myMediatorID, "");
		  }
	
		  try {
	      log("Connecting to jicp://"+mediatorTA.getHost()+":"+mediatorTA.getPort(), 1);
	      outConnection = new JICPConnection(mediatorTA);
	      writePacket(pkt, outConnection);
	      pkt = outConnection.readPacket();

	      if (pkt.getType() != JICPProtocol.ERROR_TYPE) {
				  // BackEnd creation successful
		      myMediatorID = new String(pkt.getData());
		      // Complete the mediator address with the mediator ID
		      mediatorTA = new JICPAddress(mediatorTA.getHost(), mediatorTA.getPort(), myMediatorID, null);
				  return;
	      }
		  }
		  catch (IOException ioe) {
	      // Ignore it, and try the next address...
		  	log("Connection error. "+ioe.toString(), 1);
		  }
    }

    // No address succeeded: try to handle the problem...
    throw new IMTPException("Error creating the BackEnd.");
  }
  
  private void appendProp(StringBuffer sb, String key, String val) {
  	sb.append(key);
  	sb.append('=');
  	sb.append(val);
  	sb.append('#');
  }
  
  //////////////////////////////////////////////
  // Dispatcher interface implementation
  //////////////////////////////////////////////
  
  /**
     Deliver a serialized command to the BackEnd.
     @return The serialized response
   */
  public synchronized byte[] dispatch(byte[] payload, boolean flush) throws ICPException {
  	if (outConnection != null) {
  		if (waitingForFlush && !flush) {
				throw new ICPException("Upsetting dispatching order");
  		}
  		waitingForFlush = false;
  
  		int status = 0;
	  	log("Issuing outgoing command "+outCnt, 3);
	  	JICPPacket pkt = new JICPPacket(JICPProtocol.COMMAND_TYPE, JICPProtocol.DEFAULT_INFO, payload);
	  	pkt.setSessionID((byte) outCnt);
	  	try {
		  	writePacket(pkt, outConnection);
		  	status = 1;
		  	pkt = outConnection.readPacket();
		  	status = 2;
	  		log("Response received "+pkt.getSessionID(), 3); 
		    if (pkt.getType() == JICPProtocol.ERROR_TYPE) {
		    	// Communication OK, but there was a JICP error on the peer
		      throw new ICPException(new String(pkt.getData()));
		    }
  			if ((pkt.getInfo() & JICPProtocol.RECONNECT_INFO) != 0) { 
  				// The BackEnd is considering the input connection no longer valid
  				refreshInp();
  			}
			  outCnt = (outCnt+1) & 0x0f;
		    return pkt.getData();
	  	}
	  	catch (IOException ioe) {
	  		// Can't reach the BackEnd. 
  			log("IOException OC["+status+"]"+ioe, 2);
  			refreshOut();
	  		throw new ICPException("Dispatching error.", ioe);
	  	}
  	}
  	else {
  		throw new ICPException("Unreachable");
  	}
  } 

  // These variables are only used within the InputManager class,
  // but are declared externally since they must "survive" when 
  // an InputManager is replaced
  private JICPPacket lastResponse = null;
  private byte lastSid = 0x10;
  private int cnt = 0;
  
  /**
     Inner class InputManager.
     This class is responsible for serving incoming commands
   */
  private class InputManager extends Thread {
  	private int myId;
  	private Connection myConnection = null;
  	
	  public void run() {
	  	if (cnt == 0) {
		  	// Give precedence to the Thread that is creating the BackEnd
		  	Thread.yield();
		  	
		    // In the meanwhile load the ConnectionListener if any
		    try {
		    	myConnectionListener = (ConnectionListener) Class.forName(props.getProperty("connection-listener")).newInstance();
		    }
		    catch (Exception e) {
		    	// Just ignore it
		    }
	  	}
	    
	  	myId = cnt++;
	    log("IM-"+myId+" started", 2);
	  	
	  	int status = 0;
			connect(INP);
  		try {
				while (isConnected()) {
					status = 0;
					JICPPacket pkt = myConnection.readPacket();
					status = 1;
  				byte sid = pkt.getSessionID();
  				if (sid == lastSid) {
  					// Duplicated packet
  					log("Duplicated packet received "+sid, 2);
  					pkt = lastResponse;
  				}
  				else {
						if (pkt.getType() == JICPProtocol.KEEP_ALIVE_TYPE) {
							// Keep-alive
						  pkt = new JICPPacket(JICPProtocol.RESPONSE_TYPE, JICPProtocol.DEFAULT_INFO, null);
						}
						else {
							// Incoming command
		      		log("Incoming command received "+sid, 3);
							byte[] rspData = mySkel.handleCommand(pkt.getData());
		      		log("Incoming command served "+ sid, 3);
						  pkt = new JICPPacket(JICPProtocol.RESPONSE_TYPE, JICPProtocol.DEFAULT_INFO, rspData);
						}
					  pkt.setSessionID(sid);
					  if (Thread.currentThread() == terminator) {
					  	// Attach the TERMINATED_INFO flag to the response
					  	pkt.setTerminatedInfo();
					  }
					  lastSid = sid;
					  lastResponse = pkt;
  				}
  				status = 2;
  				writePacket(pkt, myConnection);
  				status = 3;
  			}
  		}
  		catch (IOException ioe) {
				if (active) {
  				log("IOException IC["+status+"]"+ioe, 2);
  				refreshInp();
				}
  		}
  		
	    log("IM-"+myId+" terminated", 2);
	  }
	  
	  private void close() {
	  	try {
	  		myConnection.close();
	  	}
	  	catch (Exception e) {}
	  	myConnection = null;
	  }
	  
	  private final void setConnection(Connection c) {
	  	refreshingInput = false;
	  	myConnection = c;
	  }
	  
	  private final boolean isConnected() {
	  	return myConnection != null;
	  }
  } // END of inner class InputManager

  /**
     Close the current InputManager (if any) and start a new one
   */
  private synchronized void refreshInp() {
  	// Avoid 2 refresh at the same time
  	if (!refreshingInput && active) {
	  	// Close the current InputManager	
	  	if (myInputManager != null && myInputManager.isConnected()) {
	  		myInputManager.close();
				if (outConnection != null && myConnectionListener != null) {
					myConnectionListener.handleDisconnection();
				}
			}
			
			// Start a new InputManager
			refreshingInput = true;
	  	myInputManager = new InputManager();
	  	myInputManager.start();
  	}
  }
  	
  /**
     Close the current outConnection (if any) and starts a new thread
     that asynchronously tries to restore it.
   */
  private synchronized void refreshOut() {
  	// Close the outConnection
  	if (outConnection != null) {
  		try {
  			outConnection.close();
  		}
  		catch (Exception e) {}
  		outConnection = null;
			if (myInputManager.isConnected() && myConnectionListener != null) {
				myConnectionListener.handleDisconnection();
			}
  	}
  	
  	// Asynchronously try to recreate the outConnection
		Thread t = new Thread(this);
		t.start();
  }
	
  /**
     Asynchronously restore the OUT connection
   */
	public void run() {
		connect(OUT);
	}
	

  private void connect(byte type) {
  	int cnt = 0;
  	long startTime = System.currentTimeMillis();
  	while (active) {
	  	try {
		  	log("Connect to "+mediatorTA.getHost()+":"+mediatorTA.getPort()+" "+type+"("+cnt+")", 2);
		  	Connection c = new JICPConnection(mediatorTA);
		  	JICPPacket pkt = new JICPPacket(JICPProtocol.CONNECT_MEDIATOR_TYPE, JICPProtocol.DEFAULT_INFO, mediatorTA.getFile(), new byte[]{type});
		  	writePacket(pkt, c);
		  	pkt = c.readPacket();
			  if (pkt.getType() == JICPProtocol.ERROR_TYPE) {
		      // The JICPServer didn't find my Mediator.  
		      handleError();
		      return;
			  }
			  log("Connect OK",2);
			  handleReconnection(c, type);
			  return;
	  	}
	  	catch (IOException ioe) {
	  		log("Connect failed "+ioe.toString(), 2);
	  		cnt++;
	  		if ((System.currentTimeMillis() - startTime) > maxDisconnectionTime) {
	  			handleError();
	  			return;
	  		}
	  		else {
	  			// Wait a bit before trying again
	  			try {
	  				Thread.sleep(retryTime);
	  			}
	  			catch (Exception e) {}
	  		}
	  	}
  	}
  }

	private synchronized void handleReconnection(Connection c, byte type) {
		boolean transition = false;
		if (type == INP) {
			myInputManager.setConnection(c);
			if (outConnection != null) {
				transition = true;
			}
		}
		else if (type == OUT) {
			outConnection = c;
			// The Output connection is available again --> 
			// Activate postponed commands flushing
			waitingForFlush = myStub.flush();
			if (myInputManager.isConnected()) {
				transition = true;
			}
		}
		if (transition && myConnectionListener != null) {
			myConnectionListener.handleReconnection();
		}
	}
	
	private void handleError() {
		log("Can't reconnect ("+System.currentTimeMillis()+")", 0);
		if (myConnectionListener != null) {
			myConnectionListener.handleReconnectionFailure();
		}
		myInputManager.close();
		active = false;
	}
  
  private String[] parseBackEndAddresses(String addressesText) {
    Vector addrs = new Vector();

    if(addressesText != null && !addressesText.equals("")) {
	// Copy the string with the specifiers into an array of char
	char[] addressesChars = new char[addressesText.length()];

    	addressesText.getChars(0, addressesText.length(), addressesChars, 0);

    	// Create the StringBuffer to hold the first address
    	StringBuffer sbAddr = new StringBuffer();
    	int i = 0;

    	while(i < addressesChars.length) {
	    char c = addressesChars[i];

	    if((c != ',') && (c != ';') && (c != ' ') && (c != '\n') && (c != '\t')) {
        	sbAddr.append(c);
	    }
	    else {

        	// The address is terminated --> Add it to the result list
        	String tmp = sbAddr.toString().trim();

        	if (tmp.length() > 0) {
		    // Add the Address to the list
		    addrs.addElement(tmp);
        	}

        	// Create the StringBuffer to hold the next specifier
        	sbAddr = new StringBuffer();
	    }

	    ++i;
    	}

    	// Handle the last specifier
    	String tmp = sbAddr.toString().trim();

    	if(tmp.length() > 0) {
	    // Add the Address to the list
	    addrs.addElement(tmp);
    	}
    }

    // Convert the list into an array of strings
    String[] result = new String[addrs.size()];
    for(int i = 0; i < result.length; i++) {
			result[i] = (String)addrs.elementAt(i);
    }

    return result;

  }
  
  
  private void writePacket(JICPPacket pkt, Connection c) throws IOException {
  	c.writePacket(pkt);
  	if (Thread.currentThread() == terminator) {
  		myInputManager.close();
  	}
  	else {
	  	updateKeepAlive();
  	}
  }
  
  ////////////////////////////////////////
  // Keep-alive mechanism management
  ////////////////////////////////////////
  
  // Mutual exclusion with doTimeOut()
  private synchronized void updateKeepAlive() {
  	if (keepAliveTime > 0) {
	  	TimerDispatcher td = TimerDispatcher.getTimerDispatcher();
	  	if (kaTimer != null) {
		  	td.remove(kaTimer);
	  	}
	  	kaTimer = td.add(new Timer(System.currentTimeMillis()+keepAliveTime, this));
  	}
  }
  
  // Mutual exclusion with updateKeepAlive() and dispatch()
  public synchronized void doTimeOut(Timer t) {
  	if (t == kaTimer) { 
  		// Send a keep-alive packet to the BackEnd
  		if (outConnection != null) {
  			JICPPacket pkt = new JICPPacket(JICPProtocol.KEEP_ALIVE_TYPE, JICPProtocol.DEFAULT_INFO, null);
  			try {
	  			writePacket(pkt, outConnection);
	  			pkt = outConnection.readPacket();
	  			if ((pkt.getInfo() & JICPProtocol.RECONNECT_INFO) != 0) { 
	  				// The BackEnd is considering the input connection no longer valid
	  				refreshInp();
	  			}	  				
  			}
  			catch (IOException ioe) {
  				log("KA error "+ioe.toString(), 2);
  				refreshOut();
  			}
  		}
  	}
  }
  	
  /**
   */
  void log(String s, int level) {
    if (verbosity >= level) {
      String name = Thread.currentThread().toString();
      jade.util.Logger.println(name+": "+s);
    } 
  } 
}

