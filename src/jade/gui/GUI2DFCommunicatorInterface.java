/**
 * This interface must be implemented by a GUI that wants to interact
 * with the DF agent. Two implementations of this interface have been
 * realized: the class jade.domain.df (used by the DF agent itself) and
 * the class jade.applet.DFAppletCommunicator (used by the DFApplet).
 * @author Fabio Bellifemine - CSELT - 25/8/1999
 */
package jade.gui;

import jade.domain.AgentManagementOntology;

import java.util.Enumeration;

public interface GUI2DFCommunicatorInterface {

  /**
   * @see jade.core.Agent#doDelete()
   */
  public abstract void doDelete();

  /**
   * @see jade.core.Agent#getName()
   */
  public abstract String getName();

  /**
   * @see jade.core.Agent#getAddress()
   */
  public abstract String getAddress();

  /**
   * @see jade.core.Agent#getLocalName()
   */
  public abstract String getLocalName();

  /**
   * this method registers an agent description with the DF
   */
  public abstract void postRegisterEvent(String parentName, AgentManagementOntology.DFAgentDescriptor dfd);

  /**
   * this method deregisters an agent description with the DF
   */
  public abstract void postDeregisterEvent(String parentName, AgentManagementOntology.DFAgentDescriptor dfd);

  /**
   * this method returns all the agent descriptions registered with the DF
   */
  public abstract Enumeration getDFAgentDescriptors();

}
