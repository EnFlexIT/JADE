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

package examples.thanksAgent;

import jade.core.Agent;
import jade.core.AID;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.DFServiceCommunicator;
import jade.domain.FIPAException;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import jade.core.Runtime;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.*;


/**
 * This agent has the following functionality: 
 * <ul>
 * <li> When being born one will register 
 * <li> It will create a list of agents registered in DF.
 * <li> It will send a message to them of greeting 
 * <li> It will listen if there is some answer 
 * <li> To the agents who answer it will thank to them to answer. 
 * </ul>
 * @author Fabio Bellifemine, TILab
 * @version $Date$ $Revision$
 **/
public class ThanksAgent extends Agent {

    private static boolean IAmTheCreator = true;

    public final static String GREETINGS = "GREETINGS";
    public final static String ANSWER = "ANSWER";
    public final static String THANKS = "THANKS";

    protected void setup() {
	System.out.println(getLocalName()+" STARTED ITS JOB");

	try {
	    // create the agent descrption of itself
	    DFAgentDescription dfd = new DFAgentDescription();
	    dfd.setName(getAID());
	    // register the description with the DF
	    DFServiceCommunicator.register(this, dfd);
	    System.out.println(getLocalName()+" REGISTERED WITH THE DF");
	} catch (FIPAException e) {
	    e.printStackTrace();
	}

	if (IAmTheCreator) {
	    IAmTheCreator = false;  // next agent in this JVM will not be a creator

	    // create another two ThanksAgent
	    String t1AgentName = getLocalName()+"t1";
	    String t2AgentName = getLocalName()+"t2";

	    ThanksAgent t1 = new ThanksAgent();
	    t1.doStart(t1AgentName);
	    System.out.println(getLocalName()+" CREATED AND STARTED NEW THANKSAGENT:"+t1.getLocalName());
	    ThanksAgent t2 = new ThanksAgent();
	    t2.doStart(t2AgentName);
	    System.out.println(getLocalName()+" CREATED AND STARTED NEW THANKSAGENT:"+t2.getLocalName()); 



	    /* THIS CODE WORKS AFTER JADE 2.3. 
	       IT IS AN EXAMPLE OF USAGE OF THE INPROCESS INTERFACE
	    // Get a hold on JADE runtime
	    Runtime rt = Runtime.instance();
	    // Create a default profile
	    ProfileImpl p = new ProfileImpl();
	    // set the profile to be non-main container
	    p.putProperty(Profile.MAIN, "false");

	    try {
		// Create a new non-main container, connecting to the default
		// main container (i.e. on this host, port 1099)
		AgentContainer ac = rt.createAgentContainer(p);
		// create a new agent
		jade.wrapper.Agent t1 = ac.createAgent(t1AgentName,getClass().getName(),new Object[0]);
		// fire-up the agent
		t1.start();
		System.out.println(getLocalName()+" CREATED AND STARTED NEW THANKSAGENT:"+t1AgentName);
		// create a new agent
		jade.wrapper.Agent t2 = ac.createAgent(t2AgentName,getClass().getName(),new Object[0]);
		// fire-up the agent
		t2.start();
		System.out.println(getLocalName()+" CREATED AND STARTED NEW THANKSAGENT:"+t2AgentName);
	    } catch (Exception e2) {
		e2.printStackTrace();
	    }
	    */

	    // send them a GREETINGS message
	    ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
	    msg.setContent(GREETINGS);

	    msg.addReceiver(new AID(t1AgentName, AID.ISLOCALNAME));
	    msg.addReceiver(new AID(t2AgentName, AID.ISLOCALNAME));

	    send(msg);
	    System.out.println(getLocalName()+" SENT GREETINGS MESSAGE : "+msg); 
	}  /* IF YOU COMMENTED OUT THIS ELSE CLAUSE, THEN YOU WOULD GENERATE
	      AN INTERESTING INFINITE LOOP WITH INFINTE AGENTS AND AGENT 
	      CONTAINERS BEING CREATED 
	      else {
	      IAmTheCreator = true;
	      doWait(2000); // wait two seconds
	      }
	   */

	// add a Behaviour that listen if a greeting message arrives
	// and sends back an ANSWER.
	// if an ANSWER to a greetings message is arrived 
	// then send a THANKS message
	addBehaviour(new CyclicBehaviour(this) {
		public void action() {
		    // listen if a greetings message arrives
		    ACLMessage msg = receive(MessageTemplate.MatchPerformative(ACLMessage.INFORM));
		    if (msg != null) {
			if (GREETINGS.equalsIgnoreCase(msg.getContent())) {
			    // if a greetings message is arrived then send an ANSWER
			    System.out.println(myAgent.getLocalName()+" RECEIVED GREETINGS MESSAGE : "+msg); 
			    ACLMessage reply = msg.createReply();
			    reply.setContent(ANSWER);
			    myAgent.send(reply);
			    System.out.println(myAgent.getLocalName()+" SENT ANSWER MESSAGE : "+reply); 
			} else if (ANSWER.equalsIgnoreCase(msg.getContent())) {
			    // if an ANSWER to a greetings message is arrived 
			    // then send a THANKS message
			    System.out.println(myAgent.getLocalName()+" RECEIVED ANSWER MESSAGE : "+msg); 
			    ACLMessage replyT = msg.createReply();
			    replyT.setContent(THANKS);
			    myAgent.send(replyT);
			    System.out.println(myAgent.getLocalName()+" SENT THANKS MESSAGE : "+replyT); 
			}
		    } else // if no message is arrived, block the behaviour
			block();
		}
	    });
    }

}
