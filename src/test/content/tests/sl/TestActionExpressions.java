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

package test.content.tests.sl;

import test.common.*;
import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.content.ContentManager;
import jade.content.lang.sl.*;
import jade.content.abs.*;
import jade.content.onto.basic.Action;
import examples.content.ecommerceOntology.*;
import test.common.*;
import test.content.*;
import test.content.testOntology.*;
import java.util.Date;

/**
 * @author Giovanni Caire - TILAB
 */
public class TestActionExpressions extends Test{
  public String getName() {
  	return "Test-action-expressions";
  }
  
  public String getDescription() {
  	StringBuffer sb = new StringBuffer("Tests a content including the | (ACTION_ALTERNATIVE) and ; (ACTION_SEQUENCE) operators\n");
  	sb.append("The tested content looks like: (; (action (agent-identifier ...) (SELL...)) (| (action (agent-identifier ...) (SELL ...)) (action (agent-identifier ...) (SELL ...))))");
  	return sb.toString();
  }
  
  public Behaviour load(Agent a, DataStore ds, String resultKey) throws TestException {
  	try {
  		final ACLMessage msg = (ACLMessage) getGroupArgument(SLOperatorsTesterAgent.INFORM_MSG_NAME);;
  		return new SuccessExpectedInitiator(a, ds, resultKey) {
  			protected ACLMessage prepareMessage() throws Exception {
  				msg.setPerformative(ACLMessage.REQUEST);
  				
  				AID john = new AID("John", AID.ISLOCALNAME);
  				AID bill = new AID("Bill", AID.ISLOCALNAME);
  				Sell s1 = new Sell(john, new Item(100), new CreditCard("VISA", 1000000, new Date()));
  				Sell s2 = new Sell(john, new Item(200), new CreditCard("VISA", 1000000, new Date()));
  				Sell s3 = new Sell(john, new Item(200), new CreditCard("AMEX", 1000000, new Date()));
  				
  				Action a1 = new Action(bill, s1);
  				Action a2 = new Action(bill, s2);
  				Action a3 = new Action(bill, s3);
  				
  				AbsAgentAction alternative = new AbsAgentAction(SLVocabulary.ACTION_ALTERNATIVE);
  				alternative.set(SLVocabulary.ACTION_ALTERNATIVE_FIRST, (AbsAgentAction) ECommerceOntology.getInstance().fromObject(a2));
  				alternative.set(SLVocabulary.ACTION_ALTERNATIVE_SECOND, (AbsAgentAction) ECommerceOntology.getInstance().fromObject(a3));
  				
  				AbsAgentAction sequence = new AbsAgentAction(SLVocabulary.ACTION_SEQUENCE);
  				sequence.set(SLVocabulary.ACTION_SEQUENCE_SECOND, alternative);
  				sequence.set(SLVocabulary.ACTION_SEQUENCE_FIRST, (AbsAgentAction) ECommerceOntology.getInstance().fromObject(a1));
  				
  				myAgent.getContentManager().fillContent(msg, sequence);
  				System.out.println("Encoded content is:\n"+msg.getContent());
  				return msg;
  			}
  		};
  	}
  	catch (Exception e) {
  		throw new TestException("Wrong group argument", e);
  	}
  }

}
