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

/**
   This interface represent the whole platform as seen by a 
   <code>Container</code>.
   It provides methods that allows a <code>Container</code> to
   register/deregister to the platform, dispatch a message to an 
   <code>Agent</code> living somewhere in the platform and notify
   the platform that an agent has born/died/moved or that an MTP
   has been installed/removed.
   @see MainContainer
   @see MainContainerImpl
   @see MainContainerProxy
   @author Giovanni Caire - TILAB
 */
interface Platform {

    void addLocalContainer(NodeDescriptor desc) throws IMTPException, AuthException;
    void removeLocalContainer() throws IMTPException;

    void startSystemAgents(AgentContainerImpl ac) throws IMTPException, NotFoundException, AuthException;

}
