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

import jade.content.abs.*;
import jade.content.schema.*;
import jade.util.leap.Serializable;

/** 
   This interface defines the methods to convert objects of
   ontological classes into/from abstract descriptors. Each ontology
   has an <code>Introspector</code> and delegates it the conversion.
   @author Federico Bergenti - Universita` di Parma
 */
public interface Introspector extends Serializable {

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
	AbsObject externalise(Object obj, ObjectSchema schema, Class javaClass, Ontology referenceOnto) 
	throws OntologyException;

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
	Object internalise(AbsObject abs, ObjectSchema schema, Class javaClass, Ontology referenceOnto) 
	throws UngroundedException, OntologyException;

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
	void checkClass(ObjectSchema schema, Class javaClass, Ontology onto) throws OntologyException;

	Object getSlotValue(String slotName, Object obj, ObjectSchema schema) throws OntologyException;

	void setSlotValue(String slotName, Object slotValue, Object obj, ObjectSchema schema) throws OntologyException;
}

