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

import jade.mtp.MTPDescriptor;

import jade.domain.FIPANames;
import jade.util.leap.List;
import jade.util.leap.LinkedList;

//__SECURITY__BEGIN
import jade.security.AgentPrincipal;
import jade.security.ContainerPrincipal;
import jade.security.AuthException;
import jade.security.JADECertificate;
import jade.security.IdentityCertificate;
import jade.security.DelegationCertificate;
import jade.security.CertificateFolder;
//__SECURITY__END

/**
 
  This class acts as a Smart Proxy for the Main Container, transparently
  handling caching and reconnection
 
  @author Giovanni Rimassa - Universita' di Parma
  @version $Date$ $Revision$
*/
class MainContainerProxy implements Platform {

    private static final int CACHE_SIZE = 10;

    private Profile myProfile;

    private AgentContainerImpl localContainer;

    // Agents cache, indexed by agent name
    //    private AgentCache cachedProxies = new AgentCache(CACHE_SIZE);

    public MainContainerProxy(Profile p) throws ProfileException, IMTPException {
      myProfile = p;
    }

    public void addLocalContainer(NodeDescriptor desc) throws IMTPException, AuthException {
	// Do nothing
    }

    public void removeLocalContainer() throws IMTPException {
	// Do nothing
    }

    public void startSystemAgents(AgentContainerImpl ac) throws IMTPException {
	// Do nothing, we're not the real Main Container
    }

}

