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

import jade.imtp.leap.ICP;
import jade.imtp.leap.ICPException;
import jade.util.leap.*;
import java.io.*;

/**
 * Class declaration
 * @author Giovanni Caire - TILAB
 */
public abstract class EndPoint extends Thread {
	
  private boolean    active = true;
  private boolean    connected = false;
  private Connection theConnection;
  private Object     connectionLock = new Object(); // We can't synchronize on the connection itself as it may be null
  private DataInputStream  inp;
  private DataOutputStream out;
  
	private int pktCnt = 0;
  private OutgoingHandler[] outgoings = new OutgoingHandler[5];
  protected int verbosity = 1;

  /**
   * Constructor declaration
   */
  public EndPoint() {
  	super();
  }

  public JICPPacket deliverCommand(JICPPacket cmd) throws ICPException {
  	OutgoingHandler h = new OutgoingHandler();
  	JICPPacket rsp = h.handle(cmd);
    if (rsp.getDataType() == JICPProtocol.ERROR_TYPE) {
    	// We are connected, but there was a JICP error on the peer
      throw new ICPException(new String(rsp.getData()));
    } 
    return rsp;
  }
  
  /**
     @param self Indicate whether this shutdown was self-initiated or 
     activated as a result of a command issued by the remote peer. 
     In the first case we must send an explicit termination notification
     to the remote EndPoint to let it know we are exiting,
     In the latter case, on the other hand, the termination notification 
     must be appended to the response (that otherwise would be lost) to
     the command that activated the shutdown. It is the responsibility 
     of this EndPoint implementation (that is in charge of preparing the
     response) to do that.
	 */     
  public void shutdown(boolean self) {
  	active = false;
  	// Note that waking up OutgoingHandlers waiting for a response
  	// is necessary as the main thread can exit smoothly as soon as 
  	// we set active to false.
  	wakeupOutgoings();
  	if (self) {
  		// Send a JICPPacket with the TERMINATED_INFO set
  		JICPPacket pkt = new JICPPacket(JICPProtocol.COMMAND_TYPE, (byte) (JICPProtocol.UNCOMPRESSED_INFO | JICPProtocol.TERMINATED_INFO), null);
  		log("Pushing termination notification");
  		push((byte) 0, pkt);
  	} 		
  }
  
  public final boolean isConnected() {
  	return connected;
  }
  
  protected abstract void setup() throws ICPException;
	protected abstract JICPPacket handleCommand(JICPPacket cmd) throws Exception;
	protected void handleConnectionReady() {
	}
	protected void handleConnectionError() {
	}
	protected void handlePeerExited() {
	}

  /**
   * Thread entry point
   */
  public final void run() {
    while (active) {
      log("Connection setup...", 2);
    	try {
      	setup();
      	log("Connection ready", 2);
	    	handleConnectionReady();
	    }
	    catch (ICPException icpe) {
      	log("Connection cannot be (re)established. "+icpe.getMessage(), 0);
	    	handleConnectionError();
	    	break;
	    }

      while (connected) {
        try {
          log("Waiting for a command...");

          // Read session id and JICPPacket
          byte id = inp.readByte();
          JICPPacket pkt = JICPPacket.readFrom(inp);
          pktCnt = (pktCnt+1) & 0x0fff;
        	if (pkt.getDataType() == JICPProtocol.COMMAND_TYPE) {
          	if ((pkt.getDataInfo() & JICPProtocol.TERMINATED_INFO) != 0) {
	          	log("Peer termination notification received", 2);
          		// The remote EndPoint has terminated spontaneously -->
          		// close the connection, notify the local peer and exit
          		shutdown(false);
          		resetConnection();
          		handlePeerExited();
          	}
          	else {
	          	log("Command received. INC-SID="+id);
	
	          	// Start a new IncomingHandler for the incoming connection
	          	IncomingHandler h = new IncomingHandler(id, pkt);
	          	h.start();
          	}
        	}
        	else {
          	log("Response received. OUT-SID="+id);
          	
          	// Dispatch the response  to the OutgoingHandler that is waiting for it
          	OutgoingHandler h = deregisterOutgoing(id);
          	h.setResponse(pkt);
          	
          	if ((pkt.getDataInfo() & JICPProtocol.TERMINATED_INFO) != 0) {
          		// The remote EndPoint has terminated as a consequence
          		// of a command issued by the local peer --> 
          		// just close the connection and exit
          		shutdown(false);
          		resetConnection();
          	}
        	}	
        } 
        catch (Exception e) {
          if (active) {
            // Error reading from socket. The connection is no longer valid.
            log("Exception reading from connection: "+e, 1);
          } 
          wakeupOutgoings();
          resetConnection();
        } 
      }    // End of loop on connected
    }     // End of loop on active
 
    log("EndPoint thread terminated", 2);
  } 
  
  /**
     Mutual exclusion with setConnection() and resetConnection()
   */
  private boolean push(byte id, JICPPacket pkt) {
  	synchronized (connectionLock) {
  		if (connected) {
  			try {
			    // Write the session id and the packet
			    out.writeByte(id);
				  pkt.writeTo(out);
        	return true;
		    }
		    catch (IOException ioe) {
	        // The connection is down! Reset it so that the EndPoint thread
	      	// detects the disconnection and handles it properly.
  				resetConnection();
		    }
  		}
  		return false;
  	}
  } 

  /**
     Mutual exclusion with setConnection() and push()
   */
  protected final void resetConnection() {
  	synchronized (connectionLock) {
  		if (connected) {
			 	try {
			    inp.close();
			    out.close();
			    theConnection.close();
			  } 
			  catch (Exception e) {
			  }
			  theConnection = null;
		  	inp = null;
		  	out = null;
		  	connected = false;
  		}
  	}
  }
    	
  /**
     Mutual exclusion with resetConnection() and push()
   */
  protected final void setConnection(Connection c) throws IOException {
  	synchronized (connectionLock) {
			theConnection = c;
	    out = new DataOutputStream(theConnection.getOutputStream());
	    inp = new DataInputStream(theConnection.getInputStream());
			connected = true;
  	}
  }
		 		
  //////////////////////////////////////////
  // COMMAND RECEPTION AND HANDLING
  //////////////////////////////////////////
  
  /**
     Inner class IncomingHandler.
     This class handles an incoming command in a separated thread.
   */
  class IncomingHandler extends Thread {
    private byte            id;
    private JICPPacket      cmd;

    /**
     * Constructor declaration
     */
    public IncomingHandler(byte id, JICPPacket cmd) {
      this.id = id;
      this.cmd = cmd;
    }

    /**
     * IncomingHandler thread entry point
     */
    public final void run() {
      JICPPacket rsp = null;
      log("Start serving incoming command. INC-SID="+id);

      // Extract the serialized command and passes it to the
      // listener for processing
    	try {
        rsp = handleCommand(cmd);
        log("Command correctly handled. INC-SID="+id);
    	}
    	catch (Exception e) {
    		rsp = new JICPPacket("IMTP Error", e);
    	}

    	// Push back the response
      if (!push(id, rsp)) {
      	// The connection went down while we were serving the command.
      	// This is the worst case as it may lead to inconsistencies.
      	log("WARNING: Can't push back response. INC-SID="+id, 0);
      }
      else {
	      log("Response pushed back. INC-SID="+id);
      }
    } 
  }  // END of Inner class IncomingHandler

  //////////////////////////////////////////
  // COMMAND DELIVERY
  //////////////////////////////////////////
  /**
     Inner class OutgoingHandler.
     This class handles an outgoing command. It writes the command to 
     the connection and waits until the EndPoint thread gets the
     response from the peer. At that point the response is passed up to 
     the command originator.
   */
  private class OutgoingHandler {
    private JICPPacket rsp = null;
    private boolean rspReceived = false;

    /**
     * Constructor declaration
     */
    private OutgoingHandler() {
    }

    /**
     */
    private final JICPPacket handle(JICPPacket cmd) throws ICPException {
    	// Register as waiting for a response and acquire a free ID
	  	byte myId = registerOutgoing(this);
	    log("Start serving outgoing command. OUT-SID="+myId);
  	
  		// Push the command
    	if (!push(myId, cmd)) {  	
    		// We are disconnected --> Deregister and throw an Exception
    		log("WARNING: Can't push command. OUT-SID="+myId, 0);
    		deregisterOutgoing(myId);
    		throw new ICPException("Disconnected");
    	}
    	else {
    		log("Command pushed. OUT-SID="+myId);
    	}
    	
			// Wait until the EndPoint thread receives the response
			waitForResponse();
			
			// When we get here rsp has been filled by the EndPoint thread
			return rsp;
    } 
    
    /**
     * Wait until the EndPoint thread has read the response from
     * the Connection or a timeout has expired.
     * Mutual exclusion with setResponse()
     */
    private synchronized final void waitForResponse() throws ICPException {
      while (!rspReceived) {
        try {
        	int oldCnt = pktCnt;
          wait(20000);
          if (pktCnt == oldCnt) {
          	// Timeout expired and no packet (including the response we 
          	// are waiting for) were received --> The connection is 
          	// probably down
          	log("Response timeout expired. Reset the connection", 2);
          	resetConnection();
          	break;
          }
        } 
        catch (InterruptedException ie) {
          log("Interruption while waiting for response", 1);
        } 
      } 

      if (rsp == null) {
      	// The connection went down while we were waiting for the response
      	throw new ICPException("Disconnection while waiting for response");
      }
    } 

    /**
     * This method is called by the EndPoint thread to set the response
     * received from the peer and to wake up this OutgoingHandler
     * Mutual exclusion with waitForResponse()
     */
    private synchronized final void setResponse(JICPPacket p) {
    	rspReceived = true;
      rsp = p;
      notifyAll();
    }     
  }  // END of Inner class OutgoingHandler

  /**
     This method is called by an OutgoingHandler to be registered in
     the pool of OutgoingHandlers waiting for a response from the 
     peer.
     @return an ID that will identify the session managed by the 
     calling OutgoingHandler.
   */
  private final byte registerOutgoing(OutgoingHandler h) throws ICPException {
  	synchronized (outgoings) {
  		try {
		    // Find the first free position and put the handler there
		    int i;
	  	  for (i = 0; outgoings[i] != null; i++);
	    	outgoings[i] = h;
	    	return (byte)i;
  		}
  		catch (ArrayIndexOutOfBoundsException aioobe) {
  			throw new ICPException("Can't allocate a new session");
  		}
  	}
  } 

  /**
	 */
  private final OutgoingHandler deregisterOutgoing(byte id) {
  	synchronized (outgoings) {
  		OutgoingHandler h = outgoings[id];
  		outgoings[id] = null;
  		return h;
  	}
  }
  
  /**
     This method is called by the EndPoint thread when it detects  
     that the connection is no longer valid. 
     All OutgoingHandlers waiting for a response
     will return an error to the command originator.
   */
  private final void wakeupOutgoings() {
  	synchronized (outgoings) {
	    for (int i = 0; i < outgoings.length; ++i) {
	      if (outgoings[i] != null) {
	      	outgoings[i].setResponse(null);
	      	outgoings[i] = null;
	      } 
	    }
  	}
  } 


  /**
   */
  void log(String s) {
    log(s, 3);
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

