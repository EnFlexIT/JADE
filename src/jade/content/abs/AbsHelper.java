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

import jade.core.AID;

/**
 * @author Federico Bergenti - Universita` di Parma
 */
public class AbsHelper {
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
    public static List toListObject(AbsAggregate aggregate, FullOntology onto) throws OntologyException {
        List ret = new ArrayList();

        for (int i = 0; i < aggregate.getElementCount(); i++) {
            ret.add((Term) (onto.toObject((AbsObject) aggregate.getElement(i))));
        }

        return ret;
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
    public static AbsAggregate fromObject(List obj, FullOntology onto) 
            throws OntologyException {
        AbsAggregate ret = new AbsAggregate(BasicOntology.SEQUENCE);

        for (int i = 0; i < obj.size(); i++) {
            ret.add((AbsTerm) (onto.fromObject(obj.get(i))));
        }

        return ret;
    } 

    /**
     * Converts an <code>Iterator</code> into a <code>AbsAggregate</code> using
     * the specified ontology.
     *
     * @param obj the <code>Iterator</code>
     * @param onto the ontology.
     *
     * @return the abstract descriptor.
     *
     * @throws OntologyException
     *
     */
    public static AbsAggregate fromObject(Iterator obj, FullOntology onto)
            throws OntologyException {
        AbsAggregate ret = new AbsAggregate(BasicOntology.SEQUENCE);

        while(obj.hasNext())
            ret.add((AbsTerm) (onto.fromObject(obj.next())));

        return ret;
    }

    /**
     * Converts to an <code>AID</code> using the specified ontology.
     *
     * @param onto the ontology
     *
     * @return the AID
     *
     * @throws OntologyException
     *
     */
    public static AID toAIDObject(AbsAID aid, FullOntology onto) throws OntologyException {
        AID ret = new AID();

	String name = (String)(onto.toObject(aid.getAbsObject(BasicOntology.NAME)));

	List addresses = (List)(onto.toObject(aid.getAbsObject(BasicOntology.ADDRESSES)));
	for(Iterator i = addresses.iterator(); i.hasNext();)
	    ret.addAddresses((String)i.next());

	List resolvers = (List)(onto.toObject(aid.getAbsObject(BasicOntology.RESOLVERS)));
	for(Iterator i = resolvers.iterator(); i.hasNext();)
	    ret.addResolvers((AID)i.next());

        ret.setName(name);

        return ret;
    } 

    /**
     * Converts an <code>AID</code> into a <code>AbsAggregate</code> using
     * the specified ontology.
     *
     * @param obj the <code>AID</code>
     * @param onto the ontology.
     *
     * @return the abstract descriptor.
     *
     * @throws OntologyException
     *
     */
    public static AbsAID fromObject(AID obj, FullOntology onto) 
            throws OntologyException {
	AbsAggregate addresses = new AbsAggregate(BasicOntology.SET);

	for(Iterator i = obj.getAllAddresses(); i.hasNext(); )
	    addresses.add((AbsTerm)i.next());

	AbsAggregate resolvers = new AbsAggregate(BasicOntology.SET);
	for(Iterator i = obj.getAllResolvers(); i.hasNext(); )
	    resolvers.add((AbsTerm)i.next());

        return new AbsAID(obj.getName(), addresses, resolvers);
    } 

    /**
     * Converts to a <code>List</code> using a specified ontology.
     *
     * @param onto the ontology
     *
     * @return the <code>List</code>
     *
     * @throws OntologyException
     *
     */
    public static List toListObject(AbsContentElementList abs, FullOntology onto) throws OntologyException {
        List ret = new ArrayList();

        for (Iterator i = abs.getAll(); i.hasNext(); ) {
            ret.add(onto.toObject((AbsObject) i.next()));
        }

        return ret;
    } 

    /**
     * Converts to an abstract descriptor using the specified ontology.
     *
     * @param obj the <code>List</code> to convert.
     * @param onto the ontology to use for the conversion.
     *
     * @return the abstract descriptor.
     *
     * @throws OntologyException
     *
     */
    public static AbsContentElementList fromContentElementListObject(List obj, 
            FullOntology onto) throws OntologyException {
        AbsContentElementList ret = new AbsContentElementList();

        for (Iterator i = obj.iterator(); i.hasNext(); ) {
            ret.add((AbsContentElement) (onto.fromObject(i.next())));
        }

        return ret;
    } 

}

