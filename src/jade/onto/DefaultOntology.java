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

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

/**
  A simple implementation of the <code>Ontology</code> interface. Instances of
  this class keeps all the ontology data in memory, and don't support an
  external archive format.

  @author Giovanni Rimassa - Universita` di Parma
  @version $Date$ $Revision$
*/
public final class DefaultOntology implements Ontology {

  private static final List primitiveTypes = new ArrayList(11);

  static {
    primitiveTypes.add(BOOLEAN_TYPE, Boolean.TYPE);
    primitiveTypes.add(BYTE_TYPE, Byte.TYPE);
    primitiveTypes.add(CHARACTER_TYPE, Character.TYPE);
    primitiveTypes.add(DOUBLE_TYPE, Double.TYPE);
    primitiveTypes.add(FLOAT_TYPE, Float.TYPE);
    primitiveTypes.add(INTEGER_TYPE, Integer.TYPE);
    primitiveTypes.add(LONG_TYPE, Long.TYPE);
    primitiveTypes.add(SHORT_TYPE, Short.TYPE);
    primitiveTypes.add(STRING_TYPE, String.class);
    primitiveTypes.add(BINARY_TYPE, (new byte[0]).getClass());
    primitiveTypes.add(ANY_TYPE, (new Object()).getClass());
  }


  /** Symbolic constant identifying a frame representing an action **/ 
  public static String NAME_OF_ACTION_FRAME = "action";
  /** Symbolic constant identifying a slot representing an actor **/ 
  public static String NAME_OF_ACTOR_SLOT = Frame.UNNAMEDPREFIX+".ACTION.actor";
  /** Symbolic constant identifying a slot representing an action **/ 
  public static String NAME_OF_ACTION_SLOT = Frame.UNNAMEDPREFIX+".ACTION.action";

  // Special slot, all actions must have it.
  private static final TermDescriptor actorSlot = new TermDescriptor(NAME_OF_ACTOR_SLOT, ANY_TYPE, M);

  private Map schemas;
  private Map factories;

  /**
    Default constructor.
  */
  public DefaultOntology() {
    schemas = new HashMap();
    factories = new HashMap();
  }

  // Raw interface, exposes Frame Schemas directly.
  // This is package scoped, not to be used by applications.

  FrameSchema lookupSchema(String name) {
    return (FrameSchema)schemas.get(new Name(name));
  }

  RoleFactory lookupFactory(String name) {
    return (RoleFactory)factories.get(new Name(name));
  }

  /**
    Adds a new frame to this ontology, without an user defined Java class to
    represent it.
    @see jade.onto.Ontology#addFrame(String conceptName, int kind, TermDescriptor[] slots)
  */
  public void addFrame(String conceptName, int kind, TermDescriptor[] slots) throws OntologyException {

    if(kind != CONCEPT_TYPE) 
      throw new OntologyException("Error: Unknown kind of Frame requested");

    FrameSchema fs = new FrameSchema(this, conceptName, kind);

    for(int i = 0; i < slots.length; i++) {
      String n = slots[i].getName();
      if(n.length() == 0)
	slots[i].setName("_" + i);
      fs.addTerm(slots[i]);
    }

    addSchemaToTable(conceptName, fs);

  }

  /**
    Adds a new frame to this ontology, with an user defined Java class to
    represent it.
    @see jade.onto.Ontology#addFrame(String conceptName, int kind, TermDescriptor[] slots, RoleFactory rf)
  */  
  public void addFrame(String conceptName, int kind, TermDescriptor[] slots, RoleFactory rf) throws OntologyException {
    addFrame(conceptName, kind, slots);

    Class c = rf.getClassForRole();
    checkClass(conceptName, c);
    addFactoryToTable(conceptName, rf);
  }

  /**
    Creates a Java object from the given frame.
    @see jade.onto.Ontology#createObject(Frame f)
  */
  public Object createObject(Frame f) throws OntologyException {

    String roleName = f.getName();
    RoleFactory fac = lookupFactory(roleName);

    if(fac == null)
      throw new OntologyException("No class able to play " + roleName + " role. Check the definition of the ontology.");

    Class c = fac.getClassForRole();

    Object o = fac.create(f);
    return initObject(f, o, c);

  }

  /**
    Creates a frame from a given Java Object, playing a given role.
    @see jade.onto.Ontology#createFrame(Object o, String roleName)
  */
  public Frame createFrame(Object o, String roleName) throws OntologyException {
    RoleFactory rf = lookupFactory(roleName);
    if(rf == null)
      throw new OntologyException("No class able to play " + roleName + " role.Check the definition of the ontology.");

    Class theConceptClass = rf.getClassForRole();
    if(!theConceptClass.isInstance(o))
      throw new OntologyException("The object <" + o + "> is not an instance of " + theConceptClass.getName() + " class.");

    FrameSchema fs = lookupSchema(roleName);
    if(fs == null)
      throw new OntologyException("Internal error: inconsistency between schema and class table");
    if(!fs.isConcept())
      throw new OntologyException("The role " + roleName + " is not a concept in this ontology.");

    Frame f = new Frame(roleName);
    buildFromObject(f, fs, o, theConceptClass);

    return f;
  }

  /**
    Checks whether a given frame is correct with respect to this ontology.
    @see jade.onto.Ontology#check(Frame f)
  */
  public void check(Frame f) throws OntologyException {
    String roleName = f.getName();
    FrameSchema fs = lookupSchema(roleName);
    fs.checkAgainst(f);
  }

  /**
    Checks whether a given Java object is correct with respect to the given role
    in this ontology.
    @see jade.onto.Ontology#check(Object o, String roleName)
  */
  public void check(Object o, String roleName) throws OntologyException {
    /*
     *  Algorithm: 
     *
     * - Check that the object is an instance of the correct class
     * - FOR EACH term t
     *   - The Object get method must not return 'null'
     */

    RoleFactory rf = lookupFactory(roleName);
    if(rf == null)
      throw new OntologyException("No class able to play " + roleName + " role.");

    Class implementationClass = rf.getClassForRole();
    if(!implementationClass.isInstance(o))
      throw new OntologyException("The object is not an instance of " + implementationClass.getName() + " class.");

    FrameSchema fs = lookupSchema(roleName);
    Iterator it = fs.subSchemas();

    while(it.hasNext()) {

      TermDescriptor desc = (TermDescriptor)it.next();
	Method m = findMethodCaseInsensitive("get" + translateName(desc.getName()), implementationClass.getMethods());
	try {
	  Object value = m.invoke(o, new Object[] { });

	  if(!desc.isOptional() && (value == null))
	    throw new OntologyException("The given object has a 'null' value for the mandatory term " + desc.getName());

	  if(desc.isComplex()) // Recursive check for subobjects
	    check(value, desc.getTypeName());

	}
	catch(InvocationTargetException ite) {
	  String msg = ite.getTargetException().getMessage();
	  throw new OntologyException("Internal error: a reflected method threw an exception.\nMessage was " + msg);
	}
	catch(IllegalAccessException iae) {
	  throw new OntologyException("Internal error: the required method is not accessible [" + iae.getMessage() + "]");
	}
	catch(SecurityException se) {
	  throw new OntologyException("Wrong class: some required method is not accessible."); 
	}
    }
  }

  /**
    Checks whether a given frame is a concept in this ontology.
    @see jade.onto.Ontology#isConcept(String roleName)
  */
  public boolean isConcept(String roleName) throws OntologyException {
    FrameSchema fs = lookupSchema(roleName);
    if(fs == null)
      throw new OntologyException("No schema was found for " + roleName + "role.");
    return fs.isConcept();
  }


  /**
    Get the descriptions for all the slots that made the structure of a given
    ontological role.
    @see jade.onto.Ontology#getTerms(String roleName)
  */
  public TermDescriptor[] getTerms(String roleName) throws OntologyException {
    FrameSchema fs = lookupSchema(roleName);
    return fs.termsArray();
  }



  // Private methods.


  private String translateName(String name) {
    StringBuffer buf = new StringBuffer(name);
    for(int i = 0; i < buf.length(); i++) {
      char c = buf.charAt(i);
      switch(c) {
      case ':':
	buf.deleteCharAt(i);
	--i;
	break;
      case '-':
	buf.deleteCharAt(i);
	buf.setCharAt(i, Character.toUpperCase(buf.charAt(i)));
	--i;
	break;
      }
    }
    
    return new String(buf);
  }


  private Class checkGetAndSet(String name, Method[] methods) throws OntologyException {
    Class result;
    Method getMethod = findMethodCaseInsensitive("get" + name, methods);
    Method setMethod = findMethodCaseInsensitive("set" + name, methods);

    // Make sure "get" method takes no arguments.
    Class[] getParams = getMethod.getParameterTypes();
    if(getParams.length > 0)
      throw new OntologyException("Wrong class: method " +  getMethod.getName() + "() must take no arguments.");

    // Now find a matching set method.
    result = getMethod.getReturnType();

    Class[] setParams = setMethod.getParameterTypes();
    if((setParams.length != 1) || (!setParams[0].equals(result)))
      throw new OntologyException("Wrong class: method " +  setMethod.getName() + "() must take a single argument of type " + result.getName() + ".");
    Class setReturn = setMethod.getReturnType();
    if(!setReturn.equals(Void.TYPE))
      throw new OntologyException("Wrong class: method " +  setMethod.getName() + "() must return void.");

    return result;

  }

  private void checkClass(String roleName, Class c) throws OntologyException {

    FrameSchema fs = lookupSchema(roleName);
    if(fs == null)
      throw new OntologyException("No schema was found for " + roleName + "role.");

    Iterator it = fs.subSchemas();
    while(it.hasNext()) {
      TermDescriptor desc = (TermDescriptor)it.next();

      String termName = translateName(desc.getName());
      try {
	Method[] methods = c.getMethods();

	// Check for correct set and get methods for the current
	// descriptor and retrieve the implementation type.
	Class implType = checkGetAndSet(termName, methods);

	// If the descriptor is a complex term (Concept)
	// and some class C is registered for that role,
	// then the implementation type must be a supertype of C.
	if(desc.isComplex()) {
	  RoleFactory rf = lookupFactory(desc.getTypeName());
	  if(rf != null) {
	    Class roleType = rf.getClassForRole();
	    if(!implType.isAssignableFrom(roleType))
	      throw new OntologyException("Wrong class: the " + desc.getName() + " role is played by " + roleType + " class, which is not a subtype of " + implType + " class.");
	  }
	}
	else {	// Check that the returned type is compatible with the one dictated by the TermDescriptor
	  Class primitive = (Class)primitiveTypes.get(desc.getType());
	  if(!implType.isAssignableFrom(primitive))
	    throw new OntologyException("Wrong class: the primitive term " + desc.getName() + " is of type "+ primitive + ", but must be a subtype of " + implType + " class.");
	}
      }
      catch(SecurityException se) {
	throw new OntologyException("Wrong class: some required method is not accessible."); 
      }

    }

  }


  private Object initObject(Frame f, Object concept, Class theConceptClass) throws OntologyException {

    String roleName = f.getName();

    FrameSchema fs = lookupSchema(roleName);
    Iterator it = fs.subSchemas();

    while(it.hasNext()) {
      TermDescriptor desc = (TermDescriptor)it.next();
      String slotName = desc.getName();
      String methodName = "set" + translateName(slotName);

      // Retrieve the modifier method from the class and call it
      Method setMethod = findMethodCaseInsensitive(methodName, theConceptClass.getMethods());
      try {

	Object slotValue = f.getSlot(slotName);
	
	// For complex slots, transform from sub-frame to sub-object.
	// This is performed calling createObject() recursively.
	if(desc.isComplex())
	  slotValue = createObject((Frame)slotValue);

	  
	setMethod.invoke(concept, new Object[] { slotValue });

      }
      catch(Frame.NoSuchSlotException fnsse) { // Ignore 'No such slot' errors for optional slots
	if(!desc.isOptional())
	  throw fnsse;
      }
      catch(InvocationTargetException ite) {
	Throwable e = ite.getTargetException();
	e.printStackTrace();
	throw new OntologyException("Internal error: a reflected method threw an exception.\n e.getMessage()");
      }
      catch(IllegalAccessException iae) {
	throw new OntologyException("Internal error: the required method is not accessible [" + iae.getMessage() + "]");
      }
      catch(SecurityException se) {
	throw new OntologyException("Wrong class: some required method is not accessible."); 
      }
      catch(IllegalArgumentException iare){
      throw new OntologyException("Possible mismatch between the type returned by the parser and the type declared in the ontology [" + iare.getMessage() + "]");	
      }
    }

    return concept;
  }


  private void buildFromObject(Frame f, FrameSchema fs, Object o, Class c) throws OntologyException {
    Iterator it = fs.subSchemas();
    while(it.hasNext()) {
      TermDescriptor desc = (TermDescriptor)it.next();
      String name = desc.getName();
      String methodName = translateName(name);

      // Retrieve the accessor method from the class and call it
      Method getMethod = findMethodCaseInsensitive("get" +methodName, c.getMethods());
      try {
	Object value = getMethod.invoke(o, new Object[] { });

	// Now set the corresponding frame subterm appropriately
	if(!desc.isComplex()) { // For elementary terms, just put the Object as a slot
	  f.putSlot(name, value);
	}
	else { // For complex terms, do a name lookup and call createFrame() recursively
	  String roleName = desc.getTypeName();
	  f.putSlot(name, createFrame(value, roleName));
	}
      }
      catch(InvocationTargetException ite) {
	String msg = ite.getTargetException().getMessage();
	throw new OntologyException("Internal error: a reflected method threw an exception.\nMessage was " + msg);
      }
      catch(IllegalAccessException iae) {
	throw new OntologyException("Internal error: the required method is not accessible [" + iae.getMessage() + "]");
      }
      catch(SecurityException se) {
	throw new OntologyException("Wrong class: some required method is not accessible."); 
      }

    }

  }


  private void addSchemaToTable(String roleName, FrameSchema fs) {
    schemas.put(new Name(roleName), fs);
  }


  private void addFactoryToTable(String roleName, RoleFactory rf) {
    factories.put(new Name(roleName), rf);
  }


  private Method findMethodCaseInsensitive(String name, Method[] methods) throws OntologyException {

    for(int i = 0; i < methods.length; i++) {
      String ithName = methods[i].getName();
      if(ithName.equalsIgnoreCase(name))
	return methods[i];
    }
    throw new OntologyException("Method " + name + " not found.");

  }


}
