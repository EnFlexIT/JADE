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
import jade.mtp.TransportAddress;
import java.io.*;
// import java.net.*;
import jade.util.leap.*;
import jade.core.Runtime;

/**
 * Class declaration
 * @author Nicolas Lhuillier - Motorola
 * @author Giovanni Caire - TILAB
 * @author Jerome Picault - Motorola
 */
public class JICPMServer extends Thread {

  private long             retryTime;
  private long             maxDisconnectionTime;

  private boolean          active = true;
  private JICPMPeer        myPeer;
  private Thread           terminator = null;

  private ICP.Listener     cmdListener;

  // Variables related to the connection with the Mediator
  private TransportAddress mediatorTA;
  private Connection       theConnection;
  private DataInputStream  inp;
  private DataOutputStream out;
  private boolean          connected = false;
  private long             totalDisconnectionTime = 0;

  private static int       verbosity = 1;

  /**
   * Construct a new JICPMServer.
   * @param ta The <code>TransportAddress</code> of the Mediator this
   * JICPMServer is connected to.
   * @param l The <code>ICP.Listener</code> that will handle commands
   * received by this JICPMServer
   */
  public JICPMServer(TransportAddress ta, ICP.Listener l, JICPMPeer p, long r, long m) throws ICPException {
    cmdListener = l;
    mediatorTA = ta;
    myPeer = p;
    retryTime = r;
    maxDisconnectionTime = m;
  }

  /**
   * Shut down this JICP server.
   * This is called when the local container is exiting.
   */
  public void shutdown() {
    active = false;
    terminator = Thread.currentThread();
    log("Shutdown initiated. Terminator thread is "+terminator);
  } 

  /**
   * MServer thread entry point
   */
  public void run() {
    while (active) {

      // Connect to the Mediator
      while (!connected) {
        try {
          log("Connecting to Mediator...");
          connect();
          myPeer.notifyConnected();
          log("Connection OK");
          totalDisconnectionTime = 0;
        } 
        catch (IOException ioe) {
          if (totalDisconnectionTime > maxDisconnectionTime) {
            // PANIC
            log("Cannot reconnect to the platform! Try again later", 0);
            return;
          } 
          try {
          	// Wait for a while before trying again to (re-)connect
            Thread.sleep(retryTime);
          } 
          catch (InterruptedException ie) {
            log("InterruptedException while waiting for next reconnection attempt", 1);
          } 
          totalDisconnectionTime += retryTime;
        } 
      } 

      while (connected) {
        try {
          log("Waiting for a command");

          // Read connection id
          int id = inp.readInt();
          log("Received a command from Mediator");

          JICPPacket        cmd = JICPPacket.readFrom(inp);

          // Start a new handler for the incoming connection
          ConnectionHandler ch = new ConnectionHandler(id, cmd, this);

          ch.start();
        } 
        catch (Exception e) {
          if (active) {
            // Error reading from push socket.
            log("Exception reading from connection to Mediator: "+e, 1);
          } 
          resetConnection();
        } 
      }    // End of loop on connected
    }     // End of loop on active
 
    log("Terminated");
  } 

  /**
   */
  private void connect() throws IOException {
    // Open the socket and gets the output and input streams
    theConnection = new Connection(mediatorTA);
    out = new DataOutputStream(theConnection.getOutputStream());
    inp = new DataInputStream(theConnection.getInputStream());

    // Send a CONNECT_MEDIATOR request
    JICPPacket p = new JICPPacket(JICPProtocol.CONNECT_MEDIATOR_TYPE, JICPProtocol.UNCOMPRESSED_INFO, mediatorTA.getFile(), null);
    p.writeTo(out);

    // Check that the response is OK
    p = JICPPacket.readFrom(inp);
    byte type = p.getType();
    if (type != JICPProtocol.RESPONSE_TYPE) {
    	// My Mediator does not exist!!!
    	// FIXME: Here we should try to create a new Mediator and connect to it
      throw new IOException("Unexpected response to CONNECT_MEDIATOR request. "+type);
    } 

    connected = true;
  } 

  /**
   */
  private void resetConnection() {
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

  /**
   * This method is called by the ConnectionHandlers to send
   * back responses to the Mediator in a synchronized way.
   * Mutual exclusion with itself as this method can be called by
   * different threads (the ConnectionHandlers) at the same time
   */
  synchronized void sendResponse(int id, JICPPacket rsp) throws Exception {
    boolean closing = false;
    // If this is the Thread that is shutting down this JICPMServer
    // (i.e. the Thread that has previously called the shutdown() method)
    // --> notify the Mediator
    if (Thread.currentThread().equals(terminator)) {
      log("Activate Mediator shutdown (after the current command has been served)");
      out.writeInt(-1);
      closing = true;
    } 

    // Send the connection id
    out.writeInt(id);

    // Send the response packet
    rsp.writeTo(out);

    if (closing) {
      out.close();
    } 
  } 

  /**
   */
  class ConnectionHandler extends Thread {
    private JICPMServer parent;
    private int         id;
    private JICPPacket  cmd;

    /**
     * Constructor declaration
     */
    public ConnectionHandler(int id, JICPPacket cmd, JICPMServer parent) {
      this.id = id;
      this.cmd = cmd;
      this.parent = parent;
    }

    /**
     * Thread entry point
     */
    public void run() {
      JICPPacket rsp = null;
      log("Start serving command");
      try {
        if (cmd.getType() != JICPProtocol.COMMAND_TYPE) {
          // If the JICP packet to handle is not a COMMAND -->
          // returns back an ERROR
          log("Current JICPPacket does not include an IMTP command. Send back an error response", 1);
          rsp = new JICPPacket(JICPProtocol.ERROR_TYPE, JICPProtocol.UNCOMPRESSED_INFO, null);
        } 
        else {
          // Extract the serialized command and passes it to the
          // command listener for processing
          byte[] rspData = cmdListener.handleCommand(cmd.getData());
          log("Command correctly served");
          rsp = new JICPPacket(JICPProtocol.RESPONSE_TYPE, JICPProtocol.UNCOMPRESSED_INFO, rspData);
        } 

        parent.sendResponse(id, rsp);
        log("Response sent back");
      } 
      catch (Exception e) {
        e.printStackTrace();
      } 
    } 
  }

  /**
   */
  static void log(String s) {
    log(s, 2);
  } 

  /**
   */
  static void log(String s, int level) {
    if (verbosity >= level) {
      String name = Thread.currentThread().toString();
      System.out.println("MServer ("+name+"): "+s);
    } 
  } 

}

