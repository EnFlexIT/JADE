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
import java.util.*;

/**
  Descriptor class for the slots of ontological roles. Instances of this
  class are used to describe the characteristics of the slots of a 
  role in an ontology.
 
  @author Giovanni Rimassa - Universita` di Parma
  @version $Date$ $Revision$
*/
public class SlotDescriptor {

  private Name name;
  private int category; // indicates if the value of the slot is a primitive entity, and instance of a given role, a set or a sequence
  private String type; // indicates the type of the elements of a set/sequence
  private boolean optionality;

  /**
    Build the descriptor for a named slot.  
    @param n The name of the described slot.
    @param c A symbolic constant identifying the category of the slot (i.e. 
    one value between <code>
    Ontology.FRAME_SLOT , Ontology.SET_SLOT, Ontology.SEQUENCE_SLOT,
    Ontology.PRIMITIVE_SLOT, Ontology.ANY_SLOT</code>. )
    @param t The name of the type of the values allowed for this slot, (i.e.
    one value between <code> Ontology.STRING_TYPE, Ontology.XXX_TYPE </code>,
    or, in case of a FRAME_SLOT, the name of the role in the ontology the value 
    of this slot is an instance of
    @param o One of <code>Ontology.M</code> (for mandatory slots) and
    <code>Ontology.O</code> (for optional slots).
  */
  public SlotDescriptor(String n, int c, String t, boolean o) {
    name = new Name(n);
    category = c;
    type = t;
    optionality = o;
  }



  /**
    Build the descriptor for an unnamed slot. 
    @see jade.onto#SlotDescriptor(String n, int t, String vt, boolean o)
  */
  public SlotDescriptor(int c, String t, boolean o) {
    this("",c,t,o);
  }



  /**
    Get the name of a slot.
    @return The name of the slot described by this object, as set by the
    constructor, or <code>""</code> if the slot is unnamed.
  */
  public String getName() {
    return name.toString();
  }

  /**
    Get the category of a slot.
    @return A symbolic constant, representing the category of the slot described by
    this object, as set by the constructor.
  */
  public int getCategory() {
    return category;
  }

  /**
    Get the name of the type of the values of this slot.
    @return The name of the type of the values for this slot described by this object, as set
    by the constructor. For primitive types, the name of the type is returned
    (e.g. <code>java.lang.Integer</code> or <code>java.lang.String</code>); for complex types, the name
    of the specific concept is returned.
  */
  public String getType() {
    return type;
  }

  /**
  	@return true if the values of this slot assumes primitive types (e.g. Integer, String, ...)
  */
	public boolean hasPrimitiveType() {
  	return primitiveTypes.contains(type);
	}

  /** static List of primitive types */
  static final List primitiveTypes = new ArrayList(12);
  static { 
    primitiveTypes.add(Ontology.BOOLEAN_TYPE);
    primitiveTypes.add(Ontology.BYTE_TYPE);
    primitiveTypes.add(Ontology.CHARACTER_TYPE);
    primitiveTypes.add(Ontology.DOUBLE_TYPE);
    primitiveTypes.add(Ontology.FLOAT_TYPE);
    primitiveTypes.add(Ontology.INTEGER_TYPE);
    primitiveTypes.add(Ontology.LONG_TYPE);
    primitiveTypes.add(Ontology.SHORT_TYPE);
    primitiveTypes.add(Ontology.STRING_TYPE);
    primitiveTypes.add(Ontology.BINARY_TYPE);
    primitiveTypes.add(Ontology.DATE_TYPE);
    primitiveTypes.add(Ontology.ANY_TYPE);
  }


  /**
    Tells whether a slot is optional.
    @return <code>true</code> if the slot described by this object is optional
    in its ontological role, <code>false</code> otherwise.
  */
  public boolean isOptional() {
    return optionality;
  }

  /**
    Tells whether a slot is complex.
    @return <code>true</code> if the category of the slot described by this 
    object is <code>Ontology.FRAME_SLOT</code>
    <code>false</code> otherwise.
  */
  public boolean isComplex() {
    return (category == Ontology.FRAME_SLOT); 
  }

  /**
    Tells whether a slot is set or a sequence.
    @return <code>true</code> if the category of the slot described by this 
    object is <code>Ontology.SET_SLOT</code> or <code>Ontology.SEQUENCE_SLOT</code>)
    <code>false</code> otherwise.
  */
  public boolean isSet() {
    return (category == Ontology.SET_SLOT) || (category == Ontology.SEQUENCE_SLOT);
  }

  /**
  	Tells whethet the slot is primitive.
    @return <code>true</code> if the category of the slot described by this 
    object is <code>Ontology.PRIMITIVE_SLOT</code>
    <code>false</code> otherwise.
  */
	public boolean isPrimitive() {
  	return (category == Ontology.PRIMITIVE_SLOT);
	}

  void setName(String n) {
    name = new Name(n);
  }

  /**
  	return a String representation of the object, just for debugging purposes
  */
	public String toString() {
		return name.toString()+" of category "+category+" and type "+type+" and optionality "+optionality;
	}
}
