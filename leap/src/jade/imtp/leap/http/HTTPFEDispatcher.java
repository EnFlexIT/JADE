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
import jade.imtp.leap.JICP.*;

import jade.util.leap.Properties;
import jade.util.leap.ArrayList;

import java.io.*;


/**
   FrontEnd-side dispatcher class using JICP over HTTP as transport 
   protocol
   @author Giovanni Caire - TILAB
 */
public class HTTPFEDispatcher extends Thread implements FEConnectionManager, Dispatcher {

  private MicroSkeleton mySkel;
  private BackEndStub myStub;
  
  private Thread terminator;
  private boolean active = false;
  private DisconnectionManager myDisconnectionManager;
  private Connection inpConnection;
  private Connection outConnection;
  private int outCnt;
  private boolean waitingForFlush = false;
  private long maxDisconnectionTime;
  private Properties props;
  
  private TransportAddress mediatorTA;
  private String myMediatorID;
  
  private String owner;
  private int verbosity = 1;

  private String beAddrsText;
  private String[] backEndAddresses;


  ////////////////////////////////////////////////
  // FEConnectionManager interface implementation
  ////////////////////////////////////////////////
  
	/**
	   Create a BackEnd in the fixed network and return a stub to 
	   communicate with it
	 */
  public BackEnd getBackEnd(FrontEnd fe, Properties p) throws IMTPException {
      props = p;

      beAddrsText = props.getProperty(FrontEnd.REMOTE_BACK_END_ADDRESSES);
      backEndAddresses = parseBackEndAddresses(beAddrsText);

      // Verbosity
  	try {
	    String v = props.getProperty("jade_imtp_leap_http_HTTPFEDispatcher_verbosity");
	    if(v != null) {
  		verbosity = Integer.parseInt(v);
	    }
  	}
  	catch (NumberFormatException nfe) {
      // Use default
  	}

	log("Creating the BackEnd: ", 2);
		
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
  	
		// Compose URL. Note that we build a JICPAddress just to avoid
  	// loading the HTTPAddress class.
  	mediatorTA = JICPProtocol.getInstance().buildAddress(host, String.valueOf(port), null, null);
 		log("Remote URL is http://"+host+":"+port, 2);
			
		// Read re-connection retry time
	  long retryTime = JICPProtocol.DEFAULT_RETRY_TIME;
		String tmp = props.getProperty(JICPProtocol.RECONNECTION_RETRY_TIME_KEY);
    try {
      retryTime = Long.parseLong(tmp);
    } 
    catch (Exception e) {
      // Use default
    } 
		log("Reconnection retry time is "+retryTime, 2);
			
		// Read Max disconnection time
	  maxDisconnectionTime = JICPProtocol.DEFAULT_MAX_DISCONNECTION_TIME;
		tmp = props.getProperty(JICPProtocol.MAX_DISCONNECTION_TIME_KEY);
    try {
      maxDisconnectionTime = Long.parseLong(tmp);
    } 
    catch (Exception e) {
      // Use default
    } 
		log("Max disconnection time is "+maxDisconnectionTime, 2);

		myDisconnectionManager = new DisconnectionManager(retryTime, maxDisconnectionTime);
		
		// Read the owner if any
		owner = props.getProperty("owner");
		
		// Create the BackEnd stub and the FrontEnd skeleton
		myStub = new BackEndStub(this);
		mySkel = new FrontEndSkel(fe);

		// Create the connection for outgoing commands. The connection
		// for incoming commands is created by the embedded thread.
		outConnection = new HTTPClientConnection(mediatorTA); 

		// Start the embedded Thread
		active = true;
		start();
				
		// Create the remote BackEnd
		createBackEnd();
		log("BackEnd created successfully", 1);

		return myStub;
  }

  /**
     Make this HTTPFEDispatcher terminate.
     Note that when the BackEnd receives the termination notification
     (explicitly sent in case of a self-initiated shutdown or 
     attached to the response to the EXIT command), it closes the 
     inpConnection. The local embedded thread gets an exception and,
     since active has been set to false, terminates.
	 */
  public void shutdown() {
  	active = false;
  	
  	terminator = Thread.currentThread();
  	if (terminator != this) {
	  	// This is a self-initiated shut down --> we must explicitly
	  	// notify the BackEnd. 
  		JICPPacket pkt = new JICPPacket(JICPProtocol.COMMAND_TYPE, (byte) (JICPProtocol.DEFAULT_INFO), null);
  		// Mutual exclusion with dispatch() to avoid using the outConnection at the same time
  		synchronized (this) {
  			// If we are disconnected don't even send the termination notification
  			if (myDisconnectionManager.isReachable()) {
		  		log("Pushing termination notification", 2);
		  		try {
		  			deliver(pkt, outConnection);
		  		}
		  		catch (IOException ioe) {
		  			// When the BackEnd receives the termination notification,
		  			// it just closes the connection --> we always have this
		  			// exception --> just explicitly close the outConnection
		  			log("The BackEnd has closed the outgoing connection as expected", 2);
		  		}
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
      log("Sending CREATE_MEDIATOR packet", 2);
      StringBuffer sb = new StringBuffer();
      appendProp(sb, JICPProtocol.MEDIATOR_CLASS_KEY, "jade.imtp.leap.http.HTTPBEDispatcher");
      appendProp(sb, "verbosity", String.valueOf(verbosity));
      appendProp(sb, JICPProtocol.MAX_DISCONNECTION_TIME_KEY, String.valueOf(maxDisconnectionTime));
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
	      log("Connecting to "+mediatorTA.getHost()+":"+mediatorTA.getPort(), 1);
	      pkt = deliver(pkt, outConnection);

	      if (pkt.getType() != JICPProtocol.ERROR_TYPE) {
				  // BackEnd creation successful
		      myMediatorID = new String(pkt.getData());
		      // Complete the mediator address with the mediator ID
		      mediatorTA = new JICPAddress(mediatorTA.getHost(), mediatorTA.getPort(), myMediatorID, null);
		      myDisconnectionManager.setReachable();
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
     Dispatch a serialized command to the BackEnd and get back a
     serialized response.
     Mutual exclusion with itself and ping() to avoid using the 
     outConnection at the same time
   */
  public synchronized byte[] dispatch(byte[] payload, boolean flush) throws ICPException {
  	// Note that we don't even try to dispatch packets while the
  	// device is not reachable to preserve dispatching order.
	  // If dispatching succeeded in fact this command would overcome
	  // any postponed command waiting to be flushed.
  	if (myDisconnectionManager.isReachable()) {
  		// The following check preserves dispatching order when the 
  		// device has just reconnected but flushing has not started yet
  		if (waitingForFlush && !flush) {
				throw new ICPException("Upsetting dispatching order");
  		}
  		waitingForFlush = false;
  		
		  int sid = outCnt;
		  outCnt = (outCnt+1) & 0x0f;
	  	log("Issuing outgoing command "+sid, 3);
	  	try {
		  	JICPPacket pkt = new JICPPacket(JICPProtocol.COMMAND_TYPE, JICPProtocol.COMPRESSED_INFO, payload);
		  	pkt.setSessionID((byte) sid);
		  	pkt = deliver(pkt, outConnection);
	  		log("Response received "+pkt.getSessionID(), 3); 
		    if (pkt.getType() == JICPProtocol.ERROR_TYPE) {
		    	// Communication OK, but there was a JICP error on the peer
		      throw new ICPException(new String(pkt.getData()));
		    }
		    return pkt.getData();
	  	}
	  	catch (IOException ioe) {
	  		// Can't reach the BackEnd. Assume we are unreachable and start
	  		// a reachability checker
  			log("IOException on output connection. "+ioe, 2);
	  		myDisconnectionManager.setUnreachable();
	  		throw new ICPException("Dispatching error.", ioe);
	  	}
  	}
  	else {
  		throw new ICPException("Unreachable");
  	}
  } 
  
  /////////////////////////////////////////////
  // Embedded Thread handling incoming commands
  /////////////////////////////////////////////
  public void run() {
  	// Give precedence to the Thread that is creating the BackEnd
  	Thread.yield();
  	
  	JICPPacket lastResponse = null;
  	byte lastSid = 0x10; // Different from any valid sid
  	
		// Open the connection for incoming commands
  	TransportAddress currentTA = mediatorTA;
		inpConnection = new HTTPClientConnection(currentTA);
		
		// Prepare a dummy response
		JICPPacket rsp = new JICPPacket(JICPProtocol.RESPONSE_TYPE, JICPProtocol.DEFAULT_INFO, new byte[] {(byte) 0xff});
  	
  	while (active) {  			
  		try {
				while (true) {
					myDisconnectionManager.waitUntilReachable();
					if ((!mediatorTA.getHost().equals(currentTA.getHost())) || (!mediatorTA.getPort().equals(currentTA.getPort()))) {
						// If the MediatorTA has changed refresh the input connection
						currentTA = mediatorTA;
						inpConnection = new HTTPClientConnection(currentTA);
					}						
  				JICPPacket cmd = deliver(rsp, inpConnection);
  				byte sid = cmd.getSessionID();
  				if (sid == lastSid) {
  					log("Duplicated command received "+sid, 2);
  					rsp = lastResponse;
  				}
  				else {
	      		log("Incoming command received "+sid, 3);
						byte[] rspData = mySkel.handleCommand(cmd.getData());
	      		log("Incoming command served "+ sid, 3);
					  rsp = new JICPPacket(JICPProtocol.RESPONSE_TYPE, JICPProtocol.DEFAULT_INFO, rspData);
					  rsp.setSessionID(sid);
					  lastSid = sid;
					  lastResponse = rsp;
  				}
  			}
  		}
  		catch (IOException ioe) {
				if (active) {
  				log("IOException on input connection. "+ioe, 2);
				}
  			closeSilently(inpConnection);
				if (active) {
					myDisconnectionManager.setUnreachable();
				}
  		}
  	}
    log("HTTPFEDispatcher Thread terminated", 1);
  }

  /**
     Deliver a packet over a given connection and get back 
     a response
   */
  private JICPPacket deliver(JICPPacket pkt, Connection c) throws IOException {
  	//OutputStream os = c.getOutputStream();
  	if (Thread.currentThread() == terminator) {
  		log("Setting TERMINATED_INFO", 2);
  		pkt.setTerminatedInfo();
  	}
  	pkt.setRecipientID(mediatorTA.getFile());
  	//pkt.writeTo(os);
  	c.writePacket(pkt);
  	if (Thread.currentThread() == terminator) {
  		log("Termination packet sent", 2);
  	}
  	//InputStream is = c.getInputStream();
  	//pkt = JICPPacket.readFrom(is);
  	pkt = c.readPacket();
  	if (Thread.currentThread() == terminator) {
  		log("Reply to termination packet received", 2);
  	}
  	return pkt;
  }
  
  /**
     Close a connection catching possible exceptions
   */
  private void closeSilently(Connection c) {
  	try {
  		c.close();
  	}
  	catch (IOException ioe) {
  		log("Error closing connection. "+ioe.toString(), 2);
  	}
  }
  
  /**
     Inner class DisconnectionManager.
	   Manages issues related to disconnection of the device.
	 */
  class DisconnectionManager implements Runnable {
  	private boolean reachable = false;
  	private boolean pingOK = false;
  	private Thread myThread;
  	private long retryTime;
  	private long maxDisconnectionTime;
  	
  	private DisconnectionManager(long retryTime, long maxDisconnectionTime) {
  		this.retryTime = retryTime;
  		this.maxDisconnectionTime = maxDisconnectionTime;
  	}
  		
  	private synchronized final boolean isReachable() {
  		return reachable;
  	}
  	
  	/**
  	   Set the reachability state as "unreachable" and starts
  	   a separate thread that periodically ping the back-end to 
  	   detect when the device is reachable again
  	 */
  	private synchronized void setUnreachable() {
  		if (reachable && !pingOK) {
  			log("Starting DisconnectionManager thread. ", 2);
  			reachable = false;
  			myThread = new Thread(this);
  			myThread.start();
  		}
  	}
  	
  	/**
  	   Set the reachability state as "reachable" and notify
  	   the embedded thread
  	 */
  	private synchronized void setReachable() {
  		reachable = true;
  		notifyAll();
  	}
  	
  	/**
  	   Wait until the device is reachable again. This is
  	   executed by the HTTPFEDispatcher embedded thread before 
  	   re-opening the input connection.
  	 */
  	private synchronized void waitUntilReachable() {
  		while (!reachable) {
  			try {
  				wait();
  			}
  			catch (InterruptedException ie) {
  			}
  		}
  		pingOK = false;
  	}
  	
		/**
		   Periodically ping (that is send a CONNECT_MEDIATOR packet) the
		   BackEnd to detect when the device is reachable again.
		   When the BackEnd receives a CONNECT_MEDIATOR packet it
		   resets the input connection --> If blocked waiting for incoming 
		   commands, the HTTPFEDispatcher embedded thread should get an 
		   IOException.
		 */
  	public void run() {
  		int attemptCnt = 0;
		  long startTime = System.currentTimeMillis();
  		try {	
				while (true) {
				  waitABit(retryTime);
				  if (ping(attemptCnt)) {
				  	break;
				  }
			    attemptCnt++;
					if ((System.currentTimeMillis() - startTime) > maxDisconnectionTime) {
						throw new ICPException("Max disconnection timeout expired");
					}
				}
				
				// Ping succeeded
				log("Ping successful.", 2);
	  		synchronized (this) {
	  			pingOK = true;
	  			// Notify the thread handling incoming commands in case
	  			// it is waiting in waitUntilReachable()
	  			setReachable();
	  			// Activate postponed commands flushing
	  			waitingForFlush = myStub.flush();
	  		}
  		}
  		catch (ICPException icpe) {
  			// Impossible to reconnect to the BackEnd
				log("Impossible to reconnect to the BackEnd. "+icpe.getMessage(), 0);
  			handleConnectionError();
  		}
  	}

  	private void waitABit(long time) {
      try {
				log("Wait a bit ("+time+")...", 2);
        Thread.sleep(time);
				log("Wake up", 2);
      } 
      catch (InterruptedException ie) {
        log("InterruptedException in Thread.sleep()", 1);
      }
  	}  	
  }  // END of Inner class DisconnectionManager
  
  
  /**
     Mutual exclusion with dispatch() to avoid using the 
     outConnection at the same time
   */
  private synchronized boolean ping(int cnt) throws ICPException {

	  // Try first with the current transport address, then with the various backup addresses
	  for(int i = -1; i < backEndAddresses.length; i++) {

	      if(i >= 0) {
		  // Set the mediator address to a new address..
		  String addr = backEndAddresses[i];
		  int colonPos = addr.indexOf(':');
		  String host = addr.substring(0, colonPos);
		  String port = addr.substring(colonPos + 1, addr.length());
		  mediatorTA = new JICPAddress(host, port, myMediatorID, "");
		  outConnection = new HTTPClientConnection(mediatorTA);
	      }

	      try {

		  log("Ping the BackEnd "+cnt, 2);
		  JICPPacket pkt = new JICPPacket(JICPProtocol.CONNECT_MEDIATOR_TYPE, JICPProtocol.DEFAULT_INFO, null);
		  pkt = deliver(pkt, outConnection);
		  if (pkt.getType() == JICPProtocol.ERROR_TYPE) {
		      // The JICPServer didn't find my Mediator.  
		      throw new ICPException("Mediator expired.");
		  }
		  return true;
	      }
	      catch (IOException ioe) {
		  // Ignore it, and try the next address...
	  	log(ioe.toString(), 2);
	      }
	      catch(ICPException icpe) {
		  // Ignore it, and try the next address...
	      }
	  }

	  // No address succeeded: try to handle the problem...
	  log("Ping failed.", 2);
	  return false;
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
     Executed by the DisconnectionManager thread as soon as it detects it is 
     impossible to contact the BackEnd again.
     Users can define a proper Runnable object that acts as a 
     disconnection handler and is invoked when an unrecoverable disconnection
     occurs.
   */
  protected void handleConnectionError() {
		try {
			String discHandler = props.getProperty("disconnection-handler");
			Runnable r = (Runnable) Class.forName(discHandler).newInstance();
			r.run();
		}
		catch (Exception e) { 
			// Just ignore it
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

