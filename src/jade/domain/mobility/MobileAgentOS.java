package jade.domain.mobility;

import jade.core.*;
import jade.content.*;

public class MobileAgentOS implements Concept {
    private String name;
    private Long majorVersion;
    private Long minorVersion;
    private String dependencies;

    public void setName(String n) {
      name = n;
    }

    public String getName() {
      return name;
    }

    public void setMajorVersion(Long v) {
      majorVersion = v;
    }

    public Long getMajorVersion() {
      return majorVersion;
    }

    public void setMinorVersion(Long v) {
      minorVersion = v;
    }

    public Long getMinorVersion() {
      return minorVersion;
    }

    public void setDependencies(String d) {
      dependencies = d;
    }

    public String getDependencies() {
      return dependencies;
    }

  } 