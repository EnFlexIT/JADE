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

import java.lang.reflect.*;

import java.io.Serializable;
import java.io.Reader;
import java.io.Writer;
import java.io.IOException;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.Iterator;

import jade.core.CaseInsensitiveString;

/**
@author Giovanni Rimassa - Universita` di Parma
@version $Date$ $Revision$
*/
class FrameSchema implements Cloneable, Serializable {

  static class WrongSlotTypeException extends OntologyException {
    public WrongSlotTypeException(String functorName, String slotName, String slotType) {
      super("No slot of type " + slotType + " named " + slotName + " in functor " + functorName);
    }
  }

  private Ontology myOntology;
  private CaseInsensitiveString myName;
  private List slots;


  public FrameSchema(Ontology o, String n) {
    myOntology = o;
    myName = new CaseInsensitiveString(n);
    slots = new ArrayList();
  }

  public String getName() {
    return myName.toString();
  }

  public void addSlot(SlotDescriptor td) {
    slots.add(td);
  }


	private boolean isGoodPrimitiveType(String required, Object current) {
  	try { 
    	return (Class.forName(required).isInstance(current));
  	} catch (Exception e) {
    	e.printStackTrace();
    	return false;
  	}
	}


  public void checkAgainst(Frame f) throws OntologyException {
    for(int i = 0; i < slots.size(); i++) {
      SlotDescriptor sd = (SlotDescriptor)slots.get(i);
      String name = sd.getName();
      if(!sd.isOptional()) {
				Object o = f.getSlot(name); // If a slot called name is not present in f, this method throws an exception
				if (sd.isPrimitive()) {
	  			if (!isGoodPrimitiveType(sd.getType(),o))
	    			throw new WrongSlotTypeException(f.getName(), name, sd.getType()); 
				} 
				else {
					// If the slot is not primitive than its value must be an instance of Frame
					// In this case recursively check that frame
	  			if(!(o instanceof Frame))
	    			throw new WrongSlotTypeException(f.getName(), name, "Frame");
	  			myOntology.check((Frame)o);
				}

      } // End of 'if slot is not optional'

    } // End of slot iteration

  }

  Iterator subSchemas() {
    return slots.iterator();
  }

  SlotDescriptor[] slotsArray() {
    Object[] objs = slots.toArray();
    SlotDescriptor[] result = new SlotDescriptor[objs.length];
    System.arraycopy(objs, 0, result, 0, objs.length);
    return result;
  }

}

