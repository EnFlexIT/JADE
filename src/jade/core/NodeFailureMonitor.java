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

   The <code>NodeFailureMonitor</code> class detects node failures and
   service manager <i>Service Manager</i> and <i>Service Finder</i>
   implementation.

   @author Giovanni Rimassa - FRAMeTech s.r.l.

   @see jade.core.ServiceManagerImpl
*/
public class NodeFailureMonitor implements Runnable {

    private Node target;
    private NodeEventListener listener;
    private boolean active = true;

    public NodeFailureMonitor(Node n, NodeEventListener nel) {
      target = n;
      listener = nel;
    }

    public void run() {
      while(active) {
	  try {
	      target.ping(true); // Hang on this call
	      active = false;
	      System.out.println("PING from node "+ target.getName() + " returned normally");
	  }
	  catch(IMTPException imtpe1) { // Connection down
	      System.out.println("PING from node " + target.getName() + " exited with exception");
	      listener.nodeUnreachable(target);
	      try {
		  target.ping(false); // Try a non blocking ping to check

		  System.out.println("PING from node " + target.getName() + " returned OK");
		  listener.nodeReachable(target);
	      }
	      catch(IMTPException imtpe2) { // Object down
		  active = false;
	      }
	  }
	  catch(Throwable t) {
	      t.printStackTrace();
	  }
      } // END of while
      
      // If we reach this point the node is no longer active
      listener.nodeRemoved(target);

      }
  
  }
