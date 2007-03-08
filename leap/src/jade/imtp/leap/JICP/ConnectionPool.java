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

//#MIDP_EXCLUDE_FILE

import jade.mtp.TransportAddress;
import java.io.*;
import jade.imtp.leap.*;
import jade.util.leap.*;
import jade.imtp.leap.http.HTTPProtocol;

/**
 * @author Giovanni Caire - TILAB
 */
class ConnectionPool {
	private HashMap connections = new HashMap();
	private TransportProtocol myProtocol;
	private ConnectionFactory myFactory;
	private int maxSize;
	private int size;
	private boolean closed = false;
	
	ConnectionPool(TransportProtocol p, ConnectionFactory f, int ms) {
		myProtocol = p;
		myFactory = f;
		// Temporary hack for HTTP since HTTP connections cannot be re-used
		if (myProtocol instanceof HTTPProtocol) {
			maxSize = 0;
		}
		else {
			maxSize = ms;
		}
		size = 0;
	}
	
	synchronized ConnectionWrapper acquire(TransportAddress ta, boolean requireFreshConnection) throws ICPException {
		if (!closed) {
			ConnectionWrapper cw = null;
			String url = myProtocol.addrToString(ta);
			List l = (List) connections.get(url);
			if (l == null) {
				l = new ArrayList();
				connections.put(url, l);
			}
			if (requireFreshConnection) {
				// We are checking a given destination. This means that this destination may be no longer valid
				// --> In order to avoid keeping invalid connections that can lead to very long waiting times, 
				// close all non-used connections towards this destination.
				l = closeConnections(l);
				connections.put(url, l);
			}
			else {
				Iterator it = l.iterator();
				while (it.hasNext()) {
					cw = (ConnectionWrapper) it.next();
					if (cw.lock()) {
						cw.setReused();
						return cw;
					}
				}
			}
			// If we get here no connection is available --> create a new one
			try {
				Connection c = myFactory.createConnection(ta);
				if (size < maxSize) {
					// Reusable connection --> Store it
					cw = new ConnectionWrapper(c);
					l.add(cw);
					size++;
				}
				else {
					// OneShot connection --> don't even store it
					cw = new ConnectionWrapper(c);
					cw.setOneShot();
				}
				return cw;
			}
			catch (IOException ioe) {
				throw new ICPException("Error creating connection. ", ioe);
			}
		}
		else {
			throw new ICPException("Pool closed");
		}
	}
	
	private List closeConnections(List l) {
		List validConnections = new ArrayList();
		Iterator it = l.iterator();
		while (it.hasNext()) {
			ConnectionWrapper cw = (ConnectionWrapper) it.next();
			if (cw.lock()) {
				cw.close();
				cw.unlock();
			}
			else {
				validConnections.add(cw);
			}
		}
		return validConnections;
	}

	synchronized void release(ConnectionWrapper cw) {
		cw.unlock();
	}
	
	synchronized void remove(TransportAddress ta, ConnectionWrapper cw) {
		try {
			String url = myProtocol.addrToString(ta);
			List l = (List) connections.get(url);
			if (l != null) {
				if (l.remove(cw)) {
					size--;
				}
			}
			cw.getConnection().close();
		}
		catch (Exception e) {
			// Just ignore it
		}
	}
	
	synchronized void shutdown() {
		Iterator it = connections.values().iterator();
		
		while (it.hasNext()) {
			List l = (List) it.next();
			for (int i = 0; i < l.size(); i++) {
				ConnectionWrapper cw = (ConnectionWrapper) l.get(i);
				cw.close();
			}
			l.clear();
		} 
		connections.clear();
		closed = true;
	}
}

