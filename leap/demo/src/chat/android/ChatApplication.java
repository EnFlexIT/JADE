/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop 
multi-agent systems in compliance with the FIPA specifications.
Copyright (C) 2000 CSELT S.p.A. 

GNU Lesser General Public License

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation, 
version 2.1 of the License. 

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the
Free Software Foundation, Inc., 59 Temple Place - Suite 330,
Boston, MA  02111-1307, USA.
*****************************************************************/

package chat.android;

//#MIDP_EXCLUDE_FILE
//#J2SE_EXCLUDE_FILE
//#PJAVA_EXCLUDE_FILE

import jade.core.Profile;
import jade.util.Logger;
import jade.util.leap.Properties;
import android.app.Application;

/**
 * This is the Android Chat Demo Application.
 * 
 * @author Michele Izzo - Telecom Italia
 */

public class ChatApplication extends Application {
	private Logger logger = Logger.getMyLogger(this.getClass().getName());

	private static Properties properties;
	
	@Override
	public void onCreate() {
		super.onCreate();

	    ChatGateway gateway = ChatGateway.getInstance();

	    logger.info("Create properties " + getString(R.string.default_host) + ":" + getString(R.string.default_port));
	    
		properties = new Properties();
		properties.setProperty(Profile.MAIN_HOST,
				getString(R.string.default_host));
		properties.setProperty(Profile.MAIN_PORT,
				getString(R.string.default_port));

		gateway.init(getApplicationContext(), properties);
	}
	
	public static Properties getProperties() {
		return properties;
	}
}
