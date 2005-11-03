package jade.core;

//#MIDP_EXCLUDE_FILE
//#APIDOC_EXCLUDE_FILE

import jade.domain.introspection.IntrospectionServer;

public class ContainerMonitorAgent extends Agent {
	private AgentContainerImpl myContainer;
	private LADT myLADT;
	
	protected void setup() {
		Object[] args = getArguments();
		myContainer = (AgentContainerImpl) args[0];
		myLADT = (LADT) args[1];
		
		addBehaviour(new IntrospectionServer(this));
	}
	
	public String[] getLADTStatus() {
		return myLADT.getStatus();
	}
}
