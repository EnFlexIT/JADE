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
package jade.content.schema;

import jade.content.onto.*;
import jade.content.abs.*;
import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;
import jade.content.schema.facets.*;
import jade.core.CaseInsensitiveString;

/**
 * @author Federico Bergenti - Universita` di Parma
 * @author Giovanni Caire - TILAB
 */
public abstract class ObjectSchema {
    /** 
       Canstant value indicating that a slot in a schema is mandatory,
       i.e. its value must not be null
     */
    public static final int MANDATORY = 0;
    /** 
       Canstant value indicating that a slot in a schema is optional,
       i.e. its value can be null
     */
    public static final int OPTIONAL = 1;
    /** 
       Canstant value indicating that a slot in a schema has an 
       infinite maximum cardinality
     */
    public static final int UNLIMITED = -1;
    
    //protected String          typeName = null;
    public static final String         BASE_NAME = "Object";
    //private static ObjectSchema baseSchema = new ObjectSchema();
    protected static ObjectSchema baseSchema = null;
    
    private boolean encodingByOrder = false;

    /**
     * Construct a schema that vinculates an entity to be a generic
     * object (i.e. no constraints at all)
     *
    private ObjectSchema() {
        this(BASE_NAME);
    }*/

    /**
     * Creates an <code>ObjectSchema</code> with a given type-name.
     * @param typeName The name of this <code>ObjectSchema</code>.
     */
    /*protected ObjectSchema(String typeName) {
        this.typeName = typeName;
    }*/

    /**
     * Retrieve the generic base schema for all objects.
     * @return the generic base schema for all objects.
     */
    public static ObjectSchema getBaseSchema() {
        return baseSchema;
    } 

    /**
     * Add a slot to the schema.
     * @param name The name of the slot.
     * @param slotSchema The schema defining the type of the slot.
     * @param optionality The optionality, i.e., <code>OPTIONAL</code> 
     * or <code>MANDATORY</code>
     */
    protected abstract void add(String name, ObjectSchema slotSchema, int optionality);/* {
    	CaseInsensitiveString ciName = new CaseInsensitiveString(name);
      if (slots.put(ciName, new SlotDescriptor(name, slotSchema, optionality)) == null) {
        	slotNames.addElement(ciName);
      }
    } */

  	/**
     * Add a mandatory slot to the schema.
     * @param name name of the slot.
     * @param slotSchema schema of the slot.
     */
    protected abstract void add(String name, ObjectSchema slotSchema);/* {
        add(name, slotSchema, MANDATORY);
    } */

    /**
     * Add a slot with cardinality between <code>cardMin</code>
     * and <code>cardMax</code> to this schema. 
     * Adding such a slot is equivalent to add a slot
     * of type Aggregate and then to add proper facets (constraints)
     * to check that the type of the elements in the aggregate are
     * compatible with <code>elementsSchema</code> and that the 
     * aggregate contains at least <code>cardMin</code> elements and
     * at most <code>cardMax</code> elements.
     * @param name The name of the slot.
     * @param elementsSchema The schema for the elements of this slot.
     * @param cardMin This slot must get at least <code>cardMin</code>
     * values
     * @param cardMax This slot can get at most <code>cardMax</code>
     * values
     */
    protected abstract void add(String name, ObjectSchema elementsSchema, int cardMin, int cardMax);/* {
      int optionality = (cardMin == 0 ? OPTIONAL : MANDATORY);
    	try {
    		add(name, BasicOntology.getInstance().getSchema(BasicOntology.SEQUENCE), optionality);
     		// Add proper facets
    		addFacet(name, new TypedAggregateFacet(elementsSchema));
    		addFacet(name, new CardinalityFacet(cardMin, cardMax));
    	}
    	catch (OntologyException oe) {
    		// Should never happen
    		oe.printStackTrace();
    	}
    } */

    /**
     * Add a super schema tho this schema, i.e. this schema will
     * inherit all characteristics from the super schema
     * @param superSchema the super schema.
     */
    protected abstract void addSuperSchema(ObjectSchema superSchema);/* {
        superSchemas.addElement(superSchema);
    } */

    /** 
       Add a <code>Facet</code> on a slot of this schema
       @param slotName the name of the slot the <code>Facet</code>
       must be added to.
       @param f the <code>Facet</code> to be added.
       @throws OntologyException if slotName does not identify
       a valid slot in this schema
     */
		protected abstract void addFacet(String slotName, Facet f) throws OntologyException; /* {
			if (containsSlot(slotName)) {
				CaseInsensitiveString ciName = new CaseInsensitiveString(slotName);
				Vector v = (Vector) facets.get(ciName);
				if (v == null) {
					v = new Vector();
					facets.put(ciName, v);
					//DEBUG
					//System.out.println("Added facet "+f+" to slot "+slotName); 
				}
				v.addElement(f);
			}
			else {
				throw new OntologyException(slotName+" is not a valid slot in this schema");
			}
		}*/
		
		/**
		 * Sets an indication about whether the preferred encoding for the 
		 * slots of concepts compliants to this schema is by oredr or by name. 
		 * It should be noted however that the Content Language encoder is 
		 * free to use or ignore this indication depending on the CL grammar 
		 * and actual implementation.
		 */
		public void setEncodingByOrder(boolean b) {
			encodingByOrder = b;
		}
		
		/**
		 * Get the indication whether the preferred encoding for the slots 
		 * of concepts compliant to this schema is by order or by name.
		 */
		public boolean getEncodingByOrder() {
			return encodingByOrder;
		}
		
    /**
     * Retrieves the name of the type of this schema.
     * @return the name of the type of this schema.
     */
    public abstract String getTypeName();/* {
        return typeName;
    } */

    /**
     * Returns the names of all the slots in this <code>Schema</code> 
     * (including slots defined in super schemas).
     *
     * @return the names of all slots.
     */
    public abstract String[] getNames();/* {
        Vector allSlotNames = new Vector();

        fillAllSlotNames(allSlotNames);

        String[] names = new String[allSlotNames.size()];
        int      counter = 0;
        for (Enumeration e = allSlotNames.elements(); e.hasMoreElements(); ) {
            names[counter++] = ((CaseInsensitiveString) e.nextElement()).toString();
        }

        return names;
    } */

    /**
     * Retrieves the schema of a slot of this <code>Schema</code>.
     *
     * @param name The name of the slot.
     * @return the <code>Schema</code> of slot <code>name</code>
     * @throws OntologyException If no slot with this name is present
     * in this schema.
     */
    public abstract ObjectSchema getSchema(String name) throws OntologyException;/* {
        CaseInsensitiveString ciName = new CaseInsensitiveString(name);
        SlotDescriptor slot = (SlotDescriptor) slots.get(ciName);

        if (slot == null) {
            for (Enumeration e = superSchemas.elements(); e.hasMoreElements(); ) {
                try {
                    ObjectSchema superSchema = (ObjectSchema) e.nextElement();
                    return superSchema.getSchema(name);
                } 
                catch (OntologyException oe) {
                	// Do nothing. Maybe the slot is defined in another super-schema
                }
            } 

            throw new OntologyException("No slot named: " + name);
        } 

        return slot.schema;
    } */

    /**
     * Indicate whether a given <code>String</code> is the name of a
     * slot defined in this <code>Schema</code>
     *
     * @param name The <code>String</code> to test.
     * @return <code>true</code> if <code>name</code> is the name of a
     * slot defined in this <code>Schema</code>.
     */
    public abstract boolean containsSlot(String name);/* {
        CaseInsensitiveString ciName = new CaseInsensitiveString(name);
        SlotDescriptor slot = (SlotDescriptor) slots.get(ciName);

        if (slot != null) {
            return true;
        } 

	      for (Enumeration e = superSchemas.elements(); e.hasMoreElements(); ) {
  	        ObjectSchema superSchema = (ObjectSchema) e.nextElement();
            if (superSchema.containsSlot(name)) {
               	return true;
            } 
        } 

        return false;
    } */

    /**
     * Creates an Abstract descriptor to hold an object compliant to 
     * this <code>Schema</code>.
     */
    public abstract AbsObject newInstance() throws OntologyException;/* {
    	throw new OntologyException("AbsObject cannot be instantiated");
    }*/

    /*private void fillAllSlotNames(Vector v) {
    		// Get slot names of super schemas first
        for (Enumeration e = superSchemas.elements(); e.hasMoreElements(); ) {
            ObjectSchema superSchema = (ObjectSchema) e.nextElement();

            superSchema.fillAllSlotNames(v);
        } 
				
        // Then add slot names of this schema
        for (Enumeration e = slotNames.elements(); e.hasMoreElements(); ) {
        	v.addElement(e.nextElement());
        }
    } */
    
		/**
	     Check whether a given abstract descriptor complies with this 
	     schema.
	     @param abs The abstract descriptor to be checked
	     @throws OntologyException If the abstract descriptor does not 
	     complies with this schema
	   */
  	public abstract void validate(AbsObject abs, Ontology onto) throws OntologyException;/* { 
  		validateSlots(abs, onto);
  	}*/
  	
  	/**
  	   For each slot
  	   - get the corresponding attribute value from the abstract descriptor 
  	     abs
  	   - Check that it is not null if the slot is mandatory
  	   - Check that its schema is compatible with the schema of the slot
  	   - Check that it is a correct abstract descriptor by validating it
  	     against its schema.
  	 */
  	/*protected void validateSlots(AbsObject abs, Ontology onto) throws OntologyException {
  		// Validate all the attributes in the abstract descriptor
  		String[] slotNames = getNames();
  		for (int i = 0; i < slotNames.length; ++i) {
  			AbsObject slotValue = abs.getAbsObject(slotNames[i]);
  			CaseInsensitiveString ciName = new CaseInsensitiveString(slotNames[i]);
  			validate(ciName, slotValue, onto);
  		}
  	}*/
  		
		/**
		   Validate a given abstract descriptor as a value for a slot
		   defined in this schema
		   @param slotName The name of the slot
	     @param value The abstract descriptor to be validated
	     @throws OntologyException If the abstract descriptor is not a 
	     valid value 
	     @return true if the slot is defined in this schema (or in 
	     one of its super schemas). false otherwise
	   */
  	/*private boolean validate(CaseInsensitiveString slotName, AbsObject value, Ontology onto) throws OntologyException {
			// DEBUG
  		//System.out.println("Validating "+value+" as a value for slot "+slotName); 
  		// NOTE: for performance reasons we don't want to scan the schema 
  		// to check if slotValue is a valid slot and THEN to scan again
  		// the schema to validate value. This is the reason for the 
  		// boolean return value of this method
  		boolean slotFound = false;
  		
  		// If the slot is defined in this schema --> check the value
  		// against the schema of the slot. Otherwise let the super-schema
  		// where the slot is defined validate the value
  		SlotDescriptor dsc = (SlotDescriptor) slots.get(slotName);
  		if (dsc != null) {
				// DEBUG
  			//System.out.println("Slot "+slotName+" is defined in schema "+this); 
  			if (value == null) {
  				// Check optionality
  				if (dsc.optionality == MANDATORY) {
  					throw new OntologyException("Missing value for mandatory slot "+slotName);
  				}
  				// Don't need to check facets on a null value for an optional slot
  				return true;
  			}
  			else {
  				// - Get from the ontology the schema s that defines the type 
  				// of the abstract descriptor value.
  				// - Check if this schema is compatible with the schema for 
  				// slot slotName
  				// - Finally check value against s
  				ObjectSchema s = onto.getSchema(value.getTypeName());
  				//DEBUG 
  				//System.out.println("Actual schema for "+value+" is "+s); 
  				if (s == null) {
  					throw new OntologyException("No schema found for type "+value.getTypeName());
  				}
  				if (!s.isCompatibleWith(dsc.schema)) {
  					throw new OntologyException("Schema "+s+" for element "+value+" is not compatible with schema "+dsc.schema+" for slot "+slotName); 
  				}
  				//DEBUG
  				//System.out.println("Schema "+s+" for type "+value+" is compatible with schema "+dsc.schema+" for slot "+slotName); 
  				s.validate(value, onto);
  			}
  			slotFound = true;
  		}
  		else {
  			Enumeration e = superSchemas.elements();
  			while (e.hasMoreElements()) {
  				ObjectSchema s = (ObjectSchema) e.nextElement();
  				if (s.validate(slotName, value, onto)) {
  					slotFound = true;
  					// Don't need to check other super-schemas
  					break;
  				}
  			}
  		}
  		
  		if (slotFound) {
  			// Check value against the facets (if any) defined for the  
  			// slot in this schema
  			Vector ff = (Vector) facets.get(slotName);
  			if (ff != null) {
  				Enumeration e = ff.elements();
  				while (e.hasMoreElements()) {
  					Facet f = (Facet) e.nextElement();
  					//DEBUG
  					//System.out.println("Checking facet "+f+" defined on slot "+slotName);
  					f.validate(value, onto);
  				}
  			}
  			else {
  				//DEBUG
  				//System.out.println("No facets for slot "+slotName);
  			}
  		}
  		
  		return slotFound;
  	}*/
  			
  	/**
  	   Check if this schema is compatible with a given schema s.
  	   This is the case if 
  	   1) This schema is equals to s
  	   2) s is one of the super-schemas of this schema
  	   3) This schema descends from s i.e.
  	      - s is the base schema for the XXXSchema class this schema is
  	        an instance of (e.g. s is ConceptSchema.getBaseSchema() and this 
  	        schema is an instance of ConceptSchema)
  	      - s is the base schema for a super-class of the XXXSchema class
  	        this schema is an instance of (e.g. s is TermSchema.getBaseSchema()
  	        and this schema is an instance of ConceptSchema)
  	 */
  	public abstract boolean isCompatibleWith(ObjectSchema s);/* {
  		if (equals(s)) {
  			return true;
  		}
  		if (isSubSchemaOf(s)) {
  			return true;
  		}
  		if (descendsFrom(s)) {
  			return true;
  		}
  		return false;
  	}*/
  	
  	/**
  	   Return true if 
  	   - s is the base schema for the XXXSchema class this schema is
  	     an instance of (e.g. s is ConceptSchema.getBaseSchema() and this 
  	     schema is an instance of ConceptSchema)
  	   - s is the base schema for a super-class of the XXXSchema class
  	     this schema is an instance of (e.g. s is TermSchema.getBaseSchema()
  	     and this schema is an instance of ConceptSchema)
  	 */
  	protected abstract boolean descendsFrom(ObjectSchema s);/* {
  		// The base schema for the ObjectSchema class descends only
  		// from itself
  		if (s!= null) {
  			return s.equals(getBaseSchema());
  		}
  		else {
  			return false;
  		}
  	}*/
  			
  	/**
  	   Return true if s is a super-schema (directly or indirectly)
  	   of this schema
  	 */
  	/*private boolean isSubSchemaOf(ObjectSchema s) {
  		Enumeration e = superSchemas.elements();
  		while (e.hasMoreElements()) {
  			ObjectSchema s1 = (ObjectSchema) e.nextElement();
  			if (s1.equals(s)) {
  				return true;
  			}
  			if (s1.isSubSchemaOf(s)) {
  				return true;
  			}
  		}
  		return false;
  	}*/
  	
    /*public String toString() {
    	return getClass().getName()+"-"+getTypeName();
    }
    
    public boolean equals(Object o) {
    	if (o != null) {
	    	return toString().equals(o.toString());
    	}
    	else {
    		return false;
    	}
    }*/
}

