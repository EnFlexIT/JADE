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
import jade.util.leap.Properties;
import jade.imtp.leap.JICP.*;

/*#MIDP_INCLUDE_BEGIN
import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
#MIDP_INCLUDE_END*/

import java.io.*;


/**
 * Class declaration
 * @author Giovanni Caire - TILAB
 */
public class HTTPFEDispatcher extends Thread implements FEConnectionManager, Dispatcher {

  private MicroSkeleton mySkel;
  private BackEndStub myStub;

  private Thread terminator;
  private boolean active = false;
  private boolean connectionUp = false;
  private Connection inpConnection;
  private Connection outConnection;
  
  private TransportAddress mediatorTA;
  
  // Configuration parameters
  private long retryTime = JICPProtocol.DEFAULT_RETRY_TIME;
  private long maxDisconnectionTime = JICPProtocol.DEFAULT_MAX_DISCONNECTION_TIME;
  private String owner;
  private int verbosity = 1;

  ////////////////////////////////////////////////
  // FEConnectionManager interface implementation
  ////////////////////////////////////////////////
  
	/**
	 */
  public BackEnd getBackEnd(FrontEnd fe, Properties props) throws IMTPException {
		// Verbosity
  	try {
  		verbosity = Integer.parseInt(props.getProperty("jade_imtp_leap_http_HTTPFEDispatcher_verbosity"));
  	}
  	catch (NumberFormatException nfe) {
      // Use default
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
  	
		// Compose URL. Note that we build a JICPAddress just to avoid
  	// loading the HTTPAddress class.
  	mediatorTA = JICPProtocol.getInstance().buildAddress(host, String.valueOf(port), null, null);
 		log("Remote URL is http://"+host+":"+port, 2);
			
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
		 	
		// Read the owner if any
		owner = props.getProperty("owner");
		
		// Create the BackEnd stub and the FrontEnd skeleton
		myStub = new BackEndStub(this);
		mySkel = new FrontEndSkel(fe);

		// Create the connections for outgoing commands
		outConnection = new HTTPClientConnection(mediatorTA); 

		// Create the remote BackEnd
		createBackEnd();
		log("Connection OK", 1);

		// Start the embedded Thread
		active = true;
		connectionUp = true;
		start();
		return myStub;
  }

  /**
	 */
  public void shutdown() {
  	active = false;
  	
  	terminator = Thread.currentThread();
  	if (terminator != this) {
	  	// This is a self-initiated shut down --> we must explicitly
	  	// notify the BackEnd. 
  		JICPPacket pkt = new JICPPacket(JICPProtocol.COMMAND_TYPE, (byte) (JICPProtocol.DEFAULT_INFO), null);
  		log("Pushing termination notification", 2);
  		try {
  			deliver(pkt, outConnection);
  		}
  		catch (IOException ioe) {
  			// When the BackEnd receives the termination notification,
  			// it just closes the connection --> we always have this
  			// exception --> just explicitly close the outConnection
  			log("The BackEnd has closed the outgoing connection as expected", 2);
  			try {
	  			outConnection.close();
  			}
  			catch (IOException ioe2) {}
  		}
  	} 		
  }
  
  //////////////////////////////////////////////
  // Dispatcher interface implementation
  //////////////////////////////////////////////
  
  /**
     Dispatch a serialized command to the BackEnd.
     Mutual exclusion with reconnect() to avoid using the outConnection
     at the same time and preserve dispatching order
   */
  public synchronized byte[] dispatch(byte[] payload) throws ICPException {
	  log("Issuing outgoing command", 3); 
  	// Note that we don't even try to dispatch packets while the
  	// connection is down to preserve dispatching order
  	if (connectionUp) {
	  	try {
		  	JICPPacket pkt = new JICPPacket(JICPProtocol.COMMAND_TYPE, JICPProtocol.COMPRESSED_INFO, payload);
		  	pkt = deliver(pkt, outConnection);
	  		log("Response received", 3); 
		    return pkt.getData();
	  	}
	  	catch (Throwable t) {
	  		// FIXME: we should notify the embedded Thread.
	  		throw new ICPException("Dispatching error.", t);
	  	}
  	}
  	else {
  		throw new ICPException("Connection down");
  	}
  } 

  private JICPPacket deliver(JICPPacket pkt, Connection c) throws IOException {
  	//Connection c = new HTTPClientConnection(mediatorTA);
  	OutputStream os = c.getOutputStream();
  	if (Thread.currentThread() == terminator) {
  		pkt.setTerminatedInfo();
  	}
  	pkt.setRecipientID(mediatorTA.getFile());
  	pkt.writeTo(os);
  	log("CREATE_MEDIATOR packet sent. Reading response", 2);
  	InputStream is = c.getInputStream();
  	pkt = JICPPacket.readFrom(is);
  	//os.close();
  	//is.close();
  	//c.close();
  	return pkt;
  }
  
  /////////////////////////////////////////////
  // Embedded Thread handling incoming commands
  /////////////////////////////////////////////
  public void run() {
  	long totalDisconnectionTime = 0;
  	while (active) {
  		try {
  			// Open the connection for incoming commands
				inpConnection = new HTTPClientConnection(mediatorTA); 
  			// Prepare a dummy response
	  		JICPPacket rsp = new JICPPacket(JICPProtocol.RESPONSE_TYPE, JICPProtocol.DEFAULT_INFO, new byte[] {(byte) 0xff});
  			while (true) {
  				JICPPacket cmd = deliver(rsp, inpConnection);
  				if (!active) {
  					break;
  				}
      		log("Incoming command received", 3);
					byte[] rspData = mySkel.handleCommand(cmd.getData());
      		log("Incoming command served", 3);
				  rsp = new JICPPacket(JICPProtocol.RESPONSE_TYPE, JICPProtocol.DEFAULT_INFO, rspData);
  			}
  		}
  		catch (IOException ioe) {
  			log("Exception reading from the incoming connection", 2);
  		}
  		
			try {
  			inpConnection.close();
			}
			catch (IOException ioe2) {}
			if (active) {
				// This is a disconnection
  			log("Disconnection detected. Try to reconnect", 2);
				connectionUp = false;
				while (!reconnect() && totalDisconnectionTime < maxDisconnectionTime) {
        	log("Reconnection failed. Wait a bit before retrying...", 2);
          try {
            Thread.sleep(retryTime);
          } 
          catch (InterruptedException ie) {
            log("InterruptedException while waiting for next reconnection attempt", 1);
          } 
          totalDisconnectionTime += retryTime;
				}
				if (connectionUp) {
					// Reconnection succeeded :-) 
  				log("Reconnection OK", 2);
					totalDisconnectionTime = 0;
				}
				else {
					// It's impossible to reconnect :-( --> terminate
					log("Max disconnection timeout expired", 1);
					handleConnectionError();
					active = false;
				}
  		}
  	}
    log("HTTPFEDispatcher Thread terminated", 1);
  }			
 
  private void createBackEnd() throws IMTPException {
    log("Sending CREATE_MEDIATOR packet", 2);
  	// Send a CREATE_MEDIATOR request and
  	// specify the proper Mediator class to instantiate
  	StringBuffer sb = new StringBuffer(JICPProtocol.MEDIATOR_CLASS_KEY);
  	sb.append('=');
  	sb.append("jade.imtp.leap.http.HTTPBEDispatcher;");
  	sb.append(JICPProtocol.MAX_DISCONNECTION_TIME_KEY);
  	sb.append('=');
  	sb.append(maxDisconnectionTime);
  	sb.append(";verbosity=");
  	sb.append(verbosity);
  	if (owner != null) {
  		sb.append(";owner=");
  		sb.append(owner);
  	}
  	JICPPacket pkt = new JICPPacket(JICPProtocol.CREATE_MEDIATOR_TYPE, JICPProtocol.DEFAULT_INFO, null, sb.toString().getBytes());
		try {
	  	pkt = deliver(pkt, outConnection);
		}
		catch (IOException ioe) {
			throw new IMTPException("Error connecting to the BackEnd.", ioe);
		}

    if (pkt.getType() == JICPProtocol.ERROR_TYPE) {
    	// The JICPServer refused to create the Mediator 
    	byte[] data = pkt.getData();
    	throw new IMTPException((data != null ? new String(data) : null));
    } 
    // Complete the mediator address with the mediator ID
		mediatorTA = new JICPAddress(mediatorTA.getHost(), mediatorTA.getPort(), new String(pkt.getData()), null);
  }
  
  /**
     Mutual exclusion with dispatch() to avoid using the outConnection
     at the same time and preserve dispatching order
     FIXME: There is still a case in which the dispatching order may 
     not be preserved: 
     - This thread release the monitor on "this".
     - A normal message dispatching thread enters the dispatch() method
     - The flushing thread starts flushing
   */
  private synchronized boolean reconnect() {
    log("Sending CONNECT_MEDIATOR packet", 2);
  	JICPPacket pkt = new JICPPacket(JICPProtocol.CONNECT_MEDIATOR_TYPE, JICPProtocol.DEFAULT_INFO, null);
		try {
	  	pkt = deliver(pkt, outConnection);
		}
		catch (IOException ioe) {
    	log("Reconnection failure. "+ioe.toString(), 2);
    	return false;
		}

    if (pkt.getType() == JICPProtocol.ERROR_TYPE) {
    	// The JICPServer didn't find my Mediator. We return true,
    	// but we don't set connectionUp 
    	log("Reconnection succeeded, but my Mediator has expired.", 2);
    	return true;
    } 
    
    // Reconnection OK
    connectionUp = true;
    myStub.flush();
    return true;
  }	
  
  protected void handleConnectionError() {
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

