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

import jade.mtp.MTP;
import jade.domain.FIPAAgentManagement.Envelope;

/**
   Implementation of <code><b>fipa.mts.mtp.iiop.std</b></code>
   specification for delivering ACL messages over the OMG IIOP
   transport protocol.

   @author Giovanni Rimassa - Universita` di Parma
   @version $Date$ $Revision$

 */
public class MessageTransportProtocol implements MTP {

  public void activate() {

  }

  public void deactivate() {

  }

  public void deliver(TransportAddress ta, Envelope env, byte[] payload) {

  }

  public TransportAddress strToAddr(String rep) {

  }

  public String addrToStr(TransportAddress ta) {

  }

}
