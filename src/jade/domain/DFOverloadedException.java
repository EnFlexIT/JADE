/*
  $Log$
  Revision 1.3  1998/10/04 18:01:25  rimassa
  Added a 'Log:' field to every source file.

*/

package jade.domain;

public class DFOverloadedException extends FIPAException {

  public DFOverloadedException() {
    super(AgentManagementOntology.Exception.DFOVERLOADED);
  }

}
