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

import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPAException;
import jade.core.AID;

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
  void postRegisterEvent(Object source, AID dfName, DFAgentDescription dfd);

  /**
   * this method deregisters an agent description with the DF
   */
  void postDeregisterEvent(Object source, AID dfName, DFAgentDescription dfd);

  /**
   * this method modifies an agent description with the DF
   */
  void postModifyEvent(Object source, AID dfName, DFAgentDescription dfd);
  
  /**
   * this method searches agents with the DF according to the dfd
   */
  void postSearchEvent(Object source, AID dfName, DFAgentDescription dfd, SearchConstraints c); 
  
  /**
  * This method federate the df with another df indicated
  */
  void postFederateEvent(Object source, AID dfName, DFAgentDescription dfd);
  
  
  

  /**
   * this method returns the agent description of an agent registered with the DF given the agent name
   */
  DFAgentDescription getDFAgentDsc(AID name) throws FIPAException;
  
  
 
  
  /*
  * This method returns the description of this df used for federation with other dfs.
  */
  DFAgentDescription getDescriptionOfThisDF();


}