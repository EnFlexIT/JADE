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

//#APIDOC_EXCLUDE_FILE
//#MIDP_EXCLUDE_FILE

import jade.util.leap.Set;
import jade.util.leap.List;

import jade.core.ContainerID;

import jade.core.event.PlatformListener;
import jade.core.event.MTPListener;

import jade.mtp.MTPException;
import jade.mtp.MTPDescriptor;

import jade.security.Authority;
import jade.security.AgentPrincipal;
import jade.security.AuthException;
import jade.security.IdentityCertificate;
import jade.security.DelegationCertificate;
import jade.security.CertificateFolder;

import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.domain.FIPAAgentManagement.AlreadyRegistered;
import jade.domain.FIPAAgentManagement.NotRegistered;

/**
@author Giovanni Rimassa - Universita' di Parma
@version $Date$ $Revision$
*/

/**
  This interface provides Agent Life Cycle management services to the
  platform AMS.
  */
public interface AgentManager {

  /**
     This callback interface is implemented by the AMS in order to be
     notified of significant platform-level events (e.g. container
     added or removed, agents birth or death, mtp configuration changes, etc.).
   */
  public static interface Listener extends PlatformListener, MTPListener {
  }

  // Listeners related methods
  void addListener(Listener l);
  void removeListener(Listener l);

  // Platform information retrieval methods
  ContainerID[] containerIDs();
  AID[] agentNames();
  List containerMTPs(ContainerID cid) throws NotFoundException;
  List containerAgents(ContainerID cid) throws NotFoundException;

  void addTool(AID tool);
  void removeTool(AID tool);
  AID[] agentTools();
  
  ContainerID getContainerID(AID agentID) throws NotFoundException;
  AMSAgentDescription getAMSDescription(AID agentID) throws NotFoundException;
  //CertificateFolder getAMSDelegation(AID agentID);
  
  Authority getAuthority();
  
  // JADE actions method
    /**
     * Create an agent on the given container. If the container-name is null
     * then the agent is created on the main-container.
     * @throws NotFoundException if the passed container does not exist
     * @throws UnreachableException if the container is unreachable 
     * @throws AuthException if this action is not authorized
     **/
  void create(String agentName, String className, String arguments[], ContainerID cid, String ownership, CertificateFolder certs) throws UnreachableException, AuthException, NotFoundException, NameClashException;
  void kill(AID agentID) throws NotFoundException, UnreachableException, AuthException;
  void suspend(AID agentID) throws NotFoundException, UnreachableException, AuthException;
  void activate(AID agentID) throws NotFoundException, UnreachableException, AuthException;
  void wait(AID agentID, String password) throws NotFoundException, UnreachableException;
  void wake(AID agentID, String password) throws NotFoundException, UnreachableException;

  void move(AID agentID, Location where) throws NotFoundException, UnreachableException, AuthException;
  void copy(AID agentID, Location where, String newAgentName) throws NotFoundException, NameClashException, UnreachableException, AuthException;

  void shutdownPlatform() throws AuthException;
  void killContainer(ContainerID cid) throws NotFoundException, AuthException;
  MTPDescriptor installMTP(String address, ContainerID cid, String className) throws NotFoundException, UnreachableException, MTPException;
  void uninstallMTP(String address, ContainerID cid) throws NotFoundException, UnreachableException, MTPException;

  void take(AID agentID, String username, byte[] password) throws NotFoundException, UnreachableException, AuthException;

  void sniffOn(AID snifferName, List toBeSniffed) throws NotFoundException, UnreachableException;
  void sniffOff(AID snifferName, List toBeSniffed) throws NotFoundException, UnreachableException;
  void debugOn(AID debuggerName, List toBeDebugged) throws NotFoundException, UnreachableException;
  void debugOff(AID debuggerName, List toBeDebugged) throws NotFoundException, UnreachableException;

  // FIPA actions method
  void amsRegister(AMSAgentDescription dsc) throws AlreadyRegistered, AuthException;
  void amsDeregister(AMSAgentDescription dsc) throws NotRegistered, AuthException;
  void amsModify(AMSAgentDescription dsc) throws NotRegistered, NotFoundException, UnreachableException, AuthException; 
  List amsSearch(AMSAgentDescription template, long maxResults);
}

