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
  public jade.wrapper.AgentContainer createAgentContainer(Profile p) {

    String host = p.getParameter(ProfileImpl.HOST);
    String port = p.getParameter(ProfileImpl.PORT);

    String platformRMI = "rmi://" + host + ":" + port + "/JADE";
    String[] empty = new String[] { };
    try {
      AgentContainerImpl impl = new AgentContainerImpl();
      impl.joinPlatform(platformRMI, new LinkedList().iterator(), empty, empty);
      beginContainer();

      return new jade.wrapper.AgentContainer(impl);
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

    String host = p.getParameter(ProfileImpl.HOST);
    String port = p.getParameter(ProfileImpl.PORT);
    String platformID = p.getParameter(ProfileImpl.PLATFORM_ID);

    String platformRMI = "rmi://" + host + ":" + port + "/JADE";

    try {
      MainContainerImpl impl = new MainContainerImpl(platformID);

      // Create an embedded RMI Registry within the platform and
      // bind the Agent Platform to it

      int portNumber = Integer.parseInt(port);

      Registry theRegistry = LocateRegistry.createRegistry(portNumber);
      Naming.bind(platformRMI, impl);
      String[] empty = new String[] { };
      impl.joinPlatform(platformRMI, new LinkedList().iterator(), empty, empty);
      beginContainer();

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


  // Called by jade.core.Starter to make the VM terminate when all the
  // containers are closed.
  void setCloseVM(boolean flag) {
    closeVM = flag;
  }

  // Called by a starting up container.
  void beginContainer() {
    if(activeContainers == 0) {

      // Set up group and attributes for time critical threads
      criticalThreads = new ThreadGroup("JADE time-critical threads");
      criticalThreads.setMaxPriority(Thread.MAX_PRIORITY);

      // Initialize and start up the timer dispatcher
      theDispatcher = new TimerDispatcher();
      Thread t = new Thread(criticalThreads, theDispatcher);
      t.setPriority(criticalThreads.getMaxPriority());
      theDispatcher.setThread(t);
      theDispatcher.start();

    }

    ++activeContainers;
  }

  // Called by a terminating container.
  void endContainer() {
    --activeContainers;
    if(activeContainers == 0) {
      theDispatcher.stop();

      try {
	criticalThreads.destroy();
      }
      catch(IllegalThreadStateException itse) {
	System.out.println("Time-critical threads still active: ");
	criticalThreads.list();
      }
      finally {
	criticalThreads = null;
      }

      if(closeVM)
	System.exit(0);
    }

  }

  TimerDispatcher getTimerDispatcher() {
    return theDispatcher;
  }

  private ThreadGroup criticalThreads;
  private TimerDispatcher theDispatcher;
  private int activeContainers = 0;
  private boolean closeVM = false;

  /********** FIXME: This is just to support the JSP example *************/

  private AgentToolkit defaultToolkit;

  void setDefaultToolkit(AgentToolkit tk) {
    defaultToolkit = tk;
  }

  AgentToolkit getDefaultToolkit() {
    return defaultToolkit;
  }

  /************************************************************************/


}
