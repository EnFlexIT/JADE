/*
  $Log$
  Revision 1.3  1998/10/04 18:01:20  rimassa
  Added a 'Log:' field to every source file.

*/

package jade.domain;

public class AgentAlreadyRegisteredException extends FIPAException {

  public AgentAlreadyRegisteredException() {
    super(AgentManagementOntology.Exception.AGENTALREADYREG);
  }

}
