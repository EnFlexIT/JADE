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

  private static final String MOBILE_AGENT_DESCRIPTION = ":mobile-agent-description";
  private static final String MOBILE_AGENT_PROFILE = ":mobile-agent-profile";
  private static final String MOBILE_AGENT_SYSTEM = ":mobile-agent-system";
  private static final String MOBILE_AGENT_LANGUAGE = ":mobile-agent-language";
  private static final String MOBILE_AGENT_OS = ":mobile-agent-os";

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

  private void initInstance() {
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
	     public Object create(Frame f) { // Use Indexed Creation }
	     public Class getClassForRole() { return Location.class; }
	   });

    }
    catch(OntologyException oe) {

    }
  }

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
      System.arraycopy(s, 0, signature, s.length);
    }

    public byte[] getSignature() {
      byte[] result = new byte[s.length];
      System.arraycopy(signature, 0, result, signature.length);
      return result;
    }

  } // End of MobileAgentDescription class


  public static class MobileAgentProfile {

    private MobileAgentSystem system;
    private MobileAgentLanguage language;
    private MobileAgentOS os;

    public MobileAgentProfile(Frame f) {

    }

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


}
