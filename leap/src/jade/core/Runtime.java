/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

/**
 * ***************************************************************
 * The LEAP libraries, when combined with certain JADE platform components,
 * provide a run-time environment for enabling FIPA agents to execute on
 * lightweight devices running Java. LEAP and JADE teams have jointly
 * designed the API for ease of integration and hence to take advantage
 * of these dual developments and extensions so that users only see
 * one development platform and a
 * single homogeneous set of APIs. Enabling deployment to a wide range of
 * devices whilst still having access to the full development
 * environment and functionalities that JADE provides.
 * 
 * GNU Lesser General Public License
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation,
 * version 2.1 of the License.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 * **************************************************************
 */

package jade.core;

import jade.util.leap.LinkedList;

/**
 * This class is a Singleton class, allowing intial access to the JADE
 * runtime system.
 * 
 * @author Giovanni Rimassa - Universita` di Parma
 * @author Giovanni Caire - TILAB
 * 
 */
public class Runtime {

  /**
   * should gc() be called?
   */
  private boolean        runGC = false;

  /**
   * should gc() display debug messages?
   */
  private boolean        debugGC = false;

  private static Runtime theInstance;

  static {
    theInstance = new Runtime();
  } 

  private TimerDispatcher theDispatcher;
  private LinkedList      terminators = new LinkedList();

  // The Profile of the unique Container this "micro" Runtime is able to start
  private Profile         theProfile;

  /**
   * Private constructor to forbid instantiation outside the class.
   */
  private Runtime() {
    // Do nothing
  } 

  /**
   * Return the singleton instance of Runtime
   */
  public static Runtime instance() {
    return theInstance;
  } 

  /**
   * Launch a JADE container configured according to the given
   * Profile
   */
  public void startUp(Profile p) {
    // check whether gc should be called
    if (CaseInsensitiveString.equalsIgnoreCase("true", p.getParameter("rungc", "false"))) {
      runGC = true;
    } 

    // check whether gc debug messages should be shown
    if (CaseInsensitiveString.equalsIgnoreCase("true", p.getParameter("debuggc", "false"))) {
      debugGC = true;
    } 

    // Print the JADE copyright notice
    System.out.println(getCopyrightNotice());

    // Initialize and start up the timer dispatcher
    theDispatcher = new TimerDispatcher();

    Thread t = new Thread(theDispatcher);
    t.setPriority(Thread.MAX_PRIORITY);
    theDispatcher.setThread(t);
    theDispatcher.start();

    // Create the container and launch it
    AgentContainerImpl impl = new AgentContainerImpl(p);
    impl.joinPlatform();

    theProfile = p;
  } 


  /**
   * Called by the terminating container.
   */
  void endContainer() {
    // Span another Thread that asynchronously
    // executes the "terminators" registered to this Runtime
    // and then notify midlet destruction
    // Thread t = new Thread(new Terminator(Thread.currentThread()));
    // t.start();
    Thread t = new Thread(new Runnable() {

      /**
       * Method declaration
       *
       * @see
       */
      public void run() {
        for (int i = 0; i < terminators.size(); ++i) {
          Runnable r = (Runnable) terminators.get(i);
          r.run();
        } 
        jade.Boot.midlet.notifyDestroyed();
      } 

    });
    t.start();
  } 

  /**
   * Return the singleton TimerDispatcher
   */
  public TimerDispatcher getTimerDispatcher() {
    return theDispatcher;
  } 

  /**
   * Return a String with copyright Notice, Name and Version of this version of JADE
   */
  public static String getCopyrightNotice() {
    String CVSname = "$Name$";
    String CVSdate = "$Date$";
    int    colonPos = CVSname.indexOf(":");
    int    dollarPos = CVSname.lastIndexOf('$');
    String name = CVSname.substring(colonPos+1, dollarPos);
    if (name.indexOf("JADE") == -1) {
      name = "JADE snapshot";
    } 
    else {
      name = name.replace('-', ' ');
      name = name.replace('_', '.');
      name = name.trim();
    } 

    colonPos = CVSdate.indexOf(':');
    dollarPos = CVSdate.lastIndexOf('$');

    String date = CVSdate.substring(colonPos+1, dollarPos);
    date = date.trim();

    return ("    This is "+name+" - "+date+"\n    downloaded in Open Source, under LGPL restrictions,\n    at http://jade.cselt.it/\n");
  } 


  /**
   * Call the garbage collector and print out memory usage depending
   * on whether the <code>runGC</code> and <code>debugGC</code> flags
   * are set.
   */
  public void gc(String s) {
    if (!runGC) {
      return;
    } 

    java.lang.Runtime rt = java.lang.Runtime.getRuntime();

    if (debugGC) {
      System.out.print(s+": "+((rt.totalMemory()-rt.freeMemory())/1024)+"K");
      System.out.flush();
    } 

    rt.gc();

    if (debugGC) {
      System.out.println("; "+((rt.totalMemory()-rt.freeMemory())/1024)+"K");
    } 
  } 

  /**
   * Allwos setting a <code>Runnable</code> that is executed when
   * the last container in this JVM is terminated.
   */
  public void invokeOnTermination(Runnable r) {
    terminators.addFirst(r);
  } 

  /**
   * Inner class to asynchronously terminate the local JVM
   * @author LEAP
   * 
   * private class Terminator implements Runnable {
   * private Thread initiator;
   * 
   * 
   * 
   * public Terminator(Thread t) {
   * initiator = t;
   * }
   */

  /**
   * Terminator thread entry point
   * 
   * public void run() {
   * try {
   * initiator.join();
   * }
   * catch (InterruptedException ie) {
   * ie.printStackTrace();
   * }
   * 
   * // Close all intra-platform links
   * try {
   * theProfile.getIMTPManager().shutDown();
   * }
   * catch (ProfileException pe) {
   * // Do nothing as we are exiting
   * }
   * 
   * // DEBUG
   * System.out.println("Exiting now!");
   * System.out.flush();
   * // not the good way to close the Midlet (generates a SecurityException)
   * //System.exit(0);
   * // this one is better:
   * jade.Boot.midlet.notifyDestroyed();
   * 
   * }
   * 
   * }
   */
}

