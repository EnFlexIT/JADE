package jade.onto;

import java.lang.reflect.*;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class DefaultOntology implements Ontology {

  private static final String ACTOR_METHOD_NAME = "__actor";

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

  // Raw interface, exposes Functor Schemas directly.
  // This is package scoped, not to be used by applications.

  FunctorSchema lookupSchema(String name) {
    return (FunctorSchema)schemas.get(new Name(name));
  }

  Class lookupClass(String name) {
    return (Class)classes.get(new Name(name));
  }

  // Higher level interface, allows direct usage of user-defined
  // classes but requires a compliance to some rules (that represent
  // the metaclass of the user class).

  /*************************************************************

  Rules for a concept class:

  1) Must have a public 'init()' method taking a Frame, an Action or a Predicate as parameter.

  2) For every Functor term named "term", of type T, the class must have
     two public methods:

       void setTerm(T value)
       T getTerm()

  3) The two methods above must be such that, for each obj of a
     concept class and for each object v of type T, after the fragment

       obj.setTerm(v);
       T v2 = obj.getTerm();

     the postCondition 'v.equals(v2)' must be true

  4) For every sub-functor of the Functor, (i.e. a slot not of
     boolean, int, double, String, byte[]), named "SubFunctor", there
     must be a class F that must itself obey to the four rules with
     respect to the "SubFunctor" functor (i.e. it must be a valid
     Concept class).

  **************************************************************/

  public void addClass(String roleName, Class c) throws OntologyException {

    // Check for the public "init()" method
    try {
      Method initMethod = c.getMethod("init", new Class[] { Frame.class });
    }
    catch(NoSuchMethodException nsme) {
      throw new OntologyException("Wrong class: Missing a suitable init() method.");
    }
    catch(SecurityException se) {
      throw new OntologyException("Wrong class: A suitable init() method  is not accessible.");
    }

    FunctorSchema fs = lookupSchema(roleName);
    if(fs == null)
      throw new OntologyException("No schema was found for " + roleName + "role.");

    // For actions, a class must also have a method '__actor()' that
    // returns a String with the name of the agent that is to perform
    // the action
    if(fs.isAction())
      checkActorMethod(c);

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
	  Class roleType = lookupClass(desc.getName());
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

  public void addConcept(String conceptName, TermDescriptor[] slots) throws OntologyException {

    FunctorSchema fs = new FunctorSchema(this, conceptName, FunctorSchema.CONCEPT);

    for(int i = 0; i < slots.length; i++) {
      fs.addTerm(slots[i]);
    }

    addSchemaToTable(conceptName, fs);

  }

  public void addAction(String actionName, TermDescriptor[] args) throws OntologyException {

    FunctorSchema fs = new FunctorSchema(this, actionName, FunctorSchema.ACTION);

    for(int i = 0; i < args.length; i++) {
      fs.addTerm(args[i]);
    }

    addSchemaToTable(actionName, fs);

  }

  public void addPredicate(String predicateName, TermDescriptor[] terms) throws OntologyException {

    FunctorSchema fs = new FunctorSchema(this, predicateName, FunctorSchema.PREDICATE);

    for(int i = 0; i < terms.length; i++) {
      fs.addTerm(terms[i]);
    }

    addSchemaToTable(predicateName, fs);

  }

  public Object createObject(Frame f) throws OntologyException {
    Object concept;
    String roleName = f.getName();
    try {
      Class theConceptClass = lookupClass(roleName);
      if(theConceptClass == null)
	throw new OntologyException("No class able to play " + roleName + " role.");
      concept = theConceptClass.newInstance();
      Method initMethod = theConceptClass.getMethod("init", new Class[] { Frame.class });
      initMethod.invoke(concept, new Object[] { f });
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
      throw new OntologyException("Exception in init() method: " + ite.getTargetException().getMessage());
    }
    return concept;
  }

  public Object createObject(Action a) throws OntologyException {
    Object action;
    String roleName = a.getName();
    try {
      Class theActionClass = lookupClass(roleName);
      if(theActionClass == null)
	throw new OntologyException("No class able to play " + roleName + " role.");
      action = theActionClass.newInstance();
      Method initMethod = theActionClass.getMethod("init", new Class[] { Action.class });
      initMethod.invoke(action, new Object[] { a });
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
      throw new OntologyException("Exception in init() method: " + ite.getTargetException().getMessage());
    }
    return action;
  }

  public Object createObject(Predicate p) throws OntologyException {
    Object predicate;
    String roleName = p.getName();
    try {
      Class thePredicateClass = lookupClass(roleName);
      if(thePredicateClass == null)
	throw new OntologyException("No class able to play " + roleName + " role.");
      predicate = thePredicateClass.newInstance();
      Method initMethod = thePredicateClass.getMethod("init", new Class[] { Predicate.class });
      initMethod.invoke(predicate, new Object[] { p });
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
      throw new OntologyException("Exception in init() method: " + ite.getTargetException().getMessage());
    }
    return predicate;
  }

  public Frame createConcept(Object o, String roleName) throws OntologyException {
    Class theConceptClass = lookupClass(roleName);
    if(theConceptClass == null)
      throw new OntologyException("No class able to play " + roleName + " role.");
    if(!theConceptClass.isInstance(o))
      throw new OntologyException("The object is not an instance of " + theConceptClass.getName() + "class.");

    FunctorSchema fs = lookupSchema(roleName);
    if(!fs.isConcept())
      throw new OntologyException("The role " + roleName + " is not a concept in this ontology.");

    Frame f = new Frame(roleName);
    buildFromObject(f, fs, o, theConceptClass);

    return f;
  }

  public Action createAction(Object o, String roleName) throws OntologyException {
    Class theConceptClass = lookupClass(roleName);
    if(theConceptClass == null)
      throw new OntologyException("No class able to play " + roleName + " role.");
    if(!theConceptClass.isInstance(o))
      throw new OntologyException("The object is not an instance of " + theConceptClass.getName() + "class.");

    FunctorSchema fs = lookupSchema(roleName);
    if(!fs.isAction())
      throw new OntologyException("The role " + roleName + " is not an action in this ontology.");

    String actor = getActorFromActionObject(o);
    Action a = new Action(roleName, actor);
    buildFromObject(a, fs, o, theConceptClass);

    return a;
  }

  public Predicate createPredicate(Object o, String roleName) throws OntologyException {
    Class theConceptClass = lookupClass(roleName);
    if(theConceptClass == null)
      throw new OntologyException("No class able to play " + roleName + " role.");
    if(!theConceptClass.isInstance(o))
      throw new OntologyException("The object is not an instance of " + theConceptClass.getName() + "class.");

    FunctorSchema fs = lookupSchema(roleName);
    if(!fs.isPredicate())
      throw new OntologyException("The role " + roleName + " is not a predicate in this ontology.");

    Predicate p = new Predicate(roleName);
    buildFromObject(p, fs, o, theConceptClass);

    return p;
  }

  public void check(Frame f) throws OntologyException {
    String roleName = f.getName();
    FunctorSchema fs = lookupSchema(roleName);
    fs.checkAgainst(f);
  }

  public void check(Action a) throws OntologyException {
    String roleName = a.getName();
    FunctorSchema fs = lookupSchema(roleName);
    fs.checkAgainst(a);
  }

  public void check(Predicate p) throws OntologyException {
    String roleName = p.getName();
    FunctorSchema fs = lookupSchema(roleName);
    fs.checkAgainst(p);
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

    FunctorSchema fs = lookupSchema(roleName);
    Iterator it = fs.subSchemas();
    while(it.hasNext()) {

      TermDescriptor desc = (TermDescriptor)it.next();
      if(!desc.isOptional()) {
	Method m = findMethodCaseInsensitive("get" + translateName(desc.getName()), implementationClass.getMethods());

	try {
	  Object value = m.invoke(o, new Object[] { });
	  if(value == null)
	    throw new OntologyException("The given object has a 'null' value for the mandatory term " + desc.getName());
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


  }

  public TermDescriptor[] getTerms(String roleName) throws OntologyException {
    FunctorSchema fs = lookupSchema(roleName);
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

  private void buildFromObject(Functor f, FunctorSchema fs, Object o, Class c) throws OntologyException {
    Iterator it = fs.subSchemas();
    while(it.hasNext()) {
      TermDescriptor desc = (TermDescriptor)it.next();
      String name = translateName(desc.getName());

      // Retrieve the accessor method from the class and call it
      Method getMethod = findMethodCaseInsensitive("get" + name, c.getMethods());
      try {
	Object value = getMethod.invoke(o, new Object[] { });

	// Now set the corresponding functor subterm appropriately
	if(!desc.isComplex()) { // For elementary terms, just put the Object as a slot
	  f.putTerm(name, value);
	}
	else { // For complex terms, do a name lookup and call this method recursively
	  switch(desc.getType()) {
	  case Ontology.CONCEPT_TYPE:
	    f.putTerm(name, createConcept(value, name));
	    break;
	  case Ontology.ACTION_TYPE:
	    f.putTerm(name, createAction(value, name));
	    break;
	  case Ontology.PREDICATE_TYPE:
	    f.putTerm(name, createPredicate(value, name));
	    break;
	  default:
	    throw new InternalError("Non existent complex functor type.");
	  }
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

  private void addSchemaToTable(String roleName, FunctorSchema fs) {
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

  private String getActorFromActionObject(Object o) throws OntologyException {
    Class c = o.getClass();
    try {
      Method m = c.getMethod(ACTOR_METHOD_NAME, new Class[] { });
      return (String)m.invoke(o, new Object[] { });
    }
    catch(NoSuchMethodException nsme) {
      throw new OntologyException("The method named " + ACTOR_METHOD_NAME + "must be present in classes representing actions.");
    }
    catch(InvocationTargetException ite) {
      String msg = ite.getTargetException().getMessage();
      throw new OntologyException("Internal error: a reflected method threw an exception.\nMessage was " + msg);
    }
    catch(IllegalAccessException iae) {
      throw new OntologyException("Internal error: the required method is not accessible [" + iae.getMessage() + "]");
    }
    catch(SecurityException se) {
      throw new OntologyException("The method named " + ACTOR_METHOD_NAME + "must be accessible in classes representing actions.");
    }
  }

  private void checkActorMethod(Class c) throws OntologyException {
    try {
      Method m = c.getMethod(ACTOR_METHOD_NAME, new Class[] { });
      Class r = m.getReturnType();
      if(!r.equals(String.class))
	throw new OntologyException(ACTOR_METHOD_NAME + " method must return a String annd not a " + r.getName());
    }
    catch(NoSuchMethodException nsme) {
      throw new OntologyException("The method named " + ACTOR_METHOD_NAME + "must be present in classes representing actions.");
    }
    catch(SecurityException se) {
      throw new OntologyException("The method named " + ACTOR_METHOD_NAME + "must be accessible in classes representing actions.");
    }

  }


}
