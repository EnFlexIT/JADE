/*
  $Log$
  Revision 1.3  1998/10/04 18:01:29  rimassa
  Added a 'Log:' field to every source file.

*/

package jade.domain;


public class NoCommunicationMeansException extends FIPAException {

  public NoCommunicationMeansException() {
    super(AgentManagementOntology.Exception.NOCOMM);
  }

}
