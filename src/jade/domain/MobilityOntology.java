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

import jade.onto.Ontology;
import jade.onto.DefaultOntology;
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
    
  }

  public static class MobileAgentDescription {

    private String agentName;
    private String address;
    private Location destination;
    private MobileAgentProfile agentProfile;
    private String agentVersion;

    public MobileAgentDescription(Frame f) {
      try {
	String an = f.getStringSlot(":agent-name");
      }
      catch(OntologyException oe) {

      }
    }

    public void setAgentName(String an) {
      agentName = new String(an);
    }

    public String getAgentName() {
      return new String(agentName);
    }

    public void setAddress(String a) {
      address = new String(a);
    }

    public String getAddress() {
      return new String(address);
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
      agentVersion = new String(v);
    }

    public String getAgentVersion() {
      return new String(agentVersion);
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
    private String majorVersion;
    private String minorVersion;
    private String dependencies;

    public MobileAgentSystem(Frame f) {

    }

    public void setName(String n) {
      name = new String(n);
    }

    public String getName() {
      return new String(name);
    }

    public void setMajorVersion(String v) {
      majorVersion = new String(v);
    }

    public String getMajorVersion() {
      return new String(majorVersion);
    }

    public void setMinorVersion(String v) {
      minorVersion = new String(v);
    }

    public String getMinorVersion() {
      return new String(minorVersion);
    }

    public void setDependencies(String d) {
      dependencies = new String(d);
    }

    public String getDependencies() {
      return new String(dependencies);
    }



  } // End of MobileAgentSystem class

  public static class MobileAgentLanguage {

  } // End of MobileAgentLanguage class

  public static class MobileAgentOS {

  } // End of MobileAgentOS class


}
