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


/**
@author Giovanni Rimassa - Universita` di Parma
@version $Date$ $Revision$
*/

interface MainContainer {

    void register(AgentContainerImpl ac, ContainerID cid) throws InvocationException;

    void deregister(AgentContainer ac) throws InvocationException;

    String getPlatformName() throws InvocationException;

    String addContainer(AgentContainer ac, ContainerID cid) throws InvocationException;
    void removeContainer(ContainerID cid) throws InvocationException;

    AgentContainer lookup(ContainerID cid) throws InvocationException, NotFoundException;

    void bornAgent(AID name, RemoteProxy rp, ContainerID cid) throws InvocationException, NameClashException;
    void deadAgent(AID name) throws InvocationException, NotFoundException;

    void newMTP(String mtpAddress, ContainerID cid) throws InvocationException;
    void deadMTP(String mtpAddress, ContainerID cid) throws InvocationException;

    boolean transferIdentity(AID agentID, ContainerID src, ContainerID dest) throws InvocationException, NotFoundException;

    RemoteProxy getProxy(AID id) throws InvocationException, NotFoundException;

}
