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



package examples.mobile;

import java.util.*;

import jade.proto.*;
import jade.lang.acl.*;

import jade.domain.MobilityOntology;
import jade.domain.FIPAException;

import jade.lang.Codec;
import jade.lang.sl.SL0Codec;

import jade.core.*;
import jade.onto.OntologyException;
import jade.onto.basic.Action;
import jade.onto.basic.ResultPredicate;

  /*
   * This behaviour extends FipaRequestInitiatorBehaviour in order
   * to request to the AMS the list of available locations where
   * the agent can move.
   * Then, it displays these locations into the GUI
   * @author Fabio Bellifemine - CSELT S.p.A.
   * @version $Date$ $Revision$
   */
public class GetAvailableLocationsBehaviour extends FipaRequestInitiatorBehaviour {

    private ACLMessage request;
  private static MessageTemplate template = MessageTemplate.MatchLanguage(SL0Codec.NAME);

   public GetAvailableLocationsBehaviour(MobileAgent a) {
     // call the constructor of FipaRequestInitiatorBehaviour
     super(a, new ACLMessage(ACLMessage.REQUEST), template);
     request = getRequest();
     // fills all parameters of the request ACLMessage
     request.clearAllReceiver();
     request.addReceiver(a.getAMS());
     request.setLanguage(SL0Codec.NAME);
     request.setOntology(MobilityOntology.NAME);
     request.setProtocol("fipa-request");
     // creates the content of the ACLMessage
     try {
       Action action = new Action();
       action.setActor(a.getAMS());
       action.setAction(new MobilityOntology.QueryPlatformLocationsAction());
       List tuple = new ArrayList();
       tuple.add(action);
       a.fillContent(request, tuple);
     }
     catch(FIPAException fe) {
       fe.printStackTrace();
     }
     // creates the Message Template
     template = MessageTemplate.and(MessageTemplate.MatchOntology(MobilityOntology.NAME),template);
     // reset the fiparequestinitiatorbheaviour in order to put new values
     // for the request aclmessage and the template
     reset(request,template);
   }

   protected void handleNotUnderstood(ACLMessage reply) {
     System.out.println(myAgent.getLocalName()+ " handleNotUnderstood : "+reply.toString());
   }

   protected void handleRefuse(ACLMessage reply) {
     System.out.println(myAgent.getLocalName()+ " handleRefuse : "+reply.toString());
   }

   protected void handleFailure(ACLMessage reply) {
     System.out.println(myAgent.getLocalName()+ " handleFailure : "+reply.toString());
   }

   protected void handleAgree(ACLMessage reply) {
   }

   protected void handleInform(ACLMessage inform) {
     String content = inform.getContent();
     System.out.println(inform.toString());
     try {
       List tuple = myAgent.extractContent(inform);
       ResultPredicate r = (ResultPredicate)tuple.get(0);
       //update the GUI
       ((MobileAgent)myAgent).gui.updateLocations(r.getAll_1());
     }
     catch(Exception e) {
       e.printStackTrace();
     }
   }

}
