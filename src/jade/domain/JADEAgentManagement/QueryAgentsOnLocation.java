package jade.domain.JADEAgentManagement;

import jade.core.*;
import jade.content.*;

public class QueryAgentsOnLocation implements AgentAction {

  private Location location;
  
  public void setLocation(Location loc) {
    location = loc;
  }

  public Location getLocation() {
    return location;
  }

}