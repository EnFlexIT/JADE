/*
  $Log$
  Revision 1.3  1998/10/04 18:01:28  rimassa
  Added a 'Log:' field to every source file.

*/

package jade.domain;

public class InconsistencyException extends FIPAException {

  public InconsistencyException() {
    super(AgentManagementOntology.Exception.INCONSISTENCY);
  }

}
