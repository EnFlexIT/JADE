/*
  $Log$
  Revision 1.3  1998/10/04 18:01:18  rimassa
  Added a 'Log:' field to every source file.

*/

package jade.domain;

public class ACCUnavailableException extends FIPAException {

  public ACCUnavailableException() {
    super(AgentManagementOntology.Exception.ACCUNAVAIL);
  }

}
