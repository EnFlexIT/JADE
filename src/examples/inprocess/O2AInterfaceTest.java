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

package examples.inprocess;


import jade.core.Agent;
import jade.core.Runtime;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.*;


/**
   This class is an example of how you can embed JADE runtime
   environment within your applications.

   @author Giovanni Iavarone - Michele Izzo

 */


public class O2AInterfaceTest {

  // This class is a agent that implements O2AInterface 
	
  public static class Agent1 extends  Agent implements O2AInterface1,O2AInterface2 {	
		/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


		public Agent1() {
			registerO2AInterface(O2AInterface1.class, this);

			registerO2AInterface(O2AInterface2.class, this);
		}

		// This is from O2AInterface1
		@Override
		public void method1() {
			System.out.println("Method 1");
		}
//		public void method2() throws CloneNotSupportedException {
		public void method2() {
			System.out.println("Method 2");

//			throw new CloneNotSupportedException();
		}

	   

  } // End of Agent1 class
  
	public interface O2AInterface2 extends O2AInterface {
//		public void method2() throws CloneNotSupportedException;
		public void method2() ;
	}

	public interface O2AInterface1 extends O2AInterface {
		public void method1();
	}	

	public interface O2AInterface {
	}
 
public static void main(String[] args) throws StaleProxyException {
	 Runtime rt = Runtime.instance();
	// Launch a complete platform on the 8888 port
    // create a default Profile 
    Profile pMain = new ProfileImpl(null, 8888, null);

    System.out.println("Launching a whole in-process platform..."+pMain);
    AgentContainer mc = rt.createMainContainer(pMain);

    // set now the default Profile to start a container
    ProfileImpl pContainer = new ProfileImpl(null, 8888, null);
    System.out.println("Launching the agent container ..."+pContainer);
    
    System.out.println("Launching the rma agent on the main container ...");
    AgentController rma = mc.createNewAgent("rma", "jade.tools.rma.rma", new Object[0]);
    rma.start();

	
	  // Get a hold on JADE runtime
     
      Profile p = new ProfileImpl(false);
	 AgentContainer ac = rt.createAgentContainer(p);

	  // Create a new agent, Agent1O2A
	  AgentController controller = ac.createNewAgent("AgentO2A",  Agent1.class.getName(), new Object[0]);

	  // Fire up the agent
	  System.out.println("Starting up a AgentO2A...");
	  controller.start();
	  O2AInterface1 o2a1 = null;

	try {
		o2a1 = controller.getO2AInterface(O2AInterface1.class);
	} catch(StaleProxyException e) {
		e.printStackTrace();
	}

	// This is implemented by the agent
	o2a1.method1();

	O2AInterface2 o2a2 = null;

	try {
		o2a2 = controller.getO2AInterface(O2AInterface2.class);
	} catch(StaleProxyException e) {
		e.printStackTrace();
	}

	// This is implemented by the agent
	o2a2.method2();
  }
}