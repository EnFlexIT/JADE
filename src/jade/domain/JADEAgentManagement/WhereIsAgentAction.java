package jade.domain.JADEAgentManagement;

import jade.core.*;
import jade.content.*;

public class WhereIsAgentAction implements AgentAction {

    private AID agentName;


	public void set_0(AID id) {
		setAgentIdentifier(id);
	}
	
	public AID get_0() {
		return getAgentIdentifier();
	}
	
    public void setAgentIdentifier(AID id) {
      agentName = id;
    }

    public AID getAgentIdentifier() {
      return agentName;
    }
}
