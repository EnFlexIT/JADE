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

package jade.domain;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import jade.core.AID;
import jade.core.behaviours.Behaviour;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import jade.lang.Codec;
import jade.lang.sl.SL0Codec;

import jade.onto.basic.Action;
import jade.onto.basic.ResultPredicate;
import jade.onto.Frame;
import jade.onto.Ontology;
import jade.onto.OntologyException;
import jade.onto.Name;

import jade.proto.FipaRequestResponderBehaviour;

/**
   This behaviour manages all the mobility-related features of the AMS.

  Javadoc documentation for the file
  @author Giovanni Rimassa - Universita` di Parma
  @version $Date$ $Revision$

 */

class MobilityManager {

  private ams theAMS;
  private Map locations;
  private FipaRequestResponderBehaviour main;

  public MobilityManager(ams a) {
    theAMS = a;
    locations = new HashMap();
    MessageTemplate mt = 
      MessageTemplate.and(MessageTemplate.MatchLanguage(SL0Codec.NAME),
			  MessageTemplate.MatchOntology(MobilityOntology.NAME));
    main = new FipaRequestResponderBehaviour(theAMS, mt);

    main.registerFactory(MobilityOntology.MOVE,
			 new FipaRequestResponderBehaviour.Factory() {
				 public FipaRequestResponderBehaviour.ActionHandler create(ACLMessage msg) {
				     return new MoveBehaviour(msg);
				 }
			     });
    main.registerFactory(MobilityOntology.CLONE,
			 new FipaRequestResponderBehaviour.Factory() {
				 public FipaRequestResponderBehaviour.ActionHandler create(ACLMessage msg) {
				     return new CloneBehaviour(msg);
				 }
			     });
    main.registerFactory(MobilityOntology.WHERE_IS,
			 new FipaRequestResponderBehaviour.Factory() {
				 public FipaRequestResponderBehaviour.ActionHandler create(ACLMessage msg) {
				     return new WhereIsBehaviour(msg);
				 }
			     });
    main.registerFactory(MobilityOntology.QUERY_PLATFORM_LOCATIONS,
			 new FipaRequestResponderBehaviour.Factory() {
				 public FipaRequestResponderBehaviour.ActionHandler create(ACLMessage msg) {
				     return new QPLBehaviour(msg);
				 }
			     });

  }

  public Behaviour getMain() {
    return main;
  }


  private abstract class MobilityBehaviour extends FipaRequestResponderBehaviour.ActionHandler {

    MobilityBehaviour(ACLMessage msg) {
      super(MobilityManager.this.theAMS,msg);
    }

    protected abstract void doAction(Action a) throws FIPAException;

    public final void action() {
      Object o;
      ACLMessage msg = getRequest();
      try {
	List l = theAMS.extractContent(msg);
	Action a = (Action)l.get(0);
	sendReply(ACLMessage.AGREE,"(true)");
	try {
	  doAction(a);
	}
	catch(FIPAException fe) {
	  sendReply(ACLMessage.FAILURE,"("+fe.getMessage()+")");
	  return;
	}
      }
      catch(FIPAException fe) {
	sendReply(ACLMessage.REFUSE,"("+fe.getMessage()+")");
      }
    }

    public final boolean done() {
      return true;
    }

    public final void reset() {
      // Empty
    }

  } // End of MobilityBehaviour class


  private class MoveBehaviour extends MobilityBehaviour {
    public MoveBehaviour(ACLMessage msg) {
      super(msg);
    }
    protected void doAction(Action a) throws FIPAException {
      MobilityOntology.MoveAction action = (MobilityOntology.MoveAction)a.get_1();
      MobilityOntology.MobileAgentDescription desc = action.get_0();

      AID agentName = desc.getName();
      MobilityOntology.Location destination = desc.getDestination();
      theAMS.AMSMoveAgent(agentName, destination);
      sendReply(ACLMessage.INFORM,"FIXME");
    }

  }

  private class CloneBehaviour extends MobilityBehaviour {
    public CloneBehaviour(ACLMessage msg) {
      super(msg);
    }
    protected void doAction(Action a) throws FIPAException {
      MobilityOntology.CloneAction action = (MobilityOntology.CloneAction)a.get_1();
      MobilityOntology.MobileAgentDescription desc = action.get_0();

      AID agentName = desc.getName();
      MobilityOntology.Location destination = desc.getDestination();
      String newName = action.get_1();
      theAMS.AMSCloneAgent(agentName, destination, newName);
      sendReply(ACLMessage.INFORM,"FIXME");
    }

  }

  private class WhereIsBehaviour extends MobilityBehaviour {
    public WhereIsBehaviour(ACLMessage msg) {
      super(msg);
    }
    protected void doAction(Action a) throws FIPAException {
      MobilityOntology.WhereIsAgentAction action = (MobilityOntology.WhereIsAgentAction)a.get_1();

      AID agentName = action.get_0();
      MobilityOntology.Location where = theAMS.AMSWhereIsAgent(agentName);

      ACLMessage reply = getReply();
      reply.setPerformative(ACLMessage.INFORM);
      reply.setLanguage(SL0Codec.NAME);
      reply.setOntology(MobilityOntology.NAME);
      ResultPredicate r = new ResultPredicate();
      r.set_0(a);
      r.add_1(where);
      List l = new ArrayList(1);
      l.add(r);
      theAMS.fillContent(reply, l);
      theAMS.send(reply);
    }

  }

  private class QPLBehaviour extends MobilityBehaviour {
    public QPLBehaviour(ACLMessage msg) {
      super(msg);
    }
    protected void doAction(Action a) throws FIPAException {
      MobilityOntology.QueryPlatformLocationsAction action = (MobilityOntology.QueryPlatformLocationsAction)a.get_1();
      Iterator locations = theAMS.AMSGetPlatformLocations();

      ACLMessage reply = getReply();
      reply.setPerformative(ACLMessage.INFORM);
      reply.setLanguage(SL0Codec.NAME);
      reply.setOntology(MobilityOntology.NAME);
      ResultPredicate r = new ResultPredicate();
      r.set_0(a);
      for (; locations.hasNext(); )
      	r.add_1(locations.next());
      List l = new ArrayList();
      l.add(r);
      theAMS.fillContent(reply,l); 
      theAMS.send(reply);
    }

  }

  public void addLocation(String name, MobilityOntology.Location l) {
    locations.put(new Name(name), l);
  }

  public void removeLocation(String name) {
    locations.remove(new Name(name));
  }

  public MobilityOntology.Location getLocation(String containerName) {
    return (MobilityOntology.Location)locations.get(new Name(containerName));
  }

  public Iterator getLocations() {
    return locations.values().iterator();
  }

}
