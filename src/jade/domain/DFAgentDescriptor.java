/*
  $Id$
*/

package jade.domain;

class DFAgentDescriptor {

  private String name;
  private String address;
  private ServiceDescriptor services;
  private String type;
  private String interactionProtocols;
  private String ontology;
  private String ownership;
  private String DFState;


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

  public void setServices(ServiceDescriptor sd) {
    services = sd;
  }

  public ServiceDescriptor getAgentServices() {
    return services;
  }

  public void setType(String t) {
    type = t;
  }
 
  public String getType() {
    return type;
  }

  public void setInteractionProtocols(String ip) {
    interactionProtocols = ip;
  }

  public String getInteractionProtocols() {
    return interactionProtocols;
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
    System.out.println("  ( " + AgentManagementOntology.DFAgentDescription.NAME + " " + name + ")");
    System.out.println("  ( " + AgentManagementOntology.DFAgentDescription.ADDRESS + " " + address + ")");

    System.out.println("  (" + AgentManagementOntology.DFAgentDescription.SERVICES);
    System.out.println("    ( :service-type " + services.getType() + ")");
    System.out.println("    ( :service-ontology " + services.getOntology() + ")");
    System.out.println("    ( :service-description" + services.getDescription() + ")");
    System.out.println("    ( :service-conditions" + services.getConditions() + ")");
    System.out.println("  )");

    System.out.println("  ( " + AgentManagementOntology.DFAgentDescription.TYPE + " " + type + ")");
    System.out.println("  ( " + AgentManagementOntology.DFAgentDescription.PROTOCOLS + " " + interactionProtocols + ")");
    System.out.println("  ( " + AgentManagementOntology.DFAgentDescription.ONTOLOGY + " " + ontology + ")");
    System.out.println("  ( " + AgentManagementOntology.DFAgentDescription.OWNERSHIP + " " + ownership + ")");
    System.out.println("  ( " + AgentManagementOntology.DFAgentDescription.DFSTATE + " " + DFState + ")");
    System.out.println(")");

  }


}
