/*
  $Id$
*/

package jade.domain;

public class AMSOverloadedException extends FIPAException {

  public AMSOverloadedException() {
    super(AgentManagementOntology.Exception.AMSOVERLOADED);
  }

}
