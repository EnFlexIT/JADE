/*
  $Log$
  Revision 1.3  1998/10/04 18:01:24  rimassa
  Added a 'Log:' field to every source file.

*/

package jade.domain;

import jade.core.Behaviour;
import jade.lang.acl.ACLMessage;


interface BehaviourPrototype {

  // This method creates a copy of an AMS behaviour, passing the
  // received message and a partially filled reply message.
  public Behaviour instance(ACLMessage request, ACLMessage reply);

}

