package jade.security;


public class AgentPermission extends AuthPermission implements java.security.Guard, java.io.Serializable {
	
	private final static String[] allActions = new String[] {"create", "kill",
			"suspend", "resume", "take", "send-to", "send-as", "move", "copy"};

	public String[] getAllActions() {
		return allActions;
	}

	/**
		Creates a new AgentPermission.
		@param name The name of the permission.
		@param actions The actions of the permission.
	*/
	public AgentPermission(String name, String actions) {
		super(name, actions);
	}
}
