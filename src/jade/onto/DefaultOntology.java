package jade.onto;

import java.lang.reflect.*;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class DefaultOntology implements Ontology {

  private static final List primitiveTypes = new ArrayList(10);

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
  // the metaclass of the user class).

  /*************************************************************

  Rules for a concept class:

  1) For every Frame slot named "term", of type T, the class must have
     two public methods:

       void setTerm(T value)
       T getTerm()

  2) The two methods above must be such that, for each obj of a
     concept class and for each object v of type T, after the fragment

       obj.setTerm(v);
       T v2 = obj.getTerm();

     the postCondition 'v.equals(v2)' must be true

  3) For every sub-frame of the Frame, (i.e. a slot not of
     boolean, int, double, String, byte[]), named "SubFrame", there
     must be a class F that must itself obey to the four rules with
     respect to the "SubFrame" frame (i.e. it must be a valid
     Concept class).

  **************************************************************/

  public void addClass(String roleName, Class c) throws OntologyException {

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

	// If the descriptor is a complex term (Concept, Action or
	// Predicate) and some class C is registered for that role,
	// then the implementation type must be a supertype of C.
	if(desc.isComplex()) {
	  Class roleType = lookupClass(desc.getTypeName());
	  if((roleType != null) && (!implType.isAssignableFrom(roleType)))
	    throw new OntologyException("Wrong class: the " + desc.getName() + " role is played by " + roleType + " class, which is not a subtype of " + implType + " class.");
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
    addClassToTable(roleName, c);

  }

  public void addFrame(String conceptName, int kind, TermDescriptor[] slots) throws OntologyException {
    int realKind;
    switch(kind) {
    case CONCEPT:
      realKind = CONCEPT_TYPE;
      break;
    case ACTION:
      realKind = ACTION_TYPE;
      break;
    case PREDICATE:
      realKind = PREDICATE_TYPE;
      break;
    default:
      throw new OntologyException("Error: Unknown kind of Frame requested");
    }

    FrameSchema fs = new FrameSchema(this, conceptName, realKind);

    for(int i = 0; i < slots.length; i++) {
      fs.addTerm(slots[i]);
    }

    addSchemaToTable(conceptName, fs);

  }

  public Object initObject(Frame f) throws OntologyException {
    String roleName = f.getName();
    Class theConceptClass = lookupClass(roleName);

    if(theConceptClass == null)
      throw new OntologyException("No class able to play " + roleName + " role.");

    try {

      Object o = theConceptClass.newInstance();
      return initObject(f, o);
    }
    catch(IllegalAccessException iae) {
      throw new OntologyException("Wrong class: The default constructor of " + theConceptClass.getName() + " is not accessible.");
    }
    catch(InstantiationException ie) {
      throw new OntologyException("Wrong class: The class " + theConceptClass.getName() + " cannot be instantiated.");
    }
  }

  public Object initObject(Frame f, Object concept) throws OntologyException {

    String roleName = f.getName();

    Class theConceptClass = lookupClass(roleName);
    if(theConceptClass == null)
      throw new OntologyException("No class able to play " + roleName + " role.");

    if(!theConceptClass.isInstance(concept))
      throw new OntologyException("The object <" + concept + "> is not an instance of " + theConceptClass.getName() + " class.");

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
	// System.out.println("Name: " + slotName + " - Value: " + slotValue);

	// For complex slots, transform from sub-slot to
	// sub-object. Three steps are made:
	//   1) The subObject is retrieved using the accessor method getXXX() ).
	//   2) initObject() is called recursively to fill the returned sub-object with sub-frame data.
	//   3) The modifier method setXXX() is called to write back the changes.
	if(desc.isComplex()) {
	  Method getMethod = findMethodCaseInsensitive("get" + translateName(slotName), theConceptClass.getMethods());
	  Object subObject = getMethod.invoke(concept, new Object[] { });

	  slotValue = initObject((Frame)slotValue, subObject);

	}
	setMethod.invoke(concept, new Object[] { slotValue });

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

    }

    return concept;
  }

  public Frame createFrame(Object o, String roleName) throws OntologyException {
    Class theConceptClass = lookupClass(roleName);
    if(theConceptClass == null)
      throw new OntologyException("No class able to play " + roleName + " role.");
    if(!theConceptClass.isInstance(o))
      throw new OntologyException("The object <" + o + "> is not an instance of " + theConceptClass.getName() + " class.");
    FrameSchema fs = lookupSchema(roleName);
    if(fs == null)
      throw new OntologyException("Internal error: inconsistency between schema and class table");
    if(!fs.isConcept() && !fs.isAction() && !fs.isPredicate())
      throw new OntologyException("The role " + roleName + " is not a concept, action or predicate in this ontology.");

    Frame f = new Frame(roleName);
    buildFromObject(f, fs, o, theConceptClass);

    return f;
  }

  public void check(Frame f) throws OntologyException {
    String roleName = f.getName();
    FrameSchema fs = lookupSchema(roleName);
    fs.checkAgainst(f);
  }

  public void check(Object o, String roleName) throws OntologyException {
    /*
     *  Algorithm: 
     *
     * - Check that the object is an instance of the correct class
     * - FOR EACH term t
     *   - The Object get method must not return 'null'
     */

    Class implementationClass = lookupClass(roleName);
    if(implementationClass == null)
      throw new OntologyException("No class able to play " + roleName + " role.");
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

  private void buildFromObject(Frame f, FrameSchema fs, Object o, Class c) throws OntologyException {
    Iterator it = fs.subSchemas();
    while(it.hasNext()) {
      TermDescriptor desc = (TermDescriptor)it.next();
      String name = translateName(desc.getName());

      // Retrieve the accessor method from the class and call it
      Method getMethod = findMethodCaseInsensitive("get" + name, c.getMethods());
      try {
	Object value = getMethod.invoke(o, new Object[] { });

	// Now set the corresponding frame subterm appropriately
	if(!desc.isComplex()) { // For elementary terms, just put the Object as a slot
	  f.putSlot(name, value);
	}
	else { // For complex terms, do a name lookup and call this method recursively
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

  private void addClassToTable(String roleName, Class c) {
    classes.put(new Name(roleName), c);
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
