/*
  $Log$
  Revision 1.3  1998/10/04 18:01:31  rimassa
  Added a 'Log:' field to every source file.

*/

package jade.domain;


public class UnauthorisedException extends FIPAException {

  public UnauthorisedException() {
    super(AgentManagementOntology.Exception.UNAUTHORISED);
  }

}
