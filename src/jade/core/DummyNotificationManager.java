package jade.core;

//import jade.lang.acl.ACLMessage;

class DummyNotificationManager implements NotificationManager {
  public void initialize(AgentContainerImpl ac, LADT ladt) {
  }
  
  // ACTIVATION/DEACTIVATION METHODS
  public void enableSniffer(AID snifferName, AID toBeSniffed) {
  }
  public void disableSniffer(AID snifferName, AID notToBeSniffed) {
  }
  public void enableDebugger(AID debuggerName, AID toBeDebugged) {
  }
  public void disableDebugger(AID debuggerName, AID notToBeDebugged) {
  }
  
  // NOTIFICATION METHODS
  public void fireEvent(int eventType, Object[] param) {
  }
  //void fireSentMessage(ACLMessage msg, AID sender);
  //void firePostedMessage(ACLMessage msg, AID receiver);
  //void fireReceivedMessage(ACLMessage msg, AID receiver);
  //void fireRoutedMessage(ACLMessage msg, Channel from, Channel to);
  //void fireChangedAgentState(AID agentID, AgentState from, AgentState to);

}