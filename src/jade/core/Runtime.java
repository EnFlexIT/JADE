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

import jade.util.leap.LinkedList;

/**
   This class is a Singleton class, allowing intial access to the JADE
   runtime system. Invoking methods on the shared instance of this
   class (i.e. <code>Runtime.instance()</code>, 
   it is possible to create <it>in-process</it> agent
   containers.

   @author Giovanni Rimassa - Universita` di Parma

 */
public class Runtime {

  private static Runtime theInstance;

  static {
    theInstance = new Runtime();
  }

  private ThreadGroup criticalThreads;
  private TimerDispatcher theDispatcher;
  private int activeContainers = 0;
  private boolean closeVM = false;
  private Object lock = new Object();

  // Private constructor to forbid instantiation outside the class.
  private Runtime() {
    // Do nothing
  }

    /**
     * This method returns the singleton instance of this class
     * that should be then used to create agent containers.
     **/
  public static Runtime instance() {
    return theInstance;
  }

  /**
     Creates a new agent container in the current JVM, providing
     access through a proxy object.
     @parameter p the profile containing boostrap and configuration
     data for this container
     @return A proxy object, through which services can be requested
     from the real JADE container.
   */
  public jade.wrapper.AgentContainer createAgentContainer(Profile p) {
      p.setParameter(Profile.MAIN, "false"); // set to an agent container
      AgentContainerImpl impl = new AgentContainerImpl(p);
      beginContainer();
      impl.joinPlatform();
      if (enableDefaultToolkit)
	  setDefaultToolkit(impl); // FIXME: Temporary hack for JSP example
      return new jade.wrapper.AgentContainer(impl);
  }

  /**
     Creates a new main container in the current JVM, providing
     access through a proxy object.
     @parameter p the profile containing boostrap and configuration
     data for this container
     @return A proxy object, through which services can be requested
     from the real JADE main container.
   */
  public jade.wrapper.MainContainer createMainContainer(Profile p) {
      p.setParameter(Profile.MAIN, "true"); // set to a main container
      AgentContainerImpl impl = new AgentContainerImpl(p);
      beginContainer();
      impl.joinPlatform();
      if (enableDefaultToolkit)
	  setDefaultToolkit(impl); // FIXME: Temporary hack for JSP example
      return new jade.wrapper.MainContainer(impl);
  }


  // Called by jade.Boot to make the VM terminate when all the
  // containers are closed.
  public void setCloseVM(boolean flag) {
    closeVM = flag;
  }

  // Called by a starting up container.
  void beginContainer() {
    System.out.println(getCopyrightNotice());
    if(activeContainers == 0) {

      // Set up group and attributes for time critical threads
      criticalThreads = new ThreadGroup("JADE time-critical threads");
      criticalThreads.setMaxPriority(Thread.MAX_PRIORITY);

      // Initialize and start up the timer dispatcher
      theDispatcher = new TimerDispatcher();
      Thread t = new Thread(criticalThreads, theDispatcher);
      t.setPriority(criticalThreads.getMaxPriority());
      t.setName("JADE Timer dispatcher");
      //Thread t = ResourceManager.getThread(ResourceManager.CRITICAL, theDispatcher);
      theDispatcher.setThread(t);
      theDispatcher.start();

    }

    ++activeContainers;
  }

  // Called by a terminating container.
  void endContainer() {
    --activeContainers;
    if(activeContainers == 0) {
    	// If this JVM must be closed --> span another Thread that 
    	// asynchronously does it
      if(closeVM) {
      	Thread t = new Thread(new Runnable() {
      		public void run() {
      			System.out.println("Exiting now!");
						System.exit(0);
      		}
      	} );
      	t.setDaemon(false);
      	t.start();
      }
      
      // Terminate the TimerDispatcher and release its resources
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
    }
  }

  TimerDispatcher getTimerDispatcher() {
    return theDispatcher;
  }

  /********** FIXME: This is just to support the JSP example *************/
  private boolean enableDefaultToolkit = false; 
  private AgentToolkit defaultToolkit;

  private void setDefaultToolkit(AgentToolkit tk) {
    defaultToolkit = tk;
  }

  AgentToolkit getDefaultToolkit() {
    return defaultToolkit;
  }

    /**
     * @deprecated This method should not be used. It has been temporarily
     * introduced for the JSP example and it will be removed in the next
     * version of JADE
     **/
    public void enableDefaultToolkit() {
	enableDefaultToolkit=true;
    }





  /**
   * Return a String with copyright Notice, Name and Version of this version of JADE
   */
  public static String getCopyrightNotice() {
    String CVSname = "$Name$";
    String CVSdate = "$Date$";
    int colonPos = CVSname.indexOf(":");
    int dollarPos = CVSname.lastIndexOf('$');
    String name = CVSname.substring(colonPos + 1, dollarPos);
    if(name.indexOf("JADE") == -1)
    	name = "JADE snapshot";
    else 
    {
        name = name.replace('-', ' ');
	      name = name.replace('_', '.');
	      name = name.trim();
    }
    colonPos = CVSdate.indexOf(':');
    dollarPos = CVSdate.lastIndexOf('$');
    String date = CVSdate.substring(colonPos + 1, dollarPos);
    date = date.trim();
    return("    This is "+name + " - " + date+"\n    downloaded in Open Source, under LGPL restrictions,\n    at http://jade.cselt.it/\n");
  }

  /************************************************************************/
}
