/*
  $Log$
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
  private MessageDispatcher demux;

  public void setDesc(AgentManagementOntology.AMSAgentDescriptor amsd) {
    desc = amsd;
  }

  public void setDemux(MessageDispatcher md) {
    demux = md;
  }

  public AgentManagementOntology.AMSAgentDescriptor getDesc() {
    return desc;
  }

  public MessageDispatcher getDemux() {
    return demux;
  }


}



