/*
  $Id$
*/

package jade.domain;

public class DFOverloadedException extends FIPAException {

  public DFOverloadedException() {
    super(AgentManagementOntology.Exception.DFOVERLOADED);
  }

}
