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

package jade.imtp.rmi;

import java.io.*;
import java.net.*;
import java.rmi.server.*;
import javax.net.ssl.*;
import java.security.KeyStore;
import javax.net.*;
import javax.net.ssl.*;
import javax.security.cert.X509Certificate;
import com.sun.net.ssl.*;

/**
	This class creates a secure socket to build	a secure RMI session on.

	@author Michele Tomaiuolo - Universita` di Parma
	@version $Date$ $Revision$
*/
public class RMISSLClientSocketFactory
	implements RMIClientSocketFactory, Serializable {

	/**
		Creates the SSL client socket, which will be used
		to instantiate a <code>UnicastRemoteObject</code>.
		@param host The host to connect to.
		@param port The port to connect to.
		@return The SSL client socket.
	*/
	public Socket createSocket(String host, int port) throws IOException {
		if (System.getProperty("jade.security.ssl") == null)
			return new Socket(host, port);

	    /*SSLSocketFactory factory = null;
	    try {
			System.setProperty("javax.net.ssl.trustStore", "trustkeys");

			javax.net.ssl.SSLContext ctx;
			javax.net.ssl.KeyManagerFactory kmf;
			KeyStore ks;
			char[] passphrase = "passphrase".toCharArray();
	
			ctx = javax.net.ssl.SSLContext.getInstance("TLS");
			kmf = javax.net.ssl.KeyManagerFactory.getInstance("SunX509");
			ks = KeyStore.getInstance("JKS");
	
			ks.load(new FileInputStream("clientkeys"), passphrase);
	
			kmf.init(ks, passphrase);
			ctx.init(kmf.getKeyManagers(), null, null);
	
			factory = ctx.getSocketFactory();
	    }
	    catch (Exception e) {
			throw new IOException(e.getMessage());
	    }*/
		SocketFactory factory = SSLSocketFactory.getDefault();
	    return factory.createSocket(host, port);
	}
}
