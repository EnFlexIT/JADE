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

//#MIDP_EXCLUDE_FILE

import jade.lang.acl.ACLMessage;
import jade.core.behaviours.Behaviour;
//__SECURITY__BEGIN
import jade.security.Authority;
import jade.security.AuthException;
import jade.security.AgentPrincipal;
import jade.security.IdentityCertificate;
import jade.security.DelegationCertificate;
import jade.security.CertificateFolder;
import jade.security.dummy.DummyAuthority;
//__SECURITY__END

/**
 * This is a degenerate toolkit which is used before the actual one is
 * setup or during agent termination. Every method which returns an object
 * will return null and all others do nothing.
 * @author Dick Cowan - HP
 */
final class DummyToolkit implements AgentToolkit {

    static AgentToolkit at = null;
    
    static AgentToolkit instance() {
        if (at == null) {
            at = new DummyToolkit();
        }
        return at;
    }

    public Location here() {
        return null;
    }
    
    public void handleStart(String localName, Agent instance) {
        throw new InternalError("Trying to start an agent without proper runtime support.");
    }

    //FIXME should we here throw an InternalError also?
    public void handleEnd(AID agentID) {}
    public void handleSend(ACLMessage msg, AID sender) throws AuthException {}
    public void handlePosted(AID agentID, ACLMessage msg) throws AuthException {}
    public void handleReceived(AID agentID, ACLMessage msg) throws AuthException {}
    public void handleChangedAgentState(AID agentID, AgentState from, AgentState to) {}
    public void handleBehaviourAdded(AID agentID, Behaviour b) {}
    public void handleBehaviourRemoved(AID agentID, Behaviour b) {}
    public void handleChangeBehaviourState(AID agentID, Behaviour b, String from, String to) {}

    // FIXME: Needed due to the Persistence Service being an add-on
    public void handleSave(AID agentID, String repository) throws ServiceException, NotFoundException, IMTPException {}
    public void handleFreeze(AID agentID, String repository, ContainerID bufferContainer) throws ServiceException, NotFoundException, IMTPException {}

    //__SECURITY__BEGIN
    public void handleChangedAgentPrincipal(AID agentID, AgentPrincipal from, CertificateFolder certs) {}
    public Authority getAuthority() {
        return new DummyAuthority();
    } 
    //__SECURITY__END
    
    public void setPlatformAddresses(AID id) {}
    
    public AID getAMS() {
        return null;
    }
    
    public AID getDefaultDF() {
        return null;
    }
    
	public String getProperty(String key, String aDefault) {
		return null;
	}

    public ServiceHelper getHelper(Agent a, String serviceName) throws ServiceException {
        return null;
    }

}
