/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

/*
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


package jade.imtp.leap;

import java.io.IOException;
import jade.domain.FIPAAgentManagement.Envelope;
import jade.domain.FIPAAgentManagement.ReceivedObject;
import jade.util.leap.*;
import jade.core.AID;

/**
 * EnvelopeSerializer
 */
class EnvelopeSerializer implements Serializer {

  /**
   * @see Serializer#serialize()
   */
  public void serialize(Object obj, 
                        DeliverableDataOutputStream ddout) throws LEAPSerializationException {
    try {
      Envelope e = (Envelope) obj;

      // to
      Iterator it = e.getAllTo();

      while (it.hasNext()) {
        ddout.writeBoolean(true);
        ddout.serializeAID((AID) it.next());
      } 

      ddout.writeBoolean(false);
      ddout.writeAID(e.getFrom());
      ddout.writeString(e.getComments());
      ddout.writeString(e.getAclRepresentation());
      ddout.writeLong(e.getPayloadLength().longValue());
      ddout.writeString(e.getPayloadEncoding());
      ddout.writeDate(e.getDate());

      // encrypted
      it = e.getAllEncrypted();

      while (it.hasNext()) {
        ddout.writeBoolean(true);
        ddout.writeUTF((String) it.next());
      } 

      ddout.writeBoolean(false);

      // intended receivers
      it = e.getAllIntendedReceiver();

      while (it.hasNext()) {
        ddout.writeBoolean(true);
        ddout.serializeAID((AID) it.next());
      } 

      ddout.writeBoolean(false);
      ddout.writeObject(e.getReceived());

      // ddout.writeObject(e.getTransportBehaviour());
    } 
    catch (ClassCastException cce) {
      throw new LEAPSerializationException("Object "+obj
                                           +" not instance of class Envelope. Can't serialize it");
    } 
    catch (IOException ioe) {
      throw new LEAPSerializationException("IO error serializing Envelope "+obj);
    } 
  } 

  /**
   * @see Serializer#deserialize()
   */
  public Object deserialize(DeliverableDataInputStream ddin) throws LEAPSerializationException {
    Envelope e = new Envelope();

    try {
      while (ddin.readBoolean()) {
        e.addTo(ddin.deserializeAID());
      } 

      e.setFrom(ddin.readAID());
      e.setComments(ddin.readString());
      e.setAclRepresentation(ddin.readString());
      e.setPayloadLength(new Long(ddin.readLong()));
      e.setPayloadEncoding(ddin.readString());
      e.setDate(ddin.readDate());

      while (ddin.readBoolean()) {
        e.addEncrypted(ddin.readUTF());
      } 

      while (ddin.readBoolean()) {
        e.addIntendedReceiver(ddin.deserializeAID());
      } 

      e.setReceived((ReceivedObject) ddin.readObject());

      // e.setTransportBehaviour((Properties) ddin.readObject());
      return e;
    } 
    catch (IOException ioe) {
      throw new LEAPSerializationException("IO error deserializing Envelope");
    } 
  } 

}

