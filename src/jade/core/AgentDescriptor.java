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

import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.security.AgentPrincipal;
import jade.security.CertificateFolder;


/**
   Hold all information about an agent
   @author Giovanni Rimassa - Universita` di Parma
   @author Giovanni Caire - TILAB
   @version $Date$ $Revision$
*/

class AgentDescriptor {
	private AMSAgentDescription description;
  private AgentProxy proxy;
  private ContainerID containerID;
  private AgentPrincipal principal;
	private CertificateFolder amsDelegation;

	// AMS description
  public void setDescription(AMSAgentDescription dsc) {
    description = dsc;
  }

  public AMSAgentDescription getDescription() {
    return description;
  }

  // Agent proxy
  public void setProxy(AgentProxy rp) {
    proxy = rp;
  }

  public AgentProxy getProxy() {
    return proxy;
  }

  // Container ID
  public void setContainerID(ContainerID cid) {
    containerID = cid;
  }

  public ContainerID getContainerID() {
    return containerID;
  }

  // Agent principal
  public void setPrincipal(AgentPrincipal p) {
    principal = p;
  }

  public AgentPrincipal getPrincipal() {
    return principal;
  }

  // AMS delegation
  public void setAMSDelegation(CertificateFolder cf) {
    amsDelegation = cf;
  }

  public CertificateFolder getAMSDelegation() {
    return amsDelegation;
  }
}
