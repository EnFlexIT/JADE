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

//#MIDP_EXCLUDE_FILE

import jade.imtp.leap.ICP;
import jade.imtp.leap.ICPException;
import jade.util.leap.Properties;
import java.net.*;

/**
 * @author Giovanni Caire - Telecom Italia LAB S.p.A.
 */
public interface JICPMediator {
	
  /**
     Initialize this JICPMediator
   */
  void init(JICPServer srv, String id, Properties props) throws ICPException;
  
  /**
     Kill this JICPMediator 
   */
  void kill(); 

  /**
   * Passes to this JICPMediator the connection opened by the mediated 
   * entity.
   * This is called by the JICPServer this Mediator is attached to
   * as soon as the mediated entity (re)connects.
   * @param c the connection to the mediated entity
   * @param pkt the packet that was sent by the mediated entity when 
   * opening this connection
   * @param addr the address of the mediated entity
   * @param port the local port used by the mediated entity
   */
  JICPPacket handleIncomingConnection(Connection c, JICPPacket pkt, InetAddress addr, int port);
  
  /**
   * Passes to this JICPMediator a JICP packet.
   * This is called by the JICPServer this Mediator is attached to
   * when a JICPPacket is received having the recipient-ID field
   * set to the ID of this JICPMediator.
   * @param p the JICPPacket
   * @param addr the address of the mediated entity
   * @param port the local port used by the mediated entity
   */
  JICPPacket handleJICPPacket(JICPPacket p, InetAddress addr, int port) throws ICPException; 
}

