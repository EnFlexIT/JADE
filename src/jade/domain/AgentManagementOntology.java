/*
  $Log$
  Revision 1.13  1998/11/05 23:33:22  rimassa
  Added some String constants and toText() method for CreateAgentAction
  and KillAgentAction inner classes.

  Revision 1.12  1998/11/03 00:33:43  rimassa
  Complete implementation of a class hierarchy for AMS events; now each
  AMS event has set()/get() methods for all its fields and a couple of
  fromText()/toText() methods to convert it to and from strings.

  Revision 1.11  1998/11/02 01:59:41  rimassa
  Added inner classes to represent AMS notifications of various events
  (container creation and deletion, new agents, ecc.).

  Revision 1.10  1998/10/31 16:39:37  rimassa
  Completed the definition of KillAgentAction inner class, to represent
  'kill-agent' AMS action instances.

  Revision 1.9  1998/10/26 22:39:48  Giovanni
  Added a list of properties to CreateAgentAction class.

  Revision 1.8  1998/10/26 00:02:21  rimassa
  Added new inner classes and string constant to represent
  'create-agent' and 'kill-agent' new AMS actions.

  Revision 1.7  1998/10/18 17:35:36  rimassa
  Added some static final String varibles to Constraint class to
  represent various keywords.

  Revision 1.6  1998/10/04 18:01:21  rimassa
  Added a 'Log:' field to every source file.

*/

package jade.domain;

import java.io.IOException;
import java.io.Serializable;
import java.io.Reader;
import java.io.Writer;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;


/**************************************************************

  Name: AgentManagementOntology

  Responsibility and Collaborations:

  + Holds in a single place all constants and data of
    'fipa-agent-management' ontology.

  + Provides access methods to check message correctness with respect
    to 'fipa-agent-management' ontology.

****************************************************************/
public class AgentManagementOntology {

  /*****************************************************************

    Name: ServiceDescriptor

    Responsibility and Collaborations:

    + Represents a 'FIPA-Service-Desc' of 'fipa-agent-management'
      ontology as a Java object, to avoid scattering parsing code
      throughout applications.
      (df, AgentManagementParser)

  ******************************************************************/
  public static class ServiceDescriptor {

    static final String TITLE = ":service-description";

    // These String constants are the keywords in
    // 'FIPA-Service-Desc-Item' objects
    static final String NAME = ":service-name";
    static final String TYPE = ":service-type";
    static final String ONTOLOGY = ":service-ontology";
    static final String FIXEDPROPS = ":fixed-properties";
    static final String NEGOTIABLEPROPS = ":negotiable-properties";
    static final String COMMUNICATIONPROPS = ":communication-properties";

    // Table of allowed keywords in 'FIPA-Service-Desc-Item' objects
    private static Hashtable keywords = new Hashtable(6, 1.0f);

    private String name;
    private String type;
    private String ontology;
    private String fixedProperties;
    private String negotiableProperties;
    private String communicationProperties;

    public static ServiceDescriptor fromText(Reader r) throws ParseException, TokenMgrError {
      return AgentManagementOntology.parser.parseServiceDescriptor(r);
    }

    public void setName(String n) {
      name = n;
    }

    public String getName() {
      return name;
    }

    public void setType(String t) {
      type = t;
    }

    public String getType() {
      return type;
    }

    public void setOntology(String o) {
      ontology = o;
    }

    public String getOntology() {
      return ontology;
    }

    public void setFixedProps(String fp) {
      fixedProperties = fp;
    }

    public String getFixedProps() {
      return fixedProperties;
    }

    public void setNegotiableProps(String np) {
      negotiableProperties = np;
    }

    public String getNegotiableProps() {
      return negotiableProperties;
    }

    public void setCommunicationProps(String cp) {
      communicationProperties = cp;
    }

    public String getCommunicationProps() {
      return communicationProperties;
    }

    // Convert a service description object to characters text

    public void toText(Writer w) {
      try {
	w.write("( " + TITLE);
	if(name != null)
	  w.write("( " + NAME + " " + name + " )");
	if(type != null)
	  w.write("( " + TYPE + " " + type + " )");
	if(ontology != null)
	  w.write("( " + ONTOLOGY + " " + ontology + " )");
	if(fixedProperties != null)
	  w.write("( " + FIXEDPROPS + " " + fixedProperties + " )");
	if(negotiableProperties != null)
	  w.write("( " + NEGOTIABLEPROPS + " " + negotiableProperties + " )");
	if(communicationProperties != null)
	  w.write("( " + COMMUNICATIONPROPS +" " + communicationProperties + " )");
	w.write(" )");
	w.flush();
      }
      catch(IOException ioe) {
	ioe.printStackTrace();
      }

    }

  } // End of ServiceDescriptor class



  /************************************************************************

    Name: AMSAgentDescriptor

    Responsibilities and Collaborations:

    + Provide platform-level support to AMS agent, holding all
      informations needed by 'AMS-agent-description' objects in
      'fipa-agent-management' ontology.
      (ams)

    + Represent the information above as an object, to avoid scattering
      parsing code throughout applications.
      (AgentManagementParser)

  ************************************************************************/
  public static class AMSAgentDescriptor implements Serializable {

    // These String constants are the keywords in
    // 'FIPA-AMS-description' objects
    static final String NAME = ":agent-name";
    static final String ADDRESS = ":address";
    static final String SIGNATURE = ":signature";
    static final String APSTATE = ":ap-state";
    static final String DELEGATE = ":delegate-agent-name";
    static final String FORWARD = ":forward-address";
    static final String OWNERSHIP = ":ownership";

    // Table of allowed keywords in 'FIPA-AMS-description' objects
    private static Hashtable keywords = new Hashtable(7, 1.0f);

    private String name;
    private String address;
    private String signature;
    private int APState;
    private String delegateAgentName;
    private String forwardAddress;
    private String ownership;


    public static AMSAgentDescriptor fromText(Reader r) throws ParseException, TokenMgrError {
      return AgentManagementOntology.parser.parseAMSDescriptor(r);
    }

    // Modifiers

    public void setName(String n) {
      name = n;
    }

    public void setAddress(String a) {
      address = a;
    }

    public void setSignature(String s) {
      signature = s;
    }

    public void setAPState(int AP) throws IllegalArgumentException {
      if( (AP <= Agent.AP_MIN)||(AP >= Agent.AP_MAX) )
	throw new IllegalArgumentException("APState out of range");
      APState = AP;
    }

    public void setDelegateAgentName(String d) {
      delegateAgentName = d;
    }

    public void setForwardAddress(String f) {
      forwardAddress = f;
    }

    public void setOwnership(String o) {
      ownership = o;
    }

    // Accessors

    public String getName() {
      return name;
    }

    public String getAddress() {
      return address;
    }

    public String getSignature() {
      return signature;
    }

    public String getAPState() {
      String result = null;
      try {
	result = AgentManagementOntology.instance().getAPStateByCode(APState);
      }
      catch(FIPAException fe) {
	//	fe.printStackTrace();
      }
      return result;
    }

    public String getDelegateAgentName() {
      return delegateAgentName;
    }

    public String getForwardAddress() {
      return forwardAddress;
    }

    public String getOwnership() {
      return ownership;
    }

    // Print out AMS descriptor

    public void toText(Writer w) {

      AgentManagementOntology o = AgentManagementOntology.instance();
      try {
	if(name != null)
	  w.write("( " + NAME + " " + name + " )");
	if(address != null)
	  w.write("( " + ADDRESS + " " + address + " )");
	if(signature != null)
	  w.write("( " + SIGNATURE + " " + signature + " )");
	try {
	  String APStateName = o.getAPStateByCode(APState);
	  w.write("( " + APSTATE + " " + APStateName + " )");
	}
	catch(FIPAException fe) {
	  // Invalid APState value: don't print it
	}
	if(delegateAgentName != null)
	  w.write("( " + DELEGATE + " " + delegateAgentName + " )");
	if(forwardAddress != null)
	  w.write("( " + FORWARD + " " + forwardAddress + " )");
	if(ownership != null)
	  w.write("( " + OWNERSHIP + " " + ownership + " )");
	w.flush();
      }
      catch(IOException ioe) {
	ioe.printStackTrace();
      }

    }

  } // End of AMSAgentDescriptor class


  /************************************************************************

    Name: DFAgentDescriptor

    Responsibilities and Collaborations:

    + Provide platform-level support to DF agent, holding all
      informations needed by 'DF-agent-description' objects in
      'fipa-agent-management' ontology.
      (df)

    + Represent the information above as an object, to avoid scattering
      parsing code throughout applications.
      (AgentManagementParser)

  ************************************************************************/
  public static class DFAgentDescriptor {

    // These String constants are the keywords in
    // 'FIPA-DF-description' objects
    static final String NAME = ":agent-name";
    static final String ADDRESS = ":agent-address";
    static final String SERVICES = ":agent-services";
    static final String TYPE = ":agent-type";
    static final String PROTOCOLS = ":interaction-protocols";
    static final String ONTOLOGY = ":ontology";
    static final String OWNERSHIP = ":ownership";
    static final String DFSTATE = ":df-state";

    // Table of allowed keywords in 'FIPA-DF-description' objects
    private static Hashtable keywords = new Hashtable(8, 1.0f);

    private String name;
    private Vector addresses = new Vector();
    private Vector services = new Vector();
    private String type;
    private Vector interactionProtocols = new Vector();
    private String ontology;
    private String ownership;
    private String DFState;


    public static DFAgentDescriptor fromText(Reader r) throws ParseException, TokenMgrError {
      return AgentManagementOntology.parser.parseDFDescriptor(r);
    }

    public void setName(String n) {
      name = n;
    }

    public String getName() {
      return name;
    }

    public void addAddress(String a) {
      addresses.addElement(a);
    }

    public void removeAddresses() {
      addresses.removeAllElements();
    }

    public Enumeration getAddresses() {
      return addresses.elements();
    }

    public void addService(ServiceDescriptor sd) {
      services.addElement(sd);
    }

    public void removeAgentServices() {
      services.removeAllElements();
    }

    public Enumeration getAgentServices() {
      return services.elements();
    }

    public void setType(String t) {
      type = t;
    }
 
    public String getType() {
      return type;
    }

    public void addInteractionProtocol(String ip) {
      interactionProtocols.addElement(ip);
    }

    public void removeInteractionProtocols() {
      interactionProtocols.removeAllElements();
    }

    public Enumeration getInteractionProtocols() {
      return interactionProtocols.elements();
    }

    public void setOntology(String o) {
      ontology = o;
    }

    public String getOntology() {
      return ontology;
    }

    public void setOwnership(String o) {
      ownership = o;
    }

    public String getOwnership() {
      return ownership;
    }

    public void setDFState(String dfs) {
      DFState = dfs;
    }

    String getDFState() {
      return DFState;
    }

    public void toText(Writer w) {
      try {
	Enumeration e = null;
	if(name != null)
	  w.write("( " + NAME + " " + name + " )");

	if(!addresses.isEmpty()) {
	  w.write("( " + ADDRESS);
	  e = getAddresses();
	  while(e.hasMoreElements())
	    w.write("    " + e.nextElement());
	  w.write(" )");
	}

	if(!services.isEmpty()) {
	  w.write("( " + SERVICES);
	  e = getAgentServices();
	  while(e.hasMoreElements()) {
	    ServiceDescriptor sd = (ServiceDescriptor)e.nextElement();
	    sd.toText(w);
	  }
	  w.write(" )");
	}

	if(type != null)
	  w.write(" ( " + TYPE + " " + type + " )");

	if(!interactionProtocols.isEmpty()) {
	  w.write("( " + PROTOCOLS + " (");
	  e = getInteractionProtocols();
	  while(e.hasMoreElements())
	    w.write(" " + e.nextElement());
	  w.write(" ) )");
	}

	if(ontology != null)
	  w.write(" ( " + ONTOLOGY + " " + ontology + " )");

	if(ownership != null)
	  w.write(" ( " + OWNERSHIP + " " + ownership + " )");

	if(DFState != null)
	  w.write(" ( " + DFSTATE + " " + DFState + " )");

	w.flush();
      }
      catch(IOException ioe) {
	ioe.printStackTrace();
      }

    }

  } // End of DFAgentDescriptor class


  public static class Constraint {

    public static final String DFDEPTH = ":df-depth";
    public static final String RESPREQ = ":resp-req";

    public static final String MAX = "Max";
    public static final String MIN = "Min";
    public static final String EXACTLY = "Exactly";

    private String name;
    private String fn;
    private int arg;

    public void setName(String s) {
      name = s;
    }

    public String getName() {
      return name;
    }

    public void setFn(String s) {
      fn = s;
    }

    public String getFn() {
      return fn;
    }

    public void setArg(int i) {
      arg = i;
    }

    public int getArg() {
      return arg;
    }

  }

  public static interface Action {

    public void setName(String name);
    public String getName();
    public void toText(Writer w);

  }

  public static class AMSAction implements Action {

    // These String constants are the names of the actions supported
    // by AMS agent
    public static final String AUTHENTICATE = "authenticate";
    public static final String REGISTERAGENT = "register-agent";
    public static final String DEREGISTERAGENT = "deregister-agent";
    public static final String MODIFYAGENT = "modify-agent";
    public static final String CREATEAGENT = "create-agent";
    public static final String KILLAGENT = "kill-agent";

    static final String ARGNAME = ":ams-description";

    private static Hashtable actions = new Hashtable(6, 1.0f);
    private String name;
    private AMSAgentDescriptor arg;


    public static AMSAction fromText(Reader r) throws ParseException, TokenMgrError {
      return AgentManagementOntology.parser.parseAMSAction(r);
    }

    public void setName(String s) {
      name = s;
    }

    public String getName() {
      return name;
    }

    public void setArg(AMSAgentDescriptor amsd) {
      arg = amsd;
    }

    public AMSAgentDescriptor getArg() {
      return arg;
    }

    public void toText(Writer w) {
      try {
	w.write("( " + name + " ");
	w.write("( " + ARGNAME + " ");
	arg.toText(w);
	w.write(" )");
	w.write(" )");
	w.flush();
      }
      catch(IOException ioe) {
	ioe.printStackTrace();
      }
    }

  } // End of AMSAction class

  public static class CreateAgentAction extends AMSAction {

    public static final String AGENTCODE = ":agent-code";
    public static final String AGENTPROPERTIES = ":agent-properties";

    public static final String CONTAINER = ":container";

    private String className = "jade.core.Agent";
    private Properties agentProperties = new Properties();

    public CreateAgentAction() {
      setName(CREATEAGENT);
    }

    public void setClassName(String cn) {
      className = cn;
    }

    public String getClassName() {
      return className;
    }

    public void addProperty(String name, String value) {
      agentProperties.put(name.toLowerCase(), value);
    }

    public String getProperty(String name) {
      return agentProperties.getProperty(name.toLowerCase());
    }

    public void removeProperty(String name) {
      agentProperties.remove(name.toLowerCase());
    }

    public void toText(Writer w) {
      try {
	w.write("( " + name + " ");
	w.write("( " + ARGNAME + " ");
	arg.toText(w);
	w.write(" )");
	w.write(" ( " + AGENTCODE + " " + className + " )");
	w.write(" ( " + AGENTPROPERTIES + " ");

	Enumeration propNames = agentProperties.propertyNames();
	while(propNames.hasMoreElements()) {
	  String name = (String)propNames.nextElement();
	  w.write(" ( " + name + " " + agentProperties.getProperty(name) + " ) ");
	}

	w.write(" ) ");
	w.write(" )");
	w.flush();
      }
      catch(IOException ioe) {
	ioe.printStackTrace();
      }
    }

  }

  public static class KillAgentAction extends AMSAction {

    public static final String AGENTNAME = ":agent-name";
    public static final String PASSWORD = ":password";

    private String agentName;
    private String password;

    public KillAgentAction() {
      setName(KILLAGENT);
    }

    public void setAgentName(String an) {
      agentName = an;
    }

    public String getAgentName() {
      return agentName;
    }

    public void setPassword(String pwd) {
      password = pwd;
    }

    public String getPassword() {
      return password;
    }

    public void toText(Writer w) {
      try {
	w.write("( " + name + " ");
	w.write("( " + AGENTNAME + " " + agentName + " )");
	if(password != null)
	  w.write("( " + PASSWORD + " " + password + " )");
	w.write(" )");
	w.flush();
      }
      catch(IOException ioe) {
	ioe.printStackTrace();
      }
    }

  }

  public static abstract class AMSEvent {

    public static final int NEWCONTAINER = 0;
    public static final int DEADCONTAINER = 1;
    public static final int NEWAGENT = 2;
    public static final int DEADAGENT = 3;

    protected static final String[] eventNames = { "new-container", "dead-container", "new-agent", "dead-agent" };

    private int kind;

    public static AMSEvent fromText(Reader r) throws ParseException, TokenMgrError {
      return AgentManagementOntology.parser.parseAMSEvent(r);
    }

    public abstract void toText(Writer w);

    public void setKind(int k) {
      kind = k;
    }

    public int getKind() {
      return kind;
    }

  }

  public static class AMSContainerEvent extends AMSEvent {

    private String containerName;

    public void toText(Writer w) {
      try {
	w.write("(" + eventNames[kind] + " " + containerName + " )\n");
	w.flush();
      }
      catch(IOException ioe) {
	ioe.printStackTrace();
      }
    }

    public void setContainerName(String cn) {
      containerName = cn;
    }

    public String getContainerName() {
      return containerName;
    }

  }

  public static class AMSAgentEvent extends AMSContainerEvent {

    private AMSAgentDescriptor agentDescriptor;

    public void toText(Writer w) {
      try {
	w.write("( ");
	w.write(eventNames[kind] + " ( :container " + containerName + " ) ");
	w.write("( " + AMSAction.ARGNAME + " ");
	agentDescriptor.toText(w);
	w.write(" )");
	w.write(" )");
	w.flush();
      }
      catch(IOException ioe) {
	ioe.printStackTrace();
      }
    }

    public void setAgentDescriptor(AMSAgentDescriptor amsd) {
      agentDescriptor = amsd;
    }

    public AMSAgentDescriptor getAgentDescriptor() {
      return agentDescriptor;
    }

  }

  public static class DFAction implements Action {

    // These String constants are the names of the actions supported
    // by DF agent
    public static final String REGISTER = "register";
    public static final String DEREGISTER = "deregister";
    public static final String MODIFY = "modify";
    public static final String SEARCH = "search";

    static final String ARGNAME = ":df-description";

    private static Hashtable actions = new Hashtable(4, 1.0f);
    private String name;
    private DFAgentDescriptor arg;


    public static DFAction fromText(Reader r) throws ParseException, TokenMgrError {
      return AgentManagementOntology.parser.parseDFAction(r);
    }

    public void setName(String s) {
      name = s;
    }

    public String getName() {
      return name;
    }

    public void setArg(DFAgentDescriptor dfd) {
      arg = dfd;
    }

    public DFAgentDescriptor getArg() {
      return arg;
    }

    public void toText(Writer w) {
      try {
	w.write("( " + name + " ");
	w.write("( " + ARGNAME + " ");
	arg.toText(w);
	w.write(" )");
	w.write(" )");
	w.flush();
      }
      catch(IOException ioe) {
	ioe.printStackTrace();
      }
    }

  } // End of DFAction class

  public static class DFSearchAction extends DFAction {

    private Vector constraints = new Vector();

    public void addConstraint(Constraint c) {
      constraints.addElement(c);
    }

    public void removeConstraints() {
      constraints.removeAllElements();
    }

    public Enumeration getConstraints() {
      return constraints.elements();
    }

    public void toText(Writer w) {
      try {
	Constraint c = null;
	w.write("( " + getName() + " ");
	w.write("( " + ARGNAME + " ");
	getArg().toText(w);
	w.write(" )");
	Enumeration e = getConstraints();
	while(e.hasMoreElements()) {
	  c = (Constraint) e.nextElement();
	  w.write("( ");
	  w.write(c.getName() + " " + c.getFn() + " " + c.getArg());
	  w.write(" )");
	}
	w.write(" )");
	w.flush();
      }
      catch(IOException ioe) {
	ioe.printStackTrace();
      }
    }

  } // End of DFSearchAction class

  public static class ACCAction implements Action {
    public static final String FORWARD = "forward";
    static final String ARGNAME = "";

    private static Hashtable actions = new Hashtable(1, 1.0f);
    private String name;
    private ACLMessage arg;


    public static ACCAction fromText(Reader r) throws ParseException, TokenMgrError {
      return AgentManagementOntology.parser.parseACCAction(r);
    }

    public void setName(String s) {
      name = s;
    }

    public String getName() {
      return name;
    }

    public void setArg(ACLMessage msg) {
      arg = msg;
    }

    public ACLMessage getArg() {
      return arg;
    }

    public void toText(Writer w) {
      try {
	w.write("( " + name + " ");
	arg.toText(w);
	w.write(" )");
	w.flush();
      }
      catch(IOException ioe) {
	ioe.printStackTrace();
      }
    }

  } // End of ACCAction class


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

  } // End of APLifeCycle class


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

  } // End of DomainLifeCycle class


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



  // Used as lock in Doubly Checked Locking
  private static Object lock = new Object();

  // The single instance of AgentManagementOntology
  private static AgentManagementOntology singleton = null;

  // A parser to convert strings describing 'fipa-agent-management' objects into Java objects
  private static AgentManagementParser parser = AgentManagementParser.create();

  // Private constructor: instantiate only through instance() method.
  private AgentManagementOntology() {

    // Fill in keyword tables -- When key == value an Hashtable is used as a Set

    PlatformProfile.keywords.put(PlatformProfile.NAME, PlatformProfile.NAME);
    PlatformProfile.keywords.put(PlatformProfile.IIOP, PlatformProfile.IIOP);
    PlatformProfile.keywords.put(PlatformProfile.DYNAMICREG, PlatformProfile.DYNAMICREG);
    PlatformProfile.keywords.put(PlatformProfile.MOBILITY, PlatformProfile.MOBILITY);
    PlatformProfile.keywords.put(PlatformProfile.OWNERSHIP, PlatformProfile.OWNERSHIP);
    PlatformProfile.keywords.put(PlatformProfile.CERTAUTH, PlatformProfile.CERTAUTH);
    PlatformProfile.keywords.put(PlatformProfile.DEFAULTDF, PlatformProfile.DEFAULTDF);

    ServiceDescriptor.keywords.put(ServiceDescriptor.NAME, ServiceDescriptor.NAME);
    ServiceDescriptor.keywords.put(ServiceDescriptor.TYPE, ServiceDescriptor.TYPE);
    ServiceDescriptor.keywords.put(ServiceDescriptor.ONTOLOGY, ServiceDescriptor.ONTOLOGY);
    ServiceDescriptor.keywords.put(ServiceDescriptor.FIXEDPROPS, ServiceDescriptor.FIXEDPROPS);
    ServiceDescriptor.keywords.put(ServiceDescriptor.NEGOTIABLEPROPS, ServiceDescriptor.NEGOTIABLEPROPS);
    ServiceDescriptor.keywords.put(ServiceDescriptor.COMMUNICATIONPROPS, ServiceDescriptor.COMMUNICATIONPROPS);

    AMSAgentDescriptor.keywords.put(AMSAgentDescriptor.NAME, new Integer(0));
    AMSAgentDescriptor.keywords.put(AMSAgentDescriptor.ADDRESS, new Integer(1));
    AMSAgentDescriptor.keywords.put(AMSAgentDescriptor.SIGNATURE, new Integer(2));
    AMSAgentDescriptor.keywords.put(AMSAgentDescriptor.APSTATE, new Integer(3));
    AMSAgentDescriptor.keywords.put(AMSAgentDescriptor.DELEGATE, new Integer(4));
    AMSAgentDescriptor.keywords.put(AMSAgentDescriptor.FORWARD, new Integer(5));
    AMSAgentDescriptor.keywords.put(AMSAgentDescriptor.OWNERSHIP, new Integer(6));

    DFAgentDescriptor.keywords.put(DFAgentDescriptor.NAME, new Integer(0));
    DFAgentDescriptor.keywords.put(DFAgentDescriptor.ADDRESS, new Integer(1));
    DFAgentDescriptor.keywords.put(DFAgentDescriptor.SERVICES, new Integer(2));
    DFAgentDescriptor.keywords.put(DFAgentDescriptor.TYPE, new Integer(3));
    DFAgentDescriptor.keywords.put(DFAgentDescriptor.PROTOCOLS, new Integer(4));
    DFAgentDescriptor.keywords.put(DFAgentDescriptor.ONTOLOGY, new Integer(5));
    DFAgentDescriptor.keywords.put(DFAgentDescriptor.OWNERSHIP, new Integer(6));
    DFAgentDescriptor.keywords.put(DFAgentDescriptor.DFSTATE, new Integer(7));



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

    AMSAction.actions.put(AMSAction.AUTHENTICATE, new Integer(0));
    AMSAction.actions.put(AMSAction.REGISTERAGENT, new Integer(1));
    AMSAction.actions.put(AMSAction.DEREGISTERAGENT, new Integer(2));
    AMSAction.actions.put(AMSAction.MODIFYAGENT, new Integer(3));
    AMSAction.actions.put(AMSAction.CREATEAGENT, new Integer(4));
    AMSAction.actions.put(AMSAction.KILLAGENT, new Integer(5));

    DFAction.actions.put(DFAction.REGISTER, new Integer(0));
    DFAction.actions.put(DFAction.DEREGISTER, new Integer(1));
    DFAction.actions.put(DFAction.MODIFY, new Integer(2));
    DFAction.actions.put(DFAction.SEARCH, new Integer(3));


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
  // up in 'keywords' Hashtable of AMSAction and AMSAgentDescriptor
  // classes
  private static final boolean AMSMandatoryAttributes[][] = {
    { true,  false, true,  false, false, false, false }, // 'authenticate' action
    { true,  true,  false, true,  false, false, false }, // 'register-agent' action
    { true,  false, false, false, false, false, false }, // 'deregister-agent' action
    { true,  false, false, false, false, false, false }  // 'modify-agent' action
  };


  // Table of mandatory attributes for DF actions. First index is the
  // action number and second index is attribute number. Those two
  // numbers can be obtained from action and attribute names, looking
  // up in 'keywords' Hashtable of DFAction and DFAgentDescriptor
  // classes
  private static final boolean DFMandatoryAttributes[][] = {
    { true,  true,  false, true,  false, false, true,  true  }, // 'register' action
    { true,  false, false, false, false, false, false, false }, // 'deregister' action
    { true,  false, false, false, false, false, false, false }, // 'modify' action
    { false, false, false, false, false, false, false, false }  // 'search' action
  };


  // Static method to obtain an handle to the single instance of
  // AgentManagementOntology class
  public static final AgentManagementOntology instance() {

    // Double-Checked Locking initialization
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

  // Check that 'keyword' is a valid 'FIPA-DF-description' keyword
  public boolean isValidDFADKeyword(String keyword) {
    return DFAgentDescriptor.keywords.containsKey(keyword);
  }

  // Check that 'keyword' is a valid 'FIPA-AP-description' keyword
  public boolean isValidPPKeyword(String keyword) {
    return PlatformProfile.keywords.containsKey(keyword);
  }

  // Check that 'keyword' is a valid 'FIPA-Service-Desc-Item' keyword
  public boolean isValidSDKeyword(String keyword) {
    return ServiceDescriptor.keywords.containsKey(keyword);
  }

  // Check that 'keyword' is a valid 'FIPA-AMS-description' keyword
  public boolean isValidAMSADKeyword(String keyword) {
    return AMSAgentDescriptor.keywords.containsKey(keyword);
  }

  // Check that 'message' is a valid 'AgentManagementException' error
  // message
  public boolean isValidException(String message) {
    return Exception.JavaExceptions.containsKey(message);
  }

  // Tell whether 'attributeName' is a mandatory attribute for
  // 'actionName' AMS action
  public boolean isMandatoryForAMS(String actionName, String attributeName) {
    Integer actionIndex = (Integer)AMSAction.actions.get(actionName);
    Integer attributeIndex = (Integer)AMSAgentDescriptor.keywords.get(attributeName);
    return AMSMandatoryAttributes[actionIndex.intValue()][attributeIndex.intValue()];
  }

  // Tell whether 'attributeName' is a mandatory attribute for
  // 'actionName' DF action
  public boolean isMandatoryForDF(String actionName, String attributeName) {
    Integer actionIndex = (Integer)DFAction.actions.get(actionName);
    Integer attributeIndex = (Integer)DFAgentDescriptor.keywords.get(attributeName);
    return DFMandatoryAttributes[actionIndex.intValue()][attributeIndex.intValue()];
  }


  // Lookup methods to convert between different data representations

  // Return the Java exception corresponding to a given message
  public FIPAException getException(String message) {
    FIPAException fe = (FIPAException)Exception.JavaExceptions.get(message);
    fe.fillInStackTrace();
    return fe;
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
