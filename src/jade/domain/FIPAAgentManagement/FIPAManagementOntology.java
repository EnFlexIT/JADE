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
public class FIPAManagementOntology  extends Ontology implements FIPAManagementVocabulary {

  private static Ontology theInstance = new FIPAManagementOntology();
  /**
     This method grants access to the unique instance of the
     ontology.
     @return An <code>Ontology</code> object, containing the concepts
     of the ontology.
  */
  public static Ontology getInstance() {
    return theInstance;
  }

  private FIPAManagementOntology() {
    //__CLDC_UNSUPPORTED__BEGIN
  	super(NAME, ExceptionOntology.getInstance(), new BCReflectiveIntrospector());
    //__CLDC_UNSUPPORTED__END
    	
		/*__J2ME_COMPATIBILITY__BEGIN    	
  	super(NAME, BasicOntology.getInstance(), null);
   	__J2ME_COMPATIBILITY__END*/


		try {
    	//__CLDC_UNSUPPORTED__BEGIN
	  	add(new ConceptSchema(DFAGENTDESCRIPTION), DFAgentDescription.class);
	  	add(new ConceptSchema(SERVICEDESCRIPTION), ServiceDescription.class);
	  	add(new ConceptSchema(SEARCHCONSTRAINTS), SearchConstraints.class);
	  	add(new ConceptSchema(AMSAGENTDESCRIPTION), AMSAgentDescription.class);
	  	add(new ConceptSchema(PROPERTY), Property.class);
	  	add(new ConceptSchema(ENVELOPE), Envelope.class);
	  	add(new ConceptSchema(RECEIVEDOBJECT), ReceivedObject.class);
	  	add(new ConceptSchema(APDESCRIPTION), APDescription.class);
	  	add(new ConceptSchema(APTRANSPORTDESCRIPTION), APTransportDescription.class);
	  	add(new ConceptSchema(MTPDESCRIPTION), MTPDescription.class);
	 	 	
	  	add(new AgentActionSchema(REGISTER), Register.class);
	  	add(new AgentActionSchema(DEREGISTER), Deregister.class);
	  	add(new AgentActionSchema(MODIFY), Modify.class);
	  	add(new AgentActionSchema(SEARCH), Search.class);
	  	add(new AgentActionSchema(GETDESCRIPTION), GetDescription.class);
	  	add(new AgentActionSchema(QUIT), Quit.class);
	  
    	//__CLDC_UNSUPPORTED__END
			   	  
			/*__J2ME_COMPATIBILITY__BEGIN    	
	  	add(new ConceptSchema(DFAGENTDESCRIPTION));
	  	add(new ConceptSchema(SERVICEDESCRIPTION));
	  	add(new ConceptSchema(SEARCHCONSTRAINTS));
	  	add(new ConceptSchema(AMSAGENTDESCRIPTION));
	  	add(new ConceptSchema(PROPERTY));
	  	
	  	add(new AgentActionSchema(REGISTER));
	  	add(new AgentActionSchema(DEREGISTER));
	  	add(new AgentActionSchema(MODIFY));
	  	add(new AgentActionSchema(SEARCH));
	  	add(new AgentActionSchema(GETDESCRIPTION));
	  	add(new AgentActionSchema(QUIT));
	  
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
	  	
	  	ConceptSchema cs = (ConceptSchema)getSchema(DFAGENTDESCRIPTION);
	  	cs.add(DFAGENTDESCRIPTION_NAME, (ConceptSchema)getSchema(BasicOntology.AID), ObjectSchema.OPTIONAL);
	  	cs.add(DFAGENTDESCRIPTION_SERVICES, (ConceptSchema)getSchema(SERVICEDESCRIPTION), 0, ObjectSchema.UNLIMITED);
	  	cs.add(DFAGENTDESCRIPTION_PROTOCOLS, (PrimitiveSchema)getSchema(BasicOntology.STRING), 0 , ObjectSchema.UNLIMITED);
	  	cs.add(DFAGENTDESCRIPTION_LANGUAGES, (PrimitiveSchema)getSchema(BasicOntology.STRING), 0, ObjectSchema.UNLIMITED);
	  	cs.add(DFAGENTDESCRIPTION_ONTOLOGIES, (PrimitiveSchema)getSchema(BasicOntology.STRING), 0, ObjectSchema.UNLIMITED);
  		// For FIPA 2000 compatibility
	  	cs.add(DFAGENTDESCRIPTION_PROTOCOL, (PrimitiveSchema)getSchema(BasicOntology.STRING), 0 , ObjectSchema.UNLIMITED);
	  	cs.add(DFAGENTDESCRIPTION_LANGUAGE, (PrimitiveSchema)getSchema(BasicOntology.STRING), 0, ObjectSchema.UNLIMITED);
	  	cs.add(DFAGENTDESCRIPTION_ONTOLOGY, (PrimitiveSchema)getSchema(BasicOntology.STRING), 0, ObjectSchema.UNLIMITED);

	  	cs = (ConceptSchema)getSchema(SERVICEDESCRIPTION);
	  	cs.add(SERVICEDESCRIPTION_NAME, (PrimitiveSchema)getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL);
	  	cs.add(SERVICEDESCRIPTION_TYPE, (PrimitiveSchema)getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL);
	  	cs.add(SERVICEDESCRIPTION_OWNERSHIP, (PrimitiveSchema)getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL);
	  	cs.add(SERVICEDESCRIPTION_PROTOCOLS, (PrimitiveSchema)getSchema(BasicOntology.STRING), 0, ObjectSchema.UNLIMITED);
	  	cs.add(SERVICEDESCRIPTION_LANGUAGES, (PrimitiveSchema)getSchema(BasicOntology.STRING), 0 , ObjectSchema.UNLIMITED);
	  	cs.add(SERVICEDESCRIPTION_ONTOLOGIES, (PrimitiveSchema)getSchema(BasicOntology.STRING), 0 , ObjectSchema.UNLIMITED);
	  	cs.add(SERVICEDESCRIPTION_PROPERTIES, (ConceptSchema)getSchema(PROPERTY), 0, ObjectSchema.UNLIMITED);
  		// For FIPA 2000 compatibility
	  	cs.add(SERVICEDESCRIPTION_PROTOCOL, (PrimitiveSchema)getSchema(BasicOntology.STRING), 0, ObjectSchema.UNLIMITED);
	  	cs.add(SERVICEDESCRIPTION_LANGUAGE, (PrimitiveSchema)getSchema(BasicOntology.STRING), 0 , ObjectSchema.UNLIMITED);
	  	cs.add(SERVICEDESCRIPTION_ONTOLOGY, (PrimitiveSchema)getSchema(BasicOntology.STRING), 0 , ObjectSchema.UNLIMITED);

	  	cs = (ConceptSchema)getSchema(SEARCHCONSTRAINTS);
	  	cs.add(SEARCHCONSTRAINTS_MAX_DEPTH, (PrimitiveSchema)getSchema(BasicOntology.INTEGER), ObjectSchema.OPTIONAL);
	  	cs.add(SEARCHCONSTRAINTS_MAX_RESULTS, (PrimitiveSchema)getSchema(BasicOntology.INTEGER), ObjectSchema.OPTIONAL);
	  
	  	cs = (ConceptSchema)getSchema(AMSAGENTDESCRIPTION);
	  	cs.add(AMSAGENTDESCRIPTION_NAME, (ConceptSchema)getSchema(BasicOntology.AID), ObjectSchema.OPTIONAL);
	  	cs.add(AMSAGENTDESCRIPTION_OWNERSHIP, (PrimitiveSchema)getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL);
	  	cs.add(AMSAGENTDESCRIPTION_STATE, (PrimitiveSchema)getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL);
	  
	  	cs = (ConceptSchema)getSchema(PROPERTY);
	  	cs.add(PROPERTY_NAME, (PrimitiveSchema)getSchema(BasicOntology.STRING));
	  	cs.add(PROPERTY_VALUE, (TermSchema)TermSchema.getBaseSchema());

	  	cs = (ConceptSchema)getSchema(ENVELOPE);
	  	cs.add(ENVELOPE_TO, (ConceptSchema)getSchema(BasicOntology.AID), 1, ObjectSchema.UNLIMITED);
	  	cs.add(ENVELOPE_FROM, (ConceptSchema)getSchema(BasicOntology.AID));
	  	cs.add(ENVELOPE_COMMENTS, (PrimitiveSchema)getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL);
	  	cs.add(ENVELOPE_ACLREPRESENTATION, (PrimitiveSchema)getSchema(BasicOntology.STRING));
	  	cs.add(ENVELOPE_PAYLOADLENGTH, (PrimitiveSchema)getSchema(BasicOntology.INTEGER));
	  	cs.add(ENVELOPE_PAYLOADENCODING, (PrimitiveSchema)getSchema(BasicOntology.STRING));
	  	cs.add(ENVELOPE_DATE, (PrimitiveSchema)getSchema(BasicOntology.DATE));
	  	cs.add(ENVELOPE_ENCRYPTED, (PrimitiveSchema)getSchema(BasicOntology.STRING), 0, ObjectSchema.UNLIMITED);
	  	cs.add(ENVELOPE_INTENDEDRECEIVER, (ConceptSchema)getSchema(BasicOntology.AID), 0, ObjectSchema.UNLIMITED);
	  	cs.add(ENVELOPE_STAMPS, (ConceptSchema)getSchema(RECEIVEDOBJECT), 0, ObjectSchema.UNLIMITED);

	  	cs = (ConceptSchema)getSchema(RECEIVEDOBJECT);
	  	cs.add(RECEIVEDOBJECT_BY, (PrimitiveSchema)getSchema(BasicOntology.STRING));
	  	cs.add(RECEIVEDOBJECT_FROM, (PrimitiveSchema)getSchema(BasicOntology.STRING));
	  	cs.add(RECEIVEDOBJECT_DATE, (PrimitiveSchema)getSchema(BasicOntology.DATE));
	  	cs.add(RECEIVEDOBJECT_ID, (PrimitiveSchema)getSchema(BasicOntology.STRING));
	  	cs.add(RECEIVEDOBJECT_VIA, (PrimitiveSchema)getSchema(BasicOntology.STRING));
	  	
	  	cs = (ConceptSchema)getSchema(APDESCRIPTION);
	  	cs.add(APDESCRIPTION_NAME, (PrimitiveSchema)getSchema(BasicOntology.STRING));
	  	cs.add(APDESCRIPTION_DYNAMIC, (PrimitiveSchema)getSchema(BasicOntology.BOOLEAN));
	  	cs.add(APDESCRIPTION_MOBILITY, (PrimitiveSchema)getSchema(BasicOntology.BOOLEAN));
	  	cs.add(APDESCRIPTION_TRANSPORTPROFILE, (ConceptSchema)getSchema(APTRANSPORTDESCRIPTION));

	  	cs = (ConceptSchema)getSchema(APTRANSPORTDESCRIPTION);
	  	cs.add(APTRANSPORTDESCRIPTION_AVAILABLEMTPS, (ConceptSchema)getSchema(MTPDESCRIPTION), 0, ObjectSchema.UNLIMITED);
	  	
	  	cs = (ConceptSchema)getSchema(MTPDESCRIPTION);
	  	cs.add(MTPDESCRIPTION_NAME, (PrimitiveSchema)getSchema(BasicOntology.STRING));
	  	cs.add(MTPDESCRIPTION_PROFILE, (PrimitiveSchema)getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL);
	  	cs.add(MTPDESCRIPTION_ADDRESSES, (PrimitiveSchema)getSchema(BasicOntology.STRING), 1, ObjectSchema.UNLIMITED);
	  	
  	
  		// FIXME: Configurare gli schemi per Envelope, ReceivedObject, APDescription, APTransportDescription, MTPDescription

	  	AgentActionSchema as = (AgentActionSchema)getSchema(REGISTER);
	  	as.add(REGISTER_DESCRIPTION, (TermSchema)TermSchema.getBaseSchema(), ObjectSchema.MANDATORY);
	  	as.setEncodingByOrder(true);
	  	
	  	as = (AgentActionSchema)getSchema(DEREGISTER);
	  	as.add(DEREGISTER_DESCRIPTION, (TermSchema)TermSchema.getBaseSchema(), ObjectSchema.MANDATORY);
	  	as.setEncodingByOrder(true);
	  
	  	as = (AgentActionSchema)getSchema(MODIFY);
	  	as.add(MODIFY_DESCRIPTION, (TermSchema)TermSchema.getBaseSchema(), ObjectSchema.MANDATORY);
	  	as.setEncodingByOrder(true);
	  
	  	as = (AgentActionSchema)getSchema(SEARCH);
	  	as.add(SEARCH_DESCRIPTION, (TermSchema)TermSchema.getBaseSchema(), ObjectSchema.MANDATORY);
	  	as.add(SEARCH_CONSTRAINTS, (ConceptSchema)getSchema(SEARCHCONSTRAINTS), ObjectSchema.MANDATORY);
	  	as.setEncodingByOrder(true);

	  	as = (AgentActionSchema)getSchema(QUIT);
	  	as.add(QUIT_AID, (ConceptSchema)getSchema(BasicOntology.AID), ObjectSchema.MANDATORY);
	  	as.setEncodingByOrder(true);
	  
    } 
    catch(OntologyException oe) {
    }
  } //end of initInstance


}