/*
  $Id$
*/

package jade.domain;

public class AgentAlreadyRegisteredException extends FIPAException {

  public AgentAlreadyRegisteredException() {
    super(AgentManagementOntology.Exception.AGENTALREADYREG);
  }

}
