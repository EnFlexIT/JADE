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
import jade.domain.FIPAAgentManagement.ReceivedObject;

/**
 * ReceivedObjectSerializer
 */
class ReceivedObjectSerializer implements Serializer {

  /**
   * @see Serializer#serialize()
   */
  public void serialize(Object obj, 
                        DeliverableDataOutputStream ddout) throws LEAPSerializationException {
    try {
      ReceivedObject r = (ReceivedObject) obj;

      ddout.writeString(r.getBy());
      ddout.writeString(r.getFrom());
      ddout.writeDate(r.getDate());
      ddout.writeString(r.getId());
      ddout.writeString(r.getVia());
    } 
    catch (ClassCastException cce) {
      throw new LEAPSerializationException("Object "+obj
                                           +" not instance of class ReceivedObject. Can't serialize it");
    } 
  } 

  /**
   * @see Serializer#deserialize()
   */
  public Object deserialize(DeliverableDataInputStream ddin) throws LEAPSerializationException {
    ReceivedObject r = new ReceivedObject();

    r.setBy(ddin.readString());
    r.setFrom(ddin.readString());
    r.setDate(ddin.readDate());
    r.setId(ddin.readString());
    r.setVia(ddin.readString());

    return r;
  } 

}

