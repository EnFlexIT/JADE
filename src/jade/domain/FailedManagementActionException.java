/*
  $Log$
  Revision 1.1  1998/11/30 00:26:07  rimassa
  New kind of FIPAException, to fully support FIPA 98 version.

*/

package jade.domain;

public class FailedManagementActionException extends FIPAException {

  public FailedManagementActionException() {
    super(AgentManagementOntology.Exception.FAILEDMANACTION);
  }

}
