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

package jade.imtp.leap.JICP;

import java.io.*;

/**
 * Class declaration
 * @author Giovanni Caire - TILAB
 */
public class AsymFEDispatcher extends FrontEndDispatcher {

  /**
   * Constructor declaration
   */
  public AsymFEDispatcher() {
  	super();
  }

  /**
     Redefine the deliver() method in order to send packets to the 
     back end on a separate channel.
     This is necessary for certain KVM that do not support a write()
     operation on a socket to take place while there is a blocking
     read() in place.
   */
	protected int deliver(JICPPacket pkt) throws IOException {
		pkt.setRecipientID(mediatorId);
    Connection c = new Connection(mediatorServerTA);
    OutputStream o = c.getOutputStream();
		int cnt = pkt.writeTo(o);
		o.close();
		c.close();
		return cnt;
	}		
}

