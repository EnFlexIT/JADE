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

package test.roundTripTime;

import jade.core.Agent;
import jade.core.Runtime;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.*;
import test.common.*;

/**
 * @author Giovanni Caire - TILAB
 */
public class RoundTripTimeTesterAgent extends TesterAgent {
	// Names and default values for group arguments
	public static final String N_COUPLES_KEY = "couples";
	public static final int N_COUPLES_DEFAULT = 10;
	
	public static final String N_ITERATIONS_KEY = "iterations";
	public static final int N_ITERATIONS_DEFAULT = 1000;
	
	public static final String REMOTE_HOST_KEY = "host";
	public static final String REMOTE_HOST_DEFAULT = "localhost";
	
	public static final String BENCHMARK_CLASSPATH1_KEY = "bch-classpath1";
	public static final String BENCHMARK_CLASSPATH2_KEY = "bch-classpath2";
	public static final String BENCHMARK_CLASSPATH_DEFAULT = "c:/jade/add-ons/benchmark/classes/";

	// Roundtrippers and receivers classes
	public static final String ROUNDTRIPPER_CLASS = "benchmark.roundTripTime.RoundTripSender";
	public static final String RECEIVER_CLASS = "benchmark.roundTripTime.RoundTripReceiver";
	
	protected TestGroup getTestGroup() {
		TestGroup tg = new TestGroup("test/roundTripTime/roundTripTimeTestsList.xml");		
		
		tg.specifyArgument(N_COUPLES_KEY, "Number of couples", String.valueOf(N_COUPLES_DEFAULT));
		tg.specifyArgument(N_ITERATIONS_KEY, "Number of iterations", String.valueOf(N_ITERATIONS_DEFAULT));
		tg.specifyArgument(REMOTE_HOST_KEY, "The remote host for tests with 2 machines", String.valueOf(REMOTE_HOST_DEFAULT));
		tg.specifyArgument(BENCHMARK_CLASSPATH1_KEY, "Benchmark add-on additional classpath 1", BENCHMARK_CLASSPATH_DEFAULT);
		tg.specifyArgument(BENCHMARK_CLASSPATH2_KEY, "Benchmark add-on additional classpath 2", BENCHMARK_CLASSPATH_DEFAULT);
		
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

      AgentContainer mc = rt.createMainContainer(pMain);

      AgentController rma = mc.createNewAgent("rma", "jade.tools.rma.rma", new Object[0]);
      rma.start();

      AgentController tester = mc.createNewAgent("tester", "test.behaviours.RoundTripTimeTesterAgent", args);
      tester.start();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
