/*
  $Id$
*/

package jade.domain;


public class NoCommunicationMeansException extends FIPAException {

  public NoCommunicationMeansException() {
    super(AgentManagementOntology.Exception.NOCOMM);
  }

}
