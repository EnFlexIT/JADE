package jade.domain.mobility;

import jade.core.*;
import jade.content.*;

public class MobileAgentDescription implements Concept {

    private AID name;
    private Location destination;
    private MobileAgentProfile agentProfile;
    private String agentVersion;
    private String signature;

    public void setName(AID id) {
      name = id;
    }

    public AID getName() {
      return name;
    }

    public void setDestination(Location d) {
      destination = d;
    }

    public Location getDestination() {
      return destination;
    }

    public void setAgentProfile(MobileAgentProfile ap) {
      agentProfile = ap;
    }

    public MobileAgentProfile getAgentProfile() {
      return agentProfile;
    }

    public void setAgentVersion(String v) {
      agentVersion = v;
    }

    public String getAgentVersion() {
      return agentVersion;
    }

    public void setSignature(String s) {
      signature = s; 
    }

    public String getSignature() {
      return signature;
    }

  } 