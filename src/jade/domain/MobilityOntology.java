/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop
multi-agent systems in compliance with the FIPA specifications.
Copyright (C) 2000 CSELT S.p.A. 

The updating of this file to JADE 2.0 has been partially supported by the IST-1999-10211 LEAP Project

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

import jade.util.leap.Iterator;
import jade.util.leap.List;
import jade.util.leap.ArrayList;
import jade.util.leap.Map;
import jade.util.leap.HashMap;

import jade.lang.Codec;

import jade.core.AID;
import jade.core.Location;
import jade.core.ContainerID;

import jade.onto.Frame;
import jade.onto.Ontology;
import jade.onto.DefaultOntology;
import jade.onto.SlotDescriptor;
import jade.onto.OntologyException;


import jade.onto.basic.*;

/**
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
    The symbolic constant that identifies an AgentIdentifier
    **/
  //public static final String AGENTIDENTIFIER = "agent-identifier";

  //public static final String DONE = "done"; 
  //public static final String RESULT = "result"; 
  /**
    A symbolic constant, containing the name of the concept.
  */
  public static final String MOBILE_AGENT_DESCRIPTION = "mobile-agent-description";
  
  /**
    A symbolic constant, containing the name of the concept.
  */
  public static final String MOBILE_AGENT_PROFILE = "mobile-agent-profile";
  
  /**
    A symbolic constant, containing the name of the concept.
  */
  public static final String MOBILE_AGENT_SYSTEM = "mobile-agent-system";
  
  /**
    A symbolic constant, containing the name of the concept.
  */
  public static final String MOBILE_AGENT_LANGUAGE = "mobile-agent-language";

  /**
    A symbolic constant, containing the name of the concept.
  */
  public static final String MOBILE_AGENT_OS = "mobile-agent-os";

  /**
    A symbolic constant, containing the name of the concept.
  */  
  public static final String LOCATION = "location";

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
  static { // must be at the end of the file, otherwise it does not work
    initInstance();
  }
  private static void initInstance() {
  	try{
			// Adds the roles of the basic ontology (ACTION, AID,...)
    	theInstance.joinOntology(BasicOntology.instance());

	theInstance.addRole(MOBILE_AGENT_DESCRIPTION, new SlotDescriptor[] {
	  new SlotDescriptor("name", Ontology.FRAME_SLOT, BasicOntology.AGENTIDENTIFIER, Ontology.M),
	  new SlotDescriptor("destination", Ontology.FRAME_SLOT, LOCATION, Ontology.M),
	  new SlotDescriptor("agent-profile", Ontology.FRAME_SLOT, MOBILE_AGENT_PROFILE, Ontology.O),
	  new SlotDescriptor("agent-version", Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.O),
	  new SlotDescriptor("signature", Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.O)
	}, MobileAgentDescription.class);

	theInstance.addRole(MOBILE_AGENT_PROFILE, new SlotDescriptor[] {
	  new SlotDescriptor("system", Ontology.FRAME_SLOT, MOBILE_AGENT_SYSTEM, Ontology.O),
	  new SlotDescriptor("language", Ontology.FRAME_SLOT, MOBILE_AGENT_LANGUAGE, Ontology.O),
          new SlotDescriptor("os", Ontology.FRAME_SLOT, MOBILE_AGENT_OS, Ontology.M)
	}, MobileAgentProfile.class); 

	theInstance.addRole(MOBILE_AGENT_SYSTEM, new SlotDescriptor[] {
	  new SlotDescriptor("name", Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.M),
	  new SlotDescriptor("major-version", Ontology.PRIMITIVE_SLOT, Ontology.LONG_TYPE, Ontology.M),
	  new SlotDescriptor("minor-version", Ontology.PRIMITIVE_SLOT, Ontology.LONG_TYPE, Ontology.O),
	  new SlotDescriptor("dependencies", Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.O)
	}, MobileAgentSystem.class); 

	theInstance.addRole(MOBILE_AGENT_LANGUAGE, new SlotDescriptor[] {
	  new SlotDescriptor("name", Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.M),
	  new SlotDescriptor("major-version", Ontology.PRIMITIVE_SLOT, Ontology.LONG_TYPE, Ontology.M),
	  new SlotDescriptor("minor-version", Ontology.PRIMITIVE_SLOT, Ontology.LONG_TYPE, Ontology.O),
	  new SlotDescriptor("dependencies", Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.O)
	}, MobileAgentLanguage.class);

	theInstance.addRole(MOBILE_AGENT_OS, new SlotDescriptor[] {
	  new SlotDescriptor("name", Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.M),
	  new SlotDescriptor("major-version", Ontology.PRIMITIVE_SLOT, Ontology.LONG_TYPE, Ontology.M),
	  new SlotDescriptor("minor-version", Ontology.PRIMITIVE_SLOT, Ontology.LONG_TYPE, Ontology.O),
	  new SlotDescriptor("dependencies", Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.O)
	}, MobileAgentOS.class);

	theInstance.addRole(LOCATION, new SlotDescriptor[] {
	    new SlotDescriptor("name", Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.M),
	    new SlotDescriptor("protocol", Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.M),
	    new SlotDescriptor("address", Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.M)
	}, ContainerID.class);

	theInstance.addRole("container-ID", new SlotDescriptor[] {
	    new SlotDescriptor("name", Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.M),
	    new SlotDescriptor("protocol", Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.M),
	    new SlotDescriptor("address", Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.M)
	}, BCLocation.class);

	theInstance.addRole(MOVE, new SlotDescriptor[] {
	    new SlotDescriptor(Ontology.FRAME_SLOT, MOBILE_AGENT_DESCRIPTION, Ontology.M)
	}, MoveAction.class);

	theInstance.addRole(CLONE, new SlotDescriptor[] {
	    new SlotDescriptor(Ontology.FRAME_SLOT, MOBILE_AGENT_DESCRIPTION, Ontology.M),
	    new SlotDescriptor(Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.M)
	}, CloneAction.class);

	theInstance.addRole(WHERE_IS, new SlotDescriptor[] {
	    new SlotDescriptor(Ontology.FRAME_SLOT, BasicOntology.AGENTIDENTIFIER , Ontology.M)
	}, WhereIsAgentAction.class);

	theInstance.addRole(QUERY_PLATFORM_LOCATIONS, new SlotDescriptor[] {
	}, QueryPlatformLocationsAction.class);

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

    private AID name;
    private Location destination;
    private MobileAgentProfile agentProfile;
    private String agentVersion;
    private String signature;

    public void setName(AID id) {
      name = id;
    }

    public AID getName() {
      return name;
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

    public void setSignature(String s) {
      signature = s; 
    }

    public String getSignature() {
      return signature;
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
    private Long majorVersion;
    private Long minorVersion;
    private String dependencies;

    public void setName(String n) {
      name = n;
    }

    public String getName() {
      return name;
    }

    public void setMajorVersion(Long v) {
      majorVersion = v;
    }

    public Long getMajorVersion() {
      return majorVersion;
    }

    public void setMinorVersion(Long v) {
      minorVersion = v;
    }

    public Long getMinorVersion() {
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
    private Long majorVersion;
    private Long minorVersion;
    private String dependencies;

    public void setName(String n) {
      name = n;
    }

    public String getName() {
      return name;
    }

    public void setMajorVersion(Long v) {
      majorVersion = v;
    }

    public Long getMajorVersion() {
      return majorVersion;
    }

    public void setMinorVersion(Long v) {
      minorVersion = v;
    }

    public Long getMinorVersion() {
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
    private Long majorVersion;
    private Long minorVersion;
    private String dependencies;

    public void setName(String n) {
      name = n;
    }

    public String getName() {
      return name;
    }

    public void setMajorVersion(Long v) {
      majorVersion = v;
    }

    public Long getMajorVersion() {
      return majorVersion;
    }

    public void setMinorVersion(Long v) {
      minorVersion = v;
    }

    public Long getMinorVersion() {
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
    This class represent the 'move-agent' action in JADE
    mobility ontology. It has various get- and set- methods, according to the
    rules for ontological classes in JADE.
    @see jade.onto.Ontology
  */
  public static class MoveAction {

    private MobileAgentDescription agentToMove;

    public void set_0(MobileAgentDescription desc) {
      agentToMove = desc;
    }

    public MobileAgentDescription get_0() {
      return agentToMove;
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

    private AID agentName;

    public void set_0(AID id) {
      agentName = id;
    }

    public AID get_0() {
      return agentName;
    }

  } // End of WhereIsAgentAction class

  /**
    This class represent the 'query-platform-locations' action in JADE
    mobility ontology. It has various get- and set- methods, according to the
    rules for ontological classes in JADE.
    @see jade.onto.Ontology
  */
  public static class QueryPlatformLocationsAction {

  } // End of QueryPlatformLocationsAction

  public static class BCLocation extends ContainerID {
	  // This class is serialized by sending normal ContainerID
	  public ContainerID toCid() {
	  	ContainerID cid = new ContainerID();
	    cid.setName(getName());
	    cid.setAddress(getAddress());
	    cid.setProtocol(getProtocol());
	  	return cid;
	  }
  }

}
