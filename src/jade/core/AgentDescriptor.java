package jade.core;

import java.io.Serializable;

/************************************************************************

  Name: AgentDescriptor

  Responsibilities and Collaborations:

  + Gather in a single object all the informations needed to locate an
    agent and interact with it (name, home and current addresses,
    current life cycle state).

  + Provide platform-level support to AMS agent, holding all
    informations needed by 'fipa-man-ams-agent-description' objects in
    'fipa-agent-management' ontology.
    (ams)

************************************************************************/
class AgentDescriptor implements Serializable {

  private String name;
  private MessageDispatcher demux;
  private String address;
  private String signature;
  private String delegateAgent;
  private String forwardAddress;
  private int APState;


  public void setAll(String n, MessageDispatcher md, String a,
		     String s, String d, String f, int AP) throws IllegalArgumentException {
    name = n;
    demux = md;
    address = a;
    signature = s;
    delegateAgent = d;
    forwardAddress = f;
    if( (AP <= Agent.AP_MIN)||(AP >= Agent.AP_MAX) )
      throw new IllegalArgumentException("APState out of range");
    APState = AP;
  }

  public void setName(String n) {
    name = n;
  }

  public void setDemux(MessageDispatcher md) {
    demux = md;
  }

  public void setAddress(String a) {
    address = a;
  }

  public void setSignature(String s) {
    signature = s;
  }

  public void setDelegateAgent(String d) {
    delegateAgent = d;
  }

  public void setForwardAddress(String f) {
    forwardAddress = f;
  }

  public void setAPState(int AP) throws IllegalArgumentException {
    if( (AP <= Agent.AP_MIN)||(AP >= Agent.AP_MAX) )
      throw new IllegalArgumentException("APState out of range");
    APState = AP;
  }

  public String getName() {
    return name;
  }

  public MessageDispatcher getDemux() {
    return demux;
  }

  public String getAddress() {
    return address;
  }

  public String getSignature() {
    return signature;
  }

  public String getDelegateAgent() {
    return delegateAgent;
  }

  public String getForwardAddress() {
    return forwardAddress;
  }

  public int getAPState() {
    return APState;
  }

}

