/*
  $Log$
  Revision 1.1  1999/03/17 13:12:19  rimassa
  A class representing a remote agent address, embedding the specific
  communication protocol and the information to reach the agent. This class
  is abstract and all its concrete subclasses are serializable, in order to
  be exchanged among containers via RMI.

*/

package jade.core;

import java.io.Serializable;

abstract class RemoteProxy implements AgentProxy, Serializable {

  public abstract void ping() throws UnreachableException;

}

