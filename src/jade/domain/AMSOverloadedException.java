/*
  $Log$
  Revision 1.3  1998/10/04 18:01:19  rimassa
  Added a 'Log:' field to every source file.

*/

package jade.domain;

public class AMSOverloadedException extends FIPAException {

  public AMSOverloadedException() {
    super(AgentManagementOntology.Exception.AMSOVERLOADED);
  }

}
