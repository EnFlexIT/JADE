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
import jade.imtp.leap.ICP;
import jade.imtp.leap.ICPException;
import jade.util.leap.Properties;
/*#MIDP_INCLUDE_BEGIN
import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
#MIDP_INCLUDE_END*/

import java.io.*;

/**
 * Class declaration
 * @author Giovanni Caire - TILAB
 */
public class FrontEndDispatcher extends EndPoint implements FEConnectionManager, Dispatcher {

  //private Thread            terminator = null;

  private MicroSkeleton mySkel = null;
  private BackEndStub myStub = null;

  // Variables related to the connection with the Mediator
  private TransportAddress mediatorServerTA;
  private String           mediatorId = null;
  private boolean          mediatorAlive = false;
  private long             retryTime = JICPProtocol.DEFAULT_RETRY_TIME;
  private long             maxDisconnectionTime = JICPProtocol.DEFAULT_MAX_DISCONNECTION_TIME;
  private long             totalDisconnectionTime = 0;
  private String           errorMsg;
  
  private String owner;

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
  		// Verbosity
	  	try {
	  		verbosity = Integer.parseInt(props.getProperty("jade_imtp_leap_JICP_EndPoint_verbosity"));
	  	}
	  	catch (NumberFormatException nfe) {
	      // Use default (1)
	  	}
	  	
  		log("Connecting to the BackEnd", 2);
  		
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
	  	mediatorServerTA = JICPProtocol.getInstance().buildAddress(host, String.valueOf(port), null, null);
	 		log("Remote URL is "+JICPProtocol.getInstance().addrToString(mediatorServerTA), 2);
				
			// Read (re)connection retry time
			String tmp = props.getProperty(JICPProtocol.RECONNECTION_RETRY_TIME_KEY);
	    try {
	      retryTime = Long.parseLong(tmp);
	    } 
	    catch (Exception e) {
	      // Use default
	    } 
			log("Reconnection retry time is "+retryTime, 2);
				
			// Read Max disconnection time
			tmp = props.getProperty(JICPProtocol.MAX_DISCONNECTION_TIME_KEY);
	    try {
	      maxDisconnectionTime = Long.parseLong(tmp);
	    } 
	    catch (Exception e) {
	      // Use default
	    } 
			log("Max disconnection time is "+maxDisconnectionTime, 2);
			 	
			// Create the BackEnd stub and the FrontEnd skeleton
			myStub = new BackEndStub(this);
			mySkel = new FrontEndSkel(fe);
			
			// Read the owner if any
			owner = props.getProperty("owner");
			
	    // Start the embedded Thread and wait until it connects to the BackEnd
	    start();
	    waitUntilConnected();
			log("Connection OK", 2);
	
			return myStub;
  	}
  	catch (ICPException icpe) {
  		throw new IMTPException("Connection error", icpe);
  	}
  } 

  /**
   * Shut down this FrontEndDispatcher.
   * This is called when the local FrontEnd container is exiting.
   *
  public void shutdown(boolean self) {
    terminator = Thread.currentThread();
    // If the termination is "self-initiated" the underlying EndPoint
    // must notify its peer
  	super.shutdown(self);
    log("Shutdown initiated. Terminator thread is "+terminator, 2);
  } */

  //////////////////////////////////////////////
  // Dispatcher interface implementation
  //////////////////////////////////////////////
  
  /**
   * Deliver a serialized command to the BackEnd 
   */
  public byte[] dispatch(byte[] payload) throws ICPException {
  	JICPPacket pkt = new JICPPacket(JICPProtocol.COMMAND_TYPE, JICPProtocol.COMPRESSED_INFO, payload);
  	pkt = deliverCommand(pkt);
    return pkt.getData();
  } 

  /**
     Mutual exclusion with handleConnectionReady/Error()
   */
  private synchronized void waitUntilConnected() throws ICPException {
    while (!isConnected()) {
      try {
        wait();
        if (!isConnected()) {
        	throw new ICPException(errorMsg);
        }
      } 
      catch (InterruptedException ie) {
      } 
    } 
  } 

  ////////////////////////////
  // EndPoint IMPLEMENTATION
  ////////////////////////////
  /**
   */
  protected JICPPacket handleCommand(JICPPacket cmd) throws Exception {
  	byte[] rspData = mySkel.handleCommand(cmd.getData());
    // If this is the Thread that is shutting down this FrontEndDispatcher
    // (i.e. the Thread that has previously called the shutdown() method)
    // --> notify the Mediator
    /*if (Thread.currentThread().equals(terminator)) {
      log("Activate Mediator shutdown (after the current command has been served)");
    	return new JICPPacket(JICPProtocol.RESPONSE_TYPE, (byte) (JICPProtocol.UNCOMPRESSED_INFO | JICPProtocol.TERMINATED_INFO), rspData);
    }*/
    return new JICPPacket(JICPProtocol.RESPONSE_TYPE, JICPProtocol.UNCOMPRESSED_INFO, rspData);
  }
  
  /**
   */
  protected void setup() throws ICPException {
    while (true) {
      try {
        connect();
        totalDisconnectionTime = 0;
        return;
      } 
      catch (IOException ioe) {
      	if (mediatorAlive) {
      		// Can't reconnect to the Mediator. Wait for a while before trying again
      		// or PANIC if the max-disconnection timeout expired
	        if (totalDisconnectionTime < maxDisconnectionTime) {
	        	log("Can't connect to the BackEnd. Wait a bit before retrying...", 2);
	          try {
	            Thread.sleep(retryTime);
	          } 
	          catch (InterruptedException ie) {
	            log("InterruptedException while waiting for next reconnection attempt", 1);
	          } 
	          totalDisconnectionTime += retryTime;
	        }
	        else {
	        	throw new ICPException("Timeout expired");
	        }
      	}
      	else {
      		// Can't reach the JICPServer to create my Mediator. Notify and exit
      		errorMsg = ioe.toString();
	        throw new ICPException("Can't contact remote host");
      	}
      }
    }
  }  

  private void connect() throws IOException, ICPException {
    // Open the connection and gets the output and input streams
  	log("Opening connection to the BackEnd", 2);
    Connection c = new Connection(mediatorServerTA);
    DataOutputStream out = new DataOutputStream(c.getOutputStream());
    DataInputStream inp = new DataInputStream(c.getInputStream());

    log("Sending CREATE/CONNECT_MEDIATOR packet", 2);
    JICPPacket pkt = null;
    if (mediatorAlive) {
    	// This is a reconnection --> Send a CONNECT_MEDIATOR request
    	pkt = new JICPPacket(JICPProtocol.CONNECT_MEDIATOR_TYPE, JICPProtocol.UNCOMPRESSED_INFO, mediatorId, null);
    }
    else {
    	// This is the first time --> Send a CREATE_MEDIATOR request and
    	// specify the proper Mediator class to instantiate
    	StringBuffer sb = new StringBuffer(JICPProtocol.MEDIATOR_CLASS_KEY);
    	sb.append('=');
    	sb.append("jade.imtp.leap.JICP.BackEndDispatcher;");
    	sb.append(JICPProtocol.MAX_DISCONNECTION_TIME_KEY);
    	sb.append('=');
    	sb.append(maxDisconnectionTime);
    	sb.append(";verbosity=");
    	sb.append(verbosity);
    	if (owner != null) {
    		sb.append(";owner=");
    		sb.append(owner);
    	}
    	pkt = new JICPPacket(JICPProtocol.CREATE_MEDIATOR_TYPE, JICPProtocol.UNCOMPRESSED_INFO, null, sb.toString().getBytes());
    }    	
    pkt.writeTo(out);

    // Read the response
    pkt = JICPPacket.readFrom(inp);
    if (pkt.getType() == JICPProtocol.ERROR_TYPE) {
    	// The JICPServer refused to create the Mediator or didn't find myMediator anymore
    	byte[] data = pkt.getData();
    	errorMsg = (data != null ? new String(data) : null);
    	throw new ICPException(errorMsg);
    } 
		if (!mediatorAlive) {
			mediatorId = new String(pkt.getData());
			mediatorAlive = true;
		}
		setConnection(c);
  } 
  
  /**
     The connection is up --> Notify threads hang in 
     waitUntilConnected() and flush bufferd commands. 
     Mutual exclusion with waitUntilConnected()
   */
	protected synchronized void handleConnectionReady() {
		notifyAll();
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
			/*#MIDP_INCLUDE_BEGIN
			// We cannot re-connected --> Notify the user (MIDP specific) 
			Display d = Display.getDisplay(jade.core.Agent.midlet);
			d.setCurrent(new Form("Cannot reconnect. Try again later"));
			#MIDP_INCLUDE_END*/
		}
	}
}

