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

package jade.core;

import java.util.Date;
import java.util.Iterator;

import jade.domain.FIPAAgentManagement.Envelope;

import jade.lang.acl.ACLMessage;

import jade.mtp.MTP;
import jade.mtp.MTPException;

/**
  @author Giovanni Rimassa - Universita` di Parma
  @version $Date$ $Revision$
*/
class ACCProxy implements AgentProxy {

  private AID receiver;
  private acc myACC;

  public ACCProxy(AID id, acc anACC) {
    receiver = id;
    myACC = anACC;
  }

  public void dispatch(ACLMessage msg) throws NotFoundException {

    AID aid = msg.getSender();
    if(aid == null) {
      System.out.println("ERROR: null message sender. Aborting message dispatch...");
      return;
    }

    // if has no address set, then adds the addresses of this platform
    if(!aid.getAllAddresses().hasNext())
      myACC.addPlatformAddresses(aid);

    Iterator it1 = msg.getAllReceiver();
    while(it1.hasNext()) {
      AID id = (AID)it1.next();
      if(!id.getAllAddresses().hasNext())
	myACC.addPlatformAddresses(id);
    }

    Iterator it2 = msg.getAllReplyTo();
    while(it2.hasNext()) {
      AID id = (AID)it2.next();
      if(!id.getAllAddresses().hasNext())
	myACC.addPlatformAddresses(id);
    }

    myACC.prepareEnvelope(msg, receiver);
    Envelope env = msg.getEnvelope();
    byte[] payload = myACC.encodeMessage(msg);

    Iterator addresses = receiver.getAllAddresses();
    while(addresses.hasNext()) {
      String address = (String)addresses.next();
      try {
	myACC.forwardMessage(env, payload, address);
	return;
      }
      catch(MTPException mtpe) {
	System.out.println("Bad address [" + address + "]: trying the next one...");
      }
    }
    throw new acc.NoMoreAddressesException("No valid address contained within the AID.");
  }

}
