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
import jade.content.onto.basic.*;
import jade.core.CaseInsensitiveString;

/**
 * @author Federico Bergenti - Universita` di Parma
 */
class BasicIntrospector implements Introspector {

    /**
     * Translate an object of a class representing an element in an
     * ontology into a proper abstract descriptor 
     * @param onto The reference ontology 
     * @param obj The Object to be translated
     * @return The Abstract descriptor produced by the translation 
		 * @throws UnknownSchemaException If no schema for the object to be
		 * translated is defined in the ontology that uses this Introspector
		 * @throws OntologyException If some error occurs during the translation
     */
    public AbsObject externalise(Ontology onto, Ontology referenceOnto, Object obj) 
    			throws UnknownSchemaException, OntologyException {
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
            if (obj instanceof Integer) {
                return AbsPrimitive.wrap(((Integer) obj).intValue());
            } 
            //__CLDC_UNSUPPORTED__BEGIN
            if (obj instanceof Float) {
                return AbsPrimitive.wrap(((Float) obj).floatValue());
            } 
            //__CLDC_UNSUPPORTED__END


            if (obj instanceof List) {
              return AbsHelper.externaliseList((List) obj, referenceOnto);
            }

	    			if (obj instanceof Iterator) {
							return AbsHelper.externaliseIterator((Iterator) obj, referenceOnto);
	    			}
	    
	    			if(obj instanceof jade.core.AID) {
							return AbsHelper.externaliseAID((jade.core.AID)obj);
	    			}

            if (obj instanceof ContentElementList) {
            	return AbsHelper.externaliseContentElementList((ContentElementList) obj, referenceOnto);
            } 
	    
	    			if(obj instanceof TrueProposition) {
	    				AbsPredicate absTrueProp = new AbsPredicate(BasicOntology.TRUE_PROPOSITION);
							return absTrueProp;
	    			}

	    			if(obj instanceof Done) {
	    				AbsActionPredicate absDone = new AbsActionPredicate(BasicOntology.DONE);
  						absDone.set(BasicOntology.DONE_ACTION, (AbsGenericAction) onto.fromObject(((Done) obj).getAction()));
							return absDone;
	    			}

	    			if(obj instanceof Result) {
	    				AbsActionPredicate absResult = new AbsActionPredicate(BasicOntology.RESULT);
  						absResult.set(BasicOntology.RESULT_ACTION, (AbsGenericAction) onto.fromObject(((Result) obj).getAction()));
  						absResult.set(BasicOntology.RESULT_ITEMS, (AbsTerm) onto.fromObject(((Result) obj).getItems()));
							return absResult;
	    			}

	    			if(obj instanceof Equals) {
	    				AbsPredicate absEquals = new AbsPredicate(BasicOntology.EQUALS);
  						absEquals.set(BasicOntology.EQUALS_LEFT, (AbsTerm) onto.fromObject(((Equals) obj).getLeft()));
  						absEquals.set(BasicOntology.EQUALS_RIGHT, (AbsTerm) onto.fromObject(((Equals) obj).getRight()));
							return absEquals;
	    			}

            throw new UnknownSchemaException();
        } 
        catch (OntologyException oe) {
        		// Forward the exception
            throw oe;
        } 
        catch (Throwable t) {
            throw new OntologyException("Schema and Java class do not match");
        } 
    } 

    /**
     * Translate an abstract descriptor into an object of a proper class 
     * representing an element in an ontology 
     * @param onto The reference ontology 
     * @param abs The abstract descriptor to be translated
     *
     * @return The Java object produced by the translation 
     * @throws UngroundedException If the abstract descriptor to be translated 
     * contains a variable
		 * @throws UnknownSchemaException If no schema for the abstract descriptor
		 * to be translated is defined in the ontology that uses this Introspector
     * @throws OntologyException If some error occurs during the translation
     */
    public Object internalise(Ontology onto, Ontology referenceOnto, AbsObject abs) 
    			throws UngroundedException, UnknownSchemaException, OntologyException {

        try {
            if (abs == null) {
                return null;
            } 

            if (abs instanceof AbsPrimitive) {
                return ((AbsPrimitive) abs).getObject();
            } 
            if (abs instanceof AbsAggregate) {
                return AbsHelper.internaliseList((AbsAggregate) abs, referenceOnto);
            } 
	    			if (abs instanceof AbsAID) {
							return AbsHelper.internaliseAID((AbsAID) abs);
	    			}

            if (abs instanceof AbsContentElementList) {
            	return AbsHelper.internaliseContentElementList((AbsContentElementList) abs, referenceOnto);
            } 

	    			if (CaseInsensitiveString.equalsIgnoreCase(abs.getTypeName(), BasicOntology.TRUE_PROPOSITION)) { 
							TrueProposition t = new TrueProposition();
							return t;
	    			}
	    			
	    			if (CaseInsensitiveString.equalsIgnoreCase(abs.getTypeName(), BasicOntology.DONE)) { 
							Done d = new Done();
  						AbsActionPredicate absDone = (AbsActionPredicate) abs;
  						d.setAction((GenericAction) onto.toObject(absDone.getAbsTerm(BasicOntology.DONE_ACTION))); 
							return d;
	    			}
	    			
	    			if (CaseInsensitiveString.equalsIgnoreCase(abs.getTypeName(), BasicOntology.RESULT)) { 
							Result r = new Result();
  						AbsActionPredicate absResult = (AbsActionPredicate) abs;
  						r.setAction((GenericAction) onto.toObject(absResult.getAbsTerm(BasicOntology.RESULT_ACTION))); 
  						r.setItems((List) onto.toObject(absResult.getAbsTerm(BasicOntology.RESULT_ITEMS))); 
							return r;
	    			}
	    			
	    			if (CaseInsensitiveString.equalsIgnoreCase(abs.getTypeName(), BasicOntology.EQUALS)) { 
							Equals e = new Equals();
  						AbsPredicate absEquals = (AbsPredicate) abs;
  						e.setLeft(onto.toObject(absEquals.getAbsTerm(BasicOntology.EQUALS_LEFT))); 
  						e.setRight(onto.toObject(absEquals.getAbsTerm(BasicOntology.EQUALS_RIGHT))); 
							return e;
	    			}
	    			
	    			throw new UnknownSchemaException();
        } 
        catch (OntologyException oe) {
        		// Forward the exception
            throw oe;
        } 
        catch (Throwable t) {
            throw new OntologyException("Schema and Java class do not match");
        } 
    } 
}
