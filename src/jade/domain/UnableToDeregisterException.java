/*
  $Id$
*/

package jade.domain;


public class UnableToDeregisterException extends FIPAException {

  public UnableToDeregisterException() {
    super(AgentManagementOntology.Exception.UNABLETODEREG);
  }

}
