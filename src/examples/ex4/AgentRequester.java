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

package examples.ex4;

// This agent plays the initiator role in fipa-request protocol.

import java.io.*;

import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.*;

/**
Javadoc documentation for the file
@author Giovanni Rimassa - Università di Parma
@version $Date$ $Revision$
*/

public class AgentRequester extends Agent {

  private static class Receiver {

    // Utility class with private constructor -- do not instantiate.
    private Receiver() {
    }

    public static final ACLMessage receive(AgentRequester a, String messageType) {

      // Receive <messageType> message from peer

      MessageTemplate mt1 = MessageTemplate.MatchProtocol("fipa-request");
      MessageTemplate mt2 = MessageTemplate.MatchConversationId(a.getConvID());
      MessageTemplate mt3 = MessageTemplate.MatchSource(a.getPeer());
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

  // This is the name of the peer agent, i.e. the one who plays
  // responder's role in fipa-request protocol.
  private String myPeer;

  private int howManyRequests;

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

    public void reset() {
      finished = false;
    }

  } // End of ReceiveBehaviour


  protected void setup() {

    int len = 0;
    byte[] buffer = new byte[1024];

    try {
      System.out.println("Enter responder agent name:");
      len = System.in.read(buffer);
      myPeer = new String(buffer,0,len-1);
    }
    catch(IOException ioe) {
      ioe.printStackTrace();
    }


    ComplexBehaviour mainBehaviour = new SequentialBehaviour(this) {

      protected void postAction() {
	reset();
      }

    };

    mainBehaviour.addSubBehaviour(new OneShotBehaviour(this) {

      public void action() {

	// Send a 'request' message to peer
	System.out.println("Sending 'request' message to: " + getPeer());
	receivedAgree = false;
	sendRequest();

      }
    });

    ComplexBehaviour receive1stReply = NonDeterministicBehaviour.createWhenAny(this);

    receive1stReply.addSubBehaviour(new ReceiveBehaviour() {

      public void action() {

	ACLMessage msg = Receiver.receive(AgentRequester.this, "not-understood");
	if(msg != null)
	  dumpMessage(msg);
        else block();
	finished = (msg != null);
      }

    });

    receive1stReply.addSubBehaviour(new ReceiveBehaviour() {

      public void action() {

	ACLMessage msg = Receiver.receive(AgentRequester.this, "refuse");
	if(msg != null)
	  dumpMessage(msg);
        else block();
	finished = (msg != null);
      }

    });

    receive1stReply.addSubBehaviour(new ReceiveBehaviour() {

      public void action() {

	ACLMessage msg = Receiver.receive(AgentRequester.this, "agree");
	if(msg != null)
	  receiveAgree(msg);
        else block();
	finished = (msg != null);
      }

    });

    // Nondeterministically receives not-understood, refuse or agree.
    mainBehaviour.addSubBehaviour(receive1stReply);

    // If agree is received, also receive inform or failure messages.
    mainBehaviour.addSubBehaviour(new OneShotBehaviour(this) {

      private ComplexBehaviour receiveAfterAgree;

      public void action() {
	if(agreed()) {

	  receiveAfterAgree = NonDeterministicBehaviour.createWhenAny(AgentRequester.this);
	  receiveAfterAgree.addSubBehaviour(new ReceiveBehaviour() {

	    public void action() {

	      ACLMessage msg = Receiver.receive(AgentRequester.this, "failure");
	      if(msg != null)
		handleFailure(msg);
              else block();
	      finished = (msg != null);
	    }

	  });

	  receiveAfterAgree.addSubBehaviour(new ReceiveBehaviour() {

	    public void action() {

	      ACLMessage msg = Receiver.receive(AgentRequester.this, "inform");
	      if(msg != null)
		handleInform(msg);
              else block();
	      finished = (msg != null);
	    }

	  });

	  // Schedules next behaviour for execution
	  parent.addSubBehaviour(receiveAfterAgree);
	}

	else
	  System.out.println("Protocol ended.");
      }

      public void reset() {
	if(agreed()) {
	  receivedAgree = false;
	  parent.removeSubBehaviour(receiveAfterAgree);
	  receiveAfterAgree = null;
	}
      }

    });

    addBehaviour(mainBehaviour);

  } // End of setup()


  public String getPeer() {
    return myPeer;
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
      "    :receiver " + myPeer +
      "    :protocol fipa-request" +
      "    :language \"Plain Text\"" +
      "    :content  ( \"Example Request\" )" +
      "    :conversation-id " + convID +
      ")";

    try {
      ACLMessage toSend = ACLMessage.fromText(new StringReader(text));
      send(toSend);
      System.out.println("[Agent.sendRequest()]\tRequest sent");
    }
    catch(jade.lang.acl.ParseException jlape) {
      System.out.println("ERROR: Parse exception caught during read.");
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

  
