package jade.wrapper.gateway;

import jade.core.AID;
import jade.core.MicroRuntime;
import jade.core.NotFoundException;
import jade.core.Profile;
import jade.util.Logger;
import jade.util.leap.Properties;
import jade.wrapper.ControllerException;
import jade.wrapper.StaleProxyException;

//#J2ME_EXCLUDE_FILE
//#ANDROID_EXCLUDE_FILE
//#DOTNET_EXCLUDE_FILE

public class SplitJadeGateway extends DynamicJadeGateway {

	private static Logger myLogger = Logger.getMyLogger(SplitJadeGateway.class.getName());
	
	@Override
	public void checkJADE() throws StaleProxyException, ControllerException {
		
		// Initialize JADE profile
		initProfile();
		
		// Start up the JADE runtime
		final Properties properties = profile.getProperties();
		MicroRuntime.startJADE(properties, new Runnable() {
			public void run() {
				myLogger.log(Logger.INFO,"Disconnected from the platform at " + properties.getProperty(Profile.MAIN_HOST) + ":" + properties.getProperty(Profile.MAIN_PORT));
			} 
		});
		
		if (MicroRuntime.isRunning()) {
			myLogger.log(Logger.INFO, "Connected to the platform at " + properties.getProperty(Profile.MAIN_HOST) + ":" + properties.getProperty(Profile.MAIN_PORT));
			
			if (myAgent == null) {
				
				// Prepare agent name
				if (agentName == null) {
					agentName = "Control"+MicroRuntime.getContainerName();
				}
				
				try {
					Class agentClass = Class.forName(agentType);
					if (GatewayAgent.class.isAssignableFrom(agentClass)) {
						
						// Include the gateway-listener into the agent arguments (see GatewayAgent)  
						if (agentArguments == null) {
							agentArguments = new Object[1];
							agentArguments[0] = new GatewayListenerImpl();
						}
						
						// We are able to detect the GatewayAgent state only if the internal agent is a GatewayAgent instance
						gatewayAgentState = NOT_ACTIVE;
					}
				} catch (ClassNotFoundException e) {
					throw new ControllerException("GatewayAgent class not found [" + e + "]");
				}
				
				// Start gateway agent
				try {
					MicroRuntime.startAgent(agentName, agentType, agentArguments);
					
					if (gatewayAgentState == NOT_ACTIVE) {
						// Set the ACTIVE state synchronously so that when checkJADE() completes isGatewayActive() certainly returns true 
						gatewayAgentState = ACTIVE;
					}
				} catch (Exception e) {
					throw new ControllerException("Error creating GatewayAgent [" + e + "]");
				}
				
				myAgent = MicroRuntime.getAgent(agentName);
			}		
		}
		else {
			myLogger.log(Logger.WARNING, "Cannot connect to the platform at " + properties.getProperty(Profile.MAIN_HOST) + ":" + properties.getProperty(Profile.MAIN_PORT));
			shutdown();
		}
	}
	
	@Override
	public final void shutdown() {
		if (myAgent != null) {
			try {
				MicroRuntime.killAgent(agentName);
			} catch (NotFoundException e) {
				myLogger.log(Logger.WARNING, "Try to kill a not present agent "+agentName);
			}
			myAgent = null;
		}
		
		MicroRuntime.stopJADE();
	}
	
	@Override
	public boolean isGatewayActive() {
		if (gatewayAgentState != UNKNOWN) {
			return gatewayAgentState == ACTIVE;
		}
		else {
			// If we are not able to monitor the actual gatewayAgentState, just check if MicroRuntime is running and myAgent is not null
			return MicroRuntime.isRunning() && myAgent != null;
		}
	}

	@Override
	public AID createAID(String localName) {
		return new AID(localName, AID.ISLOCALNAME);
	}
}
