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

import java.util.Iterator;

import jade.lang.acl.ACLMessage;

// This class translates ':sender' and ':receiver' AIDs, converting
// back and forth between JADE nicknames and FIPA GUIDs.
class AIDTranslator {

  private String platformID;

  public AIDTranslator(String id) {
    platformID = id;
  }

  // Tells whether the given AID refers to an agent of this platform
  // or not.
  public boolean livesHere(AID id) {
    String hap = id.getHap();
    return hap.equalsIgnoreCase(platformID);
  }

  // Returns an AID for a local agent of a given nickname, suitable
  // only for intra-platform use (i.e. no addresses).
  public AID localAID(String agentName) {
    if(!agentName.endsWith('@' + platformID))
      agentName = agentName.concat('@' + platformID);
    AID id = new AID();
    id.setName(agentName);
    id.clearAllAddresses();
    id.clearAllResolvers();
    return id;
  }

  // Returns an AID for a local agent of a given neckname, suitable
  // for also for use in different platforms.
  public AID globalAID(String agentName) {
    AID id = localAID(agentName);
    // FIXME: Add all platform addresses to this AID
    return id;
  }

  public void translateOutgoing(ACLMessage msg) {

    // The AID of the message sender must have the complete GUID
    AID msgSource = msg.getSender();
    if(!livesHere(msgSource)) {
      String guid = msgSource.getName();
      guid = guid.concat("@" + platformID);
      msgSource.setName(guid);
    }

    Iterator dests = msg.getAllReceiver();
    while(dests.hasNext()) {

      AID dest = (AID)dests.next();

      // If this AID has no explicit addresses, but it does not seem
      // to live here, then the platform ID is appended to the AID
      // name
      Iterator addresses = dest.getAllAddresses();
      if(!addresses.hasNext() && !livesHere(dest)) {
	String guid = dest.getName();
	guid = guid.concat("@" + platformID);
	dest.setName(guid);
      }

    }

  }

  public void translateIncoming(ACLMessage msg) {

  }

  public void translateRouted(ACLMessage msg) {

  }

}
