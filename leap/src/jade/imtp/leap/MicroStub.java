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

import jade.core.IMTPException;
import java.util.Vector;
import java.util.Enumeration;
import jade.util.Logger;

/**
 * Class declaration
 * @author Giovanni Caire - TILAB
 */
public class MicroStub {
	protected Dispatcher myDispatcher;
	protected Vector pendingCommands = new Vector();
	private int activeCnt = 0;
	private boolean flushing = false;
	
	public MicroStub(Dispatcher d) {
		myDispatcher = d;
	}
	
	protected Command executeRemotely(Command c) throws IMTPException, ICPException {
		try {
			byte[] cmd = SerializationEngine.serialize(c);
			byte[] rsp = myDispatcher.dispatch(cmd);
			Command r = SerializationEngine.deserialize(rsp);
			if (r.getCode() == Command.ERROR) {
				if (!((Boolean) r.getParamAt(0)).booleanValue()) {
					// Unexpected exception thrown in the remote site
					String msg = new String("Exception "+(String) r.getParamAt(1)+" occurred in remote site. "+(String) r.getParamAt(2));
					Logger.println(msg);
      		throw new IMTPException(msg);
				}
				else if (((String) r.getParamAt(1)).equals("jade.core.IMTPException")) {
					throw new IMTPException((String) r.getParamAt(2));
				}
			}
			return r;
		}
		catch (LEAPSerializationException lse) {
			throw new IMTPException("Serialization error", lse);
		}
	}
	
	protected void store(Command c) {
		pendingCommands.addElement(c);
	}
	
	public void flush() {
		if (pendingCommands.size() > 0) {
			// This is called by the main thread of the underlying EndPoint
			// --> The actual flushing must be done asynchronously to avoid
			// deadlock
			Thread t = new Thread() {
				public void run() {
					// 1) Lock the buffer of pending commands to avoid calling 
					// remote methods while flushing
					synchronized (pendingCommands) {
						while (activeCnt > 0) {
							try {
								pendingCommands.wait();
							}
							catch (InterruptedException ie) {
							}
						}
						flushing = true;
					}
					
					// Flush the buffer of pending commands
					Enumeration e = pendingCommands.elements();
					while (e.hasMoreElements()) {
						Command c = (Command) e.nextElement();
						// Exceptions and return values of commands whose delivery
						// was delayed for disconnection problems can and must not
						// be handled!!!
						try {
							Command r = executeRemotely(c);
							if (r.getCode() == Command.ERROR) {
								Logger.println("WARNING: Exception in command asynchronous delivery. "+r.getParamAt(2));
							}
						}
						catch (Exception ex) {
							Logger.println("WARNING: Error in command asynchronous delivery. "+ex.getMessage());
						}
					}
					pendingCommands.removeAllElements();
					
					// 3) Unlock the buffer of pending commands
					synchronized (pendingCommands) {
						flushing = false;
						pendingCommands.notifyAll();
					}
				}
			};
			t.start();
		}
	}
	
	protected void disableFlush() {
		synchronized (pendingCommands) {
			while (flushing) {
				try {
					pendingCommands.wait();
				}
				catch (InterruptedException ie) {
				}
			}
			activeCnt++;
		}
	}
		
	protected void enableFlush() {
		synchronized (pendingCommands) {
			activeCnt--;
			if (activeCnt == 0) {
				pendingCommands.notifyAll();
			}
		}
	}		
}

