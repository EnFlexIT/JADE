/*
  $Log$
  Revision 1.3  1998/10/04 18:01:23  rimassa
  Added a 'Log:' field to every source file.

*/

package jade.domain;


public class AgentNotRegisteredException extends FIPAException {

  public AgentNotRegisteredException() {
    super(AgentManagementOntology.Exception.AGENTNOTREG);
  }

}
