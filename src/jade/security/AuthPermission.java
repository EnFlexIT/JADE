package jade.security;

import java.security.Permission;
import java.security.PermissionCollection;

import java.util.StringTokenizer;


public class AuthPermission extends java.security.BasicPermission implements java.io.Serializable {

	private int mask = 0;
	private transient String actions;
													
	public AuthPermission(String name, String actions) {
		super(name, actions);
		if (name == null)
			throw new NullPointerException("Name can't be null");
		if (actions == null)
			throw new NullPointerException("Actions can't be null");
		this.mask = decodeActions(actions);
	}

	public String getActions() {
		if (actions == null)
			actions = encodeActions(this.mask);
		return actions;
	}

	public boolean implies(Permission p) {
		if (!getClass().isInstance(p))
			return false;
		AuthPermission that = (AuthPermission) p;
		return ((this.mask & that.mask) == that.mask) && super.implies(that);
	}
	
	public PermissionCollection newPermissionCollection() {
		return new AuthPermissionCollection();
	}
	
	public int getActionsMask() {
		return mask;
	}

	public String[] getAllActions() {
		return new String[] {};
	}

	private int decodeActions(String actions) {
		int mask = 0;
		if (actions == null)
			return mask;
		
		String[] allActions = getAllActions();
		StringTokenizer tokenizer = new StringTokenizer(actions, ", \r\n\f\t");
		while (tokenizer.hasMoreTokens()) {
			String action = tokenizer.nextToken();
			for (int i = 0; i < allActions.length; i++) {
				if (action.equals(allActions[i])) {
					mask |= 1 << i;
					continue;
				}
			}
		}
		return mask;
	}

	private String encodeActions(int mask) {
		StringBuffer sb = new StringBuffer();
		boolean comma = false;
		String[] allActions = getAllActions();
		
		for (int i = 0; i < allActions.length; i++) {
			int onebit = 1 << i;
			if ((mask & onebit) == onebit) {
				if (comma) sb.append(',');
				else comma = true;
				sb.append(allActions[i]);
			}
		}
		
		return sb.toString();
	}

	public String toString() {
		return "(" + getClass().getName() + " " + getName() + " " + getActions() + ")";
	}
}
