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

package examples.ex5;

import jade.core.*;
import jade.core.behaviours.*;

import jade.lang.acl.ACLMessage;

/**
Javadoc documentation for the file
@author Giovanni Rimassa - Università di Parma
@version $Date$ $Revision$
*/

public class MobileAgent extends Agent {

  private ComplexBehaviour mainBehaviour = new SequentialBehaviour() {
    protected void postAction() {
      reset();
    }
  };
  private ACLMessage replyMsg = new ACLMessage(ACLMessage.INFORM);

  public void setup() {

    replyMsg.setContent("\"Hello there, I'm moving\"");

    mainBehaviour.addSubBehaviour(new OneShotBehaviour(this) {
      public void action() {
	System.out.println("First calculation step...");
	System.out.println("Waiting for a message...");
	ACLMessage msg = blockingReceive();
	System.out.println("OK. Now replying and moving...");
	replyMsg.removeAllDests();
	replyMsg.addDest(msg.getSource());
	send(replyMsg);
	doMove("Container-1");
      }
    });

    mainBehaviour.addSubBehaviour(new OneShotBehaviour(this) {
      public void action() {
	System.out.println("Second calculation step...");
	doMove("Container-2");
      }
    });

    mainBehaviour.addSubBehaviour(new OneShotBehaviour(this) {
      public void action() {
	System.out.println("Third calculation step...");
	System.out.println("Waiting for a message...");
	ACLMessage msg = blockingReceive();
	System.out.println("OK. Now replying and moving...");
	replyMsg.removeAllDests();
	replyMsg.addDest(msg.getSource());
	send(replyMsg);
	doMove("Front-End");
      }
    });

    /*
    mainBehaviour.addSubBehaviour(new OneShotBehaviour(this) {
      public void action() {
	System.out.println("Back home");
	System.out.println("Waiting for a message...");
	ACLMessage msg = blockingReceive();
	System.out.println("OK. Now replying and ending...");
	replyMsg.removeAllDests();
	replyMsg.addDest(msg.getSource());
	send(replyMsg);
	doDelete();
      }
    });
    */
    addBehaviour(mainBehaviour);

  }

  protected void beforeMove() {
    System.out.println("beforeMove() called.");
  }

  protected void afterMove() {
    System.out.println("afterMove() called.");
  }

}
