/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop multi-agent systems in compliance with the FIPA specifications.
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

/*
  $Log$
  Revision 1.13  2000/02/17 09:35:29  trucco
  no message

  Revision 1.12  1999/09/02 15:01:55  rimassa
  Handled new ParseException exception of ACLMessage.fromText().

  Revision 1.11  1999/05/20 14:12:40  rimassa
  Updated import clauses to reflect JADE package structure changes.

  Revision 1.10  1999/02/25 08:01:27  rimassa
  Changed direct access to 'myName' and 'myAddress' variables to
  accessor method calls.

  Revision 1.9  1999/02/14 22:52:05  rimassa
  Renamed addBehaviour() calls to addSubBehaviour() calls.

  Revision 1.8  1998/10/18 16:10:38  rimassa
  Some code changes to avoid deprecated APIs.

   - Agent.parse() is now deprecated. Use ACLMessage.fromText(Reader r) instead.
   - ACLMessage() constructor is now deprecated. Use ACLMessage(String type)
     instead.
   - ACLMessage.dump() is now deprecated. Use ACLMessage.toText(Writer w)
     instead.

  Revision 1.7  1998/10/04 18:00:31  rimassa
  Added a 'Log:' field to every source file.

*/

package examples.ex5;

// This agent allows a comprehensive testing of Agent Management
// System agent.

import java.io.*;

import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.*;

/**
Javadoc documentation for the file
@author Giovanni Rimassa - Università di Parma
@version $Date$ $Revision$
*/

public class amsTester extends Agent {

  private static class Receiver {

    // Utility class with private constructor -- do not instantiate.
    private Receiver() {
    }

    public static final ACLMessage receive(amsTester a, String messageType) {

      // Receive <messageType> message from peer

      MessageTemplate mt1 = MessageTemplate.MatchProtocol("fipa-request");
      MessageTemplate mt2 = MessageTemplate.MatchConversationId(a.getConvID());
      MessageTemplate mt3 = MessageTemplate.MatchSource("ams");
      MessageTemplate mt4 = MessageTemplate.MatchType(messageType);

      MessageTemplate mt12 = MessageTemplate.and(mt1, mt2);
      MessageTemplate mt34 = MessageTemplate.and(mt3, mt4);

      MessageTemplate mt = MessageTemplate.and(mt12, mt34);

      ACLMessage msg = a.receive(mt);

      return msg;

    }

  } // End of Receiver class


  // Used to generate conversation IDs.
  private int convCounter = 0;

  // Name of the action the AMS will be required to perform
  private String myAction;

  // Values for the various parameters of the request to AMS agent
  private String agentName = null;
  private String address = null;
  private String signature = null;
  private String APState = null;
  private String delegateAgent = null;
  private String forwardAddress = null;


  // Holds the current conversation ID.
  private String convID;

  private boolean receivedAgree = false;


  private abstract class ReceiveBehaviour extends SimpleBehaviour {

    protected boolean finished = false;

    protected ReceiveBehaviour() {
    }

    public abstract void action();

    public boolean done() {
      return finished;
    }
    
  } // End of ReceiveBehaviour class


  protected void setup() {

    int len = 0;
    byte[] buffer = new byte[1024];

    try {
      System.out.println("Enter AMS agent action to perform:");
      len = System.in.read(buffer);
      myAction = new String(buffer,0,len-1);

      System.out.println("Enter values for parameters (ENTER leaves them blank)");

      System.out.print(":agent-name ");
      len = System.in.read(buffer);
      agentName = new String(buffer,0,len-1);

      System.out.print(":address ");
      len = System.in.read(buffer);
      address = new String(buffer,0,len-1);

      System.out.print(":signature ");
      len = System.in.read(buffer);
      signature = new String(buffer,0,len-1);

      System.out.print(":ap-state ");
      len = System.in.read(buffer);
      APState = new String(buffer,0,len-1);

      System.out.print(":delegate-agent-name ");
      len = System.in.read(buffer);
      delegateAgent = new String(buffer,0,len-1);

      System.out.print(":forward-address ");
      len = System.in.read(buffer);
      forwardAddress = new String(buffer,0,len-1);

      System.out.println("");

    }
    catch(IOException ioe) {
      ioe.printStackTrace();
    }


    ComplexBehaviour mainBehaviour = new SequentialBehaviour(this);

    mainBehaviour.addSubBehaviour(new OneShotBehaviour(this) {

      public void action() {

	System.out.println("Sending 'request' message to AMS");

	sendRequest();

      }
    });

    ComplexBehaviour receive1stReply = NonDeterministicBehaviour.createWhenAny(this);

    receive1stReply.addSubBehaviour(new ReceiveBehaviour() {

      public void action() {

	ACLMessage msg = Receiver.receive(amsTester.this, "not-understood");
	if(msg != null)
	  dumpMessage(msg);
	finished = (msg != null);
      }

    });

    receive1stReply.addSubBehaviour(new ReceiveBehaviour() {

      public void action() {

	ACLMessage msg = Receiver.receive(amsTester.this,"refuse");
	if(msg != null)
	  dumpMessage(msg);
	finished = (msg != null);
      }

    });

    receive1stReply.addSubBehaviour(new ReceiveBehaviour() {

      public void action() {

	ACLMessage msg = Receiver.receive(amsTester.this,"agree");
	if(msg != null)
	  receiveAgree(msg);
	finished = (msg != null);
      }

    });

    // Nondeterministically receives not-understood, refuse or agree.
    mainBehaviour.addSubBehaviour(receive1stReply);

    // If agree is received, also receive inform or failure messages.
    mainBehaviour.addSubBehaviour(new OneShotBehaviour(this) {

      public void action() {
	if(agreed()) {
	  
	  ComplexBehaviour receiveAfterAgree = NonDeterministicBehaviour.createWhenAny(amsTester.this);
	  receiveAfterAgree.addSubBehaviour(new ReceiveBehaviour() {

	    public void action() {

	      ACLMessage msg = Receiver.receive(amsTester.this,"failure");
	      if(msg != null)
		handleFailure(msg);
	      finished = (msg != null);
	    }

	  });

	  receiveAfterAgree.addSubBehaviour(new ReceiveBehaviour() {

	    public void action() {

	      ACLMessage msg = Receiver.receive(amsTester.this,"inform");
	      if(msg != null)
		handleInform(msg);
	      finished = (msg != null);
	    }

	  });

	  // Schedules next behaviour for execution
	  myAgent.addBehaviour(receiveAfterAgree);
	}

	else
	  System.out.println("Protocol ended.");
      }

    });

    addBehaviour(mainBehaviour);

  } // End of setup()


  public String getAction() {
    return myAction;
  }

  public boolean agreed() {
    return receivedAgree;
  }

  private String newConvID() {
    String s = new String(getLocalName() + (new Integer(convCounter).toString()));
    ++convCounter;
    return s;
  } 

  public String getConvID() {
    return convID;
  }

  // Message handlers for various protocol steps.

  public void sendRequest() {

    convID = newConvID();

    String text = "( request " +
      "    :sender " + getLocalName() +
      "    :receiver ams" +
      "    :protocol fipa-request" +
      "    :ontology fipa-agent-management" +
      "    :language SL0" +
      "    :content  ( action ams ( " + myAction +
      "    ( :ams-description ";

    if(agentName.length() > 0)
      text = text.concat("( :agent-name " + agentName + " )");
    if(address.length() > 0)
      text = text.concat("( :address " + address + " )");
    if(signature.length() > 0)
      text = text.concat("( :signature " + signature + " )");
    if(APState.length() > 0)
      text = text.concat("( :ap-state " + APState + " )");
    if(forwardAddress.length() > 0)
      text = text.concat("( :forward-address" + forwardAddress + " )");
    if(delegateAgent.length() > 0)
      text = text.concat("( :delegate-agent-name" + delegateAgent + " )");

    text = text +
      " ) ) )" +
      " :conversation-id " + convID +
      ")";

    try {
      ACLMessage toSend = ACLMessage.fromText(new StringReader(text));
      send(toSend);

      System.out.println("[Agent.sendRequest()]\tRequest sent");
    }
    catch(jade.lang.acl.ParseException jlape) {
      jlape.printStackTrace();
    }


  }

  public void dumpMessage(ACLMessage msg) {
    System.out.println("[Agent.dumpMessage]\tReceived message:");
    msg.toText(new BufferedWriter(new OutputStreamWriter(System.out)));
  }
 
  public void receiveAgree(ACLMessage msg) {
    System.out.println("[Agent.receiveAgree]\tSuccess!!! Responder agreed to do action !");
    msg.toText(new BufferedWriter(new OutputStreamWriter(System.out)));
    receivedAgree = true;
  }

  public void handleFailure(ACLMessage msg) {
    System.out.println("Responder failed to process the request. Reason was:");
    System.out.println(msg.getContent());
    msg.toText(new BufferedWriter(new OutputStreamWriter(System.out)));
  }

  public void handleInform(ACLMessage msg) {
    System.out.println("Responder has just informed me that the action has been carried out.");
    msg.toText(new BufferedWriter(new OutputStreamWriter(System.out)));
  }

}

  
