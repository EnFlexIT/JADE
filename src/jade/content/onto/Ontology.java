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
 * @see jade.content.abs.Concept
 * @see jade.content.onto.ACLOntology
 * @see jade.content.onto.BasicOntology
 * @see jade.content.schema.ConceptSchema
 * @author Federico Bergenti - Universita` di Parma
 */
public abstract class Ontology {
    protected Ontology     base = null;
    protected String       name = null;
    protected Introspector introspector = null;

    /**
     * Construct an ontology with a given <code>name</code>
     *
     * @param name identifier of the ontology.
     *
     */
    public Ontology(String name) {
        this.name = name;
    }

    /**
     * Construct an ontology with a given <code>name</code> that extends 
     * <code>base</code>.
     *
     * @param name identifier of the ontology.
     * @param base base ontology.
     *
     */
    public Ontology(String name, Ontology base) {
        this.name = name;
        this.base = base;
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
    public Ontology(String name, Ontology base, Introspector introspector) {
        this.name = name;
        this.base = base;
        this.introspector = introspector;
    }

    /**
     * Retrieves the name of the ontology.
     *
     * @return the name of the ontology.
     *
     */
    public String getName() {
        return name;
    } 
}
