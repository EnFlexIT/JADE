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

package jade.gui;

import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAException;

import java.util.Iterator;
import java.util.List;

/**
 * This interface must be implemented by a GUI that wants to interact
 * with the DF agent. Two implementations of this interface have been
 * realized: the class jade.domain.df (used by the DF agent itself) and
 * the class jade.applet.DFAppletCommunicator (used by the DFApplet).
 * @author Fabio Bellifemine - CSELT - 25/8/1999
 * @version $Date$ $Revision$
 */

public interface GUI2DFCommunicatorInterface {

  /**
   * @see jade.core.Agent#getName()
   */
  String getName();

  /**
   * @see jade.core.Agent#getHap()
   */
  String getHap();

  /**
   * @see jade.core.Agent#getLocalName()
   */
  String getLocalName();

  /**
   * this method makes the agent close this gui
   */
  void postCloseGuiEvent(Object g);

  /**
   * this method makes the agent exit
   */
  void postExitEvent(Object g);

  /**
   * this method registers an agent description with the DF
   */
  void postRegisterEvent(Object source, String dfName, DFAgentDescription dfd);

  /**
   * this method deregisters an agent description with the DF
   */
  void postDeregisterEvent(Object source, String dfName, DFAgentDescription dfd);

  /**
   * this method modifies an agent description with the DF
   */
  void postModifyEvent(Object source, String dfName, DFAgentDescription dfd);
  
  /**
   * this method searches agents with the DF according to the dfd
   */
  void postSearchEvent(Object source, String dfName, DFAgentDescription dfd); 
  
  /**
  * This method federate the df with another df indicated
  */
  void postFederateEvent(Object source, String dfName, DFAgentDescription dfd);
  
  /**
  * This method searches according to the specificated constraints
  */
  void postSearchWithConstraintEvent(Object source, String dfName, DFAgentDescription dfd, List constraints);
  
  /**
  * This method returns all the agent descriptions registered with the DF
  */
  Iterator getAllDFAgentDsc();

  /**
   * this method returns the agent description of an agent registered with the DF given the agent name
   */
  DFAgentDescription getDFAgentDsc(String name) throws FIPAException;
  
  /**
  * This method returns the descriptor of an agent result of a search
  */
  DFAgentDescription getDFAgentSearchDsc(String name) throws FIPAException;
  
  /**
  * This methods returns the parent of the df that are the dfs with which this df is federated.
  */
  Iterator getParents();
  
  /*
  * This method returns the df-agents registered with this df.
  */
  Iterator getChildren();
  
  /**
  * This method returns the constraints for the search operation.
  */
  List getConstraints();
  
  /*
  * This method returns the default description of the df.
  */
  DFAgentDescription getDescriptionOfThisDF();

  /*
  * This method refresh the GUI for the applet
  */
  void postRefreshAppletGuiEvent(Object source);

}
