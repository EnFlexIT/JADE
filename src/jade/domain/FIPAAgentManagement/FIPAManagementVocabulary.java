package jade.domain.FIPAAgentManagement;

public interface FIPAManagementVocabulary extends ExceptionVocabulary {
	
  /**
    A symbolic constant, containing the name of this ontology.
  */
  public static final String NAME = "FIPA-Agent-Management";

  // Concepts
  public static final String DFAGENTDESCRIPTION = "df-agent-description";
  public static final String DFAGENTDESCRIPTION_NAME	= "name";
  public static final String DFAGENTDESCRIPTION_SERVICES = "services";
  public static final String DFAGENTDESCRIPTION_PROTOCOLS = "protocols";
  public static final String DFAGENTDESCRIPTION_ONTOLOGIES = "ontologies";
  public static final String DFAGENTDESCRIPTION_LANGUAGES = "languages";
  // For FIPA 2000 compatibility
  public static final String DFAGENTDESCRIPTION_PROTOCOL = "protocol";
  public static final String DFAGENTDESCRIPTION_ONTOLOGY = "ontology";
  public static final String DFAGENTDESCRIPTION_LANGUAGE = "language";

  public static final String SERVICEDESCRIPTION	= "service-description";
  public static final String SERVICEDESCRIPTION_NAME = "name";
  public static final String SERVICEDESCRIPTION_TYPE = "type";
  public static final String SERVICEDESCRIPTION_OWNERSHIP = "ownership";
  public static final String SERVICEDESCRIPTION_PROTOCOLS = "protocols";
  public static final String SERVICEDESCRIPTION_ONTOLOGIES = "ontologies";
  public static final String SERVICEDESCRIPTION_LANGUAGES = "languages";
  public static final String SERVICEDESCRIPTION_PROPERTIES = "properties";
  // For FIPA 2000 compatibility
  public static final String SERVICEDESCRIPTION_PROTOCOL = "protocol";
  public static final String SERVICEDESCRIPTION_ONTOLOGY = "ontology";
  public static final String SERVICEDESCRIPTION_LANGUAGE = "language";
  
  public static final String SEARCHCONSTRAINTS = "search-constraints";
  public static final String SEARCHCONSTRAINTS_MAX_DEPTH = "max-depth";
  public static final String SEARCHCONSTRAINTS_MAX_RESULTS = "max-results";
  
  public static final String AMSAGENTDESCRIPTION = "ams-agent-description";
  public static final String AMSAGENTDESCRIPTION_NAME = "name";
  public static final String AMSAGENTDESCRIPTION_OWNERSHIP = "ownership";
  public static final String AMSAGENTDESCRIPTION_STATE = "state";	

  public static final String PROPERTY = "property";
  public static final String PROPERTY_NAME = "name";
  public static final String PROPERTY_VALUE = "value";

  public static final String ENVELOPE 					= "envelope";
  public static final String ENVELOPE_TO 				= "to";
  public static final String ENVELOPE_FROM 				= "from";
  public static final String ENVELOPE_COMMENTS			= "comments";
  public static final String ENVELOPE_ACLREPRESENTATION = "acl-representation";
  public static final String ENVELOPE_PAYLOADLENGTH		= "payload-length";
  public static final String ENVELOPE_PAYLOADENCODING	= "payload-encoding";
  public static final String ENVELOPE_DATE				= "date";
  public static final String ENVELOPE_ENCRYPTED			= "encrypted";
  public static final String ENVELOPE_INTENDEDRECEIVER	= "intended-receiver";
  public static final String ENVELOPE_TRANSPORTBEHAVIOUR= "transport-behaviour";
  public static final String ENVELOPE_STAMPS			= "stamps";
 
  public static final String RECEIVEDOBJECT				= "received-object";
  public static final String RECEIVEDOBJECT_BY			= "by";
  public static final String RECEIVEDOBJECT_FROM		= "from";
  public static final String RECEIVEDOBJECT_DATE		= "date";
  public static final String RECEIVEDOBJECT_ID		 	= "id";
  public static final String RECEIVEDOBJECT_VIA	 		= "via";

  public static final String APDESCRIPTION					= "ap-description";
  public static final String APDESCRIPTION_NAME				= "name";
  public static final String APDESCRIPTION_DYNAMIC			= "dynamic";
  public static final String APDESCRIPTION_MOBILITY			= "mobility";
  public static final String APDESCRIPTION_TRANSPORTPROFILE = "transport-profile";

  public static final String APTRANSPORTDESCRIPTION 			= "ap-transport-description";
  public static final String APTRANSPORTDESCRIPTION_AVAILABLEMTPS	= "available-mtps";

  public static final String MTPDESCRIPTION					= "mtp-description";
  public static final String MTPDESCRIPTION_PROFILE			= "profile";
  public static final String MTPDESCRIPTION_NAME			= "mtp-name";
  public static final String MTPDESCRIPTION_ADDRESSES	    = "addresses"; 
 
  // Actions
  public static final String REGISTER = "register";
  public static final String REGISTER_DESCRIPTION = "description";
  
  public static final String DEREGISTER	= "deregister";
  public static final String DEREGISTER_DESCRIPTION = "description";
  
  public static final String MODIFY	= "modify";
  public static final String MODIFY_DESCRIPTION = "description";
  
  public static final String SEARCH	= "search";
  public static final String SEARCH_DESCRIPTION = "description";
  public static final String SEARCH_CONSTRAINTS = "constraints";
  
  public static final String GETDESCRIPTION = "get-description";
  
  public static final String QUIT = "quit";
  public static final String QUIT_AID = "agent-identifier";

  // Not-understood Exception Predicates
  public static final String UNSUPPORTEDACT = "unsupported-act";
  public static final String UNSUPPORTEDACT_ACT = "act";	
  
  public static final String UNEXPECTEDACT = "unexpected-act";
  public static final String UNEXPECTEDACT_ACT = "act";
  
  public static final String UNSUPPORTEDVALUE = "unsupported-value";
  public static final String UNSUPPORTEDVALUE_VALUE = "value";
    
  public static final String UNRECOGNISEDVALUE = "unrecognised-value";
  public static final String UNRECOGNISEDVALUE_VALUE = "value";	
  
  // Refusal Exception Predicates
  public static final String UNAUTHORISED = "unauthorised";
  
  public static final String UNSUPPORTEDFUNCTION = "unsupported-function";
  public static final String UNSUPPORTEDFUNCTION_FUNCTION = "function";
  
  public static final String MISSINGPARAMETER = "missing-parameter";
  public static final String MISSINGPARAMETER_OBJECT_NAME    = "object-name";	
  public static final String MISSINGPARAMETER_PARAMETER_NAME = "parameter-name";

  public static final String UNEXPECTEDPARAMETER = "unexpected-parameter";
  public static final String UNEXPECTEDPARAMETER_OBJECT_NAME = "object-name";
  public static final String UNEXPECTEDPARAMETER_PARAMETER_NAME = "parameter-name";
  
  public static final String UNRECOGNISEDPARAMETERVALUE = "unrecognised-parameter-value";
  public static final String UNRECOGNISEDPARAMETERVALUE_PARAMETER_NAME = "parameter-name";
  public static final String UNRECOGNISEDPARAMETERVALUE_PARAMETER_VALUE = "parameter-value";

  // Failure Exception Predicates
  public static final String ALREADYREGISTERED = "already-registered";
  public static final String NOTREGISTERED = "not-registered";
  
  public static final String INTERNALERROR = "internal-error";  
  public static final String INTERNALERROR_MESSAGE = "error-message";	

}