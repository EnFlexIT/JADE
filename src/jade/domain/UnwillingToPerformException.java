/*
  $Log$
  Revision 1.3  1998/10/04 18:01:34  rimassa
  Added a 'Log:' field to every source file.

*/

package jade.domain;


public class UnwillingToPerformException extends FIPAException {

  public UnwillingToPerformException() {
    super(AgentManagementOntology.Exception.UNWILLING);
  }

}
