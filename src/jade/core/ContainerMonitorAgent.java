package jade.core;

//#J2ME_EXCLUDE_FILE
//#APIDOC_EXCLUDE_FILE

import java.lang.reflect.Method;

import jade.core.behaviours.*;
import jade.core.messaging.MessagingService;
import jade.domain.introspection.IntrospectionServer;
import jade.lang.acl.*;

public class ContainerMonitorAgent extends Agent {
	public static final String CONTAINER_MONITOR_ONTOLOGY = "container-monitor";
	
	public static final String DUMP_AGENTS_ACTION = "DUMP-AGENTS";
	public static final String DUMP_MESSAGEMANAGER_ACTION = "DUMP-MESSAGEMANAGER";
	public static final String DUMP_LADT_ACTION = "DUMP-LADT";
	
	private AgentContainerImpl myContainer;
	private LADT myLADT;
	
	protected void setup() {
		Object[] args = getArguments();
		myContainer = (AgentContainerImpl) args[0];
		myLADT = (LADT) args[1];
		
		addBehaviour(new IntrospectionServer(this));
		
		addBehaviour(new CyclicBehaviour(this) {
			private MessageTemplate template = MessageTemplate.and(
					MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
					MessageTemplate.MatchOntology(CONTAINER_MONITOR_ONTOLOGY) );
			
			public void action() {
				ACLMessage msg = myAgent.receive(template);
				if (msg != null) {
					ACLMessage reply = msg.createReply();
					String content = msg.getContent();
					try {
						if (content.equalsIgnoreCase(DUMP_AGENTS_ACTION)) {
							reply.setPerformative(ACLMessage.INFORM);
							reply.setContent(getAgentsDump());
						}
						else if (content.equalsIgnoreCase(DUMP_MESSAGEMANAGER_ACTION)) {
							reply.setPerformative(ACLMessage.INFORM);
							reply.setContent(getMessageManagerDump());
						}
						else if (content.equalsIgnoreCase(DUMP_LADT_ACTION)) {
							reply.setPerformative(ACLMessage.INFORM);
							reply.setContent(getLADTDump());
						}
						else {
							reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
						}
					}
					catch (Exception e) {
						e.printStackTrace();
						reply.setPerformative(ACLMessage.FAILURE);
						reply.setContent(e.toString());
					}
					myAgent.send(reply);
				}
				else {
					block();
				}
			}
		});
	}
	
	public String[] getLADTStatus() {
		return myLADT.getStatus();
	}
	
	public String getAgentsDump() {
		StringBuffer sb = new StringBuffer();
		sb.append("-------------------------------------------------------------\n");
		sb.append("Container ");
		sb.append(myContainer.getID().getName());
		sb.append(" agents DUMP\n");
		sb.append("-------------------------------------------------------------\n");
	    AID[] agents = myLADT.keys();
	    for (int i = 0; i < agents.length; ++i) {
	    	Agent a = myLADT.acquire(agents[i]);
	    	if (a != null) {
	    		try {
		    		sb.append("Agent "+a.getName()+"\n");
		    		sb.append("  - Class = "+a.getClass().getName()+"\n");
		    		sb.append("  - State = "+a.getState()+"\n");
		    		sb.append("  - MessageQueue size = "+a.getMessageQueue().size()+"\n");
		    		sb.append("  - Behaviours\n");
		    		Behaviour[] bb = a.getScheduler().getBehaviours();
		    		for (int j = 0; j < bb.length; ++j) {
		    			Behaviour b = bb[j];
			    		sb.append("    - Behaviour "+b.getBehaviourName()+"\n");
			    		appendBehaviourInfo(b, sb, "      ");
		    		}
	    		}
	    		catch (Exception e) {
	    			e.printStackTrace();
	    		}
	    		finally {
	    			myLADT.release(agents[i]);
	    		}
	    	}
	    }
		sb.append("-------------------------------------------------------------\n");
		return sb.toString();
	}
	
	private void appendBehaviourInfo(Behaviour b, StringBuffer sb, String prefix) {
		sb.append(prefix+"- Class = "+b.getClass().getName()+"\n");
		sb.append(prefix+"- State = "+b.getState()+"\n");
		sb.append(prefix+"- Runnable = "+b.isRunnable()+"\n");
		if (b instanceof CompositeBehaviour) {
			sb.append(prefix+"- Type = "+getCompositeType((CompositeBehaviour) b)+"\n");
			Behaviour child = getCurrent((CompositeBehaviour) b, b.getClass());
			if (child != null) {
				sb.append(prefix+"- Current child information\n");
				sb.append(prefix+"  - Name = "+child.getBehaviourName()+"\n");
				appendBehaviourInfo(child, sb, prefix+"  ");
			}
		}
		else if (b instanceof ThreadedBehaviourFactory.ThreadedBehaviourWrapper) {
			sb.append(prefix+"- Type = Threaded\n");
			sb.append(prefix+"- Thread Information\n");
			Thread t = ((ThreadedBehaviourFactory.ThreadedBehaviourWrapper) b).getThread();
			if (t != null) {
				sb.append(prefix+"  - Alive = "+t.isAlive()+"\n");
				sb.append(prefix+"  - Interrupted = "+t.isInterrupted()+"\n");
			}
			sb.append(prefix+"- Threaded Behaviour Information\n");
			Behaviour tb = ((ThreadedBehaviourFactory.ThreadedBehaviourWrapper) b).getBehaviour();
			sb.append(prefix+"  - Name = "+tb.getBehaviourName()+"\n");
			appendBehaviourInfo(tb, sb, prefix+"  ");
		}
		else {
			sb.append(prefix+"- Type = "+getSimpleType(b)+"\n");
		}
	}
	
	private String getSimpleType(Behaviour b) {
		if (b instanceof CyclicBehaviour) {
			return "Cyclic";
		}
		else if (b instanceof OneShotBehaviour) {
			return "OneShot";
		}
		else if (b instanceof WakerBehaviour) {
			return "Waker";
		}
		else if (b instanceof TickerBehaviour) {
			return "Ticker";
		}
		else {
			return "Simple";
		}
	}
	
	private String getCompositeType(CompositeBehaviour cb) {
		if (cb instanceof FSMBehaviour) {
			return "FSM";
		}
		else if (cb instanceof SequentialBehaviour) {
			return "Sequential";
		}
		else if (cb instanceof ParallelBehaviour) {
			return "Parallel";
		}
		else {
			return "Composite";
		}
	}
	
	private Behaviour getCurrent(CompositeBehaviour cb, Class c) {
		Method getCurrentMethod = null;
		try {
			Behaviour b = null;
			getCurrentMethod = c.getDeclaredMethod("getCurrent", (Class[]) null);
			boolean accessibilityChanged = false;
			if (!getCurrentMethod.isAccessible()) {
				try {
					getCurrentMethod.setAccessible(true);
					accessibilityChanged = true;
				}
				catch (SecurityException se) {
					// Cannot change accessibility 
					return null;
				}
			}					
			try { 			
				b = (Behaviour) getCurrentMethod.invoke(cb, (Object[]) null);
				// Restore accessibility if changed
				if (accessibilityChanged) {
					getCurrentMethod.setAccessible(false);
				}
			}
			catch (Exception e) {
				// Should never happen
				e.printStackTrace();
			}
			return b;
		}
		catch (NoSuchMethodException e) {
			// getCurrent() method not defined. Try in the superclass if any.	
			Class superClass = c.getSuperclass();
			if (superClass != null) {
				return getCurrent(cb, superClass);
			}
		}
		catch (Exception e1) {
		}
		return null;
	}
	
	public String getLADTDump() {
		StringBuffer sb = new StringBuffer();
		sb.append("-------------------------------------------------------------\n");
		sb.append("Container ");
		sb.append(myContainer.getID().getName());
		sb.append(" LADT DUMP\n");
		sb.append("-------------------------------------------------------------\n");
		try {
			String[] ladtStatus = myLADT.getStatus();	
			for (int i = 0; i < ladtStatus.length; ++i) {
				sb.append("- "+ladtStatus[i]+"\n");
			}
		}
		catch (Exception e) {
		}
		sb.append("-------------------------------------------------------------\n");
		return sb.toString();
	}
	
	public String getMessageManagerDump() {
		StringBuffer sb = new StringBuffer();
		sb.append("-------------------------------------------------------------\n");
		sb.append("Container ");
		sb.append(myContainer.getID().getName());
		sb.append(" Message-Manager DUMP\n");
		sb.append("-------------------------------------------------------------\n");
		try {
			ServiceFinder sf = myContainer.getServiceFinder();
			MessagingService service = (MessagingService) sf.findService(MessagingService.NAME);
			String[] queueStatus = service.getMessageManagerQueueStatus();	
			sb.append("- Queue status:\n");
			if (queueStatus.length == 0) {
				sb.append("    EMPTY\n");
			}
			else {
				for (int i = 0; i < queueStatus.length; ++i) {
					sb.append("  - "+queueStatus[i]+"\n");
				}
			}
			sb.append("- Thread pool status:\n");
			String[] threadPoolStatus = service.getMessageManagerThreadPoolStatus();	
			for (int i = 0; i < threadPoolStatus.length; ++i) {
				sb.append("  - "+threadPoolStatus[i]+"\n");
			}
		}
		catch (Exception e) {
		}
		sb.append("-------------------------------------------------------------\n");
		return sb.toString();
	}
}
