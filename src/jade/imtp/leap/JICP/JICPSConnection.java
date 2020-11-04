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


//#J2ME_EXCLUDE_FILE
import java.io.IOException;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.net.Socket;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import jade.imtp.leap.SSLHelper;

import jade.mtp.TransportAddress;
import jade.util.Logger;

/**
 * Class declaration
 * @author Steffen Rusitschka - Siemens
 * @author Giosue Vitaglione - TILAB
 */
public class JICPSConnection extends JICPConnection {

	private static SSLSocketFactory scsf = null;
	protected static Logger myLogger = Logger.getMyLogger(JICPSConnection.class.getName());

	protected JICPSConnection() {
		super();
	}

	/**
	 * Constructor declaration
	 */
	public JICPSConnection(TransportAddress ta) throws IOException {
		this(ta, false, 0, null, 0);
	}

	public JICPSConnection(TransportAddress ta, boolean useSSLAuth) throws IOException {
		this(ta, useSSLAuth, 0, null, 0);
	}
	
	public JICPSConnection(TransportAddress ta, boolean useSSLAuth, int timeout, String bindHost, int bindPort) throws IOException {
		if (useSSLAuth) {
			constructJICPSConnectionWithAuth(ta, timeout, bindHost, bindPort);
		} else {
			constructJICPSConnectionNoAuth(ta, timeout, bindHost, bindPort);
		}
	} // end constructor

	private void constructJICPSConnectionNoAuth(TransportAddress ta, int timeout, String bindHost, int bindPort) throws IOException {
		myLogger.log(Logger.INFO, "Creating JICPSConnection with NO-AUTHENTICATION (only confidentiality).");

		if (scsf == null) {
			try {
				SSLContext ctx = SSLHelper.createContextNoAuth();
				scsf = (SSLSocketFactory) ctx.getSocketFactory();
			} catch (Exception e) {
				throw new IOException("Error creating SSLSocketFactory. " + e.toString());
			}
		}

		// For some reason the local address or port may be
		// in use
		int bindExceptionCnt = 0;
		while (true) {
			try {
				sc = scsf.createSocket();
				if (bindHost != null || bindPort > 0) {
					// Local binding explicitly specified
					myLogger.log(Logger.INFO, "Binding JICPSConnection with bindHost="+bindHost+" and bindPort="+bindPort);
					if (bindHost != null) {
						sc.bind(new InetSocketAddress(bindHost, bindPort));
					}
					else {
						sc.bind(new InetSocketAddress(bindPort));
					}
				}
				else {
					bindSocket(sc);
				}
				sc.connect(new InetSocketAddress(ta.getHost(), Integer.parseInt(ta.getPort())), timeout);
				socketCnt++;
				((SSLSocket) sc).setEnabledCipherSuites(SSLHelper.getSupportedKeys());
				sc.setTcpNoDelay(true);
				is = sc.getInputStream();
				os = getOutputStream();
				break;
			} catch (BindException be) {
				bindExceptionCnt++;
				if (bindExceptionCnt >= 10) {
					myLogger.log(Logger.SEVERE, "Error binding JICPSConnection with bindHost="+bindHost+" and bindPort="+bindPort);
					throw be;
				}
				
				// Do nothing and try again
			}
		}
	}

	private void constructJICPSConnectionWithAuth(TransportAddress ta, int timeout, String bindHost, int bindPort) throws IOException {
		if (myLogger.isLoggable(Logger.FINE)) {
			myLogger.log(Logger.FINE,
					"Creating JICPSConnection with MUTUAL AUTHENTICATION.");
		}
		if (scsf == null) {
			try {
				// create and init new SSL context appropriate for mutual Auth
				SSLContext ctx = SSLHelper.createContextWithAuth();
				scsf = (SSLSocketFactory) ctx.getSocketFactory();
			} catch (Exception e) {
				throw new IOException("Error creating SSLSocketFactory. " + e.toString());
			}
		}

		// For some reason the local address or port may be
		// in use
		while (true) {
			try {
				sc = scsf.createSocket();
				if (bindHost != null || bindPort > 0) {
					// Local binding explicitly specified
					myLogger.log(Logger.INFO, "Binding JICPSConnection with bindHost="+bindHost+" and bindPort="+bindPort);
					if (bindHost != null) {
						sc.bind(new InetSocketAddress(bindHost, bindPort));
					}
					else {
						sc.bind(new InetSocketAddress(bindPort));
					}
				}
				else {
					bindSocket(sc);
				}
				sc.connect(new InetSocketAddress(ta.getHost(), Integer.parseInt(ta.getPort())), timeout);
				socketCnt++;
				is = sc.getInputStream();
				os = getOutputStream();
				break;
			} catch (BindException be) {
				// Do nothing and try again
			}
		}// end while
	}

	/**
	 * Constructor declaration
	 */
	public JICPSConnection(Socket s) {
		super(s);
	}
}

