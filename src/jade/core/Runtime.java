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

import java.net.MalformedURLException;

import java.rmi.*; // FIXME: This will go away...
import java.rmi.registry.*; // FIXME: This will go away...

import java.util.LinkedList;

import jade.wrapper.AgentContainer;
import jade.wrapper.MainContainer;

/**
   This class is a Singleton class, allowing intial access to the JADE
   runtime system. Invoking methods on the shared instance of this
   class, it is possible to create <it>in-process</it> agent
   containers.

   @author Giovanni Rimassa - Universita` di Parma

 */
public class Runtime {

  private static Runtime theInstance;

  static {
    theInstance = new Runtime();
  }

  // Private constructor to forbid instantiation outside the class.
  private Runtime() {
    // Do nothing
  }

  public static Runtime instance() {
    return theInstance;
  }

  /**
     Creates a new agent container in the current JVM, providing
     access through a proxy object.
     @return A proxy object, through which services can be requested
     from the real JADE container.
   */
  public AgentContainer createAgentContainer(Profile p) {
    String host = p.getMainContainerHost();
    String port = p.getMainContainerPort();

    String platformRMI = "rmi://" + host + ":" + port + "/JADE";
    String[] empty = new String[] { };
    try {
      AgentContainerImpl impl = new AgentContainerImpl();
      impl.joinPlatform(platformRMI, new LinkedList().iterator(), empty, empty);
      return new AgentContainer(impl);
    }
    catch(RemoteException re) {
      throw new InternalError("Remote exception in a local call.");
    }

  }

  /**
     Creates a new main container in the current JVM, providing
     access through a proxy object.
     @return A proxy object, through which services can be requested
     from the real JADE main container.
   */
  public jade.wrapper.MainContainer createMainContainer(Profile p) {

    String host = p.getMainContainerHost();
    String port = p.getMainContainerPort();
    String platformID = p.getPlatformID();

    String platformRMI = "rmi://" + host + ":" + port + "/JADE";

    try {
      MainContainerImpl impl = new MainContainerImpl(platformID);

      // Create an embedded RMI Registry within the platform and
      // bind the Agent Platform to it

      int portNumber = Integer.parseInt(port);

      Registry theRegistry = LocateRegistry.createRegistry(portNumber);
      Naming.bind(platformRMI, impl);

      return new jade.wrapper.MainContainer(impl);
    }
    catch(RemoteException re) {
      throw new InternalError("Remote Exception"); // FIXME: Need to throw a suitable exception
    }
    catch(MalformedURLException murle) {
      throw new InternalError("Malformed URL exception"); // FIXME: Need to throw a suitable exception
    }
    catch(AlreadyBoundException abe) {
      throw new InternalError("Already Bound Exception"); // FIXME: Need to throw a suitable exception
    }

  }

}
