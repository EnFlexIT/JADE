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

package test.distribution;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.distribution.AssignmentManager;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.lang.acl.ACLMessage;
import jade.proto.AchieveREInitiator;
import test.common.*;


/**
   Performs tests related to the distribution package of the Misc add-on
   @author Giovanni Caire - TILAB
 */
public class DistributionTesterAgent extends TesterAgent {
	
	public static final String ASSIGNMENT_MANAGER_ARG = "ASSIGNMENT-MANAGER";
	public static final String TG_AGENT_1_ARG = "TG1";
	public static final String TG_AGENT_2_ARG = "TG2";
	public static final String TG_AGENT_3_ARG = "TG3";
	
	private AID tg1, tg2, tg3;
	
	private AssignmentManager<String> assignmentManager;
	
	protected TestGroup getTestGroup() {
		TestGroup tg = new TestGroup("test/distribution/distributionTestsList.xml") {
			protected void initialize(Agent a) throws TestException {
				assignmentManager = new AssignmentManager<String>(TargetAgent.TARGET_TYPE) {
					protected Behaviour createAssignmentBehaviour(final String item, AID target, int context) {
						ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
						request.addReceiver(target);
						request.setContent(item);
						return new AchieveREInitiator(null, request) {
							private int ret = 0;
							public void handleInform(ACLMessage inform) {
								ret = 1;
								System.err.println("--- Item "+item+" assignment completed: owner = "+inform.getSender().getLocalName());
							}
							
							public int onEnd() {
								return ret;
							}
						};
					}
				};
				assignmentManager.start(a);
				setArgument(ASSIGNMENT_MANAGER_ARG, assignmentManager);
				
				// Start 3 target agents
				tg1 = TestUtility.createAgent(a, "tg1", TargetAgent.class.getName(), null);
				setArgument(TG_AGENT_1_ARG, tg1);
				tg2 = TestUtility.createAgent(a, "tg2", TargetAgent.class.getName(), null);
				setArgument(TG_AGENT_2_ARG, tg2);
				tg3 = TestUtility.createAgent(a, "tg3", TargetAgent.class.getName(), null);
				setArgument(TG_AGENT_3_ARG, tg3);
			}
			
			protected void shutdown(Agent a) {
				killTg(a, tg1);
				killTg(a, tg2);
				killTg(a, tg3);
			}
		};
		return tg;
	}
	
	private void killTg(Agent a, AID tg) {
		if (tg != null) {
			try {
				TestUtility.killAgent(a, tg);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}