/*
  $Id$
*/

package jade.domain;

public class ACCUnavailableException extends FIPAException {

  public ACCUnavailableException() {
    super(AgentManagementOntology.Exception.ACCUNAVAIL);
  }

}
