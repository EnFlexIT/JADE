/*
  $Id$
*/

package jade.domain;

import java.util.StringTokenizer;

import jade.core.Behaviour;
import jade.lang.acl.ACLMessage;


interface BehaviourPrototype {

  // This method creates a copy of an AMS behaviour, passing the
  // reply message and a StringTokenizer that is reading the content
  // of a received message.
  public Behaviour instance(ACLMessage msg, StringTokenizer st);

}

