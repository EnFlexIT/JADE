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
  Descriptor class for the slots of ontological concepts. Instances of this
  class are used to describe the slots of the various entities of an ontology
  (i.e. <it>Concepts</it>).

  @author Giovanni Rimassa - Universita` di Parma
  @version $Date$ $Revision$
*/
public class TermDescriptor {

  private Name myName;
  private int type;
  private String typeName; // indicates the type of the elements of a set/sequence
  private boolean optionality;

  /**
    Build the descriptor for a named slot of a complex type. This constructor
    can be used to describe a named slot, whose type is one among
    <code>Ontology.FRAME_TERM , Ontology.SET_TERM, Ontology.SEQUENCE_TERM,
    Ontology.CONSTANT_TERM, Ontology.ANY_TERM</code>. 
    @param n The name of the described slot.
    @param t A symbolic constant to identify the type of the slot (i.e. 
    one value between <code>
    Ontology.FRAME_TERM , Ontology.SET_TERM, Ontology.SEQUENCE_TERM,
    Ontology.CONSTANT_TERM, Ontology.ANY_TERM</code>. )
    @param vt The name of the type of the values allowed for this slot, (i.e.
    one value between <code> Ontology.STRING_TYPE, Ontology.XXX_TYPE </code>,
    or, in case of a FRAME_TERM, the name of the role played by this slot 
    in the
    ontology.
    @param o One of <code>Ontology.M</code> (for mandatory slots) and
    <code>Ontology.O</code> (for optional slots).
  */
  public TermDescriptor(String n, int t, String vt, boolean o) {
    myName = new Name(n);
    type = t;
    typeName = vt;
    optionality = o;
  }



  /**
    Build the descriptor for an unnamed slot. 
    @see TermDescriptor(String n, int t, String vt, boolean o)
  */
  public TermDescriptor(int t, String vt, boolean o) {
    this("",t,vt,o);
  }



  /**
    Get the name of a slot.
    @return The name of the slot described by this object, as set by the
    constructor, or <code>""</code> if the slot is unnamed.
  */
  public String getName() {
    return myName.toString();
  }

  /**
    Get the type of a slot.
    @return A symbolic constant, representing the type of the slot described by
    this object, as set by the constructor.
  */
  public int getType() {
    return type;
  }

  /**
    Get the name of the type of the values of this slot.
    @return The name of the type of the values for this slot described by this object, as set
    by the constructor. For primitive types, the name of the type is returned
    (e.g. <code>java.lang.Integer</code> or <code>java.lang.String</code>); for complex types, the name
    of the specific concept is returned.
  */
  public String getValueType() {
    return typeName;
  }

  /**
   * @return true if the values of this slot assumes primitive types (e.g. Integer, String, ...)
   */
public boolean hasPrimitiveTypeValues() {
  return primitiveTypes.contains(typeName);
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
    in its frame, <code>false</code> otherwise.
  */
  public boolean isOptional() {
    return optionality;
  }

   /**
    Tells whether a slot is complex.
    @return <code>true</code> if the slot described by this object is of a
    <code>Ontology.FRAME_TERM</code>
    <code>false</code> otherwise.
  */
  public boolean isConcept() {
    return (type == Ontology.FRAME_TERM); 
  }

   /**
    Tells whether a slot is set or a sequence.
    @return <code>true</code> if the slot described by this object is 
    <code>Ontology.SET_TERM</code> or <code>Ontology.SEQUENCE_TERM</code>)
    <code>false</code> otherwise.
  */
  public boolean isSet() {
    return (type == Ontology.SET_TERM) || (type == Ontology.SEQUENCE_TERM);
  }

  /**
   * Tells whethet the slot is a CONSTANT_TERM
   */
public boolean isConstant() {
  return (type == Ontology.CONSTANT_TERM);
}

  void setName(String n) {
    myName = new Name(n);
  }

  /**
   * return a String representation of the object, just for debugging purposes
   **/
public String toString() {
return myName.toString()+" of type "+type+" and type of values "+typeName+" and optionality "+optionality;
}
}

