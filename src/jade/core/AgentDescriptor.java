/*
  $Log$
  Revision 1.8  1999/03/09 12:56:18  rimassa
  Added code to hold container name inside an AgentDescriptor object.

  Revision 1.7  1998/11/02 01:56:20  rimassa
  Removed every reference to MessageDispatcher class; now an
  AgentDescriptor uses AgentContainer directly.

  Revision 1.6  1998/10/04 18:00:58  rimassa
  Added a 'Log:' field to every source file.

*/

package jade.core;

import java.io.Serializable;

import jade.domain.AgentManagementOntology;
import jade.domain.FIPAException;

/************************************************************************

  Name: AgentDescriptor

  Responsibilities and Collaborations:

  + Gather in a single object all the informations needed to find its
  Agent Container and its AMS description.

************************************************************************/
class AgentDescriptor implements Serializable {

  private AgentManagementOntology.AMSAgentDescriptor desc;
  private AgentContainer container;
  private String containerName;

  public void setDesc(AgentManagementOntology.AMSAgentDescriptor amsd) {
    desc = amsd;
  }

  public void setContainer(AgentContainer ac, String cn) {
    container = ac;
    containerName = cn;
  }

  public AgentManagementOntology.AMSAgentDescriptor getDesc() {
    return desc;
  }

  public AgentContainer getContainer() {
    return container;
  }

  public String getContainerName() {
    return containerName;
  }

}



