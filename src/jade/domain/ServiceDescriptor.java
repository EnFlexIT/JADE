/*
  $Id$
*/

package jade.domain;

class ServiceDescriptor {

  private String type;
  private String ontology;
  private String description;
  private String conditions;


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

  public void setDescription(String d) {
    description = d;
  }

  public String getDescription() {
    return description;
  }

  public void setConditions(String c) {
    conditions = c;
  }

  public String getConditions() {
    return conditions;
  }


}
