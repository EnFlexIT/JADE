/*
  $Log$
  Revision 1.3  1998/10/04 18:01:32  rimassa
  Added a 'Log:' field to every source file.

*/

package jade.domain;


public class UnrecognizedAttributeException extends FIPAException {

  public UnrecognizedAttributeException() {
    super(AgentManagementOntology.Exception.UNRECOGNIZEDATTR);
  }

}
