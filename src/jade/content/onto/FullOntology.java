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
import jade.content.abs.*;
import jade.content.schema.*;

/*
 * @author Federico Bergenti - Universita` di Parma
 */
public class FullOntology extends Ontology {
    protected Introspector introspector = null;
    
    private Hashtable    elements = new Hashtable();
    private Hashtable    classes  = new Hashtable();
    private Hashtable    schemas  = new Hashtable();
    
    /**
     * Construct an ontology with a given <code>name</code>
     *
     * @param name identifier of the ontology.
     *
     */
    public FullOntology(String name) {
        super(name);
    }

    /**
     * Construct an ontology with a given <code>name</code> that extends 
     * <code>base</code>.
     *
     * @param name identifier of the ontology.
     * @param base base ontology.
     *
     */
    public FullOntology(String name, Ontology base) {
        super(name, base);
        introspector = new ReflectiveIntrospector();
    }

    /**
     * Construct an ontology with a given <code>name</code> that extends 
     * <code>base</code>. The object will use <code>introspector</code>
     * for serialization and de-serialization.
     *
     * @param name identifier of the ontology.
     * @param base base ontology.
     * @param introspector the introspector.
     *
     */
    public FullOntology(String name, Ontology base, Introspector introspector) {
	super(name, base);
	this.introspector = introspector;
    }

    /**
     * Adds a schema to the ontology
     *
     * @param schema the schema to add
     *
     * @throws OntologyException
     *
     */
    public void add(ObjectSchema schema) throws OntologyException {
        add(schema, null);
    } 

    /**
     * Adds a schema to the ontology and associates it to the class
     * <code>javaClass</code>
     *
     * @param schema the schema.
     * @param javaClass the concrete class.
     *
     * @throws OntologyException
     *
     */
    public void add(ObjectSchema schema, 
                    Class javaClass) throws OntologyException {
        if (schema.getTypeName() == null) {
            throw new OntologyException("Invalid schema identifier");
        } 

        elements.put(schema.getTypeName(), schema);

        if (javaClass != null) {
            classes.put(schema.getTypeName(), javaClass);
            schemas.put(javaClass, schema);
        } 
    } 

    /**
     * Retrieves the schema associated with <code>name</code>.
     *
     * @param name the name of the schema in the vocabulary.
     *
     * @return the schema.
     *
     * @throws OntologyException
     *
     */
    public ObjectSchema getSchema(String name) 
            throws OntologyException {
        if (name == null) {
            throw new OntologyException("Null schema identifier");
        } 

        ObjectSchema ret = (ObjectSchema) elements.get(name);

        if (ret == null) {
            if (base != null) {
                return base.getSchema(name);
            } 

            throw new OntologyException("Invalid schema identifier");
        } 

        return ret;
    } 

    /**
     * Retrieves the schema associated with <code>javaClass</code>
     *
     * @param javaClass the Java class
     *
     * @return the schema
     *
     * @throws OntologyException
     *
     */
    public ObjectSchema getSchema(Class javaClass) 
            throws OntologyException {
        if (javaClass == null) {
            throw new OntologyException("Null schema identifier");
        } 

        ObjectSchema ret = (ObjectSchema) schemas.get(javaClass);

        if (ret == null) {
            if (base != null) {
                return base.getSchema(javaClass);
            } 

            return null;
        } 

        return ret;
    } 

    /**
     * Retrieves the concrete class associated with <code>name</code> in
     * the vocabulary.
     *
     * @param name the name of the schema.
     *
     * @return the Java class.
     *
     * @throws OntologyException
     *
     */
    public Class getClass(String name) throws OntologyException {
        if (name == null) {
            throw new OntologyException("Null schema identifier");
        } 

        Class ret = (Class) classes.get(name);

        if (ret == null) {
            if (base != null) {
                return base.getClass(name);
            } 

            return null;
        } 

        return ret;
    } 
   /**
     * Converts an abstract descriptor to an object.
     * @param abs the abstract descriptor.
     * @return the object
     * @throws OntologyException
     * @throws UngroundedException
     * @see fromObject(Object)
     */
    public Object toObject(AbsObject abs)
            throws OntologyException, UngroundedException {
        return introspector.internalise(this, abs);
    } 

    /**
     * Converts an object to an abstract descriptor.
     * @param obj the object
     * @return the abstract descriptor.
     * @throws OntologyException
     * @see toObject(AbsObject)
     */
    public AbsObject fromObject(Object obj) throws OntologyException {
        return introspector.externalise(this, obj);
    } 

}
