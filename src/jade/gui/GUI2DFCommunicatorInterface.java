/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop
multi-agent systems in compliance with the FIPA specifications.
Copyright (C) 2000 CSELT S.p.A. 

GNU Lesser General Public License

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation, 
version 2.1 of the License. 

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the
Free Software Foundation, Inc., 59 Temple Place - Suite 330,
Boston, MA  02111-1307, USA.
*****************************************************************/

/**
 * This interface must be implemented by a GUI that wants to interact
 * with the DF agent. Two implementations of this interface have been
 * realized: the class jade.domain.df (used by the DF agent itself) and
 * the class jade.applet.DFAppletCommunicator (used by the DFApplet).
 * @author Fabio Bellifemine - CSELT - 25/8/1999
 * @version $Date$ $Revision$
 */
package jade.gui;

import jade.domain.AgentManagementOntology;
import jade.domain.FIPAException;

import java.util.Enumeration;

public interface GUI2DFCommunicatorInterface {

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
   * this method makes the agent close this gui
   */
  public void postCloseGuiEvent(Object g);

  /**
   * this method makes the agent exit
   */
  public void postExitEvent(Object g);

  /**
   * this method registers an agent description with the DF
   */
  public void postRegisterEvent(Object source, String dfName, AgentManagementOntology.DFAgentDescriptor dfd);

  /**
   * this method deregisters an agent description with the DF
   */
  public void postDeregisterEvent(Object source, String dfName, AgentManagementOntology.DFAgentDescriptor dfd);

  /**
   * this method modifies an agent description with the DF
   */
  public void postModifyEvent(Object source, String dfName, AgentManagementOntology.DFAgentDescriptor dfd);
  
  /**
   * this method searches agents with the DF according to the dfd
   */
  public void postSearchEvent(Object source, String dfName, AgentManagementOntology.DFAgentDescriptor dfd); 
  
  public void postFederateEvent(Object source, String dfName, AgentManagementOntology.DFAgentDescriptor dfd);
  
  /**
  * this method returns all the agent descriptions registered with the DF
  */
  public abstract Enumeration getAllDFAgentDsc();

  /**
   * this method returns the agent description registered with the DF given the agent name
   */
  public AgentManagementOntology.DFAgentDescriptor getDFAgentDsc(String name) throws FIPAException;
  
  public Enumeration getParents();
  
  public Enumeration getChildren();
  
  public AgentManagementOntology.DFAgentDescriptor getDescriptionOfThisDF();
  
  
	
}