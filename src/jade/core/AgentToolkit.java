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

import jade.lang.acl.ACLMessage;
import jade.security.AuthException;
//#MIDP_EXCLUDE_BEGIN
import jade.core.behaviours.Behaviour;
import jade.security.Authority;
import jade.security.AgentPrincipal;
import jade.security.IdentityCertificate;
import jade.security.DelegationCertificate;
import jade.security.CertificateFolder;
//#MIDP_EXCLUDE_END

/**
@author Giovanni Rimassa - Universita' di Parma
@version $Date$ $Revision$
*/

interface AgentToolkit {
  Location here();
  void handleStart(String localName, Agent instance);
  void handleEnd(AID agentID);
  void handleChangedAgentState(AID agentID, AgentState from, AgentState to);
  void handleSend(ACLMessage msg, AID sender) throws AuthException;

  //#MIDP_EXCLUDE_BEGIN
  void handlePosted(AID agentID, ACLMessage msg) throws AuthException;
  void handleReceived(AID agentID, ACLMessage msg) throws AuthException;
  void handleBehaviourAdded(AID agentID, Behaviour b);
  void handleBehaviourRemoved(AID agentID, Behaviour b);
  void handleChangeBehaviourState(AID agentID, Behaviour b, String from, String to);
  void handleChangedAgentPrincipal(AID agentID, AgentPrincipal from, CertificateFolder certs);

  ServiceHelper getHelper(Agent a, String serviceName) throws ServiceException;
  Authority getAuthority();

  // FIXME: Needed due to the Persistence Service being an add-on
  void handleSave(AID agentID, String repository) throws ServiceException, NotFoundException, IMTPException;
  void handleFreeze(AID agentID, String repository, ContainerID bufferContainer) throws ServiceException, NotFoundException, IMTPException;

  //#MIDP_EXCLUDE_END

  void setPlatformAddresses(AID id);
  AID getAMS();
  AID getDefaultDF();
  String getProperty(String key, String aDefault);

}
