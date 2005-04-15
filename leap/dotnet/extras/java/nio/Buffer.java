/*****************************************************************
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

package java.nio;

/**
 * A container for data of a specific primitive type.
 *
 * <p> A buffer is a linear, finite sequence of elements of a specific
 * primitive type.  Aside from its content, the essential properties of a
 * buffer are its capacity, limit, and position: </p>
 *
 * <blockquote>
 *
 *   <p> A buffer's <i>capacity</i> is the number of elements it contains.  The
 *   capacity of a buffer is never negative and never changes.  </p>
 *
 *   <p> A buffer's <i>limit</i> is the index of the first element that should
 *   not be read or written.  A buffer's limit is never negative and is never
 *   greater than the its capacity.  </p>
 *
 *   <p> A buffer's <i>position</i> is the index of the next element to be
 *   read or written.  A buffer's position is never negative and is never
 *   greater than its limit.  </p>
 *
 * </blockquote>
 *
 * <p> There is one subclass of this class for each non-boolean primitive type.
 *
 * 
 * <h4> Transferring data </h4>
 *
 * <p> Each subclass of this class defines two categories of <i>get</i> and
 * <i>put</i> operations: </p>
 *
 * <blockquote>
 *
 *   <p> <i>Relative</i> operations read or write one or more elements starting
 *   at the current position and then increment the position by the number of
 *   elements transferred.  If the requested transfer exceeds the limit then a
 *   relative <i>get</i> operation throws a {@link BufferUnderflowException}
 *   and a relative <i>put</i> operation throws a {@link
 *   BufferOverflowException}; in either case, no data is transferred.  </p>
 *
 *   <p> <i>Absolute</i> operations take an explicit element index and do not
 *   affect the position.  Absolute <i>get</i> and <i>put</i> operations throw
 *   an {@link IndexOutOfBoundsException} if the index argument exceeds the
 *   limit.  </p>
 *
 * </blockquote>
 *
 * <p> Data may also, of course, be transferred in to or out of a buffer by the
 * I/O operations of an appropriate channel, which are always relative to the
 * current position.
 *
 *
 * <h4> Marking and resetting </h4>
 *
 * <p> A buffer's <i>mark</i> is the index to which its position will be reset
 * when the {@link #reset reset} method is invoked.  The mark is not always
 * defined, but when it is defined it is never negative and is never greater
 * than the position.  If the mark is defined then it is discarded when the
 * position or the limit is adjusted to a value smaller than the mark.  If the
 * mark is not defined then invoking the {@link #reset reset} method causes an
 * {@link InvalidMarkException} to be thrown.
 *
 *
 * <h4> Invariants </h4>
 *
 * <p> The following invariant holds for the mark, position, limit, and
 * capacity values:
 *
 * <blockquote>
 *     <tt>0</tt> <tt>&lt;=</tt>
 *     <i>mark</i> <tt>&lt;=</tt>
 *     <i>position</i> <tt>&lt;=</tt>
 *     <i>limit</i> <tt>&lt;=</tt>
 *     <i>capacity</i>
 * </blockquote>
 *
 * <p> A newly-created buffer always has a position of zero and a mark that is
 * undefined.  The initial limit may be zero, or it may be some other value
 * that depends upon the type of the buffer and the manner in which it is
 * constructed.  The initial content of a buffer is, in general,
 * undefined.
 *
 *
 * <h4> Clearing, flipping, and rewinding </h4>
 *
 * <p> In addition to methods for accessing the position, limit, and capacity
 * values and for marking and resetting, this class also defines the following
 * operations upon buffers:
 *
 * <ul>
 *
 *   <li><p> {@link #clear} makes a buffer ready for a new sequence of
 *   channel-read or relative <i>put</i> operations: It sets the limit to the
 *   capacity and the position to zero.  </p></li>
 *
 *   <li><p> {@link #flip} makes a buffer ready for a new sequence of
 *   channel-write or relative <i>get</i> operations: It sets the limit to the
 *   current position and then sets the position to zero.  </p></li>
 *
 *   <li><p> {@link #rewind} makes a buffer ready for re-reading the data that
 *   it already contains: It leaves the limit unchanged and sets the position
 *   to zero.  </p></li>
 *
 * </ul>
 *
 *
 * <h4> Read-only buffers </h4>
 *
 * <p> Every buffer is readable, but not every buffer is writable.  The
 * mutation methods of each buffer class are specified as <i>optional
 * operations</i> that will throw a {@link ReadOnlyBufferException} when
 * invoked upon a read-only buffer.  A read-only buffer does not allow its
 * content to be changed, but its mark, position, and limit values are mutable.
 * Whether or not a buffer is read-only may be determined by invoking its
 * {@link #isReadOnly isReadOnly} method.
 *
 *
 * <h4> Thread safety </h4>
 *
 * <p> Buffers are not safe for use by multiple concurrent threads.  If a
 * buffer is to be used by more than one thread then access to the buffer
 * should be controlled by appropriate synchronization.
 *
 *
 * <h4> Invocation chaining </h4>
 *
 * <p> Methods in this class that do not otherwise have a value to return are
 * specified to return the buffer upon which they are invoked.  This allows
 * method invocations to be chained; for example, the sequence of statements
 *
 * <blockquote><pre>
 * b.flip();
 * b.position(23);
 * b.limit(42);</pre></blockquote>
 *
 * can be replaced by the single, more compact statement
 *
 * <blockquote><pre>
 * b.flip().position(23).limit(42);</pre></blockquote>
 *
 *
 * @author Mark Reinhold
 * @author JSR-51 Expert Group
 * @version 1.29, 03/01/23
 * @since 1.4
 * @author Federico Pieri - Erxa (DotNET version)
 * @version $Date: 2005/03/08 14:30:00 $ $Revision: 1.02 $
 */

public abstract class Buffer
{
	//Invariants: mark <= position <= limit <= capacity
	private int m_Mark		= -1;
	private int m_Position	= 0;
	private int m_Limit;
	private int m_Capacity;

	public Buffer(int mark, int pos, int lim, int cap)
	{
		if (cap < 0)
			throw new IllegalArgumentException();
		m_Capacity = cap;
		limit(lim);
		position(pos);
		if (mark > 0)
		{
			if (mark > pos)
				throw new IllegalArgumentException();
			m_Mark = mark;
		}
	}
    
	/**
	 * Returns this buffer's capacity. </p>
	 *
	 * @return  The capacity of this buffer
	 */
	public final int capacity()
	{
		return m_Capacity;
	}

	/**
	 * Returns this buffer's position. </p>
	 *
	 * @return  The position of this buffer
	 */
	public final int position()
	{
		return m_Position;
	}

	/**
	 * Sets this buffer's position.  If the mark is defined and larger than the
	 * new position then it is discarded. </p>
	 *
	 * @param  newPosition
	 *         The new position value; must be non-negative
	 *         and no larger than the current limit
	 *
	 * @return  This buffer
	 *
	 * @throws  IllegalArgumentException
	 *          If the preconditions on <tt>newPosition</tt> do not hold
	 */
	public final Buffer position(int newPosition)
	{
		if ( (newPosition > m_Limit) || (newPosition <0) )
			throw new IllegalArgumentException();
		m_Position = newPosition;
		if (m_Mark > m_Position)
			m_Mark = -1;
		return this;
	}

	/**
	 * Returns this buffer's limit. </p>
	 *
	 * @return  The limit of this buffer
	 */
	public final int limit()
	{
		return m_Limit;
	}

	/**
	 * Sets this buffer's limit.  If the position is larger than the new limit
	 * then it is set to the new limit.  If the mark is defined and larger than
	 * the new limit then it is discarded. </p>
	 *
	 * @param  newLimit
	 *         The new limit value; must be non-negative
	 *         and no larger than this buffer's capacity
	 *
	 * @return  This buffer
	 *
	 * @throws  IllegalArgumentException
	 *          If the preconditions on <tt>newLimit</tt> do not hold
	 */
	public final Buffer limit(int newLimit)
	{
		if ( (newLimit > m_Capacity) || (newLimit < 0) )
			throw new IllegalArgumentException();
		m_Limit = newLimit;
		if (m_Position > m_Limit)
			m_Position = m_Limit;
		if (m_Mark > m_Limit)
			m_Mark = -1;
		return this;
	}

	/**
	 * Sets this buffer's mark at its position. </p>
	 *
	 * @return  This buffer
	 */
	public final Buffer mark()
	{
		m_Mark = m_Position;
		return this;
	}

	/**
	 * Resets this buffer's position to the previously-marked position.
	 *
	 * <p> Invoking this method neither changes nor discards the mark's
	 * value. </p>
	 *
	 * @return  This buffer
	 *
	 * @throws  InvalidMarkException
	 *          If the mark has not been set
	 */
	public final Buffer reset()
	{
		int m = m_Mark;
		if (m < 0)
			throw new InvalidMarkException();
		m_Position = m;
		return this;
	}

	/**
	 * Clears this buffer.  The position is set to zero, the limit is set to
	 * the capacity, and the mark is discarded.
	 *
	 * <p> Invoke this method before using a sequence of channel-read or
	 * <i>put</i> operations to fill this buffer.  For example:
	 *
	 * <blockquote><pre>
	 * buf.clear();     // Prepare buffer for reading
	 * in.read(buf);    // Read data</pre></blockquote>
	 *
	 * <p> This method does not actually erase the data in the buffer, but it
	 * is named as if it did because it will most often be used in situations
	 * in which that might as well be the case. </p>
	 *
	 * @return  This buffer
	 */
	public final Buffer clear()
	{
		m_Position	= 0;
		m_Limit		= m_Capacity;
		m_Mark		= -1;
		return this;
	}

	/**
	 * Flips this buffer.  The limit is set to the current position and then
	 * the position is set to zero.  If the mark is defined then it is
	 * discarded.
	 *
	 * <p> After a sequence of channel-read or <i>put</i> operations, invoke
	 * this method to prepare for a sequence of channel-write or relative
	 * <i>get</i> operations.  For example:
	 *
	 * <blockquote><pre>
	 * buf.put(magic);    // Prepend header
	 * in.read(buf);      // Read data into rest of buffer
	 * buf.flip();        // Flip buffer
	 * out.write(buf);    // Write header + data to channel</pre></blockquote>
	 *
	 * <p> This method is often used in conjunction with the {@link
	 * java.nio.ByteBuffer#compact compact} method when transferring data from
	 * one place to another.  </p>
	 *
	 * @return  This buffer
	 */
	public final Buffer flip()
	{
		m_Limit		= m_Position;
		m_Position	= 0;
		m_Mark		= -1;
		return this;
	}

	/**
	 * Rewinds this buffer.  The position is set to zero and the mark is
	 * discarded.
	 *
	 * <p> Invoke this method before a sequence of channel-write or <i>get</i>
	 * operations, assuming that the limit has already been set
	 * appropriately.  For example:
	 *
	 * <blockquote><pre>
	 * out.write(buf);    // Write remaining data
	 * buf.rewind();      // Rewind buffer
	 * buf.get(array);    // Copy data into array</pre></blockquote>
	 *
	 * @return  This buffer
	 */
	public final Buffer rewind()
	{
		m_Position  = 0;
		m_Mark		= -1;
		return this;
	}

	/**
	 * Returns the number of elements between the current position and the
	 * limit. </p>
	 *
	 * @return  The number of elements remaining in this buffer
	 */
	public final int remaining()
	{
		return m_Limit - m_Position;
	}

	/**
	 * Tells whether there are any elements between the current position and
	 * the limit. </p>
	 *
	 * @return  <tt>true</tt> if, and only if, there is at least one element
	 *          remaining in this buffer
	 */
	public final boolean hasRemaining()
	{
		return m_Position < m_Limit;

	}

	/**
	 * Checks the current position against the limit, throwing a {@link
	 * BufferUnderflowException} if it is not smaller than the limit, and then
	 * increments the position. </p>
	 *
	 * @return  The current position value, before it is incremented
	 */
	protected final int nextGetIndex()
	{
		if (m_Position >= m_Limit)
			throw new BufferUnderflowException();
		return m_Position++;
	}

    protected final int nextGetIndex(int nb)
	{
		if (m_Position + nb > m_Limit)
			throw new BufferUnderflowException();
		int p = m_Position;
		m_Position += nb;
		return p;
	}

	/**
	 * Checks the current position against the limit, throwing a {@link
	 * BufferOverflowException} if it is not smaller than the limit, and then
	 * increments the position. </p>
	 *
	 * @return  The current position value, before it is incremented
	 */
	protected final int nextPutIndex()
	{
		if (m_Position >= m_Limit)
			throw new BufferOverflowException();
		return m_Position++;
	}

	protected final int nextPutIndex(int nb)
	{
		if (m_Position + nb > m_Limit)
			throw new BufferOverflowException();
		int p = m_Position;
		m_Position += nb;
		return p;
	}

	/**
	 * Checks the given index against the limit, throwing an {@link
	 * IndexOutOfBoundsException} if it is not smaller than the limit
	 * or is smaller than zero.
	 */
	protected final int checkIndex(int i)
	{
		if ( (i<0) || (i >= m_Limit) )
			throw new IndexOutOfBoundsException();
		return i;
	}

	protected final int checkIndex(int i, int nb)
	{
		if ( (i<0) || (i+nb>m_Limit) )
			throw new IndexOutOfBoundsException();
		return i;
	}

	protected final int markValue()
	{
		return m_Mark;
	}

	protected static void checkBounds(int off, int len, int size)
	{
		if ( (off | len | (off+len) | (size - (off+len)) ) < 0)
			throw new IndexOutOfBoundsException();
	}
}
