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

import java.io.*;

import jade.core.*;
import jade.core.behaviours.*;
import jade.proto.*;
import jade.lang.acl.*;
import jade.domain.FIPAAgentManagement.*;
import jade.util.leap.*;

/**
 * This example shows an agent performing the role of the initiator of a FIPARequest of FIPAQuery protocol.
 * It sends random  one of the following performatives:
 * <ul>
 * <li> REQUEST <code>ACLMessage</code> to start a FIPA request protocol
 * <li> QUERY-IF or QUERY-REF <code>ACLMessage</code> to start a FIPA Query protocol. 
 * </ul>
 * The user must pass as arguements to the initiator agent the local names of the responder agents otherwise the agent exits.
 * This example show how a initiator role of a FIPARequest or FIPAQuery protocol can be realized using the
 * <code>AchiveREInitiator</code> behaviour. 
 * @see jade.proto.AchieveREInitiator
 * @see jade.proto.AchiveREResponder
 * @author Tiziana Trucco - Telecom Italia Lab S.p.A
 * @version $Date$ $Revision$
 **/



public class Initiator extends Agent {

    /**
       Creare a new ACLMessage to start a protocol.
     **/
    ACLMessage createNewMessage(){
	
	ACLMessage messageToSend = new ACLMessage(ACLMessage.NOT_UNDERSTOOD);
	double chance = Math.random();
	double range = 1.0/3.0;

	//set the receivers according to the parameter setted.
	Object[] receivers = getArguments();
	if(receivers == null || receivers.length == 0){
	    //no receiver set as argument of the agent
	    System.out.println( "ERROR: No responders passed as arguments. To run this example launch some responders and pas its local names as argument to the initiator agent" );
	    //doDelete();
		System.exit(0);
	}else{
	    //set the receiver of the message
	    for (int i=0; i<receivers.length; i++)
		messageToSend.addReceiver(new AID((String)receivers[i],false));
	}
	//set the timeout to 10 seconds
	long timeout = System.currentTimeMillis() + 10000;
	messageToSend.setReplyByDate(new java.util.Date(timeout));


	if (chance <range)
	    {//sending a REQUEST
		messageToSend.setPerformative(ACLMessage.REQUEST);
		messageToSend.setProtocol(jade.proto.FIPAProtocolNames.FIPA_REQUEST);
	    }else if(chance < (range * 2)){
		//sending a QUERY_REF
		messageToSend.setPerformative(ACLMessage.QUERY_REF);
		messageToSend.setProtocol(jade.proto.FIPAProtocolNames.FIPA_QUERY);
		System.out.println("Initiator is sending a Query-Ref message");
	    }else {
		//sending a QUERY_IF
		messageToSend.setPerformative(ACLMessage.QUERY_IF);
		messageToSend.setProtocol(jade.proto.FIPAProtocolNames.FIPA_QUERY);
		System.out.println("Initiator is sending a Query-If message");
	    }
	
	System.out.println(getLocalName()+" is sending a "+ACLMessage.getPerformative(messageToSend.getPerformative())+" message to initiate the protocol");
	return messageToSend;
    }
    
    public void setup() {
	
	Behaviour b;
	ACLMessage request = createNewMessage();
	//System.out.println(request);
	b = new MyInitiator(this, request, true);
	addBehaviour(b);
    }
    
    /**
       Inner class MyInitiator
    */
    class MyInitiator extends AchieveREInitiator {
	boolean restartOnEnd;
	/**
	 * @param req the ACLMessage to be sent in order to initiate the protocol
	 * @param restartOnEnd if true the behaviour is reset and readded
	 * to the agent queue of behaviours as soon as it terminates. Take
	 * care becuase this might cause an infinite loop.
	 *
	 **/
	public MyInitiator(Agent a, ACLMessage req, boolean restartOnEnd) {
	    super(a, req);
	    this.restartOnEnd = restartOnEnd;
	}
	
	protected void handleAgree(ACLMessage agree) {
	    System.out.println(myAgent.getLocalName()+ " in handleAgree: " + agree);
	}
	
	protected void handleRefuse(ACLMessage refuse) {
	    System.out.println(myAgent.getLocalName()+ " in handleRefuse: " + refuse);
	}
	protected void handleFailure(ACLMessage failure) {
	    System.out.println(myAgent.getLocalName()+ " in handleFailure: " + failure);
	}
	protected void handleNotUnderstood(ACLMessage notUnderstood) {
	    System.out.println(myAgent.getLocalName()+ " in handleNotUnderstood: " + notUnderstood);
	}
	protected void handleInform(ACLMessage inform) {
	    System.out.println(myAgent.getLocalName()+ " in handleInform: " + inform);
	}
	protected void handleAllResponses(List allResponses) {
	    System.out.print(myAgent.getLocalName()+ " in handleAllResponses: ");
	    Iterator it = allResponses.iterator();
	    while(it.hasNext()) {
		ACLMessage rsp = (ACLMessage) it.next();
		System.out.print(ACLMessage.getPerformative(rsp.getPerformative())+" ");
	    }
	    System.out.println();
	}

	protected void handleAllResultNotifications(List allResultNotifications) {
	    System.out.println(myAgent.getLocalName()+ " in handleAllResultNotifications: ");
	    Iterator it = allResultNotifications.iterator();
	    while(it.hasNext()) {
		ACLMessage rsp = (ACLMessage) it.next();
		System.out.print(ACLMessage.getPerformative(rsp.getPerformative())+" ");
	    }
	    System.out.println();
	}

	protected void handleOutOfSequence(ACLMessage msg){
	    System.out.println(myAgent.getLocalName()+ " in handleOutOfSequence: " + msg);
	}
	
	protected List prepareRequests(ACLMessage msg) {
	    List l = super.prepareRequests(msg);
	    if (l.size() == 0) //msg was null because the beahviour was reset
		l.add(createNewMessage());
	    return l;
	}

	public int onEnd(){
	    try{
		if (restartOnEnd) {
		    System.out.println("\nPress a Key to repeat the protocol: ");
		    BufferedReader buff = new BufferedReader(new InputStreamReader(System.in));
		    String reply = buff.readLine();
		
		    reset(createNewMessage());
		    myAgent.addBehaviour(this);
		}
	    }catch(Exception e){
		e.printStackTrace();
	    }
	    return super.onEnd();
	}
	
    } // End of inner class MyInitiator
    
}//end class Initiator
