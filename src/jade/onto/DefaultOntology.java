package jade.onto;

import java.lang.reflect.*;

import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;

public class DefaultOntology implements Ontology {

  private static final Map wrapperTypes = new HashMap(8);

  static {
    wrapperTypes.put(Boolean.class, Boolean.TYPE);
    wrapperTypes.put(Byte.class, Byte.TYPE);
    wrapperTypes.put(Character.class, Character.TYPE);
    wrapperTypes.put(Double.class, Double.TYPE);
    wrapperTypes.put(Float.class, Float.TYPE);
    wrapperTypes.put(Integer.class, Integer.TYPE);
    wrapperTypes.put(Long.class, Long.TYPE);
    wrapperTypes.put(Short.class, Short.TYPE);
  }

  private Map schemas;
  private Map classes;

  public DefaultOntology() {
    schemas = new HashMap();
    classes = new HashMap();
  }

  // Raw interface, exposes Frame Schemas directly.
  // This is package scoped, not to be used by applications.

  FrameSchema lookupSchema(String name) {
    return (FrameSchema)schemas.get(new Name(name));
  }

  Class lookupClass(String name) {
    return (Class)classes.get(new Name(name));
  }

  // Higher level interface, allows direct usage of user-defined
  // classes but requires a compliance to some rules (that represent
  // the metaclass of the user class.

  /*************************************************************

  Rules for a concept class:

  1) Must have a public constructor taking a Frame and an Ontology as parameters

  2) For every Frame slot named "slot", of type T, the class must have
     two public methods:

       void setSlot(T value)
       T getSlot()

  3) The two methods above must be such that, for each obj of a
     concept class and for each object v of type T, after the fragment
  
       obj.setSlot(v);
       T v2 = obj.getSlot();

     the postCondition 'v.equals(v2)' must be true

  4) For every sub-frame of the Frame,  (i.e. a slot not of
     boolean, int, double, String, byte[]), named "SubFrame", the
     class F must itself obey to these rules (i.e. it must be a valid
     Concept class).

  **************************************************************/

  public void addConcept(String roleName, Class c, String[] slotNames, boolean[] optionality) throws OntologyException {
    if(slotNames.length != optionality.length)
      throw new OntologyException("The two arrays holding names and optionality must have the same length.");
    // Check for the public constructor
    try {
      Constructor constr = c.getConstructor(new Class[] { Frame.class, Ontology.class });
    }
    catch(NoSuchMethodException nsme) {
      throw new OntologyException("Wrong class: Missing a constructor with Frame and Ontology parameters.");
    }
    catch(SecurityException se) {
      throw new OntologyException("Wrong class: The constructor with Frame and Ontology parameters is not accessible.");
    }

    FrameSchema fs = new FrameSchema(this, roleName);

    // Gather all set/get method pairs and build a frame schema (or a
    // set of frame schemas recursively)
    try {
      Method[] methods = c.getMethods();

      for(int i = 0; i < methods.length; i++) {
	Method m = methods[i];
	String s = m.getName();
	if(s.startsWith("get")) {

	  // Recover the slot name removing "get" from the method name
	  String slotName = s.substring(3);
 
	  // Eliminate spurious methods inherited from Object:
	  // - getClass()

	  if(slotName.equalsIgnoreCase("Class"))
	    continue;

	  // Make sure that this method takes no arguments.
	  Class[] parameterTypes = m.getParameterTypes();
	  if(parameterTypes.length > 0)
	    throw new OntologyException("Wrong class: method " + s + "() must take no arguments.");

	  // Now find a matching set method.
	  Class slotType = m.getReturnType();

	  try {
	    Method setMethod = c.getMethod("set" + slotName, new Class[] { slotType });
	  }
	  catch(NoSuchMethodException nsme) {
	    throw new OntologyException("Wrong class: no matching set" + slotName + "() method found for " + s +"().");
	  }

	  // OK. Now we found a matching set/get pair, so we must add a
	  // slot to the frame schema, taking the slot name from the
	  // method names and the slot type from their common
	  // parameter/return type.
	  // E.G. from 'Person getUser()' and 'void setUser(Person p)'
	  // we obtain slot name => "user" and slot type => Person.class

	  // Check first for primitive types
	  if(slotType.isPrimitive())
	    addPrimitiveSlot(fs, slotName, slotType);
	  else if(slotType.equals(String.class)) { // Is it a String ?
	    fs.addStringSlot(slotName, M);
	  }
	  else if(slotType.equals((new byte[0]).getClass())) { // Is it a byte array ?
	    fs.addBinarySlot(slotName, M);
	  }
	  else { // A user defined class: then it must be a sub-frame
	    Class subFrameType = lookupClass(slotName);
	    // If this class is not already in the ontological table, then add it.
	    if(subFrameType == null) {
	      // What to do here ?
	      // 1) Ignore it
	      // 2) addConcept(slotName, slotType);
	      // 3) throw new OntologyException("!!!");
	    }
	    else { // Make sure that the class is type-compatible with the slotType
	      if(!slotType.isAssignableFrom(subFrameType))
		throw new OntologyException("Wrong class: the " + slotName + " role is played by " + subFrameType + " class, which is not a subtype of " + slotType + " class.");
	    }
	    fs.addFrameSlot(slotName, M);
	  }
	}
      }
    }
    catch(SecurityException se) {
      throw new OntologyException("Wrong class: some required method is not accessible."); 
    }

    // OK. The Java class can represent an ontological concept. Now we
    // must make sure that the passed slot names and optionality are
    // consistent with the class.

    for(int i = 0; i < slotNames.length; i++) {
      // Implicitly make sure that every slot in 'slotNames' array is defined
      fs.setOptionality(slotNames[i], optionality[i]);
    }

    fs.setNameList(slotNames);
    addClass(roleName, c);
    addSchema(roleName, fs);

  }


  public void addConcept(String roleName, Frame f, String slotNames[], boolean[] optionality) throws OntologyException {

    if(slotNames.length != optionality.length)
      throw new OntologyException("The two arrays holding names and optionality must have the same length.");

    FrameSchema fs = new FrameSchema(this, roleName);

    Iterator it = f.slots();

    while(it.hasNext()) {

      Map.Entry e = (Map.Entry)it.next();
      String slotName = ((Name)e.getKey()).toString();
      Object slotValue = e.getValue();
      Class slotType = slotValue.getClass();

      // Check first whether this slot is a wrapper for a primitive type
      Class wrappedType = (Class)wrapperTypes.get(slotType);
      if(wrappedType != null) {
	addPrimitiveSlot(fs, slotName, wrappedType);
      }
      else if(slotType.equals(String.class)) { // Is it a String ?
	fs.addStringSlot(slotName, M);
      }
      else if(slotType.equals((new byte[0]).getClass())) { // Is it a byte array ?
	fs.addBinarySlot(slotName, M);
      }
      else if(Frame.class.isAssignableFrom(slotType)) { // Is it a sub-frame ?
	FrameSchema subSchema = lookupSchema(slotName); // Is there a schema for this sub-frame already ?
	if(subSchema != null) { // If so, use it to check the slot value
	  subSchema.checkAgainst((Frame)slotValue);
	}
	else { // Otherwise, create a new schema for this sub-frame
	  // What to do here ?
	  // 1) Ignore it
	  // 2) addConcept(slotName, (Frame)slotValue);
	  // 3) throw new OntologyException("!!!");
	}
	fs.addFrameSlot(slotName, M);
      }
      else { // A different class: then it must be an error
	throw new OntologyException("Internal Error: unknown slot type for " + slotName + " slot in frame " + roleName);
      }
    }

    for(int i = 0; i < slotNames.length; i++) {
      // Implicitly make sure that every slot in 'slotNames' array is defined
      fs.setOptionality(slotNames[i], optionality[i]);
    }

    fs.setNameList(slotNames);
    addSchema(roleName, fs);
  }

  public Object createObject(Frame f, String roleName) throws OntologyException {
    Object concept;
    try {
      Class theConceptClass = lookupClass(roleName);
      if(theConceptClass == null)
	throw new OntologyException("No class able to play " + roleName + " role.");
      Constructor buildFromFrameAndOnto = theConceptClass.getConstructor(new Class[] { Frame.class, Ontology.class });
      concept = buildFromFrameAndOnto.newInstance(new Object[] { f, this });
    }
    catch(NoSuchMethodException nsme) {
      throw new OntologyException("Wrong class: " + nsme.getMessage());
    }
    catch(SecurityException se) {
      throw new OntologyException("Access violation: " + se.getMessage());
    }
    catch(InstantiationException ie) {
      throw new OntologyException("Wrong class: " + ie.getMessage());
    }
    catch(IllegalAccessException iae) {
      throw new OntologyException("Access violation: " + iae.getMessage());
    }
    catch(IllegalArgumentException iae) {
      throw new OntologyException("Wrong class: " + iae.getMessage());
    }
    catch(InvocationTargetException ite) {
      throw new OntologyException("Exception in constructor: " + ite.getTargetException().getMessage());
    }
    return concept;
  }

  public Frame createFrame(Object o, String roleName) throws OntologyException {

    Class theConceptClass = lookupClass(roleName);
    if(theConceptClass == null)
      throw new OntologyException("No class able to play " + roleName + " role.");
    if(!theConceptClass.isInstance(o))
      throw new OntologyException("The object is not an instance of " + theConceptClass.getName() + "class.");
    FrameSchema fs = lookupSchema(roleName);
    Frame f = fs.buildFromObject(o, theConceptClass);
    return f;
  }

  public void check(Frame f, String roleName) throws OntologyException {
    FrameSchema fs = lookupSchema(roleName);
    fs.checkAgainst(f);
  }

  public void check(Object o, String roleName) throws OntologyException {
    // Just try to create a Frame out of this object, and the check will be made.
    Frame f = createFrame(o, roleName);
  }

  // Name <-> Position lookup methods

  public String getSlotName(String roleName, int index) throws OntologyException {
    FrameSchema fs = lookupSchema(roleName);
    return fs.getSlotName(index);
  }

  public int getSlotPosition(String roleName, String name) throws OntologyException {
    FrameSchema fs = lookupSchema(roleName);
    return fs.getSlotPosition(name);
  }

  // Private methods.

  private void addSchema(String roleName, FrameSchema fs) {
    schemas.put(new Name(roleName), fs);
  }

  private void addClass(String roleName, Class c) {
    classes.put(new Name(roleName), c);
  }

  private void addPrimitiveSlot(FrameSchema fs, String slotName, Class primitiveType) throws OntologyException {
    if(primitiveType == Void.TYPE) {
      throw new OntologyException("Internal Error: a slot cannot have void type");
    }
    else {
      try {
	String typeName = primitiveType.getName();
	String methodName = "add" + typeName.substring(0,1).toUpperCase() + typeName.substring(1) + "Slot";
	Method m = fs.getClass().getDeclaredMethod(methodName, new Class[] { String.class, Boolean.TYPE });
	m.invoke(fs, new Object[] { slotName, new Boolean(M) });
      }
      catch(NoSuchMethodException nsme) {
	nsme.printStackTrace();
	throw new OntologyException("Internal Error: " + nsme.getMessage());
      }
      catch(IllegalAccessException iae) {
	iae.printStackTrace();
	throw new OntologyException("Internal Error: " + iae.getMessage());
      }
      catch(IllegalArgumentException iae) {
	iae.printStackTrace();
	throw new OntologyException("Internal Error: " + iae.getMessage());
      }
      catch(InvocationTargetException ite) {
	ite.printStackTrace();
	throw new OntologyException("Internal Error: " + ite.getMessage());
      }
    }

  }

}
