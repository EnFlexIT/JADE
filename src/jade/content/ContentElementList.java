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
package jade.content;

import jade.content.onto.*;
import jade.content.abs.*;
import jade.util.leap.*;

/**
 * @author Federico Bergenti - Universita` di Parma
 */
public class ContentElementList extends ContentElement {
    private List elements = new ArrayList();

    /**
     * Constructor
     *
     */
    public ContentElementList() {}

    /**
     * Adds a new element to the list. 
     *
     * @param t the new element.
     *
     */
    public void addElement(ContentElement t) {
        elements.add(t);
    } 

    /**
     * Retrieves the <code>i</code>-th element of the list. 
     *
     * @param i index of the element
     *
     * @return the element to retrieve
     *
     */
    public ContentElement getElement(int i) {
        return (ContentElement) elements.get(i);
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
     * Retrieves the element in this list.
     *
     * @return an iterator to the list of elements.
     *
     */
    public Iterator getElements() {
        return elements.iterator();
    } 

    /**
     * Converts this list to an abstract descriptor using the ontology 
     * <code>onto</code>
     *
     * @param onto the ontology to use.
     *
     * @return the abstract descriptor.
     *
     * @throws OntologyException
     *
     */
    public AbsContentElementList fromObject(Ontology onto) 
            throws OntologyException {
        AbsContentElementList ret = new AbsContentElementList();

        for (int i = 0; i < elements.size(); i++) {
            ret.addElement((AbsContentElement) (onto.fromObject(elements.get(i))));
        }

        return ret;
    } 

}

