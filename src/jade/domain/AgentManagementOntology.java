/*
  $Id$
*/

package jade.domain;

import jade.core.Agent;

import java.util.Hashtable;


/**************************************************************

  Name: AgentManagementOntology

  Responsibility and Collaborations:

  + Holds in a single place all constants and data of
    'fipa-agent-management' ontology.

  + Provides access methods to check message correctness with respect
    to 'fipa-agent-management' ontology.

****************************************************************/
public class AgentManagementOntology {


  public static class APLifeCycle {

    // These String constants are the states of AP Life-Cycle
    public static final String INITIATED = "initiated";
    public static final String ACTIVE = "active";
    public static final String SUSPENDED = "suspended";
    public static final String WAITING = "waiting";
    public static final String DELETED = "deleted";

    // Table of AP Life-Cycle states
    private static Hashtable states = new Hashtable(5, 1.0f);

    // Utility class; can't be instantiated
    private APLifeCycle() {
    }

  }

  public static class DomainLifeCycle {

    // These String constants are the states of Domain Life-Cycle
    public static final String ACTIVE = "active";
    public static final String SUSPENDED = "suspended";
    public static final String RETIRED = "retired";
    public static final String UNKNOWN = "unknown";

    // Table of Domain Life-Cycle states
    private static Hashtable states = new Hashtable(4, 1.0f);

    // Utility class; can't be instantiated
    private DomainLifeCycle() {
    }

  }


  public static class DFAgentDescription {

    // These String constants are the keywords in
    // 'fipa-man-df-agent-description' objects
    static final String NAME = ":agent-name";
    static final String SERVICES = ":agent-services";
    static final String TYPE = ":agent-type";
    static final String PROTOCOLS = ":interaction-protocols";
    static final String ONTOLOGY = ":ontology";
    static final String ADDRESS = ":agent-address";
    static final String OWNERSHIP = ":ownership";
    static final String DFSTATE = ":df-state";

    // Table of allowed keywords in 'fipa-man-df-agent-description'
    // objects
    private static Hashtable keywords = new Hashtable(8, 1.0f);

    // Utility class; can't be instantiated
    private DFAgentDescription() {
    }

  } // End of DFAgentDescription class


  public static class PlatformProfile {

    // These String constants are the keywords in
    // 'fipa-man-platform-profile' objects.
    static final String NAME = ":platform-name";
    static final String IIOP = ":iiop-url";
    static final String DYNAMICREG = ":dynamic-registration";
    static final String MOBILITY = ":mobility";
    static final String OWNERSHIP = ":ownership";
    static final String CERTAUTH = ":certification-authority";
    static final String DEFAULTDF = ":default-df";

    // Table of allowed keywords in 'fipa-man-platform-profile'
    // objects
    private static Hashtable keywords = new Hashtable(7, 1.0f);

    // Utility class; can't be instantiated
    private PlatformProfile() {
    }

  } // End of PlatformProfile class


  public static class ServiceDescription {

    // These String constants are the keywords in
    // 'fipa-man-service-description' objects
    static final String TYPE = ":service-type";
    static final String ONTOLOGY = ":service-ontology";
    static final String DESCRIPTION = ":service-description";
    static final String CONDITION = ":service-condition";

    // Table of allowed keywords in 'fipa-man-service-description'
    // objects
    private static Hashtable keywords = new Hashtable(4, 1.0f);

    // Utility class; can't be instantiated
    private ServiceDescription() {
    }

  } // End of ServiceDescription class


  public static class ServiceTypes {

    // These String constants are the names of FIPA special services
    static final String FIPADF = "fipa-df";
    static final String FIPAAMS = "fipa-ams";
    static final String FIPAACC = "fipa-acc";
    static final String FIPAAGENT = "fipa-agent";

    // Utility class; can't be instantiated
    private ServiceTypes() {
    }

  } // End of ServiceTypes class


  public static class AMSAgentDescription {

    // These String constants are the keywords in
    // 'fipa-man-ams-agent-description' objects
    static final String NAME = ":agent-name";
    static final String ADDRESS = ":address";
    static final String SIGNATURE = ":signature";
    static final String APSTATE = ":ap-state";
    static final String DELEGATE = ":delegate-agent";
    static final String FORWARD = ":forward-address";

    // Table of allowed keywords in 'fipa-man-ams-agent-description'
    // objects
    private static Hashtable keywords = new Hashtable(6, 1.0f);

    // Utility class; can't be instantiated
    private AMSAgentDescription() {
    }

  } // End of AMSAgentDescription class


  public static class Exception {

    // These String constants are the names of all
    // 'fipa-management-exception' objects.
    static final String UNRECOGNIZEDVALUE = "unrecognised-attribute-value";
    static final String UNRECOGNIZEDATTR = "unrecognised-attribute";
    static final String UNWILLING = "unwilling-to-perform";
    static final String AGENTNOTREG = "agent-not-registered";
    static final String NOCOMM = "no-communication-means";
    static final String ACCUNAVAIL = "acc-unavailable";
    static final String UNABLETODEREG = "unable-to-deregister";
    static final String DFOVERLOADED = "df-overloaded";
    static final String INCONSISTENCY = "inconsistency";
    static final String AGENTALREADYREG = "agent-already-registered";
    static final String UNAUTHORISED = "unauthorised";
    static final String AMSOVERLOADED = "ams-overloaded";

    // Mapping of messages in 'fipa-man-ams-exception'
    // objects to Java exception objects
    private static Hashtable JavaExceptions = new Hashtable(12, 1.0f);

    // Utility class; can't be instantiated
    private Exception() {
    }

  } // End of Exception class


  public static class AMSActions {

    // These String constants are the names of the actions supported
    // by AMS agent
    static final String AUTHENTICATE = "authenticate";
    static final String REGISTERAGENT = "register-agent";
    static final String DEREGISTERAGENT = "deregister-agent";
    static final String MODIFYAGENT = "modify-agent";

    private static Hashtable actions = new Hashtable(4, 1.0f);

    // Utility class; can't be instantiated
    private AMSActions() {
    }

  } // End of AMSActions class


  public static class DFActions {

    // These String constants are the names of the actions supported
    // by DF agent
    static final String REGISTER = "register";
    static final String DEREGISTER = "deregister";
    static final String MODIFY = "modify";
    static final String SEARCH = "search";

    private static Hashtable actions = new Hashtable(4, 1.0f);

    // Utility class; can't be instantiated
    private DFActions() {
    }

  } // End of DFActions class


  // Used as lock in Doubly Checked Locking
  private static Object lock = new Object();

  // The single instance of AgentManagementOntology
  private static AgentManagementOntology singleton = null;

  // Private constructor: instantiate only through instance() method.
  private AgentManagementOntology() {

    // Fill in keyword tables -- When key == value an Hashtable is used as a Set

    DFAgentDescription.keywords.put(DFAgentDescription.NAME, new Integer(0));
    DFAgentDescription.keywords.put(DFAgentDescription.SERVICES, new Integer(1));
    DFAgentDescription.keywords.put(DFAgentDescription.TYPE, new Integer(2));
    DFAgentDescription.keywords.put(DFAgentDescription.PROTOCOLS, new Integer(3));
    DFAgentDescription.keywords.put(DFAgentDescription.ONTOLOGY, new Integer(4));
    DFAgentDescription.keywords.put(DFAgentDescription.ADDRESS, new Integer(5));
    DFAgentDescription.keywords.put(DFAgentDescription.OWNERSHIP, new Integer(6));
    DFAgentDescription.keywords.put(DFAgentDescription.DFSTATE, new Integer(7));

    PlatformProfile.keywords.put(PlatformProfile.NAME, PlatformProfile.NAME);
    PlatformProfile.keywords.put(PlatformProfile.IIOP, PlatformProfile.IIOP);
    PlatformProfile.keywords.put(PlatformProfile.DYNAMICREG, PlatformProfile.DYNAMICREG);
    PlatformProfile.keywords.put(PlatformProfile.MOBILITY, PlatformProfile.MOBILITY);
    PlatformProfile.keywords.put(PlatformProfile.OWNERSHIP, PlatformProfile.OWNERSHIP);
    PlatformProfile.keywords.put(PlatformProfile.CERTAUTH, PlatformProfile.CERTAUTH);
    PlatformProfile.keywords.put(PlatformProfile.DEFAULTDF, PlatformProfile.DEFAULTDF);

    ServiceDescription.keywords.put(ServiceDescription.TYPE, ServiceDescription.TYPE);
    ServiceDescription.keywords.put(ServiceDescription.ONTOLOGY, ServiceDescription.ONTOLOGY);
    ServiceDescription.keywords.put(ServiceDescription.DESCRIPTION, ServiceDescription.DESCRIPTION);
    ServiceDescription.keywords.put(ServiceDescription.CONDITION, ServiceDescription.CONDITION);

    AMSAgentDescription.keywords.put(AMSAgentDescription.NAME, new Integer(0));
    AMSAgentDescription.keywords.put(AMSAgentDescription.ADDRESS, new Integer(1));
    AMSAgentDescription.keywords.put(AMSAgentDescription.SIGNATURE, new Integer(2));
    AMSAgentDescription.keywords.put(AMSAgentDescription.APSTATE, new Integer(3));
    AMSAgentDescription.keywords.put(AMSAgentDescription.DELEGATE, new Integer(4));
    AMSAgentDescription.keywords.put(AMSAgentDescription.FORWARD, new Integer(5));



    // Fill in exception message -> Java Exception mapping

    Exception.JavaExceptions.put(Exception.UNRECOGNIZEDVALUE, new UnrecognizedAttributeValueException());
    Exception.JavaExceptions.put(Exception.UNRECOGNIZEDATTR, new UnrecognizedAttributeException());
    Exception.JavaExceptions.put(Exception.UNWILLING, new UnwillingToPerformException());
    Exception.JavaExceptions.put(Exception.AGENTNOTREG, new AgentNotRegisteredException());
    Exception.JavaExceptions.put(Exception.NOCOMM, new NoCommunicationMeansException());
    Exception.JavaExceptions.put(Exception.ACCUNAVAIL, new ACCUnavailableException());
    Exception.JavaExceptions.put(Exception.UNABLETODEREG, new UnableToDeregisterException());
    Exception.JavaExceptions.put(Exception.DFOVERLOADED, new DFOverloadedException());
    Exception.JavaExceptions.put(Exception.INCONSISTENCY, new InconsistencyException());
    Exception.JavaExceptions.put(Exception.AGENTALREADYREG, new AgentAlreadyRegisteredException());
    Exception.JavaExceptions.put(Exception.UNAUTHORISED, new UnauthorisedException());
    Exception.JavaExceptions.put(Exception.AMSOVERLOADED, new AMSOverloadedException());


    // Fill in action names for AMS and DF agents

    AMSActions.actions.put(AMSActions.AUTHENTICATE, new Integer(0));
    AMSActions.actions.put(AMSActions.REGISTERAGENT, new Integer(1));
    AMSActions.actions.put(AMSActions.DEREGISTERAGENT, new Integer(2));
    AMSActions.actions.put(AMSActions.MODIFYAGENT, new Integer(3));

    DFActions.actions.put(DFActions.REGISTER, new Integer(0));
    DFActions.actions.put(DFActions.DEREGISTER, new Integer(1));
    DFActions.actions.put(DFActions.MODIFY, new Integer(2));
    DFActions.actions.put(DFActions.SEARCH, new Integer(3));


    // Fill in AP Life-Cycle states

    APLifeCycle.states.put(APLifeCycle.INITIATED, new Integer(Agent.AP_INITIATED));
    APLifeCycle.states.put(APLifeCycle.ACTIVE, new Integer(Agent.AP_ACTIVE));
    APLifeCycle.states.put(APLifeCycle.SUSPENDED, new Integer(Agent.AP_SUSPENDED));
    APLifeCycle.states.put(APLifeCycle.WAITING, new Integer(Agent.AP_WAITING));
    APLifeCycle.states.put(APLifeCycle.DELETED, new Integer(Agent.AP_DELETED));


    // Fill in Domain Life-Cycle states

    DomainLifeCycle.states.put(DomainLifeCycle.ACTIVE, new Integer(Agent.D_ACTIVE));
    DomainLifeCycle.states.put(DomainLifeCycle.SUSPENDED, new Integer(Agent.D_SUSPENDED));
    DomainLifeCycle.states.put(DomainLifeCycle.RETIRED, new Integer(Agent.D_RETIRED));
    DomainLifeCycle.states.put(DomainLifeCycle.UNKNOWN, new Integer(Agent.D_UNKNOWN));

  }


  // Table of mandatory attributes for AMS actions. First index is the
  // action number and second index is attribute number. Those two
  // numbers can be obtained from action and attribute names, looking
  // up in 'keywords' Hashtable of AMSActions and AMSAgentDescription
  // classes
  private static final boolean AMSMandatoryAttributes[][] = {
    { true,  false, true,  false, false, false }, // 'authenticate' action
    { true,  true,  false, true,  false, false }, // 'register-agent' action
    { true,  false, false, false, false, false }, // 'deregister-agent' action
    { true,  false, false, false, false, false }  // 'modify-agent' action
  };


  // Table of mandatory attributes for DF actions. First index is the
  // action number and second index is attribute number. Those two
  // numbers can be obtained from action and attribute names, looking
  // up in 'keywords' Hashtable of DFActions and DFAgentDescription
  // classes
  private static final boolean DFMandatoryAttributes[][] = {
    { true,  false, true,  false, false, true,  true,  true  }, // 'register' action
    { true,  false, false, false, false, false, false, false }, // 'deregister' action
    { true,  false, false, false, false, false, false, false }, // 'modify' action
    { false, false, false, false, false, false, false, false }  // 'search' action
  };


  // Static method to obtain an handle to the single instance of
  // AgentManagementOntology class
  public static final AgentManagementOntology instance() {

    // Doubly-Checked Locking initialization
    if(singleton == null)
      synchronized(lock) {
	if(singleton == null)
	  singleton = new AgentManagementOntology();
      }

    return singleton;

  }


  // Ontological check methods. These methods are used by AMS and DF
  // agents to perform correctness controls over received messages
  // content


  // Check that 'keyword' is a valid 'fipa-man-df-agent-description'
  // keyword
  public boolean isValidDFADKeyword(String keyword) {
    return DFAgentDescription.keywords.containsKey(keyword);
  }

  // Check that 'keyword' is a valid 'fipa-man-platform-profile'
  // keyword
  public boolean isValidPPKeyword(String keyword) {
    return PlatformProfile.keywords.containsKey(keyword);
  }

  // Check that 'keyword' is a valid 'fipa-man-service-description'
  // keyword
  public boolean isValidSDKeyword(String keyword) {
    return ServiceDescription.keywords.containsKey(keyword);
  }

  // Check that 'keyword' is a valid 'fipa-man-ams-agent-description'
  // keyword
  public boolean isValidAMSADKeyword(String keyword) {
    return AMSAgentDescription.keywords.containsKey(keyword);
  }

  // Check that 'message' is a valid 'fipa-man-exception' error
  // message
  public boolean isValidException(String message) {
    return Exception.JavaExceptions.containsKey(message);
  }

  // Tell whether 'attributeName' is a mandatory attribute for
  // 'actionName' AMS action
  public boolean isMandatoryForAMS(String actionName, String attributeName) {
    Integer actionIndex = (Integer)AMSActions.actions.get(actionName);
    Integer attributeIndex = (Integer)AMSAgentDescription.keywords.get(attributeName);
    return AMSMandatoryAttributes[actionIndex.intValue()][attributeIndex.intValue()];
  }

  // Tell whether 'attributeName' is a mandatory attribute for
  // 'actionName' DF action
  public boolean isMandatoryForDF(String actionName, String attributeName) {
    Integer actionIndex = (Integer)DFActions.actions.get(actionName);
    Integer attributeIndex = (Integer)DFAgentDescription.keywords.get(attributeName);
    return DFMandatoryAttributes[actionIndex.intValue()][attributeIndex.intValue()];
  }


  // Lookup methods to convert between different data representations

  // Return the Java exception corresponding to a given message
  public FIPAException getException(String message) {
    return (FIPAException)Exception.JavaExceptions.get(message);
  }

  // Return the number code of a given AP Life-Cycle state
  public int getAPStateByName(String name) throws FIPAException {
    Integer i = (Integer)APLifeCycle.states.get(name);
    if(i == null) throw getException(Exception.UNRECOGNIZEDVALUE);
    return i.intValue();
  }

  // Return the name of the AP Life-Cycle state of a given code
  public String getAPStateByCode(int code) throws FIPAException {
    switch(code) {
    case Agent.AP_INITIATED:
      return APLifeCycle.INITIATED;
    case Agent.AP_ACTIVE:
      return APLifeCycle.ACTIVE;
    case Agent.AP_SUSPENDED:
      return APLifeCycle.SUSPENDED;
    case Agent.AP_WAITING:
      return APLifeCycle.WAITING;
    case Agent.AP_DELETED:
      return APLifeCycle.DELETED;
    default:
      throw getException(Exception.UNRECOGNIZEDVALUE);
    }
  }

  // Return the number code of a given Domain Life-Cycle state
  public int getDomainStateByName(String name) throws FIPAException {
    Integer i = (Integer)DomainLifeCycle.states.get(name);
    if(i == null) throw getException(Exception.UNRECOGNIZEDVALUE);
    return i.intValue();
  }


  public String getDomainStatebyCode(int code) throws FIPAException {
    switch(code) {
    case Agent.D_ACTIVE:
      return DomainLifeCycle.ACTIVE;
    case Agent.D_SUSPENDED:
      return DomainLifeCycle.SUSPENDED;
    case Agent.D_RETIRED:
      return DomainLifeCycle.RETIRED;
    case Agent.D_UNKNOWN:
      return DomainLifeCycle.UNKNOWN;
    default:
      throw getException(Exception.UNRECOGNIZEDVALUE);
    }
  }


}
