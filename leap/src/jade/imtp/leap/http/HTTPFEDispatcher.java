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
import jade.imtp.leap.JICP.*;

import jade.util.leap.Properties;

import java.util.Vector;
import java.io.*;

//#MIDP_EXCLUDE_BEGIN
import java.net.*;
//#MIDP_EXCLUDE_END
/*#MIDP_INCLUDE_BEGIN
import javax.microedition.io.*;
#MIDP_INCLUDE_END*/

/**
   FrontEnd-side dispatcher class using JICP over HTTP as transport 
   protocol
   @author Giovanni Caire - TILAB
 */
public class HTTPFEDispatcher extends Thread implements FEConnectionManager, Dispatcher, TimerListener {

  private MicroSkeleton mySkel;
  private BackEndStub myStub;
  
  private Thread terminator;
  private boolean active = false;
  private DisconnectionManager myDisconnectionManager;
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

  private ConnectionListener myConnectionListener;

  private Object connectorLock = new Object();
  private boolean locked = false;
  
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
     input connection. The local embedded thread gets an exception and,
     since active has been set to false, terminates.
	 */
  public void shutdown() {
  	active = false;
  	
  	terminator = Thread.currentThread();
  	if (terminator != this) {
	  	// This is a self-initiated shut down --> we must explicitly
	  	// notify the BackEnd. 
  		JICPPacket pkt = new JICPPacket(JICPProtocol.COMMAND_TYPE, (byte) (JICPProtocol.DEFAULT_INFO), null);
  		// Avoid sending the termination notification while dispatching a command
  		synchronized (this) {
  			// If we are disconnected don't even send the termination notification
  			if (myDisconnectionManager.isReachable()) {
		  		log("Pushing termination notification", 2);
		  		try {
		  			deliver(pkt);
		  		}
		  		catch (IOException ioe) {
		  			// When the BackEnd receives the termination notification,
		  			// it just closes the connection --> we always have this
		  			// exception
		  			log("BackEnd closed", 2);
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
	      pkt = deliver(pkt);

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
     Mutual exclusion with itself to preserve dispatching order 
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
		  	JICPPacket pkt = new JICPPacket(JICPProtocol.COMMAND_TYPE, JICPProtocol.DEFAULT_INFO, payload);
		  	pkt.setSessionID((byte) sid);
		  	pkt = deliver(pkt);
	  		log("Response received "+pkt.getSessionID(), 3); 
		    if (pkt.getType() == JICPProtocol.ERROR_TYPE) {
		    	// Communication OK, but there was a JICP error on the peer
		      throw new ICPException(new String(pkt.getData()));
		    }
		    if ((pkt.getInfo() & JICPProtocol.REFRESH_INFO) != 0) {
  				log("Refresh request from BackEnd.", 2);
		    	myDisconnectionManager.setUnreachable();
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
  	
    // In the meanwhile load the ConnectionListener if any
    try {
    	myConnectionListener = (ConnectionListener) Class.forName(props.getProperty("connection-listener")).newInstance();
    }
    catch (Exception e) {
    	// Just ignore it
    }
    
  	JICPPacket lastResponse = null;
  	byte lastSid = 0x10;
  	
		// Prepare a dummy response
		JICPPacket rsp = new JICPPacket(JICPProtocol.RESPONSE_TYPE, JICPProtocol.DEFAULT_INFO, null);
  	
  	while (active) {  			
  		try {
				while (true) {
					myDisconnectionManager.waitUntilReachable();
  				JICPPacket cmd = deliver(rsp);
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
					myDisconnectionManager.setUnreachable();
				}
  		}
  	}
    log("HTTPFEDispatcher Thread terminated", 1);
  }

  /**
     Deliver a packet to the BackEnd TransportAddress and get back 
     a response
   */
  private JICPPacket deliver(JICPPacket pkt) throws IOException {
  	if (Thread.currentThread() == terminator) {
  		log("Setting TERMINATED_INFO", 2);
  		pkt.setTerminatedInfo();
  	}
  	pkt.setRecipientID(mediatorTA.getFile());
  	
		//#MIDP_EXCLUDE_BEGIN
	  HttpURLConnection hc = null;
		//#MIDP_EXCLUDE_END
		/*#MIDP_INCLUDE_BEGIN
	  HttpConnection hc = null;
		#MIDP_INCLUDE_END*/
  	OutputStream os = null;
  	InputStream is = null;
  	int status = 0;
  	try {
  		String url = "http://"+mediatorTA.getHost()+":"+mediatorTA.getPort()+"/jade";
	  	//#MIDP_EXCLUDE_BEGIN
			hc = (HttpURLConnection) (new URL(url)).openConnection();
			hc.setDoOutput(true);
			hc.setRequestMethod("POST");
			hc.connect();
			os = hc.getOutputStream();
			status = 1;
	    pkt.writeTo(os);
	    status = 2;
			is = hc.getInputStream();
	  	//#MIDP_EXCLUDE_END
			/*#MIDP_INCLUDE_BEGIN
			hc = (HttpConnection) Connector.open(url, Connector.READ_WRITE, true);
			hc.setRequestMethod(HttpConnection.POST);
	    os = hc.openOutputStream();
			status = 1;			
	    pkt.writeTo(os);
	    status = 2;
  		
	    lock();
	    if (pkt.getType() == JICPProtocol.RESPONSE_TYPE) {
	    	TimerDispatcher.getTimerDispatcher().add(new Timer(System.currentTimeMillis()+5000, this));
				is = hc.openInputStream();
  		}
  		else {
				is = hc.openInputStream();
				unlock();
  		}
			#MIDP_INCLUDE_END*/
  		
	    status = 3;
		  pkt = JICPPacket.readFrom(is);
		  status = 4;
	  	return pkt;
  	}
  	catch (IOException ioe) {
  		// Retrow the exception adding the status
  		throw new IOException(ioe.getMessage()+'['+status+']');
  	}
  	finally {
  		try {if (is != null) is.close();} catch(Exception e) {}
  		try {if (os != null) os.close();} catch(Exception e) {}
  		try {
  			if (hc != null) {
		    	//#MIDP_EXCLUDE_BEGIN
		    	hc.disconnect();
		    	//#MIDP_EXCLUDE_END
    			/*#MIDP_INCLUDE_BEGIN
  				hc.close();
    			#MIDP_INCLUDE_END*/
  			}
  		}
  		catch (Exception e) {}
  	}
  }
  
  public void doTimeOut(Timer t) {
  	unlock();
  }
  
  private void lock() {
  	synchronized (connectorLock) {
  		while (locked) {
  			try {
  				connectorLock.wait();
  			}
  			catch (Exception e) {}
  		}
  		locked = true;
  	}
  }
  
  private void unlock() {
  	synchronized (connectorLock) {
  		locked = false;
  		connectorLock.notifyAll();
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
  			if (myConnectionListener != null) {
  				myConnectionListener.handleDisconnection();
  			}
  			log("Starting DM ("+System.currentTimeMillis()+").", 2);
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
				  if (ping(attemptCnt)) {
				  	break;
				  }
			    attemptCnt++;
					if ((System.currentTimeMillis() - startTime) > maxDisconnectionTime) {
						throw new ICPException("Max disconnection timeout expired");
					}
					else {
					  waitABit(retryTime);
					}
				}
				
				// Ping succeeded
				log("Ping OK.", 2);
	  		synchronized (this) {
	  			if (myConnectionListener != null) {
	  				myConnectionListener.handleReconnection();
	  			}
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
				log("Impossible to reconnect to the BackEnd ("+System.currentTimeMillis()+"). "+icpe.getMessage(), 0);
				if (myConnectionListener != null) {
					myConnectionListener.handleReconnectionFailure();
				}
  		}
  	}

  	private void waitABit(long time) {
      try {
				log("Wait a bit ("+time+")...", 3);
        Thread.sleep(time);
				log("Wake up", 3);
      } 
      catch (InterruptedException ie) {
      }
  	}  	
  }  // END of Inner class DisconnectionManager
  
  
  /**
     Send a CONNECT_MEDIATOR packet to the BackEnd to check if it is reachable
   */
  private boolean ping(int cnt) throws ICPException {

	  // Try first with the current transport address, then with the various backup addresses
	  for(int i = -1; i < backEndAddresses.length; i++) {

	      if(i >= 0) {
		  // Set the mediator address to a new address..
		  String addr = backEndAddresses[i];
		  int colonPos = addr.indexOf(':');
		  String host = addr.substring(0, colonPos);
		  String port = addr.substring(colonPos + 1, addr.length());
		  mediatorTA = new JICPAddress(host, port, myMediatorID, "");
		  ///outConnection = new HTTPClientConnection(mediatorTA);
	      }

	      try {

		  log("Ping "+mediatorTA.getHost()+":"+mediatorTA.getPort()+"("+cnt+")", 2);
		  JICPPacket pkt = new JICPPacket(JICPProtocol.CONNECT_MEDIATOR_TYPE, JICPProtocol.DEFAULT_INFO, null);
		  pkt = deliver(pkt);
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
	  }

	  // No address succeeded.
	  log("Ping failed.", 2);
	  return false;
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

  /**
   */
  void log(String s, int level) {
    if (verbosity >= level) {
      String name = Thread.currentThread().toString();
      jade.util.Logger.println(name+": "+s);
    } 
  } 
}

