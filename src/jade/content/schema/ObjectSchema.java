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
    private class AttributeDescriptor {
        private String       name = null;
        private ObjectSchema schema = null;
        private int          cardinality = 0;

        /**
         * Constructor declaration
         *
         * @param name
         * @param schema
         * @param cardinality
         *
         */
        private AttributeDescriptor(String name, ObjectSchema schema, 
                                    int cardinality) {
            this.name = name;
            this.schema = schema;
            this.cardinality = cardinality;
        }

    }

    public static final int MANDATORY = 0;
    public static final int OPTIONAL = 1;
    private Hashtable       attributeDescriptors = new Hashtable();
    private Vector          baseSchemas = new Vector();
    private String          typeName = null;

    /**
     * Constructor
     *
     * @param typeName
     *
     */
    protected ObjectSchema(String typeName) {
        this.typeName = typeName;
    }

    /**
     * Add an attribute to the schema.
     *
     * @param name name of the attribute.
     * @param elementSchema schema of the attribute.
     * @param cardinality cardinality, i.e., optional or mandatory
     *
     */
    protected void addAttribute(String name, ObjectSchema elementSchema, 
                                int cardinality) {
        attributeDescriptors.put(name.toUpperCase(), 
                                 new AttributeDescriptor(name, elementSchema, 
                                 cardinality));
    } 

  /**
     * Add an attribute to the schema.
     *
     * @param name name of the attribute.
     * @param elementSchema schema of the attribute.
     *
     */
    protected void addAttribute(String name, ObjectSchema elementSchema) {
        addAttribute(name, elementSchema, MANDATORY);
    } 

    /**
     * Adds a base schema.
     *
     * @param base the base schema.
     *
     */
    protected void addBaseSchema(ObjectSchema base) {
        baseSchemas.add(base);
    } 

    private void getAttributeDescriptorNames(Vector v) {
        for (Enumeration e = baseSchemas.elements(); e.hasMoreElements(); ) {
            ObjectSchema base = (ObjectSchema) e.nextElement();

            base.getAttributeDescriptorNames(v);
        } 

        for (Enumeration e = attributeDescriptors.keys(); 
                e.hasMoreElements(); ) {
            v.add(e.nextElement());
        }
    } 

    /**
     * Returns the names of all attributes.
     *
     * @return the names of all attributes.
     *
     */
    public String[] getAttributeNames() {
        Vector allAttributeDescriptors = new Vector();

        getAttributeDescriptorNames(allAttributeDescriptors);

        String[] ret = new String[allAttributeDescriptors.size()];
        int      counter = 0;

        for (Enumeration e = allAttributeDescriptors.elements(); 
                e.hasMoreElements(); ) {
            ret[counter++] = (String) e.nextElement();
        }

        return ret;
    } 

    /**
     * Is the attribute <code>name</code> mandatory?
     *
     * @param name name of the attribute.
     *
     * @return <code>true</code> if the attribute is mandatory.
     *
     * @throws OntologyException
     *
     */
    public boolean isMandatory(String name) throws OntologyException {
        name = name.toUpperCase();

        AttributeDescriptor ad = 
            (AttributeDescriptor) attributeDescriptors.get(name);

        if (ad == null) {
            for (Enumeration e = baseSchemas.elements(); 
                    e.hasMoreElements(); ) {
                try {
                    ObjectSchema base = (ObjectSchema) e.nextElement();

                    return base.isMandatory(name);
                } 
                catch (OntologyException oe) {}
            } 

            throw new OntologyException("No element named: " + name);
        } 

        return (ad.cardinality == MANDATORY);
    } 

    /**
     * Is <code>name</code> an attribute?
     *
     * @param name name to test.
     *
     * @return <code>true</code> if <code>name</code> is an attribute.
     *
     * @throws OntologyException
     *
     */
    public boolean isAttribute(String name) throws OntologyException {
        name = name.toUpperCase();

        AttributeDescriptor ad = 
            (AttributeDescriptor) attributeDescriptors.get(name);

        if (ad != null) {
            return true;
        } 

        for (Enumeration e = baseSchemas.elements(); e.hasMoreElements(); ) {
            ObjectSchema base = (ObjectSchema) e.nextElement();

            if (base.isAttribute(name)) {
                return true;
            } 
        } 

        return false;
    } 

    /**
     * Retrieves an attribute's schema.
     *
     * @param name name of the attribute.
     *
     * @return the schema of attribute <code>name</code>
     *
     * @throws OntologyException
     *
     */
    public ObjectSchema getAttributeSchema(String name) 
            throws OntologyException {
        name = name.toUpperCase();

        AttributeDescriptor ad = 
            (AttributeDescriptor) attributeDescriptors.get(name);

        if (ad == null) {
            for (Enumeration e = baseSchemas.elements(); 
                    e.hasMoreElements(); ) {
                try {
                    ObjectSchema base = (ObjectSchema) e.nextElement();

                    return base.getAttributeSchema(name);
                } 
                catch (OntologyException oe) {}
            } 

            throw new OntologyException("No element named: " + name);
        } 

        return ad.schema;
    } 

    /**
     * Retrieves the name of the type of this schema.
     *
     * @return the name of the type.
     *
     */
    public String getTypeName() {
        return typeName;
    } 

    /**
     * Creates a new instance.
     *
     * @return the new instance.
     *
     */
    public abstract AbsObject newInstance();
}

