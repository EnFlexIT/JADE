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

import java.util.LinkedList;

import jade.core.AID;
import jade.core.Location;


/**

   This class is a Proxy class, allowing access to a JADE agent.
   Invoking methods on instances of this class, it is possible to
   trigger state transition of the agent life cycle.  This class must
   not be instantiated by applications. Instead, use the
   <code>createAgent()</code> method in class
   <code>AgentContainer</code>.
   @see AgentContainer.createAgent()

   @author Giovanni Rimassa - Universita` di Parma
 */
public class Agent {

  private AID agentID;
  private jade.core.Agent adaptee;

  /**
     Public constructor. This should not be called by applications,
     but the method <code>AgentContainer.createAgent()</code> should
     be used instead.

     @see AgentContainer#createAgent(AID agentID, String className, String[] args)
     @param a A real JADE agent, that will be wrapped by this proxy.
   */
  public Agent(AID id, jade.core.Agent a) {
    agentID = id;
    adaptee = a;
  }


  /**
     Triggers a state transition from <b>INITIATED</b> to
     <b>ACTIVE</b>. This call also starts the internal agent
     thread. If this call is performed on an already started agent,
     nothing happens.
   */
  public void start() {
    adaptee.doStart(agentID.getLocalName());
  }

  /**
     Triggers a state transition from <b>ACTIVE</b> to
     <b>SUSPENDED</b>.
   */
  public void suspend() {
    adaptee.doSuspend();    
  }

  /**
     Triggers a state transition from <b>SUSPENDED</b> to
     <b>ACTIVE</b>.
   */
  public void activate() {
    adaptee.doActivate();
  }

  /**
     Triggers a state transition from <b>ACTIVE</b> to
     <b>DELETED</b>. This call also stops the internal agent thread
     and fully terminates the agent. If this call is performed on an
     already terminated agent, nothing happens.
   */
  public void delete() {
    adaptee.doDelete();
  }

  /**
     Triggers a state transition from <b>ACTIVE</b> to
     <b>TRANSIT</b>. This call also moves the agent code and data to
     another container. This calls terminates the locally running
     agent, so that this proxy object becomes detached from the moved
     agent that keeps on executing elsewhere (i.e., no proxy
     remotization is performed.

     @param where A <code>Location</code> object, representing the
     container the agent should move to.
  */
  public void move(Location where) {
    adaptee.doMove(where);
  }


  /**
     Clones the current agent. Calling this method does not really
     trigger a state transition in the current agent
     lifecycle. Rather, it creates another agent on the given
     location, that is just a copy of this agent.

     @param where The <code>Location</code> object, representing the
     container where the new agent copy will start.
     @param newName The new nickname to give to the copy.
   */
  public void clone(Location where, String newName) {
    adaptee.doClone(where, newName);
  }

}
