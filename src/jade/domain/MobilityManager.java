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

import jade.core.behaviours.Behaviour;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import jade.lang.Codec;
import jade.lang.sl.SL0Codec;

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

    // Register SL0 and jade-mobility-ontology into suitable AMS tables.
    theAMS.registerLanguage(SL0Codec.NAME, new SL0Codec());
    theAMS.registerOntology(MobilityOntology.NAME, MobilityOntology.instance());

    main.registerFactory(MobilityOntology.MOVE,
			 new FipaRequestResponderBehaviour.Factory() {
				 public FipaRequestResponderBehaviour.Action create() {
				     return new MoveBehaviour();
				 }
			     });
    main.registerFactory(MobilityOntology.CLONE,
			 new FipaRequestResponderBehaviour.Factory() {
				 public FipaRequestResponderBehaviour.Action create() {
				     return new CloneBehaviour();
				 }
			     });
    main.registerFactory(MobilityOntology.WHERE_IS,
			 new FipaRequestResponderBehaviour.Factory() {
				 public FipaRequestResponderBehaviour.Action create() {
				     return new WhereIsBehaviour();
				 }
			     });
    main.registerFactory(MobilityOntology.QUERY_PLATFORM_LOCATIONS,
			 new FipaRequestResponderBehaviour.Factory() {
				 public FipaRequestResponderBehaviour.Action create() {
				     return new QPLBehaviour();
				 }
			     });

  }

  public Behaviour getMain() {
    return main;
  }


  private abstract class MobilityBehaviour extends FipaRequestResponderBehaviour.Action {

    MobilityBehaviour() {
      super(MobilityManager.this.theAMS);
    }

    protected abstract void doAction(Object o) throws FIPAException;

    public final void action() {
      Object o;
      ACLMessage msg = getRequest();
      try {
	o = theAMS.extractContent(msg);
	sendAgree();
	try {
	  doAction(o);
	}
	catch(FIPAException fe) {
	  sendFailure(fe.getMessage());
	  return;
	}
      }
      catch(FIPAException fe) {
	sendRefuse(fe.getMessage());
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

    protected void doAction(Object o) throws FIPAException {
      MobilityOntology.MoveAction action = (MobilityOntology.MoveAction)o;
      MobilityOntology.MobileAgentDescription desc = action.get_0();

      String agentName = desc.getName();
      MobilityOntology.Location destination = desc.getDestination();
      theAMS.AMSMoveAgent(agentName, destination);
      sendInform();
    }

  }

  private class CloneBehaviour extends MobilityBehaviour {

    protected void doAction(Object o) throws FIPAException {
      MobilityOntology.CloneAction action = (MobilityOntology.CloneAction)o;
      MobilityOntology.MobileAgentDescription desc = action.get_0();

      String agentName = desc.getName();
      MobilityOntology.Location destination = desc.getDestination();
      String newName = action.get_1();
      theAMS.AMSCloneAgent(agentName, destination, newName);
      sendInform();
    }

  }

  private class WhereIsBehaviour extends MobilityBehaviour {

    protected void doAction(Object o) throws FIPAException {
      MobilityOntology.WhereIsAgentAction action = (MobilityOntology.WhereIsAgentAction)o;

      String agentName = action.get_0();
      MobilityOntology.Location where = theAMS.AMSWhereIsAgent(agentName);

      ACLMessage reply = getReply();
      reply.setPerformative(ACLMessage.INFORM);
      reply.setLanguage(SL0Codec.NAME);
      reply.setOntology(MobilityOntology.NAME);
      theAMS.fillContent(reply, where, MobilityOntology.LOCATION);
      theAMS.send(reply);
    }

  }

  private class QPLBehaviour extends MobilityBehaviour {

    protected void doAction(Object o) throws FIPAException {
      MobilityOntology.QueryPlatformLocationsAction action = (MobilityOntology.QueryPlatformLocationsAction)o;
      MobilityOntology.Location[] locations = theAMS.AMSGetPlatformLocations();
      String content = new String();
      for(int i = 0; i < locations.length; i++) {
	try {
	  Ontology mob = MobilityOntology.instance();
	  Codec c = theAMS.lookupLanguage(SL0Codec.NAME);

	  Frame f = mob.createFrame(locations[i], MobilityOntology.LOCATION);
	  String s = c.encode(f, mob);
	  content = content + s + "||"; // FIXME: Hand-made separator
	}
	catch(OntologyException oe) {
	  oe.printStackTrace();
	  sendFailure(oe.getMessage());
	  return;
	}
      }

      // Remove last separator
      content = content.substring(0, content.length() - 2);

      // Use ByteLengthEncoded format
      content = "#" + content.length() + "\"" + content;

      ACLMessage reply = getReply();
      reply.setPerformative(ACLMessage.INFORM);
      reply.setLanguage(SL0Codec.NAME);
      reply.setOntology(MobilityOntology.NAME);
      reply.setContent(content);
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

  public MobilityOntology.Location[] getLocations() {
    Object[] content = locations.values().toArray();
    MobilityOntology.Location[] result = new MobilityOntology.Location[content.length];
    System.arraycopy(content, 0, result, 0, result.length);
    return result;
  }

}
