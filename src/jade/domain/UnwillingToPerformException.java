/*
  $Id$
*/

package jade.domain;


public class UnwillingToPerformException extends FIPAException {

  public UnwillingToPerformException() {
    super(AgentManagementOntology.Exception.UNWILLING);
  }

}
