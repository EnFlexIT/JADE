/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

/**
 * ***************************************************************
 * JADE - Java Agent DEvelopment Framework is a framework to develop
 * multi-agent systems in compliance with the FIPA specifications.
 * Copyright (C) 2000 CSELT S.p.A.
 * GNU Lesser General Public License
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation,
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 * **************************************************************
 */
package jade.core;

import java.util.List;

/**
 * @author Giovanni Caire - Telecom Italia Lab
 */
public interface IMTPManager {

    /**
     * Initialize this IMTPManager
     */
    void initialize(Profile p) throws IMTPException;

    /**
     * Makes the indicated AgentContainer accessible from remote
     * JVMs.
     */
    void remotize(AgentContainer ac) throws IMTPException;

    /**
     * Makes the indicated MainContainer accessible from remote
     * JVMs.
     */
    void remotize(MainContainer mc) throws IMTPException;

    /**
       Disconnects the given Agent Container and hides it from remote
       JVMs.
     */
    void unremotize(AgentContainer ac) throws IMTPException;

    /**
       Disconnects the given Main Container and hides it from remote
       JVMs.
     */
    void unremotize(MainContainer mc) throws IMTPException;

    /**
       Creates a proxy for the given agent, on the given container.
     */
    AgentProxy createAgentProxy(AgentContainer ac, AID id) throws IMTPException;

    /**
       Return the MainContainer or a stub of it depending on whether
       we are in the Main Container or in a perfipheral container.
     */
    MainContainer getMain() throws IMTPException;

    /**
     * Release all resources of this IMTPManager
     */
    void shutDown();

    /**
       Return the the List of TransportAddress where this IMTP is 
       waiting for intra-platform remote calls.
     */
    List getLocalAddresses() throws IMTPException;
}

