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

package test.content;

import jade.core.Agent;
import jade.core.Runtime;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.AID;
import jade.wrapper.*;
import jade.domain.*;
import jade.lang.acl.*;
import jade.content.*;
import jade.content.lang.*;

import test.common.*;
import test.common.agentConfigurationOntology.*;
import test.common.xml.TestDescriptor;
import test.common.xml.XMLManager;
import test.content.testOntology.TestOntology;

/**
 * @author Giovanni Caire - TiLab
 * @author Elisabetta Cortese - TiLab
 *
 */

public class ContentTesterAgent extends TesterAgent {
	// Names and default values for group arguments
	public static final String CODEC_CLASS_NAME = "codec";
	private static final String CODEC_CLASS_DEFAULT = "jade.content.lang.sl.SLCodec";
		
	public static final String INFORM_MSG_NAME = "inform-msg";
	
	private static final String RESPONDER_NAME = "responder";
	
	protected TestGroup getTestGroup() {
		TestDescriptor[] td = XMLManager.getTests("test\\content\\contentTestsList.xml");
		String[] listT = new String[td.length];
		for (int i = 0; i < td.length; i++) {
			listT[i] = td[i].getTestClassName();
		}
		TestGroup tg = new TestGroup(listT){		
			
			private AID resp;
			
			public void initialize(Agent a) throws TestException {
				// Load the codec to be used in the tests
    		Codec codec = null;
    		String codecClassName = (String) getArgument(CODEC_CLASS_NAME);
    		try {
    			codec = (Codec) Class.forName(codecClassName).newInstance();
    		}
    		catch (Exception e) {
    			throw new TestException("Error loading codec "+codecClassName, e);
    		}
    		System.out.println("Test group performed using the "+codec.getName()+" language");
				
    		// Register the codec and ontology in the Agent's content manager
    		a.getContentManager().registerLanguage(codec);
    		a.getContentManager().registerOntology(TestOntology.getInstance());
    		
    		// Create and configure a responder agent
				resp = TestUtility.createTarget(a, RESPONDER_NAME);
				TestUtility.addBehaviour(a, resp, "test.content.Responder");
				TestUtility.forceAction(a, resp, new LoadLanguage(null, codecClassName));

				// Prepare the message that will be used in all tests
				ACLMessage msg  = new ACLMessage(ACLMessage.INFORM);
				msg.addReceiver(resp);
  			msg.setLanguage(codec.getName());
  			msg.setOntology(TestOntology.getInstance().getName());
  			msg.setConversationId(Responder.TEST_CONVERSATION);
  			msg.setReplyWith(Responder.TEST_RESPONSE_ID);
				setArgument(INFORM_MSG_NAME, msg);
			}
			
			public void shutdown(Agent a) {
				try {
					TestUtility.killAgent(a, resp);
				}
				catch (TestException te) {
					te.printStackTrace();
				}
			}
		};
				
		tg.specifyArgument(CODEC_CLASS_NAME, "Codec class name", CODEC_CLASS_DEFAULT);
		
		return tg;
	}
	
	// Main method that allows launching this test as a stand-alone program	
	public static void main(String[] args) {
		try {
      // Get a hold on JADE runtime
      Runtime rt = Runtime.instance();

      // Exit the JVM when there are no more containers around
      rt.setCloseVM(true);

      Profile pMain = new ProfileImpl(null, Test.DEFAULT_PORT, null);

      MainContainer mc = rt.createMainContainer(pMain);

      AgentController rma = mc.createNewAgent("rma", "jade.tools.rma.rma", new Object[0]);
      rma.start();

      AgentController tester = mc.createNewAgent("tester", "test.content.ContentTesterAgent", args);
      tester.start();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
 
}
