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

/** 
   This introspector does nothing but "asking objects to convert
   themselves into/from abstract descriptors". It could be used 
   when working in Java environments where the reflection is not 
   supported (MIDP). All classes in an ontology using this introspector
   must implement the <code>Introspectable</code> interface.
   @author Giovanni Caire - TILAB
 */
public class MicroIntrospector implements Introspector {

    /**
     * Translate an object of a class representing an element in an
     * ontology into a proper abstract descriptor 
     * @param onto The reference ontology 
     * @param obj The Object to be translated
     * @return The Abstract descriptor produced by the translation 
		 * @throws UnknownSchemaException If no schema for the object to be
		 * translated is defined in the ontology that uses this Introspector
		 * @throws OntologyException If some error occurs during the translation
     */
    public AbsObject externalise(Object obj, ObjectSchema schema, Class javaClass, Ontology referenceOnto) 
    				throws OntologyException {
    				
    	try {
        AbsObject abs = schema.newInstance();
        
    		Introspectable intro = (Introspectable) obj;
    		intro.externalise(abs, referenceOnto);
    		return abs;
    	}
    	catch (OntologyException oe) {
    		// Just forward the exception
    		throw oe;
    	}
    	catch (ClassCastException cce) {
    		throw new OntologyException("Object "+obj+" is not Introspectable");
    	}
    	catch (Throwable t) {
        throw new OntologyException("Schema and Java class do not match", t);
    	}
    } 

    /**
     * Translate an abstract descriptor into an object of a proper class 
     * representing an element in an ontology 
     * @param onto The reference ontology 
     * @param abs The abstract descriptor to be translated
     *
     * @return The Java object produced by the translation 
     * @throws UngroundedException If the abstract descriptor to be translated 
     * contains a variable
		 * @throws UnknownSchemaException If no schema for the abstract descriptor
		 * to be translated is defined in the ontology that uses this Introspector
     * @throws OntologyException If some error occurs during the translation
     */
    public Object internalise(AbsObject abs, ObjectSchema schema, Class javaClass, Ontology referenceOnto) 
            throws UngroundedException, OntologyException {
    	try {
        Object obj = javaClass.newInstance();
        //DEBUG System.out.println("Object created");
        
    		Introspectable intro = (Introspectable) obj;
    		intro.internalise(abs, referenceOnto);
    		return intro;
    	}
    	catch (OntologyException oe) {
    		// Just forward the exception
    		throw oe;
    	}
    	catch (ClassCastException cce) {
    		throw new OntologyException("Class for type "+abs.getTypeName()+" is not Introspectable");
    	}
    	catch (Throwable t) {
        throw new OntologyException("Schema and Java class do not match", t);
    	}
    } 

    /**
       Check the structure of a java class associated to an ontological element 
       to ensure that translations to/from abstract descriptors and java objects
       (instances of that class) can be accomplished by this introspector.
       This is the case if <code>javaClass</code> implements the
       <code>Introspectable</code>
       @param schema The schema of the ontological element
       @param javaClass The java class associated to the ontologcal element
       @param onto The Ontology that uses this Introspector
       @throws OntologyException if the java class does not have the correct 
       structure
     */
    public void checkClass(ObjectSchema schema, Class javaClass, Ontology onto) throws OntologyException {
    	// FIXME: Not yet implemented
    }
}
