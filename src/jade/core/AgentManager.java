package jade.core;

import java.util.Set;

import jade.domain.FIPAException;
import jade.domain.NoCommunicationMeansException;
import jade.domain.AgentAlreadyRegisteredException;

// FIXME: This interface requires a lot of cleaning !!!

/**
  This interface provides Agent Life Cycle management services to the
  platform AMS.
  */
public interface AgentManager {

  Set AMSContainerNames();
  Set AMSAgentNames();
  String AMSGetContainerName(String agentName) throws FIPAException;
  String AMSGetAddress(String agentName);
  void AMSCreateAgent(String agentName, String className, String containerName) throws NoCommunicationMeansException;
  void AMSCreateAgent(String agentName, Agent instance, String containerName) throws NoCommunicationMeansException;
  void AMSKillContainer(String containerName);
  void AMSKillAgent(String agentName, String password) throws NoCommunicationMeansException;
  void AMSNewData(String agentName, String address, String signature, String APState,
		  String delegateAgentName, String forwardAddress, String ownership)
      throws FIPAException, AgentAlreadyRegisteredException;
  void AMSChangeData(String agentName, String address, String signature, String APState,
		     String delegateAgentName, String forwardAddress, String ownership)
      throws FIPAException;
  void AMSRemoveData(String agentName, String address, String signature, String APState,
		     String delegateAgentName, String forwardAddress, String ownership)
      throws FIPAException;
  void AMSDumpData();
  void AMSDumpData(String agentName);

}
