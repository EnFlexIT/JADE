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

import jade.content.*;
import jade.content.abs.*;
import jade.content.schema.*;
import jade.util.leap.List;
import jade.util.leap.Iterator;
import java.lang.reflect.*;

/**
 * @author Federico Bergenti - Universita` di Parma
 */
public class BasicIntrospector implements Introspector {

    /**
     * Externalize
     *
     * @param ontology
     * @param obj
     *
     * @return
     *
     * @throws OntologyException
     *
     */
    public AbsObject externalise(Ontology ontology, 
                                 Object obj) throws OntologyException {
	FullOntology onto = (FullOntology)ontology;

        try {
            if (obj == null) {
                return null;
            } 

            if (obj instanceof String) {
                return AbsPrimitive.wrap((String) obj);
            } 

            if (obj instanceof Boolean) {
                return AbsPrimitive.wrap(((Boolean) obj).booleanValue());
            } 

            if (obj instanceof Float) {
                return AbsPrimitive.wrap(((Float) obj).floatValue());
            } 

            if (obj instanceof Integer) {
                return AbsPrimitive.wrap(((Integer) obj).intValue());
            } 

            if (obj instanceof List) {
                return AbsHelper.fromObject((List) obj, onto);
            }

	    if(obj instanceof jade.core.AID) {
		return AbsHelper.fromObject((jade.core.AID)obj, onto);
	    }

            if (obj instanceof ContentElementList) {
                return AbsHelper.fromContentElementListObject((List) obj, onto);
            } 
	    
	    if (obj instanceof Iterator) {
		return AbsHelper.fromObject((Iterator) obj, onto);
	    }
	    
	    return null;
        } 
        catch (OntologyException oe) {
            throw oe;
        } 
        catch (Throwable t) {
            throw new OntologyException("Schema and Java class do not match");
        } 
    } 

    /**
     * Internalize
     *
     * @param ontology
     * @param abs
     *
     * @return
     *
     * @throws OntologyException
     * @throws UngroundedException
     *
     */
    public Object internalise(Ontology ontology, AbsObject abs) 
            throws UngroundedException, OntologyException {
	FullOntology onto = (FullOntology)ontology;

        try {
            if (abs == null) {
                return null;
            } 

            if (abs instanceof AbsPrimitive) {
                return AbsPrimitive.toObject((AbsPrimitive) abs);
            } 

            if (abs instanceof AbsAggregate) {
                return AbsHelper.toListObject((AbsAggregate) abs, onto);
            } 

	    if (abs instanceof AbsAID) {
		return AbsHelper.toAIDObject((AbsAID) abs, onto);
	    }

            if (abs instanceof AbsContentElementList) {
                return AbsHelper.toListObject((AbsContentElementList) abs, onto);
            } 

            return null;
        } 
        catch (OntologyException oe) {
            throw oe;
        } 
        catch (Throwable t) {
            throw new OntologyException("Schema and Java class do not match");
        } 
    } 
}
