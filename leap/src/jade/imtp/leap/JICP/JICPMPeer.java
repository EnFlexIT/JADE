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

import jade.core.Profile;
import jade.core.ProfileException;
import jade.mtp.TransportAddress;
import jade.imtp.leap.*;
import java.io.*;

/**
 * Class declaration
 * @author Nicolas Lhuillier - Motorola Labs
 * @author Ronnie Taib - Motorola Labs
 * @author Giovanni Caire - TILAB
 */
public class JICPMPeer implements ICP {

  /**
   * Default port for the local container JICP transport address
   */
  private JICPProtocol protocol;
  private JICPClient   client;
  private JICPMServer  server;
  private boolean      connected;

  /**
   * Constructor declaration
   */
  public JICPMPeer() {
    protocol = new JICPProtocol();
    client = new JICPClient();
    connected = false;
  }

  /**
   * Prepare to receive commands by activating a Mediator
   */
  public TransportAddress activate(ICP.Listener l, String peerID, Profile p) throws ICPException {
  	long             retryTime = JICPProtocol.DEFAULT_RETRY_TIME;
  	long             maxDisconnectionTime = JICPProtocol.DEFAULT_MAX_DISCONNECTION_TIME;
  	TransportAddress mediatorTA = null;
  	
  	StringBuffer sb = new StringBuffer(peerID);
		sb.append('-');
		int idLength = sb.length();
			
		// Read Mediator server URL 
		sb.append(JICPProtocol.REMOTE_URL_KEY);
		String remoteURL = p.getParameter(sb.toString(), null);
  	TransportAddress mediatorServerTA = protocol.stringToAddr(remoteURL);
			
		// Read Re-connection retry time
		sb.setLength(idLength);
		sb.append(JICPProtocol.RECONNECTION_RETRY_TIME_KEY);
		String tmp = p.getParameter(sb.toString(), null);
    try {
      retryTime = Long.parseLong(tmp);
    } 
    catch (Exception e) {
      // Use default
    } 
			
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
    sb = null;
			
    // Send the CREATE_MEDIATOR request
    byte[]           respPayload = client.send(mediatorServerTA, JICPProtocol.CREATE_MEDIATOR_TYPE, String.valueOf(maxDisconnectionTime).getBytes());
    String           mediatorID = new String(respPayload);
    mediatorTA = new JICPAddress(mediatorServerTA.getHost(), mediatorServerTA.getPort(), mediatorID, null);
    	
    // Create and start the server
    server = new JICPMServer(mediatorTA, l, this, retryTime, maxDisconnectionTime);
    server.start();

    // Wait for the server to connect to the Mediator
    waitUntilConnected();

    return mediatorTA;
  } 

  /**
   */
  private synchronized void waitUntilConnected() {
    while (!connected) {
      try {
        wait();
      } 
      catch (InterruptedException ie) {
      } 
    } 
  } 

  /**
   */
  synchronized void notifyConnected() {
    connected = true;
    notifyAll();
  } 

  /**
   */
  public void deactivate() throws ICPException {
    if (server != null) {
      server.shutdown();
    } 
    else {
      throw new ICPException("No external listener has been activated.");
    } 
  } 

  /**
   * Deliver a serialized command to a given transport address
   */
  public byte[] deliverCommand(TransportAddress ta, byte[] payload) throws ICPException {
    byte[] respPayload = client.send(ta, JICPProtocol.COMMAND_TYPE, payload);

    return (respPayload);
  } 

  /**
   * Method declaration
   * @return
   * @see
   */
  public TransportProtocol getProtocol() {
    return protocol;
  } 

}

