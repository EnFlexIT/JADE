/*
  $Log$
  Revision 1.1  1999/02/03 15:40:48  rimassa
  A new exception to distinguish between wrong-valued and missing attributes.

*/

package jade.domain;

public class MissingAttributeException extends FIPAException {

  public MissingAttributeException() {
    super(AgentManagementOntology.Exception.MISSINGATTRIBUTE);
  }

}
