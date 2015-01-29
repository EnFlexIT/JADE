package test.distribution.tests;

import java.util.ArrayList;
import java.util.List;

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

public class TestItemReassignmentToRestartedAgent extends Test {
	
	public static final int SIZE = 10;
	public static final String ITEM_PREFIX = "ITEM";
	
	private AssignmentManager<String> assignmentManager;
	private AID tg1;
	private List<String> tg1Items = new ArrayList<String>();
	
	public Behaviour load(Agent a) throws TestException {
		// AssignmentManager has been created once for all in the TesterAgent
		assignmentManager = (AssignmentManager<String>) getGroupArgument(DistributionTesterAgent.ASSIGNMENT_MANAGER_ARG);
		if (assignmentManager == null) {
			throw new TestException("Missing AssignmentManager argument");
		}
		tg1 = (AID) getGroupArgument(DistributionTesterAgent.TG_AGENT_1_ARG);
		if (tg1 == null) {
			throw new TestException("Missing target agent "+DistributionTesterAgent.TG_AGENT_1_ARG);
		}
		
		SequentialBehaviour sb = new SequentialBehaviour(a);
		
		// Step 0. Wait a bit to be sure the target agents registered with the DF
		sb.addSubBehaviour(new WakerBehaviour(a, 5000) {
		});
		
		// Step 1. Assign 10 items. Keep track of items assigned to agent tg1
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
								if (owner.equals(tg1)) {
									tg1Items.add(item);
								}
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
		
		// Step 2. Wait a bit then kill target agent tg1
		sb.addSubBehaviour(new WakerBehaviour(a, 5000){
			public void onWake() {
				try {
					log("--- 2) Killing target agent "+tg1.getLocalName());
					TestUtility.killAgent(myAgent, tg1);
				}
				catch (TestException e) {
					failed("--- Error killing target agent "+tg1.getLocalName()+". "+e);
				}
			}
		});
		
		// Step 3. Wait a bit then restart target agent tg1
		sb.addSubBehaviour(new WakerBehaviour(a, 10000){
			public void onWake() {
				try {
					log("--- 3) Restarting target agent "+tg1.getLocalName());
					tg1 = TestUtility.createAgent(myAgent, "tg1", TargetAgent.class.getName(), null);
				} catch (TestException e) {
					// TODO Auto-generated catch block
					failed("--- Error Restarting target agent "+tg1.getLocalName()+". "+e);
				}
			}
		});
		
		// Step 3. Wait a bit and then check assignments 
		sb.addSubBehaviour(new WakerBehaviour(a, assignmentManager.getDeadAgentsRestertTimeout() + 10000){
			public void onWake() {
				log("--- 4) Check assignments of items originally assigned to tg1...");
				for (String item : tg1Items) {
					AID owner = assignmentManager.getOwner(item);
					if (owner == null) {
						failed("--- Item "+item+" not assigned");
						return;
					}
					else if (!owner.equals(tg1)) {
						failed("--- Item "+item+" no longer assigned to restarted agent "+tg1.getLocalName());
						return;
					}
				}
				passed("--- All items successflly re-assigned to restarted agent "+tg1.getLocalName());
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
	}


}
