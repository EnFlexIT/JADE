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
import jade.mtp.TransportAddress;
import jade.imtp.leap.BackEndStub;
import jade.imtp.leap.MicroSkeleton;
import jade.imtp.leap.FrontEndSkel;
import jade.imtp.leap.Dispatcher;
import jade.imtp.leap.ICPException;
import jade.imtp.leap.ConnectionListener;
import jade.util.Logger;
import jade.util.leap.Properties;
import jade.util.leap.List;
import jade.util.leap.ArrayList;

import java.io.*;

/**
 * Class declaration
 * @author Giovanni Caire - TILAB
 */
public class FrontEndDispatcher extends EndPoint implements FEConnectionManager, Dispatcher {

  private MicroSkeleton mySkel = null;
  private BackEndStub myStub = null;

  // Variables related to the connection with the Mediator
  protected TransportAddress mediatorTA;
  private String myMediatorID;
  private boolean mediatorAlive = false;
  private long retryTime = JICPProtocol.DEFAULT_RETRY_TIME;
  private long maxDisconnectionTime = JICPProtocol.DEFAULT_MAX_DISCONNECTION_TIME;
  private long totalDisconnectionTime = 0;
  private String owner;
  private String errorMsg;

  private String beAddrsText;
  private String[] backEndAddresses;
  
  private ConnectionListener myConnectionListener;

  /**
   * Constructor declaration
   */
  public FrontEndDispatcher() {
  	super();
  }

  //////////////////////////////////////////////
  // FEConnectionManager interface implementation
  //////////////////////////////////////////////
  
  /**
   * Connect to a remote BackEnd and return a stub to communicate with it
   */
  public BackEnd getBackEnd(FrontEnd fe, Properties props) throws IMTPException {  	
  	try {

	    beAddrsText = props.getProperty(FrontEnd.REMOTE_BACK_END_ADDRESSES);
	    backEndAddresses = parseBackEndAddresses(beAddrsText);

	    // Verbosity
      /*
        // Not available with new Logger
        try {
        verbosity = Integer.parseInt(props.getProperty("jade_imtp_leap_JICP_EndPoint_verbosity"));
        }
        catch (NumberFormatException nfe) {
        // Use default (1)
        }
      */
      

	    log("Connecting to the BackEnd",Logger.INFO);
  		
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
	    log("Remote URL is "+JICPProtocol.getInstance().addrToString(mediatorTA),Logger.INFO);

	    // Read (re)connection retry time
	    String tmp = props.getProperty(JICPProtocol.RECONNECTION_RETRY_TIME_KEY);
	    try {
		retryTime = Long.parseLong(tmp);
	    }
	    catch (Exception e) {
		// Use default
	    }
	    log("Reconnection retry time is "+retryTime,Logger.INFO);

	    // Read Max disconnection time
	    tmp = props.getProperty(JICPProtocol.MAX_DISCONNECTION_TIME_KEY);
	    try {
		maxDisconnectionTime = Long.parseLong(tmp);
	    } 
	    catch (Exception e) {
		// Use default
	    }
	    log("Max disconnection time is "+maxDisconnectionTime,Logger.INFO);

	    // Create the BackEnd stub and the FrontEnd skeleton
	    myStub = new BackEndStub(this);
	    mySkel = new FrontEndSkel(fe);

	    // Read the owner if any
	    owner = props.getProperty("owner");

	    // Start the embedded Thread and wait until it connects to the BackEnd
	    start();
	    Thread.yield();
	    // In the meanwhile load the ConnectionListener if any
	    try {
	    	myConnectionListener = (ConnectionListener) Class.forName(props.getProperty("connection-listener")).newInstance();
	    }
	    catch (Exception e) {
	    	// Just ignore it
	    }
	    waitUntilConnected();
	    log("Connection OK",Logger.INFO);

	    return myStub;
  	}
  	catch (ICPException icpe) {
	    throw new IMTPException("Connection error", icpe);
  	}
  }

  /**
     Mutual exclusion with handleConnectionReady/Error()
   */
  private synchronized void waitUntilConnected() throws ICPException {
    while (!isConnected()) {
      try {
      	errorMsg = "Connection timeout expired";
        wait(respTimeout);
        if (!isConnected()) {
        	throw new ICPException(errorMsg);
        }
      } 
      catch (InterruptedException ie) {
      } 
    } 
  } 

  //////////////////////////////////////////////
  // Dispatcher interface implementation
  //////////////////////////////////////////////
  
  /**
   * Deliver a serialized command to the BackEnd 
   */
  public byte[] dispatch(byte[] payload, boolean flush) throws ICPException {
  	// FIXME: Dispatching order is not guaranteed if this method
  	// is called after the device reconnects, but before flushing has
  	// started
  	JICPPacket pkt = new JICPPacket(JICPProtocol.COMMAND_TYPE, JICPProtocol.COMPRESSED_INFO, payload);
  	pkt = deliverCommand(pkt);
    return pkt.getData();
  } 

  ////////////////////////////
  // EndPoint IMPLEMENTATION
  ////////////////////////////
  /**
   */
  protected JICPPacket handleCommand(JICPPacket cmd) throws Exception {
  	byte[] rspData = mySkel.handleCommand(cmd.getData());
    return new JICPPacket(JICPProtocol.RESPONSE_TYPE, JICPProtocol.DEFAULT_INFO, rspData);
  }
  
  /**
   */
  protected void setup() throws ICPException {
  	if (mediatorAlive) {
  		if (myConnectionListener != null) {
	  		myConnectionListener.handleConnectionEvent(ConnectionListener.DISCONNECTED);
  		}
  	}
  	
      while (true) {

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
		  connect();
		  totalDisconnectionTime = 0;
		  return;
	      }
	      catch (IOException ioe) {
		  // Ignore it, and try the next address...
	      }
	      catch(ICPException icpe) {
		  // Ignore it, and try the next address...
	      }
	  }

	  // No address succeeded: try to handle the problem...

	  if (mediatorAlive) {
	      // Can't reconnect to the Mediator. Wait for a while before trying again
	      // or PANIC if the max-disconnection timeout expired
	      if(totalDisconnectionTime < maxDisconnectionTime) {
		  log("Can't connect to the BackEnd. Wait a bit before retrying...",Logger.WARNING);
		  try {
		      Thread.sleep(retryTime);
		  }
		  catch (InterruptedException ie) {
		      log("InterruptedException while waiting for next reconnection attempt",Logger.WARNING);
		  }
		  totalDisconnectionTime += retryTime;
	      }
	      else {
		  throw new ICPException("Timeout expired");
	      }
	  }
	  else {
	      // Can't reach the JICPServer to create my Mediator. Notify and exit
	      errorMsg = "Can't connect to " + mediatorTA + ".";
	      throw new ICPException(errorMsg);
	  }
      }
  }

  protected void connect() throws IOException, ICPException {
    // Open the connection and gets the output and input streams
  	log("Opening connection to the BackEnd",Logger.INFO);
    Connection c = new JICPConnection(mediatorTA);

    log("Sending CREATE/CONNECT_MEDIATOR packet",Logger.FINEST);
    JICPPacket pkt = null;
    if (mediatorAlive) {
    	// This is a reconnection --> Send a CONNECT_MEDIATOR request
    	pkt = new JICPPacket(JICPProtocol.CONNECT_MEDIATOR_TYPE, JICPProtocol.DEFAULT_INFO, mediatorTA.getFile(), null);
    }
    else {
    	// This is the first time --> Send a CREATE_MEDIATOR request and
    	// specify the proper Mediator class to instantiate and other parameters
    	StringBuffer sb = new StringBuffer();
    	appendProp(sb, JICPProtocol.MEDIATOR_CLASS_KEY, "jade.imtp.leap.JICP.BackEndDispatcher");
    	//appendProp(sb, "verbosity", String.valueOf(verbosity));
    	appendProp(sb, JICPProtocol.MAX_DISCONNECTION_TIME_KEY, String.valueOf(maxDisconnectionTime));
	if(beAddrsText != null) {
	    appendProp(sb, FrontEnd.REMOTE_BACK_END_ADDRESSES, beAddrsText);
	}
    	if (owner != null) {
    		appendProp(sb, "owner", owner);
    	}
    	pkt = new JICPPacket(JICPProtocol.CREATE_MEDIATOR_TYPE, JICPProtocol.DEFAULT_INFO, null, sb.toString().getBytes());
    }
    c.writePacket(pkt);

    log("Packet sent. Read response",Logger.FINEST);
    // Read the response
    pkt = c.readPacket();
    log("Response read",Logger.FINEST);
    if (pkt.getType() == JICPProtocol.ERROR_TYPE) {
    	// The JICPServer refused to create the Mediator or didn't find myMediator anymore
    	byte[] data = pkt.getData();
    	errorMsg = (data != null ? new String(data) : null);
    	throw new ICPException(errorMsg);
    }
    if (!mediatorAlive) {
	// Store the mediator ID
	myMediatorID = new String(pkt.getData());

	mediatorTA = new JICPAddress(mediatorTA.getHost(), mediatorTA.getPort(), myMediatorID, null);
	mediatorAlive = true;
    }
    else {
    	if (myConnectionListener != null) {
	  		myConnectionListener.handleConnectionEvent(ConnectionListener.RECONNECTED);
    	}
    }
    setConnection(c);
  }

  private void appendProp(StringBuffer sb, String key, String val) {
      sb.append(key);
      sb.append('=');
      sb.append(val);
      sb.append('#');
  }

  private String[] parseBackEndAddresses(String addressesText) {
    ArrayList addrs = new ArrayList();

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
		    addrs.add(tmp);
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
	    addrs.add(tmp);
    	}
    }

    // Convert the list into an array of strings
    String[] result = new String[addrs.size()];
    for(int i = 0; i < result.length; i++) {
	result[i] = (String)addrs.get(i);
    }

    return result;

  }

  
  /**
     The connection is up --> Notify threads hang in 
     waitUntilConnected() and flush bufferd commands. 
     Mutual exclusion with waitUntilConnected()
   */
	protected synchronized void handleConnectionReady() {
		notifyAll();
		Thread.yield();
		if (mediatorAlive) {
			// We have just re-connected --> flush pending commands
			myStub.flush();
		}
	}
	
  /**
     Mutual exclusion with waitUntilConnected()
   */
	protected synchronized void handleConnectionError() {
		notifyAll();		
		if (mediatorAlive) {
			// We cannot re-connected 
    	if (myConnectionListener != null) {
	  		myConnectionListener.handleConnectionEvent(ConnectionListener.RECONNECTION_FAILURE);
    	}
		}
	}
}

