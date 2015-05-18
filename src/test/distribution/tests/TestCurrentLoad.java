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

public class TestCurrentLoad extends Test {

	public static final int SIZE = 10;
	public static final String ITEM_PREFIX = "ITEM";
	
	private AssignmentManager<String> assignmentManager;
	private AID tg1, tg2, tg3;
	
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
		
		// Step 0-a. Wait a bit to be sure the target agents registered with the DF
		sb.addSubBehaviour(new WakerBehaviour(a, 5000) {
			public void onStart() {
				log("--- Wait a bit ...");
				super.onStart();
			}
		});
		
		// Step 0-b. Kill tg2 and tg3 so that there is just a single target agent. Then wait a bit
		sb.addSubBehaviour(new WakerBehaviour(a, 5000) {
			public void onStart() {
				try {
					tg2 = (AID) getGroupArgument(DistributionTesterAgent.TG_AGENT_2_ARG);
					if (tg2 != null) {
						TestUtility.killAgent(myAgent, tg2);
						log("--- Agent "+tg2.getLocalName()+" successfully killed");
					}
					tg3 = (AID) getGroupArgument(DistributionTesterAgent.TG_AGENT_3_ARG);
					if (tg3 != null) {
						TestUtility.killAgent(myAgent, tg3);
						log("--- Agent "+tg2.getLocalName()+" successfully killed");
					}
					log("--- Wait a bit ...");
				}
				catch (Exception e) {
					failed("Error killing target agents");
				}
				super.onStart();
			}
		});
		
		// Step 1. Assign 10 items and check load before assignments are completed
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
				int load = assignmentManager.getCurrentLoad(tg1);
				if (load == SIZE) {
					log("--- Current load of agent "+tg1.getLocalName()+" before assignments are completed is "+SIZE+" as expected");
				}
				else {
					failed("--- Current load of agent "+tg1.getLocalName()+" before assignments are completed is "+load+" while "+SIZE+" was expected");
				}
			}
		});
		
		// Step 2. Wait a bit to be sure assignments are completed, then check load again
		sb.addSubBehaviour(new WakerBehaviour(a, 5000) {
			@Override
			public void onWake() {
				int load = assignmentManager.getCurrentLoad(tg1);
				if (load == SIZE) {
					log("--- Current load of agent "+tg1.getLocalName()+" after assignments are completed is "+SIZE+" as expected");
				}
				else {
					failed("--- Current load of agent "+tg1.getLocalName()+" after assignments are completed is "+load+" while "+SIZE+" was expected");
				}
			}
		});
		
		// Recreate the killed target agents. We cannot do that in the clean() method since 
		// the AssignmentManager could not reinitialize correctly due to inter-test cleanup
		sb.addSubBehaviour(new WakerBehaviour(a, 2000) {
			public void onStart() {
				if (tg2 != null) {
					try {
						tg2 = TestUtility.createAgent(myAgent, "tg2", TargetAgent.class.getName(), null);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				if (tg3 != null) {
					try {
						tg3 = TestUtility.createAgent(myAgent, "tg3", TargetAgent.class.getName(), null);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				super.onStart();
			}
			
			public void onWake() {
				if (!isFailed()) {
					passed("--- Test successful");
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
					}
					
				});
			}
		}
	}
}
