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
 * Copyright (C) 2001 Motorola.
 * Copyright (C) 2001 Telecom Italia LAB S.p.A.
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

//#J2ME_EXCLUDE_FILE

import java.io.*;
import java.net.*;
import java.util.*;
import jade.imtp.leap.Command;
import jade.imtp.leap.ICP;
import jade.imtp.leap.ICPException;
import jade.util.leap.Properties;
import jade.util.Logger;
import jade.core.TimerDispatcher;
import jade.core.Timer;
import jade.core.TimerListener;
import jade.core.Runtime;

/**
 * @author Nicolas Lhuillier - Motorola Labs
 * @author Jerome Picault - Motorola Labs
 * @author Giovanni Caire - Telecom Italia LAB S.p.A.
 */
public class Mediator extends EndPoint implements JICPMediator {
  private long              maxDisconnectionTime;

  // Lock for handling blocking PING
  private Object            pingLock = new Object();

  private JICPMediatorManager        myMediatorManager;
  private String            myID;

  // The socket connected to the mediated container
  private Connection        conn;
  private boolean           newConnectionReady = false;

  /**
   * Constructor declaration
   */
  public Mediator() { 
  }

  public String getId() {
  	return myID;
  }
  
  /**
     Initialize this JICPMediator
   */
  public void init(JICPMediatorManager mgr, String id, Properties props) throws ICPException {
    myMediatorManager = mgr;
    myID = id;
    try {
	    maxDisconnectionTime = Long.parseLong(props.getProperty(JICPProtocol.MAX_DISCONNECTION_TIME_KEY));
    }
    catch (NumberFormatException nfe) {
    	log("Error parsing max-disconnection-time", Logger.WARNING);
    	throw new ICPException("Error parsing max-disconnection-time");
    }

    start();

    //initCnt();
    log("Created Mediator v5.1. ID = "+myID+" MaxDisconnectionTime = "+maxDisconnectionTime,Logger.INFO);
  }  
  
  /**
     Make this Mediator terminate.
   */
  public void shutdown() {
    log("Initiate Mediator shutdown",Logger.INFO);

    // Deregister from the JICPServer
    if (myID != null) {
	    myMediatorManager.deregisterMediator(myID);
	    myID = null;
    }

    // Un-block threads hang in PING
    // FIXME: If shutdown is abnormal we should force the thread hanging
    // on the pingLock to return with an exception in order to make the Main 
    // container clean its tables.
	  synchronized (pingLock) {
  	  pingLock.notifyAll();
    }

    // Enable EndPoint shutdown
    super.shutdown();
  } 

  /**
     Shutdown forced by the JICPServer this Mediator is attached 
     to
   */
  public void kill() {
  	shutdown();
  }
  
  public void tick(long currentTime) {
  	// Not used
  }
  
  ///////////////////////////////////////////////
  // COMMANDS TO THE MEDIATED CONTAINER
  ///////////////////////////////////////////////
  
  /**
   * Push the received command to the mediated container unless it is a PING
   * Called by the JICPServer this Mediator is attached to.
   */
  public JICPPacket handleJICPPacket(JICPPacket p, InetAddress addr, int port) throws ICPException {
  	JICPPacket r = null;
  	if (isPing(p)) {
  		// If the command is a PING handle it locally
			r = new JICPPacket(JICPProtocol.RESPONSE_TYPE, JICPProtocol.DEFAULT_INFO, getSerializedPingResponse(true));   		
  	}
  	else {
  		// Otherwise forward it to the mediated container
  		p.setTerminatedInfo(false);
      r = deliverCommand(p);
  		updateTransmitted(p.getLength());
  		updateReceived(r.getLength());
  	}
		return r;
  } 

  /**
   * The IMTP PING command must be handled differently: it should
   * not be forwarded to the mediated container (no matter whether
   * it is a blocking or non-blocking PING)
   */
  private boolean isPing(JICPPacket p) {
    byte[] data = p.getData();
    int code = getCommandCode(data);
    if (code == Command.PING_NODE_BLOCKING) {
      // The JICPPacket carries a blocking PING command
      synchronized (pingLock) {
        try {
          log("Handling blocking PING command --> go to sleep",Logger.INFO);
          pingLock.wait();
          log("Resumed from sleeping on PING lock",Logger.INFO);
        } 
        catch (InterruptedException ie) {
          log("Interrupted while sleeping on PING lock"+ie,Logger.WARNING);
          ie.printStackTrace();
        } 
      } 
      return true;
    } 
    else if (code == Command.PING_NODE_NONBLOCKING) {
      // The JICPPacket carries a NON-blocking PING command
      log("Handling NON-blocking PING command --> reply directly",Logger.INFO);
      return true;
    } 

    // The JICPPacket does not carry a PING command
    return false;
  }

  //////////////////////////////////////////
  // COMMANDS FROM THE MEDIATED CONTAINER
  //////////////////////////////////////////
  /**
     Forwards a command received from the mediated container to the
     actual destination
   */
  protected JICPPacket handleCommand(JICPPacket cmd) throws Exception {
    Socket s = null;
    DataInputStream inp = null;
    DataOutputStream out = null;
    log("Start forwarding command",Logger.INFO);

    // Extract the serialized command and forwards it to the
    // actual destination
  	try {
  		updateReceived(cmd.getLength());
  		// Get the destination transport address
  		String addr = cmd.getRecipientID();
			JICPAddress destTa = (JICPAddress) JICPProtocol.getInstance().stringToAddr(addr); 
			
			// Adjust the recipient ID
			cmd.setRecipientID(destTa.getFile());
			// Notify the server that this connection is not reusable
      cmd.setTerminatedInfo(true);
			
			// Open a connection to the destination
      s = new Socket(destTa.getHost(), Integer.parseInt(destTa.getPort()));
      out = new DataOutputStream(s.getOutputStream());
      inp = new DataInputStream(s.getInputStream());

      // Forward the command
      cmd.writeTo(out);

      // Read the response
      JICPPacket rsp = JICPPacket.readFrom(inp);
      updateTransmitted(rsp.getLength());
      return rsp;
    } 
    catch (IOException ioe) {
      return new JICPPacket("Destination unreachable", ioe);
    } 
    finally {
      try {
        // Close the connection
        if (inp != null) {inp.close();} 
        if (out != null) {out.close();} 
        if (s != null) {s.close();} 
      } 
      catch (IOException ioe) {
      	// Ignore it.
      } 
    } 	
  }
  
  //////////////////////////////////////////
  // EndPoint IMPLEMENTATION
  //////////////////////////////////////////  
  /**
   */
  protected synchronized void setup() throws ICPException {
    while (!newConnectionReady) {
      try {
        wait(maxDisconnectionTime);
        if (!newConnectionReady) {
        	throw new ICPException("Mediated container is probably down!");
        }
      } 
      catch (InterruptedException ie) {
        log("InterruptedException while waiting for mediated container to (re)connect",Logger.INFO);
      } 
    } 
    // If we get here there is a new connection ready --> Pass the connection to the EndPoint
    try {
	    setConnection(conn);
    	newConnectionReady = false;
    }
    catch (IOException ioe) {
    	// The new connection is already down. Ignore it. The embedded thread
    	// will call setup() again.
    }
  }
  
	protected void handleConnectionError() {
		// If the connection cannot be re-established, exit
		shutdown();
	}	
	
  /**
   * Prepare to set the connection to the mediated container.
   * This is called by the JICPServer this Mediator is attached to
   * as soon as the mediated container (re)connects.
   * @param c the connection to the mediated container
   */
  public synchronized boolean handleIncomingConnection(Connection c, JICPPacket pkt, InetAddress addr, int port) {
    if (isConnected()) {
      // If the connection seems to be still valid then reset it so that 
    	// the embedded thread realizes it is no longer valid.
      resetConnection();
    } 
    conn = c;
    newConnectionReady = true;
    notifyAll();
    return true;
  } 
  
  /**
     Inspect a serialized command and retrieve the command code
   */
  private int getCommandCode(byte[] serializedCommand) {
    // the command code is an int at the beginning of the
    // byte array (see serializeCommand() in DeliverableDataOutputStream
    int ret = 0;
    for (int i = 0; i < 4; ++i) {
      // System.out.print(" " + serializedCommand[i]);
      ret <<= 8;
      ret |= ((int) serializedCommand[i])&255;
    } 
    return ret;
  } 

  /**
     Create an array of bytes that represent a serialized response to 
     a PING command
   */
  private byte[] getSerializedPingResponse(boolean b) {
  	ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
    	
		try {
			dos.writeInt(Command.OK);   		
			dos.writeInt(Command.DUMMY_ID);   		
			dos.writeInt(1);  // 1 param   		
			dos.writeBoolean(true); // Presence flag for param 1
			dos.writeByte((byte) 3); // Serializer.BOOLEAN_ID
			dos.writeBoolean(b);
    } 
    catch (IOException ioe) {
    	// Should never happen
      ioe.printStackTrace();
      return null;
    } 
    return baos.toByteArray();
  }
  
  //////////////////////////////////////////////////////////////////////
  // This part is only related to counting transmitted/received bytes
  //////////////////////////////////////////////////////////////////////
  
  private int transmittedCnt = 0;
  private int receivedCnt = 0;
  private Object cntLock = new Object();
  
  private void updateTransmitted(int n) {
  	synchronized(cntLock) {
  		transmittedCnt += n;
  	}
  }
  
  private void updateReceived(int n) {
  	synchronized(cntLock) {
  		receivedCnt += n;
  	}
  }
  
  private void initCnt() {
  	if (myID != null) {
  		final String id = myID;
	  	Timer t = new Timer(System.currentTimeMillis()+30000, new TimerListener() {
	  		public void doTimeOut(Timer t) {
	  			synchronized (cntLock) {
	  				System.out.println("Mediator "+id);
	  				System.out.println("Transmitted cnt = "+transmittedCnt);
	  				System.out.println("Received cnt    = "+receivedCnt);
	  				System.out.println("------------------------");
						transmittedCnt = 0;
						receivedCnt = 0;
	  			}
	  			initCnt();
	  		}
	  	} );
	  	TimerDispatcher td = Runtime.instance().getTimerDispatcher();
	  	td.add(t);
  	}
  }
}

