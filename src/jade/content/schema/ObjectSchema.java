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

/**
 * @author Federico Bergenti - Universita` di Parma
 */
public abstract class ObjectSchema {
    private class SlotDescriptor {
        private String       name = null;
        private ObjectSchema schema = null;
        private int          cardinality = 0;

        /**
           Construct a SlotDescriptor
         */
        private SlotDescriptor(String name, ObjectSchema schema, 
                                    int cardinality) {
            this.name = name;
            this.schema = schema;
            this.cardinality = cardinality;
        }

    }

    public static final int MANDATORY = 0;
    public static final int OPTIONAL = 1;
    private Hashtable       slots = new Hashtable();
    private Vector          superSchemas = new Vector();
    private String          typeName = null;

    /**
     * Construct an empty schema with only the type-name specified
     * @param typeName 
     */
    protected ObjectSchema(String typeName) {
        this.typeName = typeName;
    }

    /**
     * Add a slot to the schema.
     *
     * @param name The name of the slot.
     * @param slotSchema The schema defining the type of the slot.
     * @param cardinality The cardinality, i.e., <code>OPTIONAL</code> 
     * or <code>MANDATORY</code>
     */
    protected void add(String name, ObjectSchema slotSchema, int cardinality) {
        slots.put(name.toUpperCase(), new SlotDescriptor(name, slotSchema, cardinality));
    } 

  	/**
     * Add a mandatory attribute to the schema.
     *
     * @param name name of the attribute.
     * @param slotSchema schema of the attribute.
     */
    protected void add(String name, ObjectSchema slotSchema) {
        add(name, slotSchema, MANDATORY);
    } 

    /**
     * Add a super schema tho this schema, i.e. this schema will
     * inherit all characteristics from the super schema
     *
     * @param superSchema the super schema.
     */
    protected void addSuperSchema(ObjectSchema superSchema) {
        superSchemas.addElement(superSchema);
    } 

    /**
     * Retrieves the name of the type of this schema.
     *
     * @return the name of the type of this schema.
     */
    public String getTypeName() {
        return typeName;
    } 

    /**
     * Returns the names of all the slots in this <code>Schema</code> 
     * (including slots defined in super schemas).
     *
     * @return the names of all slots.
     */
    public String[] getNames() {
        Vector allSlotDescriptors = new Vector();

        getAllSlotDescriptors(allSlotDescriptors);

        String[] names = new String[allSlotDescriptors.size()];
        int      counter = 0;
        for (Enumeration e = allSlotDescriptors.elements(); e.hasMoreElements(); ) {
            names[counter++] = (String) e.nextElement();
        }

        return names;
    } 

    /**
     * Retrieves the schema of a slot of this <code>Schema</code>.
     *
     * @param name The name of the slot.
     * @return the <code>Schema</code> of slot <code>name</code>
     * @throws OntologyException If no slot with this name is present
     * in this schema.
     */
    public ObjectSchema getSchema(String name) throws OntologyException {
        name = name.toUpperCase();
        SlotDescriptor slot = (SlotDescriptor) slots.get(name);

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
    } 

    /**
     * Indicate whether a slot is mandatory or not.
     *
     * @param name The name of the slot.
     * @return <code>true</code> if the slot is mandatory.
     * @throws OntologyException If no slot with this name is present
     * in this schema.
     */
    public boolean isMandatory(String name) throws OntologyException {
        name = name.toUpperCase();
        SlotDescriptor slot = (SlotDescriptor) slots.get(name);

        if (slot == null) {
            for (Enumeration e = superSchemas.elements(); e.hasMoreElements(); ) {
                try {
                    ObjectSchema superSchema = (ObjectSchema) e.nextElement();
                    return superSchema.isMandatory(name);
                } 
                catch (OntologyException oe) {
                	// Do nothing. Maybe the slot is defined in another super-schema
                }
            } 
            throw new OntologyException("No slot named: " + name);
        } 

        return (slot.cardinality == MANDATORY);
    } 

    /**
     * Indicate whether a given <code>String</code> is the name of a
     * slot defined in this <code>Schema</code>
     *
     * @param name The <code>String</code> to test.
     * @return <code>true</code> if <code>name</code> is the name of a
     * slot defined in this <code>Schema</code>.
     */
    public boolean isSlot(String name) {
        name = name.toUpperCase();
        SlotDescriptor slot = (SlotDescriptor) slots.get(name);

        if (slot != null) {
            return true;
        } 

	      for (Enumeration e = superSchemas.elements(); e.hasMoreElements(); ) {
  	        ObjectSchema superSchema = (ObjectSchema) e.nextElement();
            if (superSchema.isSlot(name)) {
               	return true;
            } 
        } 

        return false;
    } 

    /**
     * Creates an Abstract descriptor to hold an object compliant to 
     * this <code>Schema</code>.
     */
    public abstract AbsObject newInstance() throws OntologyException;

    private void getAllSlotDescriptors(Vector v) {
    		// Get slot descriptors of super schemas
        for (Enumeration e = superSchemas.elements(); e.hasMoreElements(); ) {
            ObjectSchema superSchema = (ObjectSchema) e.nextElement();

            superSchema.getAllSlotDescriptors(v);
        } 

        // Get slot descriptors directly defined in this schema
        for (Enumeration e = slots.keys(); e.hasMoreElements(); ) {
            v.addElement(e.nextElement());
        }
    } 

    public String toString() {
    	return getClass().getName()+"-"+getTypeName();
    }
}

