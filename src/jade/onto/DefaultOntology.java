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
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.IOException;
import java.text.*;

/**
  A simple implementation of the <code>Ontology</code> interface. Instances of
  this class keeps all the ontology data in memory, and don't support an
  external archive format.

  @author Giovanni Rimassa - Universita` di Parma
  @version $Date$ $Revision$
*/
public final class DefaultOntology implements Ontology {

	private final static String BEGIN_ONTOLOGY_TAG = "ONTOLOGY";
	private final static String END_ONTOLOGY_TAG = "END-ONTOLOGY";
	private final static String ONTOLOGY_NAME_TAG = "ONTOLOGY-NAME";
	private final static String BEGIN_ROLE_TAG = "ROLE";
	private final static String END_ROLE_TAG = "END-ROLE";
	private final static String ROLE_NAME_TAG = "ROLE-NAME";
	private final static String ROLE_FACTORY_TAG = "ROLE-ENTITY-FACTORY";
	private final static String BEGIN_SLOT_TAG = "SLOT";
	private final static String END_SLOT_TAG = "END-SLOT";
	private final static String SLOT_NAME_TAG = "SLOT-NAME";
	private final static String SLOT_CATEGORY_TAG = "SLOT-CATEGORY";
	private final static String SLOT_TYPE_TAG = "SLOT-TYPE";
	private final static String SLOT_PRESENCE_TAG = "SLOT-PRESENCE";
	
	private final static int INIT_STATE = 1;
	private final static int PARSE_ONTOLOGY_BEGIN_STATE = 2;
	private final static int PARSE_ONTOLOGY_ROLES_STATE = 3;
	private final static int PARSE_ROLE_BEGIN_STATE = 4;
	private final static int PARSE_ROLE_SLOTS_STATE = 5;
	private final static int PARSE_ROLE_END_STATE = 6;
	private final static int PARSE_SLOT_BEGIN_STATE = 7;
	private final static int PARSE_SLOT_NAME_OK_STATE = 8;
	private final static int PARSE_SLOT_CATEGORY_OK_STATE = 9;
	private final static int PARSE_SLOT_TYPE_OK_STATE = 10;
	private final static int PARSE_SLOT_END_STATE = 11;
	private final static int END_STATE = 12;
	private final static int ERROR_STATE = 13;

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
    @see jade.onto.Ontology#joinOntology(Ontology o)
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
		 
  /*
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
    Creates a frame from a given Java Object representing an instance of 
    a given role.
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
    @see jade.onto.Ontology#isRole(String roleName)
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

  /** 
  	@return the name of the role represented by the passed class as 
  	registered in this ontology
  	@throws OntologyException if no role is found for this class
    @see jade.onto.Ontology#getRoleName(Class c)
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
					Class slotType = null;
	  			try {
	    			slotType = Class.forName(desc.getType()); 
	  				// System.out.println("- primitive class is: "+primitive.getName());
	    			if(!implType.isAssignableFrom(slotType))
	      			throw new OntologyException("1 Wrong class: the primitive slot " + desc.getName() + " is of type "+ implType + ", but must be a subtype of " + slotType + ".");
	  			} 
	  			catch (Exception e) {
	  				// The check might have failed because the compared types are arrays.
	  				// In this case in fact Class.forName() does not work.
	  				// Let's try also this case
	  				try{
	  					String type = desc.getType();
	  					Class componentImplType = implType.getComponentType();
	  					if (type.endsWith("[]") && componentImplType != null){
	  						String componentType = type.substring(0, type.length() - 2); 
	  						slotType = Class.forName(componentType);
	  						if (!componentImplType.isAssignableFrom(slotType)) 
	    						throw new OntologyException("2 Wrong class: the primitive slot " + desc.getName() + " is of type "+ componentImplType + "[], but must be a subtype of " + slotType + "[].");
	  					}
	  					else
	    					throw new OntologyException("3 Wrong class: the primitive slot " + desc.getName() + " is of type "+ implType + ", but must be a subtype of " + slotType + ".");
	  				}
	  				catch (Exception e1) {
	      			throw new OntologyException("4 Wrong class: the primitive slot " + desc.getName() + " is of type "+ implType + ", but must be a subtype of " + desc.getType() + ".");
	  				}
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
    
    // LOOP on slots 
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
	  			
				// COMPLEX SLOT: the value of this slot is another Frame object -->
	  		// Transform from sub-frame to sub-object by calling 
				// createObject() recursively.
	  		if(desc.isComplex()) {
	  			slotValue = createSingleObject((Frame)slotValue);
	  			setMethod.invoke(entity, new Object[] { slotValue });
				}
				
				// SET SLOT or SEQUENCE SLOT: the value of this slot is a set or sequence
				else if (desc.isSet()) {
	  			Frame set = (Frame) slotValue; //this is the frame representing the set
	  			if (desc.getType().equalsIgnoreCase(Ontology.ANY_TYPE)){
	    			for (int i=0; i<set.size(); i++) { 
	      			try { //try as a complex frame
								Object element = createSingleObject((Frame)set.getSlot(i));
								setMethod.invoke(entity, new Object[]{element});
	      			} 
	      			catch (Exception ee1) {
								// if exception then it is a primitive frame
								setMethod.invoke(entity, new Object[]{set.getSlot(i)}); 
	      			}
	    			} //end of for int
	  			} 
	  			else if (desc.hasPrimitiveType()) {
	    			for (int i=0; i<set.size(); i++) // add all the elements of the set
	      			setMethod.invoke(entity, new Object[]{ castPrimitiveValue(set.getSlot(i), desc.getType())});
	  			}
	  			else {// convert the elements into an object and then add
	    			for (int i=0; i<set.size(); i++) { 
	      			Object element = createSingleObject((Frame)set.getSlot(i));
	      			setMethod.invoke(entity, new Object[]{element});
	    			} 
	  			}
				}
				
				// PRIMITIVE SLOT: the value of this slot has a primitive type -->
				// It can be directly set.
				else { 
	  			setMethod.invoke(entity, new Object[] { castPrimitiveValue(slotValue, desc.getType()) });
				}
				slotPosition++;
				
      } // END of try
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
      
    } // END of LOOP on slots
    
    return entity;
  }

	private Object castPrimitiveValue(Object value, String type) throws IllegalArgumentException {
		// The CLParser cannot distinguish between Byte, Short, Integer, Long and 
		// between Character and string ....
		// Therefore it simply returns 
		// - Long for strings representing a valid integer value
		// - Double for strings representing a valid rational value 
		// - Boolean for strings equals to "true" and "false"
		// - Date for strings representing a valid UTCTime
		// - String for strings not belonging to the above cases
		// - Byte[] for non-string values
		// This method performs the necessary checks and conversions.
		
		String stringifiedValue = value.toString();
		Object castedValue = null;
		try{
			if (type.equalsIgnoreCase(Ontology.INTEGER_TYPE))
				castedValue = (Object) new Integer(stringifiedValue);
			else if (type.equalsIgnoreCase(Ontology.SHORT_TYPE))
				castedValue = (Object) new Short(stringifiedValue);
			else if (type.equalsIgnoreCase(Ontology.FLOAT_TYPE))
				castedValue = (Object) new Float(stringifiedValue);
			else if (type.equalsIgnoreCase(Ontology.CHARACTER_TYPE)){
				if (stringifiedValue.length() != 1)
					throw new IllegalArgumentException("Type mismatch for value " + stringifiedValue + " and type " + type);
				castedValue = (Object) new Character(stringifiedValue.charAt(0));
			}
			else if (type.equalsIgnoreCase(Ontology.BYTE_TYPE)){
				castedValue = (Object) new Byte(stringifiedValue);
			}
			else if (type.equalsIgnoreCase(Ontology.STRING_TYPE)){
				if (!value.getClass().equals(java.lang.Byte[].class))
					castedValue = stringifiedValue;
			}
			else 
				castedValue = value;
		}
		catch(NumberFormatException nfe) {
			throw new IllegalArgumentException("Format mismatch between value " + stringifiedValue + " and type " + type);
		}
		
		return castedValue;
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
  
  /**
    Initializes this DefaultOntology object from a text including an ontology
    according to the format described hereafter.
    
    ONTOLOGY
    ONTOLOGY-NAME <name>
    
    	ROLE
    	ROLE-NAME <name>
    
    		SLOT
    		[SLOT-NAME <name>]
    		SLOT-CATEGORY <category> // Must be one among Ontology.FRAME_SLOT ....
    		SLOT-TYPE <type>         // Must be one among Ontology.SLOT_TYPE ....
    		SLOT-PRESENCE <presence> // Must be M or O
    		END-SLOT // This block can be repeated n times
    		
    	[ROLE-ENTITY-FACTORY <factory>]
    	
    	END-ROLE // This block can be repeated n times
    	
    END-ONTOLOGY
    
    It should be noticed that if a Factory is specified for a given role, 
    that Factory must have an accessible constructor with no parameters.
    
    @param inp The <code>BufferedReader</code> where to read text from.
    @return The name of the ontology
    @see toText(BufferedWriter out)
  */  
  public String fromText(BufferedReader inp) throws IOException, ParseException, OntologyException {
  	StringBuffer sb = new StringBuffer();
  	String line = inp.readLine();
  	while (line != null) {
  		sb.append(" " + line);
  		line = inp.readLine();
  	}
	 	StringTokenizer st = new StringTokenizer(sb.toString(), " \t");
	 	
	 	String ontologyName = null;
	 	int state = INIT_STATE;
	 	while (state != END_STATE) {
	 		try {
	 			String token = st.nextToken();
	 			switch (state) {
	 			case INIT_STATE:
	 				if (token.equals(BEGIN_ONTOLOGY_TAG))
	 					state = PARSE_ONTOLOGY_BEGIN_STATE;
	 				else
						throw new ParseException("State PARSE_ONTOLOGY_BEGIN_STATE: Unexpected token " + token, 0);
	 				break;
	 				
				case PARSE_ONTOLOGY_BEGIN_STATE:
					if (token.equals(ONTOLOGY_NAME_TAG)) {
	 					ontologyName = st.nextToken();
	 					state = PARSE_ONTOLOGY_ROLES_STATE;
	 				}
	 				else 
						throw new ParseException("State PARSE_ONTOLOGY_BEGIN_STATE: Unexpected token " + token, 0);
	 				break;
	 				
				case PARSE_ONTOLOGY_ROLES_STATE:
					if (token.equals(BEGIN_ROLE_TAG))
						parseRole(st); 
						// does not change the state
					else if (token.equals(END_ONTOLOGY_TAG))
						state = END_STATE;
					else
						throw new ParseException("State PARSE_ONTOLOGY_ROLES_STATE: Unexpected token " + token, 0);
					break;
					
				default:
					throw new ParseException("Unknown parsing state after token " + token, 0);
	 			} // END of switch
	 			
	 		} // END of try
	 		catch (NoSuchElementException nsee) {
	 			throw new ParseException("End of stream reached before parsing termination", 0);
	 		}
	 	} // END of while
	 	
	 	return ontologyName;
  }
  
  private void parseRole(StringTokenizer st) throws ParseException, NoSuchElementException, OntologyException {
  	String roleName = null;
  	List slots = new ArrayList(); 
  	RoleEntityFactory ref = null;
  	
  	int state = PARSE_ROLE_BEGIN_STATE;
  	boolean stopFlag = false;
  	while (!stopFlag) {
  		String token = st.nextToken();
  		switch(state) {
			case PARSE_ROLE_BEGIN_STATE: 
				if (token.equals(ROLE_NAME_TAG)) {
					roleName = st.nextToken();
					state = PARSE_ROLE_SLOTS_STATE;
				}
				else
					throw new ParseException("State PARSE_ROLE_BEGIN_STATE: Unexpected token" + token, 0);
				break;

			case PARSE_ROLE_SLOTS_STATE: 
				if (token.equals(BEGIN_SLOT_TAG)){
					SlotDescriptor dsc = parseSlot(st, roleName);
					slots.add(dsc);
					// Does not change state
				}
				else if (token.equals(ROLE_FACTORY_TAG)) {
					String refName = st.nextToken();
					try {
						Class c = Class.forName(refName);
						ref = (RoleEntityFactory) (c.newInstance());
					}
					catch (IllegalAccessException iae) {
						throw new ParseException("State PARSE_ROLE_SLOTS_STATE: RoleEntityFactory " + refName + " for role " + roleName + " does not have an accessible constructor", 0);
					}
					catch (Exception e) {
						throw new ParseException("State PARSE_ROLE_SLOTS_STATE: RoleEntityFactory " + refName + " for role " + roleName + " cannot be loaded or instantiated", 0);
					}
					state = PARSE_ROLE_END_STATE;
				}
				else if (token.equals(END_ROLE_TAG))
					stopFlag = true;
				else
					throw new ParseException("State PARSE_ROLE_SLOTS_STATE: Unexpected token " + token + " parsing role " + roleName, 0);
				break;

			case PARSE_ROLE_END_STATE:
				if (token.equals(END_ROLE_TAG))
					stopFlag = true;
				else
					throw new ParseException("State PARSE_ROLE_END_STATE: Unexpected token " + token + " parsing role " + roleName, 0);		
				break;
				
			default:
				throw new ParseException("Unknown parsing state after token " + token, 0);
  		} // END of switch
  		
  	} // END of while
  	
  	// Add the role to the ontology
  	SlotDescriptor[] tmp = new SlotDescriptor[slots.size()];
  	Iterator it = slots.iterator();
  	int i = 0;
  	while (it.hasNext())
  		tmp[i++] = (SlotDescriptor) it.next();
  	if (ref != null)
  		addRole(roleName, tmp, ref);
  	else
  		addRole(roleName, tmp);
  
  	return;
  }
  
  private SlotDescriptor parseSlot(StringTokenizer st, String roleName) throws ParseException, NoSuchElementException{
  	String slotName = null;
  	int slotCategory = 0;
  	String slotType = null;
  	boolean isOptional = false;
  	
  	int state = PARSE_SLOT_BEGIN_STATE;
  	boolean stopFlag = false;
  	while (!stopFlag) {
  		String token = st.nextToken();
  		switch(state) {
			case PARSE_SLOT_BEGIN_STATE:
				if (token.equals(SLOT_NAME_TAG)) {
					slotName = st.nextToken();
					state = PARSE_SLOT_NAME_OK_STATE;
				}
				else if (token.equals(SLOT_CATEGORY_TAG)){
					Integer ii = new Integer(st.nextToken());
					slotCategory = ii.intValue();
					state = PARSE_SLOT_CATEGORY_OK_STATE;
				}
				else
					throw new ParseException("State PARSE_SLOT_BEGIN_STATE: Unexpected token " + token + " parsing role " + roleName, 0);
  			break;
  			
			case PARSE_SLOT_NAME_OK_STATE:
				if (token.equals(SLOT_CATEGORY_TAG)){
					Integer ii = new Integer(st.nextToken());
					slotCategory = ii.intValue();
					state = PARSE_SLOT_CATEGORY_OK_STATE;
				}
				else
					throw new ParseException("State PARSE_SLOT_NAME_OK_STATE: Unexpected token " + token + " parsing slot " + slotName + " of role " + roleName, 0);
				break;
				
			case PARSE_SLOT_CATEGORY_OK_STATE:
				if (token.equals(SLOT_TYPE_TAG)){
					slotType = st.nextToken();
					state = PARSE_SLOT_TYPE_OK_STATE;
				}
				else 
					throw new ParseException("State PARSE_SLOT_CATEGORY_OK_STATE: Unexpected token " + token + " parsing slot " + slotName + " of role " + roleName, 0);
				break;
				
			case PARSE_SLOT_TYPE_OK_STATE:
				if (token.equals(SLOT_PRESENCE_TAG)){
					String tmp = st.nextToken();
					isOptional = (tmp.equalsIgnoreCase("O"));
					state = PARSE_SLOT_END_STATE;
				}
				else
					throw new ParseException("State PARSE_SLOT_TYPE_OK_STATE: Unexpected token " + token + " parsing slot " + slotName + " of role " + roleName, 0);
				break;
				
			case PARSE_SLOT_END_STATE:
				if (token.equals(END_SLOT_TAG))
					stopFlag = true;
				else
					throw new ParseException("State PARSE_SLOT_END_STATE: Unexpected token " + token + " parsing slot " + slotName + " of role " + roleName, 0);
				break;
				
			default:
				throw new ParseException("Unknown parsing state after token " + token, 0);
				
  		} // END of switch
  		
  	} // END of while
  	
  	SlotDescriptor dsc = null;
  	if (slotName != null)
  		dsc = new SlotDescriptor(slotName, slotCategory, slotType, isOptional);
  	else
  		dsc = new SlotDescriptor(slotCategory, slotType, isOptional);
  	
  	return dsc;
  }
  
  /**
    Writes the ontology represented by this Ontology object as a text formatted
    according to the following syntax.
    ONTOLOGY
    ONTOLOGY-NAME <name>
    
    	ROLE
    	ROLE-NAME <name>
    
    		SLOT
    		[SLOT-NAME <name>]
    		SLOT-CATEGORY <category> // Must be one among Ontology.FRAME_SLOT ....
    		SLOT-TYPE <type>         // Must be one among Ontology.SLOT_TYPE ....
    		SLOT-PRESENCE <presence> // Must be M or O
    		END-SLOT // This block can be repeated n times
    		
    	[ROLE-ENTITY-FACTORY <factory>]
    	
    	END-ROLE // This block can be repeated n times
    	
    END-ONTOLOGY
    
    @param ontologyName This <code>String</code> will be used as the ontology name.
    @param out The <code>BufferedWriter</code> where to write text into.
    @see fromText(BufferedReader inp)
  */  
  public void toText(String ontologyName, BufferedWriter out) throws IOException {
  	// Ontology BEGIN TAG
  	out.write(BEGIN_ONTOLOGY_TAG);
  	out.newLine();
  	// Ontology Name
  	out.write(ONTOLOGY_NAME_TAG + " " + ontologyName);
  	out.newLine();
  	
  	// Loop on roles
  	Iterator i = schemas.values().iterator();
  	while (i.hasNext()) {
  		FrameSchema fs = (FrameSchema) i.next();
  		String roleName = fs.getName();
  		// Role BEGIN TAG
  		out.write(BEGIN_ROLE_TAG);
  		out.newLine();
  		// Role Name
			out.write(ROLE_NAME_TAG + " " + roleName);
  		out.newLine();
	  	
	  	// Loop on slots
	  	Iterator j = fs.subSchemas();
	  	while (j.hasNext()) {
	  		SlotDescriptor dsc = (SlotDescriptor) j.next();
	  		// Slot BEGIN TAG
	  		out.write(BEGIN_SLOT_TAG);
	  		out.newLine();
  			// Slot Name
	  		if (!(dsc.getName().equals("") || dsc.getName().startsWith(Frame.UNNAMEDPREFIX) ) ) {
	  			out.write(SLOT_NAME_TAG + " " + dsc.getName());
  				out.newLine();
	  		}
	  		// Slot Category
	  		out.write(SLOT_CATEGORY_TAG + " " + dsc.getCategory());
  			out.newLine();
	  		// Slot Type
	  		out.write(SLOT_TYPE_TAG + " " + dsc.getType());
  			out.newLine();
	  		// Slot Presence
	  		if (dsc.isOptional())
	  			out.write(SLOT_PRESENCE_TAG + " O");
	  		else
	  			out.write(SLOT_PRESENCE_TAG + " M");
  			out.newLine();
	  		// Slot END TAG
	  		out.write(END_SLOT_TAG);
  			out.newLine();
	  			
	  	} // END loop on slots
	  	
	  	// Role Entity Factory
    	if (factories.containsKey(new Name(roleName))) {
    		RoleEntityFactory fac = lookupFactory(roleName);
    		out.write(ROLE_FACTORY_TAG + " " + fac.getClass().getName());
  			out.newLine();
    	}
	  	// Role END TAG
	  	out.write(END_ROLE_TAG);
  		out.newLine();
	  	
  	} // END loop on roles
  	
  	// Ontology END TAG
  	out.write(END_ONTOLOGY_TAG);
  	out.newLine();
  	
  	out.flush();
  	out.close();
  }
}