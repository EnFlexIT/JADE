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
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.content.*;
import jade.content.onto.basic.*;
import test.common.*;
import test.content.*;
import examples.content.ecommerceOntology.*;
import java.util.Date;

public class TestContentElementList extends Test{

  public Behaviour load(Agent a, DataStore ds, String resultKey) throws TestException {
  	final Logger l = Logger.getLogger();
  	
  	try {
  		final ACLMessage msg = (ACLMessage) getGroupArgument(ContentTesterAgent.MSG_NAME);
  		return new SuccessExpectedInitiator(a, ds, resultKey) {
  			protected ACLMessage prepareMessage() throws Exception {
  				msg.setPerformative(ACLMessage.PROPOSE);
  				ContentElementList cel = new ContentElementList();
  				Sell sell = new Sell();
  				Item i = new Item();
  				i.setSerialID(35624);
  				sell.setItem(i);
  				sell.setBuyer(myAgent.getAID());
  				sell.setCreditCard(new CreditCard("VISA", 987453457, new Date()));
  				Action act = new Action(myAgent.getAID(), sell);
  				cel.add(act);
  				cel.add(new TrueProposition());
  				myAgent.getContentManager().fillContent(msg, cel);
  				l.log("Content correctly encoded");
  				l.log(msg.getContent());
  				return msg;
  			}
  			
  			protected boolean checkReply(ACLMessage reply) throws Exception {
  				ContentElementList cel = (ContentElementList) myAgent.getContentManager().extractContent(reply);
  				l.log("Content correctly decoded");
  				Action act = (Action) cel.get(0);
  				TrueProposition tp = (TrueProposition) cel.get(1);
  				l.log("Content element list OK");
  				return true;
  			}
  		};
  	}
  	catch (Exception e) {
  		throw new TestException("Wrong group argument", e);
  	}
  }

}
