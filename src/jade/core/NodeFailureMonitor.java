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

//#APIDOC_EXCLUDE_FILE

import jade.util.Logger;
import jade.util.leap.LinkedList;
import jade.util.leap.List;

/**
 * The abstract class <code>NodeFailureMonitor</code> provides a basic implementation
 * for all classes that supervise nodes and detect node failures.
 * 
 * The monitor can only supervise a single node. If there are additional nodes in <br />
 * the same JVM, you can add these as child nodes. A child node is not supervised <br />
 * directly. Instead it has always the same state than its parent node. 
 * So if the parent node gets unreachable automatically all its child nodes will 
 * turn to the state unreachable.
 * 
 * @author Roland Mungenast - Profactor
 * @see jade.core.BlockingNodeFailureMonitor
 * @see jade.core.UDPNodeFailureMonitor
 * @see jade.core.NodeEventListener
 */
public abstract class NodeFailureMonitor {

  protected Node target;
  protected NodeEventListener listener;
  protected List childNodes = new LinkedList();
  protected Logger logger = Logger.getMyLogger(this.getClass().getName());

  public static NodeFailureMonitor getFailureMonitor(Profile p, Node n, NodeEventListener listener) {
  	try {
  		String className = (p.getBooleanProperty(Profile.UDP_MONITORING, false) ? "jade.core.UDPNodeFailureMonitor" : "jade.core.BlockingNodeFailureMonitor");
  		NodeFailureMonitor monitor = (NodeFailureMonitor) Class.forName(className).newInstance();
			monitor.init(p, n, listener);
			return monitor;
  	}
  	catch (Throwable t) {
  		// FIXME: Properly hand;le the exception
  		t.printStackTrace();
  		return null;
  	}
  }
  
  /**
   * Constructor
   * @param n target node to monitor
   * @param nel listener to inform about new events
   */
  public void init(Profile p, Node n, NodeEventListener nel) {
    target = n;
    listener = nel;
  }
  
  /**
   * Adds a child node for monitoring. 
   * @param n child node
   */
  public synchronized void addChild(Node n) {
    childNodes.add(n);
  }
  
  /**
   * Removes a child node from monitoring
   * @param n child node
   */
  public synchronized void removeChild(Node n) {
    childNodes.remove(n);
  }
  
  /**
   * Returns the monitored target node
   */
  public Node getNode() {
    return target;
  }
  
  /**
   * Starts the monitoring
   */
  public abstract void start();
 
  /**
   * Stops the monitoring
   */
  public abstract void stop();
  
}
