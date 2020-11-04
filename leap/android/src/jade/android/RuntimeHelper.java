/**
 * ***************************************************************
 * JADE - Java Agent DEvelopment Framework is a framework to develop
 * multi-agent systems in compliance with the FIPA specifications.
 * Copyright (C) 2000 CSELT S.p.A.
 * 
 * GNU Lesser General Public License
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation,
 * version 2.1 of the License.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 * **************************************************************
 */
package jade.android;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.util.leap.Properties;

/**
 * @author Federico Bergenti - Universita' di Parma
 */
class RuntimeHelper {
	
	// Used for simplified MicroRuntime startup
	public static Properties createConnectionProperties(String address, int port) {
		Properties properties = new Properties();
		properties.setProperty(Profile.MAIN, Boolean.FALSE.toString());
		properties.setProperty(Profile.MAIN_HOST, address);
		if (port <= 0)
			port = Profile.DEFAULT_PORT;
		properties.setProperty(Profile.MAIN_PORT, Integer.toString(port));
		completeProperties(properties);
		return properties;
	}
	
	public static void completeProperties(Properties properties) {
		if (properties.getProperty(Profile.JVM, null) == null)
			properties.setProperty(Profile.JVM, Profile.ANDROID);
	}

	// Used for simplified AgentContainer startup
	public static Profile createContainerProfile(String address, int port) {
		Properties properties = new Properties();
		properties.setProperty(Profile.MAIN, Boolean.FALSE.toString());
		properties.setProperty(Profile.MAIN_HOST, address);
		if (port <= 0)
			port = Profile.DEFAULT_PORT;
		properties.setProperty(Profile.MAIN_PORT, Integer.toString(port));
		Profile p = new ProfileImpl(properties);
		completeProfile(p);
		return p;
	}

	// Used for simplified MainContainer startup
	public static Profile createMainProfile() {
		Properties properties = new Properties();
		properties.setProperty(Profile.MAIN, Boolean.TRUE.toString());
		Profile p = new ProfileImpl(properties);
		completeProfile(p);
		return p;
	}

	public static void completeProfile(Profile profile) {
		if (profile.getParameter(Profile.JVM, null) == null)
			profile.setParameter(Profile.JVM, Profile.ANDROID);

		if (profile.getParameter(Profile.LOCAL_HOST, null) == null)
			profile.setParameter(Profile.LOCAL_HOST, AndroidHelper.getLocalIPAddress());
	}
}
