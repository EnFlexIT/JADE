  package jade.domain.mobility;
  
  import jade.domain.JADEAgentManagement.JADEManagementVocabulary;
  
  public interface MobilityVocabulary extends JADEManagementVocabulary {
  
  	public static final String MOBILE_AGENT_DESCRIPTION = "mobile-agent-description";
  	public static final String MOBILE_AGENT_DESCRIPTION_NAME = "name";
  	public static final String MOBILE_AGENT_DESCRIPTION_DESTINATION = "destination";
  	public static final String MOBILE_AGENT_DESCRIPTION_AGENT_PROFILE = "agent-profile";
  	public static final String MOBILE_AGENT_DESCRIPTION_AGENT_VERSION = "agent-version";
  	public static final String MOBILE_AGENT_DESCRIPTION_SIGNATURE = "signature";
  	  	
    public static final String MOBILE_AGENT_PROFILE = "mobile-agent-profile";
    public static final String MOBILE_AGENT_PROFILE_SYSTEM = "system";
    public static final String MOBILE_AGENT_PROFILE_LANGUAGE = "language";
    public static final String MOBILE_AGENT_PROFILE_OS = "os";
    
    public static final String MOBILE_AGENT_SYSTEM = "mobile-agent-system";
    public static final String MOBILE_AGENT_SYSTEM_NAME = "name";
    public static final String MOBILE_AGENT_SYSTEM_MAJOR_VERSION = "major-version";
    public static final String MOBILE_AGENT_SYSTEM_MINOR_VERSION = "minor-version";
    public static final String MOBILE_AGENT_SYSTEM_DEPENDENCIES = "dependencies";
    
    public static final String MOBILE_AGENT_LANGUAGE = "mobile-agent-language";
    public static final String MOBILE_AGENT_LANGUAGE_NAME = "name";
    public static final String MOBILE_AGENT_LANGUAGE_MAJOR_VERSION = "major-version";
    public static final String MOBILE_AGENT_LANGUAGE_MINOR_VERSION = "minor-version";
    public static final String MOBILE_AGENT_LANGUAGE_DEPENDENCIES = "dependencies";
       
    public static final String MOBILE_AGENT_OS = "mobile-agent-os";
    public static final String MOBILE_AGENT_OS_NAME = "name";
    public static final String MOBILE_AGENT_OS_MAJOR_VERSION = "major-version";
    public static final String MOBILE_AGENT_OS_MINOR_VERSION = "minor-version";
    public static final String MOBILE_AGENT_OS_DEPENDENCIES = "dependencies";
        
	public static final String MOVE = "move-agent";
	public static final String MOVE_MOBILE_AGENT_DESCRIPTION = "mobile-agent-description";
		
    public static final String CLONE = "clone-agent";
    public static final String CLONE_MOBILE_AGENT_DESCRIPTION = "mobile-agent-description";
    public static final String CLONE_NEW_NAME = "new-name";
    
  

    
  }