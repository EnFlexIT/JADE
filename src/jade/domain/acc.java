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


package jade.domain;

import java.io.StringReader;

import jade.core.*;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import jade.proto.FipaRequestResponderBehaviour;

/**
  Standard <em>Agent Communication Channel</em> agent. This class
  implements <em><b>FIPA</b></em> <em>ACC</em> agent. <b>JADE</b>
  applications cannot use this class directly, but interact with it
  through <em>ACL</em> message passing.
  
  
  @author Giovanni Rimassa - Universita` di Parma
  @version $Date$ $Revision$

*/
public class acc extends Agent {

  private class ACCBehaviour
    extends FipaRequestResponderBehaviour.Action
    implements FipaRequestResponderBehaviour.Factory {

    private AgentManagementOntology.ACCAction myAction;

    protected ACCBehaviour() {
      super(acc.this);
    }

    public FipaRequestResponderBehaviour.Action create() {
      return new ACCBehaviour();
    }

    public void action() {

      try {
	ACLMessage msg = getRequest();
	String content = msg.getContent();

	// Obtain an ACC action from message content
	try {
	  myAction = AgentManagementOntology.ACCAction.fromText(new StringReader(content));
	}
	catch(ParseException pe) {
	  pe.printStackTrace();
	  throw myOntology.getException(AgentManagementOntology.Exception.UNRECOGNIZEDATTR);
	}
	catch(TokenMgrError tme) {
	  tme.printStackTrace();
	  throw myOntology.getException(AgentManagementOntology.Exception.UNRECOGNIZEDATTR);
	}

	ACLMessage toForward = myAction.getArg();

	// Make sure destination agent is registered with platform AMS
	String destName = toForward.getDest();

	// Forward message
	send(toForward);

	// Acknowledge caller
	sendAgree();
	sendInform();
      }
      catch(FIPAException fe) {
	sendRefuse(fe.getMessage());
      }

    }

    public boolean done() {
      return true;
    }

    public void reset() {
    }

  } // End of ACCBehaviour class


  private AgentManagementOntology myOntology;
  private FipaRequestResponderBehaviour dispatcher;

  /**
     This constructor creates a new <em>ACC</em> agent. Since a direct
     reference to an Agent Platform implementation must be passed to
     it, this constructor cannot be called from application
     code. Therefore, no other <em>ACC</em> agent can be created
     beyond the default one.
  */
  public acc() {

    myOntology = AgentManagementOntology.instance();

    MessageTemplate mt = 
      MessageTemplate.and(MessageTemplate.MatchLanguage("SL0"),
			  MessageTemplate.MatchOntology("fipa-agent-management"));

    dispatcher = new FipaRequestResponderBehaviour(this, mt);

    // Associate each ACC action name with the behaviour to execute
    // when the action is requested in a 'request' ACL message
    dispatcher.registerFactory(AgentManagementOntology.ACCAction.FORWARD, new ACCBehaviour());

  }

  /**
   This method starts the <em>ACC</em> behaviours to allow the agent
   to carry on its duties within <em><b>JADE</b></em> agent platform.
  */
  protected void setup() {
    addBehaviour(dispatcher);
  }

}
