/*
  $Log$
  Revision 1.3  1998/10/04 18:01:33  rimassa
  Added a 'Log:' field to every source file.

*/

package jade.domain;


public class UnrecognizedAttributeValueException extends FIPAException {

  public UnrecognizedAttributeValueException() {
    super(AgentManagementOntology.Exception.UNRECOGNIZEDVALUE);
  }

}
