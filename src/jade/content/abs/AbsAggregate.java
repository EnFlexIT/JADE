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
package jade.content.abs;

import jade.content.onto.*;
import jade.content.*;
import jade.content.schema.*;
import jade.util.leap.List;
import jade.util.leap.ArrayList;
import jade.util.leap.Iterator;

/**
 * @author Federico Bergenti - Universita` di Parma
 */
public class AbsAggregate extends AbsTerm {
    private List elements = new ArrayList();

    /**
     * Constructor
     *
     * @param typeName name of the type of the elements.
     *
     */
    public AbsAggregate(String typeName) {
        super(typeName);
    }

    /**
     * Adds a new element.
     *
     * @param element the element to add.
     *
     */
    public void addElement(AbsTerm element) {
        elements.add(element);
    } 

    /**
     * Retrieves the number of elements.
     *
     * @return the number of elements.
     *
     */
    public int getElementCount() {
        return elements.size();
    } 

    /**
     * Retrieves the <code>i</code>-th element.
     *
     * @param i index of the element to retrieve.
     *
     * @return the element.
     *
     */
    public AbsTerm getElement(int i) {
        return (AbsTerm) elements.get(i);
    } 

    /**
     * Retrieves all elements.
     *
     * @return the elements.
     *
     */
    public List getElements() {
        return elements;
    } 

    /**
     * Converts to a <code>List</code> using the specified ontology.
     *
     * @param onto the ontology
     *
     * @return the list
     *
     * @throws OntologyException
     *
     */
    public List toObject(Ontology onto) throws OntologyException {
        List ret = new ArrayList();

        for (int i = 0; i < elements.size(); i++) {
            ret.add((Term) (onto.toObject((AbsObject) elements.get(i))));
        }

        return ret;
    } 

    protected void dump(int indent) {
        for (int i = 0; i < indent; i++) {
            System.out.print("  ");
        }

        System.out.println("(");

        for (int i = 0; i < elements.size(); i++) {
            ((AbsObject) elements.get(i)).dump(indent + 1);
        }

        for (int i = 0; i < indent; i++) {
            System.out.print("  ");
        }

        System.out.println(")");
    } 

    /**
     * Converts a <code>List</code> into a <code>AbsAggregate</code> using
     * the specified ontology.
     *
     * @param obj the <code>List</code>
     * @param onto the ontology.
     *
     * @return the abstract descriptor.
     *
     * @throws OntologyException
     *
     */
    public static AbsAggregate fromObject(List obj, Ontology onto) 
            throws OntologyException {
        AbsAggregate ret = new AbsAggregate(BasicOntology.SEQUENCE);

        for (int i = 0; i < obj.size(); i++) {
            ret.addElement((AbsTerm) (onto.fromObject(obj.get(i))));
        }

        return ret;
    } 

}

