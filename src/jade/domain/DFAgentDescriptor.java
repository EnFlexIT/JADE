/*
  $Id$
*/

package jade.domain;

import java.util.Enumeration;
import java.util.Vector;

class DFAgentDescriptor {

  private String name;
  private Vector addresses = new Vector();
  private Vector services = new Vector();
  private String type;
  private Vector interactionProtocols = new Vector();
  private String ontology;
  private String ownership;
  private String DFState;


  public void setName(String n) {
    name = n;
  }

  public String getName() {
    return name;
  }

  public void addAddress(String a) {
    addresses.addElement(a);
  }

  public Enumeration getAddresses() {
    return addresses.elements();
  }

  public void addService(ServiceDescriptor sd) {
    services.addElement(sd);
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

  public void dump() { 
    System.out.println("(");
    System.out.println("  ( " + AgentManagementOntology.DFAgentDescription.NAME + " " + name + " )");

    System.out.println("  ( " + AgentManagementOntology.DFAgentDescription.ADDRESS);
    Enumeration e = getAddresses();
    while(e.hasMoreElements())
      System.out.println("    " + e.nextElement());
    System.out.println("  )");
    /*
    if(services != null) {
      System.out.println("  (" + AgentManagementOntology.DFAgentDescription.SERVICES);
      System.out.println("    ( :service-name " + services.getName() + " )");
      System.out.println("    ( :service-type " + services.getType() + " )");
      System.out.println("    ( :service-ontology " + services.getOntology() + " )");
      System.out.println("    ( :fixed-properties " + services.getFixedProps() + " )");
      System.out.println("    ( :negotiable-properties " + services.getNegotiableProps() + " )");
      System.out.println("    ( :communication-properties " + services.getCommunicationProps() + " )");
      System.out.println("  )");
    }
    */
    System.out.println("  ( " + AgentManagementOntology.DFAgentDescription.TYPE + " " + type + " )");

    System.out.println("  ( " + AgentManagementOntology.DFAgentDescription.PROTOCOLS);
    e = getInteractionProtocols();
    while(e.hasMoreElements())
      System.out.println("    " + e.nextElement());
    System.out.println("  )");

    System.out.println("  ( " + AgentManagementOntology.DFAgentDescription.ONTOLOGY + " " + ontology + " )");
    System.out.println("  ( " + AgentManagementOntology.DFAgentDescription.OWNERSHIP + " " + ownership + " )");
    System.out.println("  ( " + AgentManagementOntology.DFAgentDescription.DFSTATE + " " + DFState + " )");

    System.out.println(")");

  }


}
