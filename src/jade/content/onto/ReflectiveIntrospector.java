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

import jade.content.*;
import jade.content.abs.*;
import jade.content.schema.*;
import jade.util.leap.List;
import jade.util.leap.Iterator;
import java.lang.reflect.*;

/**
 * @author Federico Bergenti - Universita` di Parma
 */
public class ReflectiveIntrospector implements Introspector {

    /**
     * Translate an object of a class representing an element in an
     * ontology into a proper abstract descriptor 
     * @param onto The ontology that uses this Introspector.
     * @param referenceOnto The reference ontology in the context of
     * this translation i.e. the most extended ontology that extends 
     * <code>onto</code> (directly or indirectly). 
     * @param obj The Object to be translated
     * @return The Abstract descriptor produced by the translation 
		 * @throws UnknownSchemaException If no schema for the object to be
		 * translated is defined in the ontology that uses this Introspector
		 * @throws OntologyException If some error occurs during the translation
     */
    public AbsObject externalise(Ontology onto, Ontology referenceOnto, Object obj) 
    				throws UnknownSchemaException, OntologyException {
        try {
            Class        javaClass = obj.getClass();            
            ObjectSchema schema = onto.getSchema(javaClass);
            if (schema == null) {
            	throw new UnknownSchemaException();
            }
            //DEBUG System.out.println("Schema is: "+schema);
            System.out.println("Schema is: "+schema);
            AbsObject    abs = schema.newInstance();
            Method[]     methods = javaClass.getMethods();
            String[]     names = schema.getNames();

            //FIXME: The correct way to do this would be to loop on
            // slot names and call related get methods.
            for (int i = 0; i < methods.length; i++) {
            	Method m = methods[i];
              String methodName = m.getName();

              if (methodName.startsWith("get")) {
                String attributeName = (methodName.substring(3, methodName.length())).toUpperCase();

                if (schema.isSlot(attributeName)) {
            			//DEBUG System.out.println("Handling attribute "+attributeName);
            			System.out.println("Handling attribute "+attributeName);
                  AbsObject attributeValue = invokeGetMethod(referenceOnto, m, obj);
            			//DEBUG System.out.println("Attribute value is: "+attributeValue);
            			System.out.println("Attribute value is: "+attributeValue);

                  if (attributeValue != null) {
                    //abs.set(attributeName, attributeValue);
                  	Ontology.setAttribute(abs, attributeName, attributeValue);
                  } 
                } 
              } 
            } 

            return abs;
        } 
        catch (OntologyException oe) {
            throw oe;
        } 
        catch (Throwable t) {
            throw new OntologyException("Schema and Java class do not match");
        } 
    } 

    private AbsObject invokeGetMethod(Ontology onto, Method method, 
                                      Object obj) throws OntologyException {
        Object result = null;
        try {
            result = method.invoke(obj, null);

            if (result == null) {
                return null;
            } 

            return onto.fromObject(result);
        } 
        catch (OntologyException oe) {
        		// Forward the exception
            throw oe;
        } 
        catch (Exception e) {
            throw new OntologyException("Error invoking get method");
        } 
    } 

    /**
     * Translate an abstract descriptor into an object of a proper class 
     * representing an element in an ontology 
     * @param onto The ontology that uses this Introspector.
     * @param referenceOnto The reference ontology in the context of
     * this translation i.e. the most extended ontology that extends 
     * <code>onto</code> (directly or indirectly). 
     * @param abs The abstract descriptor to be translated
     * @return The Java object produced by the translation 
     * @throws UngroundedException If the abstract descriptor to be translated 
     * contains a variable
		 * @throws UnknownSchemaException If no schema for the abstract descriptor
		 * to be translated is defined in the ontology that uses this Introspector
     * @throws OntologyException If some error occurs during the translation
     */
    public Object internalise(Ontology onto, Ontology referenceOnto, AbsObject abs) 
            throws UngroundedException, UnknownSchemaException, OntologyException {

        try {
        		String type = abs.getTypeName();
        		// Retrieve the schema
            ObjectSchema schema = onto.getSchema(type, false);
            if (schema == null) {
            	throw new UnknownSchemaException();
            }
            //DEBUG System.out.println("Schema is: "+schema);
            System.out.println("Schema is: "+schema);
            
            Class        javaClass = onto.getClassForElement(type);
            //DEBUG System.out.println("Class is: "+javaClass.getName());
            System.out.println("Class is: "+javaClass.getName());
            Object       obj = javaClass.newInstance();
            //DEBUG System.out.println("Object created");
            System.out.println("Object created");
            
            Method[]     methods = javaClass.getMethods();
            String[]     names = schema.getNames();

            for (int i = 0; i < methods.length; i++) {
            	Method m = methods[i];
              String methodName = m.getName();

              if (methodName.startsWith("set")) {
                String attributeName = (methodName.substring(3, methodName.length())).toUpperCase();

                if (schema.isSlot(attributeName)) {
            			//DEBUG System.out.println("Handling attribute "+attributeName);
            			System.out.println("Handling attribute "+attributeName);
                	AbsObject attributeValue = abs.getAbsObject(attributeName);
            			//DEBUG System.out.println("Attribute value is: "+attributeValue);
            			System.out.println("Attribute value is: "+attributeValue);

                  if (attributeValue != null) {
                  	invokeSetMethod(referenceOnto, m, obj, attributeValue);
                  } 
                } 
              } 
            } 

            return obj;
        } 
        catch (OntologyException oe) {
            throw oe;
        } 
        catch (Throwable t) {
            throw new OntologyException("Schema and Java class do not match");
        } 
    } 

    private void invokeSetMethod(Ontology onto, Method method, Object obj, 
                                 AbsObject value) throws OntologyException {
        try {
            Object objValue = onto.toObject(value);

            if (objValue == null) {
                return;
            } 

            Object[] params = new Object[] {
                objValue
            };

            method.invoke(obj, params);
        } 
        catch (OntologyException oe) {
        		// Forward the exception
            throw oe;
        } 
        catch (Exception e) {
            throw new OntologyException("Error invoking set method");
        } 
    } 

}

