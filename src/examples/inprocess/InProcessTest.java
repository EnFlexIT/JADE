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

import jade.core.AID;
import jade.core.Runtime;
import jade.core.Profile;
import jade.core.ProfileImpl;

import jade.wrapper.*;

/**
   This class is an example of how you can embed JADE runtime
   environment within your applications.

   @author Giovanni Rimassa - Universita` di Parma

 */
public class InProcessTest {


  public static void main(String args[]) {

    try {
      // Create a default profile
      Profile p = new ProfileImpl();

      // Get a hold on JADE runtime
      Runtime rt = Runtime.instance();

      // Create a new non-main container, connecting to the default
      // main container (i.e. on this host, port 1099)
      AgentContainer ac = rt.createAgentContainer(p);

      // Create a new agent, a DummyAgent
      AID id = new AID("inProcess", AID.ISLOCALNAME);
      Agent dummy = ac.createAgent(id, "jade.tools.DummyAgent.DummyAgent", new String[0]);

      // Fire up the agent
      dummy.start();

      // Create another peripheral container within the same JVM
      AgentContainer another = rt.createAgentContainer(p);

      // Launch the Mobile Agent example
      AID mobID = new AID("Johnny", AID.ISLOCALNAME);
      Agent mobile = another.createAgent(mobID, "examples.mobile.MobileAgent", new String[0]);
      mobile.start();

      // Wait for 5 seconds
      Thread.sleep(5000);

      // Kill the DummyAgent
      dummy.delete();

    }
    catch(Exception e) {
      e.printStackTrace();
    }

  }

}
