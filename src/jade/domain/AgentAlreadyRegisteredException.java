/*
  $Id$
*/

package jade.domain;

public class AgentAlreadyRegisteredException extends FIPAException {

  public AgentAlreadyRegisteredException() {
    super("agent-already-registered");
  }

}
