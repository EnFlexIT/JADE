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

import java.util.Hashtable;
import java.util.Date;

import jade.content.Term;
import jade.content.abs.AbsObject;
import jade.content.schema.ObjectSchema;
import jade.util.leap.List;
import jade.util.leap.Iterator;
import jade.util.leap.Serializable;
import jade.util.Logger;
import jade.core.CaseInsensitiveString;

/**
 *  An application-specific ontology describes the elements that agents
 * can use within content of messages. It defines a vocabulary and
 * relationships between the elements in such a vocabulary.
 * The relationships can be:
 * <ul>
 * <li>structural, e.g., the predicate <code>fatherOf</code> accepts two
 *     parameters, a father and a set of children;
 * <li>semantic, e.g., a concept of class <code>Man</code> is also of class
 *     <code>Person</code>.
 * </ul>
 * Application-specific ontologies are implemented through objects
 * of class <code>Ontology</code>.<br>
 * An ontology is characterized by:
 * <ul>
 * <li>one name;
 * <li>one (or more) base ontology that it extends;
 * <li>a set of <i>element schemas</i>.
 * </ul>
 * Element schemas are objects describing the structure of concepts, actions,
 * and predicates. that are allowed in messages. For example,
 * <code>People</code> ontology contains an element schema called
 * <code>Person</code>. This schema states that a <code>Person</code> is
 * characterized by a <code>name</code> and by an <code>address</code>:
 * <code>
 * ConceptSchema personSchema = new ConceptSchema(PERSON);
 * personSchema.addSlot(NAME,    stringSchema);
 * personSchema.addSlot(ADDRESS, addressSchema, ObjectSchema.OPTIONAL);
 * </code>
 * where <code>PERSON<code>, <code>NAME</code> and <code>ADDRESS</code> are
 * string constants. When you register your schema with the ontology, such
 * constants become part of the vocabulary of the ontology.<br>
 * Schemas that describe concepts support inheritance. You can define the
 * concept <code>Man</code> as a refinement of the concept <code>Person</code>:
 * <code>
 * ConceptSchema manSchema = new ConceptSchema(MAN);
 * manSchema.addSuperSchema(personSchema);
 * </code>
 * Each element schema can be associated with a Java class to map elements of
 * the ontology that comply with a schema with Java objects of that class. The
 * following is a class that might be associated with the <code>Person</code>
 * schema:
 * <code>
 * public class Person extends Concept {
 *       private String  name    = null;
 *       private Address address =  null;
 *
 *       public void setName(String name) {
 *               this.name = name;
 *       }
 *
 *       public void setAddress(Address address) {
 *               this.address = address;
 *       }
 *
 *       public String getName() {
 *               return name;
 *       }
 *
 *       public Address getAddress() {
 *               return address;
 *       }
 * }
 * </code>
 * When sending/receiving messages you can represent your content in terms of
 * objects belonging to classes that the ontology associates with schemas.<br>
 * As the previous example suggests, you cannot use objects of class
 * <code>Person</code> when asking for the value of some attribute, e.g., when
 * asking for the value of <code>address</code>. Basically, the problem is that
 * you cannot 'assign' a variable to an attribute of an object, i.e.
 * you cannot write something like:
 * <code>person.setName(new Variable("X"))</code>.<br>
 * In order to solve this problem, you can describe your content in terms of
 * <i>abstract descriptors</i>. An abstract descriptor is an
 * object that reifies an element of the ontology.
 * The following is the creation of an abstract
 * descriptor for a concept of type <code>Man</code>:
 * <code>
 * AbsConcept absMan = new AbsConcept(MAN);
 * absMan.setSlot(NAME,    "John");
 * absMan.setSlot(ADDRESS, absAddress);
 * </code>
 * where <code>absAddress</code> is the abstract descriptor for John's
 * address:
 * <code>
 * AbsConcept absAddress = new AbsConcept(ADDRESS);
 * absAddress.setSlot(CITY, "London");
 * </code>
 * Objects of class <code>Ontology</code> allows you to:
 * <ul>
 * <li>register schemas with associated (i) a mandatory term of the
 *     vocabulary e.g. <code>NAME</code> and (ii) an optional Java class,
 *     e.g. <code>Person</code>;
 * <li>retrieve the registered information through various keys.
 * </ul>
 * The framework already provides the <code>BasicOntology</code> ontology
 * that provides all basic elements, i.e. primitive data types, aggregate
 * types, etc.
 * Application-specific ontologies should be implemented extending it.

 * @see jade.content.Concept
 * @see jade.content.abs.AbsConcept
 * @see jade.content.schema.ConceptSchema
 * @see jade.content.onto.BasicOntology
 * @author Federico Bergenti - Universita` di Parma
 * @author Giovanni Caire - TILAB
 */
public class Ontology implements Serializable {
		private static final String DEFAULT_INTROSPECTOR_CLASS = "jade.content.onto.ReflectiveIntrospector";
    private Ontology[]   base = new Ontology[0];
    private String       name = null;
    private Introspector introspector = null;

    private Hashtable    elements = new Hashtable(); // Maps type-names to schemas
    private Hashtable    classes  = new Hashtable(); // Maps type-names to java classes
    private Hashtable    schemas  = new Hashtable(); // Maps java classes to schemas

    private Logger logger = Logger.getMyLogger(this.getClass().getName());

    // This is required for compatibility with CLDC MIDP where XXX.class
    // is not supported
		private static Class absObjectClass = null;
		static {
			try {
				absObjectClass = Class.forName("jade.content.abs.AbsObject");
			}
			catch (Exception e) {
				// Should never happen
				e.printStackTrace();
			}
		}

    /**
     * Construct an Ontology object with a given <code>name</code>
     * that extends a given ontology.
     * The <code>ReflectiveIntrospector</code> is used by default to
     * convert between Java objects and abstract descriptors.
     * @param name The identifier of the ontology.
     * @param base The base ontology.
     */
    public Ontology(String name, Ontology base) {
      this(name, base, null);
      try {
      	introspector = (Introspector) Class.forName(DEFAULT_INTROSPECTOR_CLASS).newInstance();
      }
      catch (Exception e) {
      	throw new RuntimeException("Class "+DEFAULT_INTROSPECTOR_CLASS+"for default Introspector not found");
      }
    }

    /**
     * Construct an Ontology object with a given <code>name</code>
     * that uses a given Introspector to
     * convert between Java objects and abstract descriptors.
     * @param name The identifier of the ontology.
     * @param introspector The introspector.
     */
    public Ontology(String name, Introspector introspector) {
      this(name, new Ontology[0], introspector);
    }

    /**
     * Construct an Ontology object with a given <code>name</code>
     * that extends a given ontology and that uses a given Introspector to
     * convert between Java objects and abstract descriptors.
     * @param name The identifier of the ontology.
     * @param base The base ontology.
     * @param introspector The introspector.
     */
    public Ontology(String name, Ontology base, Introspector introspector) {
      this(name, (base != null ? new Ontology[]{base} : new Ontology[0]), introspector);
    }

    /**
     * Construct an Ontology object with a given <code>name</code>
     * that extends a given set of ontologies and that uses a given Introspector to
     * convert between Java objects and abstract descriptors.
     * @param name The identifier of the ontology.
     * @param base The base ontology.
     * @param introspector The introspector.
     */
    public Ontology(String name, Ontology[] base, Introspector introspector) {
        this.name = name;
        this.introspector = introspector;
        this.base = (base != null ? base : new Ontology[0]);
    }

    /**
     * Retrieves the name of this ontology.
     * @return the name of this ontology.
     */
    public String getName() {
        return name;
    }

    /**
     * Adds a schema to this ontology
     * @param schema The schema to add
     * @throws OntologyException
     */
    public void add(ObjectSchema schema) throws OntologyException {
        add(schema, null);
    }


    /**
     * Adds a schema to the ontology and associates it to the class
     * <code>javaClass</code>
     * @param schema the schema.
     * @param javaClass the concrete class.
     * @throws OntologyException
     */
    public void add(ObjectSchema schema, Class javaClass) throws OntologyException {
        if (schema.getTypeName() == null) {
            throw new OntologyException("Invalid schema identifier");
        }

        String s = schema.getTypeName().toLowerCase();
        elements.put(s, schema);

        if (javaClass != null) {
            classes.put(s, javaClass);
            if (!absObjectClass.isAssignableFrom(javaClass)) {
							if (introspector != null) {
	    					introspector.checkClass(schema, javaClass, this);
							}
            	schemas.put(javaClass, schema);
            }
            else {
            	// If the java class is an abstract descriptor check the
            	// coherence between the schema and the abstract descriptor
            	if (!javaClass.isInstance(schema.newInstance())) {
            		throw new OntologyException("Java class "+javaClass.getName()+" can't represent instances of schema "+schema);
            	}
            }
        }
    }

    /**
     * Retrieves the schema of element <code>name</code> in this ontology.
     * The search is extended to the base ontologies if the schema is not
     * found.
     * @param name the name of the schema in the vocabulary.
     * @return the schema or <code>null</code> if the schema is not found.
     * @throws OntologyException
     */
    public ObjectSchema getSchema(String name) throws OntologyException {
      if (name == null) {
        throw new OntologyException("Null schema identifier");
      }

      ObjectSchema ret = (ObjectSchema) elements.get(name.toLowerCase());

      if (ret == null) {
        if(logger.isLoggable(Logger.FINE))
          logger.log(Logger.FINE,"Ontology "+getName()+". Schema for "+name+" not found");
        for (int i = 0; i < base.length; ++i) {
          if (base[i] == null) {
            if(logger.isLoggable(Logger.FINE))
              logger.log(Logger.FINE,"Base ontology # "+i+" for ontology "+getName()+" is null");
          }
          ret = base[i].getSchema(name);
          if (ret != null) {
            return ret;
          }
        }
    	}
      return ret;
		}

    /**
     * Converts an abstract descriptor to a Java object of the proper class.
     * @param abs the abstract descriptor.
     * @return the object
     * @throws UngroundedException if the abstract descriptor contains a
     * variable
     * @throws OntologyException if some mismatch with the schema is found
     * @see #fromObject(Object)
     */
  	public Object toObject(AbsObject abs) throws OntologyException, UngroundedException {
			if (abs == null) {
				return null;
			}

  		try {
  			return toObject(abs, abs.getTypeName().toLowerCase(), this);
  		}
      catch (UnknownSchemaException use) {
      	// If we get this exception here, the schema is globally unknown
      	// (i.e. is unknown in the reference ontology and all its base
      	// ontologies) --> throw a generic OntologyException
      	throw new OntologyException("No schema found for type "+abs.getTypeName());
      }
      catch (OntologyException oe) {
      	// This ontology can have been thrown as the Abs descriptor is
      	// ungrounded. In this case an UngroundedException must be thrown.
      	// Note that we don't check ungrouding before to speed up performances
				if (!abs.isGrounded()) {
					throw new UngroundedException();
				}
				else {
      		throw oe;
				}
      }
  	}

    /**
     * Converts a Java object into a proper abstract descriptor.
     * @param obj the object
     * @return the abstract descriptor.
     * @throws OntologyException if some mismatch with the schema is found
     * @see #toObject(AbsObject)
     */
    public AbsObject fromObject(Object obj) throws OntologyException {
    	if (obj == null) {
    		return null;
    	}

    	try {
    		return fromObject(obj, this);
    	}
      catch (UnknownSchemaException use) {
      	// If we get this exception here, the schema is globally unknown
      	// (i.e. is unknown in the reference ontology and all its base
      	// ontologies) --> throw a generic OntologyException
      	throw new OntologyException("No schema found for class "+obj.getClass().getName());
      }
    }

    /**
     * Retrieves the concrete class associated with element <code>name</code>
     * in this ontology. The search is extended to the base ontologies
     * @param name the name of the schema.
     * @return the Java class or null if no schema called <code>name</code>
     * is found or if no class is associated to that schema.
     * @throws OntologyException if name is null
     */
    public Class getClassForElement(String name) throws OntologyException {
        if (name == null) {
            throw new OntologyException("Null schema identifier");
        }

        Class ret = (Class) classes.get(name.toLowerCase());

      	if (ret == null) {
        	for (int i = 0; i < base.length; ++i) {
          	ret = base[i].getClassForElement(name);
          	if (ret != null) {
            	return ret;
          	}
        	}
    		}
      	return ret;
    }

		//#APIDOC_EXCLUDE_BEGIN
    /**
     * Converts an abstract descriptor to a Java object of the proper class.
     * @param abs the abstract descriptor.
     * @param lcType the type of the abstract descriptor to be translated
     * aconverted into lower case. This is passed as parameters to avoid
     * making the conversion to lower case for each base ontology.
     * @param globalOnto The ontology this ontology is part of (i.e. the
     * ontology that extends this ontology).
     * @return the object
		 * @throws UnknownSchemaException If no schema for the abs descriptor
		 * to be translated is defined in this ontology.
     * @throws UngroundedException if the abstract descriptor contains a
     * variable
     * @throws OntologyException if some mismatch with the schema is found      * ontology. In this case UnknownSchema
     */
    protected Object toObject(AbsObject abs, String lcType, Ontology globalOnto)
    			throws UnknownSchemaException, UngroundedException, OntologyException {

      if(logger.isLoggable(Logger.FINE))
        logger.log(Logger.FINE,"Ontology "+getName()+". Abs is: "+abs);
      // Retrieve the schema
      ObjectSchema schema = (ObjectSchema) elements.get(lcType);
      if(logger.isLoggable(Logger.FINE))
        logger.log(Logger.FINE,"Ontology "+getName()+". Schema is: "+schema);
      if (schema != null) {

        // Retrieve the java class
        Class javaClass = (Class) classes.get(lcType);
        if (javaClass == null) {
        	throw new OntologyException("No java class associated to type "+abs.getTypeName());
        }
        if(logger.isLoggable(Logger.FINE))
          logger.log(Logger.FINE,"Ontology "+getName()+". Class is: "+javaClass.getName());

        // If the Java class is an Abstract descriptor --> just return abs
	      if (absObjectClass.isAssignableFrom(javaClass)) {
	        return abs;
	      }

    		if (introspector != null) {
                if(logger.isLoggable(Logger.FINE))
                  logger.log(Logger.FINE,"Ontology "+getName()+". Try to internalise "+abs+" through "+introspector);
        	return introspector.internalise(abs, schema, javaClass, globalOnto);
    		}
      }

      // If we get here --> This ontology is not able to translate abs
      // --> Try to convert it using the base ontologies
      for (int i = 0; i < base.length; ++i) {
        try {
        	return base[i].toObject(abs, lcType, globalOnto);
        }
        catch (UnknownSchemaException use) {
        	// Try the next one
        }
      }

      throw new UnknownSchemaException();
    }

    /**
     * Converts a Java object into a proper abstract descriptor.
     * @param obj the object
     * @param globalOnto The ontology this ontology is part of (i.e. the
     * ontology that extends this ontology).
     * @return the abstract descriptor.
		 * @throws UnknownSchemaException If no schema for the object to be
		 * translated is defined in this ontology.
     * @throws OntologyException if some mismatch with the schema is found
     */
    protected AbsObject fromObject(Object obj, Ontology globalOnto)
    			throws UnknownSchemaException, OntologyException {

      // If obj is already an abstract descriptor --> just return it
    	if (obj instanceof AbsObject) {
    		return (AbsObject) obj;
    	}

    	// Retrieve the Java class
      Class        javaClass = obj.getClass();
      if(logger.isLoggable(Logger.FINE))
        logger.log(Logger.FINE,"Ontology "+getName()+". Class is: "+javaClass);

      // Retrieve the schema
      ObjectSchema schema = (ObjectSchema) schemas.get(javaClass);
      if(logger.isLoggable(Logger.FINE))
        logger.log(Logger.FINE,"Ontology "+getName()+". Schema is: "+schema);
      if (schema != null) {
    		if (introspector != null) {
                if(logger.isLoggable(Logger.FINE))
                  logger.log(Logger.FINE,"Ontology "+getName()+". Try to externalise "+obj+" through "+introspector);
        	return introspector.externalise(obj, schema, javaClass, globalOnto);
        }
    	}

      // If we get here --> This ontology is not able to translate obj
      // --> Try to convert it using the base ontologies
      for (int i = 0; i < base.length; ++i) {
        try {
	        return base[i].fromObject(obj, globalOnto);
        }
        catch (UnknownSchemaException use) {
        	// Try the next one
        }
      }

      throw new UnknownSchemaException();
    }
		//#APIDOC_EXCLUDE_END


    /////////////////////////
    // Utility static methods
    /////////////////////////

    /**
     * Check whether a given object is a valid term.
     * If it is an Aggregate (i.e. a <code>List</code>) it also check
     * the elements.
     * @throws OntologyException if the given object is not a valid term
     */
    public static void checkIsTerm(Object obj) throws OntologyException {
    	// FIXME: This method is likely to be removed as it does not add any value and creates problems
    	// when using the Serializable Ontology
    	/*if (obj instanceof String ||
    		  obj instanceof Boolean ||
    		  obj instanceof Integer ||
    		  obj instanceof Long ||
    		  //#MIDP_EXCLUDE_BEGIN
    		  obj instanceof Float ||
    		  obj instanceof Double ||
    		  //#MIDP_EXCLUDE_END
    		  obj instanceof Date ||
    		  obj instanceof Term) {
    		return;
    	}
    	if (obj instanceof List) {
    		Iterator it = ((List) obj).iterator();
    		while (it.hasNext()) {
    			checkIsTerm(it.next());
    		}
    		return;
    	}

    	// If we reach this point the object is not a term
    	throw new OntologyException("Object "+obj+" of class "+obj.getClass().getName()+" is not a term");
    	*/
    }
}
