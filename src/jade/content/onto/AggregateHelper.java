/*****************************************************************
 WADE - Workflow and Agent Development Environment is a framework to develop 
 multi-agent systems able to execute tasks defined according to the workflow
 metaphor.
 Copyright (C) 2008 Telecom Italia S.p.A. 

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
package jade.content.onto;

import java.lang.reflect.Array;
import java.util.Iterator;

import jade.content.schema.AggregateSchema;
import jade.content.schema.ObjectSchema;
import jade.content.schema.TermSchema;

public class AggregateHelper {

	/**
	 * Get ontology schema associated to class
	 * Try to manage as aggregate
	 * 
	 * @param clazz class to get schema
	 * @param elementSchema aggregate element schema   
	 * @return associated class schema
	 */
	public static ObjectSchema getSchema(Class clazz, TermSchema elementSchema) {
		ObjectSchema schema = null;
		
		// Sequence type
		if (java.util.List.class.isAssignableFrom(clazz) ||
			jade.util.leap.List.class.isAssignableFrom(clazz) ||
			clazz.isArray()) {

			schema = new AggregateSchema(BasicOntology.SEQUENCE, elementSchema);
		}

		// Set type
		else if (java.util.Set.class.isAssignableFrom(clazz) ||
			jade.util.leap.Set.class.isAssignableFrom(clazz)) {
			
			schema = new AggregateSchema(BasicOntology.SET, elementSchema);
		}

		return schema;
	}

	/**
	 * Try to convert, if possible, the aggregate value srcValue into an instance of destClass
	 * Possible source and destination classes are java array, java collection and jade collection 
	 * @throws Exception 
	 */
	public static Object adjustAggregateValue(Object srcValue, Class destClass) throws Exception {
		Object destValue = srcValue;
		if (srcValue != null) {
			Class srcClass = srcValue.getClass();
			if (srcClass != destClass) {
				
				// Destination is an array
				if (destClass.isArray()) {
					
					// Source is a java collection
					if (java.util.Collection.class.isAssignableFrom(srcClass)) {
						java.util.Collection javaCollection = (java.util.Collection)srcValue;
						Object array = collectionToArray(javaCollection.iterator(), javaCollection.size());
						if (array != null) {
							destValue = array;
						}
					}
					
					// Source is a jade collection
					else if (jade.util.leap.Collection.class.isAssignableFrom(srcClass)) {
						jade.util.leap.Collection jadeCollection = (jade.util.leap.Collection)srcValue;
						Object array = collectionToArray(jadeCollection.iterator(), jadeCollection.size());
						if (array != null) {
							destValue = array;
						}
					}
				}
				
				// Destination is a java collection
				else if (java.util.Collection.class.isAssignableFrom(destClass)) {

					// Source is an array
					if (srcClass.isArray()) {
						java.util.Collection javaCollection = (java.util.Collection)destClass.newInstance();
						int size = Array.getLength(srcValue);
						for (int index=0; index<size; index++) {
							javaCollection.add(Array.get(srcValue, index));
						}
						destValue = javaCollection;
					}
					
					// Source is a jade collection
					else if (jade.util.leap.Collection.class.isAssignableFrom(srcClass)) {
						java.util.Collection javaCollection = (java.util.Collection)destClass.newInstance();
						jade.util.leap.Collection jadeCollection = (jade.util.leap.Collection)srcValue;
						Iterator it = jadeCollection.iterator();
						while(it.hasNext()) {
							javaCollection.add(it.next());
						}
						destValue = javaCollection;
					}
				}
				
				// Destination is a jade collection
				else if (jade.util.leap.Collection.class.isAssignableFrom(destClass)) {

					// Source is an array
					if (srcClass.isArray()) {
						jade.util.leap.Collection jadeCollection = (jade.util.leap.Collection)destClass.newInstance();
						int size = Array.getLength(srcValue);
						for (int index=0; index<size; index++) {
							jadeCollection.add(Array.get(srcValue, index));
						}
						destValue = jadeCollection;
					}
					
					// Source is a java collection
					else if (java.util.Collection.class.isAssignableFrom(srcClass)) {
						jade.util.leap.Collection jadeCollection = (jade.util.leap.Collection)destClass.newInstance();
						java.util.Collection javaCollection = (java.util.Collection)srcValue;
						Iterator it = javaCollection.iterator();
						while(it.hasNext()) {
							jadeCollection.add(it.next());
						}
						destValue = jadeCollection;
					}
				}
			}
		}
		return destValue;
	}
	
	private static Object collectionToArray(Iterator it, int size) {
		int index = 0;
		Object array = null;
		while(it.hasNext()) {
			Object item = it.next();
			if (index == 0) {
				array = Array.newInstance(item.getClass(), size);
			}
			Array.set(array, index, item);
			index++;
		}
		return array;
	}
}
