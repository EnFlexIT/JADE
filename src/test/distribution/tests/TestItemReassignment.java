package test.distribution.tests;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.distribution.AssignmentManager;
import jade.util.Callback;
import test.common.Test;
import test.common.TestException;
import test.common.TestUtility;
import test.distribution.DistributionTesterAgent;
import test.distribution.TargetAgent;

public class TestItemReassignment extends Test {
	
	public static final int SIZE = 10;
	public static final String ITEM_PREFIX = "ITEM";
	
	private AssignmentManager<String> assignmentManager;
	private AID tg1;
	
	public Behaviour load(Agent a) throws TestException {
		// AssignmentManager has been created once for all in the TesterAgent
		assignmentManager = (AssignmentManager<String>) getGroupArgument(DistributionTesterAgent.ASSIGNMENT_MANAGER_ARG);
		if (assignmentManager == null) {
			throw new TestException("Missing AssignmentManager argument");
		}
		
		SequentialBehaviour sb = new SequentialBehaviour(a);
		
		// Step 0. Wait a bit to be sure the target agents registered with the DF
		sb.addSubBehaviour(new WakerBehaviour(a, 5000) {
		});
		
		// Step 1. Assign 10 items
		sb.addSubBehaviour(new OneShotBehaviour(a) {
			public void action() {
				log("--- 1) Assign "+SIZE+" items");
				for (int i = 0; i < SIZE; ++i) {
					final String item = ITEM_PREFIX+"-"+i;
					assignmentManager.assign(item, new Callback<AID>() {
						@Override
						public void onSuccess(AID owner) {
							if (owner != null) {
								log("--- Item "+item+" assigned to agent "+owner.getLocalName());
							}
							else {
								failed("--- NULL assignment for item "+item);
							}
						}
	
						@Override
						public void onFailure(Throwable th) {
							failed("--- Assignment error for item "+item+": "+th.getMessage());
						}
					});
				}
			}
		});
		
		// Step 2. Wait a bit then kill one of the target agents
		sb.addSubBehaviour(new WakerBehaviour(a, 5000){
			public void onWake() {
				tg1 = (AID) getGroupArgument(DistributionTesterAgent.TG_AGENT_1_ARG);
				if (tg1 != null) {
					try {
						log("--- 3) Killing target agent "+tg1.getLocalName());
						TestUtility.killAgent(myAgent, tg1);
					}
					catch (TestException e) {
						failed("--- Error killing target agent "+tg1.getLocalName()+". "+e);
					}
				}
			}
		});
		
		// Step 3. Wait the dead-agents-restart-timeout plus a while and then check assignments
		sb.addSubBehaviour(new WakerBehaviour(a, assignmentManager.getDeadAgentsRestertTimeout() + 10000){
			public void onWake() {
				log("--- 3) Check assignments ...");
				for (int i = 0; i < SIZE; ++i) {
					final String item = ITEM_PREFIX+"-"+i;
					AID owner = assignmentManager.getOwner(item);
					if (owner == null) {
						failed("--- Item "+item+" not assigned");
					}
					else if (owner.equals(tg1)) {
						failed("--- Item "+item+" still assigned to dead agent "+tg1.getLocalName());
					}
				}
				passed("--- All items successflly assigned to available agents");
			}
		});
		
		return sb;
	}
	
	public void clean(Agent a) {
		// Unassign items
		if (assignmentManager != null) {
			for (int i = 0; i < SIZE; ++i) {
				final String item = ITEM_PREFIX+"-"+i;
				assignmentManager.unassign(item, null);
			}
		}
		
		// Recreate the killed target agent
		if (tg1 != null) {
			try {
				tg1 = TestUtility.createAgent(a, "tg1", TargetAgent.class.getName(), null);
			} catch (TestException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
