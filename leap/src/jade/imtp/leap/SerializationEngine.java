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

package jade.imtp.leap;

import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.domain.FIPAAgentManagement.Envelope;
import jade.util.leap.Properties;
import jade.util.leap.Iterator;
import jade.util.Logger;

import java.util.Date;
import java.util.Enumeration;
import java.io.*;

/**
 * Transform commands to/from sequences of bytes
 * @author Giovanni Caire - TILAB
 */
class SerializationEngine {
	private static final byte NULL_ID = 0;
	private static final byte STRING_ID = 1;
	private static final byte ACL_ID = 2;
	private static final byte STRING_ARRAY_ID = 3;
	private static final byte BOOLEAN_ID = 4;
	
	final static byte[] serialize(Command cmd) throws LEAPSerializationException {
  	ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
    try {
      dos.writeByte(cmd.getCode());
      int paramCnt = cmd.getParamCnt();
      dos.writeByte(paramCnt);
      for (int i = 0; i < paramCnt; ++i) {
      	serializeObject(cmd.getParamAt(i), dos);
      }
      byte[] bb = baos.toByteArray();
      //Logger.println("Serialized command. Type = "+cmd.getCode()+". Length = "+(bb != null ? bb.length : 0));
      return bb;
    } 
    catch (IOException ioe) {
      throw new LEAPSerializationException("Error serializing Command");
    }
	}

	final static Command deserialize(byte[] data) throws LEAPSerializationException {
  	DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data));
    try {
      Command cmd = new Command((int) dis.readByte());
      int paramCnt = (int) dis.readByte();
      for (int i = 0; i < paramCnt; ++i) {
        cmd.addParam(deserializeObject(dis));
      } 
      //Logger.println("De-serialized command. Type = "+cmd.getCode()+". Length = "+(data != null ? data.length : 0));
      return cmd;
    } 
    catch (IOException ioe) {
      throw new LEAPSerializationException("Error deserializing Command");
    }
	}
	
  /**
   * Writes an object whose class is not known from the context to
   * a given DataOutputStream.
   * @param o the object to be written.
   * @param dos the DataOutputStream.
   * @exception LEAPSerializationException if an error occurs during
   * serialization or the object is an instance of a class that cannot be
   * serialized.
   */
  private final static void serializeObject(Object o, DataOutputStream dos) throws LEAPSerializationException {
    try {
      if (o != null) {
        if (o instanceof String) {            // String
            dos.writeByte(STRING_ID);
            dos.writeUTF((String) o);
        } 
        else if (o instanceof ACLMessage) {   // ACLMessage
            dos.writeByte(ACL_ID);
            serializeACL((ACLMessage) o, dos);
        } 
        else if (o instanceof String[]) {     // Array of Strings
            dos.writeByte(STRING_ARRAY_ID);
            serializeStringArray((String[]) o, dos);
        } 
        else if (o instanceof Boolean) {      // Boolean
            dos.writeByte(BOOLEAN_ID);
            dos.writeBoolean(((Boolean) o).booleanValue());
        } 
        else {
        	throw new LEAPSerializationException("Unknown class "+o.getClass().getName());
        }
      }
      else {
      	dos.writeByte(NULL_ID);
      }
    }  // END of try
    catch (IOException ioe) {
    	throw new LEAPSerializationException("I/O Error Serializing object "+o+". "+ioe.getMessage());
    } 
  }
  
  /**
   * Reads an object whose class is not known from the context from
   * a given DataInputStream.
   * @param dis The DataInputStream.
   * @return the object that has been read.
   * @exception LEAPSerializationException if an error occurs during
   * deserialization or the object is an instance of a class that cannot be
   * deserialized.
   */
  private final static Object deserializeObject(DataInputStream dis) throws LEAPSerializationException {
    String className = null;    
    try {
      byte id = dis.readByte();
      switch (id) {
      case NULL_ID:
      	return null;
      case STRING_ID:
      	return dis.readUTF();
      case ACL_ID:
      	return deserializeACL(dis);
      case STRING_ARRAY_ID:
      	return deserializeStringArray(dis);
      case BOOLEAN_ID:
      	return new Boolean(dis.readBoolean());
      default:
      	throw new LEAPSerializationException("Unknown class ID: "+id);
      }
    }      // END of try
    catch (IOException e) {
    	throw new LEAPSerializationException("I/O Error Deserializing a generic object");
    } 
  }
  
  /**
   */
  private final static void serializeACL(ACLMessage msg, DataOutputStream dos) throws IOException, LEAPSerializationException {
    dos.writeByte(msg.getPerformative());

    byte presence1 = 0;
    byte presence2 = 0;
    AID sender = msg.getSender();
    String language = msg.getLanguage();
    String ontology = msg.getOntology();
    String encoding = msg.getEncoding();
    String protocol = msg.getProtocol();
    String conversationId = msg.getConversationId();
    String inReplyTo = msg.getInReplyTo();
    String replyWith = msg.getReplyWith();
    Date replyBy = msg.getReplyByDate();
    Envelope envelope = null;
		//#CUSTOM_EXCLUDE_BEGIN
		envelope = msg.getEnvelope();
    Properties props = msg.getAllUserDefinedParameters();
    if (props.size() > 63) {
    	throw new LEAPSerializationException("Cannot serialize more than 63 params");
    }
		//#CUSTOM_EXCLUDE_END

    if (sender != null) { presence1 |= 0x80; }
    if (language != null) { presence1 |= 0x40; }
    if (ontology != null) { presence1 |= 0x20; }
    if (encoding != null) { presence1 |= 0x10; }
    if (protocol != null) { presence1 |= 0x08; }
    if (conversationId != null) { presence1 |= 0x04; }
    if (inReplyTo != null) { presence1 |= 0x02; }
    if (replyWith != null) { presence1 |= 0x01; }
    if (replyBy != null) { presence2 |= 0x80; }
    if (envelope != null) { presence2 |= 0x40; }
		//#CUSTOM_EXCLUDE_BEGIN
    presence2 |= (props.size() & 0x3F);
		//#CUSTOM_EXCLUDE_END
    dos.writeByte(presence1);
    dos.writeByte(presence2);

    if (sender != null) { serializeAID(sender, dos); }
    if (language != null) { dos.writeUTF(language); }
    if (ontology != null) { dos.writeUTF(ontology); }
    if (encoding != null) { dos.writeUTF(encoding); }
    if (protocol != null) { dos.writeUTF(protocol); }
    if (conversationId != null) { dos.writeUTF(conversationId); }
    if (inReplyTo != null) { dos.writeUTF(inReplyTo); }
    if (replyWith != null) { dos.writeUTF(replyWith); }
    if (replyBy != null) { dos.writeLong(replyBy.getTime()); }
//#CUSTOM_EXCLUDE_BEGIN
    if (envelope != null) { serializeEnvelope(envelope, dos); }

    // User defined parameters
    serializeProperties(props, dos);
//#CUSTOM_EXCLUDE_END    
    // Receivers
    Iterator it = msg.getAllReceiver();
    while (it.hasNext()) {
      dos.writeBoolean(true);
      serializeAID((AID) it.next(), dos);
    } 
    dos.writeBoolean(false);

    // Reply-to
    it = msg.getAllReplyTo();
    while (it.hasNext()) {
      dos.writeBoolean(true);
      serializeAID((AID) it.next(), dos);
    } 
    dos.writeBoolean(false);
    
    // Content
    if (msg.hasByteSequenceContent()) {
    	// Content present in bynary form
      dos.writeByte(2);
      byte[] content = msg.getByteSequenceContent();
      dos.writeInt(content.length);
      dos.write(content, 0, content.length);
    } 
    else {
      String content = msg.getContent();
      if (content != null) { 
    		// Content present in String form
      	dos.writeByte(1);
      	dos.writeUTF(content);
      }
      else {
    		// Content NOT present
      	dos.writeByte(0);
      }
    } 
  }

  /**
   */
  private final static ACLMessage deserializeACL(DataInputStream dis) throws IOException, LEAPSerializationException {
  	ACLMessage msg = new ACLMessage((int) dis.readByte());

    byte presence1 = dis.readByte();
    byte presence2 = dis.readByte();
    
    if ((presence1 & 0x80) != 0) { msg.setSender(deserializeAID(dis)); }
    if ((presence1 & 0x40) != 0) { msg.setLanguage(dis.readUTF()); }
    if ((presence1 & 0x20) != 0) { msg.setOntology(dis.readUTF()); }
    if ((presence1 & 0x10) != 0) { msg.setEncoding(dis.readUTF()); }
    if ((presence1 & 0x08) != 0) { msg.setProtocol(dis.readUTF()); }
    if ((presence1 & 0x04) != 0) { msg.setConversationId(dis.readUTF()); }
    if ((presence1 & 0x02) != 0) { msg.setInReplyTo(dis.readUTF()); }
    if ((presence1 & 0x01) != 0) { msg.setReplyWith(dis.readUTF()); }
    if ((presence2 & 0x80) != 0) { msg.setReplyByDate(new Date(dis.readLong())); }
		//#CUSTOM_EXCLUDE_BEGIN
    if ((presence2 & 0x40) != 0) { msg.setEnvelope(deserializeEnvelope(dis)); }
    // User defined properties
    int propsSize = presence2 & 0x3F;
    for (int i = 0; i < propsSize; ++i) {
    	String key = dis.readUTF();
    	String val = dis.readUTF();
    	msg.addUserDefinedParameter(key, val);
    }
		//#CUSTOM_EXCLUDE_END
    
    // Receivers
    while (dis.readBoolean()) {
    	msg.addReceiver(deserializeAID(dis));
    } 

    // Reply-to
    while (dis.readBoolean()) {
    	msg.addReplyTo(deserializeAID(dis));
    }

    // Content
    byte type = dis.readByte();
    if (type == 2) {
    	// Content present in bynary form
      byte[] content = new byte[dis.readInt()];
      dis.read(content, 0, content.length);
      msg.setByteSequenceContent(content);
    }
    else if (type == 1) {
    	// Content present in String form
    	msg.setContent(dis.readUTF());
    }
        
    return msg;
  }

  private final static void serializeAID(AID id, DataOutputStream dos) throws IOException, LEAPSerializationException {
    byte presence = 0;
    String name = id.getName();
    Iterator addresses = id.getAllAddresses();
		//#CUSTOM_EXCLUDE_BEGIN
    Iterator resolvers = id.getAllResolvers();
    Properties props = id.getAllUserDefinedSlot();
    if (props.size() > 31) {
    	throw new LEAPSerializationException("Cannot serialize more than 31 slots");
    }
		//#CUSTOM_EXCLUDE_END
    if (name != null) { presence |= 0x80; }
    if (addresses.hasNext()) { presence |= 0x40; }
		//#CUSTOM_EXCLUDE_BEGIN
    if (resolvers.hasNext()) { presence |= 0x20; }
    presence |= (props.size() & 0x1F);
		//#CUSTOM_EXCLUDE_END
    dos.writeByte(presence);
    
    if (name != null) { dos.writeUTF(name); }
    // Addresses
    while (addresses.hasNext()) {
    	dos.writeUTF((String) addresses.next());
    	dos.writeBoolean(addresses.hasNext());
    }
		//#CUSTOM_EXCLUDE_BEGIN
    // Resolvers
    while (resolvers.hasNext()) {
    	serializeAID((AID) resolvers.next(), dos);
    	dos.writeBoolean(resolvers.hasNext());
    }
    // User defined slots
    serializeProperties(props, dos);
		//#CUSTOM_EXCLUDE_END
  }
  
  private final static AID deserializeAID(DataInputStream dis) throws IOException, LEAPSerializationException {
    byte presence = dis.readByte();
    AID id = ((presence & 0x80) != 0 ? new AID(dis.readUTF(), AID.ISGUID) : new AID());
    
    // Addresses
    if ((presence & 0x40) != 0) {
    	do {
    		id.addAddresses(dis.readUTF());
    	} while (dis.readBoolean());
    }
    //#CUSTOM_EXCLUDE_BEGIN
    // Resolvers
    if ((presence & 0x20) != 0) {
    	do {
    		id.addResolvers(deserializeAID(dis));
    	} while (dis.readBoolean());
    }
    
    // User defined slots
    int propsSize = presence & 0x1F;
    for (int i = 0; i < propsSize; ++i) {
    	String key = dis.readUTF();
    	String val = dis.readUTF();
    	id.addUserDefinedSlot(key, val);
    }
    //#CUSTOM_EXCLUDE_END
  	return id;
  }
  
  private final static void serializeStringArray(String[] ss, DataOutputStream dos) throws IOException, LEAPSerializationException {
  	dos.writeByte(ss.length);
  	for (int i = 0; i < ss.length; ++i) {
  		dos.writeUTF(ss[i]);
  	}
  }
  
  private final static String[] deserializeStringArray(DataInputStream dis) throws IOException, LEAPSerializationException {
  	String[] ss = new String[dis.readByte()];
  	for (int i = 0; i < ss.length; ++i) {
  		ss[i] = dis.readUTF();
  	}
  	return ss;
  }

//#CUSTOM_EXCLUDE_BEGIN
  private final static void serializeEnvelope(Envelope env, DataOutputStream dos) throws IOException, LEAPSerializationException {
  	System.out.println("SerializationEngine.serializeEnvelope() not yet implemented");
  	// FIXME: To be implemented
  }
  
  private final static Envelope deserializeEnvelope(DataInputStream dis) throws IOException, LEAPSerializationException {
  	System.out.println("SerializationEngine.deserializeEnvelope() not yet implemented");
  	// FIXME: To be implemented
  	return null;
  }
  
  private static final void serializeProperties(Properties props, DataOutputStream dos) throws IOException, LEAPSerializationException {
    Enumeration e = props.keys();
    while (e.hasMoreElements()) {
    	String key = (String) e.nextElement();
    	dos.writeUTF(key);
    	dos.writeUTF(props.getProperty(key));
    }
  }  
//#CUSTOM_EXCLUDE_END

}

