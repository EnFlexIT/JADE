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
import java.util.*;

/**
  A simple implementation of the <code>Ontology</code> interface. Instances of
  this class keeps all the ontology data in memory, and don't support an
  external archive format.

  @author Giovanni Rimassa - Universita` di Parma
  @version $Date$ $Revision$
*/
public final class DefaultOntology implements Ontology {

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

  RoleEntityFactory lookupFactory(String name) {
    return (RoleEntityFactory)factories.get(new Name(name));
  }

  /**
    Adds a new role to this ontology, without a user defined Java class to
    represent it.
    @see jade.onto.Ontology#addRole(String roleName, SlotDescriptor[] slots)
  */
  public void addRole(String roleName, SlotDescriptor[] slots) throws OntologyException {
		// Checks whether a role with this name already exists in the ontology
  	if (schemas.containsKey(new Name(roleName)))
			throw new OntologyException("A role with name \""+roleName+"\" already exists in the ontology");
  	
		// Adds the new role
    FrameSchema fs = new FrameSchema(this, roleName);

    for(int i = 0; i < slots.length; i++) {
      String n = slots[i].getName();
      if(n.length() == 0)
				slots[i].setName(Frame.UNNAMEDPREFIX+ "_"+i);
      fs.addSlot(slots[i]);
    }

    addSchemaToTable(roleName, fs);

  }
  
  /**
    Adds a new role to this ontology, with a user defined Java class to
    represent it.
    @see jade.onto.Ontology#addRole(String roleName, SlotDescriptor[] slots, RoleEntityFactory ref)
  */  
  public void addRole(String roleName, SlotDescriptor[] slots, RoleEntityFactory ref) throws OntologyException {
    // Checks whether the user defined class representing the role already represents another role in the ontology
    Class newClass = ref.getClassForRole();
    Iterator i = factories.values().iterator();
  	while (i.hasNext()){
  		RoleEntityFactory fac = (RoleEntityFactory) i.next();
  		Class c = fac.getClassForRole();
  		if (newClass.equals(c))
				throw new OntologyException("The class \""+newClass.getName()+"\" already represents a role in the ontology");
  	}
  	
  	// Adds the role to the ontology
  	addRole(roleName, slots);

  	// Registers the factory of objects of the user defined class representing the role
    checkClass(roleName, newClass);
    addFactoryToTable(roleName, ref);
  }

  /**
    Adds to this ontology all roles included into another ontology 
    @param o The <code>Ontology</code> object whose roles will 
    be added
  */
  public void joinOntology(Ontology o) throws OntologyException
  {
  	// Gets the names of all roles in the ontology to join
  	List roleNames = o.getVocabulary();
	  Iterator i = roleNames.iterator();
	  
	  // For each role try to add it to the current ontology
	  while (i.hasNext()){
	  	String name = (String) (i.next());
	  	// DEBUG: System.out.println("Try to add role \""+name+"\"");
			SlotDescriptor[] slots = o.getSlots(name);
			RoleEntityFactory fac = null;
			boolean hasFactoryFlag;
			try{
				fac = o.getFactory(name);
				// If no exception has been thrown --> the role has a factory associated to it
				hasFactoryFlag = true; 
			}
			catch (OntologyException oe){
				// If an exception has been thrown --> the role does not have a factory associated to it
				hasFactoryFlag = false; 
			}
			try{
			if (hasFactoryFlag)
				addRole(name, slots, fac);
			else
				addRole(name, slots);
			}
			catch (OntologyException oe){oe.printStackTrace();}
	  }		
  }
  	

  /**
    Creates a List of Java objects from the given list of frame.
    @see jade.onto.Ontology#createObject(List v)
  */
  public List createObject(List v) throws OntologyException {
    List outvec = new ArrayList();
    for (int i=0; i<v.size(); i++) 
      outvec.add(createSingleObject( (Frame)v.get(i) ));
    return outvec;
  }
		 
  /**
   * Creates a Java object from the given Frame.
   * This method is called by createObject().
   */
  private Object createSingleObject(Frame f) throws OntologyException {

      String roleName = f.getName();
      RoleEntityFactory fac = lookupFactory(roleName);

      if(fac == null)
				throw new OntologyException("No class able to represent " + roleName + " role. Check the definition of the ontology.");

      Class c = fac.getClassForRole();

      Object o = fac.create(f);
      return initObject(f, o, c);
  }

  /**
    Creates a frame from a given Java Object, representing an instance of a given role.
    @see jade.onto.Ontology#createFrame(Object o, String roleName)
  */
  public Frame createFrame(Object o, String roleName) throws OntologyException {
    RoleEntityFactory fac = lookupFactory(roleName);
    if(fac == null)
      throw new OntologyException("No class able to represent " + roleName + " role. Check the definition of the ontology.");

    Class theRoleClass = fac.getClassForRole();
    if(!theRoleClass.isInstance(o))
      throw new OntologyException("The object <" + o + "> is not an instance of " + theRoleClass.getName() + " class.");

    FrameSchema fs = lookupSchema(roleName);
    if(fs == null)
      throw new OntologyException("Internal error: inconsistency between schema and class table");
    //if(!fs.isConcept())
    //  throw new OntologyException("The role " + roleName + " is not a concept in this ontology.");

    Frame f = new Frame(roleName);
    buildFromObject(f, fs, o, theRoleClass);

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
     * - FOR EACH mandatory slot S
     *   - The getS method of the class o is an instance of must not return 'null'
     */

    RoleEntityFactory fac = lookupFactory(roleName);
    if(fac == null)
      throw new OntologyException("No class able to represent " + roleName + " role.");

    Class theRoleClass = fac.getClassForRole();
    if(!theRoleClass.isInstance(o))
      throw new OntologyException("The object is not an instance of " + theRoleClass.getName() + " class.");

    FrameSchema fs = lookupSchema(roleName);
    Iterator it = fs.subSchemas(); // This iterates on the slots of the FrameSchema

    while(it.hasNext()) {
      SlotDescriptor desc = (SlotDescriptor)it.next();
			Method m = findMethodCaseInsensitive("get" + translateName(desc.getName()), theRoleClass);
			try {
	  		Object value = m.invoke(o, new Object[] { });

	  		if(!desc.isOptional() && (value == null))
	    		throw new OntologyException("The given object has a 'null' value for the mandatory term " + desc.getName());

	  		if(desc.isComplex() || desc.isSet())// Recursive check for sub-objects
	    		check(value, desc.getType());
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
    Checks whether a given string is the name of a role in this ontology.
    @see jade.onto.Ontology#isConcept(String roleName)
  */
  public boolean isRole(String roleName) throws OntologyException {
  	FrameSchema fs = lookupSchema(roleName);
  	return (fs != null);
  }

  /**
    Get the descriptions for all the slots that define the structure of a given
    ontological role.
    @see jade.onto.Ontology#getSlots(String roleName)
  */
  public SlotDescriptor[] getSlots(String roleName) throws OntologyException {
    FrameSchema fs = lookupSchema(roleName);
    return fs.slotsArray();
  }

  /** @return the name of the role represented by the passed class as registered in this ontology
   * @throws OntologyException if no role is found for this class
  **/
	public String getRoleName(Class c) throws OntologyException{
  	Set s = factories.entrySet(); // each element of the Set is a Map.Entry
  	Iterator i = s.iterator();
  	while (i.hasNext()) {
    	Map.Entry element = (Map.Entry)i.next();
    	// element.getValue() returns a RoleFactory
    	if (c.equals(((RoleEntityFactory)element.getValue()).getClassForRole()))
      	return ((Name)element.getKey()).toString();
  	}
  	throw new OntologyException("No rolename registered in the ontology for class "+c.getName());
	}
  
	/** 
  	@return a <code>List</code> including the names of all the roles
  	in the ontology, i.e. the Vocabulary used by the ontology
    @see jade.onto.Ontology#getVocabulary()
  */
  public List getVocabulary(){
  	// The Vocabulary is the list of the names of the roles in the ontology;
  	// role names are stored as Name while we want to return them as String
  	List vocabulary = new ArrayList();
  	Iterator i = schemas.keySet().iterator();
  	while (i.hasNext()){
  		String roleNameAsString = ((Name) (i.next())).toString();
  		vocabulary.add(roleNameAsString);
  	}
  	return vocabulary;	
  }
  
  /** 
  	Returns the factory for instances of the user defined class
  	representing a given role
  	@param roleName The name of the ontological role.
  	@return the factory for instances of the user defined class
  	representing a given role
    @throws OntologyException if no role is found with the specified name
    or if a factory is not registered for the role
    @see jade.onto.Ontology#getFactory(String roleName)
  */
  public RoleEntityFactory getFactory(String roleName) throws OntologyException{
    if (!factories.containsKey(new Name(roleName)))
 			throw new OntologyException("No role with name \""+roleName+"\" has a factory registered");

 		return (RoleEntityFactory) factories.get(new Name(roleName));
  }
  


  // Private methods.

  /**
   * if name starts with UNNAMED_PREFIX, it removes it
   * @return the name of a method, given the name of a slot
   */
  private String translateName(String name) {
    StringBuffer buf;
    if (name.startsWith(Frame.UNNAMEDPREFIX))
      buf = new StringBuffer(name.substring(Frame.UNNAMEDPREFIX.length()));
    else
      buf = new StringBuffer(name);
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


  private Class checkGetAndSet(String name, Class c) throws OntologyException {
    Class result;
    Method getMethod = findMethodCaseInsensitive("get" + name, c);
    Method setMethod = findMethodCaseInsensitive("set" + name, c);

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

  /**
   * @return the number of arguments of the method m
   */
  private int getArgumentLength(Method m) {
    Class[] getParams = m.getParameterTypes();
    return getParams.length;
  }

  /**
    @ return the Class of the return type of the method m
   */
  private Class getReturnType(Method m) {
    return m.getReturnType();
  }

  /**
    @ return the Class of the argument type number no. of the method m
   */
  private Class getArgumentType(Method m, int no) {
    Class[] setParams = m.getParameterTypes();
    return setParams[no];
  }

  /**
   * This method checks for correct get and set methods for the
   * current descriptor and retrieves the implementation type.
   * This check is for slots of category SET_SLOT or SEQUENCE_SLOT.
   * <p> 
   * For every <code>SlotDescriptor</code> 
   * of category <code>SET_SLOT</code> or <code>SEQUENCE_SLOT</code>
   * and named <code>XXX</code>, with elements of type <code>T</code>, the
   * class must have two accessible methods, with the following
   * signature:</i>
   *  <ul>
   *  <li> <code>Iterator getAllXXX()</code>
   *  <li> <code>void addXXX(T t)</code>
   *  </ul>
   */
  private Class checkGetAndSet2(String name, Class c) throws OntologyException {
    Method getMethod = findMethodCaseInsensitive("getAll" + name, c);
    Method addMethod = findMethodCaseInsensitive("add" + name, c);
    //Method remMethod = findMethodCaseInsensitive("remove" + name, c);
    //Method clrMethod = findMethodCaseInsensitive("clearAll" + name, c);
    Class result = getArgumentType(addMethod,0);  
    //FIXME. The type of result should be taken from the SlotDescriptor 
    // and not directly from the method argument. 

    // check "get" method 
    if (getArgumentLength(getMethod) != 0)
      throw new OntologyException("Wrong class: method " +  getMethod.getName() + "() must take no arguments.");
    if (!getReturnType(getMethod).equals(java.util.Iterator.class))
      throw new OntologyException("Wrong class: method " +  getMethod.getName() + "() must return a java.util.Iterator." + getReturnType(getMethod).toString());

    // check 'add' method 
    if (getArgumentLength(addMethod) != 1)
      throw new OntologyException("Wrong class: method " +  addMethod.getName() + "() must take one argument.");
    if (!getArgumentType(addMethod,0).equals(result))
      throw new OntologyException("Wrong class: method " +  addMethod.getName() + "() has the wrong argument type.");
    if (!getReturnType(addMethod).equals(Void.TYPE))
      throw new OntologyException("Wrong class: method " +  addMethod.getName() + "() must return a void.");

    /* check remove method
    if (getArgumentLength(remMethod) != 1)
      throw new OntologyException("Wrong class: method " +  remMethod.getName() + "() must take one argument.");
    if (!getArgumentType(remMethod,0).equals(result))
      throw new OntologyException("Wrong class: method " +  remMethod.getName() + "() has the wrong argument type.");
    if (!getReturnType(remMethod).equals(Boolean.TYPE))
      throw new OntologyException("Wrong class: method " +  remMethod.getName() + "() must return a boolean.");
      */
    /* check clear method
    if (getArgumentLength(clrMethod) != 0)
      throw new OntologyException("Wrong class: method " +  clrMethod.getName() + "() must take no arguments.");
    if (!getReturnType(clrMethod).equals(Void.TYPE))
      throw new OntologyException("Wrong class: method " +  clrMethod.getName() + "() must return a void.");
      */
    return result;

  }

  private void checkClass(String roleName, Class c) throws OntologyException {

    FrameSchema fs = lookupSchema(roleName);
    if(fs == null)
      throw new OntologyException("No schema was found for " + roleName + "role.");

    Iterator it = fs.subSchemas();
    // System.out.println("checkClass. Role=\""+fs.getName()+"\" represented by class "+c.getName());
    
    while(it.hasNext()) {
      SlotDescriptor desc = (SlotDescriptor)it.next();
      // System.out.println("checkClass. SlotDescriptor="+desc.toString());
      String slotName = translateName(desc.getName());
      try {
				// Check for correct set and get methods for the current
				// descriptor and retrieve the implementation type.
				Class implType;
				//if ((desc.getType() == SET_SLOT) || (desc.getType() == SEQUENCE_SLOT))
				if (desc.isSet())
	  			implType = checkGetAndSet2(slotName, c);
				else
	  			implType = checkGetAndSet(slotName, c);
	  		// System.out.println("- implType class is: "+implType.getName());

				// If the descriptor is a complex slot (i.e. its values are instances of a 
				// role) and some class C is registered for that role,
				// then the implementation type must be a supertype of C.
				if(desc.isComplex() || desc.isSet()) { //DUBBIO Non dovrebbe essere if (!desc.hasPrimitiveTypes())
	  			RoleEntityFactory fac = lookupFactory(desc.getType());
	  			if(fac != null) {
	    			Class complex = fac.getClassForRole();
	  				// System.out.println("- complex class is: "+complex.getName());
	    			if(!implType.isAssignableFrom(complex))
	      			throw new OntologyException("Wrong class: the " + desc.getName() + " role is represented by " + complex + " class, which is not a subtype of " + implType + " class.");
	  			}
				}
				else {	// Check that the returned type is compatible with the one dictated by the SlotDescriptor
	  			try {
	    			Class primitive = Class.forName(desc.getType()); 
	  				// System.out.println("- primitive class is: "+primitive.getName());
	    			if(!implType.isAssignableFrom(primitive))
	      			throw new OntologyException("Wrong class: the primitive term " + desc.getName() + " is of type "+ primitive + ", but must be a subtype of " + implType + " class.");
	  			} 
	  			catch (Exception e) {
	    			throw new OntologyException("Wrong class: the primitive term " + desc.getName() + " must be a subtype of " + implType + " class.");
	  			}
				}
      }
      catch(SecurityException se) {
				throw new OntologyException("Wrong class: some required method is not accessible."); 
      }

    }

  }


  private Object initObject(Frame f, Object entity, Class theRoleClass) throws OntologyException {

    String roleName = f.getName();

    FrameSchema fs = lookupSchema(roleName);
    Iterator it = fs.subSchemas();
    int slotPosition = 0;
    
    while(it.hasNext()) {
      SlotDescriptor desc = (SlotDescriptor)it.next();
      String slotName = desc.getName();
      String methodName;
      if (desc.isSet())
	methodName = "add" + translateName(slotName);
      else
	methodName = "set" + translateName(slotName);
      
      // Retrieve the modifier method from the class and call it
      Method setMethod = findMethodCaseInsensitive(methodName, theRoleClass);
      try {
	Object slotValue;
	if (slotName.startsWith(Frame.UNNAMEDPREFIX))
	  slotValue = f.getSlot(slotPosition);
	else
	  slotValue = f.getSlot(slotName);
	// For complex slots, transform from sub-frame to sub-object.
	// This is performed calling createObject() recursively.
	if(desc.isComplex()) {
	  slotValue = createSingleObject((Frame)slotValue);
	  setMethod.invoke(entity, new Object[] { slotValue });
	}
	else if (desc.isSet()) {
	  Frame set = (Frame)slotValue;//this is the frame representing the set
	  if (desc.getType().equalsIgnoreCase(Ontology.ANY_TYPE)){
	    for (int i=0; i<set.size(); i++) { 
	      try { //try as a complex frame
		Object element = createSingleObject((Frame)set.getSlot(i));
		setMethod.invoke(entity, new Object[]{element});
	      } catch (Exception ee1) {
		// if exception then it is a primitive frame
		setMethod.invoke(entity, new Object[]{set.getSlot(i)}); 
	      }
	    }//end of for int
	  } else if (desc.hasPrimitiveType())
	    for (int i=0; i<set.size(); i++) // add all the elements of the set
	      setMethod.invoke(entity, new Object[]{set.getSlot(i)}); 
	  else // convert the elements into an object and then add
	    for (int i=0; i<set.size(); i++) { 
	      Object element = createSingleObject((Frame)set.getSlot(i));
	      setMethod.invoke(entity, new Object[]{element});
	    } 
	} 
	else 
	  setMethod.invoke(entity, new Object[] { slotValue });
	slotPosition++;
      }
      catch(Frame.NoSuchSlotException fnsse) { // Ignore 'No such slot' errors for optional slots
	if(!desc.isOptional())
	  throw fnsse;
      }
      catch(InvocationTargetException ite) {
	Throwable e = ite.getTargetException();
	e.printStackTrace();
	throw new OntologyException("Internal error: a reflected method threw an exception.\n e.getMessage()",ite);
      }
      catch(IllegalAccessException iae) {
	throw new OntologyException("Internal error: the required method is not accessible [" + iae.getMessage() + "]",iae);
      }
      catch(SecurityException se) {
	throw new OntologyException("Wrong class: some required method is not accessible.",se); 
      }
      catch(IllegalArgumentException iare){
      	throw new OntologyException("Possible mismatch between the type returned by the parser and the type declared in the ontology [" + iare.getMessage() + "]. For role "+roleName+" and slot "+slotName,iare);	
      }
      catch(ClassCastException iacce) {
      	throw new OntologyException("Possibly a primitive value has been used instead of a Frame slot",iacce);
      }
    }
    
    return entity;
  }


  private void buildFromObject(Frame f, FrameSchema fs, Object o, Class theRoleClass) throws OntologyException {
    Iterator it = fs.subSchemas();
    while(it.hasNext()) {
      SlotDescriptor desc = (SlotDescriptor)it.next();
      String slotName = desc.getName();
      String methodName;
      if (desc.isSet())
	methodName = "getAll" + translateName(slotName);
      else
	methodName = "get" + translateName(slotName);

      // Retrieve the accessor method from the class and call it
      Method getMethod = findMethodCaseInsensitive(methodName, theRoleClass);
      try {
	Object value = getMethod.invoke(o, new Object[] { });
	if (value == null) {
	  if (!desc.isOptional())
	    throw new OntologyException("Slot "+slotName+" has a null value and it is mandatory"); 
	} 
	else {
	  // Now set the corresponding frame subterm appropriately
	  if(!desc.isComplex() && !desc.isSet()) { // For elementary terms, just put the Object as a slot
	    f.putSlot(slotName, value);
	  }
	  else if (desc.isComplex()) { 
	    // For complex terms, do a name lookup and 
	    // call createFrame() recursively
	    String roleName = desc.getType();
	    if (roleName.equalsIgnoreCase(Ontology.ANY_TYPE))
	      roleName = getRoleName(value.getClass());
	    f.putSlot(slotName, createFrame(value, roleName));
	  }
	  else if (desc.isSet()) {
	    Frame setFrame;
	    if (desc.getCategory() == Ontology.SET_SLOT)
	      setFrame = new Frame(Ontology.NAME_OF_SET_FRAME); 
	    else
	      setFrame = new Frame(Ontology.NAME_OF_SEQUENCE_FRAME); 
	    Iterator i = (Iterator)value;
	    if (desc.getType().equalsIgnoreCase(Ontology.ANY_TYPE)) {
	      while (i.hasNext()) {
		Object elem = i.next();
		try { //try before as a complex frame
		  setFrame.putSlot(createFrame(elem, getRoleName(elem.getClass()))); 
		} catch (Exception e) {
		  // if exception then it is a primitive slot
		  setFrame.putSlot(elem);
		}
	      }
	    } else if (desc.hasPrimitiveType())
	      while (i.hasNext()) 
		setFrame.putSlot(i.next());
	    else 
	      while (i.hasNext()) 
		setFrame.putSlot(createFrame(i.next(), desc.getType()));
	    f.putSlot(slotName,setFrame);
	  }
	} //if (value==null) else
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


  private void addFactoryToTable(String roleName, RoleEntityFactory fac) {
    factories.put(new Name(roleName), fac);
  }


  private Method findMethodCaseInsensitive(String name, Class c) throws OntologyException {
    Method[] methods = c.getMethods();
    for(int i = 0; i < methods.length; i++) {
      String ithName = methods[i].getName();
      if(ithName.equalsIgnoreCase(name))
				return methods[i];
    }
    throw new OntologyException("Method " + name + " not found in class "+c.getName());
  }

}
