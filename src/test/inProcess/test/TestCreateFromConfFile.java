package test.inProcess.test;

import jade.core.Agent;
import jade.core.AID;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.wrapper.*;
import jade.content.onto.basic.*;
import jade.domain.JADEAgentManagement.*;
import jade.util.leap.*;
import test.common.*;

/**
   @author Giovanni Caire - TILAB
 */
public class TestCreateFromConfFile extends Test {
	private AgentContainer ac;
	
	public Behaviour load(jade.core.Agent a) throws TestException {
		Behaviour b = new OneShotBehaviour(a) {
			public void action() {
				try {
					log("Reading configuration file");
					// Launch a new container reading the properties from a configuration file
					Properties pp = new Properties();
					pp.load("test/inProcess/test.properties");
					
					log("Launching container");
					ac = Runtime.instance().createAgentContainer(new ProfileImpl(pp));
					if (ac != null) {
						log("Retrieving local agent");
						AgentController controller = ac.getAgent("test");
						if (controller != null) {
							log("Killing container");
							try {
								ac.kill();
								ac = null;
								passed("Container successfully killed");
							}
							catch (StaleProxyException spe) {
								failed("Error killing container. "+spe);
							}
						}
						else {
							failed("Cannot find test agent");
						}	
					}
					else {
						failed("Error creating container");
					}
				}
				catch (Exception e) {
					e.printStackTrace();
					failed("Unexpected error: "+e.toString()); 
				}
			}
		};
		return b;
	}
	
	public void clean(Agent a) {
		if (ac != null) {
			try {
				ac.kill();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}			
}
