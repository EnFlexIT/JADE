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
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import java.util.Date;

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
            if (obj instanceof Long) {
                return AbsPrimitive.wrap(((Long) obj).longValue());
            } 
            //__CLDC_UNSUPPORTED__BEGIN
            if (obj instanceof Float) {
                return AbsPrimitive.wrap(((Float) obj).floatValue());
            } 
            if (obj instanceof Double) {
                return AbsPrimitive.wrap(((Double) obj).doubleValue());
            } 
            //__CLDC_UNSUPPORTED__END
            if (obj instanceof Date) {
                return AbsPrimitive.wrap((Date) obj);
            } 


            if (obj instanceof List) {
              return AbsHelper.externaliseList((List) obj, referenceOnto);
            }

	    			if (obj instanceof Iterator) {
							return AbsHelper.externaliseIterator((Iterator) obj, referenceOnto);
	    			}
	    
	    			if(obj instanceof AID) {
							return AbsHelper.externaliseAID((AID)obj);
	    			}

            if (obj instanceof ContentElementList) {
            	return AbsHelper.externaliseContentElementList((ContentElementList) obj, referenceOnto);
            } 
	    
	    			if(obj instanceof TrueProposition) {
	    				AbsPredicate absTrueProp = new AbsPredicate(BasicOntology.TRUE_PROPOSITION);
							return absTrueProp;
	    			}

	    			if(obj instanceof Done) {
	    				AbsPredicate absDone = new AbsPredicate(BasicOntology.DONE);
  						absDone.set(BasicOntology.DONE_ACTION, (AbsAgentAction) referenceOnto.fromObject(((Done) obj).getAction()));
							return absDone;
	    			}

	    			if(obj instanceof Result) {
	    				AbsPredicate absResult = new AbsPredicate(BasicOntology.RESULT);
  						absResult.set(BasicOntology.RESULT_ACTION, (AbsAgentAction) referenceOnto.fromObject(((Result) obj).getAction()));
  						absResult.set(BasicOntology.RESULT_ITEMS, (AbsAggregate) referenceOnto.fromObject(((Result) obj).getItems()));
							return absResult;
	    			}

	    			if(obj instanceof Equals) {
	    				AbsPredicate absEquals = new AbsPredicate(BasicOntology.EQUALS);
  						absEquals.set(BasicOntology.EQUALS_LEFT, (AbsTerm) referenceOnto.fromObject(((Equals) obj).getLeft()));
  						absEquals.set(BasicOntology.EQUALS_RIGHT, (AbsTerm) referenceOnto.fromObject(((Equals) obj).getRight()));
							return absEquals;
	    			}

	    			if (obj instanceof Action) {
	    				AbsAgentAction absAction = new AbsAgentAction(BasicOntology.ACTION);
	    				((Action) obj).externalise(absAction, referenceOnto);
	    				return absAction;
	    			}
	    			
	    			if (obj instanceof ACLMessage) {
							return AbsHelper.externaliseACLMessage((ACLMessage)obj, referenceOnto);
	    			}
	    			
            throw new UnknownSchemaException();
        } 
        catch (OntologyException oe) {
        		// Forward the exception
            throw oe;
        } 
        catch (Throwable t) {
            throw new OntologyException("Schema and Java class do not match", t);
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

            // PRIMITIVE
            if (abs instanceof AbsPrimitive) {
                return ((AbsPrimitive) abs).getObject();
            } 
            // AGGREGATES
            if (abs instanceof AbsAggregate) {
                return AbsHelper.internaliseList((AbsAggregate) abs, referenceOnto);
            } 
						// CONTENT ELEMENT LIST
            if (abs instanceof AbsContentElementList) {
            	return AbsHelper.internaliseContentElementList((AbsContentElementList) abs, referenceOnto);
            } 
						// AID
	    			if (CaseInsensitiveString.equalsIgnoreCase(abs.getTypeName(), BasicOntology.AID)) { 
							return AbsHelper.internaliseAID((AbsConcept) abs);
	    			}
	    			// TRUE_PROPOSITION
	    			if (CaseInsensitiveString.equalsIgnoreCase(abs.getTypeName(), BasicOntology.TRUE_PROPOSITION)) { 
							TrueProposition t = new TrueProposition();
							return t;
	    			}
	    			// DONE
	    			if (CaseInsensitiveString.equalsIgnoreCase(abs.getTypeName(), BasicOntology.DONE)) { 
							Done d = new Done();
  						d.setAction((AgentAction) referenceOnto.toObject(abs.getAbsObject(BasicOntology.DONE_ACTION))); 
							return d;
	    			}
	    			// RESULT
	    			if (CaseInsensitiveString.equalsIgnoreCase(abs.getTypeName(), BasicOntology.RESULT)) { 
							Result r = new Result();
  						r.setAction((AgentAction) referenceOnto.toObject(abs.getAbsObject(BasicOntology.RESULT_ACTION))); 
  						r.setItems((List) referenceOnto.toObject(abs.getAbsObject(BasicOntology.RESULT_ITEMS))); 
							return r;
	    			}
	    			// EQUALS
	    			if (CaseInsensitiveString.equalsIgnoreCase(abs.getTypeName(), BasicOntology.EQUALS)) { 
							Equals e = new Equals();
  						e.setLeft(referenceOnto.toObject(abs.getAbsObject(BasicOntology.EQUALS_LEFT))); 
  						e.setRight(referenceOnto.toObject(abs.getAbsObject(BasicOntology.EQUALS_RIGHT))); 
							return e;
	    			}
	    			// ACTION
	    			if (CaseInsensitiveString.equalsIgnoreCase(abs.getTypeName(), BasicOntology.ACTION)) { 
	    				Action a = new Action();
	    				a.internalise(abs, referenceOnto);
	    				return a;
	    			}
						// ACLMESSAGE
	    			if (CaseInsensitiveString.equalsIgnoreCase(abs.getTypeName(), BasicOntology.ACLMSG)) { 
							return AbsHelper.internaliseACLMessage((AbsAgentAction) abs, referenceOnto);
	    			}
	    			
	    			throw new UnknownSchemaException();
        } 
        catch (OntologyException oe) {
        		// Forward the exception
            throw oe;
        } 
        catch (Throwable t) {
            throw new OntologyException("Schema and Java class do not match", t);
        } 
    } 
}
