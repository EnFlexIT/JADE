
/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop multi-agent systems in compliance with the FIPA specifications.
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

package jade.gui;

import jade.domain.AgentManagementOntology;

import java.util.Enumeration;

/**
Javadoc documentation for the file
@author Giovanni Caire - CSELT S.p.A
@version $Date$ $Revision$
*/

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
