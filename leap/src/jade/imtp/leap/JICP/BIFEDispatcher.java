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
import jade.util.leap.Properties;
import jade.util.leap.List;
import jade.util.leap.ArrayList;

import java.io.*;

/**
 * Class declaration
 * @author Giovanni Caire - TILAB
 */
public class BIFEDispatcher extends Thread implements FEConnectionManager, Dispatcher {
	private static final String INP = "inp";
	private static final String OUT = "out";
	
  private MicroSkeleton mySkel = null;
  private BackEndStub myStub = null;

  // Variables related to the connection with the Mediator
  protected TransportAddress mediatorTA;
  private String myMediatorID;
  private long retryTime = JICPProtocol.DEFAULT_RETRY_TIME;
  private long maxDisconnectionTime = JICPProtocol.DEFAULT_MAX_DISCONNECTION_TIME;
  private boolean waitingForFlush = false;
  private String owner;
  private Properties props;

  private String beAddrsText;
  private String[] backEndAddresses;
  
  private Connection outConnection, inpConnection;
  private ConnectionListener myConnectionListener;
  
  private int outCnt = 0;
  private boolean active = true;
  private Thread terminator;
  
  private int verbosity = 1;

  /**
   * Constructor declaration
   */
  public BIFEDispatcher() {
  	super();
  }

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
	    log("Remote URL is "+JICPProtocol.getInstance().addrToString(mediatorTA), 2);

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

	    createBackEnd();
	    log("Connection OK", 1);

	    // Start the embedded thread that deals with incoming commands
	    start();
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
  	if (terminator != this) {
	  	// This is a self-initiated shut down --> we must explicitly
	  	// notify the BackEnd.
  		if (outConnection != null) {
		  	log("Sending termination notification", 2);
	  		JICPPacket pkt = new JICPPacket(JICPProtocol.COMMAND_TYPE, JICPProtocol.TERMINATED_INFO, null);
	  		try {
	  			outConnection.writePacket(pkt);
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
	      outConnection = new JICPConnection(mediatorTA);
	      outConnection.writePacket(pkt);
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
		  int sid = outCnt;
		  outCnt = (outCnt+1) & 0x0f;
	  	log("Issuing outgoing command "+sid, 3);
	  	JICPPacket pkt = new JICPPacket(JICPProtocol.COMMAND_TYPE, JICPProtocol.DEFAULT_INFO, payload);
	  	pkt.setSessionID((byte) sid);
	  	try {
		  	outConnection.writePacket(pkt);
		  	status = 1;
		  	pkt = outConnection.readPacket();
		  	status = 2;
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
  			log("IOException OC["+status+"]"+ioe, 2);
  			handleDisconnection(outConnection);
  			Thread t = new Thread() {
  				public void run() {
  					synchronized (BIFEDispatcher.this) {
  						try {
				  			outConnection = connect(OUT);
				  			// Activate postponed commands flushing
				  			waitingForFlush = myStub.flush();
				  			handleReconnection(outConnection);
  						}
			  			catch (ICPException icpe) {
			  				handleError();
			  			}
  					}
  				}
  			};
  			t.start();
	  		throw new ICPException("Dispatching error.", ioe);
	  	}
  	}
  	else {
  		throw new ICPException("Unreachable");
  	}
  } 

  //////////////////////////////////////////////////
  // The embedded Thread handling incoming commands
  //////////////////////////////////////////////////
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
  	int status = 0;
		try {
			inpConnection = connect(INP);
		}
		catch (ICPException icpe) {
			handleError();
		}
  	
  	while (active) {
  		try {
				while (true) {
					status = 0;
					JICPPacket pkt = inpConnection.readPacket();
					status = 1;
  				byte sid = pkt.getSessionID();
  				if (sid == lastSid) {
  					log("Duplicated command received "+sid, 2);
  					pkt = lastResponse;
  				}
  				else {
	      		log("Incoming command received "+sid, 3);
						byte[] rspData = mySkel.handleCommand(pkt.getData());
	      		log("Incoming command served "+ sid, 3);
					  pkt = new JICPPacket(JICPProtocol.RESPONSE_TYPE, JICPProtocol.DEFAULT_INFO, rspData);
					  pkt.setSessionID(sid);
					  if (Thread.currentThread() == terminator) {
					  	// Attach the TERMINATED_INFO flag to the response
					  	pkt.setTerminatedInfo();
					  }
					  lastSid = sid;
					  lastResponse = pkt;
  				}
  				status = 2;
  				inpConnection.writePacket(pkt);
  				status = 3;
  			}
  		}
  		catch (IOException ioe) {
				if (active) {
  				log("IOException IC["+status+"]"+ioe, 2);
  				handleDisconnection(inpConnection);
  				try {
	  				inpConnection = connect(INP);
  					handleReconnection(inpConnection);
  				}
  				catch (ICPException icpe) {
  					handleError();
  				}
				}
  		}
  	}
    log("BIFEDispatcher Thread terminated", 1);
  }

	private synchronized void handleDisconnection(Connection c) {
		boolean transition = false;
		if (c == inpConnection) {
			inpConnection = null;
			if (outConnection != null) {
				transition = true;
			}
		}
		else if (c == outConnection) {
			outConnection = null;
			if (inpConnection != null) {
				transition = true;
			}
		}
		try {
			c.close();
		}
		catch (IOException ioe) {}
		if (transition && myConnectionListener != null) {
			myConnectionListener.handleDisconnection();
		}
	}
	
	private synchronized void handleReconnection(Connection c) {
		if (c == null) {
			return;
		}
		boolean transition = false;
		if (c == inpConnection) {
			if (outConnection != null) {
				transition = true;
			}
		}
		else if (c == outConnection) {
			if (inpConnection != null) {
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
	}
  

  private Connection connect(String type) throws ICPException {
  	int cnt = 0;
  	long startTime = System.currentTimeMillis();
  	while (active) {
	  	try {
		  	log("Connect to "+mediatorTA.getHost()+":"+mediatorTA.getPort()+" "+type+"("+cnt+")", 2);
		  	Connection c = new JICPConnection(mediatorTA);
		  	JICPPacket pkt = new JICPPacket(JICPProtocol.CONNECT_MEDIATOR_TYPE, JICPProtocol.DEFAULT_INFO, mediatorTA.getFile(), type.getBytes());
		  	c.writePacket(pkt);
		  	pkt = c.readPacket();
			  if (pkt.getType() == JICPProtocol.ERROR_TYPE) {
		      // The JICPServer didn't find my Mediator.  
		      throw new ICPException("Mediator expired.");
			  }
			  log("Connect OK",2);
			  return c;
	  	}
	  	catch (IOException ioe) {
	  		log("Connect failed "+ioe.toString(), 2);
	  		cnt++;
	  		if ((System.currentTimeMillis() - startTime) > maxDisconnectionTime) {
	  			throw new ICPException("Timeout");
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
  	return null;
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
   */
  void log(String s, int level) {
    if (verbosity >= level) {
      String name = Thread.currentThread().toString();
      jade.util.Logger.println(name+": "+s);
    } 
  } 
}

