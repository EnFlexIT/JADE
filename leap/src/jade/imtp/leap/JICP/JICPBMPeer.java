/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

/**
 * ************************************************************************
 * The LEAP libraries, when combined with certain JADE platform components,
 * provide a run-time environment for enabling FIPA agents to execute on
 * lightweight devices running Java. LEAP and JADE teams have jointly
 * designed the API for ease of integration and hence to take advantage
 * of these dual developments and extensions so that users only see
 * one development platform and a
 * single homogeneous set of APIs. Enabling deployment to a wide range of
 * devices whilst still having access to the full development
 * environment and functionalities that JADE provides.
 * Copyright (C) 2001 Telecom Italia LAB S.p.A.
 * Copyright (C) 2001 Motorola.
 * 
 * GNU Lesser General Public License
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation,
 * version 2.1 of the License.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 * ************************************************************************
 */
package jade.imtp.leap.JICP;

import jade.core.Runtime;
import jade.core.Profile;
import jade.core.ProfileException;
import jade.imtp.leap.ICP;
import jade.imtp.leap.ICPException;
import jade.imtp.leap.TransportProtocol;
import jade.mtp.TransportAddress;
import jade.util.leap.*;
import java.io.*;

/**
 * Class declaration
 * @author Nicolas Lhuillier - Motorola
 * @author Giovanni Caire - TILAB
 * @author Jerome Picault - Motorola
 */
public class JICPBMPeer extends EndPoint implements ICP {

  //private Thread            terminator = null;

  private ICP.Listener      cmdListener;

  // Variables related to the connection with the Mediator
  private TransportAddress mediatorServerTA;
  private String           mediatorId = null;
  private boolean          mediatorAlive = false;
  private long             retryTime = JICPProtocol.DEFAULT_RETRY_TIME;
  private long             maxDisconnectionTime = JICPProtocol.DEFAULT_MAX_DISCONNECTION_TIME;
  private long             totalDisconnectionTime = 0;
  private String           errorMsg;

  /**
   * Constructor declaration
   */
  public JICPBMPeer() {
  	super();
  }

  /////////////////////////////
  // ICP INTERFACE
  /////////////////////////////
  
  /**
     Return the protocol object describing the protocol used by this Peer
   */
  public TransportProtocol getProtocol() {
    return JICPProtocol.getInstance();
  } 

  /**
   * Prepare to receive commands by activating a Mediator
   */
  public TransportAddress activate(ICP.Listener l, String peerID, Profile p) throws ICPException {  	
		cmdListener = l;
  	
  	log("Activating JICPBMPeer", 2);
  	StringBuffer sb = null;
		int idLength;
		if (peerID != null) {
  		sb = new StringBuffer(peerID);
			sb.append('-');
			idLength = sb.length();
		}
		else {
			sb = new StringBuffer();
			idLength = 0;
		}
			
		// Read Mediator server URL 
		sb.append(JICPProtocol.REMOTE_URL_KEY);
		String remoteURL = p.getParameter(sb.toString(), null);
  	mediatorServerTA = JICPProtocol.getInstance().stringToAddr(remoteURL);
		log("Remote URL for Mediator is "+remoteURL, 2);
			
		// Read (re)connection retry time
		sb.setLength(idLength);
		sb.append(JICPProtocol.RECONNECTION_RETRY_TIME_KEY);
		String tmp = p.getParameter(sb.toString(), null);
    try {
      retryTime = Long.parseLong(tmp);
    } 
    catch (Exception e) {
      // Use default
    } 
		log("Reconnection retry time is "+retryTime, 2);
			
		// Read Max disconnection time
		sb.setLength(idLength);
		sb.append(JICPProtocol.MAX_DISCONNECTION_TIME_KEY);
		tmp = p.getParameter(sb.toString(), null);
    try {
      maxDisconnectionTime = Long.parseLong(tmp);
    } 
    catch (Exception e) {
      // Use default
    } 
		log("Max disconnection time is "+maxDisconnectionTime, 2);
    sb = null;
			
    // Start the embedded Thread
    //registerListener(this);
    start();

    // Wait for the embedded Thread to create/connect to the Mediator
    waitUntilConnected();
		log("Peer activation OK ", 1);
		
    return new JICPAddress(mediatorServerTA.getHost(), mediatorServerTA.getPort(), mediatorId, null);
  } 

  /**
   * Shut down this JICP peer.
   * This is called when the local container is exiting.
   */
  public void deactivate() throws ICPException {
    //terminator = Thread.currentThread();
  	shutdown();
    //log("Shutdown initiated. Terminator thread is "+terminator);
  } 

  /**
   * Deliver a serialized command to a given transport address through 
   * the Mediator
   */
  public byte[] deliverCommand(TransportAddress ta, byte[] payload) throws ICPException {
  	// Note that we don't care about handling a PING command 
  	// differently as a mediated container will never send a PING. 
  	JICPPacket pkt = new JICPPacket(JICPProtocol.COMMAND_TYPE, JICPProtocol.COMPRESSED_INFO, JICPProtocol.getInstance().addrToString(ta), payload);
  	pkt = deliverCommand(pkt);
    return pkt.getData();
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

  ////////////////////////////
  // EndPoint IMPLEMENTATION
  ////////////////////////////
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
      		errorMsg = "Can't connect to "+mediatorServerTA+". "+ioe.toString();
	        throw new ICPException(errorMsg);
      	}
      }
    }
  }  

  private void connect() throws IOException, ICPException {
    // Open the connection and gets the output and input streams
    Connection c = new Connection(mediatorServerTA);
    DataOutputStream out = new DataOutputStream(c.getOutputStream());
    DataInputStream inp = new DataInputStream(c.getInputStream());

    JICPPacket pkt = null;
    if (mediatorAlive) {
    	// This is a reconnection --> Send a CONNECT_MEDIATOR request
    	pkt = new JICPPacket(JICPProtocol.CONNECT_MEDIATOR_TYPE, JICPProtocol.DEFAULT_INFO, mediatorId, null);
    }
    else {
    	// This is the first time --> Send a CREATE_MEDIATOR request
    	StringBuffer sb = new StringBuffer(JICPProtocol.MEDIATOR_CLASS_KEY);
    	sb.append("=jade.imtp.leap.JICP.Mediator;");
    	sb.append(JICPProtocol.MAX_DISCONNECTION_TIME_KEY);
    	sb.append('=');
    	sb.append(maxDisconnectionTime);
    	pkt = new JICPPacket(JICPProtocol.CREATE_MEDIATOR_TYPE, JICPProtocol.DEFAULT_INFO, null, sb.toString().getBytes());
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
  
  ////////////////////////////////////
  // EndPoint IMPLEMENTATION
  ////////////////////////////////////
  protected JICPPacket handleCommand(JICPPacket cmd) throws Exception {
  	byte[] rspData = cmdListener.handleCommand(cmd.getData());
    return new JICPPacket(JICPProtocol.RESPONSE_TYPE, JICPProtocol.DEFAULT_INFO, rspData);
  }
  
  /**
     Mutual exclusion with waitUntilConnected()
   */
	protected synchronized void handleConnectionReady() {
		notifyAll();		
	}
	
  /**
     Mutual exclusion with waitUntilConnected()
   */
	protected synchronized void handleConnectionError() {
		notifyAll();		
	}
}

