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

package jade.wrapper;

import java.rmi.RemoteException; // FIXME: This will have to go away...

import jade.core.AID;
import jade.core.AgentContainerImpl;
import jade.core.NotFoundException;
import jade.core.IMTPException;

import jade.mtp.MTPException;

/**
   This class is a Proxy class, allowing access to a JADE agent
   container. Invoking methods on instances of this class, it is
   possible to request services from <it>in-process</it> agent
   containers.
   This class must not be instantiated by applications. Instead, use
   the <code>createContainer()</code> method in class
   <code>Runtime</code>.
   @see Runtime.createContainer()

   @author Giovanni Rimassa - Universita` di Parma

 */
public class AgentContainer {

  private AgentContainerImpl myImpl;

  /**
     Public constructor. This constructor requires a concrete
     implementation of a JADE agent container, which cannot be
     instantiated bt applications, so it cannot be meaningfully called
     from application code. The proper way to create an agent
     container from an application is to call the
     <code>Runtime.createContainer()</code> method.
     @see jade.core.Runtime#createContainer(Profile p)
     @param impl A concrete implementation of a JADE agent container.
   */
  public AgentContainer(AgentContainerImpl impl) {
    myImpl = impl;
  }

  /**
     Creates a new JADE agent, running within this container, 
     @param nickname A platform-unique nickname for the newly created
     agent. The agent will be given a FIPA compliant agent identifier
     using the nickname and the ID of the platform it is running on.
     @param className The fully qualified name of the class that
     implements the agent.
     @param args A string array, containing initialization parameters
     to pass to the new agent. The <code>setArguments()</code> method
     of the <code>jade.core.Agent</code> class will be called on the
     new agent with this array as argument.
     @return A proxy object, allowing to call state-transition forcing
     methods on the real agent instance.
   */
  public Agent createAgent(String nickname, String className, String[] args) throws NotFoundException, StaleProxyException {
    if(myImpl == null)
      throw new StaleProxyException();
    try {
      jade.core.Agent a = (jade.core.Agent)Class.forName(new String(className)).newInstance();
      a.setArguments(args);
      AID agentID = new AID(nickname, AID.ISLOCALNAME);
      myImpl.initAgent(agentID, a, false);

      Agent result = new Agent(agentID, a);
      return result;
    }
    catch(ClassNotFoundException cnfe) {
      throw new NotFoundException("Class " + className + " for agent " + nickname + " was not found.");
    }
    catch(IllegalAccessException iae) {
      throw new InternalError("IllegalAccessException"); // FIXME: Need to throw a more meaningful exception
    }
    catch(InstantiationException ie) {
      throw new InternalError("InstantiationException"); // FIXME: Need to throw a more meaningful exception
    }

  }

  /**
     Shuts down this container, terminating all the agents running within it.
   */
  public void kill() throws StaleProxyException {
    if(myImpl == null)
      throw new StaleProxyException();
    myImpl.shutDown();
    myImpl = null;
  }



  /**
     Installs a new message transport protocol, that will run within
     this container.

     @param address The transport address exported by the new MTP, in
     string format.
     @param className The fully qualified name of the Java class that
     implements the transport protocol.
     @exception MTPException If something goes wrong during transport
     protocol activation.
   */
  public void installMTP(String address, String className) throws MTPException, StaleProxyException {
    if(myImpl == null)
      throw new StaleProxyException();
    try {
      myImpl.installMTP(address, className);
    }
    catch(IMTPException re) { // It should never happen...
      throw new InternalError("Remote exception on a local call.");
    }
  }

  /**
     Removes a message transport protocol, previously running within this
     container.

     @param address The transport address exported by the new MTP, in
     string format.
     @exception MTPException If something goes wrong during transport
     protocol activation.
     @exception NotFoundException If no protocol with the given
     address is currently installed on this container.
   */
  public void uninstallMTP(String address) throws MTPException, NotFoundException, StaleProxyException {
    if(myImpl == null)
      throw new StaleProxyException();
    try {
      myImpl.uninstallMTP(address);
    }
    catch(IMTPException re) { // It should never happen...
      throw new InternalError("Remote exception on a local call.");
    }
  }

}
