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

package examples.protocols;

import jade.core.Runtime;
import jade.core.Profile;
import jade.core.ProfileImpl;

import jade.wrapper.*;

/**
*
* This examples permits to launch a JADE platform with an <code>Initiator</code> agent  
* and some <code>Responder</code> agents to test the <code>AchieveREResponder</code> and 
<code>AchieveREInitiator</code> protocols using the inProcess interface.
*
* To run this  tester the user just have run this class giving as command line argument the names of 
* the responder agents.
* 
* Using for examples the following command line, the programs starts an <code>Initiator</code> agent sending 
* his messages to three <code>Responder</code> agents receiver1, receiver2. receiver3: 
* java -classpath <jade-path> ProtocolTester receiver1 receiver2 receiver3
*
* @author Tiziana Trucco - Telecom Italia Lab S.p.A
* @version $Date$ $Revision$
**/
public class ProtocolTester {


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

      System.out.println("Launching the rma agent on the main container ...");
      Agent rma = mc.createAgent("rma", "jade.tools.rma.rma", new Object[0]);
      rma.start();

      if( args.length > 0 ){
	  System.out.println( "Launching the Initiator Agent" );
	  
	  Object[] arguments = new Object[args.length];
	  for(int i=0;i<args.length;i++){
	      System.out.println( "Launching the Responder Agent: " + args[i]);
	      Agent responder = mc.createAgent(args[i], "examples.protocols.Responder",new Object[0]);
	      responder.start();
	      arguments[i]=args[i];
	  }
	  Agent requester = mc.createAgent( "initiator", "examples.protocols.Initiator",arguments);
	  requester.start();
      }else{
	  //start the initiator but then it will exit.
	  System.out.println( "Launching the Initiator Agent" );
	  Agent requester = mc.createAgent( "initiator", "examples.protocols.Initiator",new Object[0]);
	  requester.start();
      }
      

    }
    catch(Exception e) {
      e.printStackTrace();
    }

  }

}//end class protocol tester.
