/*
  $Id$
*/

package jade.domain;


public class UnauthorisedException extends FIPAException {

  public UnauthorisedException() {
    super(AgentManagementOntology.Exception.UNAUTHORISED);
  }

}
