/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop 
multi-agent systems in compliance with the FIPA specifications.
Copyright (C) 2000 CSELT S.p.A. 

The updating of this file to JADE 2.0 has been partially supported by the IST-1999-10211 LEAP Project

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

import jade.util.leap.Map;
import jade.util.leap.HashMap;
import jade.util.leap.List;
import jade.util.leap.ArrayList;
import jade.util.leap.Iterator;

import jade.core.AID;
import jade.core.Location;
import jade.core.CaseInsensitiveString;
import jade.core.Agent;

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

import jade.proto.SimpleAchieveREResponder;

import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.UnsupportedFunction;
import jade.domain.FIPAAgentManagement.Unauthorised;

import jade.security.AuthException;

/**
	 This behaviour manages all the mobility-related features of the AMS.

	Javadoc documentation for the file
	@author Giovanni Rimassa - Universita` di Parma
	@author Tiziana Trucco - Tilab
	@version $Date$ $Revision$

 */

class MobilityManager {


    class MobilityManagerResponderB extends DFResponderBehaviour{

	private ACLMessage res;


	MobilityManagerResponderB(Agent a, MessageTemplate mt){
	    super(a,mt);
	}

	protected ACLMessage prepareResponse(ACLMessage request) throws NotUnderstoodException, RefuseException{

	    Action SLAction = null;
	    Object action = null;
	    MobilityOntology.MobileAgentDescription description;

	    res = request.createReply();
	    res.setPerformative(ACLMessage.INFORM);
	    try {
		//extract the content of the message this could throws a FIPAException
		List l = myAgent.extractMsgContent(request);
		SLAction = (Action)l.get(0);
		action = SLAction.getAction();
		if(action instanceof MobilityOntology.MoveAction){
		    if(action instanceof MobilityOntology.CloneAction){
			description = ((MobilityOntology.CloneAction)action).get_0(); 
			AID aName = description.getName();
			Location dest = description.getDestination();
			String newName = ((MobilityOntology.CloneAction)action).get_1();
			theAMS.AMSCloneAgent(aName, dest, newName, request.getSender());
			// res.setContent("FIXME");
			res.setContent(createInformDoneContent(SLAction,request.getLanguage(),request.getOntology()));
		    }else{
		
			description = ((MobilityOntology.MoveAction)action).get_0();
			AID agentName = description.getName();
			Location destination = description.getDestination();
			theAMS.AMSMoveAgent(agentName, destination, request.getSender());
			//res.setContent("FIXME");
			res.setContent(createInformDoneContent(SLAction,request.getLanguage(),request.getOntology()));
		    }		   
		}else if(action instanceof MobilityOntology.WhereIsAgentAction){
		
		    AID agentN = ((MobilityOntology.WhereIsAgentAction)action).get_0();
		    Location where = theAMS.AMSWhereIsAgent(agentN, request.getSender());
		    ResultPredicate r = new ResultPredicate();
		    r.set_0(SLAction);
		    r.add_1(where);
		    List l3 = new ArrayList(1);
		    l3.add(r);
		    theAMS.fillMsgContent(res, l3);
		}else if(action instanceof MobilityOntology.QueryPlatformLocationsAction){
		
	
		    ResultPredicate r2 = new ResultPredicate();
		    r2.set_0(SLAction);
		    
		    // Mutual exclusion with addLocation() and removeLocation() methods
		    synchronized(locations) {
			Iterator it = locations.values().iterator();
			while(it.hasNext())
			    r2.add_1(it.next());
		    }
		    List l2 = new ArrayList();
		    l2.add(r2);
		    theAMS.fillMsgContent(res, l2); 
		}else{
		    //this case should never occur since if the action does not exist the extract content throws a Ontology Exception.
		    //FIXME: the UnsupportedFunction exception requires as parameter the name of the unsupported function.
		    //how can we retrive this name ?
		    UnsupportedFunction uf = new UnsupportedFunction();
		    createExceptionalMsgContent(SLAction,uf,request);
		    throw uf;
		}
		
	    }catch(FailureException fe){
		createExceptionalMsgContent(SLAction,fe,request);
		res.setPerformative(ACLMessage.FAILURE);
		res.setContent(fe.getMessage());

	    }catch(AuthException au){
		Unauthorised un = new Unauthorised();
		createExceptionalMsgContent(SLAction,un,request);
		throw un;
	    }catch(FIPAException fe){
		//FIXME: verify the content.
		RefuseException re = new RefuseException(fe.getMessage());
		createExceptionalMsgContent(SLAction,re,request);
		throw re;
	    }
		//if everything is OK returns an AGREE message.
		ACLMessage agree = request.createReply();
		agree.setPerformative(ACLMessage.AGREE);
		//agree.setContent("(true)");
		agree.setContent(createAgreeContent(SLAction,request.getLanguage(),request.getOntology()));
		return agree;		
	}


	protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) throws FailureException{
	    return res;
	}

	//to reset the action
	public void reset(){
	    super.reset();
	    res = null;
	}
    }

	private ams theAMS;
	private Map locations;
   
        private MobilityManagerResponderB main;

	public MobilityManager(ams a) {
		theAMS = a;
		locations = new HashMap();
		MessageTemplate mt = 
			MessageTemplate.and(MessageTemplate.MatchLanguage(SL0Codec.NAME),
				MessageTemplate.MatchOntology(MobilityOntology.NAME));

		mt = MessageTemplate.and(mt,MessageTemplate.MatchPerformative(ACLMessage.REQUEST));

		main = new MobilityManagerResponderB(theAMS,mt);		
	}

	public Behaviour getMain() {
		return main;
	}

	public void addLocation(String name, Location l) {
		synchronized (locations) {
			locations.put(new CaseInsensitiveString(name), l);
		}
	}

	public void removeLocation(String name) {
		synchronized (locations) {
			locations.remove(new CaseInsensitiveString(name));
		}
	}

	public Location getLocation(String containerName) {
		return (Location)locations.get(new CaseInsensitiveString(containerName));
	}
}
