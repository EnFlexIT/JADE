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

//import java.rmi.RemoteException;

/**
 
  This class acts as a Smart Proxy for the Main Container, transparently
  handling caching and reconnection
 
  @author Giovanni Rimassa - Universita` di Parma
  @version $Date$ $Revision$
*/
class MainContainerProxy implements MainContainer {

	private Profile myProfile;
  private MainContainer adaptee;

    MainContainerProxy(Profile p) throws ProfileException, IMTPException {
    	myProfile = p;
			// Use the IMTPManager to get a stub of the real Main container
			adaptee = myProfile.getIMTPManager().getMain();
    }

    public void register(AgentContainerImpl ac, ContainerID cid) throws IMTPException {
			
    	// The Main Container initialization of a peripheral container is just adding it to the platform.
      String name = adaptee.addContainer(ac, cid);
      cid.setName(name);
    }

    public void deregister(AgentContainer ac) throws IMTPException {
      // This call does nothing, since the container deregistration is triggered by the FailureMonitor thread.
    }

    public void newMTP(String mtpAddress, ContainerID cid) throws IMTPException {
      adaptee.newMTP(mtpAddress, cid);
    }

    public RemoteProxy getProxy(AID id) throws IMTPException, NotFoundException {
      return adaptee.getProxy(id);
    }

    public void bornAgent(AID name, RemoteProxy rp, ContainerID cid) throws IMTPException, NameClashException {
      adaptee.bornAgent(name, rp, cid);
    }

    public String getPlatformName() throws IMTPException {
      return adaptee.getPlatformName();
    }

    public AgentContainer lookup(ContainerID cid) throws IMTPException, NotFoundException {
      return adaptee.lookup(cid);
    }

    public void deadAgent(AID name) throws IMTPException, NotFoundException {
      adaptee.deadAgent(name);
    }

    public String addContainer(AgentContainer ac, ContainerID cid) throws IMTPException {
      return adaptee.addContainer(ac, cid);
    }

    public void deadMTP(String mtpAddress, ContainerID cid) throws IMTPException {
      adaptee.deadMTP(mtpAddress, cid);
    }

    public boolean transferIdentity(AID agentID, ContainerID src, ContainerID dest) throws IMTPException, NotFoundException {
      return adaptee.transferIdentity(agentID, src, dest);
    }

    public void removeContainer(ContainerID cid) throws IMTPException {
      adaptee.removeContainer(cid);
    }

}

