package jade.wrapper.gateway;

import jade.core.AID;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.util.Event;
import jade.util.Logger;
import jade.util.leap.Properties;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.ControllerException;
import jade.wrapper.StaleProxyException;

public class DynamicJadeGateway {

	private ContainerController myContainer = null;
	private AgentController myAgent = null;
	private String agentType;
	// jade profile properties
	private ProfileImpl profile;
	private Properties jadeProps;
	private Object[] agentArguments;
	private static Logger myLogger = Logger.getMyLogger(DynamicJadeGateway.class.getName());
	
	
	/** Searches for the property with the specified key in the JADE Platform Profile. 
	 *	The method returns the default value argument if the property is not found. 
	 * @param key - the property key. 
	 * @param defaultValue - a default value
	 * @return the value with the specified key value
	 * @see java.util.Properties#getProperty(String, String)
	 **/
	public final String getProfileProperty(String key, String defaultValue) {
		return profile.getParameter(key, defaultValue);
	}
	
	/**
	 * execute a command. 
	 * This method first check if the executor Agent is alive (if not it
	 * creates container and agent), then it forwards the execution
	 * request to the agent, finally it blocks waiting until the command
	 * has been executed (i.e. the method <code>releaseCommand</code> 
	 * is called by the executor agent)
	 * @throws StaleProxyException if the method was not able to execute the Command
	 * @see jade.wrapper.AgentController#putO2AObject(Object, boolean)
	 **/
	public final void execute(Object command) throws StaleProxyException,ControllerException,InterruptedException {
		execute(command, 0);
	}
	
	/**
	 * Execute a command specifying a timeout. 
	 * This method first check if the executor Agent is alive (if not it
	 * creates container and agent), then it forwards the execution
	 * request to the agent, finally it blocks waiting until the command
	 * has been executed. In case the command is a behaviour this method blocks 
	 * until the behaviour has been completely executed. 
	 * @throws InterruptedException if the timeout expires or the Thread
	 * executing this method is interrupted.
	 * @throws StaleProxyException if the method was not able to execute the Command
	 * @see jade.wrapper.AgentController#putO2AObject(Object, boolean)
	 **/
	public final void execute(Object command, long timeout) throws StaleProxyException,ControllerException,InterruptedException {
		Event e = null;
		synchronized (JadeGateway.class) {
			checkJADE();
			// incapsulate the command into an Event
			e = new Event(-1, command);
			try {
				if (myLogger.isLoggable(Logger.INFO)) 
					myLogger.log(Logger.INFO, "Requesting execution of command "+command);
				myAgent.putO2AObject(e, AgentController.ASYNC);
			} catch (StaleProxyException exc) {
				exc.printStackTrace();
				// in case an exception was thrown, restart JADE
				// and then reexecute the command
				restartJADE();
				myAgent.putO2AObject(e, AgentController.ASYNC);
			}
		}
		// wait until the answer is ready
		e.waitUntilProcessed(timeout);
	}
	
	/**
	 * This method checks if both the container, and the agent, are up and running.
	 * If not, then the method is responsible for renewing myContainer.
	 * Normally programmers do not need to invoke this method explicitly.
	 **/
	public final void checkJADE() throws StaleProxyException,ControllerException {
		if (myContainer == null) {
			initProfile();
			
			myContainer = Runtime.instance().createAgentContainer(profile); 
			if (myContainer == null) {
				throw new ControllerException("JADE startup failed.");
			}
		}
		if (myAgent == null) {
			myAgent = myContainer.createNewAgent("Control"+myContainer.getContainerName(), agentType, agentArguments);
			myAgent.start();
		}
	}
	
	/** Restart JADE.
	 * The method tries to kill both the agent and the container,
	 * then it puts to null the values of their controllers,
	 * and finally calls checkJADE
	 **/
	final void restartJADE() throws StaleProxyException,ControllerException {
		shutdown();
		checkJADE();
	}
	
	/**
	 * Initialize this gateway by passing the proper configuration parameters
	 * @param agentClassName is the fully-qualified class name of the JadeGateway internal agent. If null is passed
	 * the default class will be used.
	 * @param agentArgs is the list of agent arguments
	 * @param jadeProfile the properties that contain all parameters for running JADE (see jade.core.Profile).
	 * Typically these properties will have to be read from a JADE configuration file.
	 * If jadeProfile is null, then a JADE container attaching to a main on the local host is launched
	 **/
	public final void init(String agentClassName, Object[] agentArgs, Properties jadeProfile) {
		agentType = agentClassName;
		if (agentType == null) {
			agentType = GatewayAgent.class.getName();
		}
		
		jadeProps = jadeProfile;
		if (jadeProps != null) {
			// Since we will create a non-main container --> force the "main" property to be false
			jadeProps.setProperty(Profile.MAIN, "false");
		}
		
		agentArguments = agentArgs;
	}

	public final void init(String agentClassName, Properties jadeProfile) {
		init(agentClassName, null, jadeProfile);
	}
	
	final void initProfile() {
		// to initialize the profile every restart, otherwise an exception would be thrown by JADE
		profile = (jadeProps == null ? new ProfileImpl(false) : new ProfileImpl(jadeProps));
	}
	
	/**
	 * Kill the JADE Container in case it is running.
	 */
	public final void shutdown() {
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
	}
	
	/**
	 * Return the state of JadeGateway
	 * @return true if the container and the gateway agent are active, false otherwise
	 */
	public final boolean isGatewayActive() {
		return myContainer != null && myAgent != null;
	}
	
	public AID createAID(String localName) {
		return new AID(localName+'@'+myContainer.getPlatformName(), AID.ISGUID);
	}
}
