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
import examples.content.ecommerceOntology.*;
import test.common.*;
import test.content.*;
import java.util.Date;

public class TestAction1 extends Test{
	private Codec c = null;
	
  public String getName() {
  	return "BasicOntology.ACTION-with-AgentAction";
  }
  public String getDescription() {
  	StringBuffer sb = new StringBuffer("Tests sending an AgentAction within a BasicOntology.ACTION");
  	return sb.toString();
  }
  public Behaviour load(Agent a, DataStore ds, String resultKey) throws TestException {
  	try {
  		Object[] args = getGroupArguments();
  		final ACLMessage msg = (ACLMessage) args[0];
  		return new SuccessExpectedInitiator(a, ds, resultKey) {
  			protected ACLMessage prepareMessage() throws Exception {
  				Sell sell = new Sell();
  				Item i = new Item();
  				i.setSerialID(35624);
  				sell.setItem(i);
  				sell.setBuyer(myAgent.getAID());
  				sell.setCreditCard(new CreditCard("VISA", 987453457, new Date()));
  	
  				Action act = new Action(myAgent.getAID(), sell);
  		
  				myAgent.getContentManager().fillContent(msg, act);
  				return msg;
  			}
  		};
  	}
  	catch (Exception e) {
  		throw new TestException("Wrong group argument", e);
  	}
  }

}
