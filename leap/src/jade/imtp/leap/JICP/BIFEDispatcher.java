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

import jade.core.MicroRuntime;
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
import jade.util.Logger;

import java.io.*;
import java.util.Vector;
import java.util.Enumeration;

/**
 * Class declaration
 * @author Giovanni Caire - TILAB
 */
public class BIFEDispatcher implements FEConnectionManager, Dispatcher, TimerListener, Runnable {
	protected static final byte INP = (byte) 1;
	protected static final byte OUT = (byte) 0;
	
	private static final String MSISDN = "msisdn";
	
	protected String myMediatorClass = "jade.imtp.leap.JICP.BIBEDispatcher";
	
  private MicroSkeleton mySkel = null;
  private BackEndStub myStub = null;

  // Variables related to the connection with the Mediator
  protected TransportAddress mediatorTA;
  private String myMediatorID;
  private long retryTime = JICPProtocol.DEFAULT_RETRY_TIME;
  private long maxDisconnectionTime = JICPProtocol.DEFAULT_MAX_DISCONNECTION_TIME;
  private long keepAliveTime = JICPProtocol.DEFAULT_KEEP_ALIVE_TIME;
  private Timer kaTimer;
  private Properties props;

  protected Connection outConnection;
  protected InputManager myInputManager;
  private ConnectionListener myConnectionListener;
  
  private boolean active = true;
  private boolean waitingForFlush = false;
  protected boolean refreshingInput = false;
  protected boolean refreshingOutput = false;
  private byte lastSid = 0x0f;
  private int outCnt = 0;
  private Thread terminator;
  
  private String beAddrsText;
  private String[] backEndAddresses;
  
  private Logger myLogger = Logger.getMyLogger(getClass().getName());
  
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

	    // Host
	    String host = props.getProperty(MicroRuntime.HOST_KEY);
	    if (host == null) {
				host = "localhost";
	    }
	    
	    // Port
	    int port = JICPProtocol.DEFAULT_PORT;
	    try {
				port = Integer.parseInt(props.getProperty(MicroRuntime.PORT_KEY));
	    }
	    catch (NumberFormatException nfe) {
				// Use default
	    }

	    // Compose URL 
	    mediatorTA = JICPProtocol.getInstance().buildAddress(host, String.valueOf(port), null, null);
	    if (myLogger.isLoggable(Logger.FINE)) {
	    	myLogger.log(Logger.FINE, "Remote URL="+JICPProtocol.getInstance().addrToString(mediatorTA));
	    }

	    // Mediator class
	    String tmp = props.getProperty(JICPProtocol.MEDIATOR_CLASS_KEY);
	    if (tmp != null) {
	    	myMediatorClass = tmp;
	    }
	    if (myLogger.isLoggable(Logger.FINE)) {
	    	myLogger.log(Logger.FINE, "Mediator class="+myMediatorClass);
	    }
	    
	    // Read (re)connection retry time
	    tmp = props.getProperty(JICPProtocol.RECONNECTION_RETRY_TIME_KEY);
	    try {
				retryTime = Long.parseLong(tmp);
	    }
	    catch (Exception e) {
				// Use default
	    }
	    if (myLogger.isLoggable(Logger.FINE)) {
	    	myLogger.log(Logger.FINE, "Reconnection time="+retryTime);
	    }

	    // Read Max disconnection time
	    tmp = props.getProperty(JICPProtocol.MAX_DISCONNECTION_TIME_KEY);
	    try {
				maxDisconnectionTime = Long.parseLong(tmp);
	    } 
	    catch (Exception e) {
				// Use default
	    }
	    if (myLogger.isLoggable(Logger.FINE)) {
	    	myLogger.log(Logger.FINE, "Max discon. time="+maxDisconnectionTime);
	    }

	    // Read Keep-alive time
	    tmp = props.getProperty(JICPProtocol.KEEP_ALIVE_TIME_KEY);
	    try {
				keepAliveTime = Long.parseLong(tmp);
	    } 
	    catch (Exception e) {
				// Use default
	    }
	    if (myLogger.isLoggable(Logger.FINE)) {
	    	myLogger.log(Logger.FINE, "Keep-alive time="+keepAliveTime);
	    }

	    // Create the BackEnd stub and the FrontEnd skeleton
	    myStub = new BackEndStub(this);
	    mySkel = new FrontEndSkel(fe);

	    outConnection = createBackEnd();

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
	    	myLogger.log(Logger.FINE, "Sending termination notification");
	  		JICPPacket pkt = new JICPPacket(JICPProtocol.COMMAND_TYPE, JICPProtocol.TERMINATED_INFO, null);
	  		try {
	  			writePacket(pkt, outConnection);
		    	myLogger.log(Logger.FINE, "Done.");
	  		}
	  		catch (Exception e) {
	  			// When the BackEnd receives the termination notification,
	  			// it just closes the connection --> we always have this
	  			// exception
		    	myLogger.log(Logger.FINE, "BackEnd closed");
	  		}
  		}
  	} 		
  }
  
  /**
     Send the CREATE_MEDIATOR command with the necessary parameter
     in order to create the BackEnd in the fixed network.
     Executed 
     - at bootstrap time by the thread that creates the FrontEndContainer. 
     - To re-attach to the platform after a fault of the BackEnd
   */
  private JICPConnection createBackEnd() throws IMTPException {
    StringBuffer sb = new StringBuffer();
    appendProp(sb, JICPProtocol.MEDIATOR_CLASS_KEY, myMediatorClass);
    appendProp(sb, JICPProtocol.MAX_DISCONNECTION_TIME_KEY, String.valueOf(maxDisconnectionTime));
    appendProp(sb, JICPProtocol.KEEP_ALIVE_TIME_KEY, String.valueOf(keepAliveTime));
    if(myMediatorID != null) {
    	// This is a request to re-create my expired back-end
		  appendProp(sb, JICPProtocol.MEDIATOR_ID_KEY, myMediatorID);
	    appendProp(sb, "outcnt", String.valueOf(outCnt));
	    appendProp(sb, "lastsid", String.valueOf(lastSid));
    }
	  appendProp(sb, FrontEnd.REMOTE_BACK_END_ADDRESSES, beAddrsText);
	  appendProp(sb, JICPProtocol.OWNER_KEY, props.getProperty(JICPProtocol.OWNER_KEY));
	  appendProp(sb, MSISDN, props.getProperty(MSISDN));
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
	    	myLogger.log(Logger.INFO, "Creating BackEnd on jicp://"+mediatorTA.getHost()+":"+mediatorTA.getPort());

	      JICPConnection con = new JICPConnection(mediatorTA);

	      writePacket(pkt, con);

	      pkt = con.readPacket();

		    String replyMsg = new String(pkt.getData());
	      if (pkt.getType() != JICPProtocol.ERROR_TYPE) {
				  // BackEnd creation successful
		      int index = replyMsg.indexOf('#');
		      myMediatorID = replyMsg.substring(0, index);
		      props.setProperty(JICPProtocol.MEDIATOR_ID_KEY, myMediatorID);
		      props.setProperty(JICPProtocol.LOCAL_HOST_KEY, replyMsg.substring(index+1));
		      // Complete the mediator address with the mediator ID
		      mediatorTA = new JICPAddress(mediatorTA.getHost(), mediatorTA.getPort(), myMediatorID, null);
		    	myLogger.log(Logger.INFO, "BackEnd OK");
				  return con;	      
	      }
	      else {
		    	myLogger.log(Logger.INFO, "Mediator error: "+replyMsg);
	      }
		  }
		  catch (IOException ioe) {
	      // Ignore it, and try the next address...
	    	myLogger.log(Logger.INFO, "Connection error. "+ioe.toString());
		  }
    }

    // No address succeeded: try to handle the problem...
    throw new IMTPException("Error creating the BackEnd.");
  }
  
  private void appendProp(StringBuffer sb, String key, String val) {
  	if (val != null) {
	  	sb.append(key);
	  	sb.append('=');
	  	sb.append(val);
	  	sb.append('#');
  	}
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
  		if (myLogger.isLoggable(Logger.FINEST)) {
  			myLogger.log(Logger.FINEST, "Issuing outgoing command "+outCnt);
  		}
	  	JICPPacket pkt = new JICPPacket(JICPProtocol.COMMAND_TYPE, JICPProtocol.DEFAULT_INFO, payload);
	  	pkt.setSessionID((byte) outCnt);
	  	try {
		  	writePacket(pkt, outConnection);
		  	status = 1;
		  	pkt = outConnection.readPacket();
		  	if (pkt.getSessionID() != outCnt) {
		  		pkt = outConnection.readPacket();
		  	}
		  	status = 2;
	  		if (myLogger.isLoggable(Logger.FINEST)) {
	  			myLogger.log(Logger.FINEST, "Response received "+pkt.getSessionID());
	  		}
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
	  		if (myLogger.isLoggable(Logger.FINE)) {
	  			myLogger.log(Logger.FINE, "IOException OC["+status+"]"+ioe);
	  		}
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
  		if (myLogger.isLoggable(Logger.CONFIG)) {
  			myLogger.log(Logger.CONFIG, "IM-"+myId+" started");
  		}
	  	
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
			  		if (myLogger.isLoggable(Logger.FINE)) {
			  			myLogger.log(Logger.FINE, "Duplicated packet received "+sid);
			  		}
  					pkt = lastResponse;
  				}
  				else {
						if (pkt.getType() == JICPProtocol.KEEP_ALIVE_TYPE) {
							// Keep-alive
						  pkt = new JICPPacket(JICPProtocol.RESPONSE_TYPE, JICPProtocol.DEFAULT_INFO, null);
						}
						else {						
							// Incoming command
				  		if (myLogger.isLoggable(Logger.FINEST)) {
				  			myLogger.log(Logger.FINEST, "Incoming command received "+sid+" pkt-type="+pkt.getType());
				  		}
							byte[] rspData = mySkel.handleCommand(pkt.getData());
				  		if (myLogger.isLoggable(Logger.FINEST)) {
				  			myLogger.log(Logger.FINEST, "Incoming command served "+ sid);
				  		}
						  pkt = new JICPPacket(JICPProtocol.RESPONSE_TYPE, JICPProtocol.DEFAULT_INFO, rspData);
						}
					  pkt.setSessionID(sid);
					  if (Thread.currentThread() == terminator) {
					  	// Attach the TERMINATED_INFO flag to the response
					  	pkt.setTerminatedInfo(true);
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
		  		if (myLogger.isLoggable(Logger.CONFIG)) {
		  			myLogger.log(Logger.CONFIG, "IOException IC["+status+"]"+ioe);
		  		}
  				refreshInp();
				}
  		}
  		
  		if (myLogger.isLoggable(Logger.CONFIG)) {
  			myLogger.log(Logger.CONFIG, "IM-"+myId+" terminated");
  		}
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
  protected synchronized void refreshInp() {
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
  protected synchronized void refreshOut() {
  	// Avoid having two refreshing processes at the same time
  	if (!refreshingOutput) {
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
  		refreshingOutput = true;
			Thread t = new Thread(this);
			t.start();
  	}
  }
	
  /**
     Asynchronously restore the OUT connection
   */
	public void run() {
		connect(OUT);
		refreshingOutput = false;
	}
	

  private void connect(byte type) {
  	int jicpErrorCnt = 0;
  	int cnt = 0;
  	long startTime = System.currentTimeMillis();
  	while (active) {
	  	try {
	  		if (myLogger.isLoggable(Logger.INFO)) {
	  			myLogger.log(Logger.INFO, "Connecting to "+mediatorTA.getHost()+":"+mediatorTA.getPort()+" "+type+"("+cnt+")");
	  		}
		  	Connection c = new JICPConnection(mediatorTA);
		  	JICPPacket pkt = new JICPPacket(JICPProtocol.CONNECT_MEDIATOR_TYPE, JICPProtocol.DEFAULT_INFO, mediatorTA.getFile(), new byte[]{type});
		  	writePacket(pkt, c);
		  	pkt = c.readPacket();
			  if (pkt.getType() == JICPProtocol.ERROR_TYPE) {
		  		c.close();
		  		// The JICPServer didn't find my Mediator anymore. There was probably 
			  	// a fault. Try to recreate the BackEnd
			  	if (type == OUT) {
			  		// BackEnd recreation is attempted only when restoring the 
			  		// OUT connection since the BackEnd uses the connection that 
			  		// creates it to receive outgoing commands. Moreover this ensures
			  		// that (if the BackEnd is created on a different host) we do not
			  		// end up with the INP and OUT connections pointing to different 
			  		// hosts.
				  	try {
				  		c = createBackEnd();
				  		handleReconnection(c, type);
				  	}
				  	catch (IMTPException imtpe) { 
				      handleError();
				  	}
			  	}
			  	else {
		  			// In case the outConnection still appears to be OK, refresh it
			  		refreshOut();
			  		// Then behave as if there was an IOException --> go to sleep for a while and try again
		  			throw new IOException();
			  	}
			  }
			  else {
				  // The local-host address may have changed
				  props.setProperty(JICPProtocol.LOCAL_HOST_KEY, new String(pkt.getData()));
		  		if (myLogger.isLoggable(Logger.INFO)) {
		  			myLogger.log(Logger.INFO, "Connect OK");
		  		}
				  handleReconnection(c, type);
			  }
			  return;
	  	}
	  	catch (IOException ioe) {
	  		if (myLogger.isLoggable(Logger.INFO)) {
	  			myLogger.log(Logger.INFO, "Connect failed "+ioe.toString());
	  		}
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

	protected synchronized void handleReconnection(Connection c, byte type) {
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
		myLogger.log(Logger.SEVERE, "Can't reconnect ("+System.currentTimeMillis()+")");

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
  
  
  protected void writePacket(JICPPacket pkt, Connection c) throws IOException {
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
  
  // Mutual exclusion with updateKeepAlive() 
  public synchronized void doTimeOut(Timer t) {
  	if (t == kaTimer) { 
  		sendKeepAlive();
  	}
  }
  
  // Mutual exclusion with dispatch()
  protected synchronized void sendKeepAlive() {
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
				myLogger.log(Logger.FINE, "KA error "+ioe.toString());
				refreshOut();
			}
		}
  }  	
}

