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
import jade.util.leap.Map;
import jade.util.leap.HashMap;
import jade.util.leap.Iterator;
import jade.util.leap.List;
import jade.util.leap.ArrayList;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.IOException;
import java.text.*;

import jade.onto.JADEMetaOntology.*;

import jade.lang.sl.SL0Codec;
import jade.lang.Codec.CodecException;
import jade.core.CaseInsensitiveString;

/**
  A simple implementation of the <code>Ontology</code> interface. Instances of
  this class keeps all the ontology data in memory, and don't support an
  external archive format.

  @author Giovanni Rimassa - Universita` di Parma
  @version $Date$ $Revision$
*/
public final class DefaultOntology implements Ontology {

	private Map schemas;
  private Map roleClasses;

  /**
    Default constructor.
  */
  public DefaultOntology() {
    schemas = new HashMap();
    roleClasses = new HashMap();
  }

  // Raw interface, exposes Frame Schemas directly.
  // This is package scoped, not to be used by applications.

  FrameSchema lookupSchema(String name) {
    return (FrameSchema)schemas.get(new CaseInsensitiveString(name));
  }


  /**
    Adds a new role to this ontology, without a user defined Java class to
    represent it.
    @see jade.onto.Ontology#addRole(String roleName, SlotDescriptor[] slots)
  */
  public void addRole(String roleName, SlotDescriptor[] slots) throws OntologyException {
    // Checks whether a role with this name already exists in the ontology
    if (lookupSchema(roleName) != null)
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
    @see jade.onto.Ontology#addRole(String roleName, SlotDescriptor[] slots, Class c)
  */  
  public void addRole(String roleName, SlotDescriptor[] slots, Class newClass) throws OntologyException {
    // Checks whether the user defined class representing the role already represents another role in the ontology
    Iterator it = roleClasses.keySet().iterator();
    while (it.hasNext()) {
    	Class c = (Class) roleClasses.get(it.next());
    	if (newClass.equals(c)) {
    		throw new OntologyException("The class \""+newClass.getName()+"\" already represents a role in this ontology");
    	}
    }
  	// Modified for compatibility with CLDC
  	//if (roleClasses.containsValue(newClass))
    //  throw new OntologyException("The class \""+newClass.getName()+"\" already represents a role in this ontology");
    
  	// Adds the role to the ontology
    addRole(roleName, slots);
    // Registers the user defined class representing the role
    checkClass(roleName, newClass);
    roleClasses.put(new CaseInsensitiveString(roleName), newClass);
  }

  /**
    Adds to this ontology all roles included into another ontology 
    @param o The <code>Ontology</code> object whose roles will 
    be added
    @see jade.onto.Ontology#joinOntology(Ontology o)
  */
  public void joinOntology(Ontology o) throws OntologyException {
    // Gets the names of all roles in the ontology to join
    for (Iterator i=o.getVocabulary().iterator(); i.hasNext(); ) {
      // For each role try to add it to the current ontology
      String name = (String) i.next();
      SlotDescriptor[] slots = o.getSlots(name);
      Class c = o.getClassForRole(name);
      if (c != null)
	addRole(name, slots, c);
      else
	addRole(name, slots);
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
      Class c = getClassForRole(roleName);
      if(c == null)
	throw new OntologyException("No class able to represent " + roleName + " role. Check the definition of the ontology.");

      Object o = create(f);
      return initObject(f, o, c);
  }

  /**
    Creates a frame from a given Java Object representing an instance of 
    a given role.
    @see jade.onto.Ontology#createFrame(Object o, String roleName)
  */
  public Frame createFrame(Object o, String roleName) throws OntologyException {
    Class theRoleClass = getClassForRole(roleName);
    if (theRoleClass == null)
      throw new OntologyException("No class able to represent " + roleName + " role. Check the definition of the ontology.");
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
    Class theRoleClass = getClassForRole(roleName);
    if (theRoleClass == null)
      throw new OntologyException("No class able to represent " + roleName + " role.");
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
	  		throw new OntologyException("Internal error: the reflected method "+m+" threw an exception.\nMessage was " + msg);
			}
			catch(IllegalAccessException iae) {
	  		throw new OntologyException("Internal error: the required method "+m+" is not accessible [" + iae.getMessage() + "]");
			}
			catch(SecurityException se) {
	  		throw new OntologyException("Wrong class: some required method is not accessible:"+m); 
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
    for (Iterator i=roleClasses.keySet().iterator(); i.hasNext(); ) {
      Object key = i.next();
      if (c.equals((Class) roleClasses.get(key))) {
      	return ((CaseInsensitiveString) key).toString();
      }
  		// Modified for compatibility with CLDC
    	//Map.Entry elem = (Map.Entry)i.next();
      //if (c.equals((Class)elem.getValue())) 
      //	return ((CaseInsensitiveString)elem.getKey()).toString();
    } 
    // if this instruction is executed, then no class has been found
    throw new OntologyException("No rolename registered in this ontology for class "+c.getName());
  }
  
  /** 
    @return a <code>List</code> including the names of all the roles
    in the ontology, i.e. the Vocabulary used by the ontology
    @see jade.onto.Ontology#getVocabulary()
    **/
  public List getVocabulary(){
    // The Vocabulary is the list of the names of the roles in the ontology;
    // role names are stored as Name while we want to return them as String
  	List vocabulary = new ArrayList();
  	Iterator i = schemas.keySet().iterator();
  	while (i.hasNext()){
  		String roleNameAsString = ((CaseInsensitiveString) (i.next())).toString();
  		vocabulary.add(roleNameAsString);
  	}
  	return vocabulary;	
  }
  




  /**
    Provides the Java class associated with this ontological role. This class is
    usually the class used by the <code>create()</code> method to instantiate
    objects. A useful technique is returning an interface or an abstract class,
    while using concrete subclasses to create objects.
    @param a string representing the name of the ontological role
    @return the Java class that plays this ontological role (e.g. <code>DFAgentDescription.class</code>
  */
  public Class getClassForRole(String roleName) {
    return (Class) roleClasses.get(new CaseInsensitiveString(roleName));
  }
  


  // Private methods.

  /**
   * if name starts with UNNAMED_PREFIX, it removes it
   * @return the name of a method, given the name of a slot
   */
	private String translateName(String name) {
		String       n;
		StringBuffer buf = new StringBuffer();
		if (name.startsWith(Frame.UNNAMEDPREFIX)) {
			n = name.substring(Frame.UNNAMEDPREFIX.length());
		} 
		else {
			n = name;
		} 

		boolean capitalize = false;

		for (int i = 0; i < n.length(); i++) {
			char c = n.charAt(i);
			switch (c) {
			case ':':
				// Just ignore it
	     	break;
			case '-':
				// Don't copy the character, but capitalize the next
				// one so that x-y becomes xY
	     	capitalize = true;
				break;
 			default:
				if (capitalize) {
					buf.append(Character.toUpperCase(c));
					capitalize = false;
				} 
				else {
					buf.append(c);
				} 
			}
		} 
		return new String(buf);
	} 
/*
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
*/

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
    // MODIFIED by GC
    // The return value of the getAllXXX() method of the user defined class 
    // must be a jade.util.leap.Iterator or a super-class/interface of it -->
    // OK if it is a java.util.Iterator.
    if (!(getReturnType(getMethod)).isAssignableFrom(jade.util.leap.Iterator.class))
    //if (!getReturnType(getMethod).equals(jade.util.leap.Iterator.class))
      throw new OntologyException("Wrong class: method " +  getMethod.getName() + "() must return a jade.util.leap.Iterator." + getReturnType(getMethod).toString());

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
	  Class complex = getClassForRole(desc.getType());
	  if (complex != null) {
	    // System.out.println("- complex class is: "+complex.getName());
	    if(!implType.isAssignableFrom(complex))
	      throw new OntologyException("Wrong class: the " + desc.getName() + " role is represented by " + complex + " class, which is not a subtype of " + implType + " class.");
	  }
	} else {	// Check that the returned type is compatible with the one dictated by the SlotDescriptor
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
      	throw new OntologyException("Possibly a primitive value has been used instead of a Frame slot. RoleName="+roleName+" SlotName="+slotName,iacce);
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
  		//__JADE_ONLY__BEGIN
	    // If the getAllXXX() method of the user defined class returns a
	    // java.util.Iterator --> OK. If it returns a jade.util.leap.Iterator 
	    // the cast works in any case as jade.util.leap.Iterator extends java.util.Iterator
	    java.util.Iterator i = (java.util.Iterator) value;
  		//__JADE_ONLY__END
  		/*__J2ME_COMPATIBILITY__BEGIN If we are on J2ME the getAllXXX method definitely returns a jade.util.leap.Iterator
	    // The getAllXXX() method of the user defined class must return 
	    // a jade.util.leap.Iterator
	    jade.util.leap.Iterator i = (jade.util.leap.Iterator) value;
  		__J2ME_COMPATIBILITY__END*/
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
				throw new OntologyException("Internal error: the reflected method "+getMethod+" threw an exception.\nMessage was " + msg);
      }
      catch(IllegalAccessException iae) {
				throw new OntologyException("Internal error: the required method "+getMethod+" is not accessible [" + iae.getMessage() + "]");
      }
      catch(SecurityException se) {
				throw new OntologyException("Wrong class: the required method "+getMethod+" is not accessible."); 
      }

    }

  }


  private void addSchemaToTable(String roleName, FrameSchema fs) {
    schemas.put(new CaseInsensitiveString(roleName), fs);
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
   * @see Ontology#fromSL0String(String)
  **/
  public String fromSL0String(String str) throws CodecException, OntologyException {
    Ontology meta = JADEMetaOntology.instance();
    schemas.clear();
    roleClasses.clear();
    List l = (new SL0Codec()).decode(str, meta); 
    AnOntology o = (AnOntology)meta.createObject(l).get(0);
    fromMetaOntologyRepresentation(o);
    return o.getName(); 
  }

  /**
    @see Ontology#fromMetaOntologyRepresentation(AnOntology)
    **/
  public void fromMetaOntologyRepresentation(AnOntology o) throws OntologyException {
    for (Iterator i=o.getAllRoles(); i.hasNext(); ) { //iteration on roles
      Role r = (Role)i.next();
      ArrayList slots = new ArrayList();
      for (Iterator j=r.getAllSlots(); j.hasNext(); ) { //iteration on slots
	Slot s = (Slot)j.next();
	SlotDescriptor sd;
	if (s.getName() != null) 
	  sd = new SlotDescriptor(s.getName(), s.getCategory().intValue(), s.getType(), s.getPresence().booleanValue());
	else
	  sd = new SlotDescriptor(s.getCategory().intValue(), s.getType(), s.getPresence().booleanValue());
	slots.add(sd);
      } //end iteration on slots
      SlotDescriptor[] sdarray = new SlotDescriptor[slots.size()];
      int counter = 0;
      for(Iterator it = slots.iterator(); it.hasNext(); )
	  sdarray[counter++] = (SlotDescriptor)it.next();
      if (r.getClassName() == null)
	addRole(r.getName(),sdarray);
      else 
	try {
	  addRole(r.getName(), sdarray, Class.forName(r.getClassName()));
	} catch (ClassNotFoundException e) {
	  System.out.println("WARNING: ClassNotFoundException in adding role "+r.getName()+" to the ontology. The role has been then added without any class");
	  addRole(r.getName(), sdarray);
	}
    } // end iteration on roles
  }
  

  /**
   * Return a String representing this ontology Object by calling
   * the method <code>toSL0String()</code> and catching any exception.
   * Notice that this method ignores the name of the ontology and, therefore,
   * the method <code>toSL0String()</code> should be preferred, instead.
   * @return the String representing this ontology, or null if any
   * exception occurs.
  **/
  public String toString() {
    try {
      return toSL0String("unknownOntologyName");
    } catch (OntologyException o) {
      o.printStackTrace();
    }
    return null;
  }

  /**
    @see jade.onto.Ontology#toMetaOntologyRepresentation(String)
    **/
  public AnOntology toMetaOntologyRepresentation (String ontologyName){
    AnOntology o = new AnOntology();
    if ((ontologyName == null) || (ontologyName.trim().equals("")))
      o.setName("unknownOntologyName");
    else
      o.setName(ontologyName);
    
    // Loop on roles
    for (Iterator i=schemas.values().iterator(); i.hasNext(); ) {
      FrameSchema fs = (FrameSchema) i.next();
      String roleName = fs.getName();
      Role r = new Role();
      r.setName(roleName);
      Class c = getClassForRole(roleName);
      if (c != null) 
	r.setClassName(c.getName());
      // Loop on slots
      for (Iterator j = fs.subSchemas(); j.hasNext(); ) {
	SlotDescriptor dsc = (SlotDescriptor) j.next();
	Slot s = new Slot();
	// Slot Name
	if (!(dsc.getName().equals("") || dsc.getName().startsWith(Frame.UNNAMEDPREFIX) ) ) 
	  s.setName(dsc.getName());
	// Slot Category
	s.setCategory(new Long(dsc.getCategory()));
	// Slot Type
	s.setType(dsc.getType());
	// Slot Presence
	s.setPresence(new Boolean(dsc.isOptional()));
	r.addSlots(s);
      } // END loop on slots
      o.addRoles(r);
    } // END loop on roles
    return o;
  }

  /**
    Writes the ontology represented by this Ontology object as an
    SL-0 expression. 
    @see jade.onto.Ontology#toSL0String(String) 
  */  
  public String toSL0String(String ontologyName) throws OntologyException {
    AnOntology o = toMetaOntologyRepresentation(ontologyName);
    Ontology meta = JADEMetaOntology.instance();
    String s;
    List l = new ArrayList(1);
    l.add(meta.createFrame(o, meta.getRoleName(o.getClass())));
    s = (new SL0Codec()).encode(l, meta); 
    return(s);
  }



  public Object create(Frame f) throws OntologyException {
    try {
      return getClassForRole(f.getName()).newInstance();
    } catch (Exception e) {
      throw new OntologyException(e.getMessage()); 
    }
  }

}
