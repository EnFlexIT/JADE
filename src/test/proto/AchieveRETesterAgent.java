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

package test.proto;

import jade.core.Agent;
import jade.core.Runtime;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.*;
import test.common.*;

/**
 */
public class AchieveRETesterAgent extends TesterAgent {
	protected TestGroup getTestGroup() {
		TestGroup tg = new TestGroup(new String[] {
			"test.proto.tests.achieveRE.TestMyselfAsResp",
			"test.proto.tests.achieveRE.TestMissingAgree",
			"test.proto.tests.achieveRE.TestTimeoutAndOutOfSeq",
			"test.proto.tests.achieveRE.TestRegisterHandleInform",
			"test.proto.tests.achieveRE.TestMissingNotification"
		});
		
		return tg;
	}
		
	// Main method that allows launching this test as a stand-alone program	
	public static void main(String[] args) {
		try {
      // Get a hold on JADE runtime
      Runtime rt = Runtime.instance();

      // Exit the JVM when there are no more containers around
      rt.setCloseVM(true);

      Profile pMain = new ProfileImpl(null, 8888, null);

      MainContainer mc = rt.createMainContainer(pMain);

      AgentController rma = mc.createNewAgent("rma", "jade.tools.rma.rma", new Object[0]);
      rma.start();

      AgentController tester = mc.createNewAgent("tester", "test.proto.AchieveRETesterAgent", args);
      tester.start();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
