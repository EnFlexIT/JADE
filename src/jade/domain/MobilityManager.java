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

import jade.content.onto.basic.Action;

import jade.content.onto.basic.Result;

import jade.proto.SimpleAchieveREResponder;

import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.UnsupportedFunction;
import jade.domain.FIPAAgentManagement.Unauthorised;

import jade.domain.mobility.*;
import jade.domain.JADEAgentManagement.*;

import jade.security.AuthException;

/**
	 This behaviour manages all the mobility-related features of the AMS.

	Javadoc documentation for the file
	@author Giovanni Rimassa - Universita` di Parma
	@author Tiziana Trucco - Tilab
	@version $Date$ $Revision$

 */

class MobilityManager {


    /* class MobilityManagerResponderB extends DFResponderBehaviour{

	private ACLMessage res;


	MobilityManagerResponderB(Agent a, MessageTemplate mt){
	    super(a,mt);
	}

	protected ACLMessage prepareResponse(ACLMessage request) throws NotUnderstoodException, RefuseException{

	    Action SLAction = null;
	    Object action = null;
	    MobileAgentDescription description;

	    res = request.createReply();
	    res.setPerformative(ACLMessage.INFORM);
	    try {
		//extract the content of the message this could throws a FIPAException
		SLAction = (Action)myAgent.getContentManager().extractContent(request);
		action = SLAction.getAction();
		if(action instanceof WhereIsAgentAction){
		
		    AID agentN = ((WhereIsAgentAction)action).get_0();
		    Location where = theAMS.AMSWhereIsAgent(agentN, request.getSender());
		    Result r = new Result();
		    r.setAction(SLAction);
		    List l1 = new ArrayList();
		    l1.add(where);
		    r.setItems(l1);
		    try {
		    	theAMS.getContentManager().fillContent(res, r);
		  	} catch (Exception e) {
		  		System.out.println(e);
		  	}

		}else if(action instanceof QueryPlatformLocationsAction){
	
		    Result r2 = new Result();
		    r2.setAction(SLAction);
		    
		    // Mutual exclusion with addLocation() and removeLocation() methods
		    synchronized(locations) {
			Iterator it = locations.values().iterator();
			List l1 = new ArrayList();
			while(it.hasNext())
				l1.add(it.next());
			r2.setItems(l1);
		    }
		    try {
		    	theAMS.getContentManager().fillContent(res, r2); 
		  	} catch (Exception e) {
		  		System.out.println(e);
		  	}
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
	    } catch (Exception e) {
	    	System.out.println("errore");
	    	System.exit(0);
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
    } */

	private ams theAMS;
	public Map locations;
   
    // private MobilityManagerResponderB main;

	public MobilityManager(ams a) {
		theAMS = a;
		locations = new HashMap();
		MessageTemplate mt = 
		MessageTemplate.and(MessageTemplate.MatchLanguage("FIPA-SL0"),
		MessageTemplate.MatchOntology(jade.domain.mobility.MobilityOntology.NAME));

		mt = MessageTemplate.or(mt,MessageTemplate.MatchOntology(JADEManagementOntology.NAME));
		mt = MessageTemplate.and(mt,MessageTemplate.MatchPerformative(ACLMessage.REQUEST));
		
		// main = new MobilityManagerResponderB(theAMS,mt);		
	}

	/*public Behaviour getMain() {
		return main;
	}*/

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

	public List getLocations() {
		List l1 = new ArrayList();
		synchronized(locations) {
			Iterator it = locations.values().iterator();
			while(it.hasNext())
				l1.add(it.next());
		}
		return l1;
	}

	public Location getLocation(String containerName) {
		return (Location)locations.get(new CaseInsensitiveString(containerName));
	}
}
