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


package jade.onto;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

/**
  Representation of an ontological entity as a set of untyped slots.
  This class can hold different slots, keeping track both their unique name and
  their position.

  @author Giovanni Rimassa - Universita` di Parma
  @version $Date$ $Revision$
*/
public class Frame {

  static class NoSuchSlotException extends OntologyException {
    public NoSuchSlotException(String frameName, String slotName) {
      super("No slot named " + slotName + " in frame " + frameName);
    }
  }

  private String myName;
  private Map slotsByName;
  private List slotsByPosition;

  /**
    Creates a new frame with the given name.
    @param name The name of this frame.
  */
  public Frame(String name) {
    myName = name;
    slotsByName = new HashMap();
    slotsByPosition = new ArrayList();
  }

  /**
    Reads the name of this frame.
    @return The name of this frame, as was set by the constructor.
  */
  public String getName() {
    return myName;
  }

  /**
    Adds a named slot to this frame.
    @param name The unique name of the new slot
    @param value A Java object that will be associated with the given name.
  */
  public void putSlot(String name, Object value) {
    slotsByName.put(new Name(name), value);
    slotsByPosition.add(value);
  }

  /**
    Adds an unnamed slot to this frame. The new slot has no name, and its
    position is determined by the number of slots of this frame at the time of
    the call. The given Java object is put at the end of the slot sequence.  
    @param value A Java object that will be associated with the given position.
  */
  public void putSlot(Object value) {
    // generate a name with an underscore followed by the position number
    String dummyName = "_" + Integer.toString(slotsByPosition.size());

    // Add more underscores as needed
    while(slotsByName.containsKey(dummyName))
      dummyName = "_" + dummyName;

    putSlot(dummyName, value);

  }

  /**
    Retrieves a named slot from this frame, by name.
    @param name The name of the desired slot.
    @return The value of that slot 
    @exception OntologyException If no suitable slot exists.
  */
  public Object getSlot(String name) throws OntologyException {
    Object result = slotsByName.get(new Name(name));
    if(result == null)
      throw new NoSuchSlotException(myName, name);
    return result;
  }

  /**
    Retrieves an unnamed slot from this frame, by position.
    @param position The position of the desired slot.
    @return The value of that slot 
    @exception OntologyException If no suitable slot exists.
  */
  public Object getSlot(int position) throws OntologyException { 
    try {
      return slotsByPosition.get(position);
    }
    catch(IndexOutOfBoundsException ioobe) {
      throw new NoSuchSlotException(myName, "@" + position);
    }
  }

  final Iterator terms() {
    return slotsByPosition.iterator();
  }

}

