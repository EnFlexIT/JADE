/**
 * ***************************************************************
 * JADE - Java Agent DEvelopment Framework is a framework to develop
 * multi-agent systems in compliance with the FIPA specifications.
 * Copyright (C) 2000 CSELT S.p.A.
 * 
 * GNU Lesser General Public License
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation,
 * version 2.1 of the License.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 * **************************************************************
 */
package jade.content.onto;

//#MIDP_EXCLUDE_FILE

import jade.content.*;
import jade.content.abs.*;
import jade.content.schema.*;
import jade.util.leap.List;
import jade.util.leap.Iterator;
import java.lang.reflect.*;

/**
 * Backward Compatible reflective introspector. This Introspector 
 * uses Java Reflection to translate java objects to/from abstract
 * descriptors as the <code>ReflectiveIntrospector</code> does, but 
 * it assumes the accessors methods for aggregate slots to be in the 
 * "old JADE style" i.e.
 * <i> For every aggregate <b>slot</b> named <code>XXX</code>,
 * with elements of type <code>T</code>, the Java class must have 
 * two accessible methods, with the following signature:</i>
 * <ul>
 *  	<li> <code>Iterator getAllXXX()</code>
 *  	<li> <code>void addXXX(T t)</code>
 * </ul> 
 * <br>
 * <b>NOT available in MIDP</b>
 * <br>
 * @author Giovanni Caire - TILAB
 */
public class BCReflectiveIntrospector extends ReflectiveIntrospector {
	
	/**
	 * Translate an object of a class representing an element in an
	 * ontology into a proper abstract descriptor.  
	 * @param obj The Object to be translated
	 * @param schema The schema for the ontological element this object
	 * is an instance of.
	 * @param javaClass The class of the Object to be translated
	 * @param referenceOnto The reference ontology in the context of
	 * this translation. 
	 * @return The Abstract descriptor produced by the translation 
	 * @throws OntologyException If some error occurs during the translation
	 */
	public AbsObject externalise(Object obj, ObjectSchema schema, Class javaClass, Ontology referenceOnto) throws OntologyException {
		try {
			AbsObject    abs = schema.newInstance();
			//System.out.println("Externalizing Object of class "+javaClass.getName()+". Schema is "+schema.getTypeName());
			
			String[]     names = schema.getNames();
			// Loop on slots
			for (int i = 0; i < names.length; ++i) {
				String slotName = names[i];
				//System.out.println("Handling slot "+slotName);
				
				// Retrieve the accessor method from the class and call it
				// Agregate slots require a special handling 
				ObjectSchema slotSchema = schema.getSchema(slotName);
				if (slotSchema instanceof AggregateSchema) {
					String methodName = "getAll" + translateName(slotName);
					Method getMethod = findMethodCaseInsensitive(methodName, javaClass);
					Object slotValue = invokeAccessorMethod(getMethod, obj);
					if (slotValue != null) {
						// Directly call AbsHelper.externaliseIterator() to properly handle different types of aggregate
						//#J2ME_EXCLUDE_BEGIN
						java.util.Iterator it = (java.util.Iterator) slotValue;
						//#J2ME_EXCLUDE_END
						/*#J2ME_INCLUDE_BEGIN
						Iterator it = (Iterator) slotValue;
						#J2ME_INCLUDE_END*/
						if (it.hasNext() || schema.isMandatory(slotName)) {
							AbsObject absSlotValue = AbsHelper.externaliseIterator(it, referenceOnto, slotSchema.getTypeName());
							AbsHelper.setAttribute(abs, slotName, absSlotValue);
						}
					} 
				}
				else {
					String methodName = "get" + translateName(slotName);
					Method getMethod = findMethodCaseInsensitive(methodName, javaClass);
					Object slotValue = invokeAccessorMethod(getMethod, obj);
					if (slotValue != null) {
						AbsObject absSlotValue = referenceOnto.fromObject(slotValue);
						AbsHelper.setAttribute(abs, slotName, absSlotValue);
					} 
				}
			}
			return abs;
		}
		catch (OntologyException oe) {
			throw oe;
		} 
		catch (Throwable t) {
			throw new OntologyException("Schema and Java class do not match", t);
		} 
	} 
	
	/**
	 * Translate an abstract descriptor into an object of a proper class 
	 * representing an element in an ontology 
	 * @param abs The abstract descriptor to be translated
	 * @param schema The schema for the ontological element this abstract descriptor
	 * is an instance of.
	 * @param javaClass The class of the Object to be produced by the translation
	 * @param referenceOnto The reference ontology in the context of
	 * this translation. 
	 * @return The Java object produced by the translation 
	 * @throws UngroundedException If the abstract descriptor to be translated 
	 * contains a variable
	 * @throws OntologyException If some error occurs during the translation
	 */
	public Object internalise(AbsObject abs, ObjectSchema schema, Class javaClass, Ontology referenceOnto) throws UngroundedException, OntologyException {
		
		try {     
			Object       obj = javaClass.newInstance();
			String[]     names = schema.getNames();
			
			// LOOP on slots 
			for (int i = 0; i < names.length; ++i) {
				String slotName = names[i];
				AbsObject absSlotValue = abs.getAbsObject(slotName);
				if (absSlotValue != null) {
					Object slotValue = referenceOnto.toObject(absSlotValue);
					
					// Retrieve the modifier method from the class and call it
					ObjectSchema slotSchema = schema.getSchema(slotName);
					String methodName;
					if (slotSchema instanceof AggregateSchema) {
						methodName = "add" + translateName(slotName);
						Method addMethod = findMethodCaseInsensitive(methodName, javaClass);
						invokeAddMethod(addMethod, obj, slotValue);
					}
					else {
						methodName = "set" + translateName(slotName);
						Method setMethod = findMethodCaseInsensitive(methodName, javaClass);
						invokeSetterMethod(setMethod, obj, slotValue);
					}
				} 
			}
			return obj;
		}
		catch (OntologyException oe) {
			throw oe;
		} 
		catch (InstantiationException ie) {
			throw new OntologyException("Class "+javaClass+" can't be instantiated", ie);
		} 
		catch (IllegalAccessException iae) {
			throw new OntologyException("Class "+javaClass+" does not have an accessible constructor", iae);
		} 
		catch (Throwable t) {
			throw new OntologyException("Schema and Java class do not match", t);
		} 
	} 
	
	private void invokeAddMethod(Method method, Object obj, 
			Object value) throws OntologyException {
		try {
			List l = (List) value;
			
			Iterator it = l.iterator();
			while (it.hasNext()) {
				Object ithValue = it.next();
				invokeSetterMethod(method, obj, ithValue);
			}
		} 
		catch (ClassCastException cce) {
			throw new OntologyException("Can't apply recursively method "+method.getName()+" to object "+obj+" as value "+value+" is not a List", cce);
		} 
	} 
	
	/**
	 Check the structure of a java class associated to an ontological element 
	 to ensure that translations to/from abstract descriptors and java objects
	 (instances of that class) can be accomplished by this introspector.
	 @param schema The schema of the ontological element
	 @param javaClass The java class associated to the ontologcal element
	 @param onto The Ontology that uses this Introspector
	 @throws OntologyException if the java class does not have the correct 
	 structure
	 */
	public void checkClass(ObjectSchema schema, Class javaClass, Ontology onto) throws OntologyException {
		String[] slotNames = schema.getNames();
		
		for (int i = 0; i < slotNames.length; ++i) {
			String sName = slotNames[i];
			ObjectSchema slotSchema = schema.getSchema(sName);
			String mName = translateName(sName);
			try {
				// Check for correct set and get methods for the current
				// slot and retrieve the implementation type for values.
				Class slotGetSetClass;
				if (slotSchema instanceof AggregateSchema)
					slotGetSetClass = checkGetAndSet2(mName, javaClass);
				else
					slotGetSetClass = checkGetAndSet(mName, javaClass);
				// If slotSchema is a complex schema and some class C is registered 
				// for that schema, then the implementation class must be a supertype 
				// of C.
				if(!(slotSchema instanceof PrimitiveSchema)) { 
					Class slotClass = onto.getClassForElement(slotSchema.getTypeName());
					if (slotClass != null) {
						if(!slotGetSetClass.isAssignableFrom(slotClass)) {
							throw new OntologyException("Wrong class for schema: "+schema.getTypeName()+". Slot "+sName+": expected class="+slotClass+", Get/Set method class="+slotGetSetClass);
						}
					}
				} 
				else {	
					// The slot has a primitive type
					String type = slotSchema.getTypeName();
					if (type.equals(BasicOntology.STRING)) {
						if (!slotGetSetClass.isAssignableFrom(String.class)) { 
							throw new OntologyException("Wrong class for schema: "+schema.getTypeName()+". Slot "+sName+": expected class="+String.class+", Get/Set method class="+slotGetSetClass);
						}
					}
					else if (type.equals(BasicOntology.INTEGER)) {
						if ((!slotGetSetClass.equals(Integer.TYPE)) &&
								(!slotGetSetClass.equals(Integer.class)) &&
								(!slotGetSetClass.equals(Long.TYPE)) &&
								(!slotGetSetClass.equals(Long.class)) ) { 
							throw new OntologyException("Wrong class for schema: "+schema.getTypeName()+". Slot "+sName+": expected class=INTEGER, Get/Set method class="+slotGetSetClass);
						}
					}
				}
			}
			catch(Exception e) {
				throw new OntologyException("Wrong class for schema: "+schema.getTypeName()+". Slot "+sName+": unexpected error. "+e.getMessage()); 
			}
		}
	}
	
	/**
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
	 */
	private Class checkGetAndSet2(String name, Class c) throws OntologyException {
		Method getMethod = findMethodCaseInsensitive("getAll" + name, c);
		Method addMethod = findMethodCaseInsensitive("add" + name, c);
		Class result = getArgumentType(addMethod,0);  
		
		// check "get" method 
		if (getArgumentLength(getMethod) != 0)
			throw new OntologyException("Wrong class: method " +  getMethod.getName() + "() must take no arguments.");
		// MODIFIED by GC
		// The return value of the getAllXXX() method of the user defined class 
		// must be a jade.util.leap.Iterator or a super-class/interface of it -->
		// OK if it is a java.util.Iterator.
		if (!(getReturnType(getMethod)).isAssignableFrom(jade.util.leap.Iterator.class))
			throw new OntologyException("Wrong class: method " +  getMethod.getName() + "() must return a jade.util.leap.Iterator." + getReturnType(getMethod).toString());
		
		// check 'add' method 
		if (getArgumentLength(addMethod) != 1)
			throw new OntologyException("Wrong class: method " +  addMethod.getName() + "() must take one argument.");
		if (!getArgumentType(addMethod,0).equals(result))
			throw new OntologyException("Wrong class: method " +  addMethod.getName() + "() has the wrong argument type.");
		if (!getReturnType(addMethod).equals(Void.TYPE))
			throw new OntologyException("Wrong class: method " +  addMethod.getName() + "() must return a void.");
		
		return result;
	}
	
	/**
	 @ return the Class of the argument type number no. of the method m
	 */
	private Class getArgumentType(Method m, int no) {
		Class[] setParams = m.getParameterTypes();
		return setParams[no];
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
}

