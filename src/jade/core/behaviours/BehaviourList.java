/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop multi-agent
systems in compliance with the FIPA specifications.
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

package jade.core.behaviours;

import java.util.*;
import java.io.Serializable;

/**
 * @author Giovanni Caire - Telecom Italia Lab
 * @version $Date$ $Revision$
 */
class BehaviourList implements Serializable {
  	private List theList = new ArrayList();
  	private int current = 0;
  	
  	public int size() {
  		return theList.size();
  	}
  	
  	public boolean isEmpty() {
  		return theList.isEmpty();
  	}
  	
  	/** 
  	 * Add a Behaviour to the tail of the list. This does not require
  	 * adjusting the current index
  	 */
  	public void addElement(Behaviour b) {
  		theList.add(b);
  	}
  	
    /** 
     * Remove b from the list. If b was in the list, return true
     * otherwise return false.
     * This requires adjusting the current index in the following cases:
     * - the index of the removed Behaviour is < than the current index
     * - the removed Behaviour is the current one and it is also 
     * the last element of the list. In this case the current index
     * must be set to 0
     */
  	public boolean removeElement(Behaviour b) {
  		int index = theList.indexOf(b);
    	if(index != -1) {
      		theList.remove(b);
      		if (index < current) {
				--current;
      		}
      		else if (index == current && current == theList.size()) {
      			current = 0;
      		}
    	}
    	return index != -1;
  	}
  	
  	public Behaviour getCurrent() {
  		Behaviour b = null;
  		try {
  			b = (Behaviour) theList.get(current);
  		}
  		catch (IndexOutOfBoundsException ioobe) {
  			// Just do nothing
  		}
  		return b;
  	}
  		
  	public void begin() {
  		current = 0;
  	}
  	
  	public boolean currentIsLast() {
  		return (current == (theList.size() - 1));
  	}
  	
  	/** 
  	 * Advance the current index (taking into account wrap around)
  	 * and return the new current Behaviour
  	 */ 
  	public Behaviour next() {
  		if (currentIsLast() || isEmpty()) {
  			current = 0;
  		}
  		else {
  			current++;
  		}
  		return getCurrent();
  	}
  	
  	public Collection values() {
  		return theList;
  	}
}
  
 