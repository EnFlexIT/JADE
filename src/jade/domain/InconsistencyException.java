/*
  $Id$
*/


package jade.domain;

public class InconsistencyException extends FIPAException {

  public InconsistencyException() {
    super(AgentManagementOntology.Exception.INCONSISTENCY);
  }

}
