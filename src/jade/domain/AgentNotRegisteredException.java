/*
  $Id$
*/

package jade.domain;


public class AgentNotRegisteredException extends FIPAException {

  public AgentNotRegisteredException() {
    super(AgentManagementOntology.Exception.AGENTNOTREG);
  }

}
