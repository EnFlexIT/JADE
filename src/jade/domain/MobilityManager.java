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
