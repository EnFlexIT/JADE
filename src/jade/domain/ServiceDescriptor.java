/*
  $Id$
*/

package jade.domain;

class ServiceDescriptor {

  private String name;
  private String type;
  private String ontology;
  private String fixedProperties;
  private String negotiableProperties;
  private String communicationProperties;

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


}
