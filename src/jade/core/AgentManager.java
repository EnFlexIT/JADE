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

package jade.core;

import java.net.InetAddress;

import java.util.Set;
import java.util.Map;

/**
@author Giovanni Rimassa - Universita` di Parma
@version $Date$ $Revision$
*/

/**
  This interface provides Agent Life Cycle management services to the
  platform AMS.
  */
public interface AgentManager {

  static final String MAIN_CONTAINER_NAME = "Main-Container";
  static final String AUX_CONTAINER_NAME = "Container-";

  /**
     This callback interface is implemented by the AMS in order to be
     notified of significant platform-level events (e.g. container
     added or removed, agent state changes, etc.).
   */
  static interface Listener {
    void handleNewContainer(String name, InetAddress host);
    void handleDeadContainer(String name);
    void handleNewAgent(String containerName, AID agentID);
    void handleDeadAgent(String containerName, AID agentID);
    void handleMovedAgent(String fromContainer, String toContainer, AID agentID);
  }

  void addListener(Listener l);
  void removeListener(Listener l);

  String[] containerNames();
  AID[] agentNames();
  String[] platformAddresses();

  String getContainerName(AID agentID) throws NotFoundException;
  void create(String agentName, String className, String containerName) throws UnreachableException;

  void killContainer(String containerName);
  void kill(AID agentID, String password) throws NotFoundException, UnreachableException;

  void suspend(AID agentID, String password) throws NotFoundException, UnreachableException;
  void activate(AID agentID, String password) throws NotFoundException, UnreachableException;

  void wait(AID agentID, String password) throws NotFoundException, UnreachableException;
  void wake(AID agentID, String password) throws NotFoundException, UnreachableException;

  void sniffOn(String SnifferName, Map ToBeSniffed) throws UnreachableException;
  void sniffOff(String SnifferName, Map ToBeSniffed) throws UnreachableException;

  void move(AID agentID, Location where, String password) throws NotFoundException, UnreachableException;
  void copy(AID agentID, Location where, String newAgentName, String password) throws NotFoundException, UnreachableException;

}

