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
import jade.util.Logger;
import java.io.*;

/**
 * Class declaration
 * @author Giovanni Caire - TILAB
 * @author Jerome Picault - Motorola Labs
 */
public abstract class EndPoint extends Thread {
	
  private boolean    active = true;
  private boolean    connected = false;
  private Connection theConnection;
  protected Object     connectionLock = new Object(); // We can't synchronize on the connection itself as it may be null
  private InputStream  inp;
  private OutputStream out;
  private Thread terminator;
  private Logger myLogger = Logger.getMyLogger(getClass().getName());
      

	protected int pktCnt = 0;
  private OutgoingHandler[] outgoings = new OutgoingHandler[5];
  
  // The following variables are protected as they can be set by
  // subclasses
  //protected int verbosity = 100;
  protected long respTimeout = 30000; // 30 sec
  protected int packetSize = 1024;  	

  /**
     Constructor declaration
  */
  public EndPoint() {
  	super();
  }

  /**
     Deliver a JICPPacket carrying a command to the remote
     EndPoint and get back another JICPPacket carrying the
     response
  */
  public JICPPacket deliverCommand(JICPPacket cmd) throws ICPException {
  	OutgoingHandler h = new OutgoingHandler();
  	JICPPacket rsp = h.handle(cmd);
    if (rsp.getType() == JICPProtocol.ERROR_TYPE) {
    	// We are connected, but there was a JICP error on the peer
      throw new ICPException(new String(rsp.getData()));
    } 
    return rsp;
  }
  
  /**
     Make this EndPoint terminate
  */     
  public void shutdown() {
  	if(myLogger.isLoggable(Logger.INFO))
  		myLogger.log(Logger.INFO,"Initiating shutdown");
    active = false;
  	// Note that waking up OutgoingHandlers waiting for a response
  	// is necessary as the main thread may exit smoothly as soon as 
  	// we set active to false.
  	wakeupOutgoings();
  	
    // If this is a self-initiated shut down, we must explicitly
  	// notify the peer. Otherwise the TERMINATED_INFO will be appended 
  	// to the response to the command that activated the shutdown.
  	// Note that in any case the TERMINATED_INFO is set within the push() 
  	// method.
  	terminator = Thread.currentThread();
  	if ((terminator != this) && !(terminator instanceof IncomingHandler)) {
  		JICPPacket pkt = new JICPPacket(JICPProtocol.COMMAND_TYPE, (byte) (JICPProtocol.DEFAULT_INFO), null);
  		if(myLogger.isLoggable(Logger.INFO))
  			myLogger.log(Logger.INFO,"Pushing termination notification");
  		push((byte) 0, pkt);
  	} 		
  }
  
  /**
     @return <code>true</code> if the connection to the remote
     EndPoint is currently up.
  */
  public final boolean isConnected() {
  	return connected;
  }
  
  /** 
      Set up the connection to the remote EndPoint. 
      Subclasses are expected to implement this method
  */   
  protected abstract void setup() throws ICPException;

  /** 
      Handle a JICPPacket carrying a command received from the
      remote EndPoint.
      Subclasses are expected to implement this method.
      @return the JICPPacket carrying the response to be sent back
  */   
  protected abstract JICPPacket handleCommand(JICPPacket cmd) throws Exception;
  
  /** 
      This method is called as soon as (and each time) the 
      connection to the remote EndPoint becomes up.
      The default implementation of this method does nothing, but 
      subclasses may redefine it to react to this event as needed.
  */
	protected void handleConnectionReady() {
	}
	
  /** 
      This method is called when there is no way to (re)establish
      the connection to the remote EndPoint.
      The default implementation of this method does nothing, but 
      subclasses may redefine it to react to this event as needed.
  */
	protected void handleConnectionError() {
	}
	
  /** 
      This method is called when the remote EndPoint exits
      spontaneously.
      The default implementation of this method does nothing, but 
      subclasses may redefine it to react to this event as needed.
  */
	protected void handlePeerExited() {
	}
  
  /**
   * EndPoint thread entry point
   */
  public final void run() {
    while(active) {
      if(myLogger.isLoggable(Logger.INFO))
      	myLogger.log(Logger.INFO,"Connection setup...");
    	try {
        setup();
        if(myLogger.isLoggable(Logger.INFO))
        	myLogger.log(Logger.INFO,"Connection ready");
        handleConnectionReady();
      }
      catch (ICPException icpe) {
        if(myLogger.isLoggable(Logger.WARNING))
        	myLogger.log(Logger.WARNING,"Connection cannot be (re)established. "+icpe.getMessage());
        handleConnectionError();
        break;
      }
      
      while (connected) {
        try {
          if(myLogger.isLoggable(Logger.INFO))
          	myLogger.log(Logger.INFO,"Waiting for a command...");

          // Read JICPPacket
          JICPPacket pkt = theConnection.readPacket();
          servePacket(pkt);          
        }
        catch (Throwable t) {
          if (active) {
            // Error reading from socket. The connection is no longer valid.
            if(myLogger.isLoggable(Logger.WARNING))
            	myLogger.log(Logger.WARNING,"Exception reading from connection: "+t);
          }
          if(myLogger.isLoggable(Logger.INFO))
          	myLogger.log(Logger.INFO,"Wakeing up outgoings");
          wakeupOutgoings();
          if(myLogger.isLoggable(Logger.INFO))
          	myLogger.log(Logger.INFO,"Resetting the connection");
          resetConnection();
        }
      }   // End of loop on connected
    }     // End of loop on active
    
    if(myLogger.isLoggable(Logger.INFO))
    	myLogger.log(Logger.INFO,"EndPoint thread terminated");
  }
  
  /**
     Serve an incoming packet:
     - If it is a COMMAND an IncomingHandler is created to 
     handle it.
     - If it is a RESPONSE or an ERROR it is passed to the 
     OutgoingHandler that is waiting for it.
     - Otherwise (likely it is a KEEP_ALIVE) only the incoming
     packet counter is incremented.
  */
  protected void servePacket(JICPPacket pkt) {
    byte id = pkt.getSessionID();
    byte type = pkt.getType();
    pktCnt = (pktCnt+1) & 0x0fff;
  	if (type == JICPProtocol.COMMAND_TYPE) {
    	if ((pkt.getInfo() & JICPProtocol.TERMINATED_INFO) != 0) {
      	if(myLogger.isLoggable(Logger.INFO))
      		myLogger.log(Logger.INFO,"Peer termination notification received");
    		// The remote EndPoint has terminated spontaneously -->
    		// close the connection, notify the local peer and exit
    		shutdown();
    		resetConnection();
    		handlePeerExited();
    	}
    	else {
      	if(myLogger.isLoggable(Logger.INFO))
      		myLogger.log(Logger.INFO,"Command received. INC-SID="+id);

      	// Start a new IncomingHandler for the incoming connection
      	IncomingHandler h = new IncomingHandler(id, pkt);
      	h.start();
    	}
  	}
  	else if (type == JICPProtocol.RESPONSE_TYPE || type == JICPProtocol.ERROR_TYPE) {
    	if(myLogger.isLoggable(Logger.FINEST))
    		myLogger.log(Logger.FINEST,"Response received. OUT-SID="+id);
    	
    	// Dispatch the response  to the OutgoingHandler that is waiting for it
    	OutgoingHandler h = deregisterOutgoing(id);
    	if (h == null) {
    		if(myLogger.isLoggable(Logger.WARNING))
    			myLogger.log(Logger.WARNING,"WARNING: No OutgoingHandler for id "+id);
    	}
    	else {
	    	h.setResponse(pkt);
	    	
	    	if ((pkt.getInfo() & JICPProtocol.TERMINATED_INFO) != 0) {
	    		// The remote EndPoint has terminated as a consequence
	    		// of a command issued by the local peer --> 
	    		// just close the connection and exit
	    		if(myLogger.isLoggable(Logger.INFO))
	    			myLogger.log(Logger.INFO,"Last response received. Close connection");
	    		shutdown();
	    		resetConnection();
	    	}
    	}
  	}
  }

  /**
     Push a packet with the given session ID to the remote EndPoint. 
     This is protected so that it can be directly called by 
     subclasses e.g. to send a KEEP_ALIVE packet.
     Mutual exclusion with setConnection() and resetConnection()
  */
  protected int push(byte id, JICPPacket pkt) {
  	synchronized (connectionLock) {
  		if (connected) {
  			try {
  				if (Thread.currentThread() == terminator) {
  					if(myLogger.isLoggable(Logger.INFO))
  						myLogger.log(Logger.INFO,"Setting TERMINATED_INFO");
  					pkt.setTerminatedInfo(true);
  				}
			    // Write the session id and the packet
  				pkt.setSessionID(id);
				  return deliver(pkt);
		    }
		    catch (IOException ioe) {
	        // The connection is down! Reset it so that the EndPoint thread
	      	// detects the disconnection and handles it properly.
		    	if(myLogger.isLoggable(Logger.INFO))
		    		myLogger.log(Logger.INFO,"Exception delivering packet. "+ioe.toString());
  				resetConnection();
		    }
  		}
  		return -1;
  	}
  } 
  
  /**
     The following code is isolated in a separate protected method to
     make it possible to customize it
  */
  protected int deliver(JICPPacket pkt) throws IOException {
  	return theConnection.writePacket(pkt);
  }

  /**
     Reset the connection to the remote EndPoint
     Mutual exclusion with setConnection() and push()
  */
  protected final void resetConnection() {
    synchronized (connectionLock) {
      if (connected) {
	      try {
          synchronized (inp) {
            inp.notifyAll();
          }
          inp.close();
          out.close();
          theConnection.close();
	      } 
	      catch (Exception e) {
          if(myLogger.isLoggable(Logger.WARNING))
          	myLogger.log(Logger.WARNING,"Exception resetting the connection "+e.toString());
	      }
	      theConnection = null;
	      inp = null;
	      out = null;
	      connected = false;
      }
    }
  }
    	
  /**
     Set <code>c</code> to be the connection to the remote
     EndPoint.
     Mutual exclusion with resetConnection() and push()
  */
  protected final void setConnection(Connection c) throws IOException {
  	synchronized (connectionLock) {
			theConnection = c;
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
      if(myLogger.isLoggable(Logger.FINEST))
      	myLogger.log(Logger.FINEST,"Start serving incoming command. INC-SID="+id);

      // Extract the serialized command and passes it to the
      // listener for processing
    	try {
        rsp = handleCommand(cmd);
        if(myLogger.isLoggable(Logger.INFO))
        	myLogger.log(Logger.INFO,"Command correctly handled. INC-SID="+id);
    	}
    	catch (Exception e) {
    		rsp = new JICPPacket("IMTP Error", e);
    	}

    	// Push back the response
      if (push(id, rsp) == -1) {
      	// The connection went down while we were serving the command.
      	// This is the worst case as it may lead to inconsistencies.
      	if(myLogger.isLoggable(Logger.WARNING))
      		myLogger.log(Logger.WARNING,"Can't push back response. INC-SID="+id);
      }
      else {
	      if(myLogger.isLoggable(Logger.FINEST))
	      	myLogger.log(Logger.FINEST,"Response pushed back. INC-SID="+id);
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
    private int oldPktCnt;

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
	    if(myLogger.isLoggable(Logger.FINEST))
	    	myLogger.log(Logger.FINEST,"Start serving outgoing command. OUT-SID="+myId);
      // Push the command
	    oldPktCnt = pktCnt;
	    int size = push(myId, cmd);
      if (size == -1) {  	
    		// We are disconnected --> Deregister and throw an Exception
    		if(myLogger.isLoggable(Logger.INFO))
    			myLogger.log(Logger.INFO,"Can't push command. OUT-SID="+myId);
    		deregisterOutgoing(myId);
    		throw new ICPException("Disconnected");
    	}
    	else {
    		if(myLogger.isLoggable(Logger.INFO))
    			myLogger.log(Logger.INFO,"Command pushed. OUT-SID="+myId);
    	}
    	
			// Wait until the EndPoint thread receives the response
			waitForResponse(respTimeout * (1+size/packetSize));
			
			// When we get here rsp has been filled by the EndPoint thread
			return rsp;
    } 
    
    /**
     * Wait until the EndPoint thread has read the response from
     * the Connection or a timeout has expired.
     * Mutual exclusion with setResponse()
     */
    private synchronized final void waitForResponse(long timeout) throws ICPException {
      while (!rspReceived) {
      	try {
          wait(timeout);
          if (pktCnt == oldPktCnt) {
          	// Timeout expired and no packet (including the response we 
          	// are waiting for) were received --> The connection is 
          	// probably down
          	if(myLogger.isLoggable(Logger.INFO))
          		myLogger.log(Logger.INFO,"Response timeout expired. Reset the connection");
          	resetConnection();
          	break;
          }
        } 
        catch (InterruptedException ie) {
          if(myLogger.isLoggable(Logger.INFO))
          	myLogger.log(Logger.INFO,"Interruption while waiting for response");
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
  /*
  void log(String s) {
    log(s, 3);
  } 
  */

  /**
   */
  //#J2ME_EXCLUDE_BEGIN
  protected void log(String s, java.util.logging.Level level) {
    //#J2ME_EXCLUDE_END 
    /*#J2ME_INCLUDE_BEGIN
    protected void log(String s, int level) {
    #J2ME_INCLUDE_END*/
    String name = Thread.currentThread().toString();
    if(myLogger.isLoggable(level))
    	myLogger.log(level,name+": "+s);
    //System.out.println(name+": "+s);
  } 
  
}

