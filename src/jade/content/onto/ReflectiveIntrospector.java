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
import jade.core.CaseInsensitiveString;

/**
   The default introspector for user defined ontologies that uses 
   Java Reflection to translate java objects to/from abstract
   descriptors.
   <br>
   <b>NOT available in MIDP</b>
   <br>
   @author Federico Bergenti - Universita` di Parma
   @author Giovanni Caire - TILAB
 */
public class ReflectiveIntrospector implements Introspector {

	/**
	 * Translate an object of a class representing an element in an
	 * ontology into a proper abstract descriptor 
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
			String[]     names = schema.getNames();

			// Loop on slots
			for (int i = 0; i < names.length; ++i) {
				String slotName = names[i];

				// Retrieve the accessor method from the class and call it
				/*String methodName = "get" + translateName(slotName);
				Method getMethod = findMethodCaseInsensitive(methodName, javaClass);
				Object slotValue = invokeAccessorMethod(getMethod, obj);*/
				Object slotValue = getSlotValue(slotName, obj, schema);
				if (slotValue != null) {
					// Agregate slots require a special handling 
					if (isAggregateObject(slotValue)) {
						ObjectSchema slotSchema = schema.getSchema(slotName);
						externaliseAndSetAggregateSlot(abs, schema, slotName, slotValue, slotSchema, referenceOnto);
					}
					else {
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
	
	public Object getSlotValue(String slotName, Object obj, ObjectSchema schema) throws OntologyException {
		String methodName = "get" + translateName(slotName);
		Method getMethod = findMethodCaseInsensitive(methodName, obj.getClass());
		return invokeAccessorMethod(getMethod, obj);
	}

	//#APIDOC_EXCLUDE_BEGIN
	protected boolean isAggregateObject(Object slotValue) {
		return slotValue instanceof List;
	}

	protected void externaliseAndSetAggregateSlot(AbsObject abs, ObjectSchema schema, String slotName, Object slotValue, ObjectSchema slotSchema, Ontology referenceOnto) throws OntologyException {
		List l = (List) slotValue;
		if (!l.isEmpty() || schema.isMandatory(slotName)) {
			AbsObject absSlotValue = AbsHelper.externaliseList(l, referenceOnto, slotSchema.getTypeName()); 
			AbsHelper.setAttribute(abs, slotName, absSlotValue);
		}
	}

	protected Object invokeAccessorMethod(Method method, Object obj) throws OntologyException {
		try {
			return method.invoke(obj, (Object[]) null);
		} 
		catch (Exception e) {
			throw new OntologyException("Error invoking accessor method "+method.getName()+" on object "+obj, e);
		} 
	} 
	//#APIDOC_EXCLUDE_END

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
					Object slotValue = null;
					// Aggregate slots require a special handling 
					if (absSlotValue.getAbsType() == AbsObject.ABS_AGGREGATE) {
						slotValue = internaliseAggregateSlot((AbsAggregate) absSlotValue, referenceOnto);
					}
					else {
						slotValue = referenceOnto.toObject(absSlotValue);
					}

					// Retrieve the modifier method from the class and call it
					/*String methodName = "set" + translateName(slotName);
					Method setMethod = findMethodCaseInsensitive(methodName, javaClass);
					invokeSetterMethod(setMethod, obj, slotValue);*/
					setSlotValue(slotName, slotValue, obj, schema);
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
	
	public void setSlotValue(String slotName, Object slotValue, Object obj, ObjectSchema schema) throws OntologyException {
		String methodName = "set" + translateName(slotName);
		Method setMethod = findMethodCaseInsensitive(methodName, obj.getClass());
		invokeSetterMethod(setMethod, obj, slotValue);
	}

	//#APIDOC_EXCLUDE_BEGIN
	protected Object internaliseAggregateSlot(AbsAggregate absAggregate, Ontology referenceOnto) throws OntologyException {
		List l = AbsHelper.internaliseList(absAggregate, referenceOnto);
		// FIXME: Here we should check for Long --> Integer casting, but how?
		return l;
	}

	protected void invokeSetterMethod(Method method, Object obj, 
			Object value) throws OntologyException {
		try {
			Object[] params = new Object[] {value};
			try {
				method.invoke(obj, params);
			}
			catch (IllegalArgumentException iae) {
				// Maybe the method required an int argument and we supplied 
				// a Long. Similarly maybe the method required a float and 
				// we supplied a Double. Try these possibilities
				params[0] = BasicOntology.resolveNumericValue(value, method.getParameterTypes()[0]);

				method.invoke(obj, params);
			}
		} 
		catch (Exception e) {
			throw new OntologyException("Error invoking setter method "+method.getName()+" on object "+obj+" with parameter "+value, e);
		}
	} 
	//#APIDOC_EXCLUDE_END

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
		// FIXME: Not yet implemented
	}

	//#APIDOC_EXCLUDE_BEGIN
	protected Method findMethodCaseInsensitive(String name, Class c) throws OntologyException {
		Method[] methods = c.getMethods();
		for(int i = 0; i < methods.length; i++) {
			String ithName = methods[i].getName();
			if(CaseInsensitiveString.equalsIgnoreCase(ithName, name))
				return methods[i];
		}
		throw new OntologyException("Method " + name + " not found in class "+c.getName());
	}

	protected String translateName(String name) {
		StringBuffer buf = new StringBuffer();

		// Capitalize the first char so that e.g. getxxx becomes getXxx 
		boolean capitalize = true;

		for (int i = 0; i < name.length(); i++) {
			char c = name.charAt(i);
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
		return buf.toString();
	} 
	//#APIDOC_EXCLUDE_END
}

