package fipa.core;

import java.io.Serializable;

/************************************************************************

  Name: AgentDescriptor

  Responsibilities and Collaborations:

  + Gather in a single object all the informations needed to locate an
    agent and interact with it (name, home and current addresses,
    current life cycle state).

************************************************************************/
class AgentDescriptor implements Serializable {

  private String name;
  private MessageDispatcher demux;
  // Current address
  // Current life cycle state

  public void set(String s, MessageDispatcher md) {
    name = s;
    demux = md;
  }

  public String getName() {
    return name;
  }

  public MessageDispatcher getDemux() {
    return demux;
  }

}

