package jade.wrapper; 

import jade.core.Runtime;
import jade.core.ProfileImpl;
import jade.core.Profile;
import jade.util.Event;
import jade.util.leap.Properties;
import jade.util.Logger;

/**
 * This class is the entry point for using the functionalities of
 * a simple gateway useful to send commands to a JADE Agent.
 * The class is responsible for creating, and keeping alive, a JADE Container
 * and a JADE Agent.
 * <p> The package must be used as follows:
 * <ul>
 * <li> create an application-specific class that extends <code>GatewayAgent</code>, implement its method <code>processCommand</code>
 * and is responsible for processing all command-requests
 * <li> initialize this JadeGateway by calling its method <code>init</code> with the
 * name of the class of the application-specific agent 
 * <li> finally, in order to request the processing of a Command, you must call the method <code>JadeGateway.execute(Object command)<code>.
 * This method will cause the callback of
 * the method <code>processCommand</code> of the application-specific agent.
 * The method <code>execute</code> will return only after the method <code>GatewayAgent.releaseCommand(command)</code> has been called
 * by your application-specific agent.
 * </ul>
 * <b>NOT available in MIDP</b>
 * @author Fabio Bellifemine, Telecom Italia LAB
 * @version $Date$ $Revision$
 **/
public class JadeGateway {

		private static ContainerController myContainer = null;
		private static AgentController myAgent = null;
		private static String agentType;
		// jade profile properties
		private static Properties profile;
		private static final Logger myLogger = Logger.getMyLogger(JadeGateway.class.getName());

		/**
		 * @throws StaleProxyException if the method was not able to execute the Command
		 * @see jade.wrapper.AgentController#putO2AObject(Object, boolean)
		 **/
		public static synchronized void execute(Object command) throws StaleProxyException,ControllerException,InterruptedException {
				checkJADE();
				// incapsulate the command into an Event
				Event e = new Event(-1, command);
				try {
						if (myLogger.isLoggable(Logger.INFO)) 
								myLogger.log(Logger.INFO, "Requesting execution of command "+command);
						myAgent.putO2AObject(e, myAgent.ASYNC);
				} catch (StaleProxyException exc) {
						exc.printStackTrace();
						// in case an exception was thrown, restart JADE
						// and then reexecute the command
						restartJADE();
						myAgent.putO2AObject(e, myAgent.ASYNC);
				}
				// wait until the answer is ready
				e.waitUntilProcessed();
		}

		/**
		 * This method checks if both the container, and the agent, are up and running.
		 * If not, then the method is responsible for renewing myContainer
		 **/
		private final static void checkJADE() throws StaleProxyException,ControllerException {
				if (myContainer == null) {
						myContainer = Runtime.instance().createAgentContainer(profile == null ? new ProfileImpl(false) : new ProfileImpl(profile));
				}
				if (myAgent == null) {
						myAgent = myContainer.createNewAgent("Control"+myContainer.getContainerName(), agentType, null);
						myAgent.start();
				}
		}

		/** Restart JADE.
		* The method tries to kill both the agent and the container,
		* then it puts to null the values of their controllers,
		* and finally calls checkJADE
		**/
		private final static void restartJADE() throws StaleProxyException,ControllerException {
				try { // try to kill, but neglect any exception thrown
						if (myAgent != null)
								myAgent.kill();
				} catch (Exception e) {
				}
				try { // try to kill, but neglect any exception thrown
						if (myContainer != null)
								myContainer.kill();
				} catch (Exception e) {
				}
				myAgent = null;
				myContainer = null;
				checkJADE();
		}

		/**
		 * Initialize this gateway by passing the proper configuration parameters
		 * @param agentClassName is the fully-qualified class name of the agent to be executed 
		 * @param jadeProfile the properties that contain all parameters for running JADE (see jade.core.Profile).
		 * Typically these properties will have to be read from a JADE configuration file.
		 * If jadeProfile is null, then a JADE container attaching to a main on the local host is launched
		 **/
		public static void init(String agentClassName, Properties jadeProfile) {
				agentType = agentClassName;
				profile = jadeProfile; 
		}
		
		/** This private constructor avoids other objects to create a new instance of this singleton **/
		private JadeGateway() {
		}

}
