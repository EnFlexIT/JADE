/*
  $Log$
  Revision 1.3  1998/10/04 18:01:30  rimassa
  Added a 'Log:' field to every source file.

*/

package jade.domain;


public class UnableToDeregisterException extends FIPAException {

  public UnableToDeregisterException() {
    super(AgentManagementOntology.Exception.UNABLETODEREG);
  }

}
