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


package jade.domain;

import java.util.List;
import java.util.LinkedList;

import jade.lang.Codec;

import jade.onto.Frame;
import jade.onto.Ontology;
import jade.onto.DefaultOntology;
import jade.onto.TermDescriptor;
import jade.onto.RoleFactory;
import jade.onto.OntologyException;

/**
Javadoc documentation for the file
@author Giovanni Rimassa - Universita` di Parma
@version $Date$ $Revision$
*/

/**
   This class represents the ontology used for JADE mobility. There is
   only a single instance of this class.
   @see jade.domain.MobilityOntology#instance()
 */
public class MobilityOntology {

  /**
    A symbolic constant, containing the name of this ontology.
   */
  public static final String NAME = "jade-mobility-ontology";

  /**
    A symbolic constant, containing the name of the concept.
  */
  public static final String MOBILE_AGENT_DESCRIPTION = ":mobile-agent-description";
  
  /**
    A symbolic constant, containing the name of the concept.
  */
  public static final String MOBILE_AGENT_PROFILE = ":mobile-agent-profile";
  
  /**
    A symbolic constant, containing the name of the concept.
  */
  public static final String MOBILE_AGENT_SYSTEM = ":mobile-agent-system";
  
  /**
    A symbolic constant, containing the name of the concept.
  */
  public static final String MOBILE_AGENT_LANGUAGE = ":mobile-agent-language";

  /**
    A symbolic constant, containing the name of the concept.
  */
  public static final String MOBILE_AGENT_OS = ":mobile-agent-os";

  /**
    A symbolic constant, containing the name of the concept.
  */  
  public static final String LOCATION = ":location";

  /**
    A symbolic constant, containing the name of the action.
  */
  public static final String MOVE = "move-agent";
  
  /**
    A symbolic constant, containing the name of the action.
  */
  public static final String CLONE = "clone-agent";
  
  /**
    A symbolic constant, containing the name of the action.
  */
  public static final String WHERE_IS = "where-is-agent";
  
  /**
    A symbolic constant, containing the name of the action.
  */
  public static final String QUERY_PLATFORM_LOCATIONS = "query-platform-locations";

  private static Ontology theInstance = new DefaultOntology();

  static {
    initInstance();
  }

  /**
     This method grants access to the unique instance of JADE mobility
     ontology.
     @return An <code>Ontology</code> object, containing the concepts
     of JADE mobility ontology.
  */
  public static Ontology instance() {
    return theInstance;
  }

  private MobilityOntology() {
  }

  private static void initInstance() {
    try {
	theInstance.addFrame(MOBILE_AGENT_DESCRIPTION, Ontology.CONCEPT_TYPE, new TermDescriptor[] {
	  new TermDescriptor(":name", Ontology.STRING_TYPE, Ontology.M),
	  new TermDescriptor(":address", Ontology.STRING_TYPE, Ontology.M),
	  new TermDescriptor(":destination", Ontology.CONCEPT_TYPE, LOCATION, Ontology.M),
	  new TermDescriptor(":agent-profile", Ontology.CONCEPT_TYPE, MOBILE_AGENT_PROFILE, Ontology.O),
	  new TermDescriptor(":agent-version", Ontology.STRING_TYPE, Ontology.O),
	  new TermDescriptor(":signature", Ontology.BINARY_TYPE, Ontology.O)
	}, new RoleFactory() {
	     public Object create(Frame f) { return new MobileAgentDescription(); }
	     public Class getClassForRole() { return MobileAgentDescription.class; }
	   });

	theInstance.addFrame(MOBILE_AGENT_PROFILE, Ontology.CONCEPT_TYPE, new TermDescriptor[] {
	  new TermDescriptor(":system", Ontology.CONCEPT_TYPE, MOBILE_AGENT_SYSTEM, Ontology.O),
	  new TermDescriptor(":language", Ontology.CONCEPT_TYPE, MOBILE_AGENT_LANGUAGE, Ontology.O),
          new TermDescriptor(":os", Ontology.CONCEPT_TYPE, MOBILE_AGENT_OS, Ontology.M)
	}, new RoleFactory() {
	     public Object create(Frame f) { return new MobileAgentProfile(); }
	     public Class getClassForRole() { return MobileAgentProfile.class; }
	   });

	theInstance.addFrame(MOBILE_AGENT_SYSTEM, Ontology.CONCEPT_TYPE, new TermDescriptor[] {
	  new TermDescriptor(":name", Ontology.STRING_TYPE, Ontology.M),
	  new TermDescriptor(":major-version", Ontology.SHORT_TYPE, Ontology.M),
	  new TermDescriptor(":minor-version", Ontology.SHORT_TYPE, Ontology.O),
	  new TermDescriptor(":dependencies", Ontology.STRING_TYPE, Ontology.O)
	}, new RoleFactory() {
	     public Object create(Frame f) { return new MobileAgentSystem(); }
	     public Class getClassForRole() { return MobileAgentSystem.class; }
	   });

	theInstance.addFrame(MOBILE_AGENT_LANGUAGE, Ontology.CONCEPT_TYPE, new TermDescriptor[] {
	  new TermDescriptor(":name", Ontology.STRING_TYPE, Ontology.M),
	  new TermDescriptor(":major-version", Ontology.SHORT_TYPE, Ontology.M),
	  new TermDescriptor(":minor-version", Ontology.SHORT_TYPE, Ontology.O),
	  new TermDescriptor(":dependencies", Ontology.STRING_TYPE, Ontology.O)
	}, new RoleFactory() {
	     public Object create(Frame f) { return new MobileAgentLanguage(); }
	     public Class getClassForRole() { return MobileAgentLanguage.class; }
	   });

	theInstance.addFrame(MOBILE_AGENT_OS, Ontology.CONCEPT_TYPE, new TermDescriptor[] {
	  new TermDescriptor(":name", Ontology.STRING_TYPE, Ontology.M),
	  new TermDescriptor(":major-version", Ontology.SHORT_TYPE, Ontology.M),
	  new TermDescriptor(":minor-version", Ontology.SHORT_TYPE, Ontology.O),
	  new TermDescriptor(":dependencies", Ontology.STRING_TYPE, Ontology.O)
	}, new RoleFactory() {
	     public Object create(Frame f) { return new MobileAgentOS(); }
	     public Class getClassForRole() { return MobileAgentOS.class; }
	   });

	theInstance.addFrame(LOCATION, Ontology.CONCEPT_TYPE, new TermDescriptor[] {
	    new TermDescriptor(":name", Ontology.STRING_TYPE, Ontology.M),
	    new TermDescriptor(":transport-protocol", Ontology.STRING_TYPE, Ontology.M),
	    new TermDescriptor(":transport-address", Ontology.STRING_TYPE, Ontology.M)
	}, new RoleFactory() {
	     public Object create(Frame f) { return new Location(); } // FIXME: use Indexed Creation
	     public Class getClassForRole() { return Location.class; }
	   });

	theInstance.addFrame(MOVE, Ontology.ACTION_TYPE, new TermDescriptor[] {
	    new TermDescriptor(Ontology.CONCEPT_TYPE, MOBILE_AGENT_DESCRIPTION, Ontology.M)
	}, new RoleFactory() {
	     public Object create(Frame f) { return new MoveAction(); }
	     public Class getClassForRole() { return MoveAction.class; }
	   });

	theInstance.addFrame(CLONE, Ontology.ACTION_TYPE, new TermDescriptor[] {
	    new TermDescriptor(Ontology.CONCEPT_TYPE, MOBILE_AGENT_DESCRIPTION, Ontology.M),
	    new TermDescriptor(Ontology.STRING_TYPE, Ontology.M)
	}, new RoleFactory() {
	     public Object create(Frame f) { return new CloneAction(); }
	     public Class getClassForRole() { return CloneAction.class; }
	   });

	theInstance.addFrame(WHERE_IS, Ontology.ACTION_TYPE, new TermDescriptor[] {
	    new TermDescriptor(Ontology.STRING_TYPE, Ontology.M)
	}, new RoleFactory() {
	     public Object create(Frame f) { return new WhereIsAgentAction(); }
	     public Class getClassForRole() { return WhereIsAgentAction.class; }
	   });

	theInstance.addFrame(QUERY_PLATFORM_LOCATIONS, Ontology.ACTION_TYPE, new TermDescriptor[] {
	}, new RoleFactory() {
	     public Object create(Frame f) { return new QueryPlatformLocationsAction(); }
	     public Class getClassForRole() { return QueryPlatformLocationsAction.class; }
	   });

    }
    catch(OntologyException oe) {
      oe.printStackTrace();
    }
  }

  /**
    This class represent the ':mobile-agent-description' concept in JADE
    mobility ontology. It has various get- and set- methods, according to the
    rules for ontological classes in JADE.
    @see jade.onto.Ontology
  */
  public static class MobileAgentDescription {

    private String name;
    private String address;
    private Location destination;
    private MobileAgentProfile agentProfile;
    private String agentVersion;
    private byte[] signature;

    public void setName(String n) {
      name = n;
    }

    public String getName() {
      return name;
    }

    public void setAddress(String a) {
      address = a;
    }

    public String getAddress() {
      return address;
    }

    public void setDestination(Location d) {
      destination = d;
    }

    public Location getDestination() {
      return destination;
    }

    public void setAgentProfile(MobileAgentProfile ap) {
      agentProfile = ap;
    }

    public MobileAgentProfile getAgentProfile() {
      return agentProfile;
    }

    public void setAgentVersion(String v) {
      agentVersion = v;
    }

    public String getAgentVersion() {
      return agentVersion;
    }

    public void setSignature(byte[] s) {
      signature = new byte[s.length];
      System.arraycopy(s, 0, signature, 0, s.length);
    }

    public byte[] getSignature() {
      byte[] result = new byte[signature.length];
      System.arraycopy(signature, 0, result, 0, signature.length);
      return result;
    }

  } // End of MobileAgentDescription class

  /**
    This class represent the ':mobile-agent-profile' concept in JADE
    mobility ontology. It has various get- and set- methods, according to the
    rules for ontological classes in JADE.
    @see jade.onto.Ontology
  */
  public static class MobileAgentProfile {

    private MobileAgentSystem system;
    private MobileAgentLanguage language;
    private MobileAgentOS os;

    public void setSystem(MobileAgentSystem s) {
      system = s;
    }

    public MobileAgentSystem getSystem() {
      return system;
    }

    public void setLanguage(MobileAgentLanguage l) {
      language = l;
    }

    public MobileAgentLanguage getLanguage() {
      return language;
    }

    public void setOS(MobileAgentOS o) {
      os = o;
    }

    public MobileAgentOS getOS() {
      return os;
    }

  } // End of MobileAgentProfile class

  /**
    This class represent the ':mobile-agent-system' concept in JADE
    mobility ontology. It has various get- and set- methods, according to the
    rules for ontological classes in JADE.
    @see jade.onto.Ontology
  */
  public static class MobileAgentSystem {

    private String name;
    private short majorVersion;
    private short minorVersion;
    private String dependencies;

    public void setName(String n) {
      name = n;
    }

    public String getName() {
      return name;
    }

    public void setMajorVersion(short v) {
      majorVersion = v;
    }

    public short getMajorVersion() {
      return majorVersion;
    }

    public void setMinorVersion(short v) {
      minorVersion = v;
    }

    public short getMinorVersion() {
      return minorVersion;
    }

    public void setDependencies(String d) {
      dependencies = d;
    }

    public String getDependencies() {
      return dependencies;
    }

  } // End of MobileAgentSystem class

  /**
    This class represent the ':mobile-agent-language' concept in JADE
    mobility ontology. It has various get- and set- methods, according to the
    rules for ontological classes in JADE.
    @see jade.onto.Ontology
  */
  public static class MobileAgentLanguage {
    private String name;
    private short majorVersion;
    private short minorVersion;
    private String dependencies;

    public void setName(String n) {
      name = n;
    }

    public String getName() {
      return name;
    }

    public void setMajorVersion(short v) {
      majorVersion = v;
    }

    public short getMajorVersion() {
      return majorVersion;
    }

    public void setMinorVersion(short v) {
      minorVersion = v;
    }

    public short getMinorVersion() {
      return minorVersion;
    }

    public void setDependencies(String d) {
      dependencies = d;
    }

    public String getDependencies() {
      return dependencies;
    }

  } // End of MobileAgentLanguage class

  /**
    This class represent the ':mobile-agent-os' concept in JADE
    mobility ontology. It has various get- and set- methods, according to the
    rules for ontological classes in JADE.
    @see jade.onto.Ontology
  */
  public static class MobileAgentOS {
    private String name;
    private short majorVersion;
    private short minorVersion;
    private String dependencies;

    public void setName(String n) {
      name = n;
    }

    public String getName() {
      return name;
    }

    public void setMajorVersion(short v) {
      majorVersion = v;
    }

    public short getMajorVersion() {
      return majorVersion;
    }

    public void setMinorVersion(short v) {
      minorVersion = v;
    }

    public short getMinorVersion() {
      return minorVersion;
    }

    public void setDependencies(String d) {
      dependencies = d;
    }

    public String getDependencies() {
      return dependencies;
    }

  } // End of MobileAgentOS class

  /**
    This class represent the ':location' concept in JADE
    mobility ontology. It has various get- and set- methods, according to the
    rules for ontological classes in JADE.
    @see jade.onto.Ontology
  */
  public static class Location implements jade.core.Location {

    /**
    @serial
    */
  	private String name;
  	/**
  	@serial
  	*/
    private String protocol;
    /**
    @serial
    */
    private String address;

    public void setName(String n) {
      name = n;
    }

    public String getName() {
      return name;
    }

    public void setTransportProtocol(String tp) {
      protocol = tp;
    }

    public String getTransportProtocol() {
      return protocol;
    }

    public void setTransportAddress(String ta) {
      address = ta;
    }

    public String getTransportAddress() {
      return address;
    }

    public String getID() {
      return name + '@' + protocol + "://" + address;
    }

    public String getProtocol() {
      return getTransportProtocol();
    }

    public String getAddress() {
      return getTransportAddress();
    }

  } // End of Location class

  /**
    This class represent the 'move-agent' action in JADE
    mobility ontology. It has various get- and set- methods, according to the
    rules for ontological classes in JADE.
    @see jade.onto.Ontology
  */
  public static class MoveAction {

    private MobileAgentDescription agentToMove;
    private String actor;

    public void set_0(MobileAgentDescription desc) {
      agentToMove = desc;
    }

    public MobileAgentDescription get_0() {
      return agentToMove;
    }

    public void setActor(String a) {
      actor = a;
    }

    public String getActor() {
      return actor;
    }

  } // End of MoveAction class

  /**
    This class represent the 'clone-agent' action in JADE
    mobility ontology. It has various get- and set- methods, according to the
    rules for ontological classes in JADE.
    @see jade.onto.Ontology
  */
  public static class CloneAction extends MoveAction {

    private String newName;

    public void set_1(String nn) {
      newName = nn;
    }

    public String get_1() {
      return newName;
    }

  } // End of CloneAction class

  /**
    This class represent the 'where-is-agent' action in JADE
    mobility ontology. It has various get- and set- methods, according to the
    rules for ontological classes in JADE.
    @see jade.onto.Ontology
  */
  public static class WhereIsAgentAction {

    private String agentName;
    private String actor;

    public void set_0(String n) {
      agentName = n;
    }

    public String get_0() {
      return agentName;
    }

    public void setActor(String a) {
      actor = a;
    }

    public String getActor() {
      return actor;
    }

  } // End of WhereIsAgentAction class

  /**
    This class represent the 'query-platform-locations' action in JADE
    mobility ontology. It has various get- and set- methods, according to the
    rules for ontological classes in JADE.
    @see jade.onto.Ontology
  */
  public static class QueryPlatformLocationsAction {

    private String actor;

    public void setActor(String a) {
      actor = a;
    }

    public String getActor() {
      return actor;
    }

  } // End of QueryPlatformLocationsAction

  /**
    This method parses the <code>:content</code> slot of the <code>inform</code>
    message sent by the AMS when a <code>query-platform-locations</code> request
    succeeds.
    <i>This method is a temporary workaround and will go away as soon as JADE
    supports the newest FIPA specifications.</i>
    @param c The <code>Codec</code> for the content language to use. At present,
    the JADE AMS uses the <i>SL0</i> language, so an <code>SL0Codec</code> must
    be used.
    @param list The string containing the list of the locations. This string
    must be the <code>:content</code> slot of the <code>inform</code> message
    for a <code>:query-platform-locations</code> action.
    @return An array of <code>Location</code> objects, representing all the
    available locations of the current JADE platform.
    @exception Codec.CodecException Thrown if some parsing error occurs.
    @exception OntologyException Thrown if an inconsistency in the JADE mobility
    ontology is detected.
   */
  public static Location[] parseLocationsList(Codec c, String list) throws Codec.CodecException, OntologyException {

    if(list.startsWith("#")) { // Handle ByteLenghtEncoded content
      int prefixEnd = list.indexOf('"');
      list = list.substring(prefixEnd + 1); // Remove '#<len>"' prefix
    }

    List locations = new LinkedList();
    int from = 0;                      // From the beginning of the string ...
    do {
      int to = list.indexOf("||", from); // ... until the first occurrence of the separator.
      if(to == -1)
	to = list.length(); // ...until the end if there's no separator.

      String item = list.substring(from, to);

      // Convert the string in a Location object with the given codec.
      Frame f = c.decode(item, theInstance);
      Location l = (Location)theInstance.createObject(f);

      // Put the Location object at the end of the list
      locations.add(l);

      from = to + 2; // Next item starts after the separator.
    }
    while(from < list.length());

    Location[] result = new Location[locations.size()];

    for(int i = 0; i < result.length; i++)
      result[i] = (Location)locations.get(i);

    return result;
  }

}