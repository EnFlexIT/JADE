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

package test.content.tests;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.core.behaviours.*;
import jade.content.*;
import jade.content.lang.*;
import jade.content.onto.basic.*;
import jade.util.leap.*;
import test.common.*;
import test.content.*;
import test.content.testOntology.*;
import java.util.Date;

public class TestMultipleInheritance extends Test{
	private static final int SPEED = 100;
	
  public Behaviour load(Agent a, DataStore ds, String resultKey) throws TestException {
  	final Logger l = Logger.getLogger();
  	
  	try {
  		final ACLMessage msg = (ACLMessage) getGroupArgument(ContentTesterAgent.MSG_NAME);
  		return new SuccessExpectedInitiator(a, ds, resultKey) {
  			protected ACLMessage prepareMessage() throws Exception {
    			myAgent.getContentManager().registerOntology(MultipleInheritanceOntology.getInstance());
  				ACLMessage msg1 = (ACLMessage) msg.clone();
  				msg1.setOntology(MultipleInheritanceOntology.getInstance().getName());
  				
    			Camper cmp = new Camper();
    			cmp.setRooms(new ArrayList());
    			cmp.setMecPieces(new ArrayList());
    			cmp.setMaxSpeed(SPEED);
    			Exists e = new Exists(cmp);
  				myAgent.getContentManager().fillContent(msg1, e);
  				l.log("Content correctly encoded");
  				l.log(msg1.getContent());
  				return msg1;
  			}
  			
  			protected boolean checkReply(ACLMessage reply) throws Exception {
  				Exists e = (Exists) myAgent.getContentManager().extractContent(reply);
  				l.log("Content correctly decoded");
  				Camper cmp = (Camper) e.getWhat();
  				// Check inherited field
  				int speed = cmp.getMaxSpeed();
  				if (speed == SPEED) {
  					l.log("Inherited field value OK");
  				}
  				else {
  					l.log("Wrong Inherited field value: expected "+SPEED+", received "+speed); 
  					return false;
  				}
  				// Check optional empty aggregate
  				if (cmp.getRooms() == null) {
  					l.log("Optional empty aggregate OK");
  				}
  				else {
  					l.log("Wrong optional empty aggregate: expected null, received "+cmp.getRooms()); 
  					return false;
  				}
  				// Check mandatory empty aggregate
  				List pieces = cmp.getMecPieces();
  				if (pieces == null) {
  					l.log("Wrong mandatory empty aggregate: expected non-null empty, received null");
  					return false;
  				}
  				if (!pieces.isEmpty()) {
  					l.log("Wrong mandatory empty aggregate: expected non-null empty, received non-empty");
  					return false;
  				}
					l.log("Mandatory empty aggregate OK");
  				return true;
  			}
  		};
  	}
  	catch (Exception e) {
  		throw new TestException("Wrong group argument", e);
  	}
  }

}
