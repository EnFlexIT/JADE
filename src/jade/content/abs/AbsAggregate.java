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
    public void add(AbsTerm element) {
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
    public Iterator getAllElement() {
        return elements.iterator();
    } 

    /**
     * Returns an iterator to all elements.
     *
     * @return the elements.
     *
     */
    public Iterator getAllElements () {
	return elements.iterator();
    }

   /**
     * Clear the aggregate.
     *
     */
    public void clearAllElement () {
	elements.clear();
    }

   /**
     * Test if the aggregate contains an element.
     *
     * @return the result of the test.
     *
     */
    public boolean containsElement (AbsTerm element) {
	return elements.contains(element);
    }

   /**
     * Returns the position of an element.
     *
     * @return the position.
     *
     */
    public int indexOfElement (AbsTerm element) {
	return elements.indexOf(element);
    }

   /**
     * Removes an element.
     *
     * @return the removed element.
     *
     */
    public AbsTerm removeElement (int index) {
	return (AbsTerm)elements.remove(index);
    }

   /**
     * Removes an element.
     *
     * @return if the element has been removed.
     *
     */
    public boolean removeElement (AbsTerm element) {
	return elements.remove(element);
    }

   /**
     * Test if the aggregate is empty.
     *
     * @return the result of the test.
     *
     */
    public boolean isEmpty () {
	return elements.isEmpty();
    }

   /**
     * Converts the aggregate to an array.
     *
     * @return the converted array.
     *
     */
    public AbsTerm[] toArray () {
	int size = elements.size();
       	AbsTerm[] tmp = new AbsTerm[size];
       	for (int i = 0; i < size; i++)
       		tmp[i] = (AbsTerm)elements.get(i);
       	return tmp;
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
}

