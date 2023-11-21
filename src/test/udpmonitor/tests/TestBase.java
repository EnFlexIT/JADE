package test.udpmonitor.tests;

import jade.core.AID;
import jade.core.Agent;
import jade.core.AgentContainer;
import jade.core.ContainerID;
import jade.core.behaviours.*;
import jade.domain.introspection.*;

import test.common.*;
import test.udpmonitor.UDPMonitorTesterAgent;

import java.util.Map;

/**
 * Common base class for tests about UDP monitoring
 */
public abstract class TestBase extends Test {
	public static final String PERIPHERAL_CONTAINER_NAME = "Remote-Peripheral-Container";
	
	protected int expectedAddedContainer = 0;
	protected int expectedRemovedContainer = 0;
	
	private AMSListenerBehaviour listener = null;
	
	/**
	 * Inner class AMSListenerBehaviour
	 */
	private class AMSListenerBehaviour extends AMSSubscriber {
		private String containerName;
		private int addedContainerCnt = 0;
		private int removedContainerCnt = 0;

		public AMSListenerBehaviour(AID ams, String containerName) {
			super(ams);
			this.containerName = containerName;
		}

		protected void installHandlers(Map handlersTable) {
			handlersTable.put(IntrospectionOntology.ADDEDCONTAINER, new EventHandler() {
				public void handle(Event ev) {
					AddedContainer ac = (AddedContainer) ev;
					ContainerID cid = ac.getContainer();
					if (cid.getName().equals(containerName)) {
						addedContainerCnt++;
					}
				}
			});
			
			handlersTable.put(IntrospectionOntology.REMOVEDCONTAINER, new EventHandler() {
				public void handle(Event ev) {
					RemovedContainer rc = (RemovedContainer) ev;
					ContainerID cid = rc.getContainer();
					if (cid.getName().equals(containerName)) {
						removedContainerCnt++;
					}
				}
			});
		}
		
		public int getAddedContainerCnt() {
			return addedContainerCnt;
		}
		
		public int getRemovedContainerCnt() {
			return removedContainerCnt;
		}
	} // END of inner class AMSListenerBehaviour

	
	public Behaviour load(Agent a) throws TestException {
		listener = new AMSListenerBehaviour((AID) getGroupArgument(UDPMonitorTesterAgent.REMOTE_AMS_AID_KEY), PERIPHERAL_CONTAINER_NAME);
		
		Behaviour b = loadSpecific(a);
		
		ParallelBehaviour pb = new ParallelBehaviour(a, ParallelBehaviour.WHEN_ANY) {
			public int onEnd() {
				if (!isFailed()) {
					int addedContainerCnt = listener.getAddedContainerCnt();
					int removedContainerCnt = listener.getRemovedContainerCnt();
					
				    if (addedContainerCnt == expectedAddedContainer && removedContainerCnt == expectedRemovedContainer) {
						passed("Received " + expectedAddedContainer + " ADDED-CONTAINER and " + expectedRemovedContainer + " REMOVED-CONTAINER event(s) as expected");
					} 
				    else {
						if (addedContainerCnt != expectedAddedContainer)
							failed("Received " + addedContainerCnt + " ADDED-CONTAINER event(s) while " + expectedAddedContainer + " were expected.");
						if (removedContainerCnt != expectedRemovedContainer)
							failed("Received " + removedContainerCnt + " REMOVED-CONTAINER event(s) while " + expectedRemovedContainer + " were expected.");
					}	
				}
			    return super.onEnd();
			}
		};
		pb.addSubBehaviour(b);
		pb.addSubBehaviour(listener);
		
		return pb;
	}
	
	public void clean(Agent a) {
		a.send(listener.getCancel());
	}
	
	protected abstract Behaviour loadSpecific(Agent a) throws TestException;
	
	public AID getRemoteAMS() {
		return (AID) getGroupArgument(UDPMonitorTesterAgent.REMOTE_AMS_AID_KEY);
	}
	
	public JadeController startPeripheralContainer(Agent a, String specificArgs) throws TestException {
		return TestUtility.launchJadeInstance(PERIPHERAL_CONTAINER_NAME, null, "-container-name "+PERIPHERAL_CONTAINER_NAME+" -container -port "+UDPMonitorTesterAgent.REMOTE_PLATFORM_PORT+" -host " + TestUtility.getContainerHostName(a, getRemoteAMS(), AgentContainer.MAIN_CONTAINER_NAME) + " "+specificArgs, null);		
	}
	
	public int getAddedContainerCnt() {
		return listener.getAddedContainerCnt();
	}
	
	public int getRemovedContainerCnt() {
		return listener.getRemovedContainerCnt();
	}
}
