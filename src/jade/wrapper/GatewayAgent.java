package jade.wrapper;

import jade.core.Agent;
import jade.core.behaviours.*;
import java.util.HashMap;
import java.util.Iterator;
import jade.util.Event;
import jade.util.Logger;

/**
 * This agent is the gateway able to execute all requests coming from JadeGateway.
 * @see JadeGateway
 * @author Fabio Bellifemine, Telecom Italia LAB
 * @version $Date$ $Revision$
 **/
public abstract class GatewayAgent extends Agent {

		// queue of all pending commands that have not yet been released (see method releaseCommand)
		// In this Hash, the key is the Object while the value is the Event
		private final HashMap commandQueue = new HashMap(2);

		private final Logger myLogger = Logger.getMyLogger(this.getClass().getName());

		/** subclasses must implement this method.
		 * The method is called each time a request to process a command
		 * is received from the JSP Gateway.
		 * <p> The recommended pattern is the following implementation:
<code>
if (c instanceof Command1)
  exexCommand1(c);
else if (c instanceof Command2)
  exexCommand2(c);
</code>
     * </p>
		 * <b> REMIND THAT WHEN THE COMMAND HAS BEEN PROCESSED,
		 * YOU MUST CALL THE METHOD <code>releaseCommand</code>.
		 * Infact, you might prefer launching a new Behaviour that processes
		 * this command and release the command just when the Behaviour terminates,
		 * i.e. in its <code>onEnd()</code> method.
		 **/
		abstract protected void processCommand(Object command);


		/**
		 * notify that the command has been processed and remove the command from the queue
		 * @param command is the same object that was passed in the processCommand method
		 **/
		final public void releaseCommand(Object command) {	
				if (myLogger.isLoggable(Logger.INFO)) 
						myLogger.log(Logger.INFO, getLocalName()+" terminated execution of command "+command);			
				// remove the command from the queue
				Event e = (Event)commandQueue.remove(command);
				// notify that the command has been processed such as the JADEGateway is waken-up
				if (e != null)
						e.notifyProcessed(null);
		}

		public GatewayAgent() {
				// enable object2agent communication with queue of infinite length
				setEnabledO2ACommunication(true, 0);
		}

		protected final void setup() {
				if (myLogger.isLoggable(Logger.INFO)) 
						myLogger.log(Logger.INFO, "Started GatewayAgent "+getLocalName());			
				Behaviour mainB = new DispatchCommandBehaviour();
				addBehaviour(mainB);
		}

		/**
		 * Release all pending commands
		 **/
		protected void takeDown() {
				if (myLogger.isLoggable(Logger.INFO)) 
						myLogger.log(Logger.INFO, "Terminated GatewayAgent "+getLocalName());			
				for (Iterator i=commandQueue.values().iterator(); i.hasNext(); )
						((Event)(i.next())).notifyProcessed(null);
		}

		class DispatchCommandBehaviour extends CyclicBehaviour {
				public void action() {
						Event e = (Event)getO2AObject();
						if (e == null) {
								block();
								return;
						}
						// put the event into the command Queue
						commandQueue.put(e.getSource(), e);
						if (myLogger.isLoggable(Logger.INFO)) 
								myLogger.log(Logger.INFO, getLocalName()+" started execution of command "+e.getSource());			
						// call the processCommand method such as the command is executed
						processCommand(e.getSource());
				}
		}
}
