/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop 
multi-agent systems in compliance with the FIPA specifications.
Copyright (C) 2000 CSELT S.p.A. 

The updating of this file to JADE 2.0 has been partially supported by the IST-1999-10211 LEAP Project

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

package jade.mtp.iiop;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Date;
import java.util.Calendar;

import org.omg.CORBA.*;

import FIPA.*; // OMG IDL Stubs

import jade.core.AID;

import jade.mtp.InChannel;
import jade.mtp.OutChannel;
import jade.mtp.MTP;
import jade.mtp.MTPException;
import jade.mtp.TransportAddress;
import jade.domain.FIPAAgentManagement.Envelope;
import jade.domain.FIPAAgentManagement.ReceivedObject;


/**
   Implementation of <code><b>fipa.mts.mtp.iiop.std</b></code>
   specification for delivering ACL messages over the OMG IIOP
   transport protocol.

   @author Giovanni Rimassa - Universita` di Parma
   @version $Date$ $Revision$
 */
public class MessageTransportProtocol implements MTP {

  private static class MTSImpl extends FIPA._MTSImplBase {

    private InChannel.Dispatcher dispatcher;

    public MTSImpl(InChannel.Dispatcher disp) {
      dispatcher = disp;
    }

    public void message(FipaMessage aFipaMessage) {
      FIPA.Envelope[] envelopes = aFipaMessage.messageEnvelopes;
      byte[] payload = aFipaMessage.messageBody;
      
      Envelope env = new Envelope();

      // Read all the envelopes sequentially, so that later slots
      // overwrite earlier ones.
      for(int e = 0; e < envelopes.length; e++) {
	FIPA.Envelope IDLenv = envelopes[e];

	// Read in the 'to' slot
	if(IDLenv.to.length > 0)
	  env.clearAllTo();
	for(int i = 0; i < IDLenv.to.length; i++) {
	  AID id = unmarshalAID(IDLenv.to[i]);
	  env.addTo(id);
	}

	// Read in the 'from' slot
	if(IDLenv.from.length > 0) {
	  AID id = unmarshalAID(IDLenv.from[0]);
	  env.setFrom(id);
	}

	// Read in the 'intended-receiver' slot
	if(IDLenv.intendedReceiver.length > 0)
	  env.clearAllIntendedReceiver();
	for(int i = 0; i < IDLenv.intendedReceiver.length; i++) {
	  AID id = unmarshalAID(IDLenv.intendedReceiver[i]);
	  env.addIntendedReceiver(id);
	}

	// Read in the 'encrypted' slot
	if(IDLenv.encrypted.length > 0)
	  env.clearAllEncrypted();
	for(int i = 0; i < IDLenv.encrypted.length; i++) {
	  String word = IDLenv.encrypted[i];
	  env.addEncrypted(word);
	}

	// Read in the other slots
	if(IDLenv.comments.length() > 0)
	  env.setComments(IDLenv.comments);
	if(IDLenv.aclRepresentation.length() > 0)
	  env.setAclRepresentation(IDLenv.aclRepresentation);
	if(IDLenv.payloadLength > 0)
	  env.setPayloadLength(new Long(IDLenv.payloadLength));
	if(IDLenv.payloadEncoding.length() > 0)
	  env.setPayloadEncoding(IDLenv.payloadEncoding);
	if(IDLenv.date.length > 0) {
	  Date d = unmarshalDateTime(IDLenv.date[0]);
	  env.setDate(d);
	}

	// Read in the 'received' stamp
	if(IDLenv.received.length > 0)
	  env.addStamp(unmarshalReceivedObj(IDLenv.received[0]));

	// FIXME: need to unmarshal user properties

      }

      // Dispatch the message
      dispatcher.dispatchMessage(env, payload);

    }


    private AID unmarshalAID(FIPA.AgentID id) {
      AID result = new AID();
      result.setName(id.name);
      for(int i = 0; i < id.addresses.length; i++)
	result.addAddresses(id.addresses[i]);
      for(int i = 0; i < id.resolvers.length; i++)
	result.addResolvers(unmarshalAID(id.resolvers[i]));
      return result;
    }

    private Date unmarshalDateTime(FIPA.DateTime d) {
      Date result = new Date();
      return result;
    }

    private ReceivedObject unmarshalReceivedObj(FIPA.ReceivedObject ro) {
      ReceivedObject result = new ReceivedObject();
      result.setBy(ro.by);
      result.setFrom(ro.from);
      result.setDate(unmarshalDateTime(ro.date));
      result.setId(ro.id);
      result.setVia(ro.via);
      return result;
    }

  } // End of MTSImpl class


  private ORB myORB;
  private MTSImpl server;

  public MessageTransportProtocol() {
    myORB = ORB.init(new String[0], null);
  }

  public TransportAddress activate(InChannel.Dispatcher disp) throws MTPException {
    server = new MTSImpl(disp);
    myORB.connect(server);
    return new IIOPAddress(myORB, server);
  }

  public void activate(InChannel.Dispatcher disp, TransportAddress ta) throws MTPException {
    throw new MTPException("User supplied transport address not supported.");
  }

  public void deactivate(TransportAddress ta) throws MTPException {
    myORB.disconnect(server);
  }

  public void deactivate() throws MTPException {
    myORB.disconnect(server);
  }

  public void deliver(String addr, Envelope env, byte[] payload) throws MTPException {
    try {
      TransportAddress ta = strToAddr(addr);
      IIOPAddress iiopAddr = (IIOPAddress)ta;
      FIPA.MTS objRef = iiopAddr.getObject();

      // Fill in the 'to' field of the IDL envelope
      Iterator itTo = env.getAllTo();
      List to = new ArrayList();
      while(itTo.hasNext()) {
	AID id = (AID)itTo.next();
	to.add(marshalAID(id));
      }

      FIPA.AgentID[] IDLto = new FIPA.AgentID[to.size()];
      for(int i = 0; i < to.size(); i++)
	IDLto[i] = (FIPA.AgentID)to.get(i);


      // Fill in the 'from' field of the IDL envelope
      AID from = env.getFrom();
      FIPA.AgentID[] IDLfrom = new FIPA.AgentID[] { marshalAID(from) };


      // Fill in the 'intended-receiver' field of the IDL envelope
      Iterator itIntendedReceiver = env.getAllIntendedReceiver();
      List intendedReceiver = new ArrayList();
      while(itIntendedReceiver.hasNext()) {
	AID id = (AID)itIntendedReceiver.next();
	intendedReceiver.add(marshalAID(id));
      }

      FIPA.AgentID[] IDLintendedReceiver = new FIPA.AgentID[intendedReceiver.size()];
      for(int i = 0; i < intendedReceiver.size(); i++)
	IDLintendedReceiver[i] = (FIPA.AgentID)intendedReceiver.get(i);


      // Fill in the 'encrypted' field of the IDL envelope
      Iterator itEncrypted = env.getAllEncrypted();
      List encrypted = new ArrayList();
      while(itEncrypted.hasNext()) {
	String word = (String)itEncrypted.next();
	encrypted.add(word);
      }

      String[] IDLencrypted = new String[encrypted.size()];
      for(int i = 0; i < encrypted.size(); i++)
	IDLencrypted[i] = (String)encrypted.get(i);


      // Fill in the other fields of the IDL envelope ...
      String IDLcomments = env.getComments();
      String IDLaclRepresentation = env.getAclRepresentation();
      Long payloadLength = env.getPayloadLength();
      int IDLpayloadLength = payloadLength.intValue();
      String IDLpayloadEncoding = env.getPayloadEncoding();
      FIPA.DateTime[] IDLdate = new FIPA.DateTime[] { marshalDateTime(env.getDate()) };
      FIPA.Property[][] IDLtransportBehaviour = new FIPA.Property[][] { };
      FIPA.Property[] IDLuserDefinedProperties = new FIPA.Property[] { }; // FIXME: need to marshal user properties

      // Fill in the list of 'received' stamps
      /* FIXME: Maybe several IDL Envelopes should be generated, one for every 'received' stamp...
      ReceivedObject[] received = env.getStamps();
      FIPA.ReceivedObject[] IDLreceived = new FIPA.ReceivedObject[received.length];
      for(int i = 0; i < received.length; i++)
	IDLreceived[i] = marshalReceivedObj(received[i]);
      */

      // FIXME: For now, only the current 'received' object is considered...
      ReceivedObject received = env.getReceived();
      FIPA.ReceivedObject[] IDLreceived;
      if(received != null)
	IDLreceived = new FIPA.ReceivedObject[] { marshalReceivedObj(received) };
      else
	IDLreceived = new FIPA.ReceivedObject[] { };

      FIPA.Envelope IDLenv = new FIPA.Envelope(IDLto,
					       IDLfrom,
					       IDLcomments,
					       IDLaclRepresentation,
					       IDLpayloadLength,
					       IDLpayloadEncoding,
					       IDLdate,
					       IDLencrypted,
					       IDLintendedReceiver,
					       IDLreceived,
					       IDLtransportBehaviour,
					       IDLuserDefinedProperties);

      FipaMessage msg = new FipaMessage(new FIPA.Envelope[] { IDLenv }, payload);
      objRef.message(msg);
    }
    catch(ClassCastException cce) {
      cce.printStackTrace();
      throw new MTPException("Address mismatch: this is not a valid IIOP address.");
    }

  }

  public TransportAddress strToAddr(String rep) throws MTPException {
    return new IIOPAddress(myORB, rep);
  }

  public String addrToStr(TransportAddress ta) throws MTPException {
    try {
      IIOPAddress addr = (IIOPAddress)ta;
      return addr.getIOR();
    }
    catch(ClassCastException cce) {
      throw new MTPException("Address mismatch: this is not a valid IIOP address.");
    }
  }

  public String getName() {
    return "iiop";
  }

  private FIPA.AgentID marshalAID(AID id) {
    String name = id.getName();
    String[] addresses = id.getAddressesArray();
    AID[] resolvers = id.getResolversArray();
    FIPA.Property[] userDefinedProperties = new FIPA.Property[] { };
    int numOfResolvers = resolvers.length;
    FIPA.AgentID result = new FIPA.AgentID(name, addresses, new AgentID[numOfResolvers], userDefinedProperties);
    for(int i = 0; i < numOfResolvers; i++) {
      result.resolvers[i] = marshalAID(resolvers[i]); // Recursively marshal all resolvers, which are, in turn, AIDs.
    }

    return result;

  }

  private FIPA.DateTime marshalDateTime(Date d) {
    Calendar cal = Calendar.getInstance();
    cal.setTime(d);
    short year = (short)cal.get(Calendar.YEAR);
    short month = (short)cal.get(Calendar.MONTH);
    short day = (short)cal.get(Calendar.DAY_OF_MONTH);
    short hour = (short)cal.get(Calendar.HOUR_OF_DAY);
    short minutes = (short)cal.get(Calendar.MINUTE);
    short seconds = (short)cal.get(Calendar.SECOND);
    short milliseconds = 0; // FIXME: This is truncated to the second
    char typeDesignator = ' '; // FIXME: Uses local timezone ?
    FIPA.DateTime result = new FIPA.DateTime(year,
					     month,
					     day,
					     hour,
					     minutes,
					     seconds,
					     milliseconds,
					     typeDesignator);
    return result;
  }

  private FIPA.ReceivedObject marshalReceivedObj(ReceivedObject ro) {
    FIPA.ReceivedObject result = new FIPA.ReceivedObject();
    result.by = ro.getBy();
    result.from = ro.getFrom();
    result.date = marshalDateTime(ro.getDate());
    result.id = ro.getId();
    result.via = ro.getVia();
    return result;
  }

} // End of class MessageTransportProtocol

/**
This class represents an IIOP address.
Three syntaxes are allowed for an IIOP address (all case-insensitive):
<code>
IIOPAddress ::= "ior:" (HexDigit HexDigit+)
              | "iiop://" "ior:" (HexDigit HexDigit)+
              | "iiop://" HostName ":" portNumber "/" objectKey
ObjectKey = WORD 
</code>
Notice that, in the third case, BIG_ENDIAN is assumed by default. In the first and second case, instead, the indianess information is contained within the IOR definition. 
**/
  class IIOPAddress implements TransportAddress {

    public static final byte BIG_ENDIAN = 0;
    public static final byte LITTLE_ENDIAN = 1;

    private static final String TYPE_ID = "IDL:FIPA/MTS:1.0";
    private static final int TAG_INTERNET_IOP = 0;
    private static final byte IIOP_MAJOR = 1;
    private static final byte IIOP_MINOR = 0;

    private ORB orb;

    private String ior;
    private String host;
    private short port;
    private String objectKey;

    private CDRCodec codecStrategy;

    public IIOPAddress(ORB anOrb, FIPA.MTS objRef) throws MTPException {
      orb = anOrb;
      String s = orb.object_to_string(objRef);
      if(s.toLowerCase().startsWith("ior:"))
	initFromIOR(s);
      else if(s.toLowerCase().startsWith("iiop:"))
	initFromURL(s, BIG_ENDIAN);
      else if(s.toLowerCase().startsWith("corbaloc:"))
	initFromURL(s, BIG_ENDIAN);
      else
	throw new MTPException("Invalid string prefix");
    }

    public IIOPAddress(ORB anOrb, String s) throws MTPException {
      orb = anOrb;
      if(s.toLowerCase().startsWith("ior:"))
	initFromIOR(s);
      else if(s.toLowerCase().startsWith("iiop:"))
	initFromURL(s, BIG_ENDIAN);
      else if(s.toLowerCase().startsWith("corbaloc:"))
	initFromURL(s, BIG_ENDIAN);
      else
	throw new MTPException("Invalid string prefix");
    }

    
    private void initFromIOR(String s) throws MTPException {

      // Store stringified IOR
      ior = new String(s.toUpperCase());

      // Remove 'IOR:' prefix to get Hex digits
      String hexString = ior.substring(4);

      short endianness = Short.parseShort(hexString.substring(0, 2), 16);

      switch(endianness) {
      case BIG_ENDIAN:
	codecStrategy = new BigEndianCodec(hexString);
	break;
      case LITTLE_ENDIAN:
	codecStrategy = new LittleEndianCodec(hexString);
	break;
      default:
	throw new MTPException("Invalid endianness specifier");
      }
      // Read 'string type_id' field
      String typeID = codecStrategy.readString();
      if(!typeID.equalsIgnoreCase(TYPE_ID))
	throw new MTPException("Invalid type ID" + typeID);

      // Read 'sequence<TaggedProfile> profiles' field
      // Read sequence length
      int seqLen = codecStrategy.readLong();
      for(int i = 0; i < seqLen; i++) {
	// Read 'ProfileId tag' field
	int tag = codecStrategy.readLong();
	byte[] profile = codecStrategy.readOctetSequence();
	if(tag == TAG_INTERNET_IOP) {
	  // Process IIOP profile
	  CDRCodec profileBodyCodec;
	  switch(profile[0]) {
	  case BIG_ENDIAN:
	    profileBodyCodec = new BigEndianCodec(profile);
	    break;
	  case LITTLE_ENDIAN:
	    profileBodyCodec = new LittleEndianCodec(profile);
	    break;
	  default:
	    throw new MTPException("Invalid endianness specifier");
	  }

	  // Read IIOP version
	  byte versionMajor = profileBodyCodec.readOctet();
	  byte versionMinor = profileBodyCodec.readOctet();
	  if(versionMajor != 1)
	    throw new MTPException("IIOP version not supported");

	  // Read 'string host' field
	  host = profileBodyCodec.readString();

	  // Read 'unsigned short port' field
	  port = profileBodyCodec.readShort();

	  // Read 'sequence<octet> object_key' field and convert it
	  // into a String object
	  byte[] keyBuffer = profileBodyCodec.readOctetSequence();
	  objectKey = new String(keyBuffer);

	  codecStrategy = null;

	}
      }
    }

    private void initFromURL(String s, short endianness) throws MTPException {

      // Remove 'iiop://' prefix to get URL host, port and file
      s = s.substring(7);
      int colonPos = s.indexOf(':');
      int slashPos = s.indexOf('/');
      if((colonPos == -1) || (slashPos == -1))
	throw new MTPException("Invalid URL string");

      host = new String(s.substring(0, colonPos));
      port = Short.parseShort(s.substring(colonPos + 1, slashPos));
      objectKey = new String(s.substring(slashPos + 1, s.length()));

      switch(endianness) {
      case BIG_ENDIAN:
	codecStrategy = new BigEndianCodec(new byte[0]);
	break;
      case LITTLE_ENDIAN:
	codecStrategy = new LittleEndianCodec(new byte[0]);
	break;
      default:
	throw new MTPException("Invalid endianness specifier");
      }

      codecStrategy.writeString(TYPE_ID);

      // Write '1' as profiles sequence length
      codecStrategy.writeLong(1);

      codecStrategy.writeLong(TAG_INTERNET_IOP);
      CDRCodec profileBodyCodec;
      switch(endianness) {
      case BIG_ENDIAN:
	profileBodyCodec = new BigEndianCodec(new byte[0]);
	break;
      case LITTLE_ENDIAN:
	profileBodyCodec = new LittleEndianCodec(new byte[0]);
	break;
      default:
	throw new MTPException("Invalid endianness specifier");
      }

      // Write IIOP 1.0 profile to auxiliary CDR codec
      profileBodyCodec.writeOctet(IIOP_MAJOR);
      profileBodyCodec.writeOctet(IIOP_MINOR);
      profileBodyCodec.writeString(host);
      profileBodyCodec.writeShort(port);
      byte[] objKey = objectKey.getBytes();
      profileBodyCodec.writeOctetSequence(objKey);

      byte[] encapsulatedProfile = profileBodyCodec.writtenBytes();

      // Write encapsulated profile to main IOR codec
      codecStrategy.writeOctetSequence(encapsulatedProfile);

      String hexString = codecStrategy.writtenString();
      ior = "IOR:" + hexString;

      codecStrategy = null;
    }

    public String getURL() {
      int portNum = port;
      if(portNum < 0)
	portNum += 65536;
      return "iiop://" + host + ":" + portNum + "/" + objectKey;
    }

    public String getIOR() {
      return ior;
    }

    public FIPA.MTS getObject() {
      return FIPA.MTSHelper.narrow(orb.string_to_object(ior));
    }

    private static abstract class CDRCodec {

      private static final char[] HEX = {
	'0','1','2','3','4','5','6','7',
	'8','9','a','b','c','d','e','f'
      };

      protected byte[] readBuffer;
      protected StringBuffer writeBuffer;
      protected int readIndex = 0;
      protected int writeIndex = 0;

      protected CDRCodec(String hexString) {
	// Put all Hex digits into a byte array
	readBuffer = bytesFromHexString(hexString);
	readIndex = 1;
	writeBuffer = new StringBuffer(255);
      }

      protected CDRCodec(byte[] hexDigits) {
	readBuffer = new byte[hexDigits.length];
	System.arraycopy(hexDigits, 0, readBuffer, 0, readBuffer.length);
	readIndex = 1;
	writeBuffer = new StringBuffer(255);
      }

      public String writtenString() {
	return new String(writeBuffer);
      }

      public byte[] writtenBytes() {
	return bytesFromHexString(new String(writeBuffer));
      }

      public byte readOctet() {
	return readBuffer[readIndex++];
      }

      public byte[] readOctetSequence() {
	int seqLen = readLong();
	byte[] result = new byte[seqLen];
	System.arraycopy(readBuffer, readIndex, result, 0, seqLen);
	readIndex += seqLen;
	return result;
      }

      public String readString() {
	int strLen = readLong(); // This includes '\0' terminator
	String result = new String(readBuffer, readIndex, strLen - 1);
	readIndex += strLen;
	return result;
      }

      // These depend on endianness, so are deferred to subclasses
      public abstract short readShort();   // 16 bits
      public abstract int readLong();      // 32 bits
      public abstract long readLongLong(); // 64 bits

      // Writes a couple of hexadecimal digits representing the given byte.
      // All other marshalling operations ultimately use this method to modify
      // the write buffer
      public void writeOctet(byte b) {
	char[] digits = new char[2];
	digits[0] = HEX[(b & 0xF0) >> 4]; // High nibble
	digits[1] = HEX[b & 0x0F]; // Low nibble
	writeBuffer.append(digits);
	writeIndex++;
      }

      public void writeOctetSequence(byte[] seq) {
	int seqLen = seq.length;
	writeLong(seqLen);
	for(int i = 0; i < seqLen; i++)
	  writeOctet(seq[i]);
      }

      public void writeString(String s) {
	int strLen = s.length() + 1; // This includes '\0' terminator
	writeLong(strLen);
	byte[] bytes = s.getBytes();
	for(int i = 0; i < s.length(); i++)
	  writeOctet(bytes[i]);
	writeOctet((byte)0x00);
      }

      // These depend on endianness, so are deferred to subclasses
      public abstract void writeShort(short s);   // 16 bits
      public abstract void writeLong(int i);      // 32 bits
      public abstract void writeLongLong(long l); // 64 bits

      protected void setReadAlignment(int align) {
	while((readIndex % align) != 0)
	  readIndex++;
      }

      protected void setWriteAlignment(int align) {
	while(writeIndex % align != 0)
	  writeOctet((byte)0x00);
      }

      private byte[] bytesFromHexString(String hexString) {
	int hexLen = hexString.length() / 2;
	byte[] result = new byte[hexLen];

	for(int i = 0; i < hexLen; i ++) {
	  String currentDigit = hexString.substring(2*i, 2*(i + 1));
	  Short s = Short.valueOf(currentDigit, 16);
	  result[i] = s.byteValue();
	}

	return result;
      }

    } // End of CDRCodec class

    private static class BigEndianCodec extends CDRCodec {

      public BigEndianCodec(String ior) {
	super(ior);
	writeOctet((byte)0x00); // Writes 'Big Endian' magic number
      }

      public BigEndianCodec(byte[] hexDigits) {
	super(hexDigits);
	writeOctet((byte)0x00); // Writes 'Big Endian' magic number
      }

      public short readShort() {
	setReadAlignment(2);
	short result = (short)((readBuffer[readIndex++] << 8) + readBuffer[readIndex++]);
	return result;
      }

      public int readLong() {
	setReadAlignment(4);
	int result = (readBuffer[readIndex++] << 24) + (readBuffer[readIndex++] << 16);
	result += (readBuffer[readIndex++] << 8) + readBuffer[readIndex++];
	return result;
      }

      public long readLongLong() {
	setReadAlignment(8);
	long result = (readBuffer[readIndex++] << 56) + (readBuffer[readIndex++] << 48);
	result += (readBuffer[readIndex++] << 40) + (readBuffer[readIndex++] << 32);
	result += (readBuffer[readIndex++] << 24) + (readBuffer[readIndex++] << 16);
	result += (readBuffer[readIndex++] << 8) + readBuffer[readIndex++];
	return result;
      }

      public void writeShort(short s) {
	setWriteAlignment(2);
	writeOctet((byte)((s & 0xFF00) >> 8));
	writeOctet((byte)(s & 0x00FF));
      }

      public void writeLong(int i) {
	setWriteAlignment(4);
	writeOctet((byte)((i & 0xFF000000) >> 24));
	writeOctet((byte)((i & 0x00FF0000) >> 16));
	writeOctet((byte)((i & 0x0000FF00) >> 8));
	writeOctet((byte)(i & 0x000000FF));
      }

      public void writeLongLong(long l) {
	setWriteAlignment(8);
	writeOctet((byte)((l & 0xFF00000000000000L) >> 56));
	writeOctet((byte)((l & 0x00FF000000000000L) >> 48));
	writeOctet((byte)((l & 0x0000FF0000000000L) >> 40));
	writeOctet((byte)((l & 0x000000FF00000000L) >> 32));
	writeOctet((byte)((l & 0x00000000FF000000L) >> 24));
	writeOctet((byte)((l & 0x0000000000FF0000L) >> 16));
	writeOctet((byte)((l & 0x000000000000FF00L) >> 8));
	writeOctet((byte)(l & 0x00000000000000FFL));
      }

    } // End of BigEndianCodec class

    private static class LittleEndianCodec extends CDRCodec {

      public LittleEndianCodec(String ior) {
	super(ior);
	writeOctet((byte)0x01); // Writes 'Little Endian' magic number
      }

      public LittleEndianCodec(byte[] hexDigits) {
	super(hexDigits);
	writeOctet((byte)0x01); // Writes 'Little Endian' magic number
      }

      public short readShort() {
	setReadAlignment(2);
	short result = (short)(readBuffer[readIndex++] + (readBuffer[readIndex++] << 8));
	return result;
      }

      public int readLong() {
	setReadAlignment(4);
	int result = readBuffer[readIndex++] + (readBuffer[readIndex++] << 8) + (readBuffer[readIndex++] << 16) + (readBuffer[readIndex++] << 24);
	return result;
      }

      public long readLongLong() {
	setReadAlignment(8);
	long result = readBuffer[readIndex++] + (readBuffer[readIndex++] << 8);
	result += (readBuffer[readIndex++] << 16) + (readBuffer[readIndex++] << 24);
	result += (readBuffer[readIndex++] << 32) + (readBuffer[readIndex++] << 40);
	result += (readBuffer[readIndex++] << 48) + (readBuffer[readIndex++] << 56);
	return result;
      }

      public void writeShort(short s) {
	setWriteAlignment(2);
	writeOctet((byte)(s & 0x00FF));
	writeOctet((byte)((s & 0xFF00) >> 8));
      }

      public void writeLong(int i) {
	setWriteAlignment(4);
	writeOctet((byte)(i & 0x000000FF));
	writeOctet((byte)((i & 0x0000FF00) >> 8));
	writeOctet((byte)((i & 0x00FF0000) >> 16));
	writeOctet((byte)((i & 0xFF000000) >> 24));
      }

      public void writeLongLong(long l) {
	setWriteAlignment(8);
	writeOctet((byte)(l & 0x00000000000000FFL));
	writeOctet((byte)((l & 0x000000000000FF00L) >> 8));
	writeOctet((byte)((l & 0x0000000000FF0000L) >> 16));
	writeOctet((byte)((l & 0x00000000FF000000L) >> 24));
	writeOctet((byte)((l & 0x000000FF00000000L) >> 32));
	writeOctet((byte)((l & 0x0000FF0000000000L) >> 40));
	writeOctet((byte)((l & 0x00FF000000000000L) >> 48));
	writeOctet((byte)((l & 0xFF00000000000000L) >> 56));
      }

    }  // End of LittleEndianCodec class

    public String getProto() {
      return "iiop";
    }

    public String getHost() {
      return host;
    }

    public String getPort() {
      return Short.toString(port);
    }

    public String getFile() {
      return objectKey;
    }

    public String getAnchor() {
      return "";
    }

  } // End of IIOPAddress class

