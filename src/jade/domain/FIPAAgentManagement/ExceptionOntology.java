/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop
multi-agent systems in compliance with the FIPA specifications.
Copyright (C) 2000 CSELT S.p.A. 

GNU Lesser General Public License

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation, 
version 2.1 of the License. 

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the
Free Software Foundation, Inc., 59 Temple Place - Suite 330,
Boston, MA  02111-1307, USA.
*****************************************************************/


package jade.domain.FIPAAgentManagement;

import jade.util.leap.List;
import jade.util.leap.LinkedList;
import jade.util.leap.Map;
import jade.util.leap.HashMap;
import jade.util.leap.Iterator;

import jade.content.lang.Codec;
import jade.content.onto.*;
import jade.content.onto.basic.*;
import jade.content.schema.*;

/**
   @author Fabio Bellifemine - CSELT S.p.A.
   @version $Date$ $Revision$
*/

/**
   This class represents the ontology defined by FIPA Agent Management 
   specifications (document no. 23). There is
   only a single instance of this class.
   <p>
   The package contains one class for each Frame in the ontology.
   <p>
   Notice that userDefinedslots will be parsed but ignored and not
   returned in the Java object. In order to get a userDefined Slot, a new
   Termdescriptor must be added to the Frame
   of this
   ontology and a new couple of set/get methods to the Java class representing
   that frame.
   Every class implementing a concept of the fipa-agent-management ontology is a 
   simple collection of attributes, with public methods to read and write them, 
   according to the frame based model that represents FIPA fipa-agent-management 
   ontology concepts.
   The following convention has been used. 
   For each attribute of the class, named attrName and of type attrType, 
   two cases are possible:
   1) The attribute type is a single value; then it can be read with attrType getAttrName() 
      and written with void setAttrName(attrType a), where every call to setAttrName() 
      overwrites any previous value of the attribute.
   2) The attribute type is a set or a sequence of values; then there is 
      an void addAttrName(attrType a) method to insert a new value and 
      a void clearAllAttrName() method to remove all the values (the list becomes empty). 
      Reading is performed by a  Iterator getAllAttrName() method that returns an Iterator 
      that allows the programmer to walk through the List and cast its elements to the appropriate type.
      * <p>
      * <i>
      * FIPA2000 still uses singular names for some slots whose type
      * value is a set. In particular for "ontologies","languages","protocols".
      * Because of that, since JADE 2.4, both singular and plural names
      * can be used and are valid for those slots.
      * That might change as soon as FIPA takes a final decision on the
      * names of those slots.
      * </i>
 */
public class ExceptionOntology  extends Ontology implements ExceptionVocabulary {

  private static Ontology theInstance = new ExceptionOntology();
  /**
     This method grants access to the unique instance of the
     ontology.
     @return An <code>Ontology</code> object, containing the concepts
     of the ontology.
  */
  public static Ontology getInstance() {
    return theInstance;
  }

  private ExceptionOntology() {
    //__CLDC_UNSUPPORTED__BEGIN
  	super(NAME, BasicOntology.getInstance(), new BCReflectiveIntrospector());
    //__CLDC_UNSUPPORTED__END
    	
		/*__J2ME_COMPATIBILITY__BEGIN    	
  	super(NAME, BasicOntology.getInstance(), null);
   	__J2ME_COMPATIBILITY__END*/


		try {
    	//__CLDC_UNSUPPORTED__BEGIN
	  	add(new PredicateSchema(UNAUTHORISED), Unauthorised.class);
	  	add(new PredicateSchema(UNSUPPORTEDACT), UnsupportedAct.class);
	  	add(new PredicateSchema(UNEXPECTEDACT), UnexpectedAct.class);
	  	add(new PredicateSchema(UNSUPPORTEDVALUE), UnsupportedValue.class);
	  	add(new PredicateSchema(UNRECOGNISEDVALUE), UnrecognisedValue.class);
	  	add(new PredicateSchema(UNSUPPORTEDFUNCTION), UnsupportedFunction.class);
	  	add(new PredicateSchema(MISSINGPARAMETER), MissingParameter.class);
	  	add(new PredicateSchema(UNEXPECTEDPARAMETER), UnexpectedParameter.class);
	  	add(new PredicateSchema(UNRECOGNISEDPARAMETERVALUE), UnrecognisedParameterValue.class);
	  	add(new PredicateSchema(ALREADYREGISTERED), AlreadyRegistered.class);
	  	add(new PredicateSchema(NOTREGISTERED), NotRegistered.class);
	  	add(new PredicateSchema(INTERNALERROR), InternalError.class);
    	//__CLDC_UNSUPPORTED__END
			   	  
			/*__J2ME_COMPATIBILITY__BEGIN    	
		add(new PredicateSchema(UNAUTHORISED));
	  	add(new PredicateSchema(UNSUPPORTEDACT));
	  	add(new PredicateSchema(UNEXPECTEDACT));
	  	add(new PredicateSchema(UNSUPPORTEDVALUE));
	  	add(new PredicateSchema(UNRECOGNISEDVALUE));
	  	add(new PredicateSchema(UNSUPPORTEDFUNCTION));
	  	add(new PredicateSchema(MISSINGPARAMETER));
	  	add(new PredicateSchema(UNEXPECTEDPARAMETER));
	  	add(new PredicateSchema(UNRECOGNISEDPARAMETERVALUE));
	  	add(new PredicateSchema(ALREADYREGISTERED));
	  	add(new PredicateSchema(NOTREGISTERED));
	  	add(new PredicateSchema(INTERNALERROR));
   		__J2ME_COMPATIBILITY__END*/
	  	
	  	PredicateSchema ps = (PredicateSchema)getSchema(UNSUPPORTEDACT);
	  	ps.add(UNSUPPORTEDACT_ACT, (PrimitiveSchema)getSchema(BasicOntology.STRING), ObjectSchema.MANDATORY);
	  	
	  	ps = (PredicateSchema)getSchema(UNEXPECTEDACT);
	  	ps.add(UNEXPECTEDACT_ACT, (PrimitiveSchema)getSchema(BasicOntology.STRING), ObjectSchema.MANDATORY);
	  
	  	ps = (PredicateSchema)getSchema(UNSUPPORTEDVALUE);
	  	ps.add(UNSUPPORTEDVALUE_VALUE, (PrimitiveSchema)getSchema(BasicOntology.STRING), ObjectSchema.MANDATORY);
	  
	  	ps = (PredicateSchema)getSchema(UNRECOGNISEDVALUE);
	  	ps.add(UNRECOGNISEDVALUE_VALUE, (PrimitiveSchema)getSchema(BasicOntology.STRING), ObjectSchema.MANDATORY);
	  
	  	ps = (PredicateSchema)getSchema(UNAUTHORISED);
	  
	  	ps = (PredicateSchema)getSchema(UNSUPPORTEDFUNCTION);
	  	ps.add(UNSUPPORTEDFUNCTION_FUNCTION, (PrimitiveSchema)getSchema(BasicOntology.STRING), ObjectSchema.MANDATORY);
	  
	  	ps = (PredicateSchema)getSchema(MISSINGPARAMETER);
	  	ps.add(MISSINGPARAMETER_OBJECT_NAME, (PrimitiveSchema)getSchema(BasicOntology.STRING), ObjectSchema.MANDATORY);
	  	ps.add(MISSINGPARAMETER_PARAMETER_NAME, (PrimitiveSchema)getSchema(BasicOntology.STRING), ObjectSchema.MANDATORY);
	  
	  	ps = (PredicateSchema)getSchema(UNEXPECTEDPARAMETER);
	  	ps.add(UNEXPECTEDPARAMETER_OBJECT_NAME, (PrimitiveSchema)getSchema(BasicOntology.STRING), ObjectSchema.MANDATORY);
	  	ps.add(UNEXPECTEDPARAMETER_PARAMETER_NAME, (PrimitiveSchema)getSchema(BasicOntology.STRING), ObjectSchema.MANDATORY);
	  
	    ps = (PredicateSchema)getSchema(UNRECOGNISEDPARAMETERVALUE);
	  	ps.add(UNRECOGNISEDPARAMETERVALUE_PARAMETER_NAME, (PrimitiveSchema)getSchema(BasicOntology.STRING), ObjectSchema.MANDATORY);
	  	ps.add(UNRECOGNISEDPARAMETERVALUE_PARAMETER_VALUE, (PrimitiveSchema)getSchema(BasicOntology.STRING), ObjectSchema.MANDATORY);

      	ps = (PredicateSchema)getSchema(INTERNALERROR);
      	ps.add(INTERNALERROR_MESSAGE, (PrimitiveSchema)getSchema(BasicOntology.STRING), ObjectSchema.MANDATORY);
    } 
    catch(OntologyException oe) {
    }
  } //end of initInstance


}