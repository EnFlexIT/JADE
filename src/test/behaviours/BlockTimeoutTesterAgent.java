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

package test.behaviours;

import jade.core.Agent;
import jade.core.Runtime;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.*;
import test.common.*;

/**
 * @author Giovanni Caire - TILAB
 */
public class BlockTimeoutTesterAgent extends TesterAgent {
	// Names and default values for group arguments
	public static final String N_AGENTS_NAME = "n-agents";
	private static final int N_AGENTS_DEFAULT = 10;
	
	public static final String N_MESSAGES_NAME = "n-messages";
	private static final int N_MESSAGES_DEFAULT = 100;
	
	public static final String PERIOD_NAME = "period";
	private static final long PERIOD_DEFAULT = 10;
	
	public static final String TIMEOUT_INCREASE_NAME = "timeout-increase";
	private static final long TIMEOUT_INCREASE_DEFAULT = 0;
	
	protected TestGroup getTestGroup() {
		TestGroup tg = new TestGroup(new String[] {
			"test.behaviours.tests.TestBlockTimeout"
		} );
		
		tg.specifyArgument(N_AGENTS_NAME, "Number of senders", String.valueOf(N_AGENTS_DEFAULT));
		tg.specifyArgument(N_MESSAGES_NAME, "Number of messages", String.valueOf(N_MESSAGES_DEFAULT));
		tg.specifyArgument(PERIOD_NAME, "Shortest period in ms", String.valueOf(PERIOD_DEFAULT));
		tg.specifyArgument(TIMEOUT_INCREASE_NAME, "Failure timeout increase", String.valueOf(TIMEOUT_INCREASE_DEFAULT));
		
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

      AgentController tester = mc.createNewAgent("tester", "test.behaviours.BlockTimeoutTesterAgent", args);
      tester.start();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
