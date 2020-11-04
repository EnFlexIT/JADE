package java.nio;

import System.BitConverter;
import System.Text.UnicodeEncoding;
import System.Text.Encoding;

/**
  * A read/write HeapByteBuffer.
  * @author Federico Pieri - Erxa
  * @version $Date: 2005/03/08 14:30:00 $ $Revision: 1.01 $
  */

class HeapByteBuffer extends ByteBuffer
{

    // For speed these fields are actually declared in X-Buffer;
    // these declarations are here as documentation
    /*

    protected final byte[] hb;
    protected final int offset;

    */

	HeapByteBuffer(int cap, int lim) 
	{	
		super(-1, 0, lim, cap, new byte[cap], 0);
	}

	HeapByteBuffer(byte[] buf, int off, int len) 
	{ 
		super(-1, off, off + len, buf.length, buf, 0);
	}

	HeapByteBuffer(byte[] buf,
		int mark, int pos, int lim, int cap,
		int off)
	{
		super(mark, pos, lim, cap, buf, off);
	}

    public ByteBuffer slice() 
	{
		return new HeapByteBuffer(m_Array, -1, 0, this.remaining(), 
			this.remaining(), this.position() + m_Offset);
    }

    public ByteBuffer duplicate() 
	{
		return new HeapByteBuffer(m_Array, this.markValue(), this.position(), this.limit(),
			this.capacity(), m_Offset);
    }

    
    protected int ix(int i) 
	{
		return i + m_Offset;
    }

    public byte get() 
	{
		return m_Array[ix( nextGetIndex() )];
    }

    public byte get(int i) 
	{
		return m_Array[ix( checkIndex(i) )];
    }

	public ByteBuffer get(byte[] dst, int offset, int length) 
	{
		checkBounds(offset, length, dst.length);
		if (length > remaining())
			throw new BufferUnderflowException();
		System.arraycopy(m_Array, ix(position()), dst, offset, length);
		position(position() + length);
		return this;
	}

    public boolean isDirect() 
	{
		return false;
    }

	public boolean isReadOnly() 
	{
		return false;
    }

	public ByteBuffer put(byte x) 
	{
		m_Array[ix( nextPutIndex() )] = x;
		return this;
	}

    public ByteBuffer put(int i, byte x) 
	{
		m_Array[ix( checkIndex(i) )] = x;
		return this;
    }

	public ByteBuffer put(byte[] src, int offset, int length) 
	{
		checkBounds(offset, length, src.length);
		if (length > remaining())
			throw new BufferOverflowException();
		System.arraycopy(src, offset, m_Array, ix(position()), length);
		position(position() + length);
		return this;
	}

    public ByteBuffer put(ByteBuffer src) 
	{
		if (src instanceof HeapByteBuffer) 
		{
			if (src == this)
				throw new IllegalArgumentException();
			int n = src.remaining();
			if ( n > remaining() )
				throw new BufferOverflowException();
			System.arraycopy(src.m_Array, src.ix( src.position() ), m_Array, ix( position() ), n);
			src.position( src.position() + n);
			position( position() + n);
		}
		return this;
    }

    public ByteBuffer compact() 
	{
		System.arraycopy(m_Array, ix(position()), m_Array, ix(0), remaining());
		position(remaining());
		limit(capacity());
		return this;
    }

    public byte _get(int i) 
	{
		return m_Array[i];
    }

	public void _put(int i, byte b) 
	{

		m_Array[i] = b;
	}

    // char
    public char getChar() 
	{
		byte[] originalValue = new byte[2];
		ubyte[] finalValue   = new ubyte[2];

		originalValue[0] = this.get();
		originalValue[1] = this.get();
		/*
		finalValue = convertByteToUbyte( originalValue );

		UnicodeEncoding ue = new UnicodeEncoding(m_BigEndian, true);
		String byteString  = BitConverter.ToString( finalValue );
		ubyte[] resBytes   = ue.GetBytes(byteString);

		return BitConverter.ToChar(resBytes, 0);
		*/
		boolean test1 = m_BigEndian && BitConverter.IsLittleEndian;
		boolean test2 = !m_BigEndian && !BitConverter.IsLittleEndian;
		if (test1 || test2)
			finalValue = toInvertedUBytesArray(originalValue);

		return BitConverter.ToChar(finalValue, 0);
    }

    public char getChar(int i) 
	{
		byte[] originalValue= new byte[2];
		ubyte[] finalValue	= new ubyte[2];

		originalValue[0] = this._get(i+0);
		originalValue[1] = this._get(i+1);
		/*
		ubyte[] finalValue = convertByteToUbyte( bArray );

		UnicodeEncoding ue = new UnicodeEncoding(m_BigEndian, true);
		String byteString  = BitConverter.ToString( finalValue );
		ubyte[] resBytes   = ue.GetBytes(byteString);

		return BitConverter.ToChar(resBytes, 0);
		*/
		boolean test1 = m_BigEndian && BitConverter.IsLittleEndian;
		boolean test2 = !m_BigEndian && !BitConverter.IsLittleEndian;
		if (test1 || test2)
			finalValue = toInvertedUBytesArray(originalValue);

		return BitConverter.ToChar(finalValue, 0);
    }

    public ByteBuffer putChar(char x) 
	{
		boolean testLittleEndian = (BitConverter.IsLittleEndian) && (!m_BigEndian);
		boolean testBigEndian	 = (!BitConverter.IsLittleEndian) && (m_BigEndian);

		if ( testLittleEndian || testBigEndian )
		{
			this.put( toByteArray(x) );		
		}
		else
		{
			this.put( toInvertedByteArray(x) );
		}
		return this;
    }

    public ByteBuffer putChar(int i, char x) 
	{
		boolean testLittleEndian = (BitConverter.IsLittleEndian) && (!m_BigEndian);
		boolean testBigEndian	 = (!BitConverter.IsLittleEndian) && (m_BigEndian);

		if ( testLittleEndian || testBigEndian )
		{
			byte[] resArray		= toByteArray(x);
			
			for (int j=0; j<4; j++)
				this._put(i+j, resArray[j]);
		}
		else
		{
			byte[] resArray = toInvertedByteArray(x);
			
			for (int j=0; j<4; j++)
				this._put(i+j, resArray[j]);
		}
		return this;
    }
   

    // short
    public short getShort() 
	{
		byte[] originalValue = new byte[2];
		ubyte[] finalValue   = new ubyte[2];

		originalValue[0] = this.get();
		originalValue[1] = this.get();
		/*
		finalValue = convertByteToUbyte( originalValue );

		UnicodeEncoding ue = new UnicodeEncoding(m_BigEndian, true);
		String byteString  = BitConverter.ToString( finalValue );
		ubyte[] resBytes   = ue.GetBytes(byteString);

		return BitConverter.ToInt16(resBytes, 0);
		*/
		boolean test1 = m_BigEndian && BitConverter.IsLittleEndian;
		boolean test2 = !m_BigEndian && !BitConverter.IsLittleEndian;
		if (test1 || test2)
			finalValue = toInvertedUBytesArray(originalValue);

		return BitConverter.ToInt16(finalValue, 0);
    }

    public short getShort(int i) 
	{
		byte[] originalValue= new byte[2];
		ubyte[] finalValue	= new ubyte[2];

		originalValue[0] = this._get(i+0);
		originalValue[1] = this._get(i+1);
		/*
		ubyte[] finalValue = convertByteToUbyte( bArray );

		UnicodeEncoding ue = new UnicodeEncoding(m_BigEndian, true);
		String byteString  = BitConverter.ToString( finalValue );
		ubyte[] resBytes   = ue.GetBytes(byteString);

		return BitConverter.ToInt16(resBytes, 0);
		*/
		boolean test1 = m_BigEndian && BitConverter.IsLittleEndian;
		boolean test2 = !m_BigEndian && !BitConverter.IsLittleEndian;
		if (test1 || test2)
			finalValue = toInvertedUBytesArray(originalValue);

		return BitConverter.ToInt16(finalValue, 0);
    }

    public ByteBuffer putShort(short x) 
	{
		boolean testLittleEndian = (BitConverter.IsLittleEndian) && (!m_BigEndian);
		boolean testBigEndian	 = (!BitConverter.IsLittleEndian) && (m_BigEndian);

		if ( testLittleEndian || testBigEndian )
		{
			this.put( toByteArray(x) );		
		}
		else
		{
			this.put( toInvertedByteArray(x) );
		}
		return this;
    }

    public ByteBuffer putShort(int i, short x) 
	{
		boolean testLittleEndian = (BitConverter.IsLittleEndian) && (!m_BigEndian);
		boolean testBigEndian	 = (!BitConverter.IsLittleEndian) && (m_BigEndian);

		if ( testLittleEndian || testBigEndian )
		{
			byte[] resArray		= toByteArray(x);
			
			for (int j=0; j<4; j++)
				this._put(i+j, resArray[j]);
		}
		else
		{
			byte[] resArray = toInvertedByteArray(x);
			
			for (int j=0; j<4; j++)
				this._put(i+j, resArray[j]);
		}
		return this;
    }


    // int
	public int getInt()
	{
		byte[] originalValue = new byte[4];
		ubyte[] finalValue   = new ubyte[4];

		originalValue[0] = this.get();
		originalValue[1] = this.get();
		originalValue[2] = this.get();
		originalValue[3] = this.get();

		/*
		finalValue = convertByteToUbyte( originalValue );

		UnicodeEncoding ue = new UnicodeEncoding(m_BigEndian, true);
		String byteString  = BitConverter.ToString( finalValue );
		ubyte[] resBytes   = ue.GetBytes(byteString);
		
		return BitConverter.ToInt32(resBytes, 0);
		*/
		boolean test1 = m_BigEndian && BitConverter.IsLittleEndian;
		boolean test2 = !m_BigEndian && !BitConverter.IsLittleEndian;
		if (test1 || test2)
			finalValue = toInvertedUBytesArray(originalValue);

		return BitConverter.ToInt32(finalValue, 0);
	}

    public int getInt(int i) 
	{
		byte[] originalValue = new byte[4];
		ubyte[] finalValue   = new ubyte[4];

		originalValue[0] = this._get(i+0);
		originalValue[1] = this._get(i+1);
		originalValue[2] = this._get(i+2);
		originalValue[3] = this._get(i+3);

		/*
		ubyte[] finalValue = convertByteToUbyte( bArray );

		UnicodeEncoding ue = new UnicodeEncoding(m_BigEndian, true);
		String byteString  = BitConverter.ToString( finalValue );
		ubyte[] resBytes   = ue.GetBytes(byteString);
		*/
		boolean test1 = m_BigEndian && BitConverter.IsLittleEndian;
		boolean test2 = !m_BigEndian && !BitConverter.IsLittleEndian;
		if (test1 || test2)
			finalValue = toInvertedUBytesArray(originalValue);

		//return BitConverter.ToInt32(resBytes, 0);
		return BitConverter.ToInt32(finalValue, 0);
    }

	public ByteBuffer putInt(int x) 
	{
		boolean testLittleEndian = (BitConverter.IsLittleEndian) && (!m_BigEndian);
		boolean testBigEndian	 = (!BitConverter.IsLittleEndian) && (m_BigEndian);

		if ( testLittleEndian || testBigEndian )
		{
			this.put( toByteArray(x) );		
		}
		else
		{
			this.put( toInvertedByteArray(x) );
		}
		return this;
	}

	public ByteBuffer putInt(int i, int x) 
	{
		boolean testLittleEndian = (BitConverter.IsLittleEndian) && (!m_BigEndian);
		boolean testBigEndian	 = (!BitConverter.IsLittleEndian) && (m_BigEndian);

		if ( testLittleEndian || testBigEndian )
		{
			byte[] resArray		= toByteArray(x);
			
			for (int j=0; j<4; j++)
				this._put(i+j, resArray[j]);
		}
		else
		{
			byte[] resArray = toInvertedByteArray(x);
			
			for (int j=0; j<4; j++)
				this._put(i+j, resArray[j]);
		}
		return this;
	}

    
    // long
    public long getLong() 
	{
		byte[] originalValue = new byte[8];
		ubyte[] finalValue   = new ubyte[8];

		originalValue[0] = this.get();
		originalValue[1] = this.get();
		originalValue[2] = this.get();
		originalValue[3] = this.get();
		originalValue[4] = this.get();
		originalValue[5] = this.get();
		originalValue[6] = this.get();
		originalValue[7] = this.get();

		/*
		finalValue = convertByteToUbyte( originalValue );

		UnicodeEncoding ue = new UnicodeEncoding(m_BigEndian, true);
		String byteString  = BitConverter.ToString( finalValue );
		ubyte[] resBytes   = ue.GetBytes(byteString);

		return BitConverter.ToInt64(resBytes, 0);
		*/
		boolean test1 = m_BigEndian && BitConverter.IsLittleEndian;
		boolean test2 = !m_BigEndian && !BitConverter.IsLittleEndian;
		if (test1 || test2)
			finalValue = toInvertedUBytesArray(originalValue);

		return BitConverter.ToInt64(finalValue, 0);
    }

    public long getLong(int i) 
	{
		byte[] originalValue= new byte[8];
		ubyte[] finalValue	= new ubyte[8];

		originalValue[0] = this._get(i+0);
		originalValue[1] = this._get(i+1);
		originalValue[2] = this._get(i+2);
		originalValue[3] = this._get(i+3);
		originalValue[3] = this._get(i+4);
		originalValue[3] = this._get(i+5);
		originalValue[3] = this._get(i+6);
		originalValue[3] = this._get(i+7);

		/*
		ubyte[] finalValue = convertByteToUbyte( bArray );

		UnicodeEncoding ue = new UnicodeEncoding(m_BigEndian, true);
		String byteString  = BitConverter.ToString( finalValue );
		ubyte[] resBytes   = ue.GetBytes(byteString);

		return BitConverter.ToInt64(resBytes, 0);
		*/
		boolean test1 = m_BigEndian && BitConverter.IsLittleEndian;
		boolean test2 = !m_BigEndian && !BitConverter.IsLittleEndian;
		if (test1 || test2)
			finalValue = toInvertedUBytesArray(originalValue);

		return BitConverter.ToInt64(finalValue, 0);
    }

    public ByteBuffer putLong(long x) 
	{
		boolean testLittleEndian = (BitConverter.IsLittleEndian) && (!m_BigEndian);
		boolean testBigEndian	 = (!BitConverter.IsLittleEndian) && (m_BigEndian);

		if ( testLittleEndian || testBigEndian )
		{
			this.put( toByteArray(x) );		
		}
		else
		{
			this.put( toInvertedByteArray(x) );
		}
		return this;
	}

	public ByteBuffer putLong(int i, long x) 
	{
		boolean testLittleEndian = (BitConverter.IsLittleEndian) && (!m_BigEndian);
		boolean testBigEndian	 = (!BitConverter.IsLittleEndian) && (m_BigEndian);

		if ( testLittleEndian || testBigEndian )
		{
			byte[] resArray		= toByteArray(x);
			
			for (int j=0; j<8; j++)
				this._put(i+j, resArray[j]);
		}
		else
		{
			byte[] resArray = toInvertedByteArray(x);
			
			for (int j=0; j<8; j++)
				this._put(i+j, resArray[j]);
		}
		return this;    
	}
	    

    // float
    public float getFloat() 
	{
		byte[] originalValue = new byte[4];
		ubyte[] finalValue   = new ubyte[4];

		originalValue[0] = this.get();
		originalValue[1] = this.get();
		originalValue[2] = this.get();
		originalValue[3] = this.get();
		/*
		finalValue = convertByteToUbyte( originalValue );

		UnicodeEncoding ue = new UnicodeEncoding(m_BigEndian, true);
		String byteString  = BitConverter.ToString( finalValue );
		ubyte[] resBytes   = ue.GetBytes(byteString);

		return BitConverter.ToSingle(resBytes, 0);
		*/
		boolean test1 = m_BigEndian && BitConverter.IsLittleEndian;
		boolean test2 = !m_BigEndian && !BitConverter.IsLittleEndian;
		if (test1 || test2)
			finalValue = toInvertedUBytesArray(originalValue);

		return BitConverter.ToSingle(finalValue, 0);
    }

    public float getFloat(int i) 
	{
		byte[] originalValue= new byte[4];
		ubyte[] finalValue	= new ubyte[4];

		originalValue[0] = this._get(i+0);
		originalValue[1] = this._get(i+1);
		originalValue[2] = this._get(i+2);
		originalValue[3] = this._get(i+3);
		/*
		ubyte[] finalValue = convertByteToUbyte( bArray );

		UnicodeEncoding ue = new UnicodeEncoding(m_BigEndian, true);
		String byteString  = BitConverter.ToString( finalValue );
		ubyte[] resBytes   = ue.GetBytes(byteString);

		return BitConverter.ToSingle(resBytes, 0);
		*/
		boolean test1 = m_BigEndian && BitConverter.IsLittleEndian;
		boolean test2 = !m_BigEndian && !BitConverter.IsLittleEndian;
		if (test1 || test2)
			finalValue = toInvertedUBytesArray(originalValue);

		return BitConverter.ToSingle(finalValue, 0);
    }



    public ByteBuffer putFloat(float x) 
	{
		boolean testLittleEndian = (BitConverter.IsLittleEndian) && (!m_BigEndian);
		boolean testBigEndian	 = (!BitConverter.IsLittleEndian) && (m_BigEndian);

		if ( testLittleEndian || testBigEndian )
		{
			this.put( toByteArray(x) );		
		}
		else
		{
			this.put( toInvertedByteArray(x) );
		}
		return this;
    }

    public ByteBuffer putFloat(int i, float x) 
	{
		boolean testLittleEndian = (BitConverter.IsLittleEndian) && (!m_BigEndian);
		boolean testBigEndian	 = (!BitConverter.IsLittleEndian) && (m_BigEndian);

		if ( testLittleEndian || testBigEndian )
		{
			byte[] resArray		= toByteArray(x);
			
			for (int j=0; j<8; j++)
				this._put(i+j, resArray[j]);
		}
		else
		{
			byte[] resArray = toInvertedByteArray(x);
			
			for (int j=0; j<8; j++)
				this._put(i+j, resArray[j]);
		}
		return this;
    }


    

    // double
    public double getDouble() 
	{
		byte[] originalValue = new byte[8];
		ubyte[] finalValue   = new ubyte[8];

		originalValue[0] = this.get();
		originalValue[1] = this.get();
		originalValue[2] = this.get();
		originalValue[3] = this.get();
		originalValue[4] = this.get();
		originalValue[5] = this.get();
		originalValue[6] = this.get();
		originalValue[7] = this.get();
		/*
		finalValue = convertByteToUbyte( originalValue );

		UnicodeEncoding ue = new UnicodeEncoding(m_BigEndian, true);
		String byteString  = BitConverter.ToString( finalValue );
		ubyte[] resBytes   = ue.GetBytes(byteString);

		return BitConverter.ToDouble(resBytes, 0);
		*/
		boolean test1 = m_BigEndian && BitConverter.IsLittleEndian;
		boolean test2 = !m_BigEndian && !BitConverter.IsLittleEndian;
		if (test1 || test2)
			finalValue = toInvertedUBytesArray(originalValue);

		return BitConverter.ToDouble(finalValue, 0);
    }

    public double getDouble(int i) 
	{
		byte[] originalValue= new byte[8];
		ubyte[] finalValue	= new ubyte[8];

		originalValue[0] = this._get(i+0);
		originalValue[1] = this._get(i+1);
		originalValue[2] = this._get(i+2);
		originalValue[3] = this._get(i+3);
		originalValue[3] = this._get(i+4);
		originalValue[3] = this._get(i+5);
		originalValue[3] = this._get(i+6);
		originalValue[3] = this._get(i+7);
		/*
		ubyte[] finalValue = convertByteToUbyte( bArray );

		UnicodeEncoding ue = new UnicodeEncoding(m_BigEndian, true);
		String byteString  = BitConverter.ToString( finalValue );
		ubyte[] resBytes   = ue.GetBytes(byteString);

		return BitConverter.ToDouble(resBytes, 0);
		*/
		boolean test1 = m_BigEndian && BitConverter.IsLittleEndian;
		boolean test2 = !m_BigEndian && !BitConverter.IsLittleEndian;
		if (test1 || test2)
			finalValue = toInvertedUBytesArray(originalValue);

		return BitConverter.ToDouble(finalValue, 0);
    }



    public ByteBuffer putDouble(double x) 
	{
		boolean testLittleEndian = (BitConverter.IsLittleEndian) && (!m_BigEndian);
		boolean testBigEndian	 = (!BitConverter.IsLittleEndian) && (m_BigEndian);

		if ( testLittleEndian || testBigEndian )
		{
			this.put( toByteArray(x) );		
		}
		else
		{
			this.put( toInvertedByteArray(x) );
		}
		return this;
    }

    public ByteBuffer putDouble(int i, double x) 
	{
		boolean testLittleEndian = (BitConverter.IsLittleEndian) && (!m_BigEndian);
		boolean testBigEndian	 = (!BitConverter.IsLittleEndian) && (m_BigEndian);

		if ( testLittleEndian || testBigEndian )
		{
			byte[] resArray		= toByteArray(x);
			
			for (int j=0; j<8; j++)
				this._put(i+j, resArray[j]);
		}
		else
		{
			byte[] resArray = toInvertedByteArray(x);
			
			for (int j=0; j<8; j++)
				this._put(i+j, resArray[j]);
		}
		return this;
    }


	protected byte[] toByteArray(int i)
	{
		ubyte[] endianBytes	= BitConverter.GetBytes(i);
		return toBytesArray( endianBytes );
	}

	protected byte[] toInvertedByteArray(int i)
	{
		ubyte[] endianBytes	= BitConverter.GetBytes(i);
		return toInvertedBytesArray( endianBytes );
	}

	protected byte[] toByteArray(long l)
	{
		ubyte[] endianBytes	= BitConverter.GetBytes(l);
		return toBytesArray( endianBytes );
	}

	protected byte[] toInvertedByteArray(long l)
	{
		ubyte[] endianBytes	= BitConverter.GetBytes(l);
		return toInvertedBytesArray( endianBytes );
	}

	protected byte[] toByteArray(float f)
	{
		ubyte[] endianBytes	= BitConverter.GetBytes(f);
		return toBytesArray( endianBytes );
	}

	protected byte[] toInvertedByteArray(float f)
	{
		ubyte[] endianBytes	= BitConverter.GetBytes(f);
		return toInvertedBytesArray( endianBytes );
	}

	protected byte[] toByteArray(double d)
	{
		ubyte[] endianBytes	= BitConverter.GetBytes(d);
		return toBytesArray( endianBytes );
	}

	protected byte[] toInvertedByteArray(double d)
	{
		ubyte[] endianBytes	= BitConverter.GetBytes(d);
		return toInvertedBytesArray( endianBytes );
	}

	protected byte[] toByteArray(short s)
	{
		ubyte[] endianBytes	= BitConverter.GetBytes(s);
		return toBytesArray( endianBytes );
	}

	protected byte[] toInvertedByteArray(short s)
	{
		ubyte[] endianBytes	= BitConverter.GetBytes(s);
		return toInvertedBytesArray( endianBytes );
	}

	protected byte[] toByteArray(char c)
	{
		ubyte[] endianBytes	= BitConverter.GetBytes(c);
		return toBytesArray( endianBytes );
	}

	protected byte[] toInvertedByteArray(char c)
	{
		ubyte[] endianBytes	= BitConverter.GetBytes(c);
		return toInvertedBytesArray( endianBytes );
	}

	private byte[] toBytesArray(ubyte[] endianBytes)
	{
		ubyte[] resBytes	= new ubyte[endianBytes.length];
		byte[] resArray		= convertUbyteToByte( resBytes );
		return resArray;
	}

	private ubyte[] toInvertedUBytesArray(byte[] endianBytes)
	{
		int dimension			= endianBytes.length;
		ubyte[] resUBytes		= new ubyte[dimension];
		for (int i=0; i<dimension; i++)
			resUBytes[dimension-i-1] = (ubyte) endianBytes[i];
		return resUBytes;
	}

	private byte[] toInvertedBytesArray(ubyte[] endianBytes)
	{
		ubyte[] resBytes	= new ubyte[endianBytes.length];

		int tot = endianBytes.length;
		for (int j=0; j<tot; j++)
			resBytes[tot-j-1] = endianBytes[j];
			
		byte[] resArray		= convertUbyteToByte( resBytes );
		return resArray;
	}

}
