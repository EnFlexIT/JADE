package jade.core;

import java.util.Set;

/**
  This interface provides Agent Life Cycle management services to the
  platform AMS.
  */
public interface AgentManager {

  Set containerNames();
  Set agentNames();
  String getContainerName(String agentName) throws NotFoundException;
  String getAddress(String agentName);
  void createAgent(String agentName, String className, String containerName) throws UnreachableException;
  void createAgent(String agentName, Agent instance, String containerName) throws UnreachableException;
  void killContainer(String containerName);
  void kill(String agentName, String password) throws NotFoundException, UnreachableException;
  void suspend(String agentName, String password) throws NotFoundException, UnreachableException;
  void activate(String agentName, String password) throws NotFoundException, UnreachableException;
  void wait(String agentName, String password) throws NotFoundException, UnreachableException;
  void wake(String agentName, String password) throws NotFoundException, UnreachableException;

}

