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

//#J2ME_EXCLUDE_FILE

import jade.core.Profile;
import jade.core.ProfileException;
import jade.mtp.TransportAddress;
import jade.imtp.leap.*;
import java.io.*;
import java.net.*;
import javax.net.ssl.*;

/**
 * Class declaration
 * @author Giovanni Caire - TILAB
 * @author Jamie Lawrence - Broadcom Eireann Research
 * @author Ronnie Taib - Motorola
 * @author Nicolas Lhuillier - Motorola
 */
public class JICPSPeer extends JICPPeer {
	private SSLContext ctx = null;
	
  /**
     Subclasses may re-define this method to return their own
     ConnectionFactory
   */
  public ConnectionFactory getConnectionFactory() {
    return new ConnectionFactory() {
			public Connection createConnection(Socket s) {
				return new JICPSConnection(s);
			}
			public Connection createConnection(TransportAddress ta) throws IOException {
				return new JICPSConnection(ta);
			}
    };
  }  
  
  protected ServerSocket getServerSocket(String host, int port, boolean changePortIfBusy) throws ICPException {
  	// Create the SSLContext if necessary
  	if (ctx == null) {
	  	try{
				ctx = SSLContext.getInstance("TLS");
				ctx.init(null, null, null);
			} 
			catch( Exception e) { 
				throw new ICPException("Error creating SSLContext.", e);
			}
  	}
		
		// Create the SSLServerSocket
		SSLServerSocket sss = null;
  	try {
			SSLServerSocketFactory ssf = ctx.getServerSocketFactory();
	    try {
	    	sss = (SSLServerSocket) ssf.createServerSocket(port); 
	    } 
	    catch (IOException ioe) {
	    	if (changePortIfBusy) {
	    		// The specified port is busy. Let the system find a free one
	    		try {
	      		sss = (SSLServerSocket) ssf.createServerSocket(0);
	    		}
	    		catch (IOException ioe2) {
	      		throw new ICPException("Problems initializing server socket. No free port found.", ioe2);
					}
	    	}
	    	else {
		      throw new ICPException("I/O error opening server socket on port "+port, ioe);
	    	}
	    }
  	}
  	catch (Exception e) {
  		throw new ICPException("Error creating SSLServerSocketFactory.", e);
  	}
  	
    // Initialize the SSLServerSocket to disable authentication
		try {
			sss.setEnabledCipherSuites(new String[] {"SSL_DH_anon_WITH_RC4_128_MD5"});

			String[] ecs = sss.getEnabledCipherSuites();
			//DEBUG
			//for (int i=0; i<ecs.length; i++) { 
			//	System.out.println("--"+i+"-- "+ecs[i]);
			//}
		}
		catch (Exception e) {
			throw new ICPException("Error enabling cypher suites.", e);
		}
		
		return sss;
  }
}

