package jade.security;


public class ContainerPermission extends AuthPermission {
	
	private final static String[] allActions = new String[] {"create", "kill",
			"create-main", "kill-main", "create-in", "move-from", "move-to", "copy-from", "copy-to"};

	public String[] getAllActions() {
		return allActions;
	}

	/**
		Creates a new ContainerPermission.
		@param name The name of the permission.
		@param actions The actions of the permission.
	*/
	public ContainerPermission(String name, String actions) {
		super(name, actions);
	}
}
