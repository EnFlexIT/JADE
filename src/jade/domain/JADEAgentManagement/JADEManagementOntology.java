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

package jade.domain.JADEAgentManagement;

import jade.core.ContainerID;
import jade.content.onto.*;
import jade.content.schema.*;
import jade.domain.FIPAAgentManagement.ExceptionOntology;
import jade.domain.FIPAAgentManagement.NotRegistered;

/**
   This class represents the JADE-Agent-Management-ontology i.e. the set of
   concepts, actions and predicates that relates to the JADE specific
   actions that can be requested to the JADE AMS and DF.
   <p>
   The actual <code>Ontology</code> object representing the 
   JADE-Agent-Management-ontology is a singleton and is accessible through 
   the static method <code>getInstance()</code>
   @author Giovanni Caire -  TILAB
 */
public class JADEManagementOntology extends Ontology implements JADEManagementVocabulary {
 
  // The singleton instance of this ontology
	private static Ontology theInstance = new JADEManagementOntology();
	
  /**
     This method returns the unique instance (according to the singleton 
     pattern) of the JADE-Agent-Management-ontology.
     @return The singleton <code>Ontology</code> object, containing the 
     schemas for the elements of the JADE-Agent-Management-ontology.
  */
	public static Ontology getInstance() {
		return theInstance;
	}
	
  private JADEManagementOntology() {
    //#MIDP_EXCLUDE_BEGIN
  	super(NAME, BasicOntology.getInstance(), new BCReflectiveIntrospector());
    //#MIDP_EXCLUDE_END
    	
		/*#MIDP_INCLUDE_BEGIN    	
  	super(NAME, BasicOntology.getInstance(), null);
   	#MIDP_INCLUDE_END*/

    try {
    	//#MIDP_EXCLUDE_BEGIN
    	// Concepts definitions
    	add(new ConceptSchema(CONTAINERID), ContainerID.class);
    	add(new ConceptSchema(LOCATION));

    	// AgentActions definitions
	add(new AgentActionSchema(SHUTDOWNPLATFORM), ShutdownPlatform.class);
    	add(new AgentActionSchema(KILLCONTAINER), KillContainer.class);
    	add(new AgentActionSchema(CREATEAGENT), CreateAgent.class);
    	add(new AgentActionSchema(KILLAGENT), KillAgent.class);
    	add(new AgentActionSchema(INSTALLMTP), InstallMTP.class);
    	add(new AgentActionSchema(UNINSTALLMTP), UninstallMTP.class);
    	add(new AgentActionSchema(SNIFFON), SniffOn.class);
    	add(new AgentActionSchema(SNIFFOFF), SniffOff.class);
    	add(new AgentActionSchema(DEBUGON), DebugOn.class);
    	add(new AgentActionSchema(DEBUGOFF), DebugOff.class);
    	add(new AgentActionSchema(SHOWGUI), ShowGui.class);
    	add(new AgentActionSchema(WHEREISAGENT), WhereIsAgentAction.class);
    	add(new AgentActionSchema(QUERYAGENTSONLOCATION), QueryAgentsOnLocation.class);
    	add(new AgentActionSchema(QUERY_PLATFORM_LOCATIONS), QueryPlatformLocationsAction.class);

    	// Predicates definitions
    	add(new PredicateSchema(NOTREGISTERED), NotRegistered.class);
    	//#MIDP_EXCLUDE_END
    	
			/*#MIDP_INCLUDE_BEGIN    	
    	// Concepts definitions
    	add(new ConceptSchema(CONTAINERID));
    	
    	// AgentActions definitions
	add(new AgentActionSchema(SHUTDOWNPLATFORM));
    	add(new AgentActionSchema(KILLCONTAINER));
    	add(new AgentActionSchema(CREATEAGENT));
    	add(new AgentActionSchema(KILLAGENT));
    	add(new AgentActionSchema(INSTALLMTP));
    	add(new AgentActionSchema(UNINSTALLMTP));
    	add(new AgentActionSchema(SNIFFON));
    	add(new AgentActionSchema(SNIFFOFF));
    	add(new AgentActionSchema(DEBUGON));
    	add(new AgentActionSchema(DEBUGOFF));
    	add(new AgentActionSchema(SHOWGUI));

    	// Predicates definitions
    	add(new PredicateSchema(UNSUPPORTEDVALUE));
    	add(new PredicateSchema(UNRECOGNISEDVALUE));
    	add(new PredicateSchema(UNSUPPORTEDFUNCTION));
    	add(new PredicateSchema(MISSINGPARAMETER));
    	add(new PredicateSchema(UNEXPECTEDPARAMETER));
    	add(new PredicateSchema(UNRECOGNISEDPARAMETERVALUE));
    	add(new PredicateSchema(NOTREGISTERED));
    	add(new PredicateSchema(INTERNALERROR));
   		#MIDP_INCLUDE_END*/
   		
   		// Slots definitions
    	ConceptSchema cs = (ConceptSchema)getSchema(LOCATION);
			cs.add(LOCATION_NAME, (PrimitiveSchema)getSchema(BasicOntology.STRING));
			cs.add(LOCATION_PROTOCOL, (PrimitiveSchema)getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL);
			cs.add(LOCATION_ADDRESS, (PrimitiveSchema)getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL);
    	
    	cs = (ConceptSchema) getSchema(CONTAINERID);
    	cs.addSuperSchema((ConceptSchema) getSchema(LOCATION));

    	AgentActionSchema as = (AgentActionSchema) getSchema(SHUTDOWNPLATFORM);

    	as = (AgentActionSchema) getSchema(KILLCONTAINER);
    	as.add(KILLCONTAINER_CONTAINER, (ConceptSchema) getSchema(CONTAINERID)); 
    	as.add(KILLCONTAINER_PASSWORD, (PrimitiveSchema) getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL); 
    	
    	as = (AgentActionSchema) getSchema(CREATEAGENT);
    	as.add(CREATEAGENT_AGENT_NAME, (PrimitiveSchema) getSchema(BasicOntology.STRING)); 
    	as.add(CREATEAGENT_CLASS_NAME, (PrimitiveSchema) getSchema(BasicOntology.STRING)); 
    	as.add(CREATEAGENT_ARGUMENTS, (TermSchema) TermSchema.getBaseSchema(), 0, ObjectSchema.UNLIMITED); 
    	as.add(CREATEAGENT_CONTAINER, (ConceptSchema) getSchema(CONTAINERID));
    	as.add(CREATEAGENT_DELEGATION, (PrimitiveSchema) getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL); 
    	as.add(CREATEAGENT_PASSWORD, (PrimitiveSchema) getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL); 
    	
    	as = (AgentActionSchema) getSchema(KILLAGENT);
    	as.add(KILLAGENT_AGENT, (ConceptSchema) getSchema(BasicOntology.AID)); 
    	as.add(KILLAGENT_PASSWORD, (PrimitiveSchema) getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL); 

    	as = (AgentActionSchema) getSchema(INSTALLMTP);
    	as.add(INSTALLMTP_ADDRESS, (PrimitiveSchema) getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL); 
    	as.add(INSTALLMTP_CONTAINER, (ConceptSchema) getSchema(CONTAINERID)); 
    	as.add(INSTALLMTP_CLASS_NAME, (PrimitiveSchema) getSchema(BasicOntology.STRING)); 
    	
    	as = (AgentActionSchema) getSchema(UNINSTALLMTP);
    	as.add(UNINSTALLMTP_ADDRESS, (PrimitiveSchema) getSchema(BasicOntology.STRING)); 
    	as.add(UNINSTALLMTP_CONTAINER, (ConceptSchema) getSchema(CONTAINERID)); 
    	
    	as = (AgentActionSchema) getSchema(SNIFFON);
    	as.add(SNIFFON_SNIFFER, (ConceptSchema) getSchema(BasicOntology.AID)); 
    	as.add(SNIFFON_SNIFFED_AGENTS, (ConceptSchema) getSchema(BasicOntology.AID), 1, ObjectSchema.UNLIMITED); 
    	as.add(SNIFFON_PASSWORD, (PrimitiveSchema) getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL); 
    	
    	as = (AgentActionSchema) getSchema(SNIFFOFF);
    	as.add(SNIFFOFF_SNIFFER, (ConceptSchema) getSchema(BasicOntology.AID)); 
    	as.add(SNIFFOFF_SNIFFED_AGENTS, (ConceptSchema) getSchema(BasicOntology.AID), 1, ObjectSchema.UNLIMITED); 
    	as.add(SNIFFOFF_PASSWORD, (PrimitiveSchema) getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL); 
    	
    	as = (AgentActionSchema) getSchema(DEBUGON);
    	as.add(DEBUGON_DEBUGGER, (ConceptSchema) getSchema(BasicOntology.AID)); 
    	as.add(DEBUGON_DEBUGGED_AGENTS, (ConceptSchema) getSchema(BasicOntology.AID), 1, ObjectSchema.UNLIMITED); 
    	as.add(DEBUGON_PASSWORD, (PrimitiveSchema) getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL); 
    	
    	as = (AgentActionSchema) getSchema(DEBUGOFF);
    	as.add(DEBUGOFF_DEBUGGER, (ConceptSchema) getSchema(BasicOntology.AID)); 
    	as.add(DEBUGOFF_DEBUGGED_AGENTS, (ConceptSchema) getSchema(BasicOntology.AID), 1, ObjectSchema.UNLIMITED); 
    	as.add(DEBUGOFF_PASSWORD, (PrimitiveSchema) getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL); 
    	
    	as = (AgentActionSchema)getSchema(WHEREISAGENT);
			as.add(WHEREISAGENT_AGENTIDENTIFIER, (ConceptSchema)getSchema(BasicOntology.AID));
    
    	as = (AgentActionSchema)getSchema(QUERYAGENTSONLOCATION);
    	as.add(QUERYAGENTSONLOCATION_LOCATION, (ConceptSchema)getSchema(LOCATION));    	
    }
    catch(OntologyException oe) {
      oe.printStackTrace();
    }
  } 
}
