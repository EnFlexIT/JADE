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

//#MIDP_EXCLUDE_FILE
//#APIDOC_EXCLUDE_FILE

import jade.util.Logger;
import jade.util.leap.Iterator;

/**
 *  The <code>BlockingNodeFailureMonitor</code> class detects node failures and
 *  notifies its registered listener using RMI method calls.
 *
 * @author Giovanni Rimassa - FRAMeTech s.r.l.
 * @see jade.core.NodeFailureMonitor
 */
public class BlockingNodeFailureMonitor extends NodeFailureMonitor
  implements Runnable {

    private boolean nodeExited = false;
    private boolean stopped = false;

    public void init(Profile p, Node n, NodeEventListener nel) {
    	super.init(p, n, nel);
    }
    
    public void run() {
      listener.nodeAdded(target);
      while(!nodeExited && !stopped) {
        try {
          nodeExited = target.ping(true); // Hang on this call
          if(logger.isLoggable(Logger.INFO))
            logger.log(Logger.INFO,"PING from node " + target.getName() + " returned [" + 
                (nodeExited ? "EXIT]" : "GO ON]"));
        }
        catch(IMTPException imtpe1) { // Connection down
          if(logger.isLoggable(Logger.INFO))
            logger.log(Logger.INFO,"PING from node " + target.getName() + " exited with exception");
          
          if(!stopped) {
            listener.nodeUnreachable(target);
          }
          try {
            target.ping(false); // Try a non blocking ping to check
    
            if(logger.isLoggable(Logger.INFO))
              logger.log(Logger.INFO,"PING from node " + target.getName() + " returned OK");
            
            if(!stopped) {
              listener.nodeReachable(target);
            }
          }
          catch(IMTPException imtpe2) { // Object down
            nodeExited = true;
          }
        }
        catch(Throwable t) {
          t.printStackTrace();
        }
      } // END of while
      
      // If we reach this point without being explicitly stopped the node is no longer active
      if(!stopped) {
        listener.nodeRemoved(target);
        synchronized (this) {
          Iterator it = childNodes.iterator();
          while (it.hasNext()) {
            Node n = (Node) it.next();
            listener.nodeRemoved(n);
          }
          childNodes.clear();
        }
      }
    }

    public void stop() {
      try {
      stopped = true;
      target.interrupt();
      
      } catch(IMTPException imtpe) {
        if(logger.isLoggable(Logger.INFO))
          logger.log(Logger.INFO,"-- The node <" + target.getName() + "> is already dead --" );
        // Ignore it: the node must be dead already...
      }
    }

    public void start() {
      Thread thread = new Thread(this);
      thread.setName(target.getName()+"-failure-monitor");
      thread.start();
    }
}
