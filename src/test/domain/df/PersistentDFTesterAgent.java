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

package test.domain.df;

import jade.core.Agent;
import jade.core.Runtime;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.AgentContainer;
import jade.wrapper.*;


import test.common.*;


/**
 * @author Giovanni Caire - TILAB
 * @author Elisabetta Cortese - TILAB
 * @author Roland Mungenast - Profactor
 */
public class PersistentDFTesterAgent extends TesterAgent {
	// keys for group arguments
	public static final String DB_DEFAULT_KEY = "db-default";
	public static final String DB_URL_KEY = "db-url";
	public static final String DB_DRIVER_KEY = "db-driver";
	public static final String DB_USERNAME_KEY = "db-username";
	public static final String DB_PASSWORD_KEY = "db-password";
	public static final String DB_ADDITIONAL_CP_KEY = "db-additional-classpath";
	public static final String PERSISTENT_DF_CONTAINER_KEY = "container";
	public static final String POOLSIZE_KEY = "poolsize";
	
	protected TestGroup getTestGroup() {
		TestGroup tg = new TestGroup("test/domain/df/persistentDFTestsList.xml") {
			private JadeController jc;
			
			protected void initialize(Agent a) throws TestException {
				// Read group arguments
				String dbDefault = (String) getArgument(DB_DEFAULT_KEY);
				String dbDefaultOption = "";
				if (dbDefault != null && dbDefault.trim().length() > 0) {
					dbDefaultOption = "-jade_domain_df_db-default "+dbDefault;
				}
				String url = (String) getArgument(DB_URL_KEY);
				String urlOption = "";
				if (url != null && url.trim().length() > 0) {
					urlOption = "-jade_domain_df_db-url "+url;
				}
				String driver = (String) getArgument(DB_DRIVER_KEY);
				String driverOption = "";
				if (driver != null && driver.trim().length() > 0) {
					driverOption = "-jade_domain_df_db-driver "+driver;
				}
				String username = (String) getArgument(DB_USERNAME_KEY);
				String usernameOption = "";
				if (username != null && username.trim().length() > 0) {
					usernameOption = "-jade_domain_df_db-username "+username;
				}
				String password = (String) getArgument(DB_PASSWORD_KEY);
				String passwordOption = "";
				if (password != null && password.trim().length() > 0) {
					passwordOption = "-jade_domain_df_db-password "+password;
				}
				
				String poolsize = (String) getArgument(POOLSIZE_KEY);
				String poolsizeOption = "";
				if (poolsize != null && poolsize.trim().length() > 0) {
					poolsizeOption = "-jade_domain_df_poolsize "+poolsize;
				}
				
				String addClasspath = (String) getArgument(DB_ADDITIONAL_CP_KEY);
				if (addClasspath != null && addClasspath.trim().length() > 0) {
					addClasspath = "+"+addClasspath;
				}
				
				// Kill the default DF and activate a persistent DF
				TestUtility.killAgent(a, a.getDefaultDF());
				jc = TestUtility.launchJadeInstance("Persistent", addClasspath, "-container -host "+TestUtility.getLocalHostName()
						+ " -port "+String.valueOf(Test.DEFAULT_PORT)
						+ " " + urlOption + " " + driverOption + " " + usernameOption + " " + passwordOption + " " + dbDefaultOption + " " + poolsizeOption, new String[] {});
				
				TestUtility.createAgent(a, "df", "jade.domain.df", null, a.getAMS(), jc.getContainerName());
				setArgument(PERSISTENT_DF_CONTAINER_KEY, jc.getContainerName());
			}
			
			protected void shutdown(Agent a) {
				try {
					// Kill the persistent DF and restore the default one
					jc.kill();
					TestUtility.createAgent(a, "df", "jade.domain.df", null, a.getAMS(), AgentContainer.MAIN_CONTAINER_NAME);
				}
				catch (Exception e) {
					System.out.println("WARNING: can't restore default DF");
					e.printStackTrace();
				}
			}
		};
		
		tg.specifyArgument(DB_DEFAULT_KEY, "Use Default DB", "true");
		tg.specifyArgument(DB_URL_KEY, "DB URL", null);
		tg.specifyArgument(DB_DRIVER_KEY, "DB Driver", null);
		tg.specifyArgument(DB_USERNAME_KEY, "DB username", null);
		tg.specifyArgument(DB_PASSWORD_KEY, "DB password", null);
		tg.specifyArgument(POOLSIZE_KEY, "FIPA request serving pool-size", "2");
		tg.specifyArgument(DB_ADDITIONAL_CP_KEY, "DB Additional classpath", null);
		
		return tg;
	}
	
	// Main method that allows launching this test as a stand-alone program	
	public static void main(String[] args) {
		try {
			// Get a hold on JADE runtime
			Runtime rt = Runtime.instance();
			
			// Exit the JVM when there are no more containers around
			rt.setCloseVM(true);
			
			Profile pMain = new ProfileImpl(null, Test.DEFAULT_PORT, null);
			
			ContainerController mc = rt.createMainContainer(pMain);
			
			AgentController rma = mc.createNewAgent("rma", "jade.tools.rma.rma", new Object[0]);
			rma.start();
			
			AgentController tester = mc.createNewAgent("tester", "test.domain.df.DFTesterAgent", args);
			tester.start();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
