/**
 * JADE - Java Agent DEvelopment Framework is a framework to develop
 * multi-agent systems in compliance with the FIPA specifications.
 * Copyright (C) 2000 CSELT S.p.A. 
 * Copyright (C) 2001,2002 TILab S.p.A. 
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
 */

package jade.util;
 
/**
 * This class provides support for  
 * synchronizing threads acting on a generic resource in such a way that 
 * - If a thread is writing the resource no other thread can act on it 
 * in any way
 * - Several threads can read the resource at the same time
 * - If one or more threads are reading the resource no thread can write it
 * @author Giovanni Caire - TILab 
 */
public class RWLock {
	// The counter of threads currently reading the resource
	private int readersCnt = 0;
	
	// The Thread currently writing the resource (there can only be
	// one such a therad at a given time)
	private Thread currentWriter = null;
	
	// writeLock()/unlock() can be nested. This indicates the current 
	// depth
	private int writeLockDepth = 0;
	
	public synchronized void writeLock() {
		Thread me = Thread.currentThread();
		while ((currentWriter != null && currentWriter != me) || readersCnt > 0) {
			// Someone (not me) is writing the resource OR
			// There are one or more Threads reading the resource
			// --> Go to sleep
			try {
				wait();
			}
			catch (InterruptedException ie) {
				System.out.println("Unexpected interruption. "+ie.getMessage());
			}
		}
		writeLockDepth++;
		if (writeLockDepth == 1) {
			currentWriter = me;
			onWriteStart();
		}
	}
	
	public synchronized void writeUnlock() {
		if (Thread.currentThread() == currentWriter) {
			writeLockDepth--;
			if (writeLockDepth == 0) {
				// I have finished writing the resource --> Wake up hanging threads
				currentWriter = null;
				notifyAll();
				onWriteEnd();
			}
		}
	}
	
	public synchronized void readLock() {
		while (currentWriter != null) {
			// Someone is writing the resource --> Go to sleep
			try {
				wait();
			}
			catch (InterruptedException ie) {
				System.out.println("Unexpected interruption. "+ie.getMessage());
			}
		}
		readersCnt++;
	}
	
	public synchronized void readUnlock() {
		readersCnt--;
		if (readersCnt == 0) {
			// No one is reading the resource anymore --> Wake up threads
			// waiting to write it
			notifyAll();
		}
	}
	
	protected void onWriteStart() {
	}
	
	protected void onWriteEnd() {
	}
}