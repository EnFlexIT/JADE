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

import jade.core.CaseInsensitiveString;
import jade.content.*;
import jade.content.abs.*;
import jade.content.schema.*;
import jade.util.leap.List;
import jade.util.leap.Iterator;
import java.lang.reflect.*;

/**
 * Backward Compatible reflective introspector. This Introspector 
 * uses Java Reflection to translate java object to/from abstract
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
  public AbsObject externalise(Object obj, ObjectSchema schema, Class javaClass, Ontology referenceOnto) 
    				throws OntologyException {
  	try {
      AbsObject    abs = schema.newInstance();
            
      String[]     names = schema.getNames();
      // Loop on slots
      for (int i = 0; i < names.length; ++i) {
      	String slotName = names[i];
      	
      	// Retrieve the accessor method from the class and call it
      	// Agregate slots require a special handling 
      	ObjectSchema slotSchema = schema.getSchema(slotName);
      	if (slotSchema instanceof AggregateSchema) {
					String methodName = "getAll" + translateName(slotName);
      		Method getMethod = findMethodCaseInsensitive(methodName, javaClass);
        	Object slotValue = invokeAccessorMethod(getMethod, obj);
        	if (slotValue != null) {
        		// Directly call AbsHelper.externaliseIterator() to properly handle different types of aggregate
        		Iterator it = (Iterator) slotValue;
        		if (it.hasNext()) {
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
  public Object internalise(AbsObject abs, ObjectSchema schema, Class javaClass, Ontology referenceOnto) 
            throws UngroundedException, OntologyException {

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
     @throws OntologyException if the java class does not have the correct 
     structure
   */
  public void checkClass(ObjectSchema schema, Class javaClass) throws OntologyException {
  	// FIXME: Not yet implemented
  }
    
}

