/*
  $Log$
  Revision 1.1  1999/02/03 10:20:57  rimassa
  New class to model outgoing IIOP connections. It can hold a CORBA object
  reference for FIPA_Agent_97 IDL interface and convert it among various formats,
  such as IOR and URL.

*/

package jade.core;

import org.omg.CORBA.*;
import FIPA_Agent_97;
import FIPA_Agent_97Helper;

class OutGoingIIOP {

  public static final short BIG_ENDIAN = 0;
  public static final short LITTLE_ENDIAN = 1;

  private static final String TYPE_ID = "IDL:FIPA_Agent_97:1.0";
  private static final int TAG_INTERNET_IOP = 0;
  private static final byte IIOP_MAJOR = 1;
  private static final byte IIOP_MINOR = 0;

  private ORB orb;

  private String ior;
  private String host;
  private short port;
  private String objectKey;

  private CDRCodec codecStrategy;

  public OutGoingIIOP(ORB anOrb, FIPA_Agent_97 objRef) throws IIOPFormatException {
    orb = anOrb;
    String s = orb.object_to_string(objRef);
    if(s.startsWith("IOR:"))
      initFromIOR(s);
    else if(s.startsWith("iiop://"))
      initFromURL(s, LITTLE_ENDIAN);
    else
      throw new IIOPFormatException("Invalid string prefix");
  }

  public OutGoingIIOP(ORB anOrb, String s) throws IIOPFormatException {
    orb = anOrb;
    if(s.startsWith("IOR:"))
      initFromIOR(s);
    else if(s.startsWith("iiop://"))
      initFromURL(s, LITTLE_ENDIAN);
    else
      throw new IIOPFormatException("Invalid string prefix");
  }

  private void initFromIOR(String s) throws IIOPFormatException {

    // Store stringified IOR
    ior = new String(s);

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
      throw new IIOPFormatException("Invalid endianness specifier");
    }
    // Read 'string type_id' field
    String typeID = codecStrategy.readString();
    if(!typeID.equalsIgnoreCase(TYPE_ID))
      throw new IIOPFormatException("Invalid type ID");

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
          throw new IIOPFormatException("Invalid endianness specifier");
        }

        // Read IIOP version
        byte versionMajor = profileBodyCodec.readOctet();
        byte versionMinor = profileBodyCodec.readOctet();
        if(versionMajor != 1)
          throw new IIOPFormatException("IIOP version not supported");

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

  private void initFromURL(String s, short endianness) throws IIOPFormatException {

    // Remove 'iiop://' prefix to get URL host, port and file
    s = s.substring(7);
    int colonPos = s.indexOf(':');
    int slashPos = s.indexOf('/');
    if((colonPos == -1) || (slashPos == -1))
      throw new IIOPFormatException("Invalid URL string");

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
      throw new IIOPFormatException("Invalid endianness specifier");
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
      throw new IIOPFormatException("Invalid endianness specifier");
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
    return "iiop://" + host + ":" + port + "/" + objectKey;
  }

  public String getIOR() {
    return ior;
  }

  public FIPA_Agent_97 getObject() {
    return FIPA_Agent_97Helper.narrow(orb.string_to_object(ior));
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

  }

  private class BigEndianCodec extends CDRCodec {

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

  }

  private class LittleEndianCodec extends CDRCodec {

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


  }

}

