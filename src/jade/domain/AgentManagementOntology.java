/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop multi-agent systems in compliance with the FIPA specifications.
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


package jade.domain;

import java.io.IOException;
import java.io.Serializable;
import java.io.Reader;
import java.io.Writer;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Iterator;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.onto.DefaultOntology;

/**
   Holds an OO model of standard <code>fipa-agent-management</code>
   ontology. This class contains all elements of
   <code>fipa-agent-management</code> ontology as inner classes. User
   defined agents can access this class to create Java objects
   representing ontological entities and build message content out of
   a <code>String</code> representation of these objects.  Since
   <em>ACL</em> message content is a raw <code>String</code>, all
   <code>AgentManagementOntology</code> inner classes hava a pair of
   methods to perform bidirectional conversions to/from character
   stream objects: a <code>fromText(Reader r)</code> static
   <em>Factory Method</code> builds a new ontology object out of a
   <code>java.io.Reader</code> object, whereas <code>toText(Writer
   w)</code> method writes an existing object onto a suitable
   <code>java.io.Writer</code> object. These two methods work just
   like <em>Java Serialization API</em>, letting programmers deal with
   objects all the time and converting to an external format only for
   storage or transmission.

   Every inner class is a simple collection of attributes, with
   <code>public</code> methods to read and write them; if the class
   has an attribute named <code>attr</code> of type
   <code>attrType</code>, two cases are possible:
   <ol>
   <li> The attribute has a single value; then it can be read with
   <code>attrType getAttr()</code> and written with <code>void
   setAttr(attrType a)</code>; every call to <code>setAttr()</code>
   overwrites any previous value of the attribute.
   <li> The attribute has a list of values; then there is a <code>void
   addAttr(attrType a)</code> method to insert a new value and a
   <code>void removeAttrs()</code> to remove all the values (the list
   becomes empty). Reading is performed by a <code>Enumeration
   getAttrs()</code> method; then the programmer must iterate along
   the <code>Enumeration</code> and cast its elements to the
   appropriate type.
   </ol>
  
  Javadoc documentation for the file
  @author Giovanni Rimassa - Universita` di Parma
  @version $Date$ $Revision$

 */
public class AgentManagementOntology extends DefaultOntology {

  /**
     Models service descriptors for DF data base.
     Represents a <code>FIPA-Service-Desc</code> object of
     <code>fipa-agent-management</code> ontology as a Java object, to
     avoid scattering parsing code throughout applications.
  */
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

    /**
       Reads an object from a stream. This static <em>Factory
       Method</em> recovers a <code>service-descriptor</code> object
       from a readable stream.
       @param r The <code>Reader</code> containing a string
       representation for this object.
       @return A new <code>ServiceDescriptor</code> object,
       initialized from stream data.
     */
    public static ServiceDescriptor fromText(Reader r) throws ParseException, TokenMgrError {
      synchronized(parserLock) {
	return AgentManagementOntology.parser.parseServiceDescriptor(r);
      }
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

    /**
       Writes an object to a stream. This method writes a
       <code>service-descriptor</code> object on a writable stream.
       @param w The <code>Writer</code> object onto which a string
       representation of this object will be written.
    */
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



  /**
     Model of AMS agent descriptors. This class provides platform-level
     support to <em>AMS</em> agent, holding all informations needed by
     <code>AMS-agent-description</code> objects in
     <code>fipa-agent-management</code> ontology.
  */
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
    static final String SNAME = ":sniffer-name";
    static final String AGLIST = ":agent-list";

    // Table of allowed keywords in 'FIPA-AMS-description' objects
    private static Hashtable keywords = new Hashtable(7, 1.0f);

    private String name;
    private String address;
    private String signature;
    private int APState;
    private String delegateAgentName;
    private String forwardAddress;
    private String ownership;

    /**
       Reads an object from a stream. This static <em>Factory
       Method</em> recovers a <code>AMS-agent-description</code> object
       from a readable stream.
       @param r The <code>Reader</code> containing a string
       representation for this object.
       @return A new <code>AMSAgentDescriptor</code> object,
       initialized from stream data.
     */
    public static AMSAgentDescriptor fromText(Reader r) throws ParseException, TokenMgrError {
      synchronized(parserLock) {
	return AgentManagementOntology.parser.parseAMSDescriptor(r);
      }
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

    /**
       Writes an object to a stream. This method writes a
       <code>AMS-agent-descriptor</code> object on a writable stream.
       @param w The <code>Writer</code> object onto which a string
       representation of this object will be written.
    */
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


  /**
    Models a DF agent descriptor.  This class provides platform-level
    support to <em>DF</em> agent, holding all informations needed by
    <code>DF-agent-description</code> objects in
    <code>fipa-agent-management</code> ontology.
  */
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

    /**
       Reads an object from a stream. This static <em>Factory
       Method</em> recovers a <code>DF-agent-descriptor</code> object
       from a readable stream.
       @param r The <code>Reader</code> containing a string
       representation for this object.
       @return A new <code>DFAgentDescriptor</code> object,
       initialized from stream data.
     */
    public static DFAgentDescriptor fromText(Reader r) throws ParseException, TokenMgrError {
      synchronized(parserLock) {
      return AgentManagementOntology.parser.parseDFDescriptor(r);
      }
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

    public void addAgentService(ServiceDescriptor sd) {
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

    public String getDFState() {
      return DFState;
    }

    /**
       Writes an object to a stream. This method writes a
       <code>DF-agent-descriptor</code> object on a writable stream.
       @param w The <code>Writer</code> object onto which a string
       representation of this object will be written.
    */
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

  /**
     Models a DF search constraint. This class is used as a
     <code>constraint</code> within parameters of a
     <code>search</code> <em>DF</em> action.
  */
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

  } // End of Constraint class


  /**
    Models the result set of a <code>search</code> <em>DF</em>
    action. This class is used to hold a set of
    <code>DFAgentDescriptor</code> objects, that is the result of a
    previous query to a <em>DF</em> agent. When such a query succeeds,
    a <code>DFSearchResult</code> object allows both access to agent
    descriptor by name and iteration through the result set. If the
    query failed, a <code>FIPAException</code> is thrown as soon as
    data access is attempted.
  */
  public static class DFSearchResult {

    private Hashtable results = new Hashtable();

    // This exception object records last search outcome. When it is
    // 'null', all went OK.
    private FIPAException searchOutcome = null;

    /**
       Reads an object from a stream. This static <em>Factory
       Method</em> recovers a <code>DFSearchResult</code> object
       from a readable stream.
       @param r The <code>Reader</code> containing a string
       representation for this object.
       @return A new <code>DFSearchResult</code> object,
       initialized from stream data.
     */
    public static DFSearchResult fromText(Reader r) throws ParseException, TokenMgrError {
      synchronized(parserLock) {
      AgentManagementOntology o = AgentManagementOntology.instance();
      DFSearchResult dfsr;
      try {
	dfsr = parser.parseSearchResult(r);
      }
      catch(ParseException pe) {
	dfsr = new AgentManagementOntology.DFSearchResult();
	dfsr.searchOutcome = o.getException(Exception.UNRECOGNIZEDVALUE);
	throw pe;
      }
      catch(TokenMgrError tme) {
	dfsr = new AgentManagementOntology.DFSearchResult();
	dfsr.searchOutcome = o.getException(Exception.UNRECOGNIZEDVALUE);
	throw tme;
      }
      return dfsr;
      }
    }

    public void setException(FIPAException fe) {
      searchOutcome = fe;
    }

    /**
       Writes an object to a stream. This method writes a
       <code>DFSearchResult</code> object on a writable stream.
       @param w The <code>Writer</code> object onto which a string
       representation of this object will be written.
    */
    public void toText(Writer w) throws FIPAException {
      if(searchOutcome != null)
	throw searchOutcome;
      try {
	// w.write("( result ");
	w.write("(");
	Enumeration e = results.elements();
	while(e.hasMoreElements()) {
	  w.write("(" + DFAction.ARGNAME + " ");
	  DFAgentDescriptor current = (DFAgentDescriptor)e.nextElement();
	  current.toText(w);
	  w.write(" )");
	}
	w.write(")");
	// w.write(")");
	w.flush();
      }
      catch(IOException ioe) {
	ioe.printStackTrace();
      }
    }

    public void put(String agentName, DFAgentDescriptor dfd) {
      results.put(agentName, dfd);
    }

    public DFAgentDescriptor get(String agentName) throws FIPAException {
      if(searchOutcome != null)
	throw searchOutcome;
      return (DFAgentDescriptor)results.get(agentName);
    }

    public Enumeration elements() throws FIPAException {
      if(searchOutcome != null)
	throw searchOutcome;
      return results.elements();
    }

  } // End of DFSearchResult class

  /**
     Generic property-based interface. This interface allows to
     describe <em>JADE</em> specific concepts (such as agent
     containers) as string proerties.
  */
  public static interface PropertyContainer {
    void addProperty(String name, String value);
    String getProperty(String name);
    void removeProperty(String name);
  }

  /**
    Generic interface for actions. This interface is implemented by
    all classes representing actions described in
    <code>fipa-agent-management</code> ontology.
  */
  public static interface Action {
    void setName(String name);
    String getName();
    void setActor(String name);
    String getActor();
    void toText(Writer w);

  }

  /**
    Model of <em>AMS</em> actions. This class represent all the
    actions that an <em>AMS</em> agent can perform. Since most of
    these actions have the same parameters, there is no need to have a
    separate subclass for each one of them. Only peculiar actions such
    as <code>create-agent</code> will be represented as a specific
    subclass.
  */
  public static class AMSAction implements Action {

    // These String constants are the names of the actions supported
    // by AMS agent
    public static final String AUTHENTICATE = "authenticate";
    public static final String REGISTERAGENT = "register-agent";
    public static final String DEREGISTERAGENT = "deregister-agent";
    public static final String MODIFYAGENT = "modify-agent";
    public static final String QUERYPLATFORMPROFILE = "query-platform-profile";
    public static final String SEARCHAGENT = "search-agent";
    public static final String KILLCONTAINER = "kill-container";
    public static final String CREATEAGENT = "create-agent";
    public static final String KILLAGENT = "kill-agent";
    public static final String SNIFFAGENTON = "sniff-agent-on";
    public static final String SNIFFAGENTOFF = "sniff-agent-off";

    static final String ARGNAME = ":ams-description";

    private static Hashtable actions = new Hashtable(11, 1.0f);
    protected String name;
    protected String actor;
    protected AMSAgentDescriptor arg;

    /**
       Reads an object from a stream. This static <em>Factory
       Method</em> recovers a <code>AMSAction</code> object
       from a readable stream.
       @param r The <code>Reader</code> containing a string
       representation for this object.
       @return A new <code>AMSAction</code> object,
       initialized from stream data.
     */
    public static AMSAction fromText(Reader r) throws ParseException, TokenMgrError {
      synchronized(parserLock) {
	return AgentManagementOntology.parser.parseAMSAction(r);
      }
    }

    public AMSAction() {
      actor = "ams";
    }

    public void setName(String s) {
      name = s;
    }

    public String getName() {
      return name;
    }

    public void setActor(String name) {
      actor = name;
    }

    public String getActor() {
      return actor;
    }

    public void setArg(AMSAgentDescriptor amsd) {
      arg = amsd;
    }

    public AMSAgentDescriptor getArg() {
      return arg;
    }

    /**
       Writes an object to a stream. This method writes a
       <code>AMSAction</code> object on a writable stream.
       @param w The <code>Writer</code> object onto which a string
       representation of this object will be written.
    */
    public void toText(Writer w) {
      try {
	w.write("( action " + actor + " ");
	w.write("( " + name + " ");
	w.write("( " + ARGNAME + " ");
	arg.toText(w);
	w.write(" )");
	w.write(" )");
	w.write(" )");
	w.flush();
      }
      catch(IOException ioe) {
	ioe.printStackTrace();
      }
    }

  } // End of AMSAction class

  /**
   Models <code>kill-container</code> <em>AMS</em> action.
  */
  public static class KillContainerAction extends AMSAction {

    private String containerName;

    public KillContainerAction() {
      setName(KILLCONTAINER);
    }

    public void setContainerName(String cn) {
      containerName = cn;
    }

    public String getContainerName() {
      return containerName;
    }

    /**
       Writes an object to a stream. This method writes a
       <code>KillContainerAction</code> object on a writable stream.
       @param w The <code>Writer</code> object onto which a string
       representation of this object will be written.
    */
    public void toText(Writer w) {
      try {
	w.write("( action " + actor + " ");
	w.write("( " + name + " ");
	w.write(containerName + " )");
	w.write(" )");
	w.flush();
      }
      catch(IOException ioe) {
	ioe.printStackTrace();
      }

    }

  } // End of KillContainerAction class

  /**
    Models <code>query-platform-profile</code> <em>AMS</em> action.
  */
  public static class QueryPlatformProfileAction extends AMSAction {

    public void toText(Writer w) {
      try {
	w.write("( action " + getActor() + " ");
	w.write("( " + name + " )");
	w.write(")");
	w.flush();
      }
      catch(IOException ioe) {
	ioe.printStackTrace();
      }
    }

  } // End of QueryPlatformProfileAction

  /**
    Models <code>create-agent</code> <em>AMS</em> action.
  */
  public static class CreateAgentAction extends AMSAction implements PropertyContainer {

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

    /**
       Writes an object to a stream. This method writes a
       <code>CreateAgentAction</code> object on a writable stream.
       @param w The <code>Writer</code> object onto which a string
       representation of this object will be written.
    */
    public void toText(Writer w) {
      try {
	w.write("( action " + getActor() + " ");
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
	w.write(" )");
	w.flush();
      }
      catch(IOException ioe) {
	ioe.printStackTrace();
      }
    }

  }


  public static class SniffAgentOnAction extends AMSAction {

    public static final String SNIFFERNAME = ":sniffer-name";
    public static final String AGENTLIST = ":agent-list";

    private String mySnifferName;
    
    private Map myAgentList = new HashMap();

    public SniffAgentOnAction() {
      setName(SNIFFAGENTON);
    }

    public void setSnifferName( String sn ) {
      mySnifferName = sn;
    }
 
    public String getSnifferName() {
      return mySnifferName; 
    }

    public Iterator getAgents() {
      Set mySet = myAgentList.keySet();
      Iterator myIt = mySet.iterator();
      return myIt;
    }

    public void addSniffedAgent(String ag) {
      myAgentList.put(ag,mySnifferName); // Overwrites old value, if present
    }

    public void put(String ag, String sn) {
      myAgentList.put(ag,sn); // Overwrites old value, if present
    }

    public String getSniffer( String i ) {
      return (String)myAgentList.get(i);
    }

    public int getListSize() {
      return myAgentList.size();
    }	

    public Map getEntireList() { 
      return myAgentList;
    }

    /**
       Writes an object to a stream. This method writes a
       <code>CreateAgentAction</code> object on a writable stream.
       @param w The <code>Writer</code> object onto which a string
       representation of this object will be written.
    */
    
    public void toText(Writer w) {
    	
      Iterator agl = this.getAgents();
    	
      try {
	w.write("( action " + getActor() + " ");
	w.write("( " + getName() + " ");
	w.write("( " + SNIFFERNAME + " " + mySnifferName + " ");
	w.write(" " + AGENTLIST + " { ");
	while ( agl.hasNext() )
	  w.write((String)agl.next() + " ");
	w.write(" }");					
	w.write(" ) ");
	w.write(" )");
	w.write(" )");
	w.flush();
      }
      catch(IOException ioe) {
	ioe.printStackTrace();
      }
    }

  }


  public static class SniffAgentOffAction extends AMSAction {

    public static final String SNIFFERNAME = ":sniffer-name";
    public static final String AGENTLIST = ":agent-list";

    private String mySnifferName;
    
    private Map myAgentList = new HashMap();

    public SniffAgentOffAction() {
      setName(SNIFFAGENTOFF);
    }

    public void setSnifferName( String sn ) {
      mySnifferName = sn;
    }
	
    public String getSnifferName() {
      return mySnifferName;
    } 

    public Iterator getAgents() {
      Set mySet = myAgentList.keySet();
      Iterator myIt = mySet.iterator();
      return myIt;
    }

    public void addNotSniffedAgent(String ag) {
      myAgentList.put(ag,mySnifferName);
    }

    public void put(String ag, String sn) {
      myAgentList.put(ag,sn);
    }

    public String getNotSniffedAgent( String i ) {
      return (String)myAgentList.get(i);
    }

    public int getListSize() {
      return myAgentList.size();
    }
		
    public Map getEntireList() { 
      return myAgentList;
    }

    /**
       Writes an object to a stream. This method writes a
       <code>CreateAgentAction</code> object on a writable stream.
       @param w The <code>Writer</code> object onto which a string
       representation of this object will be written.
    */
    
    public void toText(Writer w) {
    	
      Iterator agl = this.getAgents();
    	    	
      try {
	w.write("( action " + getActor() + " ");
	w.write("( " + getName() + " ");
	w.write("( " + SNIFFERNAME + " " + mySnifferName + " ");
	w.write(" " + AGENTLIST + " { ");
	while ( agl.hasNext() )
	  w.write((String)agl.next() + " ");
	w.write(" }");
	w.write(" ) ");
	w.write(" )");
	w.write(" )");
	w.flush();
      }
      catch(IOException ioe) {
	ioe.printStackTrace();
      }
    }

  }


  /**
    Models <code>kill-agent</code> <em>AMS</em> action.
  */
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

    /**
       Writes an object to a stream. This method writes a
       <code>KillAgentAction</code> object on a writable stream.
       @param w The <code>Writer</code> object onto which a string
       representation of this object will be written.
    */
    public void toText(Writer w) {
      try {
	w.write("( action " + getActor() + " ");
	w.write("( " + name + " ");
	w.write("( " + AGENTNAME + " " + agentName + " )");
	if(password != null)
	  w.write("( " + PASSWORD + " " + password + " )");
	w.write(" )");
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
    public static final int MOVEDAGENT = 4;

    protected static final String[] eventNames = { "new-container", "dead-container", "new-agent", "dead-agent", "moved-agent" };

    private int kind;

    public static AMSEvent fromText(Reader r) throws ParseException, TokenMgrError {
      synchronized(parserLock) {
      return AgentManagementOntology.parser.parseAMSEvent(r);
      }
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
	w.write("(" + eventNames[getKind()] + " " + containerName + " )\n");
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

  public static class AMSAgentEvent extends AMSContainerEvent implements PropertyContainer {

    protected AMSAgentDescriptor agentDescriptor;

    public void toText(Writer w) {
      try {
	w.write("( ");
	w.write(eventNames[getKind()] + " ( :agent-properties ");
	w.write(" ( :container " + getContainerName() + " ) ");
	w.write(" ) ");
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

    public void addProperty(String name, String value) {
      if(name.equalsIgnoreCase(":container")) {
	setContainerName(value);
      }
    }

    public String getProperty(String name) {
      if(name.equalsIgnoreCase(":container"))
	return getContainerName();
      else
	return null;
    }

    public void removeProperty(String name) {
      if(name.equalsIgnoreCase(":container"))
	setContainerName(null);
    }

    public void setAgentDescriptor(AMSAgentDescriptor amsd) {
      agentDescriptor = amsd;
    }

    public AMSAgentDescriptor getAgentDescriptor() {
      return agentDescriptor;
    }

  }

  public static class AMSMotionEvent extends AMSAgentEvent {

    private String src;
    private String dest;


    public void addProperty(String name, String value) {
      if(name.equalsIgnoreCase(":from")) {
	setSrc(value);
      }
      else if(name.equalsIgnoreCase(":to")) {
	setDest(value);
      }
      else super.addProperty(name, value);
    }

    public String getProperty(String name) {
      if(name.equalsIgnoreCase(":from"))
	return getSrc();
      else if(name.equalsIgnoreCase(":to"))
	return getDest();
      else
	return super.getProperty(name);
    }

    public void removeProperty(String name) {
      if(name.equalsIgnoreCase(":from"))
	setSrc(null);
      else if(name.equalsIgnoreCase(":to"))
	setDest(null);
      else
        super.removeProperty(name);
    }

    public void toText(Writer w) {
      try {
	w.write("( ");
	w.write(eventNames[getKind()] + " ( :agent-properties ");
	w.write(" ( :from " + getSrc() + " ) ");
	w.write(" ( :to " + getDest() + " ) ");
	w.write(" ) ");
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

    public void setSrc(String s) {
      src = s;
    }

    public String getSrc() {
      return src;
    }

    public void setDest(String d) {
      dest = d;
    }

    public String getDest() {
      return dest;
    }

  }

  /**
    Model of <em>DF</em> actions. This class represent all the
    actions that a <em>DF</em> agent can perform. Since most of
    these actions have the same parameters, there is no need to have a
    separate subclass for each one of them. Only peculiar actions such
    as <code>search</code> will be represented as a specific
    subclass.
  */
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
    private String actor;
    private DFAgentDescriptor arg;

    /**
       Reads an object from a stream. This static <em>Factory
       Method</em> recovers a <code>DFAction</code> object
       from a readable stream.
       @param r The <code>Reader</code> containing a string
       representation for this object.
       @return A new <code>DFAction</code> object,
       initialized from stream data.
     */
    public static DFAction fromText(Reader r) throws ParseException, TokenMgrError {
      synchronized(parserLock) {
      return AgentManagementOntology.parser.parseDFAction(r);
      }
    }

    public void setName(String s) {
      name = s;
    }

    public String getName() {
      return name;
    }

    public void setActor(String name) {
      actor = name;
    }

    public String getActor() {
      return actor;
    }

    public void setArg(DFAgentDescriptor dfd) {
      arg = dfd;
    }

    public DFAgentDescriptor getArg() {
      return arg;
    }

    /**
       Writes an object to a stream. This method writes a
       <code>DFAction</code> object on a writable stream.
       @param w The <code>Writer</code> object onto which a string
       representation of this object will be written.
    */
    public void toText(Writer w) {
      try {
	w.write("( action " + actor + " ");
	w.write("( " + name + " ");
	w.write("( " + ARGNAME + " ");
	arg.toText(w);
	w.write(" )");
	w.write(" )");
	w.write(" )");
	w.flush();
      }
      catch(IOException ioe) {
	ioe.printStackTrace();
      }
    }

  } // End of DFAction class

  /**
   Models <em>DF</em> <code>search</code> action.
  */
  public static class DFSearchAction extends DFAction {

    private Vector constraints = new Vector();

    public DFSearchAction() {
      super();
      super.setName(super.SEARCH);
    }

    public void addConstraint(Constraint c) {
      constraints.addElement(c);
    }

    public void removeConstraints() {
      constraints.removeAllElements();
    }

    public Enumeration getConstraints() {
      return constraints.elements();
    }

    /**
       Writes an object to a stream. This method writes a
       <code>DFSearchAction</code> object on a writable stream.
       @param w The <code>Writer</code> object onto which a string
       representation of this object will be written.
    */
    public void toText(Writer w) {
      try {
	Constraint c = null;
	w.write("( action " + getActor() + " ");
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
	w.write(" )");
	w.flush();
      }
      catch(IOException ioe) {
	ioe.printStackTrace();
      }
    }

  } // End of DFSearchAction class


  /**
    Model of <em>ACC</em> actions. This class represent all the
    actions that an <em>ACC</em> agent can perform. Since most of
    these actions have the same parameters, there is no need to have a
    separate subclass for each one of them.
  */
  public static class ACCAction implements Action {
    public static final String FORWARD = "forward";
    static final String ARGNAME = "";

    private static Hashtable actions = new Hashtable(1, 1.0f);
    private String name;
    private String actor;
    private ACLMessage arg;

    public ACCAction() {
      actor = "acc";
    }

    /**
       Reads an object from a stream. This static <em>Factory
       Method</em> recovers a <code>ACCAction</code> object
       from a readable stream.
       @param r The <code>Reader</code> containing a string
       representation for this object.
       @return A new <code>ACCAction</code> object,
       initialized from stream data.
     */
    public static ACCAction fromText(Reader r) throws ParseException, TokenMgrError {
      synchronized(parserLock) {
        return AgentManagementOntology.parser.parseACCAction(r);
      }
    }

    public void setName(String s) {
      name = s;
    }

    public String getName() {
      return name;
    }

    public void setActor(String name) {
      actor = name;
    }

    public String getActor() {
      return actor;
    }

    public void setArg(ACLMessage msg) {
      arg = msg;
    }

    public ACLMessage getArg() {
      return arg;
    }

    /**
       Writes an object to a stream. This method writes a
       <code>ACCAction</code> object on a writable stream.
       @param w The <code>Writer</code> object onto which a string
       representation of this object will be written.
    */
    public void toText(Writer w) {
      try {
	w.write("( action " + actor + " ");
	w.write("( " + name + " ");
	arg.toText(w);
	w.write(" )");
	w.write(" )");
	w.flush();
      }
      catch(IOException ioe) {
	ioe.printStackTrace();
      }
    }

  } // End of ACCAction class


  /**
     This class contains string constants for Agent Platform Life
     Cycle states.
  */
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


  /**
     This class contains string constants for Domain Life Cycle
     states.
  */
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

  /**
     This class represents the Agent Platform Profile.
  */
  public static class PlatformProfile {

    // These String constants are the keywords in
    // 'ap-profile' objects.
    static final String NAME = ":platform-name";
    static final String IIOP = ":iiop-url";
    static final String DYNAMICREG = ":dynamic-registration";
    static final String MOBILITY = ":mobility";
    static final String OWNERSHIP = ":ownership";
    static final String CERTAUTH = ":certification-authority";
    static final String FIPAVERSION = ":fipa-man-compliance";

    public static final String MAIN_CONTAINER_NAME = "Front-End";
    public static final String AUX_CONTAINER_NAME = "Container-";

    private static final String APPROFILE = ":ap-profile";

    // Table of allowed keywords in 'fipa-man-platform-profile'
    // objects
    private static Hashtable keywords = new Hashtable(7, 1.0f);

    private String platformName;
    private String iiopURL;
    private String dynReg;
    private String mobility;
    private String ownership;
    private String certAuth;
    private String fipaVersion;

    /**
     Default constructor.
     */
    public PlatformProfile() {
    }

    /**
     This <em>Factory Method</em> reads an <code>ap-profile</code>
     object from a stream.
    */
    public static PlatformProfile fromText(Reader r) throws ParseException, TokenMgrError {
      synchronized(parserLock) {
	return AgentManagementOntology.parser.parsePlatformProfile(r);
      }
    }

    /**
     Write <code>:plaform-name</code> slot.
     @param pn The new slot value.
     */
    public void setPlatformName(String pn) {
      platformName = pn;
    }

    /**
     Read <code>:platform-name</code> slot.
     @return The current slot value.
     */
    public String getPlatformName() {
      return platformName;
    }

    /**
     Write <code>:iiop-url</code> slot.
     @param iu The new slot value.
     */
    public void setIiopURL(String iu) {
      iiopURL = iu;
    }

    /**
     Read <code>:iiop-url</code> slot.
     @return The current slot value.
     */
    public String getIiopURL() {
      return iiopURL;
    }

    /**
     Write <code>:dynamic-registration</code> slot.
     @param yn The new slot value. Must be either <em>"yes"</em> or
     <em>"no"</em>
     */
    public void setDynReg(String yn) {
      dynReg = yn;
    }

    /**
     Read <code>:dynamic-registration</code> slot.
     @return The current slot value.
     */
    public String getDynReg() {
      return dynReg;
    }

    /**
     Write <code>:mobility</code> slot.
     @param yn The new slot value. Must be either <em>"yes"</em> or
     <em>"no:"</em>
     */
    public void setMobility(String yn) {
      mobility = yn;
    }

    /**
     Read <code>:mobility</code> slot.
     @return The current slot value.
     */
    public String getMobility() {
      return mobility;
    }

    /**
     Write <code>:ownership</code> slot.
     @param o The new slot value.
     */
    public void setOwnership(String o) {
      ownership = o;
    }

    /**
     Read <code>:ownership</code> slot.
     @return The current slot value.
     */
    public String getOwnership() {
      return ownership;
    }

    /**
     Write <code>:certification-authority</code> slot.
     @param a The new slot value.
     */
    public void setCertificationAuthority(String a) {
      certAuth = a;
    }

    /**
     Read <code>:certification-authority</code> slot.
     @return The current slot value.
     */
    public String getCertificationAuthority() {
      return certAuth;
    }

    /**
     Write <code>:fipa-version</code> slot.
     @param fv The new slot value.
     */
    public void setFipaVersion(String fv) {
      fipaVersion = fv;
    }

    /**
     Read <code>:fipa-version</code> slot.
     @return The current slot value.
     */
    public String getFipaVersion() {
      return fipaVersion;
    }

    /**
     Writes an <code>ap-profile</code> on a stream.
     */
    public void toText(Writer w) {
      try {
	w.write(" ( " + APPROFILE);
	w.write(" ( " + NAME + " " + platformName + " ) ");
	w.write(" ( " + IIOP + " " + iiopURL + " ) ");
	w.write(" ( " + DYNAMICREG + " " + dynReg + " ) ");
	w.write(" ( " + MOBILITY + " " + mobility + " ) ");
	w.write(" ( " + OWNERSHIP + " " + ownership + " ) ");
	w.write(" ( " + CERTAUTH + " " + certAuth + " ) ");
	w.write(" ( " + FIPAVERSION + " " + fipaVersion + " ) ");
	w.write(" )");
	w.flush();
      }
      catch(IOException ioe) {
	ioe.printStackTrace();
      }
    }

  } // End of PlatformProfile class


  static class ServiceTypes {

    // These String constants are the names of FIPA special services
    static final String FIPADF = "fipa-df";
    static final String FIPAAMS = "fipa-ams";
    static final String FIPAACC = "fipa-acc";
    static final String FIPAAGENT = "fipa-agent";

    // Utility class; can't be instantiated
    private ServiceTypes() {
    }

  } // End of ServiceTypes class

  /**
     This class contains string constants for <em><b>FIPA</b></em>
     standard exception names.
  */
  public static class Exception {

    // These String constants are the names of all
    // 'fipa-management-exception' objects.
    static final String MISSINGATTRIBUTE = "missing-attribute";
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
    static final String FAILEDMANACTION = "failed-management-action";

    // Mapping of messages in 'fipa-man-ams-exception'
    // objects to Java exception objects
    private static Hashtable JavaExceptions = new Hashtable(14, 1.0f);

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
  private static Object parserLock = new Object(); 


  static {
    // Fill in keyword tables -- When key == value an Hashtable is used as a Set

    PlatformProfile.keywords.put(PlatformProfile.NAME, PlatformProfile.NAME);
    PlatformProfile.keywords.put(PlatformProfile.IIOP, PlatformProfile.IIOP);
    PlatformProfile.keywords.put(PlatformProfile.DYNAMICREG, PlatformProfile.DYNAMICREG);
    PlatformProfile.keywords.put(PlatformProfile.MOBILITY, PlatformProfile.MOBILITY);
    PlatformProfile.keywords.put(PlatformProfile.OWNERSHIP, PlatformProfile.OWNERSHIP);
    PlatformProfile.keywords.put(PlatformProfile.CERTAUTH, PlatformProfile.CERTAUTH);
    PlatformProfile.keywords.put(PlatformProfile.FIPAVERSION, PlatformProfile.FIPAVERSION);

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

    Exception.JavaExceptions.put(Exception.MISSINGATTRIBUTE, new MissingAttributeException());
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
    Exception.JavaExceptions.put(Exception.FAILEDMANACTION, new FailedManagementActionException());


    // Fill in action names for AMS and DF agents

    AMSAction.actions.put(AMSAction.AUTHENTICATE, new Integer(0));
    AMSAction.actions.put(AMSAction.REGISTERAGENT, new Integer(1));
    AMSAction.actions.put(AMSAction.DEREGISTERAGENT, new Integer(2));
    AMSAction.actions.put(AMSAction.MODIFYAGENT, new Integer(3));
    AMSAction.actions.put(AMSAction.CREATEAGENT, new Integer(4));
    AMSAction.actions.put(AMSAction.KILLAGENT, new Integer(5));
    AMSAction.actions.put(AMSAction.KILLCONTAINER, new Integer(6));
    AMSAction.actions.put(AMSAction.QUERYPLATFORMPROFILE, new Integer(7));
    AMSAction.actions.put(AMSAction.SEARCHAGENT, new Integer(8));

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


  /**
     Get <em>Singleton</em> object for
     <code>fipa-agent-management</code> ontology.  Static <em>Factory
     Method</em> to obtain an handle to the single instance of
     <code>AgentManagementOntology</code> class.
  */
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
  static boolean isValidDFADKeyword(String keyword) {
    return DFAgentDescriptor.keywords.containsKey(keyword);
  }

  // Check that 'keyword' is a valid 'FIPA-AP-description' keyword
  static boolean isValidPPKeyword(String keyword) {
    return PlatformProfile.keywords.containsKey(keyword);
  }

  // Check that 'keyword' is a valid 'FIPA-Service-Desc-Item' keyword
  static boolean isValidSDKeyword(String keyword) {
    return ServiceDescriptor.keywords.containsKey(keyword);
  }

  // Check that 'keyword' is a valid 'FIPA-AMS-description' keyword
  static boolean isValidAMSADKeyword(String keyword) {
    return AMSAgentDescriptor.keywords.containsKey(keyword);
  }

  // Check that 'message' is a valid 'AgentManagementException' error
  // message
  static boolean isValidException(String message) {
    return Exception.JavaExceptions.containsKey(message);
  }

  // Tell whether 'attributeName' is a mandatory attribute for
  // 'actionName' AMS action
  static boolean isMandatoryForAMS(String actionName, String attributeName) {
    Integer actionIndex = (Integer)AMSAction.actions.get(actionName);
    Integer attributeIndex = (Integer)AMSAgentDescriptor.keywords.get(attributeName);
    return AMSMandatoryAttributes[actionIndex.intValue()][attributeIndex.intValue()];
  }

  // Tell whether 'attributeName' is a mandatory attribute for
  // 'actionName' DF action
  static boolean isMandatoryForDF(String actionName, String attributeName) {
    Integer actionIndex = (Integer)DFAction.actions.get(actionName);
    Integer attributeIndex = (Integer)DFAgentDescriptor.keywords.get(attributeName);
    return DFMandatoryAttributes[actionIndex.intValue()][attributeIndex.intValue()];
  }


  // Lookup methods to convert between different data representations

   /**
      Access a standard <b><em>FIPA</em></b> exception by name.
      @param message The name of the exception.
      @return the Java exception corresponding to a given message.
   */
  public static FIPAException getException(String message) {
    FIPAException fe = (FIPAException)Exception.JavaExceptions.get(message);
    if(fe == null)
      fe = new FIPAException(message);
    fe.fillInStackTrace();
    return fe;
  }

  /**
     Reads a standard <em><b>FIPA</b></em> exception from a readable
     stream.
     @param r a <code>Reader</code> object, containing a string
     representation of a <em><b>FIPA</b></em> exception.
     @return A <code>FIPAException</code> object, whose string
     representation was contained in the given stream.
  */
  public static FIPAException getException(Reader r) {
      synchronized(parserLock) {
    FIPAException fe = null;
    try {
      fe = parser.parseFIPAException(r);
    }
    catch(ParseException pe) {
      // pe.printStackTrace();
      fe = getException(AgentManagementOntology.Exception.FAILEDMANACTION);
    }
    catch(TokenMgrError tme) {
      // tme.printStackTrace();
      fe = getException(AgentManagementOntology.Exception.FAILEDMANACTION);
    }
    return fe;
      }
  }

  /**
     Converts standard Agent Platform Life Cycle states names to
     <b>JADE</b> specific codes.
     @param name The state name.
     @return The number code of a given Agent Platform Life-Cycle
     state.
  */
  public static int getAPStateByName(String name) throws FIPAException {
    Integer i = (Integer)APLifeCycle.states.get(name);
    if(i == null) throw getException(Exception.UNRECOGNIZEDVALUE);
    return i.intValue();
  }

  /**
     Converts <b>JADE</b> specific codes to standard Agent Platform
     Life Cycle states.
     @param code The code of a state.
     @return The standard name for that state.
  */
  public static String getAPStateByCode(int code) throws FIPAException {
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

  /**
     Converts standard Domain Life Cycle states names to <b>JADE</b>
     specific codes.
     @param name The state name.
     @return The number code of a given Domain Life-Cycle state.
  */
  public static int getDomainStateByName(String name) throws FIPAException {
    Integer i = (Integer)DomainLifeCycle.states.get(name);
    if(i == null) throw getException(Exception.UNRECOGNIZEDVALUE);
    return i.intValue();
  }

  /**
     Converts <b>JADE</b> specific codes to standard Domain Life Cycle
     states.
     @param code The code of a state.
     @return The standard name for that state.
  */
  public static String getDomainStatebyCode(int code) throws FIPAException {
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

  // Private constructor: instantiate only through instance() method.
  private AgentManagementOntology() {
      //    addFrame();
  }


}
