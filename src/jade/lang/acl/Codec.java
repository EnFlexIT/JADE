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

package jade.lang.acl;

/**
   Abstract interface for converting ACL messages back and forth
   between Java objects and raw byte sequences, according to a FIPA
   ACL message representation.

   @author Giovanni Rimassa - Universita` di Parma
   @version $Date$ $Revision$

 */
public interface Codec {

  /**
     Encodes an <code>ACLMessage</code> object into a byte sequence,
     according to the specific message representation.
     @param msg The ACL message to encode.
     @return a byte array, containing the encoded message.
  */
  byte[] encode(ACLMessage msg);

  /**
     Recovers an <code>ACLMessage</code> object back from raw data,
     using the specific message representation to interpret the byte
     sequence.
     @param data The byte sequence containing the encoded message.
     @return A new <code>ACLMessage</code> object, built from the raw
     data.
     @exception CodecException If some kind of syntax error occurs.
   */
  ACLMessage decode(byte[] data) throws CodecException;

  /**
     Query the name of the message representation handled by this
     <code>Codec</code> object. The FIPA standard representations have
     a name starting with <code><b>"fipa.acl.rep."</b></code>.
     @return The name of the handled ACL message representation.
   */
  String getName();

}
