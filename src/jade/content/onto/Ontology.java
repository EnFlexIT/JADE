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

import jade.content.*;
import jade.content.abs.*;
import jade.content.schema.*;
import jade.util.leap.List;
import jade.util.leap.Iterator;
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
 * <li>one base ontology that it extends;
 * <li>a set of <i>element schemata</i>.
 * </ul>
 * Element schemata are objects describing the structure of concepts, actions, 
 * predicate, etc. that are allowed in messages. For example, 
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
 * Schemata that describe concepts support inheritance (this is not true for
 * all other schemata, e.g., predicates, actions, etc.). You can define the
 * concept <code>Man</code> as a refinement of the concept <code>Person</code>:
 * <code>
 * ConceptSchema manSchema = new ConceptSchema(MAN);
 * manSchema.addSuperClass(personSchema);
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
 * objects belonging to classes that the ontology associates with schemata.<br>
 * As the previous example suggests, you cannot use objects of class
 * <code>Person</code> when asking for the value of some attribute, e.g., when 
 * asking for the value of <code>address</code>. Basically, the problem is that
 * you cannot 'assign' a variable to an attribute of an object, i.e., 
 * you cannot write something like: 
 * <code>person.setName(new Variable("X"))</code>.<br>
 * In order to solve this problem, you can describe your content in terms of
 * <i>abstract descriptors</i>. An abstract descriptor is an
 * object that reifies an element of the ontology.
 * The following is the definition of the abstract
 * descriptor for the concept <code>Person</code>:
 * <code>
 * AbsConcept absPerson = new AbsConcept(MAN);
 * absPerson.setSlot(NAME,    "John");
 * absPerson.setSlot(ADDRESS, absAddress);
 * </code>
 * where <code>absAddress</code> is the abstract descriptor for the Mary's 
 * address:
 * <code>
 * AbsConcept absAddress = new AbsConcept(ADDRESS);
 * absAddress.setSlot(CITY, "London");
 * </code>
 * Objects of class <code>Ontology</code> allows you to:
 * <ul>
 * <li>register schemata with associated (i) a mandatory terms of the 
 *     vocabulary and, e.g., <code>NAME</code> (ii) an optional Java class, 
 *     e.g., <code>Person</code>;
 * <li>retrieve the registered information through various keys.
 * </ul>
 * The framework provides two ontologies that you can use for building your
 * application-specific ontologies:
 * <ul>
 * <li><code>BasicOntology</code>: that provides all basic elements, i.e., 
 *     primitive data types, aggregate types, etc.
 * <li><code>ACLOntology</code>: that extends the <code>BasicOntology</code> to
 *     provide the elements that the semantics of the FIPA ACL mandates, e.g., 
 *     the <code>Done</code> modality, variables with an associated 
 *     cardinality, etc.
 * </ul>
 * Application-specific ontologies should be implemented extending the 
 * <code>ACLOntology</code>. 

 * @see jade.content.Concept
 * @see jade.content.abs.AbsConcept
 * @see jade.content.schema.ConceptSchema
 * @see jade.content.onto.ACLOntology
 * @see jade.content.onto.BasicOntology
 * @author Federico Bergenti - Universita` di Parma
 */
public class Ontology {
	  private static final String DEFAULT_INTROSPECTOR_CLASS = "jade.content.onto.ReflectiveIntrospector";
    private Ontology[]   base = new Ontology[0];
    private String       name = null;
    private Introspector introspector = null;
    
    private Hashtable    elements = new Hashtable();
    private Hashtable    classes  = new Hashtable();
    private Hashtable    schemas  = new Hashtable();
    

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
        
        introspector.checkClass(schema, javaClass);
        
        CaseInsensitiveString s = new CaseInsensitiveString(schema.getTypeName());
        elements.put(s, schema);

        if (javaClass != null) {
            classes.put(s, javaClass);
            schemas.put(javaClass, schema);
        } 
    } 

    /**
     * Retrieves the schema associated with <code>name</code>. The 
     * search is extended to the base ontologies if the schema is not
     * found.
     * @param name the name of the schema in the vocabulary.
     * @return the schema or <code>null</code> if the schema is not found.
     * @throws OntologyException 
     */
    public ObjectSchema getSchema(String name) throws OntologyException {
    	return getSchema(name, true);
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
  			return toObject(abs, this);
  		}
      catch (UnknownSchemaException use) {
      	// If we get this exception here, the schema is globally unknown 
      	// (i.e. is unknown in the reference ontology and all its base
      	// ontologies) --> throw a generic OntologyException
      	throw new OntologyException("No schema found for type "+abs.getTypeName());
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
     * Retrieves the schema associated with <code>name</code>.
     * @param name the name of the schema in the vocabulary.
     * @param searchInBase If <code>true</code> the 
     * search is extended to the base ontologies if the schema is not
     * found.
     * @return the schema.
     * @throws OntologyException
     */
    ObjectSchema getSchema(String name, boolean searchInBase) throws OntologyException {
        if (name == null) {
            throw new OntologyException("Null schema identifier");
        } 

        ObjectSchema ret = (ObjectSchema) elements.get(name.toLowerCase());

        if (ret == null) {
        	//System.out.println("Schema for "+name+" not found in "+getName());
          if (searchInBase) {
            for (int i = 0; i < base.length; ++i) {
            	try {
            		if (base[i] == null)
            			System.out.println("Base ontology # "+i+" for ontology "+getName()+" is null");
                ret = base[i].getSchema(name);
                if (ret != null) {
                	return ret;
                }
              }
              catch (OntologyException oe) {
                // Ignore and try next one
              }
            }
          } 
        } 

        return ret;
    } 

    /**
     * Retrieves the schema associated with <code>javaClass</code>
     * The search is not extended to the base ontologies
     * @param javaClass the Java class
     * @return the schema
     * @throws OntologyException
     */
    ObjectSchema getSchema(Class javaClass) throws OntologyException {
        if (javaClass == null) {
            throw new OntologyException("Null schema identifier");
        } 
        return (ObjectSchema) schemas.get(javaClass);
    } 

    /**
     * Retrieves the concrete class associated with <code>name</code> in
     * the vocabulary. The search is not extended to the base ontologies
     * @param name the name of the schema.
     * @return the Java class.
     * @throws OntologyException
     */
    Class getClassForElement(String name) throws OntologyException {
        if (name == null) {
            throw new OntologyException("Null schema identifier");
        } 

        return (Class) classes.get(name.toLowerCase());
    } 
    
    /**
     * Converts an abstract descriptor to a Java object of the proper class.
     * @param abs the abstract descriptor.
     * @param globalOnto The ontology this ontology is part of (i.e. the 
     * ontology that extends this ontology).
     * @return the object
     * @throws OntologyException if some mismatch with the schema is found
     * @throws UngroundedException if the abstract descriptor contains a 
     * variable
     */ 
    private Object toObject(AbsObject abs, Ontology globalOnto) throws UngroundedException, OntologyException {
    		try {
    			if (introspector != null) {
        		//DEBUG System.out.println("Try to internalise "+abs+" through "+introspector);
        		return introspector.internalise(this, globalOnto, abs);
    			}
    			else {
    				// If the introspector is not set all schemas are unknown
    				throw new UnknownSchemaException();
    			}
        }
        catch (UnknownSchemaException use1) {
        	// Try to convert the abstract descriptor using the base ontologies
        	for (int i = 0; i < base.length; ++i) {
        		try {
        			return base[i].toObject(abs, globalOnto);
        		}
        		catch (UnknownSchemaException use2) {
        			// Try the next one
        		}	
        	}
        	throw use1;
        }    		
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
    private AbsObject fromObject(Object obj, Ontology globalOnto) 
    			throws UnknownSchemaException, OntologyException {
    				
        // If the object is already an abstract descriptor, just return it
    		//if (obj instanceof AbsObject) {
    		//	return (AbsObject) obj;
    		//}
    		
    		try {
    			if (introspector != null) {
        		//DEBUG System.out.println("Try to externalise "+obj+" through "+introspector);
        		return introspector.externalise(this, globalOnto, obj);
    			}
    			else {
    				throw new UnknownSchemaException();
    			}
    		}
        catch (UnknownSchemaException use1) {
        	// Try to convert the object using the base ontologies
        	for (int i = 0; i < base.length; ++i) {
        		try {
	        		return base[i].fromObject(obj, globalOnto);
        		}
        		catch (UnknownSchemaException use2) {
        			// Try the next one
        		}
        	}
        	throw use1;
        }
    } 

    
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
    	if (obj instanceof String ||
    		  obj instanceof Boolean ||
    		  obj instanceof Integer ||
    		  obj instanceof Long ||
    		  //__CLDC_UNSUPPORTED__BEGIN
    		  obj instanceof Float ||
    		  obj instanceof Double ||
    		  //__CLDC_UNSUPPORTED__END
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
    	throw new OntologyException("Object "+obj+" is not a term");
    }
 
    /**
     * Set an attribute in an abstract descriptor performing all 
     * necessary type checks.
     * @throws OntologyException if a type mismatch is detected
     */
    public static void setAttribute(AbsObject abs, String attrName, AbsObject attrValue) throws OntologyException { 
    	if (abs instanceof AbsAgentAction) {
				if (attrValue instanceof AbsTerm) {
					((AbsAgentAction) abs).set(attrName, (AbsTerm) attrValue);
					return;
				}
				if (attrValue instanceof AbsPredicate) {
					((AbsAgentAction) abs).set(attrName, (AbsPredicate) attrValue);
					return;
				}
			}
    	if (abs instanceof AbsConcept) {
				if (attrValue instanceof AbsTerm) {
					((AbsConcept) abs).set(attrName, (AbsTerm) attrValue);
					return;
				}
			}
			else if (abs instanceof AbsPredicate) {
				((AbsPredicate) abs).set(attrName, attrValue);
				return;
			}
			else if (abs instanceof AbsIRE) {
				if (attrValue instanceof AbsVariable && CaseInsensitiveString.equalsIgnoreCase(attrName, IRESchema.VARIABLE)) {
					((AbsIRE) abs).setVariable((AbsVariable) attrValue);
					return;
				}
				else if (attrValue instanceof AbsPredicate && CaseInsensitiveString.equalsIgnoreCase(attrName, IRESchema.PROPOSITION)) {
					((AbsIRE) abs).setProposition((AbsPredicate) attrValue);
					return;
				}
			}
			else if (abs instanceof AbsVariable) {
				if (attrValue instanceof AbsPrimitive && CaseInsensitiveString.equalsIgnoreCase(attrName, VariableSchema.NAME)) {
					((AbsVariable) abs).setName(((AbsPrimitive) attrValue).getString());
					return;
				}
				else if (attrValue instanceof AbsPrimitive && CaseInsensitiveString.equalsIgnoreCase(attrName, VariableSchema.VALUE_TYPE)) {
					((AbsVariable) abs).setType(((AbsPrimitive) attrValue).getString());
					return;
				}
			}
									
			// If we reach this point there is a type incompatibility
			throw new OntologyException("Type incompatibility: attribute "+attrName+" of "+abs+" is of type "+attrValue); 
    }
			
    
}
