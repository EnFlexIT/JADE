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
 
import java.util.Vector;
//#MIDP_EXCLUDE_BEGIN
import java.util.EventObject;
//#MIDP_EXCLUDE_END

/**
 * This class represents a generic event carrying some information
 * (accessible in the form of <code>Object</code> parameters) and 
 * provides support for synchronous processing through the 
 * <code>waitUntilProcessed()</code> and <code>notifyProcessed()</code>
 * methods.
 * @author Giovanni Caire - TILab 
 */
 public class Event 
    //#MIDP_EXCLUDE_BEGIN
 		extends EventObject
    //#MIDP_EXCLUDE_END
    {
    	
	/*#MIDP_INCLUDE_BEGIN
  protected Object source;
	#MIDP_INCLUDE_END*/
	protected int type; 
	private Vector param = null;
	
	private boolean processed = false;	
	private Object processingResult = null;
	
	/**
	   Construct an <code>Event</code> of a given type produced by
	   the indicated source
	   @param type The type of the event
	   @param source The source that generated the event
	 */
	public Event(int type, Object source) {
    //#MIDP_EXCLUDE_BEGIN
		super(source);
    //#MIDP_EXCLUDE_END    	
		/*#MIDP_INCLUDE_BEGIN
  	this.source = source;
		#MIDP_INCLUDE_END*/
		this.type = type;
	}
	
	/**
	   Construct an <code>Event</code> of a given type produced by
	   the indicated source and carrying a given information.
	   @param type The type of the event
	   @param source The source that generated the event
	   @param info The information associated to the event. This value
	   is handled as the first parameter of the event and can be 
	   accessed using the <code>getParameter(0)</code> method
	 */
	public Event(int type, Object source, Object info) {
		this(type, source);
		addParameter(info);
	}
	
	/*#MIDP_INCLUDE_BEGIN
  public Object getSource() {
  	return source;
  }
	#MIDP_INCLUDE_END*/
	
	/**
	   @return the type of this <code>Event</code> object
	 */
	public int getType() {
		return type;
	}
	
	/**
	   Add a parameter to this <code>Event</code> object
	   @param obj The parameter to be added
	 */
	public void addParameter(Object obj) {
		if (param == null) {
			param = new Vector();
		}
		param.addElement(obj);
	}
	
	/**
	   @return the index-th parameter of this <code>Event</code> object
	 */
	public Object getParameter(int index) {
		if (param == null) {
			throw new IndexOutOfBoundsException();
		}
		else {
			return param.elementAt(index);
		}
	}
	
	/**
	   Blocks the calling thread until the <code>notifyProcessed()</code>
	   method is called.
	   @return the result of the processing of this <code>Event</code> 
	   object as set by the <code>notifyProcessed()</code> method.
	 */
	public synchronized Object waitUntilProcessed() throws InterruptedException {
		while (!processed) {
			wait();
		}
		return processingResult;
	}
	
	/**
	   Wakes up threads waiting for the processing of this <code>Event</code>
	   object within the <code>waitUntilProcessed()</code> method.
	   @param result The result of the processing. This value is passed
	   to the waked threads as the result of the <code>waitUntilProcessed()</code> 
	   method.
	 */
	public synchronized void notifyProcessed(Object result) {
		if (!processed) {
			processingResult = result;
			processed = true;
			notifyAll();
		}
	}	
	
	/**
	   Reset the "processed" status of this <code>Event</code>
	 */
	public synchronized void resetProcessed() {
		processed = false;
		processingResult = null;
	}
}