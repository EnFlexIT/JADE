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
		tg1 = (AID) getGroupArgument(DistributionTesterAgent.TG_AGENT_1_ARG);
		if (tg1 == null) {
			throw new TestException("Missing target agent "+DistributionTesterAgent.TG_AGENT_1_ARG);
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
				try {
					log("--- 2) Killing target agent "+tg1.getLocalName());
					TestUtility.killAgent(myAgent, tg1);
				}
				catch (TestException e) {
					failed("--- Error killing target agent "+tg1.getLocalName()+". "+e);
				}
			}
		});
		
		// Step 3. Wait the dead-agents-restart-timeout plus a while and then check assignments
		sb.addSubBehaviour(new WakerBehaviour(a, assignmentManager.getDeadAgentsRestartTimeout() + 10000){
			public void onWake() {
				log("--- 3) Check assignments ...");
				for (int i = 0; i < SIZE; ++i) {
					String item = ITEM_PREFIX+"-"+i;
					AID owner = assignmentManager.getOwner(item);
					if (owner == null) {
						failed("--- Item "+item+" not assigned");
						return;
					}
					else if (owner.equals(tg1)) {
						failed("--- Item "+item+" still assigned to dead agent "+tg1.getLocalName());
						return;
					}
				}
				
				// Call to passed() is done in next step
			}
		});
		
		// Recreate the killed target agent. We cannot do that in the clean() method since 
		// the AssignmentManager could not reinitialize correctly due to inter-test cleanup
		sb.addSubBehaviour(new WakerBehaviour(a, 2000) {
			public void onStart() {
				if (tg1 != null) {
					try {
						tg1 = TestUtility.createAgent(myAgent, "tg1", TargetAgent.class.getName(), null);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				super.onStart();
			}
			
			public void onWake() {
				if (!isFailed()) {
					passed("--- All items successflly re-assigned to available agents");
				}
			}
		});
		return sb;
	}
	
	public void clean(Agent a) {
		// Unassign items
		if (assignmentManager != null) {
			for (int i = 0; i < SIZE; ++i) {
				final String item = ITEM_PREFIX+"-"+i;
				System.out.println("Unassigning item "+item+" owned by agent "+assignmentManager.getOwner(item).getLocalName());
				assignmentManager.unassign(item, new Callback<AID>() {

					@Override
					public void onSuccess(AID result) {
						System.out.println("Item "+item+" successfully unassigned from agent "+result.getLocalName());
					}

					@Override
					public void onFailure(Throwable throwable) {
						// TODO Auto-generated method stub
						
					}
					
				});
			}
		}
	}

}
