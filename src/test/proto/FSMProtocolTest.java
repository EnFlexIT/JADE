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

import jade.core.Runtime;
import jade.core.Profile;
import jade.core.ProfileImpl;

import jade.wrapper.*;

/**
This class shows the use of the AchieveRE protocols Initiator and Responder.

   @author Tiziana Trucco TILab S.p.A

 */
public class FSMProtocolTest {


  public static void main(String args[]) {

    try {

      // Get a hold on JADE runtime
      Runtime rt = Runtime.instance();

      // Exit the JVM when there are no more containers around
      rt.setCloseVM(true);

      // Launch a complete platform on the 8888 port
      // create a default Profile 
      Profile pMain = new ProfileImpl(null, 8888, null);

      System.out.println("Launching a whole in-process platform..."+pMain);
      MainContainer mc = rt.createMainContainer(pMain);

      //System.out.println("Launching the rma agent on the main container ...");
      //Agent rma = mc.createAgent("rma", "jade.tools.rma.rma", new Object[0]);
      //rma.start();

      if( args.length > 0 ){
	  System.out.println( "Launching the FIPARequestInitiatorTest" );
	  //Agent requester = mc.createAgent( "requester", "test.proto.FIPARequestInitiatorTest",args);
	  Object[] arguments = new Object[args.length];
	  for(int i=0;i<args.length;i++){
	      System.out.println( "Launching the FIPARequestResponderTest: " + args[i]);
	      Agent responder = (Agent) mc.createNewAgent(args[i], "test.proto.FIPARequestResponderTest",new Object[0]);
	      responder.start();
	      arguments[i]=args[i];
	  }
	  Agent requester = (Agent) mc.createNewAgent( "requester", "test.proto.FIPARequestInitiatorTest",arguments);
	  requester.start();
      }else{
	  System.out.println( "Launching the FIPARequestInitiatorTest" );
	  Agent requester = (Agent) mc.createNewAgent( "requester", "test.proto.FIPARequestInitiatorTest",new Object[0]);
	  requester.start();
      }
      //requester.start();


      // System.out.println( "Launching the DummyAgent0" );
      //Agent da0 = mc.createAgent( "da0", "jade.tools.DummyAgent.DummyAgent", new Object[0]);
      //da0.start();

      //System.out.println( "Launching the DummyAgent1" );
      //Agent da1 = mc.createAgent( "da1", "jade.tools.DummyAgent.DummyAgent", new Object[0]);
      //da1.start();

    }
    catch(Exception e) {
      e.printStackTrace();
    }

  }

}
