/*
  $Id$
*/

package jade.domain;


public class UnrecognizedAttributeException extends FIPAException {

  public UnrecognizedAttributeException() {
    super(AgentManagementOntology.Exception.UNRECOGNIZEDATTR);
  }

}
