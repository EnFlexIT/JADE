package jade.security;

import java.security.Permission;
import java.security.PermissionCollection;
import java.util.Hashtable;
import java.util.Enumeration;

public class AuthPermissionCollection extends PermissionCollection { 

	private Hashtable permissions = new Hashtable();

	public void add(Permission permission) {
		if (isReadOnly())
			throw new SecurityException("Attempt to add a Permission to a readonly PermissionCollection");
		if (!(permission instanceof AuthPermission))
			throw new IllegalArgumentException("Invalid permission: " + permission);
		
		String type = permission.getClass().getName();
		String name = permission.getName();
		String key = type + "/" + name;
		
		AuthPermission existing = (AuthPermission)permissions.get(key);
		if (existing != null) {
			try {
				String actions = existing.getActions() + "," + permission.getActions();
				AuthPermission combined = (AuthPermission)(Class.forName(type).getConstructor(new Class[] {String.class, String.class}).newInstance(new Object[] {name, actions}));
				permissions.put(key, combined);
			}
			catch (Exception e) { e.printStackTrace(); }
		}
		else {
			permissions.put(key, permission);
		}
	}

	public boolean implies(Permission permission) {
		if (!(permission instanceof AuthPermission))
			return false;

		AuthPermission p = (AuthPermission) permission;
		AuthPermission x;

		String type = p.getClass().getName();
		String name = p.getName();

		int desired = p.getActionsMask();
		int effective = 0;

		// strategy:
		// Check for full match first. Then work our way up the
		// name looking for matches on a.b.*

		x = (AuthPermission) permissions.get(type + "/" + name);
		if (x != null) {
			effective |= x.getActionsMask();
			if ((effective & desired) == desired)
				return true;
		}

		// work our way up the tree...
		int last = name.length();
		while ((last = name.lastIndexOf(".", last - 1)) != -1) {
			name = name.substring(0, last + 1) + "*";
			//System.out.println("check " + name);

			x = (AuthPermission) permissions.get(type + "/" + name);
			if (x != null) {
				effective |= x.getActionsMask();
				if ((effective & desired) == desired)
					return true;
			}
		}

		// let's take a look at the root
		x = (AuthPermission) permissions.get(type + "/*");
		if (x != null) {
			effective |= x.getActionsMask();
			if ((effective & desired) == desired)
				return true;
		}

		return false;
	}

	public Enumeration elements() {
		return permissions.elements();
	}
}
