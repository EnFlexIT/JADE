package jade.domain.mobility;

import jade.core.*;
import jade.content.*;

public class MoveAction implements AgentAction {

    private MobileAgentDescription agentToMove;

    public void setMobileAgentDescription(MobileAgentDescription desc) {
      agentToMove = desc;
    }

    public MobileAgentDescription getMobileAgentDescription() {
      return agentToMove;
    }

}
