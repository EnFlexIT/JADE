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
import jade.core.CaseInsensitiveString;

import java.io.PrintStream;
import java.io.ByteArrayOutputStream;

/**
 * @author Federico Bergenti - Universita` di Parma
 */
public class AbsHelper {
    /**
     * Converts a <code>List</code> into a <code>AbsAggregate</code> using
     * the specified ontology.
     * @param obj the <code>List</code>
     * @param onto the ontology.
     * @return the abstract descriptor.
     * @throws OntologyException
     */
    public static AbsAggregate externaliseList(List obj, Ontology onto) throws OntologyException {
        AbsAggregate ret = new AbsAggregate(BasicOntology.SEQUENCE);

        try {
        	for (int i = 0; i < obj.size(); i++) {
            ret.add((AbsTerm) (onto.fromObject(obj.get(i))));
        	}
        }
        catch (ClassCastException cce) {
        	throw new OntologyException("Non term object in aggregate");
        }

        return ret;
    } 

    /**
     * Converts an <code>Iterator</code> into a <code>AbsAggregate</code> using
     * the specified ontology.
     * @param obj the <code>Iterator</code>
     * @param onto the ontology.
     * @return the abstract descriptor.
     * @throws OntologyException
     */
    public static AbsAggregate externaliseIterator(Iterator obj, Ontology onto) throws OntologyException {
        AbsAggregate ret = new AbsAggregate(BasicOntology.SEQUENCE);

        try {
        	while(obj.hasNext())
            ret.add((AbsTerm) (onto.fromObject(obj.next())));
        }
        catch (ClassCastException cce) {
        	throw new OntologyException("Non term object in aggregate");
        }
        return ret;
    }

    /**
     * Converts an <code>AID</code> into a <code>AbsAID</code> using
     * the specified ontology.
     * @param obj the <code>AID</code>
     * @param onto the ontology.
     * @return the abstract descriptor.
     * @throws OntologyException
     */
    public static AbsAID externaliseAID(AID obj) {
      // Name
      String name = obj.getName();
      
      // Addresses
			AbsAggregate addresses = new AbsAggregate(BasicOntology.SET);
			for(Iterator i = obj.getAllAddresses(); i.hasNext(); ) {
				String addr = (String) i.next();
	    	addresses.add(AbsPrimitive.wrap(addr));
			}

			// Resolvers
			AbsAggregate resolvers = new AbsAggregate(BasicOntology.SET);
			for(Iterator i = obj.getAllResolvers(); i.hasNext(); ) {
				AID res = (AID) i.next();
	    	resolvers.add(externaliseAID(res));
			}
			
      return new AbsAID(name, addresses, resolvers);
    } 

    /**
     * Converts a <code>ContentElementList</code> into an
     * <code>AbsContentElementList</code> using
     * the specified ontology.
     * @param obj the <code>ContentElementList</code>
     * @param onto the ontology.
     * @return the abstract descriptor.
     * @throws OntologyException
     */
    public static AbsContentElementList externaliseContentElementList(ContentElementList obj, Ontology onto) throws OntologyException {
        AbsContentElementList ret = new AbsContentElementList();

        try {
        	for (int i = 0; i < obj.size(); i++) {
            ret.add((AbsContentElement) (onto.fromObject(obj.get(i))));
        	}
        }
        catch (ClassCastException cce) {
        	throw new OntologyException("Non content element object in content element list");
        }

        return ret;
    } 

            
    /**
     * Converts to an <code>AbsAggregate</code> into a List using the 
     * specified ontology.
     * @param onto the ontology
     * @return the List
     * @throws OntologyException
     */
    public static List internaliseList(AbsAggregate aggregate, Ontology onto) throws OntologyException {
        List ret = new ArrayList();

        for (int i = 0; i < aggregate.size(); i++) {
        	Object element = onto.toObject(aggregate.get(i));
        	// Check if the element is a Term, a primitive an AID or a List
        	Ontology.checkIsTerm(element);
          ret.add(element);
        }

        return ret;
    } 

    /**
     * Converts to an <code>AbsAID</code> into an OntoAID using the 
     * specified ontology.
     * @param onto the ontology
     * @return the OntoAID
     * @throws OntologyException
     */
    public static OntoAID internaliseAID(AbsAID aid) {
        OntoAID ret = new OntoAID();

        // Name
				String name = ((AbsPrimitive)(aid.getAbsObject(BasicOntology.AID_NAME))).getString();   
				ret.setName(name);

        // Addresses
				AbsAggregate addresses = (AbsAggregate) aid.getAbsObject(BasicOntology.AID_ADDRESSES);
				for (int i = 0; i < addresses.size(); ++i) {
					String addr = ((AbsPrimitive) addresses.get(i)).getString();
	    		ret.addAddresses(addr);
				}
				
        // Resolvers
				AbsAggregate resolvers = (AbsAggregate) aid.getAbsObject(BasicOntology.AID_RESOLVERS);
				for (int i = 0; i < resolvers.size(); ++i) {
					OntoAID res = internaliseAID((AbsAID) resolvers.get(i));
	    		ret.addResolvers(res);
				}

        return ret;
    } 

    /**
     * Converts to an <code>AbsContentElementList</code> into a 
     * ContentElementList using the 
     * specified ontology.
     * @param onto the ontology
     * @return the ContentElementList
     * @throws OntologyException
     */
    public static ContentElementList internaliseContentElementList(AbsContentElementList l, Ontology onto) throws OntologyException {
        ContentElementList ret = new ContentElementList();

        try {
        	for (int i = 0; i < l.size(); i++) {
        		ContentElement element = (ContentElement) onto.toObject(l.get(i));
          	ret.add(element);
        	}
        }
        catch (ClassCastException cce) {
        	throw new OntologyException("Non content element object in content element list");
        }

        return ret;
    } 

    public static String toString(AbsObject abs) {
			ByteArrayOutputStream str = new ByteArrayOutputStream();
			((AbsObjectImpl) abs).dump(0, new PrintStream(str));
			return new String(str.toByteArray());
    }
}

