package test.distribution.tests;

import jade.core.*;
import jade.core.behaviours.*;
import jade.distribution.AssignmentManager;
import jade.util.Callback;
import test.common.*;
import test.distribution.DistributionTesterAgent;

public class TestDuplicatedAssignment extends Test {
	
	public static final int SIZE = 3;
	
	private AssignmentManager<String> assignmentManager;
	private AID[] owners = new AID[SIZE];
	private int k = 0;
	
	public Behaviour load(Agent a) throws TestException {
		assignmentManager = (AssignmentManager<String>) getGroupArgument(DistributionTesterAgent.ASSIGNMENT_MANAGER_ARG);
		if (assignmentManager == null) {
			throw new TestException("Missing AssignmentManager argument");
		}
		
		SequentialBehaviour sb = new SequentialBehaviour(a);
		
		// Step 0. Wait a bit to be sure the target agents registered with the DF
		sb.addSubBehaviour(new WakerBehaviour(a, 5000) {
		});
		
		// Step 1. Assign 3 times the same item
		sb.addSubBehaviour(new OneShotBehaviour(a) {
			public void action() {
				final String item = "ITEM";
				log("--- 1) Assign "+SIZE+" times the same item "+item);
				for (int i = 0; i < SIZE; ++i) {
					assignmentManager.assign(item, new Callback<AID>() {
						@Override
						public void onSuccess(AID owner) {
							if (owner != null) {
								log("--- 2) Item "+item+" assigned to agent "+owner.getLocalName());
								if (k >= owners.length) {
									failed("--- Callback invoked at least "+(k+1)+" times while "+SIZE+" were expected");
								}
								else {
									owners[k] = owner;
									k++;
								}
							}
							else {
								failed("--- NULL assignment");
							}
						}
	
						@Override
						public void onFailure(Throwable th) {
							failed("--- Assignment error: "+th.getMessage());
						}
					});
				}
			}
		});
		
		// Step 2. Wait a bit then check the assignments
		sb.addSubBehaviour(new WakerBehaviour(a, 5000){
			public void onWake() {
				for (int i = 0; i < SIZE; ++i) {
					if (owners[i] != null) {
						if (!owners[i].equals(owners[0])) {
							failed("--- Wrong assignment: owner["+i+"]="+owners[i].getLocalName()+" while "+owners[0]+" was expected");
							return;
						}
					}
					else {
						failed("--- Null assignment at "+i);
						return;
					}
				}
				passed("--- All assignments correctly done to target agent "+owners[0].getLocalName());
			}
		});
		
		return sb;
	}
	
	public void clean(Agent a) {
		// Unassign items
		if (assignmentManager != null) {
			assignmentManager.unassign("ITEM", null);
		}
	}
}
