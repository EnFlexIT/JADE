/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

/**
 * ***************************************************************
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
 * Copyright (C) 2001 Broadcom Eireann Research.
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
 * **************************************************************
 */
package jade.imtp.leap.JICP;

//#MIDP_EXCLUDE_FILE

import jade.core.Profile;
import jade.core.ProfileException;
import jade.mtp.TransportAddress;
import jade.imtp.leap.*;
import java.io.*;
import java.net.*;

/**
 * Class declaration
 * @author Giovanni Caire - TILAB
 * @author Jamie Lawrence - Broadcom Eireann Research
 * @author Ronnie Taib - Motorola
 * @author Nicolas Lhuillier - Motorola
 */
public class JICPPeer implements ICP {
	private static final int POOL_SIZE = 10;
	
  private JICPClient   client;
  private JICPServer   server;
  private Ticker       ticker;

  /**
   * Start listening for internal platform messages on the specified port
   */
  public TransportAddress activate(ICP.Listener l, String peerID, Profile p) throws ICPException {
    client = new JICPClient(getProtocol(), getConnectionFactory(), POOL_SIZE);
  	
    String host = null;
    int    port = JICPProtocol.DEFAULT_PORT;
    
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
			
		// Local host
		sb.append(JICPProtocol.LOCAL_HOST_KEY);
		host = p.getParameter(sb.toString(), null);
		if (host == null) {
			// Local host not specified --> try to get it using JICP GET_ADDRESS
			sb.setLength(idLength);
			sb.append(JICPProtocol.REMOTE_URL_KEY);
			String remoteURL = p.getParameter(sb.toString(), null);
			if (remoteURL != null) {
				host = getAddress(remoteURL);
			}
  		else {
  			// Retrieve local host automatically
  			host = Profile.getDefaultNetworkName();
  		}
		}
			
		// Local port: a peripheral container can change it if busy...
		boolean changePortIfBusy = !p.getBooleanProperty(Profile.MAIN, true);
		sb.setLength(idLength);
		sb.append(JICPProtocol.LOCAL_PORT_KEY);
		String strPort = p.getParameter(sb.toString(), null);
    try {
      port = Integer.parseInt(strPort);
    } 
    catch (Exception e) {
      // Try to use the Peer-ID as the port number
    	try {
    		port = Integer.parseInt(peerID);
    	}
    	catch (Exception e1) {
    		// Keep default
    	}
    } 
			
    // Start listening for connections
    server = new JICPServer(port, changePortIfBusy, l, getConnectionFactory(), POOL_SIZE);
    server.start();

    // Start the Ticker
    ticker = new Ticker(60000);
    ticker.start();
    
    // Creates the local transport address
    TransportAddress localTA = getProtocol().buildAddress(host, String.valueOf(server.getLocalPort()), null, null);

    return localTA;
  } 

  /**
   * stop listening for internal platform messages
   */
  public void deactivate() throws ICPException {
  	if (server != null) {
  		client.shutdown();
      server.shutdown();
      ticker.shutdown();
    } 
    else {
      throw new ICPException("No external listener was activated.");
    } 
  } 

  /**
   * deliver a serialized command to a given transport address
   */
  public byte[] deliverCommand(TransportAddress ta, byte[] payload) throws ICPException {
    byte[] respPayload = client.send(ta, JICPProtocol.COMMAND_TYPE, payload);

    return (respPayload);
  } 

  /**
   * Pings the specified transport address in order to obtain
   * the local hostname or IP address.
   * @param pingURL The <code>URL</code> to ping (usually the
   * main container).
   * @return The local IP address of the local container as a
   * <code>String</code>.
   * 
   * @throws ICPException
   */
  private String getAddress(String pingURL) throws ICPException {
    byte[] respPayload = null;

    try {
      TransportAddress pingAddr = getProtocol().stringToAddr(pingURL);

      respPayload = client.send(pingAddr, JICPProtocol.GET_ADDRESS_TYPE, new byte[0]);
    } 
    catch (ICPException icpe) {
      throw new ICPException("JICP GET_ADDRESS error. Cannot retrieve local hostname: "
                                 +icpe.getMessage());
    } 

    return (new String(respPayload));
  } 

  /**
     Subclasses may re-define this method to return their own
     protocol
   */
  public TransportProtocol getProtocol() {
    return JICPProtocol.getInstance();
  } 
  
  /**
     Subclasses may re-define this method to return their own
     ConnectionFactory
   */
  protected ConnectionFactory getConnectionFactory() {
    return new ConnectionFactory() {
			public Connection createConnection(Socket s) {
				return new JICPConnection(s);
			}
			public Connection createConnection(TransportAddress ta) throws IOException {
				return new JICPConnection(ta);
			}
    };
  }  
  
  /**
     Inner class Ticker
   */
  private class Ticker extends Thread {
  	private long period;
  	private boolean active = false;
  	
  	private Ticker(long period) {
  		super();
  		this.period = period;
  	}
  	
  	public void start() {
  		active = true;
  		super.start();
  	}
  	
  	public void run() {
  		while (active) {
  			try {
  				Thread.sleep(period);
  				long currentTime = System.currentTimeMillis();
  				client.tick(currentTime);
  				server.tick(currentTime);
  			}
  			catch (InterruptedException ie) {
  			}
  		}
  	}
  	
  	public void shutdown() {
  		active = false;
  		interrupt();
  	}
  } // END of inner class Ticker
}

