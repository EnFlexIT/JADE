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
import jade.core.AID;
import jade.wrapper.*;
import jade.domain.*;
import jade.domain.JADEAgentManagement.*;
import jade.lang.acl.*;
import jade.proto.*;
import jade.content.*;
import jade.content.onto.*;
import jade.content.lang.*;
import jade.content.lang.sl.*;

import test.common.*;


/**
 * @author Giovanni Caire - TILAB
 * @author Elisabetta Cortese - TILAB
 */
public class PersistentDFTesterAgent extends TesterAgent {
	// keys for group arguments
	public static final String DB_URL_KEY = "db-url";
	public static final String DB_DRIVER_KEY = "db-driver";
	public static final String PERSISTENT_DF_CONTAINER_KEY = "container";
		
	protected TestGroup getTestGroup() {
		TestGroup tg = new TestGroup("test/domain/df/persistentDFTestsList.xml") {
			private JadeController jc;
			
			protected void initialize(Agent a) throws TestException {
		    // Read group arguments
		  	String url = (String) getArgument(DB_URL_KEY);
		  	String driver = (String) getArgument(DB_DRIVER_KEY);
  	
				// Kill the default DF and activate a persistent DF
				TestUtility.killAgent(a, a.getDefaultDF());
				jc = TestUtility.launchJadeInstance("Persistent", null, "-container -host "+TestUtility.getLocalHostName()+" -port "+String.valueOf(Test.DEFAULT_PORT)+" -jade_domain_df_verbosity 2 -jade_domain_df_db-url "+url+" -jade_domain_df_db-driver "+driver, new String[] {});
		  	TestUtility.createAgent(a, "df", "jade.domain.df", null, a.getAMS(), jc.getContainerName());
		  	setArgument(PERSISTENT_DF_CONTAINER_KEY, jc.getContainerName());
			}
			
			protected void shutdown(Agent a) {
				try {
					// Kill the persistent DF and restore the default one
					jc.kill();
			  	TestUtility.createAgent(a, "df", "jade.domain.df", null, a.getAMS(), "Main-Container");
				}
				catch (Exception e) {
					System.out.println("WARNING: can't restore default DF");
					e.printStackTrace();
				}
			}
		};
		tg.specifyArgument(DB_URL_KEY, "DB URL", "jdbc:odbc:dfdb");
		tg.specifyArgument(DB_DRIVER_KEY, "DB Driver", null);
			
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

      MainContainer mc = rt.createMainContainer(pMain);

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