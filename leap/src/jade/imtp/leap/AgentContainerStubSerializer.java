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

/**
 * AgentContainerStubSerializer
 */
class AgentContainerStubSerializer implements Serializer {

  /**
   * @see Serializer#serialize()
   */
  public void serialize(Object obj, 
                        DeliverableDataOutputStream ddout) throws LEAPSerializationException {
    try {
      AgentContainerStub acs = (AgentContainerStub) obj;

      ddout.writeInt(acs.remoteID);

      int cnt = acs.remoteTAs.size();

      ddout.writeInt(cnt);

      for (int i = 0; i < cnt; ++i) {
        ddout.writeObject(acs.remoteTAs.get(i));

        /*
         * TransportAddress ta = (TransportAddress) acs.remoteTAs.get(i);
         * Serializer s = Serializer.getSerializer(ta);
         * ddout.writeUTF(s.getClass().getName());
         * s.serialize(ta, ddout);
         */
      } 
    } 
    catch (ClassCastException cce) {
      throw new LEAPSerializationException("Object "+obj
                                           +" not instance of class MainContainerStub. Can't serialize it");
    } 
    catch (IOException ioe) {
      throw new LEAPSerializationException("IO error serializing Stub "+obj);
    } 
  } 

  /**
   * @see Serializer#deserialize()
   */
  public Object deserialize(DeliverableDataInputStream ddin) throws LEAPSerializationException {
    AgentContainerStub acs = new AgentContainerStub();

    try {
      acs.remoteID = ddin.readInt();

      int cnt = ddin.readInt();

      for (int i = 0; i < cnt; ++i) {

        /*
         * String serName = ddin.readUTF();
         * Serializer s = (Serializer) Class.forName(serName).newInstance();
         * TransportAddress ta = (TransportAddress) s.deserialize(ddin);
         * acs.remoteTAs.add(ta);
         */
        acs.remoteTAs.add(ddin.readObject());
      } 

      return acs;
    } 
    catch (IOException ioe) {
      throw new LEAPSerializationException("IO error deserializing AgentContainerStub");
    } 
    catch (Exception e) {
      throw new LEAPSerializationException("Error deserializing AgentContainerStub");
    } 
  } 

}

