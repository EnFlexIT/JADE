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

package jade.core.event;

import jade.core.AID;
import jade.core.ContainerID;

/**
   This class represents an event related to the platform life cycle
   and configuration.

   @author Giovanni Rimassa - Universita` di Parma
   @version $Date$ $Revision$
 */
public class PlatformEvent extends JADEEvent {

  public static final int ADDED_CONTAINER = 1;
  public static final int REMOVED_CONTAINER = 2;
  public static final int BORN_AGENT = 3;
  public static final int DEAD_AGENT = 4;
  public static final int MOVED_AGENT = 5;

  private int myID; // The actual type of the event
  private ContainerID container;
  private ContainerID newContainer;
  private AID agent;

  public PlatformEvent(int id, ContainerID cid) {
    super(null); // FIXME: must put the location of the Main Container
    myID = id;
    if(!isContainerBD()) {
      throw new InternalError("Bad event kind: it must be a container related kind.");
    }
    container = cid;
    newContainer = null;
    agent = null;
  }

  public PlatformEvent(int id, AID aid, ContainerID cid) {
    super(null); // FIXME: must put the location of the Main Container
    myID = id;
    if(!isAgentBD()) {
      throw new InternalError("Bad event kind: it must be an agent related kind.");
    }
    agent = aid;
    container = cid;
    newContainer = null;
  }

  public PlatformEvent(AID aid, ContainerID from, ContainerID to) {
    super(null); // FIXME: must put the location of the Main Container
    myID = MOVED_AGENT;
    agent = aid;
    container = from;
    newContainer = to;
  }

  public ContainerID getContainer() {
    return container;
  }

  public ContainerID getNewContainer() {
    return newContainer;
  }

  public AID getAgent() {
    return agent;
  }

  public boolean isContainerBD() {
    return (myID == ADDED_CONTAINER) || (myID == REMOVED_CONTAINER);
  }

  public boolean isAgentBD() {
    return (myID == BORN_AGENT) || (myID == DEAD_AGENT);
  }

}
