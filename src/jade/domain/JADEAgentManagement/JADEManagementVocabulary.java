package jade.domain.JADEAgentManagement;

public interface JADEManagementVocabulary {
	
	 /**
    A symbolic constant, containing the name of this ontology.
   */
  public static final String NAME = "JADE-Agent-Management";

  public static final String AGENTIDENTIFIER = "agent-identifier";
  
  // Concepts
  public static final String CONTAINERID = "container-ID";
  public static final String CONTAINERID_NAME = "name";
  public static final String CONTAINERID_ADDRESS = "address";
  
  public static final String LOCATION = "location";
  public static final String LOCATION_NAME = "name";
  public static final String LOCATION_ADDRESS = "address";
  public static final String LOCATION_PROTOCOL = "protocol";

  // Actions supported by the ams
  public static final String QUERYAGENTSONLOCATION = "query-agents-on-location";
  public static final String QUERYAGENTSONLOCATION_LOCATION = "location";
  
  public static final String KILLCONTAINER = "kill-container";
  public static final String KILLCONTAINER_CONTAINER = "container";
  public static final String KILLCONTAINER_PASSWORD = "password";
  
  public static final String CREATEAGENT = "create-agent";
  public static final String CREATEAGENT_AGENT_NAME = "agent-name";
  public static final String CREATEAGENT_CLASS_NAME = "class-name";
  public static final String CREATEAGENT_ARGUMENTS = "arguments";
  public static final String CREATEAGENT_CONTAINER = "container";
  public static final String CREATEAGENT_DELEGATION = "delegation";
  public static final String CREATEAGENT_PASSWORD = "password";
	
  public static final String KILLAGENT = "kill-agent";
  public static final String KILLAGENT_AGENT = "agent";
  public static final String KILLAGENT_PASSWORD = "password";

  public static final String INSTALLMTP = "install-mtp";
  public static final String INSTALLMTP_ADDRESS = "address";
  public static final String INSTALLMTP_CONTAINER = "container";
  public static final String INSTALLMTP_CLASS_NAME = "class-name";

  public static final String UNINSTALLMTP = "uninstall-mtp";
  public static final String UNINSTALLMTP_ADDRESS = "address";
  public static final String UNINSTALLMTP_CONTAINER = "container";

  public static final String SNIFFON = "sniff-on";
  public static final String SNIFFON_SNIFFER = "sniffer";
  public static final String SNIFFON_SNIFFED_AGENTS = "sniffed-agents";
  public static final String SNIFFON_PASSWORD = "password";

  public static final String SNIFFOFF = "sniff-off";
  public static final String SNIFFOFF_SNIFFER = "sniffer";
  public static final String SNIFFOFF_SNIFFED_AGENTS = "sniffed-agents";
  public static final String SNIFFOFF_PASSWORD = "password";

  public static final String DEBUGON = "debug-on";
  public static final String DEBUGON_DEBUGGER = "debugger";
  public static final String DEBUGON_DEBUGGED_AGENTS = "debugged-agents";
  public static final String DEBUGON_PASSWORD = "password";

  public static final String DEBUGOFF = "debug-off";
  public static final String DEBUGOFF_DEBUGGER = "debugger";
  public static final String DEBUGOFF_DEBUGGED_AGENTS = "debugged-agents";
  public static final String DEBUGOFF_PASSWORD = "password";

  public static final String WHEREISAGENT = "where-is-agent";
  public static final String WHEREISAGENT_AGENTIDENTIFIER = "agent-identifier";
  
  public static final String QUERY_PLATFORM_LOCATIONS = "query-platform-locations";

  // actions supported by the DF
  public static final String SHOWGUI = "showgui";

  // Exception Predicates
  public static final String NOTREGISTERED = jade.domain.FIPAAgentManagement.FIPAAgentManagementOntology.NOTREGISTERED;
  
  public static final String INTERNALERROR = jade.domain.FIPAAgentManagement.FIPAAgentManagementOntology.INTERNALERROR;
  public static final String INTERNALERROR_MESSAGE = "_0";

  public static final String UNSUPPORTEDVALUE = jade.domain.FIPAAgentManagement.FIPAAgentManagementOntology.UNSUPPORTEDVALUE;
  public static final String UNSUPPORTEDVALUE_VALUE = "_0";

  public static final String UNRECOGNISEDVALUE = jade.domain.FIPAAgentManagement.FIPAAgentManagementOntology.UNRECOGNISEDVALUE;
  public static final String UNRECOGNISEDVALUE_VALUE = "_0";

  public static final String UNSUPPORTEDFUNCTION = jade.domain.FIPAAgentManagement.FIPAAgentManagementOntology.UNSUPPORTEDFUNCTION;
  public static final String UNSUPPORTEDFUNCTION_FUNCTION = "_0";

  public static final String MISSINGPARAMETER = jade.domain.FIPAAgentManagement.FIPAAgentManagementOntology.MISSINGPARAMETER;
  public static final String MISSINGPARAMETER_OBJECT_NAME = "_0";
  public static final String MISSINGPARAMETER_PARAMETER_NAME = "_1";

  public static final String UNEXPECTEDPARAMETER = jade.domain.FIPAAgentManagement.FIPAAgentManagementOntology.UNEXPECTEDPARAMETER;
  public static final String UNEXPECTEDPARAMETER_OBJECT_NAME = "_0";
  public static final String UNEXPECTEDPARAMETER_PARAMETER_NAME = "_1";

  public static final String UNRECOGNISEDPARAMETERVALUE = jade.domain.FIPAAgentManagement.FIPAAgentManagementOntology.UNRECOGNISEDPARAMETERVALUE;
  public static final String UNRECOGNISEDPARAMETERVALUE_OBJECT_NAME = "_0";
  public static final String UNRECOGNISEDPARAMETERVALUE_PARAMETER_NAME = "_1";
}