/*
  $Id$
*/

package jade.domain;

import jade.core.Behaviour;
import jade.lang.acl.ACLMessage;


interface BehaviourPrototype {

  // This method creates a copy of an AMS behaviour, passing the
  // received message and a partially filled reply message.
  public Behaviour instance(ACLMessage request, ACLMessage reply);

}

