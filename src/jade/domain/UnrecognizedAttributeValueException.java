/*
  $Id$
*/

package jade.domain;


public class UnrecognizedAttributeValueException extends FIPAException {

  public UnrecognizedAttributeValueException() {
    super(AgentManagementOntology.Exception.UNRECOGNIZEDVALUE);
  }

}
