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


package jade.domain;

import java.util.Map;
import java.util.HashMap;

import jade.domain.FIPAAgentManagement.AID;
import jade.lang.acl.ACLMessage;

/**
  Standard <em>Agent Communication Channel</em>. This class implements
  <em><b>FIPA</b></em> <em>ACC</em> service. <b>JADE</b> applications
  cannot use this class directly, but interact with it transparently
  when using <em>ACL</em> message passing.
  
  @author Giovanni Rimassa - Universita` di Parma
  @version $Date$ $Revision$

*/
public class acc {

  private Map messageEncodings = new HashMap();
  private Map MTPs = new HashMap();

  public acc() {

  }

  public void forwardMessage(ACLMessage msg, AID receivers[]) {

    // Steps:
    // 0. Split the operation in a suitable number of single-receiver operations.
    // 1. Select the message encoding, (reading the ':encoding' slot from ACL ?)
    // 2. Encode the message into a byte array.
    // 3. Deliver the message over an MTP.
    //    3.1. Build the message envelope.
    //    3.2. Select the MTP, reading the 'protocol' field of the first ':addresses' element
    //    3.3. Try to deliver to the selected address.
    //    3.4. Repeat step 3. until OK or no more addresses.

  }

  public void shutdown() {

  }

}
