///////////////////////////////////////////////////////////////
//   
//   /     /  ___/  ___/   / /_   _/ 
//  /  /--/___  /  ___/   /_/ / / /   
// /_____/_____/_____/_____/_/_/_/
// 
// -----------------------------------------------------------
// PROJECT:    IIOPADDRESS	
// FILE NAME:  IIOPAddress.java	
// CONTENT:	   This file includes the definition of the IIOPAddress class.
// AUTHORS:	   Giovanni Caire	
// 
//////////////////////////////////////////////////////////////

package jade.core;

public class IIOPAddress
{
	public static final short BIG_ENDIAN = 0;
	public static final short LITTLE_ENDIAN = 1;

	private static final String TYPE_ID = "IDL:FIPA_Agent_97:1.0";
	private static final int TAG_INTERNET_IOP = 0;
	private static final byte IIOP_MAJOR = 1;
	private static final byte IIOP_MINOR = 0;

	private String ior;
	private String host;
	private short port;
	private String objectKey;

	private CDRCodec codecStrategy;

	private IIOPAddress()
	{
	}

	public static IIOPAddress createFromIOR(String s) throws IIOPFormatException
	{
		// Create the IIOPAddress object that will be returned
		IIOPAddress a = new IIOPAddress();

		// Store stringified upper-case IOR 
		a.ior = new String(s.toUpperCase());
		
		// Remove 'IOR:' prefix to get Hex digits
		String hexString = a.ior.substring(4);

		short endianness = Short.parseShort(hexString.substring(0, 2), 16);

		switch(endianness)
		{
		case BIG_ENDIAN:
			a.codecStrategy = a.new BigEndianCodec(hexString);
			break;
		case LITTLE_ENDIAN:
			a.codecStrategy = a.new LittleEndianCodec(hexString);
			break;
		default:
			throw new IIOPFormatException("Invalid endianness specifier");
		}

		// Read 'string type_id' field
		String typeID = a.codecStrategy.readString();
		if(!typeID.equalsIgnoreCase(TYPE_ID))
			throw new IIOPFormatException("Invalid type ID" + typeID);

		// Read 'sequence<TaggedProfile> profiles' field
		// Read sequence length
		int seqLen = a.codecStrategy.readLong();
		for(int i = 0; i < seqLen; i++) 
		{
			// Read 'ProfileId tag' field
			int tag = a.codecStrategy.readLong();
			byte[] profile = a.codecStrategy.readOctetSequence();
			if(tag == TAG_INTERNET_IOP) 
			{
				// Process IIOP profile
				CDRCodec profileBodyCodec;
				switch(profile[0]) 
				{
				case BIG_ENDIAN:
					profileBodyCodec = a.new BigEndianCodec(profile);
					break;
				case LITTLE_ENDIAN:
					profileBodyCodec = a.new LittleEndianCodec(profile);
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
				a.host = profileBodyCodec.readString();

				// Read 'unsigned short port' field
				a.port = profileBodyCodec.readShort();

				// Read 'sequence<octet> object_key' field and convert it
				// into a String object
				byte[] keyBuffer = profileBodyCodec.readOctetSequence();
				a.objectKey = new String(keyBuffer);

				//a.codecStrategy = null; // Commentato da G.Caire
			}
		}
		a.codecStrategy = null; // Aggiunto da G.Caire 
		return(a);
	}

	public static IIOPAddress createFromURL(String s, short endianness) throws IIOPFormatException
	{
		// Create the IIOPAddress object that will be returned
		IIOPAddress a = new IIOPAddress();

		// Remove 'iiop://' prefix to get URL host, port and file
		s = s.substring(7);
		int colonPos = s.indexOf(':');
		int slashPos = s.indexOf('/');
		if((colonPos == -1) || (slashPos == -1))
			throw new IIOPFormatException("Invalid URL string");

		a.host = new String(s.substring(0, colonPos));
		a.port = Short.parseShort(s.substring(colonPos + 1, slashPos));
		a.objectKey = new String(s.substring(slashPos + 1, s.length()));

		// Create the IOR String
		switch(endianness) 
		{
		case BIG_ENDIAN:
			a.codecStrategy = a.new BigEndianCodec(new byte[0]);
			break;
		case LITTLE_ENDIAN:
			a.codecStrategy = a.new LittleEndianCodec(new byte[0]);
			break;
		default:
			throw new IIOPFormatException("Invalid endianness specifier");
		}

		a.codecStrategy.writeString(TYPE_ID);

		// Write '1' as profiles sequence length
		a.codecStrategy.writeLong(1);

		a.codecStrategy.writeLong(TAG_INTERNET_IOP);
		CDRCodec profileBodyCodec;
		switch(endianness) 
		{
		case BIG_ENDIAN:
			profileBodyCodec = a.new BigEndianCodec(new byte[0]);
			break;
		case LITTLE_ENDIAN:
			profileBodyCodec = a.new LittleEndianCodec(new byte[0]);
			break;
		default:
			throw new IIOPFormatException("Invalid endianness specifier");
		}

		// Write IIOP 1.0 profile to auxiliary CDR codec
		profileBodyCodec.writeOctet(IIOP_MAJOR);
		profileBodyCodec.writeOctet(IIOP_MINOR);
		profileBodyCodec.writeString(a.host);
		profileBodyCodec.writeShort(a.port);
		byte[] objKey = a.objectKey.getBytes();
		profileBodyCodec.writeOctetSequence(objKey);

		byte[] encapsulatedProfile = profileBodyCodec.writtenBytes();

		// Write encapsulated profile to main IOR codec
		a.codecStrategy.writeOctetSequence(encapsulatedProfile);

		String hexString = a.codecStrategy.writtenString();
		a.ior = new String("IOR:" + hexString);

		a.codecStrategy = null;
		return(a);
	}

	public String getIOR()
	{
		return(ior);
	}

	public String getURL()
	{
		return("iiop://" + host + ":" + port + "/" + objectKey);
	}

	public String getHost()
	{
		return(host);
	}

	public int getPort()
	{
		return((int) port);
	}

	public String getObjectKey()
	{
		return(objectKey);
	}

	/////////////////////////////////////////
	// PRIVATE CLASSES
	private static abstract class CDRCodec 
	{

		private static final char[] HEX = 
		{
			'0','1','2','3','4','5','6','7',
			'8','9','a','b','c','d','e','f'
		};

		protected byte[] readBuffer;
		protected StringBuffer writeBuffer;
		protected int readIndex = 0;
		protected int writeIndex = 0;

		protected CDRCodec(String hexString) 
		{
			// Put all Hex digits into a byte array
			readBuffer = bytesFromHexString(hexString);
			readIndex = 1;
			writeBuffer = new StringBuffer(255);
		}

		protected CDRCodec(byte[] hexDigits) 
		{
			readBuffer = new byte[hexDigits.length];
			System.arraycopy(hexDigits, 0, readBuffer, 0, readBuffer.length);
			readIndex = 1;
			writeBuffer = new StringBuffer(255);
		}

		public String writtenString() 
		{
			return new String(writeBuffer);
		}

		public byte[] writtenBytes() 
		{
			return bytesFromHexString(new String(writeBuffer));
		}

		public byte readOctet() 
		{
			return readBuffer[readIndex++];
		}

		public byte[] readOctetSequence() 
		{
			int seqLen = readLong();
			byte[] result = new byte[seqLen];
			System.arraycopy(readBuffer, readIndex, result, 0, seqLen);
			readIndex += seqLen;
			return result;
		}

		public String readString() 
		{
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
		public void writeOctet(byte b) 
		{
			char[] digits = new char[2];
			digits[0] = HEX[(b & 0xF0) >> 4]; // High nibble
			digits[1] = HEX[b & 0x0F]; // Low nibble
			writeBuffer.append(digits);
			writeIndex++;
		}

		public void writeOctetSequence(byte[] seq) 
		{
			int seqLen = seq.length;
			writeLong(seqLen);
			for(int i = 0; i < seqLen; i++)
				writeOctet(seq[i]);
		}

		public void writeString(String s) 
		{
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

		protected void setReadAlignment(int align) 
		{
			while((readIndex % align) != 0)
				readIndex++;
		}

		protected void setWriteAlignment(int align) 
		{
			while(writeIndex % align != 0)
				writeOctet((byte)0x00);
		}

		private byte[] bytesFromHexString(String hexString) 
		{
			int hexLen = hexString.length() / 2;
			byte[] result = new byte[hexLen];
			for(int i = 0; i < hexLen; i ++) 
			{
				String currentDigit = hexString.substring(2*i, 2*(i + 1));
				Short s = Short.valueOf(currentDigit, 16);
				result[i] = s.byteValue();
			}
			return result;
		}

	}

	private class BigEndianCodec extends CDRCodec 
	{
		public BigEndianCodec(String ior) 
		{
			super(ior);
			writeOctet((byte)0x00); // Writes 'Big Endian' magic number
		}

		public BigEndianCodec(byte[] hexDigits) 
		{
			super(hexDigits);
			writeOctet((byte)0x00); // Writes 'Big Endian' magic number
		}

		public short readShort() 
		{
			setReadAlignment(2);
			short result = (short)((readBuffer[readIndex++] << 8) + readBuffer[readIndex++]);
			return result;
		}

		public int readLong() 
		{
			setReadAlignment(4);
			int result = (readBuffer[readIndex++] << 24) + (readBuffer[readIndex++] << 16);
			result += (readBuffer[readIndex++] << 8) + readBuffer[readIndex++];
			return result;
		}

		public long readLongLong() 
		{
			setReadAlignment(8);
			long result = (readBuffer[readIndex++] << 56) + (readBuffer[readIndex++] << 48);
			result += (readBuffer[readIndex++] << 40) + (readBuffer[readIndex++] << 32);
			result += (readBuffer[readIndex++] << 24) + (readBuffer[readIndex++] << 16);
			result += (readBuffer[readIndex++] << 8) + readBuffer[readIndex++];
			return result;
		}

		public void writeShort(short s) 
		{
			setWriteAlignment(2);
			writeOctet((byte)((s & 0xFF00) >> 8));
			writeOctet((byte)(s & 0x00FF));
		}

		public void writeLong(int i) 
		{
			setWriteAlignment(4);
			writeOctet((byte)((i & 0xFF000000) >> 24));
			writeOctet((byte)((i & 0x00FF0000) >> 16));
			writeOctet((byte)((i & 0x0000FF00) >> 8));
			writeOctet((byte)(i & 0x000000FF));
		}

		public void writeLongLong(long l) 
		{
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

	private class LittleEndianCodec extends CDRCodec 
	{
		public LittleEndianCodec(String ior) 
		{
			super(ior);
			writeOctet((byte)0x01); // Writes 'Little Endian' magic number
		}

		public LittleEndianCodec(byte[] hexDigits) 
		{
			super(hexDigits);
			writeOctet((byte)0x01); // Writes 'Little Endian' magic number
		}

		public short readShort() 
		{
			setReadAlignment(2);
			short result = (short)(readBuffer[readIndex++] + (readBuffer[readIndex++] << 8));
			return result;
		}

		public int readLong() 
		{
			setReadAlignment(4);
			int result = readBuffer[readIndex++] + (readBuffer[readIndex++] << 8) + (readBuffer[readIndex++] << 16) + (readBuffer[readIndex++] << 24);
			return result;
		}

		public long readLongLong() 
		{
			setReadAlignment(8);
			long result = readBuffer[readIndex++] + (readBuffer[readIndex++] << 8);
			result += (readBuffer[readIndex++] << 16) + (readBuffer[readIndex++] << 24);
			result += (readBuffer[readIndex++] << 32) + (readBuffer[readIndex++] << 40);
			result += (readBuffer[readIndex++] << 48) + (readBuffer[readIndex++] << 56);
			return result;
		}

		public void writeShort(short s) 
		{
			setWriteAlignment(2);
			writeOctet((byte)(s & 0x00FF));
			writeOctet((byte)((s & 0xFF00) >> 8));
		}

		public void writeLong(int i) 
		{
			setWriteAlignment(4);
			writeOctet((byte)(i & 0x000000FF));
			writeOctet((byte)((i & 0x0000FF00) >> 8));
			writeOctet((byte)((i & 0x00FF0000) >> 16));
			writeOctet((byte)((i & 0xFF000000) >> 24));
		}

		public void writeLongLong(long l) 
		{
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
