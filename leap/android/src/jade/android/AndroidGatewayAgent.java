package jade.android;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.util.Event;
import jade.util.Logger;
import jade.util.leap.HashMap;
import jade.wrapper.gateway.GatewayListener;

public class AndroidGatewayAgent extends Agent {
	
	private GatewayListener listener;
	private final Logger myLogger = Logger.getJADELogger(this.getClass().getName());
	
	private final HashMap commands = new HashMap();
	
	public AndroidGatewayAgent() {
		// enable object2agent communication with queue of infinite length
		setEnabledO2ACommunication(true, 0);
	}
	
	protected void setup() {
		myLogger.log(Logger.INFO, "AndroidGatewayAgent "+getLocalName()+" started");			
		// If a GatewayListener argument was passed, use it
		Object[] args = getArguments();
		if (args != null && args.length > 0) {
			if (args[0] instanceof GatewayListener) {
				listener = (GatewayListener) args[0];
			}
		}
		
		Behaviour b = new CyclicBehaviour() {
			public void action() {
				Object obj = myAgent.getO2AObject();
				if (obj != null) {
					Object command = null;
					if (obj instanceof Event) {
						// Synchronous command
						command = ((Event) obj).getSource();
					}
					else {
						// Asynchronous command
						command = ((AsynchCommandInfo) obj).getCommand();
					}
					commands.put(command, obj);
					
					myLogger.log(Logger.INFO, myAgent.getLocalName() + " started execution of command " + command);
					processCommand(command);
				}
				else {
					block();
				}
			}
		};
		addBehaviour(b);
		setO2AManager(b);
		
		if (listener != null) {
			listener.handleGatewayConnected();
		}
		
	}
	
	protected void takeDown() {
		if (listener != null) {
			listener.handleGatewayDisconnected();
		}
		myLogger.log(Logger.INFO, "AndroidGatewayAgent "+getLocalName()+" terminated");			
	}
	
	protected void processCommand(final Object command) {
		if (command instanceof Behaviour) {
			SequentialBehaviour sb = new SequentialBehaviour(this);
			sb.addSubBehaviour((Behaviour) command);
			sb.addSubBehaviour(new OneShotBehaviour(this) {
				public void action() {
					AndroidGatewayAgent.this.releaseCommand(command);
				}
			});
			addBehaviour(sb);
		}
		else {
			myLogger.log(Logger.WARNING, "Unknown command "+command);
		}
	}


	/**
	 * notify that the command has been processed and remove the command from the queue
	 * @param command is the same object that was passed in the processCommand method
	 **/
	public final void releaseCommand(Object command) {	
		// remove the command from the queue
		Object obj = commands.remove(command);
		// notify that the command has been processed such as the JADEGateway is waken-up
		if (obj != null) {
			myLogger.log(Logger.INFO, getLocalName() + " terminated execution of command " + command);
			if (obj instanceof Event) {
				((Event) obj).notifyProcessed(null);
			}
			else {
				((AsynchCommandInfo) obj).getCallback().onSuccess(null);
			}
		}
	}

}
