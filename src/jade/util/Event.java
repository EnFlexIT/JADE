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
import java.util.EventObject;

/**
 * @author Giovanni Caire - TILab 
 */
public class Event extends EventObject {
	protected int type; 
	private Vector param = null;
	
	private boolean processed = false;	
	private Object processingResult = null;
	
	public Event(int type, Object source) {
		super(source);
		this.type = type;
	}
	
	public Event(int type, Object source, Object info) {
		super(source);
		this.type = type;
		addParameter(info);
	}
	
	public int getType() {
		return type;
	}
	
	public void addParameter(Object obj) {
		if (param == null) {
			param = new Vector();
		}
		param.addElement(obj);
	}
	
	public Object getParameter(int index) {
		if (param == null) {
			throw new IndexOutOfBoundsException();
		}
		else {
			return param.elementAt(index);
		}
	}
	
	public synchronized Object waitUntilProcessed() throws InterruptedException {
		while (!processed) {
			wait();
		}
		return processingResult;
	}
	
	public synchronized void notifyProcessed(Object result) {
		if (!processed) {
			processingResult = result;
			processed = true;
			notifyAll();
		}
	}	
	
	public synchronized void resetProcessed() {
		processed = false;
	}
}